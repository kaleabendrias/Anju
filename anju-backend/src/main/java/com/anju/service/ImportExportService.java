package com.anju.service;

import com.anju.dto.ImportValidationResult;
import com.anju.dto.ImportValidationResult.FieldError;
import com.anju.entity.Appointment;
import com.anju.entity.Appointment.ServiceType;
import com.anju.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ImportExportService {

    private static final Logger log = LoggerFactory.getLogger(ImportExportService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMAT_ALT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String IMPORT_OPERATION = "APPOINTMENT_IMPORT";
    private static final String IMPORT_RECORD_OPERATION = "APPOINTMENT_IMPORT_RECORD";

    private final AppointmentService appointmentService;
    private final IdempotencyService idempotencyService;

    public ImportExportService(AppointmentService appointmentService, IdempotencyService idempotencyService) {
        this.appointmentService = appointmentService;
        this.idempotencyService = idempotencyService;
    }

    public ImportValidationResult validateAppointmentsCsv(MultipartFile file) {
        List<FieldError> errors = new ArrayList<>();
        List<Map<String, String>> validRecords = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new BusinessException("CSV file is empty or missing header row");
            }
            
            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].toLowerCase().trim(), i);
            }
            
            validateRequiredHeaders(headerIndex, errors);

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] values = parseCsvLine(line);
                
                if (values.length == 0 || (values.length == 1 && values[0].isBlank())) {
                    continue;
                }
                
                Map<String, String> record = parseRecord(headers, values);
                List<String> recordErrors = validateAppointmentRecord(record, headerIndex, lineNumber);
                
                if (recordErrors.isEmpty()) {
                    validRecords.add(record);
                } else {
                    for (String error : recordErrors) {
                        errors.add(new FieldError(lineNumber, "row", error));
                    }
                }
            }
            
        } catch (IOException e) {
            throw new BusinessException("Failed to read CSV file: " + e.getMessage());
        }

        return new ImportValidationResult(errors, validRecords, lineNumber);
    }

    @Transactional
    public ImportResult importAppointmentsCsv(MultipartFile file, String idempotencyKey, Long operatorId) {
        String effectiveIdempotencyKey = idempotencyKey != null ? idempotencyKey : generateImportIdempotencyKey(file);
        
        if (idempotencyService.isDuplicate(effectiveIdempotencyKey, IMPORT_OPERATION)) {
            log.info("Duplicate import detected: key={}", effectiveIdempotencyKey);
            ImportResult cached = idempotencyService.getCachedResult(
                    effectiveIdempotencyKey,
                    IMPORT_OPERATION,
                    ImportResult.class);
            if (cached != null) {
                cached.setFromCache(true);
                return cached;
            }
            return new ImportResult(0, 0, 0, "Duplicate import request detected", true);
        }

        ImportValidationResult validation = validateAppointmentsCsv(file);
        
        if (!validation.getErrors().isEmpty()) {
            ImportResult result = new ImportResult(0, 0, validation.getErrors().size(), 
                    "Validation failed with " + validation.getErrors().size() + " errors", false);
            result.setValidationErrors(validation.getErrors());
            return result;
        }

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;
        List<FieldError> errors = new ArrayList<>();

        for (Map<String, String> record : validation.getValidRecords()) {
            try {
                String recordIdempotencyKey = buildRecordIdempotencyKey(effectiveIdempotencyKey, record);
                
                if (idempotencyService.isDuplicate(recordIdempotencyKey, IMPORT_RECORD_OPERATION)) {
                    skipCount++;
                    log.debug("Skipping duplicate record: patient={}", record.get("patient_name"));
                    continue;
                }

                Appointment appointment = createAppointmentFromRecord(record, operatorId);
                appointmentService.createAppointment(appointment, operatorId);
                
                idempotencyService.recordOperation(recordIdempotencyKey, IMPORT_RECORD_OPERATION, appointment.getId());
                successCount++;
                
            } catch (Exception e) {
                errorCount++;
                String patientName = record.getOrDefault("patient_name", "unknown");
                errors.add(new FieldError(0, "import", "Failed to import record for patient '" + patientName + "': " + e.getMessage()));
                log.error("Import failed for patient: {}", patientName, e);
            }
        }

        ImportResult result = new ImportResult(successCount, skipCount, errorCount, 
                String.format("Import completed: %d success, %d skipped (duplicates), %d errors", 
                        successCount, skipCount, errorCount), false);
        result.setValidationErrors(errors);
        
        idempotencyService.recordOperation(effectiveIdempotencyKey, IMPORT_OPERATION, result);
        
        log.info("Import completed: key={}, success={}, skipped={}, errors={}", 
                effectiveIdempotencyKey, successCount, skipCount, errorCount);
        
        return result;
    }

    private String generateImportIdempotencyKey(MultipartFile file) {
        return "import_" + file.getOriginalFilename() + "_" + file.getSize() + "_" + 
               file.getContentType().hashCode();
    }

    private String buildRecordIdempotencyKey(String importKey, Map<String, String> record) {
        String patientName = record.getOrDefault("patient_name", "");
        String startTime = record.getOrDefault("start_time", "");
        String serviceType = record.getOrDefault("service_type", "");
        return importKey + "_" + patientName + "_" + startTime + "_" + serviceType;
    }

    public ImportResult getImportResult(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        
        ImportResult cached = idempotencyService.getCachedResult(idempotencyKey, IMPORT_OPERATION, ImportResult.class);
        if (cached != null) {
            cached.setFromCache(true);
            return cached;
        }

        return null;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndex, List<FieldError> errors) {
        List<String> requiredHeaders = List.of("service_type", "start_time", "end_time", "patient_name");
        
        for (String required : requiredHeaders) {
            if (!headerIndex.containsKey(required)) {
                errors.add(new FieldError(1, "header", "Missing required column: " + required));
            }
        }
    }

    private List<String> validateAppointmentRecord(Map<String, String> record, Map<String, Integer> headerIndex, int lineNumber) {
        List<String> errors = new ArrayList<>();

        String serviceType = record.get("service_type");
        if (serviceType == null || serviceType.isBlank()) {
            errors.add("service_type is required");
        } else {
            try {
                ServiceType.valueOf(serviceType.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid service_type '" + serviceType + "'. Valid values: QUICK_CONSULTATION, STANDARD_CONSULTATION, EXTENDED_CONSULTATION, COMPREHENSIVE_REVIEW");
            }
        }

        String startTimeStr = record.get("start_time");
        if (startTimeStr == null || startTimeStr.isBlank()) {
            errors.add("start_time is required");
        } else {
            try {
                parseDateTime(startTimeStr);
            } catch (Exception e) {
                errors.add("Invalid start_time format. Expected: yyyy-MM-dd HH:mm:ss or yyyy-MM-ddTHH:mm:ss");
            }
        }

        String endTimeStr = record.get("end_time");
        if (endTimeStr == null || endTimeStr.isBlank()) {
            errors.add("end_time is required");
        } else {
            try {
                parseDateTime(endTimeStr);
            } catch (Exception e) {
                errors.add("Invalid end_time format. Expected: yyyy-MM-dd HH:mm:ss or yyyy-MM-ddTHH:mm:ss");
            }
        }

        if (startTimeStr != null && !startTimeStr.isBlank() && 
            endTimeStr != null && !endTimeStr.isBlank()) {
            try {
                LocalDateTime startTime = parseDateTime(startTimeStr);
                LocalDateTime endTime = parseDateTime(endTimeStr);
                
                if (!endTime.isAfter(startTime)) {
                    errors.add("end_time must be after start_time");
                }
                
                long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
                Set<Integer> standardDurations = Set.of(15, 30, 60, 90);
                if (!standardDurations.contains((int) minutes)) {
                    errors.add("Invalid duration: " + minutes + " minutes. Standard durations: 15, 30, 60, 90");
                }
            } catch (Exception ignored) {
            }
        }

        String patientName = record.get("patient_name");
        if (patientName == null || patientName.isBlank()) {
            errors.add("patient_name is required");
        } else if (patientName.length() > 100) {
            errors.add("patient_name exceeds maximum length of 100 characters");
        }

        String orderAmountStr = record.get("order_amount");
        if (orderAmountStr != null && !orderAmountStr.isBlank()) {
            try {
                new BigDecimal(orderAmountStr);
            } catch (NumberFormatException e) {
                errors.add("Invalid order_amount format. Expected numeric value");
            }
        }

        return errors;
    }

    private Appointment createAppointmentFromRecord(Map<String, String> record, Long operatorId) {
        Appointment appointment = new Appointment();
        appointment.setServiceType(ServiceType.valueOf(record.get("service_type").toUpperCase().trim()));
        appointment.setStartTime(parseDateTime(record.get("start_time")));
        appointment.setEndTime(parseDateTime(record.get("end_time")));
        appointment.setPatientName(record.get("patient_name"));
        
        if (record.containsKey("order_amount") && !record.get("order_amount").isBlank()) {
            appointment.setOrderAmount(new BigDecimal(record.get("order_amount")));
        }
        
        if (record.containsKey("notes") && !record.get("notes").isBlank()) {
            appointment.setNotes(record.get("notes"));
        }
        
        if (record.containsKey("accompanying_staff_id") && !record.get("accompanying_staff_id").isBlank()) {
            appointment.setAccompanyingStaffId(Long.parseLong(record.get("accompanying_staff_id")));
        }
        
        if (record.containsKey("resource_id") && !record.get("resource_id").isBlank()) {
            appointment.setResourceId(Long.parseLong(record.get("resource_id")));
        }
        
        appointment.setOperatorId(operatorId);
        appointment.setIdempotencyKey(UUID.randomUUID().toString());
        
        return appointment;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) throws DateTimeParseException {
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMAT_ALT);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMAT);
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        
        return result.toArray(new String[0]);
    }

    private Map<String, String> parseRecord(String[] headers, String[] values) {
        Map<String, String> record = new LinkedHashMap<>();
        for (int i = 0; i < headers.length && i < values.length; i++) {
            record.put(headers[i].toLowerCase().trim(), values[i]);
        }
        return record;
    }

    public String exportAppointmentsToCsv(List<Appointment> appointments) {
        StringBuilder csv = new StringBuilder();
        
        csv.append("id,service_type,patient_name,start_time,end_time,duration_minutes,order_amount,status,");
        csv.append("reschedule_count,penalty_amount,created_at\n");
        
        for (Appointment apt : appointments) {
            csv.append(escapeCsvField(String.valueOf(apt.getId()))).append(",");
            csv.append(escapeCsvField(apt.getServiceType() != null ? apt.getServiceType().name() : "")).append(",");
            csv.append(escapeCsvField(apt.getPatientName() != null ? apt.getPatientName() : "")).append(",");
            csv.append(escapeCsvField(apt.getStartTime() != null ? apt.getStartTime().format(DATETIME_FORMAT) : "")).append(",");
            csv.append(escapeCsvField(apt.getEndTime() != null ? apt.getEndTime().format(DATETIME_FORMAT) : "")).append(",");
            csv.append(apt.getDurationMinutes()).append(",");
            csv.append(escapeCsvField(apt.getOrderAmount() != null ? apt.getOrderAmount().toPlainString() : "")).append(",");
            csv.append(escapeCsvField(apt.getStatus() != null ? apt.getStatus().name() : "")).append(",");
            csv.append(apt.getRescheduleCount() != null ? apt.getRescheduleCount() : 0).append(",");
            csv.append(escapeCsvField(apt.getPenaltyAmount() != null ? apt.getPenaltyAmount().toPlainString() : "")).append(",");
            csv.append(escapeCsvField(apt.getCreatedAt() != null ? apt.getCreatedAt().format(DATETIME_FORMAT) : "")).append("\n");
        }
        
        return csv.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    public static class ImportResult {
        private int successCount;
        private int skipCount;
        private int errorCount;
        private String message;
        private boolean fromCache;
        private List<FieldError> validationErrors;

        public ImportResult() {
        }

        public ImportResult(int successCount, int skipCount, int errorCount, String message, boolean fromCache) {
            this.successCount = successCount;
            this.skipCount = skipCount;
            this.errorCount = errorCount;
            this.message = message;
            this.fromCache = fromCache;
        }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isFromCache() { return fromCache; }
        public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }
        public List<FieldError> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<FieldError> validationErrors) { this.validationErrors = validationErrors; }
    }
}

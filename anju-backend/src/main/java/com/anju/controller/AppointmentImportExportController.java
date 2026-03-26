package com.anju.controller;

import com.anju.dto.ApiResponse;
import com.anju.dto.ImportValidationResult;
import com.anju.entity.Appointment;
import com.anju.security.UserPrincipal;
import com.anju.service.AppointmentService;
import com.anju.service.ImportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/appointments")
public class AppointmentImportExportController {

    private final ImportExportService importExportService;
    private final AppointmentService appointmentService;

    public AppointmentImportExportController(ImportExportService importExportService,
                                            AppointmentService appointmentService) {
        this.importExportService = importExportService;
        this.appointmentService = appointmentService;
    }

    @PostMapping(value = "/import/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<ImportValidationResult>> validateImportFile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }
        validateCsvFile(file);

        ImportValidationResult result = importExportService.validateAppointmentsCsv(file);
        return ResponseEntity.ok(ApiResponse.success("Validation completed", result));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<ImportExportService.ImportResult>> importAppointments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }
        validateCsvFile(file);

        ImportExportService.ImportResult result = importExportService.importAppointmentsCsv(file, idempotencyKey, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }

    @GetMapping("/import/result/{idempotencyKey}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<ImportExportService.ImportResult>> getImportResult(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String idempotencyKey) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        ImportExportService.ImportResult result = importExportService.getImportResult(idempotencyKey);
        if (result == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Import result not found"));
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<byte[]> exportAppointments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Appointment.AppointmentStatus status) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        List<Appointment> appointments = appointmentService.getVisibleAppointmentsForExport(
                principal.getId(), principal.getRole(), status);
        String csv = importExportService.exportAppointmentsToCsv(appointments);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "appointments_" + timestamp + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    private void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are supported");
        }
    }
}

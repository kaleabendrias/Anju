package com.anju.service;

import com.anju.dto.ImportValidationResult;
import com.anju.entity.Appointment;
import com.anju.entity.Appointment.ServiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportExportServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private IdempotencyService idempotencyService;

    private ImportExportService importExportService;

    @BeforeEach
    void setUp() {
        importExportService = new ImportExportService(appointmentService, idempotencyService);
    }

    @Nested
    @DisplayName("CSV Validation Tests")
    class CsvValidationTests {

        @Test
        @DisplayName("Should validate valid appointment CSV")
        void shouldValidateValidCsv() {
            String csv = """
                    service_type,start_time,end_time,patient_name,order_amount
                    STANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,John Doe,100.00
                    QUICK_CONSULTATION,2026-04-01 11:00:00,2026-04-01 11:15:00,Jane Smith,50.00
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertFalse(result.hasErrors());
            assertEquals(2, result.getValidCount());
            assertEquals(2, result.getTotalRows());
        }

        @Test
        @DisplayName("Should reject CSV missing required headers")
        void shouldRejectMissingHeaders() {
            String csv = """
                    patient_name,start_time
                    John Doe,2026-04-01 10:00:00
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.getMessage().contains("Missing required column")));
        }

        @Test
        @DisplayName("Should reject invalid service type")
        void shouldRejectInvalidServiceType() {
            String csv = """
                    service_type,start_time,end_time,patient_name
                    INVALID_TYPE,2026-04-01 10:00:00,2026-04-01 10:30:00,John Doe
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.getMessage().contains("Invalid service_type")));
        }

        @Test
        @DisplayName("Should reject invalid duration")
        void shouldRejectInvalidDuration() {
            String csv = """
                    service_type,start_time,end_time,patient_name
                    STANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:45:00,John Doe
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.getMessage().contains("Standard durations")));
        }

        @Test
        @DisplayName("Should reject missing patient name")
        void shouldRejectMissingPatientName() {
            String csv = """
                    service_type,start_time,end_time,patient_name
                    STANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.getMessage().contains("patient_name is required")));
        }

        @Test
        @DisplayName("Should reject end time before start time")
        void shouldRejectEndTimeBeforeStartTime() {
            String csv = """
                    service_type,start_time,end_time,patient_name
                    STANDARD_CONSULTATION,2026-04-01 10:30:00,2026-04-01 10:00:00,John Doe
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.getMessage().contains("end_time must be after start_time")));
        }

        @Test
        @DisplayName("Should validate ISO datetime format")
        void shouldValidateIsoDatetimeFormat() {
            String csv = """
                    service_type,start_time,end_time,patient_name
                    STANDARD_CONSULTATION,2026-04-01T10:00:00,2026-04-01T10:30:00,John Doe
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertFalse(result.hasErrors());
        }

        @Test
        @DisplayName("Should calculate success rate correctly")
        void shouldCalculateSuccessRateCorrectly() {
            String csv = """
                    service_type,start_time,end_time,patient_name
                    STANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,John Doe
                    INVALID_TYPE,2026-04-01 11:00:00,2026-04-01 11:30:00,Jane Doe
                    STANDARD_CONSULTATION,2026-04-01 12:00:00,2026-04-01 12:30:00,Bob Smith
                    """;

            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportValidationResult result = importExportService.validateAppointmentsCsv(file);

            assertEquals(2, result.getValidCount());
            assertEquals(3, result.getTotalRows());
            assertEquals(66.67, result.getSuccessRate(), 0.01);
        }
    }

    @Nested
    @DisplayName("CSV Export Tests")
    class CsvExportTests {

        @Test
        @DisplayName("Should export appointments to CSV")
        void shouldExportAppointmentsToCsv() {
            List<Appointment> appointments = List.of(
                    createTestAppointment(1L, ServiceType.STANDARD_CONSULTATION, "John Doe"),
                    createTestAppointment(2L, ServiceType.QUICK_CONSULTATION, "Jane Smith")
            );

            String csv = importExportService.exportAppointmentsToCsv(appointments);

            assertNotNull(csv);
            assertTrue(csv.contains("service_type"));
            assertTrue(csv.contains("patient_name"));
            assertTrue(csv.contains("John Doe"));
            assertTrue(csv.contains("Jane Smith"));
            assertTrue(csv.contains("STANDARD_CONSULTATION"));
            assertTrue(csv.contains("QUICK_CONSULTATION"));
        }

        @Test
        @DisplayName("Should escape CSV special characters")
        void shouldEscapeCsvSpecialCharacters() {
            List<Appointment> appointments = List.of(
                    createTestAppointment(1L, ServiceType.STANDARD_CONSULTATION, "Doe, John")
            );

            String csv = importExportService.exportAppointmentsToCsv(appointments);

            assertTrue(csv.contains("\"Doe, John\""));
        }
    }

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should return cached import result for duplicate request")
        void shouldReturnCachedImportResultForDuplicate() {
            String csv = "service_type,start_time,end_time,patient_name\n" +
                    "STANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,John Doe\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            ImportExportService.ImportResult cached = new ImportExportService.ImportResult(1, 0, 0, "cached", false);
            when(idempotencyService.isDuplicate("same-key", "APPOINTMENT_IMPORT")).thenReturn(true);
            when(idempotencyService.getCachedResult("same-key", "APPOINTMENT_IMPORT", ImportExportService.ImportResult.class))
                    .thenReturn(cached);

            ImportExportService.ImportResult result = importExportService.importAppointmentsCsv(file, "same-key", 1L);

            assertTrue(result.isFromCache());
            assertEquals(1, result.getSuccessCount());
        }

        @Test
        @DisplayName("Should persist import result via idempotency service")
        void shouldPersistImportResultViaIdempotencyService() {
            String csv = "service_type,start_time,end_time,patient_name\n" +
                    "STANDARD_CONSULTATION,2026-04-01 10:00:00,2026-04-01 10:30:00,John Doe\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "appointments.csv", "text/csv", csv.getBytes());

            when(idempotencyService.isDuplicate(any(), eq("APPOINTMENT_IMPORT"))).thenReturn(false);
            when(idempotencyService.isDuplicate(any(), eq("APPOINTMENT_IMPORT_RECORD"))).thenReturn(false);

            importExportService.importAppointmentsCsv(file, "persist-key", 1L);

            verify(idempotencyService).recordOperation(eq("persist-key"), eq("APPOINTMENT_IMPORT"), any());
        }
    }

    private Appointment createTestAppointment(Long id, ServiceType serviceType, String patientName) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setServiceType(serviceType);
        appointment.setPatientName(patientName);
        appointment.setStartTime(LocalDateTime.now().plusDays(1));
        appointment.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(serviceType.getDurationMinutes()));
        appointment.setOrderAmount(new BigDecimal("100.00"));
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment.setRescheduleCount(0);
        appointment.setCreatedAt(LocalDateTime.now());
        return appointment;
    }
}

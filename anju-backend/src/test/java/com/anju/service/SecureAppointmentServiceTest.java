package com.anju.service;

import com.anju.dto.*;
import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.entity.Appointment.ServiceType;
import com.anju.exception.ForbiddenException;
import com.anju.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecureAppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private LocalDateTime futureStartTime;
    private LocalDateTime futureEndTime;

    @BeforeEach
    void setUp() {
        futureStartTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        futureEndTime = futureStartTime.plusMinutes(30);

        testAppointment = Appointment.builder()
                .id(1L)
                .serviceType(ServiceType.STANDARD_CONSULTATION)
                .orderAmount(new BigDecimal("500.00"))
                .status(AppointmentStatus.PENDING)
                .startTime(futureStartTime)
                .endTime(futureEndTime)
                .accompanyingStaffId(100L)
                .resourceId(200L)
                .patientName("Test Patient")
                .rescheduleCount(0)
                .operatorId(1L)
                .build();
    }

    @Nested
    @DisplayName("Object Authorization Tests")
    class ObjectAuthorizationTests {

        @Test
        @DisplayName("Admin should be able to cancel any appointment")
        void adminShouldCancelAnyAppointment() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(
                    1L, 999L, "ADMIN", "Admin cancelled");

            assertNotNull(response);
            assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
        }

        @Test
        @DisplayName("Owner should be able to cancel their own appointment")
        void ownerShouldCancelOwnAppointment() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(
                    1L, 1L, "FRONTLINE", "Customer requested cancellation");

            assertNotNull(response);
            assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
        }

        @Test
        @DisplayName("Non-owner should be denied cancellation")
        void nonOwnerShouldBeDenied() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            assertThrows(ForbiddenException.class, () ->
                    appointmentService.cancelAppointment(1L, 999L, "FRONTLINE", "Test"));
        }

        @Test
        @DisplayName("Admin should access any appointment")
        void adminShouldAccessAnyAppointment() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            AppointmentResponse response = appointmentService.getAppointmentById(1L, 999L, "ADMIN");

            assertNotNull(response);
        }

        @Test
        @DisplayName("Non-owner should be denied viewing")
        void nonOwnerShouldBeDeniedViewing() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

            assertThrows(ForbiddenException.class, () ->
                    appointmentService.getAppointmentById(1L, 999L, "FRONTLINE"));
        }
    }

    @Nested
    @DisplayName("Penalty Calculation Tests")
    class PenaltyCalculationTests {

        @Test
        @DisplayName("Should calculate penalty within 24 hours")
        void shouldCalculatePenaltyWithin24Hours() {
            testAppointment.setStartTime(LocalDateTime.now().plusHours(12));
            testAppointment.setOrderAmount(new BigDecimal("500.00"));

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(
                    1L, 1L, "ADMIN", "Test cancellation");

            assertEquals(new BigDecimal("50.00"), response.getPenaltyAmount());
        }

        @Test
        @DisplayName("Should use 10% when less than 50 RMB cap")
        void shouldUseTenPercentWhenLessThanCap() {
            testAppointment.setStartTime(LocalDateTime.now().plusHours(12));
            testAppointment.setOrderAmount(new BigDecimal("100.00"));

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(
                    1L, 1L, "ADMIN", "Test cancellation");

            assertEquals(new BigDecimal("10.00"), response.getPenaltyAmount());
        }

        @Test
        @DisplayName("Should return zero penalty when more than 24 hours")
        void shouldReturnZeroPenaltyWhenMoreThan24Hours() {
            testAppointment.setStartTime(LocalDateTime.now().plusHours(48));
            testAppointment.setOrderAmount(new BigDecimal("500.00"));

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(
                    1L, 1L, "ADMIN", "Test cancellation");

            assertEquals(BigDecimal.ZERO, response.getPenaltyAmount());
        }
    }

    @Nested
    @DisplayName("Audit Log Tests")
    class AuditLogTests {

        @Test
        @DisplayName("Should create audit log on cancellation")
        void shouldCreateAuditLogOnCancellation() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            appointmentService.cancelAppointment(1L, 1L, "ADMIN", "Test audit log");

            verify(auditLogService).logOperation(
                    eq(1L),
                    eq("APPOINTMENT"),
                    eq("CANCEL"),
                    eq(1L),
                    any(),
                    any(),
                    any(),
                    any()
            );
        }

        @Test
        @DisplayName("Should create audit log on creation")
        void shouldCreateAuditLogOnCreation() {
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.STANDARD_CONSULTATION)
                    .startTime(futureStartTime)
                    .endTime(futureEndTime)
                    .patientName("New Patient")
                    .build();

            when(appointmentRepository.findConflictingStaffAppointmentsExcluding(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(appointmentRepository.findConflictingResourceAppointmentsExcluding(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

            appointmentService.createAppointment(request, 1L, "ADMIN");

            verify(auditLogService).logOperation(
                    any(),
                    eq("APPOINTMENT"),
                    eq("CREATE"),
                    eq(1L),
                    any(),
                    any(),
                    any(),
                    any()
            );
        }
    }
}

package com.anju.service;

import com.anju.dto.AppointmentCreateRequest;
import com.anju.dto.AppointmentResponse;
import com.anju.dto.AppointmentRescheduleRequest;
import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.entity.Appointment.ServiceType;
import com.anju.exception.BusinessException;
import com.anju.exception.ForbiddenException;
import com.anju.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AuditLogService auditLogService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, auditLogService);
    }

    @Nested
    @DisplayName("Service Type Duration Validation Tests")
    class ServiceTypeDurationTests {

        @Test
        @DisplayName("Should create appointment with valid standard duration (15 min)")
        void shouldCreateAppointmentWith15MinuteDuration() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(15);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.QUICK_CONSULTATION)
                    .patientName("John Doe")
                    .startTime(startTime)
                    .endTime(endTime)
                    .orderAmount(new BigDecimal("100.00"))
                    .build();

            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            AppointmentResponse response = appointmentService.createAppointment(request, 1L, "ADMIN");

            assertNotNull(response);
            assertEquals(ServiceType.QUICK_CONSULTATION, response.getServiceType());
            assertEquals(15, response.getDurationMinutes());
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should create appointment with valid standard duration (30 min)")
        void shouldCreateAppointmentWith30MinuteDuration() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(30);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.STANDARD_CONSULTATION)
                    .patientName("Jane Doe")
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            AppointmentResponse response = appointmentService.createAppointment(request, 1L, "ADMIN");

            assertEquals(ServiceType.STANDARD_CONSULTATION, response.getServiceType());
            assertEquals(30, response.getDurationMinutes());
        }

        @Test
        @DisplayName("Should create appointment with valid standard duration (60 min)")
        void shouldCreateAppointmentWith60MinuteDuration() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(60);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.EXTENDED_CONSULTATION)
                    .patientName("Bob Smith")
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            AppointmentResponse response = appointmentService.createAppointment(request, 1L, "ADMIN");

            assertEquals(ServiceType.EXTENDED_CONSULTATION, response.getServiceType());
            assertEquals(60, response.getDurationMinutes());
        }

        @Test
        @DisplayName("Should create appointment with valid standard duration (90 min)")
        void shouldCreateAppointmentWith90MinuteDuration() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(90);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.COMPREHENSIVE_REVIEW)
                    .patientName("Alice Johnson")
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            AppointmentResponse response = appointmentService.createAppointment(request, 1L, "ADMIN");

            assertEquals(ServiceType.COMPREHENSIVE_REVIEW, response.getServiceType());
            assertEquals(90, response.getDurationMinutes());
        }

        @Test
        @DisplayName("Should reject appointment with non-standard duration (45 min)")
        void shouldRejectNonStandardDuration() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(45);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.STANDARD_CONSULTATION)
                    .patientName("John Doe")
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    appointmentService.createAppointment(request, 1L, "ADMIN"));

            assertTrue(exception.getMessage().contains("Standard durations"));
        }

        @Test
        @DisplayName("Should reject appointment with mismatched service type and duration")
        void shouldRejectMismatchedServiceTypeAndDuration() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(30);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.QUICK_CONSULTATION)
                    .patientName("John Doe")
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    appointmentService.createAppointment(request, 1L, "ADMIN"));

            assertTrue(exception.getMessage().contains("does not match"));
        }
    }

    @Nested
    @DisplayName("24-Hour Advance Policy Tests")
    class AdvancePolicyTests {

        @Test
        @DisplayName("Should apply penalty when cancelling within 24 hours")
        void shouldApplyPenaltyWhenCancellingWithin24Hours() {
            LocalDateTime startTime = LocalDateTime.now().plusHours(12);
            Appointment appointment = createTestAppointment(startTime, AppointmentStatus.CONFIRMED);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(1L, 1L, "FRONTLINE", "Emergency");

            assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
            assertNotNull(response.getPenaltyAmount());
            assertTrue(response.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(response.getPenaltyReason());
            assertTrue(response.getPenaltyReason().contains("CANCEL"));
        }

        @Test
        @DisplayName("Should not apply penalty when cancelling with > 24 hours advance")
        void shouldNotApplyPenaltyWith24HourAdvance() {
            LocalDateTime startTime = LocalDateTime.now().plusHours(25);
            Appointment appointment = createTestAppointment(startTime, AppointmentStatus.CONFIRMED);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AppointmentResponse response = appointmentService.cancelAppointment(1L, 1L, "FRONTLINE", "Schedule conflict");

            assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
            assertEquals(BigDecimal.ZERO, response.getPenaltyAmount());
        }

        @Test
        @DisplayName("Should apply penalty when rescheduling within 24 hours")
        void shouldApplyPenaltyWhenReschedulingWithin24Hours() {
            LocalDateTime oldStartTime = LocalDateTime.now().plusHours(12);
            Appointment appointment = createTestAppointment(oldStartTime, AppointmentStatus.CONFIRMED);
            appointment.setRescheduleCount(0);

            LocalDateTime newStartTime = LocalDateTime.now().plusHours(48);
            LocalDateTime newEndTime = newStartTime.plusMinutes(30);

            AppointmentRescheduleRequest request = AppointmentRescheduleRequest.builder()
                    .newStartTime(newStartTime)
                    .newEndTime(newEndTime)
                    .build();

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.findConflictingStaffAppointmentsExcluding(any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AppointmentResponse response = appointmentService.rescheduleAppointment(1L, request, 1L, "FRONTLINE");

            assertEquals(1, response.getRescheduleCount());
            assertNotNull(response.getPenaltyAmount());
        }
    }

    @Nested
    @DisplayName("Max Reschedule Limit Tests")
    class MaxRescheduleTests {

        @Test
        @DisplayName("Should reject reschedule when max limit (2) reached")
        void shouldRejectRescheduleWhenMaxLimitReached() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(2);
            Appointment appointment = createTestAppointment(startTime, AppointmentStatus.CONFIRMED);
            appointment.setRescheduleCount(2);

            LocalDateTime newStartTime = LocalDateTime.now().plusDays(3);
            LocalDateTime newEndTime = newStartTime.plusMinutes(30);

            AppointmentRescheduleRequest request = AppointmentRescheduleRequest.builder()
                    .newStartTime(newStartTime)
                    .newEndTime(newEndTime)
                    .build();

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    appointmentService.rescheduleAppointment(1L, request, 1L, "FRONTLINE"));

            assertTrue(exception.getMessage().contains("Maximum reschedule limit"));
        }

        @Test
        @DisplayName("Should allow reschedule up to max limit")
        void shouldAllowRescheduleUpToMaxLimit() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(3);
            Appointment appointment = createTestAppointment(startTime, AppointmentStatus.CONFIRMED);
            appointment.setRescheduleCount(1);

            LocalDateTime newStartTime = LocalDateTime.now().plusDays(4);
            LocalDateTime newEndTime = newStartTime.plusMinutes(30);

            AppointmentRescheduleRequest request = AppointmentRescheduleRequest.builder()
                    .newStartTime(newStartTime)
                    .newEndTime(newEndTime)
                    .build();

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.findConflictingStaffAppointmentsExcluding(any(), any(), any(), any()))
                    .thenReturn(List.of());
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            AppointmentResponse response = appointmentService.rescheduleAppointment(1L, request, 1L, "FRONTLINE");

            assertEquals(2, response.getRescheduleCount());
        }
    }

    @Nested
    @DisplayName("Conflict Detection Tests")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Should detect staff conflict for new appointment")
        void shouldDetectStaffConflict() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(30);
            
            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.STANDARD_CONSULTATION)
                    .patientName("John Doe")
                    .startTime(startTime)
                    .endTime(endTime)
                    .accompanyingStaffId(100L)
                    .build();

            Appointment existingAppointment = createTestAppointment(startTime, AppointmentStatus.CONFIRMED);
            existingAppointment.setAccompanyingStaffId(100L);

            when(appointmentRepository.findConflictingStaffAppointments(eq(100L), any(), any()))
                    .thenReturn(List.of(existingAppointment));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    appointmentService.createAppointment(request, 1L, "ADMIN"));

            assertTrue(exception.getMessage().contains("conflicting appointments"));
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should allow admin to access any appointment")
        void shouldAllowAdminAccessAnyAppointment() {
            Appointment appointment = createTestAppointment(
                    LocalDateTime.now().plusDays(1), AppointmentStatus.PENDING);
            appointment.setOperatorId(999L);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertDoesNotThrow(() ->
                    appointmentService.getAppointmentById(1L, 1L, "ADMIN"));
        }

        @Test
        @DisplayName("Should allow owner to access their own appointment")
        void shouldAllowOwnerAccessOwnAppointment() {
            Appointment appointment = createTestAppointment(
                    LocalDateTime.now().plusDays(1), AppointmentStatus.PENDING);
            appointment.setOperatorId(5L);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertDoesNotThrow(() ->
                    appointmentService.getAppointmentById(1L, 5L, "FRONTLINE"));
        }

        @Test
        @DisplayName("Should reject non-owner from accessing appointment")
        void shouldRejectNonOwnerAccess() {
            Appointment appointment = createTestAppointment(
                    LocalDateTime.now().plusDays(1), AppointmentStatus.PENDING);
            appointment.setOperatorId(999L);

            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(ForbiddenException.class, () ->
                    appointmentService.getAppointmentById(1L, 5L, "FRONTLINE"));
        }
    }

    @Nested
    @DisplayName("Auto-Cancel Tests")
    class AutoCancelTests {

        @Test
        @DisplayName("Should auto-cancel stale pending appointments")
        void shouldAutoCancelStalePendingAppointments() {
            Appointment staleAppointment = createTestAppointment(
                    LocalDateTime.now().minusMinutes(20), AppointmentStatus.PENDING);
            staleAppointment.setId(1L);
            staleAppointment.setNotes(null);

            when(appointmentRepository.findStaleAppointments(eq(AppointmentStatus.PENDING), any()))
                    .thenReturn(List.of(staleAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            appointmentService.autoCancelStaleAppointments();

            ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(captor.capture());
            
            Appointment saved = captor.getValue();
            assertEquals(AppointmentStatus.CANCELLED, saved.getStatus());
            assertTrue(saved.getNotes().contains("Auto-cancelled"));
        }
    }

    private Appointment createTestAppointment(LocalDateTime startTime, AppointmentStatus status) {
        return Appointment.builder()
                .id(1L)
                .serviceType(ServiceType.STANDARD_CONSULTATION)
                .patientName("Test Patient")
                .startTime(startTime)
                .endTime(startTime.plusMinutes(30))
                .status(status)
                .orderAmount(new BigDecimal("100.00"))
                .operatorId(1L)
                .rescheduleCount(0)
                .build();
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should block simultaneous booking attempts for same resource")
        void shouldBlockSimultaneousBookingForSameResource() throws InterruptedException {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(30);

            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.STANDARD_CONSULTATION)
                    .patientName("Concurrent Patient")
                    .startTime(startTime)
                    .endTime(endTime)
                    .resourceId(100L)
                    .accompanyingStaffId(1L)
                    .orderAmount(new BigDecimal("100.00"))
                    .build();

            when(appointmentRepository.findConflictingStaffAppointments(any(), any(), any()))
                    .thenReturn(List.of());
            when(appointmentRepository.findConflictingResourceAppointments(any(), any(), any()))
                    .thenReturn(List.of());
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger conflictCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        AppointmentCreateRequest threadRequest = AppointmentCreateRequest.builder()
                                .serviceType(ServiceType.STANDARD_CONSULTATION)
                                .patientName("Concurrent Patient " + threadId)
                                .startTime(startTime.plusMinutes(threadId * 5))
                                .endTime(startTime.plusMinutes(threadId * 5 + 30))
                                .resourceId(100L)
                                .accompanyingStaffId(1L)
                                .orderAmount(new BigDecimal("100.00"))
                                .idempotencyKey("idem_" + UUID.randomUUID())
                                .build();

                        appointmentService.createAppointment(threadRequest, 1L, "ADMIN");
                        successCount.incrementAndGet();
                    } catch (BusinessException e) {
                        if (e.getMessage().contains("conflicting")) {
                            conflictCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertEquals(1, successCount.get() + conflictCount.get());
        }

        @Test
        @DisplayName("Should handle idempotent concurrent appointment creation")
        void shouldHandleIdempotentConcurrentCreation() throws InterruptedException {
            LocalDateTime startTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
            LocalDateTime endTime = startTime.plusMinutes(30);
            String idempotencyKey = "concurrent_idem_" + UUID.randomUUID();

            AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                    .serviceType(ServiceType.STANDARD_CONSULTATION)
                    .patientName("Idempotent Patient")
                    .startTime(startTime)
                    .endTime(endTime)
                    .resourceId(200L)
                    .accompanyingStaffId(1L)
                    .orderAmount(new BigDecimal("100.00"))
                    .idempotencyKey(idempotencyKey)
                    .build();

            when(appointmentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.empty());
            when(appointmentRepository.findConflictingStaffAppointments(any(), any(), any()))
                    .thenReturn(List.of());
            when(appointmentRepository.findConflictingResourceAppointments(any(), any(), any()))
                    .thenReturn(List.of());
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment apt = invocation.getArgument(0);
                apt.setId(1L);
                return apt;
            });

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        appointmentService.createAppointment(request, 1L, "ADMIN");
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertEquals(1, successCount.get());
            verify(appointmentRepository, times(1)).save(any(Appointment.class));
        }
    }
}

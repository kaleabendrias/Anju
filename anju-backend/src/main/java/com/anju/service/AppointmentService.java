package com.anju.service;

import com.anju.dto.AppointmentCreateRequest;
import com.anju.dto.AppointmentResponse;
import com.anju.dto.AppointmentRescheduleRequest;
import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.entity.Appointment.ServiceType;
import com.anju.exception.BusinessException;
import com.anju.exception.ForbiddenException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    private static final BigDecimal PENALTY_MIN_AMOUNT = new BigDecimal("50.00");
    private static final BigDecimal PENALTY_PERCENTAGE = new BigDecimal("0.10");
    private static final int MAX_RESCHEDULE_COUNT = 2;
    private static final int AUTO_CANCEL_MINUTES = 15;
    private static final int ADVANCE_BOOKING_HOURS = 24;
    private static final Set<Integer> STANDARD_DURATIONS = Set.of(15, 30, 60, 90);

    public AppointmentService(AppointmentRepository appointmentRepository, AuditLogService auditLogService) {
        this.appointmentRepository = appointmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request, Long operatorId, String operatorRole) {
        validateServiceTypeAndDuration(request);
        validateTimeRange(request.getStartTime(), request.getEndTime(), true);
        checkConflicts(request.getAccompanyingStaffId(), request.getResourceId(), 
                request.getStartTime(), request.getEndTime(), null);

        Appointment appointment = Appointment.builder()
                .serviceType(request.getServiceType())
                .orderAmount(request.getOrderAmount())
                .status(AppointmentStatus.PENDING)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .accompanyingStaffId(request.getAccompanyingStaffId())
                .resourceId(request.getResourceId())
                .patientName(request.getPatientName())
                .notes(request.getNotes())
                .rescheduleCount(0)
                .operatorId(operatorId)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        
        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                AuditLogService.OPERATION_CREATE,
                operatorId,
                null,
                String.format("{\"patient\":\"%s\",\"service_type\":\"%s\",\"start\":\"%s\",\"end\":\"%s\"}", 
                        request.getPatientName(), request.getServiceType(), request.getStartTime(), request.getEndTime()),
                "Created appointment: " + saved.getId() + " (" + request.getServiceType().getDisplayName() + ")",
                null
        );

        log.info("Appointment created: id={}, serviceType={}, startTime={}, operatorId={}", 
                saved.getId(), request.getServiceType(), request.getStartTime(), operatorId);

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id, Long operatorId, String operatorRole, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        validateObjectAccess(appointment, operatorId, operatorRole, "cancel");
        
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Appointment is already cancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessException("Cannot cancel an appointment marked as no-show");
        }

        long hoursUntilStart = Duration.between(LocalDateTime.now(), appointment.getStartTime()).toHours();
        boolean within24Hours = hoursUntilStart < ADVANCE_BOOKING_HOURS;
        
        PenaltyResult penaltyResult = calculatePenalty(appointment, within24Hours, "CANCEL");
        
        appointment.setPenaltyAmount(penaltyResult.penaltyAmount);
        appointment.setPenaltyReason(penaltyResult.reason);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setOperatorId(operatorId);
        appointment.setCancelReason(reason);

        Appointment saved = appointmentRepository.save(appointment);
        
        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                "CANCEL",
                operatorId,
                null,
                String.format("{\"penalty\":\"%s\",\"reason\":\"%s\",\"within_24h\":%b,\"cancelled_by\":\"%s\"}", 
                        penaltyResult.penaltyAmount, penaltyResult.reason, within24Hours, operatorRole),
                String.format("Cancelled appointment %d: %s (penalty: %s)", 
                        saved.getId(), penaltyResult.reason, penaltyResult.penaltyAmount),
                null
        );

        log.info("Appointment cancelled: id={}, within24Hours={}, penalty={}", 
                saved.getId(), within24Hours, penaltyResult.penaltyAmount);

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Long id, AppointmentRescheduleRequest request, 
            Long operatorId, String operatorRole) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        validateObjectAccess(appointment, operatorId, operatorRole, "reschedule");

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Cannot reschedule a cancelled appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot reschedule a completed appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessException("Cannot reschedule an appointment marked as no-show");
        }

        if (appointment.getRescheduleCount() >= MAX_RESCHEDULE_COUNT) {
            throw new BusinessException("Maximum reschedule limit (" + MAX_RESCHEDULE_COUNT + ") reached");
        }

        validateTimeRange(request.getNewStartTime(), request.getNewEndTime(), true);
        validateStandardDuration(request.getNewStartTime(), request.getNewEndTime());
        checkConflicts(appointment.getAccompanyingStaffId(), appointment.getResourceId(),
                request.getNewStartTime(), request.getNewEndTime(), id);

        long hoursUntilStart = Duration.between(LocalDateTime.now(), appointment.getStartTime()).toHours();
        boolean within24Hours = hoursUntilStart < ADVANCE_BOOKING_HOURS;
        
        PenaltyResult penaltyResult = calculatePenalty(appointment, within24Hours, "RESCHEDULE");

        String oldTime = appointment.getStartTime().toString();
        appointment.setStartTime(request.getNewStartTime());
        appointment.setEndTime(request.getNewEndTime());
        appointment.setRescheduleCount(appointment.getRescheduleCount() + 1);
        appointment.setOperatorId(operatorId);
        
        if (penaltyResult.penaltyAmount.compareTo(BigDecimal.ZERO) > 0) {
            appointment.setPenaltyAmount(penaltyResult.penaltyAmount);
            appointment.setPenaltyReason(penaltyResult.reason);
        }

        Appointment saved = appointmentRepository.save(appointment);
        
        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                "RESCHEDULE",
                operatorId,
                null,
                String.format("{\"old_time\":\"%s\",\"new_time\":\"%s\",\"count\":\"%d\",\"within_24h\":%b,\"penalty\":\"%s\"}", 
                        oldTime, request.getNewStartTime(), saved.getRescheduleCount(), within24Hours, penaltyResult.penaltyAmount),
                String.format("Rescheduled appointment %d (reschedule #%d): penalty=%s", 
                        saved.getId(), saved.getRescheduleCount(), penaltyResult.penaltyAmount),
                null
        );

        log.info("Appointment rescheduled: id={}, rescheduleCount={}, within24Hours={}, penalty={}", 
                saved.getId(), saved.getRescheduleCount(), within24Hours, penaltyResult.penaltyAmount);

        return AppointmentResponse.fromEntity(saved);
    }

    public AppointmentResponse getAppointmentById(Long id, Long operatorId, String operatorRole) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        
        validateObjectAccess(appointment, operatorId, operatorRole, "view");
        return AppointmentResponse.fromEntity(appointment);
    }

    public List<AppointmentResponse> getAllAppointments(Long operatorId, String operatorRole) {
        List<Appointment> appointments;
        
        if ("ADMIN".equals(operatorRole) || "DISPATCHER".equals(operatorRole)) {
            appointments = appointmentRepository.findAll();
        } else {
            appointments = appointmentRepository.findByOperatorId(operatorId);
        }
        
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse confirmAppointment(Long id, Long operatorId, String operatorRole) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        validateObjectAccess(appointment, operatorId, operatorRole, "confirm");

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BusinessException("Only pending appointments can be confirmed");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setOperatorId(operatorId);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                "CONFIRM",
                operatorId,
                null,
                "{\"status\":\"CONFIRMED\"}",
                "Confirmed appointment: " + saved.getId(),
                null
        );

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponse startAppointment(Long id, Long operatorId, String operatorRole) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        validateObjectAccess(appointment, operatorId, operatorRole, "start");

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed appointments can be started");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setOperatorId(operatorId);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                "START",
                operatorId,
                null,
                "{\"status\":\"IN_PROGRESS\"}",
                "Started appointment: " + saved.getId(),
                null
        );

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponse completeAppointment(Long id, Long operatorId, String operatorRole) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        validateObjectAccess(appointment, operatorId, operatorRole, "complete");

        if (appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BusinessException("Only in-progress appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setOperatorId(operatorId);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                "COMPLETE",
                operatorId,
                null,
                "{\"status\":\"COMPLETED\"}",
                "Completed appointment: " + saved.getId(),
                null
        );

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponse markNoShow(Long id, Long operatorId, String operatorRole) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        validateObjectAccess(appointment, operatorId, operatorRole, "mark no-show");

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessException("Cannot mark as no-show for completed or cancelled appointments");
        }

        long hoursUntilStart = Duration.between(LocalDateTime.now(), appointment.getStartTime()).toHours();
        boolean within24Hours = hoursUntilStart < ADVANCE_BOOKING_HOURS;
        
        PenaltyResult penaltyResult = calculatePenalty(appointment, within24Hours, "NO_SHOW");
        
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setOperatorId(operatorId);
        
        if (penaltyResult.penaltyAmount.compareTo(BigDecimal.ZERO) > 0) {
            appointment.setPenaltyAmount(penaltyResult.penaltyAmount);
            appointment.setPenaltyReason(penaltyResult.reason);
        }

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.logOperation(
                saved.getId(),
                "APPOINTMENT",
                "NO_SHOW",
                operatorId,
                null,
                String.format("{\"status\":\"NO_SHOW\",\"within_24h\":%b,\"penalty\":\"%s\"}", 
                        within24Hours, penaltyResult.penaltyAmount),
                String.format("Marked appointment %d as no-show: penalty=%s", 
                        saved.getId(), penaltyResult.penaltyAmount),
                null
        );

        return AppointmentResponse.fromEntity(saved);
    }

    @Transactional
    public void autoCancelStaleAppointments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(AUTO_CANCEL_MINUTES);
        List<Appointment> staleAppointments = appointmentRepository
                .findStaleAppointments(AppointmentStatus.PENDING, threshold);

        for (Appointment appointment : staleAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setNotes((appointment.getNotes() == null ? "" : appointment.getNotes() + "; ") 
                    + "Auto-cancelled: pending for more than " + AUTO_CANCEL_MINUTES + " minutes");
            appointment.setCancelReason("Auto-cancelled: pending for more than " + AUTO_CANCEL_MINUTES + " minutes");
            appointmentRepository.save(appointment);
            
            auditLogService.logOperation(
                    appointment.getId(),
                    "APPOINTMENT",
                    "AUTO_CANCEL",
                    null,
                    "SYSTEM",
                    "{\"reason\":\"stale_pending\",\"auto_release_minutes\":" + AUTO_CANCEL_MINUTES + "}",
                    "Auto-cancelled stale appointment: " + appointment.getId(),
                    null
            );
        }
        
        if (!staleAppointments.isEmpty()) {
            log.info("Auto-cancelled {} stale appointments (pending > {} minutes)", 
                    staleAppointments.size(), AUTO_CANCEL_MINUTES);
        }
    }

    private void validateObjectAccess(Appointment appointment, Long operatorId, String operatorRole, String action) {
        if ("ADMIN".equals(operatorRole)) {
            return;
        }

        if ("DISPATCHER".equals(operatorRole)) {
            return;
        }

        if (appointment.getOperatorId() != null && appointment.getOperatorId().equals(operatorId)) {
            return;
        }

        throw new ForbiddenException("You do not have permission to " + action + " this appointment");
    }

    private void validateServiceTypeAndDuration(AppointmentCreateRequest request) {
        if (request.getServiceType() == null) {
            throw new BusinessException("Service type is required");
        }
        
        if (request.getStartTime() != null && request.getEndTime() != null) {
            int durationMinutes = (int) Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            
            if (!STANDARD_DURATIONS.contains(durationMinutes)) {
                throw new BusinessException(String.format(
                        "Invalid appointment duration: %d minutes. Standard durations are: 15, 30, 60, or 90 minutes", 
                        durationMinutes));
            }
            
            if (request.getServiceType().getDurationMinutes() != durationMinutes) {
                throw new BusinessException(String.format(
                        "Service type duration (%d min) does not match appointment duration (%d min). " +
                        "Use service type '%s' for %d-minute appointments or '%s' for %d-minute appointments",
                        request.getServiceType().getDurationMinutes(),
                        durationMinutes,
                        ServiceType.fromDuration(durationMinutes),
                        durationMinutes,
                        ServiceType.fromDuration(request.getServiceType().getDurationMinutes()),
                        request.getServiceType().getDurationMinutes()));
            }
        }
    }

    private void validateStandardDuration(LocalDateTime startTime, LocalDateTime endTime) {
        int durationMinutes = (int) Duration.between(startTime, endTime).toMinutes();
        
        if (!STANDARD_DURATIONS.contains(durationMinutes)) {
            throw new BusinessException(String.format(
                    "Invalid appointment duration: %d minutes. Standard durations are: 15, 30, 60, or 90 minutes", 
                    durationMinutes));
        }
    }

    private record PenaltyResult(BigDecimal penaltyAmount, String reason) {}

    private PenaltyResult calculatePenalty(Appointment appointment, boolean within24Hours, String operation) {
        if (within24Hours && appointment.getOrderAmount() != null && 
            appointment.getOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal percentagePenalty = appointment.getOrderAmount()
                    .multiply(PENALTY_PERCENTAGE)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal penalty = percentagePenalty.min(PENALTY_MIN_AMOUNT);
            
            String reason = String.format("%s within %d hours of appointment: %s of order amount (min: %s)", 
                    operation, ADVANCE_BOOKING_HOURS, 
                    PENALTY_PERCENTAGE.multiply(new BigDecimal("100")).toPlainString() + "%",
                    PENALTY_MIN_AMOUNT.toPlainString());
            
            return new PenaltyResult(penalty, reason);
        }
        
        return new PenaltyResult(BigDecimal.ZERO, "No penalty - cancelled/rescheduled with sufficient advance notice");
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime, boolean requireFuture) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("Start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("End time must be after start time");
        }
        if (requireFuture && startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Start time cannot be in the past");
        }
    }

    private void checkConflicts(Long staffId, Long resourceId, 
            LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        if (staffId != null) {
            List<Appointment> staffConflicts;
            if (excludeId != null) {
                staffConflicts = appointmentRepository.findConflictingStaffAppointmentsExcluding(
                        staffId, startTime, endTime, excludeId);
            } else {
                staffConflicts = appointmentRepository.findConflictingStaffAppointments(
                        staffId, startTime, endTime);
            }
            if (!staffConflicts.isEmpty()) {
                throw new BusinessException("Staff (ID: " + staffId + ") has conflicting appointments in this time slot");
            }
        }

        if (resourceId != null) {
            List<Appointment> resourceConflicts;
            if (excludeId != null) {
                resourceConflicts = appointmentRepository.findConflictingResourceAppointmentsExcluding(
                        resourceId, startTime, endTime, excludeId);
            } else {
                resourceConflicts = appointmentRepository.findConflictingResourceAppointments(
                        resourceId, startTime, endTime);
            }
            if (!resourceConflicts.isEmpty()) {
                throw new BusinessException("Resource (ID: " + resourceId + ") is already booked in this time slot");
            }
        }
    }
}

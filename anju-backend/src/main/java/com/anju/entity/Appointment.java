package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointment_time", columnList = "start_time, end_time"),
    @Index(name = "idx_medical_staff", columnList = "accompanying_staff_id"),
    @Index(name = "idx_resource", columnList = "resource_id"),
    @Index(name = "idx_service_type", columnList = "service_type"),
    @Index(name = "idx_unique_appointment_number", columnList = "unique_appointment_number"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_appointment_number", length = 20, unique = true)
    private String uniqueAppointmentNumber;

    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(name = "order_amount", precision = 12, scale = 2)
    private BigDecimal orderAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "accompanying_staff_id")
    private Long accompanyingStaffId;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "patient_name", length = 100)
    private String patientName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reschedule_count")
    private Integer rescheduleCount = 0;

    @Column(name = "penalty_amount", precision = 12, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "penalty_reason")
    private String penaltyReason;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Appointment() {}

    public Appointment(Long id, String uniqueAppointmentNumber, String idempotencyKey, ServiceType serviceType,
                      BigDecimal orderAmount, AppointmentStatus status, LocalDateTime startTime, LocalDateTime endTime,
                      Long accompanyingStaffId, Long resourceId, String patientName, String notes,
                      Integer rescheduleCount, BigDecimal penaltyAmount, String penaltyReason, Long operatorId,
                      String cancelReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.uniqueAppointmentNumber = uniqueAppointmentNumber;
        this.idempotencyKey = idempotencyKey;
        this.serviceType = serviceType;
        this.orderAmount = orderAmount;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.accompanyingStaffId = accompanyingStaffId;
        this.resourceId = resourceId;
        this.patientName = patientName;
        this.notes = notes;
        this.rescheduleCount = rescheduleCount;
        this.penaltyAmount = penaltyAmount;
        this.penaltyReason = penaltyReason;
        this.operatorId = operatorId;
        this.cancelReason = cancelReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentBuilder builder() {
        return new AppointmentBuilder();
    }

    public static class AppointmentBuilder {
        private Long id;
        private String uniqueAppointmentNumber;
        private String idempotencyKey;
        private ServiceType serviceType;
        private BigDecimal orderAmount;
        private AppointmentStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long accompanyingStaffId;
        private Long resourceId;
        private String patientName;
        private String notes;
        private Integer rescheduleCount = 0;
        private BigDecimal penaltyAmount;
        private String penaltyReason;
        private Long operatorId;
        private String cancelReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AppointmentBuilder id(Long id) { this.id = id; return this; }
        public AppointmentBuilder uniqueAppointmentNumber(String uniqueAppointmentNumber) { this.uniqueAppointmentNumber = uniqueAppointmentNumber; return this; }
        public AppointmentBuilder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public AppointmentBuilder serviceType(ServiceType serviceType) { this.serviceType = serviceType; return this; }
        public AppointmentBuilder orderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; return this; }
        public AppointmentBuilder status(AppointmentStatus status) { this.status = status; return this; }
        public AppointmentBuilder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public AppointmentBuilder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public AppointmentBuilder accompanyingStaffId(Long accompanyingStaffId) { this.accompanyingStaffId = accompanyingStaffId; return this; }
        public AppointmentBuilder resourceId(Long resourceId) { this.resourceId = resourceId; return this; }
        public AppointmentBuilder patientName(String patientName) { this.patientName = patientName; return this; }
        public AppointmentBuilder notes(String notes) { this.notes = notes; return this; }
        public AppointmentBuilder rescheduleCount(Integer rescheduleCount) { this.rescheduleCount = rescheduleCount; return this; }
        public AppointmentBuilder penaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; return this; }
        public AppointmentBuilder penaltyReason(String penaltyReason) { this.penaltyReason = penaltyReason; return this; }
        public AppointmentBuilder operatorId(Long operatorId) { this.operatorId = operatorId; return this; }
        public AppointmentBuilder cancelReason(String cancelReason) { this.cancelReason = cancelReason; return this; }
        public AppointmentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AppointmentBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Appointment build() {
            return new Appointment(id, uniqueAppointmentNumber, idempotencyKey, serviceType, orderAmount, status,
                startTime, endTime, accompanyingStaffId, resourceId, patientName, notes, rescheduleCount,
                penaltyAmount, penaltyReason, operatorId, cancelReason, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUniqueAppointmentNumber() { return uniqueAppointmentNumber; }
    public void setUniqueAppointmentNumber(String uniqueAppointmentNumber) { this.uniqueAppointmentNumber = uniqueAppointmentNumber; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getAccompanyingStaffId() { return accompanyingStaffId; }
    public void setAccompanyingStaffId(Long accompanyingStaffId) { this.accompanyingStaffId = accompanyingStaffId; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getRescheduleCount() { return rescheduleCount; }
    public void setRescheduleCount(Integer rescheduleCount) { this.rescheduleCount = rescheduleCount; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }
    public String getPenaltyReason() { return penaltyReason; }
    public void setPenaltyReason(String penaltyReason) { this.penaltyReason = penaltyReason; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (int) Duration.between(startTime, endTime).toMinutes();
        }
        return serviceType != null ? serviceType.getDurationMinutes() : 0;
    }

    public boolean isStandardDuration() {
        int duration = getDurationMinutes();
        return duration == 15 || duration == 30 || duration == 60 || duration == 90;
    }

    public enum AppointmentStatus {
        PENDING,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    public enum ServiceType {
        QUICK_CONSULTATION(15, "Quick Consultation"),
        STANDARD_CONSULTATION(30, "Standard Consultation"),
        EXTENDED_CONSULTATION(60, "Extended Consultation"),
        COMPREHENSIVE_REVIEW(90, "Comprehensive Review");

        private final int durationMinutes;
        private final String displayName;

        ServiceType(int durationMinutes, String displayName) {
            this.durationMinutes = durationMinutes;
            this.displayName = displayName;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ServiceType fromDuration(int minutes) {
            for (ServiceType type : values()) {
                if (type.durationMinutes == minutes) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No service type for duration: " + minutes + " minutes. Valid durations: 15, 30, 60, 90");
        }
    }
}

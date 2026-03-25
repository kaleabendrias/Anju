package com.anju.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
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
}

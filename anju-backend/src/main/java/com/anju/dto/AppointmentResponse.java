package com.anju.dto;

import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.entity.Appointment.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private ServiceType serviceType;
    private BigDecimal orderAmount;
    private AppointmentStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private Long accompanyingStaffId;
    private Long resourceId;
    private String patientName;
    private String notes;
    private Integer rescheduleCount;
    private BigDecimal penaltyAmount;
    private String penaltyReason;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppointmentResponse fromEntity(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .serviceType(appointment.getServiceType())
                .orderAmount(appointment.getOrderAmount())
                .status(appointment.getStatus())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .durationMinutes(appointment.getDurationMinutes())
                .accompanyingStaffId(appointment.getAccompanyingStaffId())
                .resourceId(appointment.getResourceId())
                .patientName(appointment.getPatientName())
                .notes(appointment.getNotes())
                .rescheduleCount(appointment.getRescheduleCount())
                .penaltyAmount(appointment.getPenaltyAmount())
                .penaltyReason(appointment.getPenaltyReason())
                .cancelReason(appointment.getCancelReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}

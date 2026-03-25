package com.anju.dto;

import com.anju.entity.Appointment.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {
    
    @NotNull(message = "Service type is required")
    private ServiceType serviceType;
    
    private BigDecimal orderAmount;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    private Long accompanyingStaffId;
    
    private Long resourceId;
    
    private String patientName;
    
    private String notes;

    private static final Set<Integer> STANDARD_DURATIONS = Set.of(15, 30, 60, 90);

    public boolean hasStandardDuration() {
        if (startTime != null && endTime != null) {
            long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
            return STANDARD_DURATIONS.contains((int) minutes);
        }
        return serviceType != null && STANDARD_DURATIONS.contains(serviceType.getDurationMinutes());
    }

    public int getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return serviceType != null ? serviceType.getDurationMinutes() : 0;
    }
}

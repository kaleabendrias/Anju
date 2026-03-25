package com.anju.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRescheduleRequest {
    @NotNull(message = "New start time is required")
    private LocalDateTime newStartTime;
    @NotNull(message = "New end time is required")
    private LocalDateTime newEndTime;
}

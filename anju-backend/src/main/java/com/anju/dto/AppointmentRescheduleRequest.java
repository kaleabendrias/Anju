package com.anju.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AppointmentRescheduleRequest {
    @NotNull(message = "New start time is required")
    private LocalDateTime newStartTime;
    @NotNull(message = "New end time is required")
    private LocalDateTime newEndTime;

    public AppointmentRescheduleRequest() {}

    public AppointmentRescheduleRequest(LocalDateTime newStartTime, LocalDateTime newEndTime) {
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
    }

    public static AppointmentRescheduleRequestBuilder builder() {
        return new AppointmentRescheduleRequestBuilder();
    }

    public static class AppointmentRescheduleRequestBuilder {
        private LocalDateTime newStartTime;
        private LocalDateTime newEndTime;

        public AppointmentRescheduleRequestBuilder newStartTime(LocalDateTime newStartTime) {
            this.newStartTime = newStartTime;
            return this;
        }
        public AppointmentRescheduleRequestBuilder newEndTime(LocalDateTime newEndTime) {
            this.newEndTime = newEndTime;
            return this;
        }
        public AppointmentRescheduleRequest build() {
            return new AppointmentRescheduleRequest(newStartTime, newEndTime);
        }
    }

    public LocalDateTime getNewStartTime() { return newStartTime; }
    public void setNewStartTime(LocalDateTime newStartTime) { this.newStartTime = newStartTime; }
    public LocalDateTime getNewEndTime() { return newEndTime; }
    public void setNewEndTime(LocalDateTime newEndTime) { this.newEndTime = newEndTime; }
}

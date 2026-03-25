package com.anju.dto;

import com.anju.entity.Appointment.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
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
    
    private String idempotencyKey;

    private static final Set<Integer> STANDARD_DURATIONS = Set.of(15, 30, 60, 90);

    public AppointmentCreateRequest() {
    }

    public AppointmentCreateRequest(ServiceType serviceType, BigDecimal orderAmount, 
                                     LocalDateTime startTime, LocalDateTime endTime, 
                                     Long accompanyingStaffId, Long resourceId, 
                                     String patientName, String notes, 
                                     String idempotencyKey) {
        this.serviceType = serviceType;
        this.orderAmount = orderAmount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.accompanyingStaffId = accompanyingStaffId;
        this.resourceId = resourceId;
        this.patientName = patientName;
        this.notes = notes;
        this.idempotencyKey = idempotencyKey;
    }

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

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getAccompanyingStaffId() {
        return accompanyingStaffId;
    }

    public void setAccompanyingStaffId(Long accompanyingStaffId) {
        this.accompanyingStaffId = accompanyingStaffId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ServiceType serviceType;
        private BigDecimal orderAmount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long accompanyingStaffId;
        private Long resourceId;
        private String patientName;
        private String notes;
        private String idempotencyKey;

        public Builder serviceType(ServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder orderAmount(BigDecimal orderAmount) {
            this.orderAmount = orderAmount;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder accompanyingStaffId(Long accompanyingStaffId) {
            this.accompanyingStaffId = accompanyingStaffId;
            return this;
        }

        public Builder resourceId(Long resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public AppointmentCreateRequest build() {
            return new AppointmentCreateRequest(serviceType, orderAmount, startTime, 
                                                endTime, accompanyingStaffId, 
                                                resourceId, patientName, notes, 
                                                idempotencyKey);
        }
    }
}

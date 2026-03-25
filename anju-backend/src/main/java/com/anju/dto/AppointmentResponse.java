package com.anju.dto;

import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.entity.Appointment.ServiceType;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String uniqueAppointmentNumber;
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

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, String uniqueAppointmentNumber, ServiceType serviceType, 
                               BigDecimal orderAmount, AppointmentStatus status, 
                               LocalDateTime startTime, LocalDateTime endTime, 
                               int durationMinutes, Long accompanyingStaffId, Long resourceId, 
                               String patientName, String notes, Integer rescheduleCount, 
                               BigDecimal penaltyAmount, String penaltyReason, 
                               String cancelReason, LocalDateTime createdAt, 
                               LocalDateTime updatedAt) {
        this.id = id;
        this.uniqueAppointmentNumber = uniqueAppointmentNumber;
        this.serviceType = serviceType;
        this.orderAmount = orderAmount;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.accompanyingStaffId = accompanyingStaffId;
        this.resourceId = resourceId;
        this.patientName = patientName;
        this.notes = notes;
        this.rescheduleCount = rescheduleCount;
        this.penaltyAmount = penaltyAmount;
        this.penaltyReason = penaltyReason;
        this.cancelReason = cancelReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentResponse fromEntity(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .uniqueAppointmentNumber(appointment.getUniqueAppointmentNumber())
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueAppointmentNumber() {
        return uniqueAppointmentNumber;
    }

    public void setUniqueAppointmentNumber(String uniqueAppointmentNumber) {
        this.uniqueAppointmentNumber = uniqueAppointmentNumber;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
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

    public Integer getRescheduleCount() {
        return rescheduleCount;
    }

    public void setRescheduleCount(Integer rescheduleCount) {
        this.rescheduleCount = rescheduleCount;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public String getPenaltyReason() {
        return penaltyReason;
    }

    public void setPenaltyReason(String penaltyReason) {
        this.penaltyReason = penaltyReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String uniqueAppointmentNumber;
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

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder uniqueAppointmentNumber(String uniqueAppointmentNumber) {
            this.uniqueAppointmentNumber = uniqueAppointmentNumber;
            return this;
        }

        public Builder serviceType(ServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder orderAmount(BigDecimal orderAmount) {
            this.orderAmount = orderAmount;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            this.status = status;
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

        public Builder durationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
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

        public Builder rescheduleCount(Integer rescheduleCount) {
            this.rescheduleCount = rescheduleCount;
            return this;
        }

        public Builder penaltyAmount(BigDecimal penaltyAmount) {
            this.penaltyAmount = penaltyAmount;
            return this;
        }

        public Builder penaltyReason(String penaltyReason) {
            this.penaltyReason = penaltyReason;
            return this;
        }

        public Builder cancelReason(String cancelReason) {
            this.cancelReason = cancelReason;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AppointmentResponse build() {
            return new AppointmentResponse(id, uniqueAppointmentNumber, serviceType, 
                                           orderAmount, status, startTime, endTime, 
                                           durationMinutes, accompanyingStaffId, 
                                           resourceId, patientName, notes, 
                                           rescheduleCount, penaltyAmount, 
                                           penaltyReason, cancelReason, createdAt, 
                                           updatedAt);
        }
    }
}

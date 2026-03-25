package com.anju.dto;

import jakarta.validation.constraints.NotNull;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.time.LocalDate;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class VacancyPeriodCreateRequest {
    
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "Reason is required")
    private String reason;

    public VacancyPeriodCreateRequest() {
    }

    public VacancyPeriodCreateRequest(Long propertyId, LocalDate startDate, 
                                       LocalDate endDate, String reason) {
        this.propertyId = propertyId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long propertyId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;

        public Builder propertyId(Long propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public VacancyPeriodCreateRequest build() {
            return new VacancyPeriodCreateRequest(propertyId, startDate, endDate, reason);
        }
    }
}

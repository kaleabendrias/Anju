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
public class VacancyPeriodUpdateRequest {
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "Reason is required")
    private String reason;
    
    private Boolean isActive;

    public VacancyPeriodUpdateRequest() {
    }

    public VacancyPeriodUpdateRequest(LocalDate startDate, LocalDate endDate, 
                                       String reason, Boolean isActive) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.isActive = isActive;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private Boolean isActive;

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

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public VacancyPeriodUpdateRequest build() {
            return new VacancyPeriodUpdateRequest(startDate, endDate, reason, isActive);
        }
    }
}

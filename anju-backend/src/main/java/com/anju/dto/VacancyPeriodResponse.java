package com.anju.dto;

import com.anju.entity.VacancyPeriod;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class VacancyPeriodResponse {
    private Long id;
    private Long propertyId;
    private String propertyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private boolean currentlyActive;

    public VacancyPeriodResponse() {
    }

    public VacancyPeriodResponse(Long id, Long propertyId, String propertyCode, 
                                  LocalDate startDate, LocalDate endDate, 
                                  String reason, Long createdBy, 
                                  LocalDateTime createdAt, Boolean isActive, 
                                  boolean currentlyActive) {
        this.id = id;
        this.propertyId = propertyId;
        this.propertyCode = propertyCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.currentlyActive = currentlyActive;
    }

    public static VacancyPeriodResponse fromEntity(VacancyPeriod vacancyPeriod) {
        return VacancyPeriodResponse.builder()
                .id(vacancyPeriod.getId())
                .propertyId(vacancyPeriod.getProperty() != null ? vacancyPeriod.getProperty().getId() : null)
                .propertyCode(vacancyPeriod.getProperty() != null ? vacancyPeriod.getProperty().getUniqueCode() : null)
                .startDate(vacancyPeriod.getStartDate())
                .endDate(vacancyPeriod.getEndDate())
                .reason(vacancyPeriod.getReason())
                .createdBy(vacancyPeriod.getCreatedBy())
                .createdAt(vacancyPeriod.getCreatedAt())
                .isActive(vacancyPeriod.getIsActive())
                .currentlyActive(vacancyPeriod.isCurrentlyActive())
                .build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyCode() {
        return propertyCode;
    }

    public void setPropertyCode(String propertyCode) {
        this.propertyCode = propertyCode;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isCurrentlyActive() {
        return currentlyActive;
    }

    public void setCurrentlyActive(boolean currentlyActive) {
        this.currentlyActive = currentlyActive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long propertyId;
        private String propertyCode;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private Long createdBy;
        private LocalDateTime createdAt;
        private Boolean isActive;
        private boolean currentlyActive;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder propertyId(Long propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public Builder propertyCode(String propertyCode) {
            this.propertyCode = propertyCode;
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

        public Builder createdBy(Long createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder currentlyActive(boolean currentlyActive) {
            this.currentlyActive = currentlyActive;
            return this;
        }

        public VacancyPeriodResponse build() {
            return new VacancyPeriodResponse(id, propertyId, propertyCode, startDate, 
                                              endDate, reason, createdBy, createdAt, 
                                              isActive, currentlyActive);
        }
    }
}

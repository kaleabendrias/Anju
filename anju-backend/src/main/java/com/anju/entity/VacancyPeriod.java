package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "vacancy_periods", indexes = {
    @Index(name = "idx_vacancy_property", columnList = "property_id"),
    @Index(name = "idx_vacancy_dates", columnList = "start_date, end_date")
})
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class VacancyPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public VacancyPeriod() {}

    public VacancyPeriod(Long id, Property property, LocalDate startDate, LocalDate endDate,
                         String reason, Long createdBy, LocalDateTime createdAt, Boolean isActive) {
        this.id = id;
        this.property = property;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public static VacancyPeriodBuilder builder() {
        return new VacancyPeriodBuilder();
    }

    public static class VacancyPeriodBuilder {
        private Long id;
        private Property property;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private Long createdBy;
        private LocalDateTime createdAt;
        private Boolean isActive = true;

        public VacancyPeriodBuilder id(Long id) { this.id = id; return this; }
        public VacancyPeriodBuilder property(Property property) { this.property = property; return this; }
        public VacancyPeriodBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public VacancyPeriodBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public VacancyPeriodBuilder reason(String reason) { this.reason = reason; return this; }
        public VacancyPeriodBuilder createdBy(Long createdBy) { this.createdBy = createdBy; return this; }
        public VacancyPeriodBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public VacancyPeriodBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }

        public VacancyPeriod build() {
            return new VacancyPeriod(id, property, startDate, endDate, reason, createdBy, createdAt, isActive);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public boolean isCurrentlyActive() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        if (startDate.isAfter(LocalDate.now())) {
            return false;
        }
        return endDate == null || !endDate.isBefore(LocalDate.now());
    }

    public boolean overlapsWith(LocalDate start, LocalDate end) {
        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now().plusYears(100);
        LocalDate checkEnd = end != null ? end : LocalDate.now().plusYears(100);
        
        return !startDate.isAfter(checkEnd) && !effectiveEnd.isBefore(start);
    }
}
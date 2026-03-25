package com.anju.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy_periods", indexes = {
    @Index(name = "idx_vacancy_property", columnList = "property_id"),
    @Index(name = "idx_vacancy_dates", columnList = "start_date, end_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Boolean isActive = true;

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

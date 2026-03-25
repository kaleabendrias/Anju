package com.anju.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties", indexes = {
    @Index(name = "idx_unique_code", columnList = "unique_code", unique = true),
    @Index(name = "idx_property_status", columnList = "status"),
    @Index(name = "idx_compliance_status", columnList = "compliance_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_code", nullable = false, unique = true, length = 50)
    private String uniqueCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 30)
    @Builder.Default
    private ComplianceStatus complianceStatus = ComplianceStatus.NOT_STARTED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rent;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deposit;

    @Column(name = "rental_start_date")
    private LocalDate rentalStartDate;

    @Column(name = "rental_end_date")
    private LocalDate rentalEndDate;

    @Column(name = "materials_json", columnDefinition = "TEXT")
    private String materialsJson;

    @Column(name = "compliance_validations_json", columnDefinition = "TEXT")
    private String complianceValidationsJson;

    @Column(name = "compliance_notes", columnDefinition = "TEXT")
    private String complianceNotes;

    @Column(name = "last_compliance_check")
    private LocalDateTime lastComplianceCheck;

    @Column(name = "compliance_checked_by")
    private Long complianceCheckedBy;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VacancyPeriod> vacancyPeriods = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PropertyStatus {
        DRAFT,
        PENDING_REVIEW,
        LISTED,
        DELISTED
    }

    public enum ComplianceStatus {
        NOT_STARTED,
        IN_PROGRESS,
        PASSED,
        FAILED,
        EXPIRED,
        WAITING_DOCUMENTS
    }

    public boolean canTransitionTo(PropertyStatus newStatus) {
        return switch (this.status) {
            case DRAFT -> newStatus == PropertyStatus.PENDING_REVIEW;
            case PENDING_REVIEW -> newStatus == PropertyStatus.LISTED || newStatus == PropertyStatus.DRAFT;
            case LISTED -> newStatus == PropertyStatus.DELISTED || newStatus == PropertyStatus.PENDING_REVIEW;
            case DELISTED -> newStatus == PropertyStatus.PENDING_REVIEW || newStatus == PropertyStatus.LISTED;
        };
    }

    public boolean canTransitionCompliance(ComplianceStatus newStatus) {
        if (this.complianceStatus == ComplianceStatus.EXPIRED) {
            return newStatus == ComplianceStatus.IN_PROGRESS || newStatus == ComplianceStatus.PASSED;
        }
        return switch (this.complianceStatus) {
            case NOT_STARTED -> newStatus == ComplianceStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == ComplianceStatus.PASSED || 
                               newStatus == ComplianceStatus.FAILED || 
                               newStatus == ComplianceStatus.WAITING_DOCUMENTS;
            case WAITING_DOCUMENTS -> newStatus == ComplianceStatus.IN_PROGRESS || newStatus == ComplianceStatus.FAILED;
            case FAILED -> newStatus == ComplianceStatus.IN_PROGRESS;
            case PASSED -> newStatus == ComplianceStatus.EXPIRED;
            case EXPIRED -> newStatus == ComplianceStatus.IN_PROGRESS || newStatus == ComplianceStatus.PASSED;
        };
    }

    public void addVacancyPeriod(LocalDate startDate, LocalDate endDate, String reason) {
        VacancyPeriod period = VacancyPeriod.builder()
                .property(this)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .build();
        this.vacancyPeriods.add(period);
    }

    public boolean isVacantOn(LocalDate date) {
        if (this.status != PropertyStatus.LISTED) {
            return false;
        }
        if (vacancyPeriods == null || vacancyPeriods.isEmpty()) {
            return false;
        }
        return vacancyPeriods.stream()
                .filter(Vp -> Vp.getEndDate() == null || !Vp.getEndDate().isBefore(date))
                .anyMatch(Vp -> !Vp.getStartDate().isAfter(date));
    }
}

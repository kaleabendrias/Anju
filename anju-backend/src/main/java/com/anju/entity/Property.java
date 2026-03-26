package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "properties", indexes = {
    @Index(name = "idx_unique_code", columnList = "unique_code", unique = true),
    @Index(name = "idx_property_status", columnList = "status"),
    @Index(name = "idx_compliance_status", columnList = "compliance_status"),
    @Index(name = "idx_property_owner", columnList = "owner_id")
})
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unique_code", nullable = false, unique = true, length = 50)
    private String uniqueCode;

    @Column(name = "owner_id")
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 30)
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
    private List<VacancyPeriod> vacancyPeriods = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Property() {}

    public Property(Long id, String uniqueCode, Long ownerId, PropertyStatus status, ComplianceStatus complianceStatus,
                    BigDecimal rent, BigDecimal deposit, LocalDate rentalStartDate, LocalDate rentalEndDate,
                    String materialsJson, String complianceValidationsJson, String complianceNotes,
                    LocalDateTime lastComplianceCheck, Long complianceCheckedBy, List<VacancyPeriod> vacancyPeriods,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.uniqueCode = uniqueCode;
        this.ownerId = ownerId;
        this.status = status;
        this.complianceStatus = complianceStatus;
        this.rent = rent;
        this.deposit = deposit;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;
        this.materialsJson = materialsJson;
        this.complianceValidationsJson = complianceValidationsJson;
        this.complianceNotes = complianceNotes;
        this.lastComplianceCheck = lastComplianceCheck;
        this.complianceCheckedBy = complianceCheckedBy;
        this.vacancyPeriods = vacancyPeriods;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PropertyBuilder builder() {
        return new PropertyBuilder();
    }

    public static class PropertyBuilder {
        private Long id;
        private String uniqueCode;
        private Long ownerId;
        private PropertyStatus status;
        private ComplianceStatus complianceStatus = ComplianceStatus.NOT_STARTED;
        private BigDecimal rent;
        private BigDecimal deposit;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;
        private String materialsJson;
        private String complianceValidationsJson;
        private String complianceNotes;
        private LocalDateTime lastComplianceCheck;
        private Long complianceCheckedBy;
        private List<VacancyPeriod> vacancyPeriods = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PropertyBuilder id(Long id) { this.id = id; return this; }
        public PropertyBuilder uniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; return this; }
        public PropertyBuilder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public PropertyBuilder status(PropertyStatus status) { this.status = status; return this; }
        public PropertyBuilder complianceStatus(ComplianceStatus complianceStatus) { this.complianceStatus = complianceStatus; return this; }
        public PropertyBuilder rent(BigDecimal rent) { this.rent = rent; return this; }
        public PropertyBuilder deposit(BigDecimal deposit) { this.deposit = deposit; return this; }
        public PropertyBuilder rentalStartDate(LocalDate rentalStartDate) { this.rentalStartDate = rentalStartDate; return this; }
        public PropertyBuilder rentalEndDate(LocalDate rentalEndDate) { this.rentalEndDate = rentalEndDate; return this; }
        public PropertyBuilder materialsJson(String materialsJson) { this.materialsJson = materialsJson; return this; }
        public PropertyBuilder complianceValidationsJson(String complianceValidationsJson) { this.complianceValidationsJson = complianceValidationsJson; return this; }
        public PropertyBuilder complianceNotes(String complianceNotes) { this.complianceNotes = complianceNotes; return this; }
        public PropertyBuilder lastComplianceCheck(LocalDateTime lastComplianceCheck) { this.lastComplianceCheck = lastComplianceCheck; return this; }
        public PropertyBuilder complianceCheckedBy(Long complianceCheckedBy) { this.complianceCheckedBy = complianceCheckedBy; return this; }
        public PropertyBuilder vacancyPeriods(List<VacancyPeriod> vacancyPeriods) { this.vacancyPeriods = vacancyPeriods; return this; }
        public PropertyBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PropertyBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Property build() {
            return new Property(id, uniqueCode, ownerId, status, complianceStatus, rent, deposit, rentalStartDate,
                rentalEndDate, materialsJson, complianceValidationsJson, complianceNotes, lastComplianceCheck,
                complianceCheckedBy, vacancyPeriods, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUniqueCode() { return uniqueCode; }
    public void setUniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }
    public ComplianceStatus getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(ComplianceStatus complianceStatus) { this.complianceStatus = complianceStatus; }
    public BigDecimal getRent() { return rent; }
    public void setRent(BigDecimal rent) { this.rent = rent; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public LocalDate getRentalStartDate() { return rentalStartDate; }
    public void setRentalStartDate(LocalDate rentalStartDate) { this.rentalStartDate = rentalStartDate; }
    public LocalDate getRentalEndDate() { return rentalEndDate; }
    public void setRentalEndDate(LocalDate rentalEndDate) { this.rentalEndDate = rentalEndDate; }
    public String getMaterialsJson() { return materialsJson; }
    public void setMaterialsJson(String materialsJson) { this.materialsJson = materialsJson; }
    public String getComplianceValidationsJson() { return complianceValidationsJson; }
    public void setComplianceValidationsJson(String complianceValidationsJson) { this.complianceValidationsJson = complianceValidationsJson; }
    public String getComplianceNotes() { return complianceNotes; }
    public void setComplianceNotes(String complianceNotes) { this.complianceNotes = complianceNotes; }
    public LocalDateTime getLastComplianceCheck() { return lastComplianceCheck; }
    public void setLastComplianceCheck(LocalDateTime lastComplianceCheck) { this.lastComplianceCheck = lastComplianceCheck; }
    public Long getComplianceCheckedBy() { return complianceCheckedBy; }
    public void setComplianceCheckedBy(Long complianceCheckedBy) { this.complianceCheckedBy = complianceCheckedBy; }
    public List<VacancyPeriod> getVacancyPeriods() { return vacancyPeriods; }
    public void setVacancyPeriods(List<VacancyPeriod> vacancyPeriods) { this.vacancyPeriods = vacancyPeriods; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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
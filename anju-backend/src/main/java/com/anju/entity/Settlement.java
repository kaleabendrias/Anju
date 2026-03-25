package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements", indexes = {
    @Index(name = "idx_settlement_date", columnList = "settlement_date", unique = true)
})
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_date", nullable = false, unique = true)
    private LocalDate settlementDate;

    @Column(name = "total_income", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_refunds", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "total_penalties", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPenalties = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "transaction_count")
    private Integer transactionCount = 0;

    @Column(name = "exception_flag")
    private Boolean exceptionFlag = false;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", length = 20)
    private InvoiceStatus invoiceStatus = InvoiceStatus.NOT_REQUESTED;

    @Column(name = "invoice_requested_at")
    private LocalDateTime invoiceRequestedAt;

    @Column(name = "invoice_issued_at")
    private LocalDateTime invoiceIssuedAt;

    @Column(name = "invoice_rejected_at")
    private LocalDateTime invoiceRejectedAt;

    @Column(name = "invoice_reject_reason")
    private String invoiceRejectReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "generated_by")
    private String generatedBy;

    public Settlement() {}

    public Settlement(Long id, LocalDate settlementDate, BigDecimal totalIncome, BigDecimal totalRefunds,
                     BigDecimal totalPenalties, BigDecimal netAmount, Integer transactionCount,
                     Boolean exceptionFlag, String exceptionMessage, InvoiceStatus invoiceStatus,
                     LocalDateTime invoiceRequestedAt, LocalDateTime invoiceIssuedAt,
                     LocalDateTime invoiceRejectedAt, String invoiceRejectReason,
                     LocalDateTime createdAt, LocalDateTime updatedAt, String generatedBy) {
        this.id = id;
        this.settlementDate = settlementDate;
        this.totalIncome = totalIncome;
        this.totalRefunds = totalRefunds;
        this.totalPenalties = totalPenalties;
        this.netAmount = netAmount;
        this.transactionCount = transactionCount;
        this.exceptionFlag = exceptionFlag;
        this.exceptionMessage = exceptionMessage;
        this.invoiceStatus = invoiceStatus;
        this.invoiceRequestedAt = invoiceRequestedAt;
        this.invoiceIssuedAt = invoiceIssuedAt;
        this.invoiceRejectedAt = invoiceRejectedAt;
        this.invoiceRejectReason = invoiceRejectReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.generatedBy = generatedBy;
    }

    public static SettlementBuilder builder() {
        return new SettlementBuilder();
    }

    public static class SettlementBuilder {
        private Long id;
        private LocalDate settlementDate;
        private BigDecimal totalIncome = BigDecimal.ZERO;
        private BigDecimal totalRefunds = BigDecimal.ZERO;
        private BigDecimal totalPenalties = BigDecimal.ZERO;
        private BigDecimal netAmount = BigDecimal.ZERO;
        private Integer transactionCount = 0;
        private Boolean exceptionFlag = false;
        private String exceptionMessage;
        private InvoiceStatus invoiceStatus = InvoiceStatus.NOT_REQUESTED;
        private LocalDateTime invoiceRequestedAt;
        private LocalDateTime invoiceIssuedAt;
        private LocalDateTime invoiceRejectedAt;
        private String invoiceRejectReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String generatedBy;

        public SettlementBuilder id(Long id) { this.id = id; return this; }
        public SettlementBuilder settlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; return this; }
        public SettlementBuilder totalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; return this; }
        public SettlementBuilder totalRefunds(BigDecimal totalRefunds) { this.totalRefunds = totalRefunds; return this; }
        public SettlementBuilder totalPenalties(BigDecimal totalPenalties) { this.totalPenalties = totalPenalties; return this; }
        public SettlementBuilder netAmount(BigDecimal netAmount) { this.netAmount = netAmount; return this; }
        public SettlementBuilder transactionCount(Integer transactionCount) { this.transactionCount = transactionCount; return this; }
        public SettlementBuilder exceptionFlag(Boolean exceptionFlag) { this.exceptionFlag = exceptionFlag; return this; }
        public SettlementBuilder exceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; return this; }
        public SettlementBuilder invoiceStatus(InvoiceStatus invoiceStatus) { this.invoiceStatus = invoiceStatus; return this; }
        public SettlementBuilder invoiceRequestedAt(LocalDateTime invoiceRequestedAt) { this.invoiceRequestedAt = invoiceRequestedAt; return this; }
        public SettlementBuilder invoiceIssuedAt(LocalDateTime invoiceIssuedAt) { this.invoiceIssuedAt = invoiceIssuedAt; return this; }
        public SettlementBuilder invoiceRejectedAt(LocalDateTime invoiceRejectedAt) { this.invoiceRejectedAt = invoiceRejectedAt; return this; }
        public SettlementBuilder invoiceRejectReason(String invoiceRejectReason) { this.invoiceRejectReason = invoiceRejectReason; return this; }
        public SettlementBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SettlementBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public SettlementBuilder generatedBy(String generatedBy) { this.generatedBy = generatedBy; return this; }

        public Settlement build() {
            return new Settlement(id, settlementDate, totalIncome, totalRefunds, totalPenalties, netAmount,
                transactionCount, exceptionFlag, exceptionMessage, invoiceStatus, invoiceRequestedAt,
                invoiceIssuedAt, invoiceRejectedAt, invoiceRejectReason, createdAt, updatedAt, generatedBy);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDate settlementDate) { this.settlementDate = settlementDate; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
    public BigDecimal getTotalRefunds() { return totalRefunds; }
    public void setTotalRefunds(BigDecimal totalRefunds) { this.totalRefunds = totalRefunds; }
    public BigDecimal getTotalPenalties() { return totalPenalties; }
    public void setTotalPenalties(BigDecimal totalPenalties) { this.totalPenalties = totalPenalties; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }
    public Boolean getExceptionFlag() { return exceptionFlag; }
    public void setExceptionFlag(Boolean exceptionFlag) { this.exceptionFlag = exceptionFlag; }
    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
    public InvoiceStatus getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(InvoiceStatus invoiceStatus) { this.invoiceStatus = invoiceStatus; }
    public LocalDateTime getInvoiceRequestedAt() { return invoiceRequestedAt; }
    public void setInvoiceRequestedAt(LocalDateTime invoiceRequestedAt) { this.invoiceRequestedAt = invoiceRequestedAt; }
    public LocalDateTime getInvoiceIssuedAt() { return invoiceIssuedAt; }
    public void setInvoiceIssuedAt(LocalDateTime invoiceIssuedAt) { this.invoiceIssuedAt = invoiceIssuedAt; }
    public LocalDateTime getInvoiceRejectedAt() { return invoiceRejectedAt; }
    public void setInvoiceRejectedAt(LocalDateTime invoiceRejectedAt) { this.invoiceRejectedAt = invoiceRejectedAt; }
    public String getInvoiceRejectReason() { return invoiceRejectReason; }
    public void setInvoiceRejectReason(String invoiceRejectReason) { this.invoiceRejectReason = invoiceRejectReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public enum InvoiceStatus {
        NOT_REQUESTED,
        PENDING,
        ISSUED,
        REJECTED
    }
}
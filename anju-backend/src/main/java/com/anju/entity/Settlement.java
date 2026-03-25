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

@Entity
@Table(name = "settlements", indexes = {
    @Index(name = "idx_settlement_date", columnList = "settlement_date", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_date", nullable = false, unique = true)
    private LocalDate settlementDate;

    @Column(name = "total_income", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_refunds", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "total_penalties", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalPenalties = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(name = "transaction_count")
    @Builder.Default
    private Integer transactionCount = 0;

    @Column(name = "exception_flag")
    @Builder.Default
    private Boolean exceptionFlag = false;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", length = 20)
    @Builder.Default
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

    public enum InvoiceStatus {
        NOT_REQUESTED,
        PENDING,
        ISSUED,
        REJECTED
    }
}

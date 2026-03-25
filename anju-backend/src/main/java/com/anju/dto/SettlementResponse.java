package com.anju.dto;

import com.anju.entity.Settlement;
import com.anju.entity.Settlement.InvoiceStatus;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class SettlementResponse {
    private Long id;
    private LocalDate settlementDate;
    private BigDecimal totalIncome;
    private BigDecimal totalRefunds;
    private BigDecimal totalPenalties;
    private BigDecimal netAmount;
    private Integer transactionCount;
    private Boolean exceptionFlag;
    private String exceptionMessage;
    private InvoiceStatus invoiceStatus;
    private LocalDateTime invoiceRequestedAt;
    private LocalDateTime invoiceIssuedAt;
    private LocalDateTime invoiceRejectedAt;
    private String invoiceRejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SettlementResponse() {
    }

    public SettlementResponse(Long id, LocalDate settlementDate, BigDecimal totalIncome, 
                               BigDecimal totalRefunds, BigDecimal totalPenalties, 
                               BigDecimal netAmount, Integer transactionCount, 
                               Boolean exceptionFlag, String exceptionMessage, 
                               InvoiceStatus invoiceStatus, LocalDateTime invoiceRequestedAt, 
                               LocalDateTime invoiceIssuedAt, LocalDateTime invoiceRejectedAt, 
                               String invoiceRejectReason, LocalDateTime createdAt, 
                               LocalDateTime updatedAt) {
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
    }

    public static SettlementResponse fromEntity(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .settlementDate(settlement.getSettlementDate())
                .totalIncome(settlement.getTotalIncome())
                .totalRefunds(settlement.getTotalRefunds())
                .totalPenalties(settlement.getTotalPenalties())
                .netAmount(settlement.getNetAmount())
                .transactionCount(settlement.getTransactionCount())
                .exceptionFlag(settlement.getExceptionFlag())
                .exceptionMessage(settlement.getExceptionMessage())
                .invoiceStatus(settlement.getInvoiceStatus())
                .invoiceRequestedAt(settlement.getInvoiceRequestedAt())
                .invoiceIssuedAt(settlement.getInvoiceIssuedAt())
                .invoiceRejectedAt(settlement.getInvoiceRejectedAt())
                .invoiceRejectReason(settlement.getInvoiceRejectReason())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalRefunds() {
        return totalRefunds;
    }

    public void setTotalRefunds(BigDecimal totalRefunds) {
        this.totalRefunds = totalRefunds;
    }

    public BigDecimal getTotalPenalties() {
        return totalPenalties;
    }

    public void setTotalPenalties(BigDecimal totalPenalties) {
        this.totalPenalties = totalPenalties;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Boolean getExceptionFlag() {
        return exceptionFlag;
    }

    public void setExceptionFlag(Boolean exceptionFlag) {
        this.exceptionFlag = exceptionFlag;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public LocalDateTime getInvoiceRequestedAt() {
        return invoiceRequestedAt;
    }

    public void setInvoiceRequestedAt(LocalDateTime invoiceRequestedAt) {
        this.invoiceRequestedAt = invoiceRequestedAt;
    }

    public LocalDateTime getInvoiceIssuedAt() {
        return invoiceIssuedAt;
    }

    public void setInvoiceIssuedAt(LocalDateTime invoiceIssuedAt) {
        this.invoiceIssuedAt = invoiceIssuedAt;
    }

    public LocalDateTime getInvoiceRejectedAt() {
        return invoiceRejectedAt;
    }

    public void setInvoiceRejectedAt(LocalDateTime invoiceRejectedAt) {
        this.invoiceRejectedAt = invoiceRejectedAt;
    }

    public String getInvoiceRejectReason() {
        return invoiceRejectReason;
    }

    public void setInvoiceRejectReason(String invoiceRejectReason) {
        this.invoiceRejectReason = invoiceRejectReason;
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
        private LocalDate settlementDate;
        private BigDecimal totalIncome;
        private BigDecimal totalRefunds;
        private BigDecimal totalPenalties;
        private BigDecimal netAmount;
        private Integer transactionCount;
        private Boolean exceptionFlag;
        private String exceptionMessage;
        private InvoiceStatus invoiceStatus;
        private LocalDateTime invoiceRequestedAt;
        private LocalDateTime invoiceIssuedAt;
        private LocalDateTime invoiceRejectedAt;
        private String invoiceRejectReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder settlementDate(LocalDate settlementDate) {
            this.settlementDate = settlementDate;
            return this;
        }

        public Builder totalIncome(BigDecimal totalIncome) {
            this.totalIncome = totalIncome;
            return this;
        }

        public Builder totalRefunds(BigDecimal totalRefunds) {
            this.totalRefunds = totalRefunds;
            return this;
        }

        public Builder totalPenalties(BigDecimal totalPenalties) {
            this.totalPenalties = totalPenalties;
            return this;
        }

        public Builder netAmount(BigDecimal netAmount) {
            this.netAmount = netAmount;
            return this;
        }

        public Builder transactionCount(Integer transactionCount) {
            this.transactionCount = transactionCount;
            return this;
        }

        public Builder exceptionFlag(Boolean exceptionFlag) {
            this.exceptionFlag = exceptionFlag;
            return this;
        }

        public Builder exceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
            return this;
        }

        public Builder invoiceStatus(InvoiceStatus invoiceStatus) {
            this.invoiceStatus = invoiceStatus;
            return this;
        }

        public Builder invoiceRequestedAt(LocalDateTime invoiceRequestedAt) {
            this.invoiceRequestedAt = invoiceRequestedAt;
            return this;
        }

        public Builder invoiceIssuedAt(LocalDateTime invoiceIssuedAt) {
            this.invoiceIssuedAt = invoiceIssuedAt;
            return this;
        }

        public Builder invoiceRejectedAt(LocalDateTime invoiceRejectedAt) {
            this.invoiceRejectedAt = invoiceRejectedAt;
            return this;
        }

        public Builder invoiceRejectReason(String invoiceRejectReason) {
            this.invoiceRejectReason = invoiceRejectReason;
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

        public SettlementResponse build() {
            return new SettlementResponse(id, settlementDate, totalIncome, 
                                           totalRefunds, totalPenalties, netAmount, 
                                           transactionCount, exceptionFlag, 
                                           exceptionMessage, invoiceStatus, 
                                           invoiceRequestedAt, invoiceIssuedAt, 
                                           invoiceRejectedAt, invoiceRejectReason, 
                                           createdAt, updatedAt);
        }
    }
}

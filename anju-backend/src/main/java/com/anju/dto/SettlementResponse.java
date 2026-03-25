package com.anju.dto;

import com.anju.entity.Settlement;
import com.anju.entity.Settlement.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

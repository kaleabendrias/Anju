package com.anju.dto;

import com.anju.entity.Transaction;
import com.anju.entity.Transaction.PaymentChannel;
import com.anju.entity.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String trxId;
    private Long appointmentId;
    private BigDecimal amount;
    private TransactionType type;
    private PaymentChannel channel;
    private Boolean refundableFlag;
    private Long originalTransactionId;
    private String remark;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .trxId(transaction.getTrxId())
                .appointmentId(transaction.getAppointmentId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .channel(transaction.getChannel())
                .refundableFlag(transaction.getRefundableFlag())
                .originalTransactionId(transaction.getOriginalTransactionId())
                .remark(transaction.getRemark())
                .timestamp(transaction.getTimestamp())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

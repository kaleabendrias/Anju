package com.anju.dto;

import com.anju.entity.Transaction;
import com.anju.entity.Transaction.PaymentChannel;
import com.anju.entity.Transaction.TransactionType;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
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

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, String trxId, Long appointmentId, BigDecimal amount, 
                                TransactionType type, PaymentChannel channel, 
                                Boolean refundableFlag, Long originalTransactionId, 
                                String remark, LocalDateTime timestamp, 
                                LocalDateTime createdAt) {
        this.id = id;
        this.trxId = trxId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.type = type;
        this.channel = channel;
        this.refundableFlag = refundableFlag;
        this.originalTransactionId = originalTransactionId;
        this.remark = remark;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public PaymentChannel getChannel() {
        return channel;
    }

    public void setChannel(PaymentChannel channel) {
        this.channel = channel;
    }

    public Boolean getRefundableFlag() {
        return refundableFlag;
    }

    public void setRefundableFlag(Boolean refundableFlag) {
        this.refundableFlag = refundableFlag;
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(Long originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder trxId(String trxId) {
            this.trxId = trxId;
            return this;
        }

        public Builder appointmentId(Long appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder channel(PaymentChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder refundableFlag(Boolean refundableFlag) {
            this.refundableFlag = refundableFlag;
            return this;
        }

        public Builder originalTransactionId(Long originalTransactionId) {
            this.originalTransactionId = originalTransactionId;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransactionResponse build() {
            return new TransactionResponse(id, trxId, appointmentId, amount, type, 
                                           channel, refundableFlag, originalTransactionId, 
                                           remark, timestamp, createdAt);
        }
    }
}

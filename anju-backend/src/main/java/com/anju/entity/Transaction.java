package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_appointment_id", columnList = "appointment_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trx_id", unique = true, length = 64)
    private String trxId;

    @Column(name = "idempotency_key", unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentChannel channel;

    @Column(name = "refundable_flag")
    private Boolean refundableFlag = true;

    @Column(name = "original_transaction_id")
    private Long originalTransactionId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(length = 255)
    private String remark;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(Long id, String trxId, String idempotencyKey, Long appointmentId, BigDecimal amount,
                       TransactionType type, PaymentChannel channel, Boolean refundableFlag,
                       Long originalTransactionId, Long operatorId, String remark,
                       LocalDateTime timestamp, LocalDateTime createdAt) {
        this.id = id;
        this.trxId = trxId;
        this.idempotencyKey = idempotencyKey;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.type = type;
        this.channel = channel;
        this.refundableFlag = refundableFlag;
        this.originalTransactionId = originalTransactionId;
        this.operatorId = operatorId;
        this.remark = remark;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public static class TransactionBuilder {
        private Long id;
        private String trxId;
        private String idempotencyKey;
        private Long appointmentId;
        private BigDecimal amount;
        private TransactionType type;
        private PaymentChannel channel;
        private Boolean refundableFlag = true;
        private Long originalTransactionId;
        private Long operatorId;
        private String remark;
        private LocalDateTime timestamp;
        private LocalDateTime createdAt;

        public TransactionBuilder id(Long id) { this.id = id; return this; }
        public TransactionBuilder trxId(String trxId) { this.trxId = trxId; return this; }
        public TransactionBuilder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public TransactionBuilder appointmentId(Long appointmentId) { this.appointmentId = appointmentId; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionBuilder channel(PaymentChannel channel) { this.channel = channel; return this; }
        public TransactionBuilder refundableFlag(Boolean refundableFlag) { this.refundableFlag = refundableFlag; return this; }
        public TransactionBuilder originalTransactionId(Long originalTransactionId) { this.originalTransactionId = originalTransactionId; return this; }
        public TransactionBuilder operatorId(Long operatorId) { this.operatorId = operatorId; return this; }
        public TransactionBuilder remark(String remark) { this.remark = remark; return this; }
        public TransactionBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public TransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Transaction build() {
            return new Transaction(id, trxId, idempotencyKey, appointmentId, amount, type, channel,
                refundableFlag, originalTransactionId, operatorId, remark, timestamp, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrxId() { return trxId; }
    public void setTrxId(String trxId) { this.trxId = trxId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public PaymentChannel getChannel() { return channel; }
    public void setChannel(PaymentChannel channel) { this.channel = channel; }
    public Boolean getRefundableFlag() { return refundableFlag; }
    public void setRefundableFlag(Boolean refundableFlag) { this.refundableFlag = refundableFlag; }
    public Long getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(Long originalTransactionId) { this.originalTransactionId = originalTransactionId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public enum TransactionType {
        PAYMENT,
        REFUND,
        PENALTY
    }

    public enum PaymentChannel {
        ALIPAY,
        WECHAT_MOCK,
        CASH,
        BANK_TRANSFER
    }
}
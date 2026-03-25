package com.anju.dto;

import com.anju.entity.Transaction.PaymentChannel;
import com.anju.entity.Transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.math.BigDecimal;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class PaymentRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    
    private Long appointmentId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment channel is required")
    private PaymentChannel channel;
    
    private String remark;

    public PaymentRequest() {
    }

    public PaymentRequest(String idempotencyKey, Long appointmentId, BigDecimal amount, 
                          PaymentChannel channel, String remark) {
        this.idempotencyKey = idempotencyKey;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.channel = channel;
        this.remark = remark;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public PaymentChannel getChannel() {
        return channel;
    }

    public void setChannel(PaymentChannel channel) {
        this.channel = channel;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String idempotencyKey;
        private Long appointmentId;
        private BigDecimal amount;
        private PaymentChannel channel;
        private String remark;

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
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

        public Builder channel(PaymentChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public PaymentRequest build() {
            return new PaymentRequest(idempotencyKey, appointmentId, amount, channel, remark);
        }
    }
}

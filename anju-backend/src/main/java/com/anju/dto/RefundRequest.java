package com.anju.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class RefundRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    
    @NotNull(message = "Original transaction ID is required")
    private Long originalTransactionId;
    
    private String reason;

    public RefundRequest() {
    }

    public RefundRequest(String idempotencyKey, Long originalTransactionId, String reason) {
        this.idempotencyKey = idempotencyKey;
        this.originalTransactionId = originalTransactionId;
        this.reason = reason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(Long originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String idempotencyKey;
        private Long originalTransactionId;
        private String reason;

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        public Builder originalTransactionId(Long originalTransactionId) {
            this.originalTransactionId = originalTransactionId;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public RefundRequest build() {
            return new RefundRequest(idempotencyKey, originalTransactionId, reason);
        }
    }
}

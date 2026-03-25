package com.anju.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    
    @NotNull(message = "Original transaction ID is required")
    private Long originalTransactionId;
    
    private String reason;
}

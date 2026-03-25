package com.anju.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyUpdateRequest {
    @DecimalMin(value = "0.01", message = "Rent must be positive")
    private BigDecimal rent;

    @DecimalMin(value = "0.01", message = "Deposit must be positive")
    private BigDecimal deposit;

    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private String materialsJson;
}

package com.anju.dto;

import jakarta.validation.constraints.*;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class PropertyCreateRequest {
    @NotBlank(message = "Unique code is required")
    private String uniqueCode;

    @NotNull(message = "Rent is required")
    @DecimalMin(value = "0.01", message = "Rent must be positive")
    private BigDecimal rent;

    @NotNull(message = "Deposit is required")
    @DecimalMin(value = "0.01", message = "Deposit must be positive")
    private BigDecimal deposit;

    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private String materialsJson;

    public PropertyCreateRequest() {
    }

    public PropertyCreateRequest(String uniqueCode, BigDecimal rent, BigDecimal deposit, 
                                  LocalDate rentalStartDate, LocalDate rentalEndDate, 
                                  String materialsJson) {
        this.uniqueCode = uniqueCode;
        this.rent = rent;
        this.deposit = deposit;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;
        this.materialsJson = materialsJson;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public BigDecimal getRent() {
        return rent;
    }

    public void setRent(BigDecimal rent) {
        this.rent = rent;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public LocalDate getRentalStartDate() {
        return rentalStartDate;
    }

    public void setRentalStartDate(LocalDate rentalStartDate) {
        this.rentalStartDate = rentalStartDate;
    }

    public LocalDate getRentalEndDate() {
        return rentalEndDate;
    }

    public void setRentalEndDate(LocalDate rentalEndDate) {
        this.rentalEndDate = rentalEndDate;
    }

    public String getMaterialsJson() {
        return materialsJson;
    }

    public void setMaterialsJson(String materialsJson) {
        this.materialsJson = materialsJson;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uniqueCode;
        private BigDecimal rent;
        private BigDecimal deposit;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;
        private String materialsJson;

        public Builder uniqueCode(String uniqueCode) {
            this.uniqueCode = uniqueCode;
            return this;
        }

        public Builder rent(BigDecimal rent) {
            this.rent = rent;
            return this;
        }

        public Builder deposit(BigDecimal deposit) {
            this.deposit = deposit;
            return this;
        }

        public Builder rentalStartDate(LocalDate rentalStartDate) {
            this.rentalStartDate = rentalStartDate;
            return this;
        }

        public Builder rentalEndDate(LocalDate rentalEndDate) {
            this.rentalEndDate = rentalEndDate;
            return this;
        }

        public Builder materialsJson(String materialsJson) {
            this.materialsJson = materialsJson;
            return this;
        }

        public PropertyCreateRequest build() {
            return new PropertyCreateRequest(uniqueCode, rent, deposit, 
                                              rentalStartDate, rentalEndDate, 
                                              materialsJson);
        }
    }
}

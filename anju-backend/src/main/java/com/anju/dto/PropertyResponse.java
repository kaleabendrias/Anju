package com.anju.dto;

import com.anju.entity.Property;
import com.anju.entity.Property.PropertyStatus;
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
public class PropertyResponse {
    private Long id;
    private String uniqueCode;
    private PropertyStatus status;
    private BigDecimal rent;
    private BigDecimal deposit;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private String materialsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PropertyResponse() {
    }

    public PropertyResponse(Long id, String uniqueCode, PropertyStatus status, 
                            BigDecimal rent, BigDecimal deposit, 
                            LocalDate rentalStartDate, LocalDate rentalEndDate, 
                            String materialsJson, LocalDateTime createdAt, 
                            LocalDateTime updatedAt) {
        this.id = id;
        this.uniqueCode = uniqueCode;
        this.status = status;
        this.rent = rent;
        this.deposit = deposit;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;
        this.materialsJson = materialsJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PropertyResponse fromEntity(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .uniqueCode(property.getUniqueCode())
                .status(property.getStatus())
                .rent(property.getRent())
                .deposit(property.getDeposit())
                .rentalStartDate(property.getRentalStartDate())
                .rentalEndDate(property.getRentalEndDate())
                .materialsJson(property.getMaterialsJson())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public void setStatus(PropertyStatus status) {
        this.status = status;
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
        private String uniqueCode;
        private PropertyStatus status;
        private BigDecimal rent;
        private BigDecimal deposit;
        private LocalDate rentalStartDate;
        private LocalDate rentalEndDate;
        private String materialsJson;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder uniqueCode(String uniqueCode) {
            this.uniqueCode = uniqueCode;
            return this;
        }

        public Builder status(PropertyStatus status) {
            this.status = status;
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

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PropertyResponse build() {
            return new PropertyResponse(id, uniqueCode, status, rent, deposit, 
                                         rentalStartDate, rentalEndDate, 
                                         materialsJson, createdAt, updatedAt);
        }
    }
}

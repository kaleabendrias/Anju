package com.anju.dto;

import com.anju.entity.Property;
import com.anju.entity.Property.PropertyStatus;
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
}

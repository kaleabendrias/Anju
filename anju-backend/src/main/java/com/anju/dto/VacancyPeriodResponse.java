package com.anju.dto;

import com.anju.entity.VacancyPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyPeriodResponse {
    private Long id;
    private Long propertyId;
    private String propertyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private boolean currentlyActive;

    public static VacancyPeriodResponse fromEntity(VacancyPeriod vacancyPeriod) {
        return VacancyPeriodResponse.builder()
                .id(vacancyPeriod.getId())
                .propertyId(vacancyPeriod.getProperty() != null ? vacancyPeriod.getProperty().getId() : null)
                .propertyCode(vacancyPeriod.getProperty() != null ? vacancyPeriod.getProperty().getUniqueCode() : null)
                .startDate(vacancyPeriod.getStartDate())
                .endDate(vacancyPeriod.getEndDate())
                .reason(vacancyPeriod.getReason())
                .createdBy(vacancyPeriod.getCreatedBy())
                .createdAt(vacancyPeriod.getCreatedAt())
                .isActive(vacancyPeriod.getIsActive())
                .currentlyActive(vacancyPeriod.isCurrentlyActive())
                .build();
    }
}

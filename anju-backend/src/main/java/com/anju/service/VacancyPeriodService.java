package com.anju.service;

import com.anju.dto.VacancyPeriodCreateRequest;
import com.anju.dto.VacancyPeriodResponse;
import com.anju.dto.VacancyPeriodUpdateRequest;
import com.anju.entity.Property;
import com.anju.entity.VacancyPeriod;
import com.anju.exception.BusinessException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.PropertyRepository;
import com.anju.repository.VacancyPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VacancyPeriodService {

    private static final Logger log = LoggerFactory.getLogger(VacancyPeriodService.class);

    private final VacancyPeriodRepository vacancyPeriodRepository;
    private final PropertyRepository propertyRepository;
    private final AuditLogService auditLogService;

    public VacancyPeriodService(VacancyPeriodRepository vacancyPeriodRepository,
                                PropertyRepository propertyRepository,
                                AuditLogService auditLogService) {
        this.vacancyPeriodRepository = vacancyPeriodRepository;
        this.propertyRepository = propertyRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public VacancyPeriodResponse createVacancyPeriod(VacancyPeriodCreateRequest request, Long operatorId) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + request.getPropertyId()));

        validateDateRange(request.getStartDate(), request.getEndDate(), property.getId());

        VacancyPeriod vacancyPeriod = VacancyPeriod.builder()
                .property(property)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .createdBy(operatorId)
                .isActive(true)
                .build();

        VacancyPeriod saved = vacancyPeriodRepository.save(vacancyPeriod);

        auditLogService.logOperation(
                saved.getId(),
                "VACANCY_PERIOD",
                AuditLogService.OPERATION_CREATE,
                operatorId,
                null,
                String.format("{\"property_id\":\"%d\",\"start_date\":\"%s\",\"end_date\":\"%s\",\"reason\":\"%s\"}",
                        property.getId(), request.getStartDate(), request.getEndDate(), request.getReason()),
                "Created vacancy period for property: " + property.getUniqueCode(),
                null
        );

        log.info("Vacancy period created: id={}, property={}, startDate={}, operator={}",
                saved.getId(), property.getUniqueCode(), request.getStartDate(), operatorId);

        return VacancyPeriodResponse.fromEntity(saved);
    }

    @Transactional
    public VacancyPeriodResponse updateVacancyPeriod(Long id, VacancyPeriodUpdateRequest request, Long operatorId) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        validateDateRange(request.getStartDate(), request.getEndDate(), vacancyPeriod.getProperty().getId());

        String oldValues = String.format("{\"start_date\":\"%s\",\"end_date\":\"%s\",\"reason\":\"%s\",\"is_active\":\"%s\"}",
                vacancyPeriod.getStartDate(), vacancyPeriod.getEndDate(), vacancyPeriod.getReason(), vacancyPeriod.getIsActive());

        vacancyPeriod.setStartDate(request.getStartDate());
        vacancyPeriod.setEndDate(request.getEndDate());
        vacancyPeriod.setReason(request.getReason());
        
        if (request.getIsActive() != null) {
            vacancyPeriod.setIsActive(request.getIsActive());
        }

        VacancyPeriod saved = vacancyPeriodRepository.save(vacancyPeriod);

        auditLogService.logOperation(
                saved.getId(),
                "VACANCY_PERIOD",
                AuditLogService.OPERATION_UPDATE,
                operatorId,
                null,
                oldValues,
                "Updated vacancy period: " + saved.getId(),
                null
        );

        log.info("Vacancy period updated: id={}, startDate={}, operator={}", saved.getId(), request.getStartDate(), operatorId);

        return VacancyPeriodResponse.fromEntity(saved);
    }

    public VacancyPeriodResponse getVacancyPeriodById(Long id) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));
        return VacancyPeriodResponse.fromEntity(vacancyPeriod);
    }

    public List<VacancyPeriodResponse> getVacancyPeriodsByProperty(Long propertyId) {
        List<VacancyPeriod> periods = vacancyPeriodRepository.findByPropertyIdAndIsActiveTrue(propertyId);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getAllVacancyPeriods() {
        return vacancyPeriodRepository.findAll().stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getActiveVacancyPeriodsForProperty(Long propertyId) {
        List<VacancyPeriod> periods = vacancyPeriodRepository.findByPropertyIdAndIsActiveTrue(propertyId);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getCurrentVacancyPeriodsForProperty(Long propertyId, LocalDate date) {
        List<VacancyPeriod> periods = vacancyPeriodRepository.findActiveVacancyForPropertyOnDate(propertyId, date);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getOverlappingVacancyPeriods(Long propertyId, LocalDate startDate, LocalDate endDate) {
        List<VacancyPeriod> periods = vacancyPeriodRepository.findOverlappingVacancies(propertyId, startDate, endDate);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateVacancyPeriod(Long id, Long operatorId) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        vacancyPeriod.setIsActive(false);
        vacancyPeriodRepository.save(vacancyPeriod);

        auditLogService.logOperation(
                vacancyPeriod.getId(),
                "VACANCY_PERIOD",
                "DEACTIVATE",
                operatorId,
                null,
                null,
                "Deactivated vacancy period: " + vacancyPeriod.getId(),
                null
        );

        log.info("Vacancy period deactivated: id={}, operator={}", id, operatorId);
    }

    @Transactional
    public void deleteVacancyPeriod(Long id, Long operatorId) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        vacancyPeriodRepository.delete(vacancyPeriod);

        auditLogService.logOperation(
                vacancyPeriod.getId(),
                "VACANCY_PERIOD",
                AuditLogService.OPERATION_DELETE,
                operatorId,
                null,
                String.format("{\"property_id\":\"%d\",\"start_date\":\"%s\",\"end_date\":\"%s\"}",
                        vacancyPeriod.getProperty().getId(), vacancyPeriod.getStartDate(), vacancyPeriod.getEndDate()),
                "Deleted vacancy period: " + vacancyPeriod.getId(),
                null
        );

        log.info("Vacancy period deleted: id={}, operator={}", id, operatorId);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, Long propertyId) {
        if (startDate == null) {
            throw new BusinessException("Start date is required");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Start date cannot be in the past");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("End date must be after start date");
        }

        if (endDate != null && startDate.equals(endDate)) {
            throw new BusinessException("End date cannot be the same as start date (use single day vacancy with null end date)");
        }

        List<VacancyPeriod> overlapping = vacancyPeriodRepository.findOverlappingVacancies(
                propertyId, startDate, endDate != null ? endDate : startDate);

        if (!overlapping.isEmpty()) {
            throw new BusinessException("Vacancy period overlaps with existing vacancy periods");
        }
    }
}

package com.anju.service;

import com.anju.dto.VacancyPeriodCreateRequest;
import com.anju.dto.VacancyPeriodResponse;
import com.anju.dto.VacancyPeriodUpdateRequest;
import com.anju.entity.Property;
import com.anju.entity.VacancyPeriod;
import com.anju.exception.AccessDeniedException;
import com.anju.exception.BusinessException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.PropertyRepository;
import com.anju.repository.VacancyPeriodRepository;
import com.anju.security.UserPrincipal;
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

        validatePropertyAccess(property, operatorId);
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
    public VacancyPeriodResponse updateVacancyPeriod(Long id, VacancyPeriodUpdateRequest request, UserPrincipal principal) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        validateVacancyPeriodAccess(vacancyPeriod, principal);

        Property property = vacancyPeriod.getProperty();
        validateDateRange(request.getStartDate(), request.getEndDate(), property.getId());

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
                principal.getId(),
                null,
                oldValues,
                "Updated vacancy period: " + saved.getId(),
                null
        );

        log.info("Vacancy period updated: id={}, startDate={}, operator={}", saved.getId(), request.getStartDate(), principal.getId());

        return VacancyPeriodResponse.fromEntity(saved);
    }

    public VacancyPeriodResponse getVacancyPeriodById(Long id, UserPrincipal principal) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        validateVacancyPeriodAccess(vacancyPeriod, principal);
        return VacancyPeriodResponse.fromEntity(vacancyPeriod);
    }

    public List<VacancyPeriodResponse> getVacancyPeriodsByProperty(Long propertyId, UserPrincipal principal) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        validatePropertyAccess(property, principal);

        List<VacancyPeriod> periods = vacancyPeriodRepository.findByPropertyIdAndIsActiveTrue(propertyId);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getAllVacancyPeriods(UserPrincipal principal) {
        List<VacancyPeriod> periods;
        
        if (principal.isAdmin()) {
            periods = vacancyPeriodRepository.findAll();
            log.debug("Admin fetching all vacancy periods: count={}", periods.size());
        } else {
            periods = vacancyPeriodRepository.findByCreatedBy(principal.getId());
            log.debug("User fetching own vacancy periods: userId={}, count={}", principal.getId(), periods.size());
        }
        
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getActiveVacancyPeriodsForProperty(Long propertyId, UserPrincipal principal) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        validatePropertyAccess(property, principal);

        List<VacancyPeriod> periods = vacancyPeriodRepository.findByPropertyIdAndIsActiveTrue(propertyId);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getCurrentVacancyPeriodsForProperty(Long propertyId, LocalDate date, UserPrincipal principal) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        validatePropertyAccess(property, principal);

        List<VacancyPeriod> periods = vacancyPeriodRepository.findActiveVacancyForPropertyOnDate(propertyId, date);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<VacancyPeriodResponse> getOverlappingVacancyPeriods(Long propertyId, LocalDate startDate, LocalDate endDate, UserPrincipal principal) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        validatePropertyAccess(property, principal);

        List<VacancyPeriod> periods = vacancyPeriodRepository.findOverlappingVacancies(propertyId, startDate, endDate);
        return periods.stream()
                .map(VacancyPeriodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateVacancyPeriod(Long id, UserPrincipal principal) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        validateVacancyPeriodAccess(vacancyPeriod, principal);

        vacancyPeriod.setIsActive(false);
        vacancyPeriodRepository.save(vacancyPeriod);

        auditLogService.logOperation(
                vacancyPeriod.getId(),
                "VACANCY_PERIOD",
                "DEACTIVATE",
                principal.getId(),
                null,
                null,
                "Deactivated vacancy period: " + vacancyPeriod.getId(),
                null
        );

        log.info("Vacancy period deactivated: id={}, operator={}", id, principal.getId());
    }

    @Transactional
    public void deleteVacancyPeriod(Long id, UserPrincipal principal) {
        VacancyPeriod vacancyPeriod = vacancyPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacancy period not found: " + id));

        validateVacancyPeriodAccess(vacancyPeriod, principal);

        vacancyPeriodRepository.delete(vacancyPeriod);

        auditLogService.logOperation(
                vacancyPeriod.getId(),
                "VACANCY_PERIOD",
                AuditLogService.OPERATION_DELETE,
                principal.getId(),
                null,
                String.format("{\"property_id\":\"%d\",\"start_date\":\"%s\",\"end_date\":\"%s\"}",
                        vacancyPeriod.getProperty().getId(), vacancyPeriod.getStartDate(), vacancyPeriod.getEndDate()),
                "Deleted vacancy period: " + vacancyPeriod.getId(),
                null
        );

        log.info("Vacancy period deleted: id={}, operator={}", id, principal.getId());
    }

    private void validatePropertyAccess(Property property, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (principal.isAdmin()) {
            return;
        }
        if (property.getOwnerId() == null || !property.getOwnerId().equals(principal.getId())) {
            throw new AccessDeniedException("You do not have permission to access this property's vacancy periods");
        }
    }

    private void validatePropertyAccess(Property property, Long userId) {
        if (userId == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (property.getOwnerId() != null && property.getOwnerId().equals(userId)) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to modify this property's vacancy periods");
    }

    private void validateVacancyPeriodAccess(VacancyPeriod vacancyPeriod, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (principal.isAdmin()) {
            return;
        }
        if (vacancyPeriod.getCreatedBy() == null || !vacancyPeriod.getCreatedBy().equals(principal.getId())) {
            throw new AccessDeniedException("You do not have permission to access this vacancy period");
        }
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

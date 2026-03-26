package com.anju.service;

import com.anju.dto.PropertyCreateRequest;
import com.anju.dto.PropertyResponse;
import com.anju.dto.PropertyUpdateRequest;
import com.anju.entity.Property;
import com.anju.entity.Property.PropertyStatus;
import com.anju.exception.AccessDeniedException;
import com.anju.exception.BusinessException;
import com.anju.exception.ConflictException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.PropertyRepository;
import com.anju.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public PropertyResponse createProperty(PropertyCreateRequest request, Long operatorId) {
        validatePositiveAmount(request.getRent(), "Rent");
        validatePositiveAmount(request.getDeposit(), "Deposit");
        validateRentalDates(request.getRentalStartDate(), request.getRentalEndDate());

        if (propertyRepository.existsByUniqueCode(request.getUniqueCode())) {
            throw new ConflictException("Property with code " + request.getUniqueCode() + " already exists");
        }

        Property property = Property.builder()
                .uniqueCode(request.getUniqueCode())
                .status(PropertyStatus.DRAFT)
                .rent(request.getRent())
                .deposit(request.getDeposit())
                .rentalStartDate(request.getRentalStartDate())
                .rentalEndDate(request.getRentalEndDate())
                .materialsJson(request.getMaterialsJson())
                .ownerId(operatorId)
                .build();

        Property saved = propertyRepository.save(property);
        log.info("Property created: id={}, uniqueCode={}, owner={}", saved.getId(), saved.getUniqueCode(), operatorId);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse updateProperty(Long id, PropertyUpdateRequest request, Long operatorId) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (!canAccessProperty(property, operatorId)) {
            throw new AccessDeniedException("You do not have permission to update this property");
        }

        if (request.getRent() != null) {
            validatePositiveAmount(request.getRent(), "Rent");
            property.setRent(request.getRent());
        }
        if (request.getDeposit() != null) {
            validatePositiveAmount(request.getDeposit(), "Deposit");
            property.setDeposit(request.getDeposit());
        }
        if (request.getRentalStartDate() != null) {
            property.setRentalStartDate(request.getRentalStartDate());
        }
        if (request.getRentalEndDate() != null) {
            property.setRentalEndDate(request.getRentalEndDate());
        }
        if (request.getMaterialsJson() != null) {
            property.setMaterialsJson(request.getMaterialsJson());
        }

        validateRentalDates(property.getRentalStartDate(), property.getRentalEndDate());

        Property saved = propertyRepository.save(property);
        log.info("Property updated: id={}, operator={}", id, operatorId);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse submitForReview(Long id, Long operatorId) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (!canAccessProperty(property, operatorId)) {
            throw new AccessDeniedException("You do not have permission to submit this property for review");
        }

        if (property.getStatus() != PropertyStatus.DRAFT) {
            throw new ConflictException("Only DRAFT properties can be submitted for review");
        }

        property.setStatus(PropertyStatus.PENDING_REVIEW);
        Property saved = propertyRepository.save(property);
        log.info("Property submitted for review: id={}, operator={}", id, operatorId);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse approveProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (property.getStatus() != PropertyStatus.PENDING_REVIEW) {
            throw new ConflictException("Only PENDING_REVIEW properties can be approved");
        }

        property.setStatus(PropertyStatus.LISTED);
        Property saved = propertyRepository.save(property);
        log.info("Property approved and listed: id={}", id);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse rejectProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (property.getStatus() != PropertyStatus.PENDING_REVIEW) {
            throw new ConflictException("Only PENDING_REVIEW properties can be rejected");
        }

        property.setStatus(PropertyStatus.DRAFT);
        Property saved = propertyRepository.save(property);
        log.info("Property rejected: id={}", id);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse delistProperty(Long id, Long operatorId) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (!canAccessProperty(property, operatorId)) {
            throw new AccessDeniedException("You do not have permission to delist this property");
        }

        if (property.getStatus() != PropertyStatus.LISTED) {
            throw new ConflictException("Only LISTED properties can be delisted");
        }

        property.setStatus(PropertyStatus.DELISTED);
        Property saved = propertyRepository.save(property);
        log.info("Property delisted: id={}, operator={}", id, operatorId);
        return PropertyResponse.fromEntity(saved);
    }

    public PropertyResponse getPropertyById(Long id, UserPrincipal principal) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (!canAccessProperty(property, principal)) {
            throw new AccessDeniedException("You do not have permission to view this property");
        }

        return PropertyResponse.fromEntity(property);
    }

    public List<PropertyResponse> getAllProperties(UserPrincipal principal) {
        List<Property> properties;
        
        if (principal.isAdmin()) {
            properties = propertyRepository.findAll();
            log.debug("Admin fetching all properties: count={}", properties.size());
        } else {
            properties = propertyRepository.findByOwnerId(principal.getId());
            log.debug("User fetching own properties: userId={}, count={}", principal.getId(), properties.size());
        }
        
        return properties.stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getPropertiesByStatus(PropertyStatus status, UserPrincipal principal) {
        List<Property> properties;
        
        if (principal.isAdmin()) {
            properties = propertyRepository.findByStatus(status);
            log.debug("Admin fetching properties by status: status={}, count={}", status, properties.size());
        } else {
            properties = propertyRepository.findByStatusAndOwnerId(status, principal.getId());
            log.debug("User fetching own properties by status: userId={}, status={}, count={}", 
                    principal.getId(), status, properties.size());
        }
        
        return properties.stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private boolean canAccessProperty(Property property, Long userId) {
        return property.getOwnerId() != null && property.getOwnerId().equals(userId);
    }

    private boolean canAccessProperty(Property property, UserPrincipal principal) {
        if (principal == null) {
            return false;
        }
        if (principal.isAdmin()) {
            return true;
        }
        return canAccessProperty(property, principal.getId());
    }

    private void validatePositiveAmount(java.math.BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException(fieldName + " must be positive");
        }
    }

    private void validateRentalDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new BusinessException("Rental end date must be after rental start date");
        }
    }
}

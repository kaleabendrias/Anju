package com.anju.service;

import com.anju.dto.PropertyCreateRequest;
import com.anju.dto.PropertyResponse;
import com.anju.dto.PropertyUpdateRequest;
import com.anju.entity.Property;
import com.anju.entity.Property.PropertyStatus;
import com.anju.exception.BusinessException;
import com.anju.exception.ConflictException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional
    public PropertyResponse createProperty(PropertyCreateRequest request) {
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
                .build();

        Property saved = propertyRepository.save(property);
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse updateProperty(Long id, PropertyUpdateRequest request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

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
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse submitForReview(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (property.getStatus() != PropertyStatus.DRAFT) {
            throw new ConflictException("Only DRAFT properties can be submitted for review");
        }

        property.setStatus(PropertyStatus.PENDING_REVIEW);
        Property saved = propertyRepository.save(property);
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
        return PropertyResponse.fromEntity(saved);
    }

    @Transactional
    public PropertyResponse delistProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        if (property.getStatus() != PropertyStatus.LISTED) {
            throw new ConflictException("Only LISTED properties can be delisted");
        }

        property.setStatus(PropertyStatus.DELISTED);
        Property saved = propertyRepository.save(property);
        return PropertyResponse.fromEntity(saved);
    }

    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return PropertyResponse.fromEntity(property);
    }

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll().stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getPropertiesByStatus(PropertyStatus status) {
        return propertyRepository.findByStatus(status).stream()
                .map(PropertyResponse::fromEntity)
                .collect(Collectors.toList());
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

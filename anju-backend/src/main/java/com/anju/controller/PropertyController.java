package com.anju.controller;

import com.anju.dto.ApiResponse;
import com.anju.dto.PropertyCreateRequest;
import com.anju.dto.PropertyResponse;
import com.anju.dto.PropertyUpdateRequest;
import com.anju.entity.Property.PropertyStatus;
import com.anju.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping("/api/admin/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyCreateRequest request) {
        PropertyResponse response = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", response));
    }

    @PutMapping("/api/admin/properties/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyUpdateRequest request) {
        PropertyResponse response = propertyService.updateProperty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", response));
    }

    @PostMapping("/api/admin/properties/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<PropertyResponse>> submitForReview(@PathVariable Long id) {
        PropertyResponse response = propertyService.submitForReview(id);
        return ResponseEntity.ok(ApiResponse.success("Property submitted for review", response));
    }

    @PostMapping("/api/reviewer/properties/{id}/approve")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse<PropertyResponse>> approveProperty(@PathVariable Long id) {
        PropertyResponse response = propertyService.approveProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property approved and listed", response));
    }

    @PostMapping("/api/reviewer/properties/{id}/reject")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse<PropertyResponse>> rejectProperty(@PathVariable Long id) {
        PropertyResponse response = propertyService.rejectProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property rejected", response));
    }

    @PostMapping("/api/admin/properties/{id}/delist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> delistProperty(@PathVariable Long id) {
        PropertyResponse response = propertyService.delistProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property delisted", response));
    }

    @GetMapping("/api/properties/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getProperty(@PathVariable Long id) {
        PropertyResponse response = propertyService.getPropertyById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/properties")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties(
            @RequestParam(required = false) PropertyStatus status) {
        List<PropertyResponse> properties;
        if (status != null) {
            properties = propertyService.getPropertiesByStatus(status);
        } else {
            properties = propertyService.getAllProperties();
        }
        return ResponseEntity.ok(ApiResponse.success(properties));
    }
}

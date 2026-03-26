package com.anju.controller;

import com.anju.dto.ApiResponse;
import com.anju.dto.PropertyCreateRequest;
import com.anju.dto.PropertyResponse;
import com.anju.dto.PropertyUpdateRequest;
import com.anju.entity.Property.PropertyStatus;
import com.anju.exception.AccessDeniedException;
import com.anju.security.UserPrincipal;
import com.anju.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping("/api/admin/properties")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PropertyCreateRequest request) {
        PropertyResponse response = propertyService.createProperty(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", response));
    }

    @PutMapping("/api/admin/properties/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody PropertyUpdateRequest request) {
        PropertyResponse response = propertyService.updateProperty(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", response));
    }

    @PostMapping("/api/admin/properties/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<PropertyResponse>> submitForReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        PropertyResponse response = propertyService.submitForReview(id, principal.getId());
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
    public ResponseEntity<ApiResponse<PropertyResponse>> delistProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        PropertyResponse response = propertyService.delistProperty(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Property delisted", response));
    }

    @GetMapping("/api/properties/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        PropertyResponse response = propertyService.getPropertyById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/properties")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) PropertyStatus status) {
        List<PropertyResponse> properties;
        if (status != null) {
            properties = propertyService.getPropertiesByStatus(status, principal);
        } else {
            properties = propertyService.getAllProperties(principal);
        }
        return ResponseEntity.ok(ApiResponse.success(properties));
    }
}

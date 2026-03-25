package com.anju.controller;

import com.anju.dto.ApiResponse;
import com.anju.dto.VacancyPeriodCreateRequest;
import com.anju.dto.VacancyPeriodResponse;
import com.anju.dto.VacancyPeriodUpdateRequest;
import com.anju.security.UserPrincipal;
import com.anju.service.VacancyPeriodService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vacancy-periods")
public class VacancyPeriodController {

    private final VacancyPeriodService vacancyPeriodService;

    public VacancyPeriodController(VacancyPeriodService vacancyPeriodService) {
        this.vacancyPeriodService = vacancyPeriodService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<VacancyPeriodResponse>> createVacancyPeriod(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VacancyPeriodCreateRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        VacancyPeriodResponse response = vacancyPeriodService.createVacancyPeriod(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vacancy period created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<VacancyPeriodResponse>> updateVacancyPeriod(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody VacancyPeriodUpdateRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        VacancyPeriodResponse response = vacancyPeriodService.updateVacancyPeriod(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Vacancy period updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VacancyPeriodResponse>> getVacancyPeriodById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        VacancyPeriodResponse response = vacancyPeriodService.getVacancyPeriodById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<List<VacancyPeriodResponse>>> getAllVacancyPeriods(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<VacancyPeriodResponse> response = vacancyPeriodService.getAllVacancyPeriods();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<VacancyPeriodResponse>>> getVacancyPeriodsByProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long propertyId) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<VacancyPeriodResponse> response = vacancyPeriodService.getVacancyPeriodsByProperty(propertyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/property/{propertyId}/active")
    public ResponseEntity<ApiResponse<List<VacancyPeriodResponse>>> getActiveVacancyPeriodsForProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long propertyId) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<VacancyPeriodResponse> response = vacancyPeriodService.getActiveVacancyPeriodsForProperty(propertyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/property/{propertyId}/on-date")
    public ResponseEntity<ApiResponse<List<VacancyPeriodResponse>>> getCurrentVacancyPeriodsForProperty(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<VacancyPeriodResponse> response = vacancyPeriodService.getCurrentVacancyPeriodsForProperty(propertyId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/property/{propertyId}/overlapping")
    public ResponseEntity<ApiResponse<List<VacancyPeriodResponse>>> getOverlappingVacancyPeriods(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<VacancyPeriodResponse> response = vacancyPeriodService.getOverlappingVacancyPeriods(propertyId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<Void>> deleteVacancyPeriod(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        vacancyPeriodService.deleteVacancyPeriod(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Vacancy period deleted successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<Void>> deactivateVacancyPeriod(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        vacancyPeriodService.deactivateVacancyPeriod(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Vacancy period deactivated successfully", null));
    }
}

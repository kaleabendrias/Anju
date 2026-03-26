package com.anju.controller;

import com.anju.dto.*;
import com.anju.entity.Appointment.AppointmentStatus;
import com.anju.security.UserPrincipal;
import com.anju.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/api/admin/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAdminAppointments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AppointmentResponse> appointments;
        if (status != null) {
            appointments = appointmentService.getAppointmentsByStatus(status, principal.getId(), principal.getRole(), pageable);
        } else {
            appointments = appointmentService.getAllAppointments(principal.getId(), principal.getRole(), pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @PostMapping("/api/admin/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AppointmentCreateRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.createAppointment(
                request, principal.getId(), principal.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created successfully", response));
    }

    @GetMapping("/api/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.getAppointmentById(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAllAppointments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AppointmentResponse> appointments;
        if (status != null) {
            appointments = appointmentService.getAppointmentsByStatus(status, principal.getId(), principal.getRole(), pageable);
        } else {
            appointments = appointmentService.getAllAppointments(principal.getId(), principal.getRole(), pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }

    @PostMapping("/api/appointments/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.cancelAppointment(
                id, principal.getId(), principal.getRole(), reason);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", response));
    }

    @PostMapping("/api/appointments/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRescheduleRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.rescheduleAppointment(
                id, request, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled", response));
    }

    @PostMapping("/api/appointments/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirmAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.confirmAppointment(
                id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Appointment confirmed", response));
    }

    @PostMapping("/api/appointments/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> startAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.startAppointment(
                id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Appointment started", response));
    }

    @PostMapping("/api/appointments/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.completeAppointment(
                id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Appointment completed", response));
    }

    @PostMapping("/api/appointments/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> markNoShow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        AppointmentResponse response = appointmentService.markNoShow(
                id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Appointment marked as no-show", response));
    }
}

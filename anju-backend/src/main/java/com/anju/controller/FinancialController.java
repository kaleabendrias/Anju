package com.anju.controller;

import com.anju.dto.*;
import com.anju.security.UserPrincipal;
import com.anju.service.FinancialService;
import com.anju.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;
    private final SettlementService settlementService;

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'FRONTLINE')")
    public ResponseEntity<ApiResponse<TransactionResponse>> recordPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        TransactionResponse response = financialService.recordPayment(request, principal.getId(), 
                principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", response));
    }

    @PostMapping("/refunds")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<TransactionResponse>> processRefund(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RefundRequest request,
            @RequestParam(required = false) String secondaryPassword) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        TransactionResponse response = financialService.processRefund(request, principal.getId(),
                principal.getUsername(), principal.getRole(), secondaryPassword);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<TransactionResponse> transactions = financialService.getTransactionsByDateRange(startDate, endDate,
                principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        TransactionResponse response = financialService.getTransactionById(id, principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions/by-trx-id/{trxId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByTrxId(@PathVariable String trxId) {
        TransactionResponse response = financialService.getTransactionByTrxId(trxId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/settlements")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByDateRange(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<SettlementResponse> settlements = settlementService.getSettlementsByDateRange(startDate, endDate,
                principal.getId(), principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(settlements));
    }

    @GetMapping("/settlements/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlementByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        SettlementResponse response = settlementService.getSettlementByDate(date, principal.getId(), 
                principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/settlements/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<SettlementResponse>> generateSettlement(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        SettlementResponse response = settlementService.generateDailyStatement(date, principal.getId(), 
                principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Settlement generated for " + date, response));
    }

    @PostMapping("/settlements/{id}/request-invoice")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<SettlementResponse>> requestInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        SettlementResponse response = settlementService.requestInvoice(id, principal.getId(), 
                principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Invoice requested", response));
    }

    @PostMapping("/settlements/{id}/issue-invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettlementResponse>> issueInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) String secondaryPassword) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        SettlementResponse response = settlementService.issueInvoice(id, principal.getId(), 
                principal.getUsername(), secondaryPassword, principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Invoice issued", response));
    }

    @PostMapping("/settlements/{id}/reject-invoice")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SettlementResponse>> rejectInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String secondaryPassword) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        SettlementResponse response = settlementService.rejectInvoice(id, reason, principal.getId(),
                principal.getUsername(), secondaryPassword, principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Invoice rejected", response));
    }

    @GetMapping("/export/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> exportTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<TransactionResponse> transactions = financialService.getTransactionsByDateRange(startDate, endDate,
                principal.getId(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Exported " + transactions.size() + " transactions", transactions));
    }

    @GetMapping("/export/settlements")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> exportSettlements(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }
        List<SettlementResponse> settlements = settlementService.getSettlementsByDateRange(startDate, endDate,
                principal.getId(), principal.getUsername(), principal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Exported " + settlements.size() + " settlements", settlements));
    }
}

package com.anju.service;

import com.anju.dto.*;
import com.anju.entity.Settlement;
import com.anju.entity.Settlement.InvoiceStatus;
import com.anju.exception.BusinessException;
import com.anju.exception.ForbiddenException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.SettlementRepository;
import com.anju.repository.TransactionRepository;
import com.anju.security.SecondaryVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementRepository settlementRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;
    private final SecondaryVerificationService secondaryVerificationService;

    public SettlementService(SettlementRepository settlementRepository,
                            TransactionRepository transactionRepository,
                            AuditLogService auditLogService,
                            SecondaryVerificationService secondaryVerificationService) {
        this.settlementRepository = settlementRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
        this.secondaryVerificationService = secondaryVerificationService;
    }

    @Transactional
    public SettlementResponse generateDailyStatement(LocalDate date, Long operatorId, String operatorUsername, String operatorRole) {
        log.info("Generating daily settlement for date: {}", date);
        validateFinanceRole(operatorRole);
        
        if (settlementRepository.existsBySettlementDate(date)) {
            log.info("Settlement already exists for date: {}", date);
            return SettlementResponse.fromEntity(
                    settlementRepository.findBySettlementDate(date).orElseThrow());
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndTimestampBetween(
                startOfDay, endOfDay, com.anju.entity.Transaction.TransactionType.PAYMENT);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        List<com.anju.entity.Transaction> refunds = transactionRepository.findByTimestampBetween(startOfDay, endOfDay)
                .stream()
                .filter(t -> t.getType() == com.anju.entity.Transaction.TransactionType.REFUND)
                .toList();
        
        BigDecimal totalRefunds = refunds.stream()
                .map(com.anju.entity.Transaction::getAmount)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b.abs()));

        BigDecimal totalPenalties = transactionRepository.sumAmountByTypeAndTimestampBetween(
                startOfDay, endOfDay, com.anju.entity.Transaction.TransactionType.PENALTY);
        if (totalPenalties == null) totalPenalties = BigDecimal.ZERO;

        int transactionCount = transactionRepository.countByTimestampBetween(startOfDay, endOfDay);

        StringBuilder exceptionMessage = new StringBuilder();
        boolean hasException = checkForExceptions(date, startOfDay, endOfDay, totalIncome, totalRefunds, 
                exceptionMessage);

        BigDecimal netAmount = totalIncome.subtract(totalRefunds.abs()).add(totalPenalties);

        log.debug("Settlement calculation: date={}, income={}, refunds={}, penalties={}, net={}", 
                date, totalIncome, totalRefunds, totalPenalties, netAmount);

        Settlement settlement = Settlement.builder()
                .settlementDate(date)
                .totalIncome(totalIncome)
                .totalRefunds(totalRefunds)
                .totalPenalties(totalPenalties)
                .netAmount(netAmount)
                .transactionCount(transactionCount)
                .exceptionFlag(hasException)
                .exceptionMessage(hasException ? exceptionMessage.toString() : null)
                .generatedBy(operatorUsername != null ? operatorUsername : "DailyStatementTask")
                .build();

        Settlement saved = settlementRepository.save(settlement);
        
        auditLogService.logOperation(
                saved.getId(),
                "SETTLEMENT",
                AuditLogService.OPERATION_CREATE,
                operatorId,
                operatorUsername,
                String.format("{\"date\":\"%s\",\"income\":\"%s\",\"refunds\":\"%s\",\"penalties\":\"%s\"}", 
                        date, totalIncome, totalRefunds, totalPenalties),
                "Generated daily settlement for " + date,
                null
        );

        log.info("Daily settlement generated successfully: date={}, id={}, netAmount={}", 
                date, saved.getId(), netAmount);

        return SettlementResponse.fromEntity(saved);
    }

    @Transactional
    public SettlementResponse requestInvoice(Long settlementId, Long operatorId, String operatorUsername, String operatorRole) {
        validateFinanceRole(operatorRole);
        
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));

        if (settlement.getInvoiceStatus() != InvoiceStatus.NOT_REQUESTED) {
            throw new BusinessException("Invoice has already been requested or processed");
        }

        settlement.setInvoiceStatus(InvoiceStatus.PENDING);
        settlement.setInvoiceRequestedAt(LocalDateTime.now());

        Settlement saved = settlementRepository.save(settlement);
        
        auditLogService.logOperation(
                saved.getId(),
                "SETTLEMENT",
                "INVOICE_REQUEST",
                operatorId,
                operatorUsername,
                null,
                "Invoice requested for settlement: " + settlementId,
                null
        );

        return SettlementResponse.fromEntity(saved);
    }

    @Transactional
    public SettlementResponse issueInvoice(Long settlementId, Long operatorId, String operatorUsername, 
            String secondaryPassword, String operatorRole) {
        
        if (!secondaryVerificationService.verifySecondaryPassword(operatorUsername, secondaryPassword)) {
            throw new ForbiddenException("Secondary password verification failed for invoice issuance");
        }

        validateAdminRole(operatorRole);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));

        if (settlement.getInvoiceStatus() != InvoiceStatus.PENDING) {
            throw new BusinessException("Invoice must be in PENDING status to issue");
        }

        settlement.setInvoiceStatus(InvoiceStatus.ISSUED);
        settlement.setInvoiceIssuedAt(LocalDateTime.now());

        Settlement saved = settlementRepository.save(settlement);
        
        auditLogService.logOperation(
                saved.getId(),
                "SETTLEMENT",
                "INVOICE_ISSUE",
                operatorId,
                operatorUsername,
                null,
                "Invoice issued for settlement: " + settlementId + " by " + operatorUsername,
                null
        );

        return SettlementResponse.fromEntity(saved);
    }

    @Transactional
    public SettlementResponse rejectInvoice(Long settlementId, String reason, Long operatorId, 
            String operatorUsername, String secondaryPassword, String operatorRole) {
        
        if (!secondaryVerificationService.verifySecondaryPassword(operatorUsername, secondaryPassword)) {
            throw new ForbiddenException("Secondary password verification failed for invoice rejection");
        }

        validateAdminRole(operatorRole);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found: " + settlementId));

        if (settlement.getInvoiceStatus() != InvoiceStatus.PENDING) {
            throw new BusinessException("Invoice must be in PENDING status to reject");
        }

        settlement.setInvoiceStatus(InvoiceStatus.REJECTED);
        settlement.setInvoiceRejectedAt(LocalDateTime.now());
        settlement.setInvoiceRejectReason(reason);

        Settlement saved = settlementRepository.save(settlement);
        
        auditLogService.logOperation(
                saved.getId(),
                "SETTLEMENT",
                "INVOICE_REJECT",
                operatorId,
                operatorUsername,
                String.format("{\"reason\":\"%s\"}", reason),
                "Invoice rejected for settlement: " + settlementId + " by " + operatorUsername,
                null
        );

        return SettlementResponse.fromEntity(saved);
    }

    public List<SettlementResponse> getSettlementsByDateRange(LocalDate startDate, LocalDate endDate,
            Long operatorId, String operatorUsername, String operatorRole) {
        validateFinanceRole(operatorRole);
        
        return settlementRepository.findAll().stream()
                .filter(s -> !s.getSettlementDate().isBefore(startDate) && 
                            !s.getSettlementDate().isAfter(endDate))
                .map(SettlementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SettlementResponse getSettlementByDate(LocalDate date, Long operatorId, String operatorUsername, String operatorRole) {
        validateFinanceRole(operatorRole);
        
        Settlement settlement = settlementRepository.findBySettlementDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found for date: " + date));
        return SettlementResponse.fromEntity(settlement);
    }

    private void validateFinanceRole(String operatorRole) {
        if (operatorRole == null) {
            throw new ForbiddenException("Access denied: role is required for finance operations");
        }
        if (!"ADMIN".equals(operatorRole) && !"FINANCE".equals(operatorRole)) {
            throw new ForbiddenException("Access denied: finance operations require ADMIN or FINANCE role");
        }
    }

    private void validateAdminRole(String operatorRole) {
        if (operatorRole == null) {
            throw new ForbiddenException("Access denied: role is required for admin operations");
        }
        if (!"ADMIN".equals(operatorRole)) {
            throw new ForbiddenException("Access denied: this operation requires ADMIN role");
        }
    }

    private boolean checkForExceptions(LocalDate date, LocalDateTime startOfDay, 
            LocalDateTime endOfDay, BigDecimal totalIncome, BigDecimal totalRefunds,
            StringBuilder exceptionMessage) {
        boolean hasException = false;

        if (totalRefunds.abs().compareTo(totalIncome) > 0) {
            hasException = true;
            exceptionMessage.append("Total refunds exceed total income. ");
        }

        return hasException;
    }
}

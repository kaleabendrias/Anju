package com.anju.service;

import com.anju.dto.*;
import com.anju.entity.Transaction;
import com.anju.entity.Transaction.TransactionType;
import com.anju.exception.BusinessException;
import com.anju.exception.ForbiddenException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.TransactionRepository;
import com.anju.security.SecondaryVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinancialService {

    private static final Logger log = LoggerFactory.getLogger(FinancialService.class);

    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;
    private final SecondaryVerificationService secondaryVerificationService;

    public FinancialService(TransactionRepository transactionRepository,
                          AuditLogService auditLogService,
                          SecondaryVerificationService secondaryVerificationService) {
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
        this.secondaryVerificationService = secondaryVerificationService;
    }

    @Transactional
    public TransactionResponse recordPayment(PaymentRequest request, Long operatorId, 
            String operatorUsername, String operatorRole) {
        log.info("Recording payment: idempotencyKey={}, appointmentId={}, amount={}", 
                request.getIdempotencyKey(), request.getAppointmentId(), request.getAmount());

        if (transactionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            log.warn("Duplicate payment detected with idempotency key: {}", request.getIdempotencyKey());
            Transaction existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
            return TransactionResponse.fromEntity(existing);
        }

        Transaction transaction = Transaction.builder()
                .trxId(generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .appointmentId(request.getAppointmentId())
                .amount(request.getAmount())
                .type(TransactionType.PAYMENT)
                .channel(request.getChannel())
                .refundableFlag(true)
                .operatorId(operatorId)
                .remark(request.getRemark())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.debug("Payment saved: id={}, trxId={}", saved.getId(), saved.getTrxId());
        
        auditLogService.logOperation(
                saved.getId(),
                "TRANSACTION",
                AuditLogService.OPERATION_PAYMENT,
                operatorId,
                operatorUsername,
                String.format("{\"trx_id\":\"%s\",\"amount\":\"%s\",\"channel\":\"%s\"}", 
                        saved.getTrxId(), saved.getAmount(), saved.getChannel()),
                "Recorded payment: " + saved.getTrxId(),
                null
        );

        log.info("Payment recorded successfully: trxId={}, amount={}, channel={}", 
                saved.getTrxId(), saved.getAmount(), saved.getChannel());
        
        return TransactionResponse.fromEntity(saved);
    }

    @Transactional
    public TransactionResponse processRefund(RefundRequest request, Long operatorId,
            String operatorUsername, String operatorRole, String secondaryPassword) {
        log.info("Processing refund: idempotencyKey={}, originalTransactionId={}", 
                request.getIdempotencyKey(), request.getOriginalTransactionId());
        
        if (!secondaryVerificationService.verifySecondaryPassword(operatorUsername, secondaryPassword)) {
            throw new ForbiddenException("Secondary password verification failed for refund operation");
        }

        if (transactionRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            log.warn("Duplicate refund detected with idempotency key: {}", request.getIdempotencyKey());
            Transaction existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
            return TransactionResponse.fromEntity(existing);
        }

        Transaction originalTransaction = transactionRepository.findById(request.getOriginalTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Original transaction not found: " + request.getOriginalTransactionId()));

        if (originalTransaction.getType() != TransactionType.PAYMENT) {
            throw new BusinessException("Can only refund PAYMENT transactions");
        }

        if (!originalTransaction.getRefundableFlag()) {
            throw new BusinessException("This transaction is not refundable");
        }

        List<Transaction> existingRefunds = transactionRepository
                .findRefundsByOriginalTransactionId(originalTransaction.getId());
        
        BigDecimal totalRefunded = existingRefunds.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal remainingAmount = originalTransaction.getAmount().subtract(totalRefunded.abs());
        
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Full refund already processed for this transaction");
        }

        Transaction refundTransaction = Transaction.builder()
                .trxId(generateTransactionId())
                .idempotencyKey(request.getIdempotencyKey())
                .appointmentId(originalTransaction.getAppointmentId())
                .amount(remainingAmount.negate())
                .type(TransactionType.REFUND)
                .channel(originalTransaction.getChannel())
                .refundableFlag(false)
                .originalTransactionId(originalTransaction.getId())
                .operatorId(operatorId)
                .remark("Refund for " + originalTransaction.getTrxId() + ". Reason: " + 
                        (request.getReason() != null ? request.getReason() : "Customer request"))
                .build();

        Transaction saved = transactionRepository.save(refundTransaction);
        
        String fieldChanges = String.format(
                "{\"original_trx_id\":\"%s\",\"original_amount\":\"%s\",\"refund_amount\":\"%s\"}",
                originalTransaction.getTrxId(),
                originalTransaction.getAmount(),
                remainingAmount
        );
        
        auditLogService.logOperation(
                saved.getId(),
                "TRANSACTION",
                AuditLogService.OPERATION_REFUND,
                operatorId,
                operatorUsername,
                fieldChanges,
                "Processed refund: " + saved.getTrxId() + ", amount: " + saved.getAmount(),
                null
        );

        log.info("Refund processed successfully: trxId={}, originalTrxId={}, amount={}", 
                saved.getTrxId(), originalTransaction.getTrxId(), saved.getAmount());
        
        return TransactionResponse.fromEntity(saved);
    }

    public List<TransactionResponse> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate,
            Long operatorId, String operatorRole) {
        validateFinanceAccess(operatorRole);
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        log.debug("Fetching transactions: startDate={}, endDate={}", start, end);
        
        return transactionRepository.findByTimestampBetween(start, end).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id, Long operatorId, String operatorRole) {
        validateFinanceAccess(operatorRole);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        return TransactionResponse.fromEntity(transaction);
    }

    public TransactionResponse getTransactionByTrxId(String trxId) {
        Transaction transaction = transactionRepository.findByTrxId(trxId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with trxId: " + trxId));
        return TransactionResponse.fromEntity(transaction);
    }

    private void validateFinanceAccess(String operatorRole) {
        if (!"ADMIN".equals(operatorRole) && !"FINANCE".equals(operatorRole)) {
            throw new ForbiddenException("Access denied: finance operations require ADMIN or FINANCE role");
        }
    }

    private String generateTransactionId() {
        return "TRX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

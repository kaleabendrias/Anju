package com.anju.service;

import com.anju.dto.SettlementResponse;
import com.anju.entity.Settlement;
import com.anju.entity.Transaction;
import com.anju.entity.Transaction.TransactionType;
import com.anju.entity.Transaction.PaymentChannel;
import com.anju.repository.SettlementRepository;
import com.anju.repository.TransactionRepository;
import com.anju.security.SecondaryVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialCriticalPathTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecondaryVerificationService secondaryVerificationService;

    @InjectMocks
    private SettlementService settlementService;

    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
    }

    @Test
    @DisplayName("generateDailyStatement - netAmount = Income - Refunds + Penalties")
    void generateDailyStatement_shouldCalculateNetAmountCorrectly() {
        BigDecimal income = new BigDecimal("1000.00");
        BigDecimal refundAmount = new BigDecimal("200.00");
        BigDecimal penaltyAmount = new BigDecimal("50.00");
        BigDecimal expectedNetAmount = new BigDecimal("850.00");

        LocalDateTime startOfDay = testDate.atStartOfDay();
        LocalDateTime endOfDay = testDate.atTime(LocalTime.MAX);

        when(settlementRepository.existsBySettlementDate(testDate)).thenReturn(false);
        when(transactionRepository.sumAmountByTypeAndTimestampBetween(
                eq(startOfDay), eq(endOfDay), eq(TransactionType.PAYMENT)))
                .thenReturn(income);
        when(transactionRepository.sumAmountByTypeAndTimestampBetween(
                eq(startOfDay), eq(endOfDay), eq(TransactionType.PENALTY)))
                .thenReturn(penaltyAmount);

        Transaction refundTransaction = Transaction.builder()
                .id(2L)
                .amount(refundAmount)
                .type(TransactionType.REFUND)
                .channel(PaymentChannel.ALIPAY)
                .timestamp(testDate.atTime(10, 0))
                .build();

        when(transactionRepository.findByTimestampBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(List.of(refundTransaction));

        when(transactionRepository.countByTimestampBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(2);

        when(settlementRepository.save(any())).thenAnswer(invocation -> {
            var settlement = invocation.getArgument(0);
            var field = settlement.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(settlement, 1L);
            return settlement;
        });

        SettlementResponse result = settlementService.generateDailyStatement(testDate, 1L, "admin", "ADMIN");

        assertNotNull(result);
        assertEquals(income, result.getTotalIncome());
        assertEquals(refundAmount, result.getTotalRefunds());
        assertEquals(penaltyAmount, result.getTotalPenalties());
        assertEquals(expectedNetAmount, result.getNetAmount());

        verify(settlementRepository).save(any());
    }

    @Test
    @DisplayName("generateDailyStatement - zero refunds should not affect netAmount")
    void generateDailyStatement_withNoRefunds_shouldEqualIncomePlusPenalties() {
        BigDecimal income = new BigDecimal("500.00");
        BigDecimal penaltyAmount = new BigDecimal("30.00");
        BigDecimal expectedNetAmount = new BigDecimal("530.00");

        LocalDateTime startOfDay = testDate.atStartOfDay();
        LocalDateTime endOfDay = testDate.atTime(LocalTime.MAX);

        when(settlementRepository.existsBySettlementDate(testDate)).thenReturn(false);
        when(transactionRepository.sumAmountByTypeAndTimestampBetween(
                eq(startOfDay), eq(endOfDay), eq(TransactionType.PAYMENT)))
                .thenReturn(income);
        when(transactionRepository.sumAmountByTypeAndTimestampBetween(
                eq(startOfDay), eq(endOfDay), eq(TransactionType.PENALTY)))
                .thenReturn(penaltyAmount);
        when(transactionRepository.findByTimestampBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(List.of());
        when(transactionRepository.countByTimestampBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(1);
        when(settlementRepository.save(any())).thenAnswer(invocation -> {
            var settlement = invocation.getArgument(0);
            var field = settlement.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(settlement, 1L);
            return settlement;
        });

        SettlementResponse result = settlementService.generateDailyStatement(testDate, 1L, "admin", "ADMIN");

        assertEquals(expectedNetAmount, result.getNetAmount());
    }

    @Test
    @DisplayName("generateDailyStatement - multiple refunds should sum correctly")
    void generateDailyStatement_withMultipleRefunds_shouldSumRefunds() {
        BigDecimal income = new BigDecimal("1000.00");
        BigDecimal refund1 = new BigDecimal("100.00");
        BigDecimal refund2 = new BigDecimal("150.00");
        BigDecimal penaltyAmount = new BigDecimal("50.00");
        BigDecimal totalRefunds = refund1.add(refund2);
        BigDecimal expectedNetAmount = new BigDecimal("800.00");

        LocalDateTime startOfDay = testDate.atStartOfDay();
        LocalDateTime endOfDay = testDate.atTime(LocalTime.MAX);

        when(settlementRepository.existsBySettlementDate(testDate)).thenReturn(false);
        when(transactionRepository.sumAmountByTypeAndTimestampBetween(
                eq(startOfDay), eq(endOfDay), eq(TransactionType.PAYMENT)))
                .thenReturn(income);
        when(transactionRepository.sumAmountByTypeAndTimestampBetween(
                eq(startOfDay), eq(endOfDay), eq(TransactionType.PENALTY)))
                .thenReturn(penaltyAmount);

        List<Transaction> refunds = List.of(
                Transaction.builder().id(2L).amount(refund1).type(TransactionType.REFUND)
                        .channel(PaymentChannel.ALIPAY).timestamp(testDate.atTime(10, 0)).build(),
                Transaction.builder().id(3L).amount(refund2).type(TransactionType.REFUND)
                        .channel(PaymentChannel.WECHAT_MOCK).timestamp(testDate.atTime(14, 0)).build()
        );

        when(transactionRepository.findByTimestampBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(refunds);
        when(transactionRepository.countByTimestampBetween(eq(startOfDay), eq(endOfDay)))
                .thenReturn(3);
        when(settlementRepository.save(any())).thenAnswer(invocation -> {
            var settlement = invocation.getArgument(0);
            var field = settlement.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(settlement, 1L);
            return settlement;
        });

        SettlementResponse result = settlementService.generateDailyStatement(testDate, 1L, "admin", "ADMIN");

        assertEquals(totalRefunds, result.getTotalRefunds());
        assertEquals(expectedNetAmount, result.getNetAmount());
    }

    @Test
    @DisplayName("generateDailyStatement - returns existing settlement if date already processed")
    void generateDailyStatement_shouldReturnExistingIfAlreadyProcessed() {
        Settlement existingSettlement = Settlement.builder()
                .id(1L)
                .settlementDate(testDate)
                .totalIncome(BigDecimal.ZERO)
                .totalRefunds(BigDecimal.ZERO)
                .totalPenalties(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO)
                .build();
        when(settlementRepository.existsBySettlementDate(testDate)).thenReturn(true);
        when(settlementRepository.findBySettlementDate(testDate)).thenReturn(Optional.of(existingSettlement));

        settlementService.generateDailyStatement(testDate, 1L, "admin", "ADMIN");

        verify(settlementRepository).existsBySettlementDate(testDate);
        verify(transactionRepository, never()).sumAmountByTypeAndTimestampBetween(any(), any(), any());
    }
}

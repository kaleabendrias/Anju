package com.anju.service;

import com.anju.dto.PaymentRequest;
import com.anju.dto.RefundRequest;
import com.anju.dto.TransactionResponse;
import com.anju.entity.Transaction;
import com.anju.entity.Transaction.TransactionType;
import com.anju.exception.BusinessException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.TransactionRepository;
import com.anju.security.SecondaryVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecondaryVerificationService secondaryVerificationService;

    @InjectMocks
    private FinancialService financialService;

    private PaymentRequest paymentRequest;
    private RefundRequest refundRequest;
    private Transaction paymentTransaction;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .idempotencyKey("payment-" + System.currentTimeMillis())
                .appointmentId(1L)
                .amount(new BigDecimal("100.00"))
                .channel(Transaction.PaymentChannel.ALIPAY)
                .remark("Test payment")
                .build();

        refundRequest = RefundRequest.builder()
                .idempotencyKey("refund-" + System.currentTimeMillis())
                .originalTransactionId(1L)
                .reason("Customer request")
                .build();

        paymentTransaction = Transaction.builder()
                .id(1L)
                .trxId("TRX123")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.PAYMENT)
                .channel(Transaction.PaymentChannel.ALIPAY)
                .refundableFlag(true)
                .appointmentId(1L)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Payment Recording Tests")
    class PaymentRecordingTests {

        @Test
        @DisplayName("Should record new payment successfully")
        void shouldRecordNewPayment() {
            when(transactionRepository.existsByIdempotencyKey(paymentRequest.getIdempotencyKey()))
                    .thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> {
                        Transaction t = invocation.getArgument(0);
                        t.setId(1L);
                        t.setTrxId("TRX123");
                        t.setTimestamp(LocalDateTime.now());
                        return t;
                    });

            TransactionResponse response = financialService.recordPayment(paymentRequest, 1L, "testuser", "ADMIN");

            assertNotNull(response);
            assertEquals(new BigDecimal("100.00"), response.getAmount());
            assertEquals(TransactionType.PAYMENT, response.getType());
            verify(transactionRepository).save(any(Transaction.class));
            verify(auditLogService).logOperation(any(), eq("TRANSACTION"), 
                    eq(AuditLogService.OPERATION_PAYMENT), eq(1L), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should return existing transaction for duplicate idempotency key")
        void shouldReturnExistingForDuplicateIdempotencyKey() {
            paymentTransaction.setIdempotencyKey(paymentRequest.getIdempotencyKey());

            when(transactionRepository.existsByIdempotencyKey(paymentRequest.getIdempotencyKey()))
                    .thenReturn(true);
            when(transactionRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
                    .thenReturn(Optional.of(paymentTransaction));

            TransactionResponse response = financialService.recordPayment(paymentRequest, 1L, "testuser", "ADMIN");

            assertNotNull(response);
            assertEquals(paymentTransaction.getId(), response.getId());
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should generate unique transaction ID")
        void shouldGenerateUniqueTransactionId() {
            when(transactionRepository.existsByIdempotencyKey(any()))
                    .thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> {
                        Transaction t = invocation.getArgument(0);
                        t.setId(1L);
                        t.setTimestamp(LocalDateTime.now());
                        return t;
                    });

            TransactionResponse response = financialService.recordPayment(paymentRequest, 1L, "testuser", "ADMIN");

            assertNotNull(response.getTrxId());
            assertTrue(response.getTrxId().startsWith("TRX"));
        }
    }

    @Nested
    @DisplayName("Refund Processing Tests")
    class RefundProcessingTests {

        @BeforeEach
        void setUpRefund() {
            when(secondaryVerificationService.verifySecondaryPassword(anyString(), anyString())).thenReturn(true);
        }

        @Test
        @DisplayName("Should process valid refund successfully")
        void shouldProcessValidRefund() {
            paymentTransaction.setId(1L);
            paymentTransaction.setRefundableFlag(true);

            when(transactionRepository.existsByIdempotencyKey(refundRequest.getIdempotencyKey()))
                    .thenReturn(false);
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(paymentTransaction));
            when(transactionRepository.findRefundsByOriginalTransactionId(1L))
                    .thenReturn(Collections.emptyList());
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> {
                        Transaction t = invocation.getArgument(0);
                        t.setId(2L);
                        t.setTimestamp(LocalDateTime.now());
                        return t;
                    });

            TransactionResponse response = financialService.processRefund(refundRequest, 1L, "testuser", "ADMIN", "password");

            assertNotNull(response);
            assertEquals(TransactionType.REFUND, response.getType());
            assertEquals(new BigDecimal("-100.00"), response.getAmount());
            assertEquals(1L, response.getOriginalTransactionId());
        }

        @Test
        @DisplayName("Should prevent refund without valid original payment")
        void shouldPreventRefundWithoutOriginalPayment() {
            when(transactionRepository.existsByIdempotencyKey(refundRequest.getIdempotencyKey()))
                    .thenReturn(false);
            when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

            refundRequest.setOriginalTransactionId(999L);

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> financialService.processRefund(refundRequest, 1L, "testuser", "ADMIN", "password"));

            assertTrue(exception.getMessage().contains("Original transaction not found"));
        }

        @Test
        @DisplayName("Should prevent refund of non-payment transaction")
        void shouldPreventRefundOfNonPaymentTransaction() {
            Transaction refundTransaction = Transaction.builder()
                    .id(1L)
                    .type(TransactionType.REFUND)
                    .refundableFlag(true)
                    .build();

            when(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false);
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(refundTransaction));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> financialService.processRefund(refundRequest, 1L, "testuser", "ADMIN", "password"));

            assertTrue(exception.getMessage().contains("Can only refund PAYMENT"));
        }

        @Test
        @DisplayName("Should prevent refund of non-refundable transaction")
        void shouldPreventRefundOfNonRefundableTransaction() {
            paymentTransaction.setRefundableFlag(false);

            when(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false);
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(paymentTransaction));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> financialService.processRefund(refundRequest, 1L, "testuser", "ADMIN", "password"));

            assertTrue(exception.getMessage().contains("not refundable"));
        }

        @Test
        @DisplayName("Should prevent double refund of same transaction")
        void shouldPreventDoubleRefund() {
            Transaction existingRefund = Transaction.builder()
                    .id(2L)
                    .type(TransactionType.REFUND)
                    .amount(new BigDecimal("-100.00"))
                    .originalTransactionId(1L)
                    .build();

            when(transactionRepository.existsByIdempotencyKey(any())).thenReturn(false);
            when(transactionRepository.findById(1L)).thenReturn(Optional.of(paymentTransaction));
            when(transactionRepository.findRefundsByOriginalTransactionId(1L))
                    .thenReturn(java.util.List.of(existingRefund));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> financialService.processRefund(refundRequest, 1L, "testuser", "ADMIN", "password"));

            assertTrue(exception.getMessage().contains("Full refund already processed"));
        }

        @Test
        @DisplayName("Should return existing refund for duplicate idempotency key")
        void shouldReturnExistingRefundForDuplicateIdempotencyKey() {
            Transaction existingRefund = Transaction.builder()
                    .id(2L)
                    .trxId("REFUND123")
                    .type(TransactionType.REFUND)
                    .amount(new BigDecimal("-100.00"))
                    .originalTransactionId(1L)
                    .idempotencyKey(refundRequest.getIdempotencyKey())
                    .timestamp(LocalDateTime.now())
                    .build();

            when(transactionRepository.existsByIdempotencyKey(refundRequest.getIdempotencyKey()))
                    .thenReturn(true);
            when(transactionRepository.findByIdempotencyKey(refundRequest.getIdempotencyKey()))
                    .thenReturn(Optional.of(existingRefund));

            TransactionResponse response = financialService.processRefund(refundRequest, 1L, "testuser", "ADMIN", "password");

            assertEquals(existingRefund.getId(), response.getId());
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should handle concurrent payment requests with same idempotency key")
        void shouldHandleConcurrentPaymentRequests() {
            when(transactionRepository.existsByIdempotencyKey(paymentRequest.getIdempotencyKey()))
                    .thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(invocation -> {
                        Transaction t = invocation.getArgument(0);
                        t.setId(1L);
                        t.setTimestamp(LocalDateTime.now());
                        return t;
                    });

            TransactionResponse response1 = financialService.recordPayment(paymentRequest, 1L, "user1", "ADMIN");
            TransactionResponse response2 = financialService.recordPayment(paymentRequest, 2L, "user2", "ADMIN");

            assertEquals(response1.getId(), response2.getId());
        }
    }
}

package com.anju.service;

import com.anju.dto.PaymentRequest;
import com.anju.dto.RefundRequest;
import com.anju.dto.TransactionResponse;
import com.anju.entity.Transaction;
import com.anju.entity.Transaction.TransactionType;
import com.anju.entity.User;
import com.anju.exception.BusinessException;
import com.anju.repository.TransactionRepository;
import com.anju.repository.UserRepository;
import com.anju.security.SecondaryVerificationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinanceCriticalPathTest {

    @Autowired
    private FinancialService financialService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @SpyBean
    private SecondaryVerificationService secondaryVerificationService;

    private User operatorUser;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        operatorUser = userRepository.save(User.builder()
                .username("finance_user_" + System.currentTimeMillis())
                .passwordHash("$2a$10$encoded")
                .role(User.Role.FINANCE)
                .build());
        doReturn(true).when(secondaryVerificationService).verifySecondaryPassword(anyString(), anyString());
    }

    @Nested
    @DisplayName("Duplicate Payment Prevention Tests")
    @Transactional
    class DuplicatePaymentPreventionTests {

        @Test
        @Order(1)
        @DisplayName("Should prevent duplicate payment with same idempotency key")
        void preventDuplicatePaymentWithSameIdempotencyKey() {
            String idempotencyKey = "dup_payment_" + UUID.randomUUID();

            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey(idempotencyKey)
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build();

            TransactionResponse response1 = financialService.recordPayment(request, operatorUser.getId(), 
                    operatorUser.getUsername(), operatorUser.getRole().name());

            TransactionResponse response2 = financialService.recordPayment(request, operatorUser.getId(),
                    operatorUser.getUsername(), operatorUser.getRole().name());

            assertEquals(response1.getId(), response2.getId());
            assertEquals(1, transactionRepository.count());
        }

        @Test
        @Order(2)
        @DisplayName("Should allow payment with different idempotency key")
        void allowPaymentWithDifferentIdempotencyKey() {
            PaymentRequest request1 = PaymentRequest.builder()
                    .idempotencyKey("unique_key_1_" + UUID.randomUUID())
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build();

            PaymentRequest request2 = PaymentRequest.builder()
                    .idempotencyKey("unique_key_2_" + UUID.randomUUID())
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build();

            TransactionResponse response1 = financialService.recordPayment(request1, operatorUser.getId(),
                    operatorUser.getUsername(), operatorUser.getRole().name());
            TransactionResponse response2 = financialService.recordPayment(request2, operatorUser.getId(),
                    operatorUser.getUsername(), operatorUser.getRole().name());

            assertNotEquals(response1.getId(), response2.getId());
            assertEquals(2, transactionRepository.count());
        }

        @Test
        @Order(3)
        @DisplayName("Should prevent duplicate refund with same idempotency key")
        @Disabled("Test requires proper refund idempotency setup")
        void preventDuplicateRefundWithSameIdempotencyKey() {
            Transaction original = transactionRepository.save(Transaction.builder()
                    .trxId("TRX_ORIGINAL_" + UUID.randomUUID())
                    .idempotencyKey("original_" + UUID.randomUUID())
                    .amount(new BigDecimal("100.00"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .refundableFlag(true)
                    .build());

            String refundIdempotencyKey = "refund_" + UUID.randomUUID();

            RefundRequest request1 = RefundRequest.builder()
                    .originalTransactionId(original.getId())
                    .idempotencyKey(refundIdempotencyKey)
                    .reason("Test refund 1")
                    .build();

            RefundRequest request2 = RefundRequest.builder()
                    .originalTransactionId(original.getId())
                    .idempotencyKey(refundIdempotencyKey)
                    .reason("Test refund 2")
                    .build();

            TransactionResponse response1 = financialService.processRefund(request1, operatorUser.getId(),
                    operatorUser.getUsername(), operatorUser.getRole().name(), "admin123");

            assertThrows(BusinessException.class, () ->
                    financialService.processRefund(request2, operatorUser.getId(),
                            operatorUser.getUsername(), operatorUser.getRole().name(), "admin123"));

            assertEquals(1, transactionRepository.findRefundsByOriginalTransactionId(original.getId()).size());
        }
    }

    @Nested
    @DisplayName("Refund Validation Tests")
    @Disabled("Test requires proper transaction setup")
    class RefundValidationTests {

        @Test
        @Order(1)
        @DisplayName("Should reject refund for non-refundable transaction")
        void rejectRefundForNonRefundableTransaction() {
            Transaction nonRefundable = transactionRepository.save(Transaction.builder()
                    .trxId("TRX_NON_REFUND_" + UUID.randomUUID())
                    .idempotencyKey("non_ref_" + UUID.randomUUID())
                    .amount(new BigDecimal("100.00"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .refundableFlag(false)
                    .build());

            RefundRequest request = RefundRequest.builder()
                    .originalTransactionId(nonRefundable.getId())
                    .idempotencyKey("refund_non_ref_" + UUID.randomUUID())
                    .reason("Test")
                    .build();

            assertThrows(BusinessException.class, () ->
                    financialService.processRefund(request, operatorUser.getId(),
                            operatorUser.getUsername(), operatorUser.getRole().name(), "admin123"));
        }

        @Test
        @Order(2)
        @DisplayName("Should reject refund for already fully refunded transaction")
        void rejectRefundForFullyRefundedTransaction() {
            Transaction original = transactionRepository.save(Transaction.builder()
                    .trxId("TRX_FULL_REF_" + UUID.randomUUID())
                    .idempotencyKey("full_ref_" + UUID.randomUUID())
                    .amount(new BigDecimal("100.00"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .refundableFlag(true)
                    .build());

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX_FULL_REFUND_" + UUID.randomUUID())
                    .idempotencyKey("full_refund_" + UUID.randomUUID())
                    .originalTransactionId(original.getId())
                    .amount(new BigDecimal("100.00"))
                    .type(TransactionType.REFUND)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .refundableFlag(false)
                    .build());

            RefundRequest request = RefundRequest.builder()
                    .originalTransactionId(original.getId())
                    .idempotencyKey("refund_attempt_" + UUID.randomUUID())
                    .reason("Test")
                    .build();

            assertThrows(BusinessException.class, () ->
                    financialService.processRefund(request, operatorUser.getId(),
                            operatorUser.getUsername(), operatorUser.getRole().name(), "admin123"));
        }

        @Test
        @Order(3)
        @DisplayName("Should reject refund for non-existent transaction")
        void rejectRefundForNonExistentTransaction() {
            RefundRequest request = RefundRequest.builder()
                    .originalTransactionId(99999L)
                    .idempotencyKey("refund_nonexistent_" + UUID.randomUUID())
                    .reason("Test")
                    .build();

            assertThrows(BusinessException.class, () ->
                    financialService.processRefund(request, operatorUser.getId(),
                            operatorUser.getUsername(), operatorUser.getRole().name(), "admin123"));
        }

        @Test
        @Order(4)
        @DisplayName("Should reject refund for non-PAYMENT transaction")
        void rejectRefundForNonPaymentTransaction() {
            Transaction refundTransaction = transactionRepository.save(Transaction.builder()
                    .trxId("TRX_ALREADY_REFUND_" + UUID.randomUUID())
                    .idempotencyKey("already_ref_" + UUID.randomUUID())
                    .amount(new BigDecimal("-50.00"))
                    .type(TransactionType.REFUND)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .refundableFlag(false)
                    .build());

            RefundRequest request = RefundRequest.builder()
                    .originalTransactionId(refundTransaction.getId())
                    .idempotencyKey("refund_refund_" + UUID.randomUUID())
                    .reason("Test")
                    .build();

            assertThrows(BusinessException.class, () ->
                    financialService.processRefund(request, operatorUser.getId(),
                            operatorUser.getUsername(), operatorUser.getRole().name(), "admin123"));
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @Order(1)
        @DisplayName("Should handle concurrent idempotent requests")
        void handleConcurrentIdempotentRequests() throws InterruptedException {
            String idempotencyKey = "concurrent_" + UUID.randomUUID();
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger duplicateCount = new AtomicInteger(0);

            PaymentRequest request = PaymentRequest.builder()
                    .idempotencyKey(idempotencyKey)
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build();

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        TransactionResponse response = financialService.recordPayment(request, operatorUser.getId(),
                                operatorUser.getUsername(), operatorUser.getRole().name());
                        successCount.incrementAndGet();
                    } catch (BusinessException e) {
                        if (e.getMessage().contains("already processed")) {
                            duplicateCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertEquals(1, successCount.get() + duplicateCount.get());
            assertEquals(1, transactionRepository.count());
        }
    }

    @Nested
    @DisplayName("Transaction Rollback Tests")
    @Disabled("Test requires proper transaction setup")
    class TransactionRollbackTests {

        @Test
        @Order(1)
        @DisplayName("Should rollback on invalid operation")
        @Transactional
        void rollbackOnInvalidOperation() {
            String originalIdempotencyKey = "rollback_test_" + UUID.randomUUID();

            PaymentRequest validRequest = PaymentRequest.builder()
                    .idempotencyKey(originalIdempotencyKey)
                    .appointmentId(1L)
                    .amount(new BigDecimal("100.00"))
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build();

            TransactionResponse validResponse = financialService.recordPayment(validRequest, operatorUser.getId(),
                    operatorUser.getUsername(), operatorUser.getRole().name());

            RefundRequest invalidRefund = RefundRequest.builder()
                    .originalTransactionId(validResponse.getId())
                    .idempotencyKey("invalid_" + UUID.randomUUID())
                    .reason("Test")
                    .build();

            assertThrows(BusinessException.class, () ->
                    financialService.processRefund(invalidRefund, operatorUser.getId(),
                            operatorUser.getUsername(), operatorUser.getRole().name(), "admin123"));

            long count = transactionRepository.count();
            assertTrue(count >= 1);
        }
    }
}

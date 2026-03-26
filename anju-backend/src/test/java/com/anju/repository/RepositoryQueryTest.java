package com.anju.repository;

import com.anju.entity.*;
import com.anju.entity.Transaction.TransactionType;
import com.anju.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoryQueryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        fileRecordRepository.deleteAll();
        settlementRepository.deleteAll();
        transactionRepository.deleteAll();
        appointmentRepository.deleteAll();
        propertyRepository.deleteAll();
    }

    @Nested
    @DisplayName("Appointment Repository Tests")
    class AppointmentRepositoryTests {

        @Test
        @Order(1)
        @DisplayName("Should find conflicting staff appointments")
        void findConflictingStaffAppointments() {
            LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime end = start.plusMinutes(30);

            appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(start)
                    .endTime(end)
                    .accompanyingStaffId(100L)
                    .orderAmount(new BigDecimal("100"))
                    .build());

            List<Appointment> conflicts = appointmentRepository.findConflictingStaffAppointments(100L, start, end);
            assertEquals(1, conflicts.size());

            List<Appointment> noConflicts = appointmentRepository.findConflictingStaffAppointments(
                    200L, start, end);
            assertTrue(noConflicts.isEmpty());
        }



        @Test
        @Order(3)
        @DisplayName("Should find appointments by status")
        void findByStatus() {
            LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.STANDARD_CONSULTATION)
                    .status(Appointment.AppointmentStatus.PENDING)
                    .startTime(start)
                    .endTime(start.plusMinutes(30))
                    .orderAmount(new BigDecimal("100"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .serviceType(Appointment.ServiceType.EXTENDED_CONSULTATION)
                    .status(Appointment.AppointmentStatus.CONFIRMED)
                    .startTime(start.plusHours(2))
                    .endTime(start.plusHours(3))
                    .orderAmount(new BigDecimal("200"))
                    .build());

            List<Appointment> pending = appointmentRepository.findByStatus(Appointment.AppointmentStatus.PENDING);
            assertEquals(1, pending.size());

            List<Appointment> confirmed = appointmentRepository.findByStatus(Appointment.AppointmentStatus.CONFIRMED);
            assertEquals(1, confirmed.size());
        }
    }

    @Nested
    @DisplayName("Transaction Repository Tests")
    class TransactionRepositoryTests {

        @Test
        @Order(1)
        @DisplayName("Should find by idempotency key")
        void findByIdempotencyKey() {
            String idempotencyKey = "test_idem_" + UUID.randomUUID();

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX001")
                    .idempotencyKey(idempotencyKey)
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build());

            Optional<Transaction> found = transactionRepository.findByIdempotencyKey(idempotencyKey);
            assertTrue(found.isPresent());
            assertEquals("TRX001", found.get().getTrxId());
        }

        @Test
        @Order(2)
        @DisplayName("Should check idempotency key existence")
        void existsByIdempotencyKey() {
            String idempotencyKey = "exists_idem_" + UUID.randomUUID();

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX002")
                    .idempotencyKey(idempotencyKey)
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build());

            assertTrue(transactionRepository.existsByIdempotencyKey(idempotencyKey));
            assertFalse(transactionRepository.existsByIdempotencyKey("nonexistent"));
        }

        @Test
        @Order(3)
        @DisplayName("Should sum amounts by type and time range")
        void sumAmountsByTypeAndTimeRange() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now().plusHours(1);

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX003")
                    .idempotencyKey("sum1_" + UUID.randomUUID())
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .timestamp(LocalDateTime.now())
                    .build());

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX004")
                    .idempotencyKey("sum2_" + UUID.randomUUID())
                    .amount(new BigDecimal("200"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .timestamp(LocalDateTime.now())
                    .build());

            BigDecimal total = transactionRepository.sumAmountByTypeAndTimestampBetween(
                    start, end, TransactionType.PAYMENT);
            assertEquals(new BigDecimal("300.00"), total);
        }

        @Test
        @Order(4)
        @DisplayName("Should find transactions by timestamp range")
        void findByTimestampBetween() {
            LocalDateTime start = LocalDateTime.now().minusHours(2);
            LocalDateTime end = LocalDateTime.now().plusHours(2);

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX005")
                    .idempotencyKey("range1_" + UUID.randomUUID())
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .timestamp(LocalDateTime.now())
                    .build());

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX006")
                    .idempotencyKey("range2_" + UUID.randomUUID())
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.REFUND)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .timestamp(LocalDateTime.now())
                    .build());

            List<Transaction> transactions = transactionRepository.findByTimestampBetween(start, end);
            assertEquals(2, transactions.size());
        }

        @Test
        @Order(5)
        @DisplayName("Should find refunds by original transaction ID")
        void findRefundsByOriginalTransactionId() {
            Transaction original = transactionRepository.save(Transaction.builder()
                    .trxId("TRX007")
                    .idempotencyKey("orig_" + UUID.randomUUID())
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build());

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX008")
                    .idempotencyKey("ref1_" + UUID.randomUUID())
                    .originalTransactionId(original.getId())
                    .amount(new BigDecimal("50"))
                    .type(TransactionType.REFUND)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .build());

            List<Transaction> refunds = transactionRepository.findRefundsByOriginalTransactionId(original.getId());
            assertEquals(1, refunds.size());
            assertEquals(TransactionType.REFUND, refunds.get(0).getType());
        }

        @Test
        @Order(6)
        @DisplayName("Should count transactions by timestamp range")
        void countByTimestampBetween() {
            LocalDateTime start = LocalDateTime.now().minusHours(1);
            LocalDateTime end = LocalDateTime.now().plusHours(1);

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX009")
                    .idempotencyKey("count1_" + UUID.randomUUID())
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .timestamp(LocalDateTime.now())
                    .build());

            transactionRepository.save(Transaction.builder()
                    .trxId("TRX010")
                    .idempotencyKey("count2_" + UUID.randomUUID())
                    .amount(new BigDecimal("100"))
                    .type(TransactionType.PAYMENT)
                    .channel(Transaction.PaymentChannel.ALIPAY)
                    .timestamp(LocalDateTime.now())
                    .build());

            Integer count = transactionRepository.countByTimestampBetween(start, end);
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("Settlement Repository Tests")
    class SettlementRepositoryTests {

        @Test
        @Order(1)
        @DisplayName("Should check if settlement exists for date")
        void existsBySettlementDate() {
            LocalDate date = LocalDate.now();

            settlementRepository.save(Settlement.builder()
                    .settlementDate(date)
                    .totalIncome(new BigDecimal("1000"))
                    .totalRefunds(new BigDecimal("0"))
                    .totalPenalties(new BigDecimal("0"))
                    .netAmount(new BigDecimal("1000"))
                    .transactionCount(10)
                    .build());

            assertTrue(settlementRepository.existsBySettlementDate(date));
            assertFalse(settlementRepository.existsBySettlementDate(date.minusDays(1)));
        }

        @Test
        @Order(2)
        @DisplayName("Should find settlement by date")
        void findBySettlementDate() {
            LocalDate date = LocalDate.now();

            settlementRepository.save(Settlement.builder()
                    .settlementDate(date)
                    .totalIncome(new BigDecimal("2000"))
                    .totalRefunds(new BigDecimal("100"))
                    .totalPenalties(new BigDecimal("50"))
                    .netAmount(new BigDecimal("1950"))
                    .transactionCount(20)
                    .build());

            Optional<Settlement> found = settlementRepository.findBySettlementDate(date);
            assertTrue(found.isPresent());
            assertEquals(0, new BigDecimal("2000").compareTo(found.get().getTotalIncome()));
        }
    }

    @Nested
    @DisplayName("Property Repository Tests")
    class PropertyRepositoryTests {

        @Test
        @Order(1)
        @DisplayName("Should find properties by status")
        void findByStatus() {
            propertyRepository.save(Property.builder()
                    .uniqueCode("PROP001")
                    .status(Property.PropertyStatus.DRAFT)
                    .rent(new BigDecimal("1000"))
                    .deposit(new BigDecimal("2000"))
                    .build());

            propertyRepository.save(Property.builder()
                    .uniqueCode("PROP002")
                    .status(Property.PropertyStatus.LISTED)
                    .rent(new BigDecimal("1500"))
                    .deposit(new BigDecimal("3000"))
                    .build());

            List<Property> drafts = propertyRepository.findByStatus(Property.PropertyStatus.DRAFT);
            assertEquals(1, drafts.size());

            List<Property> listed = propertyRepository.findByStatus(Property.PropertyStatus.LISTED);
            assertEquals(1, listed.size());
        }

        @Test
        @Order(2)
        @DisplayName("Should find by unique code")
        void findByUniqueCode() {
            String uniqueCode = "UNIQUE_" + UUID.randomUUID();

            propertyRepository.save(Property.builder()
                    .uniqueCode(uniqueCode)
                    .status(Property.PropertyStatus.DRAFT)
                    .rent(new BigDecimal("1000"))
                    .deposit(new BigDecimal("2000"))
                    .build());

            Optional<Property> found = propertyRepository.findByUniqueCode(uniqueCode);
            assertTrue(found.isPresent());
            assertEquals(uniqueCode, found.get().getUniqueCode());
        }
    }

    @Nested
    @DisplayName("FileRecord Repository Tests")
    class FileRecordRepositoryTests {

        @Test
        @Order(1)
        @DisplayName("Should find by file hash")
        void findByFileHash() {
            String hash = "hash_" + UUID.randomUUID();

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId("LOGICAL001")
                    .fileHash(hash)
                    .originalName("test.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/test.pdf")
                    .build());

            Optional<FileRecord> found = fileRecordRepository.findByFileHash(hash);
            assertTrue(found.isPresent());
        }

        @Test
        @Order(2)
        @DisplayName("Should find active file by logical ID")
        void findByLogicalIdAndIsActiveTrue() {
            String logicalId = "LOGICAL_" + UUID.randomUUID();

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash1")
                    .originalName("v1.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(false)
                    .storagePath("/storage/v1.pdf")
                    .build());

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash2")
                    .originalName("v2.pdf")
                    .fileType("pdf")
                    .size(2048L)
                    .versionNumber(2)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/v2.pdf")
                    .build());

            Optional<FileRecord> active = fileRecordRepository.findByLogicalIdAndIsActiveTrue(logicalId);
            assertTrue(active.isPresent());
            assertEquals("v2.pdf", active.get().getOriginalName());
        }

        @Test
        @Order(3)
        @DisplayName("Should find versions by logical ID in descending order")
        void findByLogicalIdOrderByVersionNumberDesc() {
            String logicalId = "VERSION_" + UUID.randomUUID();

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash_v1")
                    .originalName("v1.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(false)
                    .storagePath("/storage/v1.pdf")
                    .build());

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash_v2")
                    .originalName("v2.pdf")
                    .fileType("pdf")
                    .size(2048L)
                    .versionNumber(2)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/v2.pdf")
                    .build());

            List<FileRecord> versions = fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc(logicalId);
            assertEquals(2, versions.size());
            assertEquals(2, versions.get(0).getVersionNumber());
            assertEquals(1, versions.get(1).getVersionNumber());
        }

        @Test
        @Order(4)
        @DisplayName("Should find deleted files by expiration time")
        void findByIsDeletedTrueAndExpirationTimeBefore() {
            LocalDateTime pastExpiration = LocalDateTime.now().minusDays(1);
            LocalDateTime futureExpiration = LocalDateTime.now().plusDays(1);

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId("EXPIRED")
                    .fileHash("hash_expired")
                    .originalName("expired.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/expired.pdf")
                    .expirationTime(pastExpiration)
                    .build());

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId("NOT_EXPIRED")
                    .fileHash("hash_not_expired")
                    .originalName("not_expired.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/not_expired.pdf")
                    .expirationTime(futureExpiration)
                    .build());

            List<FileRecord> expired = fileRecordRepository.findByIsDeletedTrueAndExpirationTimeBefore(LocalDateTime.now());
            assertEquals(1, expired.size());
            assertEquals("EXPIRED", expired.get(0).getLogicalId());
        }
    }

    @Nested
    @DisplayName("AuditLog Repository Tests")
    class AuditLogRepositoryTests {

        @Test
        @Order(1)
        @DisplayName("Should find by entity type and ID")
        void findByEntityTypeAndEntityId() {
            auditLogRepository.save(AuditLog.builder()
                    .entityType("APPOINTMENT")
                    .entityId(1L)
                    .operation("CREATE")
                    .operatorId(1L)
                    .operatorUsername("admin")
                    .build());

            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("APPOINTMENT", 1L);
            assertEquals(1, logs.size());
            assertEquals("CREATE", logs.get(0).getOperation());
        }

        @Test
        @Order(2)
        @DisplayName("Should find by operator ID")
        void findByOperatorId() {
            auditLogRepository.save(AuditLog.builder()
                    .entityType("TRANSACTION")
                    .entityId(1L)
                    .operation("PAYMENT")
                    .operatorId(100L)
                    .operatorUsername("operator1")
                    .build());

            auditLogRepository.save(AuditLog.builder()
                    .entityType("TRANSACTION")
                    .entityId(2L)
                    .operation("REFUND")
                    .operatorId(200L)
                    .operatorUsername("operator2")
                    .build());

            List<AuditLog> operator1Logs = auditLogRepository.findByOperatorId(100L);
            assertEquals(1, operator1Logs.size());
        }
    }
}

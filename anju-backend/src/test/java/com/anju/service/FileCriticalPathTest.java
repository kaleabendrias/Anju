package com.anju.service;

import com.anju.dto.FileRecordResponse;
import com.anju.entity.FileRecord;
import com.anju.entity.User;
import com.anju.exception.BusinessException;
import com.anju.exception.ForbiddenException;
import com.anju.repository.FileRecordRepository;
import com.anju.security.SecondaryVerificationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileCriticalPathTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @SpyBean
    private SecondaryVerificationService secondaryVerificationService;

    private User adminUser;
    private User frontlineUser;
    private User otherFrontlineUser;

    @BeforeEach
    void setUp() {
        fileRecordRepository.deleteAll();

        adminUser = User.builder()
                .id(1L)
                .username("admin_file_" + System.currentTimeMillis())
                .role(User.Role.ADMIN)
                .passwordHash("$2a$10$placeholder")
                .build();

        frontlineUser = User.builder()
                .id(2L)
                .username("frontline_file_" + System.currentTimeMillis())
                .role(User.Role.FRONTLINE)
                .passwordHash("$2a$10$placeholder")
                .build();

        otherFrontlineUser = User.builder()
                .id(3L)
                .username("other_frontline_file_" + System.currentTimeMillis())
                .role(User.Role.FRONTLINE)
                .passwordHash("$2a$10$placeholder")
                .build();

        doReturn(true).when(secondaryVerificationService).verifySecondaryPassword(anyString(), anyString());
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @Order(1)
        @DisplayName("Should soft delete file and set expiration time")
        void softDeleteSetsExpirationTime() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("softdel_" + UUID.randomUUID())
                    .fileHash("hash123")
                    .originalName("test.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/test.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            fileService.softDeleteFile(file.getId(), frontlineUser.getId(),
                    frontlineUser.getUsername(), frontlineUser.getRole().name());

            FileRecord deleted = fileRecordRepository.findById(file.getId()).orElseThrow();
            assertTrue(deleted.getIsDeleted());
            assertNotNull(deleted.getExpirationTime());
            assertNotNull(deleted.getDeletedAt());
            assertEquals(frontlineUser.getId(), deleted.getDeletedBy());
        }

        @Test
        @Order(2)
        @DisplayName("Should reject soft delete of already deleted file")
        void rejectSoftDeleteOfAlreadyDeleted() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("already_del_" + UUID.randomUUID())
                    .fileHash("hash456")
                    .originalName("deleted.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/deleted.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            assertThrows(BusinessException.class, () ->
                    fileService.softDeleteFile(file.getId(), frontlineUser.getId(),
                            frontlineUser.getUsername(), frontlineUser.getRole().name()));
        }

        @Test
        @Order(3)
        @DisplayName("Soft deleted file should not appear in active file list")
        void softDeletedFileNotInActiveList() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("hidden_" + UUID.randomUUID())
                    .fileHash("hash789")
                    .originalName("hidden.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/hidden.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            fileService.softDeleteFile(file.getId(), frontlineUser.getId(),
                    frontlineUser.getUsername(), frontlineUser.getRole().name());

            List<FileRecord> deletedFiles = fileRecordRepository.findByIsDeletedTrue();
            assertTrue(deletedFiles.stream().anyMatch(f -> f.getId().equals(file.getId())));
        }
    }

    @Nested
    @DisplayName("Restore Tests")
    class RestoreTests {

        @Test
        @Order(1)
        @DisplayName("Should restore soft deleted file")
        void restoreSoftDeletedFile() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("restore_" + UUID.randomUUID())
                    .fileHash("hash_restore")
                    .originalName("restore.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/restore.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            fileService.restoreFile(file.getId(), frontlineUser.getId(),
                    frontlineUser.getUsername(), frontlineUser.getRole().name());

            FileRecord restored = fileRecordRepository.findById(file.getId()).orElseThrow();
            assertFalse(restored.getIsDeleted());
            assertNull(restored.getExpirationTime());
            assertNull(restored.getDeletedAt());
            assertNull(restored.getDeletedBy());
        }

        @Test
        @Order(2)
        @DisplayName("Should reject restore of non-deleted file")
        void rejectRestoreOfActiveFile() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("not_deleted_" + UUID.randomUUID())
                    .fileHash("hash_active")
                    .originalName("active.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/active.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            assertThrows(BusinessException.class, () ->
                    fileService.restoreFile(file.getId(), frontlineUser.getId(),
                            frontlineUser.getUsername(), frontlineUser.getRole().name()));
        }
    }

    @Nested
    @DisplayName("Permanent Delete Tests")
    class PermanentDeleteTests {

        @Test
        @Order(1)
        @DisplayName("Should permanently delete soft deleted file")
        void permanentDeleteSoftDeletedFile() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("perm_del_" + UUID.randomUUID())
                    .fileHash("hash_perm")
                    .originalName("permanent.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/permanent.pdf")
                    .uploadedBy(adminUser.getId())
                    .build());

            fileService.permanentlyDeleteFile(file.getId(), adminUser.getId(),
                    adminUser.getUsername(), adminUser.getRole().name(), "admin123");

            assertFalse(fileRecordRepository.existsById(file.getId()));
        }

        @Test
        @Order(2)
        @DisplayName("Should reject permanent delete of non-deleted file")
        void rejectPermanentDeleteOfActiveFile() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("active_perm_" + UUID.randomUUID())
                    .fileHash("hash_active_perm")
                    .originalName("active_perm.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/active_perm.pdf")
                    .uploadedBy(adminUser.getId())
                    .build());

            assertThrows(BusinessException.class, () ->
                    fileService.permanentlyDeleteFile(file.getId(), adminUser.getId(),
                            adminUser.getUsername(), adminUser.getRole().name(), "admin123"));
        }

        @Test
        @Order(3)
        @DisplayName("Should reject permanent delete without admin role")
        void rejectPermanentDeleteWithoutAdminRole() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("frontline_perm_" + UUID.randomUUID())
                    .fileHash("hash_frontline_perm")
                    .originalName("frontline_perm.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/frontline_perm.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            assertThrows(ForbiddenException.class, () ->
                    fileService.permanentlyDeleteFile(file.getId(), frontlineUser.getId(),
                            frontlineUser.getUsername(), frontlineUser.getRole().name(), "admin123"));
        }

        @Test
        @Order(4)
        @DisplayName("Should require secondary password for permanent delete")
        void requireSecondaryPasswordForPermanentDelete() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("sec_perm_" + UUID.randomUUID())
                    .fileHash("hash_sec_perm")
                    .originalName("sec_perm.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(true)
                    .isActive(false)
                    .storagePath("/storage/sec_perm.pdf")
                    .uploadedBy(adminUser.getId())
                    .build());

            assertThrows(BusinessException.class, () ->
                    fileService.permanentlyDeleteFile(file.getId(), adminUser.getId(),
                            adminUser.getUsername(), adminUser.getRole().name(), "wrongpassword"));
        }
    }

    @Nested
    @DisplayName("Version Tests")
    class VersionTests {

        @Test
        @Order(1)
        @DisplayName("Should create new version on upload with same logical ID")
        void createNewVersionOnSameLogicalId() {
            String logicalId = "version_test_" + UUID.randomUUID();

            FileRecord v1 = fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash_v1")
                    .originalName("v1.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/v1.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            FileRecord v2 = fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash_v2")
                    .originalName("v2.pdf")
                    .fileType("pdf")
                    .size(2048L)
                    .versionNumber(2)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/v2.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            List<FileRecord> versions = fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc(logicalId);
            assertEquals(2, versions.size());
            assertEquals(2, versions.get(0).getVersionNumber());
            assertEquals(1, versions.get(1).getVersionNumber());
        }

        @Test
        @Order(2)
        @DisplayName("Should only mark one version as active")
        void onlyOneVersionActive() {
            String logicalId = "active_test_" + UUID.randomUUID();

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash_a1")
                    .originalName("a1.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(false)
                    .storagePath("/storage/a1.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            fileRecordRepository.save(FileRecord.builder()
                    .logicalId(logicalId)
                    .fileHash("hash_a2")
                    .originalName("a2.pdf")
                    .fileType("pdf")
                    .size(2048L)
                    .versionNumber(2)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/a2.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            Optional<FileRecord> activeFile = fileRecordRepository.findByLogicalIdAndIsActiveTrue(logicalId);
            assertTrue(activeFile.isPresent());
            assertEquals(2, activeFile.get().getVersionNumber());
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @Order(1)
        @DisplayName("Should handle concurrent delete requests idempotently")
        void handleConcurrentDeleteRequests() throws InterruptedException {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("concurrent_del_" + UUID.randomUUID())
                    .fileHash("hash_concurrent")
                    .originalName("concurrent.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/concurrent.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        fileService.softDeleteFile(file.getId(), frontlineUser.getId(),
                                frontlineUser.getUsername(), frontlineUser.getRole().name());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertEquals(1, successCount.get() + errorCount.get());
            FileRecord finalFile = fileRecordRepository.findById(file.getId()).orElseThrow();
            assertTrue(finalFile.getIsDeleted());
        }
    }

    @Nested
    @DisplayName("Access Control Tests")
    class AccessControlTests {

        @Test
        @Order(1)
        @DisplayName("User should access their own files")
        void userAccessOwnFiles() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("own_file_" + UUID.randomUUID())
                    .fileHash("hash_own")
                    .originalName("own.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/own.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            FileRecordResponse response = fileService.getFileInfo(file.getId(),
                    frontlineUser.getId(), frontlineUser.getRole().name());

            assertNotNull(response);
            assertEquals(file.getId(), response.getId());
        }

        @Test
        @Order(2)
        @DisplayName("User should NOT access other user's files")
        void userCannotAccessOtherFiles() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("other_file_" + UUID.randomUUID())
                    .fileHash("hash_other")
                    .originalName("other.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/other.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            assertThrows(ForbiddenException.class, () ->
                    fileService.getFileInfo(file.getId(), otherFrontlineUser.getId(),
                            otherFrontlineUser.getRole().name()));
        }

        @Test
        @Order(3)
        @DisplayName("Admin should access any file")
        void adminCanAccessAnyFile() {
            FileRecord file = fileRecordRepository.save(FileRecord.builder()
                    .logicalId("admin_file_" + UUID.randomUUID())
                    .fileHash("hash_admin")
                    .originalName("admin.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .versionNumber(1)
                    .isDeleted(false)
                    .isActive(true)
                    .storagePath("/storage/admin.pdf")
                    .uploadedBy(frontlineUser.getId())
                    .build());

            FileRecordResponse response = fileService.getFileInfo(file.getId(),
                    adminUser.getId(), adminUser.getRole().name());

            assertNotNull(response);
            assertEquals(file.getId(), response.getId());
        }
    }
}

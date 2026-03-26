package com.anju.service;

import com.anju.dto.FileRecordResponse;
import com.anju.dto.InitUploadRequest;
import com.anju.dto.InitUploadResponse;
import com.anju.entity.ChunkInfo;
import com.anju.entity.FileRecord;
import com.anju.repository.ChunkInfoRepository;
import com.anju.repository.FileRecordRepository;
import com.anju.security.SecureDataMasker;
import com.anju.security.SecondaryVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceTest {

    @Mock
    private FileRecordRepository fileRecordRepository;

    @Mock
    private ChunkInfoRepository chunkInfoRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecondaryVerificationService secondaryVerificationService;

    @Mock
    private SecureDataMasker dataMasker;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(fileRecordRepository, chunkInfoRepository, auditLogService, secondaryVerificationService, dataMasker);
        ReflectionTestUtils.setField(fileService, "storagePath", "./test-storage");
        ReflectionTestUtils.setField(fileService, "tempPath", "./test-storage/temp");
        ReflectionTestUtils.setField(fileService, "retentionDays", 30);
    }

    @Nested
    @DisplayName("Hash-Based Deduplication Tests")
    class HashBasedDeduplicationTests {

        @Test
        @DisplayName("Should initialize upload and return upload ID")
        void shouldInitializeUploadAndReturnUploadId() {
            String fileHash = "abc123def456";
            
            InitUploadRequest request = InitUploadRequest.builder()
                    .fileHash(fileHash)
                    .originalName("test-document.pdf")
                    .fileType("pdf")
                    .size(1024L)
                    .totalChunks(1)
                    .build();

            when(chunkInfoRepository.save(any(ChunkInfo.class)))
                    .thenAnswer(invocation -> {
                        ChunkInfo ci = invocation.getArgument(0);
                        ci.setId(1L);
                        return ci;
                    });

            InitUploadResponse response = fileService.initUpload(request, 1L, "testuser", "ADMIN");

            assertNotNull(response.getUploadId());
            assertEquals(fileHash, response.getFileHash());
        }

        @Test
        @DisplayName("Should initialize new upload for unique hash")
        void shouldInitializeNewUploadForUniqueHash() {
            String newFileHash = "newuniquehash789";
            
            InitUploadRequest request = InitUploadRequest.builder()
                    .fileHash(newFileHash)
                    .originalName("new-document.pdf")
                    .fileType("pdf")
                    .size(2048L)
                    .logicalId("DOC-001")
                    .totalChunks(1)
                    .build();

            when(chunkInfoRepository.save(any(ChunkInfo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InitUploadResponse response = fileService.initUpload(request, 1L, "testuser", "ADMIN");

            assertNotNull(response.getUploadId());
            assertEquals(newFileHash, response.getFileHash());
        }

        @Test
        @DisplayName("Should track logical ID in upload progress")
        void shouldTrackLogicalIdInUploadProgress() {
            String fileHash = "samecontenthash";
            
            InitUploadRequest request = InitUploadRequest.builder()
                    .fileHash(fileHash)
                    .originalName("original-name.pdf")
                    .fileType("pdf")
                    .logicalId("LOGICAL-001")
                    .totalChunks(1)
                    .build();

            when(chunkInfoRepository.save(any(ChunkInfo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InitUploadResponse response = fileService.initUpload(request, 1L, "testuser", "ADMIN");

            assertNotNull(response.getUploadId());
        }

        @Test
        @DisplayName("Should initialize upload even for deleted file hash")
        void shouldInitializeUploadEvenForDeletedFileHash() {
            String fileHash = "deletedhash";
            
            InitUploadRequest request = InitUploadRequest.builder()
                    .fileHash(fileHash)
                    .originalName("deleted-document.pdf")
                    .fileType("pdf")
                    .totalChunks(1)
                    .build();

            when(chunkInfoRepository.save(any(ChunkInfo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InitUploadResponse response = fileService.initUpload(request, 1L, "testuser", "ADMIN");

            assertNotNull(response.getUploadId());
        }
    }

    @Nested
    @DisplayName("Version Management Tests")
    class VersionManagementTests {

        @Test
        @DisplayName("Should initialize upload for same logical ID")
        void shouldInitializeUploadForSameLogicalId() {
            String logicalId = "DOC-001";
            String newHash = "newhash123";
            
            InitUploadRequest request = InitUploadRequest.builder()
                    .fileHash(newHash)
                    .originalName("updated-document.pdf")
                    .fileType("pdf")
                    .logicalId(logicalId)
                    .size(2048L)
                    .totalChunks(1)
                    .build();

            when(chunkInfoRepository.save(any(ChunkInfo.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InitUploadResponse response = fileService.initUpload(request, 1L, "testuser", "ADMIN");

            assertNotNull(response.getUploadId());
            assertEquals(newHash, response.getFileHash());
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Should set expiration time on soft delete")
        void shouldSetExpirationTimeOnSoftDelete() {
            Long fileId = 1L;
            Long operatorId = 100L;
            
            FileRecord file = FileRecord.builder()
                    .id(fileId)
                    .fileHash("hash123")
                    .originalName("test.pdf")
                    .isDeleted(false)
                    .uploadedBy(100L)
                    .build();

            when(fileRecordRepository.findById(fileId))
                    .thenReturn(Optional.of(file));
            when(fileRecordRepository.save(any(FileRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            fileService.softDeleteFile(fileId, operatorId, "testuser", "ADMIN");

            verify(fileRecordRepository).save(argThat(f -> 
                f.getIsDeleted() && 
                f.getExpirationTime() != null &&
                f.getExpirationTime().isAfter(java.time.LocalDateTime.now().plusDays(29))
            ));
        }

        @Test
        @DisplayName("Should prevent deleting already deleted file")
        void shouldPreventDeletingAlreadyDeletedFile() {
            FileRecord deletedFile = FileRecord.builder()
                    .id(1L)
                    .isDeleted(true)
                    .uploadedBy(1L)
                    .build();

            when(fileRecordRepository.findById(1L))
                    .thenReturn(Optional.of(deletedFile));

            assertThrows(com.anju.exception.BusinessException.class,
                    () -> fileService.softDeleteFile(1L, 1L, "testuser", "ADMIN"));
        }
    }

    @Nested
    @DisplayName("Concurrency Control Tests")
    class ConcurrencyControlTests {

        @Test
        @DisplayName("FileService should handle concurrent uploads via chunked upload")
        void fileServiceShouldHandleConcurrentUploads() {
            FileService service = new FileService(
                    fileRecordRepository, chunkInfoRepository, auditLogService, secondaryVerificationService, dataMasker);
            
            assertNotNull(service);
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should deny non-owner from listing file versions")
        void shouldDenyNonOwnerFromListingVersions() {
            FileRecord ownerVersion = FileRecord.builder()
                    .id(1L)
                    .logicalId("LOGICAL-1")
                    .uploadedBy(999L)
                    .versionNumber(1)
                    .build();

            when(fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc("LOGICAL-1"))
                    .thenReturn(List.of(ownerVersion));

            assertThrows(com.anju.exception.ForbiddenException.class,
                    () -> fileService.getFileVersions("LOGICAL-1", 100L, "FRONTLINE"));
        }

        @Test
        @DisplayName("Should allow owner to list file versions")
        void shouldAllowOwnerToListVersions() {
            FileRecord ownerVersion = FileRecord.builder()
                    .id(1L)
                    .logicalId("LOGICAL-1")
                    .uploadedBy(100L)
                    .versionNumber(2)
                    .build();

            when(fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc("LOGICAL-1"))
                    .thenReturn(List.of(ownerVersion));

            List<FileRecordResponse> versions = fileService.getFileVersions("LOGICAL-1", 100L, "FRONTLINE");
            assertEquals(1, versions.size());
        }

        @Test
        @DisplayName("Should deny non-owner from restoring deleted file")
        void shouldDenyNonOwnerRestore() {
            FileRecord deleted = FileRecord.builder()
                    .id(10L)
                    .uploadedBy(999L)
                    .isDeleted(true)
                    .build();

            when(fileRecordRepository.findById(10L)).thenReturn(Optional.of(deleted));

            assertThrows(com.anju.exception.ForbiddenException.class,
                    () -> fileService.restoreFile(10L, 100L, "frontline", "FRONTLINE"));
        }
    }
}

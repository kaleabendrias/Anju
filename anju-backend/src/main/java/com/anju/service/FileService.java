package com.anju.service;

import com.anju.dto.*;
import com.anju.entity.ChunkInfo;
import com.anju.entity.FileRecord;
import com.anju.exception.BusinessException;
import com.anju.exception.ForbiddenException;
import com.anju.exception.ResourceNotFoundException;
import com.anju.repository.ChunkInfoRepository;
import com.anju.repository.FileRecordRepository;
import com.anju.security.SecureDataMasker;
import com.anju.security.SecondaryVerificationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRecordRepository fileRecordRepository;
    private final ChunkInfoRepository chunkInfoRepository;
    private final AuditLogService auditLogService;
    private final SecondaryVerificationService secondaryVerificationService;
    private final SecureDataMasker dataMasker;

    private String storagePath;
    private String tempPath;
    private int retentionDays;

    public FileService(FileRecordRepository fileRecordRepository,
                      ChunkInfoRepository chunkInfoRepository,
                      AuditLogService auditLogService,
                      SecondaryVerificationService secondaryVerificationService,
                      SecureDataMasker dataMasker) {
        this.fileRecordRepository = fileRecordRepository;
        this.chunkInfoRepository = chunkInfoRepository;
        this.auditLogService = auditLogService;
        this.secondaryVerificationService = secondaryVerificationService;
        this.dataMasker = dataMasker;
    }

    @Value("${file.storage.path:./storage}")
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    @Value("${file.retention.days:30}")
    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(storagePath));
            Files.createDirectories(Paths.get(storagePath + "/files"));
            this.tempPath = storagePath + "/temp/chunks";
            Files.createDirectories(Paths.get(tempPath));
        } catch (IOException e) {
            log.error("Failed to create storage directories", e);
        }
    }

    @Transactional
    public FileRecordResponse uploadSimple(MultipartFile file, String logicalId, Long operatorId,
            String operatorUsername, String operatorRole) throws IOException {
        
        if (!canUploadFile(operatorRole)) {
            throw new ForbiddenException("You do not have permission to upload files");
        }

        String fileHash = calculateFileHash(file.getBytes());
        
        Optional<FileRecord> existing = fileRecordRepository.findByFileHash(fileHash);
        if (existing.isPresent() && !existing.get().getIsDeleted()) {
            FileRecord existingFile = existing.get();
            
            auditLogService.logOperation(
                    existingFile.getId(),
                    "FILE",
                    "DEDUPLICATION",
                    operatorId,
                    operatorUsername,
                    "{\"hash\":\"" + fileHash + "\"}",
                    "File deduplicated on upload: " + existingFile.getOriginalName(),
                    null
            );

            return FileRecordResponse.fromEntity(existingFile);
        }

        Path storagePathFile = Paths.get(storagePath + "/files/" + fileHash);
        Files.write(storagePathFile, file.getBytes());

        int nextVersion = 1;
        if (logicalId != null) {
            fileRecordRepository.findByLogicalIdAndIsActiveTrue(logicalId)
                    .ifPresent(currentActive -> {
                        currentActive.setIsActive(false);
                        fileRecordRepository.save(currentActive);
                    });
            
            List<FileRecord> existingVersions = fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc(logicalId);
            if (!existingVersions.isEmpty()) {
                nextVersion = existingVersions.get(0).getVersionNumber() + 1;
            }
        }

        FileRecord record = FileRecord.builder()
                .logicalId(logicalId)
                .fileHash(fileHash)
                .originalName(maskSensitiveFilename(file.getOriginalFilename()))
                .fileType(getFileType(file.getOriginalFilename()))
                .size(file.getSize())
                .versionNumber(nextVersion)
                .isDeleted(false)
                .isActive(true)
                .storagePath(storagePathFile.toString())
                .mimeType(file.getContentType())
                .uploadedBy(operatorId)
                .contentDisposition("attachment; filename=\"" + dataMasker.mask(file.getOriginalFilename()) + "\"")
                .build();

        FileRecord saved = fileRecordRepository.save(record);

        auditLogService.logOperation(
                saved.getId(),
                "FILE",
                AuditLogService.OPERATION_CREATE,
                operatorId,
                operatorUsername,
                String.format("{\"name\":\"%s\",\"size\":\"%d\",\"version\":\"%d\"}", 
                        saved.getOriginalName(), saved.getSize(), saved.getVersionNumber()),
                "File uploaded: " + saved.getOriginalName(),
                null
        );

        return FileRecordResponse.fromEntity(saved);
    }

    @Transactional
    public FileRecordResponse rollbackToVersion(String logicalId, Long targetVersionId, Long operatorId,
            String operatorUsername, String operatorRole, String secondaryPassword) {
        
        if (!secondaryVerificationService.verifySecondaryPassword(operatorUsername, secondaryPassword)) {
            throw new ForbiddenException("Secondary password verification failed for rollback operation");
        }

        FileRecord targetVersion = fileRecordRepository.findById(targetVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("File version not found: " + targetVersionId));

        if (!targetVersion.getLogicalId().equals(logicalId)) {
            throw new BusinessException("Version does not belong to this logical file");
        }

        validateObjectAccess(targetVersion, operatorId, operatorRole, "rollback");

        fileRecordRepository.findByLogicalIdAndIsActiveTrue(logicalId)
                .ifPresent(currentActive -> {
                    currentActive.setIsActive(false);
                    fileRecordRepository.save(currentActive);
                });

        targetVersion.setIsActive(true);
        FileRecord saved = fileRecordRepository.save(targetVersion);

        auditLogService.logOperation(
                saved.getId(),
                "FILE",
                AuditLogService.OPERATION_UPDATE,
                operatorId,
                operatorUsername,
                String.format("{\"rollback_to_version\":\"%d\"}", targetVersion.getVersionNumber()),
                "File rolled back to version " + targetVersion.getVersionNumber() + " by " + operatorUsername,
                null
        );

        log.info("File rolled back: logicalId={}, targetVersion={}, operator={}", 
                logicalId, targetVersionId, operatorUsername);
        return FileRecordResponse.fromEntity(saved);
    }

    @Transactional
    public void softDeleteFile(Long fileId, Long operatorId, String operatorUsername, String operatorRole) {
        FileRecord file = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        validateObjectAccess(file, operatorId, operatorRole, "delete");

        if (file.getIsDeleted()) {
            throw new BusinessException("File is already deleted");
        }

        LocalDateTime expirationTime = LocalDateTime.now().plusDays(retentionDays);
        file.setIsDeleted(true);
        file.setExpirationTime(expirationTime);
        file.setDeletedAt(LocalDateTime.now());
        file.setDeletedBy(operatorId);
        fileRecordRepository.save(file);

        auditLogService.logOperation(
                fileId,
                "FILE",
                AuditLogService.OPERATION_DELETE,
                operatorId,
                operatorUsername,
                String.format("{\"expiration_time\":\"%s\"}", expirationTime),
                "File soft deleted: " + file.getOriginalName() + " (retained until " + expirationTime + ")",
                null
        );

        log.info("File soft deleted: {}, expires at {}", fileId, expirationTime);
    }

    @Transactional
    public void permanentlyDeleteFile(Long fileId, Long operatorId, String operatorUsername,
            String operatorRole, String secondaryPassword) {
        
        if (!secondaryVerificationService.verifySecondaryPassword(operatorUsername, secondaryPassword)) {
            throw new ForbiddenException("Secondary password verification required for permanent deletion");
        }

        if (!"ADMIN".equals(operatorRole)) {
            throw new ForbiddenException("Only ADMIN can permanently delete files");
        }

        FileRecord file = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        if (!file.getIsDeleted()) {
            throw new BusinessException("Only deleted files can be permanently deleted");
        }

        try {
            Files.deleteIfExists(Paths.get(file.getStoragePath()));
        } catch (IOException e) {
            log.error("Failed to delete physical file", e);
        }

        fileRecordRepository.delete(file);

        auditLogService.logOperation(
                fileId,
                "FILE",
                "PERMANENT_DELETE",
                operatorId,
                operatorUsername,
                String.format("{\"hash\":\"%s\",\"name\":\"%s\"}", file.getFileHash(), file.getOriginalName()),
                "File permanently deleted: " + file.getOriginalName() + " by " + operatorUsername,
                null
        );

        log.info("File permanently deleted: {} by {}", fileId, operatorUsername);
    }

    public Resource loadFileAsResource(Long fileId, Long operatorId, String operatorRole) {
        FileRecord file = fileRecordRepository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
        
        validateObjectAccess(file, operatorId, operatorRole, "view");

        try {
            Path filePath = Paths.get(file.getStoragePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("File not readable: " + fileId);
            }
        } catch (Exception e) {
            throw new BusinessException("Could not read file: " + e.getMessage());
        }
    }

    public FileRecordResponse getFileInfo(Long fileId, Long operatorId, String operatorRole) {
        FileRecord file = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
        
        validateObjectAccess(file, operatorId, operatorRole, "view");
        
        List<FileRecord> history = fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc(
                file.getLogicalId());
        
        return FileRecordResponse.fromEntityWithHistory(file, history);
    }

    private void validateObjectAccess(FileRecord file, Long operatorId, String operatorRole, String action) {
        if ("ADMIN".equals(operatorRole)) {
            return;
        }

        if (file.getUploadedBy() != null && file.getUploadedBy().equals(operatorId)) {
            return;
        }

        if ("FRONTLINE".equals(operatorRole) && file.getUploadedBy() != null && file.getUploadedBy().equals(operatorId)) {
            return;
        }

        throw new ForbiddenException("You do not have permission to " + action + " this file");
    }

    private boolean canUploadFile(String operatorRole) {
        return "ADMIN".equals(operatorRole) || "FRONTLINE".equals(operatorRole);
    }

    private String maskSensitiveFilename(String filename) {
        if (filename == null) return "unnamed";
        int lastDot = filename.lastIndexOf('.');
        String name = lastDot > 0 ? filename.substring(0, lastDot) : filename;
        String ext = lastDot > 0 ? filename.substring(lastDot) : "";
        return dataMasker.mask(name) + ext;
    }

    private String calculateFileHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "unknown";
    }

    @Transactional
    public void cleanupExpiredFiles() {
        List<FileRecord> expiredFiles = fileRecordRepository.findByIsDeletedTrueAndExpirationTimeBefore(LocalDateTime.now());
        
        for (FileRecord file : expiredFiles) {
            try {
                Files.deleteIfExists(Paths.get(file.getStoragePath()));
            } catch (IOException e) {
                log.warn("Failed to delete physical file: {}", file.getStoragePath(), e);
            }
            fileRecordRepository.delete(file);
            log.info("Permanently deleted expired file: {} (hash: {})", file.getId(), file.getFileHash());
        }
        
        log.info("Cleanup completed: {} files permanently deleted", expiredFiles.size());
    }

    public List<FileRecordResponse> getFileVersions(String logicalId, Long operatorId, String operatorRole) {
        List<FileRecord> versions = fileRecordRepository.findByLogicalIdOrderByVersionNumberDesc(logicalId);
        return versions.stream().map(FileRecordResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void restoreFile(Long fileId, Long operatorId, String operatorUsername, String operatorRole) {
        FileRecord file = fileRecordRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        if (!file.getIsDeleted()) {
            throw new BusinessException("File is not deleted");
        }

        file.setIsDeleted(false);
        file.setExpirationTime(null);
        file.setDeletedAt(null);
        file.setDeletedBy(null);
        fileRecordRepository.save(file);

        auditLogService.logOperation(
                fileId,
                "FILE",
                "RESTORE",
                operatorId,
                operatorUsername,
                null,
                "File restored: " + file.getOriginalName(),
                null
        );

        log.info("File restored: {} by {}", fileId, operatorUsername);
    }

    public List<FileRecordResponse> getDeletedFiles(Long operatorId, String operatorRole) {
        List<FileRecord> files;
        
        if ("ADMIN".equals(operatorRole)) {
            files = fileRecordRepository.findByIsDeletedTrue();
        } else {
            files = fileRecordRepository.findByIsDeletedTrue().stream()
                    .filter(f -> f.getUploadedBy() != null && f.getUploadedBy().equals(operatorId))
                    .collect(Collectors.toList());
        }
        
        return files.stream().map(FileRecordResponse::fromEntity).collect(Collectors.toList());
    }

    public InitUploadResponse initUpload(InitUploadRequest request, Long operatorId, 
            String operatorUsername, String operatorRole) {
        String uploadId = UUID.randomUUID().toString();
        
        ChunkInfo chunkInfo = ChunkInfo.builder()
                .uploadId(uploadId)
                .fileHash(request.getFileHash())
                .originalName(request.getOriginalName())
                .fileType(getFileType(request.getOriginalName()))
                .size(request.getSize())
                .logicalId(request.getLogicalId())
                .mimeType(request.getMimeType())
                .totalChunks(request.getTotalChunks() != null ? request.getTotalChunks() : 1)
                .uploadedBytes(0L)
                .operatorId(operatorId)
                .operatorUsername(operatorUsername)
                .operatorRole(operatorRole)
                .isCompleted(false)
                .build();
        
        chunkInfoRepository.save(chunkInfo);
        
        String chunkDir = tempPath + "/" + uploadId;
        try {
            Files.createDirectories(Paths.get(chunkDir));
        } catch (IOException e) {
            log.error("Failed to create chunk directory", e);
            throw new BusinessException("Failed to initialize upload storage");
        }
        
        return InitUploadResponse.builder()
                .uploadId(uploadId)
                .fileHash(request.getFileHash())
                .deduplicated(false)
                .message("Upload initialized")
                .build();
    }

    public ChunkUploadResponse uploadChunk(String uploadId, Integer chunkIndex, Integer totalChunks, 
            byte[] data, Long operatorId, String operatorRole) {
        ChunkInfo chunkInfo = chunkInfoRepository.findByUploadIdAndIsCompletedFalse(uploadId)
                .orElseThrow(() -> new BusinessException("Upload not found: " + uploadId));
        
        if (!canUploadFile(operatorRole)) {
            throw new ForbiddenException("You do not have permission to upload files");
        }
        
        String chunkFilePath = tempPath + "/" + uploadId + "/chunk_" + String.format("%05d", chunkIndex);
        
        try {
            if (data != null && data.length > 0) {
                Files.write(Paths.get(chunkFilePath), data);
            }
        } catch (IOException e) {
            log.error("Failed to save chunk {} for upload {}", chunkIndex, uploadId, e);
            throw new BusinessException("Failed to save chunk data");
        }
        
        chunkInfo.setUploadedBytes(chunkInfo.getUploadedBytes() + (data != null ? data.length : 0));
        chunkInfoRepository.save(chunkInfo);
        
        return ChunkUploadResponse.builder()
                .uploadId(uploadId)
                .chunkIndex(chunkIndex)
                .totalChunks(chunkInfo.getTotalChunks())
                .uploaded(true)
                .message("Chunk received")
                .build();
    }

    public FileRecordResponse completeUpload(String uploadId, Long operatorId, String operatorUsername, String operatorRole) throws IOException {
        ChunkInfo chunkInfo = chunkInfoRepository.findByUploadIdAndIsCompletedFalse(uploadId)
                .orElseThrow(() -> new BusinessException("Upload not found: " + uploadId));
        
        String chunkDir = tempPath + "/" + uploadId;
        Path reconstructedFile = Paths.get(tempPath + "/" + uploadId + "_reconstructed");
        
        try {
            Files.createFile(reconstructedFile);
            
            for (int i = 0; i < chunkInfo.getTotalChunks(); i++) {
                Path chunkFile = Paths.get(chunkDir + "/chunk_" + String.format("%05d", i));
                if (!Files.exists(chunkFile)) {
                    throw new BusinessException("Missing chunk " + i + " for upload " + uploadId);
                }
                byte[] chunkData = Files.readAllBytes(chunkFile);
                Files.write(reconstructedFile, chunkData, StandardOpenOption.APPEND);
            }
            
            byte[] fileData = Files.readAllBytes(reconstructedFile);
            
            MultipartFile reconstructedMultipart = new CustomMultipartFile(
                    fileData, 
                    chunkInfo.getOriginalName(), 
                    chunkInfo.getMimeType() != null ? chunkInfo.getMimeType() : "application/octet-stream"
            );
            
            FileRecordResponse response = uploadSimple(reconstructedMultipart, chunkInfo.getLogicalId(), 
                    operatorId, operatorUsername, operatorRole);
            
            Files.deleteIfExists(reconstructedFile);
            deleteChunkDirectory(Paths.get(chunkDir));
            
            chunkInfo.setIsCompleted(true);
            chunkInfoRepository.save(chunkInfo);
            
            return response;
            
        } catch (IOException e) {
            log.error("Failed to complete upload {}", uploadId, e);
            throw new BusinessException("Failed to reconstruct file from chunks");
        }
    }
    
    private void deleteChunkDirectory(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path entry : stream) {
                        Files.deleteIfExists(entry);
                    }
                }
                Files.deleteIfExists(dir);
            }
        } catch (IOException e) {
            log.warn("Failed to delete chunk directory {}", dir, e);
        }
    }

    private static class CustomMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;
        private final String contentType;
        
        public CustomMultipartFile(byte[] content, String name, String contentType) {
            this.content = content;
            this.name = name;
            this.contentType = contentType;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getOriginalFilename() {
            return name;
        }
        
        @Override
        public String getContentType() {
            return contentType;
        }
        
        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }
        
        @Override
        public long getSize() {
            return content != null ? content.length : 0;
        }
        
        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}

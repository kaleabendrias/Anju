package com.anju.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_records", indexes = {
    @Index(name = "idx_file_hash", columnList = "file_hash", unique = true),
    @Index(name = "idx_logical_id", columnList = "logical_id"),
    @Index(name = "idx_is_deleted_expired", columnList = "is_deleted, expiration_time")
})
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logical_id", length = 64)
    private String logicalId;

    @Column(name = "file_hash", nullable = false, unique = true, length = 64)
    private String fileHash;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "content_disposition", length = 255)
    private String contentDisposition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    public FileRecord() {}

    public FileRecord(Long id, String logicalId, String fileHash, String originalName, String fileType,
                      Long size, Integer versionNumber, Boolean isDeleted, Boolean isActive,
                      LocalDateTime expirationTime, String storagePath, String mimeType, Long uploadedBy,
                      String contentDisposition, LocalDateTime createdAt, LocalDateTime updatedAt,
                      LocalDateTime deletedAt, Long deletedBy) {
        this.id = id;
        this.logicalId = logicalId;
        this.fileHash = fileHash;
        this.originalName = originalName;
        this.fileType = fileType;
        this.size = size;
        this.versionNumber = versionNumber;
        this.isDeleted = isDeleted;
        this.isActive = isActive;
        this.expirationTime = expirationTime;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.uploadedBy = uploadedBy;
        this.contentDisposition = contentDisposition;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static FileRecordBuilder builder() {
        return new FileRecordBuilder();
    }

    public static class FileRecordBuilder {
        private Long id;
        private String logicalId;
        private String fileHash;
        private String originalName;
        private String fileType;
        private Long size;
        private Integer versionNumber = 1;
        private Boolean isDeleted = false;
        private Boolean isActive = true;
        private LocalDateTime expirationTime;
        private String storagePath;
        private String mimeType;
        private Long uploadedBy;
        private String contentDisposition;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private Long deletedBy;

        public FileRecordBuilder id(Long id) { this.id = id; return this; }
        public FileRecordBuilder logicalId(String logicalId) { this.logicalId = logicalId; return this; }
        public FileRecordBuilder fileHash(String fileHash) { this.fileHash = fileHash; return this; }
        public FileRecordBuilder originalName(String originalName) { this.originalName = originalName; return this; }
        public FileRecordBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public FileRecordBuilder size(Long size) { this.size = size; return this; }
        public FileRecordBuilder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public FileRecordBuilder isDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; return this; }
        public FileRecordBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public FileRecordBuilder expirationTime(LocalDateTime expirationTime) { this.expirationTime = expirationTime; return this; }
        public FileRecordBuilder storagePath(String storagePath) { this.storagePath = storagePath; return this; }
        public FileRecordBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public FileRecordBuilder uploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; return this; }
        public FileRecordBuilder contentDisposition(String contentDisposition) { this.contentDisposition = contentDisposition; return this; }
        public FileRecordBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FileRecordBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public FileRecordBuilder deletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; return this; }
        public FileRecordBuilder deletedBy(Long deletedBy) { this.deletedBy = deletedBy; return this; }

        public FileRecord build() {
            return new FileRecord(id, logicalId, fileHash, originalName, fileType, size, versionNumber,
                isDeleted, isActive, expirationTime, storagePath, mimeType, uploadedBy, contentDisposition,
                createdAt, updatedAt, deletedAt, deletedBy);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLogicalId() { return logicalId; }
    public void setLogicalId(String logicalId) { this.logicalId = logicalId; }
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public void setExpirationTime(LocalDateTime expirationTime) { this.expirationTime = expirationTime; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getContentDisposition() { return contentDisposition; }
    public void setContentDisposition(String contentDisposition) { this.contentDisposition = contentDisposition; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
}

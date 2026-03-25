package com.anju.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;

@Entity
@Table(name = "chunk_info", indexes = {
    @Index(name = "idx_chunk_upload_id", columnList = "upload_id", unique = true),
    @Index(name = "idx_chunk_file_hash", columnList = "file_hash")
})
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class ChunkInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_id", nullable = false, unique = true, length = 64)
    private String uploadId;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "logical_id", length = 64)
    private String logicalId;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "total_chunks", nullable = false)
    private Integer totalChunks;

    @Column(name = "uploaded_bytes", nullable = false)
    private Long uploadedBytes;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_username", nullable = false, length = 100)
    private String operatorUsername;

    @Column(name = "operator_role", nullable = false, length = 50)
    private String operatorRole;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChunkInfo() {}

    public ChunkInfo(Long id, String uploadId, String fileHash, String originalName, String fileType,
                     Long size, String logicalId, String mimeType, Integer totalChunks, Long uploadedBytes,
                     Long operatorId, String operatorUsername, String operatorRole, Boolean isCompleted,
                     LocalDateTime createdAt) {
        this.id = id;
        this.uploadId = uploadId;
        this.fileHash = fileHash;
        this.originalName = originalName;
        this.fileType = fileType;
        this.size = size;
        this.logicalId = logicalId;
        this.mimeType = mimeType;
        this.totalChunks = totalChunks;
        this.uploadedBytes = uploadedBytes;
        this.operatorId = operatorId;
        this.operatorUsername = operatorUsername;
        this.operatorRole = operatorRole;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public static ChunkInfoBuilder builder() {
        return new ChunkInfoBuilder();
    }

    public static class ChunkInfoBuilder {
        private Long id;
        private String uploadId;
        private String fileHash;
        private String originalName;
        private String fileType;
        private Long size;
        private String logicalId;
        private String mimeType;
        private Integer totalChunks;
        private Long uploadedBytes;
        private Long operatorId;
        private String operatorUsername;
        private String operatorRole;
        private Boolean isCompleted = false;
        private LocalDateTime createdAt;

        public ChunkInfoBuilder id(Long id) { this.id = id; return this; }
        public ChunkInfoBuilder uploadId(String uploadId) { this.uploadId = uploadId; return this; }
        public ChunkInfoBuilder fileHash(String fileHash) { this.fileHash = fileHash; return this; }
        public ChunkInfoBuilder originalName(String originalName) { this.originalName = originalName; return this; }
        public ChunkInfoBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public ChunkInfoBuilder size(Long size) { this.size = size; return this; }
        public ChunkInfoBuilder logicalId(String logicalId) { this.logicalId = logicalId; return this; }
        public ChunkInfoBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public ChunkInfoBuilder totalChunks(Integer totalChunks) { this.totalChunks = totalChunks; return this; }
        public ChunkInfoBuilder uploadedBytes(Long uploadedBytes) { this.uploadedBytes = uploadedBytes; return this; }
        public ChunkInfoBuilder operatorId(Long operatorId) { this.operatorId = operatorId; return this; }
        public ChunkInfoBuilder operatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; return this; }
        public ChunkInfoBuilder operatorRole(String operatorRole) { this.operatorRole = operatorRole; return this; }
        public ChunkInfoBuilder isCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; return this; }
        public ChunkInfoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ChunkInfo build() {
            return new ChunkInfo(id, uploadId, fileHash, originalName, fileType, size, logicalId,
                mimeType, totalChunks, uploadedBytes, operatorId, operatorUsername, operatorRole,
                isCompleted, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getLogicalId() { return logicalId; }
    public void setLogicalId(String logicalId) { this.logicalId = logicalId; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }
    public Long getUploadedBytes() { return uploadedBytes; }
    public void setUploadedBytes(Long uploadedBytes) { this.uploadedBytes = uploadedBytes; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }
    public String getOperatorRole() { return operatorRole; }
    public void setOperatorRole(String operatorRole) { this.operatorRole = operatorRole; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
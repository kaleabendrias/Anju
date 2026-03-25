package com.anju.dto;

import com.anju.entity.FileRecord;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class FileRecordResponse {
    private Long id;
    private String logicalId;
    private String fileHash;
    private String originalName;
    private String fileType;
    private Long size;
    private Integer versionNumber;
    private Boolean isDeleted;
    private Boolean isActive;
    private LocalDateTime expirationTime;
    private String mimeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<FileVersionInfo> versionHistory;

    public FileRecordResponse() {
    }

    public FileRecordResponse(Long id, String logicalId, String fileHash, String originalName, 
                              String fileType, Long size, Integer versionNumber, 
                              Boolean isDeleted, Boolean isActive, LocalDateTime expirationTime, 
                              String mimeType, LocalDateTime createdAt, LocalDateTime updatedAt, 
                              LocalDateTime deletedAt, List<FileVersionInfo> versionHistory) {
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
        this.mimeType = mimeType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.versionHistory = versionHistory;
    }

    public static FileRecordResponse fromEntity(FileRecord record) {
        return FileRecordResponse.builder()
                .id(record.getId())
                .logicalId(record.getLogicalId())
                .fileHash(record.getFileHash())
                .originalName(record.getOriginalName())
                .fileType(record.getFileType())
                .size(record.getSize())
                .versionNumber(record.getVersionNumber())
                .isDeleted(record.getIsDeleted())
                .isActive(record.getIsActive())
                .expirationTime(record.getExpirationTime())
                .mimeType(record.getMimeType())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .deletedAt(record.getDeletedAt())
                .build();
    }

    public static FileRecordResponse fromEntityWithHistory(FileRecord record, List<FileRecord> history) {
        FileRecordResponse response = fromEntity(record);
        if (history != null && !history.isEmpty()) {
            response.setVersionHistory(history.stream()
                    .map(FileVersionInfo::fromEntity)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<FileVersionInfo> getVersionHistory() {
        return versionHistory;
    }

    public void setVersionHistory(List<FileVersionInfo> versionHistory) {
        this.versionHistory = versionHistory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String logicalId;
        private String fileHash;
        private String originalName;
        private String fileType;
        private Long size;
        private Integer versionNumber;
        private Boolean isDeleted;
        private Boolean isActive;
        private LocalDateTime expirationTime;
        private String mimeType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
        private List<FileVersionInfo> versionHistory;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder logicalId(String logicalId) {
            this.logicalId = logicalId;
            return this;
        }

        public Builder fileHash(String fileHash) {
            this.fileHash = fileHash;
            return this;
        }

        public Builder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }

        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        public Builder versionNumber(Integer versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder expirationTime(LocalDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder versionHistory(List<FileVersionInfo> versionHistory) {
            this.versionHistory = versionHistory;
            return this;
        }

        public FileRecordResponse build() {
            return new FileRecordResponse(id, logicalId, fileHash, originalName, 
                                          fileType, size, versionNumber, isDeleted, 
                                          isActive, expirationTime, mimeType, 
                                          createdAt, updatedAt, deletedAt, versionHistory);
        }
    }

    // @Data
    // @Builder
    // @NoArgsConstructor
    // @AllArgsConstructor
    public static class FileVersionInfo {
        private Long id;
        private Integer versionNumber;
        private LocalDateTime createdAt;
        private Boolean isActive;

        public FileVersionInfo() {
        }

        public FileVersionInfo(Long id, Integer versionNumber, LocalDateTime createdAt, 
                               Boolean isActive) {
            this.id = id;
            this.versionNumber = versionNumber;
            this.createdAt = createdAt;
            this.isActive = isActive;
        }

        public static FileVersionInfo fromEntity(FileRecord record) {
            return FileVersionInfo.builder()
                    .id(record.getId())
                    .versionNumber(record.getVersionNumber())
                    .createdAt(record.getCreatedAt())
                    .isActive(record.getIsActive())
                    .build();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getVersionNumber() {
            return versionNumber;
        }

        public void setVersionNumber(Integer versionNumber) {
            this.versionNumber = versionNumber;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }

        public static FileVersionInfoBuilder builder() {
            return new FileVersionInfoBuilder();
        }

        public static class FileVersionInfoBuilder {
            private Long id;
            private Integer versionNumber;
            private LocalDateTime createdAt;
            private Boolean isActive;

            public FileVersionInfoBuilder id(Long id) {
                this.id = id;
                return this;
            }

            public FileVersionInfoBuilder versionNumber(Integer versionNumber) {
                this.versionNumber = versionNumber;
                return this;
            }

            public FileVersionInfoBuilder createdAt(LocalDateTime createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public FileVersionInfoBuilder isActive(Boolean isActive) {
                this.isActive = isActive;
                return this;
            }

            public FileVersionInfo build() {
                return new FileVersionInfo(id, versionNumber, createdAt, isActive);
            }
        }
    }
}

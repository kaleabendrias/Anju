package com.anju.dto;

import jakarta.validation.constraints.NotBlank;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class InitUploadRequest {
    @NotBlank(message = "File hash is required")
    private String fileHash;
    
    @NotBlank(message = "Original name is required")
    private String originalName;
    
    @NotBlank(message = "File type is required")
    private String fileType;
    
    private Long size;
    
    private String logicalId;
    
    private String mimeType;
    
    private Integer totalChunks;

    public InitUploadRequest() {
    }

    public InitUploadRequest(String fileHash, String originalName, String fileType, 
                              Long size, String logicalId, String mimeType, 
                              Integer totalChunks) {
        this.fileHash = fileHash;
        this.originalName = originalName;
        this.fileType = fileType;
        this.size = size;
        this.logicalId = logicalId;
        this.mimeType = mimeType;
        this.totalChunks = totalChunks;
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

    public String getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileHash;
        private String originalName;
        private String fileType;
        private Long size;
        private String logicalId;
        private String mimeType;
        private Integer totalChunks;

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

        public Builder logicalId(String logicalId) {
            this.logicalId = logicalId;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder totalChunks(Integer totalChunks) {
            this.totalChunks = totalChunks;
            return this;
        }

        public InitUploadRequest build() {
            return new InitUploadRequest(fileHash, originalName, fileType, 
                                         size, logicalId, mimeType, totalChunks);
        }
    }
}

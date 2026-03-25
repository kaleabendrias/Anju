package com.anju.dto;

// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class InitUploadResponse {
    private String uploadId;
    private boolean deduplicated;
    private Long fileId;
    private String fileHash;
    private Integer versionNumber;
    private String message;

    public InitUploadResponse() {
    }

    public InitUploadResponse(String uploadId, boolean deduplicated, Long fileId, 
                               String fileHash, Integer versionNumber, String message) {
        this.uploadId = uploadId;
        this.deduplicated = deduplicated;
        this.fileId = fileId;
        this.fileHash = fileHash;
        this.versionNumber = versionNumber;
        this.message = message;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public boolean isDeduplicated() {
        return deduplicated;
    }

    public void setDeduplicated(boolean deduplicated) {
        this.deduplicated = deduplicated;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uploadId;
        private boolean deduplicated;
        private Long fileId;
        private String fileHash;
        private Integer versionNumber;
        private String message;

        public Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public Builder deduplicated(boolean deduplicated) {
            this.deduplicated = deduplicated;
            return this;
        }

        public Builder fileId(Long fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder fileHash(String fileHash) {
            this.fileHash = fileHash;
            return this;
        }

        public Builder versionNumber(Integer versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public InitUploadResponse build() {
            return new InitUploadResponse(uploadId, deduplicated, fileId, 
                                          fileHash, versionNumber, message);
        }
    }
}

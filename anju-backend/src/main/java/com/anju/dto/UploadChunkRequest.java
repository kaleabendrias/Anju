package com.anju.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class UploadChunkRequest {
    @NotBlank(message = "Upload ID is required")
    private String uploadId;
    
    @NotNull(message = "Chunk index is required")
    private Integer chunkIndex;
    
    @NotNull(message = "Total chunks is required")
    private Integer totalChunks;
    
    private byte[] data;
    
    private String mimeType;

    public UploadChunkRequest() {
    }

    public UploadChunkRequest(String uploadId, Integer chunkIndex, Integer totalChunks, 
                              byte[] data, String mimeType) {
        this.uploadId = uploadId;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.data = data;
        this.mimeType = mimeType;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uploadId;
        private Integer chunkIndex;
        private Integer totalChunks;
        private byte[] data;
        private String mimeType;

        public Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public Builder chunkIndex(Integer chunkIndex) {
            this.chunkIndex = chunkIndex;
            return this;
        }

        public Builder totalChunks(Integer totalChunks) {
            this.totalChunks = totalChunks;
            return this;
        }

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public UploadChunkRequest build() {
            return new UploadChunkRequest(uploadId, chunkIndex, totalChunks, data, mimeType);
        }
    }
}

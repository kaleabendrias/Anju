package com.anju.dto;

// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class ChunkUploadResponse {
    private String uploadId;
    private Integer chunkIndex;
    private Integer totalChunks;
    private boolean uploaded;
    private boolean complete;
    private Long fileId;
    private String fileHash;
    private String message;

    public ChunkUploadResponse() {
    }

    public ChunkUploadResponse(String uploadId, Integer chunkIndex, Integer totalChunks, 
                               boolean uploaded, boolean complete, Long fileId, 
                               String fileHash, String message) {
        this.uploadId = uploadId;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.uploaded = uploaded;
        this.complete = complete;
        this.fileId = fileId;
        this.fileHash = fileHash;
        this.message = message;
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

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
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
        private Integer chunkIndex;
        private Integer totalChunks;
        private boolean uploaded;
        private boolean complete;
        private Long fileId;
        private String fileHash;
        private String message;

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

        public Builder uploaded(boolean uploaded) {
            this.uploaded = uploaded;
            return this;
        }

        public Builder complete(boolean complete) {
            this.complete = complete;
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

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ChunkUploadResponse build() {
            return new ChunkUploadResponse(uploadId, chunkIndex, totalChunks, 
                                           uploaded, complete, fileId, fileHash, message);
        }
    }
}

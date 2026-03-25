package com.anju.dto;

// import lombok.Data;
// import lombok.Builder;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;

// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public class ThrottleStatusResponse {
    private boolean uploadAllowed;
    private long uploadUsedBytes;
    private long uploadLimitBytes;
    private long uploadRemainingBytes;
    private boolean downloadAllowed;
    private long downloadUsedBytes;
    private long downloadLimitBytes;
    private long downloadRemainingBytes;

    public ThrottleStatusResponse() {
    }

    public ThrottleStatusResponse(boolean uploadAllowed, long uploadUsedBytes, 
                                   long uploadLimitBytes, long uploadRemainingBytes, 
                                   boolean downloadAllowed, long downloadUsedBytes, 
                                   long downloadLimitBytes, long downloadRemainingBytes) {
        this.uploadAllowed = uploadAllowed;
        this.uploadUsedBytes = uploadUsedBytes;
        this.uploadLimitBytes = uploadLimitBytes;
        this.uploadRemainingBytes = uploadRemainingBytes;
        this.downloadAllowed = downloadAllowed;
        this.downloadUsedBytes = downloadUsedBytes;
        this.downloadLimitBytes = downloadLimitBytes;
        this.downloadRemainingBytes = downloadRemainingBytes;
    }

    public boolean isUploadAllowed() {
        return uploadAllowed;
    }

    public void setUploadAllowed(boolean uploadAllowed) {
        this.uploadAllowed = uploadAllowed;
    }

    public long getUploadUsedBytes() {
        return uploadUsedBytes;
    }

    public void setUploadUsedBytes(long uploadUsedBytes) {
        this.uploadUsedBytes = uploadUsedBytes;
    }

    public long getUploadLimitBytes() {
        return uploadLimitBytes;
    }

    public void setUploadLimitBytes(long uploadLimitBytes) {
        this.uploadLimitBytes = uploadLimitBytes;
    }

    public long getUploadRemainingBytes() {
        return uploadRemainingBytes;
    }

    public void setUploadRemainingBytes(long uploadRemainingBytes) {
        this.uploadRemainingBytes = uploadRemainingBytes;
    }

    public boolean isDownloadAllowed() {
        return downloadAllowed;
    }

    public void setDownloadAllowed(boolean downloadAllowed) {
        this.downloadAllowed = downloadAllowed;
    }

    public long getDownloadUsedBytes() {
        return downloadUsedBytes;
    }

    public void setDownloadUsedBytes(long downloadUsedBytes) {
        this.downloadUsedBytes = downloadUsedBytes;
    }

    public long getDownloadLimitBytes() {
        return downloadLimitBytes;
    }

    public void setDownloadLimitBytes(long downloadLimitBytes) {
        this.downloadLimitBytes = downloadLimitBytes;
    }

    public long getDownloadRemainingBytes() {
        return downloadRemainingBytes;
    }

    public void setDownloadRemainingBytes(long downloadRemainingBytes) {
        this.downloadRemainingBytes = downloadRemainingBytes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean uploadAllowed;
        private long uploadUsedBytes;
        private long uploadLimitBytes;
        private long uploadRemainingBytes;
        private boolean downloadAllowed;
        private long downloadUsedBytes;
        private long downloadLimitBytes;
        private long downloadRemainingBytes;

        public Builder uploadAllowed(boolean uploadAllowed) {
            this.uploadAllowed = uploadAllowed;
            return this;
        }

        public Builder uploadUsedBytes(long uploadUsedBytes) {
            this.uploadUsedBytes = uploadUsedBytes;
            return this;
        }

        public Builder uploadLimitBytes(long uploadLimitBytes) {
            this.uploadLimitBytes = uploadLimitBytes;
            return this;
        }

        public Builder uploadRemainingBytes(long uploadRemainingBytes) {
            this.uploadRemainingBytes = uploadRemainingBytes;
            return this;
        }

        public Builder downloadAllowed(boolean downloadAllowed) {
            this.downloadAllowed = downloadAllowed;
            return this;
        }

        public Builder downloadUsedBytes(long downloadUsedBytes) {
            this.downloadUsedBytes = downloadUsedBytes;
            return this;
        }

        public Builder downloadLimitBytes(long downloadLimitBytes) {
            this.downloadLimitBytes = downloadLimitBytes;
            return this;
        }

        public Builder downloadRemainingBytes(long downloadRemainingBytes) {
            this.downloadRemainingBytes = downloadRemainingBytes;
            return this;
        }

        public ThrottleStatusResponse build() {
            return new ThrottleStatusResponse(uploadAllowed, uploadUsedBytes, 
                                             uploadLimitBytes, uploadRemainingBytes, 
                                             downloadAllowed, downloadUsedBytes, 
                                             downloadLimitBytes, downloadRemainingBytes);
        }
    }
}

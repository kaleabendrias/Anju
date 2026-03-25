package com.anju.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class FileThrottleServiceTest {

    private FileThrottleService fileThrottleService;

    @BeforeEach
    void setUp() {
        fileThrottleService = new FileThrottleService();
        ReflectionTestUtils.setField(fileThrottleService, "maxConcurrentUploads", 10);
        ReflectionTestUtils.setField(fileThrottleService, "maxConcurrentDownloads", 50);
        ReflectionTestUtils.setField(fileThrottleService, "bytesPerSecondUpload", 10485760L);
        ReflectionTestUtils.setField(fileThrottleService, "bytesPerSecondDownload", 52428800L);
        ReflectionTestUtils.setField(fileThrottleService, "windowSeconds", 60);
        fileThrottleService.init();
    }

    @Test
    @DisplayName("Should allow upload when under limits")
    void shouldAllowUploadWhenUnderLimits() {
        assertTrue(fileThrottleService.tryAcquireUploadSlot("operator-1"));
        fileThrottleService.releaseUploadSlot("operator-1");
    }

    @Test
    @DisplayName("Should track uploaded bytes")
    void shouldTrackUploadedBytes() {
        fileThrottleService.tryAcquireUploadSlot("operator-2");
        fileThrottleService.recordBytesUploaded("operator-2", 1024);
        
        var status = fileThrottleService.getUploadStatus("operator-2");
        assertTrue(status.usedBytes() >= 1024);
    }

    @Test
    @DisplayName("Should release upload slot")
    void shouldReleaseUploadSlot() {
        assertTrue(fileThrottleService.tryAcquireUploadSlot("operator-3"));
        fileThrottleService.releaseUploadSlot("operator-3");
        assertTrue(fileThrottleService.tryAcquireUploadSlot("operator-3"));
    }

    @Test
    @DisplayName("Should allow download when under limits")
    void shouldAllowDownloadWhenUnderLimits() {
        assertTrue(fileThrottleService.tryAcquireDownloadSlot("operator-4"));
        fileThrottleService.releaseDownloadSlot("operator-4");
    }

    @Test
    @DisplayName("Should track downloaded bytes")
    void shouldTrackDownloadedBytes() {
        fileThrottleService.tryAcquireDownloadSlot("operator-5");
        fileThrottleService.recordBytesDownloaded("operator-5", 2048);
        
        var status = fileThrottleService.getDownloadStatus("operator-5");
        assertTrue(status.usedBytes() >= 2048);
    }

    @Test
    @DisplayName("Should return valid status for new operator")
    void shouldReturnValidStatusForNewOperator() {
        var status = fileThrottleService.getUploadStatus("new-operator");
        
        assertTrue(status.allowed());
        assertEquals(0, status.usedBytes());
    }

    @Test
    @DisplayName("Should cleanup expired entries")
    void shouldCleanupExpiredEntries() {
        fileThrottleService.tryAcquireUploadSlot("operator-to-cleanup");
        fileThrottleService.cleanup();
    }
}

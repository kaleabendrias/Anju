package com.anju.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FileThrottleService {

    private static final Logger log = LoggerFactory.getLogger(FileThrottleService.class);

    @Value("${file.throttle.max-concurrent-uploads:10}")
    private int maxConcurrentUploads;

    @Value("${file.throttle.max-concurrent-downloads:50}")
    private int maxConcurrentDownloads;

    @Value("${file.throttle.bytes-per-second-upload:10485760}")
    private long bytesPerSecondUpload;

    @Value("${file.throttle.bytes-per-second-download:52428800}")
    private long bytesPerSecondDownload;

    @Value("${file.throttle.window-seconds:60}")
    private int windowSeconds;

    private final Semaphore uploadSemaphore;
    private final Semaphore downloadSemaphore;
    
    private final Map<String, OperatorThrottle> uploadThrottles = new ConcurrentHashMap<>();
    private final Map<String, OperatorThrottle> downloadThrottles = new ConcurrentHashMap<>();
    private final Map<String, Semaphore> uploadSlots = new ConcurrentHashMap<>();
    private final Map<String, Semaphore> downloadSlots = new ConcurrentHashMap<>();

    public FileThrottleService() {
        this.uploadSemaphore = new Semaphore(10);
        this.downloadSemaphore = new Semaphore(50);
    }

    @PostConstruct
    public void init() {
        log.info("File throttle initialized: maxConcurrentUploads={}, maxConcurrentDownloads={}, " +
                "uploadRate={} B/s, downloadRate={} B/s, window={}s",
                maxConcurrentUploads, maxConcurrentDownloads, 
                bytesPerSecondUpload, bytesPerSecondDownload, windowSeconds);
    }

    public boolean tryAcquireUploadSlot(String operatorId) {
        Semaphore slot = uploadSlots.computeIfAbsent(operatorId, 
            k -> new Semaphore(maxConcurrentUploads / 2 + 1));
        
        if (!slot.tryAcquire()) {
            log.warn("Upload slot denied for operator: {}, all {} slots occupied", 
                    operatorId, slot.availablePermits());
            return false;
        }
        
        OperatorThrottle throttle = uploadThrottles.computeIfAbsent(operatorId, 
            k -> new OperatorThrottle());
        
        if (!throttle.tryConsume(bytesPerSecondUpload / 10)) {
            slot.release();
            log.warn("Upload rate limit exceeded for operator: {}", operatorId);
            return false;
        }
        
        return true;
    }

    public void releaseUploadSlot(String operatorId) {
        Semaphore slot = uploadSlots.get(operatorId);
        if (slot != null) {
            slot.release();
        }
    }

    public boolean tryAcquireDownloadSlot(String operatorId) {
        Semaphore slot = downloadSlots.computeIfAbsent(operatorId, 
            k -> new Semaphore(maxConcurrentDownloads / 5 + 1));
        
        if (!slot.tryAcquire()) {
            log.warn("Download slot denied for operator: {}", operatorId);
            return false;
        }
        
        OperatorThrottle throttle = downloadThrottles.computeIfAbsent(operatorId, 
            k -> new OperatorThrottle());
        
        if (!throttle.tryConsume(bytesPerSecondDownload / 10)) {
            slot.release();
            log.warn("Download rate limit exceeded for operator: {}", operatorId);
            return false;
        }
        
        return true;
    }

    public void releaseDownloadSlot(String operatorId) {
        Semaphore slot = downloadSlots.get(operatorId);
        if (slot != null) {
            slot.release();
        }
    }

    public void recordBytesUploaded(String operatorId, long bytes) {
        OperatorThrottle throttle = uploadThrottles.get(operatorId);
        if (throttle != null) {
            throttle.recordBytes(bytes);
        }
    }

    public void recordBytesDownloaded(String operatorId, long bytes) {
        OperatorThrottle throttle = downloadThrottles.get(operatorId);
        if (throttle != null) {
            throttle.recordBytes(bytes);
        }
    }

    public ThrottleStatus getUploadStatus(String operatorId) {
        OperatorThrottle throttle = uploadThrottles.get(operatorId);
        if (throttle == null) {
            return new ThrottleStatus(true, 0, 0, 0);
        }
        return throttle.getStatus(bytesPerSecondUpload, windowSeconds);
    }

    public ThrottleStatus getDownloadStatus(String operatorId) {
        OperatorThrottle throttle = downloadThrottles.get(operatorId);
        if (throttle == null) {
            return new ThrottleStatus(true, 0, 0, 0);
        }
        return throttle.getStatus(bytesPerSecondDownload, windowSeconds);
    }

    public void cleanup() {
        Instant cutoff = Instant.now().minusSeconds(windowSeconds);
        uploadThrottles.entrySet().removeIf(e -> e.getValue().isExpired(cutoff));
        downloadThrottles.entrySet().removeIf(e -> e.getValue().isExpired(cutoff));
        uploadSlots.entrySet().removeIf(e -> e.getValue().availablePermits() == 0 && e.getValue().hasQueuedThreads());
        downloadSlots.entrySet().removeIf(e -> e.getValue().availablePermits() == 0 && e.getValue().hasQueuedThreads());
    }

    public record ThrottleStatus(
        boolean allowed, 
        long usedBytes, 
        long limitBytes, 
        long remainingBytes
    ) {}

    private static class OperatorThrottle {
        private final AtomicLong totalBytes = new AtomicLong(0);
        private volatile Instant windowStart = Instant.now();

        synchronized boolean tryConsume(long bytes) {
            resetIfExpired();
            if (totalBytes.get() + bytes > getWindowLimit()) {
                return false;
            }
            totalBytes.addAndGet(bytes);
            return true;
        }

        void recordBytes(long bytes) {
            resetIfExpired();
            totalBytes.addAndGet(bytes);
        }

        ThrottleStatus getStatus(long limitBytes, int windowSecs) {
            resetIfExpired();
            long used = totalBytes.get();
            long remaining = Math.max(0, limitBytes - used);
            boolean allowed = remaining > 0;
            return new ThrottleStatus(allowed, used, limitBytes, remaining);
        }

        boolean isExpired(Instant cutoff) {
            return windowStart.isBefore(cutoff);
        }

        private long getWindowLimit() {
            return 10485760L;
        }

        private synchronized void resetIfExpired() {
            Duration elapsed = Duration.between(windowStart, Instant.now());
            if (elapsed.getSeconds() >= 60) {
                totalBytes.set(0);
                windowStart = Instant.now();
            }
        }
    }
}

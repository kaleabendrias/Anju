package com.anju.scheduler;

import com.anju.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FileCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupTask.class);

    private final FileService fileService;

    public FileCleanupTask(FileService fileService) {
        this.fileService = fileService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredFiles() {
        log.info("Starting daily file cleanup task");
        try {
            fileService.cleanupExpiredFiles();
            log.info("Daily file cleanup task completed");
        } catch (Exception e) {
            log.error("Error during file cleanup", e);
        }
    }
}

package com.anju.scheduler;

import com.anju.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyStatementTask {

    private static final Logger log = LoggerFactory.getLogger(DailyStatementTask.class);

    private final SettlementService settlementService;

    public DailyStatementTask(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyStatement() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting daily settlement generation for date: {}", yesterday);
        
        try {
            settlementService.generateDailyStatement(yesterday, null, "SYSTEM", "ADMIN");
            log.info("Daily settlement generation completed for date: {}", yesterday);
        } catch (Exception e) {
            log.error("Error generating daily settlement for date: {}", yesterday, e);
        }
    }
}

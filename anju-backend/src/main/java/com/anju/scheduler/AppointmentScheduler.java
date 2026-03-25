package com.anju.scheduler;

import com.anju.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppointmentScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentScheduler.class);

    private final AppointmentService appointmentService;

    public AppointmentScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Scheduled(fixedRate = 60000)
    public void autoCancelStaleAppointments() {
        log.info("Running auto-cancel stale appointments task");
        try {
            appointmentService.autoCancelStaleAppointments();
        } catch (Exception e) {
            log.error("Error during auto-cancel stale appointments", e);
        }
    }
}

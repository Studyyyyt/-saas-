package com.example.springboot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppointmentReminderScheduler {

    private final AppointmentService appointmentService;

    public AppointmentReminderScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Shanghai")
    public void runNextDayReminderJob() {
        System.out.println("[WECHAT_REMINDER_SCHEDULE] runNextDayReminderJob triggered");
        appointmentService.sendNextDayAppointmentReminders();
    }
}

package com.example.springboot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class BusinessInsightScheduler {

    private final BusinessAnalysisTaskService businessAnalysisTaskService;
    private final BusinessAlertService businessAlertService;

    public BusinessInsightScheduler(BusinessAnalysisTaskService businessAnalysisTaskService,
                                    BusinessAlertService businessAlertService) {
        this.businessAnalysisTaskService = businessAnalysisTaskService;
        this.businessAlertService = businessAlertService;
    }

    @Scheduled(cron = "${openai.business-analysis.weekly-cron:0 20 0 * * MON}", zone = "Asia/Shanghai")
    public void runWeeklyReport() {
        businessAnalysisTaskService.submitWeeklyReport(LocalDate.now(ZoneId.of("Asia/Shanghai")).minusWeeks(1), "SCHEDULED");
    }

    @Scheduled(cron = "${openai.business-analysis.monthly-cron:0 30 0 1 * *}", zone = "Asia/Shanghai")
    public void runMonthlyReport() {
        businessAnalysisTaskService.submitMonthlyReport(LocalDate.now(ZoneId.of("Asia/Shanghai")).minusMonths(1), "SCHEDULED");
    }

    @Scheduled(cron = "${openai.business-analysis.alert-cron:0 40 0 * * *}", zone = "Asia/Shanghai")
    public void runAlertScan() {
        businessAlertService.runDailyAlertScan(LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1), "SCHEDULED");
    }
}

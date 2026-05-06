package com.example.springboot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class BusinessDailyAnalysisScheduler {

    private final BusinessAnalysisTaskService businessAnalysisTaskService;

    public BusinessDailyAnalysisScheduler(BusinessAnalysisTaskService businessAnalysisTaskService) {
        this.businessAnalysisTaskService = businessAnalysisTaskService;
    }

    @Scheduled(cron = "${openai.business-analysis.daily-cron:0 10 0 * * *}", zone = "Asia/Shanghai")
    public void runDailyBusinessAnalysis() {
        LocalDate targetDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1);
        System.out.println("[BUSINESS_ANALYSIS_SCHEDULE] trigger date=" + targetDate);
        businessAnalysisTaskService.submitDailyAnalysis(targetDate, "SCHEDULED");
    }
}

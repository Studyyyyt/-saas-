package com.example.springboot.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessAnalysisTaskServiceTest {

    @Test
    void submitDailyAnalysisShouldMarkFailedWhenExecutorRejectsTask() {
        BusinessDailyAnalysisService dailyAnalysisService = mock(BusinessDailyAnalysisService.class);
        BusinessPeriodReportService periodReportService = mock(BusinessPeriodReportService.class);
        Executor rejectingExecutor = command -> {
            throw new RejectedExecutionException("queue full");
        };

        BusinessAnalysisTaskService service = new BusinessAnalysisTaskService(
                dailyAnalysisService,
                periodReportService,
                rejectingExecutor
        );

        LocalDate targetDate = LocalDate.of(2026, 4, 25);
        Map<String, Object> pendingReport = new LinkedHashMap<>();
        pendingReport.put("analysis_status", "PENDING");
        pendingReport.put("trigger_type", "MANUAL");
        pendingReport.put("updated_at", nowString());

        Map<String, Object> failedReport = new LinkedHashMap<>();
        failedReport.put("analysis_status", "FAILED");
        failedReport.put("trigger_type", "MANUAL");
        failedReport.put("updated_at", nowString());

        when(dailyAnalysisService.resolveAnalysisDate(targetDate)).thenReturn(targetDate);
        when(dailyAnalysisService.prepareDailyAnalysisTask(targetDate, "MANUAL")).thenReturn(pendingReport);
        when(dailyAnalysisService.markDailyAnalysisFailed(eq(targetDate), eq("MANUAL"), contains("queue full"))).thenReturn(failedReport);

        Map<String, Object> result = service.submitDailyAnalysis(targetDate, "MANUAL");

        assertEquals("FAILED", result.get("task_status"));
        assertTrue((Boolean) result.get("done"));
        assertEquals("日报任务提交失败", result.get("message"));
        verify(dailyAnalysisService).markDailyAnalysisFailed(eq(targetDate), eq("MANUAL"), contains("queue full"));
    }

    @Test
    void getDailyAnalysisTaskStatusShouldFailStalePendingTask() {
        BusinessDailyAnalysisService dailyAnalysisService = mock(BusinessDailyAnalysisService.class);
        BusinessPeriodReportService periodReportService = mock(BusinessPeriodReportService.class);
        Executor directExecutor = Runnable::run;

        BusinessAnalysisTaskService service = new BusinessAnalysisTaskService(
                dailyAnalysisService,
                periodReportService,
                directExecutor
        );

        LocalDate targetDate = LocalDate.of(2026, 4, 25);
        Map<String, Object> pendingReport = new LinkedHashMap<>();
        pendingReport.put("analysis_status", "PENDING");
        pendingReport.put("trigger_type", "SCHEDULED");
        pendingReport.put("updated_at", LocalDateTime.now(ZoneId.of("Asia/Shanghai")).minusMinutes(1).toString().replace('T', ' '));

        Map<String, Object> failedReport = new LinkedHashMap<>();
        failedReport.put("analysis_status", "FAILED");
        failedReport.put("trigger_type", "SCHEDULED");
        failedReport.put("updated_at", nowString());

        when(dailyAnalysisService.resolveAnalysisDate(targetDate)).thenReturn(targetDate);
        when(dailyAnalysisService.getAnalysisByDate(targetDate)).thenReturn(pendingReport);
        when(dailyAnalysisService.markDailyAnalysisFailed(eq(targetDate), eq("SCHEDULED"), contains("后台任务已中断"))).thenReturn(failedReport);

        Map<String, Object> result = service.getDailyAnalysisTaskStatus(targetDate);

        assertEquals("FAILED", result.get("task_status"));
        assertTrue((Boolean) result.get("done"));
        verify(dailyAnalysisService).markDailyAnalysisFailed(eq(targetDate), eq("SCHEDULED"), contains("后台任务已中断"));
    }

    private String nowString() {
        return LocalDateTime.now(ZoneId.of("Asia/Shanghai")).toString().replace('T', ' ');
    }
}

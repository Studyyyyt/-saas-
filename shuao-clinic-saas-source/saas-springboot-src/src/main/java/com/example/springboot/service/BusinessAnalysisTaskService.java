package com.example.springboot.service;

import com.example.springboot.service.BusinessPeriodReportService.PeriodTarget;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Service
public class BusinessAnalysisTaskService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<String> FINAL_STATUSES = Set.of("SUCCESS", "FALLBACK", "FAILED");
    private static final long STALE_PENDING_GRACE_SECONDS = 15L;
    private static final String INTERRUPTED_TASK_MESSAGE = "后台任务已中断，可能因服务重启或线程池拒绝导致，请重新提交。";

    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessPeriodReportService businessPeriodReportService;
    private final Executor taskExecutor;
    private final ConcurrentMap<String, Boolean> runningTasks = new ConcurrentHashMap<>();

    public BusinessAnalysisTaskService(BusinessDailyAnalysisService businessDailyAnalysisService,
                                       BusinessPeriodReportService businessPeriodReportService,
                                       @Qualifier("businessAnalysisTaskExecutor") Executor taskExecutor) {
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.taskExecutor = taskExecutor;
    }

    public Map<String, Object> submitDailyAnalysis(LocalDate analysisDate, String triggerType) {
        LocalDate targetDate = businessDailyAnalysisService.resolveAnalysisDate(analysisDate);
        String targetKey = targetDate.toString();
        String taskKey = buildTaskKey("DAILY", targetKey);
        if (isTaskRunning(taskKey)) {
            return buildDailyTaskView(targetDate,
                    businessDailyAnalysisService.getAnalysisByDate(targetDate),
                    "日报任务已在处理中");
        }

        try {
            Map<String, Object> report = businessDailyAnalysisService.prepareDailyAnalysisTask(targetDate, triggerType);
            boolean started = startTask(
                    taskKey,
                    () -> businessDailyAnalysisService.runDailyAnalysis(targetDate, triggerType),
                    message -> businessDailyAnalysisService.markDailyAnalysisFailed(targetDate, triggerType, message)
            );
            if (!started) {
                report = businessDailyAnalysisService.getAnalysisByDate(targetDate);
            }
            return buildDailyTaskView(targetDate, report, started ? "日报生成任务已提交" : "日报任务已在处理中");
        } catch (RuntimeException exception) {
            Map<String, Object> failedReport = businessDailyAnalysisService.markDailyAnalysisFailed(
                    targetDate,
                    triggerType,
                    buildTaskSubmitFailureMessage(exception)
            );
            return buildDailyTaskView(targetDate, failedReport, "日报任务提交失败");
        }
    }

    public Map<String, Object> getDailyAnalysisTaskStatus(LocalDate analysisDate) {
        LocalDate targetDate = businessDailyAnalysisService.resolveAnalysisDate(analysisDate);
        Map<String, Object> report = businessDailyAnalysisService.getAnalysisByDate(targetDate);
        report = recoverDailyTaskIfStale(targetDate, report);
        return buildDailyTaskView(targetDate, report, null);
    }

    public Map<String, Object> submitWeeklyReport(LocalDate anchorDate, String triggerType) {
        PeriodTarget target = businessPeriodReportService.resolveWeeklyTarget(anchorDate);
        return submitPeriodTask(target, triggerType);
    }

    public Map<String, Object> getWeeklyReportTaskStatus(LocalDate anchorDate) {
        PeriodTarget target = businessPeriodReportService.resolveWeeklyTarget(anchorDate);
        Map<String, Object> report = businessPeriodReportService.getReportByTypeAndPeriodKey(target.reportType, target.periodKey);
        report = recoverPeriodTaskIfStale(target, report);
        return buildPeriodTaskView(target, report, null);
    }

    public Map<String, Object> submitMonthlyReport(LocalDate anchorDate, String triggerType) {
        PeriodTarget target = businessPeriodReportService.resolveMonthlyTarget(anchorDate);
        return submitPeriodTask(target, triggerType);
    }

    public Map<String, Object> getMonthlyReportTaskStatus(LocalDate anchorDate) {
        PeriodTarget target = businessPeriodReportService.resolveMonthlyTarget(anchorDate);
        Map<String, Object> report = businessPeriodReportService.getReportByTypeAndPeriodKey(target.reportType, target.periodKey);
        report = recoverPeriodTaskIfStale(target, report);
        return buildPeriodTaskView(target, report, null);
    }

    private Map<String, Object> submitPeriodTask(PeriodTarget target, String triggerType) {
        String taskKey = buildTaskKey(target.reportType, target.periodKey);
        if (isTaskRunning(taskKey)) {
            return buildPeriodTaskView(target,
                    businessPeriodReportService.getReportByTypeAndPeriodKey(target.reportType, target.periodKey),
                    target.reportTypeLabel + "任务已在处理中");
        }

        try {
            Map<String, Object> report = "WEEKLY".equals(target.reportType)
                    ? businessPeriodReportService.prepareWeeklyReportTask(target.anchorDate, triggerType)
                    : businessPeriodReportService.prepareMonthlyReportTask(target.anchorDate, triggerType);
            boolean started = startTask(
                    taskKey,
                    () -> {
                        if ("WEEKLY".equals(target.reportType)) {
                            businessPeriodReportService.runWeeklyReport(target.anchorDate, triggerType);
                        } else {
                            businessPeriodReportService.runMonthlyReport(target.anchorDate, triggerType);
                        }
                    },
                    message -> businessPeriodReportService.markReportTaskFailed(target, triggerType, message)
            );
            if (!started) {
                report = businessPeriodReportService.getReportByTypeAndPeriodKey(target.reportType, target.periodKey);
            }
            return buildPeriodTaskView(target, report, started ? target.reportTypeLabel + "生成任务已提交" : target.reportTypeLabel + "任务已在处理中");
        } catch (RuntimeException exception) {
            Map<String, Object> failedReport = businessPeriodReportService.markReportTaskFailed(
                    target,
                    triggerType,
                    buildTaskSubmitFailureMessage(exception)
            );
            return buildPeriodTaskView(target, failedReport, target.reportTypeLabel + "任务提交失败");
        }
    }

    private boolean startTask(String taskKey, Runnable task, Consumer<String> onFailure) {
        if (runningTasks.putIfAbsent(taskKey, Boolean.TRUE) != null) {
            return false;
        }
        try {
            taskExecutor.execute(() -> {
                try {
                    task.run();
                } catch (Throwable throwable) {
                    String errorMessage = throwable.getMessage() == null || throwable.getMessage().trim().isEmpty()
                            ? throwable.getClass().getSimpleName()
                            : throwable.getMessage().trim();
                    System.err.println("[BUSINESS_ANALYSIS_ASYNC_TASK_FAILED] taskKey=" + taskKey + ", message=" + errorMessage);
                    if (onFailure != null) {
                        onFailure.accept(errorMessage);
                    }
                } finally {
                    runningTasks.remove(taskKey);
                }
            });
            return true;
        } catch (RuntimeException exception) {
            runningTasks.remove(taskKey);
            throw exception;
        }
    }

    private Map<String, Object> buildDailyTaskView(LocalDate targetDate, Map<String, Object> report, String message) {
        String taskKey = buildTaskKey("DAILY", targetDate.toString());
        String status = extractStatus(report, "analysis_status", taskKey);
        boolean done = isFinalStatus(status);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("task_type", "DAILY");
        payload.put("task_key", taskKey);
        payload.put("target_date", targetDate.toString());
        payload.put("task_status", status);
        payload.put("running", isTaskRunning(taskKey));
        payload.put("done", done);
        payload.put("message", StringUtils.hasText(message) ? message : buildStatusMessage("日报", status));
        payload.put("report", report);
        return payload;
    }

    private Map<String, Object> buildPeriodTaskView(PeriodTarget target, Map<String, Object> report, String message) {
        String taskKey = buildTaskKey(target.reportType, target.periodKey);
        String status = extractStatus(report, "report_status", taskKey);
        boolean done = isFinalStatus(status);
        Object periodLabel = report == null ? target.periodStart + " 至 " + target.periodEnd : report.get("period_label");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("task_type", target.reportType);
        payload.put("task_key", taskKey);
        payload.put("target_date", target.anchorDate.toString());
        payload.put("period_key", target.periodKey);
        payload.put("period_label", periodLabel);
        payload.put("task_status", status);
        payload.put("running", isTaskRunning(taskKey));
        payload.put("done", done);
        payload.put("message", StringUtils.hasText(message) ? message : buildStatusMessage(target.reportTypeLabel, status));
        payload.put("report", report);
        return payload;
    }

    private String extractStatus(Map<String, Object> report, String statusField, String taskKey) {
        if (report != null) {
            Object value = report.get(statusField);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return isTaskRunning(taskKey) ? "PENDING" : "NOT_FOUND";
    }

    private boolean isTaskRunning(String taskKey) {
        return Boolean.TRUE.equals(runningTasks.get(taskKey));
    }

    private boolean isFinalStatus(String status) {
        return FINAL_STATUSES.contains(status) || "NOT_FOUND".equals(status);
    }

    private Map<String, Object> recoverDailyTaskIfStale(LocalDate targetDate, Map<String, Object> report) {
        String taskKey = buildTaskKey("DAILY", targetDate.toString());
        if (!shouldRecoverStalePending(report, "analysis_status", taskKey)) {
            return report;
        }
        return businessDailyAnalysisService.markDailyAnalysisFailed(
                targetDate,
                extractTriggerType(report),
                INTERRUPTED_TASK_MESSAGE
        );
    }

    private Map<String, Object> recoverPeriodTaskIfStale(PeriodTarget target, Map<String, Object> report) {
        String taskKey = buildTaskKey(target.reportType, target.periodKey);
        if (!shouldRecoverStalePending(report, "report_status", taskKey)) {
            return report;
        }
        return businessPeriodReportService.markReportTaskFailed(
                target,
                extractTriggerType(report),
                INTERRUPTED_TASK_MESSAGE
        );
    }

    private boolean shouldRecoverStalePending(Map<String, Object> report, String statusField, String taskKey) {
        if (report == null || isTaskRunning(taskKey)) {
            return false;
        }
        Object statusValue = report.get(statusField);
        if (!"PENDING".equals(String.valueOf(statusValue))) {
            return false;
        }
        LocalDateTime updatedAt = parseUpdatedAt(report.get("updated_at"));
        return updatedAt == null || updatedAt.plusSeconds(STALE_PENDING_GRACE_SECONDS).isBefore(LocalDateTime.now(DEFAULT_ZONE));
    }

    private LocalDateTime parseUpdatedAt(Object value) {
        if (value == null || !StringUtils.hasText(String.valueOf(value))) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).trim().replace(' ', 'T'));
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String extractTriggerType(Map<String, Object> report) {
        if (report == null) {
            return "MANUAL";
        }
        Object triggerType = report.get("trigger_type");
        return triggerType == null || !StringUtils.hasText(String.valueOf(triggerType))
                ? "MANUAL"
                : String.valueOf(triggerType);
    }

    private String buildTaskSubmitFailureMessage(RuntimeException exception) {
        String errorMessage = exception.getMessage() == null || exception.getMessage().trim().isEmpty()
                ? exception.getClass().getSimpleName()
                : exception.getMessage().trim();
        return "任务提交失败，后台执行器未接受任务：" + errorMessage;
    }

    private String buildStatusMessage(String label, String status) {
        if ("PENDING".equals(status)) {
            return label + "正在后台生成";
        }
        if ("SUCCESS".equals(status)) {
            return label + "已生成完成";
        }
        if ("FALLBACK".equals(status)) {
            return label + "已生成完成，当前为规则回退结果";
        }
        if ("FAILED".equals(status)) {
            return label + "生成失败";
        }
        return label + "任务不存在";
    }

    private String buildTaskKey(String taskType, String targetKey) {
        return taskType + ":" + targetKey;
    }
}

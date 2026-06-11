package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.BusinessAnalysisChatRequest;
import com.example.springboot.service.AiConfigService;
import com.example.springboot.service.BusinessAlertService;
import com.example.springboot.service.BusinessAnalysisChatService;
import com.example.springboot.service.BusinessDailyAnalysisService;
import com.example.springboot.service.BusinessAnalysisTaskService;
import com.example.springboot.service.BusinessPeriodReportService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/business-analysis")
public class BusinessDailyAnalysisController {

    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessPeriodReportService businessPeriodReportService;
    private final BusinessAlertService businessAlertService;
    private final BusinessAnalysisTaskService businessAnalysisTaskService;
    private final BusinessAnalysisChatService businessAnalysisChatService;
    private final AiConfigService aiConfigService;

    public BusinessDailyAnalysisController(BusinessDailyAnalysisService businessDailyAnalysisService,
                                           BusinessPeriodReportService businessPeriodReportService,
                                           BusinessAlertService businessAlertService,
                                           BusinessAnalysisTaskService businessAnalysisTaskService,
                                           BusinessAnalysisChatService businessAnalysisChatService,
                                           AiConfigService aiConfigService) {
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.businessAlertService = businessAlertService;
        this.businessAnalysisTaskService = businessAnalysisTaskService;
        this.businessAnalysisChatService = businessAnalysisChatService;
        this.aiConfigService = aiConfigService;
    }

    @GetMapping("/latest")
    public Result latest() {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessDailyAnalysisService.getLatestAnalysis());
    }

    @GetMapping("/history")
    public Result history(@RequestParam(defaultValue = "20") Integer limit) {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessDailyAnalysisService.getRecentAnalyses(limit));
    }

    @GetMapping("/probe")
    public Result probe() {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessDailyAnalysisService.testModelConnection());
    }

    @GetMapping("/chat/session")
    public Result chatSession(@RequestParam(required = false) Long accountId,
                              @RequestParam(required = false) String accountName) {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessAnalysisChatService.getOrCreateSession(accountId, accountName));
    }

    @GetMapping("/chat/memory")
    public Result chatMemory(@RequestParam(required = false) Long accountId) {
        // 记忆管理已移除，返回空文档兼容前端
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("has_memory", false);
        result.put("content", "");
        result.put("updated_at", "");
        return Result.success(result);
    }

    @PostMapping("/chat/message")
    public Result chatMessage(@RequestBody BusinessAnalysisChatRequest request) {
        aiConfigService.assertAiEnabled("business-analysis");
        try {
            return Result.success(businessAnalysisChatService.sendMessage(request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    /**
     * 前端实际走 /api/ai/proxy/business-analysis 统一代理，此端点保留用于兼容，建议废弃。
     */
    @Deprecated
    @PostMapping(value = "/chat/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatStream(
            @RequestBody BusinessAnalysisChatRequest request,
            @RequestParam(required = false, defaultValue = "default") String agentKey) {
        aiConfigService.assertAiEnabled("business-analysis");
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(120000L);
        new Thread(() -> businessAnalysisChatService.sendMessageStream(request, agentKey, emitter)).start();
        return emitter;
    }

    @GetMapping("/weekly/latest")
    public Result weeklyLatest() {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessPeriodReportService.getLatestWeeklyReport());
    }

    @PostMapping("/weekly/run")
    public Result runWeekly(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.submitWeeklyReport(targetDate, "MANUAL"));
    }

    @GetMapping("/weekly/run/status")
    public Result runWeeklyStatus(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.getWeeklyReportTaskStatus(targetDate));
    }

    @GetMapping("/monthly/latest")
    public Result monthlyLatest() {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessPeriodReportService.getLatestMonthlyReport());
    }

    @PostMapping("/monthly/run")
    public Result runMonthly(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.submitMonthlyReport(targetDate, "MANUAL"));
    }

    @GetMapping("/monthly/run/status")
    public Result runMonthlyStatus(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.getMonthlyReportTaskStatus(targetDate));
    }

    @GetMapping("/alerts/recent")
    public Result recentAlerts(@RequestParam(defaultValue = "20") Integer limit) {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessAlertService.getRecentAlerts(limit));
    }

    @PostMapping("/alerts/scan")
    public Result scanAlerts(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAlertService.runDailyAlertScan(targetDate, "MANUAL"));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        aiConfigService.assertAiEnabled("business-analysis");
        return Result.success(businessDailyAnalysisService.getAnalysisById(id));
    }

    @PostMapping("/run")
    public Result run(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.submitDailyAnalysis(targetDate, "MANUAL"));
    }

    @GetMapping("/run/status")
    public Result runStatus(@RequestParam(required = false) String date) {
        aiConfigService.assertAiEnabled("business-analysis");
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.getDailyAnalysisTaskStatus(targetDate));
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}

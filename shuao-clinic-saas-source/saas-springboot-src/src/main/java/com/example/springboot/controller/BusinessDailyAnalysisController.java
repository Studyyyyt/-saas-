package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.BusinessAnalysisChatRequest;
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

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/business-analysis")
public class BusinessDailyAnalysisController {

    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessPeriodReportService businessPeriodReportService;
    private final BusinessAlertService businessAlertService;
    private final BusinessAnalysisTaskService businessAnalysisTaskService;
    private final BusinessAnalysisChatService businessAnalysisChatService;

    public BusinessDailyAnalysisController(BusinessDailyAnalysisService businessDailyAnalysisService,
                                           BusinessPeriodReportService businessPeriodReportService,
                                           BusinessAlertService businessAlertService,
                                           BusinessAnalysisTaskService businessAnalysisTaskService,
                                           BusinessAnalysisChatService businessAnalysisChatService) {
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.businessAlertService = businessAlertService;
        this.businessAnalysisTaskService = businessAnalysisTaskService;
        this.businessAnalysisChatService = businessAnalysisChatService;
    }

    @GetMapping("/latest")
    public Result latest() {
        return Result.success(businessDailyAnalysisService.getLatestAnalysis());
    }

    @GetMapping("/history")
    public Result history(@RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(businessDailyAnalysisService.getRecentAnalyses(limit));
    }

    @GetMapping("/probe")
    public Result probe() {
        return Result.success(businessDailyAnalysisService.testModelConnection());
    }

    @GetMapping("/chat/session")
    public Result chatSession(@RequestParam(required = false) Long accountId,
                              @RequestParam(required = false) String accountName) {
        return Result.success(businessAnalysisChatService.getOrCreateSession(accountId, accountName));
    }

    @GetMapping("/chat/memory")
    public Result chatMemory(@RequestParam(required = false) Long accountId) {
        return Result.success(businessAnalysisChatService.getMemoryDocument(accountId));
    }

    @PostMapping("/chat/message")
    public Result chatMessage(@RequestBody BusinessAnalysisChatRequest request) {
        try {
            return Result.success(businessAnalysisChatService.sendMessage(request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @GetMapping("/weekly/latest")
    public Result weeklyLatest() {
        return Result.success(businessPeriodReportService.getLatestWeeklyReport());
    }

    @PostMapping("/weekly/run")
    public Result runWeekly(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.submitWeeklyReport(targetDate, "MANUAL"));
    }

    @GetMapping("/weekly/run/status")
    public Result runWeeklyStatus(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.getWeeklyReportTaskStatus(targetDate));
    }

    @GetMapping("/monthly/latest")
    public Result monthlyLatest() {
        return Result.success(businessPeriodReportService.getLatestMonthlyReport());
    }

    @PostMapping("/monthly/run")
    public Result runMonthly(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.submitMonthlyReport(targetDate, "MANUAL"));
    }

    @GetMapping("/monthly/run/status")
    public Result runMonthlyStatus(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.getMonthlyReportTaskStatus(targetDate));
    }

    @GetMapping("/alerts/recent")
    public Result recentAlerts(@RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(businessAlertService.getRecentAlerts(limit));
    }

    @PostMapping("/alerts/scan")
    public Result scanAlerts(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAlertService.runDailyAlertScan(targetDate, "MANUAL"));
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.success(businessDailyAnalysisService.getAnalysisById(id));
    }

    @PostMapping("/run")
    public Result run(@RequestParam(required = false) String date) {
        LocalDate targetDate = parseDate(date);
        if (date != null && targetDate == null) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd");
        }
        return Result.success(businessAnalysisTaskService.submitDailyAnalysis(targetDate, "MANUAL"));
    }

    @GetMapping("/run/status")
    public Result runStatus(@RequestParam(required = false) String date) {
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

package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.AiCallLog;
import com.example.springboot.service.AiCallLogService;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AI 调用日志控制器
 * 提供调用日志查询与统计看板数据
 */
@RestController
@RequestMapping("/api/ai/call-logs")
public class AiCallLogController {

    private final AiCallLogService aiCallLogService;

    public AiCallLogController(AiCallLogService aiCallLogService) {
        this.aiCallLogService = aiCallLogService;
    }

    @GetMapping
    public Result list(@RequestParam(required = false) Long accountId,
                       @RequestParam(required = false) String agentKey,
                       @RequestParam(required = false) String responseStatus,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        List<AiCallLog> list = aiCallLogService.list(accountId, agentKey, responseStatus, start, end);
        return Result.success(list);
    }

    @GetMapping("/agent-stats")
    public Result agentStats(@RequestParam(required = false) Long accountId,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        List<Map<String, Object>> stats = aiCallLogService.agentStats(accountId, start, end);
        return Result.success(stats);
    }

    @GetMapping("/daily-stats")
    public Result dailyStats(@RequestParam(required = false) Long accountId,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        List<Map<String, Object>> stats = aiCallLogService.dailyStats(accountId, start, end);
        return Result.success(stats);
    }

    @GetMapping("/summary")
    public Result summary(@RequestParam(required = false) Long accountId,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDate(endDate);
        Map<String, Object> summary = aiCallLogService.summary(accountId, start, end);
        return Result.success(summary);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}

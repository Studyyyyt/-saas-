package com.example.springboot.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AI 分析任务 Webhook 通知器
 * 供 BusinessAnalysisTaskService 在任务状态变更时调用，自动推送企业微信
 */
@Service
public class WebhookTaskNotifier {

    private final WechatWorkWebhookService webhookService;

    public WebhookTaskNotifier(WechatWorkWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * 通知日报任务状态变更
     *
     * @param targetDate 目标日期（yyyy-MM-dd）
     * @param status     任务状态：PENDING / SUCCESS / FALLBACK / FAILED
     * @param message    状态说明
     * @param durationMs 执行耗时（毫秒），可选
     */
    public void notifyDailyAnalysis(String targetDate, String status, String message, Long durationMs) {
        sendTaskNotification("AI 经营日报", targetDate, status, message, durationMs);
    }

    /**
     * 通知周报任务状态变更
     *
     * @param periodLabel 周期标签，如 "2026-05-05 至 2026-05-11"
     * @param status      任务状态
     * @param message     状态说明
     * @param durationMs  执行耗时（毫秒），可选
     */
    public void notifyWeeklyReport(String periodLabel, String status, String message, Long durationMs) {
        sendTaskNotification("AI 经营周报", periodLabel, status, message, durationMs);
    }

    /**
     * 通知月报任务状态变更
     *
     * @param periodLabel 周期标签，如 "2026-05"
     * @param status      任务状态
     * @param message     状态说明
     * @param durationMs  执行耗时（毫秒），可选
     */
    public void notifyMonthlyReport(String periodLabel, String status, String message, Long durationMs) {
        sendTaskNotification("AI 经营月报", periodLabel, status, message, durationMs);
    }

    /**
     * 通用任务状态通知
     *
     * @param taskType    任务类型名称
     * @param targetLabel 目标标识（日期或周期）
     * @param status      任务状态
     * @param message     状态说明
     * @param durationMs  执行耗时（毫秒），可选
     */
    public void notifyTask(String taskType, String targetLabel, String status, String message, Long durationMs) {
        sendTaskNotification(taskType, targetLabel, status, message, durationMs);
    }

    private void sendTaskNotification(String taskType, String targetLabel, String status, String message, Long durationMs) {
        String statusEmoji = resolveStatusEmoji(status);
        String statusText = resolveStatusText(status);

        StringBuilder markdown = new StringBuilder();
        markdown.append("## ").append(statusEmoji).append(" ").append(taskType).append(" 状态通知\n\n");
        if (StringUtils.hasText(targetLabel)) {
            markdown.append("**目标周期：** ").append(targetLabel).append("\n\n");
        }
        markdown.append("**执行状态：** ").append(statusText).append("\n\n");
        if (StringUtils.hasText(message)) {
            markdown.append("**状态说明：** ").append(message).append("\n\n");
        }
        if (durationMs != null && durationMs > 0) {
            markdown.append("**执行耗时：** ").append(durationMs).append(" ms\n\n");
        }
        markdown.append("---\n");
        markdown.append("*发送时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("*");

        webhookService.sendMarkdown(markdown.toString());
    }

    private String resolveStatusEmoji(String status) {
        return switch (String.valueOf(status).toUpperCase()) {
            case "SUCCESS" -> "✅";
            case "FAILED" -> "❌";
            case "PENDING" -> "⏳";
            case "FALLBACK" -> "⚠️";
            default -> "📌";
        };
    }

    private String resolveStatusText(String status) {
        return switch (String.valueOf(status).toUpperCase()) {
            case "SUCCESS" -> "成功";
            case "FAILED" -> "失败";
            case "PENDING" -> "进行中";
            case "FALLBACK" -> "已回退（规则分析）";
            default -> status;
        };
    }
}

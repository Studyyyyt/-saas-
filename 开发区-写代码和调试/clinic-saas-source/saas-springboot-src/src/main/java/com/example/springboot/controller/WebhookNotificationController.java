package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.WechatWorkWebhookService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Webhook 通知控制器
 * 提供通用通知与 AI 任务状态通知接口
 */
@RestController
@RequestMapping("/api/webhook")
public class WebhookNotificationController {

    private final WechatWorkWebhookService webhookService;

    public WebhookNotificationController(WechatWorkWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * 通用通知接口
     *
     * @param request 请求体，包含 type（text/markdown/news）和 content
     * @return 发送结果
     */
    @PostMapping("/notify")
    public Result notify(@RequestBody Map<String, Object> request) {
        String type = request.get("type") == null ? "" : String.valueOf(request.get("type"));
        Object contentObj = request.get("content");
        if (!StringUtils.hasText(type) || contentObj == null) {
            return Result.error("参数错误：type 和 content 不能为空");
        }

        boolean success;
        switch (type.toLowerCase()) {
            case "text":
                success = webhookService.sendText(String.valueOf(contentObj), null);
                break;
            case "markdown":
                success = webhookService.sendMarkdown(String.valueOf(contentObj));
                break;
            case "news":
                if (!(contentObj instanceof Map)) {
                    return Result.error("news 类型时 content 需为对象，包含 title/description/url/picUrl");
                }
                Map<?, ?> news = (Map<?, ?>) contentObj;
                success = webhookService.sendNews(
                        toStr(news.get("title")),
                        toStr(news.get("description")),
                        toStr(news.get("url")),
                        toStr(news.get("picUrl"))
                );
                break;
            default:
                return Result.error("不支持的 type 类型，可选：text/markdown/news");
        }

        return success ? Result.success("发送成功") : Result.error("发送失败，请检查日志");
    }

    /**
     * AI 任务状态通知接口
     * 自动将任务状态格式化为 Markdown 发送到企业微信
     *
     * @param request 请求体，包含 taskType, status, message, durationMs
     * @return 发送结果
     */
    @PostMapping("/ai-task")
    public Result aiTask(@RequestBody Map<String, Object> request) {
        String taskType = toStr(request.get("taskType"));
        String status = toStr(request.get("status"));
        String message = toStr(request.get("message"));
        Long durationMs = parseLong(request.get("durationMs"));

        if (!StringUtils.hasText(taskType) || !StringUtils.hasText(status)) {
            return Result.error("参数错误：taskType 和 status 不能为空");
        }

        String statusEmoji = switch (status.toUpperCase()) {
            case "SUCCESS" -> "✅";
            case "FAILED" -> "❌";
            case "PENDING" -> "⏳";
            case "FALLBACK" -> "⚠️";
            default -> "📌";
        };

        StringBuilder markdown = new StringBuilder();
        markdown.append("## ").append(statusEmoji).append(" AI 任务状态通知\n\n");
        markdown.append("**任务类型：** ").append(taskType).append("\n\n");
        markdown.append("**执行状态：** ").append(status).append("\n\n");
        if (StringUtils.hasText(message)) {
            markdown.append("**状态说明：** ").append(message).append("\n\n");
        }
        if (durationMs != null && durationMs > 0) {
            markdown.append("**执行耗时：** ").append(durationMs).append(" ms\n\n");
        }
        markdown.append("---\n");
        markdown.append("*发送时间：").append(java.time.LocalDateTime.now()).append("*");

        boolean success = webhookService.sendMarkdown(markdown.toString());
        return success ? Result.success("发送成功") : Result.error("发送失败，请检查日志");
    }

    private String toStr(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.example.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 企业微信机器人 Webhook 服务
 * 支持发送 text / markdown / news / template_card 消息类型
 */
@Service
public class WechatWorkWebhookService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RETRIES = 3;

    private final String webhookUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WechatWorkWebhookService(
            @Value("${wechat.webhook-url:}") String webhookUrl,
            ObjectMapper objectMapper) {
        this.webhookUrl = webhookUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    /**
     * 发送文本消息
     *
     * @param content  消息内容
     * @param mentionedList @提醒的成员列表，null 则不提醒
     * @return 是否发送成功
     */
    public boolean sendText(String content, List<String> mentionedList) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("msgtype", "text");
        ObjectNode text = body.putObject("text");
        text.put("content", content);
        if (mentionedList != null && !mentionedList.isEmpty()) {
            ArrayNode array = text.putArray("mentioned_list");
            mentionedList.forEach(array::add);
        }
        return sendWithRetry(body);
    }

    /**
     * 发送 Markdown 消息
     *
     * @param content Markdown 内容
     * @return 是否发送成功
     */
    public boolean sendMarkdown(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("msgtype", "markdown");
        ObjectNode markdown = body.putObject("markdown");
        markdown.put("content", content);
        return sendWithRetry(body);
    }

    /**
     * 发送图文消息（news）
     *
     * @param title   标题
     * @param description 描述
     * @param url     点击后跳转的链接
     * @param picUrl  图片链接
     * @return 是否发送成功
     */
    public boolean sendNews(String title, String description, String url, String picUrl) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(url)) {
            return false;
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("msgtype", "news");
        ObjectNode news = body.putObject("news");
        ArrayNode articles = news.putArray("articles");
        ObjectNode article = articles.addObject();
        article.put("title", title);
        article.put("description", description == null ? "" : description);
        article.put("url", url);
        article.put("picurl", picUrl == null ? "" : picUrl);
        return sendWithRetry(body);
    }

    /**
     * 发送模板卡片消息（template_card）
     *
     * @param sourceDesc  来源描述
     * @param mainTitle   主标题
     * @param subTitle    副标题
     * @param jumpUrl     跳转链接
     * @param emphasisContent 高亮内容 Map，key 为 title/value
     * @return 是否发送成功
     */
    public boolean sendTemplateCard(String sourceDesc, String mainTitle, String subTitle,
                                    String jumpUrl, Map<String, String> emphasisContent) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("msgtype", "template_card");
        ObjectNode card = body.putObject("template_card");
        card.put("card_type", "text_notice");

        ObjectNode source = card.putObject("source");
        source.put("desc", StringUtils.hasText(sourceDesc) ? sourceDesc : "企业微信");

        ObjectNode main = card.putObject("main_title");
        main.put("title", StringUtils.hasText(mainTitle) ? mainTitle : "通知");
        main.put("desc", subTitle == null ? "" : subTitle);

        if (emphasisContent != null && !emphasisContent.isEmpty()) {
            ObjectNode emphasis = card.putObject("emphasis_content");
            Map.Entry<String, String> first = emphasisContent.entrySet().iterator().next();
            emphasis.put("title", first.getKey());
            emphasis.put("value", first.getValue());
        }

        if (StringUtils.hasText(jumpUrl)) {
            ArrayNode jumpList = card.putArray("jump_list");
            ObjectNode jump = jumpList.addObject();
            jump.put("type", 1);
            jump.put("url", jumpUrl);
            jump.put("title", "查看详情");
        }

        return sendWithRetry(body);
    }

    /**
     * 通用发送方法，支持重试
     */
    private boolean sendWithRetry(ObjectNode requestBody) {
        if (!StringUtils.hasText(webhookUrl)) {
            System.err.println("[WEBHOOK] 未配置 webhook-url，跳过发送");
            return false;
        }
        String body;
        try {
            body = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            System.err.println("[WEBHOOK] 序列化请求体失败: " + e.getMessage());
            return false;
        }

        IOException lastIOException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .timeout(REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    JsonNode json = objectMapper.readTree(response.body());
                    int errcode = json.has("errcode") ? json.get("errcode").asInt(-1) : -1;
                    if (errcode == 0) {
                        return true;
                    }
                    String errmsg = json.has("errmsg") ? json.get("errmsg").asText() : "unknown";
                    System.err.println("[WEBHOOK] 企业微信返回错误: errcode=" + errcode + ", errmsg=" + errmsg);
                    // 业务错误不重试
                    return false;
                }
                if (!isRetryableStatus(response.statusCode()) || attempt == MAX_RETRIES) {
                    System.err.println("[WEBHOOK] HTTP 错误: " + response.statusCode() + " " + response.body());
                    return false;
                }
                sleepBeforeRetry(attempt);
            } catch (IOException e) {
                lastIOException = e;
                if (attempt == MAX_RETRIES) {
                    System.err.println("[WEBHOOK] 请求失败: " + e.getMessage());
                    return false;
                }
                try {
                    sleepBeforeRetry(attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("[WEBHOOK] 重试等待被中断: " + ie.getMessage());
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[WEBHOOK] 请求被中断: " + e.getMessage());
                return false;
            }
        }
        if (lastIOException != null) {
            System.err.println("[WEBHOOK] 请求失败: " + lastIOException.getMessage());
        }
        return false;
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        long delayMillis = Math.min(4000L, 500L * attempt);
        Thread.sleep(delayMillis);
    }
}

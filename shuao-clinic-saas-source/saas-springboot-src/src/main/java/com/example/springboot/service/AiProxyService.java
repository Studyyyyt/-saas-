package com.example.springboot.service;

import com.example.springboot.entity.AiAgentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * AI 统一代理服务
 * 负责将前端请求转发到配置的外部工作流端点
 */
@Service
public class AiProxyService {

    private final AiAgentConfigService aiAgentConfigService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiProxyService(AiAgentConfigService aiAgentConfigService,
                          ObjectMapper objectMapper) {
        this.aiAgentConfigService = aiAgentConfigService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * 统一转发方法
     *
     * @param agentKey 代理标识，如 medical-expand、default、finance
     * @param payload  请求体数据，包含变量替换所需字段
     * @return 外部端点返回的 JSON 字符串
     */
    public String forward(String agentKey, Map<String, Object> payload) {
        AiAgentConfig config = resolveConfig(agentKey, payload);
        String requestBody = buildRequestBody(config, payload);
        HttpRequest request = buildHttpRequest(config, requestBody, false);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            throw new RuntimeException("AI 代理调用失败：HTTP " + response.statusCode() + " " + response.body());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("AI 代理调用异常：" + e.getMessage(), e);
        }
    }

    /**
     * SSE 流式转发方法
     *
     * @param agentKey 代理标识
     * @param payload  请求体数据
     * @param emitter  SSE 推送器
     */
    public void forwardStream(String agentKey, Map<String, Object> payload, SseEmitter emitter) {
        AiAgentConfig config = resolveConfig(agentKey, payload);
        String requestBody = buildRequestBody(config, payload);
        HttpRequest request = buildHttpRequest(config, requestBody, true);

        int timeoutSeconds = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 60;

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                emitError(emitter, "AI 代理调用失败：HTTP " + response.statusCode() + " " + body);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data.trim())) {
                            emitDone(emitter);
                            return;
                        }
                        emitData(emitter, data);
                    } else if (!line.trim().isEmpty()) {
                        // 非 SSE 格式行，直接透传
                        emitData(emitter, line);
                    }
                }
            }
            emitDone(emitter);
        } catch (Exception e) {
            emitError(emitter, "AI 代理流式调用异常：" + e.getMessage());
        }
    }

    /**
     * 解析 Agent 配置，优先按 accountId 查找，找不到用系统默认
     */
    private AiAgentConfig resolveConfig(String agentKey, Map<String, Object> payload) {
        Long accountId = extractAccountId(payload);
        AiAgentConfig config = aiAgentConfigService.getByAccountAndKey(accountId, agentKey);
        if (config == null) {
            throw new IllegalArgumentException("未找到 agent 配置：" + agentKey);
        }
        if (!StringUtils.hasText(config.getEndpointUrl())) {
            throw new IllegalStateException("agent 配置缺少 endpoint_url：" + agentKey);
        }
        return config;
    }

    private Long extractAccountId(Map<String, Object> payload) {
        Object accountId = payload != null ? payload.get("account_id") : null;
        if (accountId instanceof Number) {
            return ((Number) accountId).longValue();
        }
        if (accountId instanceof String) {
            try {
                return Long.valueOf((String) accountId);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    /**
     * 使用 request_template 构建请求体，进行变量替换
     */
    private String buildRequestBody(AiAgentConfig config, Map<String, Object> payload) {
        String template = config.getRequestTemplate();
        if (!StringUtils.hasText(template)) {
            // 没有模板时，直接序列化 payload 为 JSON
            try {
                return objectMapper.writeValueAsString(payload);
            } catch (Exception e) {
                throw new RuntimeException("序列化请求体失败", e);
            }
        }

        String body = template;
        if (payload != null) {
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
                body = body.replace(placeholder, value);
            }
        }
        // 清理未替换的占位符
        body = body.replaceAll("\\\\{\\{[^}]+\\}\\}", "");
        return body;
    }

    private HttpRequest buildHttpRequest(AiAgentConfig config, String body, boolean stream) {
        String url = config.getEndpointUrl();
        int timeoutSeconds = config.getTimeoutSeconds() != null ? config.getTimeoutSeconds() : 60;

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        // 认证头处理
        String authType = config.getAuthType();
        String authToken = config.getAuthToken();
        if (StringUtils.hasText(authType) && StringUtils.hasText(authToken)) {
            if ("bearer".equalsIgnoreCase(authType)) {
                builder.header("Authorization", "Bearer " + authToken);
            } else if ("basic".equalsIgnoreCase(authType)) {
                builder.header("Authorization", "Basic " + authToken);
            } else if ("api_key".equalsIgnoreCase(authType)) {
                builder.header("X-API-Key", authToken);
            } else {
                // 自定义 header 名，authType 作为 header 名
                builder.header(authType, authToken);
            }
        }

        if (stream) {
            builder.header("Accept", "text/event-stream");
        }

        return builder.build();
    }

    private void emitData(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().name("data").data(data));
        } catch (Exception e) {
            // 客户端断开时忽略
        }
    }

    private void emitDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private void emitError(SseEmitter emitter, String error) {
        try {
            emitter.send(SseEmitter.event().name("error").data(error));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}

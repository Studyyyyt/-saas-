package com.example.springboot.service;

import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.entity.BusinessAnalysisChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 经营分析对话服务
 * 改造后：不再直接调用 AI 模型，所有 AI 逻辑外包到外部工作流平台
 */
@Service
public class BusinessAnalysisChatService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_SESSION_MESSAGES = 24;

    private final AiAgentConfigService aiAgentConfigService;
    private final AiProxyService aiProxyService;
    private final AiConfigService aiConfigService;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, ChatSessionState> sessions = new ConcurrentHashMap<>();

    public BusinessAnalysisChatService(AiAgentConfigService aiAgentConfigService,
                                       AiProxyService aiProxyService,
                                       AiConfigService aiConfigService,
                                       ObjectMapper objectMapper) {
        this.aiAgentConfigService = aiAgentConfigService;
        this.aiProxyService = aiProxyService;
        this.aiConfigService = aiConfigService;
        this.objectMapper = objectMapper;
    }

    public synchronized Map<String, Object> getOrCreateSession(Long accountId, String accountName) {
        String userKey = buildUserKey(accountId);
        boolean restarted = flushExpiredSession(userKey);
        ChatSessionState state = sessions.get(userKey);
        if (state == null) {
            state = new ChatSessionState(UUID.randomUUID().toString(), accountId, normalizeAccountName(accountName));
            state.touch();
            sessions.put(userKey, state);
            state.messages.add(ChatMessage.assistant(
                    "我是 AI 财务分析助手。你可以直接问我门店收入、支出结构、耗材、加工账单、非耗材费用、日报/周报/月报的理解和行动建议。"
            ));
        }
        return buildSessionView(userKey, state, restarted);
    }

    public synchronized Map<String, Object> sendMessage(BusinessAnalysisChatRequest request) {
        String functionKey = StringUtils.hasText(request.getFunctionKey()) ? request.getFunctionKey() : "business-analysis";
        aiConfigService.assertAiEnabled(functionKey);
        boolean debug = Boolean.TRUE.equals(aiConfigService.getDebugMode());
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String userKey = buildUserKey(request.getAccount_id());
        boolean restarted = flushExpiredSession(userKey);
        ChatSessionState state = sessions.get(userKey);
        if (state == null) {
            state = new ChatSessionState(UUID.randomUUID().toString(), request.getAccount_id(), normalizeAccountName(request.getAccount_name()));
            sessions.put(userKey, state);
        }
        if (StringUtils.hasText(request.getSession_id()) && !request.getSession_id().trim().equals(state.sessionId)) {
            restarted = true;
        }

        String message = request.getMessage().trim();
        state.accountId = request.getAccount_id();
        state.accountName = normalizeAccountName(request.getAccount_name());
        state.messages.add(ChatMessage.user(message));
        trimSessionMessages(state);
        state.touch();

        if (debug) {
            System.out.println("[AI Debug] " + functionKey + " 用户消息: " + message);
            System.out.println("[AI Debug] " + functionKey + " 历史消息数: " + state.messages.size());
        }

        // 组装 payload 调用代理服务
        String agentKey = StringUtils.hasText(request.getFunctionKey()) ? request.getFunctionKey() : "default";
        Map<String, Object> payload = buildPayload(request, state, message);

        String assistantText;
        try {
            String response = aiProxyService.forward(agentKey, payload);
            // 尝试解析 JSON 中的 content 字段，否则直接透传
            assistantText = extractContentFromResponse(response);
        } catch (Exception e) {
            if (debug) {
                System.err.println("[AI Debug] " + functionKey + " 代理调用异常: " + e.getMessage());
            }
            assistantText = "AI 通道暂时不可用，请稍后重试。原因：" + simplifyException(e);
        }

        if (debug) {
            System.out.println("[AI Debug] " + functionKey + " 助手回复: " + assistantText);
        }

        state.messages.add(ChatMessage.assistant(assistantText));
        trimSessionMessages(state);
        state.touch();
        return buildSessionView(userKey, state, restarted);
    }

    /**
     * SSE 流式对话：改造后直接透传外部工作流 SSE 响应
     */
    public void sendMessageStream(BusinessAnalysisChatRequest request, String agentKey,
                                   org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        String functionKey = StringUtils.hasText(request.getFunctionKey()) ? request.getFunctionKey() : "business-analysis";
        try {
            aiConfigService.assertAiEnabled(functionKey);
        } catch (IllegalStateException e) {
            emitError(emitter, e.getMessage());
            return;
        }
        boolean debug = Boolean.TRUE.equals(aiConfigService.getDebugMode());
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            emitError(emitter, "消息内容不能为空");
            return;
        }
        String userKey = buildUserKey(request.getAccount_id());
        flushExpiredSession(userKey);
        ChatSessionState state = sessions.get(userKey);
        if (state == null) {
            state = new ChatSessionState(UUID.randomUUID().toString(), request.getAccount_id(), normalizeAccountName(request.getAccount_name()));
            sessions.put(userKey, state);
        }
        String message = request.getMessage().trim();
        state.accountId = request.getAccount_id();
        state.accountName = normalizeAccountName(request.getAccount_name());
        state.messages.add(ChatMessage.user(message));
        trimSessionMessages(state);
        state.touch();
        if (debug) {
            System.out.println("[AI Debug] " + functionKey + " stream 用户消息: " + message);
            System.out.println("[AI Debug] " + functionKey + " stream 历史消息数: " + state.messages.size());
        }

        // 组装 payload 调用代理服务流式转发
        String actualAgentKey = StringUtils.hasText(agentKey) ? agentKey : "default";
        Map<String, Object> payload = buildPayload(request, state, message);

        try {
            aiProxyService.forwardStream(actualAgentKey, payload, emitter);
            // 流结束后将完整回复加入会话历史（由前端在 done 时通过另一个接口保存，或此处不做保存）
            // 简化处理：流式模式下不保存 assistant 消息到内存，由前端在需要时重新拉取 session
        } catch (Exception e) {
            if (debug) {
                System.err.println("[AI Debug] " + functionKey + " stream 代理调用异常: " + e.getMessage());
            }
            emitError(emitter, "AI 通道暂时不可用，请稍后重试。原因：" + simplifyException(e));
        }
    }

    /**
     * 构建转发到外部工作流的 payload
     */
    private Map<String, Object> buildPayload(BusinessAnalysisChatRequest request, ChatSessionState state, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user_message", message);
        payload.put("account_id", request.getAccount_id());
        payload.put("account_name", normalizeAccountName(request.getAccount_name()));
        payload.put("session_id", state.sessionId);

        // 组装历史记录
        List<Map<String, String>> history = new ArrayList<>();
        for (ChatMessage item : state.messages) {
            // 最后一条是刚添加的 user 消息，也包含在内
            Map<String, String> h = new LinkedHashMap<>();
            h.put("role", item.role);
            h.put("content", item.content);
            history.add(h);
        }
        payload.put("history", history);

        return payload;
    }

    /**
     * 从外部工作流响应中提取文本内容
     * 优先取 JSON 中的 content/text/message 字段，否则直接返回原始字符串
     */
    private String extractContentFromResponse(String response) {
        if (!StringUtils.hasText(response)) {
            return "";
        }
        String trimmed = response.trim();
        if (!trimmed.startsWith("{")) {
            return trimmed;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(trimmed, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            // 尝试常见字段
            for (String key : Arrays.asList("content", "text", "message", "answer", "result", "data")) {
                Object value = map.get(key);
                if (value instanceof String) {
                    return (String) value;
                }
                if (value != null) {
                    return objectMapper.writeValueAsString(value);
                }
            }
            // 如果没有找到特定字段，返回整个 JSON 字符串
            return trimmed;
        } catch (Exception e) {
            return trimmed;
        }
    }

    private void emitError(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter, String error) {
        try {
            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                    .name("error")
                    .data(error));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private String simplifyException(Exception exception) {
        String message = exception == null ? "" : exception.getMessage();
        if (!StringUtils.hasText(message)) {
            return exception == null ? "未知错误" : exception.getClass().getSimpleName();
        }
        String normalized = message.trim();
        if (normalized.length() > 400) {
            return normalized.substring(0, 400);
        }
        return normalized;
    }

    private Map<String, Object> buildSessionView(String userKey, ChatSessionState state, boolean restarted) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("session_id", state.sessionId);
        result.put("restarted", restarted);
        result.put("started_at", formatDateTime(state.startedAt));
        result.put("last_activity_at", formatDateTime(state.lastActivityAt));
        result.put("timeout_minutes", SESSION_TIMEOUT.toMinutes());
        result.put("messages", state.messages.stream()
                .sorted(Comparator.comparing(item -> item.createdAt))
                .map(ChatMessage::toView)
                .toList());
        return result;
    }

    private void trimSessionMessages(ChatSessionState state) {
        int overflow = state.messages.size() - MAX_SESSION_MESSAGES;
        if (overflow > 0) {
            state.messages.subList(0, overflow).clear();
        }
    }

    private boolean flushExpiredSession(String userKey) {
        ChatSessionState state = sessions.get(userKey);
        if (state == null || !state.isExpired()) {
            return false;
        }
        sessions.remove(userKey);
        return true;
    }

    private String buildUserKey(Long accountId) {
        return accountId != null && accountId > 0 ? String.valueOf(accountId) : "global";
    }

    private String normalizeAccountName(String accountName) {
        return StringUtils.hasText(accountName) ? accountName.trim() : "未命名用户";
    }

    private String formatDateTime(Instant instant) {
        return instant == null ? "" : DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(instant, DEFAULT_ZONE));
    }

    private static class ChatSessionState {
        private final String sessionId;
        private Long accountId;
        private String accountName;
        private final Instant startedAt;
        private Instant lastActivityAt;
        private final List<ChatMessage> messages = new ArrayList<>();

        private ChatSessionState(String sessionId, Long accountId, String accountName) {
            this.sessionId = sessionId;
            this.accountId = accountId;
            this.accountName = accountName;
            this.startedAt = Instant.now();
            this.lastActivityAt = this.startedAt;
        }

        private void touch() {
            this.lastActivityAt = Instant.now();
        }

        private boolean isExpired() {
            return lastActivityAt.plus(SESSION_TIMEOUT).isBefore(Instant.now());
        }
    }

    private static class ChatMessage {
        private final String role;
        private final String content;
        private final Instant createdAt;

        private ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.createdAt = Instant.now();
        }

        static ChatMessage user(String content) {
            return new ChatMessage("user", content);
        }

        static ChatMessage assistant(String content) {
            return new ChatMessage("assistant", content);
        }

        Map<String, Object> toView() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("role", role);
            result.put("content", content);
            result.put("created_at", DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(createdAt, DEFAULT_ZONE)));
            return result;
        }
    }
}

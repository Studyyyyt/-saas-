package com.example.springboot.service;

import com.example.springboot.config.OpenAiAnalysisProperties;
import com.example.springboot.entity.AiAgentConfig;
import com.example.springboot.entity.BusinessAnalysisChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BusinessAnalysisChatService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Duration OPENAI_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration OPENAI_REQUEST_TIMEOUT = Duration.ofSeconds(45);
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);
    private static final int OPENAI_MAX_RETRIES = 3;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_MEMORY_PROMPT_CHARS = 12000;
    private static final int MAX_MEMORY_PREVIEW_CHARS = 1500;
    private static final int MAX_SESSION_MESSAGES = 24;
    private static final String MEMORY_DIR = System.getProperty("user.home") + "/.local/state/business-analysis-chat";

    private final OpenAiAnalysisProperties openAiProperties;
    private final BusinessDailyAnalysisService businessDailyAnalysisService;
    private final BusinessPeriodReportService businessPeriodReportService;
    private final FinanceService financeService;
    private final AiAgentConfigService aiAgentConfigService;
    private final AiToolService aiToolService;
    private final AiModelProviderService modelProviderService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ConcurrentMap<String, ChatSessionState> sessions = new ConcurrentHashMap<>();

    public BusinessAnalysisChatService(OpenAiAnalysisProperties openAiProperties,
                                       BusinessDailyAnalysisService businessDailyAnalysisService,
                                       BusinessPeriodReportService businessPeriodReportService,
                                       FinanceService financeService,
                                       AiAgentConfigService aiAgentConfigService,
                                       AiToolService aiToolService,
                                       AiModelProviderService modelProviderService,
                                       ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.financeService = financeService;
        this.aiAgentConfigService = aiAgentConfigService;
        this.aiToolService = aiToolService;
        this.modelProviderService = modelProviderService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(OPENAI_CONNECT_TIMEOUT)
                .build();
    }

    public synchronized Map<String, Object> getOrCreateSession(Long accountId, String accountName) {
        String userKey = buildUserKey(accountId);
        boolean restarted = flushExpiredSession(userKey);
        ChatSessionState state = sessions.get(userKey);
        if (state == null) {
            state = new ChatSessionState(UUID.randomUUID().toString(), accountId, normalizeAccountName(accountName));
            state.touch();
            sessions.put(userKey, state);
            if (!StringUtils.hasText(readMemoryText(userKey))) {
                state.messages.add(ChatMessage.assistant(
                        "我是 AI 财务分析助手。你可以直接问我门店收入、支出结构、耗材、加工账单、非耗材费用、日报/周报/月报的理解和行动建议。"
                ));
            } else {
                state.messages.add(ChatMessage.assistant(
                        "长期记忆已加载。本次会话会在 30 分钟无交流后自动总结进长期记忆，并在下次新会话启动时继续参考。"
                ));
            }
        }
        return buildSessionView(userKey, state, restarted || hasLongTermMemory(userKey) && state.messages.size() == 1);
    }

    public synchronized Map<String, Object> sendMessage(BusinessAnalysisChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String userKey = buildUserKey(request.getAccount_id());
        boolean restarted = flushExpiredSession(userKey);
        ChatSessionState state = sessions.get(userKey);
        if (state == null) {
            state = new ChatSessionState(UUID.randomUUID().toString(), request.getAccount_id(), normalizeAccountName(request.getAccount_name()));
            sessions.put(userKey, state);
            restarted = hasLongTermMemory(userKey);
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

        String memoryText = readMemoryText(userKey);
        String context = buildGroundingContext();
        String assistantText;
        try {
            assistantText = isOpenAiReady()
                    ? requestChatCompletion(state, memoryText, context)
                    : buildFallbackReply(message, "AI 对话未启用，已使用本地经营摘要回复。");
        } catch (IOException | InterruptedException exception) {
            assistantText = buildFallbackReply(message, "AI 通道暂时不可用，已使用本地经营摘要回复。原因：" + simplifyException(exception));
        }

        state.messages.add(ChatMessage.assistant(assistantText));
        trimSessionMessages(state);
        state.touch();
        return buildSessionView(userKey, state, restarted);
    }

    /**
     * SSE 流式对话：支持 Function Calling + 流式输出
     */
    public void sendMessageStream(BusinessAnalysisChatRequest request, String agentKey,
                                   org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
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

        if (!isOpenAiReady()) {
            String fallback = buildFallbackReply(message, "AI 对话未启用，已使用本地经营摘要回复。");
            emitToken(emitter, fallback);
            state.messages.add(ChatMessage.assistant(fallback));
            emitDone(emitter);
            return;
        }

        // 加载 Agent 配置
        AiAgentConfig agent = aiAgentConfigService.getByAccountAndKey(request.getAccount_id(), agentKey);
        String systemPrompt = buildAgentSystemPrompt(agent);
        List<String> enabledTools = agent != null ? agent.getEnabledTools() : null;

        try {
            String memoryText = readMemoryText(userKey);
            StringBuilder assistantContent = new StringBuilder();

            // 第一轮：可能触发 Function Calling
            ObjectNode requestBody = buildStreamRequestBody(state, memoryText, systemPrompt, enabledTools, false);
            StringBuilder fullOutput = new StringBuilder();
            List<FunctionCallInfo> functionCalls = new ArrayList<>();

            // 使用同步 SSE 请求获取完整响应（包括可能的 function_call）
            String firstResponse = sendChatRequestSync(requestBody);
            parseStreamResponse(firstResponse, fullOutput, functionCalls);

            // 如果触发了 Function Calling
            if (!functionCalls.isEmpty()) {
                // 先 emit 一个提示
                emitToken(emitter, "\n\n");

                // 执行工具调用
                for (FunctionCallInfo fc : functionCalls) {
                    String toolResult = aiToolService.executeTool(fc.name, fc.arguments);
                    // 将工具结果加入对话上下文
                    state.messages.add(ChatMessage.tool(toolResult));
                }

                // 第二轮：获取最终回复（流式输出给用户）
                ObjectNode finalRequestBody = buildStreamRequestBody(state, memoryText, systemPrompt, enabledTools, true);
                streamChatRequest(finalRequestBody, emitter, assistantContent);
            } else {
                // 没有 Function Calling，直接输出第一轮的结果
                String text = fullOutput.toString().trim();
                if (!text.isEmpty()) {
                    emitToken(emitter, text);
                    assistantContent.append(text);
                }
            }

            if (assistantContent.length() > 0) {
                state.messages.add(ChatMessage.assistant(assistantContent.toString()));
            }
            trimSessionMessages(state);
            state.touch();
            emitDone(emitter);
        } catch (Exception e) {
            System.err.println("[AI_STREAM_ERROR] " + e.getMessage());
            e.printStackTrace();
            String fallback = buildFallbackReply(message, "AI 通道暂时不可用，已使用本地经营摘要回复。原因：" + simplifyException(e));
            emitToken(emitter, fallback);
            state.messages.add(ChatMessage.assistant(fallback));
            emitDone(emitter);
        }
    }

    // ==================== 流式请求辅助方法 ====================

    private ObjectNode buildStreamRequestBody(ChatSessionState state, String memoryText,
                                               String systemPrompt, List<String> enabledTools,
                                               boolean includeToolResults) {
        ProviderConfig config = resolveProviderConfig();
        boolean isResponses = "responses".equals(config.apiType());

        ObjectNode requestBody = objectMapper.createObjectNode();
        applyRequestDefaults(requestBody);
        requestBody.put("stream", true);

        // 工具定义
        Map<String, Object> tools = aiToolService.buildToolDefinitions(enabledTools);

        if (isResponses) {
            requestBody.put("max_output_tokens", 2000);
            requestBody.put("instructions", systemPrompt
                    + "\n\n长期记忆文档：\n" + trimForPrompt(memoryText, MAX_MEMORY_PROMPT_CHARS)
                    + "\n\n当前日期：" + LocalDate.now(DEFAULT_ZONE));

            if (!tools.isEmpty()) {
                ArrayNode toolsArray = requestBody.putArray("tools");
                for (Object toolDef : tools.values()) {
                    toolsArray.add(objectMapper.valueToTree(toolDef));
                }
            }

            ArrayNode input = requestBody.putArray("input");
            for (ChatMessage item : state.messages) {
                ObjectNode messageNode = input.addObject();
                messageNode.put("role", item.role);
                ArrayNode content = messageNode.putArray("content");
                content.addObject()
                        .put("type", "input_text")
                        .put("text", item.content);
            }
        } else {
            // /chat/completions 格式
            requestBody.put("max_tokens", 2000);
            if (!tools.isEmpty()) {
                ArrayNode toolsArray = requestBody.putArray("tools");
                for (Object toolDef : tools.values()) {
                    // /chat/completions 需要 type: function 包装层
                    ObjectNode chatTool = objectMapper.createObjectNode();
                    chatTool.put("type", "function");
                    chatTool.set("function", objectMapper.valueToTree(toolDef));
                    toolsArray.add(chatTool);
                }
            }

            ArrayNode messages = requestBody.putArray("messages");
            // system prompt 作为第一条 system 消息
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt
                    + "\n\n长期记忆文档：\n" + trimForPrompt(memoryText, MAX_MEMORY_PROMPT_CHARS)
                    + "\n\n当前日期：" + LocalDate.now(DEFAULT_ZONE));

            for (ChatMessage item : state.messages) {
                ObjectNode messageNode = messages.addObject();
                messageNode.put("role", item.role);
                messageNode.put("content", item.content);
            }
        }
        return requestBody;
    }

    private String buildAgentSystemPrompt(AiAgentConfig agent) {
        if (agent != null && StringUtils.hasText(agent.getSystemPrompt())) {
            return agent.getSystemPrompt();
        }
        return "你是口腔门诊的 AI 财务分析助手。"
                + " 你需要先阅读长期记忆，再结合最新经营分析数据回答。"
                + " 只基于已提供的数据和长期记忆作答，不要编造。"
                + " 优先给出结论、原因、下一步动作，回答用中文，简洁但要能落地。"
                + " 若问题超出当前数据范围，要明确说明不确定。"
                + " 你可以调用工具查询数据库获取实时信息。";
    }

    /**
     * 同步发送请求，返回完整 SSE 文本（用于解析 function_call）
     */
    private String sendChatRequestSync(ObjectNode requestBody) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(requestBody);
        ProviderConfig config = resolveProviderConfig();
        String endpoint = "responses".equals(config.apiType()) ? "/responses" : "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(config.baseUrl()) + endpoint))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .timeout(OPENAI_REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new IOException("HTTP " + response.statusCode() + " " + response.body());
    }

    /**
     * 解析 SSE 响应，提取文本内容和 Function Calling 信息
     * 兼容 /responses 和 /chat/completions 两种格式
     */
    private void parseStreamResponse(String sseText, StringBuilder output, List<FunctionCallInfo> functionCalls) {
        ProviderConfig config = resolveProviderConfig();
        boolean isResponses = "responses".equals(config.apiType());
        String[] lines = sseText.split("\n");
        StringBuilder currentData = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("data: ")) {
                currentData.append(line.substring(6));
            } else if (line.isEmpty() && currentData.length() > 0) {
                try {
                    if ("[DONE]".equals(currentData.toString())) {
                        currentData.setLength(0);
                        continue;
                    }
                    JsonNode event = objectMapper.readTree(currentData.toString());
                    if (isResponses) {
                        parseResponsesStreamEvent(event, output, functionCalls);
                    } else {
                        parseChatCompletionsStreamEvent(event, output, functionCalls);
                    }
                } catch (Exception e) {
                    // 忽略解析失败的行
                }
                currentData.setLength(0);
            }
        }
    }

    private void parseResponsesStreamEvent(JsonNode event, StringBuilder output, List<FunctionCallInfo> functionCalls) {
        String type = event.has("type") ? event.get("type").asText() : "";
        if ("response.output_text.delta".equals(type) || "response.text.delta".equals(type)) {
            JsonNode delta = event.get("delta");
            if (delta != null && delta.isTextual()) {
                output.append(delta.asText());
            }
        } else if ("response.output_item.done".equals(type)) {
            JsonNode item = event.get("item");
            if (item != null && "function_call".equals(item.get("type").asText(""))) {
                String name = item.has("name") ? item.get("name").asText() : "";
                String arguments = item.has("arguments") ? item.get("arguments").asText() : "{}";
                functionCalls.add(new FunctionCallInfo(name, parseArguments(arguments)));
            }
        }
    }

    private void parseChatCompletionsStreamEvent(JsonNode event, StringBuilder output, List<FunctionCallInfo> functionCalls) {
        JsonNode choices = event.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return;
        }
        JsonNode delta = choices.get(0).get("delta");
        if (delta == null) {
            return;
        }
        // 文本内容
        JsonNode content = delta.get("content");
        if (content != null && content.isTextual()) {
            output.append(content.asText());
        }
        // Function Calling
        JsonNode toolCalls = delta.get("tool_calls");
        if (toolCalls != null && toolCalls.isArray()) {
            for (JsonNode tc : toolCalls) {
                JsonNode function = tc.get("function");
                if (function != null) {
                    String name = function.has("name") ? function.get("name").asText() : "";
                    String arguments = function.has("arguments") ? function.get("arguments").asText() : "";
                    if (StringUtils.hasText(name)) {
                        functionCalls.add(new FunctionCallInfo(name, parseArguments(arguments)));
                    }
                }
            }
        }
        // 处理 finish_reason 中的 tool_calls
        JsonNode finishReason = choices.get(0).get("finish_reason");
        if (finishReason != null && "tool_calls".equals(finishReason.asText(""))) {
            // tool_calls 已经在 delta 中处理，这里不需要额外操作
        }
    }

    /**
     * 真正的流式输出：逐 token emit 给前端
     * 兼容 /responses 和 /chat/completions
     */
    private void streamChatRequest(ObjectNode requestBody, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter,
                                    StringBuilder assistantContent) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(requestBody);
        ProviderConfig config = resolveProviderConfig();
        boolean isResponses = "responses".equals(config.apiType());
        String endpoint = isResponses ? "/responses" : "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimTrailingSlash(config.baseUrl()) + endpoint))
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .timeout(OPENAI_REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        java.util.concurrent.Flow.Subscriber<String> subscriber = new java.util.concurrent.Flow.Subscriber<>() {
            private java.util.concurrent.Flow.Subscription subscription;
            private final StringBuilder lineBuffer = new StringBuilder();
            private final StringBuilder dataBuffer = new StringBuilder();

            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String item) {
                for (char c : item.toCharArray()) {
                    if (c == '\n') {
                        processLine(lineBuffer.toString().trim());
                        lineBuffer.setLength(0);
                    } else {
                        lineBuffer.append(c);
                    }
                }
            }

            private void processLine(String line) {
                if (line.startsWith("data: ")) {
                    dataBuffer.append(line.substring(6));
                } else if (line.isEmpty() && dataBuffer.length() > 0) {
                    try {
                        String data = dataBuffer.toString();
                        if ("[DONE]".equals(data)) {
                            dataBuffer.setLength(0);
                            return;
                        }
                        JsonNode event = objectMapper.readTree(data);
                        if (isResponses) {
                            processResponsesStreamEvent(event, emitter, assistantContent);
                        } else {
                            processChatCompletionsStreamEvent(event, emitter, assistantContent);
                        }
                    } catch (Exception ignored) {}
                    dataBuffer.setLength(0);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("[AI_STREAM_SUBSCRIBER_ERROR] " + throwable.getMessage());
            }

            @Override
            public void onComplete() {}
        };

        httpClient.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.fromLineSubscriber(subscriber))
                .thenAccept(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        System.err.println("[AI_STREAM_HTTP_ERROR] " + response.statusCode());
                    }
                })
                .join();
    }

    private void processResponsesStreamEvent(JsonNode event, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter,
                                              StringBuilder assistantContent) {
        String type = event.has("type") ? event.get("type").asText() : "";
        if ("response.output_text.delta".equals(type) || "response.text.delta".equals(type)) {
            JsonNode delta = event.get("delta");
            if (delta != null && delta.isTextual()) {
                String text = delta.asText();
                emitToken(emitter, text);
                assistantContent.append(text);
            }
        }
    }

    private void processChatCompletionsStreamEvent(JsonNode event, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter,
                                                    StringBuilder assistantContent) {
        JsonNode choices = event.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            return;
        }
        JsonNode delta = choices.get(0).get("delta");
        if (delta == null) {
            return;
        }
        JsonNode content = delta.get("content");
        if (content != null && content.isTextual()) {
            String text = content.asText();
            emitToken(emitter, text);
            assistantContent.append(text);
        }
    }

    private void emitToken(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter, String token) {
        try {
            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                    .name("token")
                    .data(token));
        } catch (Exception e) {
            System.err.println("[EMIT_ERROR] " + e.getMessage());
        }
    }

    private void emitDone(org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter) {
        try {
            emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                    .name("done")
                    .data(""));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
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

    private Map<String, Object> parseArguments(String argumentsJson) {
        try {
            return objectMapper.readValue(argumentsJson, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private static class FunctionCallInfo {
        final String name;
        final Map<String, Object> arguments;
        FunctionCallInfo(String name, Map<String, Object> arguments) {
            this.name = name;
            this.arguments = arguments;
        }
    }

    public synchronized Map<String, Object> getMemoryDocument(Long accountId) {
        String userKey = buildUserKey(accountId);
        String content = readMemoryText(userKey);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("has_memory", StringUtils.hasText(content));
        result.put("content", content);
        result.put("updated_at", formatFileUpdatedAt(userKey));
        return result;
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Shanghai")
    public synchronized void summarizeExpiredSessions() {
        List<String> userKeys = new ArrayList<>(sessions.keySet());
        for (String userKey : userKeys) {
            flushExpiredSession(userKey);
        }
    }

    private boolean flushExpiredSession(String userKey) {
        ChatSessionState state = sessions.get(userKey);
        if (state == null || !state.isExpired()) {
            return false;
        }
        if (state.hasConversation()) {
            appendSessionSummary(userKey, state);
        }
        sessions.remove(userKey);
        return true;
    }

    private void appendSessionSummary(String userKey, ChatSessionState state) {
        try {
            Files.createDirectories(Paths.get(MEMORY_DIR));
            Path path = memoryPath(userKey);
            String existing = readMemoryText(userKey);
            String summary = buildSessionSummary(existing, state);
            StringBuilder builder = new StringBuilder();
            if (StringUtils.hasText(existing)) {
                builder.append(existing.trim()).append("\n\n");
            } else {
                builder.append("# AI 财务分析长期记忆\n\n");
            }
            builder.append("## 会话总结 ").append(formatDateTime(state.lastActivityAt)).append("\n");
            builder.append(summary.trim()).append("\n");
            String content = trimMemoryDocument(builder.toString());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            System.err.println("[BUSINESS_ANALYSIS_CHAT_MEMORY_WRITE_FAILED] " + exception.getMessage());
        }
    }

    private String buildSessionSummary(String existingMemory, ChatSessionState state) {
        String transcript = buildTranscript(state);
        if (!isOpenAiReady()) {
            return buildFallbackSummary(state);
        }
        try {
            ProviderConfig config = resolveProviderConfig();
            boolean isResponses = "responses".equals(config.apiType());
            ObjectNode requestBody = objectMapper.createObjectNode();
            applyRequestDefaults(requestBody);
            if (isResponses) {
                requestBody.put("max_output_tokens", 500);
                requestBody.put("instructions",
                        "你是门诊经营与财务分析助手的记忆整理器。请把本次会话提炼成适合长期记忆文档保存的 Markdown。"
                                + " 只保留长期有效的信息，不要逐句复述。"
                                + " 优先输出：用户偏好/规则、长期关注问题、已确认结论、待持续跟进事项。");
                ArrayNode input = requestBody.putArray("input");
                ObjectNode userMessage = input.addObject();
                userMessage.put("role", "user");
                ArrayNode content = userMessage.putArray("content");
                content.addObject().put("type", "input_text").put("text",
                        "已有长期记忆（可为空）：\n" + trimForPrompt(existingMemory, 4000)
                                + "\n\n本次会话记录：\n" + transcript
                                + "\n\n请输出 Markdown 条目，控制在 8 条以内。");
            } else {
                requestBody.put("max_tokens", 500);
                ArrayNode messages = requestBody.putArray("messages");
                ObjectNode systemMsg = messages.addObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", "你是门诊经营与财务分析助手的记忆整理器。请把本次会话提炼成适合长期记忆文档保存的 Markdown。只保留长期有效的信息，不要逐句复述。优先输出：用户偏好/规则、长期关注问题、已确认结论、待持续跟进事项。");
                ObjectNode userMsg = messages.addObject();
                userMsg.put("role", "user");
                userMsg.put("content",
                        "已有长期记忆（可为空）：\n" + trimForPrompt(existingMemory, 4000)
                                + "\n\n本次会话记录：\n" + transcript
                                + "\n\n请输出 Markdown 条目，控制在 8 条以内。");
            }
            String outputText = extractOutputText(sendChatRequest(requestBody));
            if (StringUtils.hasText(outputText)) {
                return outputText.trim();
            }
        } catch (Exception exception) {
            System.err.println("[BUSINESS_ANALYSIS_CHAT_MEMORY_SUMMARY_FAILED] " + exception.getMessage());
        }
        return buildFallbackSummary(state);
    }

    private String buildFallbackSummary(ChatSessionState state) {
        List<String> userMessages = state.messages.stream()
                .filter(item -> "user".equals(item.role))
                .map(item -> item.content)
                .filter(StringUtils::hasText)
                .limit(6)
                .toList();
        StringBuilder builder = new StringBuilder();
        builder.append("- 本次主要关注：").append(userMessages.isEmpty() ? "未提炼出明确主题" : String.join("；", userMessages)).append("\n");
        builder.append("- 会话时间：").append(formatDateTime(state.startedAt)).append(" 至 ").append(formatDateTime(state.lastActivityAt)).append("\n");
        builder.append("- 说明：本条为自动回退摘要，建议后续继续补充更稳定的经营偏好和财务规则。");
        return builder.toString();
    }

    private String requestChatCompletion(ChatSessionState state, String memoryText, String groundingContext) throws IOException, InterruptedException {
        ProviderConfig config = resolveProviderConfig();
        boolean isResponses = "responses".equals(config.apiType());
        ObjectNode requestBody = objectMapper.createObjectNode();
        applyRequestDefaults(requestBody);

        String systemPrompt = "你是口腔门诊的 AI 财务分析助手。"
                + " 你需要先阅读长期记忆，再结合最新经营分析数据回答。"
                + " 只基于已提供的数据和长期记忆作答，不要编造。"
                + " 优先给出结论、原因、下一步动作，回答用中文，简洁但要能落地。"
                + " 若问题超出当前数据范围，要明确说明不确定。"
                + "\n\n长期记忆文档：\n" + trimForPrompt(memoryText, MAX_MEMORY_PROMPT_CHARS)
                + "\n\n当前经营分析上下文：\n" + groundingContext;

        if (isResponses) {
            requestBody.put("max_output_tokens", 1000);
            requestBody.put("instructions", systemPrompt);
            ArrayNode input = requestBody.putArray("input");
            for (ChatMessage item : state.messages) {
                ObjectNode messageNode = input.addObject();
                messageNode.put("role", item.role);
                ArrayNode content = messageNode.putArray("content");
                content.addObject()
                        .put("type", "input_text")
                        .put("text", item.content);
            }
        } else {
            requestBody.put("max_tokens", 1000);
            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            for (ChatMessage item : state.messages) {
                ObjectNode messageNode = messages.addObject();
                messageNode.put("role", item.role);
                messageNode.put("content", item.content);
            }
        }
        String outputText = extractOutputText(sendChatRequest(requestBody));
        if (!StringUtils.hasText(outputText)) {
            throw new IOException("模型未返回可读文本");
        }
        return outputText.trim();
    }

    private String buildFallbackReply(String userMessage, String prefix) {
        Map<String, Object> latestDaily = businessDailyAnalysisService.getLatestAnalysis();
        Map<String, Object> latestWeekly = businessPeriodReportService.getLatestWeeklyReport();
        Map<String, Object> latestMonthly = businessPeriodReportService.getLatestMonthlyReport();
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Map<String, Object> expenseOverview = financeService.buildExpenseOverview(today.withDayOfMonth(1).toString(), today.toString());

        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(prefix)) {
            builder.append(prefix).append("\n\n");
        }
        builder.append("你刚才的问题是：").append(userMessage).append("\n");
        builder.append("先给你当前可直接确认的财务/经营口径：\n");
        builder.append("1. 当前经营支出固定分成三类：门店耗材、加工账单、非耗材支出。\n");
        builder.append("2. 本月支出汇总：耗材 ¥")
                .append(formatMoney(expenseOverview.get("material_expense")))
                .append("，加工 ¥")
                .append(formatMoney(expenseOverview.get("lab_expense")))
                .append("，非耗材 ¥")
                .append(formatMoney(expenseOverview.get("other_expense")))
                .append("，总支出 ¥")
                .append(formatMoney(expenseOverview.get("total_expense")))
                .append("。\n");
        if (latestDaily != null) {
            builder.append("3. 最新日报：")
                    .append(stringValue(latestDaily.get("analysis_date"), "-"))
                    .append("，")
                    .append(stringValue(latestDaily.get("headline"), "暂无标题"))
                    .append("。\n");
        }
        if (latestWeekly != null) {
            builder.append("4. 最新周报：")
                    .append(stringValue(latestWeekly.get("period_label"), "-"))
                    .append("，")
                    .append(stringValue(latestWeekly.get("headline"), "暂无标题"))
                    .append("。\n");
        }
        if (latestMonthly != null) {
            builder.append("5. 最新月报：")
                    .append(stringValue(latestMonthly.get("period_label"), "-"))
                    .append("，")
                    .append(stringValue(latestMonthly.get("headline"), "暂无标题"))
                    .append("。\n");
        }
        builder.append("如果你继续追问某一类支出、某个时间段，或者想让我解释日报/周报里某个结论，可以继续发，我会基于当前系统数据继续回答。");
        return builder.toString();
    }

    private String buildGroundingContext() {
        Map<String, Object> latestDaily = businessDailyAnalysisService.getLatestAnalysis();
        Map<String, Object> latestWeekly = businessPeriodReportService.getLatestWeeklyReport();
        Map<String, Object> latestMonthly = businessPeriodReportService.getLatestMonthlyReport();
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Map<String, Object> expenseOverview = financeService.buildExpenseOverview(
                today.withDayOfMonth(1).toString(),
                today.toString()
        );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("today", today.toString());
        context.put("latest_daily_analysis", compactAnalysisContext(latestDaily));
        context.put("latest_weekly_report", compactPeriodContext(latestWeekly));
        context.put("latest_monthly_report", compactPeriodContext(latestMonthly));
        context.put("current_month_expense_overview", compactExpenseOverview(expenseOverview));
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (Exception exception) {
            return String.valueOf(context);
        }
    }

    private Map<String, Object> compactAnalysisContext(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        result.put("analysis_date", source.get("analysis_date"));
        result.put("headline", source.get("headline"));
        result.put("summary", source.get("summary"));
        result.put("operating_score", source.get("operating_score"));
        result.put("trend", source.get("trend"));
        Map<String, Object> metrics = safeMap(source.get("metrics"));
        Map<String, Object> compactMetrics = new LinkedHashMap<>();
        copyIfPresent(metrics, compactMetrics, "today_income", "today_expense", "today_net_income",
                "today_material_expense", "today_lab_expense", "today_other_expense",
                "current_month_income", "current_month_expense", "current_month_net_income",
                "current_month_material_expense", "current_month_lab_expense", "current_month_other_expense",
                "today_appointments", "today_medical_records", "today_treatments", "future_7_day_appointments");
        result.put("metrics", compactMetrics);
        Map<String, Object> analysis = safeMap(source.get("analysis"));
        Map<String, Object> compactAnalysis = new LinkedHashMap<>();
        compactAnalysis.put("management_brief", analysis.get("management_brief"));
        compactAnalysis.put("highlights", trimList(analysis.get("highlights"), 3));
        compactAnalysis.put("risks", trimList(analysis.get("risks"), 2));
        compactAnalysis.put("opportunities", trimList(analysis.get("opportunities"), 2));
        compactAnalysis.put("actions", trimList(analysis.get("actions"), 3));
        result.put("analysis", compactAnalysis);
        return result;
    }

    private Map<String, Object> compactPeriodContext(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        result.put("period_label", source.get("period_label"));
        result.put("headline", source.get("headline"));
        result.put("summary", source.get("summary"));
        result.put("operating_score", source.get("operating_score"));
        result.put("trend", source.get("trend"));
        Map<String, Object> metrics = safeMap(source.get("metrics"));
        Map<String, Object> compactMetrics = new LinkedHashMap<>();
        copyIfPresent(metrics, compactMetrics, "total_income", "total_expense", "net_income",
                "material_expense", "lab_expense", "other_expense",
                "total_appointments", "total_treatments", "total_unique_patients",
                "net_income_change_rate", "appointment_change_rate");
        result.put("metrics", compactMetrics);
        return result;
    }

    private Map<String, Object> compactExpenseOverview(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        copyIfPresent(source, result, "start_date", "end_date", "total_expense",
                "material_expense", "lab_expense", "other_expense",
                "material_count", "lab_count", "other_count");
        return result;
    }

    private Map<String, Object> safeMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return result;
        }
        return new LinkedHashMap<>();
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String... keys) {
        for (String key : keys) {
            if (source.containsKey(key)) {
                target.put(key, source.get(key));
            }
        }
    }

    private List<Object> trimList(Object value, int maxSize) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().limit(maxSize).map(this::compactCollectionItem).toList();
    }

    private Object compactCollectionItem(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                String key = String.valueOf(entry.getKey());
                if (List.of("title", "severity", "impact", "finding", "recommendation", "priority", "action", "owner", "due", "expected_result").contains(key)) {
                    result.put(key, entry.getValue());
                }
            }
            return result;
        }
        return value;
    }

    private Map<String, Object> buildSessionView(String userKey, ChatSessionState state, boolean restarted) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("session_id", state.sessionId);
        result.put("restarted", restarted);
        result.put("started_at", formatDateTime(state.startedAt));
        result.put("last_activity_at", formatDateTime(state.lastActivityAt));
        result.put("timeout_minutes", SESSION_TIMEOUT.toMinutes());
        result.put("memory_preview", trimForPreview(readMemoryText(userKey)));
        result.put("memory_updated_at", formatFileUpdatedAt(userKey));
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

    private boolean hasLongTermMemory(String userKey) {
        return StringUtils.hasText(readMemoryText(userKey));
    }

    private String readMemoryText(String userKey) {
        try {
            Path path = memoryPath(userKey);
            if (!Files.exists(path)) {
                return "";
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            return "";
        }
    }

    private String formatFileUpdatedAt(String userKey) {
        try {
            Path path = memoryPath(userKey);
            if (!Files.exists(path)) {
                return "";
            }
            return DATE_TIME_FORMATTER.format(Files.getLastModifiedTime(path).toInstant().atZone(DEFAULT_ZONE).toLocalDateTime());
        } catch (Exception exception) {
            return "";
        }
    }

    private Path memoryPath(String userKey) {
        return Paths.get(MEMORY_DIR, "business-analysis-chat-memory-" + userKey + ".md");
    }

    private String buildTranscript(ChatSessionState state) {
        StringBuilder builder = new StringBuilder();
        for (ChatMessage item : state.messages) {
            builder.append(item.role.equals("assistant") ? "助手" : "用户")
                    .append("（").append(formatDateTime(item.createdAt)).append("）: ")
                    .append(item.content)
                    .append("\n");
        }
        return builder.toString().trim();
    }

    private String trimMemoryDocument(String value) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= 24000) {
            return text + "\n";
        }
        String prefix = "# AI 财务分析长期记忆\n\n";
        String trimmed = text.substring(text.length() - 22000);
        int headingIndex = trimmed.indexOf("## ");
        if (headingIndex >= 0) {
            trimmed = trimmed.substring(headingIndex);
        }
        return prefix + trimmed + "\n";
    }

    private String trimForPrompt(String value, int maxChars) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(text.length() - maxChars);
    }

    private String trimForPreview(String value) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= MAX_MEMORY_PREVIEW_CHARS) {
            return text;
        }
        return "...\n" + text.substring(text.length() - MAX_MEMORY_PREVIEW_CHARS);
    }

    private String buildUserKey(Long accountId) {
        return accountId != null && accountId > 0 ? String.valueOf(accountId) : "global";
    }

    private String normalizeAccountName(String accountName) {
        return StringUtils.hasText(accountName) ? accountName.trim() : "未命名用户";
    }

    private record ProviderConfig(String baseUrl, String apiKey, String modelName,
                                   String reasoningEffort, Integer maxOutputTokens,
                                   boolean enabled, String apiType) {}

    private ProviderConfig resolveProviderConfig() {
        if (modelProviderService != null) {
            com.example.springboot.entity.AiModelProvider dynamic = modelProviderService.getActiveProvider();
            if (dynamic != null && Boolean.TRUE.equals(dynamic.getEnabled())
                    && StringUtils.hasText(dynamic.getBaseUrl())
                    && StringUtils.hasText(dynamic.getApiKey())
                    && StringUtils.hasText(dynamic.getModelName())) {
                String apiType = dynamic.getApiType();
                if (!StringUtils.hasText(apiType)) {
                    apiType = "chat_completions";
                }
                return new ProviderConfig(
                        dynamic.getBaseUrl(),
                        dynamic.getApiKey(),
                        dynamic.getModelName(),
                        dynamic.getReasoningEffort(),
                        dynamic.getMaxOutputTokens(),
                        true,
                        apiType
                );
            }
        }
        return new ProviderConfig(
                openAiProperties.getBaseUrl(),
                openAiProperties.getApiKey(),
                openAiProperties.getBusinessAnalysis().getModel(),
                openAiProperties.getBusinessAnalysis().getReasoningEffort(),
                openAiProperties.getBusinessAnalysis().getMaxOutputTokens(),
                openAiProperties.isEnabled(),
                "responses"
        );
    }

    private boolean isOpenAiReady() {
        ProviderConfig config = resolveProviderConfig();
        return config.enabled()
                && StringUtils.hasText(config.apiKey())
                && StringUtils.hasText(config.baseUrl())
                && StringUtils.hasText(config.modelName());
    }

    private void applyRequestDefaults(ObjectNode requestBody) {
        ProviderConfig config = resolveProviderConfig();
        requestBody.put("model", config.modelName());
        boolean isResponses = "responses".equals(config.apiType());
        if (isResponses) {
            requestBody.put("store", !openAiProperties.isDisableResponseStorage());
            if (StringUtils.hasText(config.reasoningEffort())) {
                requestBody.putObject("reasoning").put("effort", config.reasoningEffort());
            }
        }
    }

    private JsonNode sendChatRequest(ObjectNode requestBody) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(requestBody);
        ProviderConfig config = resolveProviderConfig();
        boolean isResponses = "responses".equals(config.apiType());
        String endpoint = isResponses ? "/responses" : "/chat/completions";
        IOException lastIOException = null;
        InterruptedException lastInterruptedException = null;
        for (int attempt = 1; attempt <= OPENAI_MAX_RETRIES; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(config.baseUrl()) + endpoint))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .timeout(OPENAI_REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readTree(response.body());
                }
                if (!isRetryableStatus(response.statusCode()) || attempt == OPENAI_MAX_RETRIES) {
                    throw new IOException("HTTP " + response.statusCode() + " " + response.body());
                }
                sleepBeforeRetry(attempt);
            } catch (IOException exception) {
                lastIOException = exception;
                if (attempt == OPENAI_MAX_RETRIES) {
                    throw exception;
                }
                sleepBeforeRetry(attempt);
            } catch (InterruptedException exception) {
                lastInterruptedException = exception;
                Thread.currentThread().interrupt();
                throw exception;
            }
        }
        if (lastIOException != null) {
            throw lastIOException;
        }
        if (lastInterruptedException != null) {
            throw lastInterruptedException;
        }
        throw new IOException("请求 AI API 失败");
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        long delayMillis = Math.min(4000L, 500L * attempt);
        Thread.sleep(delayMillis);
    }

    private String extractOutputText(JsonNode responseJson) {
        if (responseJson == null) {
            return "";
        }
        ProviderConfig config = resolveProviderConfig();
        boolean isResponses = "responses".equals(config.apiType());

        if (isResponses) {
            // /responses 格式
            JsonNode direct = responseJson.get("output_text");
            if (direct != null && direct.isTextual() && StringUtils.hasText(direct.asText())) {
                return direct.asText();
            }
            JsonNode output = responseJson.get("output");
            if (output != null && output.isArray()) {
                StringBuilder builder = new StringBuilder();
                for (JsonNode item : output) {
                    JsonNode content = item.get("content");
                    if (content == null || !content.isArray()) {
                        continue;
                    }
                    for (JsonNode contentItem : content) {
                        JsonNode textNode = contentItem.get("text");
                        if (textNode != null && textNode.isTextual()) {
                            if (builder.length() > 0) {
                                builder.append('\n');
                            }
                            builder.append(textNode.asText());
                        }
                    }
                }
                return builder.toString().trim();
            }
        } else {
            // /chat/completions 格式
            JsonNode choices = responseJson.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null && content.isTextual() && StringUtils.hasText(content.asText())) {
                        return content.asText().trim();
                    }
                }
            }
        }
        return "";
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

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String formatDateTime(Instant instant) {
        return instant == null ? "" : DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(instant, DEFAULT_ZONE));
    }

    private String formatMoney(Object value) {
        try {
            return String.format(Locale.US, "%.2f", value == null ? 0D : Double.parseDouble(String.valueOf(value)));
        } catch (Exception exception) {
            return "0.00";
        }
    }

    private String stringValue(Object value, String fallback) {
        String text = value == null ? "" : String.valueOf(value).trim();
        return StringUtils.hasText(text) ? text : fallback;
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

        private boolean hasConversation() {
            return messages.stream().anyMatch(item -> "user".equals(item.role));
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

        static ChatMessage tool(String content) {
            return new ChatMessage("system", "[工具查询结果]\n" + content);
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

package com.example.springboot.service;

import com.example.springboot.config.OpenAiAnalysisProperties;
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
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ConcurrentMap<String, ChatSessionState> sessions = new ConcurrentHashMap<>();

    public BusinessAnalysisChatService(OpenAiAnalysisProperties openAiProperties,
                                       BusinessDailyAnalysisService businessDailyAnalysisService,
                                       BusinessPeriodReportService businessPeriodReportService,
                                       FinanceService financeService,
                                       ObjectMapper objectMapper) {
        this.openAiProperties = openAiProperties;
        this.businessDailyAnalysisService = businessDailyAnalysisService;
        this.businessPeriodReportService = businessPeriodReportService;
        this.financeService = financeService;
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
            ObjectNode requestBody = objectMapper.createObjectNode();
            applyRequestDefaults(requestBody);
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
            String outputText = extractOutputText(sendResponsesRequest(requestBody));
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
        ObjectNode requestBody = objectMapper.createObjectNode();
        applyRequestDefaults(requestBody);
        requestBody.put("max_output_tokens", 1000);
        requestBody.put("instructions",
                "你是口腔门诊的 AI 财务分析助手。"
                        + " 你需要先阅读长期记忆，再结合最新经营分析数据回答。"
                        + " 只基于已提供的数据和长期记忆作答，不要编造。"
                        + " 优先给出结论、原因、下一步动作，回答用中文，简洁但要能落地。"
                        + " 若问题超出当前数据范围，要明确说明不确定。"
                        + "\n\n长期记忆文档：\n" + trimForPrompt(memoryText, MAX_MEMORY_PROMPT_CHARS)
                        + "\n\n当前经营分析上下文：\n" + groundingContext);

        ArrayNode input = requestBody.putArray("input");
        for (ChatMessage item : state.messages) {
            ObjectNode messageNode = input.addObject();
            messageNode.put("role", item.role);
            ArrayNode content = messageNode.putArray("content");
            content.addObject()
                    .put("type", "input_text")
                    .put("text", item.content);
        }
        String outputText = extractOutputText(sendResponsesRequest(requestBody));
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

    private boolean isOpenAiReady() {
        return openAiProperties.isEnabled()
                && StringUtils.hasText(openAiProperties.getApiKey())
                && StringUtils.hasText(openAiProperties.getBaseUrl())
                && StringUtils.hasText(openAiProperties.getBusinessAnalysis().getModel());
    }

    private void applyRequestDefaults(ObjectNode requestBody) {
        requestBody.put("model", openAiProperties.getBusinessAnalysis().getModel());
        requestBody.put("store", !openAiProperties.isDisableResponseStorage());
        if (StringUtils.hasText(openAiProperties.getBusinessAnalysis().getReasoningEffort())) {
            requestBody.putObject("reasoning").put("effort", openAiProperties.getBusinessAnalysis().getReasoningEffort());
        }
    }

    private JsonNode sendResponsesRequest(ObjectNode requestBody) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(requestBody);
        IOException lastIOException = null;
        InterruptedException lastInterruptedException = null;
        for (int attempt = 1; attempt <= OPENAI_MAX_RETRIES; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(openAiProperties.getBaseUrl()) + "/responses"))
                    .header("Authorization", "Bearer " + openAiProperties.getApiKey())
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
        throw new IOException("请求 OpenAI Responses API 失败");
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

        Map<String, Object> toView() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("role", role);
            result.put("content", content);
            result.put("created_at", DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(createdAt, DEFAULT_ZONE)));
            return result;
        }
    }
}

package com.example.springboot.service;

import com.example.springboot.entity.AiModelProvider;
import com.example.springboot.mapper.AiModelProviderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class AiModelProviderService {

    private final AiModelProviderMapper mapper;
    private final ObjectMapper objectMapper;

    public AiModelProviderService(AiModelProviderMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public AiModelProvider getActiveProvider() {
        return mapper.selectFirst();
    }

    public AiModelProvider getById(Long id) {
        return mapper.selectById(id);
    }

    public void save(AiModelProvider provider) {
        if (provider.getId() == null) {
            if (provider.getEnabled() == null) {
                provider.setEnabled(true);
            }
            if (provider.getMaxOutputTokens() == null) {
                provider.setMaxOutputTokens(3000);
            }
            if (!StringUtils.hasText(provider.getReasoningEffort())) {
                provider.setReasoningEffort("medium");
            }
            if (!StringUtils.hasText(provider.getApiType())) {
                provider.setApiType("chat_completions");
            }
            mapper.insert(provider);
        } else {
            AiModelProvider existing = mapper.selectById(provider.getId());
            if (existing == null) {
                throw new IllegalArgumentException("配置不存在");
            }
            if (!StringUtils.hasText(provider.getApiType())) {
                provider.setApiType(existing.getApiType());
            }
            mapper.update(provider);
            if (StringUtils.hasText(provider.getApiKey())) {
                mapper.updateApiKey(provider.getId(), provider.getApiKey());
            }
        }
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }

    public boolean exists() {
        return mapper.count() > 0;
    }

    public String testConnection(AiModelProvider provider) {
        String baseUrl = provider.getBaseUrl();
        String apiKey = provider.getApiKey();
        String modelName = provider.getModelName();
        String apiType = provider.getApiType();

        if (!StringUtils.hasText(apiKey) && provider.getId() != null) {
            AiModelProvider existing = mapper.selectById(provider.getId());
            if (existing != null) {
                apiKey = existing.getApiKey();
                if (!StringUtils.hasText(apiType)) {
                    apiType = existing.getApiType();
                }
            }
        }

        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(modelName)) {
            throw new IllegalArgumentException("缺少必要参数：API 基础地址、密钥、模型名称");
        }

        if (!StringUtils.hasText(apiType)) {
            apiType = "chat_completions";
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            if ("responses".equals(apiType)) {
                return testResponsesEndpoint(client, baseUrl, apiKey, modelName);
            }
            // 默认使用 /chat/completions（兼容性最好）
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", modelName);
            body.put("max_tokens", 1);
            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", "hi");

            String url = trimTrailingSlash(baseUrl) + "/chat/completions";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "连接成功（Chat Completions），模型 " + modelName + " 可正常调用";
            } else if (response.statusCode() == 401) {
                throw new RuntimeException("API 密钥无效或已过期（HTTP 401）");
            } else if (response.statusCode() == 404) {
                // 部分供应商可能不支持 /chat/completions，回退到 /responses 测试
                return testResponsesEndpoint(client, baseUrl, apiKey, modelName);
            } else {
                String errBody = response.body();
                if (errBody != null && errBody.length() > 300) {
                    errBody = errBody.substring(0, 300);
                }
                throw new RuntimeException("请求失败：HTTP " + response.statusCode() + " " + errBody);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("连接测试失败：" + e.getMessage(), e);
        }
    }

    private String testResponsesEndpoint(HttpClient client, String baseUrl, String apiKey, String modelName) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", modelName);
            body.put("max_output_tokens", 1);
            body.put("input", "hi");

            String url = trimTrailingSlash(baseUrl) + "/responses";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "连接成功（Responses API），模型 " + modelName + " 可正常调用";
            } else if (response.statusCode() == 401) {
                throw new RuntimeException("API 密钥无效或已过期（HTTP 401）");
            } else {
                throw new RuntimeException("该供应商暂不支持 /chat/completions 或 /responses 端点（HTTP " + response.statusCode() + "）");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("连接测试失败：" + e.getMessage(), e);
        }
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
}

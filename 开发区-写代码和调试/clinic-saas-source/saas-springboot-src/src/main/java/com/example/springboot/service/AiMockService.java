package com.example.springboot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * AI Mock 服务
 * 当外部工作流端点未配置或指向测试地址时，提供模拟响应，保证前端体验可用
 */
@Service
public class AiMockService {

    public AiMockService() {
    }

    /**
     * 判断是否需要走 Mock 逻辑
     */
    public boolean shouldMock(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.trim().isEmpty()) {
            return true;
        }
        String lower = endpointUrl.toLowerCase();
        return lower.contains("httpbin.org")
                || lower.contains("example.com")
                || lower.contains("localhost")
                || lower.contains("127.0.0.1")
                || lower.contains("0.0.0.0")
                || lower.contains("::1");
    }

    /**
     * 生成"未配置 AI Agent"提示响应
     * 当外部端点未配置或指向测试地址时，明确提示用户前往配置，而非返回假数据
     */
    public String mockResponse(String agentKey, Map<String, Object> payload) {
        return buildUnconfiguredPrompt(agentKey);
    }

    /**
     * SSE 流式"未配置"提示响应
     * 模拟逐字输出效果，让前端体验与真实流式一致
     */
    public void mockStreamResponse(String agentKey, Map<String, Object> payload, SseEmitter emitter) {
        String prompt = buildUnconfiguredPrompt(agentKey);
        new Thread(() -> {
            try {
                int chunkSize = 4;
                for (int i = 0; i < prompt.length(); i += chunkSize) {
                    String chunk = prompt.substring(i, Math.min(i + chunkSize, prompt.length()));
                    emitter.send(SseEmitter.event().data(chunk));
                    Thread.sleep(25);
                }
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
    }

    private String buildUnconfiguredPrompt(String agentKey) {
        return "⚠️ AI Agent 未配置\n\n"
                + "当前 Agent「" + agentKey + "」尚未配置有效的外部工作流端点，"
                + "或配置的端点为测试地址（如 httpbin.org、example.com、localhost:9999）。\n\n"
                + "请前往【系统设置 > AI 智能中心】完成以下配置：\n"
                + "1. 添加或编辑「" + agentKey + "」Agent\n"
                + "2. 填写真实的工作流端点 URL\n"
                + "3. 配置认证信息（如 Bearer Token、API Key 等）\n\n"
                + "配置完成后重新发起对话即可正常使用 AI 功能。";
    }
}

package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WechatServiceTest {

    @Test
    void appointmentNotificationShouldUseInjectedRealConfig() {
        WechatService service = new WechatService(
                "real",
                "wx-real-appid",
                "real-secret",
                "tpl-123");

        String summary = service.debugConfigSummary();

        assertTrue(summary.contains("deliveryMode=real"), summary);
        assertTrue(summary.contains("appId=wx-real-appid"), summary);
        assertTrue(summary.contains("templateId=tpl-123"), summary);
    }
}

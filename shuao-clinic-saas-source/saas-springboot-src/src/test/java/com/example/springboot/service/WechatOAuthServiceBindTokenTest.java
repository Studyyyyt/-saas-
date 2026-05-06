package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WechatOAuthServiceBindTokenTest {

    @Test
    void issueAndConsumeStaffBindTokenShouldWorkOnce() {
        WechatOAuthService service = new WechatOAuthService(
                "wx-test-appid-123",
                "secret-123",
                "https://saas.shuao.cc",
                "https://saas.shuao.cc/app/bind-success");

        String token = service.issueStaffBindToken("openid-staff-bind-001");

        assertTrue(token != null && !token.isEmpty());
        assertEquals("openid-staff-bind-001", service.consumeStaffBindToken(token));
        assertEquals(null, service.consumeStaffBindToken(token));
    }

    @Test
    void issueAndResolvePatientPortalTokenShouldReturnSamePatientId() {
        WechatOAuthService service = new WechatOAuthService(
                "wx-test-appid-123",
                "secret-123",
                "https://saas.shuao.cc",
                "https://saas.shuao.cc/app/bind-success");

        String token = service.issuePatientPortalToken(12L);

        assertTrue(token != null && !token.isEmpty());
        assertEquals(12L, service.resolvePatientPortalToken(token));
    }

    @Test
    void issueAndResolveStaffPortalTokenShouldReturnSameAccountId() {
        WechatOAuthService service = new WechatOAuthService(
                "wx-test-appid-123",
                "secret-123",
                "https://saas.shuao.cc",
                "https://saas.shuao.cc/app/bind-success");

        String token = service.issueStaffPortalToken(8L);

        assertTrue(token != null && !token.isEmpty());
        assertEquals(8L, service.resolveStaffPortalToken(token));
    }
}

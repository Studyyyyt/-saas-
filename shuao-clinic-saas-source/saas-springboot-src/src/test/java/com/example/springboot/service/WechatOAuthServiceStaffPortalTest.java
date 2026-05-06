package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WechatOAuthServiceStaffPortalTest {

    @Test
    void buildStaffAuthorizeUrl_shouldUseConfiguredStaffPortalPath() {
        WechatOAuthService service = new WechatOAuthService(
                "wx-test-appid-123",
                "secret-123",
                "https://saas.shuao.cc",
                "https://saas.shuao.cc/app/bind-success",
                "https://saas.shuao.cc/patient-portal-home",
                "https://saas.shuao.cc/staff-portal-home",
                "https://saas.shuao.cc/admin-report-h5");

        String url = service.buildStaffAuthorizeUrl();

        assertTrue(url.contains("appid=wx-test-appid-123"), url);
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fsaas.shuao.cc%2Fstaff-portal%2Fcallback"), url);
        assertTrue(url.contains("scope=snsapi_base"), url);
    }

    @Test
    void buildStaffHomeUrl_shouldAppendAccountId() {
        WechatOAuthService service = new WechatOAuthService(
                "wx-test-appid-123",
                "secret-123",
                "https://saas.shuao.cc",
                "https://saas.shuao.cc/app/bind-success",
                "https://saas.shuao.cc/patient-portal-home",
                "https://saas.shuao.cc/staff-portal-home",
                "https://saas.shuao.cc/admin-report-h5");

        String url = service.buildStaffHomeUrl(23L);

        assertTrue(url.startsWith("https://saas.shuao.cc/staff-portal-home?accountId=23"), url);
        assertTrue(url.contains("staffToken="), url);
    }
}

package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WechatOAuthServiceTest {

    @Test
    void buildAuthorizeUrlShouldUseInjectedOauthAppId() {
        WechatOAuthService service = new WechatOAuthService(
                "wx-test-appid-123",
                "secret-123",
                "https://saas.shuao.cc",
                "https://saas.shuao.cc/app/bind-success");

        String url = service.buildAuthorizeUrl(1L);

        assertTrue(url.contains("appid=wx-test-appid-123"), url);
        assertTrue(url.contains("scope=snsapi_userinfo"), url);
    }
}

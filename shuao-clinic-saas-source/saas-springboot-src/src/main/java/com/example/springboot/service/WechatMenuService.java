package com.example.springboot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

@Service
public class WechatMenuService {

    private final String appId;
    private final String appSecret;
    private final String bindBaseUrl;
    private final ObjectMapper objectMapper;

    public WechatMenuService(@Value("${wechat.app-id:}") String appId,
                             @Value("${wechat.app-secret:}") String appSecret,
                             @Value("${wechat.bind.base-url:https://saas.shuao.cc}") String bindBaseUrl,
                             ObjectMapper objectMapper) {
        this.appId = safeTrim(appId);
        this.appSecret = safeTrim(appSecret);
        this.bindBaseUrl = trimTrailingSlash(safeTrim(bindBaseUrl).isEmpty() ? "https://saas.shuao.cc" : safeTrim(bindBaseUrl));
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getCurrentMenu() throws IOException {
        String accessToken = fetchAccessToken();
        String response = readUrl("https://api.weixin.qq.com/cgi-bin/get_current_selfmenu_info?access_token=" + encode(accessToken));
        return parseJson(response);
    }

    public Map<String, Object> buildDefaultBottomMenu() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("button", new Object[]{
                buildViewButton("就诊服务", bindBaseUrl + "/patient-portal/entry"),
                buildViewButton("我要挂号", bindBaseUrl + "/patient-register-h5"),
                buildViewButton("医生入口", bindBaseUrl + "/staff-portal/entry")
        });
        return result;
    }

    public Map<String, Object> publishDefaultBottomMenu() throws IOException {
        String accessToken = fetchAccessToken();
        String payload = objectMapper.writeValueAsString(buildDefaultBottomMenu());
        String response = postJson("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + encode(accessToken), payload);
        return parseJson(response);
    }

    private Map<String, Object> buildViewButton(String name, String url) {
        Map<String, Object> button = new LinkedHashMap<>();
        button.put("type", "view");
        button.put("name", name);
        button.put("url", url);
        return button;
    }

    private String fetchAccessToken() throws IOException {
        if (appId.isEmpty() || appSecret.isEmpty()) {
            throw new IOException("微信公众号 app-id/app-secret 未配置");
        }
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                + "&appid=" + encode(appId)
                + "&secret=" + encode(appSecret);
        Map<String, Object> response = parseJson(readUrl(url));
        Object accessToken = response.get("access_token");
        if (accessToken == null || String.valueOf(accessToken).trim().isEmpty()) {
            throw new IOException("获取 access_token 失败: " + response);
        }
        return String.valueOf(accessToken).trim();
    }

    private Map<String, Object> parseJson(String content) throws IOException {
        return objectMapper.readValue(content, new TypeReference<>() {});
    }

    private String readUrl(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("GET");
        try (Scanner scanner = new Scanner(
                connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String postJson(String url, String payload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(bytes);
        }
        try (Scanner scanner = new Scanner(
                connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

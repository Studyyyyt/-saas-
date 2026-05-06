package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.WechatMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/wechat/menu")
public class WechatMenuController {

    private final WechatMenuService wechatMenuService;

    public WechatMenuController(WechatMenuService wechatMenuService) {
        this.wechatMenuService = wechatMenuService;
    }

    @GetMapping("/current")
    public Result current() {
        try {
            return Result.success(wechatMenuService.getCurrentMenu());
        } catch (Exception e) {
            return Result.error("读取公众号菜单失败：" + e.getMessage());
        }
    }

    @GetMapping("/preview")
    public Result preview() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("menu", wechatMenuService.buildDefaultBottomMenu());
        return Result.success(payload);
    }

    @PostMapping("/publish")
    public Result publish() {
        try {
            return Result.success(wechatMenuService.publishDefaultBottomMenu());
        } catch (Exception e) {
            return Result.error("发布公众号菜单失败：" + e.getMessage());
        }
    }
}

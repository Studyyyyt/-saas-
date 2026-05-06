package com.example.springboot.controller;

import com.example.springboot.entity.Patient;
import com.example.springboot.service.WechatOAuthService;
import com.example.springboot.service.WechatPatientBindSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/wechat/portal")
public class WechatPortalController {

    private final WechatPatientBindSceneService wechatPatientBindSceneService;
    private final WechatOAuthService wechatOAuthService;

    @Autowired
    public WechatPortalController(WechatPatientBindSceneService wechatPatientBindSceneService,
                                  WechatOAuthService wechatOAuthService) {
        this.wechatPatientBindSceneService = wechatPatientBindSceneService;
        this.wechatOAuthService = wechatOAuthService;
    }

    @GetMapping
    public RedirectView portal(@RequestParam String openid,
                               @RequestParam String scene) {
        Patient patient = wechatPatientBindSceneService.bindPatientByScene(scene, openid);
        if (patient == null) {
            return new RedirectView("/portal-auth-error?reason=bind");
        }
        return new RedirectView(wechatOAuthService.buildSuccessRedirectUrl((long) patient.getId()));
    }
}

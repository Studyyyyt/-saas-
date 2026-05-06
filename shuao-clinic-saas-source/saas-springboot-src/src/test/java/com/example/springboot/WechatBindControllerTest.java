package com.example.springboot;

import com.example.springboot.controller.WechatBindController;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.WechatOAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WechatBindControllerTest {

    @Test
    void startShouldRedirectToWechatAuthorizeUrl() throws Exception {
        PatientService patientService = mock(PatientService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        WechatBindController controller = new WechatBindController(patientService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        when(patientService.selectById(1L)).thenReturn(List.of(patient));
        when(wechatOAuthService.buildAuthorizeUrl(1L, null)).thenReturn("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx-test&scope=snsapi_userinfo#wechat_redirect");

        mockMvc.perform(get("/wechat/bind/start").param("patientId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://open.weixin.qq.com/**"));
    }

    @Test
    void startShouldRedirectToWechatAuthorizeUrlWithReturnUrl() throws Exception {
        PatientService patientService = mock(PatientService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        WechatBindController controller = new WechatBindController(patientService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Patient patient = new Patient();
        patient.setId(3);
        patient.setName("王五");
        when(patientService.selectById(3L)).thenReturn(List.of(patient));
        when(wechatOAuthService.buildAuthorizeUrl(3L, "https://saas.shuao.cc/patient-portal-home?patientId=3"))
                .thenReturn("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx-test&scope=snsapi_userinfo&state=return-url#wechat_redirect");

        mockMvc.perform(get("/wechat/bind/start")
                        .param("patientId", "3")
                        .param("returnUrl", "https://saas.shuao.cc/patient-portal-home?patientId=3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://open.weixin.qq.com/**"));
    }

    @Test
    void startShouldRedirectToLoginWhenPatientMissing() throws Exception {
        PatientService patientService = mock(PatientService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        WechatBindController controller = new WechatBindController(patientService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(patientService.selectById(999L)).thenReturn(List.of());

        mockMvc.perform(get("/wechat/bind/start").param("patientId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal-auth-error?reason=patient"));
    }

    @Test
    void callbackShouldBindOpenidAndRedirectToSuccessPage() throws Exception {
        PatientService patientService = mock(PatientService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        WechatBindController controller = new WechatBindController(patientService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Patient patient = new Patient();
        patient.setId(2);
        patient.setName("李四");
        patient.setWechat_openid("openid-callback-001");

        when(wechatOAuthService.consumeBindReturnUrl("state-001")).thenReturn("https://saas.shuao.cc/patient-portal-home?patientId=2");
        when(wechatOAuthService.consumeBindState("state-001")).thenReturn(2L);
        when(wechatOAuthService.exchangeCodeForOpenid("code-001")).thenReturn("openid-callback-001");
        when(patientService.bindWechatOpenid(2L, "openid-callback-001")).thenReturn(patient);
        when(wechatOAuthService.buildSuccessRedirectUrl(2L, "https://saas.shuao.cc/patient-portal-home?patientId=2"))
                .thenReturn("https://saas.shuao.cc/patient-portal-home?patientId=2&portalToken=patient-portal-token-002&bindStatus=success");

        mockMvc.perform(get("/wechat/bind/callback")
                        .param("code", "code-001")
                        .param("state", "state-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://saas.shuao.cc/patient-portal-home*"));
    }

    @Test
    void qrcodeShouldReturnPngImage() throws Exception {
        PatientService patientService = mock(PatientService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        WechatBindController controller = new WechatBindController(patientService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/wechat/bind/qrcode")
                        .param("text", "https://saas.shuao.cc/wechat/bind/start?patientId=8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }
}

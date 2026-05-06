package com.example.springboot;

import com.example.springboot.controller.StaffPortalController;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.WechatOAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StaffPortalControllerTest {

    @Test
    void entryShouldRedirectToWechatAuthorizeUrl() throws Exception {
        AccountService accountService = mock(AccountService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        StaffPortalController controller = new StaffPortalController(accountService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(wechatOAuthService.buildStaffAuthorizeUrl())
                .thenReturn("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx-test#wechat_redirect");

        mockMvc.perform(get("/staff-portal/entry"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://open.weixin.qq.com/**"));
    }

    @Test
    void callbackShouldRedirectToUnboundPageWhenOpenidNotMatched() throws Exception {
        AccountService accountService = mock(AccountService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        StaffPortalController controller = new StaffPortalController(accountService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(wechatOAuthService.isStaffPortalState("staff-state-001")).thenReturn(true);
        when(wechatOAuthService.exchangeCodeForOpenid("code-001")).thenReturn("openid-staff-001");
        when(accountService.selectByWechatOpenid("openid-staff-001")).thenReturn(null);

        when(wechatOAuthService.issueStaffBindToken("openid-staff-001")).thenReturn("staff-bind-token-001");

        mockMvc.perform(get("/staff-portal/callback")
                        .param("code", "code-001")
                        .param("state", "staff-state-001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/staff-portal-bind?token=*"));
    }

    @Test
    void callbackShouldRedirectToStaffPortalHomeWhenMatched() throws Exception {
        AccountService accountService = mock(AccountService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        StaffPortalController controller = new StaffPortalController(accountService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Account account = new Account();
        account.setId(8);
        account.setName("王医生");
        account.setRole("doctor");

        when(wechatOAuthService.isStaffPortalState("staff-state-002")).thenReturn(true);
        when(wechatOAuthService.exchangeCodeForOpenid("code-002")).thenReturn("openid-staff-002");
        when(accountService.selectByWechatOpenid("openid-staff-002")).thenReturn(account);
        when(wechatOAuthService.buildStaffHomeUrl(8L))
                .thenReturn("https://saas.shuao.cc/staff-portal-home?accountId=8&staffToken=staff-session-002");

        mockMvc.perform(get("/staff-portal/callback")
                        .param("code", "code-002")
                        .param("state", "staff-state-002"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://saas.shuao.cc/staff-portal-home*"));
    }

    @Test
    void overviewShouldRejectMismatchedStaffToken() {
        AccountService accountService = mock(AccountService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        StaffPortalController controller = new StaffPortalController(accountService, wechatOAuthService);

        Account account = new Account();
        account.setId(8);
        account.setName("王医生");
        account.setRole("doctor");
        account.setUsername("wang");

        when(wechatOAuthService.resolveStaffPortalToken("staff-token-008")).thenReturn(9L);
        when(accountService.selectById(8L)).thenReturn(List.of(account));

        Result result = controller.overview(8L, "staff-token-008");

        assertEquals("500", result.getCode());
        assertEquals("员工身份校验失败，请重新从公众号进入", result.getMsg());
    }
}

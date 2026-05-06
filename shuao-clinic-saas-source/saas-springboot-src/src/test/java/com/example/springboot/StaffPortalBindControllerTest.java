package com.example.springboot;

import com.example.springboot.controller.StaffPortalController;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.WechatOAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StaffPortalBindControllerTest {

    @Test
    void bindShouldPersistOpenidWhenCredentialsMatch() throws Exception {
        AccountService accountService = mock(AccountService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        StaffPortalController controller = new StaffPortalController(accountService, wechatOAuthService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Account account = new Account();
        account.setId(7);
        account.setUsername("doctor01");
        account.setPassword("123456");
        account.setName("王医生");
        account.setRole("doctor");

        when(wechatOAuthService.isStaffPortalState("staff-bind-state")).thenReturn(true);
        when(wechatOAuthService.exchangeCodeForOpenid("code-bind")).thenReturn("openid-staff-bind-001");
        when(wechatOAuthService.consumeStaffBindToken("bind-token-001")).thenReturn("openid-staff-bind-001");
        when(accountService.selectByWechatOpenid("openid-staff-bind-001")).thenReturn(null);
        when(accountService.authenticateByUsernameAndPassword("doctor01", "123456")).thenReturn(account);
        when(accountService.bindWechatOpenid(7L, "openid-staff-bind-001")).thenReturn(account);

        mockMvc.perform(post("/staff-portal/bind")
                        .param("token", "bind-token-001")
                        .contentType("application/json")
                        .content("{\"username\":\"doctor01\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());

        verify(accountService).bindWechatOpenid(7L, "openid-staff-bind-001");
    }
}

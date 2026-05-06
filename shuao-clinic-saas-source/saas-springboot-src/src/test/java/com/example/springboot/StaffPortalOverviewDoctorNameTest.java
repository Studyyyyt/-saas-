package com.example.springboot;

import com.example.springboot.common.Result;
import com.example.springboot.controller.StaffPortalController;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.WechatOAuthService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaffPortalOverviewDoctorNameTest {

    @Test
    void overviewShouldExposeDoctorNameForDoctorAccount() {
        AccountService accountService = mock(AccountService.class);
        WechatOAuthService wechatOAuthService = mock(WechatOAuthService.class);
        StaffPortalController controller = new StaffPortalController(accountService, wechatOAuthService);

        Account account = new Account();
        account.setId(3);
        account.setUsername("doctor01");
        account.setName("王医生");
        account.setRole("doctor");

        when(wechatOAuthService.resolveStaffPortalToken("staff-token-003")).thenReturn(3L);
        when(accountService.selectById(3L)).thenReturn(List.of(account));

        Result result = controller.overview(3L, "staff-token-003");
        Map<?, ?> data = (Map<?, ?>) result.getData();
        Map<?, ?> summary = (Map<?, ?>) data.get("summary");

        assertEquals("王医生", summary.get("doctorName"));
    }
}

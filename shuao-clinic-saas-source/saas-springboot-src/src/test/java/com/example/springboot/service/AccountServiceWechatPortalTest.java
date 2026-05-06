package com.example.springboot.service;

import com.example.springboot.entity.Account;
import com.example.springboot.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceWechatPortalTest {

    private AccountMapper accountMapper;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountMapper = mock(AccountMapper.class);
        accountService = new AccountService();
        ReflectionTestUtils.setField(accountService, "accountMapper", accountMapper);
    }

    @Test
    void bindWechatOpenid_shouldUpdateEmployeeBinding() {
        Account account = new Account();
        account.setId(2);
        account.setName("王医生");
        account.setWechat_openid(null);
        when(accountMapper.selectById(2L)).thenReturn(List.of(account));

        Account updated = accountService.bindWechatOpenid(2L, "openid-employee-001");

        assertEquals("openid-employee-001", updated.getWechat_openid());
        verify(accountMapper).bindWechatOpenid(account);
    }

    @Test
    void bindWechatOpenid_shouldRejectMissingEmployee() {
        when(accountMapper.selectById(99L)).thenReturn(Collections.emptyList());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> accountService.bindWechatOpenid(99L, "openid-employee-404"));

        assertEquals("员工账号不存在", error.getMessage());
    }
}

package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.springboot.entity.Account;
import com.example.springboot.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

class AccountServiceWechatOpenidTrimTest {

    private AccountMapper accountMapper;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountMapper = mock(AccountMapper.class);
        accountService = new AccountService();
        ReflectionTestUtils.setField(accountService, "accountMapper", accountMapper);
    }

    @Test
    void bindWechatOpenid_shouldTrimOpenidBeforePersisting() {
        Account account = new Account();
        account.setId(1);
        account.setUsername("doctor01");
        account.setName("王医生");
        account.setRole("doctor");
        when(accountMapper.selectById(1L)).thenReturn(List.of(account));

        Account result = accountService.bindWechatOpenid(1L, "  openid-abc-123  ");

        assertEquals("openid-abc-123", result.getWechat_openid());
        verify(accountMapper).bindWechatOpenid(account);
    }

    @Test
    void bindWechatOpenid_shouldRejectBlankAccountId() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> accountService.bindWechatOpenid(0L, "openid-abc-123"));

        assertEquals("员工账号不存在", error.getMessage());
    }
}

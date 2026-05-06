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
import static org.mockito.Mockito.when;

class AccountServiceWechatBindingTest {

    private AccountMapper accountMapper;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountMapper = mock(AccountMapper.class);
        accountService = new AccountService();
        ReflectionTestUtils.setField(accountService, "accountMapper", accountMapper);
    }

    @Test
    void bindWechatOpenid_shouldPersistBinding() {
        Account account = new Account();
        account.setId(3);
        account.setUsername("doctor01");
        account.setName("王医生");
        account.setRole("doctor");

        when(accountMapper.selectById(3L)).thenReturn(List.of(account));

        Account bound = accountService.bindWechatOpenid(3L, "openid-employee-001");

        assertEquals("openid-employee-001", bound.getWechat_openid());
    }

    @Test
    void bindWechatOpenid_shouldRejectMissingAccount() {
        when(accountMapper.selectById(99L)).thenReturn(Collections.emptyList());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> accountService.bindWechatOpenid(99L, "openid-none"));

        assertEquals("员工账号不存在", error.getMessage());
    }
}

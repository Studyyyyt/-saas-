package com.example.springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.springboot.entity.Account;
import com.example.springboot.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

class AccountServiceDoctorNameTest {

    private AccountMapper accountMapper;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountMapper = mock(AccountMapper.class);
        accountService = new AccountService();
        ReflectionTestUtils.setField(accountService, "accountMapper", accountMapper);
    }

    @Test
    void findDoctorDisplayNameByAccountIdShouldReturnNameForDoctor() {
        Account account = new Account();
        account.setId(3);
        account.setUsername("doctor01");
        account.setName("王医生");
        account.setRole("doctor");
        when(accountMapper.selectById(3L)).thenReturn(List.of(account));

        assertEquals("王医生", accountService.findDoctorDisplayNameByAccountId(3L));
    }

    @Test
    void findDoctorDisplayNameByAccountIdShouldReturnNullForNonDoctor() {
        Account account = new Account();
        account.setId(1);
        account.setUsername("admin");
        account.setName("管理员");
        account.setRole("admin");
        when(accountMapper.selectById(1L)).thenReturn(List.of(account));

        assertNull(accountService.findDoctorDisplayNameByAccountId(1L));
    }
}

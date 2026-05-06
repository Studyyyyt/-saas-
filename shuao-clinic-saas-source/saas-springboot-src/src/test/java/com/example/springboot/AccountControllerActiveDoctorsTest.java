package com.example.springboot;

import com.example.springboot.controller.AccountController;
import com.example.springboot.entity.Account;
import com.example.springboot.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerActiveDoctorsTest {

    @Test
    void activeDoctorsShouldReturnOnlyDoctorAccountsWithNames() throws Exception {
        AccountService accountService = mock(AccountService.class);
        AccountController controller = new AccountController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "accountService", accountService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Account doctor = new Account();
        doctor.setId(3);
        doctor.setUsername("doctor01");
        doctor.setName("王医生");
        doctor.setRole("doctor");

        when(accountService.findActiveDoctorAccounts()).thenReturn(List.of(doctor));

        mockMvc.perform(get("/accounts/doctors/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(3))
                .andExpect(jsonPath("$.data[0].name").value("王医生"))
                .andExpect(jsonPath("$.data[0].role").value("doctor"));
    }
}

package com.example.springboot;

import com.example.springboot.config.SchemaInitializer;
import com.example.springboot.controller.PatientController;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.AccountService;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.PatientWorkbenchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
class PatientControllerWechatBindTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private PatientWorkbenchService patientWorkbenchService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private SchemaInitializer schemaInitializer;

    @Test
    void bindWechatShouldReturnSuccessAndExposeUpdatedOpenid() throws Exception {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patient.setGender("男");
        patient.setPhone("13800000001");

        patient.setWechat_openid("openid-test-001");
        when(patientService.bindWechatOpenid(1L, "openid-test-001")).thenReturn(patient);

        mockMvc.perform(put("/patients/bindWechat")
                        .contentType("application/json")
                        .content("""
                                {"id":1,"wechat_openid":"openid-test-001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.wechat_openid").value("openid-test-001"));
    }
}

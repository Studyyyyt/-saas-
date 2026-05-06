package com.example.springboot.service;

import com.example.springboot.entity.ConsultationPromptFlags;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.ConsultationRecordMapper;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.PatientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultationRecordServicePromptFlagsTest {

    private ConsultationRecordMapper consultationRecordMapper;
    private PatientMapper patientMapper;
    private ConsultationRecordService consultationRecordService;

    @BeforeEach
    void setUp() {
        consultationRecordMapper = mock(ConsultationRecordMapper.class);
        patientMapper = mock(PatientMapper.class);
        consultationRecordService = new ConsultationRecordService();
        ReflectionTestUtils.setField(consultationRecordService, "consultationRecordMapper", consultationRecordMapper);
        ReflectionTestUtils.setField(consultationRecordService, "patientMapper", patientMapper);
        ReflectionTestUtils.setField(consultationRecordService, "financeMapper", mock(FinanceMapper.class));
        ReflectionTestUtils.setField(consultationRecordService, "accountService", mock(AccountService.class));
    }

    @Test
    void matchPatientByPhoneShouldReturnMatchedPatientAndOpenConsultationCount() {
        Patient patient = new Patient();
        patient.setId(12);
        patient.setName("张三");

        when(patientMapper.selectByPhoneExact("13800138000")).thenReturn(List.of(patient));
        when(consultationRecordMapper.countOpenConsultationsByPhone("13800138000")).thenReturn(2);

        ConsultationPromptFlags flags = consultationRecordService.matchPatientByPhone("13800138000");

        assertTrue(flags.isPhoneMatchedPatient());
        assertEquals(12L, flags.getMatchedPatientId());
        assertEquals("张三", flags.getMatchedPatientName());
        assertEquals(2, flags.getOpenConsultationCount());
        assertTrue(flags.isPhoneHasOpenConsultation());
    }

    @Test
    void matchPatientByPhoneShouldReturnEmptyFlagsWhenPhoneBlank() {
        ConsultationPromptFlags flags = consultationRecordService.matchPatientByPhone(" ");

        assertFalse(flags.isPhoneMatchedPatient());
        assertFalse(flags.isPhoneHasOpenConsultation());
        assertEquals(0, flags.getOpenConsultationCount());
    }
}

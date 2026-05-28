package com.example.springboot.service;

import com.example.springboot.config.OpenAiAnalysisProperties;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Finance;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Treatment;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.BusinessDailyAnalysisMapper;
import com.example.springboot.mapper.FinanceMapper;
import com.example.springboot.mapper.MedicalRecordMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.TreatmentMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessDailyAnalysisServiceTest {

    @Test
    void runDailyAnalysisShouldFallbackToRuleBasedWhenOpenAiDisabled() {
        BusinessDailyAnalysisMapper analysisMapper = mock(BusinessDailyAnalysisMapper.class);
        AppointmentMapper appointmentMapper = mock(AppointmentMapper.class);
        FinanceMapper financeMapper = mock(FinanceMapper.class);
        MedicalRecordMapper medicalRecordMapper = mock(MedicalRecordMapper.class);
        TreatmentMapper treatmentMapper = mock(TreatmentMapper.class);
        PatientMapper patientMapper = mock(PatientMapper.class);

        OpenAiAnalysisProperties properties = new OpenAiAnalysisProperties();
        properties.setEnabled(false);

        BusinessDailyAnalysisService service = new BusinessDailyAnalysisService(
                analysisMapper,
                appointmentMapper,
                financeMapper,
                medicalRecordMapper,
                treatmentMapper,
                patientMapper,
                properties,
                mock(com.example.springboot.service.AiModelProviderService.class),
                new ObjectMapper()
        );

        LocalDate targetDate = LocalDate.of(2026, 4, 25);
        Appointment appointment = new Appointment();
        appointment.setPatient_id(1L);
        appointment.setPatient_name("张三");
        appointment.setAppointment_date(Date.valueOf(targetDate));
        appointment.setAppointment_time("10:00");
        appointment.setDoctor_name("王医生");
        appointment.setStatus("待治疗");
        appointment.setAppointment_purpose("洁牙");

        Finance finance = new Finance();
        finance.setPatient_id(1L);
        finance.setType("收入");
        finance.setAmount(520);
        finance.setDate(targetDate.toString());

        MedicalRecord record = new MedicalRecord();
        record.setPatient_id(1L);
        record.setPatient_name("张三");
        record.setDoctor_name("王医生");
        record.setVisit_date(java.sql.Date.valueOf(targetDate));
        record.setTreatment("洁牙");

        Treatment treatment = new Treatment();
        treatment.setPatient_id(1L);
        treatment.setPatient_name("张三");
        treatment.setDoctor_name("王医生");
        treatment.setTreatment_date(java.sql.Date.valueOf(targetDate));
        treatment.setTreatment_fee("520");
        treatment.setStatus("完成");
        treatment.setAppointment_purpose("洁牙");

        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");

        when(analysisMapper.selectByAnalysisDate(any())).thenReturn(null);
        when(appointmentMapper.selectAll()).thenReturn(List.of(appointment));
        when(medicalRecordMapper.selectAll()).thenReturn(List.of(record));
        when(treatmentMapper.selectAll()).thenReturn(List.of(treatment));
        when(patientMapper.selectAll()).thenReturn(List.of(patient));
        when(financeMapper.getFinanceBydate(eq(targetDate.toString()))).thenReturn(List.of(finance));
        when(financeMapper.getFinancesByMonth(eq(2026), eq(4))).thenReturn(List.of(finance));
        when(financeMapper.getFinancesByMonth(eq(2026), eq(3))).thenReturn(List.of());

        Map<String, Object> result = service.runDailyAnalysis(targetDate, "MANUAL");

        assertNotNull(result);
        assertEquals("FALLBACK", result.get("analysis_status"));
        assertEquals("RULE_BASED", result.get("source_type"));
        assertEquals("MANUAL", result.get("trigger_type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) result.get("metrics");
        assertNotNull(metrics);
        assertEquals(1, ((Number) metrics.get("today_appointments")).intValue());
        assertEquals(520.0, ((Number) metrics.get("today_income")).doubleValue());

        verify(analysisMapper).insert(any());
    }

    @Test
    void testModelConnectionShouldReturnDisabledWhenOpenAiNotEnabled() {
        OpenAiAnalysisProperties properties = new OpenAiAnalysisProperties();
        properties.setEnabled(false);

        BusinessDailyAnalysisService service = new BusinessDailyAnalysisService(
                mock(BusinessDailyAnalysisMapper.class),
                mock(AppointmentMapper.class),
                mock(FinanceMapper.class),
                mock(MedicalRecordMapper.class),
                mock(TreatmentMapper.class),
                mock(PatientMapper.class),
                properties,
                mock(com.example.springboot.service.AiModelProviderService.class),
                new ObjectMapper()
        );

        Map<String, Object> result = service.testModelConnection();

        assertNotNull(result);
        assertEquals(false, result.get("connected"));
        assertEquals("OPENAI_ENABLED 未开启", result.get("message"));
    }
}

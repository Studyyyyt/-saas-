package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.PatientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PatientServiceConsultationLinkTest {

    private PatientMapper patientMapper;
    private ConsultationRecordService consultationRecordService;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientMapper = mock(PatientMapper.class);
        consultationRecordService = mock(ConsultationRecordService.class);
        patientService = new PatientService(patientMapper);
        ReflectionTestUtils.setField(patientService, "consultationRecordService", consultationRecordService);
    }

    @Test
    void addPatientShouldLinkConsultationRecordAfterArchiveCreated() {
        Patient patient = new Patient();
        patient.setName("李四");
        patient.setPhone("13800138001");
        patient.setCustomer_source("微信");
        patient.setConsultation_record_id(18L);

        doAnswer(invocation -> {
            Patient saved = invocation.getArgument(0);
            saved.setId(77);
            return null;
        }).when(patientMapper).addPatient(any(Patient.class));

        patientService.addPatient(patient);

        verify(consultationRecordService).linkPatientForArchiveCreate(eq(18L), eq(77L), eq("微信"));
    }
}

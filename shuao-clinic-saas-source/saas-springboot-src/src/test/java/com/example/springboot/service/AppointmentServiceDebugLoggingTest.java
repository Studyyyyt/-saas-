package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceDebugLoggingTest {

    @Test
    void addAppointmentShouldReachWechatLookupPath() throws Exception {
        AppointmentMapper appointmentMapper = mock(AppointmentMapper.class);
        PatientMapper patientMapper = mock(PatientMapper.class);
        WechatService wechatService = new WechatService("mock", "", "", "");

        AppointmentService service = new AppointmentService();
        inject(service, "appointmentMapper", appointmentMapper);
        inject(service, "patientMapper", patientMapper);
        inject(service, "wechatService", wechatService);

        Appointment appointment = new Appointment();
        appointment.setPatient_id(1L);
        appointment.setPatient_name("张三");
        appointment.setAppointment_date(Date.valueOf("2026-05-01"));
        appointment.setAppointment_time(Time.valueOf("23:10:00"));
        appointment.setDoctor_name("李医生");
        appointment.setAppointment_purpose("最终验证B7");
        appointment.setStatus("待治疗");

        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patient.setWechat_openid("openid-123");

        when(patientMapper.selectById(1L)).thenReturn(List.of(patient));
        doNothing().when(appointmentMapper).insert(any(Appointment.class));

        service.addAppointment(appointment);

        verify(patientMapper).selectById(1L);
        verify(appointmentMapper).insert(appointment);
    }

    private void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

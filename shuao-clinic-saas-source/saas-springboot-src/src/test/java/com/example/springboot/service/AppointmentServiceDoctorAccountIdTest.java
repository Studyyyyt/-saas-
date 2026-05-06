package com.example.springboot.service;

import com.example.springboot.entity.Account;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceDoctorAccountIdTest {

    private AppointmentMapper appointmentMapper;
    private PatientMapper patientMapper;
    private WechatService wechatService;
    private AccountService accountService;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentMapper = mock(AppointmentMapper.class);
        patientMapper = mock(PatientMapper.class);
        wechatService = mock(WechatService.class);
        accountService = mock(AccountService.class);
        appointmentService = new AppointmentService();
        ReflectionTestUtils.setField(appointmentService, "appointmentMapper", appointmentMapper);
        ReflectionTestUtils.setField(appointmentService, "patientMapper", patientMapper);
        ReflectionTestUtils.setField(appointmentService, "wechatService", wechatService);
        ReflectionTestUtils.setField(appointmentService, "accountService", accountService);
    }

    @Test
    void addAppointmentShouldResolveDoctorNameFromDoctorAccountId() {
        Appointment appointment = new Appointment();
        appointment.setPatient_id(9L);
        appointment.setPatient_name("张三");
        appointment.setAppointment_date(Date.valueOf("2026-04-25"));
        appointment.setAppointment_time(Time.valueOf("10:00:00"));
        appointment.setDoctor_account_id(3L);
        appointment.setAppointment_purpose("洁牙");
        appointment.setStatus(" 待治疗 ");

        Patient patient = new Patient();
        patient.setId(9);
        patient.setName("张三");

        when(patientMapper.selectById(9L)).thenReturn(List.of(patient));
        when(accountService.findDoctorDisplayNameByAccountId(3L)).thenReturn(" 王医生 ");

        appointmentService.addAppointment(appointment);

        assertEquals(3L, appointment.getDoctor_account_id());
        assertEquals("王医生", appointment.getDoctor_name());
        assertEquals("待治疗", appointment.getStatus());
        verify(appointmentMapper).insert(appointment);
    }

    @Test
    void addAppointmentShouldRejectMissingDoctorAccountId() {
        Appointment appointment = new Appointment();
        appointment.setPatient_id(9L);
        appointment.setPatient_name("张三");
        appointment.setAppointment_date(Date.valueOf("2026-04-25"));
        appointment.setAppointment_time(Time.valueOf("10:00:00"));
        appointment.setAppointment_purpose("洁牙");

        Patient patient = new Patient();
        patient.setId(9);
        patient.setName("张三");

        when(patientMapper.selectById(9L)).thenReturn(List.of(patient));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.addAppointment(appointment));

        assertEquals("医生账号必填", ex.getMessage());
        verify(accountService, never()).findDoctorDisplayNameByAccountId(any());
        verify(appointmentMapper, never()).insert(any());
    }

    @Test
    void addAppointmentShouldRejectNonDoctorAccount() {
        Appointment appointment = new Appointment();
        appointment.setPatient_id(9L);
        appointment.setPatient_name("张三");
        appointment.setAppointment_date(Date.valueOf("2026-04-25"));
        appointment.setAppointment_time(Time.valueOf("10:00:00"));
        appointment.setDoctor_account_id(5L);
        appointment.setAppointment_purpose("洁牙");

        Patient patient = new Patient();
        patient.setId(9);
        patient.setName("张三");

        when(patientMapper.selectById(9L)).thenReturn(List.of(patient));
        when(accountService.findDoctorDisplayNameByAccountId(5L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.addAppointment(appointment));

        assertEquals("医生账号不存在或未启用", ex.getMessage());
        verify(appointmentMapper, never()).insert(any());
    }

    @Test
    void selectByDoctorNameShouldReturnOnlyMatchingAppointments() {
        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setPatient_name("张三");
        a1.setDoctor_name("王医生");
        a1.setDoctor_account_id(3L);
        a1.setAppointment_date(Date.valueOf("2026-04-22"));
        a1.setAppointment_time(Time.valueOf("10:00:00"));
        a1.setAppointment_purpose("洁牙");

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setPatient_name("李四");
        a2.setDoctor_name("李医生");
        a2.setDoctor_account_id(4L);
        a2.setAppointment_date(Date.valueOf("2026-04-22"));
        a2.setAppointment_time(Time.valueOf("11:00:00"));
        a2.setAppointment_purpose("补牙");

        when(appointmentMapper.selectAll()).thenReturn(List.of(a1, a2));

        List<Appointment> result = appointmentService.selectByDoctorName("王医生");

        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getPatient_name());
    }
}

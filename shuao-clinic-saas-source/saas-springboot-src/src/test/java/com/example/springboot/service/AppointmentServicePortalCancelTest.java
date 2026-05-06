package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServicePortalCancelTest {

    private AppointmentMapper appointmentMapper;
    private PatientMapper patientMapper;
    private WechatService wechatService;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentMapper = mock(AppointmentMapper.class);
        patientMapper = mock(PatientMapper.class);
        wechatService = new WechatService("mock", "", "", "");

        appointmentService = new AppointmentService();
        ReflectionTestUtils.setField(appointmentService, "appointmentMapper", appointmentMapper);
        ReflectionTestUtils.setField(appointmentService, "patientMapper", patientMapper);
        ReflectionTestUtils.setField(appointmentService, "wechatService", wechatService);
    }

    @Test
    void cancelPatientAppointment_updatesStatusAndDedicatedCancelReason() {
        Appointment existing = buildAppointment(1, "待治疗", "洁牙");
        when(appointmentMapper.selectById(1L)).thenReturn(List.of(existing));
        doNothing().when(appointmentMapper).update(any(Appointment.class));

        Appointment cancelled = appointmentService.cancelPatientAppointment(1L, "临时有事");

        assertEquals("已取消", cancelled.getStatus());
        assertEquals("洁牙", cancelled.getAppointment_purpose());
        assertEquals("临时有事", cancelled.getCancel_reason());
        verify(appointmentMapper).update(any(Appointment.class));
    }

    @Test
    void cancelPatientAppointment_usesDefaultReasonWhenBlank() {
        Appointment existing = buildAppointment(9, "待治疗", "复查");
        when(appointmentMapper.selectById(9L)).thenReturn(List.of(existing));
        doNothing().when(appointmentMapper).update(any(Appointment.class));

        Appointment cancelled = appointmentService.cancelPatientAppointment(9L, "   ");

        assertEquals("患者计划变更", cancelled.getCancel_reason());
        assertEquals("复查", cancelled.getAppointment_purpose());
    }

    @Test
    void cancelPatientAppointment_rejectsCompletedAppointment() {
        Appointment existing = buildAppointment(2, "已完成", "复诊");
        when(appointmentMapper.selectById(2L)).thenReturn(List.of(existing));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancelPatientAppointment(2L, "不来了"));

        assertEquals("当前预约状态不可取消", error.getMessage());
        verify(appointmentMapper, never()).update(any(Appointment.class));
    }

    @Test
    void selectPatientAppointmentsByPatientId_sortsLatestFirst() {
        Appointment first = buildAppointment(1, "待治疗", "项目A");
        first.setAppointment_date(Date.valueOf("2026-04-22"));
        first.setAppointment_time(Time.valueOf("10:30:00"));

        Appointment second = buildAppointment(2, "待治疗", "项目B");
        second.setAppointment_date(Date.valueOf("2026-04-25"));
        second.setAppointment_time(Time.valueOf("09:00:00"));

        Appointment third = buildAppointment(3, "待治疗", "项目C");
        third.setAppointment_date(Date.valueOf("2026-04-25"));
        third.setAppointment_time(Time.valueOf("15:00:00"));

        when(appointmentMapper.selectByPatientReference(7L)).thenReturn(List.of(first, second, third));

        List<Appointment> ordered = appointmentService.selectPatientAppointments(7L);

        assertEquals(List.of(3, 2, 1), ordered.stream().map(Appointment::getId).toList());
    }

    private Appointment buildAppointment(int id, String status, String purpose) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setPatient_name("张三");
        appointment.setAppointment_date(Date.valueOf("2026-04-22"));
        appointment.setAppointment_time(Time.valueOf("10:30:00"));
        appointment.setDoctor_name("李医生");
        appointment.setAppointment_purpose(purpose);
        appointment.setCancel_reason(null);
        appointment.setStatus(status);
        return appointment;
    }
}

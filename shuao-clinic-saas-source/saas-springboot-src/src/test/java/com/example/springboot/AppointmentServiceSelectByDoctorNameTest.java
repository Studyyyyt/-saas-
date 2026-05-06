package com.example.springboot;

import com.example.springboot.entity.Appointment;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.service.AppointmentService;
import com.example.springboot.service.WechatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppointmentServiceSelectByDoctorNameTest {

    private AppointmentMapper appointmentMapper;
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentMapper = mock(AppointmentMapper.class);
        appointmentService = new AppointmentService();
        ReflectionTestUtils.setField(appointmentService, "appointmentMapper", appointmentMapper);
        ReflectionTestUtils.setField(appointmentService, "patientMapper", mock(PatientMapper.class));
        ReflectionTestUtils.setField(appointmentService, "wechatService", mock(WechatService.class));
    }

    @Test
    void selectByDoctorNameShouldReturnOnlyMatchingAppointments() {
        Appointment a1 = new Appointment();
        a1.setId(1);
        a1.setPatient_name("张三");
        a1.setDoctor_name("王医生");
        a1.setAppointment_date(Date.valueOf("2026-04-22"));
        a1.setAppointment_time(Time.valueOf("10:00:00"));
        a1.setAppointment_purpose("洁牙");

        Appointment a2 = new Appointment();
        a2.setId(2);
        a2.setPatient_name("李四");
        a2.setDoctor_name("李医生");
        a2.setAppointment_date(Date.valueOf("2026-04-22"));
        a2.setAppointment_time(Time.valueOf("11:00:00"));
        a2.setAppointment_purpose("补牙");

        when(appointmentMapper.selectAll()).thenReturn(List.of(a1, a2));

        List<Appointment> result = appointmentService.selectByDoctorName("王医生");

        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getPatient_name());
    }
}

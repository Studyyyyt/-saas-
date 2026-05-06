package com.example.springboot;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.AppointmentService;
import com.example.springboot.service.WechatService;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.PatientMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceWechatNotificationTest {

    private final String originalAppId = System.getProperty("wechat.app-id");
    private final String originalAppSecret = System.getProperty("wechat.app-secret");
    private final String originalTemplateId = System.getProperty("wechat.template.appointment-created");
    private final String originalMode = System.getProperty("wechat.delivery-mode");

    @AfterEach
    void restoreProperties() {
        restore("wechat.app-id", originalAppId);
        restore("wechat.app-secret", originalAppSecret);
        restore("wechat.template.appointment-created", originalTemplateId);
        restore("wechat.delivery-mode", originalMode);
    }

    @Test
    void addAppointmentShouldNotifyWechatBoundPatientAfterInsert() {
        AppointmentMapper appointmentMapper = mock(AppointmentMapper.class);
        PatientMapper patientMapper = mock(PatientMapper.class);
        WechatService wechatService = mock(WechatService.class);

        AppointmentService service = new AppointmentService();
        serviceTestSupport(service, appointmentMapper, patientMapper, wechatService);

        Appointment appointment = new Appointment();
        appointment.setPatient_id(1L);
        appointment.setPatient_name("张三");
        appointment.setDoctor_name("王医生");
        appointment.setAppointment_purpose("复诊");
        appointment.setAppointment_date(Date.valueOf("2026-04-25"));
        appointment.setAppointment_time(Time.valueOf("10:00:00"));

        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patient.setWechat_openid("openid-123");

        when(patientMapper.selectById(1L)).thenReturn(List.of(patient));
        doNothing().when(appointmentMapper).insert(any(Appointment.class));
        doNothing().when(wechatService).sendAppointmentCreatedNotification(any(Patient.class), any(Appointment.class));

        service.addAppointment(appointment);

        verify(appointmentMapper).insert(appointment);
        verify(wechatService).sendAppointmentCreatedNotification(patient, appointment);
    }

    @Test
    void addAppointmentShouldSkipWechatNotificationWhenPatientNotBound() {
        AppointmentMapper appointmentMapper = mock(AppointmentMapper.class);
        PatientMapper patientMapper = mock(PatientMapper.class);
        WechatService wechatService = mock(WechatService.class);

        AppointmentService service = new AppointmentService();
        serviceTestSupport(service, appointmentMapper, patientMapper, wechatService);

        Appointment appointment = new Appointment();
        appointment.setPatient_id(2L);
        appointment.setPatient_name("李四");
        appointment.setDoctor_name("王医生");
        appointment.setAppointment_purpose("洁牙");
        appointment.setAppointment_date(Date.valueOf("2026-04-25"));
        appointment.setAppointment_time(Time.valueOf("11:00:00"));

        Patient patient = new Patient();
        patient.setId(2);
        patient.setName("李四");
        patient.setWechat_openid(null);

        when(patientMapper.selectById(2L)).thenReturn(List.of(patient));
        doNothing().when(appointmentMapper).insert(any(Appointment.class));

        service.addAppointment(appointment);

        verify(appointmentMapper).insert(appointment);
        verify(wechatService, never()).sendAppointmentCreatedNotification(any(Patient.class), any(Appointment.class));
    }

    @Test
    void realWechatServiceShouldFallbackSafelyWhenConfiguredButUnavailable() {
        System.setProperty("wechat.app-id", "wx-app-id");
        System.setProperty("wechat.app-secret", "wx-secret");
        System.setProperty("wechat.template.appointment-created", "template-001");
        System.setProperty("wechat.delivery-mode", "real");

        WechatService wechatService = new WechatService("real", "wx-app-id", "wx-secret", "template-001");
        Patient patient = new Patient();
        patient.setId(3);
        patient.setName("王五");
        patient.setWechat_openid("openid-real-001");

        Appointment appointment = new Appointment();
        appointment.setPatient_name("王五");
        appointment.setDoctor_name("赵医生");
        appointment.setAppointment_purpose("检查");
        appointment.setAppointment_date(Date.valueOf("2026-04-26"));
        appointment.setAppointment_time(Time.valueOf("09:30:00"));

        assertDoesNotThrow(() -> wechatService.sendAppointmentCreatedNotification(patient, appointment));
        assertEquals("real", readWechatField(wechatService, "deliveryMode"));
        assertEquals("wx-app-id", readWechatField(wechatService, "appId"));
    }

    private void serviceTestSupport(AppointmentService service,
                                    AppointmentMapper appointmentMapper,
                                    PatientMapper patientMapper,
                                    WechatService wechatService) {
        try {
            var appointmentMapperField = AppointmentService.class.getDeclaredField("appointmentMapper");
            appointmentMapperField.setAccessible(true);
            appointmentMapperField.set(service, appointmentMapper);

            var patientMapperField = AppointmentService.class.getDeclaredField("patientMapper");
            patientMapperField.setAccessible(true);
            patientMapperField.set(service, patientMapper);

            var wechatServiceField = AppointmentService.class.getDeclaredField("wechatService");
            wechatServiceField.setAccessible(true);
            wechatServiceField.set(service, wechatService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object readWechatField(WechatService service, String fieldName) {
        try {
            var field = WechatService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void restore(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}

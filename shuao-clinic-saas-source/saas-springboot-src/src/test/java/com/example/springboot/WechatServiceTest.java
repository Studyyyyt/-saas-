package com.example.springboot;

import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.WechatService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WechatServiceTest {

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
    void shouldStayInMockModeWhenWechatPropertiesMissing() throws Exception {
        System.clearProperty("wechat.app-id");
        System.clearProperty("wechat.app-secret");
        System.clearProperty("wechat.template.appointment-created");
        System.clearProperty("wechat.delivery-mode");

        WechatService service = new WechatService("mock", "", "", "");
        Patient patient = samplePatient();
        Appointment appointment = sampleAppointment();

        assertDoesNotThrow(() -> service.sendAppointmentCreatedNotification(patient, appointment));
        assertEquals("mock", readField(service, "deliveryMode"));
    }

    @Test
    void shouldReadRealModeWechatPropertiesWhenConfigured() throws Exception {
        System.setProperty("wechat.app-id", "wx-app-id");
        System.setProperty("wechat.app-secret", "wx-secret");
        System.setProperty("wechat.template.appointment-created", "template-001");
        System.setProperty("wechat.delivery-mode", "real");

        WechatService service = new WechatService("real", "wx-app-id", "wx-secret", "template-001");

        assertEquals("real", readField(service, "deliveryMode"));
        assertEquals("wx-app-id", readField(service, "appId"));
        assertEquals("wx-secret", readField(service, "appSecret"));
        assertEquals("template-001", readField(service, "appointmentCreatedTemplateId"));
    }

    private Patient samplePatient() {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("张三");
        patient.setWechat_openid("openid-123");
        return patient;
    }

    private Appointment sampleAppointment() {
        Appointment appointment = new Appointment();
        appointment.setPatient_name("张三");
        appointment.setDoctor_name("王医生");
        appointment.setAppointment_purpose("复诊");
        appointment.setAppointment_date(Date.valueOf("2026-04-25"));
        appointment.setAppointment_time(Time.valueOf("10:00:00"));
        return appointment;
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private void restore(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}

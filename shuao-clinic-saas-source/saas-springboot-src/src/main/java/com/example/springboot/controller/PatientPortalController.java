package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.MedicalRecord;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.PatientConsent;
import com.example.springboot.entity.PatientConsentSignRequest;
import com.example.springboot.entity.PatientImage;
import com.example.springboot.service.MedicalRecordService;
import com.example.springboot.service.PatientConsentService;
import com.example.springboot.service.PatientImageService;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.AppointmentService;
import com.example.springboot.service.WechatOAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patient-portal")
public class PatientPortalController {

    private final PatientService patientService;
    private final MedicalRecordService medicalRecordService;
    private final PatientImageService patientImageService;
    private final AppointmentService appointmentService;
    private final PatientConsentService patientConsentService;
    private final WechatOAuthService wechatOAuthService;

    public PatientPortalController(PatientService patientService,
                                   MedicalRecordService medicalRecordService,
                                   PatientImageService patientImageService,
                                   AppointmentService appointmentService,
                                   PatientConsentService patientConsentService,
                                   WechatOAuthService wechatOAuthService) {
        this.patientService = patientService;
        this.medicalRecordService = medicalRecordService;
        this.patientImageService = patientImageService;
        this.appointmentService = appointmentService;
        this.patientConsentService = patientConsentService;
        this.wechatOAuthService = wechatOAuthService;
    }

    @GetMapping("/entry")
    public RedirectView entry() {
        return new RedirectView(wechatOAuthService.buildPortalAuthorizeUrl());
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam String code, @RequestParam String state) {
        if (!wechatOAuthService.isPortalState(state)) {
            return new RedirectView("/portal-auth-error");
        }
        String openid = wechatOAuthService.exchangeCodeForOpenid(code);
        Patient patient = patientService.selectByWechatOpenid(openid);
        if (patient == null) {
            return new RedirectView("/portal-auth-error?reason=unbound");
        }
        return new RedirectView(wechatOAuthService.buildPortalHomeUrl(Long.valueOf(patient.getId())));
    }

    @GetMapping("/overview")
    public Result overview(@RequestParam(required = false) Long patientId,
                           @RequestParam String portalToken) {
        Patient patient = resolveAuthorizedPatient(patientId, portalToken);
        if (patient == null) {
            return Result.error("患者身份校验失败，请重新从公众号进入");
        }
        Long resolvedPatientId = Long.valueOf(patient.getId());

        List<Appointment> appointments = appointmentService.selectPatientAppointments(resolvedPatientId);
        List<MedicalRecord> records = medicalRecordService.selectByPatientId(resolvedPatientId);
        List<PatientImage> images = patientImageService.selectSentByPatientId(resolvedPatientId);
        List<PatientConsent> consents = patientConsentService.selectByPatientId(resolvedPatientId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patient", patient);
        result.put("appointments", appointments.stream()
                .sorted(Comparator.comparing(Appointment::getAppointment_date, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Appointment::getAppointment_time, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList()));
        result.put("records", records);
        result.put("images", images);
        result.put("consents", consents);
        return Result.success(result);
    }

    @GetMapping("/consents/{id}")
    public Result consentDetail(@PathVariable Long id,
                                @RequestParam String portalToken) {
        try {
            Patient patient = resolveAuthorizedPatient(null, portalToken);
            if (patient == null) {
                return Result.error("患者身份校验失败，请重新从公众号进入");
            }
            return Result.success(patientConsentService.markRead(id, Long.valueOf(patient.getId())));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/consents/{id}/sign")
    public Result signConsent(@PathVariable Long id,
                              @RequestParam String portalToken,
                              @RequestBody PatientConsentSignRequest request) {
        try {
            Patient patient = resolveAuthorizedPatient(null, portalToken);
            if (patient == null) {
                return Result.error("患者身份校验失败，请重新从公众号进入");
            }
            return Result.success(patientConsentService.sign(id, Long.valueOf(patient.getId()), request));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public Result cancelAppointment(@PathVariable Long id,
                                    @RequestParam String portalToken,
                                    @RequestBody(required = false) Map<String, String> payload) {
        try {
            Patient patient = resolveAuthorizedPatient(null, portalToken);
            if (patient == null) {
                return Result.error("患者身份校验失败，请重新从公众号进入");
            }
            Appointment appointment = resolveAuthorizedAppointment(id, patient);
            if (appointment == null) {
                return Result.error("预约不存在或无权操作");
            }
            String reason = payload == null ? null : payload.get("reason");
            Appointment cancelled = appointmentService.cancelPatientAppointment(id, reason);
            return Result.success(cancelled);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/appointments/{id}/edit")
    public Result editAppointment(@PathVariable Long id,
                                  @RequestParam String portalToken,
                                  @RequestBody Appointment payload) {
        try {
            Patient patient = resolveAuthorizedPatient(null, portalToken);
            if (patient == null) {
                return Result.error("患者身份校验失败，请重新从公众号进入");
            }
            Appointment appointment = resolveAuthorizedAppointment(id, patient);
            if (appointment == null) {
                return Result.error("预约不存在或无权操作");
            }
            if (payload == null) {
                return Result.error("预约信息不能为空");
            }

            appointment.setAppointment_date(payload.getAppointment_date());
            appointment.setAppointment_time(payload.getAppointment_time());
            appointment.setAppointment_purpose(payload.getAppointment_purpose());
            appointmentService.editAppointment(appointment);
            return Result.success(appointment);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    private Patient resolveAuthorizedPatient(Long patientId, String portalToken) {
        Long authorizedPatientId = wechatOAuthService.resolvePatientPortalToken(portalToken);
        if (authorizedPatientId == null) {
            return null;
        }
        if (patientId != null && !authorizedPatientId.equals(patientId)) {
            return null;
        }
        List<Patient> patients = patientService.selectById(authorizedPatientId);
        if (patients == null || patients.isEmpty()) {
            return null;
        }
        return patients.get(0);
    }

    private Appointment resolveAuthorizedAppointment(Long appointmentId, Patient patient) {
        if (appointmentId == null || appointmentId <= 0 || patient == null) {
            return null;
        }
        List<Appointment> appointments = appointmentService.selectById(appointmentId);
        if (appointments == null || appointments.isEmpty()) {
            return null;
        }
        Appointment appointment = appointments.get(0);
        if (appointment.getPatient_id() == null || appointment.getPatient_id() <= 0) {
            return null;
        }
        return appointment.getPatient_id().equals(Long.valueOf(patient.getId())) ? appointment : null;
    }
}

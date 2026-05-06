package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.PatientConsent;
import com.example.springboot.service.PatientConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/patient-consent")
public class PatientConsentController {

    @Autowired
    private PatientConsentService patientConsentService;

    @GetMapping("/selectByPatientId")
    public Result selectByPatientId(@RequestParam Long patientId) {
        return Result.success(patientConsentService.selectByPatientId(patientId));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        PatientConsent consent = patientConsentService.selectById(id);
        if (consent == null) {
            return Result.error("知情同意书不存在");
        }
        return Result.success(consent);
    }

    @PostMapping("/issue")
    public Result issue(@RequestBody PatientConsent consent) {
        try {
            return Result.success(patientConsentService.issue(consent));
        } catch (IllegalArgumentException exception) {
            return Result.error(exception.getMessage());
        }
    }
}

package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.PatientInsightSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patient-insights")
public class PatientInsightController {

    @Autowired
    private PatientInsightSummaryService patientInsightSummaryService;

    @GetMapping("/overview")
    public Result overview() {
        return Result.success(patientInsightSummaryService.buildOverview());
    }
}

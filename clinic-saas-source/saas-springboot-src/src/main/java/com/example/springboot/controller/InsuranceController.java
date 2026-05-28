package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.*;
import com.example.springboot.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/insurances")
public class InsuranceController {

    @Autowired
    private InsuranceService insuranceService;

    @GetMapping("/overview")
    public Result overview() {
        return Result.success(insuranceService.getOverview());
    }

    @GetMapping("/config")
    public Result getConfig() {
        return Result.success(insuranceService.getConfig());
    }

    @PostMapping("/config")
    public Result saveConfig(@RequestBody InsuranceConfig config) {
        try {
            return Result.success(insuranceService.saveConfig(config));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/patient-profile/{patientId}")
    public Result getPatientProfile(@PathVariable Long patientId) {
        try {
            return Result.success(insuranceService.getPatientProfile(patientId));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/patient-profile")
    public Result savePatientProfile(@RequestBody InsurancePatientProfile profile) {
        try {
            return Result.success(insuranceService.savePatientProfile(profile));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/settlements")
    public Result getSettlements(@RequestParam(required = false) Long patientId) {
        return Result.success(insuranceService.getSettlements(patientId));
    }

    @PostMapping("/settlements/draft")
    public Result createSettlementDraft(@RequestBody InsuranceSettlement settlement) {
        try {
            return Result.success(insuranceService.createSettlementDraft(settlement));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/logs")
    public Result getLogs(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(insuranceService.getRecentLogs(limit));
    }

    @PostMapping("/mock/settlement-payload")
    public Result buildMockSettlementPayload(@RequestBody Map<String, Object> payload) {
        try {
            Long patientId = payload.get("patientId") == null ? null : Long.valueOf(String.valueOf(payload.get("patientId")));
            Long treatmentCatalogId = payload.get("treatmentCatalogId") == null ? null : Long.valueOf(String.valueOf(payload.get("treatmentCatalogId")));
            Double totalAmount = payload.get("totalAmount") == null ? null : Double.valueOf(String.valueOf(payload.get("totalAmount")));
            return Result.success(insuranceService.buildMockSettlementPayload(patientId, treatmentCatalogId, totalAmount));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}

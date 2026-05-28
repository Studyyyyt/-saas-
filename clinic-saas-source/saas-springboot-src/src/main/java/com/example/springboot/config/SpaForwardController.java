package com.example.springboot.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/Home",
            "/home",
            "/inventory",
            "/Patient",
            "/Person",
            "/MedicalRecord",
            "/PatientDetail",
            "/Appointment",
            "/Appointment2",
            "/Treatment",
            "/Treatment2",
            "/Financial",
            "/Financial2",
            "/FinancialExpense",
            "/financial-expenses",
            "/Doctor",
            "/Account",
            "/Inventory",
            "/Inventory2",
            "/Inventory3",
            "/TreatmentCatalog",
            "/InsuranceOverview",
            "/InsuranceConfig",
            "/InsurancePatientProfile",
            "/InsuranceSettlement",
            "/InsuranceLog",
            "/InsuranceMockPayload",
            "/BusinessAnalysis",
            "/SystemTreatmentCatalog",
            "/SystemTreatmentOperation",
            "/SystemConsentTemplate",
            "/SystemAccountPermission",
            "/SystemAccountManage",
            "/SystemSettings",
            "/SystemAIAgentConfig",
            "/SystemModelProviderConfig",
            "/Purchase",
            "/Doctors",
            "/Accounts",
            "/Followup",
            "/advertising-spending",
            "/RiskTags",
            "/login1",
            "/register",
            "/app/bind-success",
            "/appointment-notice",
            "/patient-register-h5",
            "/patient-portal-home",
            "/patient-portal-section",
            "/portal-auth-error",
            "/staff-portal-home",
            "/staff-h5/appointments",
            "/staff-h5/consultations",
            "/staff-h5/patients",
            "/staff-h5/patientDetail",
            "/staff-h5/records",
            "/staff-h5/finance",
            "/staff-h5/inventory",
            "/staff-portal-bind",
            "/staff-portal-auth-error",
            "/admin-report-h5",
            "/patient-details",
            "/followups",
            "/purchases",
            "/insurances",
            "/treatment-plans"
    })
    public String forward() {
        return "forward:/index.html";
    }

    @GetMapping("/SystemSettings/ai/pages/config/{agentKey}")
    public String forwardAgentConfig() {
        return "forward:/index.html";
    }
}

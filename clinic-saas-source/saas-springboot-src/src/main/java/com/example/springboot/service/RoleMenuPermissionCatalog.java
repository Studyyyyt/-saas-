package com.example.springboot.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RoleMenuPermissionCatalog {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_DOCTOR = "doctor";
    public static final String ROLE_NURSE = "nurse";

    public static final List<String> ROLE_CODES = List.of(ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE);

    private static final List<MenuItem> MENU_ITEMS = List.of(
            item("/home", "首页概览", "通用", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/Patient", "患者列表", "通用", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/MedicalRecord", "病历工作台", "通用", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/Followup", "回访管理", "通用", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/Consultation", "咨询记录", "咨询管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/ConsultationDashboard", "咨询看板", "咨询管理", ROLE_ADMIN, ROLE_NURSE),
            item("/advertising-spending", "广告投放", "市场投放", ROLE_ADMIN, ROLE_NURSE),
            item("/lab-factories", "加工厂/产品库", "义齿加工", ROLE_ADMIN),
            item("/lab-orders", "加工订单", "义齿加工", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/lab-bills", "月度账单", "义齿加工", ROLE_ADMIN, ROLE_NURSE),
            item("/lab-statistics", "加工统计", "义齿加工", ROLE_ADMIN),
            item("/material-categories", "耗材分类", "耗材管理", ROLE_ADMIN),
            item("/materials", "耗材档案", "耗材管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/material-purchases", "采购记录", "耗材管理", ROLE_ADMIN, ROLE_NURSE),
            item("/material-statistics", "耗材统计", "耗材管理", ROLE_ADMIN),
            item("/Appointment", "预约工作台", "接诊与排班", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/Appointment2", "预约列表", "接诊与排班", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/Doctor", "医生排班", "接诊与排班", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/Financial", "财务信息", "财务管理", ROLE_ADMIN, ROLE_NURSE),
            item("/Financial2", "财务分析", "财务管理", ROLE_ADMIN, ROLE_NURSE),
            item("/financial-expenses", "财务支出", "财务管理", ROLE_ADMIN, ROLE_NURSE),
            item("/BusinessAnalysis", "AI经营日报", "财务管理", ROLE_ADMIN, ROLE_NURSE),
            item("/InsuranceOverview", "医保总览", "医保管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/InsuranceConfig", "医保配置", "医保管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/InsurancePatientProfile", "患者医保档案", "医保管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/InsuranceSettlement", "医保结算", "医保管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/InsuranceLog", "医保日志", "医保管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/InsuranceMockPayload", "mock报文", "医保管理", ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE),
            item("/SystemTreatmentCatalog", "项目库", "系统设置", ROLE_ADMIN, ROLE_NURSE),
            item("/SystemTreatmentOperation", "操作字典", "系统设置", ROLE_ADMIN, ROLE_NURSE),
            item("/SystemPaymentChannel", "收款渠道", "系统设置", ROLE_ADMIN, ROLE_NURSE),
            item("/SystemConsentTemplate", "知情同意书库", "系统设置", ROLE_ADMIN, ROLE_NURSE),
            item("/SystemAccountPermission", "账号权限", "系统设置", ROLE_ADMIN),
            item("/SystemAccountManage", "账号管理", "系统设置", ROLE_ADMIN)
    );

    private RoleMenuPermissionCatalog() {
    }

    public static List<MenuItem> items() {
        return MENU_ITEMS;
    }

    public static boolean containsMenuKey(String menuKey) {
        return MENU_ITEMS.stream().anyMatch(item -> item.menuKey().equals(menuKey));
    }

    public static Map<String, Boolean> buildDefaultRolePermissions(String roleCode) {
        String normalizedRole = normalizeRole(roleCode);
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (MenuItem item : MENU_ITEMS) {
            result.put(item.menuKey(), item.defaultRoles().contains(normalizedRole));
        }
        if (ROLE_ADMIN.equals(normalizedRole)) {
            result.put("/SystemAccountPermission", true);
        }
        return result;
    }

    public static String normalizeRole(String roleCode) {
        if (roleCode == null) {
            return "";
        }
        String normalized = roleCode.trim();
        if ("管理员".equals(normalized)) return ROLE_ADMIN;
        if ("医生".equals(normalized)) return ROLE_DOCTOR;
        if ("护士".equals(normalized)) return ROLE_NURSE;
        return normalized;
    }

    public record MenuItem(String menuKey, String menuLabel, String groupLabel, List<String> defaultRoles) {
    }

    private static MenuItem item(String key, String label, String group, String... roles) {
        return new MenuItem(key, label, group, List.of(roles));
    }
}

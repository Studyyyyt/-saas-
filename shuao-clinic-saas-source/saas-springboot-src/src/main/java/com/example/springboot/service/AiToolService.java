package com.example.springboot.service;

import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Function Calling 工具服务层
 * 为 AI Agent 提供数据库查询能力
 */
@Service
public class AiToolService {

    private final PatientMapper patientMapper;
    private final AppointmentMapper appointmentMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final TreatmentMapper treatmentMapper;
    private final FinanceMapper financeMapper;
    private final LabOrderMapper labOrderMapper;
    private final MaterialMapper materialMapper;

    public AiToolService(PatientMapper patientMapper,
                         AppointmentMapper appointmentMapper,
                         MedicalRecordMapper medicalRecordMapper,
                         TreatmentMapper treatmentMapper,
                         FinanceMapper financeMapper,
                         LabOrderMapper labOrderMapper,
                         MaterialMapper materialMapper) {
        this.patientMapper = patientMapper;
        this.appointmentMapper = appointmentMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.treatmentMapper = treatmentMapper;
        this.financeMapper = financeMapper;
        this.labOrderMapper = labOrderMapper;
        this.materialMapper = materialMapper;
    }

    // ==================== 患者查询工具 ====================

    public String queryPatients(Map<String, Object> params) {
        String keyword = params != null ? (String) params.get("keyword") : null;
        String action = params != null ? (String) params.get("action") : null;

        if ("count".equals(action)) {
            List<Patient> all = patientMapper.selectAll();
            return "患者总数：" + all.size() + "人";
        }

        List<Patient> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = patientMapper.searchByKeyword(keyword.trim());
        } else {
            list = patientMapper.selectAll();
            // 限制返回数量，避免token爆炸
            if (list.size() > 20) {
                list = list.subList(0, 20);
            }
        }

        if (list.isEmpty()) {
            return "未找到符合条件的患者。";
        }

        StringBuilder sb = new StringBuilder("找到 " + list.size() + " 位患者：\n");
        for (Patient p : list) {
            sb.append("- ").append(p.getName())
              .append("(").append(p.getGender() != null ? p.getGender() : "未知")
              .append(", ").append(p.getAge() != null ? p.getAge() + "岁" : "年龄未知")
              .append(", 电话:").append(p.getPhone() != null ? p.getPhone() : "无")
              .append(", 来源:").append(p.getCustomer_source() != null ? p.getCustomer_source() : "未知")
              .append(")\n");
        }
        if (list.size() >= 20) {
            sb.append("（仅展示前20位，如需更多请缩小查询范围）");
        }
        return sb.toString();
    }

    // ==================== 预约查询工具 ====================

    public String queryAppointments(Map<String, Object> params) {
        String date = params != null ? (String) params.get("date") : null;
        String status = params != null ? (String) params.get("status") : null;
        String doctor = params != null ? (String) params.get("doctor") : null;
        String action = params != null ? (String) params.get("action") : null;

        List<Appointment> all = appointmentMapper.selectAll();
        List<Appointment> filtered = all.stream().filter(a -> {
            boolean match = true;
            if (date != null && !date.isEmpty()) {
                String aDate = a.getAppointment_date() != null ?
                    new java.sql.Date(a.getAppointment_date().getTime()).toString() : "";
                match = match && aDate.equals(date);
            }
            if (status != null && !status.isEmpty()) {
                match = match && status.equals(a.getStatus());
            }
            if (doctor != null && !doctor.isEmpty()) {
                match = match && doctor.equals(a.getDoctor_name());
            }
            return match;
        }).collect(Collectors.toList());

        if ("count".equals(action)) {
            return "符合条件的预约数：" + filtered.size() + "条";
        }

        if (filtered.isEmpty()) {
            return "未找到符合条件的预约。";
        }

        // 按日期排序
        filtered.sort(Comparator.comparing(Appointment::getAppointment_date, Comparator.nullsLast(Comparator.naturalOrder())));

        StringBuilder sb = new StringBuilder("找到 " + filtered.size() + " 条预约：\n");
        int limit = Math.min(filtered.size(), 15);
        for (int i = 0; i < limit; i++) {
            Appointment a = filtered.get(i);
            sb.append("- ").append(a.getAppointment_date() != null ? a.getAppointment_date() : "")
              .append(" ").append(a.getAppointment_time() != null ? a.getAppointment_time() : "")
              .append(" | ").append(a.getPatient_name())
              .append(" | ").append(a.getDoctor_name() != null ? a.getDoctor_name() : "未指定医生")
              .append(" | ").append(a.getAppointment_purpose() != null ? a.getAppointment_purpose() : "未填写项目")
              .append(" | 状态:").append(a.getStatus() != null ? a.getStatus() : "未知")
              .append("\n");
        }
        if (filtered.size() > 15) {
            sb.append("（仅展示前15条，共" + filtered.size() + "条）");
        }
        return sb.toString();
    }

    // ==================== 病历查询工具 ====================

    public String queryMedicalRecords(Map<String, Object> params) {
        String patientName = params != null ? (String) params.get("patient_name") : null;
        String date = params != null ? (String) params.get("date") : null;
        String action = params != null ? (String) params.get("action") : null;

        List<MedicalRecord> list;
        if (patientName != null && !patientName.trim().isEmpty()) {
            list = medicalRecordMapper.selectByPatientName(patientName.trim());
        } else {
            list = medicalRecordMapper.selectAll();
        }

        if (date != null && !date.isEmpty()) {
            final String targetDate = date;
            list = list.stream().filter(r -> {
                if (r.getVisit_date() == null) return false;
                String vDate = new java.sql.Date(r.getVisit_date().getTime()).toString();
                return vDate.equals(targetDate);
            }).collect(Collectors.toList());
        }

        if ("count".equals(action)) {
            return "符合条件的病历数：" + list.size() + "份";
        }

        if (list.isEmpty()) {
            return "未找到符合条件的病历。";
        }

        StringBuilder sb = new StringBuilder("找到 " + list.size() + " 份病历：\n");
        int limit = Math.min(list.size(), 10);
        for (int i = 0; i < limit; i++) {
            MedicalRecord r = list.get(i);
            sb.append("- ").append(r.getVisit_date() != null ? r.getVisit_date() : "")
              .append(" | ").append(r.getPatient_name())
              .append(" | ").append(r.getDoctor_name() != null ? r.getDoctor_name() : "")
              .append(" | 主诉:").append(r.getChief_complaint() != null ? r.getChief_complaint() : "未填写")
              .append(" | 诊断:").append(r.getDiagnosis() != null ? r.getDiagnosis() : "未填写")
              .append("\n");
        }
        if (list.size() > 10) {
            sb.append("（仅展示前10份）");
        }
        return sb.toString();
    }

    // ==================== 财务查询工具 ====================

    public String queryFinances(Map<String, Object> params) {
        String date = params != null ? (String) params.get("date") : null;
        String month = params != null ? (String) params.get("month") : null;
        String type = params != null ? (String) params.get("type") : null;
        String action = params != null ? (String) params.get("action") : null;

        List<Finance> list;
        if (date != null && !date.isEmpty()) {
            list = financeMapper.getFinanceBydate(date);
        } else if (month != null && !month.isEmpty()) {
            try {
                String[] parts = month.split("-");
                int year = Integer.parseInt(parts[0]);
                int mon = Integer.parseInt(parts[1]);
                list = financeMapper.getFinancesByMonth(year, mon);
            } catch (Exception e) {
                list = financeMapper.getAllFinances();
            }
        } else {
            list = financeMapper.getAllFinances();
        }

        if (type != null && !type.isEmpty()) {
            final String targetType = type;
            list = list.stream().filter(f -> targetType.equals(f.getType())).collect(Collectors.toList());
        }

        if ("sum".equals(action) || "total".equals(action)) {
            double total = list.stream().mapToDouble(f -> f.getAmount()).sum();
            return "符合条件的总金额：¥" + String.format("%.2f", total);
        }

        if (list.isEmpty()) {
            return "未找到符合条件的财务记录。";
        }

        StringBuilder sb = new StringBuilder("找到 " + list.size() + " 条财务记录：\n");
        int limit = Math.min(list.size(), 15);
        for (int i = 0; i < limit; i++) {
            Finance f = list.get(i);
            sb.append("- ").append(f.getDate())
              .append(" | ").append(f.getName())
              .append(" | ¥").append(String.format("%.2f", f.getAmount()))
              .append(" | ").append(f.getType() != null ? f.getType() : "")
              .append(" | ").append(f.getBiz_type() != null ? f.getBiz_type() : "")
              .append("\n");
        }
        if (list.size() > 15) {
            sb.append("（仅展示前15条）");
        }
        return sb.toString();
    }

    // ==================== 治疗查询工具 ====================

    public String queryTreatments(Map<String, Object> params) {
        String patientName = params != null ? (String) params.get("patient_name") : null;
        String date = params != null ? (String) params.get("date") : null;
        String action = params != null ? (String) params.get("action") : null;

        List<Treatment> list;
        if (patientName != null && !patientName.trim().isEmpty()) {
            list = treatmentMapper.selectByPatientName(patientName.trim());
        } else {
            list = treatmentMapper.selectAll();
        }

        if (date != null && !date.isEmpty()) {
            final String targetDate = date;
            list = list.stream().filter(t -> {
                if (t.getTreatment_date() == null) return false;
                String tDate = new java.sql.Date(t.getTreatment_date().getTime()).toString();
                return tDate.equals(targetDate);
            }).collect(Collectors.toList());
        }

        if ("count".equals(action)) {
            return "符合条件的治疗记录：" + list.size() + "条";
        }

        if (list.isEmpty()) {
            return "未找到符合条件的治疗记录。";
        }

        StringBuilder sb = new StringBuilder("找到 " + list.size() + " 条治疗记录：\n");
        int limit = Math.min(list.size(), 10);
        for (int i = 0; i < limit; i++) {
            Treatment t = list.get(i);
            sb.append("- ").append(t.getTreatment_date() != null ? t.getTreatment_date() : "")
              .append(" | ").append(t.getPatient_name())
              .append(" | ").append(t.getDoctor_name() != null ? t.getDoctor_name() : "")
              .append(" | ").append(t.getAppointment_purpose() != null ? t.getAppointment_purpose() : "")
              .append(" | ¥").append(t.getCharged_amount() != null ? String.format("%.2f", t.getCharged_amount()) : "0.00")
              .append(" | 状态:").append(t.getBilling_status() != null ? t.getBilling_status() : "")
              .append("\n");
        }
        if (list.size() > 10) {
            sb.append("（仅展示前10条）");
        }
        return sb.toString();
    }

    // ==================== 义齿加工查询工具 ====================

    public String queryLabOrders(Map<String, Object> params) {
        String status = params != null ? (String) params.get("status") : null;
        List<LabOrder> list = labOrderMapper.selectAll();
        if (status != null && !status.isEmpty()) {
            final String targetStatus = status;
            list = list.stream().filter(l -> targetStatus.equals(l.getStatus())).collect(Collectors.toList());
        }
        if (list.isEmpty()) {
            return "未找到符合条件的加工单。";
        }
        StringBuilder sb = new StringBuilder("找到 " + list.size() + " 条加工单：\n");
        int limit = Math.min(list.size(), 10);
        for (int i = 0; i < limit; i++) {
            LabOrder l = list.get(i);
            sb.append("- ").append(l.getPatient_name())
              .append(" | ").append(l.getProduct_name())
              .append(" | 工厂:").append(l.getFactory_name())
              .append(" | 状态:").append(l.getStatus())
              .append("\n");
        }
        return sb.toString();
    }

    // ==================== 耗材查询工具 ====================

    public String queryMaterials(Map<String, Object> params) {
        List<Material> list = materialMapper.selectAll();
        if (list.isEmpty()) {
            return "未找到耗材记录。";
        }
        StringBuilder sb = new StringBuilder("耗材库存概况（共" + list.size() + "种）：\n");
        int limit = Math.min(list.size(), 10);
        for (int i = 0; i < limit; i++) {
            Material m = list.get(i);
            sb.append("- ").append(m.getName())
              .append(" | 库存:").append(m.getCurrent_stock() != null ? m.getCurrent_stock() : 0)
              .append(" | 预警值:").append(m.getMin_stock_alert() != null ? m.getMin_stock_alert() : 0)
              .append("\n");
        }
        if (list.size() > 10) {
            sb.append("（仅展示前10种）");
        }
        return sb.toString();
    }

    // ==================== 工具定义（供OpenAI Function Calling使用）====================

    public Map<String, Object> buildToolDefinitions(List<String> enabledTools) {
        Map<String, Object> tools = new LinkedHashMap<>();
        if (enabledTools == null || enabledTools.isEmpty()) {
            enabledTools = Arrays.asList("query_patients", "query_appointments", "query_finances", "query_medical_records", "query_treatments", "query_lab_orders", "query_materials");
        }
        for (String tool : enabledTools) {
            switch (tool) {
                case "query_patients":
                    tools.put("query_patients", Map.of(
                        "name", "query_patients",
                        "description", "查询患者信息，可按关键词搜索或统计总数",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "keyword", Map.of("type", "string", "description", "患者姓名关键词，支持模糊搜索"),
                                "action", Map.of("type", "string", "description", "操作类型：count统计总数，search搜索列表")
                            )
                        )
                    ));
                    break;
                case "query_appointments":
                    tools.put("query_appointments", Map.of(
                        "name", "query_appointments",
                        "description", "查询预约信息，支持按日期、状态、医生筛选",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "date", Map.of("type", "string", "description", "预约日期，格式 yyyy-MM-dd"),
                                "status", Map.of("type", "string", "description", "预约状态：待确认/已确认/已完成/已取消等"),
                                "doctor", Map.of("type", "string", "description", "医生姓名"),
                                "action", Map.of("type", "string", "description", "操作类型：count统计数量")
                            )
                        )
                    ));
                    break;
                case "query_medical_records":
                    tools.put("query_medical_records", Map.of(
                        "name", "query_medical_records",
                        "description", "查询病历信息，可按患者姓名或就诊日期筛选",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "patient_name", Map.of("type", "string", "description", "患者姓名"),
                                "date", Map.of("type", "string", "description", "就诊日期，格式 yyyy-MM-dd"),
                                "action", Map.of("type", "string", "description", "操作类型：count统计数量")
                            )
                        )
                    ));
                    break;
                case "query_finances":
                    tools.put("query_finances", Map.of(
                        "name", "query_finances",
                        "description", "查询财务流水，支持按日期、月份、类型筛选",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "date", Map.of("type", "string", "description", "具体日期，格式 yyyy-MM-dd"),
                                "month", Map.of("type", "string", "description", "月份，格式 yyyy-MM"),
                                "type", Map.of("type", "string", "description", "类型：收入/支出"),
                                "action", Map.of("type", "string", "description", "操作类型：sum计算总金额")
                            )
                        )
                    ));
                    break;
                case "query_treatments":
                    tools.put("query_treatments", Map.of(
                        "name", "query_treatments",
                        "description", "查询治疗/处置记录",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "patient_name", Map.of("type", "string", "description", "患者姓名"),
                                "date", Map.of("type", "string", "description", "日期，格式 yyyy-MM-dd"),
                                "action", Map.of("type", "string", "description", "操作类型：count统计数量")
                            )
                        )
                    ));
                    break;
                case "query_lab_orders":
                    tools.put("query_lab_orders", Map.of(
                        "name", "query_lab_orders",
                        "description", "查询义齿加工单",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "status", Map.of("type", "string", "description", "状态：待加工/加工中/已完成等")
                            )
                        )
                    ));
                    break;
                case "query_materials":
                    tools.put("query_materials", Map.of(
                        "name", "query_materials",
                        "description", "查询耗材库存",
                        "parameters", Map.of(
                            "type", "object",
                            "properties", Map.of()
                        )
                    ));
                    break;
            }
        }
        return tools;
    }

    public String executeTool(String toolName, Map<String, Object> arguments) {
        if (arguments == null) arguments = new LinkedHashMap<>();
        switch (toolName) {
            case "query_patients": return queryPatients(arguments);
            case "query_appointments": return queryAppointments(arguments);
            case "query_medical_records": return queryMedicalRecords(arguments);
            case "query_finances": return queryFinances(arguments);
            case "query_treatments": return queryTreatments(arguments);
            case "query_lab_orders": return queryLabOrders(arguments);
            case "query_materials": return queryMaterials(arguments);
            default: return "未知工具：" + toolName;
        }
    }
}

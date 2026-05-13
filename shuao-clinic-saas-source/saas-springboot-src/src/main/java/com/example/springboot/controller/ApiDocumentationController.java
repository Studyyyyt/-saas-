package com.example.springboot.controller;

import com.example.springboot.common.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API 文档接口
 * 列出所有可供外部工作流调用的业务 API
 */
@CrossOrigin(origins = "http://localhost:7070")
@RestController
@RequestMapping("/api/docs")
public class ApiDocumentationController {

    @GetMapping
    public Result getApiDocs() {
        Map<String, Object> docData = new LinkedHashMap<>();
        docData.put("title", "口腔门诊 SaaS 系统业务 API 文档");
        docData.put("version", "1.0.0");
        docData.put("baseUrl", "/");
        docData.put("apis", buildApiList());
        return Result.success(docData);
    }

    private List<Map<String, Object>> buildApiList() {
        List<Map<String, Object>> apis = new ArrayList<>();

        // ==================== 患者管理 ====================
        apis.add(buildApi(
                "患者管理",
                "查询患者列表",
                "GET",
                "/patients/search",
                Arrays.asList(
                        param("keyword", "string", false, "姓名/手机号关键词，支持模糊搜索"),
                        param("page", "int", false, "页码，默认 1"),
                        param("size", "int", false, "每页条数，默认 20")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"total\":1,\"list\":[{\"id\":1,\"name\":\"张三\",\"gender\":\"男\",\"age\":30,\"phone\":\"13800138000\"}]}}"
        ));
        apis.add(buildApi(
                "患者管理",
                "根据ID查询患者",
                "GET",
                "/patients/selectByid",
                Arrays.asList(
                        param("id", "long", true, "患者ID"),
                        param("page", "int", false, "页码"),
                        param("size", "int", false, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"total\":1,\"list\":[{\"id\":1,\"name\":\"张三\",\"gender\":\"男\",\"age\":30}]}}"
        ));
        apis.add(buildApi(
                "患者管理",
                "患者工作台查询",
                "GET",
                "/patients/workbench",
                Arrays.asList(
                        param("keyword", "string", false, "关键词"),
                        param("customerSource", "string", false, "客户来源"),
                        param("startDate", "string", false, "开始日期 yyyy-MM-dd"),
                        param("endDate", "string", false, "结束日期 yyyy-MM-dd")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":[{\"id\":1,\"name\":\"张三\",\"phone\":\"13800138000\"}]}"
        ));
        apis.add(buildApi(
                "患者管理",
                "新增患者",
                "POST",
                "/patients/add",
                Arrays.asList(
                        param("body", "Patient", true, "患者实体JSON，含 name/gender/age/phone 等必填字段")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"id\":1,\"name\":\"张三\"}}"
        ));

        // ==================== 病历管理 ====================
        apis.add(buildApi(
                "病历管理",
                "查询病历列表",
                "GET",
                "/medical-records/selectAll",
                Arrays.asList(
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数"),
                        param("doctorAccountId", "long", false, "医生账号ID过滤"),
                        param("recordStatus", "string", false, "病历状态"),
                        param("startDate", "string", false, "开始日期"),
                        param("endDate", "string", false, "结束日期")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"chief_complaint\":\"牙痛\",\"diagnosis\":\"龋齿\"}]}}"
        ));
        apis.add(buildApi(
                "病历管理",
                "根据患者ID查询病历",
                "GET",
                "/medical-records/selectByPatientId",
                Arrays.asList(
                        param("patientId", "long", true, "患者ID"),
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"visit_date\":\"2025-01-01\",\"chief_complaint\":\"牙痛\"}]}}"
        ));
        apis.add(buildApi(
                "病历管理",
                "新增病历",
                "POST",
                "/medical-records/add",
                Arrays.asList(
                        param("body", "MedicalRecord", true, "病历实体JSON，含 patient_id/doctor_name/chief_complaint 等")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"id\":1,\"patient_name\":\"张三\"}}"
        ));

        // ==================== 预约管理 ====================
        apis.add(buildApi(
                "预约管理",
                "查询预约列表",
                "GET",
                "/appointments/selectAll",
                Arrays.asList(
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数"),
                        param("status", "string", false, "预约状态：待确认/已确认/已完成/已取消")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"appointment_time\":\"2025-01-01 09:00\",\"status\":\"已确认\"}]}}"
        ));
        apis.add(buildApi(
                "预约管理",
                "查询日程条目",
                "GET",
                "/appointments/scheduleEntries",
                Arrays.asList(),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":[{\"id\":1,\"patient_name\":\"张三\",\"appointment_time\":\"2025-01-01 09:00\"}]}"
        ));
        apis.add(buildApi(
                "预约管理",
                "新增预约",
                "POST",
                "/appointments/add",
                Arrays.asList(
                        param("body", "Appointment", true, "预约实体JSON，含 patient_name/appointment_time/doctor_name 等必填字段")
                ),
                "{\"code\":\"200\",\"msg\":\"新增成功\",\"data\":null}"
        ));
        apis.add(buildApi(
                "预约管理",
                "取消预约",
                "POST",
                "/appointments/cancel/{id}",
                Arrays.asList(
                        param("id", "long", true, "预约ID"),
                        param("body.reason", "string", false, "取消原因")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"id\":1,\"status\":\"已取消\"}}"
        ));

        // ==================== 财务管理 ====================
        apis.add(buildApi(
                "财务管理",
                "查询所有财务记录",
                "GET",
                "/finances/all",
                Arrays.asList(
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"item_name\":\"补牙\",\"amount\":200.0}]}}"
        ));
        apis.add(buildApi(
                "财务管理",
                "按日期查询财务",
                "GET",
                "/finances/selectBydate",
                Arrays.asList(
                        param("date", "string", true, "日期 yyyy-MM-dd"),
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"amount\":200.0}]}}"
        ));
        apis.add(buildApi(
                "财务管理",
                "医生业绩统计",
                "GET",
                "/finances/doctorPerformance",
                Arrays.asList(
                        param("startDate", "string", false, "开始日期"),
                        param("endDate", "string", false, "结束日期"),
                        param("doctorAccountId", "long", false, "医生账号ID"),
                        param("doctorName", "string", false, "医生姓名")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":[{\"doctor_name\":\"李医生\",\"total_amount\":5000.0,\"patient_count\":20}]}"
        ));
        apis.add(buildApi(
                "财务管理",
                "费用概览",
                "GET",
                "/finances/expenseOverview",
                Arrays.asList(
                        param("startDate", "string", false, "开始日期"),
                        param("endDate", "string", false, "结束日期")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"totalIncome\":10000.0,\"totalExpense\":3000.0,\"netProfit\":7000.0}}"
        ));

        // ==================== 治疗管理 ====================
        apis.add(buildApi(
                "治疗管理",
                "查询治疗记录列表",
                "GET",
                "/treatments/selectAll",
                Arrays.asList(
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"treatment_content\":\"根管治疗\",\"tooth_positions\":\"16\"}]}}"
        ));
        apis.add(buildApi(
                "治疗管理",
                "根据患者ID查询最近治疗",
                "GET",
                "/treatments/recentByPatientId",
                Arrays.asList(
                        param("patientId", "long", true, "患者ID"),
                        param("limit", "int", false, "返回条数，默认 10")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":[{\"id\":1,\"treatment_date\":\"2025-01-01\",\"treatment_content\":\"根管治疗\"}]}"
        ));
        apis.add(buildApi(
                "治疗管理",
                "新增治疗记录",
                "POST",
                "/treatments/add",
                Arrays.asList(
                        param("body", "Treatment", true, "治疗实体JSON，含 patient_id/patient_name/treatment_date/doctor_name 等必填字段")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"id\":1,\"patient_name\":\"张三\"}}"
        ));

        // ==================== 加工单 ====================
        apis.add(buildApi(
                "加工单",
                "搜索加工单",
                "GET",
                "/lab-orders/search",
                Arrays.asList(
                        param("keyword", "string", false, "关键词：患者姓名、加工内容、加工单号"),
                        param("factoryId", "long", false, "加工厂ID"),
                        param("status", "string", false, "状态"),
                        param("patientId", "long", false, "患者ID"),
                        param("startDate", "string", false, "开始日期"),
                        param("endDate", "string", false, "结束日期"),
                        param("page", "int", false, "页码，默认 1"),
                        param("size", "int", false, "每页条数，默认 20")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"total\":1,\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"factory_name\":\"A加工厂\",\"lab_content\":\"烤瓷牙\",\"status\":\"制作中\"}]}}"
        ));
        apis.add(buildApi(
                "加工单",
                "加工单概览",
                "GET",
                "/lab-orders/dashboard/overview",
                Arrays.asList(
                        param("keyword", "string", false, "关键词"),
                        param("factoryId", "long", false, "加工厂ID"),
                        param("status", "string", false, "状态"),
                        param("patientId", "long", false, "患者ID"),
                        param("startDate", "string", false, "开始日期"),
                        param("endDate", "string", false, "结束日期")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"totalCount\":10,\"pendingCount\":3,\"completedCount\":7,\"totalAmount\":5000.0}}"
        ));
        apis.add(buildApi(
                "加工单",
                "新增加工单",
                "POST",
                "/lab-orders/add",
                Arrays.asList(
                        param("body", "LabOrder", true, "加工单实体JSON")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"id\":1,\"patient_name\":\"张三\"}}"
        ));

        // ==================== 耗材 ====================
        apis.add(buildApi(
                "耗材",
                "搜索耗材",
                "GET",
                "/materials/search",
                Arrays.asList(
                        param("keyword", "string", false, "耗材名称/编号关键词"),
                        param("categoryId", "long", false, "分类ID"),
                        param("lowStockOnly", "boolean", false, "仅显示低库存"),
                        param("status", "string", false, "状态"),
                        param("page", "int", false, "页码，默认 1"),
                        param("size", "int", false, "每页条数，默认 20")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"total\":1,\"list\":[{\"id\":1,\"name\":\"树脂材料\",\"code\":\"RZ001\",\"stock_quantity\":50,\"warning_threshold\":10}]}}"
        ));
        apis.add(buildApi(
                "耗材",
                "查询低库存耗材",
                "GET",
                "/materials/search",
                Arrays.asList(
                        param("lowStockOnly", "boolean", true, "传 true 仅显示低库存"),
                        param("page", "int", false, "页码"),
                        param("size", "int", false, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"total\":1,\"list\":[{\"id\":1,\"name\":\"树脂材料\",\"stock_quantity\":5,\"warning_threshold\":10}]}}"
        ));
        apis.add(buildApi(
                "耗材",
                "新增耗材",
                "POST",
                "/materials/add",
                Arrays.asList(
                        param("body", "Material", true, "耗材实体JSON")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"id\":1,\"name\":\"树脂材料\"}}"
        ));

        // ==================== 回访咨询 ====================
        apis.add(buildApi(
                "回访咨询",
                "查询回访记录列表",
                "GET",
                "/followup/selectAll",
                Arrays.asList(
                        param("page", "int", true, "页码"),
                        param("size", "int", true, "每页条数")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"followup_type\":\"术后回访\",\"next_followup_date\":\"2025-01-08\"}]}}"
        ));
        apis.add(buildApi(
                "回访咨询",
                "根据患者ID查询回访",
                "GET",
                "/followup/selectByPatientId",
                Arrays.asList(
                        param("patientId", "long", true, "患者ID")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":[{\"id\":1,\"followup_date\":\"2025-01-01\",\"content\":\"恢复良好\"}]}"
        ));
        apis.add(buildApi(
                "回访咨询",
                "查询咨询记录",
                "GET",
                "/consultations/search",
                Arrays.asList(
                        param("keyword", "string", false, "姓名/手机号关键词"),
                        param("startTime", "string", false, "开始时间"),
                        param("endTime", "string", false, "结束时间"),
                        param("page", "int", false, "页码，默认 1"),
                        param("size", "int", false, "每页条数，默认 20")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"list\":[{\"id\":1,\"patient_name\":\"张三\",\"chief_project\":\"种植牙\",\"intent_level\":\"高\"}]}}"
        ));
        apis.add(buildApi(
                "回访咨询",
                "新增回访记录",
                "POST",
                "/followup/add",
                Arrays.asList(
                        param("body", "PatientFollowup", true, "回访实体JSON，含 patient_id/followup_date/content 等")
                ),
                "{\"code\":\"200\",\"msg\":\"新增成功\",\"data\":null}"
        ));

        // ==================== 患者360 ====================
        apis.add(buildApi(
                "患者360",
                "获取患者360全景视图",
                "GET",
                "/patient360/overview/{patientId}",
                Arrays.asList(
                        param("patientId", "long", true, "患者ID（路径参数）")
                ),
                "{\"code\":\"200\",\"msg\":\"请求成功\",\"data\":{\"patient\":{\"id\":1,\"name\":\"张三\"},\"visitCount\":5,\"totalFee\":3000.0,\"records\":[],\"treatments\":[],\"appointments\":[]}}"
        ));

        return apis;
    }

    private Map<String, Object> buildApi(String category, String name, String method, String path,
                                          List<Map<String, Object>> params, String responseExample) {
        Map<String, Object> api = new LinkedHashMap<>();
        api.put("category", category);
        api.put("name", name);
        api.put("method", method);
        api.put("path", path);
        api.put("params", params);
        api.put("responseExample", responseExample);
        return api;
    }

    private Map<String, Object> param(String name, String type, boolean required, String description) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("name", name);
        p.put("type", type);
        p.put("required", required);
        p.put("description", description);
        return p;
    }
}

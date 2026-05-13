package com.example.springboot.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 工具描述服务层
 * 改造后：不再直接查询数据库，仅返回工具描述 Schema（供 API 文档和 MCP 使用）
 */
@Service
public class AiToolService {

    /**
     * 构建工具定义 Schema
     * 供外部工作流平台或 MCP 了解本系统可提供的工具能力
     *
     * @param enabledTools 启用的工具列表，null 或空则返回全部默认工具
     * @return 工具名 -> 工具描述 Schema
     */
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
}

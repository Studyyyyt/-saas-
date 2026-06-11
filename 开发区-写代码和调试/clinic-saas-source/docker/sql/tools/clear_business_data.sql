-- ============================================
-- 清空业务数据（保留系统配置）
-- 在 Navicat 查询窗口中全选并运行即可
-- ============================================

-- 关闭外键约束检查，避免删除顺序导致报错
SET FOREIGN_KEY_CHECKS = 0;

-- 广告投放
DELETE FROM advertising_spending;

-- AI 相关
DELETE FROM ai_chat_message;
DELETE FROM ai_chat_session;
DELETE FROM ai_few_shot_example;
DELETE FROM ai_function_call_log;
DELETE FROM ai_function_config;
DELETE FROM ai_function_mapping;
DELETE FROM ai_global_config;
DELETE FROM ai_medical_record_summary;
DELETE FROM ai_model_provider;
DELETE FROM ai_patient_risk_assessment;
DELETE FROM ai_prompt_template;

-- 预约
DELETE FROM appointment;

-- 经营分析
DELETE FROM business_alert_log;
DELETE FROM business_daily_analysis;
DELETE FROM business_period_report;

-- 知情同意书
DELETE FROM consent_template;
DELETE FROM patient_consent;

-- 咨询
DELETE FROM consultation_followups;
DELETE FROM consultation_records;

-- 医生相关
DELETE FROM doctor_home_reminder_dismissal;
DELETE FROM doctors;

-- 文件附件
DELETE FROM file_attachment;

-- 财务
DELETE FROM finances;

-- 医保
DELETE FROM insurance_config;
DELETE FROM insurance_operation_log;
DELETE FROM insurance_patient_profile;
DELETE FROM insurance_settlement;

-- 库存
DELETE FROM inventory;

-- 加工单
DELETE FROM lab_bill_items;
DELETE FROM lab_bill_templates;
DELETE FROM lab_bill_unmatched_orders;
DELETE FROM lab_bills;
DELETE FROM lab_factories;
DELETE FROM lab_factory_products;
DELETE FROM lab_orders;

-- 登录日志
DELETE FROM login_log;

-- 耗材
DELETE FROM material_categories;
DELETE FROM material_purchase_items;
DELETE FROM material_purchases;
DELETE FROM materials;

-- 病历
DELETE FROM medical_record_ai_field;
DELETE FROM medical_record_operations;
DELETE FROM medical_record_phrases;
DELETE FROM medical_record_template;
DELETE FROM medical_records;

-- 操作日志
DELETE FROM operation_log;

-- 患者相关
DELETE FROM patient_custom_group;
DELETE FROM patient_custom_group_member;
DELETE FROM patient_followup;
DELETE FROM patient_images;
DELETE FROM patient_insight_summary;
DELETE FROM patient_referral_records;
DELETE FROM patient_risk_tag;
DELETE FROM patient_timeline;
DELETE FROM patient_wechat_bind_scene;
DELETE FROM patients;

-- 收款渠道
DELETE FROM payment_channel;

-- 门户令牌
DELETE FROM portal_access_token;

-- 采购
DELETE FROM purchases;

-- 排班
DELETE FROM shift_template;

-- 治疗
DELETE FROM treatment;
DELETE FROM treatment_catalog;
DELETE FROM treatment_operation_allocations;
DELETE FROM treatment_operations;
DELETE FROM treatment_plans;
DELETE FROM treatment_project_categories;
DELETE FROM treatment_projects;
DELETE FROM treatment_scene;
DELETE FROM treatment_scene_step;
DELETE FROM project_operation_relations;

-- 恢复外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 提示完成
SELECT '所有业务数据已清空，系统配置已保留' AS result;

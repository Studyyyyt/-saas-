-- ============================================================
-- 口腔诊所 SaaS 管理系统 - 全模块测试数据
-- 用途: 供开发测试、功能演示、UI 观察使用
-- 注意: 正式部署时请使用 database_init.sql（不含测试数据）
-- ============================================================

-- 1. 角色定义表
INSERT IGNORE INTO `roles` (`code`, `name`, `description`, `sort_order`, `status`) VALUES
('admin', '管理员', '系统管理员，拥有所有权限', 1, 1),
('doctor', '医生', '门诊医生，负责诊疗工作', 2, 1),
('nurse', '护士', '护士，负责辅助诊疗和护理', 3, 1);

-- 2. 医生表
INSERT IGNORE INTO `doctors` (`doctor_name`, `status`, `shift_type`) VALUES
('王医生', '在职', '全职'),
('李医生', '在职', '全职'),
('张医生', '在职', '兼职'),
('刘医生', '在职', '全职');

-- 3. 患者表
INSERT IGNORE INTO `patients` (`name`, `gender`, `age`, `phone`, `address`, `customer_source`) VALUES
('张三', '男', 35, '13900139001', '北京市朝阳区', '美团'),
('李四', '女', 28, '13900139002', '北京市海淀区', '转介绍'),
('王五', '男', 45, '13900139003', '北京市东城区', '美团'),
('赵六', '女', 52, '13900139004', '北京市西城区', '自然到店'),
('孙七', '男', 18, '13900139005', '北京市丰台区', '抖音');

-- 4. 预约表
INSERT IGNORE INTO `appointment` (`patient_id`, `patient_name`, `appointment_date`, `appointment_time`, `doctor_account_id`, `doctor_name`, `appointment_purpose`, `status`) VALUES
(1, '张三', '2026-06-12', '09:00', 1, '王医生', '复诊-根管治疗', '已确认'),
(2, '李四', '2026-06-12', '10:30', 2, '李医生', '矫正复诊', '已确认'),
(3, '王五', '2026-06-12', '14:00', 4, '刘医生', '种植复查', '已确认'),
(4, '赵六', '2026-06-13', '09:00', 1, '王医生', '牙周治疗', '待确认'),
(5, '孙七', '2026-06-13', '11:00', 3, '张医生', '拔智齿', '已取消');

-- 5. 治疗项目分类表
INSERT IGNORE INTO `treatment_project_categories` (`name`, `sort_order`, `status`) VALUES
('基础治疗', 1, 1),
('修复治疗', 2, 1),
('正畸治疗', 3, 1),
('种植治疗', 4, 1),
('牙周治疗', 5, 1);

-- 6. 治疗项目表
INSERT IGNORE INTO `treatment_projects` (`project_name`, `project_code`, `category_id`, `default_price`, `status`, `remark`) VALUES
('普通洗牙', 'XC001', 1, 200.00, 1, '超声波洁牙'),
('深度洁牙', 'XC002', 1, 500.00, 1, '龈下刮治'),
('树脂补牙', 'XF001', 2, 300.00, 1, '光固化树脂充填'),
('全瓷冠', 'XF002', 2, 3500.00, 1, '二氧化锆全瓷冠'),
('金属托槽矫正', 'ZO001', 3, 15000.00, 1, '传统金属托槽'),
('隐形矫正', 'ZO002', 3, 35000.00, 1, '隐形矫治器'),
('单颗种植', 'ZZ001', 4, 8000.00, 1, '韩国种植体'),
('全口种植', 'ZZ002', 4, 150000.00, 1, 'All-on-4/6');

-- 7. 治疗目录表
INSERT IGNORE INTO `treatment_catalog` (`item_name`, `default_fee`, `default_content`, `default_product`, `status`) VALUES
('口腔检查', 50.00, '常规口腔检查', '检查服务', 1),
('拍片检查', 100.00, '全景片/小牙片', '影像服务', 1),
('CT检查', 300.00, '口腔CT', '影像服务', 1),
('根管治疗', 1200.00, '前牙根管治疗', '治疗服务', 1),
('拔牙', 200.00, '普通拔牙', '治疗服务', 1),
('智齿拔除', 800.00, '阻生智齿拔除', '治疗服务', 1);

-- 8. 病历表
INSERT IGNORE INTO `medical_records` (`patient_id`, `patient_name`, `doctor_account_id`, `doctor_name`, `visit_date`, `chief_complaint`, `diagnosis`, `treatment_plan`, `treatment`, `notes`) VALUES
(1, '张三', 1, '王医生', '2026-06-10', '牙痛3天', '急性牙髓炎', '根管治疗', '开髓引流，缓解疼痛', '预约下次复诊'),
(2, '李四', 2, '李医生', '2026-06-08', '矫正器脱落', '正畸治疗中', '重新粘接托槽', '更换弓丝，调整力度', NULL),
(3, '王五', 4, '刘医生', '2026-06-05', '种植牙复查', '种植术后3个月', '二期手术评估', '愈合良好，准备二期', NULL),
(4, '赵六', 1, '王医生', '2026-06-01', '牙龈出血', '慢性牙周炎', '牙周基础治疗', '洗牙+龈下刮治', '建议3个月复查'),
(5, '孙七', 3, '张医生', '2026-06-10', '智齿疼痛', '智齿冠周炎', '消炎后拔除', '冲洗上药，预约拔牙', NULL);

-- 9. 治疗记录表
INSERT IGNORE INTO `treatment` (`patient_id`, `patient_name`, `doctor_account_id`, `doctor_name`, `treatment_date`, `treatment_content`, `treatment_fee`, `status`) VALUES
(1, '张三', 1, '王医生', '2026-06-10', '开髓引流', 600.00, '进行中'),
(2, '李四', 2, '李医生', '2026-06-08', '复诊调整', 0.00, '进行中'),
(3, '王五', 4, '刘医生', '2026-06-05', '术后复查', 0.00, '已完成'),
(4, '赵六', 1, '王医生', '2026-06-01', '龈下刮治', 500.00, '已完成'),
(5, '孙七', 3, '张医生', '2026-06-10', '消炎处理', 150.00, '进行中');

-- 10. 财务记录表
INSERT IGNORE INTO `finances` (`patient_id`, `name`, `amount`, `date`, `type`, `biz_type`, `remark`) VALUES
(1, '根管治疗', 600.00, '2026-06-10', '收入', '治疗', '首付'),
(2, '矫正复诊', 0.00, '2026-06-08', '收入', '治疗', '包含在套餐内'),
(3, '种植复查', 0.00, '2026-06-05', '收入', '治疗', '术后复查免费'),
(4, '深度洁牙', 500.00, '2026-06-01', '收入', '治疗', NULL),
(5, '消炎处理', 150.00, '2026-06-10', '收入', '治疗', NULL);

-- 11. 库存表（补充）
INSERT IGNORE INTO `inventory` (`product_name`, `category`, `brand`, `supplier`, `specification`, `unit`, `quantity`, `price`) VALUES
('树脂材料', '耗材', '3M', '3M中国', 'Z350', '支', 50, 80.00),
('麻药', '药品', '赛诺菲', '赛诺菲', '斯康杜尼', '支', 30, 25.00),
('牙线', '护理', '宝洁', '宝洁', '欧乐B', '盒', 100, 15.00),
('手套', '耗材', '稳健', '稳健医疗', '一次性乳胶M号', '双', 500, 0.50),
('口罩', '耗材', '稳健', '稳健医疗', '医用外科', '个', 200, 0.30);

-- 12. 耗材表（补充）
INSERT IGNORE INTO `materials` (`name`, `spec`, `brand`, `category_id`, `category_name`, `unit`, `min_stock_alert`, `current_stock`, `status`, `remark`) VALUES
('种植体', '韩国奥齿泰 4.0*10mm', '奥齿泰', 1, '种植类', '颗', 5, 20, 1, '常用型号'),
('基台', '钛合金', '奥齿泰', 1, '种植类', '个', 5, 30, 1, '配套种植体'),
('矫正托槽', '金属自锁', 'ORMCO', 2, '矫正类', '副', 3, 15, 1, 'Damon Q'),
('镍钛丝', '0.014英寸', 'ORMCO', 2, '矫正类', '根', 10, 50, 1, '矫正用'),
('印模材', '藻酸盐', '登士柏', 3, '耗材类', '包', 10, 40, 1, '常规取模');

-- 13. 采购记录表
INSERT IGNORE INTO `purchases` (`product_name`, `category`, `brand`, `supplier`, `specification`, `unit`, `quantity`, `price`, `status`, `createdate`, `purchasedate`, `indate`) VALUES
('树脂材料', '耗材', '3M', '3M中国', 'Z350', '支', 20, 80.00, '已入库', '2026-05-20', '2026-05-20', '2026-05-21'),
('种植体', '种植', '奥齿泰', '奥齿泰', '4.0*10mm', '颗', 10, 1200.00, '已入库', '2026-05-25', '2026-05-25', '2026-05-26'),
('手套', '耗材', '稳健', '稳健医疗', '乳胶M号', '双', 200, 0.50, '已入库', '2026-06-01', '2026-06-01', '2026-06-02');

-- 14. 咨询记录表
INSERT IGNORE INTO `consultation_records` (`contact_name`, `contact_phone`, `consultation_channel`, `chief_project`, `intent_level`, `handling_result`, `created_by_name`, `consultation_time`) VALUES
('陈八', '13700137001', '电话', '种植牙', '高', '已预约', '客服小张', NOW()),
('周九', '13700137002', '微信', '矫正', '中', '待跟进', '客服小张', NOW()),
('吴十', '13700137003', '美团', '洗牙', '低', '已回复', '客服小李', NOW()),
('郑十一', '13700137004', '电话', '种植术后', '中', '已安抚', '客服小李', NOW());

-- 15. 随访记录表
INSERT IGNORE INTO `patient_followup` (`patient_id`, `doctor_account_id`, `doctor_name`, `followup_date`, `followup_type`, `summary`, `next_followup_date`) VALUES
(1, 1, '王医生', '2026-06-11', '治疗后随访', '术后疼痛缓解，无不适', '2026-06-18'),
(3, 4, '刘医生', '2026-06-06', '种植术后随访', '愈合良好，无红肿', '2026-06-20'),
(4, 1, '王医生', '2026-06-02', '牙周随访', '牙龈出血减少', '2026-09-02');

-- 16. 技工所表（已有5条，补充3条）
INSERT IGNORE INTO `lab_factories` (`name`, `contact_name`, `contact_phone`, `address`, `status`) VALUES
('精艺齿科', '张师傅', '13600136001', '北京市昌平区', 1),
('美牙工坊', '李师傅', '13600136002', '北京市通州区', 1),
('康泰义齿', '王师傅', '13600136003', '北京市大兴区', 1);

-- 17. 技工订单表
INSERT IGNORE INTO `lab_orders` (`factory_id`, `factory_name`, `patient_id`, `patient_name`, `project_name`, `product_name`, `unit_price`, `quantity`, `total_amount`, `order_date`, `expected_delivery_date`, `status`) VALUES
(1, '精艺齿科', 1, '张三', '全瓷冠', '上颌左6全瓷冠', 1200.00, 1, 1200.00, '2026-06-01', '2026-06-08', '已完工'),
(2, '美牙工坊', 2, '李四', '保持器', '下颌保持器', 300.00, 1, 300.00, '2026-06-05', '2026-06-12', '制作中'),
(1, '精艺齿科', 3, '王五', '种植冠', '上颌右5种植冠', 1500.00, 1, 1500.00, '2026-06-03', '2026-06-10', '已送件');

-- 18. 广告支出表
INSERT IGNORE INTO `advertising_spending` (`platform`, `campaign_name`, `start_date`, `end_date`, `amount`, `target_project`, `remark`) VALUES
('美团', '美团团购推广', '2026-05-01', '2026-05-31', 5000.00, '洗牙', '月度推广'),
('抖音', '抖音本地推', '2026-05-01', '2026-05-31', 8000.00, '种植', '月度推广'),
('百度', '百度搜索推广', '2026-05-01', '2026-05-31', 3000.00, '矫正', '月度推广'),
('小红书', '小红书种草', '2026-05-01', '2026-05-31', 2000.00, '美白', '月度推广');

-- 19. 经营日报表
INSERT IGNORE INTO `business_daily_analysis` (`analysis_date`, `analysis_status`, `operating_score`, `headline`, `summary`) VALUES
('2026-06-01', 'completed', 85, '今日营收良好', '就诊15人次，新客5人'),
('2026-06-02', 'completed', 90, '种植牙大单', '就诊18人次，营收1.2万'),
('2026-06-03', 'completed', 75, '平稳运营', '就诊12人次，以基础治疗为主'),
('2026-06-04', 'completed', 95, '种植高峰期', '就诊20人次，营收1.5万'),
('2026-06-05', 'completed', 88, '复诊为主', '就诊16人次，以复诊为主');

-- 20. 知情同意书表（已有6条，补充3条）
INSERT IGNORE INTO `consent_template` (`title`, `content`, `remark`, `status`, `sort_order`) VALUES
('根管治疗知情同意书', '我已了解根管治疗的流程、风险及预后，同意接受治疗。可能出现术后疼痛、器械分离、根管侧穿等并发症。', '', 1, 1),
('种植牙手术知情同意书', '我已了解种植牙手术的流程、风险及预后，同意接受手术。可能出现术后肿胀、感染、种植体失败等情况。', '', 1, 2),
('拔牙手术知情同意书', '我已了解拔牙手术的流程、风险及预后，同意接受手术。可能出现术后出血、感染、干槽症等情况。', '', 1, 3);

-- 21. 排班模板表
INSERT IGNORE INTO `shift_template` (`name`, `doctor_name`, `pattern_json`) VALUES
('早班', '王医生', '{"days":["周一","周二","周三","周四","周五"],"startTime":"09:00","endTime":"12:00"}'),
('午班', '王医生', '{"days":["周一","周二","周三","周四","周五"],"startTime":"14:00","endTime":"18:00"}'),
('早班', '李医生', '{"days":["周一","周三","周五"],"startTime":"09:00","endTime":"12:00"}'),
('午班', '张医生', '{"days":["周二","周四"],"startTime":"14:00","endTime":"18:00"}');

-- 22. 患者知情同意书记录
INSERT IGNORE INTO `patient_consent` (`patient_id`, `patient_name`, `doctor_account_id`, `doctor_name`, `title`, `content`, `status`, `signed_at`) VALUES
(1, '张三', 1, '王医生', '根管治疗知情同意书', '我已了解根管治疗的流程、风险及预后，同意接受治疗。', 1, '2026-06-10 10:00:00'),
(3, '王五', 4, '刘医生', '种植牙手术知情同意书', '我已了解种植牙手术的流程、风险及预后，同意接受手术。', 1, '2026-03-10 09:00:00'),
(5, '孙七', 3, '张医生', '拔牙手术知情同意书', '我已了解拔牙手术的流程、风险及预后，同意接受手术。', 1, '2026-06-10 14:00:00');

-- 23. 患者时间线
INSERT IGNORE INTO `patient_timeline` (`patient_id`, `event_time`, `event_type`, `event_title`, `event_content`) VALUES
(1, '2026-01-15 09:00:00', '初诊', '首次就诊', '口腔检查'),
(1, '2026-06-10 10:00:00', '治疗', '根管治疗', '根管治疗第一次'),
(2, '2026-02-20 09:00:00', '初诊', '矫正咨询', '矫正方案制定'),
(2, '2026-06-08 10:30:00', '治疗', '矫正复诊', '更换弓丝，调整力度'),
(3, '2026-03-10 09:00:00', '初诊', '种植手术', '单颗种植手术'),
(3, '2026-06-05 14:00:00', '复查', '种植复查', '术后3个月复查');

-- 24. 患者标签表
INSERT IGNORE INTO `patient_risk_tag` (`patient_id`, `tag_code`, `tag_name`, `risk_level`, `source`, `note`) VALUES
(1, 'HYPERTENSION', '高血压', '高危', '患者自述', '需监测血压'),
(1, 'PENICILLIN_ALLERGY', '青霉素过敏', '中危', '患者自述', '禁用青霉素类药物'),
(4, 'PERIODONTITIS', '牙周炎', '低危', '医生诊断', '定期牙周维护');

-- 25. 患者影像表
INSERT IGNORE INTO `patient_images` (`patient_id`, `patient_name`, `image_name`, `image_type`, `file_path`, `notes`) VALUES
(1, '张三', '全景片', 'X光片', '/uploads/patient-images/1_panorama.jpg', '初诊拍摄'),
(3, '王五', '种植CT', 'CT', '/uploads/patient-images/3_ct.jpg', '种植术前'),
(5, '孙七', '智齿片', 'X光片', '/uploads/patient-images/5_xray.jpg', '拔智齿前');

-- 26. 病历短语表
INSERT IGNORE INTO `medical_record_phrases` (`field_type`, `content`, `category`, `sort_order`, `status`) VALUES
('chief_complaint', '牙齿疼痛', '主诉', 1, 1),
('chief_complaint', '矫正复诊', '主诉', 2, 1),
('diagnosis', '急性牙髓炎', '诊断', 1, 1),
('diagnosis', '慢性牙周炎', '诊断', 2, 1),
('treatment_plan', '根管治疗', '治疗方案', 1, 1),
('treatment_plan', '拔除患牙', '治疗方案', 2, 1);

-- 27. AI 全局配置（补充）
INSERT IGNORE INTO `ai_global_config` (`config_key`, `config_value`, `description`) VALUES
('model_provider', 'openai', '默认模型提供商'),
('max_tokens', '2000', '最大token数'),
('temperature', '0.7', '温度参数');

-- 28. 操作日志表
INSERT IGNORE INTO `operation_log` (`operator_id`, `operator_name`, `operator_role`, `operation_type`, `target_type`, `target_name`, `operation_desc`, `ip_address`, `status`) VALUES
(1, 'admin', '管理员', '新增', '患者', '张三', '新增患者信息', '127.0.0.1', 1),
(1, 'admin', '管理员', '新增', '预约', '张三-王医生', '预约2026-06-12 09:00', '127.0.0.1', 1),
(1, 'admin', '管理员', '收款', '财务', '张三', '根管治疗收款600元', '127.0.0.1', 1);

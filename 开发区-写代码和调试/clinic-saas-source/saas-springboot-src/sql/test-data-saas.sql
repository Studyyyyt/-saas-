-- ============================================================
-- 口腔 SaaS 多诊所测试数据脚本
-- 说明：为多诊所架构填充测试数据
-- 执行方式：mysql -u clinic_user -p clinic_system < test-data-saas.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 诊所表 (clinic)
-- ----------------------------
-- 保留默认诊所，添加两个新诊所
INSERT IGNORE INTO clinic (id, name, address, contact_phone, status, created_at, updated_at) VALUES
('yiyin2', '一隐口腔二店', '上海市浦东新区口腔产业园B区', '021-12345678', 1, NOW(), NOW()),
('yiyin3', '一隐口腔三店', '广州市白云区牙科工业区C栋', '020-87654321', 1, NOW(), NOW());

-- ----------------------------
-- 2. 用户表 (users)
-- ----------------------------
-- 添加医生、护士账号（密码均为明文 '123456'）
INSERT INTO users (username, password, name, role, status, created_at, updated_at) VALUES
('doctor1', '123456', '李医生', 'doctor', 1, NOW(), NOW()),
('doctor2', '123456', '张医生', 'doctor', 1, NOW(), NOW()),
('doctor3', '123456', '王医生', 'doctor', 1, NOW(), NOW()),
('nurse1', '123456', '刘护士', 'nurse', 1, NOW(), NOW()),
('nurse2', '123456', '陈护士', 'nurse', 1, NOW(), NOW()),
('nurse3', '123456', '赵护士', 'nurse', 1, NOW(), NOW());

-- ----------------------------
-- 3. 用户-诊所关联表 (user_clinic)
-- ----------------------------
-- 获取新增用户的ID（admin 的 ID 应为 5 或更大）
SET @admin_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1);
SET @d1_id = (SELECT id FROM users WHERE username = 'doctor1' LIMIT 1);
SET @d2_id = (SELECT id FROM users WHERE username = 'doctor2' LIMIT 1);
SET @d3_id = (SELECT id FROM users WHERE username = 'doctor3' LIMIT 1);
SET @n1_id = (SELECT id FROM users WHERE username = 'nurse1' LIMIT 1);
SET @n2_id = (SELECT id FROM users WHERE username = 'nurse2' LIMIT 1);
SET @n3_id = (SELECT id FROM users WHERE username = 'nurse3' LIMIT 1);

-- admin 拥有所有诊所权限
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@admin_id, 'default', 'admin', 1),
(@admin_id, 'yiyin2', 'admin', 0),
(@admin_id, 'yiyin3', 'admin', 0);

-- 医生1：在 default 和 yiyin2
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@d1_id, 'default', 'doctor', 1),
(@d1_id, 'yiyin2', 'doctor', 0);

-- 医生2：在 default 和 yiyin3
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@d2_id, 'default', 'doctor', 1),
(@d2_id, 'yiyin3', 'doctor', 0);

-- 医生3：只在 yiyin2
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@d3_id, 'yiyin2', 'doctor', 1);

-- 护士1：在 default
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@n1_id, 'default', 'nurse', 1);

-- 护士2：在 yiyin2
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@n2_id, 'yiyin2', 'nurse', 1);

-- 护士3：在 yiyin3
INSERT INTO user_clinic (user_id, clinic_id, role, is_default) VALUES
(@n3_id, 'yiyin3', 'nurse', 1);

-- ----------------------------
-- 4. 患者表 (patients) - default 诊所
-- ----------------------------
INSERT INTO patients (clinic_id, name, name_pinyin, name_initials, gender, age, date_of_birth, phone, email, address, customer_source, created_at, updated_at) VALUES
('default', '张三', 'zhangsan', 'zs', '男', 35, '1991-03-15', '13800138001', 'zs@test.com', '北京市朝阳区', '网络搜索', NOW(), NOW()),
('default', '李四', 'lisi', 'ls', '女', 28, '1997-08-22', '13800138002', 'ls@test.com', '北京市海淀区', '朋友介绍', NOW(), NOW()),
('default', '王五', 'wangwu', 'ww', '男', 42, '1983-11-05', '13800138003', 'ww@test.com', '北京市西城区', '美团', NOW(), NOW()),
('default', '赵六', 'zhaoliu', 'zl', '女', 31, '1994-06-18', '13800138004', 'zl@test.com', '北京市东城区', '自然到店', NOW(), NOW()),
('default', '钱七', 'qianqi', 'qq', '男', 55, '1970-09-30', '13800138005', 'qq@test.com', '北京市丰台区', '转介绍', NOW(), NOW()),
('default', '孙八', 'sunba', 'sb', '女', 26, '1999-01-12', '13800138006', 'sb@test.com', '北京市昌平区', '网络搜索', NOW(), NOW()),
('default', '周九', 'zhoujiu', 'zj', '男', 38, '1987-04-25', '13800138007', 'zj@test.com', '北京市通州区', '美团', NOW(), NOW()),
('default', '吴十', 'wushi', 'ws', '女', 45, '1980-12-08', '13800138008', 'ws@test.com', '北京市大兴区', '朋友介绍', NOW(), NOW()),
('default', '郑一', 'zhengyi', 'zy', '男', 33, '1992-07-14', '13800138009', 'zy@test.com', '北京市顺义区', '自然到店', NOW(), NOW()),
('default', '陈二', 'chener', 'ce', '女', 29, '1996-02-28', '13800138010', 'ce@test.com', '北京市房山区', '网络搜索', NOW(), NOW());

-- yiyin2 诊所患者
INSERT INTO patients (clinic_id, name, name_pinyin, name_initials, gender, age, date_of_birth, phone, email, address, customer_source, created_at, updated_at) VALUES
('yiyin2', '刘明', 'liuming', 'lm', '男', 41, '1984-05-19', '13800138011', 'lm@test.com', '上海市浦东新区', '美团', NOW(), NOW()),
('yiyin2', '黄丽', 'huangli', 'hl', '女', 36, '1989-10-03', '13800138012', 'hl@test.com', '上海市黄浦区', '转介绍', NOW(), NOW()),
('yiyin2', '林强', 'linqiang', 'lq', '男', 48, '1977-08-11', '13800138013', 'lq@test.com', '上海市徐汇区', '朋友介绍', NOW(), NOW()),
('yiyin2', '徐静', 'xujing', 'xj', '女', 27, '1998-11-23', '13800138014', 'xj@test.com', '上海市长宁区', '自然到店', NOW(), NOW()),
('yiyin2', '马云', 'mayun', 'my', '男', 52, '1973-03-08', '13800138015', 'my@test.com', '上海市静安区', '网络搜索', NOW(), NOW());

-- yiyin3 诊所患者
INSERT INTO patients (clinic_id, name, name_pinyin, name_initials, gender, age, date_of_birth, phone, email, address, customer_source, created_at, updated_at) VALUES
('yiyin3', '朱婷', 'zhuting', 'zt', '女', 30, '1995-06-16', '13800138016', 'zt@test.com', '广州市天河区', '美团', NOW(), NOW()),
('yiyin3', '宋江', 'songjiang', 'sj', '男', 39, '1986-09-21', '13800138017', 'sj@test.com', '广州市越秀区', '转介绍', NOW(), NOW()),
('yiyin3', '唐艳', 'tangyan', 'ty', '女', 24, '2001-04-04', '13800138018', 'ty@test.com', '广州市海珠区', '自然到店', NOW(), NOW()),
('yiyin3', '韩梅', 'hanmei', 'hm', '女', 34, '1991-12-12', '13800138019', 'hm@test.com', '广州市荔湾区', '朋友介绍', NOW(), NOW()),
('yiyin3', '冯刚', 'fenggang', 'fg', '男', 46, '1979-07-07', '13800138020', 'fg@test.com', '广州市白云区', '网络搜索', NOW(), NOW());

-- ----------------------------
-- 5. 预约表 (appointment)
-- ----------------------------
SET @p1 = (SELECT id FROM patients WHERE name = '张三' AND clinic_id = 'default' LIMIT 1);
SET @p2 = (SELECT id FROM patients WHERE name = '李四' AND clinic_id = 'default' LIMIT 1);
SET @p3 = (SELECT id FROM patients WHERE name = '王五' AND clinic_id = 'default' LIMIT 1);
SET @p4 = (SELECT id FROM patients WHERE name = '赵六' AND clinic_id = 'default' LIMIT 1);
SET @p5 = (SELECT id FROM patients WHERE name = '钱七' AND clinic_id = 'default' LIMIT 1);

INSERT INTO appointment (clinic_id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_name, appointment_purpose, status, created_at, updated_at) VALUES
('default', @p1, '张三', CURDATE(), '08:30:00', 60, '李医生', '种植牙复诊', '待就诊', NOW(), NOW()),
('default', @p3, '王五', CURDATE(), '09:00:00', 90, '李医生', '根管治疗', '待就诊', NOW(), NOW()),
('default', @p2, '李四', CURDATE(), '09:30:00', 30, '张医生', '拔牙', '已完成', NOW(), NOW()),
('default', @p4, '赵六', CURDATE(), '14:00:00', 45, '张医生', '洗牙', '待就诊', NOW(), NOW()),
('default', @p5, '钱七', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', 60, '李医生', '种植牙二期', '已预约', NOW(), NOW()),
('default', @p1, '张三', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', 60, '李医生', '术后复查', '已完成', NOW(), NOW()),
('default', @p2, '李四', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '14:30:00', 30, '张医生', '拆线', '已完成', NOW(), NOW());

-- ----------------------------
-- 6. 病历表 (medical_records)
-- ----------------------------
INSERT INTO medical_records (clinic_id, patient_id, patient_name, doctor_name, visit_date, chief_complaint, diagnosis, treatment_plan, created_at, updated_at) VALUES
('default', @p2, '李四', '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '右下后牙疼痛3天', '右下智齿冠周炎', '局部麻醉下拔除右下智齿，缝合', NOW(), NOW()),
('default', @p1, '张三', '李医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '种植牙术后1周复查', '种植体愈合良好', '拆线，口腔卫生指导', NOW(), NOW()),
('default', @p3, '王五', '李医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '左下后牙冷热刺激痛1周', '左下第一磨牙可复性牙髓炎', '去腐备洞，氢氧化钙盖髓，树脂充填', NOW(), NOW()),
('default', @p4, '赵六', '张医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '牙龈出血半年', '中度牙周炎', '全口龈上洁治，口腔卫生宣教', NOW(), NOW());

-- ----------------------------
-- 7. 财务表 (finances)
-- ----------------------------
INSERT INTO finances (clinic_id, name, amount, date, type, payment_channel, notes, created_at, updated_at) VALUES
('default', '补牙收费', 2500, '2026-05-29', 'income', '现金', '嵌体全款', NOW(), NOW()),
('default', '拔牙收费', 500, '2026-05-28', 'income', '微信', '拔牙全款', NOW(), NOW()),
('default', '根管收费', 1500, '2026-05-28', 'income', '支付宝', '根管全款', NOW(), NOW()),
('default', '种植牙收费', 12000, '2026-05-27', 'income', '银行卡', '种植体+基台', NOW(), NOW()),
('default', '洗牙收费', 300, '2026-05-27', 'income', '现金', '全口洁治', NOW(), NOW()),
('default', '正畸首付', 8000, '2026-05-26', 'income', '微信', '隐形矫正首付', NOW(), NOW()),
('default', '根管退款', 300, '2026-05-28', 'expense', '原路退回', '患者要求退款', NOW(), NOW()),
('default', '材料采购', 5000, '2026-05-25', 'expense', '银行转账', '树脂材料采购', NOW(), NOW());

-- ----------------------------
-- 8. 回访表 (followups)
-- ----------------------------
INSERT INTO followups (clinic_id, patient_id, patient_name, doctor_name, followup_date, followup_method, followup_result, status, created_at, updated_at) VALUES
('default', @p1, '张三', '李医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '电话', '种植体愈合良好，无松动，牙龈健康', '已完成', NOW(), NOW()),
('default', @p3, '王五', '李医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '电话', '患者根管治疗后无不适，预约下周复诊', '已完成', NOW(), NOW()),
('default', @p4, '赵六', '张医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '微信', '牙周治疗后出血减少，口腔卫生改善', '已完成', NOW(), NOW()),
('default', @p2, '李四', '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '电话', '术后恢复良好，无肿胀疼痛', '已完成', NOW(), NOW()),
('default', @p5, '钱七', '李医生', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '电话', NULL, '待回访', NOW(), NOW());

-- ----------------------------
-- 9. 咨询记录表 (consultation_records)
-- ----------------------------
INSERT INTO consultation_records (clinic_id, patient_name, phone, channel, chief_complaint, intention_level, followup_result, is_deal, estimated_amount, next_followup_date, followup_count, created_at, updated_at) VALUES
('default', '周咨询1', '13900001111', '微信', '牙齿不齐想矫正', '高', '已预约初诊', 0, 25000, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, NOW(), NOW()),
('default', '周咨询2', '13900002222', '电话', '种植牙咨询', '高', '发送报价单', 0, 15000, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, NOW(), NOW()),
('default', '周咨询3', '13900003333', '美团', '洗牙团购', '中', '已购买团购', 1, 298, NULL, 1, NOW(), NOW()),
('default', '周咨询4', '13900004444', '百度', '根管治疗多少钱', '中', '已发送价格表', 0, 2000, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, NOW(), NOW()),
('default', '周咨询5', '13900005555', '转介绍', '小朋友蛀牙', '高', '已预约儿童牙科', 0, 800, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, NOW(), NOW()),
('default', '周咨询6', '13900006666', '抖音', '美白牙齿', '低', '暂无意向', 0, 3000, NULL, 1, NOW(), NOW());

-- ----------------------------
-- 10. 医生表 (doctors)
-- ----------------------------
-- 将 users 中的医生同步到 doctors 表（如果系统使用）
INSERT IGNORE INTO doctors (clinic_id, name, title, phone, status, created_at, updated_at) VALUES
('default', '李医生', '主治医师', '13800138001', 1, NOW(), NOW()),
('default', '张医生', '副主任医师', '13800138002', 1, NOW(), NOW()),
('yiyin2', '王医生', '执业医师', '13800138003', 1, NOW(), NOW());

-- ----------------------------
-- 11. 加工厂表 (lab_factories)
-- ----------------------------
INSERT INTO lab_factories (clinic_id, name, contact_name, contact_phone, address, status, created_at, updated_at) VALUES
('default', '精工义齿加工厂', '王师傅', '13800138100', '北京市朝阳区牙科技工园区A座', 'active', NOW(), NOW()),
('default', '美齿数字化中心', '李经理', '13800138101', '上海市浦东新区口腔产业园B区', 'active', NOW(), NOW()),
('yiyin2', '华南义齿制作中心', '陈主管', '13800138102', '广州市白云区牙科工业区C栋', 'active', NOW(), NOW());

-- ----------------------------
-- 12. 耗材分类表 (material_categories)
-- ----------------------------
INSERT INTO material_categories (clinic_id, name, sort_order, created_at, updated_at) VALUES
('default', '种植类', 1, NOW(), NOW()),
('default', '正畸类', 2, NOW(), NOW()),
('default', '修复类', 3, NOW(), NOW()),
('default', '基础耗材', 4, NOW(), NOW()),
('default', '其他', 5, NOW(), NOW());

-- ----------------------------
-- 13. 耗材档案表 (materials)
-- ----------------------------
INSERT INTO materials (clinic_id, name, specification, brand, unit, current_stock, warning_threshold, status, created_at, updated_at) VALUES
('default', '种植体', '4.0x10mm', '奥齿泰', '颗', 50, 10, 'active', NOW(), NOW()),
('default', '基台', '常规', '奥齿泰', '颗', 30, 5, 'active', NOW(), NOW()),
('default', '树脂材料', 'A2色', '3M', '支', 20, 5, 'active', NOW(), NOW()),
('default', '隐形矫治器膜片', '0.75mm', '时代天使', '张', 100, 20, 'active', NOW(), NOW()),
('default', '氧化锆瓷块', 'C2色', '威兰德', '块', 15, 3, 'active', NOW(), NOW()),
('default', '扩大针', '15#-40#', '登士柏', '板', 5, 10, 'active', NOW(), NOW()),
('default', '可吸收缝合线', '4-0', '强生', '包', 8, 5, 'active', NOW(), NOW());

-- ----------------------------
-- 14. 库存表 (inventory)
-- ----------------------------
INSERT INTO inventory (clinic_id, material_id, material_name, quantity, unit, batch_no, expiry_date, created_at, updated_at) VALUES
('default', 1, '种植体', 50, '颗', 'B2026001', '2028-12-31', NOW(), NOW()),
('default', 2, '基台', 30, '颗', 'B2026002', '2028-12-31', NOW(), NOW()),
('default', 3, '树脂材料', 20, '支', 'B2026003', '2027-06-30', NOW(), NOW()),
('default', 4, '隐形矫治器膜片', 100, '张', 'B2026004', '2029-03-31', NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

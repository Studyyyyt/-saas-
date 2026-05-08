-- ============================================================
-- 舒澳口腔 SaaS 测试数据初始化脚本
-- 说明：为新部署的系统填充演示数据，便于功能测试与界面验证
-- 执行方式：mysql -u root -p saas_db < demo-data.sql
-- 注意事项：
--   1. 请先确保基础表结构已创建（Flyway 迁移已执行）
--   2. 此脚本会清空相关表并重新插入数据
--   3. 密码均为明文 '123456'（与系统登录方式一致）
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 账号表 (account / user)
-- ----------------------------
TRUNCATE TABLE account;
INSERT INTO account (id, username, password, name, role) VALUES
(1, 'admin',  '123456', '王管理员', '管理员'),
(2, 'doctor1','123456', '李医生',   '医生'),
(3, 'doctor2','123456', '张医生',   '医生'),
(4, 'nurse1', '123456', '刘护士',   '护士'),
(5, 'nurse2', '123456', '陈护士',   '护士');

-- ----------------------------
-- 2. 患者表 (patients)
-- ----------------------------
TRUNCATE TABLE patients;
INSERT INTO patients (id, name, name_pinyin, name_initials, gender, age, date_of_birth, phone, email, address, customer_source, latest_visit_doctor, latest_treatment, created_at, updated_at) VALUES
(1,  '张三', 'zhangsan',    'zs',  '男', 35, '1991-03-15', '13800138001', 'zs@example.com', '北京市朝阳区', '网络搜索', '李医生', '种植牙一期', NOW(), NOW()),
(2,  '李四', 'lisi',        'ls',  '女', 28, '1997-08-22', '13800138002', 'ls@example.com', '北京市海淀区', '朋友介绍', '张医生', '拔牙', NOW(), NOW()),
(3,  '王五', 'wangwu',      'ww',  '男', 42, '1983-11-05', '13800138003', 'ww@example.com', '北京市西城区', '美团', '李医生', '根管治疗', NOW(), NOW()),
(4,  '赵六', 'zhaoliu',     'zl',  '女', 31, '1994-06-18', '13800138004', 'zl@example.com', '北京市东城区', '自然到店', '张医生', '洗牙', NOW(), NOW()),
(5,  '钱七', 'qianqi',      'qq',  '男', 55, '1970-09-30', '13800138005', 'qq@example.com', '北京市丰台区', '转介绍', '李医生', '种植牙二期', NOW(), NOW()),
(6,  '孙八', 'sunba',       'sb',  '女', 26, '1999-01-12', '13800138006', 'sb@example.com', '北京市昌平区', '网络搜索', '张医生', '正畸复诊', NOW(), NOW()),
(7,  '周九', 'zhoujiu',     'zj',  '男', 38, '1987-04-25', '13800138007', 'zj@example.com', '北京市通州区', '美团', '李医生', '补牙', NOW(), NOW()),
(8,  '吴十', 'wushi',       'ws',  '女', 45, '1980-12-08', '13800138008', 'ws@example.com', '北京市大兴区', '朋友介绍', '张医生', '牙周治疗', NOW(), NOW()),
(9,  '郑一', 'zhengyi',     'zy',  '男', 33, '1992-07-14', '13800138009', 'zy@example.com', '北京市顺义区', '自然到店', '李医生', '种植牙复诊', NOW(), NOW()),
(10, '陈二', 'chener',      'ce',  '女', 29, '1996-02-28', '13800138010', 'ce@example.com', '北京市房山区', '网络搜索', '张医生', '取模', NOW(), NOW()),
(11, '刘明', 'liuming',     'lm',  '男', 41, '1984-05-19', '13800138011', 'lm@example.com', '北京市石景山区', '美团', '李医生', '根管治疗', NOW(), NOW()),
(12, '黄丽', 'huangli',     'hl',  '女', 36, '1989-10-03', '13800138012', 'hl@example.com', '北京市门头沟区', '转介绍', '张医生', '拔牙', NOW(), NOW()),
(13, '林强', 'linqiang',    'lq',  '男', 48, '1977-08-11', '13800138013', 'lq@example.com', '北京市怀柔区', '朋友介绍', '李医生', '种植牙一期', NOW(), NOW()),
(14, '徐静', 'xujing',      'xj',  '女', 27, '1998-11-23', '13800138014', 'xj@example.com', '北京市平谷区', '自然到店', '张医生', '正畸初诊', NOW(), NOW()),
(15, '马云', 'mayun',       'my',  '男', 52, '1973-03-08', '13800138015', 'my@example.com', '北京市密云区', '网络搜索', '李医生', '全口修复', NOW(), NOW()),
(16, '朱婷', 'zhuting',     'zt',  '女', 30, '1995-06-16', '13800138016', 'zt@example.com', '北京市延庆区', '美团', '张医生', '洗牙', NOW(), NOW()),
(17, '宋江', 'songjiang',   'sj',  '男', 39, '1986-09-21', '13800138017', 'sj@example.com', '北京市朝阳区', '转介绍', '李医生', '补牙', NOW(), NOW()),
(18, '唐艳', 'tangyan',     'ty',  '女', 24, '2001-04-04', '13800138018', 'ty@example.com', '北京市海淀区', '自然到店', '张医生', '美白', NOW(), NOW()),
(19, '韩梅', 'hanmei',      'hm',  '女', 34, '1991-12-12', '13800138019', 'hm@example.com', '北京市西城区', '朋友介绍', '李医生', '根管治疗', NOW(), NOW()),
(20, '冯刚', 'fenggang',    'fg',  '男', 46, '1979-07-07', '13800138020', 'fg@example.com', '北京市东城区', '网络搜索', '张医生', '种植牙二期', NOW(), NOW());

-- ----------------------------
-- 3. 预约表 (appointment)
-- ----------------------------
TRUNCATE TABLE appointment;
INSERT INTO appointment (id, patient_id, patient_name, appointment_date, appointment_time, duration_minutes, doctor_account_id, doctor_name, appointment_purpose, status, has_arrears, arrears_amount) VALUES
-- 今日预约
(1,  1,  '张三', CURDATE(), '08:30:00', 60, 2, '李医生', '种植牙复诊',   '待就诊',    0, 0),
(2,  3,  '王五', CURDATE(), '09:00:00', 90, 2, '李医生', '根管治疗',     '待就诊',    0, 0),
(3,  7,  '周九', CURDATE(), '10:00:00', 60, 2, '李医生', '补牙',         '待就诊',    0, 0),
(4,  2,  '李四', CURDATE(), '09:30:00', 30, 3, '张医生', '拔牙',         '已完成',    0, 0),
(5,  4,  '赵六', CURDATE(), '14:00:00', 45, 3, '张医生', '洗牙',         '待就诊',    0, 0),
(6,  6,  '孙八', CURDATE(), '15:30:00', 60, 3, '张医生', '正畸复诊',     '待就诊',    0, 0),
(7,  14, '徐静', CURDATE(), '11:00:00', 60, 2, '李医生', '正畸初诊',     '已取消',    0, 0),
-- 明日预约
(8,  5,  '钱七', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', 60, 2, '李医生', '种植牙二期', '已预约', 0, 0),
(9,  9,  '郑一', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', 60, 2, '李医生', '种植牙复诊', '已预约', 0, 0),
(10, 11, '刘明', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 90, 2, '李医生', '根管治疗',   '已预约', 0, 0),
(11, 16, '朱婷', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 45, 3, '张医生', '洗牙',       '已预约', 0, 0),
-- 昨日
(12, 8,  '吴十', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', 60, 3, '张医生', '牙周治疗',   '已完成', 0, 0),
(13, 12, '黄丽', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', 30, 3, '张医生', '拔牙',       '已完成', 0, 0),
(14, 15, '马云', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:30:00', 90, 2, '李医生', '全口修复',   '已完成', 0, 0),
-- 前天
(15, 17, '宋江', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '08:30:00', 30, 2, '李医生', '补牙',       '已完成', 0, 0),
(16, 18, '唐艳', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '15:00:00', 60, 3, '张医生', '美白',       '已完成', 0, 0),
-- 更早
(17, 19, '韩梅', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:30:00', 90, 2, '李医生', '根管治疗',   '已完成', 0, 0),
(18, 20, '冯刚', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '11:00:00', 60, 3, '张医生', '种植牙二期', '已完成', 0, 0),
(19, 13, '林强', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:00:00', 60, 2, '李医生', '种植牙一期', '已完成', 0, 0),
(20, 10, '陈二', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '10:00:00', 30, 3, '张医生', '取模',       '已完成', 0, 0);

-- ----------------------------
-- 4. 病历表 (medical_records)
-- ----------------------------
TRUNCATE TABLE medical_records;
INSERT INTO medical_records (id, patient_id, patient_name, doctor_name, visit_date, chief_complaint, diagnosis, treatment, prescription, notes, created_at, updated_at) VALUES
(1,  2,  '李四', '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '右下后牙疼痛3天', '右下智齿冠周炎', '局部麻醉下拔除右下智齿，缝合', '头孢克肟 0.1g tid × 3天；布洛芬 0.3g prn', '术后24小时勿漱口', NOW(), NOW()),
(2,  8,  '吴十', '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '牙龈出血半年', '中度牙周炎', '全口龈上洁治，口腔卫生宣教', '复方氯己定含漱液 10ml bid × 7天', '建议3个月复查', NOW(), NOW()),
(3,  12, '黄丽', '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '左上后牙残根要求拔除', '左上第二磨牙残根', '局麻下拔除左上第二磨牙残根', '头孢克肟 0.1g tid × 3天', '术后一周拆线', NOW(), NOW()),
(4,  15, '马云', '李医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '全口牙齿松动，咀嚼无力', '重度牙周炎伴牙列缺损', '上颌种植4颗修复，下颌种植2颗修复', '阿莫西林 0.5g tid × 5天；甲硝唑 0.2g tid × 5天', '术后两周拆线，3个月后取模', NOW(), NOW()),
(5,  17, '宋江', '李医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '右上后牙缺损1月', '右上第一磨牙深龋', '去腐备洞，树脂充填', NULL, '避免咬硬物，定期复查', NOW(), NOW()),
(6,  18, '唐艳', '张医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '牙齿发黄要求美白', '牙着色', '诊室美白治疗（35%过氧化氢）', NULL, '术后48小时避免深色饮食', NOW(), NOW()),
(7,  19, '韩梅', '李医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '左下后牙自发痛2天', '左下第一磨牙急性牙髓炎', '局麻下开髓，根管预备，封氢氧化钙', '头孢克肟 0.1g tid × 3天；布洛芬 0.3g prn', '一周后复诊完成根充', NOW(), NOW()),
(8,  20, '冯刚', '张医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '种植牙术后3个月复查', '种植体骨结合良好', '二期手术，安装愈合基台', NULL, '两周后取模', NOW(), NOW()),
(9,  13, '林强', '李医生', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '下颌缺牙要求种植', '下颌第二磨牙缺失', '植入种植体一枚（4.0×10mm）', '阿莫西林 0.5g tid × 5天', '术后3个月复查骨结合情况', NOW(), NOW()),
(10, 10, '陈二', '张医生', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '正畸治疗中取模', '正畸治疗中', '硅橡胶取模，准备制作保持器', NULL, '一周后试戴保持器', NOW(), NOW()),
(11, 1,  '张三', '李医生', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '种植牙术后1周复查', '种植体愈合良好', '拆线，口腔卫生指导', NULL, '3个月后二期手术', NOW(), NOW()),
(12, 3,  '王五', '李医生', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '左下后牙冷热刺激痛1周', '左下第一磨牙可复性牙髓炎', '去腐备洞，氢氧化钙盖髓，树脂充填', NULL, '如症状加重需根管治疗', NOW(), NOW()),
(13, 5,  '钱七', '李医生', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '种植牙术后3个月', '种植体骨结合良好', '二期手术，安装愈合基台', NULL, '两周后取模', NOW(), NOW()),
(14, 7,  '周九', '李医生', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '右上后牙食物嵌塞', '右上第二磨牙邻面龋', '去腐备洞，树脂充填', NULL, '注意邻面清洁', NOW(), NOW()),
(15, 9,  '郑一', '李医生', DATE_SUB(CURDATE(), INTERVAL 8 DAY), '种植牙术后半年复查', '种植体周围组织健康', '修复体调整咬合', NULL, '半年复查一次', NOW(), NOW());

-- ----------------------------
-- 5. 处置记录表 (treatment)
-- ----------------------------
TRUNCATE TABLE treatment;
INSERT INTO treatment (id, patient_id, patient_name, appointment_purpose, status, doctor_account_id, doctor_name, treatment_date, treatment_content, tooth_positions, treatment_fee, charged_amount, billing_status, created_at, updated_at) VALUES
(1,  2,  '李四', '拔牙',           '完成', 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '拔除右下智齿，缝合',          '48',    '800.00',  800.00,  '已收费', NOW(), NOW()),
(2,  8,  '吴十', '牙周治疗',       '完成', 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '全口洁治',                    NULL,    '500.00',  500.00,  '已收费', NOW(), NOW()),
(3,  12, '黄丽', '拔牙',           '完成', 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '拔除左上第二磨牙残根',      '27',    '600.00',  600.00,  '已收费', NOW(), NOW()),
(4,  15, '马云', '全口修复',       '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '上颌种植4颗，下颌种植2颗',  NULL,    '58000.00',58000.00,'已收费', NOW(), NOW()),
(5,  17, '宋江', '补牙',           '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '右上第一磨牙树脂充填',      '16',    '400.00',  400.00,  '已收费', NOW(), NOW()),
(6,  18, '唐艳', '美白',           '完成', 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '诊室美白',                  NULL,    '2000.00', 2000.00, '已收费', NOW(), NOW()),
(7,  19, '韩梅', '根管治疗',       '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '左下第一磨牙根管治疗初诊',  '36',    '1200.00', 1200.00, '已收费', NOW(), NOW()),
(8,  20, '冯刚', '种植牙二期',     '完成', 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '二期手术，安装愈合基台',    '46',    '500.00',  500.00,  '已收费', NOW(), NOW()),
(9,  13, '林强', '种植牙一期',     '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '植入种植体4.0×10mm',        '37',    '8000.00', 8000.00, '已收费', NOW(), NOW()),
(10, 10, '陈二', '取模',           '完成', 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '硅橡胶取模',                NULL,    '200.00',  200.00,  '已收费', NOW(), NOW()),
(11, 1,  '张三', '种植牙一期',     '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '植入种植体4.5×12mm',        '36',    '8500.00', 8500.00, '已收费', NOW(), NOW()),
(12, 3,  '王五', '根管治疗',       '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '左下第一磨牙盖髓充填',      '36',    '600.00',  600.00,  '已收费', NOW(), NOW()),
(13, 5,  '钱七', '种植牙二期',     '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '二期手术，安装愈合基台',    '46',    '500.00',  500.00,  '已收费', NOW(), NOW()),
(14, 7,  '周九', '补牙',           '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 7 DAY), '右上第二磨牙树脂充填',      '17',    '350.00',  350.00,  '已收费', NOW(), NOW()),
(15, 9,  '郑一', '种植牙复诊',     '完成', 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 8 DAY), '修复体调整咬合',            '36',    '0.00',    0.00,    '已收费', NOW(), NOW());

-- ----------------------------
-- 6. 财务记录表 (finances)
-- ----------------------------
TRUNCATE TABLE finances;
INSERT INTO finances (id, patient_id, treatment_id, payment_channel_id, payment_channel_name, name, amount, date, type, biz_type, remark) VALUES
(1,  2,  1,  1, '现金',   '李四-拔牙',        800.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), '收入', '收费', '拔除右下智齿'),
(2,  8,  2,  2, '微信支付','吴十-牙周治疗',    500.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), '收入', '收费', '全口洁治'),
(3,  12, 3,  1, '现金',   '黄丽-拔牙',        600.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), '收入', '收费', '拔除残根'),
(4,  15, 4,  3, '支付宝', '马云-全口种植',    58000.00,DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), '收入', '收费', '全口种植修复'),
(5,  17, 5,  2, '微信支付','宋江-补牙',        400.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), '收入', '收费', '树脂充填'),
(6,  18, 6,  2, '微信支付','唐艳-美白',        2000.00, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), '收入', '收费', '诊室美白'),
(7,  19, 7,  1, '现金',   '韩梅-根管治疗',    1200.00, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '%Y-%m-%d'), '收入', '收费', '根管治疗初诊'),
(8,  20, 8,  2, '微信支付','冯刚-种植牙二期',  500.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '%Y-%m-%d'), '收入', '收费', '二期手术'),
(9,  13, 9,  3, '支付宝', '林强-种植牙一期',  8000.00, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '%Y-%m-%d'), '收入', '收费', '植入种植体'),
(10, 10, 10, 2, '微信支付','陈二-取模',        200.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '%Y-%m-%d'), '收入', '收费', '硅橡胶取模'),
(11, 1,  11, 2, '微信支付','张三-种植牙一期',  8500.00, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '%Y-%m-%d'), '收入', '收费', '植入种植体'),
(12, 3,  12, 1, '现金',   '王五-盖髓充填',    600.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '%Y-%m-%d'), '收入', '收费', '盖髓+树脂充填'),
(13, 5,  13, 2, '微信支付','钱七-种植牙二期',  500.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '%Y-%m-%d'), '收入', '收费', '二期手术'),
(14, 7,  14, 2, '微信支付','周九-补牙',        350.00,  DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '%Y-%m-%d'), '收入', '收费', '树脂充填'),
(15, 4,  NULL,NULL,NULL,   '赵六-洗牙预付',    300.00,  DATE_FORMAT(CURDATE(), '%Y-%m-%d'), '预收款', '预收', '明日洗牙预付定金'),
(16, 6,  NULL,NULL,NULL,   '孙八-正畸复诊费',  200.00,  DATE_FORMAT(CURDATE(), '%Y-%m-%d'), '预收款', '预收', '正畸复诊费'),
(17, NULL,NULL,NULL,NULL,   '牙椅耗材采购',    3200.00, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d'), '支出', '耗材采购', '一次性口腔器械盒200盒'),
(18, NULL,NULL,NULL,NULL,   '器械消毒外包',    1500.00, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '%Y-%m-%d'), '支出', '服务外包', '本月器械消毒费用'),
(19, 11, NULL,NULL,NULL,   '刘明-根管治疗预付',800.00, DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d'), '预收款', '预收', '明日根管治疗预付'),
(20, 14, NULL,NULL,NULL,   '徐静-正畸初诊费',  500.00, DATE_FORMAT(CURDATE(), '%Y-%m-%d'), '退款', '退费', '已取消预约退费');

-- ----------------------------
-- 7. 随访记录表 (patient_followup)
-- ----------------------------
TRUNCATE TABLE patient_followup;
INSERT INTO patient_followup (id, patient_id, doctor_account_id, doctor_name, followup_date, followup_type, summary, next_followup_date, created_at, updated_at, patient_name, patient_phone) VALUES
(1,  2,  3, '张医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '电话', '拔牙术后恢复良好，无出血疼痛', DATE_ADD(CURDATE(), INTERVAL 4 DAY), NOW(), NOW(), '李四', '13800138002'),
(2,  15, 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 5 DAY), '电话', '种植术后肿胀消退，无异常',     DATE_ADD(CURDATE(), INTERVAL 2 DAY), NOW(), NOW(), '马云', '13800138015'),
(3,  1,  2, '李医生', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '电话', '种植伤口愈合良好',             DATE_ADD(CURDATE(), INTERVAL 3 DAY), NOW(), NOW(), '张三', '13800138001'),
(4,  19, 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '电话', '根管治疗后疼痛缓解',           DATE_ADD(CURDATE(), INTERVAL 6 DAY), NOW(), NOW(), '韩梅', '13800138019'),
(5,  13, 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '电话', '种植术后无不适',               DATE_ADD(CURDATE(), INTERVAL 5 DAY), NOW(), NOW(), '林强', '13800138013'),
(6,  8,  3, '张医生', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '复诊', '牙周治疗后出血减少，建议定期洁牙', DATE_ADD(CURDATE(), INTERVAL 30 DAY), NOW(), NOW(), '吴十', '13800138008'),
(7,  20, 3, '张医生', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '电话', '二期手术后愈合良好，两周后取模', DATE_ADD(CURDATE(), INTERVAL 2 DAY), NOW(), NOW(), '冯刚', '13800138020'),
(8,  5,  2, '李医生', DATE_SUB(CURDATE(), INTERVAL 6 DAY), '电话', '二期手术顺利',                 DATE_ADD(CURDATE(), INTERVAL 1 DAY), NOW(), NOW(), '钱七', '13800138005'),
(9,  7,  2, '李医生', DATE_SUB(CURDATE(), INTERVAL 4 DAY), '电话', '补牙后无敏感症状',             DATE_ADD(CURDATE(), INTERVAL 10 DAY), NOW(), NOW(), '周九', '13800138007'),
(10, 17, 2, '李医生', DATE_SUB(CURDATE(), INTERVAL 3 DAY), '电话', '补牙后咬合正常',               DATE_ADD(CURDATE(), INTERVAL 7 DAY), NOW(), NOW(), '宋江', '13800138017');

-- ----------------------------
-- 8. 咨询记录表 (consultation_record)
-- ----------------------------
TRUNCATE TABLE consultation_record;
INSERT INTO consultation_record (id, patient_id, consultation_time, consultation_channel, chief_project, intent_level, handling_result, contact_name, contact_phone, remarks, created_by, created_by_name, created_at, updated_at, patient_name, patient_phone, patient_customer_source, total_deal_amount, has_deal) VALUES
(1,  1,  DATE_SUB(CURDATE(), INTERVAL 10 DAY), '微信', '种植牙', '高', '已预约初诊', '张三', '13800138001', '咨询allon4种植方案', 1, '王管理员', NOW(), NOW(), '张三', '13800138001', '网络搜索', 8500.00, 1),
(2,  2,  DATE_SUB(CURDATE(), INTERVAL 15 DAY), '电话', '拔牙',   '中', '已预约',     '李四', '13800138002', '智齿拔除咨询', 1, '王管理员', NOW(), NOW(), '李四', '13800138002', '朋友介绍', 800.00, 1),
(3,  4,  DATE_SUB(CURDATE(), INTERVAL 5 DAY),  '美团', '洗牙',   '高', '已预约',     '赵六', '13800138004', '团购洗牙套餐', 4, '刘护士',   NOW(), NOW(), '赵六', '13800138004', '美团', 300.00, 1),
(4,  6,  DATE_SUB(CURDATE(), INTERVAL 20 DAY), '电话', '正畸',   '高', '已成交',     '孙八', '13800138006', '隐形矫正咨询，已成交', 1, '王管理员', NOW(), NOW(), '孙八', '13800138006', '网络搜索', 28000.00, 1),
(5,  11, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '微信', '根管治疗','中','已预约',     '刘明', '13800138011', '后牙疼痛咨询', 1, '王管理员', NOW(), NOW(), '刘明', '13800138011', '美团', 1200.00, 0),
(6,  14, DATE_SUB(CURDATE(), INTERVAL 8 DAY),  '到店', '正畸',   '高', '已成交',     '徐静', '13800138014', '隐形矫正方案确认', 1, '王管理员', NOW(), NOW(), '徐静', '13800138014', '自然到店', 32000.00, 1),
(7,  16, DATE_SUB(CURDATE(), INTERVAL 2 DAY),  '美团', '洗牙',   '低', '待跟进',     '朱婷', '13800138016', '咨询洗牙套餐', 4, '刘护士',   NOW(), NOW(), '朱婷', '13800138016', '美团', 0.00, 0),
(8,  18, DATE_SUB(CURDATE(), INTERVAL 6 DAY),  '微信', '美白',   '中', '已预约',     '唐艳', '13800138018', '诊室美白咨询', 1, '王管理员', NOW(), NOW(), '唐艳', '13800138018', '自然到店', 2000.00, 0);

-- ----------------------------
-- 9. 治疗项目库 (treatment_catalog)
-- ----------------------------
TRUNCATE TABLE treatment_catalog;
INSERT INTO treatment_catalog (id, item_name, default_fee, default_content, default_product, status, sort_order, created_at, updated_at) VALUES
(1,  '普通拔牙',         '200.00',  '局部麻醉下拔除患牙',          NULL,         1, 1,  NOW(), NOW()),
(2,  '复杂拔牙（智齿）',  '800.00',  '局部麻醉下拔除阻生智齿，缝合', NULL,         1, 2,  NOW(), NOW()),
(3,  '根管治疗（前牙）',  '800.00',  '根管预备、消毒、充填',        '牙胶尖',      1, 3,  NOW(), NOW()),
(4,  '根管治疗（后牙）',  '1200.00', '根管预备、消毒、充填',        '牙胶尖',      1, 4,  NOW(), NOW()),
(5,  '树脂充填',         '350.00',  '去腐备洞，树脂充填',          '3M树脂',      1, 5,  NOW(), NOW()),
(6,  '全口洁治',         '500.00',  '超声波龈上洁治',             NULL,         1, 6,  NOW(), NOW()),
(7,  '种植牙（单颗）',   '8000.00', '植入种植体，缝合',            '韩国奥齿泰',  1, 7,  NOW(), NOW()),
(8,  '种植牙（欧美）',   '12000.00','植入种植体，缝合',            '瑞士ITI',     1, 8,  NOW(), NOW()),
(9,  '正畸初诊检查',     '200.00',  '口腔检查，拍片，方案设计',    NULL,         1, 9,  NOW(), NOW()),
(10, '隐形矫正',         '32000.00','隐形矫治器矫正',             '隐适美',      1, 10, NOW(), NOW()),
(11, '金属托槽矫正',     '18000.00','金属托槽固定矫正',           NULL,         1, 11, NOW(), NOW()),
(12, '诊室美白',         '2000.00', '35%过氧化氢诊室美白',        NULL,         1, 12, NOW(), NOW()),
(13, '全口种植修复',     '58000.00','上颌4颗+下颌2颗种植修复',     NULL,         1, 13, NOW(), NOW()),
(14, '活动义齿',         '3000.00', '局部活动义齿修复',           NULL,         1, 14, NOW(), NOW()),
(15, '全瓷冠',           '3500.00', '全瓷冠修复',                 '爱尔创',      1, 15, NOW(), NOW());

-- ----------------------------
-- 10. 收款渠道 (payment_channel)
-- ----------------------------
TRUNCATE TABLE payment_channel;
INSERT INTO payment_channel (id, channel_name, channel_type, is_enabled, sort_order, created_at, updated_at) VALUES
(1, '现金',     'CASH',       1, 1, NOW(), NOW()),
(2, '微信支付', 'WECHAT_PAY', 1, 2, NOW(), NOW()),
(3, '支付宝',   'ALIPAY',     1, 3, NOW(), NOW()),
(4, '银行卡',   'BANK_CARD',  1, 4, NOW(), NOW()),
(5, '医保卡',   'INSURANCE',  1, 5, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

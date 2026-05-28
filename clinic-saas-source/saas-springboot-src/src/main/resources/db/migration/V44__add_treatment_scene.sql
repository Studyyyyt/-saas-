-- 治疗场景（病种）配置表
CREATE TABLE IF NOT EXISTS treatment_scene (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '场景名称，如：根管治疗',
    category VARCHAR(50) DEFAULT '其他' COMMENT '分类，如：牙体牙髓、口腔外科、修复科',
    level INT DEFAULT 1 COMMENT '复杂度：1简单 2中等 3复杂',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗场景配置表';

-- 治疗场景步骤表
CREATE TABLE IF NOT EXISTS treatment_scene_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    scene_id BIGINT NOT NULL COMMENT '关联场景ID',
    name VARCHAR(100) NOT NULL COMMENT '步骤名称，如：开髓引流',
    sort_order INT DEFAULT 0 COMMENT '排序',
    forbidden_keywords VARCHAR(500) DEFAULT '' COMMENT '禁止关键词，逗号分隔',
    required_keywords VARCHAR(500) DEFAULT '' COMMENT '必须包含关键词，逗号分隔',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_scene_id (scene_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='治疗场景步骤表';

-- 插入默认治疗场景数据
INSERT INTO treatment_scene (name, category, level, enabled, sort_order) VALUES
('根管治疗', '牙体牙髓', 3, 1, 1),
('简单拔牙', '口腔外科', 1, 1, 2),
('树脂充填', '牙体牙髓', 1, 1, 3),
('种植修复', '种植科', 3, 1, 4),
('牙周基础治疗', '牙周科', 2, 1, 5),
('洁牙', '预防科', 1, 1, 6),
('全冠修复', '修复科', 2, 1, 7),
('正畸初诊', '正畸科', 3, 1, 8),
('儿童涂氟', '预防科', 1, 1, 9),
('一般初诊检查', '综合科', 1, 1, 10);

-- 根管治疗步骤
INSERT INTO treatment_scene_step (scene_id, name, sort_order, forbidden_keywords, required_keywords) VALUES
((SELECT id FROM treatment_scene WHERE name = '根管治疗'), '开髓引流', 1, '根管预备完成,根管充填完成,永久充填,冠修复完成', '开髓,拔髓,麻醉'),
((SELECT id FROM treatment_scene WHERE name = '根管治疗'), '根管预备', 2, '根管充填完成,永久修复,冠修复完成', '根管,预备,工作长度,冲洗'),
((SELECT id FROM treatment_scene WHERE name = '根管治疗'), '根管充填', 3, '', '根充,试尖,充填,严密'),
((SELECT id FROM treatment_scene WHERE name = '根管治疗'), '永久修复', 4, '', '冠修复,桩核,永久');

-- 简单拔牙步骤
INSERT INTO treatment_scene_step (scene_id, name, sort_order, forbidden_keywords, required_keywords) VALUES
((SELECT id FROM treatment_scene WHERE name = '简单拔牙'), '术前检查', 1, '拔除完成', '麻醉,检查,X线'),
((SELECT id FROM treatment_scene WHERE name = '简单拔牙'), '拔牙操作', 2, '', '拔除,牙槽窝,止血'),
((SELECT id FROM treatment_scene WHERE name = '简单拔牙'), '术后医嘱', 3, '', '医嘱,止血,抗感染');

-- 树脂充填步骤
INSERT INTO treatment_scene_step (scene_id, name, sort_order, forbidden_keywords, required_keywords) VALUES
((SELECT id FROM treatment_scene WHERE name = '树脂充填'), '去腐备洞', 1, '充填完成', '去腐,备洞,隔湿'),
((SELECT id FROM treatment_scene WHERE name = '树脂充填'), '树脂充填', 2, '', '树脂,充填,固化,调颌');

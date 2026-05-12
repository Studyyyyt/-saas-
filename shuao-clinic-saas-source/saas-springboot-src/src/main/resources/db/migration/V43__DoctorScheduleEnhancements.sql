-- 医生排班表增强：新增班次类型字段
ALTER TABLE doctors ADD COLUMN shift_type VARCHAR(20) DEFAULT NULL COMMENT '班次类型：morning/evening/rest/custom' AFTER status;

-- 将现有数据根据 status 迁移到 shift_type
UPDATE doctors SET shift_type = 'morning' WHERE status = '早班';
UPDATE doctors SET shift_type = 'evening' WHERE status = '晚班';
UPDATE doctors SET shift_type = 'rest' WHERE status = '休息';

-- 排班模板表
CREATE TABLE IF NOT EXISTS shift_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    doctor_name VARCHAR(50) DEFAULT NULL COMMENT '来源医生姓名',
    pattern_json TEXT NOT NULL COMMENT '模板模式JSON，如{"1":"morning","2":"evening"}',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班模板表';

-- 插入排班模板测试数据
INSERT INTO shift_template (name, doctor_name, pattern_json) VALUES
('标准早班周', NULL, '{"1":"morning","2":"morning","3":"morning","4":"morning","5":"morning","6":"rest","7":"rest"}'),
('早晚轮班周', NULL, '{"1":"morning","2":"evening","3":"morning","4":"evening","5":"morning","6":"rest","7":"rest"}'),
('全晚班周', NULL, '{"1":"evening","2":"evening","3":"evening","4":"evening","5":"evening","6":"rest","7":"rest"}'),
('做二休一', NULL, '{"1":"morning","2":"morning","3":"rest","4":"evening","5":"evening","6":"rest","7":"morning"}'),
('周末值班', NULL, '{"1":"rest","2":"morning","3":"morning","4":"morning","5":"morning","6":"morning","7":"evening"}');

CREATE TABLE medical_record_phrases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型：chief_complaint/present_illness_history/past_history/examination/diagnosis/treatment_plan/medical_advice/notes',
    content VARCHAR(500) NOT NULL COMMENT '词条内容',
    category VARCHAR(50) DEFAULT '' COMMENT '词条分类，如：口腔内科/口腔外科/修复/正畸',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_field_type_status (field_type, status),
    KEY idx_category (category),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历常用词条';

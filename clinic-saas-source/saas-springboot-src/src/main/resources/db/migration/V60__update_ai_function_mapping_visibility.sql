-- AI 系统功能显示控制拆分：支持分别控制所在页面和首页的显示
ALTER TABLE ai_function_mapping
    CHANGE COLUMN is_visible is_visible_on_page TINYINT(1) DEFAULT 1 COMMENT '是否在所在页面显示入口 0-隐藏 1-显示',
    ADD COLUMN is_visible_on_home TINYINT(1) DEFAULT 1 COMMENT '是否在首页AI下拉框显示 0-隐藏 1-显示' AFTER is_visible_on_page;

-- 现有数据迁移：首页显示默认跟随原页面显示值
UPDATE ai_function_mapping SET is_visible_on_home = is_visible_on_page;

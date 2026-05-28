-- AI 总览页面功能扩展：注册新增的 AI 功能开关

INSERT INTO ai_function_config (function_key, function_name, page_path, icon, is_enabled, model_name, sort_order) VALUES
('lab-order-analysis', '加工订单 AI 分析', '加工订单', '🤖', 1, '-', 6),
('lab-factory-analysis', '加工厂 AI 分析', '加工厂档案', '🤖', 1, '-', 7),
('ad-spending-analysis', '广告投放 AI 分析', '广告投放', '🤖', 1, '-', 8),
('doctor-schedule', '医生排班 AI 建议', '医生排班', '🤖', 1, '-', 9)
ON DUPLICATE KEY UPDATE
function_name = VALUES(function_name),
page_path = VALUES(page_path),
icon = VALUES(icon),
sort_order = VALUES(sort_order);

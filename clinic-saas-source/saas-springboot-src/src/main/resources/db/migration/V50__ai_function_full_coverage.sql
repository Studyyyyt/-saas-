-- AI 功能全面覆盖迁移脚本
-- 在 ai_function_config 表中注册 13 个新增 AI 功能配置

INSERT INTO ai_function_config (function_key, function_name, page_path, icon, is_enabled, model_name, sort_order) VALUES
('appointment-assist', 'AI 预约辅助', '预约视图', '📅', 0, '-', 10),
('followup-assist', 'AI 回访辅助', '回访管理', '📞', 0, '-', 11),
('consultation-assist', 'AI 咨询辅助', '咨询记录', '💬', 0, '-', 12),
('consultation-dashboard', 'AI 咨询分析', '咨询看板', '📈', 0, '-', 13),
('treatment-assist', 'AI 治疗辅助', '治疗管理', '🩺', 0, '-', 14),
('treatment-record-assist', 'AI 治疗记录辅助', '治疗记录', '📝', 0, '-', 15),
('financial-analysis', 'AI 财务分析', '财务流水', '💰', 0, '-', 16),
('monthly-bill-analysis', 'AI 月度账单分析', '月度账单', '📊', 0, '-', 17),
('lab-statistics-analysis', 'AI 加工统计', '加工统计', '🔧', 0, '-', 18),
('material-category-assist', 'AI 耗材分类辅助', '耗材分类', '📦', 0, '-', 19),
('material-inventory-assist', 'AI 库存辅助', '耗材档案', '🏭', 0, '-', 20),
('material-purchase-assist', 'AI 采购辅助', '采购记录', '🛒', 0, '-', 21),
('material-statistics-analysis', 'AI 耗材统计', '耗材统计', '📉', 0, '-', 22)
ON DUPLICATE KEY UPDATE
function_name = VALUES(function_name),
page_path = VALUES(page_path),
icon = VALUES(icon),
sort_order = VALUES(sort_order);

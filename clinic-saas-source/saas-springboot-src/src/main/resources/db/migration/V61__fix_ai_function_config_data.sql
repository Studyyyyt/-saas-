-- ============================================================
-- 修复 ai_function_config 表中的乱码数据
-- 补充功能名称和所在页面信息，便于 AI 总览页展示
-- ============================================================

UPDATE ai_function_config SET function_name = '首页 AI 助手', page_path = '首页 AI 助手' WHERE function_key = 'home-assistant';
UPDATE ai_function_config SET function_name = '病历扩写', page_path = '病历编辑页、患者详情页' WHERE function_key = 'medical-expand';
UPDATE ai_function_config SET function_name = '患者分析', page_path = '首页 AI 助手' WHERE function_key = 'patient-insight';
UPDATE ai_function_config SET function_name = '回访统计', page_path = '首页 AI 助手' WHERE function_key = 'followup-generate';
UPDATE ai_function_config SET function_name = '经营分析', page_path = '首页 AI 助手' WHERE function_key = 'business-analysis';
UPDATE ai_function_config SET function_name = '加工订单分析', page_path = '加工订单页' WHERE function_key = 'lab-order-analysis';
UPDATE ai_function_config SET function_name = '加工厂分析', page_path = '加工厂页' WHERE function_key = 'lab-factory-analysis';
UPDATE ai_function_config SET function_name = '广告投放分析', page_path = '广告投放页' WHERE function_key = 'ad-spending-analysis';
UPDATE ai_function_config SET function_name = '医生排班', page_path = '医生排班页' WHERE function_key = 'doctor-schedule';
UPDATE ai_function_config SET function_name = '预约辅助', page_path = '预约视图页' WHERE function_key = 'appointment-assist';
UPDATE ai_function_config SET function_name = '回访辅助', page_path = '回访管理页' WHERE function_key = 'followup-assist';
UPDATE ai_function_config SET function_name = '咨询辅助', page_path = '咨询记录页' WHERE function_key = 'consultation-assist';
UPDATE ai_function_config SET function_name = '咨询看板', page_path = '咨询看板页' WHERE function_key = 'consultation-dashboard';
UPDATE ai_function_config SET function_name = '治疗辅助', page_path = '治疗页面' WHERE function_key = 'treatment-assist';
UPDATE ai_function_config SET function_name = '治疗记录辅助', page_path = '治疗记录页' WHERE function_key = 'treatment-record-assist';
UPDATE ai_function_config SET function_name = '财务分析', page_path = '财务分析页' WHERE function_key = 'financial-analysis';
UPDATE ai_function_config SET function_name = '月度账单分析', page_path = '月度账单页' WHERE function_key = 'monthly-bill-analysis';
UPDATE ai_function_config SET function_name = '加工统计', page_path = '加工统计页' WHERE function_key = 'lab-statistics-analysis';
UPDATE ai_function_config SET function_name = '耗材分类辅助', page_path = '耗材分类页' WHERE function_key = 'material-category-assist';
UPDATE ai_function_config SET function_name = '耗材库存辅助', page_path = '耗材档案页' WHERE function_key = 'material-inventory-assist';
UPDATE ai_function_config SET function_name = '耗材采购辅助', page_path = '采购记录页' WHERE function_key = 'material-purchase-assist';
UPDATE ai_function_config SET function_name = '耗材统计', page_path = '耗材统计页' WHERE function_key = 'material-statistics-analysis';

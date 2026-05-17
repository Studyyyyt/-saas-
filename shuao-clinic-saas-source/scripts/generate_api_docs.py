#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
API 接口文档自动生成脚本
扫描所有 Controller.java 文件，提取接口信息并生成按业务分类的 Markdown 文档
"""

import os
import re
from pathlib import Path
from collections import defaultdict

# 项目根目录
PROJECT_ROOT = Path("/Users/cherry/Downloads/口腔saas管理系统开发/shuao-clinic-saas-source")
CONTROLLER_DIR = PROJECT_ROOT / "saas-springboot-src/src/main/java/com/example/springboot/controller"
OUTPUT_DIR = Path("/Users/cherry/Downloads/口腔saas管理系统开发/docs/03-接口文档")

# Controller → 分类 映射表（按前端导航菜单分类）
CATEGORY_MAP = {
    # 01-登录认证
    "AuthController": "01-登录认证",

    # 02-首页工作台
    "DoctorHomeReminderDismissalController": "02-首页工作台",
    "OpenDataController": "02-首页工作台",
    "FileTransferController": "02-首页工作台",

    # 03-预约管理
    "AppointmentController": "03-预约管理",

    # 04-治疗管理
    "TreatmentController": "04-治疗管理",
    "TreatmentOperationController": "04-治疗管理",
    "TreatmentPlanController": "04-治疗管理",
    "TreatmentCatalogController": "04-治疗管理",
    "TreatmentProjectController": "04-治疗管理",
    "TreatmentProjectCategoryController": "04-治疗管理",
    "TreatmentSceneController": "04-治疗管理",

    # 05-医生排班
    "DoctorController": "05-医生排班",

    # 06-患者管理
    "PatientController": "06-患者管理",
    "PatientDetailController": "06-患者管理",
    "PatientCustomGroupController": "06-患者管理",
    "PatientInsightController": "06-患者管理",
    "PatientRiskTagController": "06-患者管理",

    # 07-病历管理
    "MedicalRecordController": "07-病历管理",
    "MedicalRecordTemplateController": "07-病历管理",
    "MedicalRecordOperationController": "07-病历管理",
    "MedicalRecordPhraseController": "07-病历管理",
    "MedicalRecordAIController": "07-病历管理",

    # 08-知情同意
    "PatientConsentController": "08-知情同意",
    "ConsentTemplateController": "08-知情同意",

    # 09-回访管理
    "PatientFollowupController": "09-回访管理",
    "ConsultationFollowupController": "09-回访管理",

    # 10-咨询管理
    "ConsultationRecordController": "10-咨询管理",

    # 11-财务管理
    "FinanceController": "11-财务管理",
    "PaymentChannelController": "11-财务管理",

    # 12-保险管理
    "InsuranceController": "12-保险管理",

    # 13-加工管理
    "LabFactoryController": "13-加工管理",
    "LabOrderController": "13-加工管理",
    "LabBillController": "13-加工管理",
    "LabStatisticsController": "13-加工管理",

    # 14-耗材管理
    "MaterialController": "14-耗材管理",
    "MaterialCategoryController": "14-耗材管理",
    "MaterialPurchaseController": "14-耗材管理",
    "MaterialStatisticsController": "14-耗材管理",
    "InventoryController": "14-耗材管理",
    "PurchaseController": "14-耗材管理",

    # 15-广告支出
    "AdvertisingSpendingController": "15-广告支出",

    # 16-账号权限
    "AccountController": "16-账号权限",
    "RoleMenuPermissionController": "16-账号权限",

    # 17-影像管理
    "PatientImageController": "17-影像管理",

    # 18-AI智能中心
    "AiProxyController": "18-AI智能中心",
    "AiAgentConfigController": "18-AI智能中心",
    "AiConfigController": "18-AI智能中心",
    "AiHubController": "18-AI智能中心",
    "AiModelProviderController": "18-AI智能中心",
    "ApiKeyController": "18-AI智能中心",
    "BusinessDailyAnalysisController": "18-AI智能中心",

    # 19-系统设置
    "ApiDocumentationController": "19-系统设置",
    "WebhookNotificationController": "19-系统设置",
    "SpaForwardController": "19-系统设置",
}

# 正则表达式
# 提取类名
CLASS_NAME_RE = re.compile(r'public\s+class\s+(\w+)')
# 提取类级别的 @RequestMapping
CLASS_MAPPING_RE = re.compile(r'@RequestMapping\s*\(\s*"([^"]*)"\s*\)')
# 提取方法级别的 HTTP 方法注解
METHOD_ANNOTATION_RE = re.compile(
    r'@((Get|Post|Put|Delete|Patch)Mapping)\s*\(\s*(?:value\s*=\s*)?"([^"]*)".*?\)',
    re.DOTALL
)
# 简单版本：只提取路径
METHOD_SIMPLE_RE = re.compile(
    r'@((Get|Post|Put|Delete|Patch)Mapping)\s*\(\s*"([^"]*)"\s*\)'
)
# 更宽松的匹配，允许 value= 和 produces= 等
METHOD_LOOSE_RE = re.compile(
    r'@((Get|Post|Put|Delete|Patch)Mapping)\s*\([^)]*?"([^"]*)"[^)]*\)',
    re.DOTALL
)
# 提取方法签名行
METHOD_SIG_RE = re.compile(
    r'public\s+\S+\s+(\w+)\s*\((.*?)\)\s*\{',
    re.DOTALL
)


def extract_mapping_from_line(line):
    """从单行提取 @XxxMapping("/path")"""
    for pattern in [METHOD_SIMPLE_RE, METHOD_LOOSE_RE]:
        m = pattern.search(line)
        if m:
            return m.group(2).upper(), m.group(3)
    return None, None


def parse_controller_file(filepath):
    """解析单个 Controller 文件，返回接口列表"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 提取类名
    class_match = CLASS_NAME_RE.search(content)
    if not class_match:
        return []
    class_name = class_match.group(1)

    # 提取类级别的路径前缀
    prefix = ""
    class_mapping_match = CLASS_MAPPING_RE.search(content)
    if class_mapping_match:
        prefix = class_mapping_match.group(1)

    # 按行分割，寻找方法注解（跳过被注释掉的代码）
    lines = content.split('\n')
    endpoints = []
    i = 0
    while i < len(lines):
        raw_line = lines[i]
        line = raw_line.strip()

        # 跳过空行和纯注释行（以 // 开头，前面只有空白字符）
        if not line or raw_line.lstrip().startswith('//'):
            i += 1
            continue

        # 检查是否是 HTTP 方法注解行
        http_method, path = extract_mapping_from_line(line)
        if http_method and path:
            # 向后查找方法签名（可能跨多行）
            method_name = ""
            params_str = ""
            j = i + 1
            buffer_lines = []
            while j < len(lines) and j < i + 20:
                ctx_raw = lines[j]
                ctx_line = ctx_raw.strip()

                # 如果遇到下一个注解或下一个 public class，停止查找
                if ctx_line.startswith('public class '):
                    break

                # 跳过注释行
                if ctx_raw.lstrip().startswith('//'):
                    j += 1
                    continue

                buffer_lines.append(ctx_line)

                # 如果行包含 public 且以 { 结尾，尝试提取签名
                if 'public ' in ' '.join(buffer_lines) and '{' in ' '.join(buffer_lines):
                    combined = ' '.join(buffer_lines)
                    sig_match = METHOD_SIG_RE.search(combined)
                    if sig_match:
                        method_name = sig_match.group(1)
                        params_str = sig_match.group(2)
                        break
                j += 1

            # 组装完整路径
            full_path = prefix + path
            full_path = full_path.replace('//', '/')

            # 解析参数
            params = parse_params(params_str)

            # 如果方法名为空，用路径最后一段作为备用名称
            if not method_name:
                method_name = path.strip('/').replace('/', '_').replace('{', '').replace('}', '')
                if not method_name:
                    method_name = http_method.lower() + "_request"

            endpoints.append({
                'class_name': class_name,
                'http_method': http_method,
                'path': full_path,
                'method_name': method_name,
                'params': params,
                'java_line': lines[i] if i < len(lines) else ""
            })
        i += 1

    return endpoints


def parse_params(params_str):
    """解析 Java 方法参数字符串，提取参数列表"""
    if not params_str or not params_str.strip():
        return []

    params = []
    # 先去除换行和多余空格
    params_str = ' '.join(params_str.split())

    # 按逗号分割参数（考虑泛型嵌套）
    raw_params = []
    depth = 0
    current = ""
    for ch in params_str:
        if ch == '<':
            depth += 1
            current += ch
        elif ch == '>':
            depth -= 1
            current += ch
        elif ch == ',' and depth == 0:
            raw_params.append(current.strip())
            current = ""
        else:
            current += ch
    if current.strip():
        raw_params.append(current.strip())

    for rp in raw_params:
        if not rp:
            continue

        param = {
            'name': '',
            'type': '',
            'location': 'Body',
            'required': False,
            'description': ''
        }

        # 检测注解并推断参数位置
        if '@PathVariable' in rp:
            param['location'] = 'Path'
            param['required'] = 'required = false' not in rp
        elif '@RequestParam' in rp:
            param['location'] = 'Query'
            param['required'] = 'required = false' not in rp
        elif '@RequestBody' in rp:
            param['location'] = 'Body'
        elif '@RequestHeader' in rp:
            param['location'] = 'Header'
        elif '@ModelAttribute' in rp:
            param['location'] = 'Query'

        # 移除注解部分：从 @ 开始到匹配的 ) 或空格结束
        # 示例：@RequestParam(required = false) Long accountId
        # 步骤1：移除 final 关键字
        cleaned = rp.replace('final ', '').strip()
        # 步骤2：用正则移除所有注解 @Xxx(...)
        cleaned = re.sub(r'@\w+(?:\([^)]*\))?', '', cleaned).strip()
        # 步骤3：移除残留的关键字和符号
        cleaned = re.sub(r'\brequired\s*=\s*(?:true|false)\b', '', cleaned)
        cleaned = re.sub(r'\bdefaultValue\s*=\s*"[^"]*"', '', cleaned)
        cleaned = cleaned.replace('=', ' ').replace(')', ' ').replace('(', ' ')
        cleaned = ' '.join(cleaned.split())  # 去多余空格

        if cleaned:
            parts = cleaned.split()
            if parts:
                param['name'] = parts[-1].rstrip(',)')
                param['type'] = ' '.join(parts[:-1]) if len(parts) > 1 else 'Object'

        params.append(param)

    return params


def translate_controller_name(name):
    """Controller 英文名称 → 中文名称"""
    mapping = {
        "AuthController": "认证控制器",
        "AccountController": "账号控制器",
        "RoleMenuPermissionController": "角色菜单权限控制器",
        "PatientController": "患者控制器",
        "PatientDetailController": "患者详情控制器",
        "PatientCustomGroupController": "患者分组控制器",
        "PatientInsightController": "患者洞察控制器",
        "PatientRiskTagController": "患者风险标签控制器",
        "PatientImageController": "患者影像控制器",
        "PatientFollowupController": "患者随访控制器",
        "PatientConsentController": "患者知情同意控制器",
        "ConsultationRecordController": "咨询记录控制器",
        "ConsultationFollowupController": "咨询跟进控制器",
        "AppointmentController": "预约控制器",
        "DoctorController": "医生控制器",
        "MedicalRecordController": "病历控制器",
        "MedicalRecordTemplateController": "病历模板控制器",
        "MedicalRecordOperationController": "病历操作控制器",
        "MedicalRecordPhraseController": "病历词条控制器",
        "MedicalRecordAIController": "病历AI控制器",
        "TreatmentController": "治疗控制器",
        "TreatmentOperationController": "治疗操作控制器",
        "TreatmentPlanController": "治疗计划控制器",
        "TreatmentCatalogController": "治疗目录控制器",
        "TreatmentProjectController": "治疗项目控制器",
        "TreatmentProjectCategoryController": "治疗项目分类控制器",
        "TreatmentSceneController": "治疗场景控制器",
        "FinanceController": "财务控制器",
        "PaymentChannelController": "支付渠道控制器",
        "AdvertisingSpendingController": "广告支出控制器",
        "InsuranceController": "保险控制器",
        "InventoryController": "库存控制器",
        "PurchaseController": "采购控制器",
        "MaterialController": "耗材控制器",
        "MaterialCategoryController": "耗材分类控制器",
        "MaterialPurchaseController": "耗材采购控制器",
        "MaterialStatisticsController": "耗材统计控制器",
        "LabFactoryController": "加工厂控制器",
        "LabOrderController": "加工订单控制器",
        "LabBillController": "加工账单控制器",
        "LabStatisticsController": "加工统计控制器",
        "ConsentTemplateController": "同意书模板控制器",
        "AiProxyController": "AI代理控制器",
        "AiAgentConfigController": "AI代理配置控制器",
        "AiConfigController": "AI配置控制器",
        "AiHubController": "AI中心控制器",
        "AiModelProviderController": "AI模型提供商控制器",
        "ApiKeyController": "API密钥控制器",
        "BusinessDailyAnalysisController": "业务日常分析控制器",
        "ApiDocumentationController": "API文档控制器",
        "WebhookNotificationController": "Webhook通知控制器",
        "SpaForwardController": "SPA转发控制器",
        "FileTransferController": "文件传输控制器",
        "OpenDataController": "开放数据控制器",
        "DoctorHomeReminderDismissalController": "医生首页提醒关闭控制器",
    }
    return mapping.get(name, name)


def translate_method_name(name, path):
    """方法名 → 中文说明（基于常见命名规则自动推断）"""
    if not name:
        return "接口"

    # 常见名词后缀映射（用于 getXxx / saveXxx 等 camelCase 方法名）
    noun_map = {
        "Config": "配置", "config": "配置",
        "Detail": "详情", "detail": "详情",
        "List": "列表", "list": "列表",
        "Record": "记录", "record": "记录",
        "Template": "模板", "template": "模板",
        "Image": "影像", "image": "影像",
        "File": "文件", "file": "文件",
        "Patient": "患者", "patient": "患者",
        "Doctor": "医生", "doctor": "医生",
        "Account": "账号", "account": "账号",
        "Role": "角色", "role": "角色",
        "Menu": "菜单", "menu": "菜单",
        "Permission": "权限", "permission": "权限",
        "Profile": "档案", "profile": "档案",
        "Status": "状态", "status": "状态",
        "History": "历史", "history": "历史",
        "Log": "日志", "log": "日志",
        "Overview": "概览", "overview": "概览",
        "Dashboard": "仪表盘", "dashboard": "仪表盘",
        "Analysis": "分析", "analysis": "分析",
        "Statistics": "统计", "statistics": "统计",
        "Report": "报表", "report": "报表",
        "Chart": "图表", "chart": "图表",
        "Setting": "设置", "setting": "设置",
        "Scene": "场景", "scene": "场景",
        "Step": "步骤", "step": "步骤",
        "Key": "密钥/键", "key": "密钥/键",
        "Tag": "标签", "tag": "标签",
        "Group": "分组", "group": "分组",
        "Category": "分类", "category": "分类",
        "Catalog": "目录", "catalog": "目录",
        "Project": "项目", "project": "项目",
        "Factory": "工厂", "factory": "工厂",
        "Order": "订单", "order": "订单",
        "Bill": "账单", "bill": "账单",
        "Invoice": "发票", "invoice": "发票",
        "Material": "耗材", "material": "耗材",
        "Inventory": "库存", "inventory": "库存",
        "Purchase": "采购", "purchase": "采购",
        "Supplier": "供应商", "supplier": "供应商",
        "Brand": "品牌", "brand": "品牌",
        "Consent": "同意书", "consent": "同意书",
        "Followup": "随访", "followup": "随访",
        "Insight": "洞察", "insight": "洞察",
        "Reminder": "提醒", "reminder": "提醒",
        "Advice": "医嘱", "advice": "医嘱",
        "Diagnosis": "诊断", "diagnosis": "诊断",
        "Treatment": "治疗", "treatment": "治疗",
        "Plan": "计划", "plan": "计划",
        "Phrase": "词条", "phrase": "词条",
        "Operation": "操作", "operation": "操作",
        "Charge": "收费", "charge": "收费",
        "Refund": "退款", "refund": "退款",
        "Payment": "支付", "payment": "支付",
        "Channel": "渠道", "channel": "渠道",
        "Expense": "支出", "expense": "支出",
        "Insurance": "保险", "insurance": "保险",
        "Settlement": "结算", "settlement": "结算",
        "Provider": "提供商", "provider": "提供商",
        "Model": "模型", "model": "模型",
        "Agent": "代理", "agent": "代理",
        "Function": "功能", "function": "功能",
        "Session": "会话", "session": "会话",
        "Memory": "记忆", "memory": "记忆",
        "Message": "消息", "message": "消息",
        "Alert": "告警", "alert": "告警",
        "Weekly": "周报", "weekly": "周报",
        "Monthly": "月报", "monthly": "月报",
        "Preview": "预览", "preview": "预览",
        "Prompt": "提示词", "prompt": "提示词",
    }

    # 常见动词前缀映射
    verb_map = {
        "selectAll": "查询全部",
        "selectById": "根据ID查询",
        "selectByName": "根据名称查询",
        "selectByPatientId": "根据患者ID查询",
        "selectEnabled": "查询已启用",
        "selectLowStock": "查询低库存",
        "search": "搜索",
        "add": "新增",
        "edit": "编辑",
        "update": "更新",
        "delete": "删除",
        "deleteBatch": "批量删除",
        "get": "获取",
        "save": "保存",
        "upload": "上传",
        "download": "下载",
        "export": "导出",
        "import": "导入",
        "login": "登录",
        "register": "注册",
        "overview": "概览统计",
        "dashboard": "仪表盘",
        "cancel": "取消",
        "charge": "收费",
        "refund": "退款",
        "link": "关联",
        "linkPatient": "关联患者",
        "match": "匹配",
        "matchPatientByPhone": "根据手机号匹配患者",
        "matchForPatientCreate": "匹配患者用于新建",
        "assign": "分配",
        "dismiss": "关闭/忽略",
        "send": "发送",
        "preview": "预览",
        "expand": "扩写/展开",
        "test": "测试",
        "run": "执行",
        "scan": "扫描",
        "probe": "探测",
        "regenerate": "重新生成",
        "notify": "通知",
        "mock": "模拟",
        "batchAdd": "批量新增",
        "batchSave": "批量保存",
        "batchStatus": "批量更新状态",
        "updateStatus": "更新状态",
        "markSkip": "标记跳过",
        "void": "作废",
        "issue": "下发/签发",
    }

    # 精确匹配
    if name in verb_map:
        return verb_map[name]

    # 前缀匹配：先匹配最长的前缀
    matched_verb = None
    matched_verb_len = 0
    for eng, chn in sorted(verb_map.items(), key=lambda x: -len(x[0])):
        if name.startswith(eng):
            if len(eng) > matched_verb_len:
                matched_verb = chn
                matched_verb_len = len(eng)

    if matched_verb:
        suffix = name[matched_verb_len:]
        if suffix:
            # 尝试翻译后缀名词
            suffix_ch = noun_map.get(suffix, suffix)
            return f"{matched_verb}{suffix_ch}"
        return matched_verb

    # 兜底：根据路径推断
    if path.endswith("/add"):
        return "新增"
    if path.endswith("/edit"):
        return "编辑"
    if path.endswith("/delete/{id}") or path.endswith("/delete"):
        return "删除"
    if path.endswith("/search"):
        return "搜索"
    if "selectAll" in name or name.startswith("selectAll"):
        return "查询全部"
    if "selectBy" in name:
        return "条件查询"
    if "By" in name:
        return "查询"
    if name.lower().startswith("get"):
        return "获取"

    return name


def generate_curl_example(endpoint):
    """生成 curl 请求示例"""
    method = endpoint['http_method']
    path = endpoint['path']
    params = endpoint['params']

    lines = [f"curl -X {method} 'http://localhost:8080{path}'"]

    # 如果有 Body 参数，添加 Content-Type 和 JSON 数据
    body_params = [p for p in params if p['location'] == 'Body']
    query_params = [p for p in params if p['location'] == 'Query']
    path_params = [p for p in params if p['location'] == 'Path']

    if body_params:
        lines.append("  -H 'Content-Type: application/json'")

    # 如果有 Query 参数，拼接到 URL
    if query_params:
        query_parts = []
        for p in query_params:
            query_parts.append(f"{p['name']}={p['name']}_value")
        lines[0] = f"curl -X {method} 'http://localhost:8080{path}?{'&'.join(query_parts)}'"

    # Body JSON
    if body_params:
        body_json = {}
        for p in body_params:
            # 如果是 Map 类型，放一个示例对象
            if 'Map' in p['type']:
                body_json = {"key": "value"}
            elif p['type'] in ['String', 'string']:
                body_json[p['name']] = "string_value"
            elif p['type'] in ['int', 'Integer', 'Long', 'long']:
                body_json[p['name']] = 1
            elif p['type'] in ['boolean', 'Boolean']:
                body_json[p['name']] = True
            else:
                body_json[p['name']] = "..."
        import json
        json_str = json.dumps(body_json, ensure_ascii=False, indent=2)
        lines.append(f"  -d '{json_str}'")

    return ' \\\n'.join(lines)


def generate_category_doc(category_name, endpoints):
    """生成分类 Markdown 文档"""
    lines = []
    lines.append(f"# {category_name}")
    lines.append("")
    lines.append(f"> 本分类共 {len(endpoints)} 个接口")
    lines.append("")

    # 按 Controller 分组
    by_controller = defaultdict(list)
    for ep in endpoints:
        by_controller[ep['class_name']].append(ep)

    for controller_name, controller_eps in sorted(by_controller.items()):
        controller_chinese = translate_controller_name(controller_name)
        lines.append(f"## {controller_name}（{controller_chinese}）")
        lines.append("")

        for idx, ep in enumerate(controller_eps, 1):
            method_chinese = translate_method_name(ep['method_name'], ep['path'])
            lines.append(f"### {idx}. {ep['method_name']}（{method_chinese}）")
            lines.append("")
            lines.append(f"- **请求方式**：{ep['http_method']}")
            lines.append(f"- **请求路径**：`{ep['path']}`")
            lines.append(f"- **Controller**：{controller_name}（{controller_chinese}）")
            lines.append("")

            # 请求参数
            if ep['params']:
                lines.append("**请求参数**：")
                lines.append("")
                lines.append("| 参数名 | 位置 | 类型 | 必填 | 说明 |")
                lines.append("|--------|------|------|------|------|")
                for p in ep['params']:
                    req = "是" if p['required'] else "否"
                    desc = p['description'] if p['description'] else "-"
                    lines.append(f"| {p['name']} | {p['location']} | {p['type']} | {req} | {desc} |")
                lines.append("")
            else:
                lines.append("**请求参数**：无")
                lines.append("")

            # curl 示例
            lines.append("**curl 示例**：")
            lines.append("")
            lines.append("```bash")
            lines.append(generate_curl_example(ep))
            lines.append("```")
            lines.append("")

            # 响应说明
            lines.append("**响应说明**：")
            lines.append("")
            lines.append("```json")
            lines.append("{")
            lines.append('  "code": "200",')
            lines.append('  "msg": "success",')
            lines.append('  "data": { ... }')
            lines.append("}")
            lines.append("```")
            lines.append("")
            lines.append("---")
            lines.append("")

    return '\n'.join(lines)


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # 收集所有接口
    all_endpoints = []
    for filepath in sorted(CONTROLLER_DIR.glob("*.java")):
        endpoints = parse_controller_file(filepath)
        all_endpoints.extend(endpoints)

    print(f"共扫描到 {len(all_endpoints)} 个接口")

    # 按分类分组
    by_category = defaultdict(list)
    unmatched = []
    for ep in all_endpoints:
        controller = ep['class_name']
        category = CATEGORY_MAP.get(controller)
        if category:
            by_category[category].append(ep)
        else:
            unmatched.append(ep)

    # 未匹配的放入系统设置
    if unmatched:
        for ep in unmatched:
            by_category["19-系统设置"].append(ep)
        print(f"警告：{len(unmatched)} 个接口未匹配分类，已归入系统设置")

    # 生成每个分类的文档
    total = 0
    for category in sorted(by_category.keys()):
        endpoints = by_category[category]
        total += len(endpoints)
        doc_content = generate_category_doc(category, endpoints)
        filename = f"{category}.md"
        output_path = OUTPUT_DIR / filename
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(doc_content)
        print(f"已生成：{filename} ({len(endpoints)} 个接口)")

    print(f"\n文档生成完毕！总计 {total} 个接口，输出目录：{OUTPUT_DIR}")


if __name__ == "__main__":
    main()

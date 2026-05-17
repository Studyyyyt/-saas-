#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
增强版 API 接口文档自动生成脚本 V2
- 自动扫描并展开 Entity/DTO 内部字段
- 智能推断响应数据结构并生成具体 JSON 示例
- 按前端导航菜单分类，完整中文输出
"""

import os
import re
import json
from pathlib import Path
from collections import defaultdict

PROJECT_ROOT = Path("/Users/cherry/Downloads/口腔saas管理系统开发/shuao-clinic-saas-source")
CONTROLLER_DIR = PROJECT_ROOT / "saas-springboot-src/src/main/java/com/example/springboot/controller"
ENTITY_DIR = PROJECT_ROOT / "saas-springboot-src/src/main/java/com/example/springboot/entity"
OUTPUT_DIR = Path("/Users/cherry/Downloads/口腔saas管理系统开发/docs/03-接口文档")

CATEGORY_MAP = {
    "AuthController": "01-登录认证",
    "DoctorHomeReminderDismissalController": "02-首页工作台",
    "OpenDataController": "02-首页工作台",
    "FileTransferController": "02-首页工作台",
    "AppointmentController": "03-预约管理",
    "TreatmentController": "04-治疗管理",
    "TreatmentOperationController": "04-治疗管理",
    "TreatmentPlanController": "04-治疗管理",
    "TreatmentCatalogController": "04-治疗管理",
    "TreatmentProjectController": "04-治疗管理",
    "TreatmentProjectCategoryController": "04-治疗管理",
    "TreatmentSceneController": "04-治疗管理",
    "DoctorController": "05-医生排班",
    "PatientController": "06-患者管理",
    "PatientDetailController": "06-患者管理",
    "PatientCustomGroupController": "06-患者管理",
    "PatientInsightController": "06-患者管理",
    "PatientRiskTagController": "06-患者管理",
    "MedicalRecordController": "07-病历管理",
    "MedicalRecordTemplateController": "07-病历管理",
    "MedicalRecordOperationController": "07-病历管理",
    "MedicalRecordPhraseController": "07-病历管理",
    "MedicalRecordAIController": "07-病历管理",
    "PatientConsentController": "08-知情同意",
    "ConsentTemplateController": "08-知情同意",
    "PatientFollowupController": "09-回访管理",
    "ConsultationFollowupController": "09-回访管理",
    "ConsultationRecordController": "10-咨询管理",
    "FinanceController": "11-财务管理",
    "PaymentChannelController": "11-财务管理",
    "InsuranceController": "12-保险管理",
    "LabFactoryController": "13-加工管理",
    "LabOrderController": "13-加工管理",
    "LabBillController": "13-加工管理",
    "LabStatisticsController": "13-加工管理",
    "MaterialController": "14-耗材管理",
    "MaterialCategoryController": "14-耗材管理",
    "MaterialPurchaseController": "14-耗材管理",
    "MaterialStatisticsController": "14-耗材管理",
    "InventoryController": "14-耗材管理",
    "PurchaseController": "14-耗材管理",
    "AdvertisingSpendingController": "15-广告支出",
    "AccountController": "16-账号权限",
    "RoleMenuPermissionController": "16-账号权限",
    "PatientImageController": "17-影像管理",
    "AiProxyController": "18-AI智能中心",
    "AiAgentConfigController": "18-AI智能中心",
    "AiConfigController": "18-AI智能中心",
    "AiHubController": "18-AI智能中心",
    "AiModelProviderController": "18-AI智能中心",
    "ApiKeyController": "18-AI智能中心",
    "BusinessDailyAnalysisController": "18-AI智能中心",
    "ApiDocumentationController": "19-系统设置",
    "WebhookNotificationController": "19-系统设置",
    "SpaForwardController": "19-系统设置",
}

CLASS_NAME_RE = re.compile(r'public\s+class\s+(\w+)')
CLASS_MAPPING_RE = re.compile(r'@RequestMapping\s*\(\s*"([^"]*)"\s*\)')
METHOD_SIMPLE_RE = re.compile(r'@((Get|Post|Put|Delete|Patch)Mapping)\s*\(\s*"([^"]*)"\s*\)')
METHOD_LOOSE_RE = re.compile(r'@((Get|Post|Put|Delete|Patch)Mapping)\s*\([^)]*?"([^"]*)"[^)]*\)', re.DOTALL)
METHOD_SIG_RE = re.compile(r'public\s+(\S+)\s+(\w+)\s*\((.*?)\)\s*\{', re.DOTALL)


def find_matching_brace(text, open_idx):
    assert text[open_idx] == '{'
    depth = 1
    i = open_idx + 1
    while i < len(text) and depth > 0:
        if text[i] == '{':
            depth += 1
        elif text[i] == '}':
            depth -= 1
        i += 1
    return i - 1


def find_matching_paren(text, open_idx):
    assert text[open_idx] == '('
    depth = 1
    i = open_idx + 1
    while i < len(text) and depth > 0:
        if text[i] == '(':
            depth += 1
        elif text[i] == ')':
            depth -= 1
        i += 1
    return i - 1


def parse_all_entities():
    entity_map = {}
    files = list(sorted(ENTITY_DIR.glob("*.java")))
    for filepath in files:
        content = filepath.read_text(encoding='utf-8')
        main_class = filepath.stem
        pat = rf'(?:public\s+)?class\s+{re.escape(main_class)}\b[^{{]*\{{'
        class_match = re.search(pat, content)
        if not class_match:
            continue
        main_open = class_match.end() - 1
        main_close = find_matching_brace(content, main_open)
        main_fields = extract_fields(content, main_open + 1, main_close)
        if main_fields:
            entity_map[main_class] = main_fields
        inner_pattern = re.compile(r'(?:public\s+)?(?:static\s+)?class\s+(\w+)\s*(?:<[^>]*>)?\s*\{')
        for m in inner_pattern.finditer(content):
            inner_name = m.group(1)
            if inner_name == main_class:
                continue
            inner_open = m.end() - 1
            inner_close = find_matching_brace(content, inner_open)
            inner_fields = extract_fields(content, inner_open + 1, inner_close)
            if inner_fields:
                full_name = f"{main_class}${inner_name}"
                entity_map[full_name] = inner_fields
                if inner_name not in entity_map:
                    entity_map[inner_name] = inner_fields
    return entity_map


def extract_fields(content, start, end):
    fields = []
    segment = content[start:end]
    pattern = re.compile(r'(?<!\w)private\s+(?!static\s+final\b)([\w<> ,\?]+?)\s+(\w+)\s*;')
    for m in pattern.finditer(segment):
        fields.append((m.group(2), m.group(1).strip()))
    return fields


CONTROLLER_ENTITY_MAP = {
    "MedicalRecordController": "MedicalRecord",
    "PatientController": "Patient",
    "TreatmentController": "Treatment",
    "AppointmentController": "Appointment",
    "DoctorController": "Doctor",
    "FinanceController": "Finance",
    "AccountController": "Account",
    "PatientConsentController": "PatientConsent",
    "ConsentTemplateController": "ConsentTemplate",
    "PatientFollowupController": "PatientFollowup",
    "ConsultationRecordController": "ConsultationRecord",
    "ConsultationFollowupController": "ConsultationFollowup",
    "InsuranceController": "InsuranceConfig",
    "LabOrderController": "LabOrder",
    "LabFactoryController": "LabFactory",
    "LabBillController": "LabBill",
    "LabStatisticsController": "LabStatistics",
    "MaterialController": "Material",
    "MaterialCategoryController": "MaterialCategory",
    "MaterialPurchaseController": "MaterialPurchase",
    "MaterialStatisticsController": "MaterialStatistics",
    "InventoryController": "Inventory",
    "PurchaseController": "Purchase",
    "AdvertisingSpendingController": "AdvertisingSpending",
    "PaymentChannelController": "PaymentChannel",
    "PatientImageController": "PatientImage",
    "RoleMenuPermissionController": "RoleMenuPermission",
    "ApiKeyController": "ApiKey",
    "AiConfigController": "AiFunctionConfig",
    "AiAgentConfigController": "AiAgentConfig",
    "AiModelProviderController": "AiModelProvider",
    "AiHubController": "Map",
    "BusinessDailyAnalysisController": "BusinessDailyAnalysis",
    "MedicalRecordTemplateController": "MedicalRecordTemplate",
    "MedicalRecordOperationController": "MedicalRecordOperation",
    "MedicalRecordPhraseController": "MedicalRecordPhrase",
    "MedicalRecordAIController": "String",
    "PatientDetailController": "Patient",
    "PatientCustomGroupController": "PatientCustomGroup",
    "PatientInsightController": "PatientInsightSummary",
    "PatientRiskTagController": "PatientRiskTag",
    "TreatmentOperationController": "TreatmentOperation",
    "TreatmentPlanController": "TreatmentPlan",
    "TreatmentCatalogController": "TreatmentCatalog",
    "TreatmentProjectController": "TreatmentProject",
    "TreatmentProjectCategoryController": "TreatmentProjectCategory",
    "TreatmentSceneController": "TreatmentScene",
    "FileTransferController": "String",
    "OpenDataController": "Map",
    "DoctorHomeReminderDismissalController": "String",
    "ApiDocumentationController": "String",
    "WebhookNotificationController": "String",
    "SpaForwardController": "String",
    "AuthController": "Account",
    "AiProxyController": "Map",
}

SERVICE_ENTITY_MAP = {
    "patient": "Patient", "medicalRecord": "MedicalRecord", "treatment": "Treatment",
    "appointment": "Appointment", "finance": "Finance", "account": "Account",
    "doctor": "Doctor", "consent": "PatientConsent", "followup": "PatientFollowup",
    "consultation": "ConsultationRecord", "labOrder": "LabOrder", "material": "Material",
    "inventory": "Inventory", "purchase": "Purchase", "advertisingSpending": "AdvertisingSpending",
    "insurance": "InsuranceConfig", "roleMenuPermission": "RoleMenuPermission",
    "apiKey": "ApiKey", "aiConfig": "AiFunctionConfig", "businessDailyAnalysis": "BusinessDailyAnalysis",
    "patientImage": "PatientImage", "paymentChannel": "PaymentChannel",
    "labFactory": "LabFactory", "labBill": "LabBill", "patientRiskTag": "PatientRiskTag",
    "patientCustomGroup": "PatientCustomGroup", "patientInsight": "PatientInsightSummary",
    "patientWorkbench": "PatientWorkbenchRow", "treatmentBilling": "Finance",
    "treatmentProject": "TreatmentProject", "treatmentCatalog": "TreatmentCatalog",
    "treatmentPlan": "TreatmentPlan", "treatmentOperation": "TreatmentOperation",
    "treatmentScene": "TreatmentScene", "medicalRecordTemplate": "MedicalRecordTemplate",
    "medicalRecordOperation": "MedicalRecordOperation", "medicalRecordPhrase": "MedicalRecordPhrase",
    "medicalRecordAI": "MedicalRecordAIConfigDTO", "patientService": "Patient",
    "accountService": "Account", "patientDetail": "Patient",
}


def infer_return_type(method_name, params, controller_name, method_body):
    if method_body:
        body = method_body
        if 'PageInfo' in body or 'PageHelper.startPage' in body:
            entity = CONTROLLER_ENTITY_MAP.get(controller_name, "Object")
            return f"PageInfo<{entity}>"
        if re.search(r'Result\.success\s*\(\s*"', body):
            return "String"
        svc_match = re.search(r'Result\.success\s*\(\s*(\w+)Service\.', body)
        if svc_match:
            return SERVICE_ENTITY_MAP.get(svc_match.group(1), svc_match.group(1).capitalize())
        var_match = re.search(r'Result\.success\s*\(\s*([a-z][a-zA-Z0-9]*)\s*\)', body)
        if var_match:
            var = var_match.group(1)
            for p in params:
                if p['name'] == var:
                    return p['type']
        if 'buildPageResult' in body:
            return "PageInfo"
        if re.search(r'Result\.success\s*\(\s*(List\.of|Arrays\.asList)', body):
            return "List"
    if method_name in ('selectAll', 'getPatientList') or ('selectBy' in method_name and 'page' in str(params).lower()):
        entity = CONTROLLER_ENTITY_MAP.get(controller_name, "Object")
        return f"PageInfo<{entity}>"
    if 'selectAllForH5' in method_name or ('selectByPatientId' in method_name and 'page' in str(params).lower()):
        entity = CONTROLLER_ENTITY_MAP.get(controller_name, "Object")
        return f"PageInfo<{entity}>"
    if 'selectById' in method_name and 'page' not in str(params).lower():
        return CONTROLLER_ENTITY_MAP.get(controller_name, "Object")
    if 'selectByname' in method_name or 'selectByName' in method_name:
        entity = CONTROLLER_ENTITY_MAP.get(controller_name, "Object")
        return f"PageInfo<{entity}>"
    if 'search' in method_name and 'page' in str(params).lower():
        entity = CONTROLLER_ENTITY_MAP.get(controller_name, "Object")
        return f"PageInfo<{entity}>"
    if 'workbench' in method_name:
        return "PageInfo"
    if method_name in ('add', 'edit', 'update', 'addPatient', 'updatePatient', 'addTreatment', 'editTreatment'):
        body_params = [p for p in params if p['location'] == 'Body']
        if body_params:
            return body_params[0]['type']
        return "String"
    if 'delete' in method_name:
        return "String"
    if method_name in ('getConfig', 'selectConfig'):
        return "Map"
    if 'save' in method_name and 'Config' in method_name:
        return "String"
    if 'preview' in method_name:
        return "String"
    if 'charge' in method_name or 'refund' in method_name:
        return "Finance"
    if 'batchAdd' in method_name:
        return "List"
    if 'upload' in method_name:
        return "String"
    if 'download' in method_name:
        return "String"
    if 'login' in method_name:
        return "Account"
    if 'dashboard' in method_name or 'overview' in method_name or 'statistics' in method_name:
        return "Map"
    return "Object"


def extract_method_body(content, method_name):
    pattern = re.compile(rf'public\s+\S+\s+{re.escape(method_name)}\s*\(', re.DOTALL)
    match = pattern.search(content)
    if not match:
        return None
    open_paren = match.end() - 1
    close_paren = find_matching_paren(content, open_paren)
    brace_start = content.find('{', close_paren)
    if brace_start == -1:
        return None
    brace_end = find_matching_brace(content, brace_start)
    return content[brace_start + 1:brace_end]


def parse_controller_file(filepath, entity_map):
    content = filepath.read_text(encoding='utf-8')
    class_match = CLASS_NAME_RE.search(content)
    if not class_match:
        return []
    class_name = class_match.group(1)
    prefix = ""
    class_mapping_match = CLASS_MAPPING_RE.search(content)
    if class_mapping_match:
        prefix = class_mapping_match.group(1)
    lines = content.split('\n')
    endpoints = []
    i = 0
    while i < len(lines):
        raw_line = lines[i]
        line = raw_line.strip()
        if not line or raw_line.lstrip().startswith('//'):
            i += 1
            continue
        http_method, path = None, None
        for pattern in [METHOD_SIMPLE_RE, METHOD_LOOSE_RE]:
            m = pattern.search(line)
            if m:
                http_method = m.group(2).upper()
                path = m.group(3)
                break
        if http_method and path:
            method_name = ""
            params_str = ""
            j = i + 1
            buffer_lines = []
            while j < len(lines) and j < i + 20:
                ctx_raw = lines[j]
                ctx_line = ctx_raw.strip()
                if ctx_line.startswith('public class '):
                    break
                if ctx_raw.lstrip().startswith('//'):
                    j += 1
                    continue
                buffer_lines.append(ctx_line)
                combined = ' '.join(buffer_lines)
                sig_match = METHOD_SIG_RE.search(combined)
                if sig_match:
                    method_name = sig_match.group(2)
                    params_str = sig_match.group(3)
                    break
                j += 1
            full_path = prefix + path
            full_path = full_path.replace('//', '/')
            params = parse_params(params_str)
            method_body = extract_method_body(content, method_name) if method_name else None
            inferred_type = infer_return_type(method_name, params, class_name, method_body)
            expanded_params = []
            for p in params:
                expanded_params.append(p)
                if p['location'] == 'Body':
                    base = p['type'].split('<')[0].strip() if '<' in p['type'] else p['type']
                    if base in entity_map and base not in ('String', 'int', 'Integer', 'Long', 'long', 'boolean', 'Boolean', 'Double', 'double', 'Float', 'float', 'Map', 'List'):
                        sub_fields = expand_entity_fields(base, entity_map, prefix=p['name'])
                        expanded_params.extend(sub_fields)
            endpoints.append({
                'class_name': class_name,
                'http_method': http_method,
                'path': full_path,
                'method_name': method_name,
                'params': expanded_params,
                'inferred_type': inferred_type,
            })
        i += 1
    return endpoints


def parse_params(params_str):
    if not params_str or not params_str.strip():
        return []
    params = []
    params_str = ' '.join(params_str.split())
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
        param = {'name': '', 'type': '', 'location': 'Body', 'required': False, 'description': ''}
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
        cleaned = rp.replace('final ', '').strip()
        cleaned = re.sub(r'@\w+(?:\([^)]*\))?', '', cleaned).strip()
        cleaned = re.sub(r'\brequired\s*=\s*(?:true|false)\b', '', cleaned)
        cleaned = re.sub(r'\bdefaultValue\s*=\s*"[^"]*"', '', cleaned)
        cleaned = cleaned.replace('=', ' ').replace(')', ' ').replace('(', ' ')
        cleaned = ' '.join(cleaned.split())
        if cleaned:
            parts = cleaned.split()
            if parts:
                param['name'] = parts[-1].rstrip(',)')
                param['type'] = ' '.join(parts[:-1]) if len(parts) > 1 else 'Object'
        params.append(param)
    return params


def expand_entity_fields(class_name, entity_map, prefix=""):
    result = []
    fields = entity_map.get(class_name, [])
    for field_name, field_type in fields:
        full_name = f"{prefix}.{field_name}" if prefix else field_name
        result.append({'name': full_name, 'type': field_type, 'location': 'Body', 'required': False, 'description': '', 'is_expanded': True})
        base = field_type.replace("List<", "").replace("Map<", "").replace(">", "").strip().split(',')[-1].strip()
        if base in entity_map and not prefix:
            sub_fields = entity_map.get(base, [])
            for sf_name, sf_type in sub_fields:
                result.append({'name': f"{full_name}.{sf_name}", 'type': sf_type, 'location': 'Body', 'required': False, 'description': '', 'is_expanded': True})
    return result


def java_type_to_example(java_type, entity_map, depth=0, max_depth=3):
    if depth > max_depth:
        return "..."
    java_type = java_type.strip()
    if java_type in ('String', 'string'):
        return "示例文本"
    if java_type in ('int', 'Integer', 'Long', 'long'):
        return 1
    if java_type in ('boolean', 'Boolean'):
        return True
    if java_type in ('double', 'Double', 'float', 'Float'):
        return 100.50
    if 'Date' in java_type:
        return "2026-05-15 10:00:00"
    if java_type.startswith('Map<'):
        return {"key": "value"}
    if java_type.startswith('List<') or java_type.endswith('[]'):
        inner = "Object"
        m = re.search(r'<([^>]+)>', java_type)
        if m:
            inner = m.group(1).strip().split(',')[-1].strip()
        return [java_type_to_example(inner, entity_map, depth + 1, max_depth)]
    if java_type in entity_map:
        obj = {}
        for fn, ft in entity_map[java_type]:
            obj[fn] = java_type_to_example(ft, entity_map, depth + 1, max_depth)
        return obj
    return "..."


def generate_response_data(inferred_type, entity_map):
    if inferred_type == "String":
        return "操作成功"
    if inferred_type == "Map":
        return {"key": "value"}
    if inferred_type == "List":
        return ["..."]
    if inferred_type.startswith("PageInfo<"):
        inner = inferred_type[9:-1] if inferred_type.endswith(">") else "Object"
        return {
            "total": 100,
            "list": [java_type_to_example(inner, entity_map, 0, 3)],
            "pageNum": 1,
            "pageSize": 10,
            "size": 1,
            "pages": 10,
            "isFirstPage": True,
            "isLastPage": False,
            "hasPreviousPage": False,
            "hasNextPage": True
        }
    if inferred_type.startswith("List<"):
        inner = inferred_type[5:-1] if inferred_type.endswith(">") else "Object"
        return [java_type_to_example(inner, entity_map, 0, 3)]
    if inferred_type in entity_map:
        return java_type_to_example(inferred_type, entity_map, 0, 3)
    return {"...": "具体数据结构请查看源码或实际接口返回"}


def generate_curl_example(endpoint, entity_map):
    method = endpoint['http_method']
    path = endpoint['path']
    params = endpoint['params']
    lines = [f"curl -X {method} 'http://localhost:8080{path}'"]
    body_roots = [p for p in params if p['location'] == 'Body' and not p.get('is_expanded')]
    expanded_fields = [p for p in params if p['location'] == 'Body' and p.get('is_expanded')]
    query_params = [p for p in params if p['location'] == 'Query']
    if body_roots or expanded_fields:
        lines.append("  -H 'Content-Type: application/json'")
    if query_params:
        parts = [f"{p['name']}={p['name']}_value" for p in query_params]
        lines[0] = f"curl -X {method} 'http://localhost:8080{path}?{'&'.join(parts)}'"
    if expanded_fields:
        body_json = {}
        for p in expanded_fields:
            parts = p['name'].split('.')
            current = body_json
            for part in parts[:-1]:
                if part not in current:
                    current[part] = {}
                current = current[part]
            current[parts[-1]] = java_type_to_example(p['type'], entity_map, 0, 1)
        lines.append(f"  -d '{json.dumps(body_json, ensure_ascii=False, indent=2)}'")
    elif body_roots:
        body_json = {}
        for p in body_roots:
            t = p['type']
            if 'Map' in t:
                body_json[p['name']] = {"key": "value"}
            elif t in ('String', 'string'):
                body_json[p['name']] = "string_value"
            elif t in ('int', 'Integer', 'Long', 'long'):
                body_json[p['name']] = 1
            elif t in ('boolean', 'Boolean'):
                body_json[p['name']] = True
            else:
                body_json[p['name']] = java_type_to_example(t, entity_map, 0, 2)
        lines.append(f"  -d '{json.dumps(body_json, ensure_ascii=False, indent=2)}'")
    return ' \\\n'.join(lines)


def translate_controller_name(name):
    mapping = {
        "AuthController": "认证控制器", "AccountController": "账号控制器",
        "RoleMenuPermissionController": "角色菜单权限控制器", "PatientController": "患者控制器",
        "PatientDetailController": "患者详情控制器", "PatientCustomGroupController": "患者分组控制器",
        "PatientInsightController": "患者洞察控制器", "PatientRiskTagController": "患者风险标签控制器",
        "PatientImageController": "患者影像控制器", "PatientFollowupController": "患者随访控制器",
        "PatientConsentController": "患者知情同意控制器", "ConsultationRecordController": "咨询记录控制器",
        "ConsultationFollowupController": "咨询跟进控制器", "AppointmentController": "预约控制器",
        "DoctorController": "医生控制器", "MedicalRecordController": "病历控制器",
        "MedicalRecordTemplateController": "病历模板控制器", "MedicalRecordOperationController": "病历操作控制器",
        "MedicalRecordPhraseController": "病历词条控制器", "MedicalRecordAIController": "病历AI控制器",
        "TreatmentController": "治疗控制器", "TreatmentOperationController": "治疗操作控制器",
        "TreatmentPlanController": "治疗计划控制器", "TreatmentCatalogController": "治疗目录控制器",
        "TreatmentProjectController": "治疗项目控制器", "TreatmentProjectCategoryController": "治疗项目分类控制器",
        "TreatmentSceneController": "治疗场景控制器", "FinanceController": "财务控制器",
        "PaymentChannelController": "支付渠道控制器", "AdvertisingSpendingController": "广告支出控制器",
        "InsuranceController": "保险控制器", "InventoryController": "库存控制器",
        "PurchaseController": "采购控制器", "MaterialController": "耗材控制器",
        "MaterialCategoryController": "耗材分类控制器", "MaterialPurchaseController": "耗材采购控制器",
        "MaterialStatisticsController": "耗材统计控制器", "LabFactoryController": "加工厂控制器",
        "LabOrderController": "加工订单控制器", "LabBillController": "加工账单控制器",
        "LabStatisticsController": "加工统计控制器", "ConsentTemplateController": "同意书模板控制器",
        "AiProxyController": "AI代理控制器", "AiAgentConfigController": "AI代理配置控制器",
        "AiConfigController": "AI配置控制器", "AiHubController": "AI中心控制器",
        "AiModelProviderController": "AI模型提供商控制器", "ApiKeyController": "API密钥控制器",
        "BusinessDailyAnalysisController": "业务日常分析控制器", "ApiDocumentationController": "API文档控制器",
        "WebhookNotificationController": "Webhook通知控制器", "SpaForwardController": "SPA转发控制器",
        "FileTransferController": "文件传输控制器", "OpenDataController": "开放数据控制器",
        "DoctorHomeReminderDismissalController": "医生首页提醒关闭控制器",
    }
    return mapping.get(name, name)


def translate_method_name(name, path):
    if not name:
        return "接口"
    verb_map = {
        "selectAll": "查询全部", "selectById": "根据ID查询", "selectByName": "根据名称查询",
        "selectByPatientId": "根据患者ID查询", "selectEnabled": "查询已启用", "selectLowStock": "查询低库存",
        "search": "搜索", "add": "新增", "edit": "编辑", "update": "更新", "delete": "删除",
        "deleteBatch": "批量删除", "get": "获取", "save": "保存", "upload": "上传",
        "download": "下载", "export": "导出", "import": "导入", "login": "登录",
        "register": "注册", "overview": "概览统计", "dashboard": "仪表盘", "cancel": "取消",
        "charge": "收费", "refund": "退款", "link": "关联", "linkPatient": "关联患者",
        "match": "匹配", "matchPatientByPhone": "根据手机号匹配患者", "matchForPatientCreate": "匹配患者用于新建",
        "assign": "分配", "dismiss": "关闭/忽略", "send": "发送", "preview": "预览",
        "expand": "扩写/展开", "test": "测试", "run": "执行", "scan": "扫描",
        "probe": "探测", "regenerate": "重新生成", "notify": "通知", "mock": "模拟",
        "batchAdd": "批量新增", "batchSave": "批量保存", "batchStatus": "批量更新状态",
        "updateStatus": "更新状态", "markSkip": "标记跳过", "void": "作废", "issue": "下发/签发",
        "getConfig": "获取配置", "saveConfig": "保存配置", "getPatientList": "查询患者列表",
        "addPatient": "新增患者", "updatePatient": "更新患者", "deletePatient": "删除患者",
        "deletePatientBatch": "批量删除患者", "addTreatment": "新增治疗", "editTreatment": "编辑治疗",
        "deleteTreatment": "删除治疗", "chargeTreatment": "治疗收费", "refundTreatment": "治疗退款",
        "batchAddTreatment": "批量新增治疗",
    }
    noun_map = {
        "Config": "配置", "Detail": "详情", "List": "列表", "Record": "记录",
        "Template": "模板", "Image": "影像", "File": "文件", "Patient": "患者",
        "Doctor": "医生", "Account": "账号", "Role": "角色", "Menu": "菜单",
        "Permission": "权限", "Profile": "档案", "Status": "状态", "History": "历史",
        "Log": "日志", "Overview": "概览", "Dashboard": "仪表盘", "Analysis": "分析",
        "Statistics": "统计", "Report": "报表", "Chart": "图表", "Setting": "设置",
        "Scene": "场景", "Step": "步骤", "Key": "密钥", "Tag": "标签",
        "Group": "分组", "Category": "分类", "Catalog": "目录", "Project": "项目",
        "Factory": "工厂", "Order": "订单", "Bill": "账单", "Invoice": "发票",
        "Material": "耗材", "Inventory": "库存", "Purchase": "采购", "Supplier": "供应商",
        "Brand": "品牌", "Consent": "同意书", "Followup": "随访", "Insight": "洞察",
        "Reminder": "提醒", "Advice": "医嘱", "Diagnosis": "诊断", "Treatment": "治疗",
        "Plan": "计划", "Phrase": "词条", "Operation": "操作", "Charge": "收费",
        "Refund": "退款", "Payment": "支付", "Channel": "渠道", "Expense": "支出",
        "Insurance": "保险", "Settlement": "结算", "Provider": "提供商", "Model": "模型",
        "Agent": "代理", "Function": "功能", "Session": "会话", "Memory": "记忆",
        "Message": "消息", "Alert": "告警", "Weekly": "周报", "Monthly": "月报",
        "Preview": "预览", "Prompt": "提示词",
    }
    if name in verb_map:
        return verb_map[name]
    matched_verb = None
    matched_len = 0
    for eng, chn in sorted(verb_map.items(), key=lambda x: -len(x[0])):
        if name.startswith(eng):
            if len(eng) > matched_len:
                matched_verb = chn
                matched_len = len(eng)
    if matched_verb:
        suffix = name[matched_len:]
        if suffix:
            return f"{matched_verb}{noun_map.get(suffix, suffix)}"
        return matched_verb
    if path.endswith("/add"): return "新增"
    if path.endswith("/edit"): return "编辑"
    if path.endswith("/delete/{id}") or path.endswith("/delete"): return "删除"
    if path.endswith("/search"): return "搜索"
    if "selectAll" in name: return "查询全部"
    if "selectBy" in name: return "条件查询"
    if name.lower().startswith("get"): return "获取"
    return name


def generate_category_doc(category_name, endpoints, entity_map):
    lines = [f"# {category_name}", "", f"> 本分类共 {len(endpoints)} 个接口", ""]
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
            if ep['params']:
                lines.append("**请求参数**：")
                lines.append("")
                lines.append("| 参数名 | 位置 | 类型 | 必填 | 说明 |")
                lines.append("|--------|------|------|------|------|")
                for p in ep['params']:
                    req = "是" if p['required'] else "否"
                    desc = "（Entity字段）" if p.get('is_expanded') else "-"
                    lines.append(f"| {p['name']} | {p['location']} | {p['type']} | {req} | {desc} |")
                lines.append("")
            else:
                lines.append("**请求参数**：无")
                lines.append("")
            lines.append("**curl 示例**：")
            lines.append("")
            lines.append("```bash")
            lines.append(generate_curl_example(ep, entity_map))
            lines.append("```")
            lines.append("")
            lines.append("**响应说明**：")
            lines.append("")
            lines.append("```json")
            response_data = generate_response_data(ep['inferred_type'], entity_map)
            full_response = {"code": "200", "msg": "success", "data": response_data}
            lines.append(json.dumps(full_response, ensure_ascii=False, indent=2))
            lines.append("```")
            lines.append("")
            lines.append("---")
            lines.append("")
    return '\n'.join(lines)


def main():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    print("正在扫描 Entity 类...")
    entity_map = parse_all_entities()
    print(f"共扫描到 {len(entity_map)} 个 Entity/DTO 类")

    print("正在解析 Controller...")
    all_endpoints = []
    for filepath in sorted(CONTROLLER_DIR.glob("*.java")):
        endpoints = parse_controller_file(filepath, entity_map)
        all_endpoints.extend(endpoints)
    print(f"共解析到 {len(all_endpoints)} 个接口")

    by_category = defaultdict(list)
    unmatched = []
    for ep in all_endpoints:
        controller = ep['class_name']
        category = CATEGORY_MAP.get(controller)
        if category:
            by_category[category].append(ep)
        else:
            unmatched.append(ep)
    if unmatched:
        for ep in unmatched:
            by_category["19-系统设置"].append(ep)
        print(f"警告：{len(unmatched)} 个接口未匹配分类，已归入系统设置")

    total = 0
    for category in sorted(by_category.keys()):
        endpoints = by_category[category]
        total += len(endpoints)
        doc_content = generate_category_doc(category, endpoints, entity_map)
        filename = f"{category}.md"
        output_path = OUTPUT_DIR / filename
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(doc_content)
        print(f"已生成：{filename} ({len(endpoints)} 个接口)")

    readme_lines = [
        "# API 接口文档总览", "",
        "> 本文档按业务模块分类汇总口腔门诊 SaaS 管理系统所有后端 HTTP API 接口。",
        f"> 共扫描 **{total} 个接口**，覆盖 **{len(by_category)} 个分类**。",
        "> 生成时间：2026-05-16", "", "---", "",
        "## 分类目录", "",
        "| 序号 | 分类 | 接口数 | 文档 |",
        "|------|------|--------|------|",
    ]
    idx = 1
    for cat in sorted(by_category.keys()):
        count = len(by_category[cat])
        filename = f"{cat}.md"
        readme_lines.append(f"| {idx:02d} | {cat} | {count} | [{filename}]({filename}) |")
        idx += 1
    readme_lines.extend([
        "", "---", "", "## 统一响应结构", "",
        "系统中约 **98%** 的接口统一返回以下结构：", "",
        '```json', '{', '  "code": "200",', '  "msg": "success",', '  "data": { ... }', '}', '```', "",
        "| 字段 | 类型 | 说明 |", "|------|------|------|",
        "| `code` | String | 状态码，`200` 表示成功，其他为错误码 |",
        "| `msg` | String | 提示信息 |",
        "| `data` | Object | 业务数据，具体结构因接口而异 |", "",
        "**特殊说明**：",
        "- 文件下载类接口直接返回文件流，非 `Result` 包装。",
        "- SSE 流式接口返回 `text/event-stream` 格式。", "", "---", "",
        "## 文档格式说明", "",
        "每个接口包含以下内容：", "",
        "1. **请求方式**：GET / POST / PUT / DELETE / PATCH",
        "2. **请求路径**：完整的 HTTP 路径（含前缀）",
        "3. **Controller**：所属 Java Controller 类名",
        "4. **请求参数**：",
        "   - 参数名、位置（Path/Query/Body/Header）、类型、必填、说明",
        "   - Body 参数如果是自定义 Entity/DTO，已展开其内部所有字段",
        "5. **curl 示例**：使用展开字段构建的完整 JSON 请求示例",
        "6. **响应说明**：包含具体的 `data` 字段 JSON 结构示例", "", "---", "",
        "## 如何查找接口", "",
        "1. **按分类查找**：根据前端导航页面名称，找到对应分类文档",
        "2. **按 Controller 查找**：每个分类下按 Controller 分组，便于快速定位",
        "3. **按路径查找**：使用编辑器的全文搜索（Ctrl+F）搜索路径关键字", "", "---", "",
        "## 文档更新方法", "",
        "当后端新增或修改接口后，可重新运行生成脚本：", "",
        "```bash", "python3 shuao-clinic-saas-source/scripts/generate_api_docs_v2.py", "```", "", "---", "",
        "## 已知局限", "",
        "1. 响应数据结构基于代码静态推断（方法名 + 方法体分析），对于部分复杂返回类型可能不够精确。",
        "2. 参数描述当前为空（`-`），因为代码中缺乏 JavaDoc 注释。",
    ])
    with open(OUTPUT_DIR / "README.md", 'w', encoding='utf-8') as f:
        f.write('\n'.join(readme_lines))
    print(f"\n文档生成完毕！总计 {total} 个接口，输出目录：{OUTPUT_DIR}")


if __name__ == "__main__":
    main()

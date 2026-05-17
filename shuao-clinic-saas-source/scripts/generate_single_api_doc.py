#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成单个中文API文档
扫描所有Controller和Entity，生成完整的接口文档（单文件）
"""

import os
import re
import glob
from collections import defaultdict

# ============================================================
# 基础路径配置
# ============================================================
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CONTROLLER_DIR = os.path.join(BASE_DIR, "saas-springboot-src/src/main/java/com/example/springboot/controller")
ENTITY_DIR = os.path.join(BASE_DIR, "saas-springboot-src/src/main/java/com/example/springboot/entity")
OUTPUT_FILE = "/Users/cherry/Downloads/口腔saas管理系统开发/docs/03-接口文档/口腔门诊SaaS系统API接口文档.md"

# ============================================================
# 分类映射（Controller名称 -> 中文分类）
# ============================================================
CATEGORY_MAP = {
    "AuthController": "01-登录认证",
    "AccountController": "01-登录认证",
    "AppointmentController": "03-预约管理",
    "DoctorController": "05-医生排班",
    "PatientController": "06-患者管理",
    "PatientDetailController": "06-患者管理",
    "PatientImageController": "06-患者管理",
    "PatientRiskTagController": "06-患者管理",
    "PatientCustomGroupController": "06-患者管理",
    "PatientInsightController": "06-患者管理",
    "PatientReferralRecord": "06-患者管理",
    "MedicalRecordController": "07-病历管理",
    "MedicalRecordOperationController": "07-病历管理",
    "MedicalRecordTemplateController": "07-病历管理",
    "MedicalRecordPhraseController": "07-病历管理",
    "MedicalRecordAIController": "07-病历管理",
    "PatientConsentController": "08-知情同意",
    "ConsentTemplateController": "08-知情同意",
    "PatientFollowupController": "09-回访管理",
    "ConsultationRecordController": "10-咨询管理",
    "ConsultationFollowupController": "10-咨询管理",
    "FinanceController": "11-财务管理",
    "PaymentChannelController": "11-财务管理",
    "InsuranceController": "12-保险管理",
    "LabOrderController": "13-加工管理",
    "LabFactoryController": "13-加工管理",
    "LabBillController": "13-加工管理",
    "LabStatisticsController": "13-加工管理",
    "InventoryController": "14-耗材管理",
    "MaterialController": "14-耗材管理",
    "MaterialCategoryController": "14-耗材管理",
    "MaterialPurchaseController": "14-耗材管理",
    "PurchaseController": "14-耗材管理",
    "MaterialStatisticsController": "14-耗材管理",
    "AdvertisingSpendingController": "15-广告支出",
    "RoleMenuPermissionController": "16-账号权限",
    "TreatmentController": "04-治疗管理",
    "TreatmentCatalogController": "04-治疗管理",
    "TreatmentProjectController": "04-治疗管理",
    "TreatmentProjectCategoryController": "04-治疗管理",
    "TreatmentOperationController": "04-治疗管理",
    "TreatmentPlanController": "04-治疗管理",
    "TreatmentSceneController": "04-治疗管理",
    "AiProxyController": "18-AI智能中心",
    "AiHubController": "18-AI智能中心",
    "AiConfigController": "18-AI智能中心",
    "AiAgentConfigController": "18-AI智能中心",
    "AiModelProviderController": "18-AI智能中心",
    "BusinessDailyAnalysisController": "18-AI智能中心",
    "OpenDataController": "18-AI智能中心",
    "ApiKeyController": "18-AI智能中心",
    "WebhookNotificationController": "18-AI智能中心",
    "FileTransferController": "19-系统设置",
    "ApiDocumentationController": "19-系统设置",
    "DoctorHomeReminderDismissalController": "02-首页工作台",
    "PatientWorkbenchBaseRow": "06-患者管理",
    "PatientWorkbenchRow": "06-患者管理",
    "PatientWorkbenchQuery": "06-患者管理",
}

# ============================================================
# Entity字段解析
# ============================================================
def parse_entities():
    """解析所有Entity/DTO类的字段"""
    entities = {}
    java_files = glob.glob(os.path.join(ENTITY_DIR, "*.java"))
    print(f"扫描到 {len(java_files)} 个实体类文件...")

    for filepath in java_files:
        filename = os.path.basename(filepath)
        classname = filename.replace(".java", "")
        fields = []

        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # 去掉注释
        content_no_comment = re.sub(r'//.*', '', content)
        content_no_comment = re.sub(r'/\*.*?\*/', '', content_no_comment, flags=re.DOTALL)

        # 匹配 private 字段（包括泛型）
        field_pattern = re.compile(
            r'private\s+(?:final\s+)?([<>,.?\w\s]+?)\s+(\w+)\s*;'
        )
        for match in field_pattern.finditer(content_no_comment):
            type_name = match.group(1).strip()
            field_name = match.group(2).strip()
            # 过滤静态字段
            if 'static' not in content_no_comment[
                max(0, match.start()-20):match.start()
            ]:
                fields.append((type_name, field_name))

        entities[classname] = fields

    return entities


# ============================================================
# Controller接口解析
# ============================================================
def parse_controllers():
    """解析所有Controller的接口定义"""
    endpoints = []
    java_files = glob.glob(os.path.join(CONTROLLER_DIR, "*.java"))
    print(f"扫描到 {len(java_files)} 个控制器文件...")

    for filepath in java_files:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        filename = os.path.basename(filepath)
        controller_name = filename.replace(".java", "")

        # 获取类级别的RequestMapping前缀
        class_mapping_match = re.search(
            r'@RequestMapping\(["\']?([^"\')]+)["\']?\)',
            content
        )
        prefix = ""
        if class_mapping_match:
            prefix = class_mapping_match.group(1).strip()

        # 查找类声明获取Controller名称（可能带有泛型）
        class_decl_match = re.search(
            r'class\s+(\w+)',
            content
        )
        if not class_decl_match:
            continue

        # 提取所有方法
        # 匹配方法签名（包括注解、返回值、方法名、参数列表）
        method_pattern = re.compile(
            r'(@(?:GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*\([^)]*\)\s+)?'
            r'(?:@\w+\s*\(?:[^)]*\)\s+)*'
            r'(?:public|private|protected)\s+'
            r'([<>,.?\w\s]+?)\s+'
            r'(\w+)\s*\(\s*([^)]*)\)',
            re.DOTALL
        )

        # 更精确的方法匹配 - 提取方法签名和方法体
        # 使用 finditer 定位每个方法的位置
        method_sig_pattern = re.compile(
            r'(@(?:GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*\([^)]*\))'
            r'[^\{]*?'
            r'(public|private|protected)\s+'
            r'([<>,.?\w\s]+?)\s+'
            r'(\w+)\s*\(\s*([^)]*)\s*\)',
            re.DOTALL
        )

        for match in method_sig_pattern.finditer(content):
            mapping_anno = match.group(1).strip()
            return_type = match.group(3).strip()
            method_name = match.group(4).strip()
            params_str = match.group(5).strip()

            # 提取方法体：从参数列表右括号后开始找匹配的大括号
            method_body = ""
            body_start = content.find("{", match.end())
            if body_start != -1:
                brace_depth = 1
                body_end = body_start + 1
                while body_end < len(content) and brace_depth > 0:
                    if content[body_end] == "{":
                        brace_depth += 1
                    elif content[body_end] == "}":
                        brace_depth -= 1
                    body_end += 1
                if brace_depth == 0:
                    method_body = content[body_start:body_end]

            # 解析HTTP方法和路径
            http_method = "GET"
            path = ""

            if "GetMapping" in mapping_anno:
                http_method = "GET"
            elif "PostMapping" in mapping_anno:
                http_method = "POST"
            elif "PutMapping" in mapping_anno:
                http_method = "PUT"
            elif "DeleteMapping" in mapping_anno:
                http_method = "DELETE"
            elif "PatchMapping" in mapping_anno:
                http_method = "PATCH"
            elif "RequestMapping" in mapping_anno:
                http_method = "REQUEST"

            # 提取路径
            path_match = re.search(r'value\s*=\s*["\']([^"\']+)["\']', mapping_anno)
            if not path_match:
                path_match = re.search(r'["\']([^"\']+)["\']', mapping_anno)
            if path_match:
                path = path_match.group(1)

            full_path = prefix + path
            full_path = full_path.replace("//", "/")

            # 解析参数
            params = []
            if params_str:
                param_parts = []
                current = ""
                depth = 0
                for char in params_str:
                    if char in "<[(":
                        depth += 1
                    elif char in ")>]":
                        depth -= 1
                    elif char == "," and depth == 0:
                        param_parts.append(current.strip())
                        current = ""
                        continue
                    current += char
                if current.strip():
                    param_parts.append(current.strip())

                for p in param_parts:
                    param_info = parse_param(p)
                    if param_info:
                        params.append(param_info)

            endpoints.append({
                "controller": controller_name,
                "method_name": method_name,
                "http_method": http_method,
                "path": full_path,
                "return_type": return_type,
                "params": params,
                "method_body": method_body,
            })

    return endpoints


def parse_param(param_str):
    """解析单个参数字符串"""
    # 去除注解后的参数定义
    # 例如: @RequestBody MedicalRecord record
    # 例如: @RequestParam Integer id
    # 例如: @PathVariable Long id

    param_str = param_str.strip()
    if not param_str or param_str.startswith("HttpServlet") or param_str.startswith("Model") or param_str.startswith("BindingResult"):
        return None

    # 跳过包含常量引用的复杂参数（如 @RequestHeader(value = OPERATOR_ACCOUNT_ID_HEADER, required = false)）
    if "value =" in param_str and not re.search(r'value\s*=\s*["\']', param_str):
        return None

    # 提取注解
    annotation = ""
    anno_match = re.match(r'(@\w+(?:\([^)]*\))?)\s+', param_str)
    if anno_match:
        annotation = anno_match.group(1)
        param_str = param_str[anno_match.end():].strip()

    # 提取类型和名称
    parts = param_str.split()
    if len(parts) >= 2:
        param_type = parts[0]
        param_name = parts[1]
        # 处理泛型
        if "<" in param_str and not param_type.endswith(">"):
            # 重新提取
            type_match = re.match(r'([<>,.?\w\s]+?)\s+(\w+)$', param_str)
            if type_match:
                param_type = type_match.group(1).strip()
                param_name = type_match.group(2).strip()

        # 判断参数位置
        location = "参数"
        if "PathVariable" in annotation:
            location = "路径"
        elif "RequestParam" in annotation:
            location = "查询"
        elif "RequestBody" in annotation:
            location = "请求体"
        elif "RequestHeader" in annotation:
            location = "请求头"

        required = "是" if "required = false" not in annotation else "否"

        return {
            "annotation": annotation,
            "type": param_type,
            "name": param_name,
            "location": location,
            "required": required,
        }
    return None


# ============================================================
# 响应类型推断
# ============================================================
def infer_response_type(endpoint, entities):
    """推断接口返回的数据结构"""
    return_type = endpoint["return_type"]
    method_name = endpoint["method_name"]
    controller_name = endpoint["controller"]
    method_body = endpoint.get("method_body", "")

    # 如果是Result<T>类型，提取T
    result_match = re.search(r'Result<(.+?)>', return_type)
    if result_match:
        inner_type = result_match.group(1).strip()
        return inner_type

    # 如果是PageInfo<T>类型
    pageinfo_match = re.search(r'PageInfo<(.+?)>', return_type)
    if pageinfo_match:
        return f"分页列表({pageinfo_match.group(1).strip()})"

    # 分析方法体推断返回类型
    if method_body:
        # 检测 PageInfo
        if "new PageInfo" in method_body:
            # 尝试从 service 调用推断列表元素类型
            service_match = re.search(r'service\.(\w+)\(', method_body)
            if service_match:
                svc_method = service_match.group(1)
                # 根据 Controller 和 service 方法推断实体类型
                entity_type = infer_entity_from_controller(controller_name, svc_method)
                if entity_type:
                    return f"分页列表({entity_type})"
            return "分页列表"
        # 检测 Result.success(service.xxx(...)) -> 单个对象
        svc_select_match = re.search(r'Result\.success\(service\.(\w+)\(', method_body)
        if svc_select_match:
            svc_method = svc_select_match.group(1)
            entity_type = infer_entity_from_controller(controller_name, svc_method)
            if entity_type:
                return entity_type
            return "对象"
        # 检测 Result.success(record/entity) -> 返回传入的实体
        body_entity_match = re.search(r'Result\.success\((\w+)\)', method_body)
        if body_entity_match:
            var_name = body_entity_match.group(1)
            # 查找参数中是否有同名变量（请求体参数）
            for p in endpoint.get("params", []):
                if p["name"] == var_name:
                    return p["type"]
            return "对象"

    # 根据方法名推断
    if method_name.startswith("select") or method_name.startswith("get") or method_name.startswith("find"):
        if "List" in return_type or "Page" in return_type:
            entity_type = infer_entity_from_controller(controller_name, method_name)
            if entity_type:
                return f"分页列表({entity_type})"
            return "列表"
        entity_type = infer_entity_from_controller(controller_name, method_name)
        if entity_type:
            return entity_type
        return "对象"
    elif method_name.startswith("add") or method_name.startswith("create") or method_name.startswith("save"):
        entity_type = infer_entity_from_controller(controller_name, method_name)
        if entity_type:
            return entity_type
        return "新增结果"
    elif method_name.startswith("edit") or method_name.startswith("update"):
        entity_type = infer_entity_from_controller(controller_name, method_name)
        if entity_type:
            return entity_type
        return "更新结果"
    elif method_name.startswith("delete") or method_name.startswith("remove"):
        return "删除结果"

    return return_type


def infer_entity_from_controller(controller_name, method_name):
    """根据Controller名称推断实体类型"""
    mapping = {
        "MedicalRecordController": "MedicalRecord",
        "MedicalRecordOperationController": "MedicalRecordOperation",
        "MedicalRecordTemplateController": "MedicalRecordTemplate",
        "MedicalRecordPhraseController": "MedicalRecordPhrase",
        "MedicalRecordAIController": "MedicalRecordAIConfigDTO",
        "PatientController": "Patient",
        "PatientDetailController": "Patient",
        "PatientImageController": "PatientImage",
        "PatientRiskTagController": "PatientRiskTag",
        "PatientCustomGroupController": "PatientCustomGroup",
        "PatientFollowupController": "PatientFollowup",
        "PatientInsightController": "PatientInsightSummary",
        "PatientConsentController": "PatientConsent",
        "ConsentTemplateController": "ConsentTemplate",
        "ConsultationRecordController": "ConsultationRecord",
        "ConsultationFollowupController": "ConsultationFollowup",
        "AppointmentController": "Appointment",
        "DoctorController": "Doctor",
        "AccountController": "Account",
        "TreatmentController": "Treatment",
        "TreatmentCatalogController": "TreatmentCatalog",
        "TreatmentProjectController": "TreatmentProject",
        "TreatmentProjectCategoryController": "TreatmentProjectCategory",
        "TreatmentOperationController": "TreatmentOperation",
        "TreatmentPlanController": "Treatment_plans",
        "TreatmentSceneController": "TreatmentScene",
        "FinanceController": "Finance",
        "PaymentChannelController": "PaymentChannel",
        "InsuranceController": "InsurancePatientProfile",
        "InventoryController": "Inventory",
        "MaterialController": "Material",
        "MaterialCategoryController": "MaterialCategory",
        "MaterialPurchaseController": "MaterialPurchase",
        "PurchaseController": "Purchase",
        "MaterialStatisticsController": "BusinessPeriodReport",
        "LabOrderController": "LabOrder",
        "LabFactoryController": "LabFactory",
        "LabBillController": "LabBill",
        "LabStatisticsController": "BusinessPeriodReport",
        "AdvertisingSpendingController": "AdvertisingSpending",
        "RoleMenuPermissionController": "RoleMenuPermission",
        "AiConfigController": "AiGlobalConfig",
        "AiAgentConfigController": "AiAgentConfig",
        "AiModelProviderController": "AiModelProvider",
        "ApiKeyController": "ApiKey",
        "AuthController": "Account",
        "FileTransferController": "String",
    }
    base = controller_name.replace("Controller", "")
    if controller_name in mapping:
        return mapping[controller_name]
    # 模糊匹配
    for key, val in mapping.items():
        if base in key or key.replace("Controller", "") in base:
            return val
    return None


def get_entity_fields(type_name, entities, depth=0):
    """获取类型的字段列表，如果是Entity则展开"""
    if depth > 3:
        return []

    # 去除泛型包装
    base_type = type_name
    list_match = re.match(r'(?:List|ArrayList|Set)<(.+?)>', type_name)
    if list_match:
        base_type = list_match.group(1).strip()

    # Map类型不展开
    if base_type.startswith("Map<") or base_type.startswith("HashMap<"):
        return []

    # 查找Entity
    if base_type in entities:
        result = []
        for field_type, field_name in entities[base_type]:
            # 如果是嵌套Entity且不是基本类型，递归展开
            if field_type in entities and field_type not in ["String", "Integer", "Long", "Date", "Boolean", "Double", "BigDecimal", "LocalDate", "LocalDateTime"]:
                nested = get_entity_fields(field_type, entities, depth + 1)
                if nested:
                    result.append({
                        "type": field_type,
                        "name": field_name,
                        "is_nested": True,
                        "children": nested
                    })
                else:
                    result.append({
                        "type": field_type,
                        "name": field_name,
                        "is_nested": False,
                        "children": []
                    })
            else:
                result.append({
                    "type": field_type,
                    "name": field_name,
                    "is_nested": False,
                    "children": []
                })
        return result

    return []


def build_example_value(java_type, field_name):
    """根据Java类型和字段名生成示例值"""
    type_lower = java_type.lower()

    if "id" in field_name.lower() and ("patient" in field_name.lower() or "doctor" in field_name.lower() or "account" in field_name.lower()):
        return 1
    if field_name.lower() == "id":
        return 1
    if "id" in field_name.lower():
        return 1

    if "string" in type_lower:
        if "name" in field_name.lower():
            return "张三"
        if "phone" in field_name.lower():
            return "13800138000"
        if "email" in field_name.lower():
            return "zhangsan@example.com"
        if "address" in field_name.lower():
            return "北京市朝阳区"
        if "status" in field_name.lower():
            return "正常"
        if "type" in field_name.lower():
            return "类型A"
        if "gender" in field_name.lower() or "sex" in field_name.lower():
            return "男"
        if "date" in field_name.lower() or "time" in field_name.lower():
            return "2026-05-16"
        if "remark" in field_name.lower() or "note" in field_name.lower() or "desc" in field_name.lower():
            return "备注信息"
        if "content" in field_name.lower():
            return "内容描述"
        if "title" in field_name.lower():
            return "标题"
        if "code" in field_name.lower():
            return "CODE001"
        if "amount" in field_name.lower() or "price" in field_name.lower() or "fee" in field_name.lower():
            return "100.00"
        if "complaint" in field_name.lower():
            return "牙齿疼痛"
        if "history" in field_name.lower():
            return "无特殊病史"
        if "diagnosis" in field_name.lower():
            return "龋齿"
        if "plan" in field_name.lower() or "treatment" in field_name.lower():
            return "根管治疗"
        if "password" in field_name.lower():
            return "******"
        if "token" in field_name.lower():
            return "eyJhbGciOiJIUzI1NiIs..."
        return f"{field_name}示例值"

    if "int" in type_lower or "long" in type_lower:
        if "count" in field_name.lower() or "num" in field_name.lower() or "age" in field_name.lower():
            return 1
        return 1

    if "double" in type_lower or "bigdecimal" in type_lower or "float" in type_lower:
        return 100.00

    if "boolean" in type_lower or "bool" in type_lower:
        return True

    if "date" in type_lower or "localdate" in type_lower or "time" in type_lower:
        return "2026-05-16 10:30:00"

    return None


def build_json_example(fields, indent=2):
    """根据字段列表构建JSON示例"""
    if not fields:
        return "{}"

    lines = []
    for field in fields:
        field_name = field["name"]
        field_type = field["type"]

        if field.get("is_nested") and field.get("children"):
            if "List" in field_type or "Set" in field_type or "Array" in field_type:
                nested_json = build_json_example(field["children"], indent + 2)
                lines.append(" " * indent + f'"{field_name}": [')
                lines.append(" " * (indent + 2) + nested_json)
                lines.append(" " * indent + "]")
            else:
                nested_json = build_json_example(field["children"], indent + 2)
                lines.append(" " * indent + f'"{field_name}": {nested_json}')
        else:
            example = build_example_value(field_type, field_name)
            if isinstance(example, str):
                lines.append(" " * indent + f'"{field_name}": "{example}"')
            elif isinstance(example, bool):
                lines.append(" " * indent + f'"{field_name}": {str(example).lower()}')
            elif example is None:
                lines.append(" " * indent + f'"{field_name}": null')
            else:
                lines.append(" " * indent + f'"{field_name}": {example}')

    return "{\n" + ",\n".join(lines) + "\n" + " " * (indent - 2) + "}"


def get_category(controller_name):
    """获取Controller所属分类"""
    if controller_name in CATEGORY_MAP:
        return CATEGORY_MAP[controller_name]

    # 模糊匹配
    for key, cat in CATEGORY_MAP.items():
        if key.replace("Controller", "") in controller_name or controller_name in key:
            return cat

    return "19-系统设置"


def escape_curl_value(value):
    """转义curl中的字符串值"""
    if isinstance(value, str):
        return value.replace('"', '\\"')
    return str(value)


def build_curl_example(endpoint, entity_fields):
    """构建curl请求示例"""
    method = endpoint["http_method"]
    path = endpoint["path"]

    # 构建基础URL（示例用）
    url = f"http://localhost:8080{path}"

    headers = '-H "Content-Type: application/json"'

    # 处理路径参数
    path_params = [p for p in endpoint["params"] if p["location"] == "路径"]
    for p in path_params:
        url = url.replace("{" + p["name"] + "}", escape_curl_value(build_example_value(p["type"], p["name"])))

    # 处理查询参数
    query_params = [p for p in endpoint["params"] if p["location"] == "查询"]
    if query_params:
        query_parts = []
        for p in query_params:
            val = build_example_value(p["type"], p["name"])
            query_parts.append(f'{p["name"]}={escape_curl_value(val)}')
        url += "?" + "&".join(query_parts)

    # 处理请求体
    body_params = [p for p in endpoint["params"] if p["location"] == "请求体"]
    body_str = ""
    if body_params:
        body_json = build_json_example(entity_fields) if entity_fields else "{}"
        body_str = f"-d '{body_json}'"

    parts = ["curl", f'-X {method}', headers]
    if body_str:
        parts.append(body_str)
    parts.append(f'"{url}"')

    return " \\\n    ".join(parts)


# ============================================================
# 生成文档
# ============================================================
def generate_doc():
    print("=" * 60)
    print("开始生成API文档...")
    print("=" * 60)

    # 1. 解析所有Entity
    entities = parse_entities()
    print(f"解析完成：共 {len(entities)} 个实体类")

    # 2. 解析所有Controller
    endpoints = parse_controllers()
    print(f"解析完成：共 {len(endpoints)} 个接口")

    # 3. 按分类分组
    categories = defaultdict(list)
    for ep in endpoints:
        cat = get_category(ep["controller"])
        categories[cat].append(ep)

    # 4. 生成文档内容
    lines = []

    # 文档头
    lines.append("# 口腔门诊SaaS管理系统 - API接口文档")
    lines.append("")
    lines.append("> 本文档汇总系统所有后端HTTP接口，按业务模块分类编排。")
    lines.append(f"> 共收录 **{len(endpoints)}** 个接口，覆盖 **{len(categories)}** 个业务模块。")
    lines.append(f"> 生成时间：2026-05-16")
    lines.append("")

    # 目录
    lines.append("---")
    lines.append("## 目录")
    lines.append("")
    sorted_cats = sorted(categories.keys())
    for cat in sorted_cats:
        lines.append(f"- [{cat}](#{cat.replace('-', '')})")
    lines.append("")

    # 统一响应结构说明
    lines.append("---")
    lines.append("## 统一响应结构")
    lines.append("")
    lines.append("系统中绝大多数接口统一返回以下JSON结构：")
    lines.append("")
    lines.append("```json")
    lines.append("{")
    lines.append('  "code": "200",')
    lines.append('  "msg": "success",')
    lines.append('  "data": { ... }')
    lines.append("}")
    lines.append("```")
    lines.append("")
    lines.append("| 字段 | 类型 | 说明 |")
    lines.append("|------|------|------|")
    lines.append('| `code` | 字符串 | 状态码，`200`表示成功，其他为错误码 |')
    lines.append('| `msg` | 字符串 | 提示信息 |')
    lines.append('| `data` | 对象 | 业务数据，具体结构因接口而异 |')
    lines.append("")
    lines.append("**特殊说明**：")
    lines.append("- 文件下载类接口直接返回文件流，不进行Result包装。")
    lines.append("- SSE流式接口返回`text/event-stream`格式。")
    lines.append("")

    # 各分类详细内容
    for cat in sorted_cats:
        lines.append("---")
        lines.append(f"## {cat}")
        lines.append("")

        eps = categories[cat]
        # 按Controller分组
        controller_groups = defaultdict(list)
        for ep in eps:
            controller_groups[ep["controller"]].append(ep)

        for ctrl_name in sorted(controller_groups.keys()):
            lines.append(f"### {ctrl_name}")
            lines.append("")

            for ep in controller_groups[ctrl_name]:
                method = ep["http_method"]
                path = ep["path"]
                method_name = ep["method_name"]
                return_type = ep["return_type"]

                # 接口标题
                lines.append(f"#### {method} {path}")
                lines.append("")
                lines.append(f"- **方法名**：`{method_name}`")
                lines.append(f"- **返回类型**：`{return_type}`")
                lines.append("")

                # 请求参数表
                lines.append("**请求参数**：")
                lines.append("")

                if ep["params"]:
                    lines.append("| 参数名 | 位置 | 类型 | 必填 | 说明 |")
                    lines.append("|--------|------|------|------|------|")

                    # 收集所有要展开的字段
                    all_fields = []
                    body_entity_name = None

                    for p in ep["params"]:
                        param_type = p["type"]
                        param_name = p["name"]
                        location = p["location"]
                        required = p["required"]

                        # 检查是否是自定义Entity类型（需要展开）
                        base_type = param_type
                        list_match = re.match(r'(?:List|ArrayList)<(.+?)>', param_type)
                        if list_match:
                            base_type = list_match.group(1).strip()

                        if base_type in entities and base_type not in ["String", "Integer", "Long", "Map", "Object"]:
                            # 展开Entity字段
                            entity_fields = get_entity_fields(base_type, entities)
                            if entity_fields:
                                lines.append(f'| `{param_name}` | {location} | `{param_type}` | {required} | {base_type}对象，字段如下： |')
                                for field in entity_fields:
                                    lines.append(f'| &nbsp;&nbsp;&nbsp;&nbsp;`{field["name"]}` | {location} | `{field["type"]}` | - | - |')
                                all_fields = entity_fields
                                body_entity_name = base_type
                                continue

                        lines.append(f'| `{param_name}` | {location} | `{param_type}` | {required} | - |')

                    lines.append("")
                else:
                    lines.append("无参数")
                    lines.append("")

                # curl示例
                if ep["params"]:
                    lines.append("**请求示例（curl）：**")
                    lines.append("")
                    lines.append("```bash")
                    curl_cmd = build_curl_example(ep, all_fields)
                    lines.append(curl_cmd)
                    lines.append("```")
                    lines.append("")

                # 响应示例
                lines.append("**响应示例：**")
                lines.append("")
                lines.append("```json")

                # 推断data结构
                inferred_type = infer_response_type(ep, entities)
                response_data = "{}"

                if "分页" in inferred_type:
                    inner = inferred_type.replace("分页列表(", "").replace(")", "")
                    entity_fields = get_entity_fields(inner, entities)
                    item_example = build_json_example(entity_fields) if entity_fields else "{}"
                    response_data = f"""{{
  "total": 100,
  "list": [{item_example}],
  "pageNum": 1,
  "pageSize": 10,
  "size": 10,
  "pages": 10,
  "isFirstPage": true,
  "isLastPage": false,
  "hasPreviousPage": false,
  "hasNextPage": true
}}"""
                elif inferred_type in entities:
                    entity_fields = get_entity_fields(inferred_type, entities)
                    response_data = build_json_example(entity_fields) if entity_fields else "{}"
                elif "列表" in inferred_type or "List" in inferred_type:
                    response_data = '[{"item": "示例项"}]'
                elif "对象" in inferred_type:
                    response_data = '{"field": "value"}'
                elif "String" in inferred_type:
                    response_data = '"字符串结果"'
                elif "Boolean" in inferred_type or "boolean" in return_type.lower():
                    response_data = 'true'

                lines.append("{")
                lines.append('  "code": "200",')
                lines.append('  "msg": "success",')
                lines.append(f'  "data": {response_data}')
                lines.append("}")
                lines.append("```")
                lines.append("")
                lines.append("---")
                lines.append("")

    # 写入文件
    os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        f.write("\n".join(lines))

    print(f"\n文档已生成：{OUTPUT_FILE}")
    print(f"总接口数：{len(endpoints)}")
    print(f"分类数：{len(categories)}")
    print(f"实体类数：{len(entities)}")
    return len(endpoints), len(categories)


if __name__ == "__main__":
    generate_doc()

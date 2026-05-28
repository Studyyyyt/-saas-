"""
患者相关 MCP Tool 模块
提供患者搜索、详情查询、病历列表、预约记录等工具
"""

from typing import Any

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def search_patients(keyword: str = "", page: int = 1, size: int = 10) -> list[dict[str, Any]] | dict[str, str]:
    """
    搜索患者，支持按姓名、电话模糊匹配。默认返回前10条，如需更多请指定page参数。

    :param keyword: 搜索关键词（姓名或电话），默认空字符串表示查询全部
    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 患者列表（提取分页结构中的 list 字段）
    """
    try:
        params = {
            "keyword": keyword,
            "page": page,
            "size": size,
        }
        data = await java_get("/patients/search", params=params)

        # 提取分页结构中的 list 字段
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"搜索患者失败: {str(e)}"}


@mcp.tool()
async def get_patient_detail(patient_id: int) -> dict[str, Any] | dict[str, str]:
    """
    获取患者详细信息，包括基础资料、就诊次数、总费用、欠款、风险标签、最近病历、预约、时间轴等全景视图。

    :param patient_id: 患者主键 ID
    :return: 患者详情字典
    """
    try:
        data = await java_get(f"/patient-details/overview/{patient_id}")
        if isinstance(data, dict):
            return data
        if data is None:
            return {}
        return {"data": data}
    except Exception as e:
        return {"error": f"获取患者详情失败: {str(e)}"}


@mcp.tool()
async def get_patient_medical_records(patient_id: int, page: int = 1, size: int = 10) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取指定患者的病历列表。

    :param patient_id: 患者主键 ID
    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 病历列表（提取 PageInfo 中的 list 字段；若直接返回列表则原样返回）
    """
    try:
        params = {
            "patientId": patient_id,
            "page": page,
            "size": size,
        }
        data = await java_get("/medical-records/selectByPatientId", params=params)

        # 提取分页结构中的 list 字段
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"获取患者病历列表失败: {str(e)}"}


@mcp.tool()
async def get_patient_appointments(patient_id: int) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取指定患者的预约/治疗记录。

    :param patient_id: 患者主键 ID
    :return: 预约列表（提取返回字典中的 appointments 字段）
    """
    try:
        data = await java_get(f"/patient-details/appointments/{patient_id}")

        # 提取返回结构中的 appointments 字段
        if isinstance(data, dict) and "appointments" in data:
            return data["appointments"]
        if isinstance(data, list):
            return data
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"获取患者预约记录失败: {str(e)}"}

"""
义齿加工相关 MCP Tool 模块
提供加工订单列表查询、加工统计概览等工具
"""

from typing import Any

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_lab_orders(
    keyword: str = "",
    factory_id: int = 0,
    status: str = "",
    patient_id: int = 0,
    start_date: str = "",
    end_date: str = "",
    page: int = 1,
    size: int = 10,
) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取义齿加工订单列表，支持按关键词、加工厂、状态、患者筛选。默认返回前10条，如需更多请指定page参数。

    :param keyword: 搜索关键词，默认空字符串表示查询全部
    :param factory_id: 加工厂 ID 筛选，默认0表示全部
    :param status: 订单状态筛选，默认空字符串表示全部
    :param patient_id: 患者 ID 筛选，默认0表示全部
    :param start_date: 开始日期筛选（格式如 2024-01-01）
    :param end_date: 结束日期筛选（格式如 2024-01-01）
    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 加工订单列表（提取分页结构中的 list 或 records 字段）
    """
    try:
        params: dict[str, Any] = {
            "page": page,
            "size": size,
        }
        if keyword:
            params["keyword"] = keyword
        if factory_id:
            params["factoryId"] = factory_id
        if status:
            params["status"] = status
        if patient_id:
            params["patientId"] = patient_id
        if start_date:
            params["startDate"] = start_date
        if end_date:
            params["endDate"] = end_date

        data = await java_get("/lab-orders/search", params=params)

        # 提取分页结构中的 list 或 records 字段
        if isinstance(data, dict):
            if "list" in data:
                return data["list"]
            if "records" in data:
                return data["records"]
        if isinstance(data, list):
            return data
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"获取义齿加工订单列表失败: {str(e)}"}


@mcp.tool()
async def get_lab_statistics(
    start_date: str = "",
    end_date: str = "",
    range_preset: str = "",
) -> dict[str, Any] | dict[str, str]:
    """
    获取义齿加工统计概览，包括各状态数量、平均周转天数等。

    :param start_date: 开始日期筛选（格式如 2024-01-01）
    :param end_date: 结束日期筛选（格式如 2024-01-01）
    :param range_preset: 范围预设值（如 "week", "month", "quarter" 等）
    :return: 加工统计概览字典
    """
    try:
        params: dict[str, Any] = {}
        if start_date:
            params["startDate"] = start_date
        if end_date:
            params["endDate"] = end_date
        if range_preset:
            params["rangePreset"] = range_preset

        data = await java_get("/lab-statistics/overview", params=params)
        if isinstance(data, dict):
            return data
        if data is None:
            return {}
        return {"data": data}
    except Exception as e:
        return {"error": f"获取义齿加工统计概览失败: {str(e)}"}

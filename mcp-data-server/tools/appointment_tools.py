"""
预约相关 MCP Tool 模块
提供诊所预约列表查询、医生列表查询等工具
"""

from typing import Any

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_appointments(
    date: str = "",
    doctor_id: int = 0,
    status: str = "",
    page: int = 1,
    size: int = 10,
) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取诊所预约列表，支持按日期、医生、状态筛选。默认返回前10条，如需更多请指定page参数。

    :param date: 预约日期筛选（格式如 2024-01-01）
    :param doctor_id: 医生 ID 筛选
    :param status: 预约状态筛选，默认空字符串表示全部
    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 预约列表（提取分页结构中的 list 字段）
    """
    try:
        # 先拉取全量数据，避免分页截断导致过滤结果不完整
        all_items: list[dict[str, Any]] = []
        current_page = 1
        max_pages = 100  # 安全上限，防止异常情况下无限循环

        while current_page <= max_pages:
            params: dict[str, Any] = {
                "page": current_page,
                "size": 200,
            }
            if status:
                params["status"] = status

            data = await java_get("/appointments/selectAll", params=params)

            # 提取分页结构中的 list 字段
            if isinstance(data, dict) and "list" in data:
                items = data["list"]
            elif isinstance(data, list):
                items = data
            else:
                items = []

            if not items:
                break

            all_items.extend(items)

            # 如果返回条数小于请求条数，说明已到底
            if len(items) < 200:
                break

            current_page += 1

        # 内存过滤：date（对应 appointment_date 字段）
        if date:
            all_items = [
                item for item in all_items
                if item.get("appointment_date") == date
            ]

        # 内存过滤：doctor_id（对应 doctor_account_id 字段）
        if doctor_id:
            all_items = [
                item for item in all_items
                if item.get("doctor_account_id") == doctor_id
            ]

        # 客户端分页
        start = (page - 1) * size
        end = start + size
        return all_items[start:end]

    except Exception as e:
        return {"error": f"获取预约列表失败: {str(e)}"}


@mcp.tool()
async def get_doctor_list(page: int = 1, size: int = 10) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取医生列表与排班信息。

    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 医生列表（提取分页结构中的 list 字段）
    """
    try:
        params = {
            "page": page,
            "size": size,
        }
        data = await java_get("/doctors/selectAll", params=params)

        # 提取分页结构中的 list 字段
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"获取医生列表失败: {str(e)}"}

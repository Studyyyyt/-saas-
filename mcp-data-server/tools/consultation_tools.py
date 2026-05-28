"""
咨询相关 MCP Tool 模块
提供咨询记录列表查询、咨询看板统计等工具
"""

from typing import Any

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_consultation_records(
    keyword: str = "",
    channel: str = "",
    status: str = "",
    start_date: str = "",
    end_date: str = "",
    page: int = 1,
    size: int = 10,
) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取咨询记录列表，支持按关键词、渠道、日期范围、跟进状态筛选。默认返回前10条，如需更多请指定page参数。

    :param keyword: 搜索关键词，默认空字符串表示查询全部
    :param channel: 渠道筛选，默认空字符串表示全部
    :param status: 跟进状态筛选（映射到 handlingResult），默认空字符串表示全部
    :param start_date: 开始日期筛选（格式如 2024-01-01），映射到 startTime
    :param end_date: 结束日期筛选（格式如 2024-01-01），映射到 endTime
    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 咨询记录列表（提取分页结构中的 list 或 records 字段）
    """
    try:
        params: dict[str, Any] = {
            "page": page,
            "size": size,
        }
        if keyword:
            params["keyword"] = keyword
        if channel:
            params["channel"] = channel
        if status:
            params["handlingResult"] = status
        if start_date:
            params["startTime"] = start_date
        if end_date:
            params["endTime"] = end_date

        data = await java_get("/consultations/search", params=params)

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
        return {"error": f"获取咨询记录列表失败: {str(e)}"}


@mcp.tool()
async def get_consultation_dashboard(
    start_date: str = "",
    end_date: str = "",
) -> dict[str, Any]:
    """
    获取咨询概览/漏斗/渠道分析等多维度统计看板数据。
    此 Tool 返回多个仪表板指标的合并结果。

    :param start_date: 开始日期筛选（格式如 2024-01-01），映射到 startTime
    :param end_date: 结束日期筛选（格式如 2024-01-01），映射到 endTime
    :return: 合并后的看板数据字典，结构如 {"overview": {...}, "funnel": {...}, "channelAnalysis": {...}, "projectAnalysis": {...}}
    """
    result: dict[str, Any] = {
        "overview": {},
        "funnel": {},
        "channelAnalysis": {},
        "projectAnalysis": {},
    }

    params: dict[str, Any] = {}
    if start_date:
        params["startTime"] = start_date
    if end_date:
        params["endTime"] = end_date

    # 依次调用4个看板接口，某个接口报错不影响其他接口
    try:
        result["overview"] = await java_get("/consultations/dashboard/overview", params=params) or {}
    except Exception as e:
        result["overview"] = {"error": str(e)}

    try:
        result["funnel"] = await java_get("/consultations/dashboard/funnel", params=params) or {}
    except Exception as e:
        result["funnel"] = {"error": str(e)}

    try:
        result["channelAnalysis"] = await java_get("/consultations/dashboard/channelAnalysis", params=params) or {}
    except Exception as e:
        result["channelAnalysis"] = {"error": str(e)}

    try:
        result["projectAnalysis"] = await java_get("/consultations/dashboard/projectAnalysis", params=params) or {}
    except Exception as e:
        result["projectAnalysis"] = {"error": str(e)}

    return result

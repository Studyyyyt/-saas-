"""
经营分析相关 MCP Tool 模块
提供经营报表、广告花费等数据查询工具
"""

from typing import Any

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_business_report(report_type: str = "daily", date: str = "") -> dict[str, Any] | dict[str, str]:
    """
    获取经营分析报表，支持日报、周报、月报。默认返回最新日报。

    :param report_type: 报表类型，可选值为 "daily"（日报）/ "weekly"（周报）/ "monthly"（月报），默认 "daily"
    :param date: 指定日期（格式 yyyy-MM-dd），默认空字符串表示返回最新报表
    :return: 经营分析报表字典
    """
    try:
        # 根据报表类型选择对应接口
        if report_type == "daily":
            path = "/business-analysis/latest"
        elif report_type == "weekly":
            path = "/business-analysis/weekly/latest"
        elif report_type == "monthly":
            path = "/business-analysis/monthly/latest"
        else:
            return {"error": f"不支持的报表类型: {report_type}，可选值为 daily/weekly/monthly"}

        params = {}
        if date:
            params["date"] = date

        data = await java_get(path, params=params if params else None)

        if isinstance(data, dict):
            return data
        if data is None:
            return {}
        return {"data": data}
    except Exception as e:
        return {"error": f"获取经营报表失败: {str(e)}"}


@mcp.tool()
async def get_advertising_spending(
    channel: str = "",
    keyword: str = "",
    start_date: str = "",
    end_date: str = "",
    page: int = 1,
    size: int = 10,
) -> list[dict[str, Any]] | dict[str, str]:
    """
    获取广告投放花费记录，支持按平台、关键词、日期范围筛选。默认返回前10条，如需更多请指定page参数。

    :param channel: 投放平台/渠道，默认空字符串表示不筛选（映射到后端 platform 参数）
    :param keyword: 搜索关键词，默认空字符串
    :param start_date: 开始日期（格式 yyyy-MM-dd），默认空字符串
    :param end_date: 结束日期（格式 yyyy-MM-dd），默认空字符串
    :param page: 页码，从1开始，默认1
    :param size: 每页条数，默认10
    :return: 广告花费列表
    """
    try:
        params: dict[str, Any] = {
            "page": page,
            "size": size,
        }
        if channel:
            params["platform"] = channel
        if keyword:
            params["keyword"] = keyword
        if start_date:
            params["startDate"] = start_date
        if end_date:
            params["endDate"] = end_date

        data = await java_get("/advertising-spending/search", params=params)

        # 处理分页返回结构：优先取 list，其次 records，直接列表则原样返回
        if isinstance(data, dict):
            if "list" in data:
                return data["list"]
            if "records" in data:
                return data["records"]
            # 若字典中无已知列表键，返回空列表（避免把分页元数据当记录返回）
            return []
        if isinstance(data, list):
            return data
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"获取广告花费记录失败: {str(e)}"}

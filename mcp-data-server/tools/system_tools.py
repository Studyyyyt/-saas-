"""
系统配置相关 MCP Tool 模块
提供治疗项目目录、系统配置等查询工具
"""

from typing import Any

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_treatment_catalog() -> list[dict[str, Any]] | dict[str, Any]:
    """
    获取治疗项目目录与分类树，包含各项目的名称、单价、耗时等信息。

    :return: 项目目录列表
    """
    try:
        data = await java_get("/treatment-catalog/selectAll")

        # 统一处理返回结构
        # 注：fastmcp 对空列表会返回空 content，故空列表包装为字典
        if isinstance(data, list):
            return data if data else {"catalog": []}
        if isinstance(data, dict):
            # 若后端包装在 list/records/data 等字段中，优先提取
            if "list" in data:
                return data["list"]
            if "records" in data:
                return data["records"]
            if "data" in data and isinstance(data["data"], list):
                return data["data"]
            # 若是单条字典而非列表，包装为列表返回
            return [data]
        if data is None:
            return []

        return []
    except Exception as e:
        return {"error": f"获取治疗项目目录失败: {str(e)}"}

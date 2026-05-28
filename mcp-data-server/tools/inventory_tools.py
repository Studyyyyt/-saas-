"""
库存相关 Tool 模块
提供库存物品列表查询及低库存预警功能
"""

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_inventory(
    category: str = "",
    warehouse: str = "",
    page: int = 1,
    size: int = 10,
) -> list:
    """
    获取库存物品列表，支持按分类、品牌、供应商筛选。默认返回前10条，如需更多请指定page参数。

    参数说明：
    - category: 物品分类（保留但不传给后端）
    - warehouse: 仓库名称（保留但不传给后端）
    - page: 页码，默认1
    - size: 每页条数，默认10
    """
    try:
        # 后端selectAll只接受page和size，不接受category/warehouse
        params = {
            "page": page,
            "size": size,
        }
        data = await java_get("/inventory/selectAll", params=params)

        # 分页接口统一处理
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        return []
    except Exception as e:
        return {"error": f"获取库存物品列表失败: {str(e)}"}


@mcp.tool()
async def get_low_stock_alert(
    page: int = 1,
    size: int = 10,
) -> list:
    """
    获取低库存预警列表（库存数量低于预警阈值的物品）。

    参数说明：
    - page: 页码，默认1
    - size: 每页条数，默认10
    """
    try:
        params = {
            "page": page,
            "size": size,
        }
        data = await java_get("/inventory/selectLowStock", params=params)

        # 分页接口统一处理
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        return []
    except Exception as e:
        return {"error": f"获取低库存预警列表失败: {str(e)}"}

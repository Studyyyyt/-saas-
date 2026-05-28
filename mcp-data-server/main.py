"""
MCP Server 入口文件
使用 fastmcp 库创建 SSE 模式的 MCP Server，提供口腔诊所数据查询工具集
"""

import asyncio
import logging

from config import settings
from mcp_server import mcp

# 导入所有 Tool 模块以触发注册
from tools import (  # noqa: F401
    appointment_tools,
    business_tools,
    consultation_tools,
    finance_tools,
    inventory_tools,
    lab_tools,
    patient_tools,
    system_tools,
)


# ---------------------------------------------------------------------------
# 健康检查工具
# ---------------------------------------------------------------------------
@mcp.tool()
async def health_check() -> dict:
    """健康检查，返回服务运行状态"""
    return {"status": "ok"}


# ---------------------------------------------------------------------------
# 日志配置
# ---------------------------------------------------------------------------
def setup_logging() -> None:
    """配置标准日志格式"""
    log_level = settings.LOG_LEVEL.upper()
    logging.basicConfig(
        level=getattr(logging, log_level, logging.INFO),
        format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )


# ---------------------------------------------------------------------------
# 主入口
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    setup_logging()
    # 使用 fastmcp 内置 HTTP 服务器启动 SSE 服务
    asyncio.run(
        mcp.run_http_async(
            transport="sse",
            host="0.0.0.0",
            port=settings.PORT,
        )
    )

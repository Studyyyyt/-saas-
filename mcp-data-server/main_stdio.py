"""
MCP Server 入口文件（stdio 模式，供 Claude Code CLI 使用）
注意：stdio 模式下严禁向 stdout 输出任何非 MCP 消息内容
"""

import logging
import sys

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


@mcp.tool()
async def health_check() -> dict:
    """健康检查，返回服务运行状态"""
    return {"status": "ok"}


def setup_logging() -> None:
    """配置日志输出到 stderr，避免污染 stdout（MCP 协议通道）"""
    log_level = settings.LOG_LEVEL.upper()
    handler = logging.StreamHandler(sys.stderr)
    handler.setFormatter(
        logging.Formatter(
            "%(asctime)s - %(name)s - %(levelname)s - %(message)s",
            datefmt="%Y-%m-%d %H:%M:%S",
        )
    )
    root = logging.getLogger()
    root.handlers = []
    root.addHandler(handler)
    root.setLevel(getattr(logging, log_level, logging.INFO))


if __name__ == "__main__":
    setup_logging()
    # show_banner=False 防止 banner 输出到 stdout 干扰 MCP 协议
    mcp.run(transport="stdio", show_banner=False)

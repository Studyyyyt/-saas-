"""
MCP Server 实例模块
所有 Tool 从此模块导入 mcp 实例进行注册
"""

from fastmcp import FastMCP

# 创建 FastMCP 实例
mcp = FastMCP("口腔诊所数据助手")

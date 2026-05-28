#!/bin/bash
# MCP stdio 模式启动脚本
# 工作目录必须是 mcp-data-server 根目录，否则相对导入会失败
cd "$(dirname "$0")" || exit 1
exec .venv/bin/python main_stdio.py

"""
环境变量配置模块
使用 python-dotenv 加载 .env 文件，所有配置项均提供默认值
"""

import os
from dotenv import load_dotenv

# 加载 .env 文件（如果存在）
load_dotenv()


class Settings:
    """应用配置类，从环境变量读取，附带默认值"""

    # Java 后端 API 基础 URL
    JAVA_API_BASE: str = os.getenv("JAVA_API_BASE", "http://localhost:8080")

    # 操作者账号 ID，用于 X-Operator-Account-Id 请求头
    OPERATOR_ACCOUNT_ID: str = os.getenv("OPERATOR_ACCOUNT_ID", "")

    # MCP Server 监听端口
    PORT: int = int(os.getenv("PORT", "8000"))

    # 日志级别
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")


# 全局配置实例
settings = Settings()

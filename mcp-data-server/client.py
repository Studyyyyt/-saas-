"""
Java 后端 API 的异步 HTTP 客户端封装
提供统一请求头、错误处理及分页辅助功能
注意：每次请求创建独立 AsyncClient，避免 keep-alive 连接池在 Docker 环境中断开后复用问题
"""

from __future__ import annotations

import logging
from typing import Any

import httpx

from config import settings

# 日志记录器
logger = logging.getLogger(__name__)


class JavaApiError(Exception):
    """Java 后端返回业务错误时抛出的异常"""

    def __init__(self, message: str, code: int | None = None, response_data: dict | None = None):
        super().__init__(message)
        self.code = code
        self.response_data = response_data or {}


class JavaApiClient:
    """
    Java 后端 API 客户端
    每次请求创建独立的 httpx.AsyncClient，避免连接池复用已断开的连接
    """

    def _build_headers(self) -> dict[str, str]:
        """构建请求头，包含操作者账号 ID"""
        headers: dict[str, str] = {}
        if settings.OPERATOR_ACCOUNT_ID:
            headers["X-Operator-Account-Id"] = settings.OPERATOR_ACCOUNT_ID
        return headers

    async def request(self, method: str, path: str, **kwargs: Any) -> Any:
        """
        发送 HTTP 请求并统一处理响应

        :param method: HTTP 方法（GET/POST/PUT/DELETE 等）
        :param path: API 路径（会自动拼接 JAVA_API_BASE）
        :param kwargs: 传递给 httpx 的其他参数
        :return: 后端返回的 data 字段内容
        :raises JavaApiError: 当后端返回 code != 200 时抛出
        """
        headers = self._build_headers()
        # 合并外部传入的 headers（外部优先级更高）
        if "headers" in kwargs:
            headers.update(kwargs.pop("headers"))

        url = f"{settings.JAVA_API_BASE.rstrip('/')}/{path.lstrip('/')}"
        logger.debug("[%s] %s headers=%s", method.upper(), url, headers)

        timeout = httpx.Timeout(30.0, connect=10.0)
        async with httpx.AsyncClient(timeout=timeout) as client:
            response = await client.request(method, url, headers=headers, **kwargs)
            response.raise_for_status()

            data = response.json()
            code = data.get("code", 200)
            msg = data.get("msg", "")

            if str(code) != "200":
                logger.error("Java API 返回错误: code=%s, msg=%s", code, msg)
                raise JavaApiError(message=msg, code=code, response_data=data)

            return data.get("data")

    async def get(self, path: str, params: dict[str, Any] | None = None) -> Any:
        """GET 请求便捷方法"""
        return await self.request("GET", path, params=params)

    async def post(self, path: str, json: dict[str, Any] | None = None) -> Any:
        """POST 请求便捷方法"""
        return await self.request("POST", path, json=json)

    async def get_all_pages(
        self,
        path: str,
        params: dict[str, Any] | None = None,
        page_size: int = 100,
    ) -> list[dict[str, Any]]:
        """
        自动翻页获取所有数据（适用于 AI 需要全量数据的场景）

        :param path: API 路径
        :param params: 基础查询参数
        :param page_size: 每页条数
        :return: 合并后的全部列表数据
        """
        merged_params = dict(params) if params else {}
        merged_params.setdefault("pageSize", page_size)
        page_num = 1
        all_items: list[dict[str, Any]] = []

        while True:
            merged_params["pageNum"] = page_num
            page_data = await self.get(path, params=merged_params)

            if not isinstance(page_data, dict):
                logger.warning("分页接口返回非字典结构，终止翻页: %s", type(page_data))
                break

            items = page_data.get("list") or page_data.get("records") or page_data.get("data") or []
            if not items:
                break

            all_items.extend(items)

            total = page_data.get("total") or page_data.get("totalRow") or 0
            if len(all_items) >= total:
                break

            page_num += 1

        return all_items


# 模块级便捷函数，方便直接调用
async def java_request(method: str, path: str, **kwargs: Any) -> Any:
    """发送 HTTP 请求到 Java 后端"""
    client = JavaApiClient()
    return await client.request(method, path, **kwargs)


async def java_get(path: str, params: dict[str, Any] | None = None) -> Any:
    """GET 请求便捷函数"""
    client = JavaApiClient()
    return await client.get(path, params=params)


async def java_post(path: str, json: dict[str, Any] | None = None) -> Any:
    """POST 请求便捷函数"""
    client = JavaApiClient()
    return await client.post(path, json=json)

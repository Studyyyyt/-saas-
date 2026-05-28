#!/bin/sh
# 前端容器启动脚本 - 注入环境变量到 Nginx 配置

set -e

# 默认后端地址
BACKEND_URL="${BACKEND_URL:-http://backend:8080}"

# 替换 Nginx 配置中的占位符
sed -i "s|__BACKEND_URL__|${BACKEND_URL}|g" /etc/nginx/conf.d/default.conf

echo "前端服务启动，后端代理地址: ${BACKEND_URL}"

exec "$@"

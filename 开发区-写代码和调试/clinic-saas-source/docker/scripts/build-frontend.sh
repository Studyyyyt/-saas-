#!/bin/bash
# 前端构建脚本 - 开发环境使用
# 用法: ./build-frontend.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DOCKER_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
VUE_SRC_DIR="${DOCKER_ROOT}/../saas-vue-src"

echo "============================================"
echo "开始构建前端..."
echo "============================================"

cd "${VUE_SRC_DIR}"

# 安装依赖（如有 package.json 变更）
echo "步骤 1/3: 安装依赖..."
npm install

# 构建生产包
echo "步骤 2/3: 构建生产包..."
npm run build

echo "============================================"
echo "前端构建完成"
echo "============================================"

# 重启前端容器（如果正在运行）
cd "${DOCKER_ROOT}"

if docker compose -f docker-compose.dev.yml ps | grep -q "clinic-frontend-dev"; then
    echo "步骤 3/3: 重启前端容器..."
    docker compose -f docker-compose.dev.yml restart frontend
    echo "前端容器已重启，请访问 http://localhost:7070"
else
    echo "步骤 3/3: 前端容器未运行，请执行以下命令启动:"
    echo "  cd docker && docker compose -f docker-compose.dev.yml up -d"
fi

echo "============================================"

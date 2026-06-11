#!/bin/bash
# ============================================================
# 口腔诊所 SaaS 系统 - 镜像构建与导出脚本
# 用法:
#   ./scripts/build-and-export.sh [版本号]
#
# 示例:
#   ./scripts/build-and-export.sh           # 使用默认版本 v1.0.0
#   ./scripts/build-and-export.sh v1.0.1    # 指定版本 v1.0.1
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DOCKER_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DELIVER_DIR="${DOCKER_ROOT}/deliver"

C_GREEN='\033[0;32m'
C_YELLOW='\033[1;33m'
C_BLUE='\033[0;34m'
C_RED='\033[0;31m'
C_RESET='\033[0m'

show_info() { echo -e "${C_BLUE}[信息]${C_RESET} $1"; }
show_ok() { echo -e "${C_GREEN}[成功]${C_RESET} $1"; }
show_warn() { echo -e "${C_YELLOW}[警告]${C_RESET} $1"; }
show_err() { echo -e "${C_RED}[错误]${C_RESET} $1"; }

cd "$DOCKER_ROOT"

# ============================================================
# 解析版本号参数
# ============================================================
VERSION="${1:-v1.0.0}"
show_info "构建版本: $VERSION"
echo ""

# ============================================================
# 步骤 1：构建镜像
# ============================================================
show_info "开始构建生产镜像..."
show_info "构建后端镜像 (clinic-saas/backend:$VERSION)..."
docker compose -f docker-compose.yml build --no-cache backend

show_info "构建前端镜像 (clinic-saas/frontend:$VERSION)..."
docker compose -f docker-compose.yml build --no-cache frontend

# 给镜像打上用户指定的版本标签
docker tag clinic-saas/backend:v1.0.0 clinic-saas/backend:$VERSION
docker tag clinic-saas/frontend:v1.0.0 clinic-saas/frontend:$VERSION

show_ok "镜像构建完成"
echo ""

# ============================================================
# 步骤 2：导出镜像
# ============================================================
show_info "导出镜像到 tar 文件..."
mkdir -p "$DELIVER_DIR"

docker save \
  clinic-saas/backend:$VERSION \
  clinic-saas/frontend:$VERSION \
  -o "${DELIVER_DIR}/clinic-saas-images.tar"

show_ok "镜像导出完成: ${DELIVER_DIR}/clinic-saas-images.tar"
echo ""

# ============================================================
# 步骤 3：复制部署文件到交付包
# ============================================================
show_info "复制部署文件到交付包..."

# 清理旧运行时数据，防止误打包
rm -rf "${DELIVER_DIR}/mysql-data" "${DELIVER_DIR}/patient-uploads" "${DELIVER_DIR}/.env"

# 复制 docker-compose.yml（替换版本号）
sed "s/v1\.0\.0/$VERSION/g" docker-compose.yml > "${DELIVER_DIR}/docker-compose.yml"
cp .env.example "${DELIVER_DIR}/"

# 复制数据库初始化脚本（保持 sql/init/ 目录结构与 docker-compose.yml 一致）
mkdir -p "${DELIVER_DIR}/sql/init"
cp sql/init/database_init.sql "${DELIVER_DIR}/sql/init/"

show_ok "部署文件复制完成"
echo ""

# ============================================================
# 完成
# ============================================================
echo -e "${C_GREEN}========================================${C_RESET}"
echo -e "${C_GREEN}  交付包生成完成 (版本: $VERSION)${C_RESET}"
echo -e "${C_GREEN}========================================${C_RESET}"
echo ""
echo "交付包位置: ${DELIVER_DIR}/"
echo ""
ls -lh "${DELIVER_DIR}/"
echo ""
echo "部署说明请查看 docker/README.md"
echo ""

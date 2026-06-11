#!/bin/bash
# ============================================================
# 口腔诊所 SaaS 系统 - 镜像构建与导出脚本
# 用法:
#   ./scripts/build-and-export.sh [版本号]
#
# 示例:
#   ./scripts/build-and-export.sh           # 使用默认版本 v1.0.0
#   ./scripts/build-and-export.sh v1.0.1    # 指定版本 v1.0.1
#
# 脚本职责:
#   1. 构建后端和前端 Docker 生产镜像
#   2. 将镜像导出为 tar 文件，便于离线交付给客户
#   3. 复制 docker-compose.yml、环境变量模板、数据库初始化脚本到交付目录
#   4. 清理旧的运行时数据，避免将测试数据误打包给客户
# ============================================================

# set -e: 任一命令失败时立即退出，防止错误继续执行导致交付包不完整
set -e

# ============================================================
# 路径计算
# ============================================================

# SCRIPT_DIR: 脚本所在目录，即 docker/scripts/
# 使用 $0 获取脚本路径，确保无论从哪个目录调用都能找到正确位置
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# DOCKER_ROOT: Docker 项目根目录，即 docker/
# 构建命令和 docker-compose.yml 都位于该目录下
DOCKER_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# DELIVER_DIR: 交付包输出目录
# 所有交付给客户的内容统一输出到 docker/deliver/，便于打包和传输
DELIVER_DIR="${DOCKER_ROOT}/deliver"

# ============================================================
# 终端颜色定义
# ============================================================

C_GREEN='\033[0;32m'
C_YELLOW='\033[1;33m'
C_BLUE='\033[0;34m'
C_RED='\033[0;31m'
C_RESET='\033[0m'

show_info() { echo -e "${C_BLUE}[信息]${C_RESET} $1"; }
show_ok() { echo -e "${C_GREEN}[成功]${C_RESET} $1"; }
show_warn() { echo -e "${C_YELLOW}[警告]${C_RESET} $1"; }
show_err() { echo -e "${C_RED}[错误]${C_RESET} $1"; }

# 切换到 Docker 根目录，确保后续所有相对路径都以 docker/ 为基准
cd "$DOCKER_ROOT"

# ============================================================
# 解析版本号参数
# ============================================================

# 支持通过第一个参数指定版本号，未指定时使用默认版本 v1.0.0
# 版本号会同时用于镜像标签和 docker-compose.yml 中的 image 字段
VERSION="${1:-v1.0.0}"
show_info "构建版本: $VERSION"
echo ""

# ============================================================
# 步骤 1：构建镜像
# ============================================================

show_info "开始构建生产镜像..."

# 构建后端镜像
# --no-cache 强制不使用缓存，确保每次构建都基于最新源码
# 构建上下文为 docker-compose.yml 中 backend.build.context: ..（即 clinic-saas-source/）
show_info "构建后端镜像 (clinic-saas/backend:$VERSION)..."
docker compose -f docker-compose.yml build --no-cache backend

# 构建前端镜像
# 前端构建时会将 saas-vue-src/ 编译为 dist/，再复制到 Nginx 镜像中
show_info "构建前端镜像 (clinic-saas/frontend:$VERSION)..."
docker compose -f docker-compose.yml build --no-cache frontend

# docker compose build 生成的镜像标签固定为 v1.0.0（由 docker-compose.yml 中 image 字段决定）
# 这里根据用户传入的版本号重新打标签，便于版本管理
show_info "为镜像打上指定版本标签..."
docker tag clinic-saas/backend:v1.0.0 clinic-saas/backend:$VERSION
docker tag clinic-saas/frontend:v1.0.0 clinic-saas/frontend:$VERSION

show_ok "镜像构建完成"
echo ""

# ============================================================
# 步骤 2：导出镜像
# ============================================================

# 将后端和前端镜像打包为单个 tar 文件
# 客户拿到 tar 后可直接执行 docker load -i clinic-saas-images.tar 加载镜像
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

# 清理旧运行时数据，防止误打包给客户
# mysql-data/: MySQL 运行时数据库文件
# patient-uploads/: 患者影像上传文件
# .env: 可能包含当前环境的敏感配置
rm -rf "${DELIVER_DIR}/mysql-data" "${DELIVER_DIR}/patient-uploads" "${DELIVER_DIR}/.env"

# 复制 docker-compose.yml，并将其中固定的镜像版本 v1.0.0 替换为用户指定的版本
# 这样客户部署时会使用与导出镜像一致的标签
sed "s/v1\.0\.0/$VERSION/g" docker-compose.yml > "${DELIVER_DIR}/docker-compose.yml"

# 复制环境变量模板，客户首次部署时需基于此文件创建 .env
cp .env.example "${DELIVER_DIR}/"

# 复制数据库初始化脚本
# MySQL 首次启动时会自动执行 /docker-entrypoint-initdb.d/ 下的 SQL 文件
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

#!/bin/bash
# =============================================================================
# 口腔门诊 SaaS 管理系统 - Docker 一键部署脚本
# 用法: ./deploy.sh [dev|test|prod] [--build] [--down]
# =============================================================================

set -e

# 默认环境
ENV=${1:-dev}
BUILD_FLAG=false
DOWN_FLAG=false

# 解析参数
for arg in "$@"; do
    case $arg in
        --build)
            BUILD_FLAG=true
            ;;
        --down)
            DOWN_FLAG=true
            ;;
    esac
done

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 和 Docker Compose
if ! command -v docker &> /dev/null; then
    log_error "Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    log_error "Docker Compose 未安装"
    exit 1
fi

# 设置 Compose 命令
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

# 工作目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 加载 .env 到 shell 环境（备份脚本需要使用）
if [ -f ".env" ]; then
    set -a
    source .env
    set +a
fi

log_info "========================================"
log_info "口腔门诊 SaaS 管理系统 - Docker 部署"
log_info "环境: ${ENV}"
log_info "工作目录: ${SCRIPT_DIR}"
log_info "========================================"

# 检查 .env 文件
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        log_warn ".env 文件不存在，从 .env.example 复制"
        cp .env.example .env
    else
        log_error ".env 和 .env.example 都不存在"
        exit 1
    fi
fi

# 数据备份（生产环境）
backup_data() {
    if [ "$ENV" == "prod" ]; then
        log_info "执行生产环境数据备份..."
        BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
        mkdir -p "$BACKUP_DIR"

        # 备份数据库
        if docker ps --format '{{.Names}}' | grep -q "clinic-mysql"; then
            docker exec clinic-mysql mysqldump -u root -p"${DB_ROOT_PASSWORD}" clinic_system > "${BACKUP_DIR}/clinic_system.sql" 2>/dev/null || log_warn "数据库备份失败"
        fi

        # 备份上传文件
        if [ -d "./patient_uploads" ]; then
            cp -r ./patient_uploads "${BACKUP_DIR}/" || log_warn "上传文件备份失败"
        fi

        log_success "备份完成: ${BACKUP_DIR}"
    fi
}

# 停止服务
if [ "$DOWN_FLAG" == "true" ]; then
    log_info "停止所有服务..."
    ${COMPOSE_CMD} --env-file .env down
    log_success "服务已停止"
    exit 0
fi

# 执行备份
backup_data

# 根据环境选择 profile
COMPOSE_PROFILES=""
if [ "$ENV" == "prod" ]; then
    COMPOSE_PROFILES="--profile prod"
    log_info "启用生产环境配置（Nginx 反向代理）"
fi

# 构建镜像
if [ "$BUILD_FLAG" == "true" ] || [ "$ENV" == "prod" ]; then
    log_info "构建 Docker 镜像..."
    ${COMPOSE_CMD} --env-file .env ${COMPOSE_PROFILES} build --no-cache
fi

# 启动服务
log_info "启动服务..."
${COMPOSE_CMD} --env-file .env ${COMPOSE_PROFILES} up -d

# 等待服务就绪
log_info "等待服务健康检查..."
sleep 10

# 检查 MySQL
for i in {1..30}; do
    if docker ps --format '{{.Names}} {{.Status}}' | grep "clinic-mysql" | grep -q "healthy"; then
        log_success "MySQL 服务就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        log_error "MySQL 服务启动超时"
        exit 1
    fi
    sleep 2
done

# 检查后端
for i in {1..30}; do
    if docker ps --format '{{.Names}} {{.Status}}' | grep "clinic-backend" | grep -q "healthy"; then
        log_success "后端服务就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        log_warn "后端服务健康检查超时，请手动检查日志: docker logs clinic-backend"
    fi
    sleep 2
done

log_info "========================================"
log_success "部署完成！"
log_info "前端访问: http://localhost:${FRONTEND_PORT:-7070}"
log_info "后端 API: http://localhost:${BACKEND_PORT:-8080}"
if [ "$ENV" == "prod" ]; then
    log_info "Nginx 入口: http://localhost:${NGINX_HTTP_PORT:-80}"
fi
log_info "========================================"
log_info "常用命令:"
log_info "  查看日志: docker logs -f clinic-backend"
log_info "  查看状态: ${COMPOSE_CMD} ps"
log_info "  停止服务: ./deploy.sh ${ENV} --down"
log_info "========================================"

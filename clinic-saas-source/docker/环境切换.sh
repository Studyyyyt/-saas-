#!/bin/bash
# ============================================================
# 口腔诊所 SaaS 系统 - 环境切换脚本
# 用法:
#   ./环境切换.sh           # 交互式菜单
#   ./环境切换.sh dev       # 直接启动开发环境
#   ./环境切换.sh prod      # 直接启动生产环境
# ============================================================

# 颜色定义
C_RED='\033[0;31m'
C_GREEN='\033[0;32m'
C_YELLOW='\033[1;33m'
C_BLUE='\033[0;34m'
C_GRAY='\033[0;37m'
C_RESET='\033[0m'

# 脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# ============================================================
# 辅助函数
# ============================================================

show_title() {
    echo -e "${C_GREEN}========================================${C_RESET}"
    echo -e "${C_GREEN}  $1${C_RESET}"
    echo -e "${C_GREEN}========================================${C_RESET}"
}

show_info() {
    echo -e "${C_BLUE}[信息]${C_RESET} $1"
}

show_ok() {
    echo -e "${C_GREEN}[成功]${C_RESET} $1"
}

show_warn() {
    echo -e "${C_YELLOW}[警告]${C_RESET} $1"
}

show_err() {
    echo -e "${C_RED}[错误]${C_RESET} $1"
}

# 停止所有相关环境
stop_all() {
    show_info "正在检查现有运行中的环境..."

    # 检查并停止开发环境
    if docker compose -f docker-compose.dev.yml ps -q 2>/dev/null | grep -q .; then
        show_warn "检测到【开发环境】正在运行，正在停止..."
        docker compose -f docker-compose.dev.yml down
        show_ok "开发环境已停止"
        echo ""
    fi

    # 检查并停止生产环境
    if docker compose -f docker-compose.yml ps -q 2>/dev/null | grep -q .; then
        show_warn "检测到【生产环境】正在运行，正在停止..."
        docker compose -f docker-compose.yml down
        show_ok "生产环境已停止"
        echo ""
    fi
}

# 等待服务健康检查
wait_healthy() {
    local svc_name=$1
    local check_count=0
    local max_count=30

    show_info "等待 ${svc_name} 健康检查通过..."
    while [ $check_count -lt $max_count ]; do
        if docker compose ps "$svc_name" 2>/dev/null | grep -q "healthy"; then
            show_ok "${svc_name} 健康检查通过"
            return 0
        fi
        check_count=$((check_count + 1))
        echo -n "."
        sleep 2
    done
    echo ""
    show_warn "${svc_name} 健康检查超时，但服务可能仍在启动中"
    return 1
}

# ============================================================
# 启动开发环境
# ============================================================
start_dev() {
    show_title "正在启动开发环境"
    echo ""
    echo -e "${C_GRAY}开发环境特点：${C_RESET}"
    echo "  · 本地源码通过挂载方式直接进入容器"
    echo "  · 修改 Java/Vue 代码后自动热更新/热重启"
    echo "  · 不需要构建镜像，启动速度快"
    echo "  · 适合日常编码、调试、功能开发"
    echo ""

    stop_all

    show_info "正在启动开发环境容器..."
    if ! docker compose -f docker-compose.dev.yml up -d; then
        show_err "开发环境启动失败，请检查上方错误信息"
        exit 1
    fi

    echo ""
    wait_healthy "mysql"
    echo ""

    show_title "开发环境启动完成"
    echo -e "  前端页面: ${C_YELLOW}http://localhost:7070${C_RESET}"
    echo -e "  后端接口: ${C_YELLOW}http://localhost:8080${C_RESET}"
    echo -e "  数据库:   ${C_YELLOW}localhost:3306${C_RESET}"
    echo ""
    echo -e "${C_GRAY}常用命令：${C_RESET}"
    echo -e "  查看日志: ${C_YELLOW}docker compose -f docker-compose.dev.yml logs -f${C_RESET}"
    echo -e "  停止环境: ${C_YELLOW}docker compose -f docker-compose.dev.yml down${C_RESET}"
    echo -e "  后端热重启（修改Java代码后）: ${C_YELLOW}docker compose -f docker-compose.dev.yml exec backend mvn compile -q${C_RESET}"
    echo ""
}

# ============================================================
# 启动生产环境
# ============================================================
start_prod() {
    show_title "正在启动生产环境"
    echo ""
    echo -e "${C_GRAY}生产环境特点：${C_RESET}"
    echo "  · 源码在镜像构建阶段编译打包（JAR + 静态文件）"
    echo "  · 使用精简 JRE 运行，性能更好、资源占用更低"
    echo "  · 前端使用 Nginx 托管，支持 Gzip 压缩和静态缓存"
    echo "  · 修改代码后需要重新构建镜像才能生效"
    echo "  · 适合对外提供服务、演示、正式使用"
    echo ""

    stop_all

    # 询问是否重新构建
    echo -e "${C_YELLOW}是否需要重新构建镜像？${C_RESET}"
    echo ""
    echo "  [1] 是 - 首次部署或代码有修改时选择（耗时较长）"
    echo "  [2] 否 - 仅重启已有容器（启动更快，适合服务器重启后）"
    echo ""
    read -rp "请输入选项 [1/2，默认1]: " build_choice
    build_choice=${build_choice:-1}
    echo ""

    if [ "$build_choice" = "1" ] || [ "$build_choice" = "是" ]; then
        show_info "正在构建镜像并启动生产环境（首次构建可能需要 3-10 分钟）..."
        if ! docker compose -f docker-compose.yml up -d --build; then
            show_err "生产环境启动失败，请检查上方错误信息"
            exit 1
        fi
    else
        show_info "正在启动生产环境（使用已有镜像）..."
        if ! docker compose -f docker-compose.yml up -d; then
            show_err "生产环境启动失败，请检查上方错误信息"
            exit 1
        fi
    fi

    echo ""
    wait_healthy "mysql"
    wait_healthy "backend"
    echo ""

    show_title "生产环境启动完成"
    echo -e "  前端页面: ${C_YELLOW}http://localhost:7070${C_RESET}"
    echo -e "  后端接口: ${C_YELLOW}http://localhost:8080${C_RESET}"
    echo ""
    echo -e "${C_GRAY}常用命令：${C_RESET}"
    echo -e "  查看日志: ${C_YELLOW}docker compose -f docker-compose.yml logs -f${C_RESET}"
    echo -e "  停止环境: ${C_YELLOW}docker compose -f docker-compose.yml down${C_RESET}"
    echo -e "  强制重建: ${C_YELLOW}docker compose -f docker-compose.yml up -d --build${C_RESET}"
    echo ""
}

# ============================================================
# 显示当前状态
# ============================================================
show_status() {
    show_title "当前环境状态"
    echo ""

    local dev_running=false
    local prod_running=false

    if docker compose -f docker-compose.dev.yml ps -q 2>/dev/null | grep -q .; then
        dev_running=true
    fi

    if docker compose -f docker-compose.yml ps -q 2>/dev/null | grep -q .; then
        prod_running=true
    fi

    if $dev_running && $prod_running; then
        show_warn "异常：开发环境和生产环境同时运行中！"
    elif $dev_running; then
        show_ok "当前运行环境：开发环境"
        docker compose -f docker-compose.dev.yml ps
    elif $prod_running; then
        show_ok "当前运行环境：生产环境"
        docker compose -f docker-compose.yml ps
    else
        show_info "当前没有环境在运行"
    fi
    echo ""
}

# ============================================================
# 主菜单（交互模式）
# ============================================================
show_menu() {
    while true; do
        show_title "口腔诊所 SaaS 系统 - 环境切换工具"
        echo ""
        echo "  [1] 启动开发环境（日常编码，热更新）"
        echo "  [2] 启动生产环境（对外服务，高性能）"
        echo "  [3] 查看当前环境状态"
        echo "  [4] 停止所有环境"
        echo "  [0] 退出"
        echo ""
        read -rp "请输入选项 [0-4]: " main_choice
        echo ""

        case "$main_choice" in
            1|dev)
                start_dev
                break
                ;;
            2|prod)
                start_prod
                break
                ;;
            3|status)
                show_status
                ;;
            4|stop|down)
                stop_all
                show_ok "所有环境已停止"
                echo ""
                ;;
            0|exit|quit)
                show_info "再见"
                exit 0
                ;;
            *)
                show_err "无效选项，请重新输入"
                echo ""
                ;;
        esac
    done
}

# ============================================================
# 入口
# ============================================================
case "$1" in
    dev|开发|开发环境)
        start_dev
        ;;
    prod|生产|生产环境)
        start_prod
        ;;
    status|状态)
        show_status
        ;;
    stop|停止|down)
        stop_all
        ;;
    *)
        show_menu
        ;;
esac

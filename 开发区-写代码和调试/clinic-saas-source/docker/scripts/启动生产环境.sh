#!/bin/bash
# ============================================================
# 快捷脚本：一键启动生产环境
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DOCKER_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$DOCKER_ROOT" || exit 1

exec ./scripts/环境切换.sh prod

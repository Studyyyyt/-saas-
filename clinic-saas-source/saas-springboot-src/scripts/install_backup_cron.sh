#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup_disaster_recovery.sh"
DEFAULT_ENV_FILE="$SCRIPT_DIR/disaster_recovery.env"
SCHEDULE="${BACKUP_CRON_SCHEDULE:-0 2 * * *}"
MARKER="# shuao-clinic-saas-disaster-recovery"
ENV_ARG=""

usage() {
  cat <<'EOF'
用法:
  install_backup_cron.sh [options]

选项:
  --schedule "CRON"   指定 cron 表达式，默认 `0 2 * * *`
  --env-file PATH     指定备份环境文件；若未传且默认 env 存在则自动使用
  --help, -h          显示帮助
EOF
}

fail() {
  printf '[%s] ERROR: %s\n' "$(date '+%F %T')" "$*" >&2
  exit 1
}

command -v crontab >/dev/null 2>&1 || fail "当前系统缺少 crontab 命令"
[[ -x "$BACKUP_SCRIPT" ]] || fail "备份脚本不存在或不可执行: $BACKUP_SCRIPT"

while (($#)); do
  case "$1" in
    --schedule)
      [[ $# -ge 2 ]] || fail "--schedule 缺少参数"
      SCHEDULE="$2"
      shift 2
      ;;
    --env-file)
      [[ $# -ge 2 ]] || fail "--env-file 缺少参数"
      ENV_ARG="--env-file $(printf '%q' "$2")"
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      fail "未知参数: $1"
      ;;
  esac
done

if [[ -z "$ENV_ARG" && -f "$DEFAULT_ENV_FILE" ]]; then
  ENV_ARG="--env-file $(printf '%q' "$DEFAULT_ENV_FILE")"
fi

CURRENT_CRON="$(crontab -l 2>/dev/null || true)"
FILTERED_CRON="$(printf '%s\n' "$CURRENT_CRON" | grep -F -v "$MARKER" || true)"
QUOTED_BACKUP_SCRIPT="$(printf '%q' "$BACKUP_SCRIPT")"
NEW_LINE="$SCHEDULE /usr/bin/env bash $QUOTED_BACKUP_SCRIPT"
if [[ -n "$ENV_ARG" ]]; then
  NEW_LINE="$NEW_LINE $ENV_ARG"
fi
NEW_LINE="$NEW_LINE >> /var/log/shuao-backup.log 2>&1 $MARKER"

{
  if [[ -n "$FILTERED_CRON" ]]; then
    printf '%s\n' "$FILTERED_CRON"
  fi
  printf '%s\n' "$NEW_LINE"
} | crontab -

printf '[%s] 已安装备份定时任务: %s\n' "$(date '+%F %T')" "$NEW_LINE"

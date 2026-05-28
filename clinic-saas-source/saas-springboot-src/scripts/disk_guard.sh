#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_ENV_FILE="$SCRIPT_DIR/disk_guard.env"
ENV_FILE=""
ARGS=("$@")

fail() {
  printf '[%s] ERROR: %s\n' "$(date '+%F %T')" "$*" >&2
  exit 1
}

usage() {
  cat <<'EOF'
用法:
  disk_guard.sh [options]

选项:
  --mode MODE               检查模式：deploy 或 monitor，默认 monitor
  --mount PATH              检查挂载点，默认 /
  --min-free-gb N           发布拦截要求最少空闲 GB，默认 5
  --min-free-percent N      发布拦截要求最少空闲百分比，默认 15
  --warn-used-percent N     巡检告警阈值，默认 80
  --crit-used-percent N     巡检严重告警阈值，默认 90
  --top-count N             异常时输出每个目录下前 N 条磁盘明细，默认 8
  --top-path PATH           异常时统计的目录，可重复传入
  --env-file PATH           指定环境变量文件；未传时若默认 env 存在则自动加载
  --quiet-ok                正常时不输出，仅异常时输出
  --help, -h                显示帮助
EOF
}

for ((i = 0; i < ${#ARGS[@]}; i++)); do
  if [[ "${ARGS[i]}" == "--env-file" ]]; then
    (( i + 1 < ${#ARGS[@]} )) || fail "--env-file 缺少参数"
    ENV_FILE="${ARGS[i + 1]}"
  fi
done

if [[ -z "$ENV_FILE" && -f "$DEFAULT_ENV_FILE" ]]; then
  ENV_FILE="$DEFAULT_ENV_FILE"
fi

if [[ -n "$ENV_FILE" ]]; then
  [[ -f "$ENV_FILE" ]] || fail "环境文件不存在: $ENV_FILE"
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

MODE="${MODE:-monitor}"
MOUNT_PATH="${MOUNT_PATH:-/}"
MIN_FREE_GB="${MIN_FREE_GB:-5}"
MIN_FREE_PERCENT="${MIN_FREE_PERCENT:-15}"
WARN_USED_PERCENT="${WARN_USED_PERCENT:-80}"
CRIT_USED_PERCENT="${CRIT_USED_PERCENT:-90}"
TOP_COUNT="${TOP_COUNT:-8}"
TOP_PATHS_RAW="${TOP_PATHS:-/root /var/log}"
QUIET_OK="${QUIET_OK:-false}"
TOP_PATH_ARGS=()

while (($#)); do
  case "$1" in
    --mode)
      [[ $# -ge 2 ]] || fail "--mode 缺少参数"
      MODE="$2"
      shift 2
      ;;
    --mount)
      [[ $# -ge 2 ]] || fail "--mount 缺少参数"
      MOUNT_PATH="$2"
      shift 2
      ;;
    --min-free-gb)
      [[ $# -ge 2 ]] || fail "--min-free-gb 缺少参数"
      MIN_FREE_GB="$2"
      shift 2
      ;;
    --min-free-percent)
      [[ $# -ge 2 ]] || fail "--min-free-percent 缺少参数"
      MIN_FREE_PERCENT="$2"
      shift 2
      ;;
    --warn-used-percent)
      [[ $# -ge 2 ]] || fail "--warn-used-percent 缺少参数"
      WARN_USED_PERCENT="$2"
      shift 2
      ;;
    --crit-used-percent)
      [[ $# -ge 2 ]] || fail "--crit-used-percent 缺少参数"
      CRIT_USED_PERCENT="$2"
      shift 2
      ;;
    --top-count)
      [[ $# -ge 2 ]] || fail "--top-count 缺少参数"
      TOP_COUNT="$2"
      shift 2
      ;;
    --top-path)
      [[ $# -ge 2 ]] || fail "--top-path 缺少参数"
      TOP_PATH_ARGS+=("$2")
      shift 2
      ;;
    --env-file)
      [[ $# -ge 2 ]] || fail "--env-file 缺少参数"
      shift 2
      ;;
    --quiet-ok)
      QUIET_OK=true
      shift
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

is_integer() {
  [[ "$1" =~ ^[0-9]+$ ]]
}

is_integer "$MIN_FREE_GB" || fail "MIN_FREE_GB 必须是非负整数"
is_integer "$MIN_FREE_PERCENT" || fail "MIN_FREE_PERCENT 必须是非负整数"
is_integer "$WARN_USED_PERCENT" || fail "WARN_USED_PERCENT 必须是非负整数"
is_integer "$CRIT_USED_PERCENT" || fail "CRIT_USED_PERCENT 必须是非负整数"
is_integer "$TOP_COUNT" || fail "TOP_COUNT 必须是非负整数"
[[ "$MODE" == "deploy" || "$MODE" == "monitor" ]] || fail "MODE 仅支持 deploy 或 monitor"
(( WARN_USED_PERCENT <= CRIT_USED_PERCENT )) || fail "WARN_USED_PERCENT 不能大于 CRIT_USED_PERCENT"

if [[ ${#TOP_PATH_ARGS[@]} -gt 0 ]]; then
  TOP_PATHS=("${TOP_PATH_ARGS[@]}")
else
  read -r -a TOP_PATHS <<< "$TOP_PATHS_RAW"
fi

[[ -d "$MOUNT_PATH" ]] || fail "挂载点不存在或不是目录: $MOUNT_PATH"

read -r TOTAL_KB USED_KB AVAIL_KB USED_PCT_RAW < <(df -Pk "$MOUNT_PATH" | awk 'NR==2 {print $2, $3, $4, $5}')
read -r TOTAL_H USED_H AVAIL_H < <(df -hP "$MOUNT_PATH" | awk 'NR==2 {print $2, $3, $4}')
USED_PCT="${USED_PCT_RAW%\%}"
FREE_PCT=$((100 - USED_PCT))

print_summary() {
  local level="$1"
  local detail="$2"
  printf '[DISK][%s] mount=%s total=%s used=%s free=%s used_pct=%s%% free_pct=%s%% %s\n' \
    "$level" "$MOUNT_PATH" "$TOTAL_H" "$USED_H" "$AVAIL_H" "$USED_PCT" "$FREE_PCT" "$detail"
}

print_top_paths() {
  local target
  for target in "${TOP_PATHS[@]}"; do
    [[ -e "$target" ]] || continue
    echo "[DISK] top entries under $target"
    du -x -h --max-depth=1 "$target" 2>/dev/null | sort -h | tail -n "$TOP_COUNT"
  done
}

run_deploy_check() {
  local min_free_kb=$((MIN_FREE_GB * 1024 * 1024))
  local -a failures=()

  if (( AVAIL_KB < min_free_kb )); then
    failures+=("free space ${AVAIL_H} below required ${MIN_FREE_GB}G")
  fi
  if (( FREE_PCT < MIN_FREE_PERCENT )); then
    failures+=("free percent ${FREE_PCT}% below required ${MIN_FREE_PERCENT}%")
  fi

  if (( ${#failures[@]} > 0 )); then
    print_summary "BLOCK" "deployment blocked"
    printf '[DISK][BLOCK] %s\n' "${failures[@]}"
    print_top_paths
    exit 1
  fi

  if [[ "$QUIET_OK" != "true" ]]; then
    print_summary "OK" "deployment guard passed"
  fi
}

run_monitor_check() {
  local level="OK"
  local exit_code=0

  if (( USED_PCT >= CRIT_USED_PERCENT )); then
    level="CRITICAL"
    exit_code=2
  elif (( USED_PCT >= WARN_USED_PERCENT )); then
    level="WARNING"
    exit_code=1
  fi

  if (( exit_code == 0 )); then
    if [[ "$QUIET_OK" != "true" ]]; then
      print_summary "$level" "monitor thresholds warn=${WARN_USED_PERCENT}% critical=${CRIT_USED_PERCENT}%"
    fi
    return 0
  fi

  print_summary "$level" "monitor thresholds warn=${WARN_USED_PERCENT}% critical=${CRIT_USED_PERCENT}%"
  print_top_paths
  return "$exit_code"
}

case "$MODE" in
  deploy)
    run_deploy_check
    ;;
  monitor)
    run_monitor_check
    ;;
esac

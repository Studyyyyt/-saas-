#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ORIGINAL_ARGS=("$@")
ENV_FILE="${DR_ENV_FILE:-$SCRIPT_DIR/disaster_recovery.env}"

usage() {
  cat <<'EOF'
用法:
  backup_disaster_recovery.sh [options]

选项:
  --env-file PATH         指定环境变量文件
  --output-root PATH      指定备份根目录
  --label TEXT            指定快照标签前缀
  --skip-db               跳过 MySQL 备份
  --skip-uploads          跳过患者影像目录备份
  --skip-config           跳过 application.yml 备份
  --skip-artifact         跳过运行 jar 备份
  --skip-backend-source   跳过后端源码归档
  --skip-frontend-source  跳过前端源码归档
  --dry-run               只打印计划，不实际执行
  --help, -h              显示帮助
EOF
}

log() {
  printf '[%s] %s\n' "$(date '+%F %T')" "$*"
}

fail() {
  log "ERROR: $*"
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "缺少命令: $1"
}

load_env_file() {
  local file="$1"
  if [[ -f "$file" ]]; then
    # shellcheck disable=SC1090
    source "$file"
  fi
}

for ((i = 0; i < ${#ORIGINAL_ARGS[@]}; i++)); do
  case "${ORIGINAL_ARGS[$i]}" in
    --env-file)
      ((i + 1 < ${#ORIGINAL_ARGS[@]})) || fail "--env-file 缺少参数"
      ENV_FILE="${ORIGINAL_ARGS[$((i + 1))]}"
      ;;
    --help|-h)
      usage
      exit 0
      ;;
  esac
done

load_env_file "$ENV_FILE"

BACKUP_ROOT="${BACKUP_ROOT:-/root/disaster-recovery-backups}"
BACKUP_LABEL="${BACKUP_LABEL:-shuao-clinic-saas}"
BACKUP_KEEP_DAYS="${BACKUP_KEEP_DAYS:-14}"
BACKUP_KEEP_COUNT="${BACKUP_KEEP_COUNT:-30}"
LOCK_FILE="${LOCK_FILE:-}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-clinic_system_new}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-root}"
MYSQLDUMP_EXTRA_ARGS="${MYSQLDUMP_EXTRA_ARGS:---single-transaction --quick --routines --events --triggers --set-gtid-purged=OFF}"

UPLOAD_DIR="${UPLOAD_DIR:-$HOME/.local/uploads/patient-images}"
SPRING_CONFIG_FILE="${SPRING_CONFIG_FILE:-/root/saas-springboot-src/src/main/resources/application.yml}"
RUNTIME_JAR_PATH="${RUNTIME_JAR_PATH:-/root/newsystem-backend-cloud.jar}"
FALLBACK_ARTIFACT_PATH="${FALLBACK_ARTIFACT_PATH:-/root/saas-springboot-src/target/springboot-0.0.1-SNAPSHOT.jar}"
BACKEND_SOURCE_DIR="${BACKEND_SOURCE_DIR:-/root/saas-springboot-src}"
FRONTEND_SOURCE_DIR="${FRONTEND_SOURCE_DIR:-/root/saas-vue-src}"
POST_BACKUP_HOOK="${POST_BACKUP_HOOK:-}"

INCLUDE_DB=1
INCLUDE_UPLOADS=1
INCLUDE_CONFIG=1
INCLUDE_ARTIFACT=1
INCLUDE_BACKEND_SOURCE=1
INCLUDE_FRONTEND_SOURCE=1
DRY_RUN=0

while (($#)); do
  case "$1" in
    --env-file)
      shift 2
      ;;
    --output-root)
      [[ $# -ge 2 ]] || fail "--output-root 缺少参数"
      BACKUP_ROOT="$2"
      shift 2
      ;;
    --label)
      [[ $# -ge 2 ]] || fail "--label 缺少参数"
      BACKUP_LABEL="$2"
      shift 2
      ;;
    --skip-db)
      INCLUDE_DB=0
      shift
      ;;
    --skip-uploads)
      INCLUDE_UPLOADS=0
      shift
      ;;
    --skip-config)
      INCLUDE_CONFIG=0
      shift
      ;;
    --skip-artifact)
      INCLUDE_ARTIFACT=0
      shift
      ;;
    --skip-backend-source)
      INCLUDE_BACKEND_SOURCE=0
      shift
      ;;
    --skip-frontend-source)
      INCLUDE_FRONTEND_SOURCE=0
      shift
      ;;
    --dry-run)
      DRY_RUN=1
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

if [[ -z "$LOCK_FILE" ]]; then
  LOCK_FILE="$BACKUP_ROOT/.backup.lock"
fi

mkdir -p "$BACKUP_ROOT"
exec 9>"$LOCK_FILE"
flock -n 9 || fail "已有备份任务在执行: $LOCK_FILE"

require_command mysqldump
require_command tar
require_command gzip
require_command sha256sum
require_command flock
require_command find

TIMESTAMP="$(date '+%Y%m%d-%H%M%S')"
HOSTNAME_SHORT="$(hostname -s 2>/dev/null || hostname || echo unknown-host)"
SNAPSHOT_NAME="${BACKUP_LABEL}-${TIMESTAMP}"
SNAPSHOT_ROOT="$BACKUP_ROOT/snapshots"
STAGING_ROOT="$BACKUP_ROOT/.staging"
SNAPSHOT_DIR="$SNAPSHOT_ROOT/$SNAPSHOT_NAME"
STAGING_DIR="$STAGING_ROOT/$SNAPSHOT_NAME"

cleanup_staging() {
  if [[ -n "${STAGING_DIR:-}" && -d "${STAGING_DIR:-}" ]]; then
    rm -rf "$STAGING_DIR"
  fi
}

trap cleanup_staging EXIT

mkdir -p "$SNAPSHOT_ROOT" "$STAGING_ROOT"
[[ ! -e "$SNAPSHOT_DIR" ]] || fail "快照目录已存在: $SNAPSHOT_DIR"
mkdir -p "$STAGING_DIR"

ARTIFACTS=()

register_artifact() {
  ARTIFACTS+=("$1")
}

preview_action() {
  log "计划输出目录: $SNAPSHOT_DIR"
  log "备份数据库: $([[ $INCLUDE_DB -eq 1 ]] && echo 是 || echo 否)"
  log "备份影像目录: $([[ $INCLUDE_UPLOADS -eq 1 ]] && echo 是 || echo 否)"
  log "备份配置文件: $([[ $INCLUDE_CONFIG -eq 1 ]] && echo 是 || echo 否)"
  log "备份运行 jar: $([[ $INCLUDE_ARTIFACT -eq 1 ]] && echo 是 || echo 否)"
  log "备份后端源码: $([[ $INCLUDE_BACKEND_SOURCE -eq 1 ]] && echo 是 || echo 否)"
  log "备份前端源码: $([[ $INCLUDE_FRONTEND_SOURCE -eq 1 ]] && echo 是 || echo 否)"
}

if [[ $DRY_RUN -eq 1 ]]; then
  preview_action
  exit 0
fi

build_tar_from_paths() {
  local archive_path="$1"
  local base_dir="$2"
  shift 2

  local existing_paths=()
  local path
  for path in "$@"; do
    [[ -e "$base_dir/$path" ]] && existing_paths+=("$path")
  done

  ((${#existing_paths[@]} > 0)) || fail "归档源为空: $base_dir"

  tar \
    --exclude='./._*' \
    --exclude='._*' \
    --exclude='src/main/resources/static.bak*' \
    --exclude='src/main/resources/static_backup_*' \
    -C "$base_dir" \
    -czf "$archive_path" \
    "${existing_paths[@]}"
}

select_artifact_source() {
  if [[ -f "$RUNTIME_JAR_PATH" ]]; then
    printf '%s\n' "$RUNTIME_JAR_PATH"
    return 0
  fi
  if [[ -f "$FALLBACK_ARTIFACT_PATH" ]]; then
    printf '%s\n' "$FALLBACK_ARTIFACT_PATH"
    return 0
  fi
  return 1
}

if [[ $INCLUDE_DB -eq 1 ]]; then
  log "导出 MySQL 数据库: $DB_NAME"
  DB_DUMP_NAME="database-${DB_NAME}.sql.gz"
  IFS=' ' read -r -a MYSQLDUMP_ARGS <<< "$MYSQLDUMP_EXTRA_ARGS"
  MYSQL_PWD="$DB_PASSWORD" mysqldump \
    -h "$DB_HOST" \
    -P "$DB_PORT" \
    -u "$DB_USER" \
    "${MYSQLDUMP_ARGS[@]}" \
    "$DB_NAME" | gzip -9 > "$STAGING_DIR/$DB_DUMP_NAME"
  gzip -t "$STAGING_DIR/$DB_DUMP_NAME"
  register_artifact "$DB_DUMP_NAME"
fi

if [[ $INCLUDE_UPLOADS -eq 1 ]]; then
  [[ -d "$UPLOAD_DIR" ]] || fail "影像目录不存在: $UPLOAD_DIR"
  log "打包影像目录: $UPLOAD_DIR"
  UPLOAD_ARCHIVE_NAME="uploads-$(basename "$UPLOAD_DIR").tar.gz"
  tar -C "$(dirname "$UPLOAD_DIR")" -czf "$STAGING_DIR/$UPLOAD_ARCHIVE_NAME" "$(basename "$UPLOAD_DIR")"
  tar -tzf "$STAGING_DIR/$UPLOAD_ARCHIVE_NAME" >/dev/null
  register_artifact "$UPLOAD_ARCHIVE_NAME"
fi

if [[ $INCLUDE_CONFIG -eq 1 ]]; then
  [[ -f "$SPRING_CONFIG_FILE" ]] || fail "配置文件不存在: $SPRING_CONFIG_FILE"
  log "复制配置文件: $SPRING_CONFIG_FILE"
  CONFIG_ARCHIVE_NAME="application.yml"
  cp "$SPRING_CONFIG_FILE" "$STAGING_DIR/$CONFIG_ARCHIVE_NAME"
  register_artifact "$CONFIG_ARCHIVE_NAME"
fi

ARTIFACT_SOURCE=""
ARTIFACT_ARCHIVE_NAME=""
if [[ $INCLUDE_ARTIFACT -eq 1 ]]; then
  ARTIFACT_SOURCE="$(select_artifact_source)" || fail "未找到运行 jar，可检查 RUNTIME_JAR_PATH / FALLBACK_ARTIFACT_PATH"
  ARTIFACT_ARCHIVE_NAME="$(basename "$ARTIFACT_SOURCE")"
  log "复制运行制品: $ARTIFACT_SOURCE"
  cp "$ARTIFACT_SOURCE" "$STAGING_DIR/$ARTIFACT_ARCHIVE_NAME"
  register_artifact "$ARTIFACT_ARCHIVE_NAME"
fi

if [[ $INCLUDE_BACKEND_SOURCE -eq 1 ]]; then
  [[ -d "$BACKEND_SOURCE_DIR" ]] || fail "后端源码目录不存在: $BACKEND_SOURCE_DIR"
  log "归档后端源码: $BACKEND_SOURCE_DIR"
  BACKEND_ARCHIVE_NAME="backend-project.tar.gz"
  build_tar_from_paths "$STAGING_DIR/$BACKEND_ARCHIVE_NAME" "$BACKEND_SOURCE_DIR" pom.xml scripts sql docs src
  tar -tzf "$STAGING_DIR/$BACKEND_ARCHIVE_NAME" >/dev/null
  register_artifact "$BACKEND_ARCHIVE_NAME"
fi

if [[ $INCLUDE_FRONTEND_SOURCE -eq 1 ]]; then
  [[ -d "$FRONTEND_SOURCE_DIR" ]] || fail "前端源码目录不存在: $FRONTEND_SOURCE_DIR"
  log "归档前端源码: $FRONTEND_SOURCE_DIR"
  FRONTEND_ARCHIVE_NAME="frontend-project.tar.gz"
  build_tar_from_paths "$STAGING_DIR/$FRONTEND_ARCHIVE_NAME" "$FRONTEND_SOURCE_DIR" package.json package-lock.json babel.config.js jsconfig.json vue.config.js public src tests qidong.bat
  tar -tzf "$STAGING_DIR/$FRONTEND_ARCHIVE_NAME" >/dev/null
  register_artifact "$FRONTEND_ARCHIVE_NAME"
fi

RUNNING_JARS="$(ps -eo pid=,args= | awk '/[j]ava/ && /-jar/ {print}' | paste -sd ' | ' -)"

cat > "$STAGING_DIR/manifest.txt" <<EOF
snapshot_name=$SNAPSHOT_NAME
created_at=$(date -Iseconds)
host=$HOSTNAME_SHORT
backup_root=$BACKUP_ROOT
database_name=$DB_NAME
database_host=$DB_HOST
database_port=$DB_PORT
database_dump_archive=${DB_DUMP_NAME:-}
upload_dir=$UPLOAD_DIR
upload_archive=${UPLOAD_ARCHIVE_NAME:-}
config_source=$SPRING_CONFIG_FILE
config_archive=${CONFIG_ARCHIVE_NAME:-}
artifact_source=${ARTIFACT_SOURCE:-}
artifact_archive=${ARTIFACT_ARCHIVE_NAME:-}
backend_source_dir=$BACKEND_SOURCE_DIR
backend_source_archive=${BACKEND_ARCHIVE_NAME:-}
frontend_source_dir=$FRONTEND_SOURCE_DIR
frontend_source_archive=${FRONTEND_ARCHIVE_NAME:-}
running_jars=${RUNNING_JARS:-}
EOF
register_artifact "manifest.txt"

(
  cd "$STAGING_DIR"
  sha256sum "${ARTIFACTS[@]}" > SHA256SUMS
)

mv "$STAGING_DIR" "$SNAPSHOT_DIR"
ln -sfn "$SNAPSHOT_DIR" "$BACKUP_ROOT/latest"

if [[ "${BACKUP_KEEP_DAYS:-0}" =~ ^[0-9]+$ ]] && ((BACKUP_KEEP_DAYS > 0)); then
  find "$SNAPSHOT_ROOT" -mindepth 1 -maxdepth 1 -type d -mtime +"$BACKUP_KEEP_DAYS" -exec rm -rf {} +
fi

if [[ "${BACKUP_KEEP_COUNT:-0}" =~ ^[0-9]+$ ]] && ((BACKUP_KEEP_COUNT > 0)); then
  mapfile -t SNAPSHOT_DIRS < <(find "$SNAPSHOT_ROOT" -mindepth 1 -maxdepth 1 -type d | sort -r)
  if ((${#SNAPSHOT_DIRS[@]} > BACKUP_KEEP_COUNT)); then
    for old_dir in "${SNAPSHOT_DIRS[@]:BACKUP_KEEP_COUNT}"; do
      rm -rf "$old_dir"
    done
  fi
fi

if [[ -n "$POST_BACKUP_HOOK" ]]; then
  log "执行备份后同步钩子"
  BACKUP_SNAPSHOT_DIR="$SNAPSHOT_DIR" BACKUP_LATEST_LINK="$BACKUP_ROOT/latest" BACKUP_LABEL="$BACKUP_LABEL" bash -lc "$POST_BACKUP_HOOK"
fi

log "备份完成: $SNAPSHOT_DIR"
log "最新快照链接: $BACKUP_ROOT/latest"

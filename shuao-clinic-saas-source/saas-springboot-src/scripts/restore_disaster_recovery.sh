#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ORIGINAL_ARGS=("$@")
ENV_FILE="${DR_ENV_FILE:-$SCRIPT_DIR/disaster_recovery.env}"

usage() {
  cat <<'EOF'
用法:
  restore_disaster_recovery.sh --snapshot PATH_OR_NAME [options]

选项:
  --env-file PATH            指定环境变量文件
  --snapshot PATH_OR_NAME    指定快照目录，或使用快照名
  --verify-only              仅校验快照完整性
  --restore-db               恢复数据库
  --restore-uploads          恢复患者影像目录
  --restore-config           恢复 application.yml
  --restore-artifact         恢复运行 jar
  --restore-backend-source   恢复后端源码
  --restore-frontend-source  恢复前端源码
  --target-db-name NAME      恢复到指定数据库
  --replace-db               若目标数据库已存在则先删库重建
  --upload-target-dir PATH   指定影像恢复目录
  --replace-uploads          覆盖已有影像目录
  --config-target PATH       指定配置文件恢复路径
  --artifact-target PATH     指定 jar 恢复路径
  --overwrite-files          覆盖已有配置/jar 文件
  --backend-source-target PATH
                            指定后端源码恢复目录
  --frontend-source-target PATH
                            指定前端源码恢复目录
  --replace-source           覆盖已有源码目录
  --yes                      跳过确认提示
  --help, -h                 显示帮助
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
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-clinic_system_new}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-root}"
UPLOAD_DIR="${UPLOAD_DIR:-$HOME/.local/uploads/patient-images}"
SPRING_CONFIG_FILE="${SPRING_CONFIG_FILE:-/root/saas-springboot-src/src/main/resources/application.yml}"
RUNTIME_JAR_PATH="${RUNTIME_JAR_PATH:-/root/newsystem-backend-cloud.jar}"
FALLBACK_ARTIFACT_PATH="${FALLBACK_ARTIFACT_PATH:-/root/saas-springboot-src/target/springboot-0.0.1-SNAPSHOT.jar}"
BACKEND_SOURCE_DIR="${BACKEND_SOURCE_DIR:-/root/saas-springboot-src}"
FRONTEND_SOURCE_DIR="${FRONTEND_SOURCE_DIR:-/root/saas-vue-src}"

SNAPSHOT_INPUT=""
VERIFY_ONLY=0
RESTORE_DB=0
RESTORE_UPLOADS=0
RESTORE_CONFIG=0
RESTORE_ARTIFACT=0
RESTORE_BACKEND_SOURCE=0
RESTORE_FRONTEND_SOURCE=0
TARGET_DB_NAME=""
REPLACE_DB=0
REPLACE_UPLOADS=0
OVERWRITE_FILES=0
REPLACE_SOURCE=0
ASSUME_YES=0
UPLOAD_TARGET_DIR=""
CONFIG_TARGET=""
ARTIFACT_TARGET=""
BACKEND_SOURCE_TARGET=""
FRONTEND_SOURCE_TARGET=""

while (($#)); do
  case "$1" in
    --env-file)
      shift 2
      ;;
    --snapshot)
      [[ $# -ge 2 ]] || fail "--snapshot 缺少参数"
      SNAPSHOT_INPUT="$2"
      shift 2
      ;;
    --verify-only)
      VERIFY_ONLY=1
      shift
      ;;
    --restore-db)
      RESTORE_DB=1
      shift
      ;;
    --restore-uploads)
      RESTORE_UPLOADS=1
      shift
      ;;
    --restore-config)
      RESTORE_CONFIG=1
      shift
      ;;
    --restore-artifact)
      RESTORE_ARTIFACT=1
      shift
      ;;
    --restore-backend-source)
      RESTORE_BACKEND_SOURCE=1
      shift
      ;;
    --restore-frontend-source)
      RESTORE_FRONTEND_SOURCE=1
      shift
      ;;
    --target-db-name)
      [[ $# -ge 2 ]] || fail "--target-db-name 缺少参数"
      TARGET_DB_NAME="$2"
      shift 2
      ;;
    --replace-db)
      REPLACE_DB=1
      shift
      ;;
    --upload-target-dir)
      [[ $# -ge 2 ]] || fail "--upload-target-dir 缺少参数"
      UPLOAD_TARGET_DIR="$2"
      shift 2
      ;;
    --replace-uploads)
      REPLACE_UPLOADS=1
      shift
      ;;
    --config-target)
      [[ $# -ge 2 ]] || fail "--config-target 缺少参数"
      CONFIG_TARGET="$2"
      shift 2
      ;;
    --artifact-target)
      [[ $# -ge 2 ]] || fail "--artifact-target 缺少参数"
      ARTIFACT_TARGET="$2"
      shift 2
      ;;
    --overwrite-files)
      OVERWRITE_FILES=1
      shift
      ;;
    --backend-source-target)
      [[ $# -ge 2 ]] || fail "--backend-source-target 缺少参数"
      BACKEND_SOURCE_TARGET="$2"
      shift 2
      ;;
    --frontend-source-target)
      [[ $# -ge 2 ]] || fail "--frontend-source-target 缺少参数"
      FRONTEND_SOURCE_TARGET="$2"
      shift 2
      ;;
    --replace-source)
      REPLACE_SOURCE=1
      shift
      ;;
    --yes)
      ASSUME_YES=1
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

[[ -n "$SNAPSHOT_INPUT" ]] || fail "必须通过 --snapshot 指定恢复快照"

if [[ $VERIFY_ONLY -eq 0 && $RESTORE_DB -eq 0 && $RESTORE_UPLOADS -eq 0 && $RESTORE_CONFIG -eq 0 && $RESTORE_ARTIFACT -eq 0 && $RESTORE_BACKEND_SOURCE -eq 0 && $RESTORE_FRONTEND_SOURCE -eq 0 ]]; then
  RESTORE_DB=1
  RESTORE_UPLOADS=1
  RESTORE_CONFIG=1
  RESTORE_ARTIFACT=1
fi

require_command mysql
require_command gzip
require_command tar
require_command sha256sum

resolve_snapshot_dir() {
  local input="$1"
  if [[ -d "$input" ]]; then
    printf '%s\n' "$input"
    return 0
  fi
  if [[ -d "$BACKUP_ROOT/snapshots/$input" ]]; then
    printf '%s\n' "$BACKUP_ROOT/snapshots/$input"
    return 0
  fi
  if [[ "$input" == "latest" && -L "$BACKUP_ROOT/latest" ]]; then
    readlink -f "$BACKUP_ROOT/latest"
    return 0
  fi
  return 1
}

SNAPSHOT_DIR="$(resolve_snapshot_dir "$SNAPSHOT_INPUT")" || fail "未找到快照: $SNAPSHOT_INPUT"
MANIFEST_FILE="$SNAPSHOT_DIR/manifest.txt"
CHECKSUM_FILE="$SNAPSHOT_DIR/SHA256SUMS"
[[ -f "$MANIFEST_FILE" ]] || fail "缺少 manifest.txt: $SNAPSHOT_DIR"
[[ -f "$CHECKSUM_FILE" ]] || fail "缺少 SHA256SUMS: $SNAPSHOT_DIR"

manifest_get() {
  local key="$1"
  local line
  line="$(grep -E "^${key}=" "$MANIFEST_FILE" | tail -n 1 || true)"
  printf '%s\n' "${line#*=}"
}

confirm() {
  local message="$1"
  if [[ $ASSUME_YES -eq 1 ]]; then
    return 0
  fi
  read -r -p "$message [y/N] " answer
  [[ "$answer" == "y" || "$answer" == "Y" ]]
}

extract_archive_to_target() {
  local archive_path="$1"
  local target_dir="$2"
  local replace_existing="$3"
  local temp_dir
  temp_dir="$(mktemp -d)"

  if [[ -e "$target_dir" ]]; then
    if [[ $replace_existing -ne 1 ]]; then
      rm -rf "$temp_dir"
      fail "目标目录已存在，需显式指定覆盖: $target_dir"
    fi
    confirm "目录 $target_dir 已存在，是否覆盖？" || {
      rm -rf "$temp_dir"
      fail "用户取消"
    }
    rm -rf "$target_dir"
  fi

  mkdir -p "$(dirname "$target_dir")"
  tar -xzf "$archive_path" -C "$temp_dir"
  mapfile -t top_entries < <(find "$temp_dir" -mindepth 1 -maxdepth 1 | sort)
  if ((${#top_entries[@]} != 1)); then
    rm -rf "$temp_dir"
    fail "归档顶层结构异常: $archive_path"
  fi
  mv "${top_entries[0]}" "$target_dir"
  rm -rf "$temp_dir"
}

restore_source_archive() {
  local archive_path="$1"
  local target_dir="$2"
  local replace_existing="$3"

  if [[ -e "$target_dir" ]]; then
    if [[ $replace_existing -ne 1 ]]; then
      fail "源码目录已存在，需显式指定 --replace-source: $target_dir"
    fi
    confirm "源码目录 $target_dir 已存在，是否覆盖？" || fail "用户取消"
    rm -rf "$target_dir"
  fi

  mkdir -p "$target_dir"
  tar -xzf "$archive_path" -C "$target_dir"
}

restore_plain_file() {
  local source_file="$1"
  local target_file="$2"

  if [[ -e "$target_file" && $OVERWRITE_FILES -ne 1 ]]; then
    fail "目标文件已存在，需显式指定 --overwrite-files: $target_file"
  fi
  if [[ -e "$target_file" ]]; then
    confirm "文件 $target_file 已存在，是否覆盖？" || fail "用户取消"
  fi
  mkdir -p "$(dirname "$target_file")"
  cp "$source_file" "$target_file"
}

TARGET_DB_NAME="${TARGET_DB_NAME:-$DB_NAME}"
UPLOAD_TARGET_DIR="${UPLOAD_TARGET_DIR:-$(manifest_get upload_dir)}"
UPLOAD_TARGET_DIR="${UPLOAD_TARGET_DIR:-$UPLOAD_DIR}"
CONFIG_TARGET="${CONFIG_TARGET:-$(manifest_get config_source)}"
CONFIG_TARGET="${CONFIG_TARGET:-$SPRING_CONFIG_FILE}"
ARTIFACT_TARGET="${ARTIFACT_TARGET:-$(manifest_get artifact_source)}"
if [[ -z "$ARTIFACT_TARGET" ]]; then
  if [[ -e "$RUNTIME_JAR_PATH" ]]; then
    ARTIFACT_TARGET="$RUNTIME_JAR_PATH"
  else
    ARTIFACT_TARGET="$FALLBACK_ARTIFACT_PATH"
  fi
fi
BACKEND_SOURCE_TARGET="${BACKEND_SOURCE_TARGET:-$(manifest_get backend_source_dir)}"
BACKEND_SOURCE_TARGET="${BACKEND_SOURCE_TARGET:-$BACKEND_SOURCE_DIR}"
FRONTEND_SOURCE_TARGET="${FRONTEND_SOURCE_TARGET:-$(manifest_get frontend_source_dir)}"
FRONTEND_SOURCE_TARGET="${FRONTEND_SOURCE_TARGET:-$FRONTEND_SOURCE_DIR}"

log "校验快照完整性: $SNAPSHOT_DIR"
(cd "$SNAPSHOT_DIR" && sha256sum -c "$(basename "$CHECKSUM_FILE")")

if [[ $VERIFY_ONLY -eq 1 ]]; then
  log "校验通过"
  exit 0
fi

DB_DUMP_ARCHIVE="$(manifest_get database_dump_archive)"
UPLOAD_ARCHIVE="$(manifest_get upload_archive)"
CONFIG_ARCHIVE="$(manifest_get config_archive)"
ARTIFACT_ARCHIVE="$(manifest_get artifact_archive)"
BACKEND_SOURCE_ARCHIVE="$(manifest_get backend_source_archive)"
FRONTEND_SOURCE_ARCHIVE="$(manifest_get frontend_source_archive)"

if [[ $RESTORE_DB -eq 1 ]]; then
  [[ -n "$DB_DUMP_ARCHIVE" ]] || fail "快照中不包含数据库备份"
  [[ -f "$SNAPSHOT_DIR/$DB_DUMP_ARCHIVE" ]] || fail "数据库归档不存在: $DB_DUMP_ARCHIVE"

  EXISTING_DB="$(MYSQL_PWD="$DB_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -Nse "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='${TARGET_DB_NAME}'")"
  if [[ -n "$EXISTING_DB" ]]; then
    [[ $REPLACE_DB -eq 1 ]] || fail "目标数据库已存在，需显式指定 --replace-db: $TARGET_DB_NAME"
    confirm "数据库 $TARGET_DB_NAME 已存在，是否删库重建？" || fail "用户取消"
    MYSQL_PWD="$DB_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "DROP DATABASE \`$TARGET_DB_NAME\`; CREATE DATABASE \`$TARGET_DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  else
    MYSQL_PWD="$DB_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "CREATE DATABASE \`$TARGET_DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  fi

  log "恢复数据库到: $TARGET_DB_NAME"
  gunzip -c "$SNAPSHOT_DIR/$DB_DUMP_ARCHIVE" | MYSQL_PWD="$DB_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$TARGET_DB_NAME"
fi

if [[ $RESTORE_UPLOADS -eq 1 ]]; then
  [[ -n "$UPLOAD_ARCHIVE" ]] || fail "快照中不包含影像目录备份"
  [[ -f "$SNAPSHOT_DIR/$UPLOAD_ARCHIVE" ]] || fail "影像归档不存在: $UPLOAD_ARCHIVE"
  log "恢复影像目录到: $UPLOAD_TARGET_DIR"
  extract_archive_to_target "$SNAPSHOT_DIR/$UPLOAD_ARCHIVE" "$UPLOAD_TARGET_DIR" "$REPLACE_UPLOADS"
fi

if [[ $RESTORE_CONFIG -eq 1 ]]; then
  [[ -n "$CONFIG_ARCHIVE" ]] || fail "快照中不包含配置文件"
  [[ -f "$SNAPSHOT_DIR/$CONFIG_ARCHIVE" ]] || fail "配置归档不存在: $CONFIG_ARCHIVE"
  log "恢复配置文件到: $CONFIG_TARGET"
  restore_plain_file "$SNAPSHOT_DIR/$CONFIG_ARCHIVE" "$CONFIG_TARGET"
fi

if [[ $RESTORE_ARTIFACT -eq 1 ]]; then
  [[ -n "$ARTIFACT_ARCHIVE" ]] || fail "快照中不包含运行 jar"
  [[ -f "$SNAPSHOT_DIR/$ARTIFACT_ARCHIVE" ]] || fail "jar 归档不存在: $ARTIFACT_ARCHIVE"
  log "恢复运行 jar 到: $ARTIFACT_TARGET"
  restore_plain_file "$SNAPSHOT_DIR/$ARTIFACT_ARCHIVE" "$ARTIFACT_TARGET"
fi

if [[ $RESTORE_BACKEND_SOURCE -eq 1 ]]; then
  [[ -n "$BACKEND_SOURCE_ARCHIVE" ]] || fail "快照中不包含后端源码归档"
  [[ -f "$SNAPSHOT_DIR/$BACKEND_SOURCE_ARCHIVE" ]] || fail "后端源码归档不存在: $BACKEND_SOURCE_ARCHIVE"
  log "恢复后端源码到: $BACKEND_SOURCE_TARGET"
  restore_source_archive "$SNAPSHOT_DIR/$BACKEND_SOURCE_ARCHIVE" "$BACKEND_SOURCE_TARGET" "$REPLACE_SOURCE"
fi

if [[ $RESTORE_FRONTEND_SOURCE -eq 1 ]]; then
  [[ -n "$FRONTEND_SOURCE_ARCHIVE" ]] || fail "快照中不包含前端源码归档"
  [[ -f "$SNAPSHOT_DIR/$FRONTEND_SOURCE_ARCHIVE" ]] || fail "前端源码归档不存在: $FRONTEND_SOURCE_ARCHIVE"
  log "恢复前端源码到: $FRONTEND_SOURCE_TARGET"
  restore_source_archive "$SNAPSHOT_DIR/$FRONTEND_SOURCE_ARCHIVE" "$FRONTEND_SOURCE_TARGET" "$REPLACE_SOURCE"
fi

log "恢复完成: $SNAPSHOT_DIR"

#!/usr/bin/env bash
set -euo pipefail

ROOT="/root"
OUTPUT_DIR="/root/shared-downloads"
KEEP_DAYS="${DOWNLOAD_KEEP_DAYS:-7}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-clinic_system_new}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-root}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
PACKAGE_NAME="daily-backup-$TIMESTAMP.zip"
LATEST_NAME="daily-backup-latest.zip"
STAGING_DIR="$(mktemp -d /tmp/daily-backup-package-XXXXXX)"
PACKAGE_ROOT="$STAGING_DIR/shuao-clinic-saas-daily-backup"
TIMESTAMPED_PATH="$OUTPUT_DIR/$PACKAGE_NAME"
LATEST_PATH="$OUTPUT_DIR/$LATEST_NAME"
MYSQLDUMP_EXTRA_ARGS=(--single-transaction --quick --routines --events --triggers --set-gtid-purged=OFF)

cleanup() {
  rm -rf "$STAGING_DIR"
}
trap cleanup EXIT

prune_old_packages() {
  find "$OUTPUT_DIR" -maxdepth 1 -type f \
    \( -name 'project-source-*.zip' -o -name 'daily-backup-*.zip' \) \
    ! -name '*-latest.zip' \
    -mtime +"$KEEP_DAYS" \
    -print -delete
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "[PACKAGE] missing command: $1" >&2
    exit 1
  }
}

copy_tree() {
  local src_name="$1"
  local src_path="$ROOT/$src_name"
  local dest_path="$PACKAGE_ROOT/$src_name"

  mkdir -p "$dest_path"
  (
    cd "$src_path"
    find . \
      \( -path './node_modules' \
      -o -path './dist' \
      -o -path './target' \
      -o -path './.git' \
      -o -path './.idea' \
      -o -path './src/main/resources/static.bak*' \
      -o -path './src/main/resources/static_backup_*' \
      \) -prune -o \
      -type f \
      ! -name '._*' \
      ! -name '.DS_Store' \
      ! -name '*.log' \
      ! -name '.deploy-build-*' \
      -print
  ) | while IFS= read -r rel; do
    local clean_rel="${rel#./}"
    local from_file="$src_path/$clean_rel"
    local to_file="$dest_path/$clean_rel"
    mkdir -p "$(dirname "$to_file")"
    cp -a "$from_file" "$to_file"
  done
}

require_command mysqldump
require_command jar

mkdir -p "$PACKAGE_ROOT" "$OUTPUT_DIR"

copy_tree "saas-springboot-src"
copy_tree "saas-vue-src"

if [ -f "$ROOT/saas-system-spec.md" ]; then
  cp -a "$ROOT/saas-system-spec.md" "$PACKAGE_ROOT/"
fi

MYSQL_PWD="$DB_PASSWORD" mysqldump \
  -h "$DB_HOST" \
  -P "$DB_PORT" \
  -u "$DB_USER" \
  "${MYSQLDUMP_EXTRA_ARGS[@]}" \
  "$DB_NAME" | gzip -9 > "$PACKAGE_ROOT/database-$DB_NAME.sql.gz"

gzip -t "$PACKAGE_ROOT/database-$DB_NAME.sql.gz"

cat > "$PACKAGE_ROOT/BACKUP_INFO.txt" <<EOF
某某口腔门诊 SaaS 每日全量下载备份

生成时间: $(date '+%Y-%m-%d %H:%M:%S %Z')
数据库: $DB_NAME
备份文件: $PACKAGE_NAME

包含内容:
- saas-springboot-src 源码
- saas-vue-src 源码
- saas-system-spec.md
- database-$DB_NAME.sql.gz

已排除:
- node_modules
- dist
- target
- 静态资源备份目录
- *.log
- .git / .idea
EOF

(
  cd "$STAGING_DIR"
  rm -f "$TIMESTAMPED_PATH" "$LATEST_PATH"
  jar --create --file "$TIMESTAMPED_PATH" shuao-clinic-saas-daily-backup >/dev/null
)

cp -f "$TIMESTAMPED_PATH" "$LATEST_PATH"
prune_old_packages

echo "[PACKAGE] created: $TIMESTAMPED_PATH"
echo "[PACKAGE] latest:  $LATEST_PATH"
echo "[PACKAGE] retention days: $KEEP_DAYS"
du -sh "$TIMESTAMPED_PATH"

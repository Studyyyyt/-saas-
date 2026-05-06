#!/usr/bin/env bash
set -euo pipefail

ROOT="/root"
OUTPUT_DIR="/root/shared-downloads"
KEEP_DAYS="${DOWNLOAD_KEEP_DAYS:-7}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
PACKAGE_NAME="project-source-$TIMESTAMP.zip"
LATEST_NAME="project-source-latest.zip"
STAGING_DIR="$(mktemp -d /tmp/project-source-package-XXXXXX)"
PACKAGE_ROOT="$STAGING_DIR/shuao-clinic-saas-source"
TIMESTAMPED_PATH="$OUTPUT_DIR/$PACKAGE_NAME"
LATEST_PATH="$OUTPUT_DIR/$LATEST_NAME"

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

mkdir -p "$PACKAGE_ROOT" "$OUTPUT_DIR"

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
      -o -path './src/main/resources/static.bak.*' \
      -o -path './src/main/resources/static.bak-*' \
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

copy_tree "saas-springboot-src"
copy_tree "saas-vue-src"

if [ -f "$ROOT/saas-system-spec.md" ]; then
  cp -a "$ROOT/saas-system-spec.md" "$PACKAGE_ROOT/"
fi

cat > "$PACKAGE_ROOT/PACKAGE_INFO.txt" <<EOF
项目源码包

生成时间: $(date '+%Y-%m-%d %H:%M:%S %Z')
生成路径: $TIMESTAMPED_PATH

包含目录:
- /root/saas-springboot-src
- /root/saas-vue-src

已排除:
- node_modules
- dist
- target
- 各类静态资源备份目录
- *.log
- .git / .idea
EOF

(
  cd "$STAGING_DIR"
  rm -f "$TIMESTAMPED_PATH" "$LATEST_PATH"
  jar --create --file "$TIMESTAMPED_PATH" shuao-clinic-saas-source >/dev/null
)

cp -f "$TIMESTAMPED_PATH" "$LATEST_PATH"
prune_old_packages

echo "[PACKAGE] created: $TIMESTAMPED_PATH"
echo "[PACKAGE] latest:  $LATEST_PATH"
echo "[PACKAGE] retention days: $KEEP_DAYS"
du -sh "$TIMESTAMPED_PATH"

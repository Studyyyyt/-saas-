#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="/root/saas-springboot-src"
FRONTEND_ROOT="/root/saas-vue-src"
STATIC_DIR="$APP_ROOT/src/main/resources/static"
RUNTIME_JAR="/root/newsystem-backend-cloud.jar"
DISK_GUARD_SCRIPT="$APP_ROOT/scripts/disk_guard.sh"
LOG_FILE="/root/newsystem-backend-cloud.log"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_STATIC_DIR="$APP_ROOT/src/main/resources/static.bak.$TIMESTAMP"
BACKUP_JAR="$RUNTIME_JAR.bak.$TIMESTAMP"
BUILD_LOG="$APP_ROOT/.deploy-build-$TIMESTAMP.log"
SKIP_TESTS=false
SKIP_FRONTEND=false
SKIP_RESTART=false
RUN_REGRESSION=false
KEEP_STATIC_BACKUPS=10
KEEP_RUNTIME_JAR_BACKUPS=10

prune_backups() {
  local target_type="$1"
  local base_dir="$2"
  local name_pattern="$3"
  local keep_count="$4"
  local -a backups=()

  mapfile -t backups < <(find "$base_dir" -maxdepth 1 -mindepth 1 -type "$target_type" -name "$name_pattern" | sort)

  if [ "${#backups[@]}" -le "$keep_count" ]; then
    return 0
  fi

  local delete_count=$(( ${#backups[@]} - keep_count ))
  local backup_path
  for backup_path in "${backups[@]:0:delete_count}"; do
    rm -rf "$backup_path"
  done
}

for arg in "$@"; do
  case "$arg" in
    --skip-tests)
      SKIP_TESTS=true
      ;;
    --skip-frontend)
      SKIP_FRONTEND=true
      ;;
    --skip-restart)
      SKIP_RESTART=true
      ;;
    --with-regression)
      RUN_REGRESSION=true
      ;;
    *)
      echo "Unknown option: $arg" >&2
      echo "Usage: $0 [--skip-tests] [--skip-frontend] [--skip-restart] [--with-regression]" >&2
      exit 1
      ;;
  esac
done

echo "[DEPLOY] started at $TIMESTAMP"
echo "[DEPLOY] app root: $APP_ROOT"
echo "[DEPLOY] frontend root: $FRONTEND_ROOT"
[[ -x "$DISK_GUARD_SCRIPT" ]] || {
  echo "[DEPLOY] disk guard script missing: $DISK_GUARD_SCRIPT" >&2
  exit 1
}

echo "[DEPLOY] checking disk space"
"$DISK_GUARD_SCRIPT" --mode deploy

if [ "$SKIP_FRONTEND" = false ]; then
  echo "[DEPLOY] building frontend"
  (
    cd "$FRONTEND_ROOT"
    node ./node_modules/@vue/cli-service/bin/vue-cli-service.js build
  )

  echo "[DEPLOY] backing up static assets to $BACKUP_STATIC_DIR"
  mkdir -p "$BACKUP_STATIC_DIR"
  cp -a "$STATIC_DIR"/. "$BACKUP_STATIC_DIR"/
  prune_backups d "$(dirname "$STATIC_DIR")" "$(basename "$STATIC_DIR").bak*" "$KEEP_STATIC_BACKUPS"

  echo "[DEPLOY] publishing frontend dist to spring static"
  find "$STATIC_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
  find "$FRONTEND_ROOT/dist" -mindepth 1 -maxdepth 1 ! -name '._*' -exec cp -a {} "$STATIC_DIR"/ \;
fi

echo "[DEPLOY] packaging backend"
(
  cd "$APP_ROOT"
  if [ "$SKIP_TESTS" = true ]; then
    mvn -Dmaven.test.skip=true package >"$BUILD_LOG" 2>&1
  else
    mvn package >"$BUILD_LOG" 2>&1
  fi
)
echo "[DEPLOY] maven log: $BUILD_LOG"

echo "[DEPLOY] backing up runtime jar to $BACKUP_JAR"
cp -a "$RUNTIME_JAR" "$BACKUP_JAR"
prune_backups f "$(dirname "$RUNTIME_JAR")" "$(basename "$RUNTIME_JAR").bak*" "$KEEP_RUNTIME_JAR_BACKUPS"
cp -f "$APP_ROOT/target/springboot-0.0.1-SNAPSHOT.jar" "$RUNTIME_JAR"

if [ "$SKIP_RESTART" = false ]; then
  echo "[DEPLOY] restarting runtime jar"
  OLD_PID="$(pgrep -f "java -jar $RUNTIME_JAR" | head -n 1 || true)"
  if [ -n "$OLD_PID" ]; then
    kill "$OLD_PID"
    for _ in $(seq 1 30); do
      if ps -p "$OLD_PID" >/dev/null 2>&1; then
        sleep 1
      else
        break
      fi
    done
  fi

  nohup /usr/bin/java -jar "$RUNTIME_JAR" >"$LOG_FILE" 2>&1 &
fi

echo "[DEPLOY] running health checks"
for attempt in $(seq 1 20); do
  if "$APP_ROOT/scripts/health_check.sh" >/tmp/deploy-health-check.log 2>&1; then
    cat /tmp/deploy-health-check.log
    echo "[DEPLOY] health check passed on attempt $attempt"
    if [ "$RUN_REGRESSION" = true ]; then
      echo "[DEPLOY] running post-deploy business regression"
      python3 "$APP_ROOT/scripts/regression_patient_billing.py"
      echo "[DEPLOY] post-deploy business regression passed"
    fi
    echo "[DEPLOY] completed"
    exit 0
  fi
  sleep 2
done

cat /tmp/deploy-health-check.log >&2
echo "[DEPLOY] health check failed after retries" >&2
exit 1

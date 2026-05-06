# 舒澳口腔 SaaS 容灾备份机制

## 覆盖范围

当前机制默认一次性备份以下内容：

- MySQL 数据库 `clinic_system_new`
- 患者影像物理目录 `/root/.local/uploads/patient-images`
- 线上运行 jar，优先 `/root/newsystem-backend-cloud.jar`
- 应用配置 `src/main/resources/application.yml`
- 后端源码归档 `saas-springboot-src`
- 前端源码归档 `saas-vue-src`

这样做的目标不是只保数据库，而是把“可运行系统 + 业务数据 + 源码”一起收口，机器损坏后可以直接恢复。

## 文件位置

- 备份脚本：`scripts/backup_disaster_recovery.sh`
- 恢复脚本：`scripts/restore_disaster_recovery.sh`
- 定时任务安装脚本：`scripts/install_backup_cron.sh`
- 环境变量示例：`scripts/disaster_recovery.env.example`
- 默认备份目录：`/root/disaster-recovery-backups`

每个快照目录会包含：

- `database-*.sql.gz`
- `uploads-*.tar.gz`
- `application.yml`
- `*.jar`
- `backend-project.tar.gz`
- `frontend-project.tar.gz`
- `manifest.txt`
- `SHA256SUMS`

同时会维护一个最新快照软链接：

- `/root/disaster-recovery-backups/latest`

## 立即启用

1. 复制示例配置

```bash
cp /root/saas-springboot-src/scripts/disaster_recovery.env.example \
   /root/saas-springboot-src/scripts/disaster_recovery.env
```

2. 按实际环境调整 `DB_PASSWORD`、`RUNTIME_JAR_PATH`、异机同步配置

3. 执行一次手工备份

```bash
bash /root/saas-springboot-src/scripts/backup_disaster_recovery.sh
```

4. 校验最近一次备份

```bash
bash /root/saas-springboot-src/scripts/restore_disaster_recovery.sh --snapshot latest --verify-only
```

## 定时执行

推荐至少每天凌晨做一次全量备份：

```cron
0 2 * * * bash /root/saas-springboot-src/scripts/backup_disaster_recovery.sh >> /var/log/shuao-backup.log 2>&1
```

也可以直接用安装脚本写入当前用户 crontab：

```bash
bash /root/saas-springboot-src/scripts/install_backup_cron.sh
```

如果要改时间：

```bash
bash /root/saas-springboot-src/scripts/install_backup_cron.sh --schedule "0 */6 * * *"
```

如果要满足真正的容灾要求，必须把快照同步到异机或对象存储，不要只留在本机磁盘。

可以直接在 `disaster_recovery.env` 中配置：

```bash
POST_BACKUP_HOOK='rsync -az "$BACKUP_SNAPSHOT_DIR"/ backup@10.0.0.8:/data/shuao-saas/'
```

或：

```bash
POST_BACKUP_HOOK='rclone copy "$BACKUP_SNAPSHOT_DIR" remote:shuao-saas-backup/"$(basename "$BACKUP_SNAPSHOT_DIR")"'
```

## 恢复方式

### 1. 只校验快照

```bash
bash /root/saas-springboot-src/scripts/restore_disaster_recovery.sh --snapshot latest --verify-only
```

### 2. 恢复到新数据库做演练

```bash
bash /root/saas-springboot-src/scripts/restore_disaster_recovery.sh \
  --snapshot latest \
  --restore-db \
  --target-db-name clinic_system_new_restore
```

### 3. 正式灾难恢复

```bash
bash /root/saas-springboot-src/scripts/restore_disaster_recovery.sh \
  --snapshot latest \
  --replace-db \
  --replace-uploads \
  --overwrite-files \
  --yes
```

默认正式恢复会覆盖：

- 数据库
- 影像目录
- application.yml
- 运行 jar

源码恢复默认不自动执行；如果需要回收开发环境，可额外附带：

```bash
  --restore-backend-source --restore-frontend-source --replace-source
```

## 机制说明

- 备份使用 `flock` 防止重复并发执行
- MySQL 采用逻辑导出，适合当前单机 MySQL 场景
- 每个快照都会产出 `SHA256SUMS`，恢复前先做完整性校验
- 默认保留最近 14 天且最多 30 份快照
- 快照保留策略可在 `disaster_recovery.env` 中调整

## 当前已知边界

- 这是“单机部署 + 异机同步钩子”的容灾方案，不是双活
- 数据库仍是单点 MySQL，若要继续增强，可再加 MySQL 主从或定时物理快照
- 当前上传物理文件只有患者影像目录；若后续新增本地落盘目录，要同步加入脚本

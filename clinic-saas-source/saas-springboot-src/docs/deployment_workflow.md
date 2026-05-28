# 前后端一键发布流程

## 脚本

- 发布脚本：`scripts/deploy_backend_frontend.sh`
- 健康检查：`scripts/health_check.sh`

## 默认动作

脚本会按顺序执行：

1. 检查系统盘剩余空间，低于阈值直接拦截
2. 构建前端 `saas-vue-src/dist`
3. 备份当前 `src/main/resources/static`
4. 发布前端静态资源到 Spring Boot 工程
5. 执行 `mvn package`
6. 备份并替换运行中的 `/root/newsystem-backend-cloud.jar`
7. 清理超出保留上限的静态与 jar 备份
8. 重启 Java 进程
9. 做根路径、登录页、进程状态健康检查

## 常用命令

```bash
bash /root/saas-springboot-src/scripts/deploy_backend_frontend.sh
```

跳过测试：

```bash
bash /root/saas-springboot-src/scripts/deploy_backend_frontend.sh --skip-tests
```

发布后追加真实业务回归：

```bash
bash /root/saas-springboot-src/scripts/deploy_backend_frontend.sh --skip-tests --with-regression
```

说明：
该回归会临时创建并清理测试患者、预约、处置与收费数据，用于校验收费/退款/欠费/删除级联等真实业务流程。

只打包不重启：

```bash
bash /root/saas-springboot-src/scripts/deploy_backend_frontend.sh --skip-restart
```

## 产物与备份

- Maven 构建日志：`/root/saas-springboot-src/.deploy-build-时间戳.log`
- 静态资源备份：`src/main/resources/static.bak.时间戳`
- 运行 jar 备份：`/root/newsystem-backend-cloud.jar.bak.时间戳`
- 默认仅保留最近 `10` 个静态备份与 `10` 个运行 jar 备份

## 磁盘保护

- 发布前磁盘检查：`scripts/disk_guard.sh --mode deploy`
- 巡检安装脚本：`scripts/install_disk_guard_cron.sh`
- 默认环境文件：`scripts/disk_guard.env`

默认阈值：

- 发布拦截：`/` 至少保留 `5GB` 且至少 `15%` 空闲
- 巡检告警：磁盘使用率达到 `80%` 警告，达到 `90%` 严重告警

手动执行巡检：

```bash
bash /root/saas-springboot-src/scripts/disk_guard.sh --mode monitor
```

安装定时巡检：

```bash
bash /root/saas-springboot-src/scripts/install_disk_guard_cron.sh
```

# 患者360-1 数据与实体落地进度

## 新增文件清单

1. `sql/patient360_init.sql`
2. `src/main/java/com/example/springboot/entity/PatientFollowup.java`
3. `src/main/java/com/example/springboot/entity/PatientRiskTag.java`
4. `src/main/java/com/example/springboot/entity/PatientTimeline.java`
5. `src/main/java/com/example/springboot/mapper/PatientFollowupMapper.java`
6. `src/main/java/com/example/springboot/mapper/PatientRiskTagMapper.java`
7. `src/main/java/com/example/springboot/mapper/PatientTimelineMapper.java`

## 执行SQL命令

```bash
cd /Users/lifan/Downloads/newsystem-springboot
mysql -u <用户名> -p<密码> <数据库名> < sql/patient360_init.sql
```

> 若不希望明文密码，可使用：

```bash
cd /Users/lifan/Downloads/newsystem-springboot
mysql -u <用户名> -p <数据库名> < sql/patient360_init.sql
```

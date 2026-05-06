# 后端 Dockerfile - 基于 Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# 先复制 pom.xml 并下载依赖，利用 Docker 缓存层
COPY saas-springboot-src/pom.xml .
RUN apt-get update && apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/* \
    && mvn dependency:go-offline -B

# 复制源码并打包
COPY saas-springboot-src/src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段 - 使用 JRE 精简镜像
FROM eclipse-temurin:17-jre

WORKDIR /app

# 安装必要的工具（健康检查用）
RUN apt-get update && apt-get install -y curl wget \
    && rm -rf /var/lib/apt/lists/*

# 创建上传目录（与 PatientImageController 中的 UPLOAD_DIR 对应）
RUN mkdir -p /root/.local/uploads/patient-images

# 从构建阶段复制 JAR
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/patients/selectAll?page=1\&size=1 || exit 1

# JVM 参数优化（可通过环境变量覆盖）
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT exec java ${JAVA_OPTS} -jar app.jar

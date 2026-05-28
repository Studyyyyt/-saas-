# 前端 Dockerfile - 多阶段构建
# 阶段一：构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制 package 文件并安装依赖
COPY saas-vue-src/package*.json ./
RUN npm install

# 复制源码并构建
COPY saas-vue-src/ .
RUN npm run build

# 阶段二：运行阶段（Nginx）
FROM nginx:alpine

# 复制构建产物到 Nginx 默认目录
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 Nginx 配置模板（支持环境变量替换）
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf

# 复制启动脚本，用于注入环境变量
COPY docker/frontend/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

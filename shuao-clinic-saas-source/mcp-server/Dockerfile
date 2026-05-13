# 口腔门诊 SaaS 系统 MCP Server 镜像
# 基于 Node.js 18 Alpine

FROM node:18-alpine

# 设置工作目录
WORKDIR /app

# 先复制 package.json 和 package-lock.json（如果存在），利用 Docker 缓存层
COPY package*.json ./

# 安装依赖
RUN npm install

# 复制源码
COPY . .

# 编译 TypeScript
RUN npm run build

# 暴露服务端口
EXPOSE 3001

# 启动命令
CMD ["npm", "start"]

# 基于 Python 3.10 轻量镜像
FROM python:3.10-slim

# 设置工作目录
WORKDIR /app

# 先复制依赖文件并安装（利用 Docker 缓存层，减少重复构建时间）
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 复制项目全部代码
COPY . .

# 暴露服务端口
EXPOSE 8000

# 启动 MCP Data Server
CMD ["python", "main.py"]

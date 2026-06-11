# 开发区 - 写代码和调试

## 这个区是干嘛的

这里是**你写代码的地方**。所有源代码、开发配置都在这里。

## 本区内部结构

```
开发区-写代码和调试/
│
├── README.md                      ← 本文件
├── clinic-saas-source/            ← 【源码在这里】
│   ├── saas-vue-src/              ← 前端 Vue 源码
│   ├── saas-springboot-src/       ← 后端 Spring Boot 源码
│   └── docker/                    ← Docker 配置
│
├── docs/                          ← 【文档在这里】
│   ├── 02-开发文档/               ← 开发指南、数据库说明
│   ├── 03-接口文档/               ← API 文档
│   ├── 05-维护指南/               ← 部署说明
│   └── ui-design-preview/         ← UI 设计稿、落地页
│
├── n8n/                           ← 【n8n 部署包】（可选，AI 功能依赖）
│   └── n8n部署包/                 ← n8n Docker 数据
│
└── n8n-json/                      ← 【n8n 工作流配置】（可选）
    ├── 病历扩写agent.json
    └── WF-01 每日营业简报.json
```

## 各文件夹说明

| 文件夹 | 类型 | 说明 |
|--------|------|------|
| `clinic-saas-source/` | 源码 | 前端 + 后端源代码，开发时在这里改代码 |
| `docs/` | 文档 | 所有文档统一放这里，按类别分子文件夹 |
| `n8n/` | 运行数据 | n8n 工作流平台的数据文件，部署时用到（可选） |
| `n8n-json/` | 配置 | n8n 工作流的导出文件，可导入到 n8n 中使用（可选） |

## 日常开发步骤

### 1. 修改前端代码

```bash
# 进入前端源码目录
cd clinic-saas-source/saas-vue-src

# 安装依赖（第一次才需要）
npm install

# 修改 src/ 目录下的 .vue 文件

# 构建（测试打包是否正常）
npm run build
```

### 2. 修改后端代码

```bash
# 进入后端源码目录
cd clinic-saas-source/saas-springboot-src

# 修改 src/ 目录下的 .java 文件

# 编译打包（测试是否正常）
mvn clean package -DskipTests
```

### 3. 本地启动开发环境

```bash
# 进入 Docker 配置目录
cd clinic-saas-source/docker

# 启动开发环境（MySQL + 后端 + 前端）
docker compose -f docker-compose.dev.yml up -d

# 查看日志
docker compose -f docker-compose.dev.yml logs -f backend
```

### 4. 开发环境访问地址

- 前端页面：`http://localhost:7070`
- 后端 API：`http://localhost:8080`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

## ⚠️ 重要提醒

**这个区里的源码内容不能直接发给客户！**

特别是：
- ❌ `saas-vue-src/src/` —— 前端源码
- ❌ `saas-springboot-src/src/` —— 后端源码
- ❌ `pom.xml`、`package.json` —— 构建配置
- ❌ `docker-compose.dev.yml` —— 开发配置（里面挂载了本地源码路径）

## 开发完成后做什么

代码改好、测试通过后，执行构建脚本生成交付包：

```bash
cd clinic-saas-source/docker
./build-and-export.sh
```

交付包自动生成在 `docker/Gjimages/` 目录下，包含 Docker 镜像和部署配置，可直接发给客户。

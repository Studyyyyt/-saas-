# AI架构变更说明

## 概述

本文档说明口腔门诊SaaS管理系统AI架构从「内部AI调用」到「外部工作流代理」的变更。

## 旧架构特点

- **系统内部直接调用模型API**：直接调用OpenAI/DeepSeek等模型API。
- **Function Calling框架**：使用Function Calling框架执行数据库查询。
- **BusinessAnalysisChatService**：内部处理SSE流式生成、工具执行、记忆管理。
- **MedicalRecordAIService**：内部处理Prompt渲染、模型调用、安全校验。
- **AiToolService**：直接查询数据库Mapper。

## 新架构特点

- **系统退化为纯展示层和数据层**：前端负责展示，后端负责数据存取。
- **AI逻辑全部外包**：Prompt工程、模型选择、意图识别、数据获取、内容生成、安全校验等全部外包到外部工作流平台（如n8n、阿里百炼、Dify等）。
- **后端仅保留统一代理层**：`AiProxyService`负责转发HTTP请求到外部端点。
- **前端字段过滤**：前端根据字段启用配置过滤工作流返回的字段。
- **两种数据获取方案**：
  - **HTTP API文档**：外部工作流通过标准HTTP API获取数据。
  - **MCP服务**：外部工作流通过MCP（Model Context Protocol）服务获取数据。

## 关键变更对照表

| 模块 | 旧架构 | 新架构 |
|------|--------|--------|
| BusinessAnalysisChatService | 内部模型调用 + Function Calling | 转发到外部端点 |
| MedicalRecordAIService | 内部Prompt渲染 + 模型调用 | 纯代理转发 |
| AiToolService | 直接查询数据库 | 改造为MCP Schema生成 |
| AiModelProviderService | 管理模型供应商配置 | 已废弃 |
| `ai_agent_config`表 | 存储`system_prompt`和`enabled_tools` | 存储`endpoint_url`、`auth_type`等端点配置 |
| 模型供应商配置页面 | 配置API Key和模型参数 | 已废弃，提示使用首页助手 |
| Agent链接页面 | 独立配置外部Agent | 已合并到首页助手 |

## 变更影响

- **开发侧**：不再维护Prompt模板和模型调用逻辑，改为维护端点配置和代理转发逻辑。
- **运维侧**：AI能力由外部工作流平台提供，需关注外部端点的可用性和性能。
- **扩展侧**：新增AI功能时，只需在外部工作流平台配置新流程，系统侧仅需增加端点配置。

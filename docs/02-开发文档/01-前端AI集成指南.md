# 前端 AI 集成指南

## 概述

前端 AI 功能已通过统一代理层接入外部工作流平台。系统本身不再维护 AI 逻辑，仅作为展示层和数据层，所有 AI 交互通过后端代理转发到外部端点。

## 统一代理接口

### 通用聊天接口

```javascript
import aiStreamClient from '@/utils/aiStreamClient'

// 流式聊天调用
aiStreamClient.streamChat({
  agentKey: 'home-assistant',   // Agent 标识
  message: '用户输入内容',
  sessionId: 'xxx',
  // ... 其他参数
})
```

后端代理路径：`POST /api/ai/proxy/{agentKey}`

### 病历扩写接口

```javascript
// 病历 AI 扩写调用
aiStreamClient.streamChat({
  agentKey: 'medical-expand',
  message: currentRecordContent,
  // ...
})
```

后端代理路径：`POST /api/ai/proxy/medical-expand`

## 支持的模板变量

前端在调用代理接口时，可在请求体或模板配置中使用以下变量：

| 变量名 | 说明 |
|--------|------|
| `{{user_message}}` | 用户当前输入的消息内容 |
| `{{account_id}}` | 当前登录账号 ID |
| `{{account_name}}` | 当前登录账号名称 |
| `{{session_id}}` | 当前会话 ID |
| `{{history}}` | 历史对话记录（JSON 格式） |
| `{{fields}}` | 动态字段数据对象 |
| `{{scene_id}}` | 当前场景 ID |
| `{{scene_name}}` | 当前场景名称 |
| `{{operations}}` | 可用操作列表 |
| `{{enabled_fields}}` | 后端配置启用的字段列表 |

## 字段过滤机制

外部工作流返回的数据可能包含多个字段。前端根据后端配置的 `enabled_fields` 进行过滤，只渲染被启用的字段，避免展示未授权或废弃的内容。

过滤逻辑示例：

```javascript
const enabledFields = response.enabled_fields || []
const filteredData = Object.keys(response.data)
  .filter(key => enabledFields.includes(key))
  .reduce((obj, key) => {
    obj[key] = response.data[key]
    return obj
  }, {})
```

## 接入新 Agent 步骤

1. 在后端 `AIAgentConfig` 中新增 Agent 配置，设置 `agentKey` 和外部端点 URL
2. 前端在需要调用的页面引入 `aiStreamClient`
3. 调用 `streamChat` 时传入对应的 `agentKey`
4. 根据返回的 `enabled_fields` 渲染界面

## 注意事项

- 所有 AI 请求均为流式返回（SSE），需使用 `aiStreamClient` 处理流式数据
- 若外部端点未配置或返回异常，前端应展示友好的错误提示
- `enabled_fields` 由后端根据当前用户角色和配置动态生成，前端不可写死

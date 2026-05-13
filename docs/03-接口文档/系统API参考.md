# 系统 API 参考

## 数据获取方式

系统提供两种数据获取方式：HTTP API 和 MCP 服务。

## HTTP API

后端通过 `ApiDocumentationController` 提供统一的 API 文档接口：

```
GET /api/docs
```

返回 JSON 格式的 API 文档，包含所有可用接口的路径、方法、请求参数和响应结构。

### 使用示例

```bash
curl http://localhost:8080/api/docs
```

## MCP 服务

`mcp-server` 为独立项目，运行在端口 `3001`，提供 9 个 Tools 供外部调用。

### MCP Tool 列表

| Tool 名称 | 对应 API 映射 | 说明 |
|-----------|--------------|------|
| `list_patients` | `GET /api/patient` | 获取患者列表 |
| `get_patient` | `GET /api/patient/{id}` | 获取单个患者详情 |
| `list_appointments` | `GET /api/appointment` | 获取预约列表 |
| `get_appointment` | `GET /api/appointment/{id}` | 获取单个预约详情 |
| `list_medical_records` | `GET /api/medicalRecord` | 获取病历列表 |
| `get_medical_record` | `GET /api/medicalRecord/{id}` | 获取单个病历详情 |
| `create_appointment` | `POST /api/appointment` | 创建新预约 |
| `update_patient` | `PUT /api/patient/{id}` | 更新患者信息 |
| `delete_appointment` | `DELETE /api/appointment/{id}` | 删除预约 |

### MCP 服务启动

```bash
cd mcp-server
docker run -p 3001:3001 mcp-server
```

### 测试 MCP 服务

使用 MCP Inspector 进行测试：

```bash
npx @anthropics/mcp-inspector node mcp-server/dist/index.js
```

## 注意事项

- HTTP API 和 MCP 服务共享同一套业务逻辑，数据一致性由后端保证
- MCP 服务目前为只读 + 少量写操作，复杂业务仍建议直接调用 HTTP API
- 生产环境部署时，建议为 MCP 服务配置独立的认证机制

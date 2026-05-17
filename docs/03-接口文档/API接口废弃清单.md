# API 接口废弃清单

> 本文档列出系统中已废弃或已确认前端无引用的接口，供维护参考。

---

## 一、已废弃接口（后端标记 @Deprecated）

| 序号 | 方法 | 路径 | 废弃原因 | 后端状态 | 前端引用 |
|------|------|------|---------|---------|---------|
| 1 | POST | `/api/ai/medical-record/expand` | 已迁移到统一 AI 代理接口 `/api/ai/proxy/{agentKey}` | `@Deprecated` + `@ResponseStatus(HttpStatus.GONE)`，调用返回 410 | **无** |
| 2 | POST | `/business-analysis/chat/stream` | 经营分析聊天流式接口已废弃 | `deprecated=true` | 前端使用 `/api/ai/proxy/{agentKey}` 替代 |

---

## 二、已注释/未使用的前端调用

| 序号 | 方法 | 路径 | 说明 | 处置建议 |
|------|------|------|------|---------|
| 1 | PUT | `/accounts/update/${id}` | `AccountView.vue:338` 中已注释，实际使用 `PUT /accounts/edit` | 可清理前端注释代码 |
| 2 | PUT | `/treatments/edit` | `TreatmentView2.vue:236` 中已注释，项目主要使用 `POST /treatments/batchAdd` 和 `POST /treatments/charge/{id}` | 可清理前端注释代码 |

---

## 三、前端无独立路由但组件仍存在的页面

以下页面组件仍存在且内部调用了后端接口，但无独立路由配置（可能被其他页面内嵌引用）：

| 页面组件 | 调用的后端接口 | 状态 |
|---------|--------------|------|
| `MedicalRecordAIConfigView.vue` | `GET /api/ai-config/medical-record`、`PUT /api/ai-config/medical-record`、`POST /api/ai-config/medical-record/preview` | **组件存在，接口活跃** |
| `ModelProviderConfigView.vue` | `GET /api/model-providers`、`POST /api/model-providers` 等 | **组件存在，接口活跃** |
| `SystemTreatmentOperationView.vue` | `/treatment-operations` 相关接口 | **组件存在，接口活跃** |

> **注意**：这些页面不是「废弃」，只是没有独立路由入口。如需用户直接访问，需在 `router/index.js` 中补配路由。

---

## 四、H5/门户页面已移除但后端转发未清理

根据 `git log` 提交 `ee00652`（移除微信/H5门户集成代码），以下前端页面已移除，但 `SpaForwardController` 中仍保留了转发路径：

| 路径前缀 | 说明 |
|---------|------|
| `/patient-register-h5` | 患者注册H5 |
| `/patient-portal-home` | 患者门户首页 |
| `/patient-portal-section` | 患者门户栏目 |
| `/staff-portal-home` | 员工门户首页 |
| `/staff-h5/...` | 员工H5工作台各页面 |
| `/app/bind-success` | 绑定成功页 |
| `/appointment-notice` | 预约通知页 |

**建议**：清理 `SpaForwardController` 中已不存在的 H5/门户路径转发，避免 404 或白屏。

---

## 五、整改结论

| 检查项 | 结论 |
|--------|------|
| `POST /api/ai/medical-record/expand` | 已正确标记废弃，文档和 Apifox 集合中保留标记即可 |
| `GET /api/ai-config/medical-record` / `PUT /api/ai-config/medical-record` | **不应删除**，前端 `MedicalRecordAIConfigView.vue` 仍在调用 |
| 病历扩写参数完整性 | 文档已覆盖全部 28 个字段，字段映射关系已明确标注 |
| 接口遗漏 | 仅发现 `DELETE /api/model-providers/{id}` 未在文档中列出，已补充 |
| 前端路由与后端接口对齐 | H5 页面已移除但转发未清理；3 个管理页面组件存在但无独立路由 |

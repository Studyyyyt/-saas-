# API 接口文档整改计划

## 一、背景与目标

当前接口文档（`API接口清单.md`、`病历扩写API详解.md`、`Apifox导入集合.json`）存在与系统实际代码不匹配、参数缺失、已废弃功能未清理等问题。需全面排查后端 Controller、前端调用代码、数据库实体，产出一份与系统完全对齐的准确接口文档。

**核心目标**：
1. 文档中的每个接口必须在后端 Controller 中真实存在且可访问
2. 删除系统已废弃或前端已无入口的功能接口
3. 每个接口的请求参数（路径参数、Query 参数、请求体字段）必须完整、类型正确
4. 病历扩写相关接口参数必须与前端实际发送和后端实际接收完全一致
5. 重新生成准确的 Apifox/Postman 导入集合

---

## 二、已知问题清单（待验证）

| 序号 | 问题描述 | 涉及文档 | 严重程度 |
|------|---------|---------|---------|
| 1 | **病历AI配置接口已废弃**：系统前端已无「病历AI配置」页面，`MedicalRecordAIController` 中的 `GET /api/ai-config/medical-record` 和 `PUT /api/ai-config/medical-record` 可能已无人调用，但文档和 Apifox 集合中仍保留 | API清单、扩写详解、Apifox集合 | 高 |
| 2 | **病历扩写参数不完整**：`POST /api/ai/proxy/medical-expand` 的请求参数字段可能未完全覆盖前端实际发送的所有字段 | 扩写API详解 | 高 |
| 3 | **旧废弃接口未标记**：`POST /api/ai/medical-record/expand` 已废弃，需确认文档标记是否准确 | API清单 | 中 |
| 4 | **接口遗漏**：可能存在部分 Controller 接口未在文档中列出 | API清单 | 高 |
| 5 | **参数类型/必填性不准确**：文档中部分接口的参数类型、必填性可能与后端实际不一致 | 全部 | 中 |
| 6 | **前端路由与后端接口对不上**：部分页面可能已被删除或合并，对应的后端接口是否仍被调用需确认 | 全部 | 高 |

---

## 三、任务分解（子 Agent 并行处理）

### Agent A：后端 Controller 全面扫描
**任务**：
1. 读取 `saas-springboot-src/src/main/java/com/example/springboot/controller/` 下所有 Controller 文件
2. 提取每个 Controller 的完整接口清单：方法（GET/POST/PUT/DELETE）、路径、方法签名、参数注解（`@PathVariable`、`@RequestParam`、`@RequestBody`）
3. 标记已废弃接口（`@Deprecated`、`@ResponseStatus(HttpStatus.GONE)`）
4. 输出：一份「后端实际存在的接口清单（含参数）」

### Agent B：前端调用代码全面扫描
**任务**：
1. 读取 `saas-vue-src/src/` 下所有 `.vue` 和 `.js` 文件
2. 搜索所有 `axios.get`、`axios.post`、`axios.put`、`axios.delete` 调用
3. 提取：前端实际调用的接口路径、发送的参数字段、发送的请求头
4. 标记「前端已调用」vs「前端未调用（可能已废弃）」
5. 输出：一份「前端实际调用的接口清单（含参数）」

### Agent C：病历扩写专项审查
**任务**：
1. 重点审查 `MedicalRecordAIController.java` 和 `AiProxyController.java`
2. 审查前端 `MedicalRecordView.vue` 中 `expandWithAI` 方法的完整实现
3. 审查 `MedicalRecordAIConfigDTO.java`、`TreatmentSceneExpandRequest.java`、`MedicalRecordAIField.java`
4. 确认前端实际发送的 `POST /api/ai/proxy/medical-expand` 请求体中 **所有字段**，包括隐藏字段和拼接字段
5. 确认 `GET /api/ai-config/medical-record` 和 `PUT /api/ai-config/medical-record` 是否仍被前端调用（搜索 `ai-config/medical-record` 在前端的引用）
6. 输出：一份「病历扩写接口参数对照表」（前端字段名 → 后端接收字段名 → 类型 → 必填 → 说明）

### Agent D：数据库实体与 Mapper 扫描
**任务**：
1. 扫描 `entity/` 和 `mapper/` 目录
2. 确认文档中提到的 DTO/Entity 类是否还存在（如 `MedicalRecordAIField`、`TreatmentSceneExpandRequest`）
3. 检查是否有新增的实体类未在文档中体现
4. 输出：一份「实体/DTO 存在性清单」

### Agent E：路由与页面存在性审查
**任务**：
1. 读取 `src/router/index.js`，确认前端路由列表
2. 读取 `src/views/Manager/` 下所有页面文件
3. 确认哪些页面存在、哪些页面已删除
4. 对照后端接口，标记「有页面但无接口」或「有接口但无页面」的情况
5. 输出：一份「路由-页面-接口三方对照表」

---

## 四、验证标准

| 检查项 | 通过标准 |
|--------|---------|
| 接口存在性 | 文档中的每个接口都能在后端 Controller 中找到对应的方法 |
| 参数完整性 | 文档中列出的参数 >= 后端方法签名中定义的参数 |
| 字段映射一致性 | 前端发送的字段名、后端接收的字段名、文档描述的字段名三者一致 |
| 废弃接口清理 | 已废弃且前端无调用的接口在文档中标记为「已废弃（前端无引用）」或删除 |
| Apifox 集合准确性 | 导入集合中的接口与最终修正后的文档完全一致 |

---

## 五、执行步骤

```
Step 1: 5 个子 Agent 并行执行（A/B/C/D/E）
Step 2: 汇总各 Agent 输出，交叉比对
Step 3: 根据比对结果修正文档
Step 4: 重新生成 Apifox 导入集合
Step 5: 用 MCP 浏览器访问 /api/docs 及核心接口，验证可访问性
Step 6: 输出最终修正版文档
```

---

## 六、预期产出

1. `API接口清单_修正版.md` — 完整、准确的接口总览
2. `病历扩写API详解_修正版.md` — 参数完整、字段映射清晰的专项文档
3. `Apifox导入集合_修正版.json` — 可直接导入、无废弃接口的集合文件
4. `API接口废弃清单.md` — 列出已废弃/已删除的接口及原因

---

> **备注**：本整改计划由主 Agent 编写，供各子 Agent 按任务分解执行。用户确认后可在新 Claude Code 窗口中并行启动子 Agent 任务。

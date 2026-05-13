# 一、登录认证与患者管理

## loginController

### 用户登录

**请求方式**：POST
**请求路径**：`/loginController/login`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| username | Body | String | 是 | 账号名称 |
| password | Body | String | 是 | 密码 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/loginController/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "role": "admin",
    "roleLabel": "管理员",
    "allowedMenuKeys": ["home", "patient", "doctor"],
    "roleMenuPermissionsLoaded": true
  }
}
```

---

## AccountController

### 分页查询账号列表

**请求方式**：GET
**请求路径**：`/accounts/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/accounts/search?page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 查询在职医生账号列表

**请求方式**：GET
**请求路径**：`/accounts/doctors/active`

**请求参数**：

无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/accounts/doctors/active"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "username": "doctor1",
      "name": "张医生",
      "role": "doctor"
    }
  ]
}
```

---

### 按ID查询账号

**请求方式**：GET
**请求路径**：`/accounts/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 账号ID |
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/accounts/selectByid?id=1&page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 按名称查询账号

**请求方式**：GET
**请求路径**：`/accounts/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 账号名称（支持模糊查询） |
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/accounts/selectByname?name=admin&page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 新增账号

**请求方式**：POST
**请求路径**：`/accounts/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| account | Body | Object | 是 | 账号实体对象（含 username, password, name, role 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/accounts/add" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "123456",
    "name": "新用户",
    "role": "doctor"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

---

### 编辑账号

**请求方式**：PUT
**请求路径**：`/accounts/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| account | Body | Object | 是 | 账号实体对象（含 id, username, name, role 等字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/accounts/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "role": "admin"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

---

### 删除账号

**请求方式**：DELETE
**请求路径**：`/accounts/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Integer | 是 | 账号ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/accounts/delete/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

### 批量删除账号

**请求方式**：DELETE
**请求路径**：`/accounts/deleteBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| ids | Body | Array[Long] | 是 | 账号ID列表 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/accounts/deleteBatch" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量删除成功"
}
```

---

## RoleMenuPermissionController

### 查询权限概览

**请求方式**：GET
**请求路径**：`/role-menu-permissions/overview`

**请求参数**：

无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/role-menu-permissions/overview"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "admin": [...],
    "doctor": [...],
    "nurse": [...]
  }
}
```

---

### 按角色查询权限

**请求方式**：GET
**请求路径**：`/role-menu-permissions/byRole`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| role | Query | String | 是 | 角色编码（admin/doctor/nurse） |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/role-menu-permissions/byRole?role=admin"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": ["home", "patient", "doctor", "appointment"]
}
```

---

### 保存角色菜单权限

**请求方式**：POST
**请求路径**：`/role-menu-permissions/save`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| request | Body | Object | 是 | 保存请求对象（含 role, menuKeys 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/role-menu-permissions/save" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "doctor",
    "menuKeys": ["home", "patient", "medicalRecord"]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "保存成功"
}
```

---

## DoctorController

### 分页查询医生列表

**请求方式**：GET
**请求路径**：`/doctors/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/doctors/selectAll?page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 查询所有医生排班条目

**请求方式**：GET
**请求路径**：`/doctors/scheduleEntries`

**请求参数**：

无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/doctors/scheduleEntries"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张医生",
      "schedule": "周一 上午"
    }
  ]
}
```

---

### 按ID查询医生

**请求方式**：GET
**请求路径**：`/doctors/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 医生ID |
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/doctors/selectByid?id=1&page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 按名称查询医生

**请求方式**：GET
**请求路径**：`/doctors/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 医生姓名（支持模糊查询） |
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |
| status | Query | String | 否 | 状态筛选 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/doctors/selectByname?name=张&page=1&size=20&status=在职"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 更新医生状态

**请求方式**：PUT
**请求路径**：`/doctors/updateStatus/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 医生ID |
| doctor | Body | Object | 是 | 医生对象（含 status 字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/doctors/updateStatus/1" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "在职"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "状态更新成功"
}
```

---

### 新增医生

**请求方式**：POST
**请求路径**：`/doctors/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| doctor | Body | Object | 是 | 医生实体对象（含 name, title, department, phone 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/doctors/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "李医生",
    "title": "主任医师",
    "department": "口腔科",
    "phone": "13800138000"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

---

### 编辑医生

**请求方式**：PUT
**请求路径**：`/doctors/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| doctor | Body | Object | 是 | 医生实体对象（含 id, name, title, department, phone 等字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/doctors/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "李医生",
    "title": "副主任医师",
    "department": "口腔科",
    "phone": "13800138000"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

---

### 删除医生

**请求方式**：DELETE
**请求路径**：`/doctors/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 医生ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/doctors/delete/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

### 批量删除医生

**请求方式**：DELETE
**请求路径**：`/doctors/deleteBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| ids | Body | Array[Long] | 是 | 医生ID列表 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/doctors/deleteBatch" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量删除成功"
}
```

---

### 按日期范围查询排班

**请求方式**：GET
**请求路径**：`/doctors/schedules`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startDate | Query | String | 是 | 开始日期（格式：yyyy-MM-dd） |
| endDate | Query | String | 是 | 结束日期（格式：yyyy-MM-dd） |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/doctors/schedules?startDate=2024-01-01&endDate=2024-01-31"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张医生",
      "scheduleDate": "2024-01-01",
      "shift": "上午"
    }
  ]
}
```

---

### 批量保存排班

**请求方式**：POST
**请求路径**：`/doctors/batchSave`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| schedules | Body | Array[Object] | 是 | 排班对象列表 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/doctors/batchSave" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "id": 1,
      "name": "张医生",
      "scheduleDate": "2024-01-01",
      "shift": "上午"
    }
  ]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量保存成功"
}
```

---

## DoctorHomeReminderDismissalController

### 查询医生已关闭的提醒

**请求方式**：GET
**请求路径**：`/doctor-home-reminders/dismissed`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| doctorAccountId | Query | Long | 是 | 医生账号ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/doctor-home-reminders/dismissed?doctorAccountId=1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": ["reminder1", "reminder2"]
}
```

---

### 关闭提醒

**请求方式**：POST
**请求路径**：`/doctor-home-reminders/dismiss`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 提醒关闭记录对象（含 doctorAccountId, reminderKey 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/doctor-home-reminders/dismiss" \
  -H "Content-Type: application/json" \
  -d '{
    "doctorAccountId": 1,
    "reminderKey": "reminder1"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": null
}
```

---

### 清除已关闭的提醒

**请求方式**：DELETE
**请求路径**：`/doctor-home-reminders/dismissed`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| doctorAccountId | Query | Long | 是 | 医生账号ID |
| reminderKey | Query | String | 是 | 提醒标识键 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/doctor-home-reminders/dismissed?doctorAccountId=1&reminderKey=reminder1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": null
}
```

---

## PatientController

### 分页查询患者列表

**请求方式**：GET
**请求路径**：`/patients/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/selectAll?page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 查询所有患者（H5用）

**请求方式**：GET
**请求路径**：`/patients/selectAllForH5`

**请求参数**：

无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/selectAllForH5"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "phone": "13800138000",
      "gender": "男"
    }
  ]
}
```

---

### 按ID查询患者

**请求方式**：GET
**请求路径**：`/patients/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 患者ID |
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/selectByid?id=1&page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 按名称搜索患者

**请求方式**：GET
**请求路径**：`/patients/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 患者姓名（支持模糊查询） |
| page | Query | Integer | 是 | 页码 |
| size | Query | Integer | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/selectByname?name=张&page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 关键词搜索患者

**请求方式**：GET
**请求路径**：`/patients/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词（姓名/电话等） |
| page | Query | Integer | 否 | 页码，默认1 |
| size | Query | Integer | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/search?keyword=13800138000&page=1&size=20"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 1,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 患者工作台查询

**请求方式**：GET
**请求路径**：`/patients/workbench`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| query | Query | Object | 否 | 工作台查询条件对象 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/workbench?name=张&phone=13800138000"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...]
  }
}
```

---

### 导出患者工作台数据

**请求方式**：GET
**请求路径**：`/patients/workbench/export`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| query | Query | Object | 否 | 工作台查询条件对象 |
| X-Operator-Account-Id | Header | Long | 否 | 操作者账号ID |
| X-Secondary-Password | Header | String | 否 | 二级密码 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patients/workbench/export?name=张" \
  -H "X-Operator-Account-Id: 1" \
  -H "X-Secondary-Password: 246810"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...]
  }
}
```

---

### 新增患者

**请求方式**：POST
**请求路径**：`/patients/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patient | Body | Object | 是 | 患者实体对象（含 name, gender, age, phone, customer_source 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patients/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三",
    "gender": "男",
    "age": 30,
    "phone": "13800138000",
    "customer_source": "自然到店"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "gender": "男",
    "age": 30,
    "phone": "13800138000"
  }
}
```

---

### 编辑患者

**请求方式**：PUT
**请求路径**：`/patients/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patient | Body | Object | 是 | 患者实体对象（含 id, name, gender, age, phone 等字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/patients/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "张三",
    "gender": "男",
    "age": 31,
    "phone": "13800138000"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "gender": "男",
    "age": 31,
    "phone": "13800138000"
  }
}
```

---

### 绑定患者微信

**请求方式**：PUT
**请求路径**：`/patients/bindWechat`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patient | Body | Object | 是 | 患者对象（含 id, wechat_openid 字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/patients/bindWechat" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "wechat_openid": "o1234567890abcdef"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "wechat_openid": "o1234567890abcdef"
  }
}
```

---

### 删除患者

**请求方式**：DELETE
**请求路径**：`/patients/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Integer | 是 | 患者ID |
| X-Operator-Account-Id | Header | Long | 否 | 操作者账号ID |
| X-Secondary-Password | Header | String | 否 | 二级密码 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/patients/delete/1" \
  -H "X-Operator-Account-Id: 1" \
  -H "X-Secondary-Password: 246810"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

### 批量删除患者

**请求方式**：DELETE
**请求路径**：`/patients/deleteBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| ids | Body | Array[Long] | 是 | 患者ID列表 |
| X-Operator-Account-Id | Header | Long | 否 | 操作者账号ID |
| X-Secondary-Password | Header | String | 否 | 二级密码 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/patients/deleteBatch" \
  -H "Content-Type: application/json" \
  -H "X-Operator-Account-Id: 1" \
  -H "X-Secondary-Password: 246810" \
  -d '[1, 2, 3]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量删除成功"
}
```

---

## Patient360Controller

### 获取患者360完整视图

**请求方式**：GET
**请求路径**：`/patient360/overview/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/overview/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "patient": {...},
    "visitCount": 5,
    "lastVisit": "2024-01-15",
    "nextFollowup": "2024-02-01",
    "totalFee": 5000.00,
    "hasArrears": false,
    "arrearsAmount": 0.00,
    "patientInsight": {...},
    "referralRecord": {...},
    "riskTags": [...],
    "records": [...],
    "recentRecords": [...],
    "pendingLabOperationCount": 0,
    "recentFollowups": [...],
    "timeline": [...],
    "appointments": [...],
    "treatments": [...],
    "images": [...],
    "consents": [...],
    "consultations": [...],
    "wechatBound": true,
    "wechatBindStatusLabel": "已绑定微信",
    "wechatBindUrl": "",
    "wechatFollowQrUrl": ""
  }
}
```

---

### 获取患者基础信息

**请求方式**：GET
**请求路径**：`/patient360/basic/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/basic/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "patient": {...},
    "visitCount": 5,
    "lastVisit": "2024-01-15",
    "totalFee": 5000.00,
    "hasArrears": false,
    "arrearsAmount": 0.00,
    "wechatBound": true,
    "wechatBindStatusLabel": "已绑定微信",
    "wechatBindUrl": "",
    "wechatFollowQrUrl": ""
  }
}
```

---

### 获取患者病历列表

**请求方式**：GET
**请求路径**：`/patient360/medical-records/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/medical-records/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "records": [...],
    "recentRecords": [...],
    "pendingLabOperationCount": 0
  }
}
```

---

### 获取患者时间轴

**请求方式**：GET
**请求路径**：`/patient360/timeline/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/timeline/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "timeline": [...]
  }
}
```

---

### 获取患者预约列表

**请求方式**：GET
**请求路径**：`/patient360/appointments/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/appointments/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "appointments": [...]
  }
}
```

---

### 获取患者治疗处置列表

**请求方式**：GET
**请求路径**：`/patient360/treatments/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/treatments/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "treatments": [...]
  }
}
```

---

### 获取患者影像列表

**请求方式**：GET
**请求路径**：`/patient360/images/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/images/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "images": [...]
  }
}
```

---

### 获取患者知情同意书列表

**请求方式**：GET
**请求路径**：`/patient360/consents/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/consents/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "consents": [...]
  }
}
```

---

### 获取患者随访记录

**请求方式**：GET
**请求路径**：`/patient360/followups/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/followups/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "followups": [...],
    "recentFollowups": [...],
    "nextFollowup": "2024-02-01"
  }
}
```

---

### 获取患者洞察摘要

**请求方式**：GET
**请求路径**：`/patient360/insight/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/insight/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "patientInsight": {...},
    "referralRecord": {...}
  }
}
```

---

### 获取患者风险标签

**请求方式**：GET
**请求路径**：`/patient360/risk-tags/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient360/risk-tags/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "riskTags": [...]
  }
}
```

---

## PatientCustomGroupController

### 新增患者自定义分组

**请求方式**：POST
**请求路径**：`/patient-groups/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| group | Body | Object | 是 | 分组实体对象（含 name, description 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-groups/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "VIP患者",
    "description": "重要客户分组"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "VIP患者",
    "description": "重要客户分组"
  }
}
```

---

### 分配患者到分组

**请求方式**：POST
**请求路径**：`/patient-groups/assign`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| request | Body | Object | 是 | 分配请求对象（含 group_id, patient_ids 字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-groups/assign" \
  -H "Content-Type: application/json" \
  -d '{
    "group_id": 1,
    "patient_ids": [1, 2, 3]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "分组分配成功"
}
```

---

## PatientRiskTagController

### 按患者ID查询风险标签

**请求方式**：GET
**请求路径**：`/risk-tags/selectByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/risk-tags/selectByPatientId?patientId=1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "tag": "高血压",
      "status": 1
    }
  ]
}
```

---

### 新增风险标签

**请求方式**：POST
**请求路径**：`/risk-tags/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| t | Body | Object | 是 | 风险标签对象（含 patientId, tag 等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/risk-tags/add" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "tag": "高血压"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

---

### 编辑风险标签

**请求方式**：PUT
**请求路径**：`/risk-tags/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| t | Body | Object | 是 | 风险标签对象（含 id, patientId, tag 等字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/risk-tags/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "patientId": 1,
    "tag": "糖尿病"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

---

### 删除风险标签

**请求方式**：DELETE
**请求路径**：`/risk-tags/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 风险标签ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/risk-tags/delete/1"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## PatientInsightController

### 获取患者洞察概览

**请求方式**：GET
**请求路径**：`/patient-insights/overview`

**请求参数**：

无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-insights/overview"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "totalPatients": 100,
    "activePatients": 80,
    "newPatientsThisMonth": 20,
    "averageSpend": 5000.00
  }
}
```
# 二、预约、病历与治疗管理

## PatientImageController

### 根据患者ID查询影像列表

**请求方式**：GET
**请求路径**：`/patient-images/selectByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-images/selectByPatientId?patientId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "patient_id": 1,
      "patient_name": "张三",
      "image_name": "全景片.jpg",
      "image_type": "全景片",
      "file_path": "uuid.jpg",
      "notes": "备注",
      "image_date": "2024-01-01",
      "sent_to_patient": false
    }
  ]
}
```

### 上传患者影像

**请求方式**：POST
**请求路径**：`/patient-images/upload`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| file | Body (multipart) | MultipartFile | 是 | 影像文件 |
| patientId | Query | Long | 是 | 患者ID |
| patientName | Query | String | 是 | 患者姓名 |
| imageType | Query | String | 否 | 影像类型，默认"其他" |
| imageDate | Query | String | 否 | 影像日期，格式 yyyy-MM-dd |
| notes | Query | String | 否 | 备注 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-images/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/image.jpg" \
  -F "patientId=1" \
  -F "patientName=张三" \
  -F "imageType=全景片" \
  -F "imageDate=2024-01-01" \
  -F "notes=初诊影像"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "上传成功"
}
```

### 读取影像文件流

**请求方式**：GET
**请求路径**：`/patient-images/file/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 影像记录ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-images/file/1" \
  -H "Content-Type: application/json"
```

**响应示例**：文件流（Content-Type 根据文件类型自动探测）

### 标记影像已发送给患者

**请求方式**：POST
**请求路径**：`/patient-images/send/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 影像记录ID |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-images/send/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "sent_to_patient": true,
    "sent_at": "2024-01-01T12:00:00"
  }
}
```

### 删除影像

**请求方式**：DELETE
**请求路径**：`/patient-images/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 影像记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/patient-images/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## PatientFollowupController

### 分页查询所有回访记录

**请求方式**：GET
**请求路径**：`/followup/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/followup/selectAll?page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询所有回访记录详情

**请求方式**：GET
**请求路径**：`/followup/selectAllDetail`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/followup/selectAllDetail" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 根据患者ID查询回访记录

**请求方式**：GET
**请求路径**：`/followup/selectByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/followup/selectByPatientId?patientId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "followupType": "电话回访",
      "content": "术后恢复良好",
      "followupDate": "2024-01-01"
    }
  ]
}
```

### 新增回访记录

**请求方式**：POST
**请求路径**：`/followup/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | PatientFollowup | 是 | 回访记录对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/followup/add" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "followupType": "电话回访",
    "content": "术后恢复良好",
    "followupDate": "2024-01-01"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 编辑回访记录

**请求方式**：PUT
**请求路径**：`/followup/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | PatientFollowup | 是 | 回访记录对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/followup/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "patientId": 1,
    "followupType": "微信回访",
    "content": "更新内容",
    "followupDate": "2024-01-02"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 删除回访记录

**请求方式**：DELETE
**请求路径**：`/followup/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 回访记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/followup/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## PatientConsentController

### 根据患者ID查询知情同意书列表

**请求方式**：GET
**请求路径**：`/patient-consent/selectByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-consent/selectByPatientId?patientId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "patientId": 1,
      "templateId": 1,
      "title": "拔牙知情同意书",
      "content": "...",
      "signedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

### 查询知情同意书详情

**请求方式**：GET
**请求路径**：`/patient-consent/detail/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 知情同意书ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-consent/detail/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "templateId": 1,
    "title": "拔牙知情同意书",
    "content": "...",
    "signedAt": "2024-01-01T10:00:00"
  }
}
```

### 签发知情同意书

**请求方式**：POST
**请求路径**：`/patient-consent/issue`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | PatientConsent | 是 | 知情同意书对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-consent/issue" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "templateId": 1,
    "title": "拔牙知情同意书",
    "content": "患者同意接受拔牙治疗..."
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "title": "拔牙知情同意书"
  }
}
```

---

## ConsentTemplateController

### 查询所有知情同意书模板

**请求方式**：GET
**请求路径**：`/consent-template/selectAll`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consent-template/selectAll" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "拔牙知情同意书",
      "content": "...",
      "enabled": true
    }
  ]
}
```

### 查询启用的知情同意书模板

**请求方式**：GET
**请求路径**：`/consent-template/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consent-template/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "拔牙知情同意书",
      "content": "...",
      "enabled": true
    }
  ]
}
```

### 新增知情同意书模板

**请求方式**：POST
**请求路径**：`/consent-template/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | ConsentTemplate | 是 | 模板对象，title 和 content 必填 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/consent-template/add" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "拔牙知情同意书",
    "content": "患者同意接受拔牙治疗...",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 编辑知情同意书模板

**请求方式**：PUT
**请求路径**：`/consent-template/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | ConsentTemplate | 是 | 模板对象，id/title/content 必填 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/consent-template/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "title": "拔牙知情同意书（修订版）",
    "content": "更新后的内容...",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 删除知情同意书模板

**请求方式**：DELETE
**请求路径**：`/consent-template/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 模板ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/consent-template/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## AppointmentController

### 分页查询所有预约（支持按状态过滤）

**请求方式**：GET
**请求路径**：`/appointments/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |
| status | Query | String | 否 | 预约状态过滤 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/appointments/selectAll?page=1&size=20&status=待就诊" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 获取日程条目

**请求方式**：GET
**请求路径**：`/appointments/scheduleEntries`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/appointments/scheduleEntries" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 根据患者ID分页查询预约（支持按状态过滤）

**请求方式**：GET
**请求路径**：`/appointments/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 患者ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |
| status | Query | String | 否 | 预约状态过滤 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/appointments/selectByid?id=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 公开查询预约详情

**请求方式**：GET
**请求路径**：`/appointments/public/detail`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 预约ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/appointments/public/detail?id=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patientName": "张三",
    "appointmentDate": "2024-01-01",
    "status": "待就诊"
  }
}
```

### 根据患者姓名分页查询预约（支持按状态过滤）

**请求方式**：GET
**请求路径**：`/appointments/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 患者姓名 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |
| status | Query | String | 否 | 预约状态过滤 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/appointments/selectByname?name=张三&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 5,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 更新预约状态

**请求方式**：PUT
**请求路径**：`/appointments/updateStatus/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 预约ID |
| status | Body | String | 是 | 新状态 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/appointments/updateStatus/1" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "已就诊"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "状态更新成功"
}
```

### 更新接诊状态

**请求方式**：PUT
**请求路径**：`/appointments/updateClinicStatus/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 预约ID |
| clinic_status | Body | String | 是 | 接诊状态 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/appointments/updateClinicStatus/1" \
  -H "Content-Type: application/json" \
  -d '{
    "clinic_status": "已接诊"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "接诊状态更新成功"
}
```

### 新增预约

**请求方式**：POST
**请求路径**：`/appointments/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Appointment | 是 | 预约对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/appointments/add" \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "张三",
    "patientPhone": "13800138000",
    "appointmentDate": "2024-01-01",
    "appointmentTime": "09:00",
    "doctorName": "李医生",
    "status": "待就诊"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 手动触发次日预约提醒

**请求方式**：POST
**请求路径**：`/appointments/manual-next-day-reminder`

**请求参数**：无

**请求示例**：
```bash
curl -X POST "http://localhost:8080/appointments/manual-next-day-reminder" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "已手动触发次日预约提醒"
}
```

### 编辑预约

**请求方式**：PUT
**请求路径**：`/appointments/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Appointment | 是 | 预约对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/appointments/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "patientName": "张三",
    "appointmentDate": "2024-01-02",
    "appointmentTime": "10:00",
    "doctorName": "王医生"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 取消预约

**请求方式**：POST
**请求路径**：`/appointments/cancel/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 预约ID |
| reason | Body | String | 否 | 取消原因 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/appointments/cancel/1" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "患者临时有事"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "status": "已取消"
  }
}
```

### 删除预约

**请求方式**：DELETE
**请求路径**：`/appointments/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | int | 是 | 预约ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/appointments/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 批量删除预约

**请求方式**：DELETE
**请求路径**：`/appointments/deleteBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | List<Long> | 是 | 预约ID列表 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/appointments/deleteBatch" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量删除成功"
}
```

---

## MedicalRecordController

### 分页查询所有病历（支持多条件过滤）

**请求方式**：GET
**请求路径**：`/medical-records/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |
| doctorAccountId | Query | Long | 否 | 医生账号ID |
| recordStatus | Query | String | 否 | 病历状态 |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-records/selectAll?page=1&size=20&doctorAccountId=1&recordStatus=初诊&startDate=2024-01-01&endDate=2024-01-31" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 根据患者ID分页查询病历

**请求方式**：GET
**请求路径**：`/medical-records/selectByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-records/selectByPatientId?patientId=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 根据患者姓名分页查询病历

**请求方式**：GET
**请求路径**：`/medical-records/selectByPatientName`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 患者姓名 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-records/selectByPatientName?name=张三&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 5,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 根据ID查询病历详情

**请求方式**：GET
**请求路径**：`/medical-records/selectById`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 病历ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-records/selectById?id=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "patientName": "张三",
    "chiefComplaint": "牙痛",
    "diagnosis": "龋齿",
    "treatmentPlan": "补牙",
    "recordDate": "2024-01-01"
  }
}
```

### 新增病历

**请求方式**：POST
**请求路径**：`/medical-records/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecord | 是 | 病历对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/medical-records/add" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "patientName": "张三",
    "chiefComplaint": "牙痛",
    "diagnosis": "龋齿",
    "treatmentPlan": "补牙",
    "recordDate": "2024-01-01"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "patientName": "张三"
  }
}
```

### 编辑病历

**请求方式**：PUT
**请求路径**：`/medical-records/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecord | 是 | 病历对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/medical-records/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "patientId": 1,
    "patientName": "张三",
    "chiefComplaint": "牙痛加重",
    "diagnosis": "深龋",
    "treatmentPlan": "根管治疗"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patientId": 1,
    "patientName": "张三"
  }
}
```

### 删除病历

**请求方式**：DELETE
**请求路径**：`/medical-records/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 病历ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/medical-records/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## MedicalRecordTemplateController

### 查询所有病历模板

**请求方式**：GET
**请求路径**：`/medical-record-templates/selectAll`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-record-templates/selectAll" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "初诊模板",
      "content": "...",
      "enabled": true
    }
  ]
}
```

### 查询启用的病历模板

**请求方式**：GET
**请求路径**：`/medical-record-templates/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-record-templates/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "初诊模板",
      "content": "...",
      "enabled": true
    }
  ]
}
```

### 新增病历模板

**请求方式**：POST
**请求路径**：`/medical-record-templates/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecordTemplate | 是 | 模板对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/medical-record-templates/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "初诊模板",
    "content": "主诉：\n现病史：\n检查：",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "初诊模板"
  }
}
```

### 编辑病历模板

**请求方式**：PUT
**请求路径**：`/medical-record-templates/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecordTemplate | 是 | 模板对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/medical-record-templates/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "初诊模板（修订）",
    "content": "更新后的内容...",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "初诊模板（修订）"
  }
}
```

### 删除病历模板

**请求方式**：DELETE
**请求路径**：`/medical-record-templates/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 模板ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/medical-record-templates/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## MedicalRecordPhraseController

### 根据字段类型查询病历短语

**请求方式**：GET
**请求路径**：`/medical-record-phrases/selectByFieldType`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| fieldType | Query | String | 是 | 字段类型 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-record-phrases/selectByFieldType?fieldType=chiefComplaint" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "fieldType": "chiefComplaint",
      "phrase": "牙齿疼痛一周"
    }
  ]
}
```

### 查询所有病历短语

**请求方式**：GET
**请求路径**：`/medical-record-phrases/selectAll`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-record-phrases/selectAll" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "fieldType": "chiefComplaint",
      "phrase": "牙齿疼痛一周"
    }
  ]
}
```

### 新增病历短语

**请求方式**：POST
**请求路径**：`/medical-record-phrases/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecordPhrase | 是 | 短语对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/medical-record-phrases/add" \
  -H "Content-Type: application/json" \
  -d '{
    "fieldType": "chiefComplaint",
    "phrase": "牙齿疼痛一周"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "fieldType": "chiefComplaint",
    "phrase": "牙齿疼痛一周"
  }
}
```

### 编辑病历短语

**请求方式**：PUT
**请求路径**：`/medical-record-phrases/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecordPhrase | 是 | 短语对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/medical-record-phrases/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "fieldType": "chiefComplaint",
    "phrase": "牙齿疼痛两周"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "fieldType": "chiefComplaint",
    "phrase": "牙齿疼痛两周"
  }
}
```

### 删除病历短语

**请求方式**：DELETE
**请求路径**：`/medical-record-phrases/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 短语ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/medical-record-phrases/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## MedicalRecordOperationController

### 根据病历ID查询操作记录

**请求方式**：GET
**请求路径**：`/medical-record-operations/selectByMedicalRecordId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| medicalRecordId | Query | Long | 是 | 病历ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-record-operations/selectByMedicalRecordId?medicalRecordId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "medicalRecordId": 1,
      "operationName": "拔牙",
      "operationDate": "2024-01-01"
    }
  ]
}
```

### 查询待加工列表

**请求方式**：GET
**请求路径**：`/medical-record-operations/pendingLabList`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 否 | 患者ID |
| doctorAccountId | Query | Long | 否 | 医生账号ID |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/medical-record-operations/pendingLabList?patientId=1&doctorAccountId=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20,
    "pendingTotal": 10
  }
}
```

### 标记跳过

**请求方式**：PUT
**请求路径**：`/medical-record-operations/markSkip`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | MedicalRecordOperation | 是 | 操作对象，id/skip_reason/updated_by/updated_by_name 必填 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/medical-record-operations/markSkip" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "skip_reason": "患者取消",
    "updated_by": 1,
    "updated_by_name": "管理员"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "skip_reason": "患者取消",
    "status": "已跳过"
  }
}
```

---

## TreatmentController

### 分页查询所有治疗记录

**请求方式**：GET
**请求路径**：`/treatments/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatments/selectAll?page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询患者最近的治疗记录

**请求方式**：GET
**请求路径**：`/treatments/recentByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |
| limit | Query | Integer | 否 | 条数限制，默认10 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatments/recentByPatientId?patientId=1&limit=10" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "patient_id": 1,
      "patient_name": "张三",
      "treatment_date": "2024-01-01",
      "appointment_purpose": "补牙"
    }
  ]
}
```

### 根据患者ID分页查询治疗记录

**请求方式**：GET
**请求路径**：`/treatments/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 患者ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatments/selectByid?id=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 根据患者姓名分页查询治疗记录

**请求方式**：GET
**请求路径**：`/treatments/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 患者姓名 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatments/selectByname?name=张三&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 5,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 新增治疗记录

**请求方式**：POST
**请求路径**：`/treatments/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Treatment | 是 | 治疗记录对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatments/add" \
  -H "Content-Type: application/json" \
  -d '{
    "patient_id": 1,
    "patient_name": "张三",
    "appointment_purpose": "补牙",
    "doctor_account_id": 1,
    "doctor_name": "李医生",
    "treatment_date": "2024-01-01",
    "treatment_content": "树脂补牙",
    "tooth_positions": "16"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "patient_id": 1,
    "patient_name": "张三"
  }
}
```

### 批量新增治疗记录

**请求方式**：POST
**请求路径**：`/treatments/batchAdd`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentBatchCreateRequest | 是 | 批量创建请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatments/batchAdd" \
  -H "Content-Type: application/json" \
  -d '{
    "patient_id": 1,
    "treatments": [
      {
        "appointment_purpose": "补牙",
        "treatment_date": "2024-01-01"
      }
    ]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "patient_id": 1,
      "appointment_purpose": "补牙"
    }
  ]
}
```

### 编辑治疗记录

**请求方式**：PUT
**请求路径**：`/treatments/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Treatment | 是 | 治疗记录对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/treatments/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "patient_id": 1,
    "patient_name": "张三",
    "appointment_purpose": "根管治疗",
    "doctor_name": "王医生",
    "treatment_date": "2024-01-02"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 收费

**请求方式**：POST
**请求路径**：`/treatments/charge/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 治疗记录ID |
| (Body) | Body | TreatmentBillingRequest | 否 | 收费请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatments/charge/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "payment_method": "微信支付"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "treatment_id": 1,
    "amount": 500.00,
    "payment_method": "微信支付"
  }
}
```

### 批量收费

**请求方式**：POST
**请求路径**：`/treatments/chargeBatch/{batchNo}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| batchNo | Path | String | 是 | 批次号 |
| (Body) | Body | TreatmentBillingRequest | 否 | 收费请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatments/chargeBatch/B20240101001" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1500.00,
    "payment_method": "支付宝"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "treatment_id": 1,
      "amount": 500.00
    }
  ]
}
```

### 退费

**请求方式**：POST
**请求路径**：`/treatments/refund/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 治疗记录ID |
| (Body) | Body | TreatmentBillingRequest | 否 | 退费请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatments/refund/1" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "refund_reason": "患者取消治疗"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "treatment_id": 1,
    "amount": -500.00,
    "refund_reason": "患者取消治疗"
  }
}
```

### 删除治疗记录

**请求方式**：DELETE
**请求路径**：`/treatments/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 治疗记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/treatments/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## Treatment_plansController

### 查询所有治疗方案

**请求方式**：GET
**请求路径**：`/treatment_plans/selectAll`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment_plans/selectAll" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "planName": "综合治疗方案",
      "description": "..."
    }
  ]
}
```

---

## TreatmentCatalogController

### 查询所有治疗项目目录

**请求方式**：GET
**请求路径**：`/treatment-catalog/selectAll`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-catalog/selectAll" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "item_name": "拔牙",
      "item_code": "EX001",
      "price": 200.00
    }
  ]
}
```

### 查询启用的治疗项目目录

**请求方式**：GET
**请求路径**：`/treatment-catalog/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-catalog/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "item_name": "拔牙",
      "item_code": "EX001",
      "price": 200.00
    }
  ]
}
```

### 新增治疗项目目录

**请求方式**：POST
**请求路径**：`/treatment-catalog/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentCatalog | 是 | 目录对象，item_name 必填 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatment-catalog/add" \
  -H "Content-Type: application/json" \
  -d '{
    "item_name": "拔牙",
    "item_code": "EX001",
    "price": 200.00,
    "unit": "颗",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 编辑治疗项目目录

**请求方式**：PUT
**请求路径**：`/treatment-catalog/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentCatalog | 是 | 目录对象，id 和 item_name 必填 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/treatment-catalog/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "item_name": "拔牙（复杂）",
    "item_code": "EX002",
    "price": 500.00,
    "unit": "颗",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 删除治疗项目目录

**请求方式**：DELETE
**请求路径**：`/treatment-catalog/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 目录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/treatment-catalog/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## TreatmentProjectController

### 搜索治疗项目（支持分页）

**请求方式**：GET
**请求路径**：`/treatment-projects/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| categoryId | Query | Long | 否 | 分类ID |
| status | Query | String | 否 | 状态 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-projects/search?keyword=拔牙&categoryId=1&status=启用&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询启用的治疗项目

**请求方式**：GET
**请求路径**：`/treatment-projects/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-projects/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "projectName": "拔牙",
      "price": 200.00
    }
  ]
}
```

### 根据ID查询治疗项目

**请求方式**：GET
**请求路径**：`/treatment-projects/selectById`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 项目ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-projects/selectById?id=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectName": "拔牙",
    "price": 200.00
  }
}
```

### 新增治疗项目

**请求方式**：POST
**请求路径**：`/treatment-projects/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentProject | 是 | 项目对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatment-projects/add" \
  -H "Content-Type: application/json" \
  -d '{
    "projectName": "拔牙",
    "categoryId": 1,
    "price": 200.00,
    "unit": "颗",
    "status": "启用"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectName": "拔牙"
  }
}
```

### 编辑治疗项目

**请求方式**：PUT
**请求路径**：`/treatment-projects/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentProject | 是 | 项目对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/treatment-projects/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "projectName": "拔牙（复杂）",
    "price": 500.00
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectName": "拔牙（复杂）"
  }
}
```

### 删除治疗项目

**请求方式**：DELETE
**请求路径**：`/treatment-projects/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 项目ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/treatment-projects/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 批量导入治疗项目

**请求方式**：POST
**请求路径**：`/treatment-projects/importBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | List<TreatmentProject> | 是 | 项目列表 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatment-projects/importBatch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "projectName": "拔牙",
      "price": 200.00
    },
    {
      "projectName": "补牙",
      "price": 300.00
    }
  ]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "导入成功，共2条"
}
```

---

## TreatmentProjectCategoryController

### 查询治疗项目分类树

**请求方式**：GET
**请求路径**：`/treatment-project-categories/tree`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| includeDisabled | Query | boolean | 否 | 是否包含禁用分类，默认false |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-project-categories/tree?includeDisabled=false" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "口腔外科",
      "children": [
        {
          "id": 2,
          "name": "拔牙"
        }
      ]
    }
  ]
}
```

### 查询启用的分类（扁平列表）

**请求方式**：GET
**请求路径**：`/treatment-project-categories/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-project-categories/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "口腔外科"
    },
    {
      "id": 2,
      "name": "拔牙"
    }
  ]
}
```

### 新增治疗项目分类

**请求方式**：POST
**请求路径**：`/treatment-project-categories/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentProjectCategory | 是 | 分类对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatment-project-categories/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "口腔外科",
    "parentId": 0,
    "sortOrder": 1,
    "status": "启用"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "口腔外科"
  }
}
```

### 编辑治疗项目分类

**请求方式**：PUT
**请求路径**：`/treatment-project-categories/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentProjectCategory | 是 | 分类对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/treatment-project-categories/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "口腔外科（修订）",
    "sortOrder": 2
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "name": "口腔外科（修订）"
  }
}
```

### 删除治疗项目分类

**请求方式**：DELETE
**请求路径**：`/treatment-project-categories/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 分类ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/treatment-project-categories/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## TreatmentOperationController

### 搜索治疗操作（支持分页）

**请求方式**：GET
**请求路径**：`/treatment-operations/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| category | Query | String | 否 | 分类 |
| needLabProcessing | Query | Integer | 否 | 是否需要技工加工：1是/0否 |
| status | Query | String | 否 | 状态 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-operations/search?keyword=拔牙&category=外科&needLabProcessing=0&status=启用&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 10,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询启用的治疗操作

**请求方式**：GET
**请求路径**：`/treatment-operations/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-operations/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "operationName": "拔牙",
      "category": "外科"
    }
  ]
}
```

### 根据ID查询治疗操作

**请求方式**：GET
**请求路径**：`/treatment-operations/selectById`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 操作ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/treatment-operations/selectById?id=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "operationName": "拔牙",
    "category": "外科",
    "needLabProcessing": 0
  }
}
```

### 新增治疗操作

**请求方式**：POST
**请求路径**：`/treatment-operations/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentOperation | 是 | 操作对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatment-operations/add" \
  -H "Content-Type: application/json" \
  -d '{
    "operationName": "拔牙",
    "category": "外科",
    "needLabProcessing": 0,
    "status": "启用"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "operationName": "拔牙"
  }
}
```

### 编辑治疗操作

**请求方式**：PUT
**请求路径**：`/treatment-operations/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | TreatmentOperation | 是 | 操作对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/treatment-operations/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "operationName": "复杂拔牙",
    "category": "外科",
    "needLabProcessing": 1
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "id": 1,
    "operationName": "复杂拔牙"
  }
}
```

### 删除治疗操作

**请求方式**：DELETE
**请求路径**：`/treatment-operations/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 操作ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/treatment-operations/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 批量导入治疗操作

**请求方式**：POST
**请求路径**：`/treatment-operations/importBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | List<TreatmentOperation> | 是 | 操作列表 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/treatment-operations/importBatch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "operationName": "拔牙",
      "category": "外科"
    },
    {
      "operationName": "补牙",
      "category": "内科"
    }
  ]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "导入成功，共2条"
}
```
# 三、财务、库存与技工加工

## FinanceController

### 查询全部财务记录（分页）

**请求方式**：GET
**请求路径**：`/finances/all`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/all?page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询患者最近财务记录

**请求方式**：GET
**请求路径**：`/finances/recentByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |
| limit | Query | Integer | 否 | 返回条数，默认10 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/recentByPatientId?patientId=1&limit=10" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按ID查询财务记录（分页）

**请求方式**：GET
**请求路径**：`/finances/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 财务记录ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/selectByid?id=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按姓名查询财务记录（分页）

**请求方式**：GET
**请求路径**：`/finances/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 患者姓名 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/selectByname?name=张三&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按金额查询财务记录（分页）

**请求方式**：GET
**请求路径**：`/finances/selectByamount`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| amount | Query | int | 是 | 金额 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/selectByamount?amount=100&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按类型查询财务记录（分页）

**请求方式**：GET
**请求路径**：`/finances/selectBytype`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| type | Query | String | 是 | 财务类型 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/selectBytype?type=收入&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按日期查询财务记录（分页）

**请求方式**：GET
**请求路径**：`/finances/selectBydate`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 是 | 日期 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/selectBydate?date=2024-01-01&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按月查询财务记录

**请求方式**：GET
**请求路径**：`/finances/selectByMonth`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| year | Query | int | 是 | 年份 |
| month | Query | int | 是 | 月份 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/selectByMonth?year=2024&month=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 查询医生业绩

**请求方式**：GET
**请求路径**：`/finances/doctorPerformance`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |
| doctorAccountId | Query | Long | 否 | 医生账号ID |
| doctorName | Query | String | 否 | 医生姓名 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/doctorPerformance?startDate=2024-01-01&endDate=2024-01-31&doctorName=张医生" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 查询支出概览

**请求方式**：GET
**请求路径**：`/finances/expenseOverview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/expenseOverview?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 搜索手工支出记录

**请求方式**：GET
**请求路径**：`/finances/manualExpenseSearch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |
| keyword | Query | String | 否 | 关键词 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/manualExpenseSearch?keyword=办公&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 新增手工支出

**请求方式**：POST
**请求路径**：`/finances/manualExpense/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| finance | Body | Object | 是 | 财务实体对象（含金额、类型、日期等字段） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/finances/manualExpense/add" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500,
    "type": "支出",
    "date": "2024-01-15",
    "description": "办公用品采购"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑手工支出

**请求方式**：PUT
**请求路径**：`/finances/manualExpense/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| finance | Body | Object | 是 | 财务实体对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/finances/manualExpense/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "amount": 600,
    "type": "支出",
    "date": "2024-01-15",
    "description": "办公用品采购（修正）"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 删除手工支出

**请求方式**：DELETE
**请求路径**：`/finances/manualExpense/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 财务记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/finances/manualExpense/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 按ID及月份查询财务记录

**请求方式**：GET
**请求路径**：`/finances/select1Byid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 财务记录ID |
| year | Query | Integer | 否 | 年份，默认当前年 |
| month | Query | Integer | 否 | 月份，默认当前月 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/select1Byid?id=1&year=2024&month=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按金额及月份查询财务记录

**请求方式**：GET
**请求路径**：`/finances/select1Byamount`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| amount | Query | String | 是 | 金额 |
| year | Query | Integer | 否 | 年份，默认当前年 |
| month | Query | Integer | 否 | 月份，默认当前月 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/select1Byamount?amount=100&year=2024&month=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按姓名及月份查询财务记录

**请求方式**：GET
**请求路径**：`/finances/select1Byname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 患者姓名 |
| year | Query | Integer | 否 | 年份，默认当前年 |
| month | Query | Integer | 否 | 月份，默认当前月 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/select1Byname?name=张三&year=2024&month=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按日期及月份查询财务记录

**请求方式**：GET
**请求路径**：`/finances/select1Bydate`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 是 | 日期 |
| year | Query | Integer | 否 | 年份，默认当前年 |
| month | Query | Integer | 否 | 月份，默认当前月 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/select1Bydate?date=2024-01-01&year=2024&month=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按类型及月份查询财务记录

**请求方式**：GET
**请求路径**：`/finances/select1Bytype`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| type | Query | String | 是 | 财务类型 |
| year | Query | Integer | 否 | 年份，默认当前年 |
| month | Query | Integer | 否 | 月份，默认当前月 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/finances/select1Bytype?type=收入&year=2024&month=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 新增财务记录

**请求方式**：POST
**请求路径**：`/finances/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| finance | Body | Object | 是 | 财务实体对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/finances/add" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200,
    "type": "收入",
    "date": "2024-01-10",
    "patientName": "张三"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 编辑财务记录

**请求方式**：PUT
**请求路径**：`/finances/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| finance | Body | Object | 是 | 财务实体对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/finances/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "amount": 250,
    "type": "收入",
    "date": "2024-01-10"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 更新财务记录

**请求方式**：PUT
**请求路径**：`/finances/update`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| finance | Body | Object | 是 | 财务实体对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/finances/update" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "amount": 300,
    "type": "收入",
    "date": "2024-01-10"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "Finance record updated successfully!"
}
```

### 删除财务记录

**请求方式**：DELETE
**请求路径**：`/finances/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | int | 是 | 财务记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/finances/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "Finance record deleted successfully!"
}
```

---

## PaymentChannelController

### 查询全部收款渠道

**请求方式**：GET
**请求路径**：`/payment-channels/selectAll`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/payment-channels/selectAll" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 查询启用的收款渠道

**请求方式**：GET
**请求路径**：`/payment-channels/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/payment-channels/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 新增收款渠道

**请求方式**：POST
**请求路径**：`/payment-channels/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 收款渠道对象（channel_name 必填） |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/payment-channels/add" \
  -H "Content-Type: application/json" \
  -d '{
    "channel_name": "微信支付",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 编辑收款渠道

**请求方式**：PUT
**请求路径**：`/payment-channels/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 收款渠道对象（id、channel_name 必填） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/payment-channels/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "channel_name": "支付宝",
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 删除收款渠道

**请求方式**：DELETE
**请求路径**：`/payment-channels/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 收款渠道ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/payment-channels/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## InventoryController

### 查询全部库存（不分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectAll1`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectAll1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 查询全部库存（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectAll?page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按ID查询库存（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 库存ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectByid?id=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按名称查询库存（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 产品名称 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectByname?name=口罩&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按分类查询库存（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectBycategory`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| category | Query | String | 是 | 分类 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectBycategory?category=耗材&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按品牌查询库存（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectBybrand`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| brand | Query | String | 是 | 品牌 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectBybrand?brand=3M&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按供应商查询库存（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectBysupplier`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| supplier | Query | String | 是 | 供应商 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectBysupplier?supplier=某某公司&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 新增库存

**请求方式**：POST
**请求路径**：`/Inventory/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| inventory | Body | Object | 是 | 库存实体对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/Inventory/add" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "医用口罩",
    "quantity": 100,
    "category": "耗材",
    "brand": "3M",
    "supplier": "某某公司"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 编辑库存

**请求方式**：PUT
**请求路径**：`/Inventory/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| inventory | Body | Object | 是 | 库存实体对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/Inventory/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "productName": "医用口罩",
    "quantity": 150,
    "category": "耗材"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

### 删除库存

**请求方式**：DELETE
**请求路径**：`/Inventory/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 库存ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/Inventory/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 批量删除库存

**请求方式**：DELETE
**请求路径**：`/Inventory/deleteBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| ids | Body | List<Long> | 是 | 库存ID列表 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/Inventory/deleteBatch" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量删除成功"
}
```

### 更新库存数量

**请求方式**：PUT
**请求路径**：`/Inventory/update/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 库存ID |
| inventory | Body | Object | 是 | 库存对象（quantity 字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/Inventory/update/1" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 200
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "库存更新成功"
}
```

### 批量新增库存

**请求方式**：POST
**请求路径**：`/Inventory/addBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| inventoryList | Body | List<Object> | 是 | 库存对象列表 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/Inventory/addBatch" \
  -H "Content-Type: application/json" \
  -d '[
    {"productName": "口罩", "quantity": 100},
    {"productName": "手套", "quantity": 200}
  ]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量新增成功"
}
```

### 查询低库存物品（分页）

**请求方式**：GET
**请求路径**：`/Inventory/selectLowStock`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/selectLowStock?page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按ID查询库存（分页，扩展）

**请求方式**：GET
**请求路径**：`/Inventory/select1Byid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 库存ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/select1Byid?id=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按名称查询库存（分页，扩展）

**请求方式**：GET
**请求路径**：`/Inventory/select1Byname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 产品名称 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/select1Byname?name=口罩&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按分类查询库存（分页，扩展）

**请求方式**：GET
**请求路径**：`/Inventory/select1Bycategory`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| category | Query | String | 是 | 分类 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/select1Bycategory?category=耗材&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按品牌查询库存（分页，扩展）

**请求方式**：GET
**请求路径**：`/Inventory/select1Bybrand`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| brand | Query | String | 是 | 品牌 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/select1Bybrand?brand=3M&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按供应商查询库存（分页，扩展）

**请求方式**：GET
**请求路径**：`/Inventory/select1Bysupplier`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| supplier | Query | String | 是 | 供应商 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/Inventory/select1Bysupplier?supplier=某某公司&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

## PurchaseController

### 查询全部采购记录（分页）

**请求方式**：GET
**请求路径**：`/purchase/selectAll`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/purchase/selectAll?page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按ID查询采购记录（分页）

**请求方式**：GET
**请求路径**：`/purchase/selectByid`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 采购记录ID |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/purchase/selectByid?id=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按名称查询采购记录（分页）

**请求方式**：GET
**请求路径**：`/purchase/selectByname`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| name | Query | String | 是 | 产品名称 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/purchase/selectByname?name=口罩&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按分类查询采购记录（分页）

**请求方式**：GET
**请求路径**：`/purchase/selectBycategory`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| category | Query | String | 是 | 分类 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/purchase/selectBycategory?category=耗材&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按品牌查询采购记录（分页）

**请求方式**：GET
**请求路径**：`/purchase/selectBybrand`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| brand | Query | String | 是 | 品牌 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/purchase/selectBybrand?brand=3M&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按供应商查询采购记录（分页）

**请求方式**：GET
**请求路径**：`/purchase/selectBysupplier`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| supplier | Query | String | 是 | 供应商 |
| page | Query | int | 是 | 页码 |
| size | Query | int | 是 | 每页条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/purchase/selectBysupplier?supplier=某某公司&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 新增采购记录

**请求方式**：POST
**请求路径**：`/purchase/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| purchase | Body | Object | 是 | 采购实体对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/purchase/add" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "医用口罩",
    "quantity": 100,
    "supplier": "某某公司",
    "category": "耗材"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "新增成功"
}
```

### 删除采购记录

**请求方式**：DELETE
**请求路径**：`/purchase/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 采购记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/purchase/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 批量删除采购记录

**请求方式**：DELETE
**请求路径**：`/purchase/deleteBatch`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| ids | Body | List<Long> | 是 | 采购记录ID列表 |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/purchase/deleteBatch" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量删除成功"
}
```

### 更新采购状态

**请求方式**：PUT
**请求路径**：`/purchase/updateStatus`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| purchase | Body | Object | 是 | 采购实体对象（含状态字段） |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/purchase/updateStatus" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "status": "已完成"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "编辑成功"
}
```

---

## MaterialController

### 搜索耗材

**请求方式**：GET
**请求路径**：`/materials/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| categoryId | Query | Long | 否 | 分类ID |
| lowStockOnly | Query | Boolean | 否 | 仅低库存 |
| status | Query | String | 否 | 状态 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/materials/search?keyword=口罩&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 轻量搜索耗材

**请求方式**：GET
**请求路径**：`/materials/searchLite`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| categoryId | Query | Long | 否 | 分类ID |
| limit | Query | Integer | 否 | 限制条数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/materials/searchLite?keyword=口罩&limit=10" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按ID查询耗材

**请求方式**：GET
**请求路径**：`/materials/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 耗材ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/materials/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 新增耗材

**请求方式**：POST
**请求路径**：`/materials/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 耗材对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/materials/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "医用口罩",
    "stock": 100,
    "categoryId": 1,
    "unit": "个"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑耗材

**请求方式**：PUT
**请求路径**：`/materials/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 耗材对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/materials/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "医用口罩",
    "stock": 150,
    "categoryId": 1,
    "unit": "个"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

---

## MaterialCategoryController

### 查询耗材分类树

**请求方式**：GET
**请求路径**：`/material-categories/tree`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| includeDisabled | Query | boolean | 否 | 包含禁用分类，默认false |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/material-categories/tree?includeDisabled=true" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 查询启用的耗材分类

**请求方式**：GET
**请求路径**：`/material-categories/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/material-categories/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 新增耗材分类

**请求方式**：POST
**请求路径**：`/material-categories/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 分类对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/material-categories/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "口腔耗材",
    "parentId": 0,
    "sortOrder": 1
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑耗材分类

**请求方式**：PUT
**请求路径**：`/material-categories/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 分类对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/material-categories/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "口腔耗材",
    "parentId": 0,
    "sortOrder": 1
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 删除耗材分类

**请求方式**：DELETE
**请求路径**：`/material-categories/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 分类ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/material-categories/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## MaterialPurchaseController

### 搜索耗材采购单

**请求方式**：GET
**请求路径**：`/material-purchases/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| supplierKeyword | Query | String | 否 | 供应商关键词 |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |
| status | Query | String | 否 | 状态 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/material-purchases/search?supplierKeyword=某某&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按ID查询耗材采购单

**请求方式**：GET
**请求路径**：`/material-purchases/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 采购单ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/material-purchases/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 新增耗材采购单

**请求方式**：POST
**请求路径**：`/material-purchases/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| purchase | Body | Object | 是 | 采购单对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/material-purchases/add" \
  -H "Content-Type: application/json" \
  -d '{
    "supplier": "某某公司",
    "totalAmount": 5000,
    "items": [...]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑耗材采购单

**请求方式**：PUT
**请求路径**：`/material-purchases/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| purchase | Body | Object | 是 | 采购单对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/material-purchases/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "supplier": "某某公司",
    "totalAmount": 5500
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 作废耗材采购单

**请求方式**：POST
**请求路径**：`/material-purchases/void/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 采购单ID |
| request | Body | Object | 否 | 作废请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/material-purchases/void/1" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "采购错误"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 上传采购发票

**请求方式**：POST
**请求路径**：`/material-purchases/uploadInvoice`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| file | Body | MultipartFile | 是 | 发票文件 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/material-purchases/uploadInvoice" \
  -F "file=@invoice.pdf"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "上传成功"
}
```

### 下载采购发票

**请求方式**：GET
**请求路径**：`/material-purchases/invoice/{purchaseId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| purchaseId | Path | Long | 是 | 采购单ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/material-purchases/invoice/1" \
  -H "Content-Type: application/json"
```

**响应示例**：文件流

---

## MaterialStatisticsController

### 耗材统计概览

**请求方式**：GET
**请求路径**：`/material-statistics/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| rangePreset | Query | String | 否 | 预设时间范围 |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/material-statistics/overview?rangePreset=本月" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

---

## LabFactoryController

### 加工厂概览

**请求方式**：GET
**请求路径**：`/lab-factories/dashboard/overview`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-factories/dashboard/overview" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 搜索加工厂

**请求方式**：GET
**请求路径**：`/lab-factories/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| status | Query | String | 否 | 状态 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-factories/search?keyword=某某&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询启用的加工厂

**请求方式**：GET
**请求路径**：`/lab-factories/selectEnabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-factories/selectEnabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 按ID查询加工厂

**请求方式**：GET
**请求路径**：`/lab-factories/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 加工厂ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-factories/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 新增加工厂

**请求方式**：POST
**请求路径**：`/lab-factories/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 加工厂对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-factories/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "某某加工厂",
    "contact": "张三",
    "phone": "13800138000",
    "address": "某某路1号"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑加工厂

**请求方式**：PUT
**请求路径**：`/lab-factories/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 加工厂对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/lab-factories/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "某某加工厂",
    "contact": "李四",
    "phone": "13900139000"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 删除加工厂

**请求方式**：DELETE
**请求路径**：`/lab-factories/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 加工厂ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/lab-factories/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 查询加工厂产品列表

**请求方式**：GET
**请求路径**：`/lab-factories/{factoryId}/products`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| enabledOnly | Query | boolean | 否 | 仅启用，默认false |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-factories/1/products?enabledOnly=true" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 新增加工厂产品

**请求方式**：POST
**请求路径**：`/lab-factories/{factoryId}/products/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| item | Body | Object | 是 | 产品对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-factories/1/products/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "烤瓷牙",
    "price": 500,
    "unit": "颗"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑加工厂产品

**请求方式**：PUT
**请求路径**：`/lab-factories/{factoryId}/products/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| item | Body | Object | 是 | 产品对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/lab-factories/1/products/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "烤瓷牙",
    "price": 550,
    "unit": "颗"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 批量保存加工厂产品

**请求方式**：POST
**请求路径**：`/lab-factories/{factoryId}/products/batchSave`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| items | Body | List<Object> | 是 | 产品对象列表 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-factories/1/products/batchSave" \
  -H "Content-Type: application/json" \
  -d '[
    {"name": "烤瓷牙", "price": 500, "unit": "颗"},
    {"name": "全瓷牙", "price": 800, "unit": "颗"}
  ]'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "保存成功"
}
```

### 删除加工厂产品

**请求方式**：DELETE
**请求路径**：`/lab-factories/{factoryId}/products/delete/{productId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| productId | Path | Long | 是 | 产品ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/lab-factories/1/products/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

### 查询加工厂账单模板

**请求方式**：GET
**请求路径**：`/lab-factories/{factoryId}/templates`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-factories/1/templates" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [...]
}
```

### 新增加工厂账单模板

**请求方式**：POST
**请求路径**：`/lab-factories/{factoryId}/templates/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| item | Body | Object | 是 | 模板对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-factories/1/templates/add" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "月结模板",
    "content": "..."
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑加工厂账单模板

**请求方式**：PUT
**请求路径**：`/lab-factories/{factoryId}/templates/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| item | Body | Object | 是 | 模板对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/lab-factories/1/templates/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "月结模板",
    "content": "..."
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 删除加工厂账单模板

**请求方式**：DELETE
**请求路径**：`/lab-factories/{factoryId}/templates/delete/{templateId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Path | Long | 是 | 加工厂ID |
| templateId | Path | Long | 是 | 模板ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/lab-factories/1/templates/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## LabOrderController

### 技工订单概览

**请求方式**：GET
**请求路径**：`/lab-orders/dashboard/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| factoryId | Query | Long | 否 | 加工厂ID |
| status | Query | String | 否 | 状态 |
| patientId | Query | Long | 否 | 患者ID |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-orders/dashboard/overview?factoryId=1&status=进行中" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 搜索技工订单

**请求方式**：GET
**请求路径**：`/lab-orders/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| keyword | Query | String | 否 | 关键词 |
| factoryId | Query | Long | 否 | 加工厂ID |
| status | Query | String | 否 | 状态 |
| patientId | Query | Long | 否 | 患者ID |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-orders/search?factoryId=1&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 按ID查询技工订单

**请求方式**：GET
**请求路径**：`/lab-orders/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 订单ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-orders/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 新增技工订单

**请求方式**：POST
**请求路径**：`/lab-orders/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 订单对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-orders/add" \
  -H "Content-Type: application/json" \
  -d '{
    "factoryId": 1,
    "patientId": 1,
    "productName": "烤瓷牙",
    "quantity": 2,
    "status": "进行中"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 编辑技工订单

**请求方式**：PUT
**请求路径**：`/lab-orders/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| item | Body | Object | 是 | 订单对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/lab-orders/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "factoryId": 1,
    "patientId": 1,
    "productName": "烤瓷牙",
    "quantity": 3,
    "status": "已完成"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 批量更新技工订单状态

**请求方式**：POST
**请求路径**：`/lab-orders/batchStatus`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| request | Body | Object | 是 | 批量状态更新请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-orders/batchStatus" \
  -H "Content-Type: application/json" \
  -d '{
    "ids": [1, 2, 3],
    "status": "已完成"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "批量更新成功"
}
```

### 删除技工订单

**请求方式**：DELETE
**请求路径**：`/lab-orders/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 订单ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/lab-orders/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "删除成功"
}
```

---

## LabBillController

### 搜索技工账单

**请求方式**：GET
**请求路径**：`/lab-bills/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| factoryId | Query | Long | 否 | 加工厂ID |
| status | Query | String | 否 | 状态 |
| billMonth | Query | String | 否 | 账单月份 |
| page | Query | int | 否 | 页码，默认1 |
| size | Query | int | 否 | 每页条数，默认20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-bills/search?factoryId=1&billMonth=2024-01&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "total": 100,
    "list": [...],
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 查询技工账单详情

**请求方式**：GET
**请求路径**：`/lab-bills/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 账单ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-bills/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 导入技工账单

**请求方式**：POST
**请求路径**：`/lab-bills/import`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| file | Body | MultipartFile | 是 | 账单文件 |
| factoryId | Query | Long | 是 | 加工厂ID |
| billMonth | Query | String | 是 | 账单月份 |
| templateId | Query | Long | 否 | 模板ID |
| importedBy | Query | Long | 否 | 导入人ID |
| importedByName | Query | String | 否 | 导入人姓名 |
| parsedItemsJson | Query | String | 是 | 解析后的项目JSON |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-bills/import" \
  -F "file=@bill.xlsx" \
  -F "factoryId=1" \
  -F "billMonth=2024-01" \
  -F "parsedItemsJson=[...]"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 更新账单项目对账结果

**请求方式**：PUT
**请求路径**：`/lab-bills/items/{id}/resolution`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 账单项目ID |
| request | Body | Object | 是 | 对账请求对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/lab-bills/items/1/resolution" \
  -H "Content-Type: application/json" \
  -d '{
    "resolved": true,
    "comment": "已对账"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 更新未匹配订单对账结果

**请求方式**：PUT
**请求路径**：`/lab-bills/unmatched-orders/{id}/resolution`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 未匹配订单ID |
| request | Body | Object | 是 | 对账请求对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/lab-bills/unmatched-orders/1/resolution" \
  -H "Content-Type: application/json" \
  -d '{
    "resolved": true,
    "comment": "已处理"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 确认账单

**请求方式**：POST
**请求路径**：`/lab-bills/confirm/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 账单ID |
| request | Body | Object | 否 | 确认请求对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/lab-bills/confirm/1" \
  -H "Content-Type: application/json" \
  -d '{
    "confirmedBy": 1,
    "comment": "确认无误"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```

### 下载账单文件

**请求方式**：GET
**请求路径**：`/lab-bills/file/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 账单ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-bills/file/1" \
  -H "Content-Type: application/json"
```

**响应示例**：文件流

---

## LabStatisticsController

### 技工统计概览

**请求方式**：GET
**请求路径**：`/lab-statistics/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| rangePreset | Query | String | 否 | 预设时间范围 |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/lab-statistics/overview?rangePreset=本月" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {...}
}
```
# 四、咨询营销、AI 代理与微信门户

## 1. ConsultationRecordController

### 分页搜索咨询记录

**请求方式**：GET
**请求路径**：`/consultations/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| page | Query | Integer | 否 | 页码，默认 1 |
| size | Query | Integer | 否 | 每页条数，默认 20 |
| keyword | Query | String | 否 | 姓名/手机号关键词 |
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |
| channel | Query | String | 否 | 渠道 |
| chiefProject | Query | String | 否 | 主诉项目 |
| intentLevel | Query | String | 否 | 意向等级 |
| handlingResult | Query | String | 否 | 处理结果 |
| hasDeal | Query | Boolean | 否 | 是否成交 |
| createdBy | Query | Long | 否 | 录入人账号ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/search?page=1&size=20&keyword=张三" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "total": 100, "list": [...], "pageNum": 1, "pageSize": 20 }
}
```

---

### 根据ID查询咨询记录

**请求方式**：GET
**请求路径**：`/consultations/selectById`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Query | Long | 是 | 咨询记录ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/selectById?id=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "patientName": "张三", "chiefProject": "种植牙" }
}
```

---

### 根据患者ID查询咨询记录

**请求方式**：GET
**请求路径**：`/consultations/selectByPatientId`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/selectByPatientId?patientId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "patientName": "张三" }]
}
```

---

### 新增咨询记录

**请求方式**：POST
**请求路径**：`/consultations/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | ConsultationRecord 对象，含 patientName/phone/consultationTime 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/consultations/add" \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "张三",
    "phone": "13800138000",
    "chiefProject": "种植牙",
    "intentLevel": "高",
    "channel": "美团"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "patientName": "张三" }
}
```

---

### 编辑咨询记录

**请求方式**：PUT
**请求路径**：`/consultations/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | ConsultationRecord 对象，需包含 id |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/consultations/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "patientName": "张三",
    "phone": "13800138000",
    "chiefProject": "矫正",
    "intentLevel": "中"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": true
}
```

---

### 根据手机号匹配患者

**请求方式**：GET
**请求路径**：`/consultations/matchPatientByPhone`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| phone | Query | String | 否 | 手机号 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/matchPatientByPhone?phone=13800138000" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "name": "张三" }
}
```

---

### 为患者创建匹配咨询记录

**请求方式**：GET
**请求路径**：`/consultations/matchForPatientCreate`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| phone | Query | String | 否 | 手机号 |
| name | Query | String | 否 | 姓名 |
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| page | Query | Integer | 否 | 页码，默认 1 |
| size | Query | Integer | 否 | 每页条数，默认 20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/matchForPatientCreate?phone=13800138000&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "total": 10, "list": [...], "pageNum": 1, "pageSize": 20 }
}
```

---

### 关联患者

**请求方式**：POST
**请求路径**：`/consultations/linkPatient`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| consultationId | Body | Long | 是 | 咨询记录ID |
| patientId | Body | Long | 是 | 患者ID |
| updatedBy | Body | Long | 否 | 操作人账号ID |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/consultations/linkPatient" \
  -H "Content-Type: application/json" \
  -d '{
    "consultationId": 1,
    "patientId": 2,
    "updatedBy": 3
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": true
}
```

---

### 查询咨询跟进历史

**请求方式**：GET
**请求路径**：`/consultations/{id}/followups`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 咨询记录ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/1/followups" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "content": "电话回访", "followupTime": "2025-01-01 10:00" }]
}
```

---

### AI 分析咨询记录

**请求方式**：POST
**请求路径**：`/consultations/aiAnalyze`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| consultationId | Body | Long | 是 | 咨询记录ID |
| chiefProject | Body | String | 否 | 主诉项目 |
| intentLevel | Body | String | 否 | 意向等级 |
| remarks | Body | String | 否 | 备注 |
| customerConcerns | Body | String | 否 | 客户顾虑 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/consultations/aiAnalyze" \
  -H "Content-Type: application/json" \
  -d '{
    "consultationId": 1,
    "chiefProject": "种植牙",
    "intentLevel": "高",
    "remarks": "客户关注价格",
    "customerConcerns": "价格敏感"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "consultationId": 1,
    "intentScore": 78,
    "summary": "客户对种植牙有明确需求，沟通内容显示其关注度高。",
    "suggestedNextFollowup": "建议明天电话回访，强调本院分期免息政策。",
    "riskPoints": ["价格敏感"],
    "suggestedActions": ["发送分期方案资料", "预约院长面诊"]
  }
}
```

---

### 咨询概览

**请求方式**：GET
**请求路径**：`/consultations/dashboard/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/overview?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "totalCount": 100, "dealCount": 30, "conversionRate": 0.3 }
}
```

---

### 咨询漏斗分析

**请求方式**：GET
**请求路径**：`/consultations/dashboard/funnel`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/funnel?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "stages": [{"name": "初诊", "count": 100}] }
}
```

---

### 渠道分析

**请求方式**：GET
**请求路径**：`/consultations/dashboard/channelAnalysis`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/channelAnalysis?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "channels": [{"name": "美团", "count": 50}] }
}
```

---

### 项目分析

**请求方式**：GET
**请求路径**：`/consultations/dashboard/projectAnalysis`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/projectAnalysis?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "projects": [{"name": "种植牙", "count": 40}] }
}
```

---

### 时段热力图

**请求方式**：GET
**请求路径**：`/consultations/dashboard/hourHeatmap`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/hourHeatmap?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "heatmap": [{"hour": 9, "count": 20}] }
}
```

---

### 客服业绩

**请求方式**：GET
**请求路径**：`/consultations/dashboard/nursePerformance`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/nursePerformance?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "performances": [{"nurseName": "李护士", "count": 30}] }
}
```

---

### 转介绍分析

**请求方式**：GET
**请求路径**：`/consultations/dashboard/referralAnalysis`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startTime | Query | String | 否 | 开始时间 |
| endTime | Query | String | 否 | 结束时间 |
| rangePreset | Query | String | 否 | 时间范围预设 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/dashboard/referralAnalysis?rangePreset=week" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "referrals": [{"source": "老客户", "count": 10}] }
}
```

---

## 2. ConsultationFollowupController

### 查询跟进记录列表

**请求方式**：GET
**请求路径**：`/consultations/followups/list`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| consultationId | Query | Long | 是 | 咨询记录ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/consultations/followups/list?consultationId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "content": "电话回访", "followupTime": "2025-01-01 10:00" }]
}
```

---

### 新增跟进记录

**请求方式**：POST
**请求路径**：`/consultations/followups/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | ConsultationFollowup 对象，含 consultationId/content 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/consultations/followups/add" \
  -H "Content-Type: application/json" \
  -d '{
    "consultationId": 1,
    "content": "客户有意向，明日再联系",
    "followupTime": "2025-01-01 10:00"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "content": "客户有意向，明日再联系" }
}
```

---

### 删除跟进记录

**请求方式**：DELETE
**请求路径**：`/consultations/followups/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 跟进记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/consultations/followups/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": null
}
```

---

## 3. AdvertisingSpendingController

### 分页搜索广告投放记录

**请求方式**：GET
**请求路径**：`/advertising-spending/search`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| platform | Query | String | 否 | 投放平台 |
| keyword | Query | String | 否 | 关键词 |
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |
| createdBy | Query | Long | 否 | 录入人账号ID |
| page | Query | Integer | 否 | 页码，默认 1 |
| size | Query | Integer | 否 | 每页条数，默认 20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/advertising-spending/search?platform=美团&page=1&size=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "total": 100, "list": [...], "pageNum": 1, "pageSize": 20 }
}
```

---

### 新增广告投放记录

**请求方式**：POST
**请求路径**：`/advertising-spending/add`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | AdvertisingSpending 对象，含 platform/amount/date 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/advertising-spending/add" \
  -H "Content-Type: application/json" \
  -d '{
    "platform": "美团",
    "amount": 5000,
    "date": "2025-01-01",
    "remark": "月度推广"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "platform": "美团", "amount": 5000 }
}
```

---

### 编辑广告投放记录

**请求方式**：PUT
**请求路径**：`/advertising-spending/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | AdvertisingSpending 对象，需包含 id |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/advertising-spending/edit" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "platform": "美团",
    "amount": 6000,
    "date": "2025-01-01"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "platform": "美团", "amount": 6000 }
}
```

---

### 删除广告投放记录

**请求方式**：DELETE
**请求路径**：`/advertising-spending/delete/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 记录ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/advertising-spending/delete/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "删除成功",
  "data": null
}
```

---

### 投放概览

**请求方式**：GET
**请求路径**：`/advertising-spending/dashboard/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| startDate | Query | String | 否 | 开始日期 |
| endDate | Query | String | 否 | 结束日期 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/advertising-spending/dashboard/overview?startDate=2025-01-01&endDate=2025-01-31" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "totalAmount": 15000, "platformBreakdown": [{"platform": "美团", "amount": 5000}] }
}
```

---

## 4. AiProxyController

### AI 统一代理接口（支持 SSE 流式）

**请求方式**：POST
**请求路径**：`/api/ai/proxy/{agentKey}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| agentKey | Path | String | 是 | 代理标识，如 medical-expand、default、finance |
| (Body) | Body | Object | 是 | 请求体数据，字段视具体 agent 而定 |
| Accept | Header | String | 否 | 设为 text/event-stream 启用 SSE 模式 |

**请求示例（JSON 模式）**：
```bash
curl -X POST "http://localhost:8080/api/ai/proxy/default" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "分析今日业绩"
  }'
```

**请求示例（SSE 流式模式）**：
```bash
curl -X POST "http://localhost:8080/api/ai/proxy/default" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "message": "分析今日业绩"
  }'
```

**响应示例（JSON 模式）**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "AI 分析结果文本"
}
```

---

## 5. AiAgentConfigController

### 查询 AI 代理配置列表

**请求方式**：GET
**请求路径**：`/ai-agent-configs`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| accountId | Query | Long | 否 | 账号ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/ai-agent-configs?accountId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "agentKey": "default", "name": "默认代理" }]
}
```

---

### 查询 AI 代理配置详情

**请求方式**：GET
**请求路径**：`/ai-agent-configs/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 配置ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/ai-agent-configs/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "agentKey": "default", "name": "默认代理" }
}
```

---

### 根据 Key 查询 AI 代理配置

**请求方式**：GET
**请求路径**：`/ai-agent-configs/by-key`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| accountId | Query | Long | 否 | 账号ID |
| agentKey | Query | String | 是 | 代理标识 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/ai-agent-configs/by-key?agentKey=default&accountId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "agentKey": "default", "name": "默认代理" }
}
```

---

### 创建 AI 代理配置

**请求方式**：POST
**请求路径**：`/ai-agent-configs`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | AiAgentConfig 对象，含 agentKey/name/configJson 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/ai-agent-configs" \
  -H "Content-Type: application/json" \
  -d '{
    "agentKey": "medical-expand",
    "name": "病历扩写代理",
    "configJson": "{}"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "agentKey": "medical-expand", "name": "病历扩写代理" }
}
```

---

### 更新 AI 代理配置

**请求方式**：PUT
**请求路径**：`/ai-agent-configs/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 配置ID |
| (Body) | Body | Object | 是 | AiAgentConfig 对象 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/ai-agent-configs/1" \
  -H "Content-Type: application/json" \
  -d '{
    "agentKey": "medical-expand",
    "name": "病历扩写代理",
    "configJson": "{}"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "agentKey": "medical-expand", "name": "病历扩写代理" }
}
```

---

### 删除 AI 代理配置

**请求方式**：DELETE
**请求路径**：`/ai-agent-configs/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 配置ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/ai-agent-configs/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "删除成功",
  "data": null
}
```

---

## 6. AiConfigController

### 获取 AI 配置概览

**请求方式**：GET
**请求路径**：`/api/ai-config/overview`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/ai-config/overview" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "globalEnabled": true, "functions": [{"key": "medical-expand", "enabled": true}] }
}
```

---

### 获取 AI 功能列表

**请求方式**：GET
**请求路径**：`/api/ai-config/functions`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/ai-config/functions" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "key": "medical-expand", "name": "病历扩写", "enabled": true }]
}
```

---

### 更新全局 AI 配置

**请求方式**：PUT
**请求路径**：`/api/ai-config/global`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | GlobalConfigDTO 对象，含 enabled 等字段 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/api/ai-config/global" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "保存成功",
  "data": null
}
```

---

### 更新 AI 功能状态

**请求方式**：PUT
**请求路径**：`/api/ai-config/functions/{key}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| key | Path | String | 是 | 功能标识 |
| (Body) | Body | Object | 是 | FunctionStatusDTO 对象，含 enabled 字段 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/api/ai-config/functions/medical-expand" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "更新成功",
  "data": null
}
```

---

## 7. AiModelProviderController

### 获取当前模型提供商配置

**请求方式**：GET
**请求路径**：`/api/model-providers`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/model-providers" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "provider": "openai", "apiKey": "sk-ab****cd", "model": "gpt-4" }
}
```

---

### 保存模型提供商配置

**请求方式**：POST
**请求路径**：`/api/model-providers`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | AiModelProvider 对象，含 provider/apiKey/model/baseUrl 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/api/model-providers" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "openai",
    "apiKey": "sk-xxxxxxxx",
    "model": "gpt-4",
    "baseUrl": "https://api.openai.com"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "provider": "openai", "apiKey": "sk-ab****cd", "model": "gpt-4" }
}
```

---

### 测试模型连接

**请求方式**：POST
**请求路径**：`/api/model-providers/test`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | AiModelProvider 对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/api/model-providers/test" \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "openai",
    "apiKey": "sk-xxxxxxxx",
    "model": "gpt-4",
    "baseUrl": "https://api.openai.com"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "连接成功"
}
```

---

### 删除模型提供商配置

**请求方式**：DELETE
**请求路径**：`/api/model-providers/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 配置ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/api/model-providers/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": null
}
```

---

## 8. MedicalRecordAIController

### 获取病历 AI 扩写配置

**请求方式**：GET
**请求路径**：`/api/ai-config/medical-record`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/ai-config/medical-record" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "enabled": true, "promptTemplate": "...", "model": "gpt-4" }
}
```

---

### 保存病历 AI 扩写配置

**请求方式**：PUT
**请求路径**：`/api/ai-config/medical-record`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | MedicalRecordAIConfigDTO 对象，含 enabled/promptTemplate/model 等字段 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/api/ai-config/medical-record" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "promptTemplate": "请根据以下病历信息扩写...",
    "model": "gpt-4"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "保存成功",
  "data": null
}
```

---

### 病历 AI 扩写

**请求方式**：POST
**请求路径**：`/api/ai/medical-record/expand`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | TreatmentSceneExpandRequest 对象，含 sceneId/stepId/content 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/api/ai/medical-record/expand" \
  -H "Content-Type: application/json" \
  -d '{
    "sceneId": 1,
    "stepId": 2,
    "content": "患者主诉牙痛"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "expanded": "患者主诉右下后牙持续性疼痛..." }
}
```

---

### 预览病历扩写 Prompt

**请求方式**：POST
**请求路径**：`/api/ai-config/medical-record/preview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | TreatmentSceneExpandRequest 对象 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/api/ai-config/medical-record/preview" \
  -H "Content-Type: application/json" \
  -d '{
    "sceneId": 1,
    "stepId": 2,
    "content": "患者主诉牙痛"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "请根据以下病历信息扩写：..."
}
```

---

## 9. BusinessDailyAnalysisController

### 获取最新经营分析

**请求方式**：GET
**请求路径**：`/business-analysis/latest`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/latest" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "analysisDate": "2025-01-01", "content": "今日业绩分析..." }
}
```

---

### 获取历史经营分析列表

**请求方式**：GET
**请求路径**：`/business-analysis/history`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| limit | Query | Integer | 否 | 返回条数，默认 20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/history?limit=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "analysisDate": "2025-01-01", "content": "今日业绩分析..." }]
}
```

---

### 测试模型连接

**请求方式**：GET
**请求路径**：`/business-analysis/probe`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/probe" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": "连接正常"
}
```

---

### 获取或创建聊天会话

**请求方式**：GET
**请求路径**：`/business-analysis/chat/session`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| accountId | Query | Long | 否 | 账号ID |
| accountName | Query | String | 否 | 账号名称 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/chat/session?accountId=1&accountName=管理员" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "sessionId": "sess-1", "messages": [] }
}
```

---

### 获取聊天记忆（已移除，返回空文档）

**请求方式**：GET
**请求路径**：`/business-analysis/chat/memory`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| accountId | Query | Long | 否 | 账号ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/chat/memory?accountId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "has_memory": false, "content": "", "updated_at": "" }
}
```

---

### 发送聊天消息

**请求方式**：POST
**请求路径**：`/business-analysis/chat/message`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | BusinessAnalysisChatRequest 对象，含 sessionId/message 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/business-analysis/chat/message" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "sess-1",
    "message": "分析本周业绩"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "reply": "本周业绩总体平稳..." }
}
```

---

### 流式发送聊天消息（SSE）

**请求方式**：POST
**请求路径**：`/business-analysis/chat/stream`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | BusinessAnalysisChatRequest 对象 |
| agentKey | Query | String | 否 | 代理标识，默认 default |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/business-analysis/chat/stream" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "sessionId": "sess-1",
    "message": "分析本周业绩"
  }'
```

**响应示例**：
```
data: {"chunk":"本周业绩"}

data: {"chunk":"总体平稳"}

event: complete
data: [DONE]
```

---

### 获取最新周报

**请求方式**：GET
**请求路径**：`/business-analysis/weekly/latest`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/weekly/latest" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "weekStart": "2025-01-01", "content": "本周业绩..." }
}
```

---

### 手动触发周报生成

**请求方式**：POST
**请求路径**：`/business-analysis/weekly/run`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/business-analysis/weekly/run?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "taskId": "task-1", "status": "SUBMITTED" }
}
```

---

### 查询周报任务状态

**请求方式**：GET
**请求路径**：`/business-analysis/weekly/run/status`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/weekly/run/status?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "taskId": "task-1", "status": "COMPLETED" }
}
```

---

### 获取最新月报

**请求方式**：GET
**请求路径**：`/business-analysis/monthly/latest`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/monthly/latest" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "month": "2025-01", "content": "本月业绩..." }
}
```

---

### 手动触发月报生成

**请求方式**：POST
**请求路径**：`/business-analysis/monthly/run`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/business-analysis/monthly/run?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "taskId": "task-2", "status": "SUBMITTED" }
}
```

---

### 查询月报任务状态

**请求方式**：GET
**请求路径**：`/business-analysis/monthly/run/status`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/monthly/run/status?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "taskId": "task-2", "status": "COMPLETED" }
}
```

---

### 获取近期告警

**请求方式**：GET
**请求路径**：`/business-analysis/alerts/recent`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| limit | Query | Integer | 否 | 返回条数，默认 20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/alerts/recent?limit=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "alertType": "业绩下滑", "content": "...", "alertDate": "2025-01-01" }]
}
```

---

### 手动触发告警扫描

**请求方式**：POST
**请求路径**：`/business-analysis/alerts/scan`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/business-analysis/alerts/scan?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "scanDate": "2025-01-01", "alertCount": 3 }
}
```

---

### 获取经营分析详情

**请求方式**：GET
**请求路径**：`/business-analysis/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 分析记录ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "analysisDate": "2025-01-01", "content": "今日业绩分析..." }
}
```

---

### 手动触发日报生成

**请求方式**：POST
**请求路径**：`/business-analysis/run`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/business-analysis/run?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "taskId": "task-0", "status": "SUBMITTED" }
}
```

---

### 查询日报任务状态

**请求方式**：GET
**请求路径**：`/business-analysis/run/status`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| date | Query | String | 否 | 目标日期 yyyy-MM-dd |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/business-analysis/run/status?date=2025-01-01" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "taskId": "task-0", "status": "COMPLETED" }
}
```

---

## 10. ApiDocumentationController

### 获取 API 文档

**请求方式**：GET
**请求路径**：`/api/docs`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/docs" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "title": "口腔门诊 SaaS 系统业务 API 文档",
    "version": "1.0.0",
    "baseUrl": "/",
    "apis": [...]
  }
}
```

---

## 11. WechatMpCallbackController

### 微信公众号服务器验证

**请求方式**：GET
**请求路径**：`/wechat/mp`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| signature | Query | String | 是 | 微信签名 |
| timestamp | Query | String | 是 | 时间戳 |
| nonce | Query | String | 是 | 随机数 |
| echostr | Query | String | 是 | 随机字符串 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/mp?signature=xxx&timestamp=1234567890&nonce=abc&echostr=hello" \
  -H "Content-Type: application/json"
```

**响应示例**：
```
hello
```

---

### 接收微信公众号消息/事件

**请求方式**：POST
**请求路径**：`/wechat/mp`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| signature | Query | String | 是 | 微信签名 |
| timestamp | Query | String | 是 | 时间戳 |
| nonce | Query | String | 是 | 随机数 |
| (Body) | Body | String | 是 | XML 消息体 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/wechat/mp?signature=xxx&timestamp=1234567890&nonce=abc" \
  -H "Content-Type: application/xml" \
  -d '<xml><ToUserName><![CDATA[gh_xxx]]></ToUserName><FromUserName><![CDATA[openid_xxx]]></FromUserName><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[subscribe]]></Event></xml>'
```

**响应示例**：
```xml
<xml>
  <ToUserName><![CDATA[openid_xxx]]></ToUserName>
  <FromUserName><![CDATA[gh_xxx]]></FromUserName>
  <CreateTime>1234567890</CreateTime>
  <MsgType><![CDATA[text]]></MsgType>
  <Content><![CDATA[已完成公众号绑定...]]></Content>
</xml>
```

---

## 12. WechatPortalController

### 患者微信门户入口

**请求方式**：GET
**请求路径**：`/wechat/portal`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| openid | Query | String | 是 | 微信用户OpenID |
| scene | Query | String | 是 | 场景标识 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/portal?openid=openid_xxx&scene=bind_1" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到患者门户首页或错误页

---

## 13. WechatBindController

### 启动微信绑定流程

**请求方式**：GET
**请求路径**：`/wechat/bind/start`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 是 | 患者ID |
| returnUrl | Query | String | 否 | 绑定成功后回跳地址 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/bind/start?patientId=1&returnUrl=/patient/home" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到微信授权页

---

### 微信绑定回调

**请求方式**：GET
**请求路径**：`/wechat/bind/callback`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| code | Query | String | 是 | 微信授权码 |
| state | Query | String | 是 | 状态参数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/bind/callback?code=authcode_xxx&state=state_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到绑定成功页

---

### 生成二维码

**请求方式**：GET
**请求路径**：`/wechat/bind/qrcode`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| text | Query | String | 是 | 二维码内容 |
| size | Query | Integer | 否 | 图片尺寸，默认 220，范围 120-512 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/bind/qrcode?text=https://example.com&size=220" \
  -H "Content-Type: application/json" \
  --output qrcode.png
```

**响应示例**：PNG 图片字节流

---

## 14. WechatMenuController

### 获取当前公众号菜单

**请求方式**：GET
**请求路径**：`/wechat/menu/current`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/menu/current" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "menu": { "button": [...] } }
}
```

---

### 预览默认菜单配置

**请求方式**：GET
**请求路径**：`/wechat/menu/preview`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/wechat/menu/preview" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "menu": { "button": [...] } }
}
```

---

### 发布默认公众号菜单

**请求方式**：POST
**请求路径**：`/wechat/menu/publish`

**请求参数**：无

**请求示例**：
```bash
curl -X POST "http://localhost:8080/wechat/menu/publish" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": true
}
```

---

## 15. StaffPortalController

### 员工门户入口

**请求方式**：GET
**请求路径**：`/staff-portal/entry`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/staff-portal/entry" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到微信授权页

---

### 员工门户回调

**请求方式**：GET
**请求路径**：`/staff-portal/callback`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| code | Query | String | 是 | 微信授权码 |
| state | Query | String | 是 | 状态参数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/staff-portal/callback?code=authcode_xxx&state=state_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到员工首页或绑定页

---

### 员工账号绑定微信

**请求方式**：POST
**请求路径**：`/staff-portal/bind`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| token | Query | String | 是 | 绑定令牌 |
| username | Body | String | 是 | 员工账号 |
| password | Body | String | 是 | 密码 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/staff-portal/bind?token=bindtoken_xxx" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "bound": true,
    "accountId": 1,
    "redirectUrl": "/staff-h5/home?accountId=1"
  }
}
```

---

### 员工门户概览

**请求方式**：GET
**请求路径**：`/staff-portal/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| accountId | Query | Long | 是 | 员工账号ID |
| staffToken | Query | String | 是 | 员工身份令牌 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/staff-portal/overview?accountId=1&staffToken=token_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "account": { "id": 1, "name": "张三", "role": "医生" },
    "summary": { "displayName": "张三", "roleLabel": "医生", "wechatBound": true },
    "quickActions": {
      "appointments": "/staff-h5/appointments?accountId=1",
      "consultations": "/staff-h5/consultations?accountId=1",
      "patients": "/staff-h5/patients?accountId=1",
      "patient360": "/staff-h5/patient360?accountId=1"
    }
  }
}
```

---

## 16. PatientPortalController

### 患者门户入口

**请求方式**：GET
**请求路径**：`/patient-portal/entry`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-portal/entry" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到微信授权页

---

### 患者门户回调

**请求方式**：GET
**请求路径**：`/patient-portal/callback`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| code | Query | String | 是 | 微信授权码 |
| state | Query | String | 是 | 状态参数 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-portal/callback?code=authcode_xxx&state=state_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：HTTP 302 重定向到患者门户首页或错误页

---

### 患者门户概览

**请求方式**：GET
**请求路径**：`/patient-portal/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 否 | 患者ID |
| portalToken | Query | String | 是 | 患者身份令牌 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-portal/overview?patientId=1&portalToken=token_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "patient": { "id": 1, "name": "张三" },
    "appointments": [...],
    "records": [...],
    "images": [...],
    "consents": [...]
  }
}
```

---

### 查看知情同意书详情

**请求方式**：GET
**请求路径**：`/patient-portal/consents/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 同意书ID |
| portalToken | Query | String | 是 | 患者身份令牌 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/patient-portal/consents/1?portalToken=token_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "title": "种植牙知情同意书", "content": "...", "read": true }
}
```

---

### 签署知情同意书

**请求方式**：POST
**请求路径**：`/patient-portal/consents/{id}/sign`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 同意书ID |
| portalToken | Query | String | 是 | 患者身份令牌 |
| (Body) | Body | Object | 是 | PatientConsentSignRequest 对象，含 signName/signDate 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-portal/consents/1/sign?portalToken=token_xxx" \
  -H "Content-Type: application/json" \
  -d '{
    "signName": "张三",
    "signDate": "2025-01-01"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "signed": true, "signName": "张三" }
}
```

---

### 取消预约

**请求方式**：POST
**请求路径**：`/patient-portal/appointments/{id}/cancel`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 预约ID |
| portalToken | Query | String | 是 | 患者身份令牌 |
| reason | Body | String | 否 | 取消原因 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/patient-portal/appointments/1/cancel?portalToken=token_xxx" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "临时有事"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "status": "已取消" }
}
```

---

### 编辑预约

**请求方式**：PUT
**请求路径**：`/patient-portal/appointments/{id}/edit`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 预约ID |
| portalToken | Query | String | 是 | 患者身份令牌 |
| (Body) | Body | Object | 是 | Appointment 对象，含 appointment_date/appointment_time/appointment_purpose 等字段 |

**请求示例**：
```bash
curl -X PUT "http://localhost:8080/patient-portal/appointments/1/edit?portalToken=token_xxx" \
  -H "Content-Type: application/json" \
  -d '{
    "appointment_date": "2025-01-10",
    "appointment_time": "14:00",
    "appointment_purpose": "复诊"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "appointment_date": "2025-01-10", "appointment_time": "14:00" }
}
```

---

## 17. AdminReportPortalController

### 管理员报表门户概览

**请求方式**：GET
**请求路径**：`/admin-report-portal/overview`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| accountId | Query | Long | 是 | 管理员账号ID |
| reportToken | Query | String | 是 | 报表访问令牌 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/admin-report-portal/overview?accountId=1&reportToken=token_xxx" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "account": { "id": 1, "name": "管理员" },
    "summary": { "displayName": "管理员", "roleLabel": "管理员", "wechatBound": true },
    "latestDaily": { "id": 1, "analysisDate": "2025-01-01", "content": "..." },
    "latestWeekly": { "id": 1, "weekStart": "2025-01-01", "content": "..." },
    "latestMonthly": { "id": 1, "month": "2025-01", "content": "..." },
    "recentAlerts": [{ "id": 1, "alertType": "业绩下滑", "content": "..." }]
  }
}
```

---

## 18. FileTransferController

### 文件传输首页（HTML）

**请求方式**：GET
**请求路径**：`/file-transfer`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| uploadStatus | Query | String | 否 | 上传状态（success/error） |
| uploadMessage | Query | String | 否 | 上传状态消息 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/file-transfer" \
  -H "Content-Type: application/json"
```

**响应示例**：HTML 页面

---

### 上传文件

**请求方式**：POST
**请求路径**：`/file-transfer/upload`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| files | Body | MultipartFile[] | 是 | 文件列表 |
| note | Body | String | 否 | 备注 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/file-transfer/upload" \
  -F "files=@document.pdf" \
  -F "note=项目资料"
```

**响应示例**：HTTP 302 重定向到首页并带状态参数

---

### 下载文件

**请求方式**：GET
**请求路径**：`/file-transfer/files/{filename}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| filename | Path | String | 是 | 文件名 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/file-transfer/files/project-source-latest.zip" \
  -H "Content-Type: application/json" \
  --output project.zip
```

**响应示例**：文件字节流

---

## 19. InsuranceController

### 获取保险概览

**请求方式**：GET
**请求路径**：`/insurance/overview`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/insurance/overview" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "totalPolicies": 100, "activePolicies": 80, "totalAmount": 500000 }
}
```

---

### 获取保险配置

**请求方式**：GET
**请求路径**：`/insurance/config`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/insurance/config" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "enabled": true, "providers": [...] }
}
```

---

### 保存保险配置

**请求方式**：POST
**请求路径**：`/insurance/config`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | InsuranceConfig 对象，含 enabled/providers 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/insurance/config" \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": true,
    "providers": [{"name": "平安保险", "code": "pingan"}]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "enabled": true, "providers": [...] }
}
```

---

### 获取患者保险档案

**请求方式**：GET
**请求路径**：`/insurance/patient-profile/{patientId}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Path | Long | 是 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/insurance/patient-profile/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "patientId": 1, "insuranceType": "医保", "policyNo": "1234567890" }
}
```

---

### 保存患者保险档案

**请求方式**：POST
**请求路径**：`/insurance/patient-profile`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | InsurancePatientProfile 对象，含 patientId/insuranceType/policyNo 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/insurance/patient-profile" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "insuranceType": "医保",
    "policyNo": "1234567890"
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "patientId": 1, "insuranceType": "医保", "policyNo": "1234567890" }
}
```

---

### 获取结算记录

**请求方式**：GET
**请求路径**：`/insurance/settlements`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Query | Long | 否 | 患者ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/insurance/settlements?patientId=1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "patientId": 1, "amount": 2000, "status": "已结算" }]
}
```

---

### 创建结算草稿

**请求方式**：POST
**请求路径**：`/insurance/settlements/draft`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| (Body) | Body | Object | 是 | InsuranceSettlement 对象，含 patientId/amount/items 等字段 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/insurance/settlements/draft" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "amount": 2000,
    "items": [{"name": "补牙", "amount": 500}]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "patientId": 1, "amount": 2000, "status": "草稿" }
}
```

---

### 获取近期日志

**请求方式**：GET
**请求路径**：`/insurance/logs`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| limit | Query | Integer | 否 | 返回条数，默认 20 |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/insurance/logs?limit=20" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "action": "创建结算", "operator": "admin", "time": "2025-01-01 10:00" }]
}
```

---

### 构建模拟结算报文

**请求方式**：POST
**请求路径**：`/insurance/mock/settlement-payload`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| patientId | Body | Long | 否 | 患者ID |
| treatmentCatalogId | Body | Long | 否 | 治疗目录ID |
| totalAmount | Body | Double | 否 | 总金额 |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/insurance/mock/settlement-payload" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "treatmentCatalogId": 2,
    "totalAmount": 2000
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "patientId": 1, "items": [...], "totalAmount": 2000 }
}
```

---

## 20. TreatmentSceneController

### 查询治疗场景列表

**请求方式**：GET
**请求路径**：`/api/treatment-scenes`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/treatment-scenes" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "name": "种植牙", "category": "修复", "enabled": true }]
}
```

---

### 查询启用的治疗场景列表

**请求方式**：GET
**请求路径**：`/api/treatment-scenes/enabled`

**请求参数**：无

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/treatment-scenes/enabled" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "name": "种植牙", "category": "修复", "enabled": true }]
}
```

---

### 查询治疗场景详情

**请求方式**：GET
**请求路径**：`/api/treatment-scenes/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 场景ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/treatment-scenes/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "name": "种植牙", "category": "修复", "steps": [...] }
}
```

---

### 保存治疗场景

**请求方式**：POST
**请求路径**：`/api/treatment-scenes`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Body | Long | 否 | 场景ID（更新时传入） |
| name | Body | String | 是 | 场景名称 |
| category | Body | String | 否 | 分类，默认"其他" |
| level | Body | Integer | 否 | 层级，默认 1 |
| enabled | Body | Boolean | 否 | 是否启用，默认 true |
| sortOrder | Body | Integer | 否 | 排序，默认 0 |
| steps | Body | Array | 否 | 步骤列表，每项含 name/sortOrder/forbiddenKeywords/requiredKeywords/enabled |

**请求示例**：
```bash
curl -X POST "http://localhost:8080/api/treatment-scenes" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "种植牙",
    "category": "修复",
    "level": 1,
    "enabled": true,
    "sortOrder": 0,
    "steps": [
      {
        "name": "术前检查",
        "sortOrder": 0,
        "forbiddenKeywords": "",
        "requiredKeywords": "CT",
        "enabled": true
      }
    ]
  }'
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": { "id": 1, "name": "种植牙", "category": "修复" }
}
```

---

### 删除治疗场景

**请求方式**：DELETE
**请求路径**：`/api/treatment-scenes/{id}`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 场景ID |

**请求示例**：
```bash
curl -X DELETE "http://localhost:8080/api/treatment-scenes/1" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "删除成功",
  "data": null
}
```

---

### 查询场景步骤列表

**请求方式**：GET
**请求路径**：`/api/treatment-scenes/{id}/steps`

**请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 |
|--------|------|------|------|------|
| id | Path | Long | 是 | 场景ID |

**请求示例**：
```bash
curl -X GET "http://localhost:8080/api/treatment-scenes/1/steps" \
  -H "Content-Type: application/json"
```

**响应示例**：
```json
{
  "code": "200",
  "msg": "操作成功",
  "data": [{ "id": 1, "name": "术前检查", "sortOrder": 0, "enabled": true }]
}
```

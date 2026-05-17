#!/bin/bash
# TC-AI-06: 患者 360 AI 助手测试
# 验证患者上下文带入、弹窗状态

echo "=== TC-AI-06: 患者 360 AI 助手 ==="
echo "步骤1: 查询患者360数据"
curl -s "http://localhost:8080/patient360/overview/3" \
  -b /tmp/cookies.txt | jq '.data.patient.name'

echo "步骤2: 发送带患者上下文的 AI 请求"
curl -s -N -X POST http://localhost:8080/api/ai/proxy/default \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -b /tmp/cookies.txt \
  -d '{
    "message": "[患者上下文] 姓名：李四，性别：女，年龄：28。问题：请分析该患者",
    "account_id": 1,
    "account_name": "管理员",
    "session_id": "patient-test-001",
    "functionKey": "patient-insight"
  }' | tail -n 5

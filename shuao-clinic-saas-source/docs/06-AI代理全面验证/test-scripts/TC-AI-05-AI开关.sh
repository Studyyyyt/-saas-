#!/bin/bash
# TC-AI-05: AI 功能开关控制测试
# 验证关闭后前端隐藏、后端返回提示

echo "=== TC-AI-05: AI 功能开关控制 ==="
echo "步骤1: 查询当前配置"
curl -s "http://localhost:8080/api/ai-config/overview" \
  -b /tmp/cookies.txt | jq '.data.globalEnabled'

echo "步骤2: 关闭全局 AI 开关"
curl -s -X PUT "http://localhost:8080/api/ai-config/global" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{"globalEnabled":false}' | jq '.code'

echo "步骤3: 验证调用被阻止"
curl -s -X POST http://localhost:8080/api/ai/medical-record/expand \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{"fields":{"chiefComplaint":"牙痛"}}' | jq '.msg'

echo "步骤4: 恢复全局开关"
curl -s -X PUT "http://localhost:8080/api/ai-config/global" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{"globalEnabled":true}' | jq '.code'

echo "步骤5: 关闭单个功能（病历扩写）"
curl -s -X PUT "http://localhost:8080/api/ai-config/functions/medical-expand" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{"enabled":false}' | jq '.code'

echo "步骤6: 验证单个功能被阻止"
curl -s -X POST http://localhost:8080/api/ai/medical-record/expand \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{"fields":{"chiefComplaint":"牙痛"}}' | jq '.msg'

echo "步骤7: 恢复单个功能"
curl -s -X PUT "http://localhost:8080/api/ai-config/functions/medical-expand" \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{"enabled":true}' | jq '.code'

#!/bin/bash
# TC-AI-04: Webhook 代理测试
# 验证企业微信机器人收到消息

echo "=== TC-AI-04: Webhook 通知测试 ==="
echo "步骤1: 发送文本消息"
curl -s -X POST http://localhost:8080/api/webhook/notify \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{
    "type": "text",
    "content": "AI端到端测试 TC-AI-04 验证消息"
  }' | jq .

echo "步骤2: 发送 AI 任务状态通知"
curl -s -X POST http://localhost:8080/api/webhook/ai-task \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{
    "taskType": "经营日报生成",
    "status": "SUCCESS",
    "message": "日报生成完成，运营评分 85 分",
    "durationMs": 3200
  }' | jq .

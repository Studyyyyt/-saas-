#!/bin/bash
# TC-AI-02: 首页 AI 多轮对话测试
# 验证 SSE 流式输出、Tab 切换隔离

echo "=== TC-AI-02: SSE 流式对话 ==="
echo "步骤1: Session A 发送消息"
curl -s -N -X POST http://localhost:8080/api/ai/proxy/default \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -b /tmp/cookies.txt \
  -d '{
    "message": "今天门诊收入多少",
    "account_id": 1,
    "account_name": "管理员",
    "session_id": "session-a"
  }' | head -n 10

echo "步骤2: Session B 发送消息（验证隔离）"
curl -s -N -X POST http://localhost:8080/api/ai/proxy/default \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -b /tmp/cookies.txt \
  -d '{
    "message": "明天有哪些预约",
    "account_id": 1,
    "account_name": "管理员",
    "session_id": "session-b"
  }' | head -n 10

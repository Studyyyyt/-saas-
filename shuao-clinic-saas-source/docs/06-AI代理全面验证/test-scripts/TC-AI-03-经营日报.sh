#!/bin/bash
# TC-AI-03: 经营分析日报生成测试
# 验证异步任务状态通知、日报渲染

echo "=== TC-AI-03: 经营分析日报生成 ==="
echo "步骤1: 提交日报任务"
curl -s -X POST "http://localhost:8080/business-analysis/run" \
  -b /tmp/cookies.txt | jq '.data.task_status'

echo "步骤2: 查询任务状态（约5秒后）"
sleep 5
curl -s "http://localhost:8080/business-analysis/run/status" \
  -b /tmp/cookies.txt | jq '.data.task_status'

echo "步骤3: 查看最新日报"
curl -s "http://localhost:8080/business-analysis/latest" \
  -b /tmp/cookies.txt | jq '.data.analysis_status'

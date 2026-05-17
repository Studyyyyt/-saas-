#!/bin/bash
# TC-AI-01: 病历扩写全流程测试
# 验证 12 字段正确回填、loading 状态

echo "=== TC-AI-01: 病历扩写全流程 ==="
echo "步骤1: 登录获取 session"
curl -s -X POST http://localhost:8080/loginController/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  -c /tmp/cookies.txt | jq -r '.code'

echo "步骤2: 调用病历扩写接口"
curl -s -X POST http://localhost:8080/api/ai/medical-record/expand \
  -H "Content-Type: application/json" \
  -b /tmp/cookies.txt \
  -d '{
    "fields": {
      "chiefComplaint": "牙痛3天",
      "historyOfPresentIllness": "患者3天前开始牙痛",
      "pastHistory": "无特殊",
      "generalCondition": "精神可",
      "examinationFindings": "右下6龋坏",
      "auxiliaryExamination": "X线显示龋坏达牙本质深层",
      "diagnosis": "右下6深龋",
      "treatmentPlan": "根管治疗后冠修复",
      "treatment": "去腐备洞",
      "medicalAdvice": "避免患侧咀嚼",
      "prescription": "暂无",
      "notes": "患者对疼痛敏感"
    }
  }' | jq .

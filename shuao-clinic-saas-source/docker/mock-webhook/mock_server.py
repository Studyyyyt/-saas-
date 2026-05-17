# AI 病历扩写测试 Mock Server
# 接收 webhook 请求并返回固定 JSON，用于测试前端自动回填

import json
import sys
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# 禁用 Flask 默认的请求日志，只保留我们的自定义日志
import logging
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)

# 带颜色的打印辅助函数
def cyan(text):
    return f"\033[96m{text}\033[0m"

def green(text):
    return f"\033[92m{text}\033[0m"

def yellow(text):
    return f"\033[93m{text}\033[0m"

def print_banner(title, color=cyan):
    print("")
    print(color("=" * 70))
    print(color(f"  {title}"))
    print(color("=" * 70))

def print_json(label, data, color_fn=cyan):
    print("")
    print(color_fn(f"【{label}】"))
    print(color_fn("-" * 70))
    try:
        print(json.dumps(data, ensure_ascii=False, indent=2))
    except Exception:
        print(data)
    print(color_fn("-" * 70))

@app.route("/ai-test", methods=["POST", "OPTIONS"])
def ai_test():
    if request.method == "OPTIONS":
        return jsonify({}), 200

    payload = request.get_json(silent=True) or {}

    # 打印请求详情
    print_banner("收到 AI Webhook 请求", cyan)
    print(f"  请求方法: {request.method}")
    print(f"  请求路径: {request.path}")
    print(f"  来源IP:   {request.remote_addr}")
    print(f"  Content-Type: {request.content_type}")

    print_json("请求体 (Request Body)", payload, cyan)

    # 模拟 AI 病历扩写返回数据
    # 字段名必须与前端 MedicalRecordView.vue 中 expandWithAI() 方法的回填字段名保持一致
    # 支持的完整字段列表（2026-05-16 更新）：
    #   chief_complaint, present_illness_history, past_medical_history,
    #   infectious_history, allergy_history, general_condition,
    #   examination_findings, auxiliary_examination, diagnosis,
    #   treatment_plan, treatment, medical_advice, prescription,
    #   record_tags, image_summary, notes
    response = {
        "code": "200",
        "msg": "success",
        "data": {
            "chief_complaint": "患者因右下后牙持续性疼痛3天就诊，疼痛为钝痛，冷热刺激加重，夜间平卧时疼痛明显加剧。",
            "present_illness_history": "3天前无明显诱因出现右下后牙疼痛，呈持续性钝痛。进食冷热食物时疼痛加重，夜间平卧时疼痛尤为明显，影响睡眠。自行服用布洛芬后症状稍有缓解但不明显。无放射痛，无张口受限，无面部肿胀。",
            "past_medical_history": "否认高血压、糖尿病、心脏病、肝炎、结核等系统性疾病史。否认传染病史。",
            "infectious_history": "否认乙肝、丙肝、梅毒、艾滋病等传染病史。无结核接触史。",
            "allergy_history": "否认药物过敏史及食物过敏史。",
            "general_condition": "患者神志清楚，精神尚可，面色正常，发育正常，营养中等。",
            "examination_findings": "46牙颌面可见深龋洞，探诊敏感，龋坏组织软，去腐后近髓。叩诊(+)，松动度(-)。牙龈未见明显红肿，无窦道。冷诊敏感，热诊疼痛加剧。",
            "auxiliary_examination": "口腔全景片示：46牙龋坏达牙本质深层，近髓角，根尖周未见明显低密度影像，牙周膜间隙正常。",
            "diagnosis": "1. 46牙慢性牙髓炎急性发作；2. 46牙深龋",
            "treatment_plan": "1. 46牙根管治疗（约需2-3次就诊）；2. 根管治疗完成后行全冠修复保护患牙；3. 口腔卫生指导，建议定期复查。",
            "treatment": "局麻下开髓，揭顶，拔髓，根管预备至35#，3%次氯酸钠冲洗，纸尖干燥，封氢氧化钙。嘱一周后复诊。",
            "medical_advice": "1. 避免患侧咀嚼硬物；2. 注意口腔卫生，饭后漱口；3. 如有肿痛加剧请及时就诊；4. 按时复诊完成根管治疗。",
            "prescription": "甲硝唑片 0.2g × 24片，口服，每次0.2g，每日3次，饭后服用。布洛芬缓释胶囊 0.3g × 10粒，必要时口服。",
            "record_tags": "牙髓炎, 根管治疗, 深龋",
            "image_summary": "全景片显示46牙深龋近髓，根尖周未见明显异常。",
            "apple": "已向患者详细交代病情、治疗方案、预期效果及费用，患者表示理解并知情同意。下次预约根管预备治疗。"
        }
    }

    print_json("响应体 (Response Body)", response, green)
    print_banner("请求处理完毕，响应已发送", green)
    print("")

    return jsonify(response)


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


if __name__ == "__main__":
    print("")
    print(yellow("=" * 70))
    print(yellow("  Mock Webhook Server 启动中..."))
    print(yellow("  监听地址: http://0.0.0.0:9000"))
    print(yellow("  测试端点: POST http://0.0.0.0:9000/ai-test"))
    print(yellow("  健康检查: GET  http://0.0.0.0:9000/health"))
    print(yellow("=" * 70))
    print("")
    app.run(host="0.0.0.0", port=9000, debug=False)

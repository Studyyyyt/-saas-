import asyncio
import json
from datetime import datetime
from mcp.client.sse import sse_client
from mcp import ClientSession

OUT_PATH = "/Users/cherry/Downloads/口腔saas管理系统开发/docs/MCP_TOOL_TEST_RESULTS.md"

lines = [
    "# MCP Data Server 工具调用测试报告",
    "",
    f"**测试时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
    "**MCP 服务器**: http://127.0.0.1:8000/sse",
    "**Java 后端**: http://localhost:8080",
    "",
    "---",
    "",
]

async def run():
    async with sse_client("http://127.0.0.1:8000/sse") as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()

            tools = [
                ("health_check", {}, "健康检查"),
                ("get_doctor_list", {}, "医生列表与排班"),
                ("get_treatment_catalog", {}, "治疗项目目录"),
                ("get_business_report", {}, "经营分析报表（最新日报）"),
                ("get_consultation_dashboard", {}, "咨询统计看板"),
                ("get_lab_statistics", {}, "义齿加工统计概览"),
                ("search_patients", {}, "患者搜索（默认前10条）"),
                ("get_patient_detail", {"patient_id": 13}, "患者详情（id=13）"),
                ("get_patient_medical_records", {"patient_id": 13}, "患者病历（id=13）"),
                ("get_patient_appointments", {"patient_id": 13}, "患者预约（id=13）"),
                ("get_appointments", {}, "诊所预约列表"),
                ("get_consultation_records", {}, "咨询记录列表"),
                ("get_treatments", {}, "治疗处置记录"),
                ("get_finance_records", {}, "财务收支流水"),
                ("get_doctor_performance", {}, "医生业绩统计"),
                ("get_inventory", {}, "库存物品列表"),
                ("get_low_stock_alert", {}, "低库存预警"),
                ("get_lab_orders", {}, "义齿加工订单"),
                ("get_advertising_spending", {}, "广告投放花费"),
            ]

            ok = 0
            fail = 0

            for name, args, desc in tools:
                lines.append(f"## {desc}")
                lines.append("")
                lines.append(f"- **工具名**: `{name}`")
                lines.append(f"- **参数**: `{json.dumps(args, ensure_ascii=False)}`")
                lines.append("")

                try:
                    r = await session.call_tool(name, args)
                    if not r.content:
                        lines.append("- **状态**: ❌ 失败（返回空 content）")
                        fail += 1
                    else:
                        text = r.content[0].text
                        try:
                            data = json.loads(text)
                        except Exception:
                            data = text

                        if isinstance(data, dict) and "error" in data:
                            lines.append("- **状态**: ❌ 错误")
                            lines.append(f"- **错误信息**: {data['error']}")
                            fail += 1
                        else:
                            lines.append("- **状态**: ✅ 成功")
                            ok += 1
                            pretty = json.dumps(data, ensure_ascii=False, indent=2)
                            if len(pretty) > 3000:
                                pretty = pretty[:3000] + "\n... （截断，完整数据过长）"
                            lines.append("")
                            lines.append("```json")
                            lines.append(pretty)
                            lines.append("```")
                except Exception as e:
                    lines.append("- **状态**: ❌ 异常")
                    lines.append(f"- **异常信息**: `{str(e)}`")
                    fail += 1

                lines.append("")
                lines.append("---")
                lines.append("")

            lines.insert(7, f"**汇总**: {ok} 个通过 / {fail} 个失败 / 共 {len(tools)} 个工具")
            lines.insert(8, "")

            with open(OUT_PATH, "w", encoding="utf-8") as f:
                f.write("\n".join(lines))

            print(f"文档已生成: {OUT_PATH}")
            print(f"汇总: {ok} OK, {fail} FAIL")

if __name__ == "__main__":
    asyncio.run(run())

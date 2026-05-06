#!/usr/bin/env python3
import json
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta

BASE_URL = "http://127.0.0.1:8080"
MYSQL_DB = "clinic_system_new"
MYSQL_USER = "root"
MYSQL_PASSWORD = "root"
CONTACT_MARKER = "CONSREG_"


class ApiError(RuntimeError):
    pass


def request(method, path, payload=None):
    url = BASE_URL + path
    body = None
    headers = {}
    if payload is not None:
        body = json.dumps(payload).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=body, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            raw = resp.read().decode("utf-8", errors="replace")
            try:
                return json.loads(raw)
            except json.JSONDecodeError:
                return raw
    except urllib.error.HTTPError as exc:
        raw = exc.read().decode("utf-8", errors="replace")
        try:
            return json.loads(raw)
        except Exception as inner_error:
            raise ApiError(f"HTTP {exc.code} {url}: {raw}") from inner_error


def mysql_exec(sql):
    command = [
        "mysql",
        f"-u{MYSQL_USER}",
        f"-p{MYSQL_PASSWORD}",
        "-D",
        MYSQL_DB,
        "-N",
        "-B",
        "-e",
        sql,
    ]
    completed = subprocess.run(command, capture_output=True, text=True, check=True)
    return completed.stdout.strip()


def assert_true(condition, message):
    if not condition:
        raise AssertionError(message)


def assert_equal(actual, expected, message):
    if actual != expected:
        raise AssertionError(f"{message}: expected={expected!r}, actual={actual!r}")


def get_json(path):
    return request("GET", path)


def api_ok(response, message):
    assert_equal(response.get("code"), "200", message)
    return response.get("data")


def fetch_accounts():
    data = api_ok(get_json("/accounts/search?page=1&size=100"), "获取账号列表失败") or {}
    return data.get("list") or []


def fetch_active_doctors():
    data = api_ok(get_json("/accounts/doctors/active"), "获取启用医生失败") or []
    return data


def create_consultation(contact_name, phone, consultation_time, intent_level="高", handling_result="待跟进", chief_project="种植"):
    payload = {
        "consultation_time": consultation_time,
        "consultation_channel": "微信",
        "chief_project": chief_project,
        "intent_level": intent_level,
        "handling_result": handling_result,
        "contact_name": contact_name,
        "contact_phone": phone,
        "remarks": f"{CONTACT_MARKER} automated regression",
        "created_by": 1,
        "created_by_name": "系统管理员",
    }
    data = api_ok(request("POST", "/consultations/add", payload), f"新增咨询失败 phone={phone}")
    record = (data or {}).get("record") or {}
    assert_true(record.get("id"), "新增咨询未返回ID")
    return record


def match_by_phone(phone):
    return api_ok(get_json(f"/consultations/matchPatientByPhone?phone={urllib.parse.quote(phone)}"), "手机号匹配失败") or {}


def create_patient(name, phone, consultation_record_id=None):
    payload = {
        "name": name,
        "gender": "男",
        "age": 30,
        "phone": phone,
        "email": "",
        "address": "consultation regression",
        "customer_source": "微信",
        "consultation_record_id": consultation_record_id,
    }
    patient = api_ok(request("POST", "/patients/add", payload), f"新增患者失败 name={name}") or {}
    assert_true(patient.get("id"), "新增患者未返回ID")
    return patient


def create_treatment(patient, doctor, purpose, treatment_date, fee):
    payload = {
        "patient_id": patient["id"],
        "patient_name": patient["name"],
        "appointment_purpose": purpose,
        "status": "进行中",
        "doctor_account_id": doctor["id"],
        "doctor_name": doctor["name"],
        "treatment_date": treatment_date,
        "treatment_content": "咨询成交回写回归",
        "tooth_positions": "11",
        "treatment_product": "测试材料",
        "treatment_fee": str(fee),
    }
    treatment = api_ok(request("POST", "/treatments/add", payload), f"新增处置失败 patient={patient['name']}") or {}
    assert_true(treatment.get("id"), "新增处置未返回ID")
    return treatment


def charge_treatment(treatment_id, charge_date):
    finance = api_ok(request("POST", f"/treatments/charge/{treatment_id}", {
        "date": charge_date,
        "amount": 666.0,
        "remark": f"{CONTACT_MARKER} charge",
    }), f"处置收费失败 treatment_id={treatment_id}") or {}
    assert_true(finance.get("id"), "收费未返回财务ID")
    return finance


def select_consultation_by_id(consultation_id):
    return api_ok(get_json(f"/consultations/selectById?id={consultation_id}"), "按ID查询咨询失败") or {}


def select_consultations_by_patient_id(patient_id):
    return api_ok(get_json(f"/consultations/selectByPatientId?patientId={patient_id}"), "按患者查询咨询失败") or []


def search_consultations(params):
    query = urllib.parse.urlencode(params)
    data = api_ok(get_json(f"/consultations/search?{query}"), "查询咨询列表失败") or {}
    return data.get("list") or []


def dashboard_overview(start_time, end_time):
    query = urllib.parse.urlencode({
        "rangePreset": "custom",
        "startTime": start_time,
        "endTime": end_time,
    })
    return api_ok(get_json(f"/consultations/dashboard/overview?{query}"), "查询咨询看板总览失败") or {}


def dashboard_funnel(start_time, end_time):
    query = urllib.parse.urlencode({
        "rangePreset": "custom",
        "startTime": start_time,
        "endTime": end_time,
    })
    return api_ok(get_json(f"/consultations/dashboard/funnel?{query}"), "查询咨询看板漏斗失败") or {}


def delete_patient(patient_id):
    response = request("DELETE", f"/patients/delete/{patient_id}")
    assert_equal(response.get("code"), "200", f"删除患者失败 patient_id={patient_id}")


def cleanup(marker, patient_ids):
    for patient_id in patient_ids:
        if patient_id:
            try:
                delete_patient(patient_id)
            except Exception as error:
                print(f"[WARN] cleanup patient failed patient_id={patient_id}: {error}", file=sys.stderr)
    marker_sql = marker.replace("'", "''")
    mysql_exec(
        "DELETE FROM consultation_records "
        f"WHERE contact_name LIKE '{marker_sql}%';"
    )


def iso_now():
    return datetime.now()


def fmt(dt):
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def main():
    suffix = str(int(time.time()))
    marker = f"{CONTACT_MARKER}{suffix}_"
    patient_ids = []
    try:
        accounts = fetch_accounts()
        assert_true(any(str(item.get("role", "")).strip() == "admin" for item in accounts), "未找到管理员账号")
        doctors = fetch_active_doctors()
        assert_true(doctors, "未找到启用医生")
        doctor = doctors[0]

        now = iso_now()
        recent_time = fmt(now - timedelta(minutes=30))
        old_time = fmt(now - timedelta(days=8))

        prompt_name = f"{marker}PROMPT"
        prompt_phone = f"139{suffix[-8:]}"
        prompt_consultation = create_consultation(prompt_name, prompt_phone, recent_time)

        prompt_flags_before = match_by_phone(prompt_phone)
        assert_equal(prompt_flags_before.get("openConsultationCount"), 1, "首次手机号匹配未返回1条未成交咨询")
        assert_equal(prompt_flags_before.get("phoneMatchedPatient"), False, "首次手机号匹配不应命中患者")

        prompt_patient = create_patient(prompt_name, prompt_phone, prompt_consultation["id"])
        patient_ids.append(prompt_patient["id"])

        prompt_linked = select_consultation_by_id(prompt_consultation["id"])
        assert_equal(prompt_linked.get("patient_id"), prompt_patient["id"], "建档后咨询未回写患者ID")
        assert_equal(prompt_linked.get("handling_result"), "已预约到店", "建档反向关联后处理结果未变更为已预约到店")
        assert_true(bool(prompt_linked.get("arrived_at")), "建档反向关联后未写入 arrived_at")

        prompt_flags_after = match_by_phone(prompt_phone)
        assert_equal(prompt_flags_after.get("phoneMatchedPatient"), True, "建档后手机号匹配未命中患者")
        assert_equal(prompt_flags_after.get("matchedPatientId"), prompt_patient["id"], "建档后匹配患者ID不正确")
        assert_true(prompt_flags_after.get("openConsultationCount", 0) >= 1, "建档后未成交咨询提示数量异常")

        recent_filter_name = f"{marker}FILTER_RECENT"
        recent_filter_phone = f"138{suffix[-8:]}"
        recent_filter = create_consultation(recent_filter_name, recent_filter_phone, recent_time)

        old_filter_name = f"{marker}FILTER_OLD"
        old_filter_phone = f"137{suffix[-8:]}"
        old_filter = create_consultation(old_filter_name, old_filter_phone, old_time)

        range_start = fmt(now - timedelta(days=7))
        range_end = fmt(now + timedelta(minutes=5))
        quick_rows = search_consultations({
            "page": 1,
            "size": 100,
            "intentLevel": "高",
            "handlingResult": "待跟进",
            "rangePreset": "custom",
            "startTime": range_start,
            "endTime": range_end,
        })
        quick_ids = {int(item.get("id")) for item in quick_rows}
        assert_true(recent_filter["id"] in quick_ids, "7天快捷过滤未包含最近高意向待跟进记录")
        assert_true(old_filter["id"] not in quick_ids, "7天快捷过滤错误包含了8天前记录")
        assert_true(prompt_consultation["id"] not in quick_ids, "已预约到店记录不应出现在高意向待跟进快捷过滤里")

        full_rows = search_consultations({
            "page": 1,
            "size": 100,
            "intentLevel": "高",
            "handlingResult": "待跟进",
        })
        full_ids = {int(item.get("id")) for item in full_rows}
        assert_true(recent_filter["id"] in full_ids, "完整筛选未包含最近高意向待跟进记录")
        assert_true(old_filter["id"] in full_ids, "完整筛选未包含8天前高意向待跟进记录")

        dashboard_pending_name = f"{marker}DASH_PENDING"
        dashboard_pending_phone = f"136{suffix[-8:]}"
        create_consultation(dashboard_pending_name, dashboard_pending_phone, "2099-01-01 10:00:00")

        dashboard_deal_name = f"{marker}DASH_DEAL"
        dashboard_deal_phone = f"135{suffix[-8:]}"
        dashboard_deal_consultation = create_consultation(
            dashboard_deal_name,
            dashboard_deal_phone,
            "2099-01-01 11:00:00",
            intent_level="中",
            handling_result="待跟进",
            chief_project="正畸",
        )

        dashboard_patient = create_patient(dashboard_deal_name, dashboard_deal_phone, dashboard_deal_consultation["id"])
        patient_ids.append(dashboard_patient["id"])

        treatment = create_treatment(dashboard_patient, doctor, "成交回写回归", "2099-01-02", 666.0)
        charge_treatment(treatment["id"], "2099-01-02")

        patient_consultations = select_consultations_by_patient_id(dashboard_patient["id"])
        target = next((item for item in patient_consultations if int(item.get("id")) == int(dashboard_deal_consultation["id"])), None)
        assert_true(target is not None, "收费后未查到目标咨询记录")
        assert_true(str(target.get("deal_at", "")).startswith("2099-01-02"), "首次成交回写日期未按收费日期写入")
        assert_equal(float(target.get("total_deal_amount") or 0), 666.0, "咨询累计成交金额未回写收费金额")

        overview = dashboard_overview("2099-01-01 00:00:00", "2099-01-03 23:59:59")
        summary = overview.get("summary") or {}
        assert_equal(summary.get("currentConsultationCount"), 2, "看板咨询数不准确")
        assert_equal(summary.get("currentArrivedCount"), 1, "看板到店数不准确")
        assert_equal(summary.get("currentDealCount"), 1, "看板成交数不准确")
        assert_equal(summary.get("currentHighIntentPendingCount"), 1, "看板高意向待跟进数不准确")

        funnel = dashboard_funnel("2099-01-01 00:00:00", "2099-01-03 23:59:59")
        current_funnel = funnel.get("current") or {}
        assert_equal(current_funnel.get("consultation_count"), 2, "漏斗咨询数不准确")
        assert_equal(current_funnel.get("arrived_count"), 1, "漏斗到店数不准确")
        assert_equal(current_funnel.get("deal_count"), 1, "漏斗成交数不准确")

        print("[REGRESSION] consultation module regression passed")
        print(json.dumps({
            "prompt_consultation_id": prompt_consultation["id"],
            "prompt_patient_id": prompt_patient["id"],
            "recent_filter_consultation_id": recent_filter["id"],
            "old_filter_consultation_id": old_filter["id"],
            "dashboard_deal_consultation_id": dashboard_deal_consultation["id"],
            "dashboard_patient_id": dashboard_patient["id"],
            "treatment_id": treatment["id"],
        }, ensure_ascii=False))
    finally:
        cleanup(marker, patient_ids)


if __name__ == "__main__":
    main()

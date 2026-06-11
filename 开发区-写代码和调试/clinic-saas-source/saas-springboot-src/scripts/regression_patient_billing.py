#!/usr/bin/env python3
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

BASE_URL = "http://127.0.0.1:8080"


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
        except Exception as inner_error:  # pragma: no cover
            raise ApiError(f"HTTP {exc.code} {url}: {raw}") from inner_error


def assert_true(condition, message):
    if not condition:
        raise AssertionError(message)


def assert_equal(actual, expected, message):
    if actual != expected:
        raise AssertionError(f"{message}: expected={expected!r}, actual={actual!r}")


def assert_close(actual, expected, message, tol=1e-6):
    if abs(float(actual) - float(expected)) > tol:
        raise AssertionError(f"{message}: expected={expected!r}, actual={actual!r}")


def get_json(path):
    return request("GET", path)


def fetch_patients():
    response = get_json("/patients/selectAllForH5")
    assert_equal(response.get("code"), "200", "获取患者列表失败")
    return response.get("data") or []


def fetch_doctors():
    response = get_json("/accounts/doctors/active")
    assert_equal(response.get("code"), "200", "获取医生列表失败")
    return response.get("data") or []


def fetch_patient360(patient_id):
    response = get_json(f"/patient360/overview/{patient_id}")
    assert_equal(response.get("code"), "200", f"获取患者360失败 patient_id={patient_id}")
    return response.get("data") or {}


def fetch_appointments():
    response = get_json("/appointments/selectAll?page=1&size=1000")
    assert_equal(response.get("code"), "200", "获取预约列表失败")
    data = response.get("data") or {}
    return data.get("list") or []


def fetch_finances():
    response = get_json("/finances/all?page=1&size=1000")
    assert_equal(response.get("code"), "200", "获取财务列表失败")
    data = response.get("data") or {}
    return data.get("list") or []


def create_patient(name, age, phone):
    response = request("POST", "/patients/add", {
        "name": name,
        "gender": "男",
        "age": age,
        "phone": phone,
        "email": "",
        "address": "regression"
    })
    assert_equal(response.get("code"), "200", f"新增患者失败 name={name}")
    patient = response.get("data") or {}
    assert_true(patient.get("id"), "新增患者未返回ID")
    return patient


def create_appointment(patient, doctor, purpose, date_value, time_value, status="待治疗"):
    response = request("POST", "/appointments/add", {
        "patient_id": patient["id"],
        "patient_name": patient["name"],
        "appointment_date": date_value,
        "appointment_time": time_value,
        "doctor_account_id": doctor["id"],
        "doctor_name": doctor["name"],
        "appointment_purpose": purpose,
        "status": status
    })
    assert_equal(response.get("code"), "200", f"新增预约失败 patient={patient['name']}")


def edit_latest_appointment(patient_id, updater):
    appointments = [item for item in fetch_appointments() if item.get("patient_id") == patient_id]
    assert_true(appointments, f"未找到 patient_id={patient_id} 的预约")
    latest = sorted(appointments, key=lambda item: item.get("id", 0), reverse=True)[0]
    latest.update(updater)
    response = request("PUT", "/appointments/edit", latest)
    assert_equal(response.get("code"), "200", "编辑预约失败")
    return latest


def create_treatment(patient, doctor, purpose, fee, status="进行中"):
    response = request("POST", "/treatments/add", {
        "patient_id": patient["id"],
        "patient_name": patient["name"],
        "appointment_purpose": purpose,
        "status": status,
        "doctor_name": doctor["name"],
        "treatment_date": today_string(),
        "treatment_content": "回归测试处置",
        "tooth_positions": "11",
        "treatment_product": "测试材料",
        "treatment_fee": str(fee)
    })
    assert_equal(response.get("code"), "200", f"新增处置失败 patient={patient['name']}")
    treatment = response.get("data") or {}
    assert_true(treatment.get("id"), "新增处置未返回ID")
    return treatment


def charge_treatment(treatment_id, date_value, remark):
    return request("POST", f"/treatments/charge/{treatment_id}", {
        "date": date_value,
        "remark": remark
    })


def refund_treatment(treatment_id, amount, date_value, remark):
    return request("POST", f"/treatments/refund/{treatment_id}", {
        "amount": amount,
        "date": date_value,
        "remark": remark
    })


def delete_appointment(appointment_id):
    response = request("DELETE", f"/appointments/delete/{appointment_id}")
    assert_equal(response.get("code"), "200", f"删除预约失败 id={appointment_id}")


def delete_treatment(treatment_id):
    response = request("DELETE", f"/treatments/delete/{treatment_id}")
    assert_equal(response.get("code"), "200", f"删除处置失败 id={treatment_id}")


def delete_patient(patient_id):
    response = request("DELETE", f"/patients/delete/{patient_id}")
    assert_equal(response.get("code"), "200", f"删除患者失败 id={patient_id}")


def delete_finance(finance_id):
    response = request("DELETE", f"/finances/delete/{finance_id}")
    assert_true(response in ("Finance record deleted successfully!", {"code": "200", "msg": "请求成功", "data": "删除成功"}), f"删除财务失败 id={finance_id}, response={response!r}")


def today_string():
    return time.strftime("%Y-%m-%d")


def unique_name(prefix):
    return f"{prefix}{int(time.time() * 1000)}"


def find_patient_in_list(patient_id):
    return next((item for item in fetch_patients() if item.get("id") == patient_id), None)


def find_latest_appointment(patient_id):
    items = [item for item in fetch_appointments() if item.get("patient_id") == patient_id]
    assert_true(items, f"patient_id={patient_id} 预约不存在")
    return sorted(items, key=lambda item: item.get("id", 0), reverse=True)[0]


def find_treatment_in_360(patient_id, treatment_id):
    overview = fetch_patient360(patient_id)
    items = overview.get("treatments") or []
    treatment = next((item for item in items if item.get("id") == treatment_id), None)
    assert_true(treatment is not None, f"patient_id={patient_id} 在360中未找到处置 id={treatment_id}")
    return overview, treatment


def cleanup_created_entities(patient_ids):
    appointments = fetch_appointments()
    for patient_id in patient_ids:
        for item in sorted([a for a in appointments if a.get("patient_id") == patient_id], key=lambda x: x.get("id", 0), reverse=True):
            try:
                delete_appointment(item["id"])
            except Exception as error:  # pragma: no cover
                print(f"[WARN] cleanup appointment failed id={item.get('id')}: {error}", file=sys.stderr)

    for patient_id in patient_ids:
        try:
            overview = fetch_patient360(patient_id)
        except Exception as error:  # pragma: no cover
            print(f"[WARN] fetch patient360 during cleanup failed patient_id={patient_id}: {error}", file=sys.stderr)
            continue
        for item in sorted(overview.get("treatments") or [], key=lambda x: x.get("id", 0), reverse=True):
            try:
                delete_treatment(item["id"])
            except Exception as error:  # pragma: no cover
                print(f"[WARN] cleanup treatment failed id={item.get('id')}: {error}", file=sys.stderr)

    for patient_id in patient_ids:
        try:
            delete_patient(patient_id)
        except Exception as error:  # pragma: no cover
            print(f"[WARN] cleanup patient failed id={patient_id}: {error}", file=sys.stderr)

    cleanup_orphan_test_finances()


def cleanup_orphan_test_finances():
    prefixes = ("回归收费A", "同名隔离", "删除级联")
    for item in fetch_finances():
        name = str(item.get("name", ""))
        if any(name.startswith(prefix) for prefix in prefixes):
            try:
                delete_finance(item["id"])
            except Exception as error:  # pragma: no cover
                print(f"[WARN] cleanup finance failed id={item.get('id')}: {error}", file=sys.stderr)


def scenario_billing_flow(doctor):
    patient = create_patient(unique_name("回归收费A"), 31, f"13{int(time.time())%1000000000:09d}")
    patient_id = patient["id"]
    create_appointment(patient, doctor, "收费回归预约", today_string(), "10:00:00")
    edited = edit_latest_appointment(patient_id, {"appointment_purpose": "收费回归预约-已编辑"})
    assert_equal(edited.get("appointment_purpose"), "收费回归预约-已编辑", "预约编辑后的本地对象异常")

    overview = fetch_patient360(patient_id)
    assert_true(any(item.get("patient_id") == patient_id for item in (overview.get("appointments") or [])), "患者360未看到新增预约")
    assert_true(not overview.get("hasArrears"), "新增预约后不应直接欠费")

    treatment = create_treatment(patient, doctor, "收费回归处置", 300)
    treatment_id = treatment["id"]

    overview, treatment_snapshot = find_treatment_in_360(patient_id, treatment_id)
    assert_true(overview.get("hasArrears"), "新增未收费处置后患者应欠费")
    assert_close(overview.get("arrearsAmount"), 300, "新增未收费处置后的患者欠费金额错误")
    assert_equal(treatment_snapshot.get("billing_status"), "待收费", "新增处置后收费状态错误")

    patient_list_item = find_patient_in_list(patient_id)
    assert_true(patient_list_item and patient_list_item.get("has_arrears"), "患者列表未标记欠费")
    assert_close(patient_list_item.get("arrears_amount"), 300, "患者列表欠费金额错误")

    latest_appointment = find_latest_appointment(patient_id)
    assert_true(latest_appointment.get("has_arrears"), "预约列表未标记欠费")
    assert_close(latest_appointment.get("arrears_amount"), 300, "预约列表欠费金额错误")

    charge_response = charge_treatment(treatment_id, today_string(), "首次收费")
    assert_equal(charge_response.get("code"), "200", "首次收费失败")

    duplicate_charge = charge_treatment(treatment_id, today_string(), "重复收费")
    assert_true(duplicate_charge.get("code") != "200", "重复收费应该失败")

    overview, treatment_snapshot = find_treatment_in_360(patient_id, treatment_id)
    assert_true(not overview.get("hasArrears"), "已收费后不应继续欠费")
    assert_close(overview.get("arrearsAmount"), 0, "已收费后患者欠费金额错误")
    assert_equal(treatment_snapshot.get("billing_status"), "已收费", "首次收费后状态错误")

    partial_refund = refund_treatment(treatment_id, 100, today_string(), "部分退款")
    assert_equal(partial_refund.get("code"), "200", "部分退款失败")
    overview, treatment_snapshot = find_treatment_in_360(patient_id, treatment_id)
    assert_true(overview.get("hasArrears"), "部分退款后应重新出现欠费")
    assert_close(overview.get("arrearsAmount"), 100, "部分退款后欠费金额错误")
    assert_equal(treatment_snapshot.get("billing_status"), "欠费", "部分退款后收费状态应为欠费")

    blocked_recharge = charge_treatment(treatment_id, today_string(), "部分退款后再次收费")
    assert_true(blocked_recharge.get("code") != "200", "部分退款后不应允许再次收费")

    full_refund = refund_treatment(treatment_id, 200, today_string(), "全额退款收尾")
    assert_equal(full_refund.get("code"), "200", "全额退款失败")
    overview, treatment_snapshot = find_treatment_in_360(patient_id, treatment_id)
    assert_true(overview.get("hasArrears"), "全额退款后未重新标记全额欠费")
    assert_close(overview.get("arrearsAmount"), 300, "全额退款后欠费金额应恢复为治疗费用")
    assert_true(treatment_snapshot.get("can_charge"), "全额退款后应允许重新收费")

    recharge = charge_treatment(treatment_id, today_string(), "全额退款后重新收费")
    assert_equal(recharge.get("code"), "200", "全额退款后重新收费失败")
    overview, treatment_snapshot = find_treatment_in_360(patient_id, treatment_id)
    assert_true(not overview.get("hasArrears"), "重新收费后不应继续欠费")
    assert_close(overview.get("arrearsAmount"), 0, "重新收费后欠费金额错误")
    assert_equal(treatment_snapshot.get("billing_status"), "已收费", "重新收费后状态错误")

    return patient_id


def scenario_same_name_isolation(doctor):
    duplicate_name = unique_name("同名隔离")
    patient_a = create_patient(duplicate_name, 26, f"15{int(time.time())%1000000000:09d}")
    patient_b = create_patient(duplicate_name, 27, f"16{int(time.time())%1000000000:09d}")

    create_appointment(patient_a, doctor, "同名患者A预约", today_string(), "11:00:00")
    treatment = create_treatment(patient_a, doctor, "同名患者A处置", 180)
    charge_response = charge_treatment(treatment["id"], today_string(), "同名患者A收费")
    assert_equal(charge_response.get("code"), "200", "同名患者A收费失败")

    overview_a = fetch_patient360(patient_a["id"])
    overview_b = fetch_patient360(patient_b["id"])

    assert_true(any(item.get("patient_id") == patient_a["id"] for item in (overview_a.get("appointments") or [])), "患者A 360 未返回自己的预约")
    assert_true(not any(item.get("patient_id") == patient_a["id"] for item in (overview_b.get("appointments") or [])), "患者B 360 串入了患者A预约")
    assert_true(any(item.get("patient_id") == patient_a["id"] for item in (overview_a.get("treatments") or [])), "患者A 360 未返回自己的处置")
    assert_true(not any(item.get("patient_id") == patient_a["id"] for item in (overview_b.get("treatments") or [])), "患者B 360 串入了患者A处置")
    assert_true(not overview_b.get("hasArrears"), "同名患者B 不应继承患者A欠费")

    return [patient_a["id"], patient_b["id"]]


def scenario_delete_cascade(doctor):
    patient = create_patient(unique_name("删除级联"), 29, f"17{int(time.time())%1000000000:09d}")
    patient_id = patient["id"]
    create_appointment(patient, doctor, "删除级联预约", today_string(), "12:00:00")
    treatment = create_treatment(patient, doctor, "删除级联处置", 260)
    charge_response = charge_treatment(treatment["id"], today_string(), "删除级联收费")
    assert_equal(charge_response.get("code"), "200", "删除级联场景收费失败")

    delete_patient(patient_id)

    patients = fetch_patients()
    assert_true(not any(item.get("id") == patient_id for item in patients), "删除患者后患者主档仍存在")

    appointments = fetch_appointments()
    assert_true(not any(item.get("patient_id") == patient_id for item in appointments), "删除患者后预约仍存在")

    deleted_overview = get_json(f"/patient360/overview/{patient_id}")
    assert_true(deleted_overview.get("code") != "200", "删除患者后患者360仍可访问")

    finances = fetch_finances()
    assert_true(not any(item.get("patient_id") == patient_id for item in finances), "删除患者后财务流水仍存在")
    assert_true(not any(item.get("treatment_id") == treatment["id"] for item in finances), "删除患者后处置收费流水仍存在")


def main():
    doctors = fetch_doctors()
    assert_true(doctors, "系统中没有可用医生，无法执行回归")
    doctor = doctors[0]
    created_patient_ids = []
    try:
        created_patient_ids.append(scenario_billing_flow(doctor))
        created_patient_ids.extend(scenario_same_name_isolation(doctor))
        scenario_delete_cascade(doctor)
    finally:
        cleanup_created_entities(created_patient_ids)

    print("[OK] business regression passed")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"[FAIL] {error}", file=sys.stderr)
        sys.exit(1)

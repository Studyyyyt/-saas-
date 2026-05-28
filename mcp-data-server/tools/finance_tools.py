"""
治疗与财务相关 Tool 模块
提供治疗处置记录查询、财务收支流水查询及医生业绩统计功能
"""

from mcp_server import mcp
from client import java_get


@mcp.tool()
async def get_treatments(
    patient_id: int = 0,
    doctor_id: int = 0,
    start_date: str = "",
    end_date: str = "",
    page: int = 1,
    size: int = 10,
) -> list:
    """
    获取治疗处置记录列表。默认返回前10条，如需更多请指定page参数。

    参数说明：
    - patient_id: 患者ID（保留但不传给后端）
    - doctor_id: 医生ID（保留但不传给后端）
    - start_date: 开始日期（保留但不传给后端）
    - end_date: 结束日期（保留但不传给后端）
    - page: 页码，默认1
    - size: 每页条数，默认10
    """
    try:
        # 后端GET /treatments/selectAll 只接受page和size
        params = {
            "page": page,
            "size": size,
        }
        data = await java_get("/treatments/selectAll", params=params)

        # 分页接口统一处理
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        return []
    except Exception as e:
        return {"error": f"获取治疗处置记录失败: {str(e)}"}


@mcp.tool()
async def get_finance_records(
    record_type: str = "",
    start_date: str = "",
    end_date: str = "",
    page: int = 1,
    size: int = 10,
) -> list:
    """
    获取财务收支流水记录，支持按类型、日期范围筛选。默认返回前10条，如需更多请指定page参数。

    参数说明：
    - record_type: 记录类型（保留但不传给后端）
    - start_date: 开始日期，格式如 2024-01-01
    - end_date: 结束日期，格式如 2024-01-01
    - page: 页码，默认1
    - size: 每页条数，默认10
    """
    try:
        # 如果start_date和end_date相同且非空，调用selectBydate；否则调用selectAll
        if start_date and end_date and start_date == end_date:
            params = {
                "date": start_date,
                "page": page,
                "size": size,
            }
            data = await java_get("/finances/selectBydate", params=params)
        else:
            params = {
                "page": page,
                "size": size,
            }
            data = await java_get("/finances/selectAll", params=params)

        # 分页接口统一处理
        if isinstance(data, dict) and "list" in data:
            return data["list"]
        if isinstance(data, list):
            return data
        return []
    except Exception as e:
        return {"error": f"获取财务收支流水记录失败: {str(e)}"}


@mcp.tool()
async def get_doctor_performance(
    doctor_id: int = 0,
    month: str = "",
) -> dict:
    """
    获取医生业绩统计，包括接诊量、收入、退款等。

    参数说明：
    - doctor_id: 医生账号ID，默认0
    - month: 月份，格式如 2024-01。如果传入，则提取年月作为startDate(月初)和endDate(月末)
    """
    try:
        # 如果month非空，提取年月作为startDate(月初)和endDate(月末)
        start_date = ""
        end_date = ""
        if month:
            # month格式如 2024-01
            start_date = f"{month}-01"
            # 计算月末日期（简化处理，统一传月底）
            # 这里用简单字符串拼接，实际后端可能自行处理
            year, mon = month.split("-")
            # 获取该月天数
            import calendar
            last_day = calendar.monthrange(int(year), int(mon))[1]
            end_date = f"{month}-{last_day}"

        params = {
            "startDate": start_date,
            "endDate": end_date,
            "doctorAccountId": doctor_id,
            "doctorName": "",
        }
        data = await java_get("/finances/doctorPerformance", params=params)

        if isinstance(data, dict):
            return data
        if data is None:
            return {}
        return {"data": data}
    except Exception as e:
        return {"error": f"获取医生业绩统计失败: {str(e)}"}

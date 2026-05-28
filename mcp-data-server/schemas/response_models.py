"""
Pydantic 响应模型定义
用于 MCP Schema 描述及 Java 后端数据反序列化
所有字段均使用 Optional，以兼容后端数据结构差异
"""

from __future__ import annotations

from datetime import date, datetime
from typing import Generic, Optional, TypeVar

from pydantic import BaseModel, Field

T = TypeVar("T")


class PageResponse(BaseModel, Generic[T]):
    """分页响应基类（泛型）"""

    list: Optional[list[T]] = Field(default=None, description="当前页数据列表")
    total: Optional[int] = Field(default=None, description="总记录数")
    pageNum: Optional[int] = Field(default=None, description="当前页码")
    pageSize: Optional[int] = Field(default=None, description="每页条数")


class Patient(BaseModel):
    """患者模型"""

    id: Optional[int] = Field(default=None, description="患者主键 ID")
    name: Optional[str] = Field(default=None, description="患者姓名")
    phone: Optional[str] = Field(default=None, description="联系电话")
    gender: Optional[str] = Field(default=None, description="性别（男/女/未知）")
    age: Optional[int] = Field(default=None, description="年龄")
    birthday: Optional[date] = Field(default=None, description="出生日期")
    idCard: Optional[str] = Field(default=None, description="身份证号")
    address: Optional[str] = Field(default=None, description="住址")
    allergyHistory: Optional[str] = Field(default=None, description="过敏史")
    medicalHistory: Optional[str] = Field(default=None, description="既往病史")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")
    updateTime: Optional[datetime] = Field(default=None, description="更新时间")


class MedicalRecord(BaseModel):
    """病历模型"""

    id: Optional[int] = Field(default=None, description="病历主键 ID")
    patientId: Optional[int] = Field(default=None, description="关联患者 ID")
    patientName: Optional[str] = Field(default=None, description="患者姓名（冗余字段）")
    doctorId: Optional[int] = Field(default=None, description="主治医生 ID")
    doctorName: Optional[str] = Field(default=None, description="主治医生姓名")
    chiefComplaint: Optional[str] = Field(default=None, description="主诉")
    presentIllness: Optional[str] = Field(default=None, description="现病史")
    pastHistory: Optional[str] = Field(default=None, description="既往史")
    examination: Optional[str] = Field(default=None, description="口腔检查")
    diagnosis: Optional[str] = Field(default=None, description="诊断")
    treatmentPlan: Optional[str] = Field(default=None, description="治疗计划")
    note: Optional[str] = Field(default=None, description="备注")
    visitDate: Optional[date] = Field(default=None, description="就诊日期")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")
    updateTime: Optional[datetime] = Field(default=None, description="更新时间")


class Appointment(BaseModel):
    """预约模型"""

    id: Optional[int] = Field(default=None, description="预约主键 ID")
    patientId: Optional[int] = Field(default=None, description="患者 ID")
    patientName: Optional[str] = Field(default=None, description="患者姓名")
    doctorId: Optional[int] = Field(default=None, description="预约医生 ID")
    doctorName: Optional[str] = Field(default=None, description="预约医生姓名")
    appointmentDate: Optional[date] = Field(default=None, description="预约日期")
    appointmentTime: Optional[str] = Field(default=None, description="预约时段")
    status: Optional[str] = Field(default=None, description="预约状态（待确认/已确认/已到访/已取消）")
    type: Optional[str] = Field(default=None, description="预约类型（初诊/复诊/治疗）")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class Doctor(BaseModel):
    """医生/员工模型"""

    id: Optional[int] = Field(default=None, description="员工主键 ID")
    name: Optional[str] = Field(default=None, description="姓名")
    phone: Optional[str] = Field(default=None, description="电话")
    role: Optional[str] = Field(default=None, description="角色（admin/doctor/nurse）")
    title: Optional[str] = Field(default=None, description="职称")
    department: Optional[str] = Field(default=None, description="科室")
    status: Optional[str] = Field(default=None, description="在职状态")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class Treatment(BaseModel):
    """治疗/处置模型"""

    id: Optional[int] = Field(default=None, description="治疗记录 ID")
    patientId: Optional[int] = Field(default=None, description="患者 ID")
    patientName: Optional[str] = Field(default=None, description="患者姓名")
    doctorId: Optional[int] = Field(default=None, description="操作医生 ID")
    doctorName: Optional[str] = Field(default=None, description="操作医生姓名")
    treatmentItem: Optional[str] = Field(default=None, description="治疗项目")
    toothPosition: Optional[str] = Field(default=None, description="牙位")
    fee: Optional[float] = Field(default=None, description="费用")
    status: Optional[str] = Field(default=None, description="状态（待治疗/治疗中/已完成）")
    treatmentDate: Optional[date] = Field(default=None, description="治疗日期")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class FinanceRecord(BaseModel):
    """财务/收费记录模型"""

    id: Optional[int] = Field(default=None, description="财务记录 ID")
    patientId: Optional[int] = Field(default=None, description="患者 ID")
    patientName: Optional[str] = Field(default=None, description="患者姓名")
    type: Optional[str] = Field(default=None, description="收支类型（收入/支出）")
    category: Optional[str] = Field(default=None, description="费用类别")
    amount: Optional[float] = Field(default=None, description="金额")
    paymentMethod: Optional[str] = Field(default=None, description="支付方式")
    transactionDate: Optional[date] = Field(default=None, description="交易日期")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class InventoryItem(BaseModel):
    """库存/物品模型"""

    id: Optional[int] = Field(default=None, description="库存主键 ID")
    name: Optional[str] = Field(default=None, description="物品名称")
    category: Optional[str] = Field(default=None, description="分类")
    specification: Optional[str] = Field(default=None, description="规格")
    unit: Optional[str] = Field(default=None, description="单位")
    quantity: Optional[int] = Field(default=None, description="当前数量")
    warningThreshold: Optional[int] = Field(default=None, description="预警阈值")
    supplier: Optional[str] = Field(default=None, description="供应商")
    expiryDate: Optional[date] = Field(default=None, description="有效期")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class ConsultationRecord(BaseModel):
    """咨询记录模型"""

    id: Optional[int] = Field(default=None, description="咨询记录 ID")
    patientId: Optional[int] = Field(default=None, description="患者 ID")
    patientName: Optional[str] = Field(default=None, description="患者姓名")
    consultantId: Optional[int] = Field(default=None, description="咨询员 ID")
    consultantName: Optional[str] = Field(default=None, description="咨询员姓名")
    channel: Optional[str] = Field(default=None, description="咨询渠道（微信/电话/到店）")
    content: Optional[str] = Field(default=None, description="咨询内容")
    followUpPlan: Optional[str] = Field(default=None, description="跟进计划")
    status: Optional[str] = Field(default=None, description="状态（待跟进/已跟进/已转化）")
    consultationDate: Optional[date] = Field(default=None, description="咨询日期")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class LabOrder(BaseModel):
    """加工单模型"""

    id: Optional[int] = Field(default=None, description="加工单 ID")
    patientId: Optional[int] = Field(default=None, description="患者 ID")
    patientName: Optional[str] = Field(default=None, description="患者姓名")
    doctorId: Optional[int] = Field(default=None, description="开单医生 ID")
    doctorName: Optional[str] = Field(default=None, description="开单医生姓名")
    labName: Optional[str] = Field(default=None, description="加工厂名称")
    itemName: Optional[str] = Field(default=None, description="加工项目")
    toothPosition: Optional[str] = Field(default=None, description="牙位")
    shade: Optional[str] = Field(default=None, description="色号")
    status: Optional[str] = Field(default=None, description="状态（已送出/加工中/已返回/已戴牙）")
    sendDate: Optional[date] = Field(default=None, description="送出日期")
    returnDate: Optional[date] = Field(default=None, description="返回日期")
    fee: Optional[float] = Field(default=None, description="加工费")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")


class BusinessReport(BaseModel):
    """经营日报模型"""

    id: Optional[int] = Field(default=None, description="日报 ID")
    reportDate: Optional[date] = Field(default=None, description="报表日期")
    newPatientCount: Optional[int] = Field(default=None, description="新增患者数")
    appointmentCount: Optional[int] = Field(default=None, description="预约数")
    visitCount: Optional[int] = Field(default=None, description="实际到访数")
    treatmentCount: Optional[int] = Field(default=None, description="治疗人次")
    totalRevenue: Optional[float] = Field(default=None, description="总营收")
    totalExpense: Optional[float] = Field(default=None, description="总支出")
    netIncome: Optional[float] = Field(default=None, description="净利润")
    remark: Optional[str] = Field(default=None, description="备注")
    createTime: Optional[datetime] = Field(default=None, description="生成时间")


class RoleMenuPermission(BaseModel):
    """角色菜单权限模型"""

    id: Optional[int] = Field(default=None, description="记录 ID")
    role: Optional[str] = Field(default=None, description="角色标识")
    menuCode: Optional[str] = Field(default=None, description="菜单编码")
    menuName: Optional[str] = Field(default=None, description="菜单名称")
    enabled: Optional[bool] = Field(default=None, description="是否启用")


class PatientImage(BaseModel):
    """患者影像模型"""

    id: Optional[int] = Field(default=None, description="影像 ID")
    patientId: Optional[int] = Field(default=None, description="患者 ID")
    fileName: Optional[str] = Field(default=None, description="文件名")
    fileType: Optional[str] = Field(default=None, description="文件类型")
    imageUrl: Optional[str] = Field(default=None, description="访问路径")
    uploadTime: Optional[datetime] = Field(default=None, description="上传时间")


class AiAgentConfig(BaseModel):
    """AI Agent 配置模型"""

    id: Optional[int] = Field(default=None, description="配置 ID")
    sceneType: Optional[str] = Field(default=None, description="场景类型")
    promptTemplate: Optional[str] = Field(default=None, description="提示词模板")
    enabled: Optional[bool] = Field(default=None, description="是否启用")
    createTime: Optional[datetime] = Field(default=None, description="创建时间")

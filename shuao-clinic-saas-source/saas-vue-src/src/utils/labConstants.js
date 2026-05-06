export const LAB_FACTORY_STATUS_OPTIONS = ['合作中', '已停止合作']

export const LAB_PRODUCT_STATUS_OPTIONS = ['启用', '停用']

export const LAB_ORDER_STATUS_OPTIONS = ['已下单', '加工中', '已完成', '已收货', '已对账']

export const LAB_MANUAL_ORDER_STATUS_OPTIONS = ['已下单', '加工中', '已完成', '已收货']

export const LAB_BILL_STATUS_OPTIONS = ['待对账', '对账中', '已完成对账']

export const LAB_MATCH_STATUS_OPTIONS = ['完全匹配', '数量不符', '金额不符', '仅账单有']

export const LAB_RESOLUTION_STATUS_OPTIONS = ['待处理', '已处理', '已忽略', '无需处理']

export const LAB_TEMPLATE_FIELDS = [
  { key: 'product_name', label: '产品名称', required: true },
  { key: 'product_spec', label: '产品规格', required: false },
  { key: 'quantity', label: '数量', required: true },
  { key: 'unit_price', label: '单价', required: false },
  { key: 'total_amount', label: '金额', required: true },
  { key: 'delivery_date', label: '送货日期', required: false },
  { key: 'patient_name', label: '患者姓名', required: false }
]

export function normalizeLabRole(rawRole) {
  const role = String(rawRole || '').trim()
  if (role === '管理员' || role === 'admin') return 'admin'
  if (role === '医生' || role === 'doctor') return 'doctor'
  if (role === '护士' || role === 'nurse') return 'nurse'
  return role
}

export function canManageLabFactory(role) {
  return normalizeLabRole(role) === 'admin'
}

export function canEditLabOrders(role) {
  const normalized = normalizeLabRole(role)
  return normalized === 'admin' || normalized === 'nurse'
}

export function canViewLabOnly(role) {
  return normalizeLabRole(role) === 'doctor'
}

export function canOperateLabBills(role) {
  const normalized = normalizeLabRole(role)
  return normalized === 'admin' || normalized === 'nurse'
}

export function canViewLabStatistics(role) {
  return normalizeLabRole(role) === 'admin'
}

export function formatMoney(value) {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

export function orderStatusRank(status) {
  switch (String(status || '').trim()) {
    case '已下单':
      return 0
    case '加工中':
      return 1
    case '已完成':
      return 2
    case '已收货':
      return 3
    case '已对账':
      return 4
    default:
      return -1
  }
}

export function orderStatusTagType(status) {
  if (status === '已对账') return 'success'
  if (status === '已收货') return 'primary'
  if (status === '已完成') return 'warning'
  if (status === '加工中') return ''
  return 'info'
}

export function billStatusTagType(status) {
  if (status === '已完成对账') return 'success'
  if (status === '对账中') return 'warning'
  return 'info'
}

export function matchStatusTagType(status) {
  if (status === '完全匹配') return 'success'
  if (status === '数量不符' || status === '金额不符') return 'warning'
  return 'danger'
}

export function resolutionStatusTagType(status) {
  if (status === '无需处理' || status === '已处理') return 'success'
  if (status === '已忽略') return 'info'
  return 'warning'
}

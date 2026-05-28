export const MATERIAL_CATEGORY_STATUS_OPTIONS = ['启用', '停用']

export const MATERIAL_STATUS_OPTIONS = ['在用', '停用']

export const MATERIAL_PURCHASE_STATUS_OPTIONS = ['有效', '已作废']

export const MATERIAL_PAYMENT_METHOD_OPTIONS = ['现金', '转账', '微信', '支付宝', '对公', '挂账']

export function normalizeMaterialRole(rawRole) {
  const role = String(rawRole || '').trim()
  if (role === '管理员' || role === 'admin') return 'admin'
  if (role === '医生' || role === 'doctor') return 'doctor'
  if (role === '护士' || role === 'nurse') return 'nurse'
  return role
}

export function canManageMaterialCategories(role) {
  return normalizeMaterialRole(role) === 'admin'
}

export function canEditMaterials(role) {
  const normalized = normalizeMaterialRole(role)
  return normalized === 'admin' || normalized === 'nurse'
}

export function canCreateMaterialPurchases(role) {
  const normalized = normalizeMaterialRole(role)
  return normalized === 'admin' || normalized === 'nurse'
}

export function canVoidMaterialPurchases(role) {
  return normalizeMaterialRole(role) === 'admin'
}

export function canViewMaterialStatistics(role) {
  return normalizeMaterialRole(role) === 'admin'
}

export function formatMaterialMoney(value) {
  const amount = Number(value || 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

export function materialStatusTagType(status) {
  return status === '在用' ? 'success' : 'info'
}

export function purchaseStatusTagType(status) {
  return status === '有效' ? 'success' : 'danger'
}

export function lowStockTagType(material) {
  const current = Number(material && material.current_stock)
  const alert = Number(material && material.min_stock_alert)
  if (!Number.isFinite(current) || !Number.isFinite(alert) || alert <= 0) return ''
  return current <= alert ? 'danger' : ''
}

import { getAdminSession } from '@/utils/adminSession'

export const ROLE_MENU_CATALOG = [
  { key: '/home', label: '首页概览', group: '通用', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/Patient', label: '患者列表', group: '通用', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/MedicalRecord', label: '今日工作', group: '通用', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/Followup', label: '回访管理', group: '通用', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/Consultation', label: '咨询记录', group: '咨询管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/ConsultationDashboard', label: '咨询看板', group: '咨询管理', defaultRoles: ['admin', 'nurse'] },
  { key: '/advertising-spending', label: '广告投放', group: '市场投放', defaultRoles: ['admin', 'nurse'] },
  { key: '/lab-factories', label: '加工厂/产品库', group: '义齿加工', defaultRoles: ['admin'] },
  { key: '/lab-orders', label: '加工订单', group: '义齿加工', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/lab-bills', label: '月度账单', group: '义齿加工', defaultRoles: ['admin', 'nurse'] },
  { key: '/lab-statistics', label: '加工统计', group: '义齿加工', defaultRoles: ['admin'] },
  { key: '/material-categories', label: '耗材分类', group: '耗材管理', defaultRoles: ['admin'] },
  { key: '/materials', label: '耗材档案', group: '耗材管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/material-purchases', label: '采购记录', group: '耗材管理', defaultRoles: ['admin', 'nurse'] },
  { key: '/material-statistics', label: '耗材统计', group: '耗材管理', defaultRoles: ['admin'] },
  { key: '/Appointment', label: '预约视图', group: '接诊与排班', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/Doctor', label: '医生排班', group: '接诊与排班', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/Financial', label: '财务信息', group: '财务管理', defaultRoles: ['admin', 'nurse'] },
  { key: '/Financial2', label: '财务分析', group: '财务管理', defaultRoles: ['admin', 'nurse'] },
  { key: '/financial-expenses', label: '财务支出', group: '财务管理', defaultRoles: ['admin', 'nurse'] },
  { key: '/InsuranceOverview', label: '医保总览', group: '医保管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/InsuranceConfig', label: '医保配置', group: '医保管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/InsurancePatientProfile', label: '患者医保档案', group: '医保管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/InsuranceSettlement', label: '医保结算', group: '医保管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/InsuranceLog', label: '医保日志', group: '医保管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/InsuranceMockPayload', label: 'mock报文', group: '医保管理', defaultRoles: ['admin', 'doctor', 'nurse'] },
  { key: '/SystemTreatmentCatalog', label: '治疗项目管理', group: '系统设置', defaultRoles: ['admin', 'nurse'] },
  { key: '/SystemPaymentChannel', label: '收款渠道', group: '系统设置', defaultRoles: ['admin', 'nurse'] },
  { key: '/SystemConsentTemplate', label: '知情同意书库', group: '系统设置', defaultRoles: ['admin', 'nurse'] },
  { key: '/SystemAccountPermission', label: '账号权限', group: '系统设置', defaultRoles: ['admin'] },
  { key: '/SystemAccountManage', label: '账号管理', group: '系统设置', defaultRoles: ['admin'] },
  { key: '/SystemSettings', label: '系统设置', group: '系统设置', defaultRoles: ['admin', 'doctor', 'nurse'] }
]

export function normalizeAdminRole(rawRole) {
  const role = String(rawRole || '').trim()
  if (role === '管理员' || role === 'admin') return 'admin'
  if (role === '医生' || role === 'doctor') return 'doctor'
  if (role === '护士' || role === 'nurse') return 'nurse'
  return role
}

export function buildLegacyAllowedMenuKeys(role) {
  const normalized = normalizeAdminRole(role)
  return ROLE_MENU_CATALOG
    .filter(item => item.defaultRoles.includes(normalized))
    .map(item => item.key)
}

export function getAllowedMenuKeys(session = getAdminSession() || {}) {
  const configured = Array.isArray(session.allowedMenuKeys) ? session.allowedMenuKeys : []
  if (session && session.roleMenuPermissionsLoaded === true) {
    return configured
  }
  return buildLegacyAllowedMenuKeys(session.role)
}

export function canAccessRoleMenu(session, key) {
  return getAllowedMenuKeys(session).includes(key)
}

export function findFirstAccessibleMenuKey(session) {
  const allowedKeys = getAllowedMenuKeys(session)
  const allowed = new Set(allowedKeys)
  if (!allowedKeys.length) {
    return '/login1'
  }
  const preferred = ['/home', '/Consultation', '/Patient', '/Appointment']
  const matchedPreferred = preferred.find(key => allowed.has(key))
  if (matchedPreferred) {
    return matchedPreferred
  }
  const first = ROLE_MENU_CATALOG.find(item => allowed.has(item.key))
  return first ? first.key : '/home'
}

export function resolveMenuPermissionKey(path) {
  const text = String(path || '').trim()
  if (!text) return ''
  const aliasMap = {
    '/LabFactory': '/lab-factories',
    '/LabOrder': '/lab-orders',
    '/LabBill': '/lab-bills',
    '/LabStatistics': '/lab-statistics',
    '/MaterialCategory': '/material-categories',
    '/Material': '/materials',
    '/MaterialPurchase': '/material-purchases',
    '/MaterialStatistics': '/material-statistics',
    '/FinancialExpense': '/financial-expenses'
  }
  if (aliasMap[text]) {
    return aliasMap[text]
  }
  if (ROLE_MENU_CATALOG.some(item => item.key === text)) {
    return text
  }
  if (text.startsWith('/lab-factories/')) return '/lab-factories'
  if (text.startsWith('/lab-bills/')) return '/lab-bills'
  if (text.startsWith('/material-purchases/')) return '/material-purchases'
  return ''
}

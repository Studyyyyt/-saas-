export const CUSTOMER_SOURCE_OPTIONS = ['微信', '大众点评', '美团', '电话', '抖音', '小红书', '百度', '快手', '抖音/小红书', '转介绍', '自然到店', '其他', '暂未确认']

export const CONSULTATION_CHANNEL_OPTIONS = ['微信', '大众点评', '美团', '电话', '抖音', '小红书', '百度', '快手', '抖音/小红书', '转介绍', '自然到店', '其他']

export const ADVERTISING_PLATFORM_OPTIONS = ['抖音', '小红书', '大众点评', '美团', '微信', '百度', '快手', '其他']

export const CHIEF_PROJECT_OPTIONS = ['种植', '正畸', '修复', '洗牙', '补牙', '拔牙', '儿童齿科', '美白', '其他']

export const INTENT_LEVEL_OPTIONS = [
  { label: '高', value: '高', type: 'danger', tip: '高：明确询问价格、医生、预约时间，有强烈到店意向' },
  { label: '中', value: '中', type: 'warning', tip: '中：在了解项目，仍在比较或犹豫' },
  { label: '低', value: '低', type: 'info', tip: '低：只是初步了解，没有明确意向' }
]

export const HANDLING_RESULT_OPTIONS = ['已成交', '已预约到店', '待跟进', '不再跟进']

export const CONSULTATION_STAGE_FILTER_OPTIONS = [
  { label: '全部', value: 'all' },
  { label: '未成交', value: 'unconverted' },
  { label: '待跟进', value: 'pending' },
  { label: '已预约', value: 'booked' },
  { label: '已成交', value: 'dealt' }
]

export function isConsultationDealt(item) {
  if (!item || typeof item !== 'object') return false
  return !!String(item.deal_at || '').trim() || String(item.handling_result || '').trim() === '已成交'
}

export function matchConsultationStage(item, stage = 'all') {
  const normalizedStage = String(stage || 'all').trim()
  if (!normalizedStage || normalizedStage === 'all') return true
  const handlingResult = String((item && item.handling_result) || '').trim()
  const dealt = isConsultationDealt(item)
  if (normalizedStage === 'dealt') return dealt
  if (normalizedStage === 'unconverted') return !dealt
  if (normalizedStage === 'pending') return !dealt && handlingResult === '待跟进'
  if (normalizedStage === 'booked') return !dealt && handlingResult === '已预约到店'
  return true
}

export function resolveConsultationStageFilters(stage = 'all') {
  const normalizedStage = String(stage || 'all').trim()
  if (normalizedStage === 'pending') {
    return { handlingResult: '待跟进', hasDeal: false }
  }
  if (normalizedStage === 'booked') {
    return { handlingResult: '已预约到店', hasDeal: false }
  }
  if (normalizedStage === 'dealt') {
    return { handlingResult: '', hasDeal: true }
  }
  if (normalizedStage === 'unconverted') {
    return { handlingResult: '', hasDeal: false }
  }
  return { handlingResult: '', hasDeal: '' }
}

export function detectConsultationStageFilter(handlingResult, hasDeal) {
  const normalizedHandlingResult = String(handlingResult || '').trim()
  if (hasDeal === true) {
    return !normalizedHandlingResult || normalizedHandlingResult === '已成交' ? 'dealt' : ''
  }
  if (hasDeal === false) {
    if (!normalizedHandlingResult) return 'unconverted'
    if (normalizedHandlingResult === '待跟进') return 'pending'
    if (normalizedHandlingResult === '已预约到店') return 'booked'
    return ''
  }
  if (!normalizedHandlingResult) return 'all'
  if (normalizedHandlingResult === '待跟进') return 'pending'
  if (normalizedHandlingResult === '已预约到店') return 'booked'
  if (normalizedHandlingResult === '已成交') return 'dealt'
  return ''
}

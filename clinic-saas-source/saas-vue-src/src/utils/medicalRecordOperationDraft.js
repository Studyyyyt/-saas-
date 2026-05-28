function text(value) {
  return String(value || '').trim()
}

export function normalizeToothPositions(value) {
  return text(value)
    .split(',')
    .map(item => text(item).replace(/^#/, ''))
    .filter(Boolean)
}

export function formatToothLabel(value) {
  const list = normalizeToothPositions(value)
  if (!list.length) return ''
  return list.map(item => `#${item}`).join('、')
}

export function buildMedicalRecordTreatmentDraft(items = []) {
  const segments = (Array.isArray(items) ? items : [])
    .map(item => {
      const projectName = text(item && item.project_name)
      const operationName = text(item && item.operation_name)
      const toothLabel = formatToothLabel(item && item.tooth_positions)
      const actionText = operationName || projectName
      if (!actionText) return ''
      return toothLabel ? `${toothLabel} ${actionText}` : actionText
    })
    .filter(Boolean)

  if (!segments.length) return ''
  return `今日为患者进行${segments.join('、')}。`
}

export function buildOperationSummary(items = []) {
  const names = (Array.isArray(items) ? items : [])
    .map(item => text(item && item.operation_name))
    .filter(Boolean)
  if (!names.length) return ''
  const uniqueNames = Array.from(new Set(names))
  if (uniqueNames.length <= 3) {
    return uniqueNames.join('、')
  }
  return `${uniqueNames.slice(0, 3).join('、')}等${uniqueNames.length}项`
}

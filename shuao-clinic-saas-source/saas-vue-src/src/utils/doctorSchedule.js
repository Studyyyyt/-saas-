export const SHIFT_CODE_MORNING = '早班'
export const SHIFT_CODE_EVENING = '晚班'
export const SHIFT_CODE_REST = '休息'
export const SHIFT_CODE_CUSTOM = '自定义'
export const SHIFT_CODE_NONE = '未排班'

export const SHIFT_OPTIONS = [
  { value: SHIFT_CODE_MORNING, label: '早班', shortLabel: '早', timeText: '09:00-18:00', tone: 'morning' },
  { value: SHIFT_CODE_EVENING, label: '晚班', shortLabel: '晚', timeText: '13:00-21:00', tone: 'evening' },
  { value: SHIFT_CODE_REST, label: '休息', shortLabel: '休', timeText: '全天休息', tone: 'rest' }
]

export const SHIFT_DEFINITIONS = {
  [SHIFT_CODE_MORNING]: {
    label: '早班',
    shortLabel: '早',
    start: '09:00:00',
    end: '18:00:00',
    startMinutes: 9 * 60,
    endMinutes: 18 * 60,
    timeText: '09:00-18:00',
    tone: 'morning'
  },
  [SHIFT_CODE_EVENING]: {
    label: '晚班',
    shortLabel: '晚',
    start: '13:00:00',
    end: '21:00:00',
    startMinutes: 13 * 60,
    endMinutes: 21 * 60,
    timeText: '13:00-21:00',
    tone: 'evening'
  },
  [SHIFT_CODE_REST]: {
    label: '休息',
    shortLabel: '休',
    start: '',
    end: '',
    startMinutes: null,
    endMinutes: null,
    timeText: '全天休息',
    tone: 'rest'
  }
}

function pad2(value) {
  return String(value).padStart(2, '0')
}

export function normalizeDoctorName(value) {
  return String(value || '').trim()
}

export function normalizeDateText(value) {
  if (!value) return ''
  if (value instanceof Date) {
    const year = value.getFullYear()
    const month = pad2(value.getMonth() + 1)
    const day = pad2(value.getDate())
    return `${year}-${month}-${day}`
  }
  const text = String(value).trim()
  const matched = text.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (matched) {
    return `${matched[1]}-${matched[2]}-${matched[3]}`
  }
  const parsed = new Date(text)
  if (Number.isNaN(parsed.getTime())) {
    return text
  }
  return normalizeDateText(parsed)
}

export function normalizeTimeText(value, withSeconds = false) {
  if (!value) return ''
  const text = String(value).trim()
  const matched = text.match(/(\d{1,2}):(\d{2})(?::(\d{2}))?/)
  if (!matched) {
    return ''
  }
  const hour = pad2(matched[1])
  const minute = matched[2]
  const second = matched[3] || '00'
  return withSeconds ? `${hour}:${minute}:${second}` : `${hour}:${minute}`
}

export function timeTextToMinutes(value) {
  const text = normalizeTimeText(value)
  if (!text) return null
  const parts = text.split(':')
  const hour = Number(parts[0])
  const minute = Number(parts[1] || 0)
  if (!Number.isFinite(hour) || !Number.isFinite(minute)) {
    return null
  }
  return (hour * 60) + minute
}

export function minutesToTimeText(minutes, withSeconds = false) {
  if (!Number.isFinite(Number(minutes))) return ''
  const safeMinutes = Math.max(0, Number(minutes))
  const hour = Math.floor(safeMinutes / 60)
  const minute = safeMinutes % 60
  const text = `${pad2(hour)}:${pad2(minute)}`
  return withSeconds ? `${text}:00` : text
}

export function resolveShiftType(record) {
  if (!record) return SHIFT_CODE_NONE
  const status = String(record.status || '').trim()
  if (status === SHIFT_CODE_MORNING || status === SHIFT_CODE_EVENING || status === SHIFT_CODE_REST) {
    return status
  }
  const startMinutes = timeTextToMinutes(record.start_time)
  const endMinutes = timeTextToMinutes(record.end_time)
  if (startMinutes === SHIFT_DEFINITIONS[SHIFT_CODE_MORNING].startMinutes
    && endMinutes === SHIFT_DEFINITIONS[SHIFT_CODE_MORNING].endMinutes) {
    return SHIFT_CODE_MORNING
  }
  if (startMinutes === SHIFT_DEFINITIONS[SHIFT_CODE_EVENING].startMinutes
    && endMinutes === SHIFT_DEFINITIONS[SHIFT_CODE_EVENING].endMinutes) {
    return SHIFT_CODE_EVENING
  }
  if (startMinutes == null || endMinutes == null || endMinutes <= startMinutes) {
    return status ? status : SHIFT_CODE_REST
  }
  return status || SHIFT_CODE_CUSTOM
}

export function scheduleTone(shiftType) {
  if (shiftType === SHIFT_CODE_MORNING) return 'morning'
  if (shiftType === SHIFT_CODE_EVENING) return 'evening'
  if (shiftType === SHIFT_CODE_REST) return 'rest'
  if (shiftType === SHIFT_CODE_NONE) return 'empty'
  return 'custom'
}

export function normalizeScheduleRecord(item) {
  if (!item) return null
  const doctorName = normalizeDoctorName(item.doctor_name)
  const scheduleDate = normalizeDateText(item.schedule_date)
  if (!doctorName || !scheduleDate) {
    return null
  }
  const record = {
    id: item.id != null ? Number(item.id) : null,
    doctor_name: doctorName,
    schedule_date: scheduleDate,
    start_time: normalizeTimeText(item.start_time, true),
    end_time: normalizeTimeText(item.end_time, true),
    status: String(item.status || '').trim()
  }
  record.shiftType = resolveShiftType(record)
  return record
}

export function scheduleKey(doctorName, scheduleDate) {
  return `${normalizeDoctorName(doctorName)}|${normalizeDateText(scheduleDate)}`
}

export function createShiftPayload({ id = null, doctorName = '', scheduleDate = '', shiftType = '' } = {}) {
  const normalizedDoctorName = normalizeDoctorName(doctorName)
  const normalizedDate = normalizeDateText(scheduleDate)
  if (!normalizedDoctorName || !normalizedDate || !shiftType) {
    return null
  }
  const definition = SHIFT_DEFINITIONS[shiftType]
  if (!definition) {
    return null
  }
  return {
    id,
    doctor_name: normalizedDoctorName,
    schedule_date: normalizedDate,
    start_time: definition.start || null,
    end_time: definition.end || null,
    status: shiftType
  }
}

export function getWorkingRanges(record) {
  if (!record) return []
  const shiftType = resolveShiftType(record)
  if (shiftType === SHIFT_CODE_REST) {
    return []
  }
  const startMinutes = timeTextToMinutes(record.start_time)
  const endMinutes = timeTextToMinutes(record.end_time)
  if (startMinutes != null && endMinutes != null && endMinutes > startMinutes) {
    return [{ startMinutes, endMinutes }]
  }
  const definition = SHIFT_DEFINITIONS[shiftType]
  if (!definition || definition.startMinutes == null || definition.endMinutes == null) {
    return []
  }
  return [{
    startMinutes: definition.startMinutes,
    endMinutes: definition.endMinutes
  }]
}

export function buildNonWorkingMasks(record, dayStartMinutes, dayEndMinutes) {
  const workRanges = getWorkingRanges(record)
    .map(item => ({
      startMinutes: Math.max(dayStartMinutes, item.startMinutes),
      endMinutes: Math.min(dayEndMinutes, item.endMinutes)
    }))
    .filter(item => item.endMinutes > item.startMinutes)
    .sort((a, b) => a.startMinutes - b.startMinutes)

  if (!workRanges.length) {
    return [{
      startMinutes: dayStartMinutes,
      endMinutes: dayEndMinutes,
      label: record && resolveShiftType(record) === SHIFT_CODE_REST ? '休息' : '未排班',
      type: record ? 'rest' : 'empty'
    }]
  }

  const masks = []
  let cursor = dayStartMinutes
  workRanges.forEach(range => {
    if (range.startMinutes > cursor) {
      masks.push({
        startMinutes: cursor,
        endMinutes: range.startMinutes,
        label: '非工作时间',
        type: 'off'
      })
    }
    cursor = Math.max(cursor, range.endMinutes)
  })
  if (cursor < dayEndMinutes) {
    masks.push({
      startMinutes: cursor,
      endMinutes: dayEndMinutes,
      label: '非工作时间',
      type: 'off'
    })
  }
  return masks
}

export function isTimeRangeWithinSchedule(record, startMinutes, durationMinutes) {
  if (!record) return false
  if (!Number.isFinite(Number(startMinutes)) || !Number.isFinite(Number(durationMinutes)) || Number(durationMinutes) <= 0) {
    return false
  }
  const workRanges = getWorkingRanges(record)
  if (!workRanges.length) return false
  const endMinutes = Number(startMinutes) + Number(durationMinutes)
  return workRanges.some(range => Number(startMinutes) >= range.startMinutes && endMinutes <= range.endMinutes)
}

export function scheduleDisplayLabel(record) {
  if (!record) return SHIFT_CODE_NONE
  const shiftType = resolveShiftType(record)
  if (shiftType === SHIFT_CODE_MORNING || shiftType === SHIFT_CODE_EVENING || shiftType === SHIFT_CODE_REST) {
    return shiftType
  }
  if (record.status) return record.status
  const startTime = normalizeTimeText(record.start_time)
  const endTime = normalizeTimeText(record.end_time)
  if (startTime && endTime) {
    return SHIFT_CODE_CUSTOM
  }
  return SHIFT_CODE_NONE
}

export function scheduleTimeDescription(record) {
  if (!record) return '未设置排班'
  const shiftType = resolveShiftType(record)
  if (shiftType === SHIFT_CODE_MORNING || shiftType === SHIFT_CODE_EVENING || shiftType === SHIFT_CODE_REST) {
    return SHIFT_DEFINITIONS[shiftType].timeText
  }
  const startTime = normalizeTimeText(record.start_time)
  const endTime = normalizeTimeText(record.end_time)
  if (startTime && endTime) {
    return `${startTime}-${endTime}`
  }
  return record.status || '未设置排班'
}

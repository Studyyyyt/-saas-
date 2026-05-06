import { deepClone, nowIso, offlineDb } from './db'
import { listQueueItems } from './queue'

const CACHE_RETENTION_DAYS = 14
const CACHE_MAX_TOTAL_ENTRIES = 120
const CACHE_MAX_ENTRIES_PER_BASE_KEY = 8

let prunePromise = Promise.resolve()

function buildOfflineMeta(queueItem) {
  return {
    queued: true,
    failed: String(queueItem.status || '') === 'failed',
    label: String(queueItem.status || '') === 'failed' ? '同步失败' : '待同步',
    queueId: queueItem.id
  }
}

function compareDateString(left, right) {
  return String(right || '').localeCompare(String(left || ''))
}

function parseTime(value) {
  if (!value) return 0
  const timestamp = Date.parse(String(value))
  return Number.isFinite(timestamp) ? timestamp : 0
}

function entryOrderTime(entry) {
  return Math.max(
    parseTime(entry && entry.lastAccessedAt),
    parseTime(entry && entry.updatedAt)
  )
}

function entryBaseKey(entry) {
  const meta = entry && entry.meta ? entry.meta : {}
  return String(meta.baseKey || entry.key || '').trim()
}

function buildExpiresAt(isoValue) {
  const savedAt = parseTime(isoValue)
  if (!savedAt) return ''
  return new Date(savedAt + (CACHE_RETENTION_DAYS * 24 * 60 * 60 * 1000)).toISOString()
}

function isExpiredEntry(entry, now = Date.now()) {
  const meta = entry && entry.meta ? entry.meta : {}
  const expiresAt = parseTime(meta.expiresAt || entry.expiresAt)
  if (expiresAt) {
    return expiresAt <= now
  }
  const updatedAt = entryOrderTime(entry)
  if (!updatedAt) return false
  return updatedAt + (CACHE_RETENTION_DAYS * 24 * 60 * 60 * 1000) <= now
}

async function pruneCacheEntriesImpl() {
  const entries = await offlineDb.cacheEntries.toArray()
  if (!entries.length) return 0

  const removeKeys = new Set()
  const now = Date.now()

  entries.forEach(entry => {
    if (isExpiredEntry(entry, now)) {
      removeKeys.add(entry.key)
    }
  })

  const groupMap = new Map()
  entries
    .filter(entry => !removeKeys.has(entry.key))
    .forEach(entry => {
      const baseKey = entryBaseKey(entry)
      if (!baseKey) return
      if (!groupMap.has(baseKey)) {
        groupMap.set(baseKey, [])
      }
      groupMap.get(baseKey).push(entry)
    })

  groupMap.forEach(group => {
    group
      .sort((left, right) => entryOrderTime(right) - entryOrderTime(left))
      .slice(CACHE_MAX_ENTRIES_PER_BASE_KEY)
      .forEach(entry => removeKeys.add(entry.key))
  })

  entries
    .filter(entry => !removeKeys.has(entry.key))
    .sort((left, right) => entryOrderTime(right) - entryOrderTime(left))
    .slice(CACHE_MAX_TOTAL_ENTRIES)
    .forEach(entry => removeKeys.add(entry.key))

  if (!removeKeys.size) return 0
  await offlineDb.cacheEntries.bulkDelete(Array.from(removeKeys))
  return removeKeys.size
}

function buildOperationSummary(items) {
  const names = (Array.isArray(items) ? items : [])
    .map(item => String((item && item.operation_name) || '').trim())
    .filter(Boolean)
  if (!names.length) return ''
  const uniqueNames = Array.from(new Set(names))
  if (uniqueNames.length <= 3) return uniqueNames.join('、')
  return `${uniqueNames.slice(0, 3).join('、')}等${uniqueNames.length}项`
}

function buildPendingLabCount(items) {
  return (Array.isArray(items) ? items : []).reduce((count, item) => {
    return count + (Number(item && item.need_lab_processing) === 1 ? 1 : 0)
  }, 0)
}

function buildPatientRow(queueItem) {
  const payload = deepClone(queueItem.payload || {})
  return Object.assign({
    id: payload.id || queueItem.localId || queueItem.serverId,
    name: '',
    gender: '',
    age: null,
    phone: '',
    customer_source: '',
    latest_treatment: '',
    latest_visit_doctor_name: '',
    latest_visit_doctor: '',
    has_arrears: false,
    arrears_amount: 0,
    patient_tags: []
  }, payload, {
    _offline: buildOfflineMeta(queueItem)
  })
}

function buildAppointmentRow(queueItem) {
  const payload = deepClone(queueItem.payload || {})
  return Object.assign({
    id: payload.id || queueItem.localId || queueItem.serverId,
    patient_id: payload.patient_id || null,
    patient_name: '',
    doctor_account_id: null,
    doctor_name: '',
    appointment_date: '',
    appointment_time: '',
    duration_minutes: 60,
    appointment_purpose: '',
    status: '待治疗',
    has_arrears: false,
    arrears_amount: 0
  }, payload, {
    _offline: buildOfflineMeta(queueItem)
  })
}

function buildRecordRow(queueItem) {
  const payload = deepClone(queueItem.payload || {})
  const operationItems = Array.isArray(payload.operation_items) ? payload.operation_items : []
  return Object.assign({
    id: payload.id || queueItem.localId || queueItem.serverId,
    patient_id: payload.patient_id || null,
    patient_name: '',
    doctor_account_id: null,
    doctor_name: '',
    visit_date: '',
    chief_complaint: '',
    diagnosis: '',
    treatment: '',
    tooth_positions: '',
    prescription: '',
    notes: '',
    operation_items: operationItems,
    operation_summary: buildOperationSummary(operationItems),
    pending_lab_count: buildPendingLabCount(operationItems)
  }, payload, {
    operation_summary: payload.operation_summary || buildOperationSummary(operationItems),
    pending_lab_count: payload.pending_lab_count != null ? Number(payload.pending_lab_count) : buildPendingLabCount(operationItems),
    _offline: buildOfflineMeta(queueItem)
  })
}

function mergeRows(baseRows, queueItems, builder, sorter) {
  const baseList = Array.isArray(baseRows) ? deepClone(baseRows) : []
  const overlayMap = new Map()
  const appendRows = []

  queueItems.forEach(item => {
    const row = builder(item)
    const key = String(row.id)
    const baseIndex = baseList.findIndex(baseRow => String(baseRow.id) === key)
    if (baseIndex >= 0) {
      overlayMap.set(key, row)
    } else {
      appendRows.push(row)
    }
  })

  const merged = baseList.map(item => {
    const key = String(item.id)
    return overlayMap.has(key) ? Object.assign({}, item, overlayMap.get(key)) : item
  })

  appendRows.forEach(row => {
    if (!merged.some(item => String(item.id) === String(row.id))) {
      merged.unshift(row)
    }
  })

  if (typeof sorter === 'function') {
    return merged.sort(sorter)
  }
  return merged
}

function matchesPatientId(targetId, payload) {
  if (!payload || typeof payload !== 'object') return false
  const payloadPatientId = payload.patient_id != null ? String(payload.patient_id) : ''
  if (payloadPatientId && payloadPatientId === String(targetId)) return true
  const payloadId = payload.id != null ? String(payload.id) : ''
  return payloadId && payloadId === String(targetId)
}

function mergePatient360Raw(rawData, queueItems, patientId) {
  const source = rawData && typeof rawData === 'object' ? deepClone(rawData) : {}
  const patientQueueItems = queueItems.filter(item => item.entityType === 'patient' && matchesPatientId(patientId, item.payload))
  const appointmentQueueItems = queueItems.filter(item => item.entityType === 'appointment' && matchesPatientId(patientId, item.payload))
  const recordQueueItems = queueItems.filter(item => item.entityType === 'medicalRecord' && matchesPatientId(patientId, item.payload))

  const patientRow = patientQueueItems.length ? buildPatientRow(patientQueueItems[patientQueueItems.length - 1]) : null
  const appointments = mergeRows(source.appointments || [], appointmentQueueItems, buildAppointmentRow, (left, right) => {
    const dateCompare = compareDateString(left.appointment_date, right.appointment_date)
    if (dateCompare !== 0) return dateCompare
    return compareDateString(left.appointment_time, right.appointment_time)
  })
  const records = mergeRows(source.records || source.recentRecords || [], recordQueueItems, buildRecordRow, (left, right) => {
    return compareDateString(left.visit_date, right.visit_date)
  })

  const patient = patientRow || source.patient || null
  if (!patient) {
    return source
  }

  return Object.assign({
    patient,
    appointments,
    records,
    recentRecords: records.slice(0, 5),
    recentFollowups: source.recentFollowups || [],
    images: source.images || [],
    consents: source.consents || [],
    treatments: source.treatments || [],
    riskTags: source.riskTags || [],
    timeline: source.timeline || [],
    visitCount: records.length,
    pendingLabOperationCount: records.reduce((count, item) => count + Number(item.pending_lab_count || 0), 0),
    hasArrears: !!source.hasArrears,
    arrearsAmount: Number(source.arrearsAmount || 0)
  }, source, {
    patient,
    appointments,
    records,
    recentRecords: records.slice(0, 5),
    visitCount: Math.max(Number(source.visitCount || 0), records.length),
    pendingLabOperationCount: records.reduce((count, item) => count + Number(item.pending_lab_count || 0), 0)
  })
}

async function queueItemsForScopes(scope) {
  if (scope === 'patientsWorkbench' || scope === 'patientsH5' || scope === 'patient360') {
    return listQueueItems({ statuses: ['pending', 'failed'] })
  }
  if (scope === 'appointmentsBoard' || scope === 'appointmentsH5') {
    return listQueueItems({ entityTypes: ['appointment'], statuses: ['pending', 'failed'] })
  }
  if (scope === 'medicalRecordsList' || scope === 'medicalRecordsH5') {
    return listQueueItems({ entityTypes: ['medicalRecord'], statuses: ['pending', 'failed'] })
  }
  return []
}

export async function putCacheEntry(key, data, meta = {}) {
  const savedAt = nowIso()
  const nextMeta = Object.assign({}, deepClone(meta), {
    expiresAt: buildExpiresAt(savedAt)
  })
  await offlineDb.cacheEntries.put({
    key,
    scope: meta.scope || '',
    data: deepClone(data),
    meta: nextMeta,
    updatedAt: savedAt,
    lastAccessedAt: savedAt,
    expiresAt: nextMeta.expiresAt
  })
  await pruneCacheEntries()
}

export async function getCacheEntry(key) {
  const entry = await offlineDb.cacheEntries.get(key)
  if (!entry) return null
  const touchedAt = nowIso()
  await offlineDb.cacheEntries.update(key, {
    lastAccessedAt: touchedAt
  })
  return Object.assign({}, entry, {
    lastAccessedAt: touchedAt
  })
}

export async function listCacheEntries() {
  return offlineDb.cacheEntries.toArray()
}

export async function touchCacheEntry(key) {
  const normalizedKey = String(key || '').trim()
  if (!normalizedKey) return null
  const entry = await offlineDb.cacheEntries.get(normalizedKey)
  if (!entry) return null
  const touchedAt = nowIso()
  await offlineDb.cacheEntries.update(normalizedKey, {
    lastAccessedAt: touchedAt
  })
  return Object.assign({}, entry, {
    lastAccessedAt: touchedAt
  })
}

export function pruneCacheEntries() {
  prunePromise = prunePromise
    .catch(() => {})
    .then(() => pruneCacheEntriesImpl())
  return prunePromise
}

export async function augmentCachedData(scope, rawData, context = {}) {
  const queueItems = await queueItemsForScopes(scope)
  if (!queueItems.length) {
    return deepClone(rawData)
  }

  if (scope === 'patientsWorkbench') {
    const source = rawData && typeof rawData === 'object' ? deepClone(rawData) : {}
    const patientQueueItems = queueItems.filter(item => item.entityType === 'patient')
    const mergedList = mergeRows(source.list || [], patientQueueItems, buildPatientRow, (left, right) => {
      return compareDateString(left.updated_at || left.created_at, right.updated_at || right.created_at)
    })
    return Object.assign({}, source, {
      list: mergedList,
      total: Math.max(Number(source.total || 0), mergedList.length)
    })
  }

  if (scope === 'patientsH5') {
    const patientQueueItems = queueItems.filter(item => item.entityType === 'patient')
    return mergeRows(rawData || [], patientQueueItems, buildPatientRow, (left, right) => {
      return compareDateString(left.updated_at || left.created_at, right.updated_at || right.created_at)
    })
  }

  if (scope === 'appointmentsBoard' || scope === 'appointmentsH5') {
    return mergeRows(rawData || [], queueItems.filter(item => item.entityType === 'appointment'), buildAppointmentRow, (left, right) => {
      const dateCompare = compareDateString(left.appointment_date, right.appointment_date)
      if (dateCompare !== 0) return dateCompare
      return compareDateString(left.appointment_time, right.appointment_time)
    })
  }

  if (scope === 'medicalRecordsList' || scope === 'medicalRecordsH5') {
    const source = rawData && typeof rawData === 'object' ? deepClone(rawData) : {}
    const mergedList = mergeRows(source.list || [], queueItems.filter(item => item.entityType === 'medicalRecord'), buildRecordRow, (left, right) => {
      return compareDateString(left.visit_date, right.visit_date)
    })
    return Object.assign({}, source, {
      list: mergedList,
      total: Math.max(Number(source.total || 0), mergedList.length)
    })
  }

  if (scope === 'patient360') {
    return mergePatient360Raw(rawData, queueItems, context.patientId)
  }

  return deepClone(rawData)
}

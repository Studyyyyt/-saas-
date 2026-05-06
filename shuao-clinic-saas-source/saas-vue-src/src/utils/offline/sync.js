import axios from 'axios'
import { listCacheEntries, pruneCacheEntries, putCacheEntry } from './cache'
import { isBrowserOnline, setLastSyncError, setLastSyncedAt, setNetworkOnline, setOfflineInitialized, setQueueSummary, setSyncing } from './network'
import {
  isLocalEntityId,
  listQueueItems,
  markQueueItemFailed,
  markQueueItemPending,
  markQueueItemSyncing,
  removeQueueItem,
  resetFailedQueueItems,
  resolveMappedServerId,
  saveIdMapping,
  getQueueSummary,
  OFFLINE_QUEUE_EVENT
} from './queue'

let initialized = false
let syncPromise = null

function isSuccessCode(body) {
  return !body || body.code === undefined || String(body.code) === '200'
}

function readResponseData(response) {
  const body = response && response.data
  if (!isSuccessCode(body)) {
    const error = new Error((body && body.msg) || '同步失败')
    error.isBusinessError = true
    throw error
  }
  return body && Object.prototype.hasOwnProperty.call(body, 'data') ? body.data : body
}

function extractByPath(source, path) {
  if (!path) return source
  return String(path).split('.').reduce((current, key) => {
    if (current === null || current === undefined) return current
    return current[key]
  }, source)
}

function sanitizePayload(entityType, action, payload) {
  const next = JSON.parse(JSON.stringify(payload || {}))
  if (action === 'add' || isLocalEntityId(next.id)) {
    delete next.id
  }
  if (isLocalEntityId(next.patient_id)) {
    next.patient_id = null
  }
  if (isLocalEntityId(next.related_patient_id)) {
    next.related_patient_id = null
  }
  if (entityType === 'patient') {
    delete next._offline
  }
  return next
}

async function mapLocalReferences(queueItem, payload) {
  const next = JSON.parse(JSON.stringify(payload || {}))
  if (isLocalEntityId(next.patient_id)) {
    const mappedPatientId = await resolveMappedServerId('patient', next.patient_id)
    if (!mappedPatientId) return null
    next.patient_id = mappedPatientId
  }
  if (isLocalEntityId(next.related_patient_id)) {
    const mappedRelatedId = await resolveMappedServerId('patient', next.related_patient_id)
    if (!mappedRelatedId) return null
    next.related_patient_id = mappedRelatedId
  }
  if (queueItem && queueItem.action === 'add' && isLocalEntityId(queueItem.localId || next.id)) {
    delete next.id
  }
  return next
}

async function syncSingleItem(queueItem, dirtyScopes, dirtyPatientIds) {
  await markQueueItemSyncing(queueItem.id)

  const mappedPayload = await mapLocalReferences(queueItem, queueItem.payload)
  if (!mappedPayload) {
    await markQueueItemPending(queueItem.id)
    return false
  }

  const payload = sanitizePayload(queueItem.entityType, queueItem.action, mappedPayload)
  let response

  if (queueItem.entityType === 'patient') {
    response = queueItem.action === 'add'
      ? await axios.post('/patients/add', payload)
      : await axios.put('/patients/edit', payload)
    const data = readResponseData(response) || {}
    if (queueItem.action === 'add' && queueItem.localId && data.id) {
      await saveIdMapping('patient', queueItem.localId, data.id)
      dirtyPatientIds.add(Number(data.id))
    } else if (payload.id) {
      dirtyPatientIds.add(Number(payload.id))
    }
    dirtyScopes.add('patientsWorkbench')
    dirtyScopes.add('patientsH5')
  } else if (queueItem.entityType === 'appointment') {
    response = queueItem.action === 'add'
      ? await axios.post('/appointments/add', payload)
      : await axios.put('/appointments/edit', payload)
    readResponseData(response)
    dirtyScopes.add('appointmentsBoard')
    dirtyScopes.add('appointmentsH5')
    if (payload.patient_id) dirtyPatientIds.add(Number(payload.patient_id))
  } else if (queueItem.entityType === 'medicalRecord') {
    response = queueItem.action === 'add'
      ? await axios.post('/medical-records/add', payload)
      : await axios.put('/medical-records/edit', payload)
    readResponseData(response)
    dirtyScopes.add('medicalRecordsList')
    dirtyScopes.add('medicalRecordsH5')
    if (payload.patient_id) dirtyPatientIds.add(Number(payload.patient_id))
  } else {
    throw new Error('未知同步类型')
  }

  await removeQueueItem(queueItem.id)
  return true
}

async function refreshCachedEntry(entry) {
  const meta = entry && entry.meta ? entry.meta : {}
  const request = meta.request || {}
  if (!request.url) return
  const response = await axios.request({
    method: request.method || 'get',
    url: request.url,
    params: request.params || {},
    data: request.data || undefined
  })
  const body = response && response.data
  if (!isSuccessCode(body)) return
  const rawData = extractByPath(response, meta.responseDataPath || 'data.data')
  await putCacheEntry(entry.key, rawData, meta)
}

async function refreshDirtyCaches(dirtyScopes, dirtyPatientIds) {
  if (!dirtyScopes.size && !dirtyPatientIds.size) return
  const cacheEntries = await listCacheEntries()
  const targets = cacheEntries.filter(entry => {
    const meta = entry && entry.meta ? entry.meta : {}
    const scope = String(meta.scope || entry.scope || '')
    if (dirtyScopes.has(scope)) return true
    if (scope === 'patient360' && dirtyPatientIds.size) {
      return dirtyPatientIds.has(Number(meta.patientId || 0))
    }
    return false
  })
  for (const entry of targets) {
    try {
      await refreshCachedEntry(entry)
    } catch (error) {
      // Ignore refresh failures; next page load will retry.
    }
  }
}

export async function refreshOfflineQueueSummary() {
  const summary = await getQueueSummary()
  setQueueSummary(summary)
  return summary
}

export async function syncPendingQueue(options = {}) {
  if (syncPromise && !options.force) {
    return syncPromise
  }
  if (!isBrowserOnline()) {
    setNetworkOnline(false)
    await refreshOfflineQueueSummary()
    return false
  }

  setNetworkOnline(true)
  setSyncing(true)
  setLastSyncError('')

  syncPromise = (async () => {
    const dirtyScopes = new Set()
    const dirtyPatientIds = new Set()
    const queueItems = await listQueueItems({ statuses: ['pending', 'failed'] })
    for (const item of queueItems) {
      try {
        await syncSingleItem(item, dirtyScopes, dirtyPatientIds)
      } catch (error) {
        await markQueueItemFailed(item.id, error && error.message ? error.message : '同步失败')
        setLastSyncError(error && error.message ? error.message : '同步失败')
      }
    }
    await refreshDirtyCaches(dirtyScopes, dirtyPatientIds)
    await refreshOfflineQueueSummary()
    setLastSyncedAt(new Date().toISOString())
    return true
  })().finally(() => {
    syncPromise = null
    setSyncing(false)
  })

  return syncPromise
}

export async function retryOfflineSync() {
  await resetFailedQueueItems()
  await refreshOfflineQueueSummary()
  return syncPendingQueue({ force: true })
}

export function initOfflineSync() {
  if (initialized || typeof window === 'undefined') return
  initialized = true

  const applyOnlineState = () => setNetworkOnline(isBrowserOnline())
  const trySync = () => {
    applyOnlineState()
    if (isBrowserOnline()) {
      syncPendingQueue().catch(() => {})
    }
  }

  window.addEventListener('online', trySync)
  window.addEventListener('offline', applyOnlineState)
  window.addEventListener(OFFLINE_QUEUE_EVENT, () => {
    refreshOfflineQueueSummary().catch(() => {})
  })

  applyOnlineState()
  refreshOfflineQueueSummary().catch(() => {})
  pruneCacheEntries().catch(() => {})
  setOfflineInitialized(true)
  setTimeout(() => {
    trySync()
  }, 200)
}

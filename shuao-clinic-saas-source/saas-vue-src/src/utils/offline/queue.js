import { deepClone, nowIso, offlineDb } from './db'

export const OFFLINE_QUEUE_EVENT = 'offline-queue-changed'
export const OFFLINE_ID_MAP_EVENT = 'offline-id-map-changed'

const ENTITY_PRIORITY = {
  patient: 1,
  appointment: 2,
  medicalRecord: 3
}

function emitQueueChanged() {
  if (typeof window === 'undefined') return
  window.dispatchEvent(new Event(OFFLINE_QUEUE_EVENT))
}

function emitIdMapChanged(detail = {}) {
  if (typeof window === 'undefined') return
  window.dispatchEvent(new CustomEvent(OFFLINE_ID_MAP_EVENT, {
    detail: deepClone(detail)
  }))
}

function createLocalId(entityType) {
  return `local-${entityType}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function toServerId(value) {
  if (value === null || value === undefined || value === '') return null
  if (isLocalEntityId(value)) return null
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : null
}

function normalizeAction(action) {
  return action === 'edit' ? 'edit' : 'add'
}

function normalizeStatus(status) {
  if (status === 'syncing') return 'syncing'
  if (status === 'failed') return 'failed'
  return 'pending'
}

function queueDependency(entityType, payload) {
  if (!payload || typeof payload !== 'object') {
    return { dependsOnEntityType: '', dependsOnLocalId: '', dependsOnField: '' }
  }
  if ((entityType === 'appointment' || entityType === 'medicalRecord') && isLocalEntityId(payload.patient_id)) {
    return {
      dependsOnEntityType: 'patient',
      dependsOnLocalId: String(payload.patient_id),
      dependsOnField: 'patient_id'
    }
  }
  if (entityType === 'patient' && isLocalEntityId(payload.related_patient_id)) {
    return {
      dependsOnEntityType: 'patient',
      dependsOnLocalId: String(payload.related_patient_id),
      dependsOnField: 'related_patient_id'
    }
  }
  return { dependsOnEntityType: '', dependsOnLocalId: '', dependsOnField: '' }
}

async function findQueuedItem(predicate) {
  const rows = await offlineDb.queueItems.toArray()
  return rows.find(predicate) || null
}

async function upsertQueuedRow(currentRow, nextRow) {
  const savedAt = nowIso()
  const row = Object.assign({}, currentRow || {}, nextRow, {
    status: normalizeStatus(nextRow.status || (currentRow && currentRow.status) || 'pending'),
    retryCount: currentRow && currentRow.retryCount ? Number(currentRow.retryCount) : 0,
    errorMessage: '',
    updatedAt: savedAt
  })
  if (!currentRow || !currentRow.id) {
    row.createdAt = savedAt
    const id = await offlineDb.queueItems.add(row)
    emitQueueChanged()
    return Object.assign({}, row, { id })
  }
  await offlineDb.queueItems.update(currentRow.id, row)
  emitQueueChanged()
  return Object.assign({}, row, { id: currentRow.id })
}

export function isLocalEntityId(value) {
  return typeof value === 'string' && value.indexOf('local-') === 0
}

export async function enqueueMutation({ entityType, action, payload }) {
  const safeAction = normalizeAction(action)
  const safePayload = deepClone(payload || {})

  if (safeAction === 'add') {
    const localId = isLocalEntityId(safePayload.id) ? String(safePayload.id) : createLocalId(entityType)
    safePayload.id = localId
    const dependency = queueDependency(entityType, safePayload)
    const existing = await findQueuedItem(item =>
      item.entityType === entityType
      && item.action === 'add'
      && item.localId === localId
    )
    return upsertQueuedRow(existing, {
      entityType,
      action: 'add',
      localId,
      serverId: null,
      payload: safePayload,
      dependsOnEntityType: dependency.dependsOnEntityType,
      dependsOnLocalId: dependency.dependsOnLocalId,
      dependsOnField: dependency.dependsOnField
    })
  }

  if (isLocalEntityId(safePayload.id)) {
    return enqueueMutation({ entityType, action: 'add', payload: safePayload })
  }

  const serverId = toServerId(safePayload.id)
  if (!serverId) {
    throw new Error('缺少待同步记录ID')
  }
  const dependency = queueDependency(entityType, safePayload)
  const existing = await findQueuedItem(item =>
    item.entityType === entityType
    && item.action === 'edit'
    && Number(item.serverId || 0) === serverId
  )
  return upsertQueuedRow(existing, {
    entityType,
    action: 'edit',
    localId: '',
    serverId,
    payload: safePayload,
    dependsOnEntityType: dependency.dependsOnEntityType,
    dependsOnLocalId: dependency.dependsOnLocalId,
    dependsOnField: dependency.dependsOnField
  })
}

export async function listQueueItems(options = {}) {
  const entityTypes = Array.isArray(options.entityTypes) ? options.entityTypes.map(String) : []
  const statuses = Array.isArray(options.statuses) ? options.statuses.map(String) : []
  const rows = await offlineDb.queueItems.toArray()
  return rows
    .filter(item => !entityTypes.length || entityTypes.includes(String(item.entityType || '')))
    .filter(item => !statuses.length || statuses.includes(String(item.status || 'pending')))
    .sort((left, right) => {
      const leftPriority = ENTITY_PRIORITY[left.entityType] || 99
      const rightPriority = ENTITY_PRIORITY[right.entityType] || 99
      if (leftPriority !== rightPriority) {
        return leftPriority - rightPriority
      }
      return String(left.createdAt || '').localeCompare(String(right.createdAt || ''))
    })
}

export async function getQueueSummary() {
  const rows = await offlineDb.queueItems.toArray()
  return {
    pendingCount: rows.filter(item => String(item.status || 'pending') === 'pending').length,
    failedCount: rows.filter(item => String(item.status || 'pending') === 'failed').length
  }
}

export async function markQueueItemSyncing(id) {
  await offlineDb.queueItems.update(id, {
    status: 'syncing',
    errorMessage: '',
    updatedAt: nowIso()
  })
  emitQueueChanged()
}

export async function markQueueItemPending(id) {
  await offlineDb.queueItems.update(id, {
    status: 'pending',
    updatedAt: nowIso()
  })
  emitQueueChanged()
}

export async function markQueueItemFailed(id, errorMessage) {
  const row = await offlineDb.queueItems.get(id)
  await offlineDb.queueItems.update(id, {
    status: 'failed',
    errorMessage: String(errorMessage || '').trim(),
    retryCount: Number((row && row.retryCount) || 0) + 1,
    updatedAt: nowIso()
  })
  emitQueueChanged()
}

export async function removeQueueItem(id) {
  await offlineDb.queueItems.delete(id)
  emitQueueChanged()
}

export async function resetFailedQueueItems() {
  const rows = await listQueueItems({ statuses: ['failed'] })
  await Promise.all(rows.map(item => offlineDb.queueItems.update(item.id, {
    status: 'pending',
    updatedAt: nowIso()
  })))
  if (rows.length) emitQueueChanged()
}

export async function saveIdMapping(entityType, localId, serverId) {
  if (!entityType || !localId || !serverId) return
  const row = {
    entityType,
    localId: String(localId),
    serverId: Number(serverId),
    updatedAt: nowIso()
  }
  const existing = await offlineDb.idMaps.where('[entityType+localId]').equals([entityType, String(localId)]).first()
  if (existing && existing.id) {
    await offlineDb.idMaps.update(existing.id, row)
  } else {
    await offlineDb.idMaps.add(row)
  }
  emitIdMapChanged({
    entityType,
    localId: String(localId),
    serverId: Number(serverId)
  })
}

export async function resolveMappedServerId(entityType, localId) {
  if (!entityType || !localId) return null
  const row = await offlineDb.idMaps.where('[entityType+localId]').equals([entityType, String(localId)]).first()
  return row && row.serverId ? Number(row.serverId) : null
}

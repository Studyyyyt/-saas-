import Dexie from 'dexie'

export const offlineDb = new Dexie('clinicSaasOfflinePhase1')

offlineDb.version(1).stores({
  cacheEntries: '&key, scope, updatedAt',
  queueItems: '++id, entityType, action, status, createdAt, updatedAt, localId, serverId, dependsOnLocalId',
  idMaps: '++id, &[entityType+localId], entityType, localId, serverId, updatedAt'
})

export function nowIso() {
  return new Date().toISOString()
}

export function deepClone(value) {
  if (value === null || value === undefined) return value
  return JSON.parse(JSON.stringify(value))
}

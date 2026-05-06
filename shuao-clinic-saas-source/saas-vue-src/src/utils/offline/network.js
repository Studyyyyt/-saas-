import Vue from 'vue'

export const OFFLINE_STATUS_EVENT = 'offline-status-changed'

export const offlineStatusStore = Vue.observable({
  isOnline: typeof navigator === 'undefined' ? true : navigator.onLine !== false,
  syncing: false,
  pendingCount: 0,
  failedCount: 0,
  lastError: '',
  lastSyncedAt: '',
  initialized: false,
  cacheFallbackEntries: []
})

function emitStatusChanged() {
  if (typeof window === 'undefined') return
  window.dispatchEvent(new Event(OFFLINE_STATUS_EVENT))
}

export function isBrowserOnline() {
  if (typeof navigator === 'undefined') return true
  return navigator.onLine !== false
}

export function setNetworkOnline(value) {
  offlineStatusStore.isOnline = value !== false
  emitStatusChanged()
}

export function setOfflineInitialized(value) {
  offlineStatusStore.initialized = value === true
  emitStatusChanged()
}

export function setSyncing(value) {
  offlineStatusStore.syncing = value === true
  emitStatusChanged()
}

export function setQueueSummary(summary = {}) {
  offlineStatusStore.pendingCount = Number(summary.pendingCount || 0)
  offlineStatusStore.failedCount = Number(summary.failedCount || 0)
  emitStatusChanged()
}

export function setLastSyncError(message) {
  offlineStatusStore.lastError = String(message || '').trim()
  emitStatusChanged()
}

export function setLastSyncedAt(value) {
  offlineStatusStore.lastSyncedAt = String(value || '').trim()
  emitStatusChanged()
}

function browserPathname() {
  if (typeof window === 'undefined') return ''
  return String(window.location.pathname || '').trim()
}

function normalizeCacheFallbackEntry(entry = {}) {
  return {
    key: String(entry.key || '').trim(),
    baseKey: String(entry.baseKey || '').trim(),
    scope: String(entry.scope || '').trim(),
    routePath: String(entry.routePath || browserPathname()).trim(),
    cachedAt: String(entry.cachedAt || '').trim(),
    reason: String(entry.reason || '').trim()
  }
}

export function activateCacheFallback(entry = {}) {
  const normalized = normalizeCacheFallbackEntry(entry)
  if (!normalized.key) return
  const nextList = (offlineStatusStore.cacheFallbackEntries || [])
    .filter(item => String(item.key || '') !== normalized.key)
  nextList.unshift(normalized)
  offlineStatusStore.cacheFallbackEntries = nextList.slice(0, 50)
  emitStatusChanged()
}

export function clearCacheFallback(key) {
  const normalizedKey = String(key || '').trim()
  if (!normalizedKey) return
  const previous = offlineStatusStore.cacheFallbackEntries || []
  const next = previous.filter(item => String(item.key || '') !== normalizedKey)
  if (next.length === previous.length) return
  offlineStatusStore.cacheFallbackEntries = next
  emitStatusChanged()
}

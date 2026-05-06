import axios from 'axios'
import { augmentCachedData, getCacheEntry, listCacheEntries, putCacheEntry, touchCacheEntry } from './cache'
import { activateCacheFallback, clearCacheFallback, isBrowserOnline } from './network'
import { enqueueMutation, isLocalEntityId } from './queue'
import { syncPendingQueue } from './sync'

function isSuccessCode(body) {
  return !body || body.code === undefined || String(body.code) === '200'
}

function unwrapResponseData(response) {
  const body = response && response.data
  if (!isSuccessCode(body)) {
    const error = new Error((body && body.msg) || '请求失败')
    error.isBusinessError = true
    throw error
  }
  return body && Object.prototype.hasOwnProperty.call(body, 'data') ? body.data : body
}

function isOfflineError(error) {
  if (!error) return false
  if (!isBrowserOnline()) return true
  if (error.isBusinessError) return false
  if (error.code === 'ERR_NETWORK' || error.code === 'ECONNABORTED') return true
  return !error.response
}

function defaultNotify(notifier, message) {
  if (typeof notifier === 'function' && message) {
    notifier(message)
  }
}

function shouldQueueDirectly(payload) {
  if (!payload || typeof payload !== 'object') return false
  if (isLocalEntityId(payload.id)) return true
  return isLocalEntityId(payload.patient_id) || isLocalEntityId(payload.related_patient_id)
}

function sortSerializable(value) {
  if (Array.isArray(value)) {
    return value.map(sortSerializable)
  }
  if (value && typeof value === 'object') {
    return Object.keys(value).sort().reduce((result, key) => {
      if (value[key] !== undefined) {
        result[key] = sortSerializable(value[key])
      }
      return result
    }, {})
  }
  return value
}

function buildRequestSignature(options = {}) {
  return JSON.stringify(sortSerializable({
    method: String(options.method || 'get').toLowerCase(),
    url: options.url || '',
    params: options.params || {},
    data: options.data || {}
  }))
}

function resolveCacheKey(baseKey, options = {}) {
  if (!baseKey) return ''
  return `${baseKey}::${buildRequestSignature(options)}`
}

function buildRequestMeta(options = {}) {
  return {
    scope: options.scope || '',
    baseKey: options.baseKey || '',
    requestSignature: options.requestSignature || '',
    request: {
      method: options.method || 'get',
      url: options.url || '',
      params: options.params || {},
      data: options.data || undefined
    },
    responseDataPath: options.responseDataPath || 'data.data',
    patientId: options.patientId || ''
  }
}

function currentRoutePath() {
  if (typeof window === 'undefined') return ''
  return String(window.location.pathname || '').trim()
}

function buildFallbackStateKey(cacheKey, fallbackStateKey = '') {
  const normalized = String(fallbackStateKey || '').trim()
  if (normalized) return normalized
  const routePath = currentRoutePath()
  return `${routePath || 'global'}::${String(cacheKey || '').trim()}`
}

function formatCacheTime(value) {
  const timestamp = Date.parse(String(value || ''))
  if (!Number.isFinite(timestamp)) return ''
  const date = new Date(timestamp)
  const pad = part => String(part).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function buildCacheFallbackMessage(options = {}, cachedAt = '') {
  const baseMessage = String(options.offlineMessage || '').trim() || '网络不可用，已回退到最近缓存'
  const formattedTime = formatCacheTime(cachedAt)
  return formattedTime ? `${baseMessage}（缓存时间：${formattedTime}）` : baseMessage
}

async function findFallbackCacheEntry(options = {}) {
  const baseKey = String(options.baseKey || '').trim()
  if (!baseKey) return null
  const requestMethod = String(options.method || 'get').toLowerCase()
  const requestUrl = String(options.url || '')
  const requestSignature = String(options.requestSignature || '')
  const requestScope = String(options.scope || '')
  const entries = await listCacheEntries()
  return entries
    .filter(entry => {
      const meta = entry && entry.meta ? entry.meta : {}
      const request = meta.request || {}
      const entryBaseKey = String(meta.baseKey || '')
      const entryKey = String(entry.key || '')
      const entryScope = String(meta.scope || entry.scope || '')
      const entryMethod = String(request.method || 'get').toLowerCase()
      const entryUrl = String(request.url || '')
      if (!(entryBaseKey === baseKey || entryKey === baseKey || entryKey.startsWith(`${baseKey}::`))) {
        return false
      }
      if (requestUrl && entryUrl && entryUrl !== requestUrl) {
        return false
      }
      if (entryMethod !== requestMethod) {
        return false
      }
      if (requestScope && entryScope && entryScope !== requestScope) {
        return false
      }
      return String(meta.requestSignature || '') !== requestSignature
    })
    .sort((left, right) => String(right.updatedAt || '').localeCompare(String(left.updatedAt || '')))[0] || null
}

export async function fetchCachedResource(options = {}) {
  const cacheKey = options.cacheKey
  const scope = options.scope || ''
  const context = options.context || {}
  if (!cacheKey) {
    throw new Error('cacheKey不能为空')
  }
  const requestSignature = buildRequestSignature({
    method: options.method || 'get',
    url: options.url,
    params: options.params,
    data: options.data
  })
  const resolvedCacheKey = resolveCacheKey(cacheKey, {
    method: options.method || 'get',
    url: options.url,
    params: options.params,
    data: options.data
  })
  const routePath = currentRoutePath()
  const fallbackStateKey = buildFallbackStateKey(cacheKey, options.fallbackStateKey)

  try {
    const response = await options.loader()
    const rawData = unwrapResponseData(response)
    await putCacheEntry(resolvedCacheKey, rawData, buildRequestMeta({
      scope,
      baseKey: cacheKey,
      requestSignature,
      method: options.method || 'get',
      url: options.url,
      params: options.params,
      data: options.data,
      responseDataPath: options.responseDataPath,
      patientId: context.patientId
    }))
    clearCacheFallback(fallbackStateKey)
    return {
      data: await augmentCachedData(scope, rawData, context),
      fromCache: false
    }
  } catch (error) {
    if (!isOfflineError(error)) {
      clearCacheFallback(fallbackStateKey)
      throw error
    }
    const cached = await getCacheEntry(resolvedCacheKey) || await findFallbackCacheEntry({
      baseKey: cacheKey,
      requestSignature,
      method: options.method || 'get',
      scope,
      url: options.url
    })
    if (!cached) {
      clearCacheFallback(fallbackStateKey)
      throw error
    }
    if (cached.key && cached.key !== resolvedCacheKey) {
      await touchCacheEntry(cached.key)
    }
    activateCacheFallback({
      key: fallbackStateKey,
      baseKey: cacheKey,
      scope,
      routePath,
      cachedAt: cached.updatedAt || cached.lastAccessedAt || '',
      reason: isBrowserOnline() ? 'network-error' : 'offline'
    })
    defaultNotify(options.notifier, buildCacheFallbackMessage(options, cached.updatedAt || cached.lastAccessedAt || ''))
    return {
      data: await augmentCachedData(scope, cached.data, Object.assign({}, cached.meta || {}, context)),
      fromCache: true,
      cachedAt: cached.updatedAt
    }
  }
}

async function queueMutation(entityType, action, payload, notifier) {
  const queued = await enqueueMutation({ entityType, action, payload })
  defaultNotify(notifier, '已离线保存，待同步')
  if (isBrowserOnline()) {
    syncPendingQueue().catch(() => {})
  }
  return {
    offline: true,
    queued,
    data: queued && queued.payload ? queued.payload : payload
  }
}

async function submitMutation({ entityType, action, payload, request, notifier }) {
  if (shouldQueueDirectly(payload)) {
    return queueMutation(entityType, action, payload, notifier)
  }
  try {
    const response = await request()
    return {
      offline: false,
      data: unwrapResponseData(response),
      response
    }
  } catch (error) {
    if (!isOfflineError(error)) {
      throw error
    }
    return queueMutation(entityType, action, payload, notifier)
  }
}

export function savePatient(payload, options = {}) {
  const isEdit = options.isEdit === true
  return submitMutation({
    entityType: 'patient',
    action: isEdit ? 'edit' : 'add',
    payload,
    notifier: options.notifier,
    request: () => isEdit ? axios.put('/patients/edit', payload) : axios.post('/patients/add', payload)
  })
}

export function saveAppointment(payload, options = {}) {
  const isEdit = options.isEdit === true
  return submitMutation({
    entityType: 'appointment',
    action: isEdit ? 'edit' : 'add',
    payload,
    notifier: options.notifier,
    request: () => isEdit ? axios.put('/appointments/edit', payload) : axios.post('/appointments/add', payload)
  })
}

export function saveMedicalRecord(payload, options = {}) {
  const isEdit = options.isEdit === true
  return submitMutation({
    entityType: 'medicalRecord',
    action: isEdit ? 'edit' : 'add',
    payload,
    notifier: options.notifier,
    request: () => isEdit ? axios.put('/medical-records/edit', payload) : axios.post('/medical-records/add', payload)
  })
}

const SESSION_KEY = 'adminUserData'
const PERSISTENT_SESSION_KEY = 'adminUserDataPersistent'
const LEGACY_LOCAL_STORAGE_KEY = 'userData'
export const ADMIN_SESSION_EVENT = 'admin-session-changed'

function canUseWindow() {
  return typeof window !== 'undefined'
}

function readRawSession() {
  if (!canUseWindow() || !window.sessionStorage) return ''
  return window.sessionStorage.getItem(SESSION_KEY) || ''
}

function readPersistentSession() {
  if (!canUseWindow() || !window.localStorage) return ''
  return window.localStorage.getItem(PERSISTENT_SESSION_KEY) || ''
}

function parseSession(raw) {
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch (error) {
    return null
  }
}

function emitSessionChanged() {
  if (!canUseWindow()) return
  window.dispatchEvent(new Event(ADMIN_SESSION_EVENT))
}

export function getAdminSession() {
  if (!canUseWindow()) return null
  const current = parseSession(readRawSession())
  if (current) return current

  const persistent = parseSession(readPersistentSession())
  if (persistent) {
    if (window.sessionStorage) {
      window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(persistent))
    }
    return persistent
  }

  const legacyRaw = window.localStorage
    ? window.localStorage.getItem(LEGACY_LOCAL_STORAGE_KEY)
    : ''
  const legacySession = parseSession(legacyRaw)
  if (!legacySession) return null

  window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(legacySession))
  window.localStorage.setItem(PERSISTENT_SESSION_KEY, JSON.stringify(legacySession))
  window.localStorage.removeItem(LEGACY_LOCAL_STORAGE_KEY)
  return legacySession
}

export function saveAdminSession(user) {
  if (!canUseWindow() || !window.sessionStorage) return
  const normalizedUser = user && typeof user === 'object' ? user : {}
  window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(normalizedUser))
  if (window.localStorage) {
    window.localStorage.setItem(PERSISTENT_SESSION_KEY, JSON.stringify(normalizedUser))
    window.localStorage.removeItem(LEGACY_LOCAL_STORAGE_KEY)
  }
  emitSessionChanged()
}

export function clearAdminSession() {
  if (!canUseWindow()) return
  if (window.sessionStorage) {
    window.sessionStorage.removeItem(SESSION_KEY)
  }
  if (window.localStorage) {
    window.localStorage.removeItem(PERSISTENT_SESSION_KEY)
    window.localStorage.removeItem(LEGACY_LOCAL_STORAGE_KEY)
  }
  emitSessionChanged()
}

export function hasAdminSession() {
  const session = getAdminSession()
  return !!(session && (session.id || session.username || session.name))
}

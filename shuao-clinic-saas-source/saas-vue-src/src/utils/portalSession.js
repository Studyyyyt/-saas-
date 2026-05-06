const PATIENT_PORTAL_SESSION_KEY = 'patientPortalSession'
const STAFF_PORTAL_SESSION_KEY = 'staffPortalSession'

function getSessionStorage() {
  if (typeof window === 'undefined') return null
  try {
    return window.sessionStorage
  } catch (error) {
    return null
  }
}

function readSession(key) {
  const storage = getSessionStorage()
  if (!storage) return null
  try {
    const raw = storage.getItem(key)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch (error) {
    return null
  }
}

function writeSession(key, value) {
  const storage = getSessionStorage()
  if (!storage || !value) return
  storage.setItem(key, JSON.stringify(value))
}

function normalizeText(value) {
  return String(value || '').trim()
}

export function getPatientPortalSession() {
  return readSession(PATIENT_PORTAL_SESSION_KEY)
}

export function savePatientPortalSessionFromQuery(query) {
  const patientId = normalizeText(query && query.patientId)
  const portalToken = normalizeText(query && query.portalToken)
  if (!patientId || !portalToken) {
    return getPatientPortalSession()
  }
  const session = { patientId, portalToken }
  writeSession(PATIENT_PORTAL_SESSION_KEY, session)
  return session
}

export function getPatientPortalQuery(query = {}) {
  const session = savePatientPortalSessionFromQuery(query) || getPatientPortalSession()
  if (!session) return Object.assign({}, query)
  return Object.assign({}, query, {
    patientId: session.patientId,
    portalToken: session.portalToken
  })
}

export function getStaffPortalSession() {
  return readSession(STAFF_PORTAL_SESSION_KEY)
}

export function saveStaffPortalSessionFromQuery(query) {
  const accountId = normalizeText(query && query.accountId)
  const staffToken = normalizeText(query && query.staffToken)
  const doctorName = normalizeText(query && query.doctorName)
  const currentSession = getStaffPortalSession()
  if (!accountId || !staffToken) {
    return currentSession
  }
  const session = {
    accountId,
    staffToken,
    doctorName: doctorName || (currentSession && currentSession.doctorName) || ''
  }
  writeSession(STAFF_PORTAL_SESSION_KEY, session)
  return session
}

export function getStaffPortalQuery(query = {}) {
  const session = saveStaffPortalSessionFromQuery(query) || getStaffPortalSession()
  if (!session) return Object.assign({}, query)
  return Object.assign({}, query, {
    accountId: session.accountId,
    staffToken: session.staffToken,
    doctorName: normalizeText(query && query.doctorName) || session.doctorName || ''
  })
}

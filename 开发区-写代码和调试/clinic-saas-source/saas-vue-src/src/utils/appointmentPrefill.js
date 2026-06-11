const STORAGE_KEY = 'appointment_pending_patient_prefill'
const MAX_AGE_MS = 30 * 60 * 1000

function canUseWindow() {
  return typeof window !== 'undefined' && !!window.sessionStorage
}

function normalizePatientPayload(patient) {
  if (!patient || typeof patient !== 'object') return null
  const patientId = Number(patient.patient_id || patient.id)
  const patientName = String(patient.patient_name || patient.name || '').trim()
  const patientPhone = String(patient.patient_phone || patient.phone || '').trim()
  const appointmentPurpose = String(patient.appointment_purpose || patient.chief_project || '').trim()
  if (!patientName) return null
  const createdAt = Number(patient.created_at)
  return {
    patient_id: Number.isFinite(patientId) && patientId > 0 ? patientId : null,
    patient_name: patientName,
    patient_phone: patientPhone,
    appointment_purpose: appointmentPurpose,
    created_at: Number.isFinite(createdAt) && createdAt > 0 ? createdAt : Date.now()
  }
}

function readRawPayload() {
  if (!canUseWindow()) return ''
  return window.sessionStorage.getItem(STORAGE_KEY) || ''
}

function parsePayload(raw) {
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw)
    return normalizePatientPayload(parsed)
  } catch (error) {
    return null
  }
}

function isExpired(payload) {
  if (!payload || !payload.created_at) return true
  return (Date.now() - Number(payload.created_at)) > MAX_AGE_MS
}

export function savePendingAppointmentPatient(patient) {
  const payload = normalizePatientPayload(patient)
  if (!payload || !canUseWindow()) return null
  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  return payload
}

export function readPendingAppointmentPatient() {
  const payload = parsePayload(readRawPayload())
  if (!payload) return null
  if (isExpired(payload)) {
    clearPendingAppointmentPatient()
    return null
  }
  return payload
}

export function clearPendingAppointmentPatient() {
  if (!canUseWindow()) return
  window.sessionStorage.removeItem(STORAGE_KEY)
}

export function consumePendingAppointmentPatient() {
  const payload = readPendingAppointmentPatient()
  clearPendingAppointmentPatient()
  return payload
}

const STORAGE_KEY = 'recent_patient_ids'
const MAX_RECENT_PATIENTS = 20

function normalizePatientId(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function readRecentPatientIds() {
  if (typeof window === 'undefined' || !window.localStorage) return []
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed.map(normalizePatientId).filter(Boolean) : []
  } catch (error) {
    return []
  }
}

export function rememberRecentPatient(patientOrId) {
  const id = normalizePatientId(patientOrId && typeof patientOrId === 'object' ? patientOrId.id : patientOrId)
  if (!id || typeof window === 'undefined' || !window.localStorage) return []
  const recentIds = readRecentPatientIds()
  const nextIds = [id].concat(recentIds.filter(item => item !== id)).slice(0, MAX_RECENT_PATIENTS)
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextIds))
  } catch (error) {
    return recentIds
  }
  return nextIds
}

export function sortPatientsByRecent(list) {
  const patients = Array.isArray(list) ? list.slice() : []
  const recentIds = readRecentPatientIds()
  const recentIndexMap = recentIds.reduce((accumulator, id, index) => {
    accumulator[id] = index
    return accumulator
  }, {})

  return patients.sort((first, second) => {
    const firstId = normalizePatientId(first && first.id)
    const secondId = normalizePatientId(second && second.id)
    const firstRecentIndex = recentIndexMap[firstId]
    const secondRecentIndex = recentIndexMap[secondId]
    const firstIsRecent = firstRecentIndex !== undefined
    const secondIsRecent = secondRecentIndex !== undefined

    if (firstIsRecent && secondIsRecent) return firstRecentIndex - secondRecentIndex
    if (firstIsRecent) return -1
    if (secondIsRecent) return 1

    const firstNumericId = Number(firstId)
    const secondNumericId = Number(secondId)
    if (Number.isFinite(firstNumericId) && Number.isFinite(secondNumericId) && firstNumericId !== secondNumericId) {
      return secondNumericId - firstNumericId
    }
    return secondId.localeCompare(firstId)
  })
}

export function getPatientAge(patient) {
  if (!patient) return ''
  const ageValue = Number(patient.age)
  if (Number.isFinite(ageValue) && ageValue >= 0) {
    return Math.floor(ageValue)
  }
  return calculateAgeFromBirthDate(patient.date_of_birth)
}

function calculateAgeFromBirthDate(dateValue) {
  if (!dateValue) return ''
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) return ''

  const now = new Date()
  let age = now.getFullYear() - date.getFullYear()
  const monthDiff = now.getMonth() - date.getMonth()
  const dayDiff = now.getDate() - date.getDate()

  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    age -= 1
  }

  return age >= 0 ? age : ''
}

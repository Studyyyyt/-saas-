import Vue from 'vue'

const bus = new Vue()

export function openConsultationCreateDialog(payload = {}) {
  bus.$emit('consultation-create-open', payload)
}

export function onConsultationCreateDialogOpen(handler) {
  bus.$on('consultation-create-open', handler)
}

export function offConsultationCreateDialogOpen(handler) {
  bus.$off('consultation-create-open', handler)
}

export function emitConsultationSaved(payload = {}) {
  bus.$emit('consultation-saved', payload)
}

export function onConsultationSaved(handler) {
  bus.$on('consultation-saved', handler)
}

export function offConsultationSaved(handler) {
  bus.$off('consultation-saved', handler)
}

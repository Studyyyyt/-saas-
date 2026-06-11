import { shallowMount } from '@vue/test-utils'
import ElementUI from 'element-ui'
import Vue from 'vue'
import AppointmentView2 from '@/views/Manager/AppointmentView2.vue'

Vue.use(ElementUI)

describe('AppointmentView2 patient suggestions', () => {
  it('filters patient suggestions by entered keyword and fills selected patient name', async () => {
    const wrapper = shallowMount(AppointmentView2, {
      mocks: {
        $message: { warning: jest.fn(), success: jest.fn(), error: jest.fn() }
      }
    })

    await wrapper.setData({
      allPatients: [
        { id: 1, name: '张三' },
        { id: 2, name: '张小明' },
        { id: 3, name: '李四' }
      ],
      editItem: {
        id: null,
        patient_name: '张',
        appointment_date: '2026-04-21',
        appointment_time: '10:00:00',
        doctor_name: '李医生',
        appointment_purpose: '复诊提醒A1',
        status: '待治疗'
      },
      patientSuggestionVisible: true
    })

    expect(wrapper.vm.filteredPatientSuggestions.map(item => item.name)).toEqual(['张三', '张小明'])

    wrapper.vm.selectPatientSuggestion({ id: 2, name: '张小明' })
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.editItem.patient_name).toBe('张小明')
    expect(wrapper.vm.patientSuggestionVisible).toBe(false)
  })
})

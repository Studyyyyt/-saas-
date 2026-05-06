<template>
  <div class="page-wrap">
    <div class="page-title-bar">
      <div>
        <div class="page-kicker">预约管理</div>
        <h2>预约列表</h2>
      </div>
      <div class="page-desc">旧版预约入口已升级为患者主键关联模式。</div>
    </div>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="searchName" placeholder="输入预约人姓名" clearable style="width: 260px"></el-input>
        <el-date-picker
          v-model="searchDate"
          type="date"
          value-format="yyyy-MM-dd"
          format="yyyy-MM-dd"
          placeholder="选择预约日期"
          style="width: 180px"
        ></el-date-picker>
        <el-button type="primary" @click="searchAppointments">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="success" @click="showAddModal">新增预约</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="appointments" stripe>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="patient_name" label="预约人姓名"></el-table-column>
        <el-table-column prop="appointment_date" label="预约日期"></el-table-column>
        <el-table-column prop="appointment_time" label="预约时间"></el-table-column>
        <el-table-column prop="doctor_name" label="医生姓名"></el-table-column>
        <el-table-column prop="appointment_purpose" label="预约目的"></el-table-column>
        <el-table-column prop="status" label="状态" width="100"></el-table-column>
        <el-table-column label="操作" width="140">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="isEditing ? '编辑预约' : '新增预约'" :visible.sync="dialogVisible" width="560px">
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="患者姓名" class="patient-name-form-item">
          <div class="patient-suggest-wrap" @click.stop>
            <el-input
              v-model="editItem.patient_name"
              placeholder="请输入患者姓名/手机号/首字母"
              @input="handlePatientNameInput"
              @focus="handlePatientNameFocus"
              @blur="handlePatientNameBlur"
            ></el-input>
            <div v-if="patientSuggestionVisible && filteredPatientSuggestions.length" class="patient-suggestion-panel">
              <div
                v-for="patient in filteredPatientSuggestions"
                :key="patient.id || patient.name"
                class="patient-suggestion-item"
                @mousedown.prevent="selectPatientSuggestion(patient)"
              >
                <div class="patient-suggestion-name">{{ patient.name }}</div>
                <div v-if="patient.phone" class="patient-suggestion-meta">{{ patient.phone }}</div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="editItem.appointment_date"
            type="date"
            value-format="yyyy-MM-dd"
            format="yyyy-MM-dd"
            placeholder="选择预约日期"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-time-picker
            v-model="editItem.appointment_time"
            value-format="HH:mm:ss"
            format="HH:mm"
            placeholder="选择预约时间"
            style="width: 100%"
          ></el-time-picker>
        </el-form-item>
        <el-form-item label="医生姓名">
          <el-input v-model="editItem.doctor_name"></el-input>
        </el-form-item>
        <el-form-item label="预约目的">
          <el-input v-model="editItem.appointment_purpose" type="textarea" :rows="2"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editItem.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待治疗" value="待治疗"></el-option>
            <el-option label="已治疗" value="已治疗"></el-option>
            <el-option label="已取消" value="已取消"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          v-if="isEditing && editItem.id"
          type="danger"
          plain
          @click="handleDelete(editItem.id, { closeDialog: true })"
        >删除预约</el-button>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSave">{{ isEditing ? '保存' : '确认新增' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'

const createDefaultAppointment = () => ({
  id: null,
  patient_id: null,
  patient_name: '',
  appointment_date: '',
  appointment_time: '',
  doctor_name: '',
  appointment_purpose: '',
  status: '待治疗'
})

export default {
  name: 'AppointmentView',
  data() {
    return {
      searchName: '',
      searchDate: '',
      appointments: [],
      dialogVisible: false,
      isEditing: false,
      editItem: createDefaultAppointment(),
      allPatients: [],
      patientSuggestionVisible: false,
      patientSuggestionBlurTimer: null
    }
  },
  computed: {
    filteredPatientSuggestions() {
      return (this.allPatients || []).slice(0, 8)
    }
  },
  mounted() {
    this.fetchAppointments()
  },
  methods: {
    fetchAppointments() {
      axios.get('/appointments/selectAll', {
        params: { page: 1, size: 1000 }
      }).then(response => {
        const data = response.data.data || {}
        this.appointments = data.list || []
      }).catch(error => {
        console.error('Error fetching appointments:', error)
        showApiError(this, '获取预约列表', error)
      })
    },
    loadPatients(keyword = '') {
      axios.get('/patients/search', {
        params: {
          keyword: String(keyword || '').trim(),
          page: 1,
          size: 20
        }
      }).then(response => {
        const data = response.data.data || {}
        const list = data.list || response.data.data || []
        this.allPatients = (Array.isArray(list) ? list : [])
          .map(item => ({
            id: item.id,
            name: item.name || '',
            phone: item.phone || ''
          }))
          .filter(item => item.name)
      }).catch(error => {
        console.error('Error fetching patients:', error)
        this.allPatients = []
      })
    },
    searchAppointments() {
      if (!this.searchName && !this.searchDate) {
        this.fetchAppointments()
        return
      }
      axios.get('/appointments/selectAll', {
        params: { page: 1, size: 1000 }
      }).then(response => {
        const data = response.data.data || {}
        let list = data.list || []
        if (this.searchName) {
          const keyword = String(this.searchName).trim().toLowerCase()
          list = list.filter(item => String(item.patient_name || '').toLowerCase().includes(keyword))
        }
        if (this.searchDate) {
          list = list.filter(item => item.appointment_date === this.searchDate)
        }
        this.appointments = list
      }).catch(error => {
        console.error('Error searching appointments:', error)
        showApiError(this, '查询预约', error)
      })
    },
    resetSearch() {
      this.searchName = ''
      this.searchDate = ''
      this.fetchAppointments()
    },
    showAddModal() {
      this.isEditing = false
      this.editItem = createDefaultAppointment()
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEditing = true
      this.editItem = Object.assign(createDefaultAppointment(), row)
      this.dialogVisible = true
    },
    closeDialog() {
      this.dialogVisible = false
      this.isEditing = false
      this.editItem = createDefaultAppointment()
      this.patientSuggestionVisible = false
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
    },
    handlePatientNameInput() {
      this.editItem.patient_id = null
      this.patientSuggestionVisible = true
      this.loadPatients(this.editItem.patient_name)
    },
    handlePatientNameFocus() {
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
      this.patientSuggestionVisible = true
      this.loadPatients(this.editItem.patient_name)
    },
    handlePatientNameBlur() {
      this.patientSuggestionBlurTimer = setTimeout(() => {
        this.patientSuggestionVisible = false
      }, 120)
    },
    selectPatientSuggestion(patient) {
      this.editItem.patient_id = patient && patient.id ? patient.id : null
      this.editItem.patient_name = patient && patient.name ? patient.name : ''
      this.patientSuggestionVisible = false
    },
    validateAppointment() {
      if (!this.editItem.patient_id) return '请选择患者'
      if (!this.editItem.patient_name || !String(this.editItem.patient_name).trim()) return '患者姓名必填'
      if (!this.editItem.appointment_date) return '预约日期必填'
      if (!this.editItem.appointment_time) return '预约时间必填'
      if (!this.editItem.doctor_name || !String(this.editItem.doctor_name).trim()) return '医生姓名必填'
      if (!this.editItem.appointment_purpose || !String(this.editItem.appointment_purpose).trim()) return '预约目的必填'
      if (!this.editItem.status || !String(this.editItem.status).trim()) return '预约状态必填'
      return ''
    },
    handleSave() {
      const validationMessage = this.validateAppointment()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      const request = this.isEditing
        ? axios.put('/appointments/edit', this.editItem)
        : axios.post('/appointments/add', this.editItem)
      request.then(response => {
        if (response.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.closeDialog()
          this.fetchAppointments()
        } else {
          this.$message.error(response.data.msg || (this.isEditing ? '编辑失败' : '新增失败'))
        }
      }).catch(error => {
        console.error('Error saving appointment:', error)
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '保存失败')
      })
    },
    handleDelete(id, options = {}) {
      const appointmentId = Number(id)
      if (!appointmentId) {
        this.$message.warning('当前预约缺少有效ID，无法删除')
        return
      }
      this.$confirm('删除后无法恢复，确认删除这条预约吗？', '删除预约', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/appointments/delete/${appointmentId}`)
          .then(() => {
            this.$message.success('删除成功')
            if (options.closeDialog) {
              this.closeDialog()
            }
            this.fetchAppointments()
          })
          .catch(error => {
            console.error('Error deleting appointment:', error)
            this.$message.error((error.response && error.response.data && error.response.data.msg) || '删除失败')
          })
      }).catch(error => {
        if (error !== 'cancel' && error !== 'close') {
          console.error('Error confirming appointment deletion:', error)
        }
      })
    }
  }
}
</script>

<style scoped>
.page-wrap { display:flex; flex-direction:column; gap:14px; }
.page-title-bar { display:flex; align-items:flex-end; justify-content:space-between; gap:16px; }
.page-kicker { color:#64748b; font-size:13px; margin-bottom:4px; }
.page-title-bar h2 { margin:0; color:#0f172a; font-size:24px; }
.page-desc { color:#64748b; font-size:13px; }
.filter-card { border-radius:18px; }
.filter-row { display:flex; flex-wrap:wrap; gap:10px; align-items:center; }
.dialog-footer { text-align:right; }
.patient-suggest-wrap { position: relative; }
.patient-suggestion-panel {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
  z-index: 20;
}
.patient-suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f2f4f7;
}
.patient-suggestion-item:last-child { border-bottom: none; }
.patient-suggestion-item:hover { background: #f5f7fa; }
.patient-suggestion-name { color: #303133; font-size: 14px; line-height: 1.4; }
.patient-suggestion-meta { margin-top: 4px; color: #909399; font-size: 12px; }
</style>

<template>
  <div class="page-wrap">
    <el-card class="top-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">患者管理</div>
          <h2>回访管理</h2>
          <p>统一查看待回访与已回访记录，医生填写回访结果后自动标记为已回访。</p>
        </div>
        <div class="head-stats">
          <div class="mini-stat warn">
            <div class="mini-num">{{ pendingCount }}</div>
            <div class="mini-label">待回访</div>
          </div>
          <div class="mini-stat success">
            <div class="mini-num">{{ completedCount }}</div>
            <div class="mini-label">已回访</div>
          </div>
          <div class="mini-stat accent">
            <div class="mini-num">{{ overdueCount }}</div>
            <div class="mini-label">逾期待回访</div>
          </div>
          <div class="mini-stat">
            <div class="mini-num">{{ currentMonthCount }}</div>
            <div class="mini-label">本月计划</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="query-card" shadow="never">
      <div class="query-row">
        <el-input
          v-model="keyword"
          class="query-input"
          clearable
          placeholder="支持患者姓名/手机号搜索"
          @input="applyFilters"
          @clear="applyFilters"
        />
        <el-select v-model="statusFilter" class="query-select" @change="applyFilters">
          <el-option label="全部状态" value="ALL"></el-option>
          <el-option label="待回访" value="PENDING"></el-option>
          <el-option label="已回访" value="COMPLETED"></el-option>
        </el-select>
        <el-select
          v-model="doctorFilter"
          class="query-select"
          :disabled="isDoctor"
          @change="applyFilters"
        >
          <el-option label="全部医生" value="ALL"></el-option>
          <el-option
            v-for="doctor in doctors"
            :key="doctor.id"
            :label="doctor.name"
            :value="doctor.id"
          ></el-option>
        </el-select>
        <el-radio-group v-model="dateQuickFilter" size="small" @change="handleDateQuickChange">
          <el-radio-button label="today">今天</el-radio-button>
          <el-radio-button label="tomorrow">明天</el-radio-button>
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="all">全部</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="yyyy-MM-dd"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 220px"
          @change="applyFilters"
        />
        <el-button icon="el-icon-refresh" @click="loadAll">刷新</el-button>
        <el-button type="primary" plain icon="el-icon-circle-plus-outline" @click="openAddDialog">新增回访</el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        :data="pagedRows"
        stripe
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="followupStatusType(scope.row)" size="mini">{{ followupStatusLabel(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计划回访" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.followup_date) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="patient_name" label="患者姓名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="patient_phone" label="手机号" min-width="130">
          <template slot-scope="scope">{{ scope.row.patient_phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="doctor_name" label="负责医生" min-width="120">
          <template slot-scope="scope">{{ scope.row.doctor_name || '-' }}</template>
        </el-table-column>
        <el-table-column prop="followup_type" label="回访方式" width="100" />
        <el-table-column prop="followup_project" label="回访项目" min-width="140" show-overflow-tooltip>
          <template slot-scope="scope">{{ scope.row.followup_project || '-' }}</template>
        </el-table-column>
        <el-table-column label="回访结果" min-width="220" show-overflow-tooltip>
          <template slot-scope="scope">
            <span :class="{ 'result-empty': !followupResultText(scope.row) }">
              {{ followupResultText(scope.row) || '待填写' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="下次回访" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.next_followup_date) || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              :type="isFollowupCompleted(scope.row) ? 'primary' : 'warning'"
              plain
              @click="openEditDialog(scope.row)"
            >{{ isFollowupCompleted(scope.row) ? '编辑结果' : '填写结果' }}</el-button>
            <el-button size="mini" type="success" plain @click="goPatient360(scope.row)">患者详情</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!filteredRows.length" description="暂无回访记录"></el-empty>

      <div v-else class="pagination-row">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredRows.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="580px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="患者姓名">
          <div class="patient-suggest-wrap" @click.stop>
            <el-input
              v-model="form.patient_name"
              :disabled="isEditing"
              clearable
              placeholder="请输入患者姓名/手机号/首字母"
              @input="handlePatientNameInput"
              @focus="handlePatientNameFocus"
              @blur="handlePatientNameBlur"
            ></el-input>
            <div v-if="!isEditing && patientSuggestionVisible && filteredPatientSuggestions.length" class="patient-suggestion-panel">
              <div
                v-for="patient in filteredPatientSuggestions"
                :key="patient.id"
                class="patient-suggestion-item"
                @mousedown.prevent="selectPatientSuggestion(patient)"
              >
                <div class="patient-suggestion-name">{{ patient.name }}</div>
                <div class="patient-suggestion-meta">ID {{ patient.id }}<span v-if="patient.phone"> · {{ patient.phone }}</span></div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="负责医生">
          <el-select v-model="form.doctor_account_id" placeholder="请选择负责医生" style="width:100%">
            <el-option
              v-for="doctor in doctors"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="计划回访">
          <el-date-picker
            v-model="form.followup_date"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="回访方式">
          <el-select v-model="form.followup_type" style="width:100%">
            <el-option label="电话" value="电话"></el-option>
            <el-option label="复诊" value="复诊"></el-option>
            <el-option label="线上" value="线上"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="回访项目">
          <el-select v-model="form.followup_project" placeholder="请选择回访项目" clearable style="width:100%">
            <el-option label="术后关怀" value="术后关怀"></el-option>
            <el-option label="正畸复诊提醒" value="正畸复诊提醒"></el-option>
            <el-option label="种植术后随访" value="种植术后随访"></el-option>
            <el-option label="满意度调查" value="满意度调查"></el-option>
            <el-option label="欠费催缴" value="欠费催缴"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="回访结果">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="4"
            placeholder="回访后填写结果；留空则显示为待回访"
          ></el-input>
        </el-form-item>
        <el-form-item label="下次回访">
          <el-date-picker
            v-model="form.next_followup_date"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEditing ? '保存' : '新增' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'

function createEmptyForm() {
  return {
    id: null,
    patient_id: null,
    patient_name: '',
    patient_phone: '',
    doctor_account_id: null,
    doctor_name: '',
    followup_date: '',
    followup_type: '电话',
    followup_project: '',
    summary: '',
    next_followup_date: ''
  }
}

export default {
  name: 'FollowupManagementView',
  data() {
    return {
      currentUser: getAdminSession() || {},
      doctors: [],
      rows: [],
      filteredRows: [],
      keyword: '',
      statusFilter: 'ALL',
      doctorFilter: 'ALL',
      dateQuickFilter: 'all',
      dateRange: null,
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      isEditing: false,
      form: createEmptyForm(),
      allPatients: [],
      patientSuggestionVisible: false,
      patientSuggestionBlurTimer: null
    }
  },
  computed: {
    normalizedRole() {
      const role = String((this.currentUser && this.currentUser.role) || '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    isDoctor() {
      return this.normalizedRole === 'doctor'
    },
    currentDoctorId() {
      const id = Number(this.currentUser && this.currentUser.id)
      return Number.isFinite(id) && id > 0 ? id : 0
    },
    currentDoctorName() {
      return String((this.currentUser && this.currentUser.name) || '').trim()
    },
    pagedRows() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredRows.slice(start, start + this.pageSize)
    },
    pendingCount() {
      return this.filteredRows.filter(item => !this.isFollowupCompleted(item)).length
    },
    completedCount() {
      return this.filteredRows.filter(item => this.isFollowupCompleted(item)).length
    },
    overdueCount() {
      return this.filteredRows.filter(item => this.isFollowupOverdue(item)).length
    },
    currentMonthCount() {
      const now = new Date()
      const monthPrefix = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      return this.filteredRows.filter(item => String(this.dateKey(item && item.followup_date) || '').startsWith(monthPrefix)).length
    },
    dialogTitle() {
      return this.isEditing ? '填写/编辑回访结果' : '新增回访计划'
    },
    filteredPatientSuggestions() {
      return (this.allPatients || []).slice(0, 8)
    }
  },
  mounted() {
    this.initializeDoctorFilter()
    this.loadDoctors()
    this.loadAll()
    this.handleRouteQuery()
  },
  beforeDestroy() {
    if (this.patientSuggestionBlurTimer) {
      clearTimeout(this.patientSuggestionBlurTimer)
      this.patientSuggestionBlurTimer = null
    }
  },
  methods: {
    buildEmptyForm() {
      return createEmptyForm()
    },
    initializeDoctorFilter() {
      if (this.isDoctor && this.currentDoctorId > 0) {
        this.doctorFilter = this.currentDoctorId
      } else {
        this.doctorFilter = 'ALL'
      }
    },
    normalizeDoctor(item) {
      if (!item || !item.id || !item.name) return null
      return { id: Number(item.id), name: String(item.name).trim() }
    },
    normalizeFollowup(item) {
      return {
        ...item,
        id: item && item.id ? Number(item.id) : null,
        patient_id: item && item.patient_id ? Number(item.patient_id) : null,
        doctor_account_id: item && item.doctor_account_id ? Number(item.doctor_account_id) : null,
        patient_name: String((item && item.patient_name) || '').trim(),
        patient_phone: String((item && item.patient_phone) || '').trim(),
        doctor_name: String((item && item.doctor_name) || '').trim(),
        followup_type: String((item && item.followup_type) || '电话').trim() || '电话',
        followup_project: String((item && item.followup_project) || '').trim(),
        summary: String((item && item.summary) || '').trim()
      }
    },
    dateKey(value) {
      const text = String(value || '').trim()
      if (!text) return ''
      const matched = text.match(/^(\d{4}-\d{2}-\d{2})/)
      if (matched) return matched[1]
      const parsed = new Date(value)
      if (Number.isNaN(parsed.getTime())) return ''
      return `${parsed.getFullYear()}-${String(parsed.getMonth() + 1).padStart(2, '0')}-${String(parsed.getDate()).padStart(2, '0')}`
    },
    formatDateTime(value) {
      if (!value) return ''
      const text = String(value || '').trim()
      const matched = text.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/)
      if (matched) {
        return `${matched[1]}-${matched[2]}-${matched[3]} ${matched[4]}:${matched[5]}`
      }
      const parsed = new Date(value)
      if (Number.isNaN(parsed.getTime())) {
        return text.slice(0, 19).replace('T', ' ')
      }
      const year = parsed.getFullYear()
      const month = String(parsed.getMonth() + 1).padStart(2, '0')
      const day = String(parsed.getDate()).padStart(2, '0')
      const hour = String(parsed.getHours()).padStart(2, '0')
      const minute = String(parsed.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    },
    normalizeDateTimeInput(value) {
      if (!value) return ''
      const text = String(value || '').trim()
      const matched = text.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/)
      if (matched) {
        return `${matched[1]}-${matched[2]}-${matched[3]} ${matched[4]}:${matched[5]}:${matched[6] || '00'}`
      }
      const parsed = new Date(value)
      if (Number.isNaN(parsed.getTime())) {
        return text.slice(0, 19).replace('T', ' ')
      }
      const year = parsed.getFullYear()
      const month = String(parsed.getMonth() + 1).padStart(2, '0')
      const day = String(parsed.getDate()).padStart(2, '0')
      const hour = String(parsed.getHours()).padStart(2, '0')
      const minute = String(parsed.getMinutes()).padStart(2, '0')
      const second = String(parsed.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    followupResultText(item) {
      return String((item && item.summary) || '').trim()
    },
    isFollowupCompleted(item) {
      return !!this.followupResultText(item)
    },
    followupStatusLabel(item) {
      return this.isFollowupCompleted(item) ? '已回访' : '待回访'
    },
    followupStatusType(item) {
      return this.isFollowupCompleted(item) ? 'success' : 'warning'
    },
    isFollowupOverdue(item) {
      const dueDate = this.dateKey(item && item.followup_date)
      const today = this.dateKey(new Date())
      return !this.isFollowupCompleted(item) && !!dueDate && dueDate < today
    },
    currentDoctorById(id) {
      return (this.doctors || []).find(item => Number(item.id) === Number(id)) || null
    },
    currentDoctorByName(name) {
      const doctorName = String(name || '').trim()
      if (!doctorName) return null
      return (this.doctors || []).find(item => item.name === doctorName) || null
    },
    loadDoctors() {
      axios.get('/accounts/doctors/active').then(response => {
        this.doctors = (Array.isArray(response.data.data) ? response.data.data : [])
          .map(this.normalizeDoctor)
          .filter(Boolean)
        this.applyFilters()
      }).catch(() => {
        this.doctors = []
      })
    },
    loadAll() {
      axios.get('/followup/selectAllDetail').then(response => {
        const list = Array.isArray(response.data.data) ? response.data.data : []
        this.rows = list.map(this.normalizeFollowup)
        this.applyFilters()
      }).catch(error => {
        console.error('Error loading followups:', error)
        showApiError(this, '加载回访记录', error)
      })
    },
    applyFilters() {
      const keyword = String(this.keyword || '').trim().toLowerCase()
      const today = this.dateKey(new Date())
      const now = new Date()
      const todayDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      const weekStart = new Date(todayDate)
      weekStart.setDate(todayDate.getDate() - todayDate.getDay() + (todayDate.getDay() === 0 ? -6 : 1))
      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)
      const monthPrefix = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      this.filteredRows = (this.rows || []).filter(item => {
        const patientName = String(item.patient_name || '').toLowerCase()
        const patientPhone = String(item.patient_phone || '').toLowerCase()
        const doctorId = Number(item.doctor_account_id)
        const doctorName = String(item.doctor_name || '').trim()
        const selectedDoctor = this.currentDoctorById(this.doctorFilter)
        const itemDate = this.dateKey(item.followup_date)
        const matchesKeyword = !keyword || patientName.includes(keyword) || patientPhone.includes(keyword)
        const matchesStatus = this.statusFilter === 'ALL'
          || (this.statusFilter === 'PENDING' && !this.isFollowupCompleted(item))
          || (this.statusFilter === 'COMPLETED' && this.isFollowupCompleted(item))
        const matchesDoctor = this.doctorFilter === 'ALL'
          || Number(this.doctorFilter) === doctorId
          || (!!selectedDoctor && !!doctorName && selectedDoctor.name === doctorName)
          || (this.isDoctor && !!this.currentDoctorName && doctorName === this.currentDoctorName)
        let matchesDate = true
        if (this.dateRange && this.dateRange.length === 2) {
          matchesDate = !!itemDate && itemDate >= this.dateRange[0] && itemDate <= this.dateRange[1]
        } else if (this.dateQuickFilter === 'today') {
          matchesDate = itemDate === today
        } else if (this.dateQuickFilter === 'tomorrow') {
          const tomorrow = new Date(todayDate)
          tomorrow.setDate(todayDate.getDate() + 1)
          matchesDate = itemDate === this.dateKey(tomorrow)
        } else if (this.dateQuickFilter === 'week') {
          matchesDate = !!itemDate && itemDate >= this.dateKey(weekStart) && itemDate <= this.dateKey(weekEnd)
        } else if (this.dateQuickFilter === 'month') {
          matchesDate = !!itemDate && itemDate.startsWith(monthPrefix)
        }
        return matchesKeyword && matchesStatus && matchesDoctor && matchesDate
      })
      this.currentPage = 1
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handleCurrentChange(page) {
      this.currentPage = page
    },
    handleDateQuickChange() {
      this.dateRange = null
      this.applyFilters()
    },
    handleRouteQuery() {
      const query = this.$route.query
      if (query.action === 'add') {
        this.currentUser = getAdminSession() || {}
        this.isEditing = false
        this.form = this.buildEmptyForm()
        if (query.patientId) {
          this.form.patient_id = Number(query.patientId)
          this.form.patient_name = String(query.patientName || '')
        }
        if (this.currentDoctorId > 0) {
          this.form.doctor_account_id = this.currentDoctorId
          this.form.doctor_name = this.currentDoctorName
        }
        // 清除 query 避免刷新重复触发
        this.$router.replace({ path: this.$route.path, query: {} })
        this.dialogVisible = true
      }
    },
    loadPatients(keyword = '') {
      axios.get('/patients/search', {
        params: {
          keyword: String(keyword || '').trim(),
          page: 1,
          size: 1000
        }
      }).then(response => {
        const data = response.data.data || {}
        const list = Array.isArray(data.list) ? data.list : (Array.isArray(response.data.data) ? response.data.data : [])
        this.allPatients = list
          .map(item => ({
            id: item.id,
            name: item.name || '',
            phone: item.phone || ''
          }))
          .filter(item => item.name)
      }).catch(() => {
        this.allPatients = []
      })
    },
    handlePatientNameInput() {
      if (this.isEditing) return
      this.form.patient_id = null
      this.patientSuggestionVisible = true
      this.loadPatients(this.form.patient_name)
    },
    handlePatientNameFocus() {
      if (this.isEditing) return
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
      this.patientSuggestionVisible = true
      this.loadPatients(this.form.patient_name)
    },
    handlePatientNameBlur() {
      if (this.isEditing) return
      this.patientSuggestionBlurTimer = setTimeout(() => {
        this.patientSuggestionVisible = false
      }, 120)
    },
    selectPatientSuggestion(patient) {
      this.form.patient_id = patient && patient.id ? Number(patient.id) : null
      this.form.patient_name = patient && patient.name ? patient.name : ''
      this.form.patient_phone = patient && patient.phone ? patient.phone : ''
      this.patientSuggestionVisible = false
    },
    openAddDialog() {
      this.currentUser = getAdminSession() || {}
      this.isEditing = false
      this.form = this.buildEmptyForm()
      if (this.currentDoctorId > 0) {
        this.form.doctor_account_id = this.currentDoctorId
        this.form.doctor_name = this.currentDoctorName
      }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      const matchedDoctor = this.currentDoctorById(row.doctor_account_id) || this.currentDoctorByName(row.doctor_name)
      this.isEditing = true
      this.form = {
        id: row.id,
        patient_id: row.patient_id,
        patient_name: row.patient_name || '',
        patient_phone: row.patient_phone || '',
        doctor_account_id: matchedDoctor && matchedDoctor.id ? matchedDoctor.id : row.doctor_account_id,
        doctor_name: row.doctor_name || '',
        followup_date: this.normalizeDateTimeInput(row.followup_date),
        followup_type: row.followup_type || '电话',
        followup_project: row.followup_project || '',
        summary: row.summary || '',
        next_followup_date: this.normalizeDateTimeInput(row.next_followup_date)
      }
      this.dialogVisible = true
    },
    closeDialog() {
      this.dialogVisible = false
      this.isEditing = false
      this.form = this.buildEmptyForm()
      this.patientSuggestionVisible = false
      if (this.patientSuggestionBlurTimer) {
        clearTimeout(this.patientSuggestionBlurTimer)
        this.patientSuggestionBlurTimer = null
      }
    },
    validateForm() {
      if (!this.form.patient_id) return '请选择患者'
      if (!this.form.followup_date) return '计划回访时间不能为空'
      if (!this.form.doctor_account_id) return '请选择负责医生'
      return ''
    },
    submitForm() {
      const validationMessage = this.validateForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      const doctor = this.currentDoctorById(this.form.doctor_account_id)
      this.form.doctor_name = doctor && doctor.name ? doctor.name : (this.form.doctor_name || '')
      const payload = {
        id: this.form.id,
        patient_id: this.form.patient_id,
        doctor_account_id: this.form.doctor_account_id,
        doctor_name: this.form.doctor_name,
        followup_date: this.form.followup_date,
        followup_type: this.form.followup_type,
        followup_project: this.form.followup_project,
        summary: this.form.summary,
        next_followup_date: this.form.next_followup_date
      }
      const request = this.isEditing
        ? axios.put('/followup/edit', payload)
        : axios.post('/followup/add', payload)
      request.then(response => {
        if (response.data.code === '200') {
          this.$message.success(this.isEditing ? '保存成功' : '新增成功')
          this.closeDialog()
          this.loadAll()
        } else {
          this.$message.error(response.data.msg || (this.isEditing ? '保存失败' : '新增失败'))
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || (this.isEditing ? '保存失败' : '新增失败'))
      })
    },
    goPatient360(row) {
      if (!row || !row.patient_id) {
        this.$message.warning('当前记录缺少患者ID')
        return
      }
      this.$router.push({ path: '/Patient360', query: { id: row.patient_id } })
    },
    handleDelete(row) {
      this.$confirm(`确认删除 ${row.patient_name || '该患者'} 的回访记录？`, '提示', { type: 'warning' }).then(() => {
        axios.delete(`/followup/delete/${row.id}`).then(response => {
          if (response.data.code === '200') {
            this.$message.success('删除成功')
            this.loadAll()
          } else {
            this.$message.error(response.data.msg || '删除失败')
          }
        }).catch(error => {
          this.$message.error((error.response && error.response.data && error.response.data.msg) || '删除失败')
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-card,
.query-card,
.table-card {
  border-radius: 18px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
}

.page-kicker {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 4px;
}

.page-head h2 {
  margin: 0;
  font-size: 24px;
  color: #0f172a;
}

.page-head p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.head-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(112px, 1fr));
  gap: 10px;
  min-width: 480px;
}

.mini-stat {
  border-radius: 16px;
  background: #f8fafc;
  padding: 14px 16px;
}

.mini-stat.accent {
  background: linear-gradient(180deg, #eff6ff 0%, #dbeafe 100%);
}

.mini-stat.warn {
  background: linear-gradient(180deg, #fff7ed 0%, #ffedd5 100%);
}

.mini-stat.success {
  background: linear-gradient(180deg, #ecfdf5 0%, #dcfce7 100%);
}

.mini-num {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1;
}

.mini-label {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
}

/deep/ .query-card .el-card__body,
/deep/ .table-card .el-card__body {
  padding: 16px 18px;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.query-input {
  width: 280px;
}

.query-select {
  width: 160px;
}

.pagination-row {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.result-empty {
  color: #f59e0b;
}

.patient-suggest-wrap {
  position: relative;
}

.patient-suggestion-panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  z-index: 30;
  max-height: 240px;
  overflow: auto;
}

.patient-suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f1f5f9;
}

.patient-suggestion-item:last-child {
  border-bottom: none;
}

.patient-suggestion-item:hover {
  background: #f8fafc;
}

.patient-suggestion-name {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

.patient-suggestion-meta {
  margin-top: 3px;
  font-size: 12px;
  color: #64748b;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 1100px) {
  .page-head {
    flex-direction: column;
  }

  .head-stats {
    min-width: 0;
    width: 100%;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .head-stats {
    grid-template-columns: 1fr;
  }

  .query-input,
  .query-select {
    width: 100%;
  }
}
</style>

<template>
  <div class="patient-workbench">
    <aside class="patient-sidebar">
      <div class="sidebar-head">
        <div class="sidebar-topline">
          <div class="sidebar-kicker">患者管理</div>
          <div class="sidebar-settings">
            <i class="el-icon-setting"></i>
          </div>
        </div>
        <div class="scope-switcher">
          <button
            v-for="item in quickScopeOptions"
            :key="item.key"
            type="button"
            class="scope-chip"
            :class="{ 'is-active': quickScope === item.key }"
            @click="selectQuickScope(item.key)"
          >
            {{ item.label }}
          </button>
        </div>
        <div class="sidebar-summary">
          <div class="summary-main">{{ totalItems }}</div>
          <div class="summary-label">当前结果患者数</div>
          <div class="summary-meta">已选 {{ selectedRows.length }} 人 · 当前页 {{ patients.length }} 人</div>
        </div>
      </div>

      <div class="group-list">
        <button
          v-for="group in patientGroups"
          :key="group.key"
          type="button"
          class="group-item"
          :class="{ 'is-active': activeGroupKey === group.key }"
          @click="selectGroup(group.key)"
        >
          <span class="group-name">{{ group.label }}</span>
          <span class="group-count">{{ groupCount(group.key) }}</span>
        </button>
      </div>

      <div class="sidebar-footer">
        <el-button plain class="sidebar-action" @click="openGroupDialog">+新增分组</el-button>
        <el-button type="primary" class="sidebar-action" @click="showAddDialog">+新增患者</el-button>
        <div class="sidebar-tip">当前分组、筛选和分页已切到服务端口径，后续再补可配置分组与标签字典。</div>
      </div>
    </aside>

    <section class="patient-main">
      <div class="toolbar-shell">
        <div class="toolbar-row">
          <el-select v-model="searchType" size="small" class="toolbar-field toolbar-field--type">
            <el-option label="患者信息" value="name"></el-option>
            <el-option label="患者编号" value="id"></el-option>
          </el-select>
          <el-input
            v-model="keyword"
            size="small"
            clearable
            class="toolbar-field toolbar-field--keyword"
            :placeholder="keywordPlaceholder"
            @keyup.enter.native="searchPatients"
          ></el-input>
          <el-select
            v-model="doctorFilter"
            size="small"
            clearable
            filterable
            class="toolbar-field toolbar-field--doctor"
            placeholder="最近就诊医生"
            @change="handleLocalFilterChange"
          >
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.value"
              :label="doctor.label"
              :value="doctor.value"
            ></el-option>
          </el-select>
          <el-select
            v-model="sortMode"
            size="small"
            class="toolbar-field toolbar-field--small"
            placeholder="排序方式"
            @change="handleLocalFilterChange"
          >
            <el-option label="默认排序" value="idDesc"></el-option>
            <el-option label="最近动态" value="recent"></el-option>
            <el-option label="累计消费" value="totalSpentDesc"></el-option>
            <el-option label="到店次数" value="visitCountDesc"></el-option>
            <el-option label="最近到店" value="lastVisitDesc"></el-option>
          </el-select>
          <el-button type="primary" size="small" @click="searchPatients">查询</el-button>
          <el-button size="small" @click="toggleAdvancedFilters">{{ showAdvancedFilters ? '收起筛选' : '高级筛选' }}</el-button>
          <el-button size="small" @click="reset">重置</el-button>
          <el-button v-if="isAdmin" size="small" type="success" plain @click="exportPatients">导出Excel</el-button>
          <el-dropdown size="small" trigger="click" @command="handleBatchCommand">
            <el-button size="small" plain>
              批量操作<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="clear">清空选择</el-dropdown-item>
              <el-dropdown-item command="group">加入分组</el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" command="delete">批量删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <div v-if="showAdvancedFilters" class="toolbar-row toolbar-row--advanced">
          <el-select
            v-model="sourceFilter"
            size="small"
            clearable
            class="toolbar-field toolbar-field--small"
            placeholder="患者来源"
            @change="handleLocalFilterChange"
          >
            <el-option v-for="item in sourceOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
          <el-select
            v-model="relationFilter"
            size="small"
            clearable
            class="toolbar-field toolbar-field--small"
            placeholder="患者关系"
            @change="handleLocalFilterChange"
          >
            <el-option v-for="item in relationFilterOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
          <el-select
            v-model="arrearsFilter"
            size="small"
            clearable
            class="toolbar-field toolbar-field--small"
            placeholder="欠费状态"
            @change="handleLocalFilterChange"
          >
            <el-option label="有欠费" value="arrears"></el-option>
            <el-option label="无欠费" value="normal"></el-option>
          </el-select>
          <div class="toolbar-tip">当前已接服务端分页和业务筛选，患者标签与复诊日期直接来自后端工作台口径。</div>
        </div>

        <div class="toolbar-summary">
          <div class="summary-chip">分组：{{ currentGroupLabel }}</div>
          <div class="summary-chip">范围：{{ currentQuickScopeLabel }}</div>
          <div class="summary-chip">总数：{{ totalItems }}</div>
          <div class="summary-chip">选中：{{ selectedRows.length }}</div>
        </div>
      </div>

      <div class="table-shell">
        <el-table
          ref="patientTable"
          v-loading="loading"
          :data="patients"
          size="mini"
          stripe
          class="patient-table"
          :header-cell-style="tableHeaderStyle"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="44" align="center"></el-table-column>
          <el-table-column label="患者姓名" min-width="170">
            <template slot-scope="scope">
              <div class="name-cell">
                <el-button type="text" class="name-link" @click="go360(scope.row)">{{ scope.row.name || '-' }}</el-button>
                <div class="name-subline">
                  <el-tag
                    v-if="scope.row._offline"
                    size="mini"
                    :type="scope.row._offline.failed ? 'danger' : 'warning'"
                    effect="plain"
                    class="offline-row-tag"
                  >
                    {{ scope.row._offline.label }}
                  </el-tag>
                  <span v-if="hasRelatedPatient(scope.row)">
                    {{ relationTypeLabel(scope.row) }}：
                    <el-button type="text" class="relation-link-btn" @click="go360ByRelation(scope.row)">{{ scope.row.related_patient_name }}</el-button>
                  </span>
                  <span v-else>{{ scope.row.customer_source || '未标记来源' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="患者编号" width="96"></el-table-column>
          <el-table-column prop="phone" label="手机号码" width="138">
            <template slot-scope="scope">
              <span class="mono-text">{{ scope.row.phone || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="年龄" width="70" align="center">
            <template slot-scope="scope">{{ compactAge(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="性别" width="68" align="center">
            <template slot-scope="scope">
              <span class="gender-badge" :class="scope.row.gender === '女' ? 'is-female' : 'is-male'">{{ scope.row.gender || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="累计消费" width="118" align="right">
            <template slot-scope="scope">¥{{ formatMoney(scope.row.total_spent) }}</template>
          </el-table-column>
          <el-table-column label="最近到店" width="132">
            <template slot-scope="scope">{{ formatShortDateTime(scope.row.last_visit_date) }}</template>
          </el-table-column>
          <el-table-column label="到店次数" width="90" align="center">
            <template slot-scope="scope">{{ Number(scope.row.visit_count || 0) }}</template>
          </el-table-column>
          <el-table-column label="患者标签" min-width="190">
            <template slot-scope="scope">
              <div class="tag-cell">
                <el-tag
                  v-for="tag in patientTagItems(scope.row)"
                  :key="`${scope.row.id}-${tag.text}`"
                  size="mini"
                  :type="tag.type"
                  effect="plain"
                >
                  {{ tag.text }}
                </el-tag>
                <span v-if="!patientTagItems(scope.row).length" class="tag-empty">-</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="最近就诊医生" width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <div class="doctor-cell">
                <div class="doctor-main">{{ latestVisitDoctorLabel(scope.row) }}</div>
                <div v-if="followupDoctorHint(scope.row)" class="doctor-subline">{{ followupDoctorHint(scope.row) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="latest_treatment" label="最近开的处置" min-width="190" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.latest_treatment || '-' }}</template>
          </el-table-column>
          <el-table-column label="最近动态" width="148">
            <template slot-scope="scope">{{ activityDateTime(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template slot-scope="scope">
              <div class="action-links">
                <el-button type="text" @click="go360(scope.row)">360视图</el-button>
                <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button v-if="isAdmin" type="text" class="danger-link" @click="handleDelete(scope.row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!patients.length" class="table-empty">
          <el-empty description="当前筛选条件下暂无患者数据"></el-empty>
        </div>

        <div v-else class="table-footer">
          <div class="footer-meta">已选中 {{ selectedRows.length }} 位患者</div>
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[20, 50, 100]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItems"
          ></el-pagination>
        </div>
      </div>
    </section>

    <el-dialog title="新增患者分组" :visible.sync="groupDialogVisible" width="420px" append-to-body>
      <el-form :model="groupForm" label-width="88px">
        <el-form-item label="分组名称">
          <el-input v-model="groupForm.group_name" maxlength="50" placeholder="例如：重点复诊 / 种植二期"></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="groupForm.remark" type="textarea" :rows="3" placeholder="可选：说明分组用途"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="groupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="groupSaving" @click="submitGroup">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="加入患者分组" :visible.sync="assignGroupDialogVisible" width="420px" append-to-body>
      <el-form :model="assignGroupForm" label-width="88px">
        <el-form-item label="已选患者">
          <div class="dialog-inline-tip">本次将把 {{ selectedRows.length }} 位患者加入分组</div>
        </el-form-item>
        <el-form-item label="目标分组">
          <el-select v-model="assignGroupForm.group_id" placeholder="请选择患者分组" style="width:100%">
            <el-option
              v-for="group in customGroups"
              :key="group.id"
              :label="group.label"
              :value="group.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="assignGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="submitAssignGroup">确认加入</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px" append-to-body>
      <el-form :model="editItem" label-width="100px">
        <el-form-item>
          <span slot="label" class="required-label">患者姓名</span>
          <el-input v-model="editItem.name"></el-input>
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">患者性别</span>
          <el-select v-model="editItem.gender" placeholder="请选择性别" style="width:100%">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">年龄</span>
          <el-input-number v-model="editItem.age" :min="0" :max="150" controls-position="right" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">手机号码</span>
          <el-input v-model="editItem.phone" maxlength="11" placeholder="请输入11位手机号码"></el-input>
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">患者来源</span>
          <el-select v-model="editItem.customer_source" placeholder="请选择患者来源" style="width:100%">
            <el-option
              v-for="item in customerSourceOptions"
              :key="item"
              :label="item"
              :value="item"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="介绍人（可选）">
          <ReferralSelector
            :value="referralForm"
            :current-patient-id="editItem.id || null"
            @input="handleReferralChange"
          />
        </el-form-item>
        <el-form-item label="患者关系">
          <el-select v-model="editItem.relation_type" placeholder="可选：介绍人/家属/朋友等" style="width:100%" clearable>
            <el-option v-for="item in relationOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关联患者（可选）">
          <div class="patient-suggest-wrap" @click.stop>
            <el-input
              v-model="editItem.related_patient_name"
              placeholder="可选：输入关联患者姓名，支持模糊和首字母搜索"
              clearable
              @input="handleRelatedPatientInput"
              @focus="handleRelatedPatientFocus"
              @blur="handleRelatedPatientBlur"
            ></el-input>
            <div v-if="relationSuggestionVisible && relatedPatientSuggestions.length" class="patient-suggestion-panel">
              <div
                v-for="patient in relatedPatientSuggestions"
                :key="`related-${patient.id}`"
                class="patient-suggestion-item"
                @mousedown.prevent="selectRelatedPatientSuggestion(patient)"
              >
                <div class="patient-suggestion-name">{{ patient.name }}</div>
                <div class="patient-suggestion-meta">ID {{ patient.id }}<span v-if="patient.phone"> · {{ patient.phone }}</span></div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">{{ isEditing ? '保存' : '新增' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import * as XLSX from 'xlsx'
import ReferralSelector from '@/components/ReferralSelector.vue'
import { CUSTOMER_SOURCE_OPTIONS } from '@/utils/consultationOptions'
import { getAdminSession } from '@/utils/adminSession'
import { getPatientAge, rememberRecentPatient } from '@/utils/patientList'
import { fetchCachedResource, savePatient } from '@/utils/offline/apiClient'
import { showApiError } from '@/utils/errorMessage'

const PATIENT_PHONE_REGEX = /^\d{11}$/
const QUICK_SCOPE_OPTIONS = [
  { key: 'today', label: '今日' },
  { key: 'all', label: '全部' },
  { key: 'recent', label: '最近' }
]
const PATIENT_GROUPS = [
  { key: 'all', label: '全部' },
  { key: 'recent', label: '最近患者' },
  { key: 'public', label: '公海患者' },
  { key: 'implant', label: '种植' },
  { key: 'rootCanal', label: '根管治疗' },
  { key: 'ortho', label: '正畸' },
  { key: 'repair', label: '修复' },
  { key: 'cleaning', label: '洁治' },
  { key: 'removable', label: '活动修复' },
  { key: 'extraction', label: '拔牙' },
  { key: 'filling', label: '补牙' },
  { key: 'periodontal', label: '牙周' },
  { key: 'highValue', label: '高价值客户' },
  { key: 'lostRisk', label: '流失风险' },
  { key: 'wordOfMouth', label: '口碑客户' },
  { key: 'unconverted', label: '未成交' },
  { key: 'other', label: '其他' }
]

const createEmptyEditItem = () => ({
  name: '',
  gender: '',
  age: null,
  date_of_birth: null,
  phone: '',
  customer_source: '',
  relation_type: '',
  related_patient_id: null,
  related_patient_name: ''
})

const createEmptyReferralState = () => ({
  referrer_type: '',
  referrer_patient_id: null,
  referrer_patient_name: '',
  external_referrer_type: '',
  external_referrer_name: '',
  external_referrer_contact: '',
  referral_remark: ''
})

function normalizeText(value) {
  return String(value || '').trim()
}

function toDate(value) {
  if (!value) return null
  const date = value instanceof Date ? value : new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

function formatDateValue(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDateTimeValue(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

export default {
  name: 'PatientView',
  components: {
    ReferralSelector
  },
  data() {
    return {
      patients: [],
      selectedRows: [],
      currentPage: 1,
      pageSize: 20,
      totalItems: 0,
      searchType: 'name',
      keyword: '',
      loading: false,
      quickScope: 'all',
      activeGroupKey: 'all',
      sortMode: 'idDesc',
      doctorFilter: '',
      sourceFilter: '',
      relationFilter: '',
      arrearsFilter: '',
      showAdvancedFilters: false,
      dialogVisible: false,
      customerSourceOptions: CUSTOMER_SOURCE_OPTIONS,
      relationOptions: ['介绍人', '家属', '夫妻', '父母子女', '兄弟姐妹', '朋友', '同事', '其他'],
      relationFilterOptions: [],
      doctorOptions: [],
      sourceOptions: [],
      customGroups: [],
      groupCounts: {},
      currentUser: getAdminSession() || {},
      groupDialogVisible: false,
      groupSaving: false,
      groupForm: {
        group_name: '',
        remark: ''
      },
      assignGroupDialogVisible: false,
      assignSaving: false,
      assignGroupForm: {
        group_id: null
      },
      editItem: createEmptyEditItem(),
      referralForm: createEmptyReferralState(),
      relatedPatientSuggestions: [],
      relationSuggestionVisible: false,
      relationSuggestionBlurTimer: null,
      isEditing: false
    }
  },
  computed: {
    dialogTitle() {
      return this.isEditing ? '编辑患者信息' : '新增患者信息'
    },
    keywordPlaceholder() {
      return this.searchType === 'id'
        ? '输入患者编号'
        : '姓名/手机号/首拼/关系'
    },
    quickScopeOptions() {
      return QUICK_SCOPE_OPTIONS
    },
    patientGroups() {
      return PATIENT_GROUPS.concat((this.customGroups || []).map(group => ({
        key: group.key,
        label: group.label
      })))
    },
    currentGroupLabel() {
      const matched = this.patientGroups.find(item => item.key === this.activeGroupKey)
      return matched ? matched.label : '全部'
    },
    currentQuickScopeLabel() {
      const matched = QUICK_SCOPE_OPTIONS.find(item => item.key === this.quickScope)
      return matched ? matched.label : '全部'
    },
    normalizedRole() {
      const role = String((this.currentUser && this.currentUser.role) || '').trim()
      if (role === '管理员' || role === 'admin') return 'admin'
      if (role === '医生' || role === 'doctor') return 'doctor'
      if (role === '护士' || role === 'nurse') return 'nurse'
      return role
    },
    isAdmin() {
      return this.normalizedRole === 'admin'
    }
  },
  mounted() {
    this.currentUser = getAdminSession() || {}
    this.applyRouteFilters()
    this.loadPatients()
  },
  methods: {
    applyRouteFilters() {
      const query = this.$route && this.$route.query ? this.$route.query : {}
      const nextGroupKey = normalizeText(query.groupKey)
      const nextSortMode = normalizeText(query.sortMode)
      const nextSourceFilter = normalizeText(query.sourceFilter)
      if (nextGroupKey) {
        this.activeGroupKey = nextGroupKey
      }
      if (nextSortMode) {
        this.sortMode = nextSortMode
      }
      if (nextSourceFilter) {
        this.sourceFilter = nextSourceFilter
      }
    },
    buildSensitiveHeaders(secondaryPassword) {
      return {
        'X-Operator-Account-Id': this.currentUser && this.currentUser.id ? String(this.currentUser.id) : '',
        'X-Secondary-Password': String(secondaryPassword || '').trim()
      }
    },
    ensureAdminAccess(actionLabel) {
      if (this.isAdmin) return true
      this.$message.error(`只有管理员账号可以${actionLabel}患者`)
      return false
    },
    async requestSecondaryPassword(actionLabel) {
      if (!this.ensureAdminAccess(actionLabel)) {
        return ''
      }
      try {
        const { value } = await this.$prompt(`请输入${actionLabel}患者的二级密码`, '二级验证', {
          confirmButtonText: '验证',
          cancelButtonText: '取消',
          inputType: 'password',
          inputPlaceholder: '请输入二级密码',
          closeOnClickModal: false,
          inputValidator: input => String(input || '').trim() ? true : '请输入二级密码'
        })
        return String(value || '').trim()
      } catch (error) {
        return ''
      }
    },
    tableHeaderStyle() {
      return {
        backgroundColor: '#f5f7fb',
        color: '#4b5563',
        fontWeight: '600',
        fontSize: '12px',
        padding: '8px 0'
      }
    },
    go360(row) {
      rememberRecentPatient(row)
      this.$router.push({ path: '/Patient360', query: { id: row.id, name: row.name } })
    },
    go360ByRelation(row) {
      if (!this.hasRelatedPatient(row)) return
      const relatedPatient = {
        id: Number(row.related_patient_id),
        name: normalizeText(row.related_patient_name)
      }
      rememberRecentPatient(relatedPatient)
      this.$router.push({ path: '/Patient360', query: { id: relatedPatient.id, name: relatedPatient.name } })
    },
    buildWorkbenchParams(options = {}) {
      const keyword = typeof options.keyword === 'string' ? options.keyword.trim() : String(this.keyword || '').trim()
      return {
        searchType: this.searchType,
        keyword: keyword || undefined,
        quickScope: this.quickScope,
        groupKey: this.activeGroupKey,
        doctorFilter: this.doctorFilter || undefined,
        sourceFilter: this.sourceFilter || undefined,
        relationFilter: this.relationFilter || undefined,
        arrearsFilter: this.arrearsFilter || undefined,
        sortMode: this.sortMode || 'idDesc',
        page: options.page || this.currentPage,
        size: options.size || this.pageSize
      }
    },
    async loadPatients(options = {}) {
      const keepPage = !!options.keepPage
      if (!keepPage) {
        this.currentPage = 1
      }
      this.loading = true
      try {
        const params = this.buildWorkbenchParams(options)
        const result = await fetchCachedResource({
          cacheKey: 'page:patients:workbench',
          scope: 'patientsWorkbench',
          url: '/patients/workbench',
          params,
          loader: () => axios.get('/patients/workbench', { params }),
          notifier: message => this.$message.warning(message)
        })
        const data = result && result.data ? result.data : {}
        const filterOptions = data.filterOptions || {}
        this.patients = Array.isArray(data.list) ? data.list : []
        this.totalItems = Number(data.total || 0)
        this.groupCounts = data.groupCounts && typeof data.groupCounts === 'object' ? data.groupCounts : {}
        this.customGroups = Array.isArray(data.customGroups) ? data.customGroups : []
        this.doctorOptions = Array.isArray(filterOptions.doctors) ? filterOptions.doctors : []
        this.sourceOptions = Array.isArray(filterOptions.sources) ? filterOptions.sources : []
        this.relationFilterOptions = Array.isArray(filterOptions.relations) && filterOptions.relations.length
          ? filterOptions.relations
          : this.relationOptions.slice()
        this.currentPage = Number(data.pageNum || this.currentPage)
        this.pageSize = Number(data.pageSize || this.pageSize)
        this.clearTableSelection()
      } catch (error) {
        console.error('Error fetching patients:', error)
        this.patients = []
        this.totalItems = 0
        this.groupCounts = {}
        showApiError(this, '加载患者列表', error)
      } finally {
        this.loading = false
      }
    },
    groupCount(key) {
      return Number(this.groupCounts[key] || 0)
    },
    selectQuickScope(key) {
      this.quickScope = key
      this.currentPage = 1
      this.loadPatients({ keepPage: true })
    },
    selectGroup(key) {
      this.activeGroupKey = key
      this.currentPage = 1
      this.loadPatients({ keepPage: true })
    },
    toggleAdvancedFilters() {
      this.showAdvancedFilters = !this.showAdvancedFilters
    },
    handleLocalFilterChange() {
      this.currentPage = 1
      this.loadPatients({ keepPage: true })
    },
    searchPatients() {
      if (this.searchType === 'id' && this.keyword && !/^\d+$/.test(String(this.keyword).trim())) {
        this.$message.warning('按患者编号搜索时，请输入数字')
        return
      }
      this.currentPage = 1
      this.loadPatients({ keepPage: true, keyword: this.keyword })
    },
    reset() {
      this.searchType = 'name'
      this.keyword = ''
      this.quickScope = 'all'
      this.activeGroupKey = 'all'
      this.sortMode = 'idDesc'
      this.doctorFilter = ''
      this.sourceFilter = ''
      this.relationFilter = ''
      this.arrearsFilter = ''
      this.showAdvancedFilters = false
      this.currentPage = 1
      this.pageSize = 20
      this.clearTableSelection()
      this.loadPatients()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadPatients({ keepPage: true })
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadPatients({ keepPage: true })
    },
    handleSelectionChange(val) {
      this.selectedRows = val.map(row => row.id)
    },
    clearTableSelection() {
      if (this.$refs.patientTable && typeof this.$refs.patientTable.clearSelection === 'function') {
        this.$refs.patientTable.clearSelection()
      }
      this.selectedRows = []
    },
    handleBatchCommand(command) {
      if (command === 'clear') {
        this.clearTableSelection()
        return
      }
      if (command === 'group') {
        this.openAssignGroupDialog()
        return
      }
      if (command === 'delete') {
        this.delBatch()
      }
    },
    async exportPatients() {
      if (!this.ensureAdminAccess('导出')) {
        return
      }
      if (!this.totalItems) {
        this.$message.warning('当前没有可导出的患者数据')
        return
      }
      const secondaryPassword = await this.requestSecondaryPassword('导出')
      if (!secondaryPassword) {
        this.$message.info('已取消导出')
        return
      }
      const exportSize = Math.min(Math.max(this.totalItems, this.pageSize, 1), 5000)
      try {
        const response = await axios.get('/patients/workbench/export', {
          params: this.buildWorkbenchParams({ keepPage: true, page: 1, size: exportSize }),
          headers: this.buildSensitiveHeaders(secondaryPassword)
        })
        if (!response.data || response.data.code !== '200') {
          this.$message.error((response.data && response.data.msg) || '导出失败')
          return
        }
        const data = response.data && response.data.data ? response.data.data : {}
        const list = Array.isArray(data.list) ? data.list : []
        if (!list.length) {
          this.$message.warning('当前没有可导出的患者数据')
          return
        }
        const rows = list.map(item => ([
        item.id || '',
        item.name || '',
        item.gender || '',
        this.compactAge(item),
        item.phone || '',
        item.customer_source || '',
        this.formatRelationship(item),
        this.latestVisitDoctorLabel(item),
        item.latest_treatment || '',
        item.has_arrears ? this.formatArrears(item.arrears_amount) : '0.00',
        this.activityDateTime(item)
      ]))
        const headers = ['患者编号', '患者姓名', '性别', '年龄', '手机号码', '患者来源', '患者关系', '最近就诊医生', '最近开的处置', '欠费金额', '最近动态']
        const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows])
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, sheet, '患者列表')
        XLSX.writeFile(workbook, `patients-${Date.now()}.xlsx`)
        if (this.totalItems > exportSize) {
          this.$message.warning('当前仅导出前5000条患者记录')
        }
      } catch (error) {
        console.error('Error exporting patients:', error)
        this.$message.error('导出失败')
      }
    },
    openGroupDialog() {
      this.groupForm = {
        group_name: '',
        remark: ''
      }
      this.groupDialogVisible = true
    },
    async submitGroup() {
      if (!normalizeText(this.groupForm.group_name)) {
        this.$message.warning('请输入分组名称')
        return
      }
      this.groupSaving = true
      try {
        const response = await axios.post('/patient-groups/add', {
          group_name: normalizeText(this.groupForm.group_name),
          remark: normalizeText(this.groupForm.remark)
        })
        if (response.data.code !== '200') {
          this.$message.error(response.data.msg || '新增分组失败')
          return
        }
        this.$message.success('新增分组成功')
        this.groupDialogVisible = false
        this.loadPatients({ keepPage: true })
      } catch (error) {
        console.error('Error creating patient group:', error)
        this.$message.error('新增分组失败')
      } finally {
        this.groupSaving = false
      }
    },
    openAssignGroupDialog() {
      if (!this.selectedRows.length) {
        this.$message.warning('请先选择患者')
        return
      }
      if (!this.customGroups.length) {
        this.$message.warning('请先新增患者分组')
        return
      }
      this.assignGroupForm = {
        group_id: this.customGroups[0] ? this.customGroups[0].id : null
      }
      this.assignGroupDialogVisible = true
    },
    async submitAssignGroup() {
      if (!this.assignGroupForm.group_id) {
        this.$message.warning('请选择患者分组')
        return
      }
      if (!this.selectedRows.length) {
        this.$message.warning('请先选择患者')
        return
      }
      this.assignSaving = true
      try {
        const response = await axios.post('/patient-groups/assign', {
          group_id: this.assignGroupForm.group_id,
          patient_ids: this.selectedRows
        })
        if (response.data.code !== '200') {
          this.$message.error(response.data.msg || '加入分组失败')
          return
        }
        this.$message.success('加入分组成功')
        this.assignGroupDialogVisible = false
        this.clearTableSelection()
        this.loadPatients({ keepPage: true })
      } catch (error) {
        console.error('Error assigning patient group:', error)
        this.$message.error('加入分组失败')
      } finally {
        this.assignSaving = false
      }
    },
    patientTagItems(patient) {
      const serverTags = Array.isArray(patient && patient.patient_tags)
        ? patient.patient_tags.filter(tag => tag && normalizeText(tag.text))
        : []
      if (serverTags.length) {
        return serverTags.slice(0, 5)
      }
      const tags = []
      if (normalizeText(patient && patient.customer_source)) {
        tags.push({ text: normalizeText(patient.customer_source), type: 'info' })
      }
      if (this.relationTypeLabel(patient)) {
        tags.push({ text: this.relationTypeLabel(patient), type: 'success' })
      }
      if (patient && patient.has_arrears) {
        tags.push({ text: `欠费 ¥${this.formatArrears(patient.arrears_amount)}`, type: 'warning' })
      }
      return tags.slice(0, 3)
    },
    compactAge(patient) {
      const age = this.resolvePatientAge(patient)
      return age === null || age === '' ? '-' : String(age)
    },
    formatDateTime(value) {
      const date = toDate(value)
      return date ? formatDateTimeValue(date) : '-'
    },
    formatShortDateTime(value) {
      const date = toDate(value)
      if (!date) return '-'
      return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    latestVisitDoctorLabel(patient) {
      return normalizeText(patient && (patient.latest_visit_doctor_name || patient.latest_visit_doctor)) || '-'
    },
    followupDoctorHint(patient) {
      const followupDoctor = normalizeText(patient && patient.followup_doctor_name)
      const latestDoctor = normalizeText(patient && (patient.latest_visit_doctor_name || patient.latest_visit_doctor))
      if (followupDoctor && followupDoctor !== latestDoctor) {
        return `回访：${followupDoctor}`
      }
      return ''
    },
    activityDateTime(patient) {
      return this.formatDateTime(patient && (patient.last_activity_at || patient.updated_at || patient.created_at))
    },
    showAddDialog() {
      this.isEditing = false
      this.editItem = createEmptyEditItem()
      this.referralForm = createEmptyReferralState()
      this.relatedPatientSuggestions = []
      this.relationSuggestionVisible = false
      this.dialogVisible = true
    },
    closeDialog() {
      this.dialogVisible = false
      this.isEditing = false
      this.referralForm = createEmptyReferralState()
      this.relatedPatientSuggestions = []
      this.relationSuggestionVisible = false
      if (this.relationSuggestionBlurTimer) {
        clearTimeout(this.relationSuggestionBlurTimer)
        this.relationSuggestionBlurTimer = null
      }
    },
    handleReferralChange(value) {
      this.referralForm = Object.assign(createEmptyReferralState(), value || {})
      if (this.hasReferralPayload()) {
        this.editItem.customer_source = '转介绍'
      }
    },
    buildPatientPayload() {
      const relationType = normalizeText(this.editItem.relation_type)
      const relatedPatientName = normalizeText(this.editItem.related_patient_name)
      const hasRelatedPatient = !!relatedPatientName || !!this.editItem.related_patient_id
      const referralPayload = this.hasReferralPayload() ? this.referralForm : createEmptyReferralState()
      return {
        id: this.editItem.id,
        name: normalizeText(this.editItem.name),
        gender: normalizeText(this.editItem.gender),
        age: this.editItem.age === '' || this.editItem.age === null || this.editItem.age === undefined
          ? null
          : Number(this.editItem.age),
        date_of_birth: this.editItem.date_of_birth || null,
        phone: normalizeText(this.editItem.phone),
        customer_source: this.hasReferralPayload() ? '转介绍' : normalizeText(this.editItem.customer_source),
        wechat_openid: normalizeText(this.editItem.wechat_openid) || null,
        relation_type: relationType,
        related_patient_id: hasRelatedPatient && this.editItem.related_patient_id ? Number(this.editItem.related_patient_id) : null,
        related_patient_name: hasRelatedPatient ? relatedPatientName : '',
        referrer_type: referralPayload.referrer_type || '',
        referrer_patient_id: referralPayload.referrer_patient_id || null,
        referrer_patient_name: referralPayload.referrer_patient_name || '',
        external_referrer_type: referralPayload.external_referrer_type || '',
        external_referrer_name: referralPayload.external_referrer_name || '',
        external_referrer_contact: referralPayload.external_referrer_contact || '',
        referral_remark: referralPayload.referral_remark || ''
      }
    },
    handleAdd() {
      const validationMessage = this.validatePatientForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      savePatient(this.buildPatientPayload(), {
        notifier: message => this.$message.success(message)
      }).then(result => {
        const createdPatient = result && result.data ? result.data : null
        if (createdPatient && createdPatient.id) {
          rememberRecentPatient(createdPatient)
        }
        if (!result.offline) {
          this.$message.success('新增成功')
        }
        this.dialogVisible = false
        this.clearTableSelection()
        this.currentPage = 1
        this.loadPatients()
      }).catch(error => {
        console.error('Error adding new patient:', error)
        this.$message.error('新增失败')
      })
    },
    handleEdit(row) {
      this.editItem = Object.assign(createEmptyEditItem(), row, {
        age: this.resolvePatientAge(row),
        date_of_birth: row.date_of_birth || null
      })
      this.referralForm = Object.assign(createEmptyReferralState(), {
        referrer_type: row.referrer_type || '',
        referrer_patient_id: row.referrer_patient_id || null,
        referrer_patient_name: row.referrer_patient_name || '',
        external_referrer_type: row.external_referrer_type || '',
        external_referrer_name: row.external_referrer_name || '',
        external_referrer_contact: row.external_referrer_contact || '',
        referral_remark: row.referral_remark || ''
      })
      this.relatedPatientSuggestions = []
      this.relationSuggestionVisible = false
      this.isEditing = true
      this.dialogVisible = true
    },
    handleSaveEdit() {
      const validationMessage = this.validatePatientForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      if (this.isEditing) {
        savePatient(this.buildPatientPayload(), {
          isEdit: true,
          notifier: message => this.$message.success(message)
        }).then(result => {
          if (!result.offline) {
            this.$message.success('编辑成功')
          }
          this.closeDialog()
          this.loadPatients({ keepPage: true })
        }).catch(error => {
          console.error('Error editing patient:', error)
          this.$message.error('编辑失败')
        })
      } else {
        this.handleAdd()
      }
    },
    handleDelete(id) {
      if (!this.ensureAdminAccess('删除')) {
        return
      }
      this.$confirm('此操作将永久删除该患者，是否继续？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
        .then(async () => {
          const secondaryPassword = await this.requestSecondaryPassword('删除')
          if (!secondaryPassword) {
            this.$message.info('已取消删除')
            return
          }
          axios.delete(`/patients/delete/${id}`, {
            headers: this.buildSensitiveHeaders(secondaryPassword)
          }).then(response => {
            if (!response.data || response.data.code !== '200') {
              this.$message.error((response.data && response.data.msg) || '删除失败')
              return
            }
            this.$message.success(response.data.msg || '删除成功')
            this.clearTableSelection()
            this.loadPatients({ keepPage: true })
          }).catch(error => {
            console.error('Error deleting patient:', error)
            const msg = error && error.response && error.response.data && error.response.data.msg
              ? error.response.data.msg
              : '删除失败'
            this.$message.error(msg)
          })
        })
        .catch(() => {
          this.$message.info('已取消删除')
        })
    },
    delBatch() {
      if (!this.ensureAdminAccess('删除')) {
        return
      }
      if (!this.selectedRows.length) {
        this.$message.warning('请先选择要删除的患者')
        return
      }
      this.$confirm('此操作将永久删除所选患者，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const secondaryPassword = await this.requestSecondaryPassword('删除')
        if (!secondaryPassword) {
          this.$message.info('已取消批量删除')
          return
        }
        axios.delete('/patients/deleteBatch', {
          data: this.selectedRows,
          headers: this.buildSensitiveHeaders(secondaryPassword)
        }).then(response => {
          if (!response.data || response.data.code !== '200') {
            this.$message.error((response.data && response.data.msg) || '批量删除失败')
            return
          }
          this.$message.success(response.data.msg || '批量删除成功')
          this.clearTableSelection()
          this.loadPatients({ keepPage: true })
        }).catch(error => {
          console.error('Error deleting patients:', error)
          const msg = error && error.response && error.response.data && error.response.data.msg
            ? error.response.data.msg
            : '批量删除失败'
          this.$message.error(msg)
        })
      }).catch(() => {
        this.$message.info('已取消批量删除')
      })
    },
    validatePatientForm() {
      if (!this.editItem.name || !normalizeText(this.editItem.name)) return '患者姓名必填'
      if (!this.editItem.gender || !normalizeText(this.editItem.gender)) return '患者性别必填'
      if (this.editItem.age === null || this.editItem.age === undefined || this.editItem.age === '') return '患者年龄必填'
      if (!Number.isFinite(Number(this.editItem.age)) || Number(this.editItem.age) < 0 || Number(this.editItem.age) > 150) return '患者年龄需在0到150之间'
      if (!this.editItem.phone || !normalizeText(this.editItem.phone)) return '手机号码必填'
      if (!PATIENT_PHONE_REGEX.test(normalizeText(this.editItem.phone))) return '手机号码需为11位数字'
      if (!this.editItem.customer_source || !normalizeText(this.editItem.customer_source)) return '患者来源必填'
      if (!this.customerSourceOptions.includes(normalizeText(this.editItem.customer_source))) return '患者来源不合法'
      const hasRelationType = !!normalizeText(this.editItem.relation_type)
      const hasRelatedPatient = !!normalizeText(this.editItem.related_patient_name) || !!this.editItem.related_patient_id
      if (!hasRelationType && hasRelatedPatient) return '已选择关联患者时，必须选择患者关系'
      if (hasRelatedPatient && !this.editItem.related_patient_id) return '请从下拉列表中选择有效的关联患者'
      if (this.editItem.related_patient_id && Number(this.editItem.related_patient_id) === Number(this.editItem.id)) return '关联患者不能是本人'
      if (this.referralForm.referrer_type === 'patient' && !this.referralForm.referrer_patient_id) return '请选择有效的介绍患者'
      if (this.referralForm.referrer_type === 'patient' && Number(this.referralForm.referrer_patient_id) === Number(this.editItem.id || 0)) return '介绍患者不能是本人'
      if (this.referralForm.referrer_type === 'external' && !normalizeText(this.referralForm.external_referrer_name)) return '请输入外部介绍人姓名'
      return ''
    },
    hasReferralPayload() {
      return !!(this.referralForm && (
        Number(this.referralForm.referrer_patient_id || 0) > 0
        || normalizeText(this.referralForm.referrer_patient_name)
        || normalizeText(this.referralForm.external_referrer_type)
        || normalizeText(this.referralForm.external_referrer_name)
        || normalizeText(this.referralForm.external_referrer_contact)
        || normalizeText(this.referralForm.referrer_type)
        || normalizeText(this.referralForm.referral_remark)
      ))
    },
    loadRelatedPatientSuggestions(keyword = '') {
      axios.get('/patients/search', {
        params: {
          keyword: String(keyword || '').trim(),
          page: 1,
          size: 8
        }
      }).then(response => {
        const data = response.data && response.data.data
        const list = Array.isArray(data && data.list) ? data.list : []
        this.relatedPatientSuggestions = list.filter(item => Number(item.id) !== Number(this.editItem.id || 0))
      }).catch(() => {
        this.relatedPatientSuggestions = []
      })
    },
    handleRelatedPatientInput() {
      this.editItem.related_patient_id = null
      this.relationSuggestionVisible = true
      this.loadRelatedPatientSuggestions(this.editItem.related_patient_name)
    },
    handleRelatedPatientFocus() {
      if (this.relationSuggestionBlurTimer) {
        clearTimeout(this.relationSuggestionBlurTimer)
        this.relationSuggestionBlurTimer = null
      }
      this.relationSuggestionVisible = true
      this.loadRelatedPatientSuggestions(this.editItem.related_patient_name)
    },
    handleRelatedPatientBlur() {
      this.relationSuggestionBlurTimer = setTimeout(() => {
        this.relationSuggestionVisible = false
      }, 120)
    },
    selectRelatedPatientSuggestion(patient) {
      this.editItem.related_patient_id = patient && patient.id ? patient.id : null
      this.editItem.related_patient_name = patient && patient.name ? patient.name : ''
      this.relationSuggestionVisible = false
    },
    resolvePatientAge(patient) {
      const age = getPatientAge(patient)
      return age === '' ? null : age
    },
    formatArrears(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    hasRelatedPatient(patient) {
      return !!(patient
        && patient.related_patient_id
        && Number(patient.related_patient_id) > 0
        && normalizeText(patient.related_patient_name))
    },
    relationTypeLabel(patient) {
      return patient && patient.relation_type ? normalizeText(patient.relation_type) : ''
    },
    formatRelationship(patient) {
      if (!patient) return '-'
      const relationType = this.relationTypeLabel(patient)
      const relatedName = normalizeText(patient.related_patient_name)
      if (relationType && relatedName) return `${relationType}：${relatedName}`
      if (relationType) return relationType
      if (relatedName) return relatedName
      return '-'
    }
  }
}
</script>

<style scoped>
/* === e看牙 医疗 SaaS 风格 === */
.patient-workbench {
  --primary: #00a6c9;
  --primary-hover: #0095b5;
  --primary-light: rgba(0, 166, 201, 0.08);
  --text-primary: #1d222a;
  --text-regular: #3e3e3c;
  --text-secondary: #636a74;
  --text-muted: #9397a2;
  --bg-page: #f5f5f5;
  --bg-card: #ffffff;
  --bg-hover: #f5f7fa;
  --border-color: #d9d9d9;
  --border-light: #e8e8e8;
  --success: #52c41a;
  --warning: #faad14;
  --danger: #f86359;
  --shadow-card: 0 2px 8px rgba(0, 0, 0, 0.08);
  --radius-sm: 4px;
  --radius-md: 8px;

  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 12px;
  min-height: calc(100vh - 120px);
  color: var(--text-regular);
  background: var(--bg-page);
  padding: 12px;
}

.patient-sidebar,
.patient-main {
  min-width: 0;
}

.patient-sidebar {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.sidebar-head {
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 8px;
}

.sidebar-topline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px 4px;
}

.sidebar-kicker {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.sidebar-settings {
  color: var(--text-secondary);
  cursor: pointer;
}

.scope-switcher {
  display: flex;
  gap: 0;
  padding: 0 10px;
  border-bottom: 1px solid var(--border-light);
}

.scope-chip {
  flex: 1;
  border: 0;
  border-bottom: 3px solid transparent;
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  padding: 8px 0 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.scope-chip.is-active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  font-weight: 600;
}

.sidebar-summary {
  margin: 10px 10px 0;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: var(--primary-light);
  border: 1px solid rgba(0, 166, 201, 0.15);
}

.summary-main {
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
  color: var(--primary);
}

.summary-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.summary-meta {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

.group-list {
  flex: 1;
  overflow: auto;
  padding: 6px 0;
}

.group-item {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  border: 0;
  border-left: 3px solid transparent;
  background: transparent;
  padding: 9px 12px;
  text-align: left;
  cursor: pointer;
  color: var(--text-regular);
  font-size: 13px;
  transition: background 0.15s ease;
}

.group-item:hover {
  background: var(--bg-hover);
}

.group-item.is-active {
  background: var(--primary-light);
  border-left-color: var(--primary);
}

.group-name {
  font-size: 13px;
}

.group-count {
  font-size: 12px;
  color: var(--text-muted);
}

.sidebar-footer {
  border-top: 1px solid var(--border-light);
  padding: 10px;
  background: var(--bg-card);
}

.sidebar-action {
  width: 100%;
  margin: 0 0 8px !important;
  border-radius: var(--radius-sm);
}

.sidebar-tip {
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.5;
}

.patient-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toolbar-shell,
.table-shell {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.toolbar-shell {
  padding: 10px 12px;
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.toolbar-row + .toolbar-row {
  margin-top: 8px;
}

.toolbar-row--advanced {
  padding-top: 8px;
  border-top: 1px dashed var(--border-light);
}

.toolbar-field {
  min-width: 0;
}

.toolbar-field--type {
  width: 120px;
}

.toolbar-field--keyword {
  width: 220px;
}

.toolbar-field--doctor {
  width: 150px;
}

.toolbar-field--small {
  width: 130px;
}

.toolbar-tip {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.toolbar-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
}

.summary-chip {
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  background: var(--bg-hover);
  color: var(--text-secondary);
  font-size: 12px;
}

.table-shell {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 6px 8px 8px;
}

.patient-table {
  width: 100%;
}

.table-empty {
  padding: 24px 0 10px;
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-light);
}

.footer-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

.dialog-inline-tip {
  color: var(--text-secondary);
  line-height: 1.6;
}

.name-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.name-link,
.relation-link-btn {
  padding: 0;
  font-weight: 600;
  color: var(--primary);
}

.name-subline {
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.mono-text {
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 12px;
  color: var(--text-primary);
}

.gender-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.gender-badge.is-male {
  background: rgba(0, 166, 201, 0.08);
  color: var(--primary);
}

.gender-badge.is-female {
  background: rgba(248, 99, 89, 0.08);
  color: var(--danger);
}

.tag-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.tag-empty {
  color: var(--text-muted);
}

.doctor-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.4;
}

.doctor-main {
  color: var(--text-primary);
  font-size: 13px;
}

.doctor-subline {
  color: var(--text-secondary);
  font-size: 12px;
}

.action-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.danger-link {
  color: var(--danger);
}

.dialog-footer {
  text-align: right;
}

.required-label::before {
  content: '*';
  color: var(--danger);
  margin-right: 4px;
}

.patient-suggest-wrap {
  position: relative;
}

.patient-suggestion-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 20;
  margin-top: 4px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  max-height: 240px;
  overflow: auto;
}

.patient-suggestion-item {
  padding: 8px 12px;
  cursor: pointer;
}

.patient-suggestion-item + .patient-suggestion-item {
  border-top: 1px solid var(--border-light);
}

.patient-suggestion-item:hover {
  background: var(--bg-hover);
}

.patient-suggestion-name {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 13px;
}

.patient-suggestion-meta {
  margin-top: 2px;
  color: var(--text-secondary);
  font-size: 12px;
}

@media (max-width: 1320px) {
  .patient-workbench {
    grid-template-columns: 1fr;
  }

  .patient-sidebar {
    min-height: auto;
  }

  .group-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 6px;
    padding: 8px 10px;
  }

  .group-item {
    border: 1px solid var(--border-color);
    border-radius: var(--radius-sm);
    padding: 8px 10px;
  }

  .group-item.is-active {
    border-left-color: transparent;
    border-color: var(--primary);
    background: var(--primary-light);
  }
}

@media (max-width: 900px) {
  .table-footer,
  .toolbar-row {
    align-items: stretch;
  }

  .toolbar-field--type,
  .toolbar-field--keyword,
  .toolbar-field--doctor,
  .toolbar-field--small {
    width: 100%;
  }

  .table-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

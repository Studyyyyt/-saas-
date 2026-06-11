<template>
  <div class="system-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>账号管理</h2>
        <p>维护员工账号、姓名、密码、角色、微信绑定信息与诊所分配。</p>
      </div>
      <el-button type="primary" plain @click="showAddDialog">新增账号</el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="accounts" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column label="账号ID" width="100" align="center">
          <template slot-scope="scope">
            <code class="account-id" :title="'点击复制: ' + scope.row.id" @click="copyId(scope.row.id)">{{ scope.row.id }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号名称" width="120"></el-table-column>
        <el-table-column prop="name" label="姓名" width="100"></el-table-column>
        <el-table-column prop="roleLabel" label="角色" width="90">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.role === 'doctor' ? 'success' : scope.row.role === 'admin' ? 'danger' : 'info'">{{ scope.row.roleLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="所属诊所" min-width="180">
          <template slot-scope="scope">
            <div v-if="scope.row.clinicLabels && scope.row.clinicLabels.length" class="clinic-tags">
              <el-tag v-for="(c, i) in scope.row.clinicLabels" :key="i" size="mini" :type="c.isDefault ? 'primary' : 'info'" class="clinic-tag">
                {{ c.name }}({{ c.roleLabel }})<span v-if="c.isDefault">·默认</span>
              </el-tag>
            </div>
            <span v-else class="text-muted">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="微信绑定" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.wechat_openid" size="mini" type="success">已绑定</el-tag>
            <el-tag v-else size="mini" type="info">未绑定</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!accounts.length" description="暂无账号数据"></el-empty>
    </el-card>

    <el-dialog
      :title="isEditing ? '编辑员工账号' : '新增员工账号'"
      :visible.sync="dialogVisible"
      width="620px"
      :modal-append-to-body="false"
      :append-to-body="true"
      :close-on-click-modal="false"
      custom-class="account-dialog"
    >
      <el-form :model="editItem" label-width="100px">
        <el-form-item label="账号名称">
          <el-input v-model="editItem.username" placeholder="登录用的用户名"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editItem.name" placeholder="真实姓名"></el-input>
        </el-form-item>
        <el-form-item :label="isEditing ? '新密码' : '密码'">
          <el-input v-model="editItem.password" placeholder="为空则不修改密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editItem.role" placeholder="请选择角色" style="width:100%">
            <el-option
              v-for="r in allRoles"
              :key="r.code"
              :label="r.name"
              :value="r.code"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="微信OpenID">
          <el-input v-model="editItem.wechat_openid" placeholder="微信绑定后自动填充，可留空"></el-input>
        </el-form-item>

        <!-- 诊所分配区域 -->
        <el-divider content-position="left">诊所分配</el-divider>
        <div class="clinic-assign-section">
          <div v-if="editItem.clinicRoles && editItem.clinicRoles.length" class="clinic-role-list">
            <div v-for="(item, idx) in editItem.clinicRoles" :key="idx" class="clinic-role-row">
              <span class="clinic-name">{{ getClinicName(item.clinicId) }}</span>
              <el-select v-model="item.role" size="mini" style="width:110px" placeholder="角色">
                <el-option
                  v-for="r in allRoles"
                  :key="r.code"
                  :label="r.name"
                  :value="r.code"
                ></el-option>
              </el-select>
              <el-radio v-model="defaultClinicId" :label="item.clinicId" size="mini" @change="handleDefaultChange">默认</el-radio>
              <el-button type="text" size="mini" class="text-danger" @click="removeClinicRole(idx)">删除</el-button>
            </div>
          </div>
          <div v-else class="text-muted" style="margin-bottom:12px">尚未分配诊所，请添加至少一个诊所</div>

          <div class="add-clinic-row">
            <el-select v-model="pendingClinicId" size="small" style="width:200px" placeholder="选择要添加的诊所" clearable>
              <el-option
                v-for="c in availableClinicsToAdd"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              ></el-option>
            </el-select>
            <el-select v-model="pendingRole" size="small" style="width:120px;margin-left:8px" placeholder="角色">
              <el-option
                v-for="r in allRoles"
                :key="r.code"
                :label="r.name"
                :value="r.code"
              ></el-option>
            </el-select>
            <el-button type="primary" size="small" style="margin-left:8px" plain :disabled="!pendingClinicId" @click="addClinicRole">添加</el-button>
          </div>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAccount">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import { showApiError } from '@/utils/errorMessage'
import { getAdminSession, saveAdminSession } from '@/utils/adminSession'

const defaultAccount = () => ({
  username: '',
  password: '',
  role: '',
  name: '',
  wechat_openid: '',
  clinicRoles: []
})

export default {
  name: 'SystemAccountManageView',
  data() {
    return {
      accounts: [],
      allClinics: [],
      allRoles: [],
      dialogVisible: false,
      isEditing: false,
      editItem: defaultAccount(),
      pendingClinicId: '',
      pendingRole: 'doctor',
      defaultClinicId: ''
    }
  },
  computed: {
    availableClinicsToAdd() {
      const assigned = new Set((this.editItem.clinicRoles || []).map(r => r.clinicId))
      return this.allClinics.filter(c => !assigned.has(c.id))
    }
  },
  mounted() {
    this.fetchRoles()
    this.fetchClinics()
    this.fetchAccounts()
  },
  methods: {
    formatRole(role) {
      const r = this.allRoles.find(item => item.code === role)
      if (r) return r.name
      if (role === 'admin') return '管理员'
      if (role === 'doctor') return '医生'
      if (role === 'nurse') return '护士'
      return role || '未设置'
    },
    fetchRoles() {
      axios.get('/role-menu-permissions/roles').then(response => {
        if (response.data.code !== '200') return
        const data = response.data.data || []
        const nameMap = { admin: '管理员', doctor: '医生', nurse: '护士' }
        // 后端返回的是 Role 对象数组 {code, name, ...}
        this.allRoles = data.map(r => {
          const code = r.code || r
          const name = r.name || nameMap[code] || code
          return { code, name }
        })
      }).catch(() => {
        this.allRoles = [
          { code: 'admin', name: '管理员' },
          { code: 'doctor', name: '医生' },
          { code: 'nurse', name: '护士' }
        ]
      })
    },
    fetchClinics() {
      axios.get('/clinics/manage').then(response => {
        this.allClinics = response.data.data || []
      }).catch(() => {
        this.allClinics = []
      })
    },
    fetchAccounts() {
      axios.get('/accounts/search', { params: { page: 1, size: 1000 } }).then(async response => {
        const data = response.data.data || {}
        const list = (data.list || []).map(item => ({
          ...item,
          roleLabel: this.formatRole(item.role)
        }))
        // 并行加载每个账号的诊所分配
        await Promise.all(list.map(async acc => {
          try {
            const res = await axios.get(`/accounts/${acc.id}/clinics`)
            const clinics = res.data.data || []
            acc.clinicLabels = clinics.map(c => ({
              name: c.clinicName,
              roleLabel: this.formatRole(c.role),
              isDefault: c.isDefault === 1
            }))
          } catch (e) {
            acc.clinicLabels = []
          }
        }))
        this.accounts = list
      }).catch(error => {
        showApiError(this, '获取账号列表', error)
      })
    },
    showAddDialog() {
      this.isEditing = false
      this.editItem = defaultAccount()
      this.pendingClinicId = ''
      this.pendingRole = 'doctor'
      this.defaultClinicId = ''
      this.dialogVisible = true
    },
    async handleEdit(row) {
      this.isEditing = true
      this.editItem = Object.assign(defaultAccount(), { ...row, clinicRoles: [] })
      this.pendingClinicId = ''
      this.pendingRole = row.role || 'doctor'
      // 加载该账号的诊所分配
      try {
        const res = await axios.get(`/accounts/${row.id}/clinics`)
        const clinics = res.data.data || []
        this.editItem.clinicRoles = clinics.map(c => ({
          clinicId: c.clinicId,
          role: c.role,
          isDefault: c.isDefault === 1
        }))
        const def = clinics.find(c => c.isDefault === 1)
        this.defaultClinicId = def ? def.clinicId : (clinics[0] ? clinics[0].clinicId : '')
      } catch (e) {
        this.editItem.clinicRoles = []
        this.defaultClinicId = ''
      }
      this.dialogVisible = true
    },
    copyId(id) {
      navigator.clipboard.writeText(String(id)).then(() => {
        this.$message.success('账号ID已复制: ' + id)
      }).catch(() => {
        this.$message.warning('复制失败，请手动复制')
      })
    },
    getClinicName(clinicId) {
      const c = this.allClinics.find(x => x.id === clinicId)
      return c ? c.name : clinicId
    },
    addClinicRole() {
      if (!this.pendingClinicId) return
      this.editItem.clinicRoles.push({
        clinicId: this.pendingClinicId,
        role: this.pendingRole,
        isDefault: this.editItem.clinicRoles.length === 0
      })
      if (this.editItem.clinicRoles.length === 1) {
        this.defaultClinicId = this.pendingClinicId
      }
      this.pendingClinicId = ''
    },
    removeClinicRole(idx) {
      const removed = this.editItem.clinicRoles[idx]
      this.editItem.clinicRoles.splice(idx, 1)
      if (this.defaultClinicId === removed.clinicId && this.editItem.clinicRoles.length > 0) {
        this.defaultClinicId = this.editItem.clinicRoles[0].clinicId
        this.editItem.clinicRoles[0].isDefault = true
      }
    },
    handleDefaultChange(val) {
      this.editItem.clinicRoles.forEach(r => {
        r.isDefault = (r.clinicId === val)
      })
    },
    saveAccount() {
      // 组装 clinicRoles，带上 isDefault
      const payload = {
        ...this.editItem,
        clinicRoles: (this.editItem.clinicRoles || []).map(r => ({
          clinicId: r.clinicId,
          role: r.role,
          isDefault: r.clinicId === this.defaultClinicId ? 1 : 0
        }))
      }
      const request = this.isEditing ? axios.put('/accounts/edit', payload) : axios.post('/accounts/add', payload)
      request.then(response => {
        if (response.data.code === '200') {
          this.$message.success(this.isEditing ? '编辑成功' : '新增成功')
          this.dialogVisible = false
          this.fetchAccounts()
          // 如果编辑的是当前登录用户，同步更新 sessionStorage
          if (this.isEditing) {
            const session = getAdminSession()
            if (session && String(session.id) === String(this.editItem.id)) {
              session.name = this.editItem.name
              session.username = this.editItem.username
              session.role = this.editItem.role
              session.avatar = this.editItem.avatar
              saveAdminSession(session)
            }
          }
        } else {
          this.$message.error(response.data.msg || '保存失败')
        }
      }).catch(() => {
        this.$message.error('保存失败')
      })
    },
    handleDelete(id) {
      this.$confirm('确认删除该员工账号？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/accounts/delete/${id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchAccounts()
        })
      })
    }
  }
}
</script>

<style scoped>
.system-page { display:flex; flex-direction:column; gap:14px; }
.dialog-footer { text-align: right; }

.account-id {
  display: inline-block;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: all;
}
.account-id:hover {
  background: #e2e8f0;
  color: #3b82f6;
}

.clinic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.clinic-tag {
  font-size: 12px;
}
.text-muted {
  color: #94a3b8;
  font-size: 13px;
}

.clinic-assign-section {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px 16px;
}
.clinic-role-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}
.clinic-role-row {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}
.clinic-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}
.add-clinic-row {
  display: flex;
  align-items: center;
}
.text-danger {
  color: #ef4444;
}
</style>

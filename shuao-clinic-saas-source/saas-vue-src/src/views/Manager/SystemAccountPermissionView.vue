<template>
  <div class="system-page permission-page">
    <div class="hero-card">
      <div>
        <div class="page-kicker">系统设置</div>
        <h2>账号权限</h2>
        <p>支持调整员工角色，并为管理员/医生/护士配置所有侧边二级导航的访问开关。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" plain :loading="savingPermissions" @click="saveRolePermissions">保存导航权限</el-button>
        <el-button @click="loadAll">刷新</el-button>
      </div>
    </div>

    <el-alert
      title="说明：账号角色决定员工属于管理员/医生/护士哪一类；导航权限决定该角色能看到并进入哪些二级菜单。管理员的“账号权限”页默认强制保留，避免误锁。"
      type="info"
      :closable="false"
      show-icon
    />

    <el-card shadow="never" class="table-card">
      <div class="section-head">
        <div>
          <h3>账号角色调整</h3>
          <p>先把员工归到角色，再用下方矩阵统一控制该角色的菜单权限。</p>
        </div>
      </div>
      <el-table :data="accounts" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
        <el-table-column prop="id" label="序号" width="70" align="center"></el-table-column>
        <el-table-column prop="username" label="账号"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="roleLabel" label="当前角色" width="120"></el-table-column>
        <el-table-column label="调整角色" width="240">
          <template slot-scope="scope">
            <el-select v-model="scope.row.role" placeholder="请选择角色" size="small" style="width:140px">
              <el-option label="管理员" value="admin"></el-option>
              <el-option label="医生" value="doctor"></el-option>
              <el-option label="护士" value="nurse"></el-option>
            </el-select>
            <el-button size="small" type="primary" plain style="margin-left:8px" @click="saveRole(scope.row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="section-head">
        <div>
          <h3>角色导航权限矩阵</h3>
          <p>这里控制左侧菜单所有二级导航的显示和路由放行。</p>
        </div>
      </div>
      <el-table
        :data="menuCatalog"
        stripe
        :span-method="spanMenuGroup"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }"
      >
        <el-table-column prop="group_label" label="模块" width="120" />
        <el-table-column prop="menu_label" label="二级导航" min-width="180" />
        <el-table-column prop="menu_key" label="路径" min-width="170" />
        <el-table-column label="管理员" width="120" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="rolePermissions.admin[scope.row.menu_key]"
              :disabled="isPermissionLocked(scope.row.menu_key, 'admin')"
            />
          </template>
        </el-table-column>
        <el-table-column label="医生" width="120" align="center">
          <template slot-scope="scope">
            <el-switch v-model="rolePermissions.doctor[scope.row.menu_key]" />
          </template>
        </el-table-column>
        <el-table-column label="护士" width="120" align="center">
          <template slot-scope="scope">
            <el-switch v-model="rolePermissions.nurse[scope.row.menu_key]" />
          </template>
        </el-table-column>
        <el-table-column label="默认角色" min-width="180">
          <template slot-scope="scope">{{ formatDefaultRoles(scope.row.default_roles) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession, saveAdminSession } from '@/utils/adminSession'
import { showApiError } from '@/utils/errorMessage'

function emptyRolePermissions() {
  return {
    admin: {},
    doctor: {},
    nurse: {}
  }
}

export default {
  name: 'SystemAccountPermissionView',
  data() {
    return {
      accounts: [],
      menuCatalog: [],
      rolePermissions: emptyRolePermissions(),
      savingPermissions: false
    }
  },
  mounted() {
    this.loadAll()
  },
  methods: {
    formatRole(role) {
      if (role === 'admin') return '管理员'
      if (role === 'doctor') return '医生'
      if (role === 'nurse') return '护士'
      return role || '未设置'
    },
    formatDefaultRoles(roles) {
      const items = Array.isArray(roles) ? roles : []
      return items.map(this.formatRole).join(' / ') || '-'
    },
    loadAll() {
      this.fetchAccounts()
      this.fetchRolePermissionOverview()
    },
    fetchAccounts() {
      axios.get('/accounts/search', { params: { page: 1, size: 1000 } }).then(response => {
        const data = response.data.data || {}
        this.accounts = (data.list || []).map(item => ({
          ...item,
          roleLabel: this.formatRole(item.role)
        }))
      }).catch(error => {
        showApiError(this, '获取账号权限', error)
      })
    },
    fetchRolePermissionOverview() {
      axios.get('/role-menu-permissions/overview').then(response => {
        if (response.data.code !== '200') {
          this.$message.error((response.data.msg || '获取角色导航权限失败') + '，请刷新页面重试。如问题持续，请联系管理员。')
          return
        }
        const data = response.data.data || {}
        this.menuCatalog = Array.isArray(data.catalog) ? data.catalog : []
        this.rolePermissions = this.normalizeRolePermissions(data.role_permissions || {})
      }).catch(error => {
        showApiError(this, '获取角色导航权限', error)
      })
    },
    normalizeRolePermissions(source) {
      const result = emptyRolePermissions()
      this.menuCatalog.forEach(item => {
        const key = item.menu_key
        result.admin[key] = Boolean(source.admin && source.admin[key])
        result.doctor[key] = Boolean(source.doctor && source.doctor[key])
        result.nurse[key] = Boolean(source.nurse && source.nurse[key])
      })
      result.admin['/SystemAccountPermission'] = true
      return result
    },
    saveRole(row) {
      const payload = {
        id: row.id,
        username: row.username,
        password: row.password,
        name: row.name,
        role: row.role,
        wechat_openid: row.wechat_openid
      }
      axios.put('/accounts/edit', payload).then(response => {
        if (response.data.code === '200') {
          this.$message.success('角色修改成功')
          row.roleLabel = this.formatRole(row.role)
        } else {
          this.$message.error(response.data.msg || '角色修改失败')
        }
      }).catch(() => {
        this.$message.error('角色修改失败')
      })
    },
    saveRolePermissions() {
      this.savingPermissions = true
      const payload = {
        role_permissions: {
          admin: Object.assign({}, this.rolePermissions.admin, { '/SystemAccountPermission': true }),
          doctor: Object.assign({}, this.rolePermissions.doctor),
          nurse: Object.assign({}, this.rolePermissions.nurse)
        }
      }
      axios.post('/role-menu-permissions/save', payload).then(response => {
        if (response.data.code === '200') {
          this.$message.success('导航权限保存成功')
          this.fetchRolePermissionOverview()
          this.refreshCurrentSessionPermissions()
        } else {
          this.$message.error(response.data.msg || '导航权限保存失败')
        }
      }).catch(() => {
        this.$message.error('导航权限保存失败')
      }).finally(() => {
        this.savingPermissions = false
      })
    },
    refreshCurrentSessionPermissions() {
      const session = getAdminSession() || {}
      const role = session.role || ''
      if (!role) {
        return
      }
      axios.get('/role-menu-permissions/byRole', { params: { role } }).then(response => {
        if (response.data.code !== '200') {
          return
        }
        const permissionMap = response.data.data || {}
        const allowedMenuKeys = Object.keys(permissionMap).filter(key => permissionMap[key] === true)
        saveAdminSession({
          ...session,
          allowedMenuKeys,
          roleMenuPermissionsLoaded: true
        })
      }).catch(() => {})
    },
    isPermissionLocked(menuKey, role) {
      return role === 'admin' && menuKey === '/SystemAccountPermission'
    },
    spanMenuGroup({ row, column, rowIndex, columnIndex }) {
      if (columnIndex !== 0) {
        return [1, 1]
      }
      const currentGroup = row.group_label
      const prev = this.menuCatalog[rowIndex - 1]
      if (prev && prev.group_label === currentGroup) {
        return [0, 0]
      }
      let rowspan = 1
      for (let index = rowIndex + 1; index < this.menuCatalog.length; index += 1) {
        if (this.menuCatalog[index].group_label !== currentGroup) {
          break
        }
        rowspan += 1
      }
      return [rowspan, 1]
    }
  }
}
</script>

<style scoped>
.permission-page { display:flex; flex-direction:column; gap:14px; }
.hero-card { display:flex; justify-content:space-between; align-items:center; gap:16px; padding:18px; border-radius:18px; background:#fff; box-shadow:0 8px 24px rgba(31,71,136,.08); }
.hero-actions { display:flex; gap:10px; flex-wrap:wrap; }
.page-kicker { color:#64748b; font-size:13px; }
.hero-card h2 { margin:6px 0 8px; color:#0f172a; font-size:24px; }
.hero-card p { margin:0; color:#94a3b8; }
.table-card { border-radius:18px; }
.section-head { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; margin-bottom:14px; }
.section-head h3 { margin:0; color:#0f172a; font-size:18px; }
.section-head p { margin:6px 0 0; color:#94a3b8; font-size:12px; }
@media (max-width: 992px) {
  .hero-card { flex-direction:column; align-items:flex-start; }
}
</style>

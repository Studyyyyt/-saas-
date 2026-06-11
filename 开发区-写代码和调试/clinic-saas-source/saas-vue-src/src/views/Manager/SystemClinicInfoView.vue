<template>
  <div class="system-clinic-info apple-design-scope">
    <div class="page-header">
      <h2 class="page-title">诊所信息</h2>
      <p class="page-subtitle">编辑当前诊所的名称、地址和联系方式，修改后会同步显示在系统顶部导航与登录页面</p>
    </div>

    <div class="content-card">
      <el-form
        :model="form"
        :rules="formRules"
        ref="formRef"
        label-width="100px"
        class="clinic-info-form"
        v-loading="loading"
      >
        <el-form-item label="诊所ID" prop="id">
          <el-input v-model="form.id" disabled />
        </el-form-item>

        <el-form-item label="诊所名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入诊所名称" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="诊所地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入诊所地址" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" maxlength="20" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存设置</el-button>
          <el-button @click="loadClinicInfo">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession, saveAdminSession } from '@/utils/adminSession'

export default {
  name: 'SystemClinicInfoView',
  data() {
    return {
      loading: false,
      submitting: false,
      form: {
        id: '',
        name: '',
        address: '',
        contactPhone: ''
      },
      formRules: {
        name: [{ required: true, message: '请输入诊所名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadClinicInfo()
  },
  methods: {
    loadClinicInfo() {
      const session = getAdminSession()
      const clinicId = session ? session.currentClinicId : ''
      if (!clinicId) {
        this.$message.warning('未获取到当前诊所信息，请重新登录')
        return
      }

      this.loading = true
      axios.get(`/clinics/manage/${clinicId}`)
        .then(res => {
          if (res.data.code === '200' && res.data.data) {
            const clinic = res.data.data
            this.form = {
              id: clinic.id || clinicId,
              name: clinic.name || '',
              address: clinic.address || '',
              contactPhone: clinic.contactPhone || ''
            }
          } else {
            this.$message.error(res.data.msg || '加载诊所信息失败')
          }
        })
        .catch(err => {
          console.error('加载诊所信息失败:', err)
          this.$message.error('加载诊所信息失败')
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        this.submitting = true
        const payload = {
          name: this.form.name,
          address: this.form.address,
          contactPhone: this.form.contactPhone
        }
        axios.put(`/clinics/manage/${this.form.id}`, payload)
          .then(res => {
            if (res.data.code === '200') {
              this.$message.success('诊所信息保存成功')
              this.syncSessionClinicName(this.form.name)
            } else {
              this.$message.error(res.data.msg || '保存失败')
            }
          })
          .catch(err => {
            console.error('保存诊所信息失败:', err)
            this.$message.error('保存失败')
          })
          .finally(() => {
            this.submitting = false
          })
      })
    },
    syncSessionClinicName(newName) {
      // 更新本地会话中的诊所名称，使顶部导航、侧边栏等组件同步刷新
      const session = getAdminSession()
      if (!session) return
      session.currentClinicName = newName
      // 同步 clinics 列表中的名称
      if (session.clinics && Array.isArray(session.clinics)) {
        session.clinics.forEach(item => {
          if (item.clinicId === session.currentClinicId) {
            item.clinicName = newName
          }
        })
      }
      saveAdminSession(session)
    }
  }
}
</script>

<style scoped>
.system-clinic-info {
  padding: var(--apple-space-xl) 0;
}
.page-header {
  margin-bottom: var(--apple-space-xl);
}
.page-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin: 0 0 8px;
}
.page-subtitle {
  font-size: 14px;
  color: var(--apple-text-secondary);
  margin: 0;
}
.content-card {
  background: var(--apple-surface);
  border-radius: var(--apple-radius-lg);
  border: 1px solid var(--apple-border);
  box-shadow: var(--apple-shadow-sm);
  padding: var(--apple-space-xl);
  max-width: 640px;
}
.clinic-info-form {
  width: 100%;
}
</style>

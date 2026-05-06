<template>
  <div class="bind-page" v-loading="submitting">
    <div class="bind-card">
      <div class="bind-title">员工微信绑定</div>
      <div class="bind-subtitle">首次进入请填写账号名称和系统密码，绑定成功后下次将自动进入员工工作台。</div>
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      />
      <el-form :model="form" :rules="rules" ref="bindRef" label-width="90px">
        <el-form-item label="账号名称" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号名称"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入系统密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="submitBind">绑定并进入</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StaffPortalBind',
  data() {
    return {
      submitting: false,
      error: '',
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入账号名称', trigger: 'blur' }],
        password: [{ required: true, message: '请输入系统密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    submitBind() {
      this.$refs.bindRef.validate(valid => {
        if (!valid) return
        const { token } = this.$route.query
        if (!token) {
          this.error = '缺少绑定凭证，请重新从公众号菜单进入'
          return
        }
        this.submitting = true
        this.error = ''
        axios.post(`/staff-portal/bind?token=${encodeURIComponent(token)}`, this.form)
          .then(res => {
            if (res.data.code !== '200') {
              this.error = res.data.msg || '绑定失败'
              return
            }
            const redirectUrl = res.data.data && res.data.data.redirectUrl
            if (redirectUrl) {
              window.location.href = redirectUrl
              return
            }
            this.error = '绑定成功但未返回跳转地址'
          })
          .catch(() => {
            this.error = '绑定失败，请稍后重试'
          })
          .finally(() => {
            this.submitting = false
          })
      })
    }
  }
}
</script>

<style scoped>
.bind-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #eef4ff 0%, #f8fbff 100%);
  padding: 16px;
}
.bind-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 10px 30px rgba(31, 71, 136, 0.08);
}
.bind-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
  text-align: center;
}
.bind-subtitle {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
  text-align: center;
}
</style>

<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f5f7fa; padding: 20px; box-sizing: border-box;">
    <div style="width: 100%; max-width: 520px; background: #fff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,.08); padding: 36px 32px; text-align: center;">
      <div style="font-size: 56px; color: #67c23a; line-height: 1; margin-bottom: 16px;">✓</div>
      <div style="font-size: 24px; font-weight: 700; color: #303133; margin-bottom: 12px;">微信绑定成功</div>
      <div style="font-size: 14px; color: #606266; line-height: 1.8; margin-bottom: 20px;">
        患者微信已完成绑定，后续可接收预约通知。<br>
        <span v-if="patientId">当前患者ID：{{ patientId }}</span>
      </div>
      <el-alert
          title="您现在可以关闭此页面，返回诊所系统继续操作。"
          type="success"
          :closable="false"
          show-icon
          style="text-align: left; margin-bottom: 24px;"
      />
      <div style="display: flex; gap: 12px; justify-content: center; flex-wrap: wrap;">
        <el-button type="primary" @click="goLogin">返回登录页</el-button>
        <el-button @click="goHome">进入系统首页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BindSuccess',
  computed: {
    patientId() {
      return this.$route.query.patientId || ''
    }
  },
  methods: {
    goLogin() {
      this.$router.push('/login1')
    },
    goHome() {
      this.$router.push('/home')
    }
  }
}
</script>

<style scoped>
</style>

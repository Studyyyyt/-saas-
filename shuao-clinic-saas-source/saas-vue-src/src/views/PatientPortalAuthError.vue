<template>
  <div class="portal-error-page">
    <el-card class="portal-error-card" shadow="never">
      <div class="icon">!</div>
      <h2>患者中心暂不可用</h2>
      <p>{{ reasonText }}</p>
      <el-button type="primary" @click="goBindTip">返回绑定说明</el-button>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'PatientPortalAuthError',
  computed: {
    reasonText() {
      const reason = this.$route.query.reason
      if (reason === 'unbound') {
        return '当前微信号尚未绑定患者信息，请先联系门诊完成绑定。'
      }
      return '微信身份识别失败，请稍后重试。'
    }
  },
  methods: {
    goBindTip() {
      this.$router.push('/app/bind-success')
    }
  }
}
</script>

<style scoped>
.portal-error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fb;
  padding: 16px;
}
.portal-error-card {
  width: 100%;
  max-width: 520px;
  text-align: center;
  border-radius: 18px;
}
.icon {
  width: 68px;
  height: 68px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 42px;
  line-height: 68px;
}
h2 {
  margin: 0 0 12px;
  color: #303133;
}
p {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
}
</style>

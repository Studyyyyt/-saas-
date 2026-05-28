<template>
  <div class="help-placeholder-page">
    <div class="placeholder-content">
      <div class="placeholder-icon">
        <i class="el-icon-document" />
      </div>
      <h2 class="placeholder-title">帮助文档编写中</h2>
      <p class="placeholder-desc">
        该模块的帮助文档正在整理编写中，将在后续版本中补充完整。<br>
        你可以先通过实际操作页面熟悉功能，或联系技术支持获取帮助。
      </p>
      <div class="placeholder-modules">
        <div class="placeholder-label">以下模块的帮助文档正在准备中：</div>
        <div class="placeholder-tags">
          <el-tag size="small" type="info">AI 总览</el-tag>
          <el-tag size="small" type="info">首页助手配置</el-tag>
          <el-tag size="small" type="info">Agent 链接配置</el-tag>
          <el-tag size="small" type="info">项目与治疗</el-tag>
          <el-tag size="small" type="info">财务与收费</el-tag>
          <el-tag size="small" type="info">知情同意书</el-tag>
          <el-tag size="small" type="info">义齿加工</el-tag>
          <el-tag size="small" type="info">耗材管理</el-tag>
          <el-tag size="small" type="info">账号与权限</el-tag>
          <el-tag size="small" type="info">患者列表</el-tag>
          <el-tag size="small" type="info">患者 360° 视图</el-tag>
          <el-tag size="small" type="info">病历管理</el-tag>
          <el-tag size="small" type="info">随访管理</el-tag>
          <el-tag size="small" type="info">咨询管理</el-tag>
          <el-tag size="small" type="info">材料管理</el-tag>
          <el-tag size="small" type="info">采购管理</el-tag>
          <el-tag size="small" type="info">库存统计</el-tag>
          <el-tag size="small" type="info">加工厂管理</el-tag>
          <el-tag size="small" type="info">加工订单</el-tag>
          <el-tag size="small" type="info">加工对账单</el-tag>
        </div>
      </div>
      <el-button type="primary" icon="el-icon-back" @click="goBack">返回帮助首页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HelpPlaceholderView',
  methods: {
    goBack() {
      this.$router.push('/SystemSettings/help/index')
    }
  }
}
</script>

<style scoped>
.help-placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  padding: 32px 0;
}

.placeholder-content {
  text-align: center;
  max-width: 560px;
}

.placeholder-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--apple-accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.placeholder-icon i {
  font-size: 36px;
  color: var(--apple-accent);
}

.placeholder-title {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

.placeholder-desc {
  margin: 0 0 24px;
  font-size: 14px;
  color: var(--apple-text-secondary);
  line-height: 1.7;
}

.placeholder-modules {
  margin-bottom: 24px;
}

.placeholder-label {
  font-size: 13px;
  color: var(--apple-text-tertiary);
  margin-bottom: 12px;
}

.placeholder-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.placeholder-tags .el-tag {
  margin: 0;
}
</style>

<template>
  <div class="provider-config-page">
    <!-- 页面标题 -->
    <div class="provider-config-header">
      <div class="provider-config-header-left">
        <h1 class="provider-config-title">模型供应商配置</h1>
      </div>
      <el-button type="primary" icon="el-icon-back" @click="$router.back()">
        返回
      </el-button>
    </div>

    <!-- 废弃提示 -->
    <div class="provider-form-card">
      <div class="deprecated-content">
        <i class="el-icon-warning-outline" />
        <div class="deprecated-title">该页面已废弃</div>
        <div class="deprecated-desc">
          模型供应商配置已废弃，请通过「首页助手」配置外部端点。
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ModelProviderConfigView'
}
</script>

<style scoped>
.provider-config-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.provider-config-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
}

.provider-config-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: #0f172a;
  line-height: 1.2;
}

.provider-form-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 20px;
  padding: 48px 28px;
  text-align: center;
}

.deprecated-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.deprecated-content i {
  font-size: 48px;
  color: var(--apple-warning, #f59e0b);
}

.deprecated-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.deprecated-desc {
  font-size: 14px;
  color: var(--apple-text-secondary);
  line-height: 1.6;
}

/* 响应式 */
@media (max-width: 768px) {
  .provider-config-page {
    padding: 16px;
  }

  .provider-config-title {
    font-size: 22px;
  }

  .provider-config-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .provider-form-card {
    padding: 32px 20px;
  }
}
</style>

<template>
  <div class="page-header">
    <div class="page-header__main">
      <div v-if="kicker" class="page-header__kicker">{{ kicker }}</div>
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'PageHeader',
  props: {
    kicker: { type: String, default: '' },
    title: { type: String, required: true },
    subtitle: { type: String, default: '' }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header__kicker {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
  font-weight: 500;
}

.page-header__title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.3;
}

.page-header__subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #94a3b8;
  line-height: 1.5;
}

.page-header__actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  align-items: center;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .page-header__actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>

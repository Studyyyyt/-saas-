<template>
  <el-card class="query-card" shadow="never">
    <div class="query-card__body">
      <slot />
    </div>
    <div v-if="$slots.actions" class="query-card__actions">
      <slot name="actions" />
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'QueryCard'
}
</script>

<style scoped>
.query-card {
  border-radius: 16px;
  background: #ffffff;
}

.query-card ::v-deep .el-card__body {
  padding: 20px 24px;
}

.query-card__body {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.query-card__actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

@media (max-width: 768px) {
  .query-card__body {
    flex-direction: column;
    align-items: stretch;
  }

  .query-card__body ::v-deep .el-input,
  .query-card__body ::v-deep .el-select {
    width: 100% !important;
  }
}
</style>

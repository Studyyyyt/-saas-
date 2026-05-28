<template>
  <div class="data-table-wrap">
    <el-table
      v-loading="loading"
      :data="data"
      :stripe="stripe"
      :border="border"
      :empty-text="emptyText"
      class="data-table"
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="selection" type="selection" width="48" />
      <slot />
    </el-table>
    <div v-if="showPagination && total > 0" class="data-table__pagination">
      <el-pagination
        background
        layout="prev, pager, next, jumper, ->, total"
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataTable',
  props: {
    data: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false },
    stripe: { type: Boolean, default: true },
    border: { type: Boolean, default: false },
    emptyText: { type: String, default: '暂无数据' },
    selection: { type: Boolean, default: false },
    showPagination: { type: Boolean, default: true },
    page: { type: Number, default: 1 },
    pageSize: { type: Number, default: 10 },
    total: { type: Number, default: 0 }
  },
  methods: {
    handleSelectionChange(selection) {
      this.$emit('selection-change', selection)
    },
    handlePageChange(page) {
      this.$emit('page-change', page)
    }
  }
}
</script>

<style scoped>
.data-table-wrap {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
}

.data-table {
  width: 100%;
}

.data-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px;
  border-top: 1px solid #f1f5f9;
}

.data-table ::v-deep th {
  background: #f8fafc !important;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
}

.data-table ::v-deep td {
  font-size: 14px;
  color: #334155;
}

.data-table ::v-deep .el-table__empty-text {
  color: #94a3b8;
}
</style>

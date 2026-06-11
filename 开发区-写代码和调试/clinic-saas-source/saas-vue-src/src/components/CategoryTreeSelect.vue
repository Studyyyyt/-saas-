<template>
  <div class="category-tree-select">
    <el-popover
      v-model="popoverVisible"
      placement="bottom-start"
      trigger="click"
      popper-class="category-tree-select__popover"
      width="320"
    >
      <div class="tree-panel">
        <div class="tree-panel__head">
          <span class="tree-panel__title">{{ title }}</span>
          <el-button v-if="clearable && currentLabel" size="mini" type="text" @click="clearSelection">清空</el-button>
        </div>
        <el-input
          v-model="filterText"
          size="mini"
          clearable
          placeholder="搜索分类"
          class="tree-panel__search"
        />
        <el-tree
          ref="tree"
          :data="normalizedOptions"
          node-key="id"
          :props="treeProps"
          highlight-current
          default-expand-all
          :expand-on-click-node="false"
          :filter-node-method="filterNode"
          @node-click="handleNodeClick"
        />
      </div>
      <el-input
        slot="reference"
        :value="currentLabel"
        :placeholder="placeholder"
        :disabled="disabled"
        readonly
        clearable
        @clear="clearSelection"
      >
        <i slot="suffix" class="el-input__icon el-icon-arrow-down"></i>
      </el-input>
    </el-popover>
  </div>
</template>

<script>
export default {
  name: 'CategoryTreeSelect',
  props: {
    value: { type: [Number, String], default: '' },
    options: { type: Array, default: () => [] },
    placeholder: { type: String, default: '请选择分类' },
    title: { type: String, default: '分类树' },
    disabled: { type: Boolean, default: false },
    clearable: { type: Boolean, default: true }
  },
  data() {
    return {
      popoverVisible: false,
      filterText: '',
      treeProps: {
        label: 'name',
        children: 'children'
      }
    }
  },
  computed: {
    normalizedOptions() {
      return Array.isArray(this.options) ? this.options : []
    },
    currentLabel() {
      const matched = this.findNodeById(this.normalizedOptions, this.value)
      return matched ? matched.name : ''
    }
  },
  watch: {
    filterText(value) {
      if (this.$refs.tree) {
        this.$refs.tree.filter(value)
      }
    },
    popoverVisible(value) {
      if (!value) {
        this.filterText = ''
      }
    }
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return String(data && data.name || '').toLowerCase().includes(String(value).trim().toLowerCase())
    },
    findNodeById(nodes, id) {
      const target = String(id || '')
      if (!target) return null
      for (const node of nodes || []) {
        if (String(node.id) === target) {
          return node
        }
        const child = this.findNodeById(node.children || [], target)
        if (child) {
          return child
        }
      }
      return null
    },
    handleNodeClick(node) {
      this.$emit('input', node && node.id ? node.id : '')
      this.$emit('change', node)
      this.popoverVisible = false
    },
    clearSelection() {
      this.$emit('input', '')
      this.$emit('change', null)
      this.popoverVisible = false
    }
  }
}
</script>

<style scoped>
.category-tree-select {
  width: 100%;
}

.tree-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tree-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-panel__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.tree-panel__search {
  margin-bottom: 2px;
}
</style>

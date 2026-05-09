/**
 * 表格列宽持久化全局混入
 * 自动为页面内所有 el-table 提供列宽保存与恢复能力
 * 并为截断单元格添加原生 title，鼠标悬停显示完整内容
 */

function getTablePersistKey(vm, tableIndex) {
  const routePath = vm.$route ? vm.$route.path : 'global'
  const pageName = vm.$options && vm.$options.name ? vm.$options.name : ''
  return `table_persist_v2_${routePath}_${pageName}_${tableIndex}`
}

function saveColumnWidths(key, widths) {
  try {
    localStorage.setItem(key, JSON.stringify(widths))
  } catch {
    // ignore
  }
}

function loadColumnWidths(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : {}
  } catch {
    return {}
  }
}

function findElTables(vm) {
  const tables = []
  const traverse = (component) => {
    if (!component) return
    if (component.$options && component.$options.name === 'ElTable') {
      tables.push(component)
    }
    if (component.$children && component.$children.length) {
      component.$children.forEach(traverse)
    }
  }
  traverse(vm)
  return tables
}

function applySavedWidths(table, savedWidths) {
  if (!table || !table.columns || !Object.keys(savedWidths).length) return false
  let applied = false
  table.columns.forEach((col) => {
    const prop = col.property || col.label
    if (prop && savedWidths[prop]) {
      col.width = savedWidths[prop]
      applied = true
    }
  })
  return applied
}

function bindTablePersist(vm, table, index) {
  if (table.__persistBound) return
  table.__persistBound = true

  const key = getTablePersistKey(vm, index)

  // 恢复列宽
  const savedWidths = loadColumnWidths(key)
  if (applySavedWidths(table, savedWidths)) {
    vm.$nextTick(() => {
      if (table.doLayout) table.doLayout()
    })
  }

  // 监听列宽拖拽
  table.$on('header-dragend', (newWidth, oldWidth, column, event) => {
    const prop = column.property || column.label
    if (prop) {
      const widths = loadColumnWidths(key)
      widths[prop] = newWidth
      saveColumnWidths(key, widths)
    }
  })
}

/**
 * 为截断的单元格添加原生 title（批量处理）
 */
function applyCellTitles(vm) {
  vm.$nextTick(() => {
    const tables = vm.$el.querySelectorAll ? vm.$el.querySelectorAll('.el-table') : []
    tables.forEach((table) => {
      bindDynamicCellTitles(table)
      const cells = table.querySelectorAll('.el-table__body .cell')
      cells.forEach((cell) => {
        refreshCellTitle(cell)
      })
    })
  })
}

/**
 * 根据当前内容刷新单个单元格的 title
 */
function refreshCellTitle(cell) {
  const hasTooltipChild = cell.querySelector('.el-tooltip, [role="tooltip"]')
  if (hasTooltipChild) {
    cell.removeAttribute('title')
    return
  }
  const text = cell.textContent ? cell.textContent.trim() : ''
  if (!text) {
    cell.removeAttribute('title')
    return
  }
  // 判断内容是否被截断
  const isClipped = cell.scrollWidth > cell.clientWidth + 2
  if (isClipped) {
    cell.setAttribute('title', text)
  } else {
    cell.removeAttribute('title')
  }
}

/**
 * 在表格 body-wrapper 上绑定 mouseover 委托事件，实现动态 title
 * 解决 el-table 内部排序、筛选、滚动后 title 失效的问题
 */
function bindDynamicCellTitles(tableEl) {
  if (tableEl.__dynamicTitlesBound) return
  tableEl.__dynamicTitlesBound = true

  const bodyWrapper = tableEl.querySelector('.el-table__body-wrapper')
  if (!bodyWrapper) return

  bodyWrapper.addEventListener('mouseover', (e) => {
    const cell = e.target.closest('.cell')
    if (!cell) return
    refreshCellTitle(cell)
  })
}

export default {
  mounted() {
    this.$nextTick(() => {
      const tables = findElTables(this)
      tables.forEach((table, index) => {
        bindTablePersist(this, table, index)
      })
      applyCellTitles(this)
    })
  },
  updated() {
    // 处理 v-if / 异步加载后渲染的表格
    const tables = findElTables(this)
    tables.forEach((table, index) => {
      bindTablePersist(this, table, index)
    })
    applyCellTitles(this)
  }
}

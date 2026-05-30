<template>
  <div class="tooth-selector">
    <div v-for="group in groupedTeeth" :key="group.label" class="tooth-row">
      <div class="tooth-row__label">{{ group.label }}</div>
      <div class="tooth-row__grid">
        <el-button
          v-for="tooth in group.items"
          :key="tooth"
          size="mini"
          :type="selectedSet.has(tooth) ? 'primary' : 'default'"
          plain
          class="tooth-btn"
          @click="toggleTooth(tooth)"
        >
          {{ tooth }}
        </el-button>
      </div>
    </div>
    <div v-if="selectedList.length" class="tooth-selected">已选牙位：{{ selectedList.join('、') }}</div>
    <div v-else class="tooth-selected tooth-selected--empty">未选择牙位</div>
  </div>
</template>

<script>
export default {
  name: 'ToothSelector',
  props: {
    value: {
      type: [String, Array],
      default: ''
    }
  },
  computed: {
    groupedTeeth() {
      return [
        { label: '右上', items: ['18', '17', '16', '15', '14', '13', '12', '11'] },
        { label: '左上', items: ['21', '22', '23', '24', '25', '26', '27', '28'] },
        { label: '左下', items: ['38', '37', '36', '35', '34', '33', '32', '31'] },
        { label: '右下', items: ['41', '42', '43', '44', '45', '46', '47', '48'] }
      ]
    },
    selectedList() {
      if (Array.isArray(this.value)) {
        return this.value.map(item => String(item).trim()).filter(Boolean)
      }
      return String(this.value || '')
        .split(',')
        .map(item => item.trim())
        .filter(Boolean)
    },
    selectedSet() {
      return new Set(this.selectedList)
    }
  },
  methods: {
    toggleTooth(tooth) {
      const next = this.selectedList.slice()
      const index = next.indexOf(tooth)
      if (index >= 0) {
        next.splice(index, 1)
      } else {
        next.push(tooth)
      }
      this.$emit('input', next.join(','))
      this.$emit('change', next.join(','))
    }
  }
}
</script>

<style scoped>
.tooth-selector { display:flex; flex-direction:column; gap:10px; }
.tooth-row { display:flex; gap:10px; align-items:flex-start; }
.tooth-row__label { width:44px; color:#606266; font-size:12px; line-height:28px; }
.tooth-row__grid { display:flex; flex-wrap:wrap; gap:6px; flex:1; }
.tooth-btn { min-width:44px; margin:0; }
.tooth-selected { font-size:12px; color:#5A8F7B; }
.tooth-selected--empty { color:#909399; }
</style>

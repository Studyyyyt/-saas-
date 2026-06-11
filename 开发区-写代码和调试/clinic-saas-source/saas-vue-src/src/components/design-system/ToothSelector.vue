<template>
  <div class="tooth-selector">
    <div class="tooth-selector__diagram">
      <!-- 上颌 -->
      <div class="tooth-selector__arch">
        <div class="tooth-selector__arch-label">上颌</div>
        <div class="tooth-selector__row">
          <div
            v-for="tooth in upperLeft"
            :key="tooth"
            class="tooth-item"
            :class="{ 'tooth-item--selected': selectedSet.has(tooth) }"
            @click="toggleTooth(tooth)"
          >
            <svg viewBox="0 0 40 48" class="tooth-svg">
              <path
                d="M20 4C12 4 6 10 6 18C6 26 10 32 14 38C16 42 18 44 20 44C22 44 24 42 26 38C30 32 34 26 34 18C34 10 28 4 20 4Z"
                :fill="selectedSet.has(tooth) ? '#5A8F7B' : '#f1f5f9'"
                :stroke="selectedSet.has(tooth) ? '#4A7F6B' : '#cbd5e1'"
                stroke-width="2"
              />
              <text x="20" y="28" text-anchor="middle" fill="#0f172a" font-size="14" font-weight="600">{{ tooth }}</text>
            </svg>
          </div>
        </div>
        <div class="tooth-selector__row">
          <div
            v-for="tooth in upperRight"
            :key="tooth"
            class="tooth-item"
            :class="{ 'tooth-item--selected': selectedSet.has(tooth) }"
            @click="toggleTooth(tooth)"
          >
            <svg viewBox="0 0 40 48" class="tooth-svg">
              <path
                d="M20 4C12 4 6 10 6 18C6 26 10 32 14 38C16 42 18 44 20 44C22 44 24 42 26 38C30 32 34 26 34 18C34 10 28 4 20 4Z"
                :fill="selectedSet.has(tooth) ? '#5A8F7B' : '#f1f5f9'"
                :stroke="selectedSet.has(tooth) ? '#4A7F6B' : '#cbd5e1'"
                stroke-width="2"
              />
              <text x="20" y="28" text-anchor="middle" fill="#0f172a" font-size="14" font-weight="600">{{ tooth }}</text>
            </svg>
          </div>
        </div>
      </div>

      <!-- 下颌 -->
      <div class="tooth-selector__arch">
        <div class="tooth-selector__arch-label">下颌</div>
        <div class="tooth-selector__row">
          <div
            v-for="tooth in lowerLeft"
            :key="tooth"
            class="tooth-item"
            :class="{ 'tooth-item--selected': selectedSet.has(tooth) }"
            @click="toggleTooth(tooth)"
          >
            <svg viewBox="0 0 40 48" class="tooth-svg">
              <path
                d="M20 44C12 44 6 38 6 30C6 22 10 16 14 10C16 6 18 4 20 4C22 4 24 6 26 10C30 16 34 22 34 30C34 38 28 44 20 44Z"
                :fill="selectedSet.has(tooth) ? '#5A8F7B' : '#f1f5f9'"
                :stroke="selectedSet.has(tooth) ? '#4A7F6B' : '#cbd5e1'"
                stroke-width="2"
              />
              <text x="20" y="28" text-anchor="middle" fill="#0f172a" font-size="14" font-weight="600">{{ tooth }}</text>
            </svg>
          </div>
        </div>
        <div class="tooth-selector__row">
          <div
            v-for="tooth in lowerRight"
            :key="tooth"
            class="tooth-item"
            :class="{ 'tooth-item--selected': selectedSet.has(tooth) }"
            @click="toggleTooth(tooth)"
          >
            <svg viewBox="0 0 40 48" class="tooth-svg">
              <path
                d="M20 44C12 44 6 38 6 30C6 22 10 16 14 10C16 6 18 4 20 4C22 4 24 6 26 10C30 16 34 22 34 30C34 38 28 44 20 44Z"
                :fill="selectedSet.has(tooth) ? '#5A8F7B' : '#f1f5f9'"
                :stroke="selectedSet.has(tooth) ? '#4A7F6B' : '#cbd5e1'"
                stroke-width="2"
              />
              <text x="20" y="28" text-anchor="middle" fill="#0f172a" font-size="14" font-weight="600">{{ tooth }}</text>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedList.length" class="tooth-selector__tags">
      <span class="tooth-selector__tags-label">已选牙位：</span>
      <el-tag
        v-for="tooth in selectedList"
        :key="tooth"
        closable
        size="small"
        type="primary"
        @close="toggleTooth(tooth)"
      >
        {{ tooth }}
      </el-tag>
    </div>
    <div v-else class="tooth-selector__empty">未选择牙位，点击牙齿图标进行选择</div>
  </div>
</template>

<script>
export default {
  name: 'ToothSelector',
  props: {
    value: { type: [String, Array], default: '' }
  },
  data() {
    return {
      upperLeft: ['28', '27', '26', '25', '24', '23', '22', '21'],
      upperRight: ['11', '12', '13', '14', '15', '16', '17', '18'],
      lowerLeft: ['38', '37', '36', '35', '34', '33', '32', '31'],
      lowerRight: ['41', '42', '43', '44', '45', '46', '47', '48']
    }
  },
  computed: {
    selectedList() {
      if (Array.isArray(this.value)) {
        return this.value.map(item => String(item).trim()).filter(Boolean)
      }
      return String(this.value || '')
        .split(/[,，]/)
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
      const result = next.join(',')
      this.$emit('input', result)
      this.$emit('change', result)
    }
  }
}
</script>

<style scoped>
.tooth-selector {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tooth-selector__diagram {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
}

.tooth-selector__arch {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tooth-selector__arch-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
  text-align: center;
}

.tooth-selector__row {
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}

.tooth-item {
  cursor: pointer;
  transition: transform 0.15s ease;
  width: 40px;
}

.tooth-item:hover {
  transform: scale(1.1);
}

.tooth-item--selected .tooth-svg text {
  fill: #ffffff !important;
}

.tooth-svg {
  width: 100%;
  height: auto;
  display: block;
}

.tooth-selector__tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.tooth-selector__tags-label {
  font-size: 13px;
  color: #64748b;
}

.tooth-selector__empty {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}
</style>

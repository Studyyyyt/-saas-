<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    width="520px"
    :close-on-click-modal="false"
  >
    <el-form label-width="110px" class="material-form">
      <el-form-item label="耗材名称">
        <el-input v-model="form.name" :disabled="readonly" />
      </el-form-item>
      <el-form-item label="规格">
        <el-input v-model="form.spec" :disabled="readonly" />
      </el-form-item>
      <el-form-item label="品牌">
        <el-input v-model="form.brand" :disabled="readonly" />
      </el-form-item>
      <el-form-item label="分类">
        <CategoryTreeSelect
          v-model="form.category_id"
          :options="categories"
          :disabled="readonly"
          placeholder="请选择分类"
        />
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="form.unit" :disabled="readonly" placeholder="如：颗 / 盒 / 支" />
      </el-form-item>
      <el-form-item label="最低预警">
        <el-input-number v-model="form.min_stock_alert" :disabled="readonly" :min="0" controls-position="right" style="width:100%" />
      </el-form-item>
      <el-form-item label="当前库存">
        <el-input-number v-model="form.current_stock" :disabled="readonly" :min="0" controls-position="right" style="width:100%" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" :disabled="readonly" style="width:100%">
          <el-option v-for="item in MATERIAL_STATUS_OPTIONS" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :disabled="readonly" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <span slot="footer">
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button v-if="!readonly" type="primary" :loading="saving" @click="submit">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import axios from 'axios'
import CategoryTreeSelect from '@/components/CategoryTreeSelect.vue'
import { MATERIAL_STATUS_OPTIONS } from '@/utils/materialConstants'

function defaultForm() {
  return {
    id: null,
    name: '',
    spec: '',
    brand: '',
    category_id: '',
    unit: '',
    min_stock_alert: 0,
    current_stock: 0,
    status: '在用',
    remark: ''
  }
}

export default {
  name: 'MaterialDialog',
  components: { CategoryTreeSelect },
  props: {
    visible: { type: Boolean, default: false },
    material: { type: Object, default: () => ({}) },
    categories: { type: Array, default: () => [] },
    readonly: { type: Boolean, default: false }
  },
  data() {
    return {
      MATERIAL_STATUS_OPTIONS,
      form: defaultForm(),
      saving: false
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    dialogTitle() {
      if (this.readonly) return '耗材详情'
      return this.form.id ? '编辑耗材' : '新增耗材'
    }
  },
  watch: {
    visible(value) {
      if (value) {
        this.syncFromProps()
      }
    },
    material: {
      deep: true,
      handler() {
        if (this.visible) {
          this.syncFromProps()
        }
      }
    }
  },
  methods: {
    syncFromProps() {
      const source = this.material || {}
      this.form = Object.assign(defaultForm(), source, {
        category_id: source.category_id || '',
        min_stock_alert: Number(source.min_stock_alert || 0),
        current_stock: Number(source.current_stock || 0),
        status: source.status || '在用'
      })
    },
    validateForm() {
      if (!String(this.form.name || '').trim()) return '耗材名称不能为空'
      if (!this.form.category_id) return '请选择耗材分类'
      if (Number(this.form.min_stock_alert) < 0) return '最低库存预警不能小于0'
      if (Number(this.form.current_stock) < 0) return '当前库存不能小于0'
      return ''
    },
    async submit() {
      const validation = this.validateForm()
      if (validation) {
        this.$message.warning(validation)
        return
      }
      this.saving = true
      const payload = Object.assign({}, this.form, {
        category_id: Number(this.form.category_id),
        min_stock_alert: Number(this.form.min_stock_alert || 0),
        current_stock: Number(this.form.current_stock || 0)
      })
      const request = payload.id ? axios.put('/materials/edit', payload) : axios.post('/materials/add', payload)
      try {
        const res = await request
        if (res.data.code === '200') {
          this.$message.success(payload.id ? '更新成功' : '新增成功')
          this.$emit('saved')
          this.dialogVisible = false
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

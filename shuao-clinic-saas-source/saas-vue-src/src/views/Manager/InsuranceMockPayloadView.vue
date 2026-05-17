<template>
  <div class="insurance-page">
    <el-card class="page-card" shadow="never">
      <div class="page-head">
        <div>
          <div class="page-kicker">医保管理</div>
          <h2>医保 mock 报文</h2>
          <p>根据患者、处置收费项目和金额生成医保结算 mock 报文，用于未来接真实医保接口前的联调准备。</p>
        </div>
      </div>
    </el-card>

    <el-card class="form-card" shadow="never">
      <el-form :model="form" inline>
        <el-form-item label="患者ID">
          <el-input-number v-model="form.patientId" :min="1" controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item label="处置库项目ID">
          <el-input-number v-model="form.treatmentCatalogId" :min="1" controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item label="总金额">
          <el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="buildPayload">生成报文</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="result-card" shadow="never">
      <div slot="header" class="panel-title">报文结果</div>
      <el-empty v-if="!payloadText" description="暂未生成报文"></el-empty>
      <pre v-else class="payload-pre">{{ payloadText }}</pre>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'InsuranceMockPayloadView',
  data() {
    return {
      form: {
        patientId: null,
        treatmentCatalogId: null,
        totalAmount: 0
      },
      payloadText: ''
    }
  },
  methods: {
    buildPayload() {
      if (!this.form.patientId) {
        this.$message.warning('患者ID不能为空')
        return
      }
      axios.post('/insurances/mock/settlement-payload', this.form).then(res => {
        if (res.data.code === '200') {
          this.payloadText = JSON.stringify(res.data.data || {}, null, 2)
        } else {
          this.$message.error(res.data.msg || '生成医保 mock 报文失败')
        }
      }).catch(() => {
        this.$message.error('生成医保 mock 报文失败')
      })
    }
  }
}
</script>

<style scoped>
.insurance-page { display:flex; flex-direction:column; gap:18px; }
.page-card, .form-card, .result-card { border-radius:18px; }
.page-kicker { color:#2563eb; font-size:13px; font-weight:600; margin-bottom:8px; }
.page-head h2 { margin:0; font-size:28px; color:#0f172a; }
.page-head p { margin:10px 0 0; color:#64748b; line-height:1.7; }
.panel-title { font-weight:700; color:#0f172a; }
.payload-pre {
  background:#0f172a;
  color:#e2e8f0;
  padding:18px;
  border-radius:14px;
  overflow:auto;
  margin:0;
  font-size:13px;
  line-height:1.6;
}
</style>

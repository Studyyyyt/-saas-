<template>
  <div class="medical-record-ai-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">病历 AI 扩写配置</h1>
        <p class="page-subtitle">配置病历编辑页面中 AI 一键扩写的行为参数、字段规则与安全策略</p>
      </div>
    </div>

    <!-- 基础设置 -->
    <div class="section-card">
      <div class="section-title">基础设置</div>
      <div class="basic-settings">
        <div class="setting-row">
          <div class="setting-item">
            <div class="setting-label">功能开关</div>
            <el-switch v-model="config.enabled" active-text="启用" inactive-text="禁用" />
          </div>
          <div class="setting-item">
            <div class="setting-label">空字段处理策略</div>
            <el-radio-group v-model="config.emptyFieldStrategy">
              <el-radio label="leave">留白（不填充）</el-radio>
              <el-radio label="generate">AI 生成</el-radio>
              <el-radio label="prompt">提示医生手动填写</el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>
    </div>

    <!-- 字段配置 -->
    <div class="section-card">
      <div class="section-title">字段配置</div>
      <el-table :data="fieldConfigs" size="small" style="width: 100%">
        <el-table-column prop="fieldName" label="字段名" width="120" />
        <el-table-column label="启用扩写" width="110" align="center">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.enabled" size="mini" />
          </template>
        </el-table-column>
        <el-table-column label="最大字数" width="120">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.maxLength" :min="10" :max="2000" :step="10" size="mini" style="width: 90px" />
          </template>
        </el-table-column>
        <el-table-column label="默认值" min-width="180">
          <template slot-scope="scope">
            <el-input v-model="scope.row.defaultValue" size="mini" placeholder="未填写时自动填充此值" />
          </template>
        </el-table-column>
        <el-table-column label="必须包含" min-width="200">
          <template slot-scope="scope">
            <el-input v-model="scope.row.validationRule" size="mini" placeholder="关键词白名单，逗号分隔（如：考虑,疑似）" />
          </template>
        </el-table-column>
        <el-table-column label="校验提示" min-width="160">
          <template slot-scope="scope">
            <el-input v-model="scope.row.validationHint" size="mini" placeholder="校验失败时的提示语" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 外部扩写端点配置 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">外部扩写端点配置</div>
      </div>
      <div class="endpoint-settings">
        <div class="setting-row">
          <div class="setting-item">
            <div class="setting-label">端点地址 <span class="required">*</span></div>
            <el-input v-model="config.endpointUrl" placeholder="https://n8n.xxx.com/webhook/xxx" />
          </div>
          <div class="setting-item" style="width: 140px; flex-shrink: 0;">
            <div class="setting-label">请求方法</div>
            <el-select v-model="config.endpointMethod" style="width: 100%">
              <el-option label="POST" value="POST" />
              <el-option label="GET" value="GET" />
            </el-select>
          </div>
        </div>
        <div class="setting-row">
          <div class="setting-item">
            <div class="setting-label">认证类型</div>
            <el-select v-model="config.endpointAuthType" style="width: 100%">
              <el-option label="Bearer Token" value="bearer" />
              <el-option label="API Key" value="apikey" />
              <el-option label="无" value="none" />
            </el-select>
          </div>
          <div class="setting-item">
            <div class="setting-label">认证密钥</div>
            <el-input v-model="config.endpointAuthToken" type="password" placeholder="sk-xxx 或 webhook 密钥" show-password />
          </div>
        </div>
        <div class="form-group" style="margin-top: 8px;">
          <label class="form-label">请求体模板 <span class="required">*</span></label>
          <el-input
            v-model="config.requestTemplate"
            type="textarea"
            :rows="8"
            placeholder="请输入 JSON 请求模板..."
          />
          <div class="template-vars">
            <span class="template-vars-label">可用变量：</span>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{fields}}')">{{fields}}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{scene_id}}')">{{scene_id}}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{scene_name}}')">{{scene_name}}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{operations}}')">{{operations}}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{account_id}}')">{{account_id}}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{account_name}}')">{{account_name}}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertTemplateVar('{{enabled_fields}}')">{{enabled_fields}}</el-tag>
          </div>
          <div class="form-hint" style="margin-top:6px;color:#999;font-size:12px;">
            <i class="el-icon-info" /> 后端会将模板中的变量替换为实际值后，发送到外部端点。响应需为 JSON 格式，键名与字段名一致。
          </div>
        </div>
      </div>
    </div>

    <!-- 实时生效字段预览 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">实时生效 JSON 字段预览</div>
      </div>
      <div class="prompt-live-preview">
        <div class="live-preview-header">
          <span class="live-preview-title">当前启用的字段列表</span>
          <span class="live-preview-hint">字段开关将实时影响扩写时允许回填的字段范围</span>
        </div>
        <pre class="live-preview-code">{{ effectiveJsonPreview }}</pre>
        <div v-if="fieldConfigs.some(f => !f.enabled)" class="live-preview-tip">
          <i class="el-icon-warning-outline" />
          已禁用 {{ fieldConfigs.filter(f => !f.enabled).length }} 个字段，AI 将不会为其生成内容
        </div>
      </div>
    </div>

    <!-- 安全策略 -->
    <div class="section-card">
      <div class="section-title">安全策略</div>
      <div class="safety-settings">
        <div class="safety-item">
          <div class="safety-info">
            <div class="safety-label">禁用确诊性断言</div>
            <div class="safety-desc">拦截"确诊"、"绝对"、"保证"等确定性词汇</div>
          </div>
          <el-switch v-model="config.forbidAssertion" />
        </div>
        <div class="safety-item">
          <div class="safety-info">
            <div class="safety-label">敏感词拦截</div>
            <div class="safety-desc">AI 输出中包含以下词汇将被拦截</div>
          </div>
        </div>
        <div class="safety-input">
          <el-input
            v-model="config.sensitiveWords"
            type="textarea"
            :rows="2"
            placeholder="逗号分隔，如：确诊, 绝对, 保证, 100%, 肯定"
          />
        </div>
        <div class="safety-item">
          <div class="safety-info">
            <div class="safety-label">输出校验规则</div>
            <div class="safety-desc">AI 返回内容必须通过以下规则校验</div>
          </div>
        </div>
        <div class="safety-checks">
          <el-checkbox v-model="config.checkDiagnosisTone">诊断字段必须包含"考虑/疑似/待排"</el-checkbox>
          <el-checkbox v-model="config.checkChiefComplaintLength">主诉不得超过设定字数</el-checkbox>
          <el-checkbox v-model="config.checkHistoryTime">现病史必须包含时间描述</el-checkbox>
        </div>
      </div>
    </div>

    <!-- 诊疗场景库 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">诊疗场景库</div>
        <el-button size="small" type="primary" icon="el-icon-plus" @click="openSceneDialog()">
          新增病种
        </el-button>
      </div>
      <div v-if="scenes.length === 0" class="empty-mini">
        暂无诊疗场景，点击「新增病种」添加根管治疗、种植修复等病种及其步骤
      </div>
      <div v-else class="scene-list">
        <div v-for="scene in scenes" :key="scene.id" class="scene-item">
          <div class="scene-item-header">
            <div class="scene-item-main">
              <span class="scene-item-name">{{ scene.name }}</span>
              <el-tag size="mini" type="info">{{ scene.category }}</el-tag>
              <el-tag v-if="scene.level === 3" size="mini" type="warning">复杂（分步骤）</el-tag>
              <el-tag v-else-if="scene.level === 2" size="mini" type="info">中等</el-tag>
              <el-tag v-else size="mini" type="success">简单</el-tag>
            </div>
            <div class="scene-item-actions">
              <el-button type="text" size="mini" icon="el-icon-edit" @click="openSceneDialog(scene)">编辑</el-button>
              <el-button type="text" size="mini" icon="el-icon-delete" class="danger-text" @click="deleteScene(scene)">删除</el-button>
            </div>
          </div>
          <div v-if="scene.steps && scene.steps.length" class="scene-steps-preview">
            <div v-for="step in scene.steps" :key="step.id" class="scene-step-preview">
              <span class="step-dot"></span>
              <span class="step-name">{{ step.name }}</span>
              <el-tag v-if="step.forbiddenKeywords" size="mini" type="danger" class="step-tag">禁：{{ step.forbiddenKeywords }}</el-tag>
              <el-tag v-if="step.requiredKeywords" size="mini" type="success" class="step-tag">含：{{ step.requiredKeywords }}</el-tag>
            </div>
          </div>
          <div v-else class="scene-steps-preview">
            <span class="scene-no-steps">无分步骤，一键扩写即可</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 场景编辑弹窗 -->
    <el-dialog :visible.sync="sceneDialogVisible" title="编辑诊疗场景" width="680px" :close-on-click-modal="false" :modal="false">
      <div class="scene-form">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">病种名称 <span class="required">*</span></label>
            <el-input v-model="editingScene.name" placeholder="如：根管治疗" />
          </div>
          <div class="form-group">
            <label class="form-label">分类</label>
            <el-input v-model="editingScene.category" placeholder="如：牙体牙髓" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">复杂度</label>
            <el-select v-model="editingScene.level" style="width: 100%">
              <el-option label="简单（无需分步骤）" :value="1" />
              <el-option label="中等（可选分步骤）" :value="2" />
              <el-option label="复杂（必须分步骤）" :value="3" />
            </el-select>
          </div>
          <div class="form-group">
            <label class="form-label">排序</label>
            <el-input-number v-model="editingScene.sortOrder" :min="0" style="width: 100%" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">步骤配置</label>
          <div class="scene-steps-editor">
            <div v-for="(step, idx) in editingScene.steps" :key="idx" class="step-editor-row">
              <div class="step-editor-main">
                <el-input v-model="step.name" size="small" placeholder="步骤名称，如：开髓引流" style="width: 160px" />
                <el-input v-model="step.forbiddenKeywords" size="small" placeholder="禁止关键词，逗号分隔" style="width: 180px" />
                <el-input v-model="step.requiredKeywords" size="small" placeholder="必须包含关键词" style="width: 180px" />
              </div>
              <el-button type="text" size="mini" class="danger-text" icon="el-icon-delete" @click="removeEditingStep(idx)">删除</el-button>
            </div>
            <el-button size="small" icon="el-icon-plus" @click="addEditingStep">添加步骤</el-button>
          </div>
          <div class="form-hint">禁止关键词：AI 生成内容中绝对不能出现的词汇。必须包含关键词：AI 生成内容中必须提及的要点。</div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="sceneDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sceneSaving" @click="saveScene">保存</el-button>
      </div>
    </el-dialog>

    <!-- 效果测试 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">效果测试
          <el-tooltip content="测试扩写基于数据库中已保存的配置运行，修改字段开关或默认值后请先保存" placement="top">
            <i class="el-icon-warning-outline" style="margin-left:6px;color:var(--apple-warning);font-size:14px;cursor:pointer;" />
          </el-tooltip>
        </div>
        <div style="display:flex;gap:8px;align-items:center;">
          <el-button size="small" icon="el-icon-check" :loading="saveLoading" @click="saveConfig">
            保存
          </el-button>
          <el-tooltip content="开启时走 Mock 逻辑，关闭时尝试调用真实 AI 模型（需先配置模型供应商）" placement="top">
            <el-switch v-model="testUseMock" active-text="Mock 模式" inactive-text="真实 AI" />
          </el-tooltip>
          <el-button size="small" type="primary" icon="el-icon-magic-stick" :loading="testing" @click="testExpand">
            测试扩写
          </el-button>
        </div>
      </div>
      <div class="test-area">
        <div class="test-input">
          <label class="test-label">模拟医生填写（仅显示已启用的字段）</label>
          <div class="test-form">
            <div v-for="f in fieldConfigs.filter(x => x.enabled)" :key="f.fieldKey" class="test-form-item">
              <label class="test-form-label">{{ f.fieldName }}</label>
              <el-input v-model="testForm[f.fieldKey]" size="mini" :placeholder="'请输入' + f.fieldName + '...'" />
            </div>
            <div v-if="fieldConfigs.filter(x => x.enabled).length === 0" class="test-form-empty">
              未启用任何字段，请在上方字段配置中至少启用一个字段
            </div>
          </div>
        </div>
        <div class="test-arrow">
          <i class="el-icon-right"></i>
        </div>
        <div class="test-output">
          <label class="test-label">AI 扩写结果</label>
          <el-input
            v-model="testOutput"
            type="textarea"
            :rows="8"
            readonly
            placeholder="点击上方「测试扩写」按钮查看结果..."
          />
          <div v-if="testErrors.length" class="test-errors">
            <el-alert v-for="err in testErrors" :key="err" :title="err" type="warning" :closable="false" />
          </div>
          <el-collapse v-if="testTrace" v-model="testTraceActive" class="test-trace">
            <el-collapse-item title="链路详情" name="trace">
              <div class="trace-item">
                <span class="trace-key">调用模式：</span>
                <el-tag size="mini" :type="testTrace.mode === 'Mock' ? 'info' : 'success'">{{ testTrace.mode }}</el-tag>
              </div>
              <div class="trace-item">
                <span class="trace-key">空字段策略：</span>
                <span class="trace-value">{{ testTrace.strategy }}</span>
              </div>
              <div class="trace-item">
                <span class="trace-key">启用字段：</span>
                <span class="trace-value">{{ testTrace.enabledFields.join('、') }}</span>
              </div>
              <div v-if="testTrace.fieldSources.length" class="trace-item">
                <span class="trace-key">字段来源：</span>
                <div class="trace-source-list">
                  <el-tag v-for="s in testTrace.fieldSources" :key="s.key" size="mini" :type="s.sourceType === 'input' ? 'success' : (s.sourceType === 'leave' ? 'warning' : 'primary')" class="trace-source-tag">
                    {{ s.name }}: {{ s.source }}
                  </el-tag>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </div>

    <!-- 浮动保存按钮 -->
    <el-button
      v-if="unsaved"
      class="fab-save-btn"
      type="primary"
      icon="el-icon-check"
      :loading="saveLoading"
      @click="saveConfig"
    >
      保存配置
    </el-button>
  </div>
</template>

<script>
import axios from 'axios'

const defaultEndpointTemplate = JSON.stringify({
  fields: '{{fields}}',
  scene_id: '{{scene_id}}',
  scene_name: '{{scene_name}}',
  operations: '{{operations}}',
  account_id: '{{account_id}}',
  account_name: '{{account_name}}',
  enabled_fields: '{{enabled_fields}}'
}, null, 2)

const defaultFields = [
  { fieldKey: 'chiefComplaint', fieldName: '主诉', enabled: true, maxLength: 30, required: true, defaultValue: '', validationRule: '', validationHint: '主诉必须包含部位+症状+时间' },
  { fieldKey: 'historyOfPresentIllness', fieldName: '现病史', enabled: true, maxLength: 500, required: true, defaultValue: '', validationRule: '', validationHint: '现病史必须包含时间描述' },
  { fieldKey: 'pastHistory', fieldName: '既往史', enabled: true, maxLength: 300, required: false, defaultValue: '否认全身系统性疾病史，否认药物过敏史', validationRule: '', validationHint: '' },
  { fieldKey: 'generalCondition', fieldName: '一般情况', enabled: true, maxLength: 100, required: false, defaultValue: '精神可，饮食睡眠尚可，大小便正常', validationRule: '', validationHint: '' },
  { fieldKey: 'examinationFindings', fieldName: '检查所见', enabled: true, maxLength: 500, required: false, defaultValue: '', validationRule: '', validationHint: '' },
  { fieldKey: 'auxiliaryExamination', fieldName: '辅助检查', enabled: true, maxLength: 300, required: false, defaultValue: '暂缺，建议完善', validationRule: '', validationHint: '' },
  { fieldKey: 'diagnosis', fieldName: '诊断', enabled: true, maxLength: 100, required: true, defaultValue: '', validationRule: '', validationHint: '诊断必须用建议性语气' },
  { fieldKey: 'treatmentPlan', fieldName: '治疗计划', enabled: true, maxLength: 300, required: false, defaultValue: '', validationRule: '', validationHint: '' },
  { fieldKey: 'treatment', fieldName: '治疗文稿', enabled: true, maxLength: 500, required: false, defaultValue: '', validationRule: '', validationHint: '' },
  { fieldKey: 'medicalAdvice', fieldName: '医嘱', enabled: true, maxLength: 300, required: false, defaultValue: '', validationRule: '', validationHint: '' },
  { fieldKey: 'prescription', fieldName: '处方', enabled: true, maxLength: 200, required: false, defaultValue: '暂无', validationRule: '', validationHint: '' },
  { fieldKey: 'notes', fieldName: '病历备注', enabled: true, maxLength: 500, required: false, defaultValue: '', validationRule: '', validationHint: '' }
]

const defaultFieldDescriptions = {
  chiefComplaint: '主诉，≤30字，格式：部位+症状+时间',
  historyOfPresentIllness: '现病史，必须包含起病时间、主要症状、演变过程、诊疗经过',
  pastHistory: '既往史，包括全身疾病史、口腔疾病史、手术外伤史、过敏史等，无特殊可写\'否认全身系统性疾病史，否认药物过敏史\'',
  generalCondition: '一般情况，包括精神、饮食、睡眠、大小便等',
  examinationFindings: '口腔专科检查所见，包括视诊、叩诊、探诊、松动度、冷热测等',
  auxiliaryExamination: '辅助检查结果，如X线片、CBCT、血常规等，未做可写\'暂缺，建议完善\'',
  diagnosis: '诊断，必须用建议性语气（考虑/疑似/待排/可能），严禁使用确诊性词汇',
  treatmentPlan: '治疗计划，包括拟行治疗方案、步骤和预期效果',
  treatment: '治疗文稿，记录本次就诊实际进行的处置操作',
  medicalAdvice: '医嘱，包括注意事项、用药建议、复诊时间等',
  prescription: '处方，如有用药则记录药物名称和用法，无则写\'暂无\'',
  notes: '病历备注，记录特殊情况、患者诉求、沟通要点等'
}

export default {
  name: 'MedicalRecordAIConfigView',
  data() {
    return {
      saveLoading: false,
      testing: false,
      unsaved: false,
      scenes: [],
      sceneDialogVisible: false,
      sceneSaving: false,
      editingScene: { name: '', category: '其他', level: 1, sortOrder: 0, steps: [] },
      config: {
        enabled: true,
        emptyFieldStrategy: 'leave',
        forbidAssertion: true,
        sensitiveWords: '确诊, 绝对, 保证, 100%, 肯定',
        checkDiagnosisTone: true,
        checkChiefComplaintLength: true,
        checkHistoryTime: true,
        endpointUrl: '',
        endpointMethod: 'POST',
        endpointAuthType: 'none',
        endpointAuthToken: '',
        requestTemplate: defaultEndpointTemplate
      },
      fieldConfigs: JSON.parse(JSON.stringify(defaultFields)),
      testForm: {},
      testUseMock: true,
      testOutput: '',
      testErrors: [],
      testTrace: null,
      testTraceActive: []
    }
  },
  computed: {
    effectiveJsonPreview() {
      const enabledFields = this.fieldConfigs.filter(f => f.enabled)
      if (enabledFields.length === 0) {
        return '// 未启用任何扩写字段，AI 将不会生成任何病历内容'
      }
      const lines = enabledFields.map((f, i) => {
        const desc = defaultFieldDescriptions[f.fieldKey] || f.fieldName
        const maxlen = f.maxLength ? `，≤${f.maxLength}字` : ''
        const defval = f.defaultValue ? `，默认值：${f.defaultValue}` : ''
        const last = i === enabledFields.length - 1 ? '' : ','
        return `  "${f.fieldKey}": "${desc}${maxlen}${defval}"${last}`
      })
      return `{\n${lines.join('\n')}\n}`
    }
  },
  watch: {
    fieldConfigs: {
      deep: true,
      handler() {
        this.initTestForm()
        this.unsaved = true
      }
    },
    config: {
      deep: true,
      handler() {
        this.unsaved = true
      }
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    async loadConfig() {
      try {
        const res = await axios.get('/api/ai-config/medical-record')
        if (res.data && res.data.code === '200' && res.data.data) {
          const data = res.data.data
          if (data.config) {
            this.config = {
              enabled: data.config.enabled !== undefined ? data.config.enabled : true,
              emptyFieldStrategy: data.config.emptyFieldStrategy || 'leave',
              forbidAssertion: data.config.forbidAssertion !== undefined ? data.config.forbidAssertion : true,
              sensitiveWords: data.config.sensitiveWords || '确诊, 绝对, 保证, 100%, 肯定',
              checkDiagnosisTone: data.config.checkDiagnosisTone !== undefined ? data.config.checkDiagnosisTone : true,
              checkChiefComplaintLength: data.config.checkChiefComplaintLength !== undefined ? data.config.checkChiefComplaintLength : true,
              checkHistoryTime: data.config.checkHistoryTime !== undefined ? data.config.checkHistoryTime : true,
              endpointUrl: data.config.endpointUrl || '',
              endpointMethod: data.config.endpointMethod || 'POST',
              endpointAuthType: data.config.endpointAuthType || 'none',
              endpointAuthToken: data.config.endpointAuthToken || '',
              requestTemplate: (data.config.requestTemplate && String(data.config.requestTemplate).trim()) ? String(data.config.requestTemplate).trim() : defaultEndpointTemplate
            }
          }
          if (data.fields && data.fields.length > 0) {
            this.fieldConfigs = data.fields.map(f => ({
              fieldKey: f.fieldKey,
              fieldName: f.fieldName,
              enabled: f.enabled != null ? f.enabled : (f.isEnabled != null ? f.isEnabled : true),
              maxLength: f.maxLength || 100,
              required: f.required != null ? f.required : (f.isRequired != null ? f.isRequired : false),
              defaultValue: f.defaultValue || '',
              validationRule: f.validationRule || '',
              validationHint: f.validationHint || ''
            }))
          }
          this.initTestForm()
        }
      } catch (e) {
        console.warn('加载配置失败', e)
      }
      this.loadScenes()
    },
    initTestForm() {
      const form = {}
      for (const f of this.fieldConfigs) {
        if (f.enabled) {
          form[f.fieldKey] = this.testForm[f.fieldKey] || ''
        }
      }
      this.testForm = form
    },
    async loadScenes() {
      try {
        const res = await axios.get('/api/treatment-scenes')
        if (res.data && res.data.code === '200') {
          this.scenes = res.data.data || []
          // 为每个场景加载步骤
          for (const scene of this.scenes) {
            const stepRes = await axios.get(`/api/treatment-scenes/${scene.id}/steps`)
            if (stepRes.data && stepRes.data.code === '200') {
              scene.steps = stepRes.data.data || []
            }
          }
        }
      } catch (e) {
        console.warn('加载场景失败', e)
      }
    },
    openSceneDialog(scene) {
      if (scene) {
        this.editingScene = {
          id: scene.id,
          name: scene.name,
          category: scene.category,
          level: scene.level,
          sortOrder: scene.sortOrder || 0,
          steps: (scene.steps || []).map(s => ({
            id: s.id,
            name: s.name,
            forbiddenKeywords: s.forbiddenKeywords || '',
            requiredKeywords: s.requiredKeywords || '',
            sortOrder: s.sortOrder,
            enabled: s.enabled
          }))
        }
      } else {
        this.editingScene = { name: '', category: '其他', level: 1, sortOrder: 0, steps: [] }
      }
      this.sceneDialogVisible = true
    },
    addEditingStep() {
      this.editingScene.steps.push({
        name: '',
        forbiddenKeywords: '',
        requiredKeywords: '',
        sortOrder: this.editingScene.steps.length,
        enabled: true
      })
    },
    removeEditingStep(idx) {
      this.editingScene.steps.splice(idx, 1)
    },
    async saveScene() {
      if (!this.editingScene.name.trim()) {
        this.$message.warning('请输入病种名称')
        return
      }
      this.sceneSaving = true
      try {
        const payload = {
          id: this.editingScene.id,
          name: this.editingScene.name.trim(),
          category: this.editingScene.category,
          level: this.editingScene.level,
          sortOrder: this.editingScene.sortOrder,
          steps: this.editingScene.steps.filter(s => s.name.trim()).map((s, i) => ({
            id: s.id,
            name: s.name.trim(),
            forbiddenKeywords: s.forbiddenKeywords,
            requiredKeywords: s.requiredKeywords,
            sortOrder: i,
            enabled: s.enabled !== false
          }))
        }
        const res = await axios.post('/api/treatment-scenes', payload)
        if (res.data && res.data.code === '200') {
          this.$message.success('保存成功')
          this.sceneDialogVisible = false
          this.loadScenes()
        } else {
          this.$message.error(res.data.msg || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.sceneSaving = false
      }
    },
    deleteScene(scene) {
      this.$confirm(`确定删除「${scene.name}」吗？`, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await axios.delete(`/api/treatment-scenes/${scene.id}`)
          if (res.data && res.data.code === '200') {
            this.$message.success('删除成功')
            this.loadScenes()
          } else {
            this.$message.error(res.data.msg || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    insertTemplateVar(varName) {
      const textarea = this.$el.querySelector('.endpoint-settings textarea')
      if (!textarea) return
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const text = this.config.requestTemplate
      this.config.requestTemplate = text.substring(0, start) + varName + text.substring(end)
      this.$nextTick(() => {
        textarea.focus()
        textarea.setSelectionRange(start + varName.length, start + varName.length)
      })
    },
    async saveConfig() {
      this.saveLoading = true
      const payload = {
        config: this.config,
        fields: this.fieldConfigs
      }
      try {
        const res = await axios.put('/api/ai-config/medical-record', payload)
        if (res.data && res.data.code === '200') {
          this.unsaved = false
          this.$message.success('配置已保存')
          // 同时保存一份到 localStorage，供 MedicalRecordView 读取 enabled_fields
          try {
            localStorage.setItem('saas_medical_ai_config', JSON.stringify({ fields: this.fieldConfigs }))
          } catch (e) {
            console.warn('本地缓存病历 AI 配置失败', e)
          }
        } else {
          this.$message.warning(res.data.msg || '保存失败')
        }
      } catch (e) {
        this.$message.error('保存失败：' + (e.message || '未知错误'))
      } finally {
        this.saveLoading = false
      }
    },
    async testExpand() {
      const enabledFields = this.fieldConfigs.filter(f => f.enabled)
      if (enabledFields.length === 0) {
        this.$message.warning('请先至少启用一个扩写字段')
        return
      }
      this.testing = true
      this.testOutput = ''
      this.testErrors = []
      this.testTrace = null
      try {
        const fields = {}
        for (const f of enabledFields) {
          fields[f.fieldKey] = this.testForm[f.fieldKey] || ''
        }
        const payload = { fields: fields }
        if (this.testUseMock) {
          payload.testMode = true
        }
        const res = await axios.post('/api/ai/proxy/medical-expand', payload)
        if (res.data && res.data.code === '200') {
          const result = res.data.data || {}
          this.testOutput = JSON.stringify(result, null, 2)
          this.buildTestTrace(result)
          this.$message.success('扩写完成')
        } else if (res.data && res.data.data && res.data.data.errors) {
          this.testErrors = res.data.data.errors
          this.$message.warning('AI 输出未通过安全校验')
        } else {
          this.$message.warning(res.data.msg || '扩写失败')
        }
      } catch (e) {
        this.$message.error('测试失败：' + (e.message || '未知错误'))
      } finally {
        this.testing = false
      }
    },
    buildTestTrace(result) {
      const enabledFields = this.fieldConfigs.filter(f => f.enabled)
      const strategyMap = { leave: '留白（未填写字段返回空）', generate: 'AI 生成（未填写字段智能生成）', prompt: '提示医生（未填写字段提示手动填写）' }
      const fieldSources = []
      for (const f of enabledFields) {
        const userInput = (this.testForm[f.fieldKey] || '').trim()
        const hasResult = result.hasOwnProperty(f.fieldKey)
        const outputValue = hasResult ? String(result[f.fieldKey] || '') : ''
        let sourceType, source
        if (!hasResult) {
          sourceType = 'warning'
          source = 'AI 未返回'
        } else if (!outputValue) {
          if (this.config.emptyFieldStrategy === 'leave') {
            sourceType = 'leave'
            source = '留白'
          } else if (this.config.emptyFieldStrategy === 'prompt') {
            sourceType = 'leave'
            source = '提示医生'
          } else {
            sourceType = 'ai'
            source = 'AI 生成（空）'
          }
        } else if (userInput) {
          sourceType = 'input'
          source = '基于输入扩写'
        } else if (f.defaultValue && outputValue === f.defaultValue) {
          sourceType = 'info'
          source = '默认值填充'
        } else {
          sourceType = 'ai'
          source = 'AI 生成'
        }
        fieldSources.push({ key: f.fieldKey, name: f.fieldName, sourceType, source })
      }
      this.testTrace = {
        mode: this.testUseMock ? 'Mock' : '真实 AI',
        strategy: strategyMap[this.config.emptyFieldStrategy] || this.config.emptyFieldStrategy,
        enabledFields: enabledFields.map(f => f.fieldName),
        fieldSources: fieldSources
      }
    }
  }
}
</script>

<style scoped>
.medical-record-ai-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--apple-text-primary);
  line-height: 1.2;
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--apple-text-secondary);
}

.section-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

/* 基础设置 */
.basic-settings {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setting-row {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.setting-item {
  flex: 1;
  min-width: 280px;
}

.setting-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 8px;
}

.setting-hint {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  margin-top: 4px;
}

/* 表单 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.form-hint {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  line-height: 1.5;
}

.required {
  color: var(--apple-danger);
}

/* 外部端点配置 */
.endpoint-settings {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.template-vars {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.template-vars-label {
  font-size: 12px;
  color: var(--apple-text-tertiary);
}

.var-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.var-tag:hover {
  color: var(--apple-accent);
  border-color: var(--apple-accent);
}

/* 实时生效字段预览 */
.prompt-live-preview {
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-divider);
  border-radius: 12px;
  padding: 16px;
}

.live-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.live-preview-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.live-preview-hint {
  font-size: 12px;
  color: var(--apple-text-tertiary);
}

.live-preview-code {
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 8px;
  padding: 12px 16px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.8;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}

.live-preview-tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--apple-warning);
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Few-shot */
.empty-mini {
  text-align: center;
  padding: 32px;
  font-size: 13px;
  color: var(--apple-text-tertiary);
}

.danger-text {
  color: var(--apple-danger);
}

/* 安全策略 */
.safety-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.safety-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.safety-info {
  flex: 1;
}

.safety-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 2px;
}

.safety-desc {
  font-size: 12px;
  color: var(--apple-text-tertiary);
}

.safety-input {
  padding-left: 0;
}

.safety-checks {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 测试区 */
.test-area {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

.test-input,
.test-output {
  flex: 1;
  min-width: 0;
}

.test-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 8px;
}

.test-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-text-tertiary);
  font-size: 20px;
  padding-top: 24px;
}

.test-errors {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 320px;
  overflow-y: auto;
  padding-right: 4px;
}

.test-form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.test-form-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--apple-text-secondary);
}

.test-form-empty {
  font-size: 13px;
  color: var(--apple-text-tertiary);
  text-align: center;
  padding: 24px 0;
}

.test-trace {
  margin-top: 12px;
}

.trace-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.trace-key {
  font-weight: 600;
  color: var(--apple-text-secondary);
  white-space: nowrap;
}

.trace-value {
  color: var(--apple-text-primary);
}

.trace-source-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.trace-source-tag {
  margin-right: 0 !important;
}

.fab-save-btn {
  position: fixed;
  right: 32px;
  bottom: 32px;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.fab-save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

@media (max-width: 768px) {
  .fab-save-btn {
    right: 16px;
    bottom: 16px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .setting-row {
    flex-direction: column;
    gap: 16px;
  }

  .test-area {
    flex-direction: column;
  }

  .test-arrow {
    padding-top: 0;
    transform: rotate(90deg);
  }
}

/* 场景列表 */
.scene-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.scene-item {
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-divider);
  border-radius: 12px;
  padding: 16px;
}
.scene-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.scene-item-main {
  display: flex;
  align-items: center;
  gap: 8px;
}
.scene-item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
}
.scene-item-actions {
  display: flex;
  gap: 8px;
}
.scene-steps-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-left: 8px;
}
.scene-step-preview {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--apple-text-secondary);
  background: rgba(255, 255, 255, 0.7);
  padding: 4px 10px;
  border-radius: 8px;
}
.step-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--apple-accent);
}
.step-name {
  font-weight: 500;
}
.step-tag {
  margin: 0 !important;
}
.scene-no-steps {
  font-size: 12px;
  color: var(--apple-text-tertiary);
  font-style: italic;
}

/* 场景表单 */
.scene-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.scene-steps-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.step-editor-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.step-editor-main {
  flex: 1;
  display: flex;
  gap: 8px;
}
</style>

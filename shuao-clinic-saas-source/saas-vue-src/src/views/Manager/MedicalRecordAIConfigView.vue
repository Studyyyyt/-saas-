<template>
  <div class="medical-record-ai-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">病历 AI 扩写配置</h1>
        <p class="page-subtitle">配置病历编辑页面中 AI 一键扩写的行为参数、提示词与安全策略</p>
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
            <div class="setting-label">默认温度: {{ config.temperature }}</div>
            <el-slider v-model="config.temperature" :min="0.1" :max="1.0" :step="0.1" show-stops />
            <div class="setting-hint">低温度（0.1-0.3）输出更稳定，高温度更具创造性</div>
          </div>
        </div>
        <div class="setting-row">
          <div class="setting-item">
            <div class="setting-label">最大输出 Token</div>
            <el-input-number v-model="config.maxTokens" :min="500" :max="4000" :step="500" />
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

    <!-- 提示词模板 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">提示词模板</div>
        <div style="display:flex;gap:8px;">
          <el-button size="small" icon="el-icon-view" @click="previewPrompt">Prompt 预览</el-button>
          <el-button size="small" icon="el-icon-document-copy" @click="resetPrompt">恢复默认</el-button>
        </div>
      </div>
      <div class="prompt-section">
        <div class="form-group">
          <label class="form-label">系统提示词（System Prompt）</label>
          <el-input
            v-model="config.systemPrompt"
            type="textarea"
            :rows="10"
            placeholder="定义 AI 扩写病历时的角色与行为准则..."
          />
          <div class="prompt-actions">
            <el-tag size="mini" type="info">可插入变量:</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertVar('{kb_content}')">{kb_content}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertVar('{input_fields}')">{input_fields}</el-tag>
            <el-tag size="mini" class="var-tag" @click="insertVar('{disease_type}')">{disease_type}</el-tag>
          </div>
          <div class="prompt-hint" style="margin-top:6px;color:#999;font-size:12px;">
            <i class="el-icon-info" /> 下方配置的 Few-shot 示例与诊疗场景约束将在后端自动追加到本提示词末尾，无需手动写入。可在"完整 Prompt 实时预览"中查看最终效果。
          </div>
        </div>
      </div>

      <!-- 实时生效字段预览 -->
      <div class="prompt-live-preview">
        <div class="live-preview-header">
          <span class="live-preview-title">实时生效 JSON 字段预览</span>
          <span class="live-preview-hint">字段开关将实时影响 Prompt 中发送给 AI 的 JSON 字段列表</span>
        </div>
        <pre class="live-preview-code">{{ effectiveJsonPreview }}</pre>
        <div v-if="fieldConfigs.some(f => !f.enabled)" class="live-preview-tip">
          <i class="el-icon-warning-outline" />
          已禁用 {{ fieldConfigs.filter(f => !f.enabled).length }} 个字段，AI 将不会为其生成内容
        </div>
      </div>
    </div>

    <!-- Few-shot 示例 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">Few-shot 示例（{{ fewShotExamples.length }} 条）</div>
        <el-button size="small" icon="el-icon-plus" @click="addFewShot">添加示例</el-button>
      </div>
      <div v-if="fewShotExamples.length === 0" class="empty-mini">
        暂无示例，添加后 AI 会参考这些示例的格式和风格进行扩写
      </div>
      <div v-else class="few-shot-list">
        <div v-for="(ex, idx) in fewShotExamples" :key="idx" class="few-shot-item">
          <div class="few-shot-header">
            <span class="few-shot-title">示例 {{ idx + 1 }}</span>
            <el-button type="text" size="mini" class="danger-text" @click="removeFewShot(idx)">删除</el-button>
          </div>
          <div class="few-shot-body">
            <div class="few-shot-field">
              <label>简要输入</label>
              <el-input v-model="ex.input" type="textarea" :rows="2" size="small" placeholder="医生填写的简要内容" />
            </div>
            <div class="few-shot-field">
              <label>扩写输出</label>
              <el-input v-model="ex.output" type="textarea" :rows="3" size="small" placeholder="AI 扩写后的专业内容" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 完整 Prompt 实时预览 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">完整 Prompt 实时预览</div>
        <el-tag v-if="livePromptPreviewLoading" size="mini" type="info">生成中...</el-tag>
      </div>
      <div class="live-prompt-preview">
        <pre v-if="livePromptPreview">{{ livePromptPreview }}</pre>
        <div v-else class="empty-mini">修改系统提示词或 Few-shot 示例后，此处将实时显示最终发送给 AI 的完整 Prompt</div>
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

    <!-- Prompt 预览弹窗 -->
    <el-dialog :visible.sync="promptPreviewVisible" title="Prompt 预览" width="800px" :close-on-click-modal="false" :modal="false">
      <div style="position: relative;">
        <textarea
          v-model="promptPreviewContent"
          rows="20"
          style="width: 100%; font-family: monospace; font-size: 13px; line-height: 1.6; padding: 8px 12px; box-sizing: border-box; border: 1px solid #dcdfe6; border-radius: 4px; resize: vertical; outline: none; color: #606266;"
        />
        <div v-if="promptPreviewLoading" class="prompt-loading-overlay">
          <i class="el-icon-loading" /> 正在渲染 Prompt...
        </div>
      </div>
      <div slot="footer">
        <el-button @click="promptPreviewVisible = false">关闭</el-button>
        <el-button type="primary" icon="el-icon-document-copy" @click="copyPrompt">复制</el-button>
        <el-button type="success" icon="el-icon-check" @click="applyPreviewToPrompt">应用到编辑区</el-button>
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

const defaultPrompt = `你是一位资深口腔全科医生助理，擅长将简要病历记录扩写为规范的专业病历。

【任务】
根据医生填写的简要信息，为病历所有字段生成完整、规范的内容。
- 对于医生已填写的字段：基于已有内容进行扩充、规范化和专业化
- 对于医生未填写的字段：根据主诉和已填信息，结合口腔医学常识，智能推断并生成合理内容
- 不允许有任何字段为空字符串，每个字段都必须有实质内容

【知识库参考】（仅参考格式和术语，不编造未提及的病情）
{kb_content}

【输入信息】
{input_fields}

【输出格式要求】
严格按以下 JSON 格式返回，不要包含任何其他内容，必须包含全部字段：
{
  "chiefComplaint": "主诉，≤30字，格式：部位+症状+时间",
  "historyOfPresentIllness": "现病史，必须包含起病时间、主要症状、演变过程、诊疗经过",
  "pastHistory": "既往史，包括全身疾病史、口腔疾病史、手术外伤史、过敏史等，无特殊可写'否认全身系统性疾病史，否认药物过敏史'",
  "generalCondition": "一般情况，包括精神、饮食、睡眠、大小便等",
  "examinationFindings": "口腔专科检查所见，包括视诊、叩诊、探诊、松动度、冷热测等",
  "auxiliaryExamination": "辅助检查结果，如X线片、CBCT、血常规等，未做可写'暂缺，建议完善'",
  "diagnosis": "诊断，必须用建议性语气（考虑/疑似/待排/可能），严禁使用确诊性词汇",
  "treatmentPlan": "治疗计划，包括拟行治疗方案、步骤和预期效果",
  "treatment": "治疗文稿，记录本次就诊实际进行的处置操作",
  "medicalAdvice": "医嘱，包括注意事项、用药建议、复诊时间等",
  "prescription": "处方，如有用药则记录药物名称和用法，无则写'暂无'",
  "notes": "病历备注，记录特殊情况、患者诉求、沟通要点等"
}

【绝对禁止】
1. 禁止编造患者未提及的症状、检查结果
2. 禁止输出确诊性断言，诊断严禁使用'确诊'、'明确诊断'、'肯定'等词汇
3. 禁止输出具体药物剂量
4. 禁止输出"建议到上级医院"等推诿用语
5. 禁止任何字段返回空字符串，未填写字段必须智能生成合理内容

【语气要求】
专业、客观、严谨，使用标准口腔医学术语。`

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
        temperature: 0.2,
        maxTokens: 2000,
        emptyFieldStrategy: 'leave',
        systemPrompt: defaultPrompt,
        forbidAssertion: true,
        sensitiveWords: '确诊, 绝对, 保证, 100%, 肯定',
        checkDiagnosisTone: true,
        checkChiefComplaintLength: true,
        checkHistoryTime: true
      },
      fieldConfigs: JSON.parse(JSON.stringify(defaultFields)),
      fewShotExamples: [
        {
          input: '主诉：牙痛3天\n现病史：3天前开始牙痛，吃了止痛药没好',
          output: '主诉：右下后牙自发痛3天\n现病史：患者3天前无明显诱因出现右下后牙自发性疼痛，呈阵发性发作，每次持续约10-15分钟，冷热刺激可加重疼痛，夜间疼痛明显，伴同侧头面部放射痛。自行口服止痛药物（具体不详）后症状无明显缓解，为求进一步诊治来我院就诊。'
        }
      ],
      testForm: {},
      testUseMock: true,
      testOutput: '',
      testErrors: [],
      testTrace: null,
      testTraceActive: [],
      promptPreviewVisible: false,
      promptPreviewContent: '',
      promptPreviewLoading: false,
      // 完整 Prompt 实时预览
      livePromptPreview: '',
      livePromptPreviewLoading: false,
      livePromptPreviewDebounceTimer: null
    }
  },
  computed: {
    effectiveJsonPreview() {
      const enabledFields = this.fieldConfigs.filter(f => f.enabled)
      if (enabledFields.length === 0) {
        return '// 未启用任何扩写字段，AI 将不会生成任何病历内容'
      }
      const lines = enabledFields.map((f, i) => {
        const desc = this.getFieldDescFromPrompt(f.fieldKey) || f.fieldName
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
        this.syncPromptJsonBlock()
        this.initTestForm()
        this.unsaved = true
        this.scheduleLivePreviewUpdate()
      }
    },
    config: {
      deep: true,
      handler() {
        this.unsaved = true
        this.scheduleLivePreviewUpdate()
      }
    },
    fewShotExamples: {
      deep: true,
      handler() {
        this.unsaved = true
        this.scheduleLivePreviewUpdate()
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
              temperature: data.config.temperature !== undefined ? data.config.temperature : 0.2,
              maxTokens: data.config.maxTokens !== undefined ? data.config.maxTokens : 2000,
              emptyFieldStrategy: data.config.emptyFieldStrategy || 'leave',
              systemPrompt: (data.config.systemPrompt && String(data.config.systemPrompt).trim()) ? String(data.config.systemPrompt).trim() : defaultPrompt,
              forbidAssertion: data.config.forbidAssertion !== undefined ? data.config.forbidAssertion : true,
              sensitiveWords: data.config.sensitiveWords || '确诊, 绝对, 保证, 100%, 肯定',
              checkDiagnosisTone: data.config.checkDiagnosisTone !== undefined ? data.config.checkDiagnosisTone : true,
              checkChiefComplaintLength: data.config.checkChiefComplaintLength !== undefined ? data.config.checkChiefComplaintLength : true,
              checkHistoryTime: data.config.checkHistoryTime !== undefined ? data.config.checkHistoryTime : true
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
          if (data.fewShots) {
            this.fewShotExamples = data.fewShots.map(fs => ({
              id: fs.id,
              input: fs.input || '',
              output: fs.output || ''
            }))
          }
        }
      } catch (e) {
        console.warn('加载配置失败', e)
      }
      this.loadScenes()
      // 加载完成后立即刷新一次实时预览
      this.$nextTick(() => {
        this.updateLivePreview()
      })
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
    insertVar(varName) {
      const textarea = this.$el.querySelector('.prompt-section textarea')
      if (!textarea) return
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const text = this.config.systemPrompt
      this.config.systemPrompt = text.substring(0, start) + varName + text.substring(end)
      this.$nextTick(() => {
        textarea.focus()
        textarea.setSelectionRange(start + varName.length, start + varName.length)
      })
    },
    syncPromptJsonBlock() {
      const prompt = this.config.systemPrompt || ''
      // 匹配 prompt 中多行的 JSON 对象块：\n{\n  "key": "desc",\n  ...\n}
      const regex = /(\n\{\n)([\s\S]*?)(\n\})/
      const match = prompt.match(regex)
      if (!match) return

      const enabledFields = this.fieldConfigs.filter(f => f.enabled)
      if (enabledFields.length === 0) {
        const newBlock = '\n{\n  // 未启用任何扩写字段，AI 将不会生成任何病历内容\n}'
        this.config.systemPrompt = prompt.replace(regex, newBlock)
        return
      }

      const lines = enabledFields.map((f, i) => {
        let desc = this.getFieldDescFromPrompt(f.fieldKey)
        if (!desc) {
          desc = defaultFieldDescriptions[f.fieldKey] || f.fieldName
        }
        const last = i === enabledFields.length - 1 ? '' : ','
        return `  "${f.fieldKey}": "${desc}"${last}`
      })

      const newBlock = '\n{\n' + lines.join('\n') + '\n}'
      this.config.systemPrompt = prompt.replace(regex, newBlock)
    },
    resetPrompt() {
      this.$confirm('确定恢复默认配置吗？当前修改将丢失并自动保存。', '提示', {
        confirmButtonText: '恢复并保存',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        // 恢复所有默认配置
        this.config.systemPrompt = defaultPrompt
        this.fieldConfigs = JSON.parse(JSON.stringify(defaultFields))
        this.fewShotExamples = []
        this.$message.success('已恢复默认配置')
        // 自动保存到数据库
        await this.saveConfig()
      }).catch(() => {})
    },
    addFewShot() {
      this.fewShotExamples.push({ input: '', output: '' })
    },
    removeFewShot(idx) {
      this.fewShotExamples.splice(idx, 1)
    },
    async saveConfig() {
      this.saveLoading = true
      const payload = {
        config: this.config,
        fields: this.fieldConfigs,
        fewShots: this.fewShotExamples
      }
      try {
        const res = await axios.put('/api/ai-config/medical-record', payload)
        if (res.data && res.data.code === '200') {
          this.unsaved = false
          this.$message.success('配置已保存')
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
        const res = await axios.post('/api/ai/medical-record/expand', payload)
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
    },
    scheduleLivePreviewUpdate() {
      if (this.livePromptPreviewDebounceTimer) {
        clearTimeout(this.livePromptPreviewDebounceTimer)
      }
      this.livePromptPreviewDebounceTimer = setTimeout(() => {
        this.updateLivePreview()
      }, 800)
    },
    async updateLivePreview() {
      this.livePromptPreviewLoading = true
      try {
        const fields = {}
        for (const f of this.fieldConfigs.filter(x => x.enabled)) {
          fields[f.fieldKey] = this.testForm[f.fieldKey] || ''
        }
        const res = await axios.post('/api/ai-config/medical-record/preview', {
          fields: fields,
          testMode: true,
          systemPrompt: this.config.systemPrompt,
          emptyFieldStrategy: this.config.emptyFieldStrategy,
          fewShots: this.fewShotExamples.map(ex => ({ input: ex.input, output: ex.output }))
        })
        if (res.data && res.data.code === '200') {
          this.livePromptPreview = res.data.data
        } else {
          this.livePromptPreview = '// 预览生成失败：' + (res.data.msg || '未知错误')
        }
      } catch (e) {
        this.livePromptPreview = '// 预览生成失败：' + (e.message || '网络错误')
      } finally {
        this.livePromptPreviewLoading = false
      }
    },
    async previewPrompt() {
      this.promptPreviewVisible = true
      this.promptPreviewLoading = true
      this.promptPreviewContent = ''
      try {
        const fields = {}
        for (const f of this.fieldConfigs.filter(x => x.enabled)) {
          fields[f.fieldKey] = this.testForm[f.fieldKey] || ''
        }
        const res = await axios.post('/api/ai-config/medical-record/preview', {
          fields: fields,
          testMode: true,
          systemPrompt: this.config.systemPrompt,
          emptyFieldStrategy: this.config.emptyFieldStrategy,
          fewShots: this.fewShotExamples.map(ex => ({ input: ex.input, output: ex.output }))
        })
        if (res.data && res.data.code === '200') {
          this.promptPreviewContent = res.data.data
        } else {
          this.$message.warning(res.data.msg || '预览失败')
        }
      } catch (e) {
        this.$message.error('预览失败：' + (e.message || '未知错误'))
      } finally {
        this.promptPreviewLoading = false
      }
    },
    copyPrompt() {
      if (!this.promptPreviewContent) return
      const textarea = document.createElement('textarea')
      textarea.value = this.promptPreviewContent
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('已复制到剪贴板')
    },
    applyPreviewToPrompt() {
      if (!this.promptPreviewContent) {
        this.$message.warning('预览内容为空，无法应用')
        return
      }
      // 去掉后端自动追加的区块，避免保存后重复渲染
      let cleaned = this.promptPreviewContent
      const autoBlocks = ['\n\n【扩写示例】', '\n\n【当前诊疗场景】']
      for (const block of autoBlocks) {
        const idx = cleaned.indexOf(block)
        if (idx >= 0) {
          cleaned = cleaned.substring(0, idx)
        }
      }
      cleaned = cleaned.trim()

      this.$confirm(
        '预览内容包含后端自动追加的字段格式、Few-shot 示例和场景约束等，直接应用会自动去除这些部分，仅保留基础模板内容。',
        '应用到系统提示词',
        {
          confirmButtonText: '应用',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        this.config.systemPrompt = cleaned
        this.promptPreviewVisible = false
        this.$message.success('已应用到系统提示词编辑区')
      }).catch(() => {})
    },
    getFieldDescFromPrompt(fieldKey) {
      const prompt = this.config.systemPrompt || ''
      const regex = new RegExp(`"${fieldKey}"\\s*:\\s*"([^"]+)"`)
      const match = prompt.match(regex)
      return match ? match[1] : ''
    },
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

.prompt-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
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
  margin-top: 16px;
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

/* Prompt 预览弹窗 loading 遮罩 */
.prompt-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  z-index: 10;
  color: var(--apple-text-secondary);
  font-size: 14px;
  gap: 8px;
}

/* 实时 Prompt 预览 */
.live-prompt-preview {
  background: var(--apple-bg-primary);
  border: 1px solid var(--apple-divider);
  border-radius: 12px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.live-prompt-preview pre {
  margin: 0;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: var(--apple-text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}

/* Few-shot */
.empty-mini {
  text-align: center;
  padding: 32px;
  font-size: 13px;
  color: var(--apple-text-tertiary);
}

.few-shot-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.few-shot-item {
  border: 1px solid var(--apple-divider);
  border-radius: 12px;
  padding: 16px;
  background: var(--apple-bg-primary);
}

.few-shot-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.few-shot-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.few-shot-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.few-shot-field label {
  display: block;
  font-size: 12px;
  color: var(--apple-text-tertiary);
  margin-bottom: 4px;
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

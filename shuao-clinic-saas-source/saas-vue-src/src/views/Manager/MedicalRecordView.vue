<template>
  <div class="page-wrap medical-record-page">
    <template v-if="editorVisible">
      <el-form ref="formRef" :model="form" :rules="rules" class="editor-form" @submit.native.prevent>
        <el-card class="editor-head-card" shadow="never">
          <div class="editor-head">
            <div class="editor-head__main">
              <div class="page-kicker">病历工作台</div>
              <h2>{{ dialogTitle }}</h2>
              <p>{{ currentPatientSummary }}</p>
            </div>
            <div class="editor-head__actions">
              <el-tag size="small" :type="recordStatusTagType(form.record_status)">{{ recordStatusLabel(form.record_status) }}</el-tag>
              <el-button plain @click="closeEditor">返回列表</el-button>
            </div>
          </div>

          <div class="editor-toolbar">
            <el-form-item label="患者ID" prop="patient_id" class="toolbar-item toolbar-item--sm">
              <el-input
                v-model="form.patient_id"
                placeholder="请输入患者ID"
                @blur="handlePatientIdentityChange"
              />
            </el-form-item>
            <el-form-item label="患者姓名" prop="patient_name" class="toolbar-item toolbar-item--sm">
              <el-input
                v-model="form.patient_name"
                placeholder="请输入患者姓名"
                @blur="handlePatientIdentityChange"
              />
            </el-form-item>
            <el-form-item label="接诊医生" prop="doctor_account_id" class="toolbar-item toolbar-item--sm">
              <el-select v-model="form.doctor_account_id" filterable placeholder="请选择接诊医生" style="width:100%">
                <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="护士" class="toolbar-item toolbar-item--xs">
              <el-input v-model="form.nurse_name" placeholder="可选" />
            </el-form-item>
            <el-form-item label="助理" class="toolbar-item toolbar-item--xs">
              <el-input v-model="form.assistant_name" placeholder="可选" />
            </el-form-item>
            <el-form-item label="就诊时间" prop="visit_date" class="toolbar-item toolbar-item--md">
              <el-date-picker
                v-model="form.visit_date"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择就诊时间"
                style="width:100%"
              />
            </el-form-item>
            <el-form-item label="病历类型" class="toolbar-item toolbar-item--type">
              <el-radio-group v-model="form.record_type" size="small">
                <el-radio-button label="初诊" />
                <el-radio-button label="复诊" />
              </el-radio-group>
            </el-form-item>
            <div class="toolbar-switch">
              <span>牙位同步</span>
              <el-switch v-model="editorFlags.autoSyncToothPositions" />
            </div>
          </div>
        </el-card>

        <div class="editor-layout">
          <el-card class="template-card" shadow="never">
            <div class="panel-head">
              <div>
                <div class="panel-title">病历模板库</div>
                <div class="panel-tip">按分类选择模板，点击后立即回填到右侧录入区。</div>
              </div>
              <el-tag size="small" effect="plain">{{ medicalRecordTemplateOptions.length }} 个模板</el-tag>
            </div>

            <el-input
              v-model="templateKeyword"
              clearable
              placeholder="搜索模板名称 / 诊断"
              prefix-icon="el-icon-search"
              class="template-search"
            />

            <div class="template-category-bar">
              <span>当前分类</span>
              <el-select v-model="activeTemplateCategory" placeholder="选择分类" size="small" style="width:150px">
                <el-option v-for="category in templateCategories" :key="category" :label="category" :value="category" />
              </el-select>
            </div>

            <div class="template-tree-wrap">
              <el-tree
                :data="templateTreeData"
                node-key="nodeKey"
                default-expand-all
                :expand-on-click-node="false"
                @node-click="handleTemplateNodeClick"
              >
                <span slot-scope="{ data }" class="template-tree-node">
                  <span class="template-tree-node__label">
                    <i :class="data.isTemplate ? 'el-icon-document' : 'el-icon-folder-opened'" />
                    <span>{{ data.label }}</span>
                  </span>
                  <span v-if="!data.isTemplate" class="template-tree-node__count">{{ data.children.length }}</span>
                </span>
              </el-tree>
              <el-empty v-if="!templateTreeData.length" description="暂无匹配模板"></el-empty>
            </div>

            <div class="template-actions">
              <el-button type="success" plain @click="saveCurrentAsTemplate">保存为模板</el-button>
              <el-button plain :disabled="!selectedTemplateId" @click="deleteSelectedTemplate">删除模板</el-button>
            </div>

            <div class="template-preview-card">
              <div class="template-preview-card__title">模板预览</div>
              <template v-if="selectedTemplatePreview">
                <div class="template-preview-card__name">{{ selectedTemplatePreview.template_name }}</div>
                <div class="template-preview-card__meta">
                  {{ selectedTemplatePreview.template_category || '常用模板' }} · {{ selectedTemplatePreview.record_type || '初诊' }}
                </div>
                <div v-for="section in templatePreviewSections" :key="section.key" class="template-preview-row">
                  <span>{{ section.label }}</span>
                  <strong>{{ section.value }}</strong>
                </div>
              </template>
              <div v-else class="template-preview-card__empty">选中模板后，这里会展示模板摘要。</div>
            </div>
          </el-card>

          <div class="editor-main">
            <el-card class="record-sheet-card" shadow="never">
              <div class="panel-head panel-head--sheet">
                <div>
                  <div class="panel-title">详细病历数据</div>
                  <div class="panel-tip">保留你们后台系统的表单风格，但把录入结构改成病历工作台。</div>
                </div>
                <div class="sheet-head-tags">
                  <el-tag size="small" effect="plain">{{ form.record_type || '初诊' }}</el-tag>
                  <el-tag size="small" :type="recordStatusTagType(form.record_status)">{{ recordStatusLabel(form.record_status) }}</el-tag>
                </div>
              </div>

              <div class="record-sheet">
                <div class="sheet-row">
                  <div class="sheet-label sheet-label--required">主诉</div>
                  <div class="sheet-cell">
                    <el-form-item prop="chief_complaint" class="sheet-form-item">
                      <el-input v-model="form.chief_complaint" type="textarea" :rows="2" placeholder="请输入主诉" />
                    </el-form-item>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">现病史</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.present_illness_history" type="textarea" :rows="2" placeholder="请输入现病史" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">既往史</div>
                  <div class="sheet-cell sheet-cell--split">
                    <div class="sheet-cell__main">
                      <el-input v-model="form.past_history" type="textarea" :rows="2" placeholder="请输入既往史" />
                    </div>
                    <div class="sheet-cell__side">
                      <div class="sheet-side-title">一般情况</div>
                      <el-input v-model="form.general_condition" placeholder="例如：体健" />
                    </div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">流行病史</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.infectious_history" type="textarea" :rows="2" placeholder="可选" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">过敏史</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.allergy_history" type="textarea" :rows="2" placeholder="可选" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">检查</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.examination" type="textarea" :rows="3" placeholder="请输入检查内容" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">辅助检查</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.auxiliary_examination" type="textarea" :rows="2" placeholder="请输入辅助检查内容" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">本次操作</div>
                  <div class="sheet-cell">
                    <div class="operation-panel">
                      <div class="operation-panel__head">
                        <div>
                          <div class="operation-panel__title">结构化操作</div>
                          <div class="operation-panel__tip">本次操作必须从操作字典库选择；保存病历前至少需要添加一条有效操作。</div>
                        </div>
                        <el-button size="mini" plain @click="appendManualOperation" :disabled="!operationOptions.length">新增空白操作</el-button>
                      </div>

                      <el-row :gutter="12" class="project-suggestion-row">
                        <el-col :span="10">
                          <el-select
                            v-model="selectedQuickOperationId"
                            clearable
                            filterable
                            placeholder="从操作字典库选择后立即添加"
                            style="width:100%"
                            @change="handleQuickOperationSelect">
                            <el-option v-for="operation in operationOptions" :key="operation.id" :label="operationOptionLabel(operation)" :value="operation.id" />
                          </el-select>
                        </el-col>
                        <el-col :span="14">
                          <div class="operation-suggestion-empty">
                            操作来源已切换为操作字典库；如需补充收费归属，可在下方为每条操作单独关联项目。
                          </div>
                        </el-col>
                      </el-row>

                      <div v-if="form.operation_items.length" class="operation-list">
                        <div v-for="(item, index) in form.operation_items" :key="item.local_key" class="operation-item">
                          <div class="operation-item__head">
                            <span>操作 {{ index + 1 }}</span>
                            <div class="operation-item__actions">
                              <el-tag v-if="item.need_lab_processing === 1" size="mini" type="danger">待登记加工</el-tag>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="removeOperationItem(index)">删除</el-button>
                            </div>
                          </div>
                          <el-row :gutter="12">
                            <el-col :span="8">
                              <el-form-item label="关联项目" label-width="82px">
                                <el-select v-model="item.project_id" clearable filterable placeholder="可选" style="width:100%" @change="handleOperationProjectChange(item)">
                                  <el-option v-for="project in projectOptions" :key="project.id" :label="project.project_name" :value="project.id" />
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col :span="8">
                              <el-form-item label="操作字典" label-width="82px" required>
                                <el-select v-model="item.operation_id" clearable filterable placeholder="必填：选择操作字典项" style="width:100%" @change="handleOperationChange(item)">
                                  <el-option v-for="operation in operationOptions" :key="operation.id" :label="operationOptionLabel(operation)" :value="operation.id" />
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col :span="8">
                              <el-form-item label="备注" label-width="82px">
                                <el-input v-model="item.remark" placeholder="可选" />
                              </el-form-item>
                            </el-col>
                          </el-row>
                          <el-form-item v-if="item.need_lab_processing === 1" label="加工厂" label-width="82px">
                            <el-select
                              v-model="item.factory_id"
                              clearable
                              filterable
                              style="width:100%"
                              placeholder="可选，后续加工单自动带出"
                              @change="handleOperationFactoryChange(item)"
                            >
                              <el-option v-for="factory in labFactoryOptions" :key="factory.id" :label="factory.name" :value="factory.id" />
                            </el-select>
                          </el-form-item>
                          <el-form-item label="牙位" label-width="82px">
                            <ToothSelector v-model="item.tooth_positions" @input="handleOperationToothChange" />
                          </el-form-item>
                        </div>
                      </div>
                      <div v-else class="operation-empty">未勾选任何操作时，病历仍可按原方式保存。</div>
                    </div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label sheet-label--required">诊断</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断结论" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">治疗方案</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.treatment_plan" type="textarea" :rows="2" placeholder="请输入治疗方案" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">治疗文稿</div>
                  <div class="sheet-cell">
                    <div class="treatment-draft-toolbar">
                      <span class="treatment-draft-hint">勾选操作后自动生成初稿，医生可继续润色。</span>
                      <el-button size="mini" type="text" @click="regenerateTreatmentDraft" :disabled="!form.operation_items.length">重新生成</el-button>
                    </div>
                    <el-input v-model="form.treatment" type="textarea" :rows="3" placeholder="治疗文稿" @input="handleTreatmentInput" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">牙位</div>
                  <div class="sheet-cell">
                    <ToothSelector v-model="form.tooth_positions" />
                    <div class="sheet-field-hint">开启牙位同步时，会优先使用“本次操作”里的牙位集合。</div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">医嘱</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.medical_advice" type="textarea" :rows="2" placeholder="请输入医嘱" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">处方</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.prescription" type="textarea" :rows="2" placeholder="请输入处方内容" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">病历标签</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.record_tags" placeholder="多个标签请用逗号分隔，例如：牙周, 急诊" />
                    <div v-if="recordTagList.length" class="sheet-tag-list">
                      <el-tag v-for="tag in recordTagList" :key="tag" size="mini" effect="plain">{{ tag }}</el-tag>
                    </div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">影像说明</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.image_summary" type="textarea" :rows="2" placeholder="可填写影像摘要或说明" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">添加影像</div>
                  <div class="sheet-cell">
                    <div class="image-upload-bar">
                      <el-select v-model="imageUploadExtra.imageType" size="small" style="width:120px">
                        <el-option label="X光" value="X光" />
                        <el-option label="口扫" value="口扫" />
                        <el-option label="照片" value="照片" />
                        <el-option label="其他" value="其他" />
                      </el-select>
                      <el-date-picker
                        v-model="imageUploadExtra.imageDate"
                        type="date"
                        value-format="yyyy-MM-dd"
                        size="small"
                        placeholder="拍摄日期"
                        style="width:140px"
                      />
                      <span class="sheet-field-hint">最多 10 张，影像归属到当前患者。</span>
                    </div>

                    <div class="image-board">
                      <el-upload
                        class="image-upload-card"
                        action="/patient-images/upload"
                        :data="imageUploadExtra"
                        :show-file-list="false"
                        accept="image/*,.dcm,.pdf"
                        :disabled="imageUploadDisabled"
                        :before-upload="beforeImageUpload"
                        :on-success="handleImageUploadSuccess"
                        :on-error="handleImageUploadError"
                      >
                        <div class="image-upload-card__inner">
                          <i class="el-icon-plus" />
                          <span>{{ imageUploadDisabled ? '请先完善患者信息' : '上传影像' }}</span>
                        </div>
                      </el-upload>

                      <div v-if="patientImagesLoading" class="image-board__loading">
                        <i class="el-icon-loading" />
                      </div>

                      <template v-else>
                        <div v-for="img in visiblePatientImages" :key="img.id" class="image-thumb-card">
                          <div class="image-thumb-card__preview" @click="previewPatientImage(img)">
                            <img v-if="isImageFile(img)" :src="patientImageUrl(img)" :alt="img.image_name" />
                            <div v-else class="image-thumb-card__file">
                              <i class="el-icon-document" />
                              <span>{{ img.image_type || '文件' }}</span>
                            </div>
                          </div>
                          <div class="image-thumb-card__meta">
                            <strong>{{ img.image_name || '未命名文件' }}</strong>
                            <span>{{ formatDate(img.image_date) || '-' }}</span>
                          </div>
                          <div class="image-thumb-card__actions">
                            <el-button size="mini" type="text" @click="previewPatientImage(img)">查看</el-button>
                            <el-button size="mini" type="text" style="color:#ef4444" @click="deletePatientImage(img.id)">删除</el-button>
                          </div>
                        </div>
                      </template>
                    </div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">病历备注</div>
                  <div class="sheet-cell">
                    <el-input v-model="form.notes" type="textarea" :rows="3" placeholder="补充说明、随访提醒或内部备注" />
                  </div>
                </div>
              </div>
            </el-card>

            <div class="editor-footer">
              <el-button @click="closeEditor">退出</el-button>
              <el-button plain @click="handleSubmit('draft')">暂存</el-button>
              <el-button type="success" plain @click="saveCurrentAsTemplate">另存为模板</el-button>
              <el-button type="primary" @click="handleSubmit('final')">保存</el-button>
            </div>
          </div>
        </div>
      </el-form>

      <el-dialog :visible.sync="previewVisible" :title="previewImageItem ? previewImageItem.image_name : '影像预览'" width="68%" top="6vh">
        <div v-if="previewImageItem" class="preview-dialog-body">
          <img v-if="isImageFile(previewImageItem)" :src="patientImageUrl(previewImageItem)" :alt="previewImageItem.image_name" class="preview-dialog-image" />
          <div v-else class="preview-dialog-file">
            <i class="el-icon-document" />
            <p>{{ previewImageItem.image_name }}</p>
            <el-button type="primary" @click="openPatientImage(previewImageItem)">打开文件</el-button>
          </div>
        </div>
      </el-dialog>
    </template>

    <template v-else>
      <el-card class="top-card" shadow="never">
        <div class="page-head">
          <div>
            <div class="page-kicker">病历管理</div>
            <h2>病历列表</h2>
            <p>统一查看病历叙述、结构化操作和待登记加工提示。</p>
          </div>
          <div class="head-stats">
            <div class="mini-stat">
              <div class="mini-num">{{ total }}</div>
              <div class="mini-label">病历总数</div>
            </div>
            <div class="mini-stat accent">
              <div class="mini-num">{{ pendingLabTotal }}</div>
              <div class="mini-label">待登记加工</div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="query-card" shadow="never">
        <div class="query-row">
          <el-input v-model="searchName" placeholder="按患者姓名搜索" class="query-input" clearable @clear="loadAll" />
          <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="loadAll">重置</el-button>
          <el-button type="success" icon="el-icon-plus" @click="openAdd">新增病历</el-button>
        </div>
      </el-card>

      <el-card class="table-card" shadow="never">
        <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor: '#f8fafc', color: '#475569', fontWeight: '600' }">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="患者姓名" width="160">
            <template slot-scope="scope">
              <span>{{ scope.row.patient_name || '-' }}</span>
              <el-tag
                v-if="scope.row._offline"
                size="mini"
                :type="scope.row._offline.failed ? 'danger' : 'warning'"
                effect="plain"
                style="margin-left:6px;"
              >
                {{ scope.row._offline.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="record_type" label="类型" width="90">
            <template slot-scope="scope">
              <el-tag size="mini" effect="plain">{{ scope.row.record_type || '初诊' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="doctor_name" label="接诊医生" width="120" />
          <el-table-column prop="visit_date" label="就诊时间" width="170">
            <template slot-scope="scope">{{ formatDateTime(scope.row.visit_date) }}</template>
          </el-table-column>
          <el-table-column prop="chief_complaint" label="主诉" min-width="150" show-overflow-tooltip />
          <el-table-column prop="diagnosis" label="诊断" min-width="160" show-overflow-tooltip />
          <el-table-column prop="treatment_plan" label="治疗方案" min-width="180" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.treatment_plan || '-' }}</template>
          </el-table-column>
          <el-table-column prop="operation_summary" label="操作汇总" min-width="180" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.operation_summary || '-' }}</template>
          </el-table-column>
          <el-table-column label="待登记加工" width="120" align="center">
            <template slot-scope="scope">
              <el-badge v-if="Number(scope.row.pending_lab_count || 0) > 0" :value="scope.row.pending_lab_count" :max="99" type="danger" />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="保存状态" width="110">
            <template slot-scope="scope">
              <el-tag size="mini" :type="recordStatusTagType(scope.row.record_status)">{{ recordStatusLabel(scope.row.record_status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template slot-scope="scope">
              <el-button
                v-if="Number(scope.row.pending_lab_count || 0) > 0"
                size="mini"
                type="warning"
                plain
                @click="openLabOrderForRecord(scope.row)"
              >
                登记加工
              </el-button>
              <el-button size="mini" type="primary" plain @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-row">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </template>
  </div>
</template>

<script>
import axios from 'axios'
import ToothSelector from '@/components/ToothSelector.vue'
import { getAdminSession } from '@/utils/adminSession'
import { buildMedicalRecordTreatmentDraft, normalizeToothPositions } from '@/utils/medicalRecordOperationDraft'
import { fetchCachedResource, saveMedicalRecord } from '@/utils/offline/apiClient'
import { isLocalEntityId } from '@/utils/offline/queue'

const DEFAULT_TEMPLATE_CATEGORY = '常用模板'

function defaultOperationItem() {
  return {
    local_key: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    project_id: '',
    project_name: '',
    operation_id: '',
    operation_name: '',
    factory_id: '',
    factory_name: '',
    tooth_positions: '',
    remark: '',
    need_lab_processing: 0,
    default_processing_days: 0
  }
}

function splitRecordTags(value) {
  return Array.from(new Set(
    String(value || '')
      .split(/[，,、/\s]+/)
      .map(item => item.trim())
      .filter(Boolean)
  ))
}

export default {
  name: 'MedicalRecordView',
  components: { ToothSelector },
  data() {
    return {
      tableData: [],
      total: 0,
      page: 1,
      size: 10,
      searchName: '',
      editorVisible: false,
      dialogTitle: '新增病历',
      form: {},
      doctors: [],
      projectOptions: [],
      operationOptions: [],
      labFactoryOptions: [],
      medicalRecordTemplateOptions: [],
      projectDetailCache: {},
      selectedProjectId: '',
      selectedQuickOperationId: '',
      selectedTemplateId: null,
      currentUser: getAdminSession() || {},
      lastAutoTreatmentDraft: '',
      treatmentDraftLocked: false,
      templateKeyword: '',
      activeTemplateCategory: DEFAULT_TEMPLATE_CATEGORY,
      patientImages: [],
      patientImagesLoading: false,
      previewVisible: false,
      previewImageItem: null,
      imageUploadExtra: {
        imageType: 'X光',
        imageDate: '',
        patientId: '',
        patientName: ''
      },
      editorFlags: {
        autoSyncToothPositions: true
      },
      rules: {
        patient_id: [{ required: true, message: '请填写患者ID', trigger: 'blur' }],
        patient_name: [{ required: true, message: '请填写患者姓名', trigger: 'blur' }],
        doctor_account_id: [{ required: true, message: '请选择接诊医生', trigger: 'change' }],
        visit_date: [{ required: true, message: '请选择就诊时间', trigger: 'change' }]
      }
    }
  },
  computed: {
    pendingLabTotal() {
      return (this.tableData || []).reduce((sum, item) => sum + Number(item.pending_lab_count || 0), 0)
    },
    selectedProjectOperations() {
      const detail = this.projectDetailCache[String(this.selectedProjectId || '')]
      return detail && Array.isArray(detail.operation_relations) ? detail.operation_relations : []
    },
    templateCategories() {
      const categories = Array.from(new Set(
        (this.medicalRecordTemplateOptions || [])
          .map(item => String(item.template_category || '').trim() || DEFAULT_TEMPLATE_CATEGORY)
      ))
      return categories.length ? categories : [DEFAULT_TEMPLATE_CATEGORY]
    },
    templateTreeData() {
      const keyword = String(this.templateKeyword || '').trim().toLowerCase()
      const grouped = this.templateCategories.map(category => ({
        label: category,
        nodeKey: `category-${category}`,
        isTemplate: false,
        children: []
      }))
      const groupMap = grouped.reduce((result, item) => {
        result[item.label] = item
        return result
      }, {})
      ;(this.medicalRecordTemplateOptions || []).forEach(item => {
        const category = String(item.template_category || '').trim() || DEFAULT_TEMPLATE_CATEGORY
        const matchedText = `${item.template_name || ''} ${item.diagnosis || ''} ${item.chief_complaint || ''}`.toLowerCase()
        if (keyword && !matchedText.includes(keyword) && !category.toLowerCase().includes(keyword)) {
          return
        }
        if (!groupMap[category]) {
          groupMap[category] = {
            label: category,
            nodeKey: `category-${category}`,
            isTemplate: false,
            children: []
          }
        }
        groupMap[category].children.push({
          label: item.template_name || '未命名模板',
          nodeKey: `template-${item.id}`,
          isTemplate: true,
          id: item.id,
          category,
          template: item
        })
      })
      return Object.values(groupMap).filter(item => item.children.length)
    },
    selectedTemplatePreview() {
      return (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedTemplateId || 0)) || null
    },
    templatePreviewSections() {
      const template = this.selectedTemplatePreview
      if (!template) return []
      const rows = [
        { key: 'chief_complaint', label: '主诉', value: template.chief_complaint },
        { key: 'diagnosis', label: '诊断', value: template.diagnosis },
        { key: 'treatment_plan', label: '方案', value: template.treatment_plan },
        { key: 'treatment', label: '治疗', value: template.treatment },
        { key: 'medical_advice', label: '医嘱', value: template.medical_advice }
      ]
      return rows
        .filter(item => String(item.value || '').trim())
        .map(item => Object.assign({}, item, { value: String(item.value || '').trim() }))
        .slice(0, 5)
    },
    currentPatientSummary() {
      const patientId = String(this.form.patient_id || '').trim()
      const patientName = String(this.form.patient_name || '').trim()
      const visitDate = this.formatDateTime(this.form.visit_date)
      if (!patientId && !patientName) {
        return '填写患者基本信息后即可在右侧完成完整病历录入。'
      }
      return [`患者ID ${patientId || '-'}`, patientName || '未填写姓名', visitDate || '未设置就诊时间'].join(' · ')
    },
    recordTagList() {
      return splitRecordTags(this.form.record_tags)
    },
    imageUploadDisabled() {
      return !String(this.form.patient_id || '').trim() || !String(this.form.patient_name || '').trim() || this.patientImages.length >= 10
    },
    visiblePatientImages() {
      return (this.patientImages || []).slice(0, 10)
    }
  },
  created() {
    this.loadDoctors()
    this.loadProjectOptions()
    this.loadOperationOptions()
    this.loadLabFactoryOptions()
    this.loadMedicalRecordTemplateOptions()
    this.loadAll()
    this.form = this.buildEmptyForm()
    this.imageUploadExtra.imageDate = this.currentDateValue()
  },
  methods: {
    readCurrentUser() {
      return getAdminSession() || {}
    },
    loadAll() {
      this.page = this.page || 1
      const params = { page: this.page, size: this.size }
      fetchCachedResource({
        cacheKey: 'page:medical-records:list',
        scope: 'medicalRecordsList',
        url: '/medical-records/selectAll',
        params,
        loader: () => axios.get('/medical-records/selectAll', { params }),
        notifier: message => this.$message.warning(message)
      }).then(result => {
        const data = result && result.data ? result.data : {}
        this.tableData = Array.isArray(data.list) ? data.list : []
        this.total = Number(data.total || 0)
      }).catch(error => {
        console.error('Error fetching medical records:', error)
        this.tableData = []
        this.total = 0
      })
    },
    search() {
      if (!this.searchName) {
        this.loadAll()
        return
      }
      this.page = 1
      const params = { name: this.searchName, page: this.page, size: this.size }
      fetchCachedResource({
        cacheKey: 'page:medical-records:list',
        scope: 'medicalRecordsList',
        url: '/medical-records/selectByPatientName',
        params,
        loader: () => axios.get('/medical-records/selectByPatientName', { params }),
        notifier: message => this.$message.warning(message)
      }).then(result => {
        const data = result && result.data ? result.data : {}
        this.tableData = Array.isArray(data.list) ? data.list : []
        this.total = Number(data.total || 0)
      }).catch(error => {
        console.error('Error searching medical records:', error)
      })
    },
    handlePageChange(p) {
      this.page = p
      this.searchName ? this.search() : this.loadAll()
    },
    normalizeDoctor(item) {
      if (!item || !item.id || !item.name) return null
      return { id: Number(item.id), name: String(item.name).trim() }
    },
    currentDoctorById(id) {
      return (this.doctors || []).find(item => Number(item.id) === Number(id)) || null
    },
    resolveDefaultDoctorAccountId() {
      const currentUserId = Number(this.currentUser.id)
      if (Number.isFinite(currentUserId) && currentUserId > 0 && this.currentDoctorById(currentUserId)) {
        return currentUserId
      }
      const doctorName = this.resolveDefaultDoctorName()
      const matchedDoctor = (this.doctors || []).find(item => item.name === doctorName)
      return matchedDoctor ? matchedDoctor.id : null
    },
    resolveDefaultDoctorName(doctorAccountId = null) {
      const doctor = this.currentDoctorById(doctorAccountId)
      if (doctor && doctor.name) {
        return doctor.name
      }
      const loginDoctorName = this.currentUser.doctor_name || this.currentUser.name || ''
      return String(loginDoctorName || '').trim()
    },
    buildEmptyForm() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      return {
        patient_id: '',
        patient_name: '',
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        nurse_name: '',
        assistant_name: '',
        visit_date: this.currentDateTimeValue(),
        record_type: '初诊',
        chief_complaint: '',
        present_illness_history: '',
        past_history: '',
        infectious_history: '',
        allergy_history: '',
        general_condition: '体健',
        examination: '',
        auxiliary_examination: '',
        diagnosis: '',
        treatment_plan: '',
        treatment: '',
        tooth_positions: '',
        medical_advice: '',
        prescription: '',
        record_tags: '',
        image_summary: '',
        notes: '',
        record_status: 'final',
        operation_items: []
      }
    },
    async loadMedicalRecordTemplateOptions() {
      try {
        const result = await fetchCachedResource({
          cacheKey: 'ref:medical-record-templates',
          scope: '',
          url: '/medical-record-templates/selectEnabled',
          loader: () => axios.get('/medical-record-templates/selectEnabled')
        })
        this.medicalRecordTemplateOptions = Array.isArray(result && result.data) ? result.data : []
        if (!this.templateCategories.includes(this.activeTemplateCategory)) {
          this.activeTemplateCategory = this.templateCategories[0]
        }
      } catch (error) {
        this.medicalRecordTemplateOptions = []
      }
    },
    applySelectedTemplate() {
      const template = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedTemplateId || 0))
      if (!template) return
      this.form = Object.assign({}, this.form, {
        record_type: template.record_type || this.form.record_type || '初诊',
        chief_complaint: template.chief_complaint || '',
        present_illness_history: template.present_illness_history || '',
        past_history: template.past_history || '',
        infectious_history: template.infectious_history || '',
        allergy_history: template.allergy_history || '',
        general_condition: template.general_condition || '',
        examination: template.examination || '',
        auxiliary_examination: template.auxiliary_examination || '',
        diagnosis: template.diagnosis || '',
        treatment_plan: template.treatment_plan || '',
        treatment: template.treatment || '',
        tooth_positions: template.tooth_positions || '',
        medical_advice: template.medical_advice || '',
        prescription: template.prescription || '',
        record_tags: template.record_tags || '',
        image_summary: template.image_summary || '',
        notes: template.notes || '',
        operation_items: this.normalizeLoadedOperationItems(template.operation_items || [])
      })
      this.activeTemplateCategory = String(template.template_category || '').trim() || DEFAULT_TEMPLATE_CATEGORY
      this.selectedQuickOperationId = ''
      this.selectedProjectId = this.form.operation_items.length ? (this.form.operation_items[0].project_id || '') : ''
      this.lastAutoTreatmentDraft = buildMedicalRecordTreatmentDraft(this.form.operation_items || [])
      this.treatmentDraftLocked = String(this.form.treatment || '').trim() && String(this.form.treatment || '').trim() !== this.lastAutoTreatmentDraft
    },
    buildTemplatePayload(templateName) {
      return {
        template_name: String(templateName || '').trim(),
        template_category: this.activeTemplateCategory || DEFAULT_TEMPLATE_CATEGORY,
        chief_complaint: this.form.chief_complaint || '',
        present_illness_history: this.form.present_illness_history || '',
        past_history: this.form.past_history || '',
        infectious_history: this.form.infectious_history || '',
        allergy_history: this.form.allergy_history || '',
        general_condition: this.form.general_condition || '',
        examination: this.form.examination || '',
        auxiliary_examination: this.form.auxiliary_examination || '',
        diagnosis: this.form.diagnosis || '',
        treatment_plan: this.form.treatment_plan || '',
        treatment: this.form.treatment || '',
        tooth_positions: this.resolveRecordToothPositions(),
        medical_advice: this.form.medical_advice || '',
        prescription: this.form.prescription || '',
        record_tags: this.normalizeRecordTags(this.form.record_tags),
        image_summary: this.form.image_summary || '',
        notes: this.form.notes || '',
        record_type: this.form.record_type || '初诊',
        operation_items: this.normalizeOperationItems(this.form.operation_items),
        created_by: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
        created_by_name: this.currentUser && this.currentUser.name ? this.currentUser.name : ''
      }
    },
    saveCurrentAsTemplate() {
      const currentTemplate = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedTemplateId || 0))
      const defaultName = currentTemplate && currentTemplate.template_name
        ? currentTemplate.template_name
        : (this.form.diagnosis || this.form.chief_complaint || '病历模板')
      this.$prompt(`请输入模板名称，将保存到分类“${this.activeTemplateCategory || DEFAULT_TEMPLATE_CATEGORY}”`, '保存病历模板', {
        confirmButtonText: '保存',
        cancelButtonText: '取消',
        inputValue: defaultName
      }).then(async ({ value }) => {
        const res = await axios.post('/medical-record-templates/add', this.buildTemplatePayload(value))
        if (String((res.data || {}).code || '') !== '200') {
          this.$message.error((res.data || {}).msg || '模板保存失败')
          return
        }
        this.$message.success('病历模板已保存')
        await this.loadMedicalRecordTemplateOptions()
        this.selectedTemplateId = res.data && res.data.data && res.data.data.id ? res.data.data.id : this.selectedTemplateId
      }).catch(() => {})
    },
    deleteSelectedTemplate() {
      const template = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedTemplateId || 0))
      if (!template || !template.id) return
      this.$confirm(`确认删除模板“${template.template_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/medical-record-templates/delete/${template.id}`)
        if (String((res.data || {}).code || '') !== '200') {
          this.$message.error((res.data || {}).msg || '删除失败')
          return
        }
        this.$message.success('模板已删除')
        this.selectedTemplateId = null
        await this.loadMedicalRecordTemplateOptions()
      }).catch(() => {})
    },
    handleTemplateNodeClick(node) {
      if (!node) return
      if (node.isTemplate) {
        this.selectedTemplateId = node.id
        this.activeTemplateCategory = node.category || DEFAULT_TEMPLATE_CATEGORY
        this.applySelectedTemplate()
        return
      }
      this.activeTemplateCategory = node.label || DEFAULT_TEMPLATE_CATEGORY
    },
    syncFormDefaultDoctor() {
      if (!this.editorVisible || this.dialogTitle !== '新增病历' || !this.form || this.form.id || this.form.doctor_account_id) {
        return
      }
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      if (!doctorAccountId) return
      this.$set(this.form, 'doctor_account_id', doctorAccountId)
      this.$set(this.form, 'doctor_name', this.resolveDefaultDoctorName(doctorAccountId))
    },
    async loadProjectOptions() {
      try {
        const result = await fetchCachedResource({
          cacheKey: 'ref:treatment-projects-enabled',
          scope: '',
          url: '/treatment-projects/selectEnabled',
          loader: () => axios.get('/treatment-projects/selectEnabled')
        })
        this.projectOptions = Array.isArray(result && result.data) ? result.data : []
      } catch (error) {
        this.projectOptions = []
      }
    },
    async loadOperationOptions() {
      try {
        const result = await fetchCachedResource({
          cacheKey: 'ref:treatment-operations-enabled',
          scope: '',
          url: '/treatment-operations/selectEnabled',
          loader: () => axios.get('/treatment-operations/selectEnabled')
        })
        this.operationOptions = Array.isArray(result && result.data) ? result.data : []
      } catch (error) {
        this.operationOptions = []
      }
    },
    async loadLabFactoryOptions() {
      try {
        const result = await fetchCachedResource({
          cacheKey: 'ref:lab-factories-enabled',
          scope: '',
          url: '/lab-factories/selectEnabled',
          loader: () => axios.get('/lab-factories/selectEnabled')
        })
        this.labFactoryOptions = Array.isArray(result && result.data) ? result.data : []
      } catch (error) {
        this.labFactoryOptions = []
      }
    },
    projectOptionLabel(item) {
      const price = Number(item && item.default_price || 0)
      return `${item.project_name || '未命名项目'}（¥${price.toFixed(2)}）`
    },
    operationOptionLabel(item) {
      const category = item && item.operation_category ? ` / ${item.operation_category}` : ''
      return `${item.operation_name || '未命名操作'}${category}`
    },
    async loadProjectDetail(projectId) {
      const key = String(projectId || '')
      if (!key) return null
      if (this.projectDetailCache[key]) {
        return this.projectDetailCache[key]
      }
      const res = await axios.get('/treatment-projects/selectById', { params: { id: projectId } })
      const detail = res.data && res.data.code === '200' ? (res.data.data || null) : null
      if (detail) {
        this.$set(this.projectDetailCache, key, detail)
      }
      return detail
    },
    async handleProjectSuggestionChange(projectId) {
      this.selectedProjectId = projectId || ''
      if (projectId) {
        await this.loadProjectDetail(projectId)
      }
    },
    isSuggestedOperationSelected(relation) {
      return (this.form.operation_items || []).some(item =>
        String(item.project_id || '') === String(this.selectedProjectId || '')
        && String(item.operation_id || '') === String(relation.operation_id || '')
      )
    },
    toggleSuggestedOperation(relation) {
      if (!relation) return
      const currentIndex = (this.form.operation_items || []).findIndex(item =>
        String(item.project_id || '') === String(this.selectedProjectId || '')
        && String(item.operation_id || '') === String(relation.operation_id || '')
      )
      if (currentIndex >= 0) {
        this.removeOperationItem(currentIndex)
        return
      }
      const project = (this.projectOptions || []).find(item => String(item.id) === String(this.selectedProjectId || ''))
      const nextItem = defaultOperationItem()
      nextItem.project_id = project ? project.id : ''
      nextItem.project_name = project ? project.project_name : ''
      nextItem.operation_id = relation.operation_id
      nextItem.operation_name = relation.operation_name
      nextItem.need_lab_processing = relation.need_lab_processing === 1 ? 1 : 0
      nextItem.default_processing_days = Number(relation.default_processing_days || 0)
      if (!Array.isArray(this.form.operation_items)) {
        this.$set(this.form, 'operation_items', [])
      }
      this.form.operation_items.push(nextItem)
      this.refreshTreatmentDraft()
    },
    handleQuickOperationSelect(operationId) {
      if (!operationId) return
      this.appendOperationById(operationId)
      this.selectedQuickOperationId = ''
    },
    appendOperationById(operationId) {
      const operation = (this.operationOptions || []).find(item => String(item.id) === String(operationId || ''))
      if (!operation) return
      if (!Array.isArray(this.form.operation_items)) {
        this.$set(this.form, 'operation_items', [])
      }
      const nextItem = defaultOperationItem()
      nextItem.operation_id = operation.id
      nextItem.operation_name = operation.operation_name || ''
      nextItem.need_lab_processing = operation.need_lab_processing === 1 ? 1 : 0
      nextItem.default_processing_days = Number(operation.default_processing_days || 0)
      this.form.operation_items.push(nextItem)
      this.refreshTreatmentDraft()
    },
    appendManualOperation() {
      if (!Array.isArray(this.form.operation_items)) {
        this.$set(this.form, 'operation_items', [])
      }
      this.form.operation_items.push(defaultOperationItem())
    },
    removeOperationItem(index) {
      const list = this.form.operation_items || []
      list.splice(index, 1)
      this.refreshTreatmentDraft()
    },
    handleOperationProjectChange(item) {
      const project = (this.projectOptions || []).find(projectItem => String(projectItem.id) === String(item.project_id || ''))
      item.project_name = project ? project.project_name : ''
      this.refreshTreatmentDraft(false)
    },
    handleOperationFactoryChange(item) {
      const factory = (this.labFactoryOptions || []).find(factoryItem => String(factoryItem.id) === String(item.factory_id || ''))
      item.factory_name = factory ? factory.name : ''
    },
    handleOperationChange(item) {
      const operation = (this.operationOptions || []).find(operationItem => String(operationItem.id) === String(item.operation_id || ''))
      item.operation_name = operation ? operation.operation_name : ''
      item.need_lab_processing = operation && operation.need_lab_processing === 1 ? 1 : 0
      item.default_processing_days = operation ? Number(operation.default_processing_days || 0) : 0
      if (item.need_lab_processing !== 1) {
        item.factory_id = ''
        item.factory_name = ''
      }
      this.refreshTreatmentDraft()
    },
    handleOperationToothChange() {
      this.refreshTreatmentDraft()
    },
    handleTreatmentInput() {
      const currentText = String(this.form.treatment || '').trim()
      this.treatmentDraftLocked = currentText && currentText !== this.lastAutoTreatmentDraft
    },
    refreshTreatmentDraft(force = false) {
      const nextDraft = buildMedicalRecordTreatmentDraft(this.form.operation_items || [])
      const currentText = String(this.form.treatment || '').trim()
      const canReplace = force || !currentText || currentText === this.lastAutoTreatmentDraft || !this.treatmentDraftLocked
      this.lastAutoTreatmentDraft = nextDraft
      if (canReplace) {
        this.form.treatment = nextDraft
        this.treatmentDraftLocked = false
      }
    },
    regenerateTreatmentDraft() {
      this.treatmentDraftLocked = false
      this.refreshTreatmentDraft(true)
    },
    normalizeOperationItems(items, includeRuntimeFields = false) {
      return (Array.isArray(items) ? items : []).map(item => {
        const operation = (this.operationOptions || []).find(operationItem => String(operationItem.id) === String(item.operation_id || ''))
        const project = (this.projectOptions || []).find(projectItem => String(projectItem.id) === String(item.project_id || ''))
        const factory = (this.labFactoryOptions || []).find(factoryItem => String(factoryItem.id) === String(item.factory_id || ''))
        const needLabProcessing = operation && operation.need_lab_processing === 1 ? 1 : (item.need_lab_processing === 1 ? 1 : 0)
        const normalized = {
          project_id: item.project_id || null,
          project_name: item.project_name || (project && project.project_name) || '',
          operation_id: item.operation_id || null,
          operation_name: item.operation_name || (operation && operation.operation_name) || '',
          factory_id: needLabProcessing === 1 ? (item.factory_id || null) : null,
          factory_name: needLabProcessing === 1 ? (item.factory_name || (factory && factory.name) || '') : '',
          tooth_positions: item.tooth_positions || '',
          remark: item.remark || '',
          need_lab_processing: needLabProcessing,
          default_processing_days: operation ? Number(operation.default_processing_days || 0) : Number(item.default_processing_days || 0)
        }
        if (includeRuntimeFields) {
          normalized.id = item.id || null
        }
        return normalized
      }).filter(item => item.operation_id && item.operation_name)
    },
    resolveRecordToothPositions() {
      if (!this.editorFlags.autoSyncToothPositions) {
        return String(this.form.tooth_positions || '').trim()
      }
      const operationToothValues = this.normalizeOperationItems(this.form.operation_items)
        .flatMap(item => normalizeToothPositions(item.tooth_positions))
      if (operationToothValues.length) {
        return Array.from(new Set(operationToothValues)).join(',')
      }
      return String(this.form.tooth_positions || '').trim()
    },
    openAdd() {
      this.currentUser = this.readCurrentUser()
      this.form = this.buildEmptyForm()
      this.dialogTitle = '新增病历'
      this.selectedProjectId = ''
      this.selectedQuickOperationId = ''
      this.selectedTemplateId = null
      this.lastAutoTreatmentDraft = ''
      this.treatmentDraftLocked = false
      this.editorFlags.autoSyncToothPositions = true
      this.editorVisible = true
      this.patientImages = []
      this.syncImageUploadExtra()
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    async openEdit(row) {
      const detail = isLocalEntityId(row.id)
        ? Object.assign({}, row)
        : await axios.get('/medical-records/selectById', { params: { id: row.id } }).then(res => (res.data && res.data.code === '200' ? (res.data.data || {}) : {}))
      const matchedDoctor = this.currentDoctorById(detail.doctor_account_id) || (this.doctors || []).find(item => item.name === String(detail.doctor_name || '').trim())
      this.form = Object.assign(this.buildEmptyForm(), detail, {
        visit_date: this.formatDateTimeValue(detail.visit_date) || this.currentDateTimeValue(),
        doctor_account_id: matchedDoctor ? matchedDoctor.id : (detail.doctor_account_id ? Number(detail.doctor_account_id) : null),
        doctor_name: matchedDoctor ? matchedDoctor.name : String(detail.doctor_name || '').trim(),
        record_type: detail.record_type || '初诊',
        tooth_positions: detail.tooth_positions || '',
        record_tags: detail.record_tags || '',
        image_summary: detail.image_summary || '',
        medical_advice: detail.medical_advice || '',
        treatment_plan: detail.treatment_plan || '',
        record_status: detail.record_status || 'final',
        operation_items: this.normalizeLoadedOperationItems(detail.operation_items || [])
      })
      this.dialogTitle = '编辑病历'
      this.selectedTemplateId = null
      this.selectedProjectId = this.form.operation_items.length ? (this.form.operation_items[0].project_id || '') : ''
      this.selectedQuickOperationId = ''
      this.lastAutoTreatmentDraft = buildMedicalRecordTreatmentDraft(this.form.operation_items || [])
      this.treatmentDraftLocked = String(this.form.treatment || '').trim() && String(this.form.treatment || '').trim() !== this.lastAutoTreatmentDraft
      this.editorFlags.autoSyncToothPositions = !!(this.form.operation_items || []).length
      this.editorVisible = true
      if (this.selectedProjectId) {
        await this.loadProjectDetail(this.selectedProjectId)
      }
      this.syncImageUploadExtra()
      await this.loadPatientImages()
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    closeEditor() {
      this.editorVisible = false
      this.previewVisible = false
      this.previewImageItem = null
      this.patientImages = []
      this.selectedTemplateId = null
      this.templateKeyword = ''
      this.form = this.buildEmptyForm()
    },
    async openLabOrderForRecord(row) {
      const res = await axios.get('/medical-records/selectById', { params: { id: row.id } })
      const detail = res.data && res.data.code === '200' ? (res.data.data || {}) : {}
      const pendingItems = (detail.operation_items || []).filter(item => Number(item.need_lab_processing || 0) === 1 && Number(item.lab_order_status || 0) === 0)
      if (!pendingItems.length) {
        this.$message.warning('该病历当前没有待登记加工的操作')
        return
      }
      const query = {
        pendingLab: '1',
        openCreate: '1',
        patientId: detail.patient_id || row.patient_id,
        patientName: detail.patient_name || row.patient_name,
        medicalRecordId: detail.id || row.id
      }
      if (pendingItems.length === 1) {
        query.medicalRecordOperationId = pendingItems[0].id
      }
      this.$router.push({ path: '/lab-orders', query }).catch(() => {})
    },
    normalizeLoadedOperationItems(items) {
      return (Array.isArray(items) ? items : []).map(item => ({
        local_key: `${item.id || 'op'}-${Math.random().toString(36).slice(2, 8)}`,
        id: item.id || null,
        project_id: item.project_id || '',
        project_name: item.project_name || '',
        operation_id: item.operation_id || '',
        operation_name: item.operation_name || '',
        factory_id: item.factory_id || '',
        factory_name: item.factory_name || '',
        tooth_positions: item.tooth_positions || '',
        remark: item.remark || '',
        need_lab_processing: item.need_lab_processing === 1 ? 1 : 0,
        default_processing_days: Number(item.default_processing_days || 0)
      }))
    },
    loadDoctors() {
      fetchCachedResource({
        cacheKey: 'ref:doctors-active',
        scope: '',
        url: '/accounts/doctors/active',
        loader: () => axios.get('/accounts/doctors/active')
      }).then(res => {
        this.doctors = (Array.isArray(res && res.data) ? res.data : []).map(this.normalizeDoctor).filter(Boolean)
        this.syncFormDefaultDoctor()
      }).catch(() => {
        this.doctors = []
      })
    },
    validateToothSelection() {
      if (this.form.treatment && String(this.form.treatment).trim() && !String(this.resolveRecordToothPositions() || '').trim()) {
        return '请选择牙位'
      }
      return ''
    },
    normalizeRecordTags(value) {
      return splitRecordTags(value).join(',')
    },
    buildSubmitPayload(status = 'final') {
      const operationItems = this.normalizeOperationItems(this.form.operation_items, true)
      const doctor = this.currentDoctorById(this.form.doctor_account_id)
      return Object.assign({}, this.form, {
        patient_id: Number(this.form.patient_id || 0),
        patient_name: String(this.form.patient_name || '').trim(),
        doctor_account_id: this.form.doctor_account_id || null,
        doctor_name: doctor && doctor.name ? doctor.name : '',
        tooth_positions: this.resolveRecordToothPositions(),
        record_tags: this.normalizeRecordTags(this.form.record_tags),
        record_status: status,
        operation_items: operationItems
      })
    },
    handleSubmit(status = 'final') {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        const error = this.validateToothSelection()
        if (error) {
          this.$message.warning(error)
          return
        }
        if (!this.form.doctor_account_id) {
          this.form.doctor_account_id = this.resolveDefaultDoctorAccountId()
        }
        if (!this.form.doctor_account_id) {
          this.$message.warning('请选择接诊医生')
          return
        }
        const rawOperationItems = Array.isArray(this.form.operation_items) ? this.form.operation_items : []
        if (!rawOperationItems.length) {
          this.$message.warning('本次操作为必填项，请至少选择一个操作字典项')
          return
        }
        if (rawOperationItems.some(item => !String(item && item.operation_id || '').trim())) {
          this.$message.warning('本次操作为必填项，请为每条操作选择操作字典项')
          return
        }
        const payload = this.buildSubmitPayload(status)
        saveMedicalRecord(payload, {
          isEdit: !!payload.id,
          notifier: message => this.$message.success(message)
        }).then(result => {
          if (!result.offline) {
            this.$message.success(status === 'draft' ? '病历已暂存' : '保存成功')
          }
          this.editorVisible = false
          this.loadAll()
        }).catch(error => {
          if (error && error.message) {
            this.$message.error(error.message)
          } else {
            this.$message.error('保存失败')
          }
        })
      })
    },
    handleDelete(id) {
      this.$confirm('确认删除该病历？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/medical-records/delete/${id}`).then(res => {
          if (res.data.code === '200') {
            this.$message.success('删除成功')
            this.loadAll()
          }
        })
      })
    },
    handlePatientIdentityChange() {
      this.syncImageUploadExtra()
      this.loadPatientImages()
    },
    syncImageUploadExtra() {
      this.imageUploadExtra.patientId = String(this.form.patient_id || '').trim()
      this.imageUploadExtra.patientName = String(this.form.patient_name || '').trim()
      if (!this.imageUploadExtra.imageDate) {
        this.imageUploadExtra.imageDate = this.currentDateValue()
      }
    },
    async loadPatientImages() {
      const patientId = Number(this.form.patient_id || 0)
      if (!Number.isFinite(patientId) || patientId <= 0) {
        this.patientImages = []
        return
      }
      this.patientImagesLoading = true
      try {
        const res = await axios.get('/patient-images/selectByPatientId', { params: { patientId } })
        this.patientImages = Array.isArray(res.data && res.data.data) ? res.data.data : []
      } catch (error) {
        this.patientImages = []
      } finally {
        this.patientImagesLoading = false
      }
    },
    beforeImageUpload() {
      this.syncImageUploadExtra()
      if (!this.imageUploadExtra.patientId || !this.imageUploadExtra.patientName) {
        this.$message.warning('请先填写患者ID和患者姓名，再上传影像')
        return false
      }
      if (this.patientImages.length >= 10) {
        this.$message.warning('最多上传 10 张影像')
        return false
      }
      return true
    },
    handleImageUploadSuccess(response) {
      if (String((response || {}).code || '') !== '200') {
        this.$message.error((response || {}).msg || '上传失败')
        return
      }
      this.$message.success('影像上传成功')
      this.loadPatientImages()
    },
    handleImageUploadError() {
      this.$message.error('影像上传失败')
    },
    previewPatientImage(img) {
      if (!img) return
      if (this.isImageFile(img)) {
        this.previewImageItem = img
        this.previewVisible = true
        return
      }
      this.openPatientImage(img)
    },
    openPatientImage(img) {
      if (!img || !img.id) return
      window.open(this.patientImageUrl(img), '_blank')
    },
    deletePatientImage(id) {
      this.$confirm('确认删除该影像？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/patient-images/delete/${id}`).then(() => {
          this.$message.success('影像已删除')
          this.loadPatientImages()
        })
      }).catch(() => {})
    },
    patientImageUrl(img) {
      return `/patient-images/file/${img.id}`
    },
    isImageFile(img) {
      const name = String((img && img.image_name) || '').toLowerCase()
      return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(name)
    },
    recordStatusTagType(status) {
      return String(status || 'final') === 'draft' ? 'warning' : 'success'
    },
    recordStatusLabel(status) {
      return String(status || 'final') === 'draft' ? '暂存' : '已保存'
    },
    formatDate(d) {
      if (!d) return ''
      const text = String(d)
      const matched = text.match(/^(\d{4}-\d{2}-\d{2})/)
      if (matched) return matched[1]
      const date = new Date(d)
      if (isNaN(date.getTime())) return ''
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    formatDateTime(d) {
      if (!d) return ''
      const text = String(d).replace('T', ' ')
      const matched = text.match(/^(\d{4}-\d{2}-\d{2})(?:\s+(\d{2}:\d{2})(?::\d{2})?)?/)
      if (matched) {
        return matched[2] ? `${matched[1]} ${matched[2]}` : matched[1]
      }
      const date = new Date(d)
      if (isNaN(date.getTime())) return ''
      const pad = value => String(value).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    formatDateTimeValue(d) {
      if (!d) return ''
      const date = new Date(d)
      if (isNaN(date.getTime())) {
        const text = String(d).replace('T', ' ')
        const matched = text.match(/^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})(?::\d{2})?/)
        return matched ? `${matched[1]}:00` : ''
      }
      const pad = value => String(value).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    currentDateValue() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    currentDateTimeValue() {
      const now = new Date()
      const pad = value => String(value).padStart(2, '0')
      return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
    }
  }
}
</script>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.top-card,
.query-card,
.table-card,
.editor-head-card,
.template-card,
.record-sheet-card {
  border-radius: 18px;
}

.page-head,
.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.page-kicker {
  color: #64748b;
  font-size: 13px;
}

.page-head h2,
.editor-head h2 {
  margin: 6px 0 8px;
  color: #0f172a;
  font-size: 24px;
}

.page-head p,
.editor-head p {
  margin: 0;
  color: #94a3b8;
}

.head-stats {
  display: flex;
  gap: 12px;
}

.mini-stat {
  min-width: 120px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.mini-stat.accent {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.mini-num {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
}

.mini-label {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.query-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.query-input {
  width: 280px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

.editor-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.editor-head__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 14px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.toolbar-item {
  margin-bottom: 0;
}

.toolbar-item--xs {
  width: 160px;
}

.toolbar-item--sm {
  width: 210px;
}

.toolbar-item--md {
  width: 240px;
}

.toolbar-item--type {
  min-width: 180px;
}

.toolbar-switch {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding-top: 6px;
  color: #475569;
  font-size: 13px;
}

.editor-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.panel-head--sheet {
  margin-bottom: 18px;
}

.panel-title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.panel-tip {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.template-search {
  margin-top: 16px;
}

.template-category-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  color: #475569;
  font-size: 13px;
}

.template-tree-wrap {
  margin-top: 14px;
  min-height: 280px;
  max-height: 420px;
  overflow: auto;
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #f8fafc;
}

.template-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 12px;
}

.template-tree-node__label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #334155;
}

.template-tree-node__count {
  color: #94a3b8;
  font-size: 12px;
}

.template-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.template-preview-card {
  margin-top: 16px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid #dbeafe;
  background: #f8fbff;
}

.template-preview-card__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.template-preview-card__name {
  margin-top: 10px;
  color: #0f172a;
  font-weight: 700;
}

.template-preview-card__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.template-preview-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #d6e7ff;
}

.template-preview-row span {
  color: #64748b;
  font-size: 12px;
}

.template-preview-row strong {
  color: #1e293b;
  font-weight: 500;
  line-height: 1.7;
}

.template-preview-card__empty {
  margin-top: 10px;
  color: #94a3b8;
  font-size: 12px;
}

.sheet-head-tags {
  display: flex;
  gap: 8px;
}

.record-sheet {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  overflow: hidden;
  background: #fff;
}

.sheet-row {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  border-bottom: 1px solid #e2e8f0;
}

.sheet-row:last-child {
  border-bottom: none;
}

.sheet-label {
  display: flex;
  align-items: center;
  padding: 18px 16px;
  background: #f8fafc;
  color: #334155;
  font-weight: 600;
  border-right: 1px solid #e2e8f0;
}

.sheet-label--required::before {
  content: '*';
  margin-right: 4px;
  color: #ef4444;
}

.sheet-cell {
  padding: 16px;
}

.sheet-form-item {
  margin-bottom: 0;
}

.sheet-cell--split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 12px;
}

.sheet-side-title {
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
}

.sheet-field-hint {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

.sheet-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.operation-panel {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.operation-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
}

.operation-panel__title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
}

.operation-panel__tip {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

.project-suggestion-row {
  margin-bottom: 12px;
}

.operation-suggestion-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.operation-suggestion-empty {
  min-height: 32px;
  display: flex;
  align-items: center;
  color: #94a3b8;
  font-size: 12px;
}

.operation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.operation-item {
  background: #fff;
  border: 1px solid #dbeafe;
  border-radius: 16px;
  padding: 12px;
}

.operation-item__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: #0f172a;
  font-weight: 700;
}

.operation-item__actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.operation-empty {
  color: #94a3b8;
  font-size: 12px;
  padding: 8px 0 2px;
}

.treatment-draft-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.treatment-draft-hint {
  color: #64748b;
  font-size: 12px;
}

.image-upload-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.image-board {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(124px, 1fr));
  gap: 12px;
}

.image-upload-card,
.image-thumb-card {
  min-height: 124px;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  background: #fff;
}

.image-upload-card {
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-upload-card__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
}

.image-upload-card__inner i {
  font-size: 24px;
  color: #0ea5e9;
}

.image-thumb-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.image-thumb-card__preview {
  height: 84px;
  background: #f8fafc;
  cursor: pointer;
}

.image-thumb-card__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-thumb-card__file {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #64748b;
  font-size: 12px;
}

.image-thumb-card__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 10px 0;
}

.image-thumb-card__meta strong {
  color: #0f172a;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-all;
}

.image-thumb-card__meta span {
  color: #94a3b8;
  font-size: 12px;
}

.image-thumb-card__actions {
  display: flex;
  justify-content: space-between;
  padding: 6px 10px 10px;
}

.image-board__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 124px;
  border-radius: 16px;
  border: 1px dashed #cbd5e1;
  color: #94a3b8;
}

.editor-footer {
  position: sticky;
  bottom: 0;
  z-index: 5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
}

.preview-dialog-body {
  min-height: 320px;
}

.preview-dialog-image {
  width: 100%;
  max-height: 72vh;
  object-fit: contain;
}

.preview-dialog-file {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #64748b;
}

.preview-dialog-file i {
  font-size: 52px;
}

@media (max-width: 1360px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .page-head,
  .editor-head,
  .operation-panel__head,
  .treatment-draft-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .sheet-row {
    grid-template-columns: 1fr;
  }

  .sheet-label {
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }

  .sheet-cell--split {
    grid-template-columns: 1fr;
  }

  .editor-footer {
    position: static;
  }
}
</style>

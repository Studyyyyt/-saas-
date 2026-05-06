<template>
  <div class="staff-h5-page p360-h5-page">
    <div class="h5-hero-card hero-card-mobile">
      <div class="hero-main">
        <div class="h5-page-kicker">员工微信 H5</div>
        <h2>患者360</h2>
        <p>{{ patientName || '患者档案' }} · 微信端快速查看患者全景信息</p>
      </div>
      <el-button class="hero-back-btn" size="small" plain @click="$router.push({ path: '/staff-h5/patients', query: portalQuery })">返回患者列表</el-button>
    </div>

    <div v-if="loading" class="loading-box"><i class="el-icon-loading"></i></div>

    <template v-else-if="data.patient">
      <div class="h5-section-card">
        <div class="section-title">基本信息</div>
        <div class="info-grid">
          <div class="info-item"><span>姓名</span><strong>{{ data.patient.name || '-' }}</strong></div>
          <div class="info-item"><span>性别</span><strong>{{ data.patient.gender || '-' }}</strong></div>
          <div class="info-item"><span>手机</span><strong>{{ data.patient.phone || '-' }}</strong></div>
          <div class="info-item"><span>年龄</span><strong>{{ formatAge(data.patient) }}</strong></div>
          <div class="info-item info-item--full"><span>患者来源</span><strong>{{ data.patient.customer_source || '-' }}</strong></div>
        </div>
      </div>

      <div v-if="doctorContext" class="h5-section-card doctor-context-card">
        <div class="section-title">当前医生视角</div>
        <div class="info-grid">
          <div class="info-item"><span>医生</span><strong>{{ doctorContext }}</strong></div>
          <div class="info-item"><span>匹配预约</span><strong>{{ doctorAppointments.length }}</strong></div>
        </div>
      </div>

      <div class="h5-summary-row">
        <div class="h5-summary-card"><div class="summary-num">{{ data.visitCount || 0 }}</div><div class="summary-label">就诊次数</div></div>
        <div class="h5-summary-card accent"><div class="summary-num">{{ (data.treatments || []).length }}</div><div class="summary-label">治疗记录</div></div>
        <div class="h5-summary-card success"><div class="summary-num">{{ appointments.length }}</div><div class="summary-label">预约记录</div></div>
      </div>

      <div class="h5-actions-grid">
        <div class="h5-action-trigger">
          <el-button type="primary" class="h5-action-btn" @click="handleOpenAppointmentAction">新增预约</el-button>
        </div>
        <div class="h5-action-trigger">
          <el-button type="success" class="h5-action-btn" @click="handleOpenRecordAction">新增病历</el-button>
        </div>
        <div class="h5-action-trigger">
          <el-button type="primary" plain class="h5-action-btn" @click="handleOpenConsentAction">下发同意书</el-button>
        </div>
      </div>

      <div class="h5-upload-row">
        <el-upload
          :action="`/patient-images/upload`"
          :data="uploadExtra"
          :show-file-list="false"
          :on-success="onUploadSuccess"
          :on-error="onUploadError"
          :before-upload="beforeUpload"
          accept="image/*,.dcm"
          class="upload-btn"
        >
          <el-button type="warning">上传影像</el-button>
        </el-upload>
      </div>

      <div class="h5-section-card">
        <div class="section-title">电子知情同意书</div>
        <div v-if="consents.length" class="list-cards">
          <div v-for="item in visibleConsents" :key="item.id" class="list-card">
            <div class="list-card__title">{{ item.title || '电子知情同意书' }}</div>
            <div class="list-card__meta">{{ item.doctor_name || '门诊医生' }} · {{ item.status || '待签署' }}</div>
            <div class="list-card__desc">下发时间：{{ formatDateTime(item.issued_at) || '—' }}</div>
            <div v-if="item.signed_at" class="list-card__desc">签署时间：{{ formatDateTime(item.signed_at) }}</div>
            <div class="list-card__actions">
              <el-button size="mini" type="text" @click="previewConsent(item)">查看详情</el-button>
            </div>
          </div>
          <el-button v-if="consents.length > consentLimit" type="text" class="more-btn" @click="toggleConsentExpand">
            {{ consentExpanded ? '收起同意书' : '查看更多同意书' }}
          </el-button>
        </div>
        <el-empty v-else description="暂无知情同意书"></el-empty>
      </div>

      <div class="h5-section-card">
        <div class="section-title">影像管理</div>
        <div v-if="images.length" class="thumb-grid">
          <div v-for="img in visibleImages" :key="img.id" class="thumb-card">
            <div class="thumb-image-wrap" @click="previewImage(img)">
              <img v-if="isPreviewable(img)" :src="imageUrl(img)" :alt="img.image_name" class="thumb-image" />
              <div v-else class="thumb-file-wrap">
                <i class="el-icon-document"></i>
                <span>{{ img.image_type || '文件' }}</span>
              </div>
            </div>
              <div class="thumb-card__body">
              <div class="thumb-card__title">{{ img.image_name || '未命名文件' }}</div>
              <div class="thumb-card__meta">{{ formatDate(img.image_date) || '-' }} · {{ img.image_type || '-' }}</div>
              <div class="thumb-card__meta" :style="{ color: img.sent_to_patient ? '#67C23A' : '#E6A23C' }">
                {{ img.sent_to_patient ? '已发送到患者端' : '尚未发送到患者端' }}
              </div>
              <div class="thumb-card__actions">
                <el-button size="mini" type="text" @click="previewImage(img)">预览</el-button>
                <el-button size="mini" type="text" @click="sendImageToPatient(img)">{{ img.sent_to_patient ? '重新发送' : '发送给患者' }}</el-button>
                <el-button size="mini" type="text" style="color:#F56C6C" @click="deleteImage(img.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        <el-button v-if="images.length > imageLimit" type="text" class="more-btn" @click="toggleImageExpand">
          {{ imageExpanded ? '收起影像' : '查看更多影像' }}
        </el-button>
        <el-empty v-else description="暂无影像资料"></el-empty>
      </div>

      <div class="h5-section-card">
        <div class="section-title">最近预约</div>
        <div v-if="doctorAppointments.length" class="list-cards">
          <div v-for="item in visibleAppointments" :key="item.id" class="list-card">
            <div class="list-card__title">{{ formatDate(item.appointment_date) }} {{ formatTime(item.appointment_time) }}</div>
            <div class="list-card__meta">{{ item.doctor_name || '未指定医生' }} · {{ item.status || '未知状态' }}</div>
            <div v-if="item._offline" class="list-card__desc">
              <el-tag size="mini" :type="item._offline.failed ? 'danger' : 'warning'" effect="plain">{{ item._offline.label }}</el-tag>
            </div>
            <div class="list-card__desc">{{ item.appointment_purpose || '未填写预约项目' }}</div>
            <div class="list-card__actions">
              <el-button size="mini" type="text" @click="editAppointment(item)">编辑</el-button>
              <el-button size="mini" type="text" style="color:#F56C6C" @click="deleteAppointment(item.id)">删除</el-button>
            </div>
          </div>
          <el-button v-if="doctorAppointments.length > appointmentLimit" type="text" class="more-btn" @click="toggleAppointmentExpand">
            {{ appointmentExpanded ? '收起预约记录' : '查看更多预约' }}
          </el-button>
        </div>
        <el-empty v-else description="暂无预约记录"></el-empty>
      </div>

      <div class="h5-section-card">
        <div class="section-title">病历记录</div>
        <div v-if="pendingLabOperationCount > 0" class="record-summary-tip">待登记加工 {{ pendingLabOperationCount }}，按病历操作维度统计</div>
        <div v-if="records.length" class="list-cards">
          <div v-for="record in visibleRecords" :key="record.id" class="list-card">
            <div class="list-card__title-row">
              <div class="list-card__title">{{ formatDate(record.visit_date) || '未记录日期' }}</div>
              <el-badge v-if="Number(record.pending_lab_count || 0) > 0" :value="record.pending_lab_count" :max="99" type="danger" />
            </div>
            <div class="list-card__meta">{{ record.doctor_name || '门诊医生' }}</div>
            <div v-if="record._offline" class="list-card__desc">
              <el-tag size="mini" :type="record._offline.failed ? 'danger' : 'warning'" effect="plain">{{ record._offline.label }}</el-tag>
            </div>
            <div class="list-card__desc">操作：{{ record.operation_summary || '无' }}</div>
            <div class="list-card__desc">诊断：{{ record.diagnosis || '无' }}</div>
            <div class="list-card__actions">
              <el-button size="mini" type="text" @click="editRecord(record)">编辑</el-button>
              <el-button size="mini" type="text" style="color:#F56C6C" @click="deleteRecord(record.id)">删除</el-button>
            </div>
          </div>
          <el-button v-if="records.length > recordLimit" type="text" class="more-btn" @click="toggleRecordExpand">
            {{ recordExpanded ? '收起病历' : '查看更多病历' }}
          </el-button>
        </div>
        <el-empty v-else description="暂无病历记录"></el-empty>
      </div>
    </template>

    <div v-if="appointmentDialog" ref="appointmentEditor" class="inline-editor-card">
      <div class="inline-editor-card__header">
        <div>
          <div class="inline-editor-card__title">{{ appointmentEditing ? '编辑预约' : '新增预约' }}</div>
          <div class="inline-editor-card__tip">直接在当前页面填写预约信息，无需弹出浮层。</div>
        </div>
        <el-button size="mini" plain @click="closeAppointmentDialog">收起</el-button>
      </div>
      <el-form :model="appointmentForm" label-width="90px">
        <el-form-item label="项目库">
          <el-select v-model="selectedTreatmentCatalogId" placeholder="请选择处置收费项目" filterable default-first-option style="width:100%" @change="applyTreatmentCatalog">
            <el-option
              v-for="item in treatmentProjectOptions"
              :key="item.id"
              :label="treatmentProjectOptionLabel(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker v-model="appointmentForm.appointment_date" type="date" value-format="yyyy-MM-dd" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-time-picker v-model="appointmentForm.appointment_time" value-format="HH:mm:ss" format="HH:mm" style="width:100%"></el-time-picker>
        </el-form-item>
        <el-form-item label="接诊医生">
          <el-select v-model="appointmentForm.doctor_account_id" placeholder="请选择接诊医生" style="width:100%">
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约项目">
          <el-input v-model="appointmentForm.appointment_purpose" type="textarea" :rows="2"></el-input>
        </el-form-item>
      </el-form>
      <div class="drawer-actions">
        <el-button @click="closeAppointmentDialog">取消</el-button>
        <el-button type="primary" @click="submitAppointment">{{ appointmentEditing ? '保存' : '确定' }}</el-button>
      </div>
    </div>

    <div v-if="recordDialog" ref="recordEditor" class="inline-editor-card">
      <div class="inline-editor-card__header">
        <div>
          <div class="inline-editor-card__title">{{ recordEditing ? '编辑病历' : '新增病历' }}</div>
          <div class="inline-editor-card__tip">病历编辑区已展开到当前页面。</div>
        </div>
        <el-button size="mini" plain @click="closeRecordDialog">收起</el-button>
      </div>
      <el-form :model="recordForm" label-width="90px">
        <el-form-item label="接诊医生">
          <el-select v-model="recordForm.doctor_account_id" placeholder="请选择接诊医生" style="width:100%">
            <el-option
              v-for="doctor in doctorOptions"
              :key="doctor.id"
              :label="doctor.name"
              :value="doctor.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="就诊日期"><el-date-picker v-model="recordForm.visit_date" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item>
        <el-form-item label="主诉"><el-input v-model="recordForm.chief_complaint" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="诊断"><el-input v-model="recordForm.diagnosis" type="textarea" :rows="2" /></el-form-item>
        <div class="h5-template-panel">
          <div class="h5-template-panel__title">病历模板库</div>
          <div class="h5-template-panel__tip">选择模板即导入，也可把当前病历保存为模板。</div>
          <el-select v-model="selectedMedicalRecordTemplateId" clearable filterable placeholder="选择病历模板" style="width:100%;margin-top:10px;" @change="applyMedicalRecordTemplate">
            <el-option
              v-for="item in medicalRecordTemplateOptions"
              :key="item.id"
              :label="item.template_name"
              :value="item.id"
            />
          </el-select>
          <div class="h5-template-panel__actions">
            <el-button size="mini" type="success" plain @click="saveCurrentRecordAsTemplate">保存为模板</el-button>
            <el-button size="mini" plain :disabled="!selectedMedicalRecordTemplateId" @click="deleteSelectedMedicalRecordTemplate">删除模板</el-button>
          </div>
        </div>
        <div class="h5-operation-panel">
          <div class="h5-operation-panel__head">
            <span>本次操作</span>
            <el-button size="mini" plain @click="appendRecordOperation" :disabled="!recordOperationOptions.length">手动添加</el-button>
          </div>
          <el-select v-model="selectedRecordProjectId" clearable filterable placeholder="选择项目，展示标准操作流程" style="width:100%" @change="handleRecordProjectSuggestionChange">
            <el-option v-for="project in treatmentProjectOptions" :key="project.id" :label="project.project_name" :value="project.id" />
          </el-select>
          <div v-if="selectedRecordProjectOperations.length" class="h5-operation-suggestion-list">
            <el-button
              v-for="relation in selectedRecordProjectOperations"
              :key="`h5-record-${relation.operation_id}`"
              size="mini"
              :type="isRecordSuggestedOperationSelected(relation) ? 'primary' : 'default'"
              plain
              @click="toggleRecordSuggestedOperation(relation)"
            >
              {{ relation.operation_name }}
            </el-button>
          </div>
          <div v-else class="h5-operation-suggestion-empty">
            {{ treatmentProjectOptions.length ? '当前项目未配置标准操作，可手动添加。' : '项目库为空，病历仍可直接保存。' }}
          </div>
          <div v-if="recordForm.operation_items && recordForm.operation_items.length" class="h5-operation-list">
            <div v-for="(item, index) in recordForm.operation_items" :key="item.local_key" class="h5-operation-item">
              <div class="h5-operation-item__head">
                <span>操作 {{ index + 1 }}</span>
                <div class="h5-operation-item__actions">
                  <el-tag v-if="item.need_lab_processing === 1" size="mini" type="danger">待登记加工</el-tag>
                  <el-button size="mini" type="text" style="color:#F56C6C" @click="removeRecordOperation(index)">删除</el-button>
                </div>
              </div>
              <el-form-item label="关联项目">
                <el-select v-model="item.project_id" clearable filterable style="width:100%" @change="handleRecordOperationProjectChange(item)">
                  <el-option v-for="project in treatmentProjectOptions" :key="project.id" :label="project.project_name" :value="project.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="操作名称">
                <el-select v-model="item.operation_id" clearable filterable style="width:100%" @change="handleRecordOperationChange(item)">
                  <el-option v-for="operation in recordOperationOptions" :key="operation.id" :label="recordOperationOptionLabel(operation)" :value="operation.id" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="item.need_lab_processing === 1" label="加工厂">
                <el-select
                  v-model="item.factory_id"
                  clearable
                  filterable
                  style="width:100%"
                  placeholder="必选：不选则不会生成加工订单"
                  @change="handleRecordOperationFactoryChange(item)"
                >
                  <el-option v-for="factory in labFactoryOptions" :key="factory.id" :label="factory.name" :value="factory.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="牙位"><ToothSelector v-model="item.tooth_positions" @input="refreshRecordTreatmentDraft" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="item.remark" /></el-form-item>
            </div>
          </div>
        </div>
        <el-form-item label="治疗文稿">
          <div class="h5-treatment-toolbar">
            <span>勾选操作后自动生成初稿</span>
            <el-button size="mini" type="text" @click="regenerateRecordTreatmentDraft" :disabled="!(recordForm.operation_items || []).length">重新生成</el-button>
          </div>
          <el-input v-model="recordForm.treatment" type="textarea" :rows="3" @input="handleRecordTreatmentInput" />
        </el-form-item>
        <el-form-item label="病历牙位"><ToothSelector v-model="recordForm.tooth_positions" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="recordForm.notes" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div class="drawer-actions drawer-actions--wrap">
        <el-button type="success" plain @click="saveCurrentRecordAsTemplate">保存为模板</el-button>
        <el-button @click="closeRecordDialog">取消</el-button>
        <el-button type="primary" @click="submitRecord">{{ recordEditing ? '保存' : '确定' }}</el-button>
      </div>
    </div>

    <div v-if="labRegistrationDialogVisible" ref="labRegistrationEditor" class="inline-editor-card inline-editor-card--sub">
      <div class="inline-editor-card__header">
        <div>
          <div class="inline-editor-card__title">登记加工信息</div>
          <div class="inline-editor-card__tip">加工登记区已展开到当前页面。</div>
        </div>
        <el-button size="mini" plain @click="closeLabRegistrationDialog">收起</el-button>
      </div>
      <el-form :model="labRegistrationDraft" label-width="90px">
        <el-form-item label="项目">
          <el-input :value="labRegistrationDraft.project_name" disabled />
        </el-form-item>
        <el-form-item label="加工操作">
          <el-input :value="labRegistrationDraft.operation_name" disabled />
        </el-form-item>
        <el-form-item label="加工厂">
          <el-select v-model="labRegistrationDraft.factory_id" clearable filterable style="width:100%" placeholder="请选择加工厂">
            <el-option v-for="factory in labFactoryOptions" :key="factory.id" :label="factory.name" :value="factory.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="牙位">
          <ToothSelector v-model="labRegistrationDraft.tooth_positions" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="labRegistrationDraft.remark" />
        </el-form-item>
      </el-form>
      <div class="drawer-actions">
        <el-button @click="closeLabRegistrationDialog">取消</el-button>
        <el-button type="primary" @click="applyLabRegistrationDraft">确定</el-button>
      </div>
    </div>

    <div v-if="consentDialog" ref="consentEditor" class="inline-editor-card">
      <div class="inline-editor-card__header">
        <div>
          <div class="inline-editor-card__title">下发电子知情同意书</div>
          <div class="inline-editor-card__tip">知情同意书编辑区已展开到当前页面。</div>
        </div>
        <el-button size="mini" plain @click="closeConsentDialog">收起</el-button>
      </div>
      <el-form :model="consentForm" label-width="90px">
        <el-form-item label="模板库">
          <el-select v-model="selectedConsentTemplateId" placeholder="可选：从模板库带出" style="width:100%" @change="applyConsentTemplate">
            <el-option
              v-for="item in consentTemplateOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="consentForm.title" placeholder="如：口腔治疗知情同意书"></el-input>
        </el-form-item>
        <el-form-item label="正文内容">
          <el-input v-model="consentForm.content" type="textarea" :rows="10" placeholder="请输入需要患者阅读并签字确认的内容"></el-input>
        </el-form-item>
      </el-form>
      <div class="drawer-actions">
        <el-button @click="closeConsentDialog">取消</el-button>
        <el-button type="primary" @click="submitConsent">下发</el-button>
      </div>
    </div>

    <el-drawer :title="consentPreview.title || '电子知情同意书'" :visible.sync="consentPreviewDialog" size="92%" direction="btt" custom-class="staff-p360-drawer" append-to-body :z-index="4000">
      <div v-if="consentPreview.id" class="drawer-sheet">
        <div class="consent-preview-box">
          <div class="consent-preview-meta">
            <div class="consent-preview-item"><span>医生</span><strong>{{ consentPreview.doctor_name || '门诊医生' }}</strong></div>
            <div class="consent-preview-item"><span>状态</span><strong>{{ consentPreview.status || '待签署' }}</strong></div>
            <div class="consent-preview-item"><span>下发时间</span><strong>{{ formatDateTime(consentPreview.issued_at) || '—' }}</strong></div>
            <div class="consent-preview-item"><span>签署时间</span><strong>{{ formatDateTime(consentPreview.signed_at) || '未签署' }}</strong></div>
          </div>
          <div class="consent-preview-content">{{ consentPreview.content || '暂无内容' }}</div>
          <div v-if="consentPreview.signature_data" class="consent-preview-sign">
            <div class="consent-preview-sign__title">患者签名</div>
            <div class="consent-preview-sign__meta">签署人：{{ consentPreview.signature_name || '患者本人' }}</div>
            <img :src="consentPreview.signature_data" alt="患者签名" class="consent-preview-sign__image" />
            <div v-if="consentPreview.signature_remark" class="consent-preview-sign__meta">备注：{{ consentPreview.signature_remark }}</div>
          </div>
        </div>
        <div class="drawer-actions">
          <el-button @click="consentPreviewDialog = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>

    <el-drawer :title="previewImageName" :visible.sync="previewVisible" size="96%" direction="btt" custom-class="staff-p360-drawer" append-to-body :z-index="4000">
      <div class="drawer-sheet">
        <div class="preview-wrap">
          <img v-if="previewImageUrl" :src="previewImageUrl" :alt="previewImageName" class="preview-image" />
        </div>
        <div class="drawer-actions">
          <el-button @click="previewVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import axios from 'axios'
import ToothSelector from '@/components/ToothSelector.vue'
import { getStaffPortalQuery, saveStaffPortalSessionFromQuery } from '@/utils/portalSession'
import { getPatientAge, rememberRecentPatient } from '@/utils/patientList'
import { buildMedicalRecordTreatmentDraft, normalizeToothPositions } from '@/utils/medicalRecordOperationDraft'
import { augmentCachedData } from '@/utils/offline/cache'
import { fetchCachedResource, saveAppointment, saveMedicalRecord } from '@/utils/offline/apiClient'
import { isLocalEntityId, OFFLINE_ID_MAP_EVENT, resolveMappedServerId } from '@/utils/offline/queue'

function defaultRecordOperationItem() {
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

export default {
  name: 'StaffPatient360H5',
  components: { ToothSelector },
  data() {
    return {
      loading: false,
      data: {},
      patientId: '',
      patientName: '',
      appointmentDialog: false,
      recordDialog: false,
      labRegistrationDialogVisible: false,
      appointmentEditing: false,
      recordEditing: false,
      appointmentExpanded: false,
      recordExpanded: false,
      imageExpanded: false,
      consentExpanded: false,
      appointmentLimit: 5,
      recordLimit: 5,
      imageLimit: 5,
      consentLimit: 5,
      treatmentProjectOptions: [],
      selectedTreatmentCatalogId: null,
      recordOperationOptions: [],
      labFactoryOptions: [],
      labRegistrationDraft: {
        project_id: '',
        project_name: '',
        operation_id: '',
        operation_name: '',
        default_processing_days: 0,
        factory_id: '',
        tooth_positions: '',
        remark: ''
      },
      recordProjectDetailCache: {},
      selectedRecordProjectId: '',
      recordLastAutoTreatmentDraft: '',
      recordTreatmentDraftLocked: false,
      doctorOptions: [],
      consentDialog: false,
      consentTemplateOptions: [],
      selectedConsentTemplateId: null,
      medicalRecordTemplateOptions: [],
      selectedMedicalRecordTemplateId: null,
      consentPreviewDialog: false,
      pendingAutoOpenAppointment: false,
      pendingAutoAppointmentPurpose: '',
      pendingAutoEditAppointmentId: '',
      consentForm: {
        title: '',
        content: ''
      },
      consentPreview: {},
      appointmentForm: {
        patient_id: null,
        patient_name: '',
        appointment_date: '',
        appointment_time: '',
        duration_minutes: 60,
        doctor_account_id: null,
        doctor_name: '',
        appointment_purpose: '',
        status: '待治疗'
      },
      recordForm: {
        patient_id: '',
        patient_name: '',
        doctor_account_id: null,
        doctor_name: '',
        visit_date: '',
        chief_complaint: '',
        diagnosis: '',
        treatment: '',
        tooth_positions: '',
        prescription: '',
        notes: '',
        operation_items: []
      },
      uploadExtra: { imageType: 'X光', imageDate: '', patientId: '', patientName: '' },
      previewVisible: false,
      previewImageUrl: '',
      previewImageName: ''
    }
  },
  computed: {
    portalQuery() {
      return getStaffPortalQuery(this.$route.query)
    },
    appointments() {
      return this.data.appointments || []
    },
    doctorContext() {
      return this.portalQuery.doctorName || ''
    },
    staffAccountId() {
      const value = Number(this.portalQuery.accountId)
      return Number.isFinite(value) && value > 0 ? value : null
    },
    doctorAppointments() {
      if (!this.doctorContext) return this.appointments
      return this.appointments.filter(item => (item.doctor_name || '') === this.doctorContext)
    },
    visibleAppointments() {
      return this.appointmentExpanded ? this.doctorAppointments : this.doctorAppointments.slice(0, this.appointmentLimit)
    },
    records() {
      return this.data.records || this.data.recentRecords || []
    },
    visibleRecords() {
      return this.recordExpanded ? this.records : this.records.slice(0, this.recordLimit)
    },
    pendingLabOperationCount() {
      return (this.records || []).reduce((sum, item) => sum + Number(item.pending_lab_count || 0), 0)
    },
    selectedRecordProjectOperations() {
      const detail = this.recordProjectDetailCache[String(this.selectedRecordProjectId || '')]
      return detail && Array.isArray(detail.operation_relations) ? detail.operation_relations : []
    },
    images() {
      return this.data.images || []
    },
    consents() {
      return this.data.consents || []
    },
    visibleConsents() {
      return this.consentExpanded ? this.consents : this.consents.slice(0, this.consentLimit)
    },
    visibleImages() {
      return this.imageExpanded ? this.images : this.images.slice(0, this.imageLimit)
    }
  },
  watch: {
    '$route.query': {
      async handler(query) {
        const nextQuery = query || {}
        const nextId = String(nextQuery.id || '').trim()
        const currentId = String(this.patientId || '').trim()
        const nextPatientName = String(nextQuery.name || '').trim()
        const nextPendingAutoOpenAppointment = String(nextQuery.openAppointment || '') === '1'
        const nextPendingAutoAppointmentPurpose = String(nextQuery.appointmentPurpose || '').trim()
        const nextPendingAutoEditAppointmentId = String(nextQuery.editAppointmentId || '').trim()
        const routeActionChanged =
          nextPendingAutoOpenAppointment !== this.pendingAutoOpenAppointment ||
          nextPendingAutoAppointmentPurpose !== this.pendingAutoAppointmentPurpose ||
          nextPendingAutoEditAppointmentId !== this.pendingAutoEditAppointmentId

        this.patientName = nextPatientName
        this.pendingAutoOpenAppointment = nextPendingAutoOpenAppointment
        this.pendingAutoAppointmentPurpose = nextPendingAutoAppointmentPurpose
        this.pendingAutoEditAppointmentId = nextPendingAutoEditAppointmentId

        if (!nextId) return
        if (nextId === currentId) {
          if (routeActionChanged && this.pendingAutoOpenAppointment && this.data && this.data.patient) {
            this.handlePendingAppointmentRouteAction()
          }
          return
        }
        this.patientId = nextId
        const resolved = await this.tryResolveLocalPatientRoute()
        if (!resolved) {
          this.load360()
        }
      }
    }
  },
  mounted() {
    saveStaffPortalSessionFromQuery(this.$route.query)
    this.loadLabFactoryOptions()
    this.patientId = this.$route.query.id || ''
    this.patientName = this.$route.query.name || ''
    this.pendingAutoOpenAppointment = String(this.$route.query.openAppointment || '') === '1'
    this.pendingAutoAppointmentPurpose = String(this.$route.query.appointmentPurpose || '').trim()
    this.pendingAutoEditAppointmentId = String(this.$route.query.editAppointmentId || '').trim()
    if (typeof window !== 'undefined') {
      window.addEventListener(OFFLINE_ID_MAP_EVENT, this.handlePatientIdMapped)
    }
    if (this.patientId) {
      this.tryResolveLocalPatientRoute().then(resolved => {
        if (!resolved) {
          this.load360()
        }
      })
    }
  },
  beforeDestroy() {
    if (typeof window !== 'undefined') {
      window.removeEventListener(OFFLINE_ID_MAP_EVENT, this.handlePatientIdMapped)
    }
  },
  methods: {
    async tryResolveLocalPatientRoute() {
      const localPatientId = String(this.patientId || '').trim()
      if (!isLocalEntityId(localPatientId)) {
        return false
      }
      const serverId = await resolveMappedServerId('patient', localPatientId)
      if (!serverId) {
        return false
      }
      await this.$router.replace({
        path: '/staff-h5/patient360',
        query: Object.assign({}, this.$route.query, {
          id: String(serverId)
        })
      }).catch(() => {})
      return true
    },
    handlePatientIdMapped(event) {
      const detail = event && event.detail ? event.detail : {}
      if (detail.entityType !== 'patient') return
      if (String(detail.localId || '') !== String(this.patientId || '')) return
      this.tryResolveLocalPatientRoute()
    },
    normalizeDoctor(item) {
      if (!item || !item.id || !item.name) return null
      return {
        id: Number(item.id),
        name: String(item.name).trim()
      }
    },
    currentDoctorById(id) {
      return (this.doctorOptions || []).find(item => Number(item.id) === Number(id)) || null
    },
    resolveDefaultDoctorAccountId() {
      if (this.staffAccountId && (!(this.doctorOptions || []).length || this.currentDoctorById(this.staffAccountId))) {
        return this.staffAccountId
      }
      const matchedDoctor = (this.doctorOptions || []).find(item => item.name === this.doctorContext)
      return matchedDoctor ? matchedDoctor.id : null
    },
    resolveDefaultDoctorName(doctorAccountId = null) {
      const matchedDoctor = this.currentDoctorById(doctorAccountId)
      if (matchedDoctor && matchedDoctor.name) return matchedDoctor.name
      return this.doctorContext || ''
    },
    resetAppointmentForm() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      this.appointmentForm = {
        patient_id: this.patientId ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId)) : null,
        patient_name: (this.data.patient && this.data.patient.name) || this.patientName || '',
        appointment_date: '',
        appointment_time: '',
        duration_minutes: 60,
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        appointment_purpose: '',
        status: '待治疗'
      }
      this.selectedTreatmentCatalogId = null
    },
    resetRecordForm() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      this.recordForm = {
        patient_id: this.patientId ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId)) : null,
        patient_name: (this.data.patient && this.data.patient.name) || this.patientName || '',
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        visit_date: this.currentDateValue(),
        chief_complaint: '',
        diagnosis: '',
        treatment: '',
        tooth_positions: '',
        prescription: '',
        notes: '',
        operation_items: []
      }
      this.selectedRecordProjectId = ''
      this.selectedMedicalRecordTemplateId = null
      this.recordLastAutoTreatmentDraft = ''
      this.recordTreatmentDraftLocked = false
    },
    scrollInlineEditorIntoView(refName) {
      this.$nextTick(() => {
        const target = this.$refs[refName]
        const element = Array.isArray(target) ? target[0] : target
        if (element && typeof element.scrollIntoView === 'function') {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      })
    },
    closeAppointmentDialog() {
      this.appointmentDialog = false
      this.appointmentEditing = false
    },
    clearPendingAppointmentRouteAction() {
      this.pendingAutoOpenAppointment = false
      this.pendingAutoAppointmentPurpose = ''
      this.pendingAutoEditAppointmentId = ''
      const nextQuery = Object.assign({}, this.$route.query)
      delete nextQuery.openAppointment
      delete nextQuery.appointmentPurpose
      delete nextQuery.editAppointmentId
      this.$router.replace({
        path: this.$route.path,
        query: nextQuery
      }).catch(() => {})
    },
    handlePendingAppointmentRouteAction() {
      if (!this.pendingAutoOpenAppointment) return
      this.$nextTick(() => {
        const targetAppointmentId = String(this.pendingAutoEditAppointmentId || '').trim()
        if (targetAppointmentId) {
          const matchedAppointment = (this.appointments || []).find(item => String((item && item.id) || '').trim() === targetAppointmentId)
          if (matchedAppointment) {
            this.editAppointment(matchedAppointment)
            this.clearPendingAppointmentRouteAction()
            return
          }
        }
        this.openAppointmentDialog()
        if (this.pendingAutoAppointmentPurpose) {
          this.appointmentForm.appointment_purpose = this.pendingAutoAppointmentPurpose
        }
        this.clearPendingAppointmentRouteAction()
      })
    },
    closeRecordDialog() {
      this.recordDialog = false
      this.recordEditing = false
      this.labRegistrationDialogVisible = false
    },
    closeConsentDialog() {
      this.consentDialog = false
    },
    closeLabRegistrationDialog() {
      this.labRegistrationDialogVisible = false
    },
    openAppointmentDialog() {
      this.recordDialog = false
      this.recordEditing = false
      this.consentDialog = false
      this.labRegistrationDialogVisible = false
      this.appointmentEditing = false
      this.resetAppointmentForm()
      if (!(this.doctorOptions || []).length) {
        this.loadDoctorOptions()
      }
      this.appointmentDialog = true
      this.scrollInlineEditorIntoView('appointmentEditor')
    },
    openRecordDialog() {
      this.appointmentDialog = false
      this.appointmentEditing = false
      this.consentDialog = false
      this.recordEditing = false
      this.resetRecordForm()
      this.selectedMedicalRecordTemplateId = null
      if (!(this.doctorOptions || []).length) {
        this.loadDoctorOptions()
      }
      if (!(this.recordOperationOptions || []).length) {
        this.loadRecordOperationOptions()
      }
      if (!(this.treatmentProjectOptions || []).length) {
        this.loadTreatmentCatalogOptions()
      }
      if (!(this.medicalRecordTemplateOptions || []).length) {
        this.loadMedicalRecordTemplateOptions()
      }
      this.recordDialog = true
      this.scrollInlineEditorIntoView('recordEditor')
    },
    handleOpenAppointmentAction() {
      try {
        this.openAppointmentDialog()
      } catch (error) {
        console.error('Open appointment dialog failed:', error)
        this.$message.error('打开新增预约失败')
      }
    },
    handleOpenRecordAction() {
      try {
        this.openRecordDialog()
      } catch (error) {
        console.error('Open medical record dialog failed:', error)
        this.$message.error('打开新增病历失败')
      }
    },
    buildDefaultConsentTitle() {
      return '口腔治疗知情同意书'
    },
    buildDefaultConsentContent() {
      const patientName = (this.data.patient && this.data.patient.name) || this.patientName || '患者'
      const doctorName = this.resolveDefaultDoctorName(this.resolveDefaultDoctorAccountId()) || '门诊医生'
      return `${patientName}：\n\n您好，请在治疗前仔细阅读以下内容：\n1. 医生已向您说明本次治疗的目的、流程、常见风险及注意事项。\n2. 您已知晓治疗过程中可能出现疼痛、肿胀、出血、治疗效果个体差异等情况。\n3. 如有既往病史、药物过敏史、妊娠或其他特殊情况，请在治疗前主动告知医生。\n4. 您确认已获得充分提问机会，并自愿接受本次治疗安排。\n\n接诊医生：${doctorName}\n请阅读完毕后在患者公众号 H5 中签字确认。`
    },
    loadConsentTemplateOptions() {
      fetchCachedResource({
        cacheKey: 'ref:consent-templates-enabled',
        scope: '',
        url: '/consent-template/selectEnabled',
        loader: () => axios.get('/consent-template/selectEnabled')
      }).then(res => {
        this.consentTemplateOptions = Array.isArray(res && res.data) ? res.data : []
      }).catch(() => {
        this.consentTemplateOptions = []
      })
    },
    loadMedicalRecordTemplateOptions() {
      fetchCachedResource({
        cacheKey: 'ref:medical-record-templates',
        scope: '',
        url: '/medical-record-templates/selectEnabled',
        loader: () => axios.get('/medical-record-templates/selectEnabled')
      }).then(res => {
        this.medicalRecordTemplateOptions = Array.isArray(res && res.data) ? res.data : []
      }).catch(() => {
        this.medicalRecordTemplateOptions = []
      })
    },
    applyMedicalRecordTemplate() {
      const template = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedMedicalRecordTemplateId || 0))
      if (!template) return
      this.recordForm = Object.assign({}, this.recordForm, {
        chief_complaint: template.chief_complaint || '',
        diagnosis: template.diagnosis || '',
        treatment: template.treatment || '',
        tooth_positions: template.tooth_positions || '',
        prescription: template.prescription || '',
        notes: template.notes || '',
        operation_items: this.normalizeLoadedRecordOperationItems(template.operation_items || [])
      })
      this.selectedRecordProjectId = this.recordForm.operation_items.length ? (this.recordForm.operation_items[0].project_id || '') : ''
      this.recordLastAutoTreatmentDraft = buildMedicalRecordTreatmentDraft(this.recordForm.operation_items || [])
      this.recordTreatmentDraftLocked = String(this.recordForm.treatment || '').trim() && String(this.recordForm.treatment || '').trim() !== this.recordLastAutoTreatmentDraft
    },
    buildMedicalRecordTemplatePayload(templateName) {
      return {
        template_name: String(templateName || '').trim(),
        chief_complaint: this.recordForm.chief_complaint || '',
        diagnosis: this.recordForm.diagnosis || '',
        treatment: this.recordForm.treatment || '',
        tooth_positions: this.resolveRecordToothPositions(),
        prescription: this.recordForm.prescription || '',
        notes: this.recordForm.notes || '',
        operation_items: this.normalizeRecordOperationItems(this.recordForm.operation_items),
        created_by: this.staffAccountId,
        created_by_name: this.doctorContext || ''
      }
    },
    saveCurrentRecordAsTemplate() {
      const currentTemplate = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedMedicalRecordTemplateId || 0))
      const defaultName = currentTemplate && currentTemplate.template_name
        ? currentTemplate.template_name
        : (this.recordForm.diagnosis || this.recordForm.chief_complaint || '病历模板')
      this.$prompt('请输入模板名称', '保存病历模板', {
        confirmButtonText: '保存',
        cancelButtonText: '取消',
        inputValue: defaultName
      }).then(async ({ value }) => {
        const res = await axios.post('/medical-record-templates/add', this.buildMedicalRecordTemplatePayload(value))
        if (String((res.data || {}).code || '') !== '200') {
          this.$message.error((res.data || {}).msg || '模板保存失败')
          return
        }
        this.$message.success('病历模板已保存')
        this.selectedMedicalRecordTemplateId = null
        this.loadMedicalRecordTemplateOptions()
      }).catch(() => {})
    },
    deleteSelectedMedicalRecordTemplate() {
      const template = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedMedicalRecordTemplateId || 0))
      if (!template || !template.id) return
      this.$confirm(`确认删除模板“${template.template_name}”吗？`, '提示', { type: 'warning' }).then(async () => {
        const res = await axios.delete(`/medical-record-templates/delete/${template.id}`)
        if (String((res.data || {}).code || '') !== '200') {
          this.$message.error((res.data || {}).msg || '删除失败')
          return
        }
        this.$message.success('模板已删除')
        this.selectedMedicalRecordTemplateId = null
        this.loadMedicalRecordTemplateOptions()
      }).catch(() => {})
    },
    applyConsentTemplate() {
      const item = (this.consentTemplateOptions || []).find(option => String(option.id) === String(this.selectedConsentTemplateId || ''))
      if (!item) return
      this.consentForm.title = item.title || ''
      this.consentForm.content = item.content || ''
    },
    openConsentDialog() {
      this.appointmentDialog = false
      this.appointmentEditing = false
      this.recordDialog = false
      this.recordEditing = false
      this.labRegistrationDialogVisible = false
      this.selectedConsentTemplateId = null
      this.consentForm = {
        title: this.buildDefaultConsentTitle(),
        content: this.buildDefaultConsentContent()
      }
      this.loadConsentTemplateOptions()
      this.consentDialog = true
      this.scrollInlineEditorIntoView('consentEditor')
    },
    handleOpenConsentAction() {
      try {
        this.openConsentDialog()
      } catch (error) {
        console.error('Open consent dialog failed:', error)
        this.$message.error('打开知情同意书失败')
      }
    },
    submitConsent() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      const doctorName = this.resolveDefaultDoctorName(doctorAccountId)
      if (!this.patientId) {
        this.$message.warning('患者ID不能为空')
        return
      }
      if (!this.consentForm.title || !String(this.consentForm.title).trim()) {
        this.$message.warning('请填写同意书标题')
        return
      }
      if (!this.consentForm.content || !String(this.consentForm.content).trim()) {
        this.$message.warning('请填写同意书正文')
        return
      }
      axios.post('/patient-consent/issue', {
        patient_id: Number(this.patientId),
        patient_name: (this.data.patient && this.data.patient.name) || this.patientName || '',
        doctor_account_id: doctorAccountId,
        doctor_name: doctorName,
        title: this.consentForm.title,
        content: this.consentForm.content
      }).then(res => {
        if (res.data.code === '200') {
          this.$message.success('知情同意书已下发')
          this.consentDialog = false
          this.load360()
        } else {
          this.$message.error(res.data.msg || '下发失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '下发失败')
      })
    },
    previewConsent(item) {
      this.consentPreview = Object.assign({}, item)
      this.consentPreviewDialog = true
    },
    load360() {
      this.loading = true
      if (isLocalEntityId(this.patientId)) {
        augmentCachedData('patient360', {}, { patientId: this.patientId }).then(payload => {
          this.data = payload && payload.patient ? payload : {}
          rememberRecentPatient((this.data && this.data.patient) || this.patientId)
          this.uploadExtra.patientId = this.patientId
          this.uploadExtra.patientName = (this.data.patient && this.data.patient.name) || this.patientName
          this.resetAppointmentForm()
          this.resetRecordForm()
          this.loadDoctorOptions()
          this.loadTreatmentCatalogOptions()
          this.loadRecordOperationOptions()
          this.loadMedicalRecordTemplateOptions()
          this.appointmentEditing = false
          this.recordEditing = false
          this.handlePendingAppointmentRouteAction()
        }).finally(() => {
          this.loading = false
        })
        return
      }
      fetchCachedResource({
        cacheKey: `page:staff:patient360:${this.patientId}`,
        scope: 'patient360',
        url: `/patient360/overview/${this.patientId}`,
        loader: () => axios.get(`/patient360/overview/${this.patientId}`),
        context: { patientId: this.patientId },
        notifier: message => this.$message.warning(message)
      }).then(response => {
        this.data = response && response.data ? response.data : {}
        if (this.data && this.data.patient) {
          rememberRecentPatient((this.data && this.data.patient) || this.patientId)
          this.uploadExtra.patientId = this.patientId
          this.uploadExtra.patientName = (this.data.patient && this.data.patient.name) || this.patientName
          this.resetAppointmentForm()
          this.resetRecordForm()
          this.loadDoctorOptions()
          this.loadTreatmentCatalogOptions()
          this.loadRecordOperationOptions()
          this.loadMedicalRecordTemplateOptions()
          this.appointmentEditing = false
          this.recordEditing = false
          this.consentPreviewDialog = false
          this.handlePendingAppointmentRouteAction()
        }
      }).finally(() => {
        this.loading = false
      })
    },
    loadDoctorOptions() {
      fetchCachedResource({
        cacheKey: 'ref:doctors-active',
        scope: '',
        url: '/accounts/doctors/active',
        loader: () => axios.get('/accounts/doctors/active')
      }).then(response => {
        this.doctorOptions = (Array.isArray(response && response.data) ? response.data : [])
          .map(this.normalizeDoctor)
          .filter(Boolean)
        const defaultDoctorId = this.resolveDefaultDoctorAccountId()
        if (!this.appointmentEditing) {
          this.appointmentForm.doctor_account_id = defaultDoctorId
          this.appointmentForm.doctor_name = this.resolveDefaultDoctorName(defaultDoctorId)
        }
        if (!this.recordEditing) {
          this.recordForm.doctor_account_id = defaultDoctorId
          this.recordForm.doctor_name = this.resolveDefaultDoctorName(defaultDoctorId)
        }
      }).catch(() => {
        this.doctorOptions = []
      })
    },
    formatDate(value) {
      if (!value) return ''
      return String(value).slice(0, 10)
    },
    formatDateTime(value) {
      if (!value) return ''
      return String(value).slice(0, 19).replace('T', ' ')
    },
    formatAge(patient) {
      const age = getPatientAge(patient)
      return age === '' ? '-' : `${age}岁`
    },
    currentDateValue() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    formatTime(value) {
      if (!value) return '--:--'
      return String(value).slice(0, 5)
    },
    imageUrl(img) {
      return `/patient-images/file/${img.id}`
    },
    isPreviewable(img) {
      const name = String((img && img.image_name) || '').toLowerCase()
      return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(name)
    },
    loadTreatmentCatalogOptions() {
      fetchCachedResource({
        cacheKey: 'ref:treatment-projects-enabled',
        scope: '',
        url: '/treatment-projects/selectEnabled',
        loader: () => axios.get('/treatment-projects/selectEnabled')
      }).then(res => {
        this.treatmentProjectOptions = Array.isArray(res && res.data) ? res.data : []
      }).catch(() => {
        this.treatmentProjectOptions = []
      })
    },
    loadRecordOperationOptions() {
      fetchCachedResource({
        cacheKey: 'ref:treatment-operations-enabled',
        scope: '',
        url: '/treatment-operations/selectEnabled',
        loader: () => axios.get('/treatment-operations/selectEnabled')
      }).then(res => {
        this.recordOperationOptions = Array.isArray(res && res.data) ? res.data : []
      }).catch(() => {
        this.recordOperationOptions = []
      })
    },
    loadLabFactoryOptions() {
      fetchCachedResource({
        cacheKey: 'ref:lab-factories-enabled',
        scope: '',
        url: '/lab-factories/selectEnabled',
        loader: () => axios.get('/lab-factories/selectEnabled')
      }).then(res => {
        this.labFactoryOptions = Array.isArray(res && res.data) ? res.data : []
      }).catch(() => {
        this.labFactoryOptions = []
      })
    },
    applyTreatmentCatalog() {
      const item = (this.treatmentProjectOptions || []).find(option => String(option.id) === String(this.selectedTreatmentCatalogId || ''))
      if (!item) return
      this.appointmentForm.appointment_purpose = item.project_name || ''
    },
    recordOperationOptionLabel(item) {
      const category = item && item.operation_category ? ` / ${item.operation_category}` : ''
      return `${item.operation_name || '未命名操作'}${category}`
    },
    treatmentProjectOptionLabel(item) {
      const code = item && item.project_code ? `${item.project_code} / ` : ''
      const category = item && item.category_path ? ` / ${item.category_path}` : ''
      return `${code}${item && item.project_name ? item.project_name : '未命名项目'}${category} / ¥${this.formatMoney(item && item.default_price)}`
    },
    async loadRecordProjectDetail(projectId) {
      const key = String(projectId || '')
      if (!key) return null
      if (this.recordProjectDetailCache[key]) {
        return this.recordProjectDetailCache[key]
      }
      const res = await axios.get('/treatment-projects/selectById', { params: { id: projectId } })
      const detail = res.data && res.data.code === '200' ? (res.data.data || null) : null
      if (detail) {
        this.$set(this.recordProjectDetailCache, key, detail)
      }
      return detail
    },
    async handleRecordProjectSuggestionChange(projectId) {
      this.selectedRecordProjectId = projectId || ''
      if (projectId) {
        await this.loadRecordProjectDetail(projectId)
        this.maybeOpenLabRegistrationByProject(projectId)
      }
    },
    findLabOperationCandidate(projectId) {
      const projectDetail = this.recordProjectDetailCache[String(projectId || '')]
      const relations = projectDetail && Array.isArray(projectDetail.operation_relations) ? projectDetail.operation_relations : []
      const relation = relations.find(item => Number(item.need_lab_processing) === 1)
      if (relation) {
        return {
          operation_id: relation.operation_id,
          operation_name: relation.operation_name,
          default_processing_days: Number(relation.default_processing_days || 0)
        }
      }
      const operation = (this.recordOperationOptions || []).find(item => String(item.operation_name || '').includes('取模') || Number(item.need_lab_processing) === 1)
      if (!operation) return null
      return {
        operation_id: operation.id,
        operation_name: operation.operation_name,
        default_processing_days: Number(operation.default_processing_days || 0)
      }
    },
    maybeOpenLabRegistrationByProject(projectId) {
      const project = (this.treatmentProjectOptions || []).find(item => String(item.id) === String(projectId || ''))
      if (!project || !String(project.project_name || '').includes('取模')) {
        return
      }
      const candidate = this.findLabOperationCandidate(projectId)
      if (!candidate) {
        this.$message.warning('项目命中“取模”，但当前未配置可登记外加工的标准操作，请手动添加操作')
        return
      }
      this.labRegistrationDraft = {
        project_id: project.id,
        project_name: project.project_name || '',
        operation_id: candidate.operation_id,
        operation_name: candidate.operation_name,
        default_processing_days: candidate.default_processing_days,
        factory_id: '',
        tooth_positions: '',
        remark: ''
      }
      this.labRegistrationDialogVisible = true
      this.scrollInlineEditorIntoView('labRegistrationEditor')
    },
    applyLabRegistrationDraft() {
      const project = (this.treatmentProjectOptions || []).find(item => String(item.id) === String(this.labRegistrationDraft.project_id || ''))
      const factory = (this.labFactoryOptions || []).find(item => String(item.id) === String(this.labRegistrationDraft.factory_id || ''))
      let currentItem = (this.recordForm.operation_items || []).find(item =>
        String(item.project_id || '') === String(this.labRegistrationDraft.project_id || '')
        && String(item.operation_id || '') === String(this.labRegistrationDraft.operation_id || '')
      )
      if (!currentItem) {
        currentItem = defaultRecordOperationItem()
        this.recordForm.operation_items.push(currentItem)
      }
      currentItem.project_id = project ? project.id : this.labRegistrationDraft.project_id
      currentItem.project_name = project ? project.project_name : this.labRegistrationDraft.project_name
      currentItem.operation_id = this.labRegistrationDraft.operation_id
      currentItem.operation_name = this.labRegistrationDraft.operation_name
      currentItem.need_lab_processing = 1
      currentItem.default_processing_days = Number(this.labRegistrationDraft.default_processing_days || 0)
      currentItem.factory_id = this.labRegistrationDraft.factory_id || ''
      currentItem.factory_name = factory ? factory.name : ''
      currentItem.tooth_positions = this.labRegistrationDraft.tooth_positions || ''
      currentItem.remark = this.labRegistrationDraft.remark || ''
      this.closeLabRegistrationDialog()
      this.refreshRecordTreatmentDraft()
    },
    isRecordSuggestedOperationSelected(relation) {
      return (this.recordForm.operation_items || []).some(item =>
        String(item.project_id || '') === String(this.selectedRecordProjectId || '')
        && String(item.operation_id || '') === String(relation.operation_id || '')
      )
    },
    toggleRecordSuggestedOperation(relation) {
      if (!relation) return
      const currentIndex = (this.recordForm.operation_items || []).findIndex(item =>
        String(item.project_id || '') === String(this.selectedRecordProjectId || '')
        && String(item.operation_id || '') === String(relation.operation_id || '')
      )
      if (currentIndex >= 0) {
        this.removeRecordOperation(currentIndex)
        return
      }
      const project = (this.treatmentProjectOptions || []).find(item => String(item.id) === String(this.selectedRecordProjectId || ''))
      const nextItem = defaultRecordOperationItem()
      nextItem.project_id = project ? project.id : ''
      nextItem.project_name = project ? project.project_name : ''
      nextItem.operation_id = relation.operation_id
      nextItem.operation_name = relation.operation_name
      nextItem.need_lab_processing = relation.need_lab_processing === 1 ? 1 : 0
      nextItem.default_processing_days = Number(relation.default_processing_days || 0)
      this.recordForm.operation_items.push(nextItem)
      this.refreshRecordTreatmentDraft()
    },
    appendRecordOperation() {
      this.recordForm.operation_items.push(defaultRecordOperationItem())
    },
    removeRecordOperation(index) {
      const list = this.recordForm.operation_items || []
      list.splice(index, 1)
      this.refreshRecordTreatmentDraft()
    },
    handleRecordOperationProjectChange(item) {
      const project = (this.treatmentProjectOptions || []).find(projectItem => String(projectItem.id) === String(item.project_id || ''))
      item.project_name = project ? project.project_name : ''
      this.refreshRecordTreatmentDraft(false)
    },
    handleRecordOperationFactoryChange(item) {
      const factory = (this.labFactoryOptions || []).find(factoryItem => String(factoryItem.id) === String(item.factory_id || ''))
      item.factory_name = factory ? factory.name : ''
    },
    handleRecordOperationChange(item) {
      const operation = (this.recordOperationOptions || []).find(operationItem => String(operationItem.id) === String(item.operation_id || ''))
      item.operation_name = operation ? operation.operation_name : ''
      item.need_lab_processing = operation && operation.need_lab_processing === 1 ? 1 : 0
      item.default_processing_days = operation ? Number(operation.default_processing_days || 0) : 0
      if (item.need_lab_processing !== 1) {
        item.factory_id = ''
        item.factory_name = ''
      }
      this.refreshRecordTreatmentDraft()
    },
    normalizeLoadedRecordOperationItems(items) {
      return (Array.isArray(items) ? items : []).map(item => ({
        local_key: `${item.id || 'h5-record-op'}-${Math.random().toString(36).slice(2, 8)}`,
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
    normalizeRecordOperationItems(items, includeRuntimeFields = false) {
      return (Array.isArray(items) ? items : []).map(item => {
        const operation = (this.recordOperationOptions || []).find(operationItem => String(operationItem.id) === String(item.operation_id || ''))
        const project = (this.treatmentProjectOptions || []).find(projectItem => String(projectItem.id) === String(item.project_id || ''))
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
      const operationToothValues = this.normalizeRecordOperationItems(this.recordForm.operation_items)
        .flatMap(item => normalizeToothPositions(item.tooth_positions))
      if (operationToothValues.length) {
        return Array.from(new Set(operationToothValues)).join(',')
      }
      return String(this.recordForm.tooth_positions || '').trim()
    },
    refreshRecordTreatmentDraft(force = false) {
      const nextDraft = buildMedicalRecordTreatmentDraft(this.recordForm.operation_items || [])
      const currentText = String(this.recordForm.treatment || '').trim()
      const canReplace = force || !currentText || currentText === this.recordLastAutoTreatmentDraft || !this.recordTreatmentDraftLocked
      this.recordLastAutoTreatmentDraft = nextDraft
      if (canReplace) {
        this.recordForm.treatment = nextDraft
        this.recordTreatmentDraftLocked = false
      }
    },
    regenerateRecordTreatmentDraft() {
      this.recordTreatmentDraftLocked = false
      this.refreshRecordTreatmentDraft(true)
    },
    handleRecordTreatmentInput() {
      const currentText = String(this.recordForm.treatment || '').trim()
      this.recordTreatmentDraftLocked = currentText && currentText !== this.recordLastAutoTreatmentDraft
    },
    validateAppointmentForm() {
      this.appointmentForm.patient_id = this.patientId
        ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId))
        : (this.appointmentForm.patient_id || null)
      if (!this.appointmentForm.patient_name || !String(this.appointmentForm.patient_name).trim()) return '患者姓名必填'
      if (!this.appointmentForm.appointment_date) return '预约日期必填'
      if (!this.appointmentForm.appointment_time) return '预约时间必填'
      if (!this.appointmentForm.duration_minutes || Number(this.appointmentForm.duration_minutes) <= 0) return '预约时长必填'
      if (!this.appointmentForm.doctor_account_id) {
        this.appointmentForm.doctor_account_id = this.resolveDefaultDoctorAccountId()
      }
      if (!this.appointmentForm.doctor_account_id) return '接诊医生必填'
      if (!this.appointmentForm.appointment_purpose || !String(this.appointmentForm.appointment_purpose).trim()) return '预约项目必填'
      const doctor = this.currentDoctorById(this.appointmentForm.doctor_account_id)
      this.appointmentForm.doctor_name = doctor && doctor.name ? doctor.name : this.resolveDefaultDoctorName(this.appointmentForm.doctor_account_id)
      return ''
    },
    submitAppointment() {
      const message = this.validateAppointmentForm()
      if (message) {
        this.$message.warning(message)
        return
      }
      const payload = Object.assign({}, this.appointmentForm, {
        patient_id: this.appointmentForm.patient_id || (this.patientId ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId)) : null)
      })
      saveAppointment(payload, {
        isEdit: this.appointmentEditing,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success(this.appointmentEditing ? '编辑预约成功' : '新增预约成功')
        }
        this.closeAppointmentDialog()
        this.resetAppointmentForm()
        this.load360()
      }).catch(error => {
        this.$message.error((error && error.message) || (this.appointmentEditing ? '编辑预约失败' : '新增预约失败'))
      })
    },
    editAppointment(item) {
      this.appointmentForm = Object.assign({}, this.appointmentForm, item, {
        patient_id: item && item.patient_id ? item.patient_id : (this.patientId ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId)) : null),
        patient_name: (item && item.patient_name) || ((this.data.patient && this.data.patient.name) || this.patientName || '')
      })
      this.selectedTreatmentCatalogId = null
      this.appointmentEditing = true
      this.appointmentDialog = true
      this.scrollInlineEditorIntoView('appointmentEditor')
    },
    deleteAppointment(id) {
      this.$confirm('确认删除该预约？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/appointments/delete/${id}`).then(() => {
          this.$message.success('删除预约成功')
          this.load360()
        })
      })
    },
    validateRecordForm() {
      this.recordForm.patient_id = this.patientId
        ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId))
        : (this.recordForm.patient_id || null)
      this.recordForm.patient_name = (this.data.patient && this.data.patient.name) || this.patientName || this.recordForm.patient_name || ''
      if (!this.recordForm.patient_id) return '患者ID非法'
      if (!this.recordForm.visit_date) return '就诊日期必填'
      if (!this.recordForm.doctor_account_id) {
        this.recordForm.doctor_account_id = this.resolveDefaultDoctorAccountId()
      }
      if (!this.recordForm.doctor_account_id) return '接诊医生必填'
      const doctor = this.currentDoctorById(this.recordForm.doctor_account_id)
      this.recordForm.doctor_name = doctor && doctor.name ? doctor.name : this.resolveDefaultDoctorName(this.recordForm.doctor_account_id)
      const rawOperationItems = Array.isArray(this.recordForm.operation_items) ? this.recordForm.operation_items : []
      const missingLabFactoryIndex = rawOperationItems.findIndex(item =>
        Number(item && item.need_lab_processing || 0) === 1 && !String(item && item.factory_id || '').trim()
      )
      if (missingLabFactoryIndex >= 0) return `第${missingLabFactoryIndex + 1}条待登记加工操作未选择加工厂`
      if (this.recordForm.treatment && String(this.recordForm.treatment).trim() && !String(this.resolveRecordToothPositions() || '').trim()) {
        return '请选择牙位'
      }
      return ''
    },
    submitRecord() {
      const message = this.validateRecordForm()
      if (message) {
        this.$message.warning(message)
        return
      }
      const payload = Object.assign({}, this.recordForm, {
        patient_id: this.recordForm.patient_id || (this.patientId ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId)) : null),
        patient_name: this.recordForm.patient_name || ((this.data.patient && this.data.patient.name) || this.patientName || ''),
        tooth_positions: this.resolveRecordToothPositions(),
        operation_items: this.normalizeRecordOperationItems(this.recordForm.operation_items, true)
      })
      saveMedicalRecord(payload, {
        isEdit: this.recordEditing,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success(this.recordEditing ? '编辑病历成功' : '新增病历成功')
        }
        this.closeRecordDialog()
        this.resetRecordForm()
        this.load360()
      }).catch(error => {
        this.$message.error((error && error.message) || (this.recordEditing ? '编辑病历失败' : '新增病历失败'))
      })
    },
    editRecord(record) {
      const detailPromise = isLocalEntityId(record.id)
        ? Promise.resolve(Object.assign({}, record))
        : axios.get('/medical-records/selectById', { params: { id: record.id } }).then(res => (res.data && res.data.code === '200' ? (res.data.data || {}) : {}))
      detailPromise.then(async detail => {
        const matchedDoctor = this.currentDoctorById(detail.doctor_account_id) || (this.doctorOptions || []).find(item => item.name === (detail.doctor_name || ''))
        this.recordForm = Object.assign({}, this.recordForm, detail, {
          patient_id: detail && detail.patient_id ? detail.patient_id : (this.patientId ? (isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId)) : null),
          patient_name: (detail && detail.patient_name) || ((this.data.patient && this.data.patient.name) || this.patientName || ''),
          doctor_account_id: matchedDoctor ? matchedDoctor.id : (detail.doctor_account_id || this.resolveDefaultDoctorAccountId()),
          doctor_name: matchedDoctor ? matchedDoctor.name : (detail.doctor_name || this.resolveDefaultDoctorName(detail.doctor_account_id)),
          visit_date: this.formatDate(detail.visit_date),
          tooth_positions: detail.tooth_positions || '',
          operation_items: this.normalizeLoadedRecordOperationItems(detail.operation_items || [])
        })
        this.selectedMedicalRecordTemplateId = null
        this.selectedRecordProjectId = this.recordForm.operation_items.length ? (this.recordForm.operation_items[0].project_id || '') : ''
        this.recordLastAutoTreatmentDraft = buildMedicalRecordTreatmentDraft(this.recordForm.operation_items || [])
        this.recordTreatmentDraftLocked = String(this.recordForm.treatment || '').trim() && String(this.recordForm.treatment || '').trim() !== this.recordLastAutoTreatmentDraft
        this.recordEditing = true
        this.recordDialog = true
        this.scrollInlineEditorIntoView('recordEditor')
        if (this.selectedRecordProjectId) {
          await this.loadRecordProjectDetail(this.selectedRecordProjectId)
        }
      })
    },
    deleteRecord(id) {
      this.$confirm('确认删除该病历？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/medical-records/delete/${id}`).then(() => {
          this.$message.success('删除病历成功')
          this.load360()
        })
      })
    },
    beforeUpload(file) {
      const ok = file.type.startsWith('image/') || file.name.endsWith('.dcm')
      if (!ok) {
        this.$message.error('只支持图片或DICOM文件')
        return false
      }
      return true
    },
    onUploadSuccess(res) {
      if (res.code === '200') {
        this.$message.success('上传成功')
        this.load360()
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    onUploadError() {
      this.$message.error('上传失败')
    },
    previewImage(img) {
      if (!this.isPreviewable(img)) {
        window.open(this.imageUrl(img), '_blank')
        return
      }
      this.previewImageName = img.image_name || '影像资料'
      this.previewImageUrl = this.imageUrl(img)
      this.previewVisible = true
    },
    toggleAppointmentExpand() {
      this.appointmentExpanded = !this.appointmentExpanded
    },
    toggleRecordExpand() {
      this.recordExpanded = !this.recordExpanded
    },
    toggleConsentExpand() {
      this.consentExpanded = !this.consentExpanded
    },
    toggleImageExpand() {
      this.imageExpanded = !this.imageExpanded
    },
    deleteImage(id) {
      this.$confirm('确认删除该影像？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/patient-images/delete/${id}`).then(() => {
          this.$message.success('删除影像成功')
          this.load360()
        })
      })
    },
    sendImageToPatient(img) {
      if (!img || !img.id) {
        return
      }
      axios.post(`/patient-images/send/${img.id}`).then(res => {
        if (res.data.code === '200') {
          this.$message.success(img.sent_to_patient ? '已重新发送到患者端' : '已发送到患者端')
          this.load360()
        } else {
          this.$message.error(res.data.msg || '发送失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '发送失败')
      })
    }
  }
}
</script>

<style scoped>
.staff-h5-page {
  min-height: 100vh;
  background: #f5f7fb;
  padding: 12px;
  box-sizing: border-box;
  overflow-x: hidden;
}
.h5-hero-card,
.h5-section-card {
  background: #fff;
  border-radius: 18px;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(31, 71, 136, 0.08);
  margin-bottom: 12px;
}
.hero-card-mobile {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}
.hero-main {
  min-width: 0;
  flex: 1;
}
.hero-back-btn {
  flex-shrink: 0;
}
.h5-page-kicker {
  color: #409eff;
  font-size: 12px;
  margin-bottom: 8px;
}
.h5-hero-card h2 {
  margin: 0 0 6px;
  font-size: 22px;
  line-height: 1.25;
  color: #1f2d3d;
}
.h5-hero-card p {
  margin: 0 0 10px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}
.h5-summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}
.h5-summary-card {
  background: #fff;
  border-radius: 16px;
  padding: 12px 10px;
  text-align: center;
  box-shadow: 0 8px 20px rgba(31, 71, 136, 0.06);
  min-width: 0;
}
.h5-summary-card.accent { background: #eef6ff; }
.h5-summary-card.success { background: #eefbf3; }
.summary-num {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  word-break: break-word;
}
.summary-label {
  margin-top: 6px;
  font-size: 12px;
  color: #8b95a7;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.info-item {
  background: #f8fbff;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.info-item--full { grid-column: 1 / -1; }
.info-item span { color: #8b95a7; font-size: 12px; }
.info-item strong { color: #303133; font-size: 14px; word-break: break-word; }
.list-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.list-card {
  border-radius: 16px;
  background: #f8fbff;
  padding: 14px;
  min-width: 0;
  height: 100%;
}
.list-card__title-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}
.list-card__title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
}
.record-summary-tip {
  margin-bottom: 10px;
  font-size: 12px;
  color: #ef4444;
}
.inline-editor-card {
  margin-bottom: 14px;
  padding: 14px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid #bfdbfe;
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.08);
}
.inline-editor-card--sub {
  border-color: #fde68a;
  box-shadow: 0 10px 24px rgba(245, 158, 11, 0.08);
}
.inline-editor-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.inline-editor-card__title {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}
.inline-editor-card__tip {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}
.list-card__meta,
.list-card__desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
  margin-top: 6px;
  word-break: break-word;
}
.list-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}
.h5-template-panel {
  margin: 10px 0 16px;
  padding: 12px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #dbeafe;
}
.h5-template-panel__title {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}
.h5-template-panel__tip {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}
.h5-template-panel__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.h5-operation-panel {
  margin: 10px 0 16px;
  padding: 12px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #dbeafe;
}
.h5-operation-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}
.h5-operation-suggestion-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.h5-operation-suggestion-empty {
  margin-top: 10px;
  color: #94a3b8;
  font-size: 12px;
}
.h5-operation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}
.h5-operation-item {
  padding: 12px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #dbeafe;
}
.h5-operation-item__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}
.h5-operation-item__actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.h5-treatment-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  color: #64748b;
  font-size: 12px;
}
.thumb-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.thumb-card {
  border-radius: 16px;
  background: #f8fbff;
  overflow: hidden;
  min-width: 0;
}
.thumb-image-wrap,
.thumb-file-wrap {
  height: 124px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2f7;
}
.thumb-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-file-wrap {
  flex-direction: column;
  gap: 8px;
  color: #8b95a7;
}
.thumb-card__body {
  padding: 12px;
}
.thumb-card__title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  word-break: break-word;
}
.thumb-card__meta {
  margin-top: 6px;
  color: #8b95a7;
  font-size: 12px;
  line-height: 1.6;
  word-break: break-word;
}
.thumb-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}
.consent-preview-box {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.consent-preview-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.consent-preview-item {
  padding: 12px;
  border-radius: 14px;
  background: #f8fbff;
}
.consent-preview-item span {
  display: block;
  color: #8b95a7;
  font-size: 12px;
}
.consent-preview-item strong {
  display: block;
  margin-top: 6px;
  color: #303133;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
}
.consent-preview-content {
  max-height: 300px;
  overflow-y: auto;
  border-radius: 16px;
  border: 1px solid #dbeafe;
  background: #fff;
  padding: 14px;
  color: #334155;
  line-height: 1.9;
  white-space: pre-wrap;
}
.consent-preview-sign {
  padding: 14px;
  border-radius: 16px;
  background: #f8fbff;
  border: 1px solid #dbeafe;
}
.consent-preview-sign__title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
}
.consent-preview-sign__meta {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}
.consent-preview-sign__image {
  width: 100%;
  max-width: 320px;
  margin-top: 10px;
  border-radius: 12px;
  border: 1px solid #dbeafe;
  background: #fff;
}
.h5-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}
.h5-action-trigger {
  position: relative;
  z-index: 1;
}
.h5-actions-grid .el-button,
.h5-action-btn {
  width: 100%;
  margin-left: 0;
}
.h5-upload-row {
  margin-bottom: 12px;
}
.upload-btn {
  width: 100%;
}
.upload-btn ::v-deep .el-upload,
.upload-btn .el-button {
  width: 100%;
}
.more-btn {
  margin-top: 10px;
}
.preview-wrap {
  min-height: 40vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fb;
  border-radius: 16px;
  padding: 12px;
}
.preview-image {
  max-width: 100%;
  max-height: 75vh;
  border-radius: 12px;
}
.loading-box {
  min-height: 40vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 32px;
}
::v-deep .staff-p360-drawer {
  border-radius: 18px 18px 0 0;
}
::v-deep .staff-p360-drawer .el-drawer__header {
  margin-bottom: 0;
  padding: 16px 14px 10px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}
::v-deep .staff-p360-drawer .el-drawer__body {
  padding: 0 14px 16px;
}
::v-deep .el-form-item {
  margin-bottom: 14px;
}
::v-deep .el-form-item__label {
  float: none;
  display: block;
  text-align: left;
  line-height: 1.4;
  padding: 0 0 6px;
}
::v-deep .el-form-item__content {
  margin-left: 0 !important;
}
.drawer-sheet {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 100%;
}
.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 4px;
}
.drawer-actions--wrap {
  flex-wrap: wrap;
}
@media (max-width: 768px) {
  .h5-summary-row,
  .info-grid,
  .thumb-grid,
  .consent-preview-meta,
  .list-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .h5-actions-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
@media (max-width: 420px) {
  .staff-h5-page {
    padding: 10px;
  }
  .hero-card-mobile {
    flex-direction: column;
    align-items: stretch;
  }
  .hero-back-btn {
    width: 100%;
  }
  .h5-hero-card h2 {
    font-size: 20px;
  }
  .summary-num {
    font-size: 18px;
  }
  .inline-editor-card__header {
    flex-direction: column;
    align-items: stretch;
  }
  .list-card__title-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .thumb-image-wrap,
  .thumb-file-wrap {
    height: 112px;
  }
  .drawer-actions {
    display: flex;
    flex-direction: column-reverse;
    gap: 8px;
  }
  .drawer-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 359px) {
  .h5-summary-row,
  .info-grid,
  .thumb-grid,
  .consent-preview-meta,
  .list-cards {
    grid-template-columns: 1fr;
  }
  .h5-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>

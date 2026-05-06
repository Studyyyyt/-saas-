<template>
  <div class="p360-wrap">
    <!-- 顶栏 -->
    <div class="p360-topbar">
      <el-button icon="el-icon-arrow-left" size="small" @click="$router.push('/Patient')">返回列表</el-button>
      <span class="p360-title">患者360视图</span>
      <el-input v-model="patientId" placeholder="患者ID" style="width:110px" type="number" size="small" />
      <el-button type="primary" icon="el-icon-search" size="small" @click="load360">查看</el-button>
    </div>

    <div v-if="loading" class="p360-loading"><i class="el-icon-loading"></i></div>

    <div v-if="data" class="p360-body">
      <!-- 上方：基本信息 + 统计卡片 -->
      <div class="p360-info-row">
        <!-- 基本信息 -->
        <el-card class="p360-card-info">
          <div slot="header" class="card-header">
            <b>基本信息</b>
            <el-button type="primary" plain size="mini" icon="el-icon-edit" @click="openEditPatient">修改资料</el-button>
          </div>
          <el-descriptions :column="3" border size="mini">
            <el-descriptions-item label="姓名">{{ data.patient.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ data.patient.gender }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ data.patient.phone }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ formatAge(data.patient) }}</el-descriptions-item>
            <el-descriptions-item label="患者来源">{{ data.patient.customer_source || '-' }}</el-descriptions-item>
            <el-descriptions-item label="患者关系" :span="3">
              <template v-if="hasRelatedPatient(data.patient)">
                <span>{{ relationTypeLabel(data.patient) }}：</span>
                <el-button type="text" class="relation-link-btn" @click="openRelatedPatient(data.patient)">
                  {{ data.patient.related_patient_name }}
                </el-button>
              </template>
              <template v-else>
                {{ formatPatientRelation(data.patient) }}
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="介绍人" :span="3">
              {{ referralDisplayText }}
            </el-descriptions-item>
          </el-descriptions>
          <div v-if="data.hasArrears" class="arrears-banner">
            <el-tag type="warning" effect="dark">当前欠费</el-tag>
            <span>未收金额 ¥{{ formatMoney(data.arrearsAmount) }}</span>
          </div>
          <div class="wechat-bind-panel">
            <div class="wechat-bind-header">
              <div>
                <div class="wechat-bind-title">公众号绑定</div>
                <div class="wechat-bind-desc">患者扫码关注公众号后自动绑定微信，仅进入患者H5页面，不进入SaaS管理后台</div>
              </div>
              <el-tag :type="wechatBound ? 'success' : 'info'" size="small">{{ wechatBindStatusLabel }}</el-tag>
            </div>
            <div v-if="wechatBound" class="wechat-bind-bound-tip">
              该患者已完成微信公众号绑定，可直接使用患者端 H5 入口。
            </div>
            <div v-else class="wechat-bind-body">
              <div class="wechat-qr-box">
                <img v-if="wechatQrCodeUrl" :src="wechatQrCodeUrl" alt="患者公众号绑定二维码" />
                <div v-else class="wechat-qr-placeholder">二维码生成中</div>
              </div>
              <div class="wechat-bind-actions">
                <div class="wechat-bind-tip">{{ wechatQrTip }}</div>
                <template v-if="wechatBindUrl">
                  <el-button type="primary" size="mini" @click="openWechatBindLink">打开绑定链接</el-button>
                  <el-button size="mini" @click="copyWechatBindLink">复制绑定链接</el-button>
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 统计卡片 -->
        <div class="p360-stats">
          <div class="stat-card">
            <div class="stat-num">{{ data.visitCount }}</div>
            <div class="stat-label">就诊次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-num">{{ data.treatments ? data.treatments.length : 0 }}</div>
            <div class="stat-label">治疗次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-num fee">¥{{ totalFeeDisplay }}</div>
            <div class="stat-label">累计费用</div>
          </div>
          <div class="stat-card">
            <div class="stat-num date">{{ formatDate(data.lastVisit) || '-' }}</div>
            <div class="stat-label">最近就诊</div>
          </div>
          <div class="stat-card">
            <div class="stat-num date">{{ formatDate(data.nextFollowup) || '-' }}</div>
            <div class="stat-label">下次随访</div>
          </div>
        </div>
      </div>

      <!-- 风险标签 -->
      <div class="p360-tags-row">
        <span class="tag-label">风险标签：</span>
        <span v-if="!data.riskTags||!data.riskTags.length" style="color:#999;font-size:12px;">暂无</span>
        <el-tag v-for="tag in data.riskTags" :key="tag.id"
          :type="tag.risk_level===3?'danger':tag.risk_level===2?'warning':''"
          size="small" style="margin:0 4px;" closable @close="deleteTag(tag.id)">
          {{ tag.tag_name }}
        </el-tag>
        <el-tag v-if="data.patientInsight && data.patientInsight.high_value_flag" type="success" size="small" style="margin:0 4px;">高价值客户</el-tag>
        <el-tag v-if="data.patientInsight && data.patientInsight.lost_risk_flag" type="danger" size="small" style="margin:0 4px;">流失风险</el-tag>
        <el-tag v-if="data.patientInsight && data.patientInsight.word_of_mouth_flag" type="warning" size="small" style="margin:0 4px;">口碑客户</el-tag>
        <el-button size="mini" type="text" icon="el-icon-plus" @click="openAddTag">添加标签</el-button>
      </div>

      <!-- 5个 Tab -->
      <el-tabs v-model="activeTab" type="border-card" class="p360-tabs">

        <!-- 病历记录 -->
        <el-tab-pane label="📋 病历记录" name="records">
          <div class="tab-toolbar tab-toolbar--records">
            <el-button type="success" icon="el-icon-plus" size="small" @click="openAddRecord">新增病历</el-button>
            <div class="record-tab-indicator" v-if="pendingLabOperationCount > 0">
              <el-badge is-dot type="danger">
                <span class="record-tab-indicator__label">待登记加工 {{ pendingLabOperationCount }}</span>
              </el-badge>
            </div>
          </div>
          <el-table :data="records" row-key="id" border stripe size="small" class="full-table" default-expand-all>
            <el-table-column type="expand" width="52">
              <template slot-scope="s">
                <div class="record-expand-box">
                  <div class="record-expand-grid">
                    <div class="record-expand-item"><span>就诊日期</span><strong>{{ formatDate(s.row.visit_date) || '-' }}</strong></div>
                    <div class="record-expand-item"><span>接诊医生</span><strong>{{ s.row.doctor_name || '-' }}</strong></div>
                    <div class="record-expand-item"><span>牙位</span><strong>{{ s.row.tooth_positions || '-' }}</strong></div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">操作汇总</div>
                    <div class="record-expand-value">
                      <el-badge v-if="Number(s.row.pending_lab_count || 0) > 0" is-dot type="danger" class="record-expand-badge">
                        <span>{{ s.row.operation_summary || '无' }}</span>
                      </el-badge>
                      <span v-else>{{ s.row.operation_summary || '无' }}</span>
                    </div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">主诉</div>
                    <div class="record-expand-value">{{ s.row.chief_complaint || '无' }}</div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">诊断</div>
                    <div class="record-expand-value">{{ s.row.diagnosis || '无' }}</div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">处置方案</div>
                    <div class="record-expand-value">{{ s.row.treatment || '无' }}</div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">处方</div>
                    <div class="record-expand-value">{{ s.row.prescription || '无' }}</div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">备注</div>
                    <div class="record-expand-value">{{ s.row.notes || '无' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="visit_date" label="就诊日期" width="110">
              <template slot-scope="s">{{ formatDate(s.row.visit_date) }}</template>
            </el-table-column>
            <el-table-column prop="doctor_name" label="接诊医生" width="90" />
            <el-table-column prop="tooth_positions" label="牙位" width="140">
              <template slot-scope="s">{{ s.row.tooth_positions || '-' }}</template>
            </el-table-column>
            <el-table-column prop="chief_complaint" label="主诉" min-width="160">
              <template slot-scope="s"><div class="record-cell-text">{{ s.row.chief_complaint || '-' }}</div></template>
            </el-table-column>
            <el-table-column prop="diagnosis" label="诊断" min-width="180">
              <template slot-scope="s"><div class="record-cell-text">{{ s.row.diagnosis || '-' }}</div></template>
            </el-table-column>
            <el-table-column prop="operation_summary" label="操作汇总" min-width="180">
              <template slot-scope="s">
                <div class="record-cell-text">
                  <el-badge v-if="Number(s.row.pending_lab_count || 0) > 0" is-dot type="danger" class="record-table-badge">
                    <span>{{ s.row.operation_summary || '-' }}</span>
                  </el-badge>
                  <span v-else>{{ s.row.operation_summary || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="treatment" label="处置方案" min-width="220">
              <template slot-scope="s"><div class="record-cell-text">{{ s.row.treatment || '-' }}</div></template>
            </el-table-column>
            <el-table-column label="待登记加工" width="110" align="center">
              <template slot-scope="s">
                <el-badge v-if="Number(s.row.pending_lab_count || 0) > 0" :value="s.row.pending_lab_count" :max="99" type="danger" />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template slot-scope="s">
                <el-button
                  v-if="Number(s.row.pending_lab_count || 0) > 0"
                  size="mini"
                  type="warning"
                  plain
                  @click="openLabOrderForRecord(s.row)"
                >
                  登记加工
                </el-button>
                <el-button size="mini" type="primary" @click="openEditRecord(s.row)">编辑</el-button>
                <el-button size="mini" type="danger" @click="deleteRecord(s.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!records.length" class="empty-tip">暂无病历记录</div>
        </el-tab-pane>

        <!-- 预约信息 -->
        <el-tab-pane label="🗓 预约信息" name="appointments">
          <div class="tab-toolbar">
            <el-button type="primary" icon="el-icon-plus" size="small" @click="openAddAppointment">新增预约</el-button>
          </div>
          <el-table :data="appointments" border stripe size="small" class="full-table">
            <el-table-column prop="appointment_date" label="预约日期" width="110">
              <template slot-scope="s">{{ formatDate(s.row.appointment_date) }}</template>
            </el-table-column>
            <el-table-column prop="appointment_time" label="预约时间" width="100">
              <template slot-scope="s">{{ formatTime(s.row.appointment_time) }}</template>
            </el-table-column>
            <el-table-column label="患者状态" width="120">
              <template slot-scope="s">
                <el-tag v-if="s.row.has_arrears" size="mini" type="warning">欠费 ¥{{ formatMoney(s.row.arrears_amount) }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="doctor_name" label="接诊医生" width="100" />
            <el-table-column prop="appointment_purpose" label="预约项目" min-width="140" show-overflow-tooltip />
            <el-table-column prop="cancel_reason" label="取消原因" min-width="140" show-overflow-tooltip>
              <template slot-scope="s">{{ s.row.cancel_reason || '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template slot-scope="s">
                <el-tag :type="appointmentStatusType(s.row.status)" size="mini">{{ s.row.status || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template slot-scope="s">
                <el-button size="mini" type="primary" @click="openEditAppointment(s.row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!appointments.length" class="empty-tip">暂无预约信息</div>
        </el-tab-pane>

        <!-- 处置收费 -->
        <el-tab-pane label="💰 处置收费" name="billing">
          <div class="tab-toolbar">
            <el-button type="success" icon="el-icon-plus" size="small" @click="openAddTreatment">新增处置</el-button>
          </div>
          <div class="billing-summary">
            <el-row :gutter="16">
              <el-col :span="4">
                <div class="billing-stat">
                  <span class="bs-val">{{ data.treatments ? data.treatments.length : 0 }}</span>
                  <span class="bs-label">治疗次数</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="billing-stat">
                  <span class="bs-val fee">¥{{ totalFeeDisplay }}</span>
                  <span class="bs-label">累计费用</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="billing-stat">
                  <span class="bs-val">{{ paidCount }}</span>
                  <span class="bs-label">已完成治疗</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="billing-stat">
                  <span class="bs-val warn">{{ pendingCount }}</span>
                  <span class="bs-label">待处理</span>
                </div>
              </el-col>
              <el-col :span="4">
                <div class="billing-stat">
                  <span class="bs-val danger">¥{{ formatMoney(data.arrearsAmount) }}</span>
                  <span class="bs-label">未收金额</span>
                </div>
              </el-col>
            </el-row>
          </div>
          <el-table :data="data.treatments || []" border stripe size="small" class="full-table">
            <el-table-column prop="treatment_date" label="治疗日期" width="110" />
            <el-table-column prop="doctor_name" label="医生" width="90" />
            <el-table-column prop="appointment_purpose" label="项目" width="120" show-overflow-tooltip />
            <el-table-column prop="treatment_content" label="治疗详情" min-width="160" show-overflow-tooltip />
            <el-table-column prop="tooth_positions" label="牙位" width="120" show-overflow-tooltip>
              <template slot-scope="s">{{ s.row.tooth_positions || '-' }}</template>
            </el-table-column>
            <el-table-column prop="treatment_product" label="使用材料" min-width="120" show-overflow-tooltip />
            <el-table-column prop="treatment_fee" label="费用(元)" width="100">
              <template slot-scope="s">
                <span style="color:#E6A23C;font-weight:600;">¥{{ s.row.treatment_fee || '0' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template slot-scope="s">
                <el-tag :type="s.row.status==='完成'?'success':s.row.status==='取消'?'danger':'warning'" size="mini">
                  {{ s.row.status || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="billing_status" label="收费状态" width="110">
              <template slot-scope="s">
                <el-tag :type="billingStatusType(s.row.billing_status)" size="mini">{{ s.row.billing_status || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="已收" width="100">
              <template slot-scope="s">¥{{ formatMoney(s.row.charged_amount) }}</template>
            </el-table-column>
            <el-table-column label="已退" width="100">
              <template slot-scope="s">¥{{ formatMoney(s.row.refunded_amount) }}</template>
            </el-table-column>
            <el-table-column label="欠费" width="100">
              <template slot-scope="s">¥{{ formatMoney(s.row.arrears_amount) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template slot-scope="s">
                <el-button v-if="s.row.can_charge" size="mini" type="success" @click="openChargeDialog(s.row)">{{ batchTreatmentCount(s.row) > 1 ? '汇总收费' : '收费' }}</el-button>
                <el-button v-if="s.row.can_refund" size="mini" type="danger" plain @click="openRefundDialog(s.row)">退款</el-button>
                <span v-if="!s.row.can_charge && !s.row.can_refund">-</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!data.treatments||!data.treatments.length" class="empty-tip">暂无治疗收费记录</div>
        </el-tab-pane>

        <!-- 影像管理 -->
        <el-tab-pane label="🖼 影像管理" name="images">
          <div class="tab-toolbar">
            <el-upload
              :action="`/patient-images/upload`"
              :data="uploadExtra"
              :show-file-list="false"
              :on-success="onUploadSuccess"
              :on-error="onUploadError"
              :before-upload="beforeUpload"
              accept="image/*,.dcm"
              style="display:inline-block; margin-right:12px;">
              <el-button type="success" icon="el-icon-upload2" size="small">上传影像</el-button>
            </el-upload>
            <el-select v-model="uploadExtra.imageType" placeholder="影像类型" size="small" style="width:120px;margin-right:8px;">
              <el-option label="X光" value="X光" />
              <el-option label="CT" value="CT" />
              <el-option label="口内照" value="口内照" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-date-picker v-model="uploadExtra.imageDate" type="date" placeholder="拍摄日期"
              value-format="yyyy-MM-dd" size="small" style="width:140px;" />
          </div>

          <div v-if="!data.images||!data.images.length" class="empty-tip">暂无影像记录，点击"上传影像"添加</div>

          <div class="image-grid">
            <div v-for="img in data.images" :key="img.id" class="image-card">
              <div class="image-thumb" @click="previewImage(img)">
                <img v-if="isImage(img)" :src="`/patient-images/file/${img.id}`" :alt="img.image_name" />
                <div v-else class="file-icon"><i class="el-icon-document"></i></div>
              </div>
              <div class="image-meta">
                <div class="image-name" :title="img.image_name">{{ img.image_name }}</div>
                <div class="image-info">
                  <el-tag size="mini" type="info">{{ img.image_type }}</el-tag>
                  <el-tag size="mini" :type="img.sent_to_patient ? 'success' : 'warning'">
                    {{ img.sent_to_patient ? '已发送患者端' : '未发送患者端' }}
                  </el-tag>
                  <span class="image-date">{{ formatDate(img.image_date) }}</span>
                </div>
                <div v-if="img.notes" class="image-notes">{{ img.notes }}</div>
              </div>
              <div class="image-actions">
                <el-button size="mini" type="text" @click="previewImage(img)" icon="el-icon-zoom-in">查看</el-button>
                <el-button
                  size="mini"
                  type="text"
                  :style="{ color: img.sent_to_patient ? '#67C23A' : '#409EFF' }"
                  @click="sendImageToPatient(img)"
                >{{ img.sent_to_patient ? '重新发送' : '发送给患者' }}</el-button>
                <el-button size="mini" type="text" style="color:#F56C6C" @click="deleteImage(img.id)" icon="el-icon-delete">删除</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 回访记录 -->
        <el-tab-pane label="📞 回访记录" name="followup">
          <div class="tab-toolbar">
            <el-button type="success" icon="el-icon-plus" size="small" @click="openAddFollowup">新增回访</el-button>
          </div>
          <el-table :data="data.recentFollowups || []" border stripe size="small" class="full-table">
            <el-table-column prop="followup_date" label="计划回访" width="150">
              <template slot-scope="s">{{ formatDate(s.row.followup_date) }}</template>
            </el-table-column>
            <el-table-column prop="doctor_name" label="负责医生" width="100">
              <template slot-scope="s">{{ s.row.doctor_name || '-' }}</template>
            </el-table-column>
            <el-table-column prop="followup_type" label="方式" width="90" />
            <el-table-column label="状态" width="90">
              <template slot-scope="s">
                <el-tag :type="followupStatusType(s.row)" size="mini">{{ followupStatusLabel(s.row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="summary" label="回访结果" min-width="200" show-overflow-tooltip>
              <template slot-scope="s">{{ s.row.summary || '待填写' }}</template>
            </el-table-column>
            <el-table-column prop="next_followup_date" label="下次回访" width="150">
              <template slot-scope="s">{{ formatDate(s.row.next_followup_date) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="70">
              <template slot-scope="s">
                <el-button size="mini" type="danger" @click="deleteFollowup(s.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!data.recentFollowups||!data.recentFollowups.length" class="empty-tip">暂无随访记录</div>
        </el-tab-pane>

        <el-tab-pane label="📄 知情同意" name="consent">
          <div class="tab-toolbar">
            <el-button type="primary" icon="el-icon-plus" size="small" @click="openConsentDialog">下发知情同意书</el-button>
          </div>
          <el-table :data="data.consents || []" border stripe size="small" class="full-table">
            <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="doctor_name" label="下发医生" width="110" />
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="s">
                <el-tag :type="consentStatusType(s.row.status)" size="mini">{{ s.row.status || '待签署' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="issued_at" label="下发时间" width="160">
              <template slot-scope="s">{{ formatDateTime(s.row.issued_at) || '-' }}</template>
            </el-table-column>
            <el-table-column prop="signed_at" label="签署时间" width="160">
              <template slot-scope="s">{{ formatDateTime(s.row.signed_at) || '-' }}</template>
            </el-table-column>
            <el-table-column prop="signature_name" label="签署人" width="110">
              <template slot-scope="s">{{ s.row.signature_name || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template slot-scope="s">
                <el-button size="mini" type="primary" @click="previewConsent(s.row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!data.consents||!data.consents.length" class="empty-tip">暂无知情同意书</div>
        </el-tab-pane>

        <!-- 时间线 -->
        <el-tab-pane label="📅 时间线" name="timeline">
          <div v-if="!data.timeline||!data.timeline.length" class="empty-tip">暂无事件记录</div>
          <el-timeline v-else style="padding:20px 10px 0;">
            <el-timeline-item v-for="item in data.timeline" :key="item.id"
              :timestamp="formatDate(item.event_time)" :type="typeColor(item.event_type)" placement="top">
              <div class="timeline-content">
                <b>{{ item.event_title }}</b>
                <el-tag size="mini" style="margin-left:8px;">{{ item.event_type }}</el-tag>
                <div v-if="item.event_content" class="timeline-detail">{{ item.event_content }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 图片预览 -->
    <el-dialog :visible.sync="previewVisible" :title="previewImg ? previewImg.image_name : ''" width="80%" top="5vh">
      <div style="text-align:center;" v-if="previewImg">
        <img v-if="isImage(previewImg)" :src="`/patient-images/file/${previewImg.id}`"
          style="max-width:100%;max-height:75vh;object-fit:contain;" />
        <div v-else style="padding:40px;color:#666;">
          <i class="el-icon-document" style="font-size:60px;"></i>
          <p>{{ previewImg.image_name }}</p>
          <el-button type="primary" @click="downloadFile(previewImg)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="修改患者基本资料" :visible.sync="patientEditDialog" width="560px" append-to-body>
      <el-form :model="patientEditForm" label-width="100px">
        <el-form-item>
          <span slot="label" class="required-label">患者姓名</span>
          <el-input v-model="patientEditForm.name" />
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">患者性别</span>
          <el-select v-model="patientEditForm.gender" placeholder="请选择性别" style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">年龄</span>
          <el-input-number v-model="patientEditForm.age" :min="0" :max="150" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">手机号码</span>
          <el-input v-model="patientEditForm.phone" maxlength="11" placeholder="请输入11位手机号码" />
        </el-form-item>
        <el-form-item>
          <span slot="label" class="required-label">患者来源</span>
          <el-select v-model="patientEditForm.customer_source" placeholder="请选择患者来源" style="width:100%">
            <el-option v-for="item in patientSourceOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="介绍人（可选）">
          <ReferralSelector
            :value="patientReferralForm"
            :current-patient-id="patientEditForm.id || null"
            @input="handlePatientReferralChange"
          />
        </el-form-item>
        <el-form-item label="患者关系">
          <el-select v-model="patientEditForm.relation_type" placeholder="可选：介绍人/家属/朋友等" style="width:100%" clearable>
            <el-option v-for="item in patientRelationOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联患者（可选）">
          <div class="patient-suggest-wrap" @click.stop>
            <el-input
              v-model="patientEditForm.related_patient_name"
              placeholder="可选：输入关联患者姓名，支持模糊搜索"
              clearable
              @input="handleRelatedPatientInput"
              @focus="handleRelatedPatientFocus"
              @blur="handleRelatedPatientBlur"
            />
            <div v-if="relationSuggestionVisible && relatedPatientSuggestions.length" class="patient-suggestion-panel">
              <div
                v-for="patient in relatedPatientSuggestions"
                :key="`patient360-related-${patient.id}`"
                class="patient-suggestion-item"
                @mousedown.prevent="selectRelatedPatientSuggestion(patient)"
              >
                <div class="patient-suggestion-name">{{ patient.name }}</div>
                <div class="patient-suggestion-meta">ID {{ patient.id }}<span v-if="patient.phone"> · {{ patient.phone }}</span></div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="closePatientEditDialog">取消</el-button>
        <el-button type="primary" @click="submitPatientEdit">保存</el-button>
      </span>
    </el-dialog>

    <!-- 新增/编辑病历 -->
    <el-dialog
      :title="recordDialogTitle"
      :visible.sync="recordDialog"
      width="1360px"
      top="3vh"
      custom-class="record-workbench-dialog"
      :close-on-click-modal="false">
      <el-form :model="recordForm" ref="recordForm" class="editor-form" @submit.native.prevent>
        <el-card class="editor-head-card" shadow="never">
          <div class="editor-head">
            <div>
              <div class="page-kicker">患者360内病历工作台</div>
              <h2>{{ recordDialogTitle }}</h2>
              <p>{{ currentRecordPatientSummary }}</p>
            </div>
            <div class="editor-head__actions">
              <el-tag size="small" effect="plain">{{ recordForm.record_type || '初诊' }}</el-tag>
              <el-tag size="small" :type="recordStatusTagType(recordForm.record_status)">{{ recordStatusLabel(recordForm.record_status) }}</el-tag>
            </div>
          </div>

          <div class="editor-toolbar">
            <el-form-item label="患者ID" class="toolbar-item toolbar-item--sm">
              <el-input :value="recordForm.patient_id" disabled />
            </el-form-item>
            <el-form-item label="患者姓名" class="toolbar-item toolbar-item--sm">
              <el-input :value="recordForm.patient_name" disabled />
            </el-form-item>
            <el-form-item label="接诊医生" class="toolbar-item toolbar-item--sm">
              <el-select v-model="recordForm.doctor_account_id" filterable placeholder="请选择接诊医生" style="width:100%">
                <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="护士" class="toolbar-item toolbar-item--xs">
              <el-input v-model="recordForm.nurse_name" placeholder="可选" />
            </el-form-item>
            <el-form-item label="助理" class="toolbar-item toolbar-item--xs">
              <el-input v-model="recordForm.assistant_name" placeholder="可选" />
            </el-form-item>
            <el-form-item label="就诊时间" class="toolbar-item toolbar-item--md">
              <el-date-picker
                v-model="recordForm.visit_date"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择就诊时间"
                style="width:100%"
              />
            </el-form-item>
            <el-form-item label="病历类型" class="toolbar-item toolbar-item--type">
              <el-radio-group v-model="recordForm.record_type" size="small">
                <el-radio-button label="初诊" />
                <el-radio-button label="复诊" />
              </el-radio-group>
            </el-form-item>
            <div class="toolbar-switch">
              <span>牙位同步</span>
              <el-switch v-model="recordEditorFlags.autoSyncToothPositions" />
            </div>
          </div>
        </el-card>

        <div class="editor-layout">
          <el-card class="template-card" shadow="never">
            <div class="panel-head">
              <div>
                <div class="panel-title">病历模板库</div>
                <div class="panel-tip">在患者360里直接使用工作台模板，点击后立即回填右侧病历。</div>
              </div>
              <el-tag size="small" effect="plain">{{ medicalRecordTemplateOptions.length }} 个模板</el-tag>
            </div>

            <el-input
              v-model="recordTemplateKeyword"
              clearable
              placeholder="搜索模板名称 / 诊断"
              prefix-icon="el-icon-search"
              class="template-search"
            />

            <div class="template-category-bar">
              <span>当前分类</span>
              <el-select v-model="activeRecordTemplateCategory" placeholder="选择分类" size="small" style="width:150px">
                <el-option v-for="category in recordTemplateCategories" :key="category" :label="category" :value="category" />
              </el-select>
            </div>

            <div class="template-tree-wrap">
              <el-tree
                :data="recordTemplateTreeData"
                node-key="nodeKey"
                default-expand-all
                :expand-on-click-node="false"
                @node-click="handleRecordTemplateNodeClick"
              >
                <span slot-scope="{ data }" class="template-tree-node">
                  <span class="template-tree-node__label">
                    <i :class="data.isTemplate ? 'el-icon-document' : 'el-icon-folder-opened'" />
                    <span>{{ data.label }}</span>
                  </span>
                  <span v-if="!data.isTemplate" class="template-tree-node__count">{{ data.children.length }}</span>
                </span>
              </el-tree>
              <el-empty v-if="!recordTemplateTreeData.length" description="暂无匹配模板"></el-empty>
            </div>

            <div class="template-actions">
              <el-button type="success" plain @click="saveCurrentRecordAsTemplate">保存为模板</el-button>
              <el-button plain :disabled="!selectedMedicalRecordTemplateId" @click="deleteSelectedMedicalRecordTemplate">删除模板</el-button>
            </div>

            <div class="template-preview-card">
              <div class="template-preview-card__title">模板预览</div>
              <template v-if="selectedRecordTemplatePreview">
                <div class="template-preview-card__name">{{ selectedRecordTemplatePreview.template_name }}</div>
                <div class="template-preview-card__meta">
                  {{ selectedRecordTemplatePreview.template_category || '常用模板' }} · {{ selectedRecordTemplatePreview.record_type || '初诊' }}
                </div>
                <div v-for="section in recordTemplatePreviewSections" :key="section.key" class="template-preview-row">
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
                  <div class="panel-tip">工作台直接内嵌在患者360里，保存后会立即回到当前患者病历列表。</div>
                </div>
                <div class="sheet-head-tags">
                  <el-tag size="small" effect="plain">{{ recordForm.record_type || '初诊' }}</el-tag>
                  <el-tag size="small" :type="recordStatusTagType(recordForm.record_status)">{{ recordStatusLabel(recordForm.record_status) }}</el-tag>
                </div>
              </div>

              <div class="record-sheet">
                <div class="sheet-row">
                  <div class="sheet-label sheet-label--required">主诉</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.chief_complaint" type="textarea" :rows="2" placeholder="请输入主诉" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">现病史</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.present_illness_history" type="textarea" :rows="2" placeholder="请输入现病史" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">既往史</div>
                  <div class="sheet-cell sheet-cell--split">
                    <div class="sheet-cell__main">
                      <el-input v-model="recordForm.past_history" type="textarea" :rows="2" placeholder="请输入既往史" />
                    </div>
                    <div class="sheet-cell__side">
                      <div class="sheet-side-title">一般情况</div>
                      <el-input v-model="recordForm.general_condition" placeholder="例如：体健" />
                    </div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">流行病史</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.infectious_history" type="textarea" :rows="2" placeholder="可选" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">过敏史</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.allergy_history" type="textarea" :rows="2" placeholder="可选" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">检查</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.examination" type="textarea" :rows="3" placeholder="请输入检查内容" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">辅助检查</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.auxiliary_examination" type="textarea" :rows="2" placeholder="请输入辅助检查内容" />
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
                        <el-button size="mini" plain @click="appendRecordOperation" :disabled="!recordOperationOptions.length">新增空白操作</el-button>
                      </div>

                      <el-row :gutter="12" class="project-suggestion-row">
                        <el-col :span="10">
                          <el-select
                            v-model="selectedQuickRecordOperationId"
                            clearable
                            filterable
                            placeholder="从操作字典库选择后立即添加"
                            style="width:100%"
                            @change="handleQuickRecordOperationSelect">
                            <el-option
                              v-for="operation in recordOperationOptions"
                              :key="operation.id"
                              :label="recordOperationOptionLabel(operation)"
                              :value="operation.id" />
                          </el-select>
                        </el-col>
                        <el-col :span="14">
                          <div class="operation-suggestion-empty">
                            操作来源已切换为操作字典库；如需补充收费归属，可在下方为每条操作单独关联项目。
                          </div>
                        </el-col>
                      </el-row>

                      <div v-if="recordForm.operation_items && recordForm.operation_items.length" class="operation-list">
                        <div v-for="(item, index) in recordForm.operation_items" :key="item.local_key" class="operation-item">
                          <div class="operation-item__head">
                            <span>操作 {{ index + 1 }}</span>
                            <div class="operation-item__actions">
                              <el-tag v-if="item.need_lab_processing === 1" size="mini" type="danger">待登记加工</el-tag>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="removeRecordOperation(index)">删除</el-button>
                            </div>
                          </div>
                          <el-row :gutter="12">
                            <el-col :span="8">
                              <el-form-item label="关联项目" label-width="82px">
                                <el-select v-model="item.project_id" clearable filterable placeholder="可选" style="width:100%" @change="handleRecordOperationProjectChange(item)">
                                  <el-option v-for="project in treatmentProjectOptions" :key="project.id" :label="project.project_name" :value="project.id" />
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col :span="8">
                              <el-form-item label="操作字典" label-width="82px" required>
                                <el-select v-model="item.operation_id" clearable filterable placeholder="必填：选择操作字典项" style="width:100%" @change="handleRecordOperationChange(item)">
                                  <el-option v-for="operation in recordOperationOptions" :key="operation.id" :label="recordOperationOptionLabel(operation)" :value="operation.id" />
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
                              placeholder="必选：不选则不会生成加工订单"
                              @change="handleRecordOperationFactoryChange(item)"
                            >
                              <el-option v-for="factory in labFactoryOptions" :key="factory.id" :label="factory.name" :value="factory.id" />
                            </el-select>
                          </el-form-item>
                          <el-form-item label="牙位" label-width="82px">
                            <ToothSelector v-model="item.tooth_positions" @input="handleRecordOperationToothChange" />
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
                    <el-input v-model="recordForm.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断结论" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">治疗方案</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.treatment_plan" type="textarea" :rows="2" placeholder="请输入治疗方案" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">治疗文稿</div>
                  <div class="sheet-cell">
                    <div class="treatment-draft-toolbar">
                      <span class="treatment-draft-hint">勾选操作后自动生成初稿，医生可继续润色。</span>
                      <el-button size="mini" type="text" @click="regenerateRecordTreatmentDraft" :disabled="!(recordForm.operation_items || []).length">重新生成</el-button>
                    </div>
                    <el-input v-model="recordForm.treatment" type="textarea" :rows="3" placeholder="治疗文稿" @input="handleRecordTreatmentInput" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">牙位</div>
                  <div class="sheet-cell">
                    <ToothSelector v-model="recordForm.tooth_positions" />
                    <div class="sheet-field-hint">开启牙位同步时，会优先使用“本次操作”里的牙位集合。</div>
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">医嘱</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.medical_advice" type="textarea" :rows="2" placeholder="请输入医嘱" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">处方</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.prescription" type="textarea" :rows="3" placeholder="请输入处方" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">病历标签</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.record_tags" placeholder="多个标签请用逗号分隔，例如：牙周, 急诊" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">影像摘要</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.image_summary" type="textarea" :rows="2" placeholder="可补充记录本次影像重点" />
                  </div>
                </div>

                <div class="sheet-row">
                  <div class="sheet-label">病历备注</div>
                  <div class="sheet-cell">
                    <el-input v-model="recordForm.notes" type="textarea" :rows="3" placeholder="补充备注、复诊提醒等" />
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-form>
      <span slot="footer">
        <el-button type="success" plain @click="saveCurrentRecordAsTemplate">保存为模板</el-button>
        <el-button @click="recordDialog=false">取消</el-button>
        <el-button plain @click="submitRecord('draft')">暂存</el-button>
        <el-button type="primary" @click="submitRecord('final')">{{ recordDialogTitle === '编辑病历' ? '保存修改' : '保存病历' }}</el-button>
      </span>
    </el-dialog>

    <!-- 新增回访 -->
    <el-dialog title="新增回访计划" :visible.sync="followupDialog" width="520px">
      <el-form :model="followupForm" label-width="90px">
        <el-form-item label="负责医生">
          <el-select v-model="followupForm.doctor_account_id" placeholder="请选择负责医生" style="width:100%">
            <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划回访">
          <el-date-picker v-model="followupForm.followup_date" type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="回访方式">
          <el-select v-model="followupForm.followup_type" style="width:100%">
            <el-option label="电话" value="电话" /><el-option label="复诊" value="复诊" /><el-option label="线上" value="线上" />
          </el-select>
        </el-form-item>
        <el-form-item label="回访结果"><el-input v-model="followupForm.summary" type="textarea" :rows="3" placeholder="回访后填写结果；留空则显示为待回访" /></el-form-item>
        <el-form-item label="下次回访">
          <el-date-picker v-model="followupForm.next_followup_date" type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="followupDialog=false">取消</el-button>
        <el-button type="primary" @click="submitFollowup">确定</el-button>
      </span>
    </el-dialog>

    <!-- 新增/编辑预约 -->
    <el-dialog :title="appointmentDialogTitle" :visible.sync="appointmentDialog" width="560px">
      <el-form :model="appointmentForm" label-width="90px">
        <el-form-item label="预约日期">
          <el-date-picker v-model="appointmentForm.appointment_date" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
        <el-form-item label="预约时间">
          <el-time-picker v-model="appointmentForm.appointment_time" value-format="HH:mm:ss" format="HH:mm" style="width:100%" />
        </el-form-item>
        <el-form-item label="接诊医生">
          <el-select v-model="appointmentForm.doctor_account_id" placeholder="请选择接诊医生" style="width:100%">
            <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约项目">
          <el-input v-model="appointmentForm.appointment_purpose" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="appointmentForm.duration_minutes" :min="15" :step="15" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="appointmentForm.status" style="width:100%">
            <el-option label="待治疗" value="待治疗" />
            <el-option label="已预约" value="已预约" />
            <el-option label="待就诊" value="待就诊" />
            <el-option label="已就诊" value="已就诊" />
            <el-option label="已离开" value="已离开" />
            <el-option label="已改约" value="已改约" />
            <el-option label="已治疗" value="已治疗" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="appointmentDialog=false">取消</el-button>
        <el-button type="primary" @click="submitAppointment">{{ appointmentEditing ? '保存' : '确定' }}</el-button>
      </span>
    </el-dialog>

    <!-- 新增处置 -->
    <el-dialog title="新增处置记录" :visible.sync="treatmentDialog" width="920px" class="treatment-batch-dialog">
      <el-form :model="treatmentForm" label-width="90px" class="treatment-batch-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="默认医生">
              <el-select v-model="treatmentForm.doctor_account_id" placeholder="请选择默认医生" style="width:100%" @change="handleTreatmentDefaultDoctorChange">
                <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="treatmentForm.status" style="width:100%">
                <el-option label="进行中" value="进行中" />
                <el-option label="完成" value="完成" />
                <el-option label="取消" value="取消" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="治疗日期">
              <el-date-picker v-model="treatmentForm.treatment_date" type="date"
                value-format="yyyy-MM-dd" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源病历">
              <el-select v-model="treatmentForm.medical_record_id" clearable placeholder="默认最近病历，可手动切换" style="width:100%">
                <el-option
                  v-for="record in availableTreatmentMedicalRecords"
                  :key="record.id"
                  :label="treatmentMedicalRecordOptionLabel(record)"
                  :value="record.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="treatment-batch-list">
        <div class="treatment-batch-list__head">
          <div>
            <div class="treatment-batch-list__title">处置明细</div>
            <div class="treatment-batch-list__tip">支持同一颗牙新增多条不同处置，也支持一条处置覆盖多颗牙位。</div>
          </div>
          <el-button type="success" plain icon="el-icon-plus" @click="addTreatmentItem">新增一条处置</el-button>
        </div>

        <div v-for="(item, index) in treatmentForm.items || []" :key="`treatment-item-${index}`" class="treatment-item-card">
          <div class="treatment-item-card__head">
            <span>处置 {{ index + 1 }}</span>
            <el-button v-if="(treatmentForm.items || []).length > 1" type="text" style="color:#F56C6C" @click="removeTreatmentItem(index)">删除</el-button>
          </div>
          <el-form :model="item" label-width="82px" size="small">
            <el-form-item label="处置医生">
              <el-select v-model="item.doctor_account_id" placeholder="请选择处置医生" style="width:100%" @change="handleTreatmentItemDoctorChange(item)">
                <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="项目库">
              <el-select v-model="item.project_id" placeholder="可选：从项目库带出" clearable filterable default-first-option style="width:100%" @change="applyTreatmentCatalogToItem(item)">
                <el-option
                  v-for="project in treatmentProjectOptions"
                  :key="project.id"
                  :label="treatmentProjectOptionLabel(project)"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="已选项目">
              <el-input v-model="item.appointment_purpose" placeholder="可手工输入项目名称；选择项目库后会自动带出" />
            </el-form-item>
            <el-form-item label="费用(元)">
              <el-input-number v-model="item.treatment_fee" :min="0" :precision="2" :step="0.01" controls-position="right" style="width:100%" @change="handleTreatmentPriceChange" />
            </el-form-item>
            <el-form-item label="治疗详情"><el-input v-model="item.treatment_content" type="textarea" :rows="2" /></el-form-item>
            <el-form-item label="牙位"><ToothSelector v-model="item.tooth_positions" /></el-form-item>
          </el-form>
        </div>
      </div>

      <span slot="footer" class="treatment-batch-footer">
        <div class="treatment-price-summary">
          <div class="treatment-price-summary__line">
            <span>汇总价格</span>
            <strong>¥{{ formatMoney(treatmentOriginalTotal) }}</strong>
          </div>
          <div class="treatment-price-summary__line">
            <span>折扣率</span>
            <div class="treatment-price-summary__input">
              <el-input-number v-model="treatmentForm.discount_rate" :min="0" :max="100" :precision="2" :step="0.01" controls-position="right" size="small" @change="syncTreatmentSummaryByRate" />
              <em>%</em>
            </div>
          </div>
          <div class="treatment-price-summary__line">
            <span>折后价格</span>
            <div class="treatment-price-summary__input">
              <el-input-number v-model="treatmentForm.discounted_total_fee" :min="0" :max="treatmentOriginalTotal" :precision="2" :step="0.01" controls-position="right" size="small" @change="syncTreatmentSummaryByDiscounted" />
            </div>
          </div>
          <div class="treatment-price-summary__tip">保存后会按折后价格同比例分摊到每条处置记录。</div>
        </div>
        <div class="treatment-batch-footer__actions">
          <el-button @click="treatmentDialog=false">取消</el-button>
          <el-button type="primary" @click="submitTreatment">确定</el-button>
        </div>
      </span>
    </el-dialog>

    <el-dialog :title="chargeDialogTitle" :visible.sync="chargeDialog" width="560px">
      <el-form :model="chargeForm" label-width="90px">
        <el-form-item label="收费说明">
          <div class="charge-summary-box">
            <div class="charge-summary-box__title">{{ chargeSummaryTitle }}</div>
            <div class="charge-summary-box__desc">{{ chargeSummaryDescription }}</div>
          </div>
        </el-form-item>
        <el-form-item label="收费金额">
          <el-input-number v-model="chargeForm.amount" :min="0.01" :precision="2" :step="0.01" controls-position="right" style="width:100%" :disabled="isBatchChargeMode" />
        </el-form-item>
        <el-form-item label="收费日期">
          <el-date-picker v-model="chargeForm.date" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
        <el-form-item label="收款渠道">
          <div class="charge-channel-list">
            <div v-for="(split, index) in chargeForm.channel_splits || []" :key="`charge-split-${index}`" class="charge-channel-row">
              <el-select v-model="split.payment_channel_id" placeholder="请选择渠道" style="flex:1">
                <el-option
                  v-for="channel in paymentChannelOptions"
                  :key="channel.id"
                  :label="channel.channel_name"
                  :value="channel.id"
                />
              </el-select>
              <el-input-number v-model="split.amount" :min="0.01" :precision="2" :step="0.01" controls-position="right" style="width:150px" />
              <el-button v-if="(chargeForm.channel_splits || []).length > 1" type="text" style="color:#F56C6C" @click="removeChargeChannelSplit(index)">删除</el-button>
            </div>
            <div class="charge-channel-footer">
              <el-button type="text" @click="addChargeChannelSplit">新增渠道</el-button>
              <span>渠道合计 ¥{{ chargeChannelSplitTotal }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="chargeForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="chargeDialog=false">取消</el-button>
        <el-button type="primary" @click="submitCharge">确认收费</el-button>
      </span>
    </el-dialog>

    <el-dialog title="治疗退款" :visible.sync="refundDialog" width="420px">
      <el-form :model="refundForm" label-width="90px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0.01" :precision="2" :step="0.01" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-form-item label="退款日期">
          <el-date-picker v-model="refundForm.date" type="date" value-format="yyyy-MM-dd" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="refundForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="refundDialog=false">取消</el-button>
        <el-button type="primary" @click="submitRefund">确认退款</el-button>
      </span>
    </el-dialog>

    <!-- 添加风险标签 -->
    <el-dialog title="添加风险标签" :visible.sync="tagDialog" width="420px">
      <el-form :model="tagForm" label-width="90px">
        <el-form-item label="标签名称"><el-input v-model="tagForm.tag_name" /></el-form-item>
        <el-form-item label="标签编码"><el-input v-model="tagForm.tag_code" placeholder="如：high-risk" /></el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="tagForm.risk_level" style="width:100%">
            <el-option label="低风险" :value="1" /><el-option label="中风险" :value="2" /><el-option label="高风险" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="tagForm.note" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="tagDialog=false">取消</el-button>
        <el-button type="primary" @click="submitTag">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="下发电子知情同意书" :visible.sync="consentDialog" width="620px">
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
          <el-input v-model="consentForm.title" placeholder="如：口腔治疗知情同意书" />
        </el-form-item>
        <el-form-item label="正文内容">
          <el-input v-model="consentForm.content" type="textarea" :rows="12" placeholder="请输入需要患者阅读并签字确认的内容" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="consentDialog=false">取消</el-button>
        <el-button type="primary" @click="submitConsent">下发</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="consentPreview.title || '电子知情同意书'" :visible.sync="consentPreviewDialog" width="720px">
      <div v-if="consentPreview.id" class="consent-preview-box">
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
      <span slot="footer">
        <el-button @click="consentPreviewDialog=false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import ReferralSelector from '@/components/ReferralSelector.vue'
import ToothSelector from '@/components/ToothSelector.vue'
import { getAdminSession } from '@/utils/adminSession'
import { CUSTOMER_SOURCE_OPTIONS } from '@/utils/consultationOptions'
import { getPatientAge, rememberRecentPatient } from '@/utils/patientList'
import { buildMedicalRecordTreatmentDraft, normalizeToothPositions } from '@/utils/medicalRecordOperationDraft'
import { augmentCachedData } from '@/utils/offline/cache'
import { fetchCachedResource, saveAppointment, saveMedicalRecord, savePatient } from '@/utils/offline/apiClient'
import { isLocalEntityId, OFFLINE_ID_MAP_EVENT, resolveMappedServerId } from '@/utils/offline/queue'

const DEFAULT_TEMPLATE_CATEGORY = '常用模板'
const PATIENT_PHONE_REGEX = /^\d{11}$/
const PATIENT_RELATION_OPTIONS = ['介绍人', '家属', '夫妻', '父母子女', '兄弟姐妹', '朋友', '同事', '其他']

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

function splitRecordTags(value) {
  return Array.from(new Set(
    String(value || '')
      .split(/[，,、/\s]+/)
      .map(item => item.trim())
      .filter(Boolean)
  ))
}

function createEmptyPatientEditItem() {
  return {
    id: null,
    name: '',
    gender: '',
    age: null,
    phone: '',
    customer_source: '',
    relation_type: '',
    related_patient_id: null,
    related_patient_name: ''
  }
}

function createEmptyReferralState() {
  return {
    referrer_type: '',
    referrer_patient_id: null,
    referrer_patient_name: '',
    external_referrer_type: '',
    external_referrer_name: '',
    external_referrer_contact: '',
    referral_remark: ''
  }
}

function normalizeText(value) {
  return String(value || '').trim()
}

export default {
  name: 'Patient360View',
  components: { ToothSelector, ReferralSelector },
  data() {
    return {
      patientId: '',
      data: null,
      records: [],
      loading: false,
      activeTab: 'records',
      patientEditDialog: false,
      patientEditForm: createEmptyPatientEditItem(),
      patientReferralForm: createEmptyReferralState(),
      patientSourceOptions: CUSTOMER_SOURCE_OPTIONS,
      patientRelationOptions: PATIENT_RELATION_OPTIONS,
      relatedPatientSuggestions: [],
      relationSuggestionVisible: false,
      relationSuggestionBlurTimer: null,
      recordDialog: false,
      recordDialogTitle: '新增病历',
      recordForm: {},
      appointmentDialog: false,
      appointmentEditing: false,
      appointmentForm: {},
      followupDialog: false,
      followupForm: {},
      tagDialog: false,
      tagForm: {},
      previewVisible: false,
      previewImg: null,
      uploadExtra: { imageType: 'X光', imageDate: '', patientId: '', patientName: '' },
      treatmentDialog: false,
      treatmentForm: {},
      treatmentPricingSource: 'rate',
      chargeDialog: false,
      chargeForm: { amount: null, date: '', remark: '', channel_splits: [] },
      refundDialog: false,
      refundForm: { amount: null, date: '', remark: '' },
      billingTreatment: null,
      chargeBatchTreatments: [],
      consentDialog: false,
      consentPreviewDialog: false,
      consentForm: {},
      consentTemplateOptions: [],
      selectedConsentTemplateId: null,
      medicalRecordTemplateOptions: [],
      selectedMedicalRecordTemplateId: null,
      consentPreview: {},
      treatmentProjectOptions: [],
      recordOperationOptions: [],
      labFactoryOptions: [],
      recordProjectDetailCache: {},
      selectedRecordProjectId: '',
      selectedQuickRecordOperationId: '',
      recordLastAutoTreatmentDraft: '',
      recordTreatmentDraftLocked: false,
      recordTemplateKeyword: '',
      activeRecordTemplateCategory: DEFAULT_TEMPLATE_CATEGORY,
      recordEditorFlags: {
        autoSyncToothPositions: true
      },
      paymentChannelOptions: [],
      doctors: [],
      currentUser: getAdminSession() || {}
    }
  },
  computed: {
    appointmentDialogTitle() {
      return this.appointmentEditing ? '编辑预约' : '新增预约'
    },
    chargeDialogTitle() {
      return this.isBatchChargeMode ? '批量治疗收费' : '治疗收费'
    },
    totalFeeDisplay() {
      if (!this.data || !this.data.totalFee) return '0'
      return this.data.totalFee.toFixed(2)
    },
    referralDisplayText() {
      const patient = this.data && this.data.patient ? this.data.patient : {}
      const referralRecord = this.data && this.data.referralRecord ? this.data.referralRecord : null
      const internalName = String((patient && patient.referrer_patient_name) || (referralRecord && referralRecord.referrer_patient_name) || '').trim()
      if (internalName) return internalName
      const externalName = String((patient && patient.external_referrer_name) || (referralRecord && referralRecord.external_referrer_name) || '').trim()
      const externalType = String((patient && patient.external_referrer_type) || (referralRecord && referralRecord.external_referrer_type) || '').trim()
      if (externalName && externalType) return `${externalName}（${externalType}）`
      if (externalName) return externalName
      return '未填写'
    },
    paidCount() {
      if (!this.data || !this.data.treatments) return 0
      return this.data.treatments.filter(t => t.status === '完成').length
    },
    pendingCount() {
      if (!this.data || !this.data.treatments) return 0
      return this.data.treatments.filter(t => t.status !== '完成' && t.status !== '取消').length
    },
    pendingLabOperationCount() {
      if (this.data && Number(this.data.pendingLabOperationCount || 0) > 0) {
        return Number(this.data.pendingLabOperationCount || 0)
      }
      return (this.records || []).reduce((sum, item) => sum + Number(item.pending_lab_count || 0), 0)
    },
    selectedRecordProjectOperations() {
      const detail = this.recordProjectDetailCache[String(this.selectedRecordProjectId || '')]
      return detail && Array.isArray(detail.operation_relations) ? detail.operation_relations : []
    },
    recordTemplateCategories() {
      const categories = Array.from(new Set(
        (this.medicalRecordTemplateOptions || [])
          .map(item => String(item.template_category || '').trim() || DEFAULT_TEMPLATE_CATEGORY)
      ))
      return categories.length ? categories : [DEFAULT_TEMPLATE_CATEGORY]
    },
    recordTemplateTreeData() {
      const keyword = String(this.recordTemplateKeyword || '').trim().toLowerCase()
      const grouped = this.recordTemplateCategories.map(category => ({
        label: category,
        nodeKey: `record-category-${category}`,
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
            nodeKey: `record-category-${category}`,
            isTemplate: false,
            children: []
          }
        }
        groupMap[category].children.push({
          label: item.template_name || '未命名模板',
          nodeKey: `record-template-${item.id}`,
          isTemplate: true,
          id: item.id,
          category,
          template: item
        })
      })
      return Object.values(groupMap).filter(item => item.children.length)
    },
    selectedRecordTemplatePreview() {
      return (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedMedicalRecordTemplateId || 0)) || null
    },
    recordTemplatePreviewSections() {
      const template = this.selectedRecordTemplatePreview
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
    currentRecordPatientSummary() {
      const patientId = String(this.recordForm && this.recordForm.patient_id || '').trim()
      const patientName = String(this.recordForm && this.recordForm.patient_name || '').trim()
      const visitDate = this.formatDateTime(this.recordForm && this.recordForm.visit_date)
      if (!patientId && !patientName) {
        return '当前患者信息加载后即可直接在此完成病历工作台录入。'
      }
      return [`患者ID ${patientId || '-'}`, patientName || '未填写姓名', visitDate || '未设置就诊时间'].join(' · ')
    },
    treatmentOriginalTotal() {
      const items = this.treatmentForm && Array.isArray(this.treatmentForm.items) ? this.treatmentForm.items : []
      const total = items.reduce((sum, item) => {
        const amount = Number(item && item.treatment_fee)
        return Number.isFinite(amount) ? sum + amount : sum
      }, 0)
      return Math.round(total * 100) / 100
    },
    treatmentBatchMap() {
      const map = {}
      const treatments = this.data && Array.isArray(this.data.treatments) ? this.data.treatments : []
      treatments.forEach(item => {
        const batchNo = String(item && item.batch_no ? item.batch_no : '').trim()
        if (!batchNo) return
        if (!map[batchNo]) {
          map[batchNo] = []
        }
        map[batchNo].push(item)
      })
      return map
    },
    isBatchChargeMode() {
      return Array.isArray(this.chargeBatchTreatments) && this.chargeBatchTreatments.length > 1
    },
    chargeBatchNo() {
      if (!this.isBatchChargeMode) return ''
      return String((this.chargeBatchTreatments[0] && this.chargeBatchTreatments[0].batch_no) || '').trim()
    },
    chargeSummaryTitle() {
      if (this.isBatchChargeMode) {
        return `本次汇总收费共 ${this.chargeBatchTreatments.length} 条处置`
      }
      return this.billingTreatment && this.billingTreatment.appointment_purpose
        ? `收费项目：${this.billingTreatment.appointment_purpose}`
        : '当前处置收费'
    },
    chargeSummaryDescription() {
      if (this.isBatchChargeMode) {
        return (this.chargeBatchTreatments || [])
          .map(item => [item.appointment_purpose, item.tooth_positions ? `牙位:${item.tooth_positions}` : ''].filter(Boolean).join('｜'))
          .filter(Boolean)
          .join('；')
      }
      if (!this.billingTreatment) return '请确认本次收费信息'
      return [this.billingTreatment.treatment_content, this.billingTreatment.tooth_positions ? `牙位:${this.billingTreatment.tooth_positions}` : '']
        .filter(Boolean)
        .join('｜') || '请确认本次收费信息'
    },
    chargeChannelSplitTotal() {
      const splits = this.chargeForm && Array.isArray(this.chargeForm.channel_splits) ? this.chargeForm.channel_splits : []
      const total = splits.reduce((sum, item) => {
        const amount = Number(item && item.amount)
        return Number.isFinite(amount) ? sum + amount : sum
      }, 0)
      return this.formatMoney(total)
    },
    availableTreatmentMedicalRecords() {
      return (this.records || []).filter(item => item && item.id && !isLocalEntityId(item.id) && Number(item.id) > 0)
    },
    appointments() {
      if (!this.data || !this.data.appointments) return []
      return this.data.appointments.map(item => ({
        ...item,
        appointment_purpose: this.formatAppointmentPurpose(item.appointment_purpose)
      }))
    },
    wechatBound() {
      return !!(this.data && this.data.wechatBound)
    },
    wechatBindStatusLabel() {
      return (this.data && this.data.wechatBindStatusLabel) || '未绑定微信'
    },
    wechatBindUrl() {
      return (this.data && this.data.wechatBindUrl) || ''
    },
    wechatFollowQrUrl() {
      return (this.data && this.data.wechatFollowQrUrl) || ''
    },
    fallbackWechatQrUrl() {
      if (this.wechatBound || !this.wechatBindUrl) return ''
      return `/wechat/bind/qrcode?size=220&text=${encodeURIComponent(this.wechatBindUrl)}`
    },
    wechatQrCodeUrl() {
      if (this.wechatBound) return ''
      return this.wechatFollowQrUrl || this.fallbackWechatQrUrl
    },
    wechatQrTip() {
      if (this.wechatFollowQrUrl) {
        return '请让患者使用微信扫码直接进入公众号关注界面；关注完成后会自动绑定，并通过公众号消息进入患者H5页面。'
      }
      if (this.wechatBindUrl) {
        return '当前已回退为绑定入口二维码。请让患者使用微信扫码完成绑定，并进入患者H5页面。'
      }
      return '绑定二维码生成中'
    }
  },
  watch: {
    '$route.query.id': {
      async handler(value) {
        const nextId = String(value || '').trim()
        const currentId = String(this.patientId || '').trim()
        this.patientName = String(this.$route.query.name || '').trim()
        if (!nextId || nextId === currentId) return
        this.patientId = nextId
        const resolved = await this.tryResolveLocalPatientRoute()
        if (!resolved) {
          this.load360()
        }
      }
    },
    'treatmentForm.items': {
      deep: true,
      handler() {
        if (this.treatmentDialog) {
          this.recalculateTreatmentSummary()
        }
      }
    }
  },
  created() {
    this.loadDoctors()
    this.loadTreatmentProjectOptions()
    this.loadRecordOperationOptions()
    this.loadLabFactoryOptions()
    this.loadMedicalRecordTemplateOptions()
    this.loadPaymentChannelOptions()
    const id = this.$route.query.id
    const tab = String(this.$route.query.tab || '').trim()
    if (['records', 'appointments', 'billing', 'images'].includes(tab)) {
      this.activeTab = tab
    }
    if (typeof window !== 'undefined') {
      window.addEventListener(OFFLINE_ID_MAP_EVENT, this.handlePatientIdMapped)
    }
    if (id) {
      this.patientId = id
      this.tryResolveLocalPatientRoute().then(resolved => {
        if (!resolved) {
          this.load360()
        }
      })
    }
  },
  beforeDestroy() {
    if (this.relationSuggestionBlurTimer) {
      clearTimeout(this.relationSuggestionBlurTimer)
      this.relationSuggestionBlurTimer = null
    }
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
        path: '/Patient360',
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
    readCurrentUser() {
      return getAdminSession() || {}
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
    buildEmptyRecordForm() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      return {
        patient_id: isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId),
        patient_name: this.data.patient.name,
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
    loadMedicalRecordTemplateOptions() {
      fetchCachedResource({
        cacheKey: 'ref:medical-record-templates',
        scope: '',
        url: '/medical-record-templates/selectEnabled',
        loader: () => axios.get('/medical-record-templates/selectEnabled')
      }).then(result => {
        this.medicalRecordTemplateOptions = Array.isArray(result && result.data) ? result.data : []
        if (!this.recordTemplateCategories.includes(this.activeRecordTemplateCategory)) {
          this.activeRecordTemplateCategory = this.recordTemplateCategories[0]
        }
      }).catch(() => {
        this.medicalRecordTemplateOptions = []
      })
    },
    applyMedicalRecordTemplate() {
      const template = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedMedicalRecordTemplateId || 0))
      if (!template) return
      this.recordForm = Object.assign({}, this.recordForm, {
        record_type: template.record_type || this.recordForm.record_type || '初诊',
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
        operation_items: this.normalizeLoadedRecordOperationItems(template.operation_items || [])
      })
      this.activeRecordTemplateCategory = String(template.template_category || '').trim() || DEFAULT_TEMPLATE_CATEGORY
      this.selectedQuickRecordOperationId = ''
      this.selectedRecordProjectId = this.recordForm.operation_items.length ? (this.recordForm.operation_items[0].project_id || '') : ''
      this.recordLastAutoTreatmentDraft = buildMedicalRecordTreatmentDraft(this.recordForm.operation_items || [])
      this.recordTreatmentDraftLocked = String(this.recordForm.treatment || '').trim() && String(this.recordForm.treatment || '').trim() !== this.recordLastAutoTreatmentDraft
      this.recordEditorFlags.autoSyncToothPositions = !!(this.recordForm.operation_items || []).length
    },
    buildMedicalRecordTemplatePayload(templateName) {
      return {
        template_name: String(templateName || '').trim(),
        template_category: this.activeRecordTemplateCategory || DEFAULT_TEMPLATE_CATEGORY,
        record_type: this.recordForm.record_type || '初诊',
        chief_complaint: this.recordForm.chief_complaint || '',
        present_illness_history: this.recordForm.present_illness_history || '',
        past_history: this.recordForm.past_history || '',
        infectious_history: this.recordForm.infectious_history || '',
        allergy_history: this.recordForm.allergy_history || '',
        general_condition: this.recordForm.general_condition || '',
        examination: this.recordForm.examination || '',
        auxiliary_examination: this.recordForm.auxiliary_examination || '',
        diagnosis: this.recordForm.diagnosis || '',
        treatment_plan: this.recordForm.treatment_plan || '',
        treatment: this.recordForm.treatment || '',
        tooth_positions: this.resolveRecordToothPositions(),
        medical_advice: this.recordForm.medical_advice || '',
        prescription: this.recordForm.prescription || '',
        record_tags: this.normalizeRecordTags(this.recordForm.record_tags),
        image_summary: this.recordForm.image_summary || '',
        notes: this.recordForm.notes || '',
        operation_items: this.normalizeRecordOperationItems(this.recordForm.operation_items),
        created_by: this.currentUser && this.currentUser.id ? Number(this.currentUser.id) : null,
        created_by_name: this.currentUser && this.currentUser.name ? this.currentUser.name : ''
      }
    },
    saveCurrentRecordAsTemplate() {
      const currentTemplate = (this.medicalRecordTemplateOptions || []).find(item => Number(item.id) === Number(this.selectedMedicalRecordTemplateId || 0))
      const defaultName = currentTemplate && currentTemplate.template_name
        ? currentTemplate.template_name
        : (this.recordForm.diagnosis || this.recordForm.chief_complaint || '病历模板')
      this.$prompt(`请输入模板名称，将保存到分类“${this.activeRecordTemplateCategory || DEFAULT_TEMPLATE_CATEGORY}”`, '保存病历模板', {
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
        await this.loadMedicalRecordTemplateOptions()
        this.selectedMedicalRecordTemplateId = res.data && res.data.data && res.data.data.id ? res.data.data.id : this.selectedMedicalRecordTemplateId
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
        await this.loadMedicalRecordTemplateOptions()
      }).catch(() => {})
    },
    handleRecordTemplateNodeClick(node) {
      if (!node) return
      if (node.isTemplate) {
        this.selectedMedicalRecordTemplateId = node.id
        this.activeRecordTemplateCategory = node.category || DEFAULT_TEMPLATE_CATEGORY
        this.applyMedicalRecordTemplate()
        return
      }
      this.activeRecordTemplateCategory = node.label || DEFAULT_TEMPLATE_CATEGORY
    },
    buildEmptyAppointmentForm() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      return {
        id: null,
        patient_id: isLocalEntityId(this.patientId) ? this.patientId : Number(this.patientId),
        patient_name: this.data && this.data.patient ? this.data.patient.name : '',
        appointment_date: this.currentDateValue(),
        appointment_time: '09:00:00',
        duration_minutes: 60,
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        appointment_purpose: '',
        status: '待治疗',
        cancel_reason: ''
      }
    },
    syncRecordDefaultDoctor() {
      if (!this.recordDialog || this.recordDialogTitle !== '新增病历' || !this.recordForm || this.recordForm.id || this.recordForm.doctor_account_id) {
        return
      }
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      if (!doctorAccountId) {
        return
      }
      this.$set(this.recordForm, 'doctor_account_id', doctorAccountId)
      this.$set(this.recordForm, 'doctor_name', this.resolveDefaultDoctorName(doctorAccountId))
    },
    loadDoctors() {
      fetchCachedResource({
        cacheKey: 'ref:doctors-active',
        scope: '',
        url: '/accounts/doctors/active',
        loader: () => axios.get('/accounts/doctors/active')
      }).then(result => {
        const list = Array.isArray(result && result.data) ? result.data : []
        this.doctors = list.map(this.normalizeDoctor).filter(Boolean)
        this.syncRecordDefaultDoctor()
      }).catch(() => {
        this.doctors = []
      })
    },
    async load360() {
      if (!this.patientId) { this.$message.warning('请输入患者ID'); return }
      this.loading = true
      this.uploadExtra.patientId = this.patientId
      if (isLocalEntityId(this.patientId)) {
        const payload = await augmentCachedData('patient360', {}, { patientId: this.patientId })
        this.data = payload && payload.patient ? payload : null
        this.records = Array.isArray(payload && payload.records) ? payload.records : []
        this.uploadExtra.patientName = this.data && this.data.patient ? this.data.patient.name : ''
        this.loading = false
        return
      }
      fetchCachedResource({
        cacheKey: `page:patient360:${this.patientId}`,
        scope: 'patient360',
        url: `/patient360/overview/${this.patientId}`,
        loader: () => axios.get(`/patient360/overview/${this.patientId}`),
        context: { patientId: this.patientId },
        notifier: message => this.$message.warning(message)
      }).then(result => {
        const payload = result && result.data ? result.data : {}
        const treatments = (payload.treatments || []).map(item => ({
          ...item,
          appointment_purpose: this.formatAppointmentPurpose(item.appointment_purpose)
        }))
        this.data = payload && payload.patient ? { ...payload, treatments } : null
        if (this.data && this.data.patient) {
          rememberRecentPatient(this.data.patient)
          this.uploadExtra.patientName = this.data.patient.name
        }
        this.records = Array.isArray(this.data && this.data.records) ? this.data.records : []
        this.loadRecords()
      }).catch(() => {
        this.data = null
      }).finally(() => {
        this.loading = false
      })
    },
    loadRecords() {
      if (isLocalEntityId(this.patientId)) {
        this.records = Array.isArray(this.data && this.data.records) ? this.data.records : []
        return
      }
      axios.get('/medical-records/selectByPatientId', {
        params: { patientId: this.patientId, page: 1, size: 200 }
      }).then(res => {
        if (res.data.code === '200') {
          this.records = res.data.data.list || []
        }
      }).catch(() => {
        this.records = Array.isArray(this.data && this.data.records) ? this.data.records : []
      })
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
    async loadTreatmentProjectOptions() {
      try {
        const result = await fetchCachedResource({
          cacheKey: 'ref:treatment-projects-enabled',
          scope: '',
          url: '/treatment-projects/selectEnabled',
          loader: () => axios.get('/treatment-projects/selectEnabled')
        })
        this.treatmentProjectOptions = Array.isArray(result && result.data) ? result.data : []
      } catch (error) {
        this.treatmentProjectOptions = []
      }
    },
    async loadRecordOperationOptions() {
      try {
        const result = await fetchCachedResource({
          cacheKey: 'ref:treatment-operations-enabled',
          scope: '',
          url: '/treatment-operations/selectEnabled',
          loader: () => axios.get('/treatment-operations/selectEnabled')
        })
        this.recordOperationOptions = Array.isArray(result && result.data) ? result.data : []
      } catch (error) {
        this.recordOperationOptions = []
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
      }
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
    handleQuickRecordOperationSelect(operationId) {
      if (!operationId) return
      this.appendRecordOperationById(operationId)
      this.selectedQuickRecordOperationId = ''
    },
    appendRecordOperationById(operationId) {
      const operation = (this.recordOperationOptions || []).find(item => String(item.id) === String(operationId || ''))
      if (!operation) return
      if (!Array.isArray(this.recordForm.operation_items)) {
        this.$set(this.recordForm, 'operation_items', [])
      }
      const nextItem = defaultRecordOperationItem()
      nextItem.operation_id = operation.id
      nextItem.operation_name = operation.operation_name || ''
      nextItem.need_lab_processing = operation.need_lab_processing === 1 ? 1 : 0
      nextItem.default_processing_days = Number(operation.default_processing_days || 0)
      this.recordForm.operation_items.push(nextItem)
      this.refreshRecordTreatmentDraft()
    },
    appendRecordOperation() {
      if (!Array.isArray(this.recordForm.operation_items)) {
        this.$set(this.recordForm, 'operation_items', [])
      }
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
        local_key: `${item.id || 'record-op'}-${Math.random().toString(36).slice(2, 8)}`,
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
      if (!this.recordEditorFlags.autoSyncToothPositions) {
        return String(this.recordForm.tooth_positions || '').trim()
      }
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
    handleRecordOperationToothChange() {
      this.refreshRecordTreatmentDraft()
    },
    handleRecordTreatmentInput() {
      const currentText = String(this.recordForm.treatment || '').trim()
      this.recordTreatmentDraftLocked = currentText && currentText !== this.recordLastAutoTreatmentDraft
    },
    openEditPatient() {
      const patient = this.data && this.data.patient ? this.data.patient : null
      if (!patient || !patient.id) {
        this.$message.warning('当前患者信息不完整，暂时无法修改')
        return
      }
      this.patientEditForm = Object.assign(createEmptyPatientEditItem(), patient, {
        id: Number(patient.id),
        age: (() => {
          const age = getPatientAge(patient)
          return age === '' ? null : age
        })(),
        customer_source: normalizeText(patient.customer_source),
        relation_type: normalizeText(patient.relation_type),
        related_patient_id: patient.related_patient_id ? Number(patient.related_patient_id) : null,
        related_patient_name: normalizeText(patient.related_patient_name)
      })
      this.patientReferralForm = Object.assign(createEmptyReferralState(), {
        referrer_type: patient.referrer_type || '',
        referrer_patient_id: patient.referrer_patient_id || null,
        referrer_patient_name: patient.referrer_patient_name || '',
        external_referrer_type: patient.external_referrer_type || '',
        external_referrer_name: patient.external_referrer_name || '',
        external_referrer_contact: patient.external_referrer_contact || '',
        referral_remark: patient.referral_remark || ''
      })
      this.relatedPatientSuggestions = []
      this.relationSuggestionVisible = false
      this.patientEditDialog = true
    },
    closePatientEditDialog() {
      this.patientEditDialog = false
      this.patientEditForm = createEmptyPatientEditItem()
      this.patientReferralForm = createEmptyReferralState()
      this.relatedPatientSuggestions = []
      this.relationSuggestionVisible = false
      if (this.relationSuggestionBlurTimer) {
        clearTimeout(this.relationSuggestionBlurTimer)
        this.relationSuggestionBlurTimer = null
      }
    },
    handlePatientReferralChange(value) {
      this.patientReferralForm = Object.assign(createEmptyReferralState(), value || {})
      if (this.hasPatientReferralPayload()) {
        this.patientEditForm.customer_source = '转介绍'
      }
    },
    buildPatientEditPayload() {
      const relationType = normalizeText(this.patientEditForm.relation_type)
      const relatedPatientName = normalizeText(this.patientEditForm.related_patient_name)
      const hasRelatedPatient = !!relatedPatientName || !!this.patientEditForm.related_patient_id
      const referralPayload = this.hasPatientReferralPayload() ? this.patientReferralForm : createEmptyReferralState()
      return {
        id: Number(this.patientEditForm.id || 0),
        name: normalizeText(this.patientEditForm.name),
        gender: normalizeText(this.patientEditForm.gender),
        age: this.patientEditForm.age === '' || this.patientEditForm.age === null || this.patientEditForm.age === undefined
          ? null
          : Number(this.patientEditForm.age),
        phone: normalizeText(this.patientEditForm.phone),
        customer_source: this.hasPatientReferralPayload() ? '转介绍' : normalizeText(this.patientEditForm.customer_source),
        relation_type: relationType,
        related_patient_id: hasRelatedPatient && this.patientEditForm.related_patient_id
          ? Number(this.patientEditForm.related_patient_id)
          : null,
        related_patient_name: hasRelatedPatient ? relatedPatientName : '',
        referrer_type: referralPayload.referrer_type || '',
        referrer_patient_id: referralPayload.referrer_patient_id || null,
        referrer_patient_name: referralPayload.referrer_patient_name || '',
        external_referrer_type: referralPayload.external_referrer_type || '',
        external_referrer_name: referralPayload.external_referrer_name || '',
        external_referrer_contact: referralPayload.external_referrer_contact || '',
        referral_remark: referralPayload.referral_remark || ''
      }
    },
    validatePatientEditForm() {
      if (!normalizeText(this.patientEditForm.name)) return '患者姓名必填'
      if (!normalizeText(this.patientEditForm.gender)) return '患者性别必填'
      if (this.patientEditForm.age === null || this.patientEditForm.age === undefined || this.patientEditForm.age === '') return '患者年龄必填'
      if (!Number.isFinite(Number(this.patientEditForm.age)) || Number(this.patientEditForm.age) < 0 || Number(this.patientEditForm.age) > 150) return '患者年龄需在0到150之间'
      if (!normalizeText(this.patientEditForm.phone)) return '手机号码必填'
      if (!PATIENT_PHONE_REGEX.test(normalizeText(this.patientEditForm.phone))) return '手机号码需为11位数字'
      if (!normalizeText(this.patientEditForm.customer_source)) return '患者来源必填'
      if (!this.patientSourceOptions.includes(normalizeText(this.patientEditForm.customer_source))) return '患者来源不合法'
      const hasRelationType = !!normalizeText(this.patientEditForm.relation_type)
      const hasRelatedPatient = !!normalizeText(this.patientEditForm.related_patient_name) || !!this.patientEditForm.related_patient_id
      if (!hasRelationType && hasRelatedPatient) return '已选择关联患者时，必须选择患者关系'
      if (hasRelatedPatient && !this.patientEditForm.related_patient_id) return '请从下拉列表中选择有效的关联患者'
      if (this.patientEditForm.related_patient_id && Number(this.patientEditForm.related_patient_id) === Number(this.patientEditForm.id)) return '关联患者不能是本人'
      if (this.patientReferralForm.referrer_type === 'patient' && !this.patientReferralForm.referrer_patient_id) return '请选择有效的介绍患者'
      if (this.patientReferralForm.referrer_type === 'patient' && Number(this.patientReferralForm.referrer_patient_id) === Number(this.patientEditForm.id || 0)) return '介绍患者不能是本人'
      if (this.patientReferralForm.referrer_type === 'external' && !normalizeText(this.patientReferralForm.external_referrer_name)) return '请输入外部介绍人姓名'
      return ''
    },
    hasPatientReferralPayload() {
      return !!(this.patientReferralForm && (
        Number(this.patientReferralForm.referrer_patient_id || 0) > 0
        || normalizeText(this.patientReferralForm.referrer_patient_name)
        || normalizeText(this.patientReferralForm.external_referrer_type)
        || normalizeText(this.patientReferralForm.external_referrer_name)
        || normalizeText(this.patientReferralForm.external_referrer_contact)
        || normalizeText(this.patientReferralForm.referrer_type)
        || normalizeText(this.patientReferralForm.referral_remark)
      ))
    },
    submitPatientEdit() {
      const validationMessage = this.validatePatientEditForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      savePatient(this.buildPatientEditPayload(), {
        isEdit: true,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success('患者资料已更新')
        }
        this.closePatientEditDialog()
        this.load360()
      }).catch(error => {
        this.$message.error((error && error.message) || '患者资料更新失败')
      })
    },
    loadRelatedPatientSuggestions(keyword = '') {
      axios.get('/patients/search', {
        params: {
          keyword: normalizeText(keyword),
          page: 1,
          size: 8
        }
      }).then(response => {
        const data = response.data && response.data.data
        const list = Array.isArray(data && data.list) ? data.list : []
        this.relatedPatientSuggestions = list.filter(item => Number(item.id) !== Number(this.patientEditForm.id || 0))
      }).catch(() => {
        this.relatedPatientSuggestions = []
      })
    },
    handleRelatedPatientInput() {
      this.patientEditForm.related_patient_id = null
      this.relationSuggestionVisible = true
      this.loadRelatedPatientSuggestions(this.patientEditForm.related_patient_name)
    },
    handleRelatedPatientFocus() {
      if (this.relationSuggestionBlurTimer) {
        clearTimeout(this.relationSuggestionBlurTimer)
        this.relationSuggestionBlurTimer = null
      }
      this.relationSuggestionVisible = true
      this.loadRelatedPatientSuggestions(this.patientEditForm.related_patient_name)
    },
    handleRelatedPatientBlur() {
      this.relationSuggestionBlurTimer = setTimeout(() => {
        this.relationSuggestionVisible = false
      }, 120)
    },
    selectRelatedPatientSuggestion(patient) {
      this.patientEditForm.related_patient_id = patient && patient.id ? Number(patient.id) : null
      this.patientEditForm.related_patient_name = patient && patient.name ? patient.name : ''
      this.relationSuggestionVisible = false
    },
    // 预约
    openAddAppointment() {
      if (!this.patientId || !this.data || !this.data.patient) {
        this.$message.warning('当前患者信息不完整，无法新增预约')
        return
      }
      this.appointmentEditing = false
      this.appointmentForm = this.buildEmptyAppointmentForm()
      this.appointmentDialog = true
    },
    openEditAppointment(row) {
      const matchedDoctor = this.currentDoctorById(row.doctor_account_id) || (this.doctors || []).find(item => item.name === String(row.doctor_name || '').trim())
      this.appointmentEditing = true
      this.appointmentForm = {
        ...this.buildEmptyAppointmentForm(),
        ...row,
        doctor_account_id: matchedDoctor ? matchedDoctor.id : (row.doctor_account_id ? Number(row.doctor_account_id) : null),
        doctor_name: matchedDoctor ? matchedDoctor.name : String(row.doctor_name || '').trim()
      }
      this.appointmentDialog = true
    },
    validateAppointmentForm() {
      if (!this.appointmentForm.patient_id) return '患者ID不能为空'
      if (!this.appointmentForm.patient_name || !String(this.appointmentForm.patient_name).trim()) return '患者姓名不能为空'
      if (!this.appointmentForm.appointment_date) return '预约日期不能为空'
      if (!this.appointmentForm.appointment_time) return '预约时间不能为空'
      if (!this.appointmentForm.duration_minutes || Number(this.appointmentForm.duration_minutes) <= 0) return '预约时长不能为空'
      if (!this.appointmentForm.doctor_account_id) return '接诊医生不能为空'
      if (!this.appointmentForm.appointment_purpose || !String(this.appointmentForm.appointment_purpose).trim()) return '预约项目不能为空'
      const doctor = this.currentDoctorById(this.appointmentForm.doctor_account_id)
      this.appointmentForm.doctor_name = doctor && doctor.name ? doctor.name : this.resolveDefaultDoctorName(this.appointmentForm.doctor_account_id)
      return ''
    },
    submitAppointment() {
      const validationMessage = this.validateAppointmentForm()
      if (validationMessage) {
        this.$message.warning(validationMessage)
        return
      }
      saveAppointment(this.appointmentForm, {
        isEdit: this.appointmentEditing,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success(this.appointmentEditing ? '预约编辑成功' : '预约新增成功')
        }
        this.appointmentDialog = false
        this.load360()
      }).catch(error => {
        this.$message.error((error && error.message) || (this.appointmentEditing ? '预约编辑失败' : '预约新增失败'))
      })
    },
    // 病历
    openAddRecord() {
      this.currentUser = this.readCurrentUser()
      this.recordForm = this.buildEmptyRecordForm()
      this.selectedMedicalRecordTemplateId = null
      this.selectedRecordProjectId = ''
      this.selectedQuickRecordOperationId = ''
      this.recordLastAutoTreatmentDraft = ''
      this.recordTreatmentDraftLocked = false
      this.recordTemplateKeyword = ''
      this.recordEditorFlags.autoSyncToothPositions = true
      this.recordDialogTitle = '新增病历'; this.recordDialog = true
    },
    async openEditRecord(row) {
      const detail = isLocalEntityId(row.id)
        ? Object.assign({}, row)
        : await axios.get('/medical-records/selectById', { params: { id: row.id } }).then(res => (res.data && res.data.code === '200' ? (res.data.data || {}) : {}))
      const matchedDoctor = this.currentDoctorById(detail.doctor_account_id) || (this.doctors || []).find(item => item.name === String(detail.doctor_name || '').trim())
      this.recordForm = Object.assign({}, this.buildEmptyRecordForm(), detail, {
        visit_date: this.formatDateTimeValue(detail.visit_date) || this.currentDateTimeValue(),
        doctor_account_id: matchedDoctor ? matchedDoctor.id : (detail.doctor_account_id ? Number(detail.doctor_account_id) : null),
        doctor_name: matchedDoctor ? matchedDoctor.name : String(detail.doctor_name || '').trim(),
        nurse_name: detail.nurse_name || '',
        assistant_name: detail.assistant_name || '',
        record_type: detail.record_type || '初诊',
        present_illness_history: detail.present_illness_history || '',
        past_history: detail.past_history || '',
        infectious_history: detail.infectious_history || '',
        allergy_history: detail.allergy_history || '',
        general_condition: detail.general_condition || '体健',
        examination: detail.examination || '',
        auxiliary_examination: detail.auxiliary_examination || '',
        treatment_plan: detail.treatment_plan || '',
        tooth_positions: detail.tooth_positions || '',
        medical_advice: detail.medical_advice || '',
        record_tags: detail.record_tags || '',
        image_summary: detail.image_summary || '',
        record_status: detail.record_status || 'final',
        operation_items: this.normalizeLoadedRecordOperationItems(detail.operation_items || [])
      })
      this.selectedMedicalRecordTemplateId = null
      this.recordTemplateKeyword = ''
      this.selectedRecordProjectId = this.recordForm.operation_items.length ? (this.recordForm.operation_items[0].project_id || '') : ''
      this.selectedQuickRecordOperationId = ''
      this.recordLastAutoTreatmentDraft = buildMedicalRecordTreatmentDraft(this.recordForm.operation_items || [])
      this.recordTreatmentDraftLocked = String(this.recordForm.treatment || '').trim() && String(this.recordForm.treatment || '').trim() !== this.recordLastAutoTreatmentDraft
      this.recordEditorFlags.autoSyncToothPositions = !!(this.recordForm.operation_items || []).length
      this.recordDialogTitle = '编辑病历'
      this.recordDialog = true
      if (this.selectedRecordProjectId) {
        await this.loadRecordProjectDetail(this.selectedRecordProjectId)
      }
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
    normalizeRecordTags(value) {
      return splitRecordTags(value).join(',')
    },
    submitRecord(status = 'final') {
      if (!this.recordForm.doctor_account_id) {
        this.recordForm.doctor_account_id = this.resolveDefaultDoctorAccountId()
      }
      if (!this.recordForm.doctor_account_id) {
        this.$message.warning('请选择接诊医生')
        return
      }
      const doctor = this.currentDoctorById(this.recordForm.doctor_account_id)
      this.recordForm.doctor_name = doctor && doctor.name ? doctor.name : this.resolveDefaultDoctorName(this.recordForm.doctor_account_id)
      const rawOperationItems = Array.isArray(this.recordForm.operation_items) ? this.recordForm.operation_items : []
      if (!rawOperationItems.length) {
        this.$message.warning('本次操作为必填项，请至少选择一个操作字典项')
        return
      }
      if (rawOperationItems.some(item => !String(item && item.operation_id || '').trim())) {
        this.$message.warning('本次操作为必填项，请为每条操作选择操作字典项')
        return
      }
      const missingLabFactoryIndex = rawOperationItems.findIndex(item =>
        Number(item && item.need_lab_processing || 0) === 1 && !String(item && item.factory_id || '').trim()
      )
      if (missingLabFactoryIndex >= 0) {
        this.$message.warning(`第${missingLabFactoryIndex + 1}条待登记加工操作未选择加工厂`)
        return
      }
      if (this.recordForm.treatment && String(this.recordForm.treatment).trim() && !String(this.resolveRecordToothPositions() || '').trim()) {
        this.$message.warning('请选择牙位')
        return
      }
      const payload = Object.assign({}, this.recordForm, {
        tooth_positions: this.resolveRecordToothPositions(),
        record_tags: this.normalizeRecordTags(this.recordForm.record_tags),
        record_status: status,
        operation_items: this.normalizeRecordOperationItems(this.recordForm.operation_items, true)
      })
      saveMedicalRecord(payload, {
        isEdit: !!this.recordForm.id,
        notifier: message => this.$message.success(message)
      }).then(result => {
        if (!result.offline) {
          this.$message.success(status === 'draft' ? '病历已暂存' : '保存成功')
        }
        this.recordDialog = false
        this.loadRecords()
        this.load360()
      }).catch(error => {
        this.$message.error((error && error.message) || '保存失败')
      })
    },
    deleteRecord(id) {
      this.$confirm('确认删除该病历？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/medical-records/delete/${id}`).then(() => { this.$message.success('删除成功'); this.loadRecords(); this.load360() })
      })
    },
    // 影像
    beforeUpload(file) {
      const ok = file.type.startsWith('image/') || file.name.endsWith('.dcm')
      if (!ok) { this.$message.error('只支持图片或DICOM文件'); return false }
      return true
    },
    onUploadSuccess(res) {
      if (res.code === '200') { this.$message.success('上传成功'); this.load360() }
      else this.$message.error(res.msg)
    },
    onUploadError() { this.$message.error('上传失败') },
    isImage(img) { if (!img.image_name) return false; return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(img.image_name) },
    previewImage(img) { this.previewImg = img; this.previewVisible = true },
    downloadFile(img) { window.open(`/patient-images/file/${img.id}`) },
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
    },
    deleteImage(id) {
      this.$confirm('确认删除该影像？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/patient-images/delete/${id}`).then(() => { this.$message.success('删除成功'); this.load360() })
      })
    },
    // 随访
    openAddFollowup() {
      this.currentUser = this.readCurrentUser()
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      this.followupForm = {
        patient_id: Number(this.patientId),
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        followup_date: '',
        followup_type: '电话',
        summary: '',
        next_followup_date: ''
      }
      this.followupDialog = true
    },
    buildDefaultConsentTitle() {
      return '口腔治疗知情同意书'
    },
    buildDefaultConsentContent() {
      const patientName = (this.data && this.data.patient && this.data.patient.name) || '患者'
      const doctorName = this.resolveDefaultDoctorName(this.resolveDefaultDoctorAccountId()) || '门诊医生'
      return `${patientName}：\n\n您好，请在治疗前仔细阅读以下内容：\n1. 医生已向您说明本次治疗的目的、流程、常见风险及注意事项。\n2. 您已知晓治疗过程中可能出现疼痛、肿胀、出血、治疗效果个体差异等情况。\n3. 如有既往病史、药物过敏史、妊娠或其他特殊情况，请在治疗前主动告知医生。\n4. 您确认已获得充分提问机会，并自愿接受本次治疗安排。\n\n接诊医生：${doctorName}\n请阅读完毕后在患者公众号 H5 中签字确认。`
    },
    loadConsentTemplateOptions() {
      fetchCachedResource({
        cacheKey: 'ref:consent-templates-enabled',
        scope: '',
        url: '/consent-template/selectEnabled',
        loader: () => axios.get('/consent-template/selectEnabled')
      }).then(result => {
        this.consentTemplateOptions = Array.isArray(result && result.data) ? result.data : []
      }).catch(() => {
        this.consentTemplateOptions = []
      })
    },
    applyConsentTemplate() {
      const item = (this.consentTemplateOptions || []).find(option => String(option.id) === String(this.selectedConsentTemplateId || ''))
      if (!item) return
      this.consentForm.title = item.title || ''
      this.consentForm.content = item.content || ''
    },
    openConsentDialog() {
      this.selectedConsentTemplateId = null
      this.consentForm = {
        title: this.buildDefaultConsentTitle(),
        content: this.buildDefaultConsentContent()
      }
      this.loadConsentTemplateOptions()
      this.consentDialog = true
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
        patient_name: this.data && this.data.patient ? this.data.patient.name : '',
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
    submitFollowup() {
      if (!this.followupForm.patient_id) {
        this.$message.warning('患者ID不能为空')
        return
      }
      if (!this.followupForm.followup_date) {
        this.$message.warning('计划回访时间不能为空')
        return
      }
      if (!this.followupForm.doctor_account_id) {
        this.$message.warning('请选择负责医生')
        return
      }
      const doctor = this.currentDoctorById(this.followupForm.doctor_account_id)
      this.followupForm.doctor_name = doctor && doctor.name ? doctor.name : this.resolveDefaultDoctorName(this.followupForm.doctor_account_id)
      axios.post('/followup/add', this.followupForm).then(res => {
        if (res.data.code === '200') { this.$message.success('新增成功'); this.followupDialog = false; this.load360() }
        else this.$message.error(res.data.msg)
      })
    },
    deleteFollowup(id) {
      this.$confirm('确认删除？', '提示', { type: 'warning' }).then(() => {
        axios.delete(`/followup/delete/${id}`).then(() => { this.$message.success('删除成功'); this.load360() })
      })
    },
    // 新增处置
    buildEmptyTreatmentItem(defaultDoctorAccountId = null) {
      const doctorAccountId = defaultDoctorAccountId || (this.treatmentForm && this.treatmentForm.doctor_account_id) || this.resolveDefaultDoctorAccountId()
      return {
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        project_id: '',
        appointment_purpose: '',
        treatment_content: '',
        tooth_positions: '',
        treatment_fee: 0
      }
    },
    buildEmptyTreatmentForm() {
      const doctorAccountId = this.resolveDefaultDoctorAccountId()
      return {
        patient_id: Number(this.patientId),
        patient_name: this.data && this.data.patient ? this.data.patient.name : '',
        medical_record_id: this.resolveDefaultTreatmentMedicalRecordId(),
        doctor_account_id: doctorAccountId,
        doctor_name: this.resolveDefaultDoctorName(doctorAccountId),
        treatment_date: this.currentDateValue(),
        status: '进行中',
        discount_rate: 100,
        discounted_total_fee: 0,
        items: [this.buildEmptyTreatmentItem(doctorAccountId)]
      }
    },
    normalizeMoney(value) {
      const amount = Number(value || 0)
      if (!Number.isFinite(amount)) return 0
      return Math.round(amount * 100) / 100
    },
    clampDiscountRate(value) {
      const rate = this.normalizeMoney(value)
      if (rate < 0) return 0
      if (rate > 100) return 100
      return rate
    },
    normalizeDiscountedTotal(value) {
      const originalTotal = this.treatmentOriginalTotal
      let discounted = this.normalizeMoney(value)
      if (discounted < 0) discounted = 0
      if (discounted > originalTotal) discounted = originalTotal
      return discounted
    },
    recalculateTreatmentSummary() {
      const originalTotal = this.treatmentOriginalTotal
      if (!this.treatmentForm) return
      if (originalTotal <= 0) {
        this.treatmentForm.discount_rate = 100
        this.treatmentForm.discounted_total_fee = 0
        return
      }
      if (this.treatmentPricingSource === 'discounted') {
        const discountedTotal = this.normalizeDiscountedTotal(this.treatmentForm.discounted_total_fee)
        this.treatmentForm.discounted_total_fee = discountedTotal
        this.treatmentForm.discount_rate = this.normalizeMoney((discountedTotal / originalTotal) * 100)
        return
      }
      const discountRate = this.clampDiscountRate(this.treatmentForm.discount_rate === undefined ? 100 : this.treatmentForm.discount_rate)
      this.treatmentForm.discount_rate = discountRate
      this.treatmentForm.discounted_total_fee = this.normalizeMoney(originalTotal * discountRate / 100)
    },
    syncTreatmentSummaryByRate() {
      this.treatmentPricingSource = 'rate'
      this.recalculateTreatmentSummary()
    },
    syncTreatmentSummaryByDiscounted() {
      this.treatmentPricingSource = 'discounted'
      this.recalculateTreatmentSummary()
    },
    handleTreatmentPriceChange() {
      this.recalculateTreatmentSummary()
    },
    handleTreatmentDefaultDoctorChange(value) {
      const doctorAccountId = value ? Number(value) : null
      this.treatmentForm.doctor_account_id = doctorAccountId
      this.treatmentForm.doctor_name = this.resolveDefaultDoctorName(doctorAccountId)
    },
    handleTreatmentItemDoctorChange(item) {
      if (!item) return
      const doctorAccountId = item.doctor_account_id ? Number(item.doctor_account_id) : null
      item.doctor_account_id = doctorAccountId
      item.doctor_name = this.resolveDefaultDoctorName(doctorAccountId)
    },
    addTreatmentItem() {
      if (!this.treatmentForm || !Array.isArray(this.treatmentForm.items)) {
        this.treatmentForm = this.buildEmptyTreatmentForm()
      }
      this.treatmentForm.items.push(this.buildEmptyTreatmentItem(this.treatmentForm.doctor_account_id))
      this.recalculateTreatmentSummary()
    },
    removeTreatmentItem(index) {
      if (!this.treatmentForm || !Array.isArray(this.treatmentForm.items)) return
      if (this.treatmentForm.items.length <= 1) return
      this.treatmentForm.items.splice(index, 1)
      this.recalculateTreatmentSummary()
    },
    openAddTreatment() {
      this.currentUser = this.readCurrentUser()
      this.treatmentPricingSource = 'rate'
      this.treatmentForm = this.buildEmptyTreatmentForm()
      this.loadTreatmentCatalogOptions()
      this.treatmentDialog = true
    },
    loadTreatmentCatalogOptions() {
      axios.get('/treatment-projects/selectEnabled').then(res => {
        this.treatmentProjectOptions = Array.isArray(res.data.data) ? res.data.data : []
      }).catch(() => {
        this.treatmentProjectOptions = []
      })
    },
    loadPaymentChannelOptions() {
      axios.get('/payment-channels/selectEnabled').then(res => {
        this.paymentChannelOptions = Array.isArray(res.data.data) ? res.data.data : []
        const splits = this.chargeForm && Array.isArray(this.chargeForm.channel_splits) ? this.chargeForm.channel_splits : []
        if (splits.length && this.paymentChannelOptions.length) {
          splits.forEach(split => {
            if (!split.payment_channel_id) {
              split.payment_channel_id = this.paymentChannelOptions[0].id
            }
          })
        }
      }).catch(() => {
        this.paymentChannelOptions = []
      })
    },
    applyTreatmentCatalogToItem(row) {
      const item = (this.treatmentProjectOptions || []).find(option => String(option.id) === String(row && row.project_id ? row.project_id : ''))
      if (!item || !row) return
      row.project_id = item.id
      row.appointment_purpose = item.project_name || ''
      row.treatment_fee = this.normalizeMoney(item.default_price)
      row.treatment_content = row.treatment_content || ''
      this.recalculateTreatmentSummary()
    },
    submitTreatment() {
      if (!this.treatmentForm.patient_id) {
        this.$message.warning('患者ID不能为空')
        return
      }
      if (!this.treatmentForm.patient_name || !String(this.treatmentForm.patient_name).trim()) {
        this.$message.warning('患者姓名不能为空')
        return
      }
      this.treatmentForm.doctor_name = this.resolveDefaultDoctorName(this.treatmentForm.doctor_account_id || this.resolveDefaultDoctorAccountId())
      if (!this.treatmentForm.treatment_date) {
        this.$message.warning('治疗日期不能为空')
        return
      }
      if (!Array.isArray(this.treatmentForm.items) || !this.treatmentForm.items.length) {
        this.$message.warning('请至少添加一条处置')
        return
      }
      for (let index = 0; index < this.treatmentForm.items.length; index += 1) {
        const item = this.treatmentForm.items[index]
        if (!item.appointment_purpose || !String(item.appointment_purpose).trim()) {
          this.$message.warning(`第${index + 1}条处置请选择处置收费项目`)
          return
        }
        if (!item.doctor_account_id) {
          item.doctor_account_id = this.treatmentForm.doctor_account_id || this.resolveDefaultDoctorAccountId()
        }
        if (!item.doctor_account_id) {
          this.$message.warning(`第${index + 1}条处置缺少处置医生`)
          return
        }
        item.doctor_name = this.resolveDefaultDoctorName(item.doctor_account_id)
        if (!item.doctor_name || !String(item.doctor_name).trim()) {
          this.$message.warning(`第${index + 1}条处置缺少处置医生`)
          return
        }
        if (item.treatment_content && String(item.treatment_content).trim() && !String(item.tooth_positions || '').trim()) {
          this.$message.warning(`第${index + 1}条处置请选择牙位`)
          return
        }
        if (item.treatment_fee === null || item.treatment_fee === undefined || !Number.isFinite(Number(item.treatment_fee))) {
          this.$message.warning(`第${index + 1}条处置费用无效`)
          return
        }
        if (Number(item.treatment_fee) < 0) {
          this.$message.warning(`第${index + 1}条处置费用不能小于0`)
          return
        }
      }
      if (this.treatmentOriginalTotal <= 0) {
        this.$message.warning('汇总价格必须大于0')
        return
      }
      const discountedTotalFee = this.normalizeDiscountedTotal(this.treatmentForm.discounted_total_fee)
      const payload = {
        patient_id: this.treatmentForm.patient_id,
        patient_name: String(this.treatmentForm.patient_name || '').trim(),
        medical_record_id: this.treatmentForm.medical_record_id || null,
        doctor_account_id: this.treatmentForm.doctor_account_id || null,
        doctor_name: String(this.treatmentForm.doctor_name || '').trim(),
        treatment_date: this.treatmentForm.treatment_date,
        status: this.treatmentForm.status,
        discount_rate: this.clampDiscountRate(this.treatmentForm.discount_rate),
        discounted_total_fee: discountedTotalFee,
        items: this.treatmentForm.items.map(item => ({
          project_id: item.project_id || null,
          doctor_account_id: item.doctor_account_id || null,
          doctor_name: String(item.doctor_name || '').trim(),
          appointment_purpose: String(item.appointment_purpose || '').trim(),
          treatment_content: String(item.treatment_content || '').trim(),
          tooth_positions: String(item.tooth_positions || '').trim(),
          treatment_fee: this.normalizeMoney(item.treatment_fee)
        }))
      }
      axios.post('/treatments/batchAdd', payload).then(res => {
        if (res.data.code === '200') {
          const created = Array.isArray(res.data.data) ? res.data.data.length : 0
          this.$message.success(created > 0 ? `新增成功，已生成${created}条处置` : '新增成功')
          this.treatmentDialog = false
          this.treatmentForm = {}
          this.load360()
        } else {
          this.$message.error(res.data.msg || '新增处置失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '新增处置失败')
      })
    },
    buildEmptyChargeChannelSplit(amount = null) {
      const defaultChannel = (this.paymentChannelOptions || [])[0] || null
      return {
        payment_channel_id: defaultChannel ? defaultChannel.id : null,
        amount
      }
    },
    addChargeChannelSplit() {
      if (!this.chargeForm || !Array.isArray(this.chargeForm.channel_splits)) {
        this.$set(this.chargeForm, 'channel_splits', [])
      }
      this.chargeForm.channel_splits.push(this.buildEmptyChargeChannelSplit())
    },
    removeChargeChannelSplit(index) {
      if (!this.chargeForm || !Array.isArray(this.chargeForm.channel_splits)) return
      if (this.chargeForm.channel_splits.length <= 1) return
      this.chargeForm.channel_splits.splice(index, 1)
    },
    batchTreatmentCount(row) {
      const batchNo = String(row && row.batch_no ? row.batch_no : '').trim()
      if (!batchNo) return 1
      const items = this.treatmentBatchMap[batchNo] || []
      return items.length || 1
    },
    resolveChargeTargetTreatments(row) {
      const batchNo = String(row && row.batch_no ? row.batch_no : '').trim()
      if (!batchNo) {
        return row ? [row] : []
      }
      const items = this.treatmentBatchMap[batchNo] || []
      return items.length > 1 ? items : (row ? [row] : [])
    },
    resolveChargeTargetAmount(rows) {
      const list = Array.isArray(rows) ? rows : []
      const total = list.reduce((sum, item) => {
        const arrears = Number(item && item.arrears_amount)
        if (Number.isFinite(arrears) && arrears > 0) {
          return sum + arrears
        }
        const fee = Number(item && item.treatment_fee)
        return Number.isFinite(fee) ? sum + fee : sum
      }, 0)
      return this.normalizeMoney(total)
    },
    buildChargeChannelSplits(amount) {
      return [this.buildEmptyChargeChannelSplit(amount > 0 ? Number(amount.toFixed(2)) : null)]
    },
    openChargeDialog(row) {
      if (!this.paymentChannelOptions.length) {
        this.loadPaymentChannelOptions()
      }
      this.billingTreatment = row
      this.chargeBatchTreatments = this.resolveChargeTargetTreatments(row)
      const amount = this.resolveChargeTargetAmount(this.chargeBatchTreatments)
      this.chargeForm = {
        amount: amount > 0 ? Number(amount.toFixed(2)) : null,
        date: this.currentDateValue(),
        remark: '',
        channel_splits: this.buildChargeChannelSplits(amount)
      }
      this.chargeDialog = true
    },
    prepareChargeChannelSplits() {
      const splits = this.chargeForm && Array.isArray(this.chargeForm.channel_splits) ? this.chargeForm.channel_splits : []
      if (!splits.length) {
        return { error: '请至少填写一条收款渠道', data: [] }
      }
      if (!this.paymentChannelOptions.length) {
        return { error: '请先在系统设置中维护收款渠道', data: [] }
      }
      const normalized = []
      let total = 0
      for (let index = 0; index < splits.length; index += 1) {
        const split = splits[index]
        const channelId = split && split.payment_channel_id ? Number(split.payment_channel_id) : null
        const amount = Number(split && split.amount)
        if (!channelId) {
          return { error: `第${index + 1}条收款渠道未选择`, data: [] }
        }
        if (!Number.isFinite(amount) || amount <= 0) {
          return { error: `第${index + 1}条收款渠道金额无效`, data: [] }
        }
        const channel = (this.paymentChannelOptions || []).find(item => Number(item.id) === channelId)
        if (!channel) {
          return { error: `第${index + 1}条收款渠道不存在或未启用`, data: [] }
        }
        total += amount
        normalized.push({
          payment_channel_id: channelId,
          payment_channel_name: channel.channel_name,
          amount: this.normalizeMoney(amount)
        })
      }
      const expected = this.normalizeMoney(this.chargeForm.amount)
      if (Math.abs(this.normalizeMoney(total) - expected) > 0.0001) {
        return { error: '收款渠道金额合计必须等于收费金额', data: [] }
      }
      return { error: '', data: normalized }
    },
    submitCharge() {
      if (!this.billingTreatment || !this.billingTreatment.id) {
        this.$message.warning('处置记录不存在')
        return
      }
      if (!this.chargeForm.date) {
        this.$message.warning('收费日期不能为空')
        return
      }
      if (!this.chargeForm.amount || Number(this.chargeForm.amount) <= 0) {
        this.$message.warning('收费金额必须大于0')
        return
      }
      const channelPayload = this.prepareChargeChannelSplits()
      if (channelPayload.error) {
        this.$message.warning(channelPayload.error)
        return
      }
      const payload = {
        amount: this.normalizeMoney(this.chargeForm.amount),
        date: this.chargeForm.date,
        remark: this.chargeForm.remark,
        channel_splits: channelPayload.data
      }
      const request = this.isBatchChargeMode
        ? axios.post(`/treatments/chargeBatch/${this.chargeBatchNo}`, payload)
        : axios.post(`/treatments/charge/${this.billingTreatment.id}`, payload)
      request.then(res => {
        if (res.data.code === '200') {
          this.$message.success('收费成功')
          this.chargeDialog = false
          this.chargeBatchTreatments = []
          this.load360()
        } else {
          this.$message.error(res.data.msg || '收费失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '收费失败')
      })
    },
    openRefundDialog(row) {
      this.billingTreatment = row
      const refundable = Number(row.charged_amount || 0) - Number(row.refunded_amount || 0)
      this.refundForm = {
        amount: refundable > 0 ? Number(refundable.toFixed(2)) : null,
        date: this.currentDateValue(),
        remark: ''
      }
      this.refundDialog = true
    },
    submitRefund() {
      if (!this.billingTreatment || !this.billingTreatment.id) {
        this.$message.warning('处置记录不存在')
        return
      }
      if (!this.refundForm.amount || Number(this.refundForm.amount) <= 0) {
        this.$message.warning('退款金额必须大于0')
        return
      }
      if (!this.refundForm.date) {
        this.$message.warning('退款日期不能为空')
        return
      }
      axios.post(`/treatments/refund/${this.billingTreatment.id}`, this.refundForm).then(res => {
        if (res.data.code === '200') {
          this.$message.success('退款成功')
          this.refundDialog = false
          this.load360()
        } else {
          this.$message.error(res.data.msg || '退款失败')
        }
      }).catch(error => {
        this.$message.error((error.response && error.response.data && error.response.data.msg) || '退款失败')
      })
    },
    // 标签
    openAddTag() {
      this.tagForm = { patient_id: Number(this.patientId), tag_name: '', tag_code: '', risk_level: 1, note: '' }
      this.tagDialog = true
    },
    submitTag() {
      axios.post('/risk-tags/add', this.tagForm).then(res => {
        if (res.data.code === '200') { this.$message.success('添加成功'); this.tagDialog = false; this.load360() }
        else this.$message.error(res.data.msg)
      })
    },
    deleteTag(id) {
      axios.delete(`/risk-tags/delete/${id}`).then(() => { this.load360() })
    },
    openWechatBindLink() {
      if (!this.wechatBindUrl) {
        this.$message.warning('当前患者已绑定微信或绑定链接不可用')
        return
      }
      window.open(this.wechatBindUrl, '_blank')
    },
    copyWechatBindLink() {
      if (!this.wechatBindUrl) {
        this.$message.warning('当前患者已绑定微信或绑定链接不可用')
        return
      }
      const text = this.wechatBindUrl
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('绑定链接已复制')
        }).catch(() => {
          this.fallbackCopyText(text)
        })
        return
      }
      this.fallbackCopyText(text)
    },
    fallbackCopyText(text) {
      const input = document.createElement('textarea')
      input.value = text
      input.setAttribute('readonly', 'readonly')
      input.style.position = 'absolute'
      input.style.left = '-9999px'
      document.body.appendChild(input)
      input.select()
      try {
        document.execCommand('copy')
        this.$message.success('绑定链接已复制')
      } catch (error) {
        this.$message.error('复制失败，请手动复制链接')
      } finally {
        document.body.removeChild(input)
      }
    },
    formatAppointmentPurpose(value) {
      if (value === null || value === undefined) return ''
      const text = String(value).trim()
      if (!text) return ''
      if (/^历史异常值-\d+$/.test(text)) return '历史异常数据（待人工核对）'
      if (/^\d+$/.test(text)) return '异常预约目的（待人工核对）'
      return text
    },
    formatDate(d) {
      if (!d) return ''
      const date = new Date(d); if (isNaN(date.getTime())) return ''
      return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '-')
    },
    formatDateTime(value) {
      if (!value) return ''
      return String(value).slice(0, 19).replace('T', ' ')
    },
    formatDateTimeValue(value) {
      if (!value) return ''
      const date = new Date(value)
      if (isNaN(date.getTime())) {
        const text = String(value).replace('T', ' ')
        const matched = text.match(/^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})(?::\d{2})?/)
        return matched ? `${matched[1]}:00` : ''
      }
      const pad = part => String(part).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },
    formatAge(patient) {
      const age = getPatientAge(patient)
      return age === '' ? '-' : `${age}岁`
    },
    hasRelatedPatient(patient) {
      return !!(patient
        && patient.related_patient_id
        && Number(patient.related_patient_id) > 0
        && String(patient.related_patient_name || '').trim())
    },
    relationTypeLabel(patient) {
      return patient && patient.relation_type ? String(patient.relation_type).trim() : ''
    },
    formatPatientRelation(patient) {
      if (!patient) return '-'
      const relationType = this.relationTypeLabel(patient)
      const relatedPatientName = String(patient.related_patient_name || '').trim()
      if (relationType && relatedPatientName) return `${relationType}：${relatedPatientName}`
      if (relationType) return relationType
      if (relatedPatientName) return relatedPatientName
      return '-'
    },
    openRelatedPatient(patient) {
      if (!this.hasRelatedPatient(patient)) return
      const relatedPatient = {
        id: Number(patient.related_patient_id),
        name: String(patient.related_patient_name || '').trim()
      }
      rememberRecentPatient(relatedPatient)
      this.$router.push({
        path: '/Patient360',
        query: {
          id: relatedPatient.id,
          name: relatedPatient.name
        }
      })
    },
    currentDateValue() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    resolveDefaultTreatmentMedicalRecordId() {
      const first = (this.availableTreatmentMedicalRecords || [])[0]
      return first && first.id ? Number(first.id) : null
    },
    treatmentMedicalRecordOptionLabel(record) {
      if (!record) return ''
      const visitDate = this.formatDate(record.visit_date) || '未设日期'
      const diagnosis = String(record.diagnosis || '').trim()
      const operationSummary = String(record.operation_summary || '').trim()
      return [visitDate, diagnosis || operationSummary || '病历记录'].filter(Boolean).join('｜')
    },
    currentDateTimeValue() {
      const now = new Date()
      const pad = value => String(value).padStart(2, '0')
      return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    formatTime(value) {
      const text = String(value || '').trim()
      if (!text) return ''
      if (text.endsWith(':00:00')) return text.slice(0, -3)
      if (text.length === 8 && text.endsWith(':00')) return text.slice(0, 5)
      return text
    },
    appointmentStatusType(status) {
      if (status === '已取消') return 'danger'
      if (status === '已完成' || status === '完成' || status === '已治疗') return 'success'
      if (status === '已就诊') return 'success'
      if (status === '已离开') return 'info'
      if (status === '已改约') return ''
      return 'warning'
    },
    followupStatusLabel(item) {
      return String((item && item.summary) || '').trim() ? '已回访' : '待回访'
    },
    followupStatusType(item) {
      return this.followupStatusLabel(item) === '已回访' ? 'success' : 'warning'
    },
    billingStatusType(status) {
      if (status === '待收费' || status === '欠费') return 'warning'
      if (status === '已收费') return 'success'
      if (status === '部分退款') return 'info'
      if (status === '已退款' || status === '已取消') return 'danger'
      return ''
    },
    consentStatusType(status) {
      return String(status || '').trim() === '已签署' ? 'success' : 'warning'
    },
    recordStatusTagType(status) {
      return String(status || 'final') === 'draft' ? 'warning' : 'success'
    },
    recordStatusLabel(status) {
      return String(status || 'final') === 'draft' ? '暂存' : '已保存'
    },
    typeColor(type) {
      return { '预约': 'primary', '就诊': 'success', '治疗': 'warning', '随访': 'info', '预警': 'danger', '知情同意': 'primary' }[type] || 'primary'
    }
  }
}
</script>

<style scoped>
.p360-wrap { display:flex; flex-direction:column; height:100%; padding:0; background:#f5f7fa; }
.p360-topbar { display:flex; align-items:center; gap:10px; padding:10px 16px; background:#fff; border-bottom:1px solid #e4e7ed; flex-shrink:0; }
.p360-title { font-size:15px; font-weight:700; color:#303133; flex:1; }
.p360-loading { text-align:center; padding:80px; font-size:32px; color:#409EFF; }
.p360-body { flex:1; overflow:auto; padding:12px 16px; }

::v-deep .record-workbench-dialog {
  max-width: calc(100vw - 32px);
}

::v-deep .record-workbench-dialog .el-dialog__body {
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  padding: 12px 18px 16px;
  background: #f8fbff;
}

.editor-head-card,
.template-card,
.record-sheet-card {
  border-radius: 18px;
}

.editor-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-kicker {
  color: #64748b;
  font-size: 13px;
}

.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.editor-head h2 {
  margin: 6px 0 8px;
  color: #0f172a;
  font-size: 24px;
}

.editor-head p {
  margin: 0;
  color: #94a3b8;
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

.editor-main {
  min-width: 0;
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

.required-label::before {
  content: '*';
  color: #f56c6c;
  margin-right: 4px;
}

.patient-suggest-wrap {
  position: relative;
}

.patient-suggestion-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 20;
  margin-top: 6px;
  background: #fff;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  box-shadow: 0 12px 24px rgba(15, 23, 42, .08);
  max-height: 240px;
  overflow: auto;
}

.patient-suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
}

.patient-suggestion-item + .patient-suggestion-item {
  border-top: 1px solid #eff6ff;
}

.patient-suggestion-item:hover {
  background: #f8fbff;
}

.patient-suggestion-name {
  color: #0f172a;
  font-weight: 700;
}

.patient-suggestion-meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.p360-info-row { display:flex; gap:12px; margin-bottom:10px; }
.p360-card-info { flex:1; }
.p360-card-info .card-header { font-size:13px; display:flex; align-items:center; justify-content:space-between; gap:12px; }
.relation-link-btn { padding:0; font-size:12px; font-weight:700; }
.arrears-banner { margin-top:12px; display:flex; align-items:center; gap:10px; color:#b45309; font-size:13px; }
.wechat-bind-panel { margin-top:12px; padding:14px; border:1px solid #ebeef5; border-radius:8px; background:#fafcff; }
.wechat-bind-header { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; margin-bottom:10px; }
.wechat-bind-title { font-size:14px; font-weight:600; color:#303133; }
.wechat-bind-desc { margin-top:4px; font-size:12px; color:#909399; }
.wechat-bind-body { display:flex; align-items:center; gap:16px; }
.wechat-qr-box { width:220px; height:220px; padding:10px; background:#fff; border:1px dashed #dcdfe6; border-radius:8px; display:flex; align-items:center; justify-content:center; flex-shrink:0; }
.wechat-qr-box img { width:100%; height:100%; object-fit:contain; }
.wechat-qr-placeholder { color:#909399; font-size:12px; }
.wechat-bind-actions { display:flex; flex-direction:column; gap:10px; align-items:flex-start; }
.wechat-bind-tip { font-size:12px; line-height:1.6; color:#606266; }
.wechat-bind-bound-tip { margin-top:8px; padding:12px; border-radius:6px; background:#f0f9eb; color:#67c23a; font-size:13px; }
.p360-stats { display:flex; gap:8px; flex-shrink:0; }
.stat-card { background:#fff; border-radius:6px; padding:12px 16px; text-align:center; min-width:90px;
  border:1px solid #e4e7ed; display:flex; flex-direction:column; justify-content:center; }
.stat-num { font-size:22px; font-weight:700; color:#409EFF; line-height:1.2; }
.stat-num.fee { font-size:16px; color:#E6A23C; }
.stat-num.date { font-size:13px; color:#67C23A; }
.stat-label { font-size:11px; color:#909399; margin-top:4px; }

.p360-tags-row { background:#fff; border-radius:6px; border:1px solid #e4e7ed; padding:8px 14px; margin-bottom:10px;
  display:flex; align-items:center; flex-wrap:wrap; gap:4px; }
.tag-label { font-size:13px; color:#606266; margin-right:4px; }

.p360-tabs { border-radius:6px; }
.tab-toolbar { margin-bottom:10px; display:flex; align-items:center; gap:8px; }
.tab-toolbar--records { justify-content:space-between; }
.full-table { width:100%; }
.empty-tip { text-align:center; color:#909399; padding:40px 0; font-size:13px; }
.record-cell-text { white-space:pre-wrap; line-height:1.6; }
.record-expand-box { padding:6px 10px 2px; }
.record-expand-grid { display:grid; grid-template-columns:repeat(3, minmax(0, 1fr)); gap:10px; }
.record-expand-item { padding:10px 12px; border:1px solid #e5e7eb; border-radius:10px; background:#f8fbff; display:flex; flex-direction:column; gap:6px; }
.record-expand-item span { font-size:12px; color:#64748b; }
.record-expand-item strong { color:#0f172a; font-size:14px; }
.record-expand-section { margin-top:10px; padding:12px; border-radius:10px; background:#fff; border:1px solid #ebeef5; }
.record-expand-label { font-size:12px; color:#64748b; margin-bottom:6px; }
.record-expand-value { white-space:pre-wrap; line-height:1.7; color:#0f172a; }
.record-tab-indicator { display:flex; align-items:center; gap:8px; }
.record-tab-indicator__label { font-size:12px; color:#ef4444; }
.record-template-panel { margin:8px 0 18px; padding:16px; border-radius:16px; border:1px solid #dbeafe; background:#f8fbff; }
.record-template-panel__head { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; margin-bottom:12px; }
.record-template-panel__title { color:#0f172a; font-size:15px; font-weight:700; }
.record-template-panel__tip { margin-top:4px; color:#64748b; font-size:12px; line-height:1.6; }
.record-template-panel__row { display:flex; flex-wrap:wrap; gap:10px; align-items:center; }
.record-operation-panel { margin:8px 0 18px; padding:16px; border-radius:16px; border:1px solid #e2e8f0; background:#f8fafc; }
.record-operation-panel__head { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; margin-bottom:12px; }
.record-operation-panel__title { color:#0f172a; font-size:16px; font-weight:700; }
.record-operation-panel__tip { margin-top:4px; color:#64748b; font-size:12px; line-height:1.6; }
.record-operation-panel__quick { margin-bottom:12px; }
.record-operation-suggestion-list { display:flex; flex-wrap:wrap; gap:8px; min-height:32px; }
.record-operation-suggestion-empty { min-height:32px; display:flex; align-items:center; color:#94a3b8; font-size:12px; }
.record-operation-list { display:flex; flex-direction:column; gap:12px; }
.record-operation-item { background:#fff; border:1px solid #dbeafe; border-radius:14px; padding:12px; }
.record-operation-item__head { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; color:#0f172a; font-weight:700; }
.record-operation-item__actions { display:flex; gap:8px; align-items:center; }
.record-operation-empty { color:#94a3b8; font-size:12px; padding:8px 0 2px; }
.record-treatment-toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.record-treatment-toolbar__hint { color:#64748b; font-size:12px; }
.treatment-batch-form { padding-bottom:8px; border-bottom:1px solid #ebeef5; }
.treatment-batch-list { margin-top:16px; display:flex; flex-direction:column; gap:12px; }
.treatment-batch-list__head { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; }
.treatment-batch-list__title { font-size:15px; font-weight:700; color:#303133; }
.treatment-batch-list__tip { margin-top:4px; font-size:12px; color:#909399; line-height:1.6; }
.treatment-item-card { border:1px solid #e4e7ed; border-radius:12px; background:#fafcff; padding:14px 14px 2px; }
.treatment-item-card__head { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; color:#303133; font-weight:600; }
.treatment-selected-project { min-height:40px; padding:10px 12px; border-radius:8px; background:#f8fafc; border:1px solid #e5e7eb; color:#0f172a; line-height:1.5; }
.treatment-batch-footer { width:100%; display:flex; align-items:flex-end; justify-content:space-between; gap:16px; }
.treatment-price-summary { margin-left:auto; min-width:320px; padding:12px 14px; border:1px solid #dbeafe; border-radius:12px; background:#f8fbff; display:flex; flex-direction:column; gap:10px; }
.treatment-price-summary__line { display:flex; align-items:center; justify-content:space-between; gap:12px; font-size:13px; color:#475569; }
.treatment-price-summary__line strong { font-size:18px; color:#0f172a; }
.treatment-price-summary__input { display:flex; align-items:center; justify-content:flex-end; gap:8px; }
.treatment-price-summary__input em { font-style:normal; color:#64748b; }
.treatment-price-summary__tip { font-size:12px; color:#64748b; line-height:1.6; }
.treatment-batch-footer__actions { display:flex; align-items:center; gap:8px; }

.billing-summary { background:#fafafa; border:1px solid #ebeef5; border-radius:6px; padding:14px 20px; margin-bottom:14px; }
.billing-stat { text-align:center; }
.bs-val { font-size:24px; font-weight:700; color:#409EFF; display:block; }
.bs-val.fee { color:#E6A23C; font-size:20px; }
.bs-val.warn { color:#F56C6C; }
.bs-val.danger { color:#b91c1c; font-size:20px; }
.bs-label { font-size:12px; color:#909399; }
.charge-summary-box { padding:10px 12px; border-radius:10px; background:#f8fbff; border:1px solid #dbeafe; }
.charge-summary-box__title { color:#0f172a; font-weight:700; line-height:1.5; }
.charge-summary-box__desc { margin-top:6px; color:#64748b; line-height:1.7; white-space:pre-wrap; }
.charge-channel-list { display:flex; flex-direction:column; gap:10px; width:100%; }
.charge-channel-row { display:flex; align-items:center; gap:10px; }
.charge-channel-footer { display:flex; align-items:center; justify-content:space-between; color:#64748b; font-size:12px; }

.image-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(180px, 1fr)); gap:12px; padding:4px 0; }
.image-card { border:1px solid #e4e7ed; border-radius:8px; overflow:hidden; background:#fff;
  transition:box-shadow .2s; }
.image-card:hover { box-shadow:0 4px 12px rgba(0,0,0,.12); }
.image-thumb { height:140px; overflow:hidden; cursor:pointer; background:#f5f7fa;
  display:flex; align-items:center; justify-content:center; }
.image-thumb img { width:100%; height:100%; object-fit:cover; }
.file-icon { font-size:48px; color:#909399; }
.image-meta { padding:8px 10px 4px; }
.image-name { font-size:12px; font-weight:600; color:#303133; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.image-info { display:flex; align-items:center; gap:6px; margin-top:4px; }
.image-date { font-size:11px; color:#909399; }
.image-notes { font-size:11px; color:#606266; margin-top:4px; }
.image-actions { padding:4px 8px 8px; display:flex; justify-content:space-around; border-top:1px solid #f2f2f2; }

.consent-preview-box { display:flex; flex-direction:column; gap:14px; }
.consent-preview-meta { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:10px; }
.consent-preview-item { padding:12px; border-radius:14px; background:#f8fbff; }
.consent-preview-item span { display:block; color:#8b95a7; font-size:12px; }
.consent-preview-item strong { display:block; margin-top:6px; color:#303133; font-size:13px; line-height:1.7; word-break:break-word; }
.consent-preview-content { max-height:320px; overflow-y:auto; border-radius:16px; border:1px solid #dbeafe; background:#fff; padding:14px; color:#334155; line-height:1.9; white-space:pre-wrap; }
.consent-preview-sign { padding:14px; border-radius:16px; background:#f8fbff; border:1px solid #dbeafe; }
.consent-preview-sign__title { font-size:14px; font-weight:700; color:#303133; }
.consent-preview-sign__meta { margin-top:8px; color:#64748b; font-size:12px; line-height:1.7; }
.consent-preview-sign__image { width:100%; max-width:320px; margin-top:10px; border-radius:12px; border:1px solid #dbeafe; background:#fff; }

.timeline-content { background:#fff; border:1px solid #e4e7ed; border-radius:6px; padding:10px 14px; }
.timeline-detail { font-size:12px; color:#606266; margin-top:6px; }

@media (max-width: 1280px) {
  .tab-toolbar--records,
  .record-operation-panel__head,
  .record-treatment-toolbar,
  .operation-panel__head,
  .treatment-draft-toolbar,
  .editor-head {
    flex-direction:column;
    align-items:flex-start;
  }
  .editor-layout {
    grid-template-columns: 1fr;
  }
  .treatment-batch-footer {
    flex-direction:column;
    align-items:stretch;
  }
  .treatment-price-summary {
    min-width:0;
    width:100%;
  }
  .treatment-batch-footer__actions {
    justify-content:flex-end;
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
}
</style>

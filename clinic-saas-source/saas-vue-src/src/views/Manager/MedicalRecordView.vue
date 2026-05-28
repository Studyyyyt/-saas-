<template>
  <div class="page-wrap medical-record-page">
    <template v-if="editorVisible">
      <el-form ref="formRef" :model="form" :rules="rules" class="editor-form" @submit.native.prevent>
        <!-- 页面标题栏 -->
        <div class="page-header">
          <div class="page-header__main">
            <div class="page-kicker">今日工作</div>
            <h2 class="page-title">{{ dialogTitle }}</h2>
            <p class="page-subtitle">{{ currentPatientSummary }}</p>
          </div>
          <div class="page-header__actions">
            <el-tag size="small" :type="recordStatusTagType(form.record_status)">{{ recordStatusLabel(form.record_status) }}</el-tag>
            <el-button v-if="medicalRecordAgentVisible" type="primary" plain round icon="el-icon-magic-stick" :loading="aiExpanding" @click="expandWithAI">AI 扩写</el-button>
            <el-button plain round @click="closeEditor">返回列表</el-button>
          </div>
        </div>

        <div class="editor-layout apple-page-enter">
          <div class="editor-main">
            <!-- 基础信息区块 -->
            <el-card class="section-card apple-page-enter-delay-1" shadow="never">
              <div class="section-header">
                <div class="section-header__main">
                  <div class="section-header__bar"></div>
                  <i class="el-icon-user-solid section-header__icon"></i>
                  <span class="section-header__title">基础信息</span>
                </div>
              </div>
              <div class="section-body">
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="患者ID" prop="patient_id">
                      <el-input
                        v-model="form.patient_id"
                        placeholder="请输入患者ID"
                        round
                        @blur="handlePatientIdentityChange"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="患者姓名" prop="patient_name">
                      <el-input
                        v-model="form.patient_name"
                        placeholder="请输入患者姓名"
                        round
                        @blur="handlePatientIdentityChange"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="接诊医生" prop="doctor_account_id">
                      <el-select v-model="form.doctor_account_id" filterable placeholder="请选择接诊医生" style="width:100%">
                        <el-option v-for="doctor in doctors" :key="doctor.id" :label="doctor.name" :value="doctor.id" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="护士">
                      <el-input v-model="form.nurse_name" placeholder="可选" round />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="助理">
                      <el-input v-model="form.assistant_name" placeholder="可选" round />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="就诊时间" prop="visit_date">
                      <el-date-picker
                        v-model="form.visit_date"
                        type="datetime"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        placeholder="请选择就诊时间"
                        style="width:100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-form-item label="病历类型">
                      <el-radio-group v-model="form.record_type" size="small">
                        <el-radio-button label="初诊" />
                        <el-radio-button label="复诊" />
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <!-- AI 诊疗场景选择器：用户选择病种后，AI 扩写会按照该病种的规范步骤生成病历内容 -->
                    <el-form-item label="AI 诊疗场景">
                      <div style="display:flex;gap:8px;align-items:center;">
                        <el-tooltip content="选择病种后，AI 扩写将按照该病种的规范步骤生成病历内容。点击右侧设置图标可新增或删除病种。" placement="top" style="flex:1;">
                          <el-select v-model="aiSceneId" clearable placeholder="请选择病种" style="width:100%" size="small" @change="onAiSceneChange">
                            <el-option v-for="scene in scenes" :key="scene.id" :label="scene.name" :value="scene.id" />
                          </el-select>
                        </el-tooltip>
                        <el-button type="default" size="small" circle icon="el-icon-setting" title="管理病种" style="flex-shrink:0;" @click="openSceneManageDialog" />
                      </div>
                      <!-- 步骤选择器：当病种包含多个步骤时显示，用于告知 n8n 当前处理的是哪一步 -->
                      <div v-if="aiSceneSteps.length > 0" style="margin-top:8px;">
                        <div style="font-size:12px;color:var(--apple-text-secondary);margin-bottom:4px;">当前步骤</div>
                        <el-radio-group v-model="aiSelectedStepId" size="mini">
                          <el-radio-button v-for="step in aiSceneSteps" :key="step.id" :label="step.id">{{ step.name }}</el-radio-button>
                        </el-radio-group>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <div class="toolbar-switch">
                      <span>牙位同步</span>
                      <el-switch v-model="editorFlags.autoSyncToothPositions" />
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-card>

            <!-- 主诉与现病史区块 -->
            <el-card class="section-card apple-page-enter-delay-2" shadow="never">
              <div class="section-header">
                <div class="section-header__main">
                  <div class="section-header__bar"></div>
                  <i class="el-icon-s-order section-header__icon"></i>
                  <span class="section-header__title">主诉与现病史</span>
                </div>
              </div>
              <div class="section-body">
                <el-form-item prop="chief_complaint" label="主诉" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-chief_complaint" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['chief_complaint']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'chief_complaint', true)" @mouseleave="$set(phrasePopoverHover, 'chief_complaint', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 主诉</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('chief_complaint')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('chief_complaint')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('chief_complaint', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'chief_complaint')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['chief_complaint'] || !phraseData['chief_complaint'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['chief_complaint']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['chief_complaint']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('chief_complaint')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.chief_complaint" type="textarea" :rows="2" placeholder="请输入主诉" @focus="handlePhraseFocus('chief_complaint')" @blur="(e) => handlePhraseBlur('chief_complaint', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
                <el-form-item label="现病史" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-present_illness_history" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['present_illness_history']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'present_illness_history', true)" @mouseleave="$set(phrasePopoverHover, 'present_illness_history', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 现病史</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('present_illness_history')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('present_illness_history')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('present_illness_history', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'present_illness_history')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['present_illness_history'] || !phraseData['present_illness_history'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['present_illness_history']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['present_illness_history']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('present_illness_history')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.present_illness_history" type="textarea" :rows="2" placeholder="请输入现病史" @focus="handlePhraseFocus('present_illness_history')" @blur="(e) => handlePhraseBlur('present_illness_history', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
                <el-form-item label="既往史" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-past_history" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['past_history']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'past_history', true)" @mouseleave="$set(phrasePopoverHover, 'past_history', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 既往史</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('past_history')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('past_history')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('past_history', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'past_history')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['past_history'] || !phraseData['past_history'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['past_history']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['past_history']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('past_history')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.past_history" type="textarea" :rows="2" placeholder="请输入既往史" @focus="handlePhraseFocus('past_history')" @blur="(e) => handlePhraseBlur('past_history', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
                <el-row :gutter="20">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="流行病史" label-width="80px">
                      <el-input v-model="form.infectious_history" type="textarea" :rows="2" placeholder="可选" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="过敏史" label-width="80px">
                      <el-input v-model="form.allergy_history" type="textarea" :rows="2" placeholder="可选" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item label="一般情况" label-width="80px">
                  <el-input v-model="form.general_condition" placeholder="例如：体健" round />
                </el-form-item>
              </div>
            </el-card>

            <!-- 检查与诊断区块 -->
            <el-card class="section-card apple-page-enter-delay-3" shadow="never">
              <div class="section-header">
                <div class="section-header__main">
                  <div class="section-header__bar"></div>
                  <i class="el-icon-search section-header__icon"></i>
                  <span class="section-header__title">检查与诊断</span>
                </div>
              </div>
              <div class="section-body">
                <el-form-item label="检查" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-examination" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['examination']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'examination', true)" @mouseleave="$set(phrasePopoverHover, 'examination', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 检查</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('examination')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('examination')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('examination', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'examination')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['examination'] || !phraseData['examination'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['examination']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['examination']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('examination')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.examination" type="textarea" :rows="3" placeholder="请输入检查内容" @focus="handlePhraseFocus('examination')" @blur="(e) => handlePhraseBlur('examination', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
                <el-form-item label="辅助检查" label-width="80px">
                  <el-input v-model="form.auxiliary_examination" type="textarea" :rows="2" placeholder="请输入辅助检查内容" />
                </el-form-item>
                <el-form-item prop="diagnosis" label="诊断" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-diagnosis" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['diagnosis']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'diagnosis', true)" @mouseleave="$set(phrasePopoverHover, 'diagnosis', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 诊断</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('diagnosis')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('diagnosis')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('diagnosis', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'diagnosis')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['diagnosis'] || !phraseData['diagnosis'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['diagnosis']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['diagnosis']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('diagnosis')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断结论" @focus="handlePhraseFocus('diagnosis')" @blur="(e) => handlePhraseBlur('diagnosis', e)" />
                    </el-popover>
                  </div>
                </el-form-item>

                <!-- 牙位选择 -->
                <el-form-item label="牙位" label-width="80px">
                  <ToothSelector v-model="form.tooth_positions" />
                  <div class="sheet-field-hint">请直接勾选或输入牙位。</div>
                </el-form-item>
              </div>
            </el-card>

            <!-- 治疗计划区块 -->
            <el-card class="section-card apple-page-enter-delay-5" shadow="never">
              <div class="section-header">
                <div class="section-header__main">
                  <div class="section-header__bar"></div>
                  <i class="el-icon-s-claim section-header__icon"></i>
                  <span class="section-header__title">治疗计划</span>
                </div>
              </div>
              <div class="section-body">
                <el-form-item label="治疗方案" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-treatment_plan" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['treatment_plan']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'treatment_plan', true)" @mouseleave="$set(phrasePopoverHover, 'treatment_plan', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 治疗方案</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('treatment_plan')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('treatment_plan')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('treatment_plan', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'treatment_plan')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['treatment_plan'] || !phraseData['treatment_plan'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['treatment_plan']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['treatment_plan']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('treatment_plan')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.treatment_plan" type="textarea" :rows="2" placeholder="请输入治疗方案" @focus="handlePhraseFocus('treatment_plan')" @blur="(e) => handlePhraseBlur('treatment_plan', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
                <el-form-item label="治疗文稿" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-treatment" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['treatment']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'treatment', true)" @mouseleave="$set(phrasePopoverHover, 'treatment', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 治疗文稿</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('treatment')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('treatment')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('treatment', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'treatment')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['treatment'] || !phraseData['treatment'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['treatment']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['treatment']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('treatment')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.treatment" type="textarea" :rows="3" placeholder="治疗文稿" @focus="handlePhraseFocus('treatment')" @blur="(e) => handlePhraseBlur('treatment', e)" @input="handleTreatmentInput" />
                    </el-popover>
                  </div>
                </el-form-item>
              </div>
            </el-card>

            <!-- 医嘱区块 -->
            <el-card class="section-card apple-page-enter-delay-6" shadow="never">
              <div class="section-header">
                <div class="section-header__main">
                  <div class="section-header__bar"></div>
                  <i class="el-icon-first-aid-kit section-header__icon"></i>
                  <span class="section-header__title">医嘱与其他</span>
                </div>
              </div>
              <div class="section-body">
                <el-form-item label="医嘱" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-medical_advice" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['medical_advice']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'medical_advice', true)" @mouseleave="$set(phrasePopoverHover, 'medical_advice', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 医嘱</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('medical_advice')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('medical_advice')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('medical_advice', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'medical_advice')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['medical_advice'] || !phraseData['medical_advice'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['medical_advice']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['medical_advice']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('medical_advice')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.medical_advice" type="textarea" :rows="2" placeholder="请输入医嘱" @focus="handlePhraseFocus('medical_advice')" @blur="(e) => handlePhraseBlur('medical_advice', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
                <el-form-item label="处方" label-width="80px">
                  <el-input v-model="form.prescription" type="textarea" :rows="2" placeholder="请输入处方内容" />
                </el-form-item>
                <el-form-item label="病历标签" label-width="80px">
                  <el-input v-model="form.record_tags" placeholder="多个标签请用逗号分隔，例如：牙周, 急诊" round />
                  <div v-if="recordTagList.length" class="sheet-tag-list">
                    <el-tag v-for="tag in recordTagList" :key="tag" size="mini" effect="plain">{{ tag }}</el-tag>
                  </div>
                </el-form-item>
                <el-form-item label="影像说明" label-width="80px">
                  <el-input v-model="form.image_summary" type="textarea" :rows="2" placeholder="可填写影像摘要或说明" />
                </el-form-item>
                <el-form-item label="添加影像" label-width="80px">
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
                </el-form-item>
                <el-form-item label="病历备注" label-width="80px">
                  <div class="phrase-input-wrap">
                    <el-popover append-to-body ref="phrasePopover-notes" placement="bottom-start" width="340" trigger="manual" v-model="phrasePopoverVisible['notes']">
                      <div class="phrase-popover" @mouseenter="$set(phrasePopoverHover, 'notes', true)" @mouseleave="$set(phrasePopoverHover, 'notes', false)">
                        <div class="phrase-popover__title">
                          <span>常用短语 — 病历备注</span>
                          <i class="el-icon-close phrase-popover__close" @click.stop="closePhrasePopover('notes')"></i>
                        </div>
                        <div v-for="(items, category) in groupedPhrases('notes')" :key="category" class="phrase-popover__group">
                          <div class="phrase-popover__category">{{ category }}</div>
                          <div class="phrase-popover__list">
                            <div v-for="item in items" :key="item.id" class="phrase-item">
                              <el-button size="mini" type="text" @click="insertPhrase('notes', item.content)">{{ item.content }}</el-button>
                              <el-button size="mini" type="text" style="color:#ef4444" @click="deletePhrase(item.id, 'notes')"><i class="el-icon-delete" /></el-button>
                            </div>
                          </div>
                        </div>
                        <div class="phrase-popover__empty" v-if="!phraseData['notes'] || !phraseData['notes'].length">暂无词条，请在下方添加</div>
                        <div class="phrase-popover__add">
                          <el-input v-model="phraseNewCategory['notes']" size="mini" placeholder="分类（如根管治疗）" />
                          <div style="display:flex;gap:6px;margin-top:6px;">
                            <el-input v-model="phraseNewContent['notes']" size="mini" placeholder="添加新词条" style="flex:1" />
                            <el-button size="mini" type="primary" @click="addPhrase('notes')">添加</el-button>
                          </div>
                        </div>
                      </div>
                      <el-input slot="reference" v-model="form.notes" type="textarea" :rows="3" placeholder="记录特殊病例要点、随访注意事项、患者特殊需求等，后续可由 AI 提取分析" @focus="handlePhraseFocus('notes')" @blur="(e) => handlePhraseBlur('notes', e)" />
                    </el-popover>
                  </div>
                </el-form-item>
              </div>
            </el-card>
          </div>

          <aside class="editor-sidebar">
            <!-- 模板库 -->
            <el-card class="template-card section-card" shadow="never">
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
                round
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
                <el-button type="success" plain round @click="saveCurrentAsTemplate">保存为模板</el-button>
                <el-button plain round :disabled="!selectedTemplateId" @click="deleteSelectedTemplate">删除模板</el-button>
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
          </aside>
        </div>

        <!-- 底部操作栏 -->
        <div class="editor-footer">
          <el-button round @click="closeEditor">退出</el-button>
          <el-button plain round @click="handleSubmit('draft')">暂存</el-button>
          <el-button type="success" plain round @click="saveCurrentAsTemplate">另存为模板</el-button>
          <el-button type="primary" round @click="handleSubmit('final')">保存</el-button>
        </div>
      </el-form>

      <el-dialog :visible.sync="previewVisible" :title="previewImageItem ? previewImageItem.image_name : '影像预览'" width="68%" top="6vh">
        <div v-if="previewImageItem" class="preview-dialog-body">
          <img v-if="isImageFile(previewImageItem)" :src="patientImageUrl(previewImageItem)" :alt="previewImageItem.image_name" class="preview-dialog-image" />
          <div v-else class="preview-dialog-file">
            <i class="el-icon-document" />
            <p>{{ previewImageItem.image_name }}</p>
            <el-button type="primary" round @click="openPatientImage(previewImageItem)">打开文件</el-button>
          </div>
        </div>
      </el-dialog>
    </template>

    <!-- 列表视图：病历工作台 -->
    <template v-else>
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="page-header__main">
          <div class="page-kicker">今日工作</div>
          <h2 class="page-title">今日工作概览</h2>
          <p class="page-subtitle">跟踪今日就诊流程、待办事项与历史病历速查。</p>
        </div>
      </div>

      <!-- 概览统计卡片 -->
      <div class="stats-row">
        <div class="stat-card" @click="switchTab('today')">
          <div class="stat-icon" style="background: var(--apple-orange-light); color: var(--apple-orange);"><i class="el-icon-edit-outline" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ todayPendingCount }}</div>
            <div class="stat-label">今日待写</div>
          </div>
        </div>
        <div class="stat-card" @click="switchTab('history'); filterDraft()">
          <div class="stat-icon" style="background: var(--apple-orange-light); color: var(--apple-orange);"><i class="el-icon-document-copy" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ draftCount }}</div>
            <div class="stat-label">暂存待完善</div>
          </div>
        </div>
        <div class="stat-card" @click="goToLabOrders">
          <div class="stat-icon" style="background: var(--apple-red-light); color: var(--apple-red);"><i class="el-icon-s-order" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ pendingLabTotal }}</div>
            <div class="stat-label">待登记加工</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: var(--apple-green-light); color: var(--apple-green);"><i class="el-icon-circle-check" /></div>
          <div class="stat-body">
            <div class="stat-num">{{ weekCompletedCount }}</div>
            <div class="stat-label">本周完成</div>
          </div>
        </div>
      </div>

      <!-- 查询条件 -->
      <el-card class="query-card" shadow="never">
        <div class="query-row">
          <el-input v-model="searchName" placeholder="按患者姓名搜索" class="query-input" clearable round @clear="loadAll" />
          <el-radio-group v-model="dateFilter" size="small" @change="handleDateFilterChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="all">全部</el-radio-button>
          </el-radio-group>
          <el-button type="primary" round icon="el-icon-search" @click="search">搜索</el-button>
          <el-button round icon="el-icon-refresh" @click="resetFilters">重置</el-button>
          <el-button type="success" round icon="el-icon-plus" @click="openAdd">新增病历</el-button>
        </div>
      </el-card>

      <!-- 主内容区：双 Tab -->
      <el-tabs v-model="activeListTab" class="workbench-tabs" type="border-card">
        <!-- Tab 1：今日工作流 -->
        <el-tab-pane label="今日工作流" name="today">
          <div class="tab-toolbar">
            <span class="tab-tip">展示今日预约患者及病历书写状态</span>
          </div>
          <el-table
            v-if="todayFlowData.length"
            :data="todayFlowData"
            size="small"
            stripe
            border
            class="workbench-table"
          >
            <el-table-column label="患者" width="140">
              <template slot-scope="s">
                <div class="flow-patient">
                  <el-avatar :size="28" icon="el-icon-user-solid" class="flow-avatar" />
                  <span class="flow-name">{{ s.row.patient_name || '-' }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="预约时间" width="100">
              <template slot-scope="s">{{ s.row.appointment_time || '-' }}</template>
            </el-table-column>
            <el-table-column label="预约项目" min-width="140">
              <template slot-scope="s">
                <span class="text-ellipsis">{{ s.row.appointment_purpose || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="接诊状态" width="110">
              <template slot-scope="s">
                <el-tag :type="clinicStatusType(s.row.clinicStatus)" size="mini">{{ s.row.clinicStatus || '已预约' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="挂号时间" width="100">
              <template slot-scope="s">{{ s.row.checkInTime ? formatTime(s.row.checkInTime) : '-' }}</template>
            </el-table-column>
            <el-table-column label="等待时长" width="100">
              <template slot-scope="s">
                <span v-if="s.row.clinicStatus === '等待中' || s.row.clinicStatus === '已挂号'" class="wait-time">{{ computeWaitDuration(s.row.checkInTime) }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="排队" width="70" align="center">
              <template slot-scope="s">
                <el-badge v-if="s.row.queueNumber" :value="s.row.queueNumber" class="queue-badge" />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="病历状态" width="100">
              <template slot-scope="s">
                <el-tag v-if="s.row.recordStatus === 'none'" size="mini" type="info">未写</el-tag>
                <el-tag v-else-if="s.row.recordStatus === 'draft'" size="mini" type="warning">暂存</el-tag>
                <el-tag v-else size="mini" type="success">已保存</el-tag>
                <el-badge v-if="s.row.pending_lab_count > 0" is-dot type="danger" style="margin-left:4px;" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template slot-scope="s">
                <el-dropdown size="mini" split-button type="primary" plain trigger="click" @command="(cmd) => updateClinicStatus(s.row, cmd)">
                  {{ s.row.clinicStatus || '已预约' }}
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="已预约">已预约</el-dropdown-item>
                    <el-dropdown-item command="已挂号">已挂号</el-dropdown-item>
                    <el-dropdown-item command="等待中">等待中</el-dropdown-item>
                    <el-dropdown-item command="就诊中">就诊中</el-dropdown-item>
                    <el-dropdown-item command="已完成">已完成</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
                <el-button v-if="s.row.recordStatus === 'none'" size="mini" type="warning" plain round @click="openAddFollowupForPatient(s.row)">新增回访</el-button>
                <el-button v-else size="mini" type="primary" plain round @click="openEditByRecord(s.row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="今日暂无预约，可点击上方「新增病历」直接书写" />
        </el-tab-pane>

        <!-- Tab 2：历史速查 -->
        <el-tab-pane label="历史速查" name="history">
          <div class="tab-toolbar">
            <span class="tab-tip">按时间倒序浏览历史病历，支持展开查看详情</span>
          </div>
          <el-table
            v-if="filteredTableData.length"
            :data="filteredTableData"
            size="small"
            stripe
            border
            class="workbench-table"
          >
            <el-table-column type="expand" width="40">
              <template slot-scope="s">
                <div class="record-expand-box">
                  <div class="record-expand-grid">
                    <div class="record-expand-item"><span>就诊日期</span><strong>{{ formatDate(s.row.visit_date) || '-' }}</strong></div>
                    <div class="record-expand-item"><span>接诊医生</span><strong>{{ s.row.doctor_name || '-' }}</strong></div>
                    <div class="record-expand-item"><span>牙位</span><strong>{{ s.row.tooth_positions || '-' }}</strong></div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">检查</div>
                    <div class="record-expand-value">{{ s.row.examination || '无' }}</div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">诊断</div>
                    <div class="record-expand-value">{{ s.row.diagnosis || '无' }}</div>
                  </div>
                  <div class="record-expand-section">
                    <div class="record-expand-label">治疗方案</div>
                    <div class="record-expand-value">{{ s.row.treatment_plan || '无' }}</div>
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
            <el-table-column prop="patient_name" label="患者" width="100" />
            <el-table-column prop="record_type" label="类型" width="70">
              <template slot-scope="s">{{ s.row.record_type || '初诊' }}</template>
            </el-table-column>
            <el-table-column prop="doctor_name" label="医生" width="90" />
            <el-table-column prop="chief_complaint" label="主诉" min-width="140">
              <template slot-scope="s"><div class="record-cell-text">{{ s.row.chief_complaint || '-' }}</div></template>
            </el-table-column>
            <el-table-column prop="diagnosis" label="诊断" min-width="140">
              <template slot-scope="s"><div class="record-cell-text">{{ s.row.diagnosis || '-' }}</div></template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template slot-scope="s">
                <el-tag size="mini" :type="recordStatusTagType(s.row.record_status)">
                  {{ recordStatusLabel(s.row.record_status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="s">
                <el-button size="mini" type="primary" plain round @click="openEdit(s.row)">编辑</el-button>
                <el-button size="mini" type="danger" plain round @click="handleDelete(s.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无病历数据" />
          <div v-if="filteredTotal > 0" class="pagination-row">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :total="filteredTotal"
              :page-size="size"
              :current-page="page"
              @current-change="handlePageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>

    <!-- 诊疗场景管理弹窗：允许医生在病历编辑页直接新增或删除病种，无需跳转到系统设置 -->
    <el-dialog :visible.sync="sceneManageDialogVisible" title="管理 AI 诊疗场景" width="720px" :close-on-click-modal="false" append-to-body :modal="false" custom-class="scene-manage-dialog">
      <!-- 现有病种 -->
      <el-card shadow="never" style="margin-bottom: 20px; border-radius: 12px;">
        <div slot="header" style="display:flex; justify-content:space-between; align-items:center; padding: 10px 0;">
          <span style="font-weight: 600; font-size: 15px;">现有病种</span>
          <el-tag size="mini" type="info">{{ scenes.length }} 个</el-tag>
        </div>
        <el-table :data="scenes" size="small" style="width: 100%" max-height="320" :show-header="true" header-row-class-name="scene-table-header">
          <el-table-column prop="name" label="病种名称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="70" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="mini" class="danger-text" icon="el-icon-delete" @click="deleteScene(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 新增病种 -->
      <el-card shadow="never" style="border-radius: 12px;">
        <div slot="header" style="padding: 10px 0;">
          <span style="font-weight: 600; font-size: 15px;">新增病种</span>
        </div>
        <el-form label-position="top" size="small" style="padding: 4px 0;">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="病种名称">
                <el-input v-model="newSceneForm.name" placeholder="如：根管治疗" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分类">
                <el-input v-model="newSceneForm.category" placeholder="如：牙体牙髓" clearable />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="治疗步骤（可选）">
            <div class="scene-step-list">
              <div v-for="(step, idx) in newSceneForm.steps" :key="idx" class="scene-step-item">
                <span class="scene-step-index">{{ idx + 1 }}</span>
                <el-input v-model="step.name" size="mini" placeholder="步骤名称，如：开髓引流" class="scene-step-input" />
                <i class="el-icon-close scene-step-delete" @click="newSceneForm.steps.splice(idx, 1)"></i>
              </div>
            </div>
            <el-button type="primary" plain size="small" icon="el-icon-plus" style="margin-top: 10px; border-radius: 8px;" @click="newSceneForm.steps.push({ name: '' })">添加步骤</el-button>
          </el-form-item>

          <div style="display: flex; justify-content: flex-end; margin-top: 8px;">
            <el-button size="small" type="primary" icon="el-icon-check" :loading="sceneSaving" style="border-radius: 8px; padding: 9px 20px;" @click="saveNewScene">保存病种</el-button>
          </div>
        </el-form>
      </el-card>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import ToothSelector from '@/components/design-system/ToothSelector.vue'
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
      editorFromQuery: false,
      dialogTitle: '新增病历',
      form: {},
      doctors: [],
      projectOptions: [],
      labFactoryOptions: [],
      medicalRecordTemplateOptions: [],
      projectDetailCache: {},
      selectedProjectId: '',
      selectedTemplateId: null,
      currentUser: getAdminSession() || {},
      lastAutoTreatmentDraft: '',
      treatmentDraftLocked: false,
      templateKeyword: '',
      activeTemplateCategory: DEFAULT_TEMPLATE_CATEGORY,
      patientImages: [],
      patientImagesLoading: false,
      scenes: [],
      aiSceneId: null,
      aiSceneName: '',
      aiSceneSteps: [],
      aiSelectedStepId: null,
      sceneManageDialogVisible: false,
      newSceneForm: { name: '', category: '其他', steps: [] },
      sceneSaving: false,
      selectedSceneId: null,
      selectedOperations: [],
      currentSceneSteps: [],
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
      activeCollapse: ['basic', 'complaint', 'examination', 'operation', 'treatment', 'advice'],
      rules: {
        patient_id: [{ required: true, message: '请填写患者ID', trigger: 'blur' }],
        patient_name: [{ required: true, message: '请填写患者姓名', trigger: 'blur' }],
        doctor_account_id: [{ required: true, message: '请选择接诊医生', trigger: 'change' }],
        visit_date: [{ required: true, message: '请选择就诊时间', trigger: 'change' }]
      },
      activeListTab: 'today',
      dateFilter: 'today',
      scheduleEntries: [],
      todayFlowData: [],
      phraseData: {},
      phraseNewContent: {},
      phraseNewCategory: {},
      phrasePopoverHover: {},
      phrasePopoverVisible: {},
      aiExpanding: false,
      medicalRecordAgentKey: '',
      medicalRecordAgentVisible: false
    }
  },
  computed: {
    pendingLabTotal() {
      return (this.tableData || []).reduce((sum, item) => sum + Number(item.pending_lab_count || 0), 0)
    },
    sceneGroups() {
      const groups = {}
      for (const scene of this.scenes || []) {
        const cat = scene.category || '其他'
        if (!groups[cat]) groups[cat] = []
        groups[cat].push(scene)
      }
      return Object.keys(groups).map(category => ({
        category,
        scenes: groups[category].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      }))
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
    },
    filteredTableData() {
      let data = this.tableData || []
      if (this.searchName) {
        const keyword = String(this.searchName).trim().toLowerCase()
        data = data.filter(item =>
          String(item.patient_name || '').toLowerCase().includes(keyword) ||
          String(item.chief_complaint || '').toLowerCase().includes(keyword) ||
          String(item.diagnosis || '').toLowerCase().includes(keyword)
        )
      }
      if (this.dateFilter && this.dateFilter !== 'all') {
        const now = new Date()
        let start = null
        let end = null
        if (this.dateFilter === 'today') {
          start = new Date(now.getFullYear(), now.getMonth(), now.getDate())
          end = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
        } else if (this.dateFilter === 'week') {
          const day = now.getDay() || 7
          start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - day + 1)
          end = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
        } else if (this.dateFilter === 'month') {
          start = new Date(now.getFullYear(), now.getMonth(), 1)
          end = new Date(now.getFullYear(), now.getMonth() + 1, 1)
        }
        if (start && end) {
          data = data.filter(item => {
            const d = item.visit_date ? new Date(item.visit_date) : null
            return d && d >= start && d < end
          })
        }
      }
      return data
    },
    filteredTotal() {
      return this.filteredTableData.length
    },
    todayPendingCount() {
      return this.todayFlowData.filter(row => row.recordStatus === 'none').length
    },
    draftCount() {
      return (this.tableData || []).filter(item => String(item.record_status || '') === 'draft').length
    },
    weekCompletedCount() {
      const now = new Date()
      const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - (now.getDay() || 7) + 1)
      weekStart.setHours(0, 0, 0, 0)
      return (this.tableData || []).filter(item => {
        const d = item.visit_date ? new Date(item.visit_date) : null
        return d && d >= weekStart && String(item.record_status || '') === 'final'
      }).length
    }
  },
  created() {
    this.loadDoctors()
    this.loadProjectOptions()
    this.loadLabFactoryOptions()
    this.loadMedicalRecordTemplateOptions()
    this.loadAll()
    this.loadScheduleEntries()
    this.loadScenes()
    this.form = this.buildEmptyForm()
    this.imageUploadExtra.imageDate = this.currentDateValue()
    this.handleRouteQuery()
    this.loadAiFunctionMapping()
  },
  updated() {
    this.enableRowResize()
  },
  methods: {
    enableRowResize() {
      this.$nextTick(() => {
        const tables = this.$el.querySelectorAll('.workbench-table')
        tables.forEach(table => {
          const tbody = table.querySelector('.el-table__body-wrapper tbody')
          if (!tbody || tbody.dataset.rowResizeBound) return
          tbody.dataset.rowResizeBound = 'true'
          tbody.addEventListener('mousedown', e => {
            const row = e.target.closest('.el-table__row')
            if (!row) return
            const rect = row.getBoundingClientRect()
            if (e.clientY >= rect.bottom - 6) {
              e.preventDefault()
              const startY = e.clientY
              const startHeight = row.offsetHeight
              const onMove = ev => {
                const newHeight = Math.max(30, startHeight + (ev.clientY - startY))
                row.style.height = newHeight + 'px'
              }
              const onUp = () => {
                document.removeEventListener('mousemove', onMove)
                document.removeEventListener('mouseup', onUp)
              }
              document.addEventListener('mousemove', onMove)
              document.addEventListener('mouseup', onUp)
            }
          })
        })
      })
    },
    async loadAiFunctionMapping() {
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.get('/api/ai/function-mappings/medical-record-expand/agent-key', { params: { accountId } })
        if (res.data && res.data.code === '200' && res.data.data) {
          const data = res.data.data
          // 有绑定 Agent 且绑定不为空，才显示 AI 入口；否则隐藏
          if (data.agentKey && data.agentKey.trim() !== '') {
            this.medicalRecordAgentKey = data.agentKey
            this.medicalRecordAgentVisible = data.isVisibleOnPage === true || data.isVisibleOnPage === 1
          } else {
            this.medicalRecordAgentKey = ''
            this.medicalRecordAgentVisible = false
          }
        } else {
          this.medicalRecordAgentKey = ''
          this.medicalRecordAgentVisible = false
        }
      } catch (error) {
        console.error('加载病历扩写功能映射失败:', error)
        this.medicalRecordAgentKey = ''
        this.medicalRecordAgentVisible = false
      }
    },

    readCurrentUser() {
      return getAdminSession() || {}
    },
    loadAll() {
      this.page = this.page || 1
      const params = { page: this.page, size: this.size }
      const currentDoctorId = this.resolveDefaultDoctorAccountId()
      if (currentDoctorId) {
        params.doctorAccountId = currentDoctorId
      }
      if (this.dateFilter && this.dateFilter !== 'all') {
        const now = new Date()
        if (this.dateFilter === 'today') {
          params.startDate = this.formatDate(now)
          params.endDate = this.formatDate(now)
        } else if (this.dateFilter === 'week') {
          const day = now.getDay() || 7
          const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - day + 1)
          params.startDate = this.formatDate(weekStart)
          params.endDate = this.formatDate(now)
        } else if (this.dateFilter === 'month') {
          const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
          const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0)
          params.startDate = this.formatDate(monthStart)
          params.endDate = this.formatDate(monthEnd)
        }
      }
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
        this.buildTodayFlowData()
      }).catch(error => {
        console.error('Error fetching medical records:', error)
        this.tableData = []
        this.total = 0
        this.buildTodayFlowData()
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
        general_condition: '',
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
      this.$prompt(`请输入模板名称，将保存到分类"${this.activeTemplateCategory || DEFAULT_TEMPLATE_CATEGORY}"`, '保存病历模板', {
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
      this.$confirm(`确认删除模板"${template.template_name}"吗？`, '提示', { type: 'warning' }).then(async () => {
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
    async loadPhrases(fieldType) {
      if (this.phraseData[fieldType]) return
      try {
        const result = await fetchCachedResource({
          cacheKey: `ref:medical-record-phrases-${fieldType}`,
          scope: '',
          url: `/medical-record-phrases/selectByFieldType?fieldType=${fieldType}`,
          loader: () => axios.get('/medical-record-phrases/selectByFieldType', { params: { fieldType } })
        })
        this.$set(this.phraseData, fieldType, Array.isArray(result && result.data) ? result.data : [])
      } catch (error) {
        this.$set(this.phraseData, fieldType, [])
      }
    },
    handlePhraseFocus(fieldType) {
      this.loadPhrases(fieldType)
      this.$set(this.phrasePopoverVisible, fieldType, true)
    },
    closePhrasePopover(fieldType) {
      this.$set(this.phrasePopoverVisible, fieldType, false)
      this.$set(this.phrasePopoverHover, fieldType, false)
    },
    handlePhraseBlur(fieldType, event) {
      const relatedTarget = event && event.relatedTarget
      if (relatedTarget) {
        const popoverRef = this.$refs[`phrasePopover-${fieldType}`]
        const popperEl = popoverRef && popoverRef.popperElm
        if (popperEl && popperEl.contains(relatedTarget)) {
          return
        }
      }
      setTimeout(() => {
        if (!this.phrasePopoverHover[fieldType]) {
          this.$set(this.phrasePopoverVisible, fieldType, false)
        }
      }, 300)
    },
    insertPhrase(field, content) {
      const current = String(this.form[field] || '')
      this.form[field] = current ? current + '\n' + content : content
    },
    async addPhrase(fieldType) {
      const content = String(this.phraseNewContent[fieldType] || '').trim()
      if (!content) {
        this.$message.warning('词条内容不能为空')
        return
      }
      try {
        await axios.post('/medical-record-phrases/add', {
          field_type: fieldType,
          content: content,
          category: String(this.phraseNewCategory[fieldType] || '').trim() || '常用',
          sort_order: 0,
          status: 1
        })
        this.$set(this.phraseNewContent, fieldType, '')
        this.$set(this.phraseNewCategory, fieldType, '')
        this.$set(this.phraseData, fieldType, null)
        await this.loadPhrases(fieldType)
        this.$message.success('添加成功')
      } catch (error) {
        this.$message.error('添加失败')
      }
    },
    async deletePhrase(id, fieldType) {
      try {
        await this.$confirm('确认删除该常用短语吗？', '提示', { type: 'warning' })
        await axios.delete(`/medical-record-phrases/delete/${id}`)
        this.$set(this.phraseData, fieldType, null)
        await this.loadPhrases(fieldType)
        this.$message.success('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    groupedPhrases(fieldType) {
      const list = this.phraseData[fieldType] || []
      const groups = {}
      list.forEach(item => {
        const category = item.category || '常用'
        if (!groups[category]) groups[category] = []
        groups[category].push(item)
      })
      return groups
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
        && String(item.operation_name || '').trim() === String(relation.operation_name || '').trim()
      )
    },
    toggleSuggestedOperation(relation) {
      if (!relation) return
      const currentIndex = (this.form.operation_items || []).findIndex(item =>
        String(item.project_id || '') === String(this.selectedProjectId || '')
        && String(item.operation_name || '').trim() === String(relation.operation_name || '').trim()
      )
      if (currentIndex >= 0) {
        this.removeOperationItem(currentIndex)
        return
      }
      const project = (this.projectOptions || []).find(item => String(item.id) === String(this.selectedProjectId || ''))
      const nextItem = defaultOperationItem()
      nextItem.project_id = project ? project.id : ''
      nextItem.project_name = project ? project.project_name : ''
      nextItem.operation_name = relation.operation_name || ''
      nextItem.need_lab_processing = relation.need_lab_processing === 1 ? 1 : 0
      nextItem.default_processing_days = Number(relation.default_processing_days || 0)
      if (!Array.isArray(this.form.operation_items)) {
        this.$set(this.form, 'operation_items', [])
      }
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
        const project = (this.projectOptions || []).find(projectItem => String(projectItem.id) === String(item.project_id || ''))
        const factory = (this.labFactoryOptions || []).find(factoryItem => String(factoryItem.id) === String(item.factory_id || ''))
        const needLabProcessing = item.need_lab_processing === 1 ? 1 : 0
        const normalized = {
          project_id: item.project_id || null,
          project_name: item.project_name || (project && project.project_name) || '',
          operation_id: item.operation_id || null,
          operation_name: String(item.operation_name || '').trim(),
          factory_id: needLabProcessing === 1 ? (item.factory_id || null) : null,
          factory_name: needLabProcessing === 1 ? (item.factory_name || (factory && factory.name) || '') : '',
          tooth_positions: item.tooth_positions || '',
          remark: item.remark || '',
          need_lab_processing: needLabProcessing,
          default_processing_days: Number(item.default_processing_days || 0)
        }
        if (includeRuntimeFields) {
          normalized.id = item.id || null
        }
        return normalized
      }).filter(item => item.operation_name)
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
      this.activeCollapse = ['basic', 'complaint', 'examination', 'operation', 'treatment', 'advice']
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
      this.activeCollapse = ['basic', 'complaint', 'examination', 'operation', 'treatment', 'advice']
      if (this.selectedProjectId) {
        await this.loadProjectDetail(this.selectedProjectId)
      }
      this.syncImageUploadExtra()
      await this.loadPatientImages()
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    handleRouteQuery() {
      const query = this.$route.query || {}
      if (query.patientId) {
        this.form = this.buildEmptyForm()
        this.form.patient_id = query.patientId
        this.form.patient_name = query.patientName || ''
        this.dialogTitle = '新增病历'
        this.selectedProjectId = ''
        this.selectedQuickOperationId = ''
        this.selectedTemplateId = null
        this.lastAutoTreatmentDraft = ''
        this.treatmentDraftLocked = false
        this.editorFlags.autoSyncToothPositions = true
        this.patientImages = []
        this.editorFromQuery = true
        this.editorVisible = true
        this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
      } else if (query.recordId) {
        const row = { id: query.recordId }
        this.editorFromQuery = true
        this.openEdit(row)
      }
    },
    closeEditor() {
      this.editorVisible = false
      this.previewVisible = false
      this.previewImageItem = null
      this.patientImages = []
      this.selectedTemplateId = null
      this.templateKeyword = ''
      this.form = this.buildEmptyForm()
      if (this.editorFromQuery) {
        this.editorFromQuery = false
        this.$router.back()
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
          this.loadAll()
          this.$confirm('病历已保存，是否继续留在编辑页面？', '提示', {
            confirmButtonText: '继续编辑',
            cancelButtonText: '关闭',
            type: 'success'
          }).then(() => {
            // 用户选择继续编辑，保持在当前页面
          }).catch(() => {
            this.editorVisible = false
          })
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
    /**
     * AI 病历扩写功能入口
     * 调用链路：前端 → /api/ai/proxy/medical-expand → AiProxyController → AiProxyService → 外部 Webhook
     * 返回链路：外部 Webhook 返回 JSON → AiProxyService → AiProxyController（解包标准格式）→ 前端回填表单
     *
     * 【字段映射总表】前端发送字段名 → 表单输入框对应关系：
     *   patient_id          → 患者ID（隐藏字段）
     *   patient_name        → 患者姓名
     *   doctor_account_id   → 接诊医生ID
     *   doctor_name         → 接诊医生姓名
     *   nurse_name          → 护士姓名
     *   assistant_name      → 助手姓名
     *   visit_date          → 就诊日期
     *   record_type         → 病历类型（初诊/复诊）
     *   chief_complaint     → 主诉
     *   present_illness_history → 现病史
     *   past_medical_history    → 既往史（前端表单字段为 past_history）
     *   infectious_history      → 流行病史/传染病史
     *   allergy_history         → 过敏史
     *   general_condition       → 一般情况/全身情况
     *   examination_findings    → 体格检查（前端表单字段为 examination）
     *   auxiliary_examination   → 辅助检查
     *   diagnosis               → 诊断
     *   treatment_plan          → 治疗计划
     *   treatment               → 治疗文稿
     *   tooth_positions         → 牙位
     *   medical_advice          → 医嘱
     *   prescription            → 处方
     *   record_tags             → 病历标签
     *   image_summary           → 影像说明
     *   notes                   → 备注
     *   record_status           → 病历状态（final/draft）
     *   operation_items         → 治疗项目数组
     *   draft_record            → 草稿文本（由 chief_complaint + present_illness_history 拼接）
     *
     * 【回填字段映射】Webhook 返回字段名 → 前端表单字段名：
     *   chief_complaint         → this.form.chief_complaint
     *   present_illness_history → this.form.present_illness_history
     *   past_medical_history    → this.form.past_history（注意字段名不同！）
     *   infectious_history      → this.form.infectious_history
     *   allergy_history         → this.form.allergy_history
     *   general_condition       → this.form.general_condition
     *   examination_findings    → this.form.examination（注意字段名不同！）
     *   auxiliary_examination   → this.form.auxiliary_examination
     *   diagnosis               → this.form.diagnosis
     *   treatment_plan          → this.form.treatment_plan
     *   treatment               → this.form.treatment
     *   medical_advice          → this.form.medical_advice
     *   prescription            → this.form.prescription
     *   record_tags             → this.form.record_tags
     *   image_summary           → this.form.image_summary
     *   notes                   → this.form.notes
     */
    async expandWithAI() {
      const patientId = String(this.form.patient_id || '').trim()
      const patientName = String(this.form.patient_name || '').trim()
      if (!patientId || !patientName) {
        this.$message.warning('请先填写患者ID和患者姓名')
        return
      }
      this.aiExpanding = true
      try {
        // 组装发送到后端的字段数据，所有字段都会被包装到标准协议中转发给 Webhook
        const fields = {
          patient_id: patientId,
          patient_name: patientName,
          doctor_account_id: this.form.doctor_account_id || '',
          doctor_name: this.form.doctor_name || '',
          nurse_name: this.form.nurse_name || '',
          assistant_name: this.form.assistant_name || '',
          visit_date: this.form.visit_date || '',
          record_type: this.form.record_type || '',
          chief_complaint: this.form.chief_complaint || '',
          present_illness_history: this.form.present_illness_history || '',
          // 注意：前端表单字段名为 past_history，但发送给 Webhook 时统一使用 past_medical_history
          past_medical_history: this.form.past_history || '',
          infectious_history: this.form.infectious_history || '',
          allergy_history: this.form.allergy_history || '',
          general_condition: this.form.general_condition || '',
          // 注意：前端表单字段名为 examination，但发送给 Webhook 时统一使用 examination_findings
          examination_findings: this.form.examination || '',
          auxiliary_examination: this.form.auxiliary_examination || '',
          diagnosis: this.form.diagnosis || '',
          treatment_plan: this.form.treatment_plan || '',
          treatment: this.form.treatment || '',
          tooth_positions: this.form.tooth_positions || '',
          medical_advice: this.form.medical_advice || '',
          prescription: this.form.prescription || '',
          record_tags: this.form.record_tags || '',
          image_summary: this.form.image_summary || '',
          notes: this.form.notes || '',
          record_status: this.form.record_status || '',
          operation_items: this.form.operation_items || [],
          // draft_record 是给 AI 的草稿提示，由主诉+现病史拼接而成
          draft_record: (this.form.chief_complaint || '') + ' ' + (this.form.present_illness_history || '')
        }
        const accountId = this.currentUser && this.currentUser.id ? this.currentUser.id : ''
        // 调用 AI 代理接口，使用动态 agentKey，后端会将请求包装为标准协议后转发到配置的 Webhook
        const agentKey = this.medicalRecordAgentKey
        // 如果用户选择了诊疗场景，将场景信息一并传给后端，后端会提取到 context.scene_id / context.scene_name 中供 n8n 使用
        const sceneId = this.aiSceneId || ''
        const sceneName = sceneId ? (this.scenes.find(s => s.id === sceneId)?.name || '') : ''
        // 如果用户选择了具体步骤，将步骤信息也传给后端，n8n 可根据 step_name 路由到不同 prompt 模板
        const stepId = this.aiSelectedStepId || ''
        const stepName = stepId ? (this.aiSceneSteps.find(s => s.id === stepId)?.name || '') : ''
        const res = await axios.post(`/api/ai/proxy/${agentKey}`, {
          fields,
          account_id: accountId,
          scene_id: sceneId,
          scene_name: sceneName,
          step_id: stepId,
          step_name: stepName
        })
        // 后端返回结构：res.data = { code: '200', msg: 'success', data: { ... } }
        // 注意：AiProxyController 已做解包处理，res.data.data 直接就是 Webhook 返回的内层 data
        if (res.data && res.data.code === '200') {
          let result = res.data.data || {}
          // 如果 result 是字符串（Webhook 把对象序列化为字符串放入 data 字段），尝试前端解析
          if (typeof result === 'string') {
            const str = result.trim()
            try {
              result = JSON.parse(str)
            } catch (e) {
              // 尝试解析 Java Map.toString 格式: {k1=v1, k2=v2}
              if (str.startsWith('{') && str.endsWith('}')) {
                const parsed = {}
                const content = str.slice(1, -1)
                const pairs = content.split(/,\s+/)
                for (const pair of pairs) {
                  const idx = pair.indexOf('=')
                  if (idx > 0) {
                    const key = pair.slice(0, idx).trim()
                    const value = pair.slice(idx + 1).trim()
                    parsed[key] = value
                  }
                }
                result = parsed
              }
            }
          }
          // AI 返回结果自动回填到表单字段
          // 字段名必须与后端/webhook 返回的 JSON 字段名保持一致
          // 只有字段值非空时才回填，避免覆盖医生已手动填写的内容
          if (result.chief_complaint) this.form.chief_complaint = result.chief_complaint
          if (result.present_illness_history) this.form.present_illness_history = result.present_illness_history
          if (result.past_medical_history) this.form.past_history = result.past_medical_history
          if (result.infectious_history) this.form.infectious_history = result.infectious_history
          if (result.allergy_history) this.form.allergy_history = result.allergy_history
          if (result.general_condition) this.form.general_condition = result.general_condition
          if (result.examination_findings) this.form.examination = result.examination_findings
          if (result.auxiliary_examination) this.form.auxiliary_examination = result.auxiliary_examination
          if (result.diagnosis) this.form.diagnosis = result.diagnosis
          if (result.treatment_plan) this.form.treatment_plan = result.treatment_plan
          if (result.treatment) this.form.treatment = result.treatment
          if (result.medical_advice) this.form.medical_advice = result.medical_advice
          if (result.prescription) this.form.prescription = result.prescription
          if (result.record_tags) this.form.record_tags = result.record_tags
          if (result.image_summary) this.form.image_summary = result.image_summary
          if (result.notes) this.form.notes = result.notes
          this.$message.success('AI 病历扩写完成')
        } else {
          this.$message.warning(res.data?.msg || 'AI 扩写失败')
        }
      } catch (error) {
        console.error('AI 扩写失败:', error)
        this.$message.error('AI 扩写接口调用失败，请检查网络或后端服务')
      } finally {
        this.aiExpanding = false
      }
    },
    async handlePatientIdentityChange() {
      this.syncImageUploadExtra()
      this.loadPatientImages()
      // 如果输入了患者ID但姓名为空，自动查询患者信息并填充
      const patientId = Number(this.form.patient_id || 0)
      const patientName = String(this.form.patient_name || '').trim()
      if (Number.isFinite(patientId) && patientId > 0 && !patientName) {
        try {
          const res = await axios.get(`/patient-details/basic/${patientId}`)
          if (res.data && res.data.code === '200' && res.data.data && res.data.data.patient) {
            const patient = res.data.data.patient
            this.$set(this.form, 'patient_name', patient.name || '')
            this.syncImageUploadExtra()
          }
        } catch (error) {
          // 查询失败静默处理，不阻塞用户手动输入
        }
      }
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
    },
    getDiagnosisTags(diagnosis) {
      if (!diagnosis) return []
      return String(diagnosis)
        .split(/[,，;；/]/)
        .map(item => item.trim())
        .filter(Boolean)
        .slice(0, 3)
    },
    handleCollapseChange(val) {
      this.activeCollapse = val
    },
    loadScheduleEntries() {
      fetchCachedResource({
        cacheKey: 'page:appointments:schedule',
        scope: '',
        url: '/appointments/scheduleEntries',
        loader: () => axios.get('/appointments/scheduleEntries')
      }).then(result => {
        this.scheduleEntries = Array.isArray(result && result.data) ? result.data : []
        this.buildTodayFlowData()
      }).catch(() => {
        this.scheduleEntries = []
        this.buildTodayFlowData()
      })
    },
    buildTodayFlowData() {
      const todayStr = this.formatDate(new Date())
      const todayEntries = (this.scheduleEntries || []).filter(item => {
        return this.formatDate(item.appointment_date) === todayStr
      })
      const recordMap = {}
      ;(this.tableData || []).forEach(record => {
        const key = String(record.patient_id || '') + '_' + this.formatDate(record.visit_date)
        if (!recordMap[key] || new Date(record.visit_date) > new Date(recordMap[key].visit_date)) {
          recordMap[key] = record
        }
      })
      const flow = todayEntries.map(entry => {
        const key = String(entry.patient_id || '') + '_' + todayStr
        const record = recordMap[key]
        return {
          patient_id: entry.patient_id,
          patient_name: entry.patient_name,
          appointment_time: entry.appointment_time,
          appointment_purpose: entry.appointment_purpose,
          clinicStatus: entry.clinic_status || '已预约',
          checkInTime: entry.check_in_time || null,
          recordStatus: record ? (record.record_status || 'final') : 'none',
          tooth_positions: record ? (record.tooth_positions || '') : '',
          operation_summary: record ? (record.operation_summary || '') : '',
          pending_lab_count: record ? Number(record.pending_lab_count || 0) : 0,
          record_id: record ? record.id : null,
          entry_id: entry.id
        }
      })
      flow.sort((a, b) => {
        const tA = String(a.appointment_time || '99:99')
        const tB = String(b.appointment_time || '99:99')
        return tA.localeCompare(tB)
      })
      const extraRecords = (this.tableData || []).filter(record => {
        if (this.formatDate(record.visit_date) !== todayStr) return false
        const key = String(record.patient_id || '') + '_' + todayStr
        return !todayEntries.some(e => String(e.patient_id || '') + '_' + todayStr === key)
      }).map(record => ({
        patient_id: record.patient_id,
        patient_name: record.patient_name,
        appointment_time: '',
        appointment_purpose: '非预约就诊',
        clinicStatus: '已挂号',
        checkInTime: record.visit_date || null,
        recordStatus: record.record_status || 'final',
        tooth_positions: record.tooth_positions || '',
        operation_summary: record.operation_summary || '',
        pending_lab_count: Number(record.pending_lab_count || 0),
        record_id: record.id,
        entry_id: null
      }))
      const allRows = [...flow, ...extraRecords]
      // 计算等待中患者的排队序号（按挂号时间先后）
      const waitingList = allRows
        .filter(r => r.clinicStatus === '等待中' || r.clinicStatus === '已挂号')
        .sort((a, b) => new Date(a.checkInTime || '2099-01-01') - new Date(b.checkInTime || '2099-01-01'))
      const queueMap = new Map()
      waitingList.forEach((item, idx) => {
        queueMap.set(item.entry_id + '_' + item.patient_id, idx + 1)
      })
      allRows.forEach(r => {
        if (r.clinicStatus === '等待中' || r.clinicStatus === '已挂号') {
          r.queueNumber = queueMap.get(r.entry_id + '_' + r.patient_id) || null
        } else {
          r.queueNumber = null
        }
      })
      this.todayFlowData = allRows
    },
    switchTab(tab) {
      this.activeListTab = tab
    },
    filterDraft() {
      this.dateFilter = 'all'
      this.loadAll()
    },
    goToLabOrders() {
      this.$router.push({ path: '/lab-orders', query: { pendingLab: '1' } }).catch(() => {})
    },
    resetFilters() {
      this.searchName = ''
      this.dateFilter = 'today'
      this.page = 1
      this.loadAll()
    },
    handleDateFilterChange() {
      this.page = 1
      this.loadAll()
    },
    openAddFollowupForPatient(row) {
      this.$router.push({ path: '/Followup', query: { action: 'add', patientId: row.patient_id, patientName: row.patient_name } })
    },
    clinicStatusType(status) {
      const map = { '已预约': 'info', '已挂号': 'warning', '等待中': 'danger', '就诊中': 'success', '已完成': '' }
      return map[status] || 'info'
    },
    formatTime(value) {
      if (!value) return ''
      const d = new Date(value)
      if (Number.isNaN(d.getTime())) return String(value).slice(11, 16)
      const h = String(d.getHours()).padStart(2, '0')
      const m = String(d.getMinutes()).padStart(2, '0')
      return `${h}:${m}`
    },
    computeWaitDuration(checkInTime) {
      if (!checkInTime) return '-'
      const start = new Date(checkInTime)
      const now = new Date()
      const diff = Math.max(0, Math.floor((now - start) / 60000))
      if (diff < 60) return `${diff}分钟`
      const h = Math.floor(diff / 60)
      const m = diff % 60
      return `${h}小时${m}分钟`
    },
    updateClinicStatus(row, newStatus) {
      if (!row.entry_id) {
        this.$message.warning('非预约患者暂不支持状态变更')
        return
      }
      const payload = { clinic_status: newStatus }
      axios.put(`/appointments/updateClinicStatus/${row.entry_id}`, payload).then(res => {
        if (res.data.code === '200') {
          this.$message.success('状态更新成功')
          this.loadScheduleEntries()
        } else {
          this.$message.error(res.data.msg || '状态更新失败')
        }
      }).catch(err => {
        this.$message.error((err.response && err.response.data && err.response.data.msg) || '状态更新失败')
      })
    },
    openAddForPatient(row) {
      this.currentUser = this.readCurrentUser()
      this.form = this.buildEmptyForm()
      if (row.patient_id) this.form.patient_id = row.patient_id
      if (row.patient_name) this.form.patient_name = row.patient_name
      this.dialogTitle = '新增病历'
      this.selectedProjectId = ''
      this.selectedQuickOperationId = ''
      this.selectedTemplateId = null
      this.lastAutoTreatmentDraft = ''
      this.treatmentDraftLocked = false
      this.editorFlags.autoSyncToothPositions = true
      this.editorVisible = true
      this.patientImages = []
      this.activeCollapse = ['basic', 'complaint', 'examination', 'operation', 'treatment', 'advice']
      this.syncImageUploadExtra()
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    openEditByRecord(row) {
      if (!row.record_id) return
      const record = (this.tableData || []).find(item => Number(item.id) === Number(row.record_id))
      if (record) {
        this.openEdit(record)
      }
    },
    openLabOrderForFlowRow(row) {
      if (!row.record_id) return
      const record = (this.tableData || []).find(item => Number(item.id) === Number(row.record_id))
      if (record) {
        this.openLabOrderForRecord(record)
      }
    },
    openPatientDetail(patientId) {
      if (!patientId) return
      this.$router.push({ path: '/patient-details', query: { id: patientId } }).catch(() => {})
    },
    async loadScenes() {
      try {
        const res = await axios.get('/api/treatment-scenes/enabled')
        if (res.data && res.data.code === '200') {
          this.scenes = res.data.data || []
        }
      } catch (e) {
        console.warn('加载治疗场景失败', e)
      }
    },
    onSceneChange(sceneId) {
      this.selectedOperations = []
      this.currentSceneSteps = []
      if (!sceneId) return
      const scene = this.scenes.find(s => s.id === sceneId)
      if (!scene) return
      if (scene.level === 1) {
        // 简单病种不需要步骤勾选
        this.currentSceneSteps = []
        return
      }
      // 加载步骤
      axios.get(`/api/treatment-scenes/${sceneId}/steps`).then(res => {
        if (res.data && res.data.code === '200') {
          this.currentSceneSteps = (res.data.data || []).filter(s => s.enabled !== false)
          // 默认选中第一个步骤
          if (this.currentSceneSteps.length > 0) {
            this.selectedOperations = [this.currentSceneSteps[0].name]
          }
        }
      }).catch(() => {})
    },
    toggleSceneStep(stepName) {
      const idx = this.selectedOperations.indexOf(stepName)
      if (idx >= 0) {
        this.selectedOperations.splice(idx, 1)
      } else {
        this.selectedOperations.push(stepName)
      }
    },
    // AI 诊疗场景变化时加载步骤列表：有步骤的病种显示步骤选择器，供 n8n 按步骤路由
    async onAiSceneChange(sceneId) {
      this.aiSceneSteps = []
      this.aiSelectedStepId = null
      if (!sceneId) return
      const scene = this.scenes.find(s => s.id === sceneId)
      if (!scene) return
      // 简单病种（level === 1）没有分步骤，直接返回
      if (scene.level === 1) {
        this.aiSceneSteps = []
        return
      }
      try {
        const res = await axios.get(`/api/treatment-scenes/${sceneId}/steps`)
        if (res.data && res.data.code === '200') {
          this.aiSceneSteps = (res.data.data || []).filter(s => s.enabled !== false)
          // 默认选中第一个步骤
          if (this.aiSceneSteps.length > 0) {
            this.aiSelectedStepId = this.aiSceneSteps[0].id
          }
        }
      } catch (e) {
        console.warn('加载步骤失败', e)
      }
    },
    // 打开诊疗场景管理弹窗：允许在病历页直接维护病种列表
    openSceneManageDialog() {
      this.newSceneForm = { name: '', category: '其他', steps: [] }
      this.sceneManageDialogVisible = true
    },
    // 新增病种：调用后端保存接口，成功后刷新下拉列表
    async saveNewScene() {
      const name = String(this.newSceneForm.name || '').trim()
      if (!name) {
        this.$message.warning('请输入病种名称')
        return
      }
      // 过滤掉名称为空的步骤，并按顺序生成 sortOrder
      const steps = (this.newSceneForm.steps || [])
        .map(s => String(s.name || '').trim())
        .filter(n => n)
        .map((n, idx) => ({ name: n, sortOrder: idx, enabled: true }))
      // 有步骤时自动设为复杂级别（level = 3），无步骤时为简单级别（level = 1）
      const level = steps.length > 0 ? 3 : 1
      this.sceneSaving = true
      try {
        const res = await axios.post('/api/treatment-scenes', {
          name: name,
          category: String(this.newSceneForm.category || '其他').trim(),
          level: level,
          enabled: true,
          sortOrder: 0,
          steps: steps
        })
        if (res.data && res.data.code === '200') {
          this.$message.success('新增病种成功')
          this.newSceneForm = { name: '', category: '其他', steps: [] }
          await this.loadScenes()
        } else {
          this.$message.warning(res.data?.msg || '保存失败')
        }
      } catch (e) {
        console.error('新增病种失败', e)
        this.$message.error('新增病种失败：' + (e.response?.data?.msg || e.message))
      } finally {
        this.sceneSaving = false
      }
    },
    // 删除病种：二次确认后调用删除接口，成功后清空已选场景并刷新列表
    async deleteScene(scene) {
      try {
        await this.$confirm(`确定删除病种「${scene.name}」吗？删除后无法恢复。`, '确认删除', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch {
        return
      }
      try {
        const res = await axios.delete(`/api/treatment-scenes/${scene.id}`)
        if (res.data && res.data.code === '200') {
          this.$message.success('删除成功')
          // 如果删除的是当前选中的场景，清空选择
          if (this.aiSceneId === scene.id) {
            this.aiSceneId = null
            this.aiSceneName = ''
          }
          await this.loadScenes()
        } else {
          this.$message.warning(res.data?.msg || '删除失败')
        }
      } catch (e) {
        console.error('删除病种失败', e)
        this.$message.error('删除失败：' + (e.response?.data?.msg || e.message))
      }
    }
  }
}
</script>

<style scoped>
/* Apple 风格 CSS 变量 */
.medical-record-page {
  --apple-bg-primary: #f5f5f7;
  --apple-bg-secondary: #ffffff;
  --apple-text-primary: #1d1d1f;
  --apple-text-secondary: #86868b;
  --apple-border: rgba(0, 0, 0, 0.08);
  --apple-blue: #0071e3;
  --apple-blue-light: rgba(0, 113, 227, 0.06);
  --apple-red: #ff3b30;
  --apple-red-light: rgba(255, 59, 48, 0.06);
  --apple-green: #34c759;
  --apple-green-light: rgba(52, 199, 89, 0.06);
  --apple-orange: #ff9500;
  --apple-orange-light: rgba(255, 149, 0, 0.06);
  --apple-gray: #f2f2f7;
  --apple-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  --apple-shadow-hover: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--apple-bg-primary);
  min-height: 100vh;
  padding: 20px;
}

/* PageHeader 风格 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  background: var(--apple-bg-secondary);
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--apple-shadow);
}

.page-header__main {
  flex: 1;
}

.page-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header__stats {
  display: flex;
  gap: 12px;
}

.page-kicker {
  color: var(--apple-text-secondary);
  font-size: 13px;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.page-title {
  margin: 0 0 8px;
  color: var(--apple-text-primary);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 0;
  color: var(--apple-text-secondary);
  font-size: 14px;
}

.mini-stat {
  min-width: 120px;
  padding: 14px 16px;
  background: var(--apple-gray);
  border-radius: 16px;
  border: 1px solid var(--apple-border);
}

.mini-stat.accent {
  background: var(--apple-blue-light);
  border-color: rgba(0, 113, 227, 0.15);
}

.mini-num {
  font-size: 26px;
  font-weight: 700;
  color: var(--apple-text-primary);
  letter-spacing: -0.5px;
}

.mini-label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--apple-text-secondary);
}

/* 查询条件 */
.query-card {
  border-radius: 16px;
  background: var(--apple-bg-secondary);
  box-shadow: var(--apple-shadow);
  border: none;
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

/* 卡片式列表 - Apple Notes 简洁列表风格 */
.card-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 12px;
}

.record-card {
  border-radius: 16px;
  background: var(--apple-bg-secondary);
  box-shadow: var(--apple-shadow);
  border: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.record-card:hover {
  box-shadow: var(--apple-shadow-hover);
  background: #fafafa;
}

.record-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--apple-border);
}

.record-card__patient {
  display: flex;
  align-items: center;
  gap: 12px;
}

.patient-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--apple-gray);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-text-secondary);
  font-size: 20px;
}

.patient-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.patient-phone {
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin-top: 2px;
}

.record-card__meta {
  display: flex;
  gap: 6px;
}

.record-card__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-card__row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.record-card__label {
  font-size: 13px;
  color: var(--apple-text-secondary);
  min-width: 70px;
  flex-shrink: 0;
}

.record-card__value {
  font-size: 13px;
  color: var(--apple-text-primary);
  flex: 1;
}

.record-card__value--ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
}

.record-card__footer {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--apple-border);
}

/* 新卡片列表样式 */
.record-card__body {
  display: flex;
  gap: 16px;
  padding: 18px;
}

.record-card__main {
  flex: 1;
  min-width: 0;
}

.record-card__side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  min-width: 140px;
}

.record-card__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.record-card__patient {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-card__avatar {
  background: var(--apple-gray);
  color: var(--apple-text-secondary);
}

.record-card__name-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.record-card__name {
  font-size: 16px;
  font-weight: 700;
  color: var(--apple-text-primary);
}

.record-card__info {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.info-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: var(--apple-text-secondary);
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--apple-text-primary);
  flex: 1;
  min-width: 0;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-card__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.lab-badge {
  padding: 6px 10px;
  background: var(--apple-red-light);
  border-radius: 8px;
}

.lab-badge__text {
  font-size: 12px;
  color: var(--apple-red);
  font-weight: 500;
}

/* 分页 */
.pagination-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

/* 编辑页 */
.editor-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  border-radius: 16px;
  background: var(--apple-bg-secondary);
  box-shadow: var(--apple-shadow);
  border: none;
}

/* 可折叠区块 */
.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--apple-text-primary);
}

.collapse-icon {
  font-size: 16px;
  color: var(--apple-text-secondary);
}

.section-body {
  padding: 8px 0;
}

.toolbar-switch {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding-top: 6px;
  color: var(--apple-text-secondary);
  font-size: 13px;
}

/* 模板库 */
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.panel-title {
  color: var(--apple-text-primary);
  font-size: 16px;
  font-weight: 700;
}

.panel-tip {
  margin-top: 4px;
  color: var(--apple-text-secondary);
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
  color: var(--apple-text-secondary);
  font-size: 13px;
}

.template-tree-wrap {
  margin-top: 14px;
  min-height: 280px;
  max-height: 420px;
  overflow: auto;
  padding: 10px;
  border: 1px solid var(--apple-border);
  border-radius: 16px;
  background: var(--apple-bg-primary);
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
  color: var(--apple-text-primary);
}

.template-tree-node__count {
  color: var(--apple-text-secondary);
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
  border: 1px solid var(--apple-border);
  background: var(--apple-bg-primary);
}

.template-preview-card__title {
  color: var(--apple-text-primary);
  font-size: 14px;
  font-weight: 700;
}

.template-preview-card__name {
  margin-top: 10px;
  color: var(--apple-text-primary);
  font-weight: 700;
}

.template-preview-card__meta {
  margin-top: 4px;
  color: var(--apple-text-secondary);
  font-size: 12px;
}

.template-preview-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--apple-border);
}

.template-preview-row span {
  color: var(--apple-text-secondary);
  font-size: 12px;
}

.template-preview-row strong {
  color: var(--apple-text-primary);
  font-weight: 500;
  line-height: 1.7;
}

.template-preview-card__empty {
  margin-top: 10px;
  color: var(--apple-text-secondary);
  font-size: 12px;
}

/* 操作面板 */
.operation-panel {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--apple-border);
  background: var(--apple-bg-primary);
}

.operation-panel__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
}

.operation-panel__title {
  color: var(--apple-text-primary);
  font-size: 15px;
  font-weight: 700;
}

.operation-panel__tip {
  margin-top: 4px;
  color: var(--apple-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.project-suggestion-row {
  margin-bottom: 12px;
}

.operation-suggestion-empty {
  min-height: 32px;
  display: flex;
  align-items: center;
  color: var(--apple-text-secondary);
  font-size: 12px;
}

.operation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.operation-item {
  background: var(--apple-bg-secondary);
  border: 1px solid var(--apple-border);
  border-radius: 16px;
  padding: 12px;
  transition: all 0.2s ease;
}

.operation-item:hover {
  border-color: rgba(0, 113, 227, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.operation-item__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: var(--apple-text-primary);
  font-weight: 700;
}

.operation-item__actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.operation-empty {
  color: var(--apple-text-secondary);
  font-size: 12px;
  padding: 8px 0 2px;
}

/* 表单提示 */
.sheet-field-hint {
  margin-top: 8px;
  color: var(--apple-text-secondary);
  font-size: 12px;
}

.sheet-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.treatment-draft-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.treatment-draft-hint {
  color: var(--apple-text-secondary);
  font-size: 12px;
}

/* 影像上传 */
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
  border: 1px dashed var(--apple-border);
  border-radius: 16px;
  background: var(--apple-bg-secondary);
  transition: all 0.2s ease;
}

.image-upload-card {
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-upload-card:hover {
  border-color: var(--apple-blue);
  background: var(--apple-blue-light);
}

.image-upload-card__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--apple-text-secondary);
  font-size: 12px;
}

.image-upload-card__inner i {
  font-size: 24px;
  color: var(--apple-blue);
}

.image-thumb-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--apple-shadow);
  border: none;
}

.image-thumb-card__preview {
  height: 84px;
  background: var(--apple-bg-primary);
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
  color: var(--apple-text-secondary);
  font-size: 12px;
}

.image-thumb-card__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 10px 0;
}

.image-thumb-card__meta strong {
  color: var(--apple-text-primary);
  font-size: 12px;
  font-weight: 600;
  line-height: 1.5;
  word-break: break-all;
}

.image-thumb-card__meta span {
  color: var(--apple-text-secondary);
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
  border: 1px dashed var(--apple-border);
  color: var(--apple-text-secondary);
}

/* 底部操作栏 */
.editor-footer {
  position: sticky;
  bottom: 0;
  z-index: 5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 18px;
  border: 1px solid var(--apple-border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  box-shadow: var(--apple-shadow);
}

/* 弹窗预览 */
.preview-dialog-body {
  min-height: 320px;
}

.preview-dialog-image {
  width: 100%;
  max-height: 72vh;
  object-fit: contain;
  border-radius: 16px;
}

.preview-dialog-file {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: var(--apple-text-secondary);
}

.preview-dialog-file i {
  font-size: 52px;
}

/* 圆角输入框覆盖 - Apple 风格 */
::v-deep .el-input__inner,
::v-deep .el-textarea__inner {
  border-radius: 12px;
  border-color: var(--apple-border);
  transition: all 0.2s ease;
}

::v-deep .el-input__inner:focus,
::v-deep .el-textarea__inner:focus {
  border-color: var(--apple-blue);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.1);
}

::v-deep .el-button {
  border-radius: 999px;
  transition: all 0.2s ease;
}

::v-deep .el-button.is-round {
  border-radius: 999px;
}

::v-deep .el-button--primary {
  background: var(--apple-blue);
  border-color: var(--apple-blue);
}

::v-deep .el-button--primary:hover {
  background: #0077ed;
  border-color: #0077ed;
}

::v-deep .el-tag {
  border-radius: 8px;
  border: none;
  background: var(--apple-gray);
  color: var(--apple-text-primary);
}

::v-deep .el-tag--success {
  background: var(--apple-green-light);
  color: var(--apple-green);
}

::v-deep .el-tag--warning {
  background: var(--apple-orange-light);
  color: var(--apple-orange);
}

::v-deep .el-tag--danger {
  background: var(--apple-red-light);
  color: var(--apple-red);
}

::v-deep .el-tag--info {
  background: var(--apple-gray);
  color: var(--apple-text-secondary);
}

::v-deep .el-collapse-item__header {
  border-radius: 16px;
  padding: 0 16px;
  color: var(--apple-text-primary);
  font-weight: 600;
}

::v-deep .el-collapse-item__wrap {
  border-radius: 0 0 16px 16px;
}

::v-deep .el-collapse-item__content {
  padding: 16px;
}

/* 牙位选择器极简风格 */
::v-deep .tooth-selector svg {
  stroke: var(--apple-text-secondary);
  stroke-width: 1.5;
}

::v-deep .tooth-selector .tooth-selected {
  stroke: var(--apple-blue);
  stroke-width: 2.5;
  fill: var(--apple-blue-light);
}

/* 响应式 */
@media (max-width: 768px) {
  .page-wrap {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .page-header__stats {
    width: 100%;
  }

  .card-list {
    grid-template-columns: 1fr;
  }

  .query-input {
    width: 100%;
  }

  .query-row {
    flex-direction: column;
    align-items: stretch;
  }

  .query-row .el-button {
    width: 100%;
  }

  .record-card__footer {
    flex-wrap: wrap;
  }

  .record-card__footer .el-button {
    flex: 1;
  }

  .editor-footer {
    position: static;
    flex-wrap: wrap;
  }

  .editor-footer .el-button {
    flex: 1;
  }
}

@media (max-width: 992px) {
  .operation-panel__head,
  .treatment-draft-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* 双栏布局 */
.editor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  align-items: start;
}
.editor-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}
.editor-sidebar {
  position: sticky;
  top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-self: start;
}

/* 区块标题 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--apple-divider);
}
.section-header__main {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-header__bar {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: var(--apple-accent);
}
.section-header__icon {
  font-size: 16px;
  color: var(--apple-text-secondary);
}
.section-header__title {
  font-size: var(--apple-text-lg);
  font-weight: var(--apple-weight-semibold);
  color: var(--apple-text-primary);
}

/* 词条选择器 */
.phrase-input-wrap {
  width: 100%;
}
.phrase-input-wrap .el-textarea,
.phrase-input-wrap .el-input {
  width: 100%;
}
.phrase-popover {
  max-height: 380px;
  overflow-y: auto;
  padding: 12px;
  scrollbar-width: thin;
  scrollbar-color: rgba(0,0,0,0.15) transparent;
}
.phrase-popover::-webkit-scrollbar {
  width: 5px;
}
.phrase-popover::-webkit-scrollbar-track {
  background: transparent;
}
.phrase-popover::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.15);
  border-radius: 10px;
}
.phrase-popover__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--apple-text-primary);
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--apple-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.phrase-popover__close {
  cursor: pointer;
  color: var(--apple-text-secondary);
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
  font-size: 14px;
}
.phrase-popover__close:hover {
  color: var(--apple-text-primary);
  background: rgba(0,0,0,0.06);
}
.phrase-popover__group {
  margin-bottom: 14px;
}
.phrase-popover__category {
  font-size: 11px;
  font-weight: 600;
  color: var(--apple-blue);
  margin-bottom: 8px;
  padding: 3px 10px;
  background: rgba(0, 113, 227, 0.08);
  border-radius: 20px;
  display: inline-block;
  letter-spacing: 0.3px;
}
.phrase-popover__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.phrase-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #ffffff;
  border: 1px solid var(--apple-border);
  border-radius: 20px;
  padding: 5px 10px 5px 14px;
  transition: all 0.2s ease;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.phrase-item:hover {
  background: var(--apple-blue-light);
  border-color: rgba(0, 113, 227, 0.3);
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.12);
  transform: translateY(-1px);
}
.phrase-item .el-button--text {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--apple-text-primary);
  font-size: 12px;
  line-height: 1.4;
  white-space: normal;
  text-align: left;
  margin-left: 0;
}
.phrase-item .el-button--text:hover {
  color: var(--apple-blue);
}
.phrase-item .el-button--text + .el-button--text {
  padding: 2px 4px;
  border-radius: 50%;
  color: #cbd5e1;
  font-size: 11px;
  margin-left: 2px;
  transition: all 0.2s;
}
.phrase-item .el-button--text + .el-button--text:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.08);
}
.phrase-popover__empty {
  font-size: 12px;
  color: var(--apple-text-secondary);
  text-align: center;
  padding: 20px 0;
}
.phrase-popover__add {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
  padding: 10px;
  background: #f9fafb;
  border-radius: 10px;
  border: 1px solid var(--apple-border);
}
.phrase-popover__add .el-input {
  flex: 1;
}
.phrase-popover__add .el-input__inner {
  background: #ffffff;
  border-radius: 6px;
}

/* 响应式 */
@media (max-width: 992px) {
  .editor-layout {
    grid-template-columns: 1fr;
  }
  .editor-sidebar {
    position: static;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }
}
@media (max-width: 768px) {
  .editor-sidebar {
    grid-template-columns: 1fr;
  }
  .section-header .el-button span {
    display: none;
  }
}

/* 工作台统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--apple-bg-secondary);
  border-radius: 16px;
  padding: 18px 20px;
  box-shadow: var(--apple-shadow);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.stat-card:hover {
  box-shadow: var(--apple-shadow-hover);
  transform: translateY(-2px);
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: var(--apple-text-primary);
  letter-spacing: -0.5px;
}
.stat-label {
  font-size: 13px;
  color: var(--apple-text-secondary);
}

/* 工作台 Tab */
.workbench-tabs {
  background: var(--apple-bg-secondary);
  border-radius: 16px;
  box-shadow: var(--apple-shadow);
  border: none;
}
.workbench-tabs ::v-deep .el-tabs__header {
  border-radius: 16px 16px 0 0;
  background: var(--apple-bg-secondary);
}
.workbench-tabs ::v-deep .el-tabs__content {
  padding: 16px;
}
.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.tab-tip {
  font-size: 13px;
  color: var(--apple-text-secondary);
}

/* 工作台表格 */
.workbench-table {
  border-radius: 12px;
  overflow: hidden;
}
.flow-patient {
  display: flex;
  align-items: center;
  gap: 8px;
}
.flow-avatar {
  background: var(--apple-gray);
  color: var(--apple-text-secondary);
}
.flow-name {
  font-weight: 600;
  color: var(--apple-text-primary);
}

/* 展开行样式（复用 PatientDetail） */
.record-expand-box {
  padding: 12px 16px;
  background: var(--apple-bg-primary);
  border-radius: 12px;
}
.record-expand-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}
.record-expand-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.record-expand-item span {
  font-size: 12px;
  color: var(--apple-text-secondary);
}
.record-expand-item strong {
  font-size: 13px;
  color: var(--apple-text-primary);
  font-weight: 500;
}
.record-expand-section {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--apple-border);
}
.record-expand-label {
  font-size: 12px;
  color: var(--apple-text-secondary);
  margin-bottom: 4px;
}
.record-expand-value {
  font-size: 13px;
  color: var(--apple-text-primary);
  line-height: 1.7;
}
.record-cell-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

/* 响应式 */
@media (max-width: 992px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .query-row {
    flex-direction: column;
    align-items: stretch;
  }
  .query-row .el-button {
    width: 100%;
  }
}

/* 诊疗场景管理弹窗现代样式 */
.scene-manage-dialog .el-dialog__body {
  padding: 20px 24px;
}
.scene-step-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.scene-step-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: #f8f9fb;
  border-radius: 8px;
  transition: background 0.2s;
}
.scene-step-item:hover {
  background: #f0f2f5;
}
.scene-step-index {
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 50%;
  background: #e4e7ed;
  color: #606266;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.scene-step-input {
  flex: 1;
}
.scene-step-input .el-input__inner {
  border-radius: 6px;
  background: #fff;
}
.scene-step-delete {
  color: #c0c4cc;
  cursor: pointer;
  font-size: 14px;
  padding: 4px;
  transition: color 0.2s;
  flex-shrink: 0;
}
.scene-step-delete:hover {
  color: #f56c6c;
}
</style>

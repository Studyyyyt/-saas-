<template>
  <div class="ai-overview-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">AI 中心</h1>
        <p class="page-subtitle">管理 Agent 配置与 API Key</p>
      </div>
      <div class="page-header-right">
        <el-button size="small" icon="el-icon-refresh" :loading="loading" @click="loadData">刷新</el-button>
      </div>
    </div>

    <!-- Agent 配置表格 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">Agent 配置</div>
        <div style="display: flex; align-items: center; gap: 12px;">
          <el-tooltip content="开启后删除 Agent 需二次确认" placement="top">
            <div style="display: flex; align-items: center; gap: 6px; cursor: pointer;" @click="toggleSecurityLock">
              <i :class="securityLockEnabled ? 'el-icon-lock' : 'el-icon-unlock'" :style="{ color: securityLockEnabled ? '#f56c6c' : '#909399' }" />
              <el-switch v-model="securityLockEnabled" size="mini" active-text="安全锁" @change="toggleSecurityLock" />
            </div>
          </el-tooltip>
          <el-button type="primary" size="small" icon="el-icon-plus" @click="openEditor()">新增 Agent</el-button>
        </div>
      </div>
      <el-table :data="agentList" style="width: 100%" v-loading="loading">
        <el-table-column label="图标" width="55" align="center">
          <template slot-scope="scope">
            <i :class="scope.row.icon || 'el-icon-cpu'" style="font-size:16px;color:#5A8F7B" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="功能名称" width="90" show-overflow-tooltip />
        <el-table-column prop="agentKey" label="AgentKey" width="100" show-overflow-tooltip />
        <el-table-column prop="usageLocation" label="用途位置" width="80">
          <template slot-scope="scope">
            <span class="ellipsis-text">{{ scope.row.usageLocation || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endpointUrl" label="Webhook 地址" width="120">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.endpointUrl" placement="top">
              <span class="ellipsis-text">{{ scope.row.endpointUrl || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="responseType" label="响应类型" width="65" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="scope.row.responseType === 'sse' ? 'warning' : 'success'">{{ scope.row.responseType || 'json' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timeoutSeconds" label="超时" width="55" align="center">
          <template slot-scope="scope">{{ scope.row.timeoutSeconds || 60 }}s</template>
        </el-table-column>
        <el-table-column prop="authType" label="认证" width="65" align="center">
          <template slot-scope="scope">{{ scope.row.authType || 'none' }}</template>
        </el-table-column>
        <el-table-column label="请求模板" width="80">
          <template slot-scope="scope">
            <el-tooltip v-if="scope.row.requestTemplate" :content="scope.row.requestTemplate" placement="top">
              <span class="ellipsis-text" style="color:#5A8F7B">已配置</span>
            </el-tooltip>
            <span v-else class="ellipsis-text" style="color:#909399">默认协议</span>
          </template>
        </el-table-column>
        <el-table-column label="快捷指令" width="85">
          <template slot-scope="scope">
            <span class="ellipsis-text">{{ (scope.row.chips && scope.row.chips[0]) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预设消息" width="90" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="ellipsis-text">{{ scope.row.presetMessage || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="首页显示" width="75" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isVisibleOnHome"
              :active-value="true"
              :inactive-value="false"
              @change="(val) => toggleAgentHomeVisibility(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="被引用" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="getAgentUsageCount(scope.row.agentKey) > 0" size="mini" type="warning">
              {{ getAgentUsageCount(scope.row.agentKey) }} 个功能
            </el-tag>
            <span v-else style="color: #909399; font-size: 12px;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-edit" class="table-action-btn" @click="openEditor(scope.row)">编辑</el-button>
            <el-button type="text" size="small" icon="el-icon-delete" class="danger-text table-action-btn" @click="removeAgent(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 系统功能绑定 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">系统功能绑定</div>
        <div style="display:flex;gap:8px;">
          <el-button size="mini" icon="el-icon-close" @click="batchCloseUnintegrated">批量关闭未接入</el-button>
          <el-button size="mini" icon="el-icon-connection" @click="batchBindAgentVisible = true">批量绑定 Agent</el-button>
        </div>
      </div>
      <div class="api-key-desc">
        将系统功能绑定到指定 Agent，并分别控制其在所在页面和首页的显示。
      </div>
      <div class="integration-legend" style="margin-top: 8px;">
        <el-tag size="mini" type="success" style="margin-right: 6px;">已接入</el-tag>
        <span style="font-size: 12px; color: var(--apple-text-secondary); margin-right: 16px;">该功能已在前端页面实现，配置保存后立即生效</span>
        <el-tag size="mini" type="warning" style="margin-right: 6px;">未接入</el-tag>
        <span style="font-size: 12px; color: var(--apple-text-secondary);">该功能尚未在前端页面实现，配置仅保存在数据库中，开关设置不会生效</span>
      </div>
      <!-- 快速管理表格 -->
      <el-table
        :data="functionOverviewList"
        size="mini"
        style="width: 100%; margin-top: 16px;"
        v-loading="functionMappingLoading"
        border
        stripe
      >
        <el-table-column prop="functionName" label="功能名称" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <div style="display: flex; align-items: center; gap: 6px;">
              <span>{{ scope.row.functionName || scope.row.functionCode }}</span>
              <el-tag
                size="mini"
                :type="scope.row._integrationStatus === 'integrated' ? 'success' : 'warning'"
              >
                {{ scope.row._integrationLabel }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="agentName" label="绑定 Agent" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.agentName" style="color: #5A8F7B; font-weight: 500;">{{ scope.row.agentName }}</span>
            <span v-else-if="scope.row.agentKey" style="color: #5A8F7B;">{{ scope.row.agentKey }}</span>
            <span v-else style="color: #C0C4CC;">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column label="所在页面显示" width="110" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isVisibleOnPage"
              :active-value="true"
              :inactive-value="false"
              size="mini"
              @change="(val) => saveFunctionMappingToggle(scope.row, 'isVisibleOnPage', val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="首页 AI 显示" width="110" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.isVisibleOnHome"
              :active-value="true"
              :inactive-value="false"
              size="mini"
              @change="(val) => saveFunctionMappingToggle(scope.row, 'isVisibleOnHome', val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="selectedFunctionCode = scope.row.functionCode">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="function-binding-panel" style="margin-top: 20px;">
        <div class="function-select-row">
          <el-select
            v-model="selectedFunctionCode"
            size="small"
            placeholder="选择系统功能查看详情配置"
            style="width: 320px;"
            :loading="functionMappingLoading"
          >
            <el-option
              v-for="item in functionOverviewList"
              :key="item.functionCode"
              :label="(item.functionName || item.functionCode)"
              :value="item.functionCode"
            >
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <span>{{ item.functionName || item.functionCode }}</span>
                <el-tag
                  size="mini"
                  :type="item._integrationStatus === 'integrated' ? 'success' : 'warning'"
                  style="margin-left: 8px;"
                >
                  {{ item._integrationLabel }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </div>

        <div v-if="selectedFunctionItem" class="function-config-card">
          <div class="function-config-header">
            <span class="function-config-name">{{ selectedFunctionItem.functionName }}</span>
            <el-tag size="mini" type="info">{{ selectedFunctionItem.functionCode }}</el-tag>
            <el-tag v-if="selectedFunctionItem.pagePath" size="mini" type="success">{{ selectedFunctionItem.pagePath }}</el-tag>
            <el-tag
              size="mini"
              :type="selectedFunctionItem._integrationStatus === 'integrated' ? 'success' : 'warning'"
            >
              {{ selectedFunctionItem._integrationLabel }}
            </el-tag>
          </div>

          <div
            v-if="selectedFunctionItem._integrationStatus !== 'integrated'"
            class="integration-hint"
          >
            <i class="el-icon-warning-outline" style="margin-right: 6px;" />
            该功能尚未在前端页面接入。当前配置仅保存在数据库中，对应页面（{{ selectedFunctionItem._integrationPages }}）不会根据此开关显示或隐藏 AI 入口。
          </div>

          <div
            v-if="selectedFunctionItem._integrationStatus === 'integrated'"
            class="integration-hint integrated"
          >
            <i class="el-icon-success" style="margin-right: 6px;" />
            已接入页面：{{ selectedFunctionItem._integrationPages }}。配置保存后立即生效。
          </div>

          <div class="function-config-body">
            <div class="config-item">
              <div class="config-label">绑定 Agent</div>
              <el-select
                v-model="selectedFunctionItem.agentKey"
                size="small"
                placeholder="选择 Agent"
                style="width: 220px;"
                @change="(val) => saveFunctionMappingInline(selectedFunctionItem, val)"
              >
                <el-option label="-- 未绑定 --" value="" />
                <el-option
                  v-for="agent in agentList"
                  :key="agent.agentKey"
                  :label="agent.name + ' (' + agent.agentKey + ')'"
                  :value="agent.agentKey"
                />
              </el-select>
              <i
                v-if="selectedFunctionItem.agentKey"
                class="el-icon-success"
                style="color: #67c23a; margin-left: 6px;"
                title="已绑定"
              />
              <i
                v-else
                class="el-icon-warning"
                style="color: #e6a23c; margin-left: 6px;"
                title="未绑定"
              />
            </div>

            <div class="config-item">
              <div class="config-label">所在页面显示</div>
              <el-switch
                v-model="selectedFunctionItem.isVisibleOnPage"
                :active-value="true"
                :inactive-value="false"
                @change="(val) => saveFunctionMappingToggle(selectedFunctionItem, 'isVisibleOnPage', val)"
              />
            </div>

            <div class="config-item">
              <div class="config-label">首页 AI 下拉显示</div>
              <el-switch
                v-model="selectedFunctionItem.isVisibleOnHome"
                :active-value="true"
                :inactive-value="false"
                @change="(val) => saveFunctionMappingToggle(selectedFunctionItem, 'isVisibleOnHome', val)"
              />
            </div>
          </div>
        </div>

        <div v-else class="empty-hint">
          请先从上方下拉框选择一个系统功能进行配置
        </div>
      </div>
    </div>

    <!-- 全局响应字段配置 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">全局响应字段配置</div>
      </div>
      <div class="api-key-desc">
        设置 n8n 工作流返回的内容字段名。所有 AI Agent 统一使用此字段提取回复内容，留空则自动识别。
      </div>
      <div class="api-key-row" style="margin-top: 12px;">
        <el-input v-model="responseField" size="small" placeholder="如：content、reply、message" style="width: 240px;" maxlength="50" />
        <el-button size="small" type="primary" :loading="saveFieldLoading" @click="saveResponseField">保存</el-button>
      </div>
    </div>

    <!-- 运维工具 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">运维工具</div>
      </div>
      <div style="display:flex;gap:16px;flex-wrap:wrap;">
        <div style="flex:1;min-width:280px;">
          <div style="font-size:13px;font-weight:600;color:var(--apple-text-secondary);margin-bottom:10px;">Agent 连通性测试</div>
          <div style="display:flex;gap:8px;">
            <el-select v-model="testAgentKey" size="small" placeholder="选择 Agent" style="flex:1;">
              <el-option v-for="agent in agentList" :key="agent.agentKey" :label="agent.name" :value="agent.agentKey" />
            </el-select>
            <el-button size="small" type="primary" icon="el-icon-s-promotion" :loading="opsLoading" :disabled="!testAgentKey" @click="testAgentWebhook(testAgentKey)">测试</el-button>
          </div>
        </div>
        <div style="flex:1;min-width:280px;">
          <div style="font-size:13px;font-weight:600;color:var(--apple-text-secondary);margin-bottom:10px;">会话缓存管理</div>
          <div style="display:flex;gap:8px;">
            <el-button size="small" icon="el-icon-delete" @click="clearAllAiSessions">清空所有会话</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="loadLocalStorageKeys">刷新缓存列表</el-button>
          </div>
          <div v-if="localStorageKeys.length > 0" style="margin-top:10px;max-height:120px;overflow-y:auto;background:var(--apple-bg-primary);border-radius:8px;padding:8px;">
            <div v-for="item in localStorageKeys" :key="item.key" style="display:flex;align-items:center;justify-content:space-between;font-size:12px;padding:3px 0;">
              <span style="color:var(--apple-text-secondary);" :title="item.preview">{{ item.key }} ({{ item.size }}B)</span>
              <el-button type="text" size="mini" style="padding:0;" @click="clearLocalStorageItem(item.key)">清理</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 调用日志看板 -->
    <div class="section-card">
      <div class="section-header">
        <div class="section-title">调用日志看板</div>
        <div style="display:flex;gap:8px;align-items:center;">
          <el-date-picker v-model="logDateRange" size="small" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:220px;" @change="onLogDateChange" />
          <el-select v-model="logQuery.agentKey" size="small" placeholder="Agent" clearable style="width:120px;" @change="loadCallLogs">
            <el-option v-for="agent in agentList" :key="agent.agentKey" :label="agent.name" :value="agent.agentKey" />
          </el-select>
          <el-select v-model="logQuery.responseStatus" size="small" placeholder="状态" clearable style="width:100px;" @change="loadCallLogs">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="error" />
            <el-option label="超时" value="timeout" />
          </el-select>
          <el-button size="small" icon="el-icon-refresh" :loading="logLoading" @click="loadCallLogs">刷新</el-button>
        </div>
      </div>
      <div style="display:flex;gap:16px;margin-bottom:16px;flex-wrap:wrap;">
        <div class="stat-card">
          <div class="stat-value">{{ logStats.totalCount }}</div>
          <div class="stat-label">总调用次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color:#67c23a;">{{ logStats.successCount }}</div>
          <div class="stat-label">成功</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color:#f56c6c;">{{ logStats.errorCount }}</div>
          <div class="stat-label">失败</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color:#e6a23c;">{{ formatDuration(logStats.avgDuration) }}</div>
          <div class="stat-label">平均耗时</div>
        </div>
      </div>
      <el-table :data="logList" size="mini" style="width:100%;" v-loading="logLoading" max-height="360">
        <el-table-column prop="createdAt" label="时间" width="140">
          <template slot-scope="scope">{{ scope.row.createdAt ? new Date(scope.row.createdAt).toLocaleString() : '-' }}</template>
        </el-table-column>
        <el-table-column prop="agentName" label="Agent" width="100" show-overflow-tooltip />
        <el-table-column prop="accountName" label="用户" width="80" />
        <el-table-column prop="requestMessage" label="请求内容" min-width="150" show-overflow-tooltip />
        <el-table-column prop="responseStatus" label="状态" width="70" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="getLogStatusType(scope.row.responseStatus)">{{ scope.row.responseStatus || 'unknown' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时" width="70" align="center">
          <template slot-scope="scope">{{ formatDuration(scope.row.durationMs) }}</template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <span v-if="scope.row.errorMsg" style="color:#f56c6c;">{{ scope.row.errorMsg }}</span>
            <span v-else style="color:#909399;">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- API Key 管理卡片 -->
    <div class="section-card">
      <div class="section-title" style="margin-bottom: 12px;">API Key 管理</div>
      <div class="api-key-desc">
        API Key 用于第三方平台（如 n8n）调用系统接口获取数据。请妥善保管，泄露可能导致数据风险。
      </div>
      <div class="api-key-row">
        <div class="api-key-value">{{ maskedApiKey }}</div>
        <el-button size="small" icon="el-icon-document-copy" @click="copyApiKey">复制</el-button>
        <el-button size="small" icon="el-icon-refresh" :loading="regenerating" @click="regenerateApiKey">重新生成</el-button>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <el-dialog
      :title="editingId ? '编辑 Agent' : '新增 Agent'"
      :visible.sync="editorVisible"
      width="720px"
      :close-on-click-modal="false"
      :modal="false"
    >
      <el-form :model="form" label-width="110px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="功能名称">
              <el-input v-model="form.name" placeholder="请输入功能名称" maxlength="20" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AgentKey">
              <el-input v-model="form.agentKey" placeholder="唯一标识" maxlength="40" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="用途位置">
          <el-input v-model="form.usageLocation" placeholder="如：新增病历页、咨询分析" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="功能描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="描述该 Agent 的用途" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="Webhook 地址">
          <el-input v-model="form.endpointUrl" placeholder="https://n8n.xxx.com/webhook/xxx" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="认证类型">
              <el-select v-model="form.authType" placeholder="请选择" style="width:100%">
                <el-option label="无认证" value="none" />
                <el-option label="Bearer Token" value="bearer" />
                <el-option label="Basic Auth" value="basic" />
                <el-option label="API Key" value="api_key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证令牌">
              <el-input v-model="form.authToken" type="password" placeholder="Token / Key" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="响应类型">
              <el-select v-model="form.responseType" placeholder="请选择" style="width:100%">
                <el-option label="JSON（一次性返回）" value="json" />
                <el-option label="SSE（流式返回）" value="sse" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时秒数">
              <el-input-number v-model="form.timeoutSeconds" :min="5" :max="300" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="首页显示">
              <el-switch
                v-model="form.isVisibleOnHome"
                active-text="显示"
                inactive-text="隐藏"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="图标">
              <el-select v-model="form.icon" placeholder="选择图标" style="width:100%">
                <el-option
                  v-for="opt in iconOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                >
                  <i :class="opt.value" style="margin-right:6px" /> {{ opt.label }}
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="卡片样式">
          <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center;">
            <div
              v-for="preset in gradientPresets"
              :key="preset.value"
              class="gradient-preset"
              :class="{ active: form.gradient === preset.value }"
              :style="{ background: preset.value }"
              :title="preset.label"
              @click="form.gradient = preset.value"
            />
            <el-input
              v-model="form.gradient"
              size="mini"
              placeholder="自定义CSS渐变"
              style="width:200px"
            />
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="快捷指令">
              <div class="chip-editor">
                <el-tag
                  v-for="(chip, index) in form.chips"
                  :key="index"
                  closable
                  size="mini"
                  style="margin-right: 6px; margin-bottom: 4px;"
                  @close="handleChipClose(index)"
                >
                  {{ chip }}
                </el-tag>
                <el-input
                  v-if="chipInputVisible"
                  ref="chipInputRef"
                  v-model="chipInputValue"
                  size="mini"
                  style="width: 120px;"
                  placeholder="输入指令"
                  maxlength="30"
                  @keyup.enter.native="handleChipConfirm"
                  @blur="handleChipConfirm"
                />
                <el-button v-else size="mini" icon="el-icon-plus" @click="showChipInput">添加</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预设消息">
              <el-input
                v-model="form.presetMessage"
                placeholder="点击快捷卡片后填入输入框的内容"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="UI 展示模式">
              <el-select v-model="form.uiMode" placeholder="请选择" style="width:100%">
                <el-option
                  v-for="opt in uiModeOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="UI 配置 JSON">
          <el-input
            v-model="form.uiConfigJson"
            type="textarea"
            :rows="3"
            placeholder='{"columns": [...], "chartType": "line"}'
          />
          <div style="font-size:12px;color:#909399;margin-top:4px">
            根据展示模式配置个性化渲染参数，留空则使用默认布局。
          </div>
        </el-form-item>
        <el-form-item label="请求模板">
          <el-input v-model="form.requestTemplate" type="textarea" :rows="4" placeholder='请填写JSON格式的请求模板，支持变量替换' />
          <div v-pre style="font-size:12px;color:#909399;margin-top:4px">
            支持 {{变量}} 替换，留空则直接发送完整协议。可用变量：message、account_id、account_name、session_id、clinic_id 等。
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small" @click="editorVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="saveLoading" @click="saveAgent">保存</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import axios from 'axios'
import { getAdminSession } from '@/utils/adminSession'

/**
 * 系统功能与前端页面的接入状态映射
 * 用于在 AI 总览页标注每个功能是否已在前端页面实现配置读取和入口显示
 */
const PAGE_INTEGRATION_STATUS = {
  'medical-record-expand': { status: 'integrated', label: '已接入', pages: 'MedicalRecordView, PatientDetailView' },
  'consultation-assist': { status: 'integrated', label: '已接入', pages: 'ConsultationRecordDialog' },
  'consultation-dashboard': { status: 'pending', label: '未接入', pages: 'ConsultationDashboardView' },
  'appointment-assist': { status: 'integrated', label: '已接入', pages: 'AppointmentView' },
  'followup-assist': { status: 'pending', label: '未接入', pages: 'FollowupManagementView' },
  'treatment-assist': { status: 'pending', label: '未接入', pages: 'TreatmentView / TreatmentView2' },
  'treatment-record-assist': { status: 'pending', label: '未接入', pages: '无明确页面' },
  'financial-analysis': { status: 'pending', label: '未接入', pages: 'FinancialView / FinancialView2' },
  'monthly-bill-analysis': { status: 'pending', label: '未接入', pages: '无明确页面' },
  'lab-statistics-analysis': { status: 'pending', label: '未接入', pages: 'LabStatisticsView' },
  'material-category-assist': { status: 'pending', label: '未接入', pages: 'MaterialCategoryView' },
  'material-inventory-assist': { status: 'pending', label: '未接入', pages: 'InventoryView / MaterialView' },
  'material-purchase-assist': { status: 'pending', label: '未接入', pages: 'MaterialPurchaseView' },
  'material-statistics-analysis': { status: 'pending', label: '未接入', pages: 'MaterialStatisticsView' }
}

export default {
  name: 'AIOverviewView',
  data() {
    return {
      loading: false,
      agentList: [],
      apiKey: '',
      regenerating: false,
      editorVisible: false,
      editingId: null,
      saveLoading: false,
      responseField: 'content',
      saveFieldLoading: false,
      functionOverviewList: [],
      functionMappingLoading: false,
      securityLockEnabled: false,
      selectedFunctionCode: '',
      chipInputVisible: false,
      chipInputValue: '',
      form: {
        name: '',
        usageLocation: '',
        agentKey: '',
        description: '',
        endpointUrl: '',
        authType: 'none',
        authToken: '',
        requestTemplate: '',
        responseType: 'json',
        timeoutSeconds: 60,
        sortOrder: 0,
        isVisibleOnHome: true,
        chips: [],
        presetMessage: '',
        icon: '',
        gradient: '',
        uiMode: 'json',
        uiConfigJson: ''
      },
      iconOptions: [
        { label: 'CPU', value: 'el-icon-cpu' },
        { label: '数据', value: 'el-icon-data-line' },
        { label: '文档', value: 'el-icon-document' },
        { label: '聊天', value: 'el-icon-chat-dot-round' },
        { label: '图表', value: 'el-icon-s-data' },
        { label: '日历', value: 'el-icon-date' },
        { label: '设置', value: 'el-icon-setting' },
        { label: '医疗', value: 'el-icon-first-aid-kit' },
        { label: '用户', value: 'el-icon-user' },
        { label: '货币', value: 'el-icon-money' },
        { label: '警告', value: 'el-icon-warning' },
        { label: '成功', value: 'el-icon-success' }
      ],
      gradientPresets: [
        { label: '青瓷绿', value: 'linear-gradient(135deg, #5A8F7B 0%, #7EB5A2 100%)' },
        { label: '深海蓝', value: 'linear-gradient(135deg, #3B6E8F 0%, #6BA3C7 100%)' },
        { label: '暖橙', value: 'linear-gradient(135deg, #D4946A 0%, #E8B89A 100%)' },
        { label: '紫霞', value: 'linear-gradient(135deg, #7B6B8D 0%, #A99BB8 100%)' },
        { label: '石墨灰', value: 'linear-gradient(135deg, #64748b 0%, #94a3b8 100%)' },
        { label: '胭脂红', value: 'linear-gradient(135deg, #C75B5B 0%, #E08E8E 100%)' }
      ],
      uiModeOptions: [
        { label: '纯文本', value: 'json' },
        { label: '对话气泡', value: 'chat' },
        { label: '卡片布局', value: 'card' },
        { label: '表格布局', value: 'table' }
      ],
      // 运维工具
      opsLoading: false,
      testAgentKey: '',
      localStorageKeys: [],
      // 调用日志
      logLoading: false,
      logList: [],
      logQuery: {
        agentKey: '',
        responseStatus: '',
        startDate: '',
        endDate: ''
      },
      logDateRange: [],
      logStats: {
        totalCount: 0,
        successCount: 0,
        errorCount: 0,
        avgDuration: 0
      },
      // 全局配置扩展
      globalConfigForm: {
        responseField: 'content',
        defaultTimeout: 60,
        typingEffect: true,
        sessionRetentionDays: 7
      },
      globalConfigLoading: false
    }
  },
  computed: {
    maskedApiKey() {
      const key = this.apiKey || ''
      if (key.length <= 9) return key || '暂无 API Key'
      return key.slice(0, 6) + '******' + key.slice(-3)
    },
    clinicId() {
      const session = getAdminSession() || {}
      return session.clinicId || session.id || ''
    },
    selectedFunctionItem() {
      if (!this.selectedFunctionCode || !this.functionOverviewList.length) return null
      return this.functionOverviewList.find(item => item.functionCode === this.selectedFunctionCode) || null
    }
  },
  created() {
    this.securityLockEnabled = localStorage.getItem('ai_security_lock') === 'true'
    this.loadData()
    this.loadApiKey()
    this.loadGlobalConfig()
    this.loadFunctionMappings()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.get('/api/ai-agent-configs', { params: { accountId } })
        if (res.data && res.data.code === '200') {
          this.agentList = Array.isArray(res.data.data) ? res.data.data : []
        } else {
          this.agentList = []
          console.warn('[AIOverviewView] /api/ai-agent-configs 接口未返回成功数据')
        }
      } catch (error) {
        console.error('加载 Agent 列表失败:', error)
        this.agentList = []
      } finally {
        this.loading = false
      }
    },

    async loadApiKey() {
      try {
        const res = await axios.get('/api/api-key', { params: { clinicId: this.clinicId } })
        if (res.data && res.data.code === '200') {
          this.apiKey = (res.data.data && res.data.data.key) || ''
        }
      } catch (error) {
        console.error('加载 API Key 失败:', error)
        this.apiKey = ''
      }
    },

    async loadGlobalConfig() {
      try {
        const res = await axios.get('/api/ai/global-config/response_field')
        if (res.data && res.data.code === '200' && res.data.data) {
          this.responseField = res.data.data.configValue || 'content'
        }
      } catch (error) {
        console.error('加载全局配置失败:', error)
      }
    },

    async saveResponseField() {
      if (!this.responseField.trim()) {
        this.$message.warning('请输入字段名')
        return
      }
      this.saveFieldLoading = true
      try {
        const res = await axios.put('/api/ai/global-config/response_field', {
          configValue: this.responseField.trim()
        })
        if (res.data && res.data.code === '200') {
          this.$message.success('保存成功')
        } else {
          this.$message.error(res.data?.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存全局配置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveFieldLoading = false
      }
    },

    copyApiKey() {
      if (!this.apiKey) {
        this.$message.warning('暂无 API Key 可复制')
        return
      }
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(this.apiKey).then(() => {
          this.$message.success('API Key 已复制到剪贴板')
        }).catch(() => {
          this.fallbackCopy(this.apiKey)
        })
      } else {
        this.fallbackCopy(this.apiKey)
      }
    },

    fallbackCopy(text) {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.$message.success('API Key 已复制到剪贴板')
      } catch (e) {
        this.$message.error('复制失败，请手动复制')
      }
      document.body.removeChild(textarea)
    },

    regenerateApiKey() {
      this.$confirm('重新生成 API Key 后，旧 Key 将立即失效，是否继续？', '确认重新生成', {
        confirmButtonText: '重新生成',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.regenerating = true
        try {
          const res = await axios.post('/api/api-key/regenerate', null, { params: { clinicId: this.clinicId } })
          if (res.data && res.data.code === '200') {
            this.apiKey = (res.data.data && res.data.data.key) || ''
            this.$message.success('API Key 已重新生成')
          } else {
            this.$message.error(res.data?.msg || '重新生成失败')
          }
        } catch (error) {
          console.error('重新生成 API Key 失败:', error)
          this.$message.error('重新生成失败')
        } finally {
          this.regenerating = false
        }
      }).catch(() => {})
    },

    openEditor(row) {
      if (row) {
        this.editingId = row.id
        this.form = {
          name: row.name || '',
          usageLocation: row.usageLocation || '',
          agentKey: row.agentKey || '',
          description: row.description || '',
          endpointUrl: row.endpointUrl || '',
          authType: row.authType || 'none',
          authToken: row.authToken || '',
          requestTemplate: row.requestTemplate || '',
          responseType: row.responseType || 'json',
          timeoutSeconds: row.timeoutSeconds || 60,
          sortOrder: row.sortOrder || 0,
          isVisibleOnHome: row.isVisibleOnHome !== false && row.isVisibleOnHome !== 0,
          chips: Array.isArray(row.chips) ? [...row.chips] : [],
          presetMessage: row.presetMessage || '',
          icon: row.icon || '',
          gradient: row.gradient || '',
          uiMode: row.uiMode || 'json',
          uiConfigJson: row.uiConfigJson || ''
        }
      } else {
        this.editingId = null
        this.form = {
          name: '',
          usageLocation: '',
          agentKey: '',
          description: '',
          endpointUrl: '',
          authType: 'none',
          authToken: '',
          requestTemplate: '',
          responseType: 'json',
          timeoutSeconds: 60,
          sortOrder: 0,
          chips: [],
          presetMessage: '',
          icon: 'el-icon-cpu',
          gradient: 'linear-gradient(135deg, #5A8F7B 0%, #7EB5A2 100%)',
          uiMode: 'json',
          uiConfigJson: ''
        }
      }
      this.editorVisible = true
    },

    async saveAgent() {
      if (!this.form.name.trim()) {
        this.$message.error('请输入功能名称')
        return
      }
      if (!this.form.agentKey.trim()) {
        this.$message.error('请输入 AgentKey')
        return
      }
      this.saveLoading = true
      try {
        const session = getAdminSession() || {}
        const payload = {
          ...this.form,
          accountId: session.id || null
        }
        let res
        if (this.editingId) {
          res = await axios.put(`/api/ai-agent-configs/${this.editingId}`, payload)
        } else {
          res = await axios.post('/api/ai-agent-configs', payload)
        }
        if (res.data && res.data.code === '200') {
          this.$message.success(this.editingId ? '保存成功' : '新增成功')
          this.editorVisible = false
          this.loadData()
        } else {
          this.$message.error(res.data?.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存 Agent 失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },

    // ========== 快捷指令 chips 编辑 ==========
    handleChipClose(index) {
      this.form.chips.splice(index, 1)
    },
    showChipInput() {
      this.chipInputVisible = true
      this.chipInputValue = ''
      this.$nextTick(() => {
        this.$refs.chipInputRef && this.$refs.chipInputRef.focus()
      })
    },
    handleChipConfirm() {
      const val = this.chipInputValue.trim()
      if (val) {
        if (!this.form.chips) {
          this.form.chips = []
        }
        if (!this.form.chips.includes(val)) {
          this.form.chips.push(val)
        }
      }
      this.chipInputVisible = false
      this.chipInputValue = ''
    },

    getAgentUsageCount(agentKey) {
      if (!agentKey || !this.functionOverviewList) return 0
      return this.functionOverviewList.filter(item => item.agentKey === agentKey).length
    },

    toggleSecurityLock() {
      this.securityLockEnabled = !this.securityLockEnabled
      localStorage.setItem('ai_security_lock', this.securityLockEnabled.toString())
      this.$message.success(this.securityLockEnabled ? '安全锁已开启' : '安全锁已关闭')
    },

    async toggleAgentHomeVisibility(row, val) {
      try {
        const payload = { ...row, isVisibleOnHome: val }
        const res = await axios.put(`/api/ai-agent-configs/${row.id}`, payload)
        if (res.data && res.data.code === '200') {
          this.$message.success(val ? '已设为首页显示' : '已设为首页隐藏')
          row.isVisibleOnHome = val
        } else {
          this.$message.error(res.data?.msg || '更新失败')
          row.isVisibleOnHome = !val
        }
      } catch (error) {
        console.error('更新首页显示状态失败:', error)
        this.$message.error('更新失败')
        row.isVisibleOnHome = !val
      }
    },

    async removeAgent(row) {
      const doDelete = async () => {
        try {
          const res = await axios.delete(`/api/ai-agent-configs/${row.id}`)
          if (res.data && res.data.code === '200') {
            this.$message.success('已删除')
            this.loadData()
          } else {
            this.$message.error(res.data?.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除 Agent 失败:', error)
          this.$message.error('删除失败')
        }
      }

      // 查询该 Agent 被哪些系统功能引用
      let usages = []
      try {
        const usageRes = await axios.get(`/api/ai/function-mappings/agent-usages/${row.agentKey}`)
        if (usageRes.data && usageRes.data.code === '200') {
          usages = Array.isArray(usageRes.data.data) ? usageRes.data.data : []
        }
      } catch (e) {
        // 忽略查询失败
      }

      let confirmMsg = `确定删除「${row.name || row.agentKey}」吗？`
      if (usages.length > 0) {
        const usageNames = usages.map(u => `「${u.functionName || u.functionCode}」`).join('、')
        confirmMsg = `确定删除「${row.name || row.agentKey}」吗？\n\n⚠️ 该 Agent 正被以下系统功能引用：${usageNames}\n\n删除后，这些功能将无法正常使用。`
      }

      // 第一步确认
      this.$confirm(confirmMsg, '提示', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false
      }).then(() => {
        if (this.securityLockEnabled) {
          // 安全锁开启时，要求二次确认：输入 AgentKey
          this.$prompt(
            `安全锁已开启，请输入 AgentKey「${row.agentKey}」以确认删除`,
            '二次确认',
            {
              confirmButtonText: '确认删除',
              cancelButtonText: '取消',
              inputPattern: new RegExp(`^${row.agentKey.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`),
              inputErrorMessage: '输入的 AgentKey 不匹配'
            }
          ).then(() => {
            doDelete()
          }).catch(() => {})
        } else {
          doDelete()
        }
      }).catch(() => {})
    },

    async loadFunctionMappings() {
      this.functionMappingLoading = true
      try {
        const session = getAdminSession() || {}
        const accountId = session.id || ''
        const res = await axios.get('/api/ai/function-mappings/function-overview', { params: { accountId } })
        if (res.data && res.data.code === '200') {
          this.functionOverviewList = (Array.isArray(res.data.data) ? res.data.data : []).map(item => {
            const integration = PAGE_INTEGRATION_STATUS[item.functionCode] || { status: 'unknown', label: '未知', pages: '' }
            const isIntegrated = integration.status === 'integrated'
            // 未接入功能默认关闭显示开关
            const isVisibleOnPage = isIntegrated ? (item.isVisibleOnPage !== false) : false
            const isVisibleOnHome = isIntegrated ? (item.isVisibleOnHome !== false) : false
            return {
              ...item,
              isVisibleOnPage,
              isVisibleOnHome,
              _saving: false,
              _integrationStatus: integration.status,
              _integrationLabel: integration.label,
              _integrationPages: integration.pages
            }
          })
        } else {
          this.functionOverviewList = []
        }
      } catch (error) {
        console.error('加载功能总览失败:', error)
        this.functionOverviewList = []
      } finally {
        this.functionMappingLoading = false
      }
    },

    async saveFunctionMappingInline(row, val) {
      row.agentKey = val || ''
      await this.saveFunctionMappingCore(row)
    },

    async saveFunctionMappingToggle(row, field, val) {
      row[field] = val
      await this.saveFunctionMappingCore(row)
    },

    async saveFunctionMappingCore(row) {
      this.$set(row, '_saving', true)
      try {
        const session = getAdminSession() || {}
        const payload = {
          accountId: session.id || null,
          agentKey: row.agentKey || '',
          isVisibleOnPage: row.isVisibleOnPage === true ? true : false,
          isVisibleOnHome: row.isVisibleOnHome === true ? true : false,
          sortOrder: row.sortOrder || 0
        }
        const res = await axios.put(`/api/ai/function-mappings/${row.functionCode}`, payload)
        if (res.data && res.data.code === '200') {
          this.$message.success('保存成功')
        } else {
          this.$message.error(res.data?.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存功能映射失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.$set(row, '_saving', false)
      }
    },

    // ========== 批量操作 ==========
    batchCloseUnintegrated() {
      const unintegrated = this.functionOverviewList.filter(
        item => item._integrationStatus !== 'integrated' && (item.isVisibleOnPage || item.isVisibleOnHome)
      )
      if (unintegrated.length === 0) {
        this.$message.info('没有需要关闭的未接入功能')
        return
      }
      this.$confirm(`确定批量关闭 ${unintegrated.length} 个未接入功能的显示开关？`, '批量关闭', {
        confirmButtonText: '确定关闭',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        for (const row of unintegrated) {
          row.isVisibleOnPage = false
          row.isVisibleOnHome = false
          await this.saveFunctionMappingCore(row)
        }
        this.$message.success(`已关闭 ${unintegrated.length} 个未接入功能`)
      }).catch(() => {})
    },

    // ========== 运维工具 ==========
    loadLocalStorageKeys() {
      const keys = []
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i)
        if (key && (key.startsWith('ai_') || key.includes('session'))) {
          const value = localStorage.getItem(key)
          keys.push({
            key,
            size: value ? value.length : 0,
            preview: value ? value.substring(0, 60) + (value.length > 60 ? '...' : '') : ''
          })
        }
      }
      this.localStorageKeys = keys.sort((a, b) => b.size - a.size)
    },
    clearLocalStorageItem(key) {
      localStorage.removeItem(key)
      this.loadLocalStorageKeys()
      this.$message.success('已清理')
    },
    clearAllAiSessions() {
      this.$confirm('确定清空所有 AI 会话缓存？此操作不可恢复。', '确认清空', {
        confirmButtonText: '清空',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const keysToRemove = []
        for (let i = 0; i < localStorage.length; i++) {
          const key = localStorage.key(i)
          if (key && key.startsWith('ai_sessions_v2_')) {
            keysToRemove.push(key)
          }
        }
        keysToRemove.forEach(k => localStorage.removeItem(k))
        this.loadLocalStorageKeys()
        this.$message.success(`已清空 ${keysToRemove.length} 个会话缓存`)
      }).catch(() => {})
    },
    async testAgentWebhook(agentKey) {
      this.opsLoading = true
      try {
        const session = getAdminSession() || {}
        const res = await axios.post(`/api/ai/proxy/${encodeURIComponent(agentKey)}`, {
          message: '这是一条测试消息，来自 AI 设置页面的连通性检测。',
          account_id: String(session.id || ''),
          account_name: session.name || '',
          session_id: 'test-' + Date.now(),
          clinic_id: '1'
        })
        if (res.data && (res.data.code === '200' || res.data.code === 200)) {
          this.$message.success('Agent 连通性测试通过')
        } else {
          this.$message.warning(`Agent 返回异常：${res.data?.msg || '未知错误'}`)
        }
      } catch (error) {
        const msg = error?.response?.data?.msg || error.message || '请求失败'
        this.$message.error(`连通性测试失败：${msg}`)
      } finally {
        this.opsLoading = false
      }
    },

    // ========== 调用日志 ==========
    async loadCallLogs() {
      this.logLoading = true
      try {
        const session = getAdminSession() || {}
        const params = {
          accountId: session.id || '',
          agentKey: this.logQuery.agentKey || '',
          responseStatus: this.logQuery.responseStatus || '',
          startDate: this.logQuery.startDate || '',
          endDate: this.logQuery.endDate || ''
        }
        const [listRes, summaryRes] = await Promise.all([
          axios.get('/api/ai/call-logs', { params }),
          axios.get('/api/ai/call-logs/summary', { params })
        ])
        if (listRes.data && listRes.data.code === '200') {
          this.logList = Array.isArray(listRes.data.data) ? listRes.data.data : []
        }
        if (summaryRes.data && summaryRes.data.code === '200') {
          const s = summaryRes.data.data || {}
          this.logStats = {
            totalCount: s.totalCount || 0,
            successCount: s.successCount || 0,
            errorCount: s.errorCount || 0,
            avgDuration: Math.round(s.avgDuration || 0)
          }
        }
      } catch (error) {
        console.error('加载调用日志失败:', error)
      } finally {
        this.logLoading = false
      }
    },
    resetLogQuery() {
      this.logQuery = { agentKey: '', responseStatus: '', startDate: '', endDate: '' }
      this.logDateRange = []
      this.loadCallLogs()
    },
    onLogDateChange(val) {
      if (val && val.length === 2) {
        this.logQuery.startDate = val[0]
        this.logQuery.endDate = val[1]
      } else {
        this.logQuery.startDate = ''
        this.logQuery.endDate = ''
      }
      this.loadCallLogs()
    },
    getLogStatusType(status) {
      if (status === 'success') return 'success'
      if (status === 'error') return 'danger'
      if (status === 'timeout') return 'warning'
      return 'info'
    },
    formatDuration(ms) {
      if (!ms) return '0ms'
      if (ms < 1000) return ms + 'ms'
      return (ms / 1000).toFixed(1) + 's'
    }
  }
}
</script>

<style scoped>
.ai-overview-page {
  padding: 0 0 32px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-header-left {
  flex: 1;
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

/* 卡片区块 */
.section-card {
  background: var(--apple-surface);
  backdrop-filter: var(--apple-surface-blur);
  -webkit-backdrop-filter: var(--apple-surface-blur);
  border: var(--apple-surface-border);
  box-shadow: var(--apple-shadow-md), var(--apple-surface-shadow-inset);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  transition: box-shadow 0.3s ease;
}

.section-card:hover {
  box-shadow: var(--apple-shadow-lg), var(--apple-surface-shadow-inset);
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

.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.danger-text {
  color: #f56c6c;
}

.table-action-btn {
  padding: 0 4px !important;
  margin-left: 0 !important;
}

/* API Key 区域 */
.api-key-desc {
  font-size: 13px;
  color: var(--apple-text-secondary);
  margin-bottom: 12px;
  line-height: 1.5;
}

.api-key-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.api-key-value {
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  font-weight: 600;
  color: var(--apple-text-primary);
  background: var(--apple-bg-secondary);
  padding: 8px 14px;
  border-radius: 8px;
  letter-spacing: 0.04em;
  word-break: break-all;
}

/* 功能绑定卡片 */
.function-config-card {
  margin-top: 16px;
  padding: 20px;
  background: var(--apple-bg-primary);
  border-radius: 12px;
  border: 1px solid var(--apple-divider);
}

.function-config-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.function-config-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--apple-text-primary);
}

.function-config-body {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: flex-end;
}

.config-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--apple-text-secondary);
}

.integration-hint {
  margin-bottom: 16px;
  padding: 10px 14px;
  font-size: 12px;
  color: var(--apple-warning);
  background: rgba(230, 162, 60, 0.08);
  border-radius: 8px;
  border: 1px solid rgba(230, 162, 60, 0.2);
  display: flex;
  align-items: flex-start;
  line-height: 1.5;
}

.integration-hint.integrated {
  color: var(--apple-success);
  background: rgba(103, 194, 58, 0.08);
  border-color: rgba(103, 194, 58, 0.2);
}

.empty-hint {
  margin-top: 24px;
  padding: 32px;
  text-align: center;
  font-size: 13px;
  color: var(--apple-text-tertiary);
  background: var(--apple-bg-primary);
  border-radius: 12px;
  border: 1px dashed var(--apple-divider);
}

/* 响应式 */
@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .api-key-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .function-config-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}

/* 快捷指令编辑器 */
.chip-editor {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  min-height: 32px;
}

/* 渐变色预设 */
.gradient-preset {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}
.gradient-preset:hover {
  transform: scale(1.1);
}
.gradient-preset.active {
  border-color: var(--apple-text-primary);
  box-shadow: 0 0 0 2px rgba(90, 143, 123, 0.2);
}

/* 统计卡片 */
.stat-card {
  flex: 1;
  min-width: 120px;
  background: var(--apple-bg-primary);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  border: 1px solid var(--apple-divider);
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--apple-text-primary);
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--apple-text-secondary);
  margin-top: 4px;
}
</style>

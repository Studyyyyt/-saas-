/**
 * AI Agent 元信息映射表
 * 定义所有可用 agent 的显示名称、用途描述及请求示例
 */

export const AGENT_META_MAP = {
  // ==================== 已有 9 个 ====================

  'medical-expand': {
    agentKey: 'medical-expand',
    name: '病历扩写',
    description: '根据医生输入的简短病历草稿，AI 扩写为完整规范的病历内容，包括主诉、现病史、检查结果、诊断等完整章节',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1001',
        patient_name: '张三',
        age: 35,
        gender: '男',
        chief_complaint: '牙痛3天，要求拔牙',
        present_illness_history: '患者3天前开始出现右下后牙疼痛，冷热刺激加重，夜间痛明显',
        past_medical_history: '高血压病史2年，规律服药；否认糖尿病、心脏病',
        allergy_history: '青霉素过敏',
        examination_findings: '右下第一磨牙叩痛（+），松动I度，牙龈轻度红肿',
        diagnosis: '右下第一磨牙慢性牙髓炎急性发作',
        draft_record: '患者牙痛3天，要求拔牙。检查见右下第一磨牙叩痛，松动I度。'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1001',
      patient_name: '张三',
      age: 35,
      gender: '男',
      chief_complaint: '牙痛3天，要求拔牙',
      present_illness_history: '患者3天前开始出现右下后牙疼痛，冷热刺激加重，夜间痛明显',
      past_medical_history: '高血压病史2年，规律服药；否认糖尿病、心脏病',
      allergy_history: '青霉素过敏',
      examination_findings: '右下第一磨牙叩痛（+），松动I度，牙龈轻度红肿',
      diagnosis: '右下第一磨牙慢性牙髓炎急性发作',
      draft_record: '患者牙痛3天，要求拔牙。检查见右下第一磨牙叩痛，松动I度。'
    },
    exampleInputFields: {
      patient_id: '患者ID，用于关联患者档案',
      patient_name: '患者姓名',
      age: '患者年龄（数字）',
      gender: '患者性别（男/女）',
      chief_complaint: '主诉：患者就诊的主要原因',
      present_illness_history: '现病史：疾病发生、发展、诊疗经过',
      past_medical_history: '既往病史：患者过去的疾病、手术、住院史',
      allergy_history: '过敏史：药物、食物等过敏情况',
      examination_findings: '检查结果：口腔检查、影像检查等客观发现',
      diagnosis: '初步诊断：医生给出的诊断结论',
      draft_record: '医生输入的草稿记录（简短描述，AI基于此扩写）'
    },
    uiConfig: {
      mode: 'json',
      title: '病历扩写',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'patient-insight': {
    agentKey: 'patient-insight',
    name: '患者洞察',
    description: '综合分析患者历史就诊数据、治疗记录、消费情况，生成个性化诊疗建议、潜在风险提醒和复诊推荐',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1',
        patient_name: '张三',
        gender: '男',
        age: '35',
        phone: '13800138000',
        total_spent: '5000',
        visit_count: '3',
        last_visit_date: '2024-01-15',
        latest_treatment: '洗牙',
        patient_tags: ['VIP', '正畸意向'],
        has_arrears: false,
        arrears_amount: '0'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1',
      patient_name: '张三',
      gender: '男',
      age: '35',
      phone: '13800138000',
      total_spent: '5000',
      visit_count: '3',
      last_visit_date: '2024-01-15',
      latest_treatment: '洗牙',
      patient_tags: ['VIP', '正畸意向'],
      has_arrears: false,
      arrears_amount: '0'
    },
    exampleInputFields: {
      patient_id: '患者ID',
      patient_name: '患者姓名',
      gender: '性别',
      age: '年龄',
      phone: '联系电话',
      total_spent: '累计消费金额（元）',
      visit_count: '就诊次数',
      last_visit_date: '上次就诊日期',
      latest_treatment: '最近治疗项目',
      patient_tags: '患者标签列表',
      has_arrears: '是否有欠费（true/false）',
      arrears_amount: '欠费金额（元）'
    },
    uiConfig: {
      mode: 'json',
      title: '患者洞察',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'followup-generate': {
    agentKey: 'followup-generate',
    name: '随访生成',
    description: '根据患者治疗方案、术后情况自动生成个性化随访计划、提醒话术和注意事项',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1001',
        patient_name: '张三',
        contact_phone: '13800138000',
        treatment_plan: '根管治疗（已完成第一步），术后7天复查',
        treatment_type: '根管治疗',
        last_visit_date: '2026-05-10',
        doctor_name: '王医生',
        followup_interval: 7,
        operation_details: '右下第一磨牙根管预备，封氢氧化钙',
        postop_instructions: '避免患侧咀嚼，如有剧烈疼痛及时复诊',
        patient_preferences: ' prefer 微信联系'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1001',
      patient_name: '张三',
      contact_phone: '13800138000',
      treatment_plan: '根管治疗（已完成第一步），术后7天复查',
      treatment_type: '根管治疗',
      last_visit_date: '2026-05-10',
      doctor_name: '王医生',
      followup_interval: 7,
      operation_details: '右下第一磨牙根管预备，封氢氧化钙',
      postop_instructions: '避免患侧咀嚼，如有剧烈疼痛及时复诊',
      patient_preferences: ' prefer 微信联系'
    },
    exampleInputFields: {
      patient_id: '患者ID',
      patient_name: '患者姓名',
      contact_phone: '患者联系电话',
      treatment_plan: '治疗方案描述',
      treatment_type: '治疗类型（如根管治疗、拔牙、种植等）',
      last_visit_date: '上次就诊/手术日期',
      doctor_name: '主治医生姓名',
      followup_interval: '建议随访间隔天数（数字）',
      operation_details: '手术/治疗操作详情',
      postop_instructions: '已给出的术后医嘱',
      patient_preferences: '患者联系偏好（如微信、电话、短信）'
    },
    uiConfig: {
      mode: 'json',
      title: '随访生成',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'business-analysis': {
    agentKey: 'business-analysis',
    name: '经营分析',
    description: '分析诊所运营核心数据（营收、患者量、转化率、科室效能），提供经营优化建议和风险预警',
    exampleRequestBody: {
      input_fields: {
        start_date: '2026-04-01',
        end_date: '2026-04-30',
        clinic_id: '1',
        clinic_name: '舒澳口腔诊所',
        metrics: ['revenue', 'patient_count', 'appointment_count', 'conversion_rate', 'avg_spending'],
        compare_with_previous: true,
        previous_start_date: '2026-03-01',
        previous_end_date: '2026-03-31',
        group_by: 'week',
        department_stats: [
          { department: '口腔内科', revenue: 120000, patient_count: 150 },
          { department: '口腔外科', revenue: 80000, patient_count: 80 }
        ],
        new_patient_count: 120,
        returning_patient_count: 230,
        appointment_no_show_rate: 0.08
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      start_date: '2026-04-01',
      end_date: '2026-04-30',
      clinic_id: '1',
      clinic_name: '舒澳口腔诊所',
      metrics: ['revenue', 'patient_count', 'appointment_count', 'conversion_rate', 'avg_spending'],
      compare_with_previous: true,
      previous_start_date: '2026-03-01',
      previous_end_date: '2026-03-31',
      group_by: 'week',
      department_stats: [
        { department: '口腔内科', revenue: 120000, patient_count: 150 },
        { department: '口腔外科', revenue: 80000, patient_count: 80 }
      ],
      new_patient_count: 120,
      returning_patient_count: 230,
      appointment_no_show_rate: 0.08
    },
    exampleInputFields: {
      start_date: '统计开始日期（YYYY-MM-DD）',
      end_date: '统计结束日期（YYYY-MM-DD）',
      clinic_id: '诊所ID',
      clinic_name: '诊所名称',
      metrics: '分析指标列表（revenue营收/patient_count患者数/appointment_count预约数/conversion_rate转化率/avg_spending客单价）',
      compare_with_previous: '是否对比上一周期（true/false）',
      previous_start_date: '上一周期开始日期',
      previous_end_date: '上一周期结束日期',
      group_by: '分组维度（day/week/month）',
      department_stats: '各科室统计数据（含科室名、营收、患者数）',
      new_patient_count: '新增患者数',
      returning_patient_count: '复诊患者数',
      appointment_no_show_rate: '预约未到店率（0-1之间小数）'
    },
    uiConfig: {
      mode: 'json',
      title: '经营分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'lab-order-analysis': {
    agentKey: 'lab-order-analysis',
    name: '加工订单分析',
    description: '分析义齿加工订单数据，识别交付延迟风险、质量异常、成本偏差和供应商表现问题',
    exampleRequestBody: {
      input_fields: {
        order_id: 'LAB20260001',
        factory_id: 'F01',
        factory_name: 'XX义齿加工厂',
        order_type: '烤瓷冠',
        product_details: '右下第一磨牙烤瓷冠，色阶A2',
        patient_id: '1001',
        patient_name: '张三',
        order_date: '2026-05-01',
        due_date: '2026-05-08',
        actual_delivery_date: null,
        status: '加工中',
        unit_price: 350.00,
        quantity: 1,
        total_amount: 350.00,
        doctor_name: '王医生',
        quality_check_result: null,
        remarks: '加急'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      order_id: 'LAB20260001',
      factory_id: 'F01',
      factory_name: 'XX义齿加工厂',
      order_type: '烤瓷冠',
      product_details: '右下第一磨牙烤瓷冠，色阶A2',
      patient_id: '1001',
      patient_name: '张三',
      order_date: '2026-05-01',
      due_date: '2026-05-08',
      actual_delivery_date: null,
      status: '加工中',
      unit_price: 350.00,
      quantity: 1,
      total_amount: 350.00,
      doctor_name: '王医生',
      quality_check_result: null,
      remarks: '加急'
    },
    exampleInputFields: {
      order_id: '加工订单编号',
      factory_id: '加工厂ID',
      factory_name: '加工厂名称',
      order_type: '订单类型（烤瓷冠/活动义齿/种植体等）',
      product_details: '产品详细规格',
      patient_id: '患者ID',
      patient_name: '患者姓名',
      order_date: '下单日期',
      due_date: '预定交货日期',
      actual_delivery_date: '实际交货日期（null表示未交付）',
      status: '订单状态（待发送/加工中/已完成/已退回等）',
      unit_price: '单价（元）',
      quantity: '数量',
      total_amount: '总金额（元）',
      doctor_name: '开单医生',
      quality_check_result: '质检结果',
      remarks: '备注'
    },
    uiConfig: {
      mode: 'json',
      title: '加工订单分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'lab-factory-analysis': {
    agentKey: 'lab-factory-analysis',
    name: '加工厂分析',
    description: '评估加工厂合作效率、质量指标、交付时效和性价比，提供供应商优化建议',
    exampleRequestBody: {
      input_fields: {
        factory_id: 'F01',
        factory_name: 'XX义齿加工厂',
        evaluation_period: '2026-Q1',
        order_count: 156,
        total_amount: 52400.00,
        avg_delivery_days: 5.2,
        on_time_delivery_rate: 0.88,
        return_rate: 0.03,
        quality_score: 4.5,
        price_level: '中等',
        contact_name: '张经理',
        contact_phone: '13800138001',
        cooperation_start_date: '2024-06-01',
        product_types: ['烤瓷冠', '活动义齿', '种植体上部修复']
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      factory_id: 'F01',
      factory_name: 'XX义齿加工厂',
      evaluation_period: '2026-Q1',
      order_count: 156,
      total_amount: 52400.00,
      avg_delivery_days: 5.2,
      on_time_delivery_rate: 0.88,
      return_rate: 0.03,
      quality_score: 4.5,
      price_level: '中等',
      contact_name: '张经理',
      contact_phone: '13800138001',
      cooperation_start_date: '2024-06-01',
      product_types: ['烤瓷冠', '活动义齿', '种植体上部修复']
    },
    exampleInputFields: {
      factory_id: '加工厂ID',
      factory_name: '加工厂名称',
      evaluation_period: '评估周期（如2026-Q1、2026-04）',
      order_count: '订单总数',
      total_amount: '订单总金额（元）',
      avg_delivery_days: '平均交货天数',
      on_time_delivery_rate: '准时交付率（0-1之间小数）',
      return_rate: '返工率（0-1之间小数）',
      quality_score: '质量评分（1-5分）',
      price_level: '价格水平（高/中等/低）',
      contact_name: '联系人姓名',
      contact_phone: '联系人电话',
      cooperation_start_date: '合作开始日期',
      product_types: '加工产品类型列表'
    },
    uiConfig: {
      mode: 'json',
      title: '加工厂分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'ad-spending-analysis': {
    agentKey: 'ad-spending-analysis',
    name: '投放分析',
    description: '分析广告投放渠道效果、ROI、转化路径和获客成本，提供预算优化和渠道调整建议',
    exampleRequestBody: {
      input_fields: {
        channel: '微信朋友圈',
        campaign_name: '五一种植牙活动',
        start_date: '2026-04-01',
        end_date: '2026-04-30',
        budget: 15000.00,
        actual_spend: 14200.00,
        clicks: 3200,
        impressions: 85000,
        conversions: 45,
        appointments_booked: 32,
        new_patients: 28,
        cost_per_click: 4.44,
        cost_per_conversion: 315.56,
        cost_per_new_patient: 507.14,
        conversion_rate: 0.0141,
        landing_page: 'https://clinic.com/activity/may-implant'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      channel: '微信朋友圈',
      campaign_name: '五一种植牙活动',
      start_date: '2026-04-01',
      end_date: '2026-04-30',
      budget: 15000.00,
      actual_spend: 14200.00,
      clicks: 3200,
      impressions: 85000,
      conversions: 45,
      appointments_booked: 32,
      new_patients: 28,
      cost_per_click: 4.44,
      cost_per_conversion: 315.56,
      cost_per_new_patient: 507.14,
      conversion_rate: 0.0141,
      landing_page: 'https://clinic.com/activity/may-implant'
    },
    exampleInputFields: {
      channel: '投放渠道（微信朋友圈/抖音/美团/百度等）',
      campaign_name: '活动/计划名称',
      start_date: '投放开始日期',
      end_date: '投放结束日期',
      budget: '投放预算（元）',
      actual_spend: '实际花费（元）',
      clicks: '点击量',
      impressions: '曝光量',
      conversions: '转化数（表单提交/咨询等）',
      appointments_booked: '预约数',
      new_patients: '新患者数',
      cost_per_click: '单次点击成本（元）',
      cost_per_conversion: '单次转化成本（元）',
      cost_per_new_patient: '单客获客成本（元）',
      conversion_rate: '转化率（0-1之间小数）',
      landing_page: '落地页链接'
    },
    uiConfig: {
      mode: 'json',
      title: '投放分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'doctor-schedule': {
    agentKey: 'doctor-schedule',
    name: '排班建议',
    description: '根据预约量、医生专长、历史工作负荷和请假情况，智能推荐最优排班方案',
    exampleRequestBody: {
      input_fields: {
        date: '2026-05-20',
        department: '口腔内科',
        doctor_list: [
          { doctor_id: 'D01', doctor_name: '李医生', specialty: '牙体牙髓', max_patients: 12 },
          { doctor_id: 'D02', doctor_name: '王医生', specialty: '牙周治疗', max_patients: 10 }
        ],
        appointments_count: 18,
        appointments_by_type: { '洗牙': 5, '根管治疗': 4, '补牙': 6, '复诊': 3 },
        doctors_on_leave: ['D03'],
        operating_hours: { start: '09:00', end: '18:00' },
        lunch_break: { start: '12:00', end: '13:30' },
        chair_count: 4,
        peak_hours: ['09:00-11:00', '14:00-16:00']
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      date: '2026-05-20',
      department: '口腔内科',
      doctor_list: [
        { doctor_id: 'D01', doctor_name: '李医生', specialty: '牙体牙髓', max_patients: 12 },
        { doctor_id: 'D02', doctor_name: '王医生', specialty: '牙周治疗', max_patients: 10 }
      ],
      appointments_count: 18,
      appointments_by_type: { '洗牙': 5, '根管治疗': 4, '补牙': 6, '复诊': 3 },
      doctors_on_leave: ['D03'],
      operating_hours: { start: '09:00', end: '18:00' },
      lunch_break: { start: '12:00', end: '13:30' },
      chair_count: 4,
      peak_hours: ['09:00-11:00', '14:00-16:00']
    },
    exampleInputFields: {
      date: '排班日期',
      department: '科室名称',
      doctor_list: '医生列表（含ID、姓名、专长、最大接诊数）',
      appointments_count: '已预约患者总数',
      appointments_by_type: '按治疗类型分组的预约数量',
      doctors_on_leave: '请假医生ID列表',
      operating_hours: '营业时间（start/end）',
      lunch_break: '午休时间（start/end）',
      chair_count: '治疗椅数量',
      peak_hours: '高峰时段列表'
    },
    uiConfig: {
      mode: 'json',
      title: '排班建议',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'home-assistant': {
    agentKey: 'home-assistant',
    name: '首页助手',
    description: '首页 AI 对话助手，回答各类业务咨询问题，支持多轮对话和上下文理解',
    exampleRequestBody: {
      input_fields: {
        question: '今天有多少个预约？',
        context: '首页概览',
        session_id: 'sess_20260514_001',
        history: [
          { role: 'user', content: '帮我查一下本周的经营数据' },
          { role: 'assistant', content: '本周营收12.5万元，患者数180人...' }
        ],
        current_page: 'home',
        user_role: 'admin'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    streamExample: {
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      session_id: 'sess_001',
      message: '今天有多少个预约？',
      functionKey: 'chat-assistant',
      question: '今天有多少个预约？',
      context: '首页概览',
      current_page: 'home',
      user_role: 'admin'
    },
    exampleInputFields: {
      question: '用户当前问题',
      context: '对话上下文或页面场景描述',
      session_id: '会话唯一标识',
      history: '历史对话记录列表（含role和content）',
      current_page: '当前所在页面标识',
      user_role: '当前用户角色（admin/doctor/nurse）'
    },
    uiConfig: {
      mode: 'chat',
      title: '首页助手',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  // ==================== 新增 13 个 ====================

  'appointment-assist': {
    agentKey: 'appointment-assist',
    name: '预约辅助',
    description: '辅助预约管理，智能推荐时间段、处理冲突、优化资源分配，并生成预约提醒话术',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1001',
        patient_name: '张三',
        contact_phone: '13800138000',
        preferred_date: '2026-05-16',
        preferred_time: '14:00-16:00',
        treatment_type: '洗牙',
        treatment_duration: 60,
        doctor_id: 'D01',
        doctor_name: '李医生',
        current_appointments: [
          { time: '09:00-10:00', patient: '李四', type: '根管治疗' },
          { time: '10:30-11:30', patient: '王五', type: '复诊' }
        ],
        urgency: '一般',
        special_requests: '希望安排在下午',
        last_appointment_date: '2026-02-10'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1001',
      patient_name: '张三',
      contact_phone: '13800138000',
      preferred_date: '2026-05-16',
      preferred_time: '14:00-16:00',
      treatment_type: '洗牙',
      treatment_duration: 60,
      doctor_id: 'D01',
      doctor_name: '李医生',
      current_appointments: [
        { time: '09:00-10:00', patient: '李四', type: '根管治疗' },
        { time: '10:30-11:30', patient: '王五', type: '复诊' }
      ],
      urgency: '一般',
      special_requests: '希望安排在下午',
      last_appointment_date: '2026-02-10'
    },
    exampleInputFields: {
      patient_id: '患者ID',
      patient_name: '患者姓名',
      contact_phone: '患者联系电话',
      preferred_date: '期望预约日期',
      preferred_time: '期望时间段',
      treatment_type: '治疗类型',
      treatment_duration: '预计治疗时长（分钟）',
      doctor_id: '指定医生ID（可选）',
      doctor_name: '指定医生姓名',
      current_appointments: '该医生/时间段已有预约列表',
      urgency: '紧急程度（紧急/一般/可延后）',
      special_requests: '患者特殊要求',
      last_appointment_date: '上次预约日期'
    },
    uiConfig: {
      mode: 'json',
      title: '预约辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'followup-assist': {
    agentKey: 'followup-assist',
    name: '回访辅助',
    description: '辅助回访管理，根据患者治疗阶段自动生成回访话术、记录建议和客户关怀方案',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1001',
        patient_name: '张三',
        contact_phone: '13800138000',
        followup_type: '术后回访',
        followup_stage: '术后3天',
        last_visit_date: '2026-05-10',
        treatment_record: '右下第一磨牙根管治疗第一步',
        doctor_name: '王医生',
        contact_method: '电话',
        previous_followups: [
          { date: '2026-05-11', result: '接通，患者反馈轻微疼痛', status: '已完成' }
        ],
        patient_satisfaction: null,
        issues_reported: null
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1001',
      patient_name: '张三',
      contact_phone: '13800138000',
      followup_type: '术后回访',
      followup_stage: '术后3天',
      last_visit_date: '2026-05-10',
      treatment_record: '右下第一磨牙根管治疗第一步',
      doctor_name: '王医生',
      contact_method: '电话',
      previous_followups: [
        { date: '2026-05-11', result: '接通，患者反馈轻微疼痛', status: '已完成' }
      ],
      patient_satisfaction: null,
      issues_reported: null
    },
    exampleInputFields: {
      patient_id: '患者ID',
      patient_name: '患者姓名',
      contact_phone: '患者联系电话',
      followup_type: '回访类型（术后回访/满意度调查/复诊提醒/生日关怀等）',
      followup_stage: '回访阶段（如术后3天/术后7天/术后30天）',
      last_visit_date: '上次就诊日期',
      treatment_record: '治疗记录摘要',
      doctor_name: '主治医生',
      contact_method: '联系方式（电话/微信/短信）',
      previous_followups: '历史回访记录列表',
      patient_satisfaction: '患者满意度评分（1-5，回访后填写）',
      issues_reported: '患者反馈问题'
    },
    uiConfig: {
      mode: 'json',
      title: '回访辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'consultation-assist': {
    agentKey: 'consultation-assist',
    name: '咨询辅助',
    description: '辅助前台咨询接待，根据患者问题智能生成专业回复建议、推荐治疗方案和报价参考',
    exampleRequestBody: {
      input_fields: {
        consultation_id: '1001',
        contact_name: '李四',
        chief_project: '种植牙',
        intent_level: '高',
        remarks: '客户预算充足，正在比较两家医院',
        customer_concerns: '担心手术疼痛'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      consultation_id: '1001',
      contact_name: '李四',
      chief_project: '种植牙',
      intent_level: '高',
      remarks: '客户预算充足，正在比较两家医院',
      customer_concerns: '担心手术疼痛'
    },
    exampleInputFields: {
      consultation_id: '咨询记录ID',
      contact_name: '联系人姓名',
      chief_project: '意向项目',
      intent_level: '意向等级（高/中/低）',
      remarks: '备注信息',
      customer_concerns: '客户顾虑'
    },
    uiConfig: {
      mode: 'json',
      title: '咨询辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'consultation-dashboard': {
    agentKey: 'consultation-dashboard',
    name: '咨询分析',
    description: '分析咨询转化率、渠道效果、热门问题和咨询师绩效，提供咨询流程优化建议',
    exampleRequestBody: {
      input_fields: {
        start_date: '2026-04-01',
        end_date: '2026-04-30',
        channels: ['微信', '电话', '美团', '抖音'],
        clinic_id: '1',
        consultant_id: null,
        total_inquiries: 320,
        converted_inquiries: 185,
        conversion_rate: 0.578,
        avg_response_time_minutes: 12,
        avg_first_response_minutes: 3.5,
        inquiries_by_topic: { '种植牙': 80, '正畸': 65, '洗牙': 55, '补牙': 40, '其他': 80 },
        channel_conversion_rates: { '微信': 0.62, '电话': 0.58, '美团': 0.45, '抖音': 0.38 },
        consultant_stats: [
          { consultant_id: 'C01', name: '小刘', inquiries: 120, conversions: 75, conversion_rate: 0.625 }
        ]
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      start_date: '2026-04-01',
      end_date: '2026-04-30',
      channels: ['微信', '电话', '美团', '抖音'],
      clinic_id: '1',
      consultant_id: null,
      total_inquiries: 320,
      converted_inquiries: 185,
      conversion_rate: 0.578,
      avg_response_time_minutes: 12,
      avg_first_response_minutes: 3.5,
      inquiries_by_topic: { '种植牙': 80, '正畸': 65, '洗牙': 55, '补牙': 40, '其他': 80 },
      channel_conversion_rates: { '微信': 0.62, '电话': 0.58, '美团': 0.45, '抖音': 0.38 },
      consultant_stats: [
        { consultant_id: 'C01', name: '小刘', inquiries: 120, conversions: 75, conversion_rate: 0.625 }
      ]
    },
    exampleInputFields: {
      start_date: '统计开始日期',
      end_date: '统计结束日期',
      channels: '咨询渠道列表',
      clinic_id: '诊所ID',
      consultant_id: '咨询师ID（可选，筛选特定咨询师）',
      total_inquiries: '总咨询量',
      converted_inquiries: '转化咨询量',
      conversion_rate: '整体转化率（0-1小数）',
      avg_response_time_minutes: '平均响应时间（分钟）',
      avg_first_response_minutes: '平均首次响应时间（分钟）',
      inquiries_by_topic: '按话题分组的咨询量',
      channel_conversion_rates: '各渠道转化率',
      consultant_stats: '咨询师绩效统计列表'
    },
    uiConfig: {
      mode: 'json',
      title: '咨询分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'treatment-assist': {
    agentKey: 'treatment-assist',
    name: '治疗辅助',
    description: '辅助医生制定治疗方案，基于患者症状、诊断和病史推荐治疗项目、材料和用药方案',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1001',
        patient_name: '张三',
        age: 35,
        gender: '男',
        diagnosis: '慢性牙周炎（中度）',
        symptoms: '牙龈出血、牙齿松动、口腔异味',
        chief_complaint: '刷牙出血，牙齿感觉松动',
        allergy_history: '青霉素过敏',
        current_medications: ['降压药：氨氯地平'],
        previous_treatments: '2025年洗牙1次',
        oral_hygiene_habits: '每天刷牙1次，不使用牙线',
        smoking_status: '吸烟，每天10支',
        xray_findings: '全景片显示牙槽骨吸收约30%',
        doctor_id: 'D01',
        doctor_name: '李医生'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1001',
      patient_name: '张三',
      age: 35,
      gender: '男',
      diagnosis: '慢性牙周炎（中度）',
      symptoms: '牙龈出血、牙齿松动、口腔异味',
      chief_complaint: '刷牙出血，牙齿感觉松动',
      allergy_history: '青霉素过敏',
      current_medications: ['降压药：氨氯地平'],
      previous_treatments: '2025年洗牙1次',
      oral_hygiene_habits: '每天刷牙1次，不使用牙线',
      smoking_status: '吸烟，每天10支',
      xray_findings: '全景片显示牙槽骨吸收约30%',
      doctor_id: 'D01',
      doctor_name: '李医生'
    },
    exampleInputFields: {
      patient_id: '患者ID',
      patient_name: '患者姓名',
      age: '年龄',
      gender: '性别',
      diagnosis: '诊断结果',
      symptoms: '症状描述',
      chief_complaint: '主诉',
      allergy_history: '过敏史',
      current_medications: '当前用药列表',
      previous_treatments: '既往治疗记录',
      oral_hygiene_habits: '口腔卫生习惯',
      smoking_status: '吸烟情况',
      xray_findings: '影像检查结论',
      doctor_id: '医生ID',
      doctor_name: '医生姓名'
    },
    uiConfig: {
      mode: 'json',
      title: '治疗辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'treatment-record-assist': {
    agentKey: 'treatment-record-assist',
    name: '治疗记录辅助',
    description: '辅助完善治疗记录，根据已执行操作自动生成规范记录、检查必填项完整性和术语标准化',
    exampleRequestBody: {
      input_fields: {
        patient_id: '1001',
        patient_name: '张三',
        treatment_id: 'T20260001',
        treatment_date: '2026-05-14',
        draft_record: '今日完成根管治疗第一步，开髓拔髓',
        required_fields: ['麻醉方式', '使用器械', '根管长度', '冲洗药物', '术后医嘱'],
        procedure_steps: [
          { step: '麻醉', detail: '阿替卡因局部浸润麻醉' },
          { step: '开髓', detail: '高速球钻开髓，揭净髓室顶' },
          { step: '拔髓', detail: '拔髓针拔除牙髓，根管通畅' }
        ],
        materials_used: [
          { material: '阿替卡因注射液', quantity: '1支', batch: 'BAT2026001' },
          { material: '次氯酸钠', quantity: '适量', batch: 'BAT2026050' }
        ],
        doctor_id: 'D01',
        doctor_name: '李医生',
        assistant_id: 'N01',
        assistant_name: '小张',
        anesthesia_type: '局部浸润麻醉',
        treatment_duration: 45,
        patient_cooperation: '良好',
        complications: '无',
        postop_instructions_given: true
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      patient_id: '1001',
      patient_name: '张三',
      treatment_id: 'T20260001',
      treatment_date: '2026-05-14',
      draft_record: '今日完成根管治疗第一步，开髓拔髓',
      required_fields: ['麻醉方式', '使用器械', '根管长度', '冲洗药物', '术后医嘱'],
      procedure_steps: [
        { step: '麻醉', detail: '阿替卡因局部浸润麻醉' },
        { step: '开髓', detail: '高速球钻开髓，揭净髓室顶' },
        { step: '拔髓', detail: '拔髓针拔除牙髓，根管通畅' }
      ],
      materials_used: [
        { material: '阿替卡因注射液', quantity: '1支', batch: 'BAT2026001' },
        { material: '次氯酸钠', quantity: '适量', batch: 'BAT2026050' }
      ],
      doctor_id: 'D01',
      doctor_name: '李医生',
      assistant_id: 'N01',
      assistant_name: '小张',
      anesthesia_type: '局部浸润麻醉',
      treatment_duration: 45,
      patient_cooperation: '良好',
      complications: '无',
      postop_instructions_given: true
    },
    exampleInputFields: {
      patient_id: '患者ID',
      patient_name: '患者姓名',
      treatment_id: '治疗记录ID',
      treatment_date: '治疗日期',
      draft_record: '医生输入的草稿记录',
      required_fields: '必填字段列表',
      procedure_steps: '操作步骤列表（含步骤名和详情）',
      materials_used: '使用的耗材列表（含名称、数量、批号）',
      doctor_id: '操作医生ID',
      doctor_name: '操作医生姓名',
      assistant_id: '助手护士ID',
      assistant_name: '助手护士姓名',
      anesthesia_type: '麻醉方式',
      treatment_duration: '治疗时长（分钟）',
      patient_cooperation: '患者配合度（良好/一般/差）',
      complications: '并发症（无/有，具体说明）',
      postop_instructions_given: '是否已给予术后医嘱（true/false）'
    },
    uiConfig: {
      mode: 'json',
      title: '治疗记录辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'financial-analysis': {
    agentKey: 'financial-analysis',
    name: '财务分析',
    description: '分析诊所财务收支结构、成本构成、利润趋势和异常波动，提供财务健康度评估',
    exampleRequestBody: {
      input_fields: {
        start_date: '2026-04-01',
        end_date: '2026-04-30',
        analysis_type: '收支结构',
        clinic_id: '1',
        clinic_name: '舒澳口腔诊所',
        total_revenue: 285000.00,
        total_expense: 198000.00,
        net_profit: 87000.00,
        revenue_by_category: {
          '诊疗收入': 220000,
          '药品收入': 35000,
          '耗材收入': 30000
        },
        expense_by_category: {
          '人力成本': 120000,
          '耗材采购': 35000,
          '房租水电': 25000,
          '设备折旧': 10000,
          '营销费用': 8000
        },
        department_revenue: [
          { department: '口腔内科', revenue: 120000, expense: 85000 },
          { department: '口腔外科', revenue: 80000, expense: 55000 }
        ],
        compare_with_previous: true,
        previous_total_revenue: 260000,
        previous_total_expense: 190000,
        arrearage_amount: 15000.00,
        refund_amount: 2000.00
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      start_date: '2026-04-01',
      end_date: '2026-04-30',
      analysis_type: '收支结构',
      clinic_id: '1',
      clinic_name: '舒澳口腔诊所',
      total_revenue: 285000.00,
      total_expense: 198000.00,
      net_profit: 87000.00,
      revenue_by_category: {
        '诊疗收入': 220000,
        '药品收入': 35000,
        '耗材收入': 30000
      },
      expense_by_category: {
        '人力成本': 120000,
        '耗材采购': 35000,
        '房租水电': 25000,
        '设备折旧': 10000,
        '营销费用': 8000
      },
      department_revenue: [
        { department: '口腔内科', revenue: 120000, expense: 85000 },
        { department: '口腔外科', revenue: 80000, expense: 55000 }
      ],
      compare_with_previous: true,
      previous_total_revenue: 260000,
      previous_total_expense: 190000,
      arrearage_amount: 15000.00,
      refund_amount: 2000.00
    },
    exampleInputFields: {
      start_date: '统计开始日期',
      end_date: '统计结束日期',
      analysis_type: '分析类型（收支结构/成本分析/利润分析/现金流分析）',
      clinic_id: '诊所ID',
      clinic_name: '诊所名称',
      total_revenue: '总收入（元）',
      total_expense: '总支出（元）',
      net_profit: '净利润（元）',
      revenue_by_category: '按分类的收入明细',
      expense_by_category: '按分类的支出明细',
      department_revenue: '各科室收支数据',
      compare_with_previous: '是否对比上一周期（true/false）',
      previous_total_revenue: '上一周期总收入',
      previous_total_expense: '上一周期总支出',
      arrearage_amount: '欠费金额（元）',
      refund_amount: '退款金额（元）'
    },
    uiConfig: {
      mode: 'json',
      title: '财务分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'monthly-bill-analysis': {
    agentKey: 'monthly-bill-analysis',
    name: '月度账单分析',
    description: '分析月度账单趋势、同比环比变化、异常项目和患者消费结构，识别财务异常',
    exampleRequestBody: {
      input_fields: {
        year_month: '2026-04',
        clinic_id: '1',
        total_bill_amount: 285000.00,
        total_bill_count: 420,
        avg_bill_amount: 678.57,
        compare_with_last_month: true,
        last_month_amount: 260000.00,
        compare_with_last_year: true,
        last_year_same_month_amount: 240000.00,
        bill_categories: ['诊疗收入', '药品收入', '耗材收入', '检查收入'],
        category_amounts: { '诊疗收入': 220000, '药品收入': 35000, '耗材收入': 30000 },
        payment_methods: { '微信支付': 120000, '支付宝': 80000, '现金': 35000, '医保': 50000 },
        top_patients: [
          { patient_id: '1001', patient_name: '张三', amount: 15000 },
          { patient_id: '1002', patient_name: '李四', amount: 12000 }
        ],
        abnormal_bills: [
          { bill_id: 'B20260050', amount: 25000, reason: '高额种植费用，需复核' }
        ],
        discount_amount: 8500.00,
        write_off_amount: 0
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      year_month: '2026-04',
      clinic_id: '1',
      total_bill_amount: 285000.00,
      total_bill_count: 420,
      avg_bill_amount: 678.57,
      compare_with_last_month: true,
      last_month_amount: 260000.00,
      compare_with_last_year: true,
      last_year_same_month_amount: 240000.00,
      bill_categories: ['诊疗收入', '药品收入', '耗材收入', '检查收入'],
      category_amounts: { '诊疗收入': 220000, '药品收入': 35000, '耗材收入': 30000 },
      payment_methods: { '微信支付': 120000, '支付宝': 80000, '现金': 35000, '医保': 50000 },
      top_patients: [
        { patient_id: '1001', patient_name: '张三', amount: 15000 },
        { patient_id: '1002', patient_name: '李四', amount: 12000 }
      ],
      abnormal_bills: [
        { bill_id: 'B20260050', amount: 25000, reason: '高额种植费用，需复核' }
      ],
      discount_amount: 8500.00,
      write_off_amount: 0
    },
    exampleInputFields: {
      year_month: '分析年月（YYYY-MM）',
      clinic_id: '诊所ID',
      total_bill_amount: '账单总金额（元）',
      total_bill_count: '账单总笔数',
      avg_bill_amount: '客单价（元）',
      compare_with_last_month: '是否与上月对比（true/false）',
      last_month_amount: '上月账单金额',
      compare_with_last_year: '是否与去年同期对比（true/false）',
      last_year_same_month_amount: '去年同期账单金额',
      bill_categories: '账单分类列表',
      category_amounts: '各分类金额明细',
      payment_methods: '支付方式分布',
      top_patients: '消费TOP患者列表',
      abnormal_bills: '异常账单列表（含原因）',
      discount_amount: '优惠金额（元）',
      write_off_amount: '减免金额（元）'
    },
    uiConfig: {
      mode: 'json',
      title: '月度账单分析',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'lab-statistics-analysis': {
    agentKey: 'lab-statistics-analysis',
    name: '加工统计',
    description: '统计加工订单量、类型分布、时效趋势、成本分析和供应商占比',
    exampleRequestBody: {
      input_fields: {
        start_date: '2026-04-01',
        end_date: '2026-04-30',
        clinic_id: '1',
        total_orders: 186,
        total_amount: 62400.00,
        avg_order_amount: 335.48,
        group_by: '订单类型',
        order_type_distribution: {
          '烤瓷冠': { count: 80, amount: 28000 },
          '活动义齿': { count: 45, amount: 18000 },
          '种植体上部': { count: 30, amount: 12000 },
          '临时冠': { count: 31, amount: 4400 }
        },
        factory_distribution: [
          { factory_id: 'F01', factory_name: 'XX加工厂', count: 100, amount: 35000 },
          { factory_id: 'F02', factory_name: 'YY加工厂', count: 86, amount: 27400 }
        ],
        avg_delivery_days: 5.2,
        on_time_rate: 0.88,
        return_count: 3,
        return_rate: 0.016,
        urgent_orders: 12,
        include_cost: true,
        status_breakdown: { '已完成': 170, '加工中': 12, '已退回': 4 }
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      start_date: '2026-04-01',
      end_date: '2026-04-30',
      clinic_id: '1',
      total_orders: 186,
      total_amount: 62400.00,
      avg_order_amount: 335.48,
      group_by: '订单类型',
      order_type_distribution: {
        '烤瓷冠': { count: 80, amount: 28000 },
        '活动义齿': { count: 45, amount: 18000 },
        '种植体上部': { count: 30, amount: 12000 },
        '临时冠': { count: 31, amount: 4400 }
      },
      factory_distribution: [
        { factory_id: 'F01', factory_name: 'XX加工厂', count: 100, amount: 35000 },
        { factory_id: 'F02', factory_name: 'YY加工厂', count: 86, amount: 27400 }
      ],
      avg_delivery_days: 5.2,
      on_time_rate: 0.88,
      return_count: 3,
      return_rate: 0.016,
      urgent_orders: 12,
      include_cost: true,
      status_breakdown: { '已完成': 170, '加工中': 12, '已退回': 4 }
    },
    exampleInputFields: {
      start_date: '统计开始日期',
      end_date: '统计结束日期',
      clinic_id: '诊所ID',
      total_orders: '订单总数',
      total_amount: '订单总金额（元）',
      avg_order_amount: '平均订单金额（元）',
      group_by: '分组维度（订单类型/加工厂/月份）',
      order_type_distribution: '订单类型分布（含数量和金额）',
      factory_distribution: '加工厂分布统计',
      avg_delivery_days: '平均交货天数',
      on_time_rate: '准时交付率',
      return_count: '返工数量',
      return_rate: '返工率',
      urgent_orders: '加急订单数',
      include_cost: '是否包含成本统计（true/false）',
      status_breakdown: '订单状态分布'
    },
    uiConfig: {
      mode: 'json',
      title: '加工统计',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'material-category-assist': {
    agentKey: 'material-category-assist',
    name: '耗材分类辅助',
    description: '辅助耗材分类管理、编码规范化、属性补全和同类耗材归并建议',
    exampleRequestBody: {
      input_fields: {
        material_name: '3M Z350XT 纳米树脂',
        current_category: '充填材料',
        current_code: 'CF-001',
        specification: '4g/支',
        brand: '3M',
        manufacturer: '3M中国有限公司',
        unit: '支',
        material_type: '树脂充填材料',
        application_scope: '前牙美学修复、后牙充填',
        similar_materials: [
          { name: '3M Z250树脂', category: '充填材料', code: 'CF-002' },
          { name: '义获嘉树脂', category: '充填材料', code: 'CF-003' }
        ],
        suggested_category_rules: '按材料类型+品牌+用途三级分类',
        storage_conditions: '避光常温保存',
        shelf_life_months: 24
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      material_name: '3M Z350XT 纳米树脂',
      current_category: '充填材料',
      current_code: 'CF-001',
      specification: '4g/支',
      brand: '3M',
      manufacturer: '3M中国有限公司',
      unit: '支',
      material_type: '树脂充填材料',
      application_scope: '前牙美学修复、后牙充填',
      similar_materials: [
        { name: '3M Z250树脂', category: '充填材料', code: 'CF-002' },
        { name: '义获嘉树脂', category: '充填材料', code: 'CF-003' }
      ],
      suggested_category_rules: '按材料类型+品牌+用途三级分类',
      storage_conditions: '避光常温保存',
      shelf_life_months: 24
    },
    exampleInputFields: {
      material_name: '耗材名称（完整商品名）',
      current_category: '当前分类',
      current_code: '当前编码',
      specification: '规格型号',
      brand: '品牌',
      manufacturer: '生产厂家',
      unit: '计量单位（支/盒/瓶等）',
      material_type: '材料类型',
      application_scope: '适用范围',
      similar_materials: '同类耗材列表（含名称、分类、编码）',
      suggested_category_rules: '期望的分类规则描述',
      storage_conditions: '储存条件',
      shelf_life_months: '保质期（月）'
    },
    uiConfig: {
      mode: 'json',
      title: '耗材分类辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'material-inventory-assist': {
    agentKey: 'material-inventory-assist',
    name: '库存辅助',
    description: '辅助库存管理，智能预警低库存、滞销品、临期品，推荐补货量和采购时机',
    exampleRequestBody: {
      input_fields: {
        material_id: 'M001',
        material_name: '3M Z350XT 纳米树脂',
        current_stock: 5,
        safety_stock: 10,
        max_stock: 50,
        monthly_usage: 18,
        avg_daily_usage: 0.6,
        last_purchase_date: '2026-03-15',
        last_purchase_quantity: 20,
        expiration_date: '2027-03-15',
        days_until_expiry: 305,
        unit_price: 85.00,
        supplier_id: 'S001',
        supplier_name: 'XX医疗器械公司',
        lead_time_days: 3,
        storage_location: 'A区-2层-3号柜',
        recent_usage_trend: '稳定',
        batch_list: [
          { batch_no: 'BAT20260315', quantity: 5, expiry: '2027-03-15' }
        ]
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      material_id: 'M001',
      material_name: '3M Z350XT 纳米树脂',
      current_stock: 5,
      safety_stock: 10,
      max_stock: 50,
      monthly_usage: 18,
      avg_daily_usage: 0.6,
      last_purchase_date: '2026-03-15',
      last_purchase_quantity: 20,
      expiration_date: '2027-03-15',
      days_until_expiry: 305,
      unit_price: 85.00,
      supplier_id: 'S001',
      supplier_name: 'XX医疗器械公司',
      lead_time_days: 3,
      storage_location: 'A区-2层-3号柜',
      recent_usage_trend: '稳定',
      batch_list: [
        { batch_no: 'BAT20260315', quantity: 5, expiry: '2027-03-15' }
      ]
    },
    exampleInputFields: {
      material_id: '耗材ID',
      material_name: '耗材名称',
      current_stock: '当前库存量',
      safety_stock: '安全库存阈值',
      max_stock: '最大库存上限',
      monthly_usage: '月均使用量',
      avg_daily_usage: '日均使用量',
      last_purchase_date: '上次采购日期',
      last_purchase_quantity: '上次采购数量',
      expiration_date: '有效期至',
      days_until_expiry: '距离过期天数',
      unit_price: '单价（元）',
      supplier_id: '供应商ID',
      supplier_name: '供应商名称',
      lead_time_days: '采购提前期（天）',
      storage_location: '存放位置',
      recent_usage_trend: '近期使用趋势（上升/下降/稳定）',
      batch_list: '批次列表（含批号、数量、有效期）'
    },
    uiConfig: {
      mode: 'json',
      title: '库存辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'material-purchase-assist': {
    agentKey: 'material-purchase-assist',
    name: '采购辅助',
    description: '辅助采购决策，智能推荐采购量、评估供应商报价、预测采购成本和优化采购周期',
    exampleRequestBody: {
      input_fields: {
        material_id: 'M001',
        material_name: '3M Z350XT 纳米树脂',
        current_stock: 5,
        safety_stock: 10,
        monthly_usage: 18,
        avg_monthly_usage: 18,
        last_purchase_price: 85.00,
        last_purchase_date: '2026-03-15',
        preferred_suppliers: [
          { supplier_id: 'S001', supplier_name: 'XX医疗器械', quoted_price: 82.00, min_order_quantity: 10 },
          { supplier_id: 'S002', supplier_name: 'YY齿科耗材', quoted_price: 85.00, min_order_quantity: 5 }
        ],
        budget_limit: 5000.00,
        purchase_cycle_days: 30,
        recommended_quantity: 20,
        urgency: '一般',
        price_history: [
          { date: '2025-12-15', price: 88.00 },
          { date: '2026-03-15', price: 85.00 }
        ],
        seasonal_factor: '春季补牙旺季，用量预计上升15%'
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      material_id: 'M001',
      material_name: '3M Z350XT 纳米树脂',
      current_stock: 5,
      safety_stock: 10,
      monthly_usage: 18,
      avg_monthly_usage: 18,
      last_purchase_price: 85.00,
      last_purchase_date: '2026-03-15',
      preferred_suppliers: [
        { supplier_id: 'S001', supplier_name: 'XX医疗器械', quoted_price: 82.00, min_order_quantity: 10 },
        { supplier_id: 'S002', supplier_name: 'YY齿科耗材', quoted_price: 85.00, min_order_quantity: 5 }
      ],
      budget_limit: 5000.00,
      purchase_cycle_days: 30,
      recommended_quantity: 20,
      urgency: '一般',
      price_history: [
        { date: '2025-12-15', price: 88.00 },
        { date: '2026-03-15', price: 85.00 }
      ],
      seasonal_factor: '春季补牙旺季，用量预计上升15%'
    },
    exampleInputFields: {
      material_id: '耗材ID',
      material_name: '耗材名称',
      current_stock: '当前库存量',
      safety_stock: '安全库存阈值',
      monthly_usage: '月均使用量',
      avg_monthly_usage: '历史月均使用量',
      last_purchase_price: '上次采购单价（元）',
      last_purchase_date: '上次采购日期',
      preferred_suppliers: '首选供应商列表（含ID、名称、报价、起订量）',
      budget_limit: '预算上限（元）',
      purchase_cycle_days: '采购周期（天）',
      recommended_quantity: '系统推荐采购量',
      urgency: '紧急程度（紧急/一般）',
      price_history: '历史价格记录',
      seasonal_factor: '季节性因素说明'
    },
    uiConfig: {
      mode: 'json',
      title: '采购辅助',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
  },

  'material-statistics-analysis': {
    agentKey: 'material-statistics-analysis',
    name: '耗材统计',
    description: '统计耗材使用量、消耗趋势、成本分布、科室消耗占比和库存周转率',
    exampleRequestBody: {
      input_fields: {
        start_date: '2026-04-01',
        end_date: '2026-04-30',
        clinic_id: '1',
        material_ids: ['M001', 'M002', 'M003'],
        group_by: '科室',
        department_ids: ['DEPT01', 'DEPT02'],
        total_consumption_amount: 42000.00,
        total_consumption_quantity: 850,
        material_stats: [
          { material_id: 'M001', material_name: '3M树脂', quantity: 120, amount: 10200, avg_price: 85.00 },
          { material_id: 'M002', material_name: '麻醉剂', quantity: 200, amount: 8000, avg_price: 40.00 }
        ],
        department_consumption: [
          { department_id: 'DEPT01', department_name: '口腔内科', amount: 25000, percentage: 0.595 },
          { department_id: 'DEPT02', department_name: '口腔外科', amount: 17000, percentage: 0.405 }
        ],
        include_cost: true,
        top_consumption_materials: ['M001', 'M002', 'M005'],
        slow_moving_materials: ['M010', 'M011'],
        stock_turnover_rate: 4.2,
        avg_stock_value: 35000.00,
        waste_amount: 500.00,
        waste_rate: 0.012
      },
      account_id: '{{account_id}}',
      account_name: '{{account_name}}',
      clinic_id: '{{clinic_id}}',
      timestamp: '{{timestamp}}'
    },
    proxyExample: {
      start_date: '2026-04-01',
      end_date: '2026-04-30',
      clinic_id: '1',
      material_ids: ['M001', 'M002', 'M003'],
      group_by: '科室',
      department_ids: ['DEPT01', 'DEPT02'],
      total_consumption_amount: 42000.00,
      total_consumption_quantity: 850,
      material_stats: [
        { material_id: 'M001', material_name: '3M树脂', quantity: 120, amount: 10200, avg_price: 85.00 },
        { material_id: 'M002', material_name: '麻醉剂', quantity: 200, amount: 8000, avg_price: 40.00 }
      ],
      department_consumption: [
        { department_id: 'DEPT01', department_name: '口腔内科', amount: 25000, percentage: 0.595 },
        { department_id: 'DEPT02', department_name: '口腔外科', amount: 17000, percentage: 0.405 }
      ],
      include_cost: true,
      top_consumption_materials: ['M001', 'M002', 'M005'],
      slow_moving_materials: ['M010', 'M011'],
      stock_turnover_rate: 4.2,
      avg_stock_value: 35000.00,
      waste_amount: 500.00,
      waste_rate: 0.012
    },
    exampleInputFields: {
      start_date: '统计开始日期',
      end_date: '统计结束日期',
      clinic_id: '诊所ID',
      material_ids: '耗材ID列表（为空则统计全部）',
      group_by: '分组维度（科室/医生/耗材分类/月份）',
      department_ids: '科室ID列表（可选）',
      total_consumption_amount: '总消耗金额（元）',
      total_consumption_quantity: '总消耗数量',
      material_stats: '各耗材消耗明细列表',
      department_consumption: '各科室消耗分布',
      include_cost: '是否包含成本分析（true/false）',
      top_consumption_materials: '高消耗耗材TOP列表',
      slow_moving_materials: '滞销耗材列表',
      stock_turnover_rate: '库存周转率',
      avg_stock_value: '平均库存金额（元）',
      waste_amount: '损耗金额（元）',
      waste_rate: '损耗率'
    },
    uiConfig: {
      mode: 'json',
      title: '耗材统计',
      icon: 'el-icon-magic-stick',
      primaryColor: '#409EFF',
      showRetry: true,
      emptyText: '点击AI按钮开始分析',
      chips: []
    }
}
}

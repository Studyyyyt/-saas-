/**
 * 口腔门诊 SaaS 系统 MCP Server
 *
 * 提供 SSE 传输方式，将系统业务 API 封装为 MCP Tools，
 * 供外部 LLM Agent / 工作流调用。
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import express from "express";
import { z } from "zod";
import axios, { AxiosError } from "axios";

// ==================== 常量配置 ====================
const API_BASE_URL = process.env.API_BASE_URL || "http://backend:8080";
const API_AUTH_TOKEN = process.env.API_AUTH_TOKEN || "";
const MCP_API_KEY = process.env.MCP_API_KEY || "";
const PORT = parseInt(process.env.PORT || "3001", 10);
const CHARACTER_LIMIT = 25000;

// ==================== 枚举 ====================
enum ResponseFormat {
  MARKDOWN = "markdown",
  JSON = "json",
}

// ==================== 共享工具函数 ====================

/**
 * 发起带认证的 API 请求
 */
async function makeApiRequest<T>(
  endpoint: string,
  method: "GET" | "POST" | "PUT" | "DELETE" = "GET",
  data?: any,
  params?: any
): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };
  if (API_AUTH_TOKEN) {
    headers["Authorization"] = `Bearer ${API_AUTH_TOKEN}`;
  }

  const response = await axios({
    method,
    url: `${API_BASE_URL}/${endpoint}`,
    data,
    params,
    timeout: 30000,
    headers,
  });
  return response.data;
}

/**
 * 统一错误处理
 */
function handleApiError(error: unknown): string {
  if (error instanceof AxiosError) {
    if (error.response) {
      switch (error.response.status) {
        case 404:
          return "错误：资源不存在，请检查参数是否正确。";
        case 403:
          return "错误：权限不足，无法访问该资源。";
        case 401:
          return "错误：认证失败，请检查 API Token 是否配置正确。";
        case 429:
          return "错误：请求过于频繁，请稍后再试。";
        default:
          return `错误：API 请求失败，状态码 ${error.response.status}`;
      }
    } else if (error.code === "ECONNABORTED") {
      return "错误：请求超时，请检查网络或后端服务是否可用。";
    } else if (error.code === "ECONNREFUSED") {
      return "错误：无法连接到后端服务，请确认服务地址正确。";
    }
  }
  return `错误：发生意外错误：${error instanceof Error ? error.message : String(error)}`;
}

/**
 * 格式化分页结果为 Markdown
 */
function formatPageResultMarkdown(
  title: string,
  data: any,
  itemFormatter: (item: any) => string
): string {
  const list = data?.list || data?.items || [];
  const total = data?.total ?? (list?.length ?? 0);
  const lines = [`# ${title}`, ``, `共 ${total} 条记录`, ``];
  for (const item of list) {
    lines.push(itemFormatter(item));
    lines.push("");
  }
  return lines.join("\n");
}

/**
 * 截断超长响应
 */
function truncateIfNeeded(text: string): { text: string; truncated: boolean } {
  if (text.length <= CHARACTER_LIMIT) {
    return { text, truncated: false };
  }
  const truncated = text.substring(0, CHARACTER_LIMIT);
  return {
    text: truncated + "\n\n[响应过长，已截断。请使用更精确的参数缩小查询范围。]",
    truncated: true,
  };
}

// ==================== Zod Schema 定义 ====================

const PaginationSchema = z.object({
  page: z.number().int().min(1).default(1).describe("页码，从1开始"),
  size: z.number().int().min(1).max(100).default(20).describe("每页条数"),
});

const ResponseFormatSchema = z
  .nativeEnum(ResponseFormat)
  .default(ResponseFormat.MARKDOWN)
  .describe("输出格式：'markdown' 为人可读，'json' 为机器可读");

const QueryPatientsSchema = z
  .object({
    keyword: z
      .string()
      .optional()
      .describe("姓名/手机号关键词，支持模糊搜索"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryAppointmentsSchema = z
  .object({
    status: z
      .string()
      .optional()
      .describe("预约状态过滤，如：待确认、已确认、已完成、已取消"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryMedicalRecordsSchema = z
  .object({
    patient_id: z.number().int().positive().describe("患者ID"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryFinancesSchema = z
  .object({
    start_date: z
      .string()
      .optional()
      .describe("开始日期，格式 yyyy-MM-dd"),
    end_date: z.string().optional().describe("结束日期，格式 yyyy-MM-dd"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryTreatmentsSchema = z
  .object({
    patient_name: z.string().optional().describe("患者姓名关键词"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryLabOrdersSchema = z
  .object({
    keyword: z
      .string()
      .optional()
      .describe("关键词：患者姓名、加工内容、加工单号等"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryMaterialsSchema = z
  .object({
    keyword: z.string().optional().describe("耗材名称/编号关键词"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const QueryConsultationsSchema = z
  .object({
    keyword: z
      .string()
      .optional()
      .describe("姓名/手机号关键词，支持模糊搜索"),
    ...PaginationSchema.shape,
    response_format: ResponseFormatSchema,
  })
  .strict();

const GetPatient360Schema = z
  .object({
    patient_id: z.number().int().positive().describe("患者ID"),
    response_format: ResponseFormatSchema,
  })
  .strict();

// ==================== MCP Server 初始化 ====================

const server = new McpServer({
  name: "clinic-mcp-server",
  version: "1.0.0",
});

// ==================== Tool 注册 ====================

// ---------- query_patients ----------
server.registerTool(
  "query_patients",
  {
    title: "查询患者列表",
    description: `查询口腔门诊系统中的患者列表，支持按姓名或手机号模糊搜索。

Args:
  - keyword (string, optional): 姓名/手机号关键词
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  患者列表分页数据，包含姓名、性别、年龄、手机号、客户来源等字段。`,
    inputSchema: QueryPatientsSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "patients/search",
        "GET",
        undefined,
        {
          keyword: params.keyword,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText, truncated } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `患者搜索结果: ${params.keyword || "全部"}`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.name || "未知姓名"} (ID: ${item.id})`,
            `- **性别**: ${item.gender || "-"}`,
            `- **年龄**: ${item.age ?? "-"}`,
            `- **手机号**: ${item.phone || "-"}`,
            `- **来源**: ${item.customer_source || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_appointments ----------
server.registerTool(
  "query_appointments",
  {
    title: "查询预约列表",
    description: `查询门诊预约列表，支持按状态过滤。

Args:
  - status (string, optional): 预约状态，如：待确认、已确认、已完成、已取消
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  预约列表分页数据，包含患者姓名、预约时间、医生、状态等。`,
    inputSchema: QueryAppointmentsSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "appointments/selectAll",
        "GET",
        undefined,
        {
          status: params.status,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `预约列表${params.status ? ` [状态: ${params.status}]` : ""}`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.patient_name || "未知患者"} (ID: ${item.patient_id})`,
            `- **预约时间**: ${item.appointment_time || "-"}`,
            `- **医生**: ${item.doctor_name || "-"}`,
            `- **状态**: ${item.status || "-"}`,
            `- **预约目的**: ${item.appointment_purpose || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_medical_records ----------
server.registerTool(
  "query_medical_records",
  {
    title: "查询病历列表",
    description: `根据患者ID查询其病历记录列表。

Args:
  - patient_id (number, required): 患者ID
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  病历列表分页数据，包含主诉、现病史、诊断、治疗计划等。`,
    inputSchema: QueryMedicalRecordsSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "medical-records/selectByPatientId",
        "GET",
        undefined,
        {
          patientId: params.patient_id,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `患者病历列表 (患者ID: ${params.patient_id})`,
        data?.data,
        (item: any) => {
          const parts = [
            `## 病历 #${item.id} - ${item.visit_date || "-"}`,
            `- **主诉**: ${item.chief_complaint || "-"}`,
            `- **诊断**: ${item.diagnosis || "-"}`,
            `- **治疗计划**: ${item.treatment_plan || "-"}`,
            `- **医生**: ${item.doctor_name || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_finances ----------
server.registerTool(
  "query_finances",
  {
    title: "查询财务记录",
    description: `查询门诊财务/收费记录，支持按日期范围过滤。

Args:
  - start_date (string, optional): 开始日期，格式 yyyy-MM-dd
  - end_date (string, optional): 结束日期，格式 yyyy-MM-dd
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  财务记录分页数据，包含收费项目、金额、患者、收费时间等。`,
    inputSchema: QueryFinancesSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>("finances/all", "GET", undefined, {
        startDate: params.start_date,
        endDate: params.end_date,
        page: params.page,
        size: params.size,
      });

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `财务记录${
          params.start_date || params.end_date
            ? ` (${params.start_date || "-"} ~ ${params.end_date || "-"})`
            : ""
        }`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.patient_name || "未知患者"} - ${item.item_name || "收费项目"}`,
            `- **金额**: ¥${item.amount ?? "-"}`,
            `- **收费时间**: ${item.charge_date || "-"}`,
            `- **收费类型**: ${item.type || "-"}`,
            `- **医生**: ${item.doctor_name || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_treatments ----------
server.registerTool(
  "query_treatments",
  {
    title: "查询治疗记录",
    description: `查询治疗处置记录列表，支持按患者姓名搜索。

Args:
  - patient_name (string, optional): 患者姓名关键词
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  治疗记录分页数据，包含治疗内容、牙位、医生、费用等。`,
    inputSchema: QueryTreatmentsSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "treatments/selectAll",
        "GET",
        undefined,
        {
          patientName: params.patient_name,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `治疗记录${params.patient_name ? ` [患者: ${params.patient_name}]` : ""}`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.patient_name || "未知患者"} - ${item.treatment_date || "-"}`,
            `- **治疗内容**: ${item.treatment_content || "-"}`,
            `- **牙位**: ${item.tooth_positions || "-"}`,
            `- **医生**: ${item.doctor_name || "-"}`,
            `- **费用**: ¥${item.fee ?? "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_lab_orders ----------
server.registerTool(
  "query_lab_orders",
  {
    title: "查询加工单",
    description: `查询技工加工单列表，支持按关键词搜索。

Args:
  - keyword (string, optional): 关键词：患者姓名、加工内容、加工单号等
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  加工单分页数据，包含加工厂、加工内容、状态、交付日期等。`,
    inputSchema: QueryLabOrdersSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "lab-orders/search",
        "GET",
        undefined,
        {
          keyword: params.keyword,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `加工单列表${params.keyword ? ` [关键词: ${params.keyword}]` : ""}`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.patient_name || "未知患者"} - ${item.lab_content || "加工内容"}`,
            `- **加工厂**: ${item.factory_name || "-"}`,
            `- **状态**: ${item.status || "-"}`,
            `- **交付日期**: ${item.delivery_date || "-"}`,
            `- **医生**: ${item.doctor_name || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_materials ----------
server.registerTool(
  "query_materials",
  {
    title: "查询耗材库存",
    description: `查询耗材/物料库存列表，支持按名称或编号搜索。

Args:
  - keyword (string, optional): 耗材名称/编号关键词
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  耗材分页数据，包含名称、规格、库存数量、预警阈值等。`,
    inputSchema: QueryMaterialsSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "materials/search",
        "GET",
        undefined,
        {
          keyword: params.keyword,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `耗材库存${params.keyword ? ` [关键词: ${params.keyword}]` : ""}`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.name || "未知耗材"} (编号: ${item.code || "-"})`,
            `- **规格**: ${item.specification || "-"}`,
            `- **库存数量**: ${item.stock_quantity ?? "-"}`,
            `- **预警阈值**: ${item.warning_threshold ?? "-"}`,
            `- **状态**: ${item.status || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- query_consultations ----------
server.registerTool(
  "query_consultations",
  {
    title: "查询咨询/回访记录",
    description: `查询患者咨询或回访记录列表，支持按关键词搜索。

Args:
  - keyword (string, optional): 姓名/手机号关键词
  - page (number, optional): 页码，默认 1
  - size (number, optional): 每页条数，默认 20
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  咨询记录分页数据，包含咨询时间、咨询项目、意向等级、处理结果等。`,
    inputSchema: QueryConsultationsSchema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        "consultations/search",
        "GET",
        undefined,
        {
          keyword: params.keyword,
          page: params.page,
          size: params.size,
        }
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const text = formatPageResultMarkdown(
        `咨询记录${params.keyword ? ` [关键词: ${params.keyword}]` : ""}`,
        data?.data,
        (item: any) => {
          const parts = [
            `## ${item.patient_name || item.name || "未知"} - ${
              item.consultation_time || "-"
            }`,
            `- **咨询项目**: ${item.chief_project || "-"}`,
            `- **意向等级**: ${item.intent_level || "-"}`,
            `- **处理结果**: ${item.handling_result || "-"}`,
            `- **录入人**: ${item.created_by_name || "-"}`,
          ];
          return parts.join("\n");
        }
      );
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ---------- get_patient_360 ----------
server.registerTool(
  "get_patient_360",
  {
    title: "获取患者360视图",
    description: `获取指定患者的360度全景视图，整合患者基本信息、病历、治疗、预约、随访、影像、费用统计等。

Args:
  - patient_id (number, required): 患者ID
  - response_format ('markdown' | 'json'): 输出格式，默认 markdown

Returns:
  患者360全景数据，包含：
  - patient: 患者基本信息
  - visitCount: 就诊次数
  - lastVisit: 上次就诊时间
  - nextFollowup: 下次随访时间
  - totalFee: 累计消费
  - hasArrears / arrearsAmount: 是否欠费及欠费金额
  - records: 最近病历
  - treatments: 治疗记录
  - appointments: 预约记录
  - images: 影像资料
  - riskTags: 风险标签
  - wechatBound: 微信绑定状态`,
    inputSchema: GetPatient360Schema,
    annotations: {
      readOnlyHint: true,
      destructiveHint: false,
      idempotentHint: true,
      openWorldHint: true,
    },
  },
  async (params) => {
    try {
      const data = await makeApiRequest<any>(
        `patient360/overview/${params.patient_id}`,
        "GET"
      );

      if (params.response_format === ResponseFormat.JSON) {
        const text = JSON.stringify(data, null, 2);
        const { text: finalText } = truncateIfNeeded(text);
        return {
          content: [{ type: "text", text: finalText }],
          structuredContent: data,
        };
      }

      const overview = data?.data || {};
      const patient = overview.patient || {};
      const lines = [
        `# 患者360视图: ${patient.name || "未知患者"}`,
        "",
        `## 基本信息`,
        `- **姓名**: ${patient.name || "-"}`,
        `- **性别**: ${patient.gender || "-"}`,
        `- **年龄**: ${patient.age ?? "-"}`,
        `- **手机号**: ${patient.phone || "-"}`,
        `- **客户来源**: ${patient.customer_source || "-"}`,
        `- **微信绑定**: ${overview.wechatBindStatusLabel || "-"}`,
        "",
        `## 就诊统计`,
        `- **就诊次数**: ${overview.visitCount ?? "-"}`,
        `- **累计消费**: ¥${overview.totalFee ?? "-"}`,
        `- **欠费金额**: ¥${overview.arrearsAmount ?? "-"}`,
        `- **上次就诊**: ${overview.lastVisit || "-"}`,
        `- **下次随访**: ${overview.nextFollowup || "-"}`,
        "",
        `## 最近病历 (${(overview.recentRecords || []).length}条)`,
      ];

      for (const record of overview.recentRecords || []) {
        lines.push(`- ${record.visit_date || "-"}: ${record.chief_complaint || "-"}`);
      }

      lines.push("", `## 治疗记录 (${(overview.treatments || []).length}条)`);
      for (const t of (overview.treatments || []).slice(0, 10)) {
        lines.push(
          `- ${t.treatment_date || "-"}: ${t.treatment_content || "-"} (¥${t.fee ?? "-"})`
        );
      }

      lines.push("", `## 预约记录 (${(overview.appointments || []).length}条)`);
      for (const a of (overview.appointments || []).slice(0, 10)) {
        lines.push(
          `- ${a.appointment_time || "-"}: ${a.appointment_purpose || "-"} [${a.status || "-"}]`
        );
      }

      const text = lines.join("\n");
      const { text: finalText } = truncateIfNeeded(text);
      return {
        content: [{ type: "text", text: finalText }],
        structuredContent: data,
      };
    } catch (error) {
      return {
        content: [{ type: "text", text: handleApiError(error) }],
      };
    }
  }
);

// ==================== 鉴权中间件 ====================

/**
 * 验证 MCP API Key
 * 从请求头 Authorization: Bearer <token> 中提取并校验
 */
function authenticateMcpApiKey(
  req: express.Request,
  res: express.Response,
  next: express.NextFunction
): void {
  // 如果未配置 MCP_API_KEY，则跳过鉴权（开发环境兼容）
  if (!MCP_API_KEY) {
    next();
    return;
  }

  const authHeader = req.headers.authorization || "";
  const parts = authHeader.split(" ");

  if (parts.length !== 2 || parts[0].toLowerCase() !== "bearer") {
    res.status(401).json({
      status: "error",
      message: "Unauthorized: 缺少 Authorization: Bearer <token> 请求头",
    });
    return;
  }

  const token = parts[1];
  if (token !== MCP_API_KEY) {
    res.status(401).json({
      status: "error",
      message: "Unauthorized: API Key 无效",
    });
    return;
  }

  next();
}

// ==================== HTTP Server 启动 ====================

async function runHttpServer() {
  const app = express();
  app.use(express.json());

  // 健康检查端点（无需鉴权）
  app.get("/health", (_req, res) => {
    res.json({ status: "ok", service: "clinic-mcp-server", version: "1.0.0" });
  });

  // MCP Streamable HTTP 端点（需要 API Key 鉴权）
  app.post("/mcp", authenticateMcpApiKey, async (req, res) => {
    const transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: undefined,
      enableJsonResponse: true,
    });
    res.on("close", () => transport.close());
    await server.connect(transport);
    await transport.handleRequest(req, res, req.body);
  });

  app.listen(PORT, () => {
    console.error(`Clinic MCP Server running on http://localhost:${PORT}`);
    console.error(`MCP endpoint: http://localhost:${PORT}/mcp`);
    console.error(`Health check: http://localhost:${PORT}/health`);
    console.error(`API base URL: ${API_BASE_URL}`);
    console.error(
      `API Key 鉴权: ${MCP_API_KEY ? "已启用" : "未启用（未配置 MCP_API_KEY）"}`
    );
  });
}

runHttpServer().catch((error) => {
  console.error("Server error:", error);
  process.exit(1);
});

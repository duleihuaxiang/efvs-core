import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 客户服务问题信息 */
export interface CustomerServiceIssues {
  id: number // 客服服务问题表id
  processInstanceId: string // 流程实例id
  customerName: string // 客户名称
  department: string // 所属部门
  contact: string // 联系人
  contactNumber: string // 联系电话
  contactEmail: string // 联系人邮箱
  recordDate: string | Dayjs // 记录日期
  questionNumber: string // 问题编号
  customerRequestsTime: string | Dayjs // 客户要求时间
  priority: number // 优先级
  customerProblemDescription: string // 客户问题描述
  customerExpectations: string // 客户期望
  customerAttachments: string // 客户附件
  applicableToCustomerIssues: string // 客户问题适用性
  customerIssuesIndustryStandards: string // 客户问题行业标准
  customerProblemImpact: string // 客户问题影响
  customerProblemTypes: number // 客户问题类型
  isReply: number // 是否可以直接答复(0是1否)
  solutionManager: number // 解决方案负责人
  preparationTime: string | Dayjs // 编制时间
  preparedBy: string // 编制人
  solutionContent: string // 解决方案内容
  solutionAttachment: string // 解决方案的附件
  responseTime: string | Dayjs // 回复时间
  respondent: number // 回复人
  replyContent: string // 回复内容
  replyAttachment: string // 回复附件
  remark: string // 备注
}

/** 客户响应统计 */
export interface CustomerResponseStatistics {
  months: number[]
  respondedCounts: number[]
  unrespondedCounts: number[]
  monthStatistics: Array<{
    month: number
    respondedCount: number
    unrespondedCount: number
    totalCount: number
  }>
}

// 客户服务问题 API
export const CustomerServiceIssuesApi = {
  // 查询客户服务问题分页
  getCustomerServiceIssuesPage: async (params: any) => {
    return await request.get({ url: `/customerResponse/customer-service-issues/page`, params })
  },

  // 查询客户服务问题详情
  getCustomerServiceIssues: async (id: number) => {
    return await request.get({ url: `/customerResponse/customer-service-issues/get?id=` + id })
  },

  // 新增客户服务问题
  createCustomerServiceIssues: async (data: CustomerServiceIssues) => {
    return await request.post({ url: `/customerResponse/customer-service-issues/create`, data })
  },

  // 修改客户服务问题
  updateCustomerServiceIssues: async (data: CustomerServiceIssues) => {
    return await request.put({ url: `/customerResponse/customer-service-issues/update`, data })
  },

  // 删除客户服务问题
  deleteCustomerServiceIssues: async (id: number) => {
    return await request.delete({
      url: `/customerResponse/customer-service-issues/delete?id=` + id
    })
  },

  /** 批量删除客户服务问题 */
  deleteCustomerServiceIssuesList: async (ids: number[]) => {
    return await request.delete({
      url: `/customerResponse/customer-service-issues/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出客户服务问题 Excel
  exportCustomerServiceIssues: async (params) => {
    return await request.download({
      url: `/customerResponse/customer-service-issues/export-excel`,
      params
    })
  },

  // 获取客户响应月度统计
  getMonthlyStatistics: async () => {
    return await request.get<CustomerResponseStatistics>({
      url: '/customerResponse/customer-service-issues/monthly-statistics'
    })
  },

  // 各地客户分布
  getProvinceCustomerCount: async () => {
    return await request.get<any>({ url: '/data/customer-info/provinceCustomerCount' })
  }
}

import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 可靠性数据收集信息 */
export interface Data {
  reliabilityDataFormNo: string // 可靠性数据收集表单编号
  customerName: string // 客户名称
  partName: string // 部件名称
  partNumber: string // 代号（件号）
  sruName: string // SRU名称
  sruNumber: string // SRU代号
  serialNumber: string // 序列号
  productPartNumber: string // 所属部件代号（所属部件件号）
  model: string // 机型
  series: string // 系列
  machineNumber: string // 机号
  reportStartDate: string | Dayjs // 报告开始日期
  reportEndDate: string | Dayjs // 报告结束日期
  removalDate: string | Dayjs // 拆卸日期
  failureReportNumber: string // 故障报告编码
  isProductIssue?: boolean // 是否是产品问题：0=否，1=是
  removalType: string // 拆卸类型：S-计划拆换；U-非计划拆换
  removalCause: string // 拆换原因
  failureDescription: string // 故障描述
  removedPartNumber: string // 拆卸部件件号
  removedPartSerialNumber: string // 拆卸部件序列号
  remark: string // 备注
}

// 可靠性数据收集 API
export const DataApi = {
  // 查询可靠性数据收集分页
  getDataPage: async (params: any) => {
    return await request.get({ url: `/reliability/data/pageByLevelAndIssue`, params })
  },

  // 查询可靠性数据收集详情
  getData: async (id: number) => {
    return await request.get({ url: `/reliability/data/get?id=` + id })
  },

  // 新增可靠性数据收集
  createData: async (data: Data) => {
    return await request.post({ url: `/reliability/data/create`, data })
  },

  // 修改可靠性数据收集
  updateData: async (data: Data) => {
    return await request.put({ url: `/reliability/data/update`, data })
  },

  // 删除可靠性数据收集
  deleteData: async (id: number) => {
    return await request.delete({ url: `/reliability/data/delete?id=` + id })
  },

  /** 批量删除可靠性数据收集 */
  deleteDataList: async (ids: number[]) => {
    return await request.delete({ url: `/reliability/data/delete-list?ids=${ids.join(',')}` })
  },

  // 导出可靠性数据收集 Excel
  exportData: async (params) => {
    return await request.download({ url: `/reliability/data/export-excel`, params })
  },

  // 导出MTBF Excel
  exportDataMTBF: async (params) => {
    return await request.download({
      url: `/reliability/data/export-metrics-excel?type=mtbf`,
      params
    })
  },

  // 导出MTBUR Excel
  exportDataMTBUR: async (params) => {
    return await request.download({
      url: `/reliability/data/export-metrics-excel?type=mtbur`,
      params
    })
  },

  // 首页-可靠性趋势
  getDataStatCurrentYear: async () => {
    return await request.get({ url: `/reliability/data/statCurrentYear` })
  },

  // 导出MTBF或MTBUR
  exportDataMtbfAndMtbur: async (params) => {
    return await request.download({
      url: `/reliability/data/exportMtbfAndMtbur`,
      params
    })
  },

  // 导出MTBF和MTBUR
  exportDataMtbfAndMtburCombined: async (params) => {
    return await request.download({
      url: `/reliability/data/exportMtbfAndMtburCombined`,
      params
    })
  },

  // 获得可靠性数据
  getReliabilityDataById: async (id: number) => {
    return await request.get({ url: '/reliability/data/getReliabilityDataById?id=' + id })
  },

  // 可靠性数据查明细
  getReliabilityDataDetailPage: async (params: any) => {
    return await request.post({
      url: `/reliability/data/getReliabilityDataDetailPage`,
      data: params
    })
  },

  // 明细界面-导出
  exportReliabilityDataDetail: async (params: any) => {
    return await request.postDownload({
      url: `/reliability/data/exportReliabilityDataDetail`,
      params
    })
  },

  // 查询MTBF或MTBUR数据：
  queryMtbfAndMtbur: async (params: any) => {
    return await request.get({
      url: `/reliability/data/queryMtbfAndMtbur`,
      params
    })
  }
}

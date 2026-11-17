import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** qar数据管理信息 */
export interface Qar {
  id: number // id
  airline?: string // 航司
  model?: string // 机型
  efvsConfiguration?: string // efvs构型
  fileName: string // 文件名称
  fileSize: string // 文件大小(kb)
  fileSource?: string // 文件来源
  remark: string // 备注
}

// qar数据管理 API
export const QarApi = {
  // 查询qar数据管理分页
  getQarPage: async (params: any) => {
    return await request.get({ url: `/efvs/qar/page`, params })
  },

  // 查询qar数据管理详情
  getQar: async (id: number) => {
    return await request.get({ url: `/efvs/qar/get?id=` + id })
  },

  // 新增qar数据管理
  createQar: async (data: Qar) => {
    return await request.post({ url: `/efvs/qar/create`, data })
  },

  // 修改qar数据管理
  updateQar: async (data: Qar) => {
    return await request.put({ url: `/efvs/qar/update`, data })
  },

  // 删除qar数据管理
  deleteQar: async (id: number) => {
    return await request.delete({ url: `/efvs/qar/delete?id=` + id })
  },

  /** 批量删除qar数据管理 */
  deleteQarList: async (ids: number[]) => {
    return await request.delete({ url: `/efvs/qar/delete-list?ids=${ids.join(',')}` })
  },

  // 导出qar数据管理 Excel
  exportQar: async (params) => {
    return await request.download({ url: `/efvs/qar/export-excel`, params })
  },

  // 导出模板
  downloadTemplate: async () => {
    return await request.download({ url: `/qar/data/download-template` })
  },

  /** 按 qarId（架次）分页查询 QAR 数据，用于数据分析页绘制 LOC/GS 图表 */
  getQarDataPage: async (params: { qarId: number; pageNo?: number; pageSize?: number }) => {
    return await request.get({ url: `/qar/data/page`, params })
  },

  /**
   * 性能分析：多架次 taxi-phase HTTP 分页（与 /taxi-phase/play 单批条数一致，默认 10）。
   * 请求/响应结构与后端 QarTaxiComparePageReqVO / QarTaxiComparePageRespVO 一致。
   */
  taxiPhaseComparePage: async (data: { pageSize?: number; sorties: Record<string, any>[] }) => {
    return await request.post({ url: `/efvs/qar/taxi-phase/compare-page`, data })
  },

  /** 性能分析：多架次 taxi-phase 一次性聚合返回 */
  taxiPhaseComparePlay: async (data: { ids: number[] }) => {
    return await request.post({ url: `/efvs/qar/taxi-phase/compare-play`, data })
  }
}

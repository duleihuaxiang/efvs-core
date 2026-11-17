import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 可靠性指标信息 */
export interface ReliabilityDataMetrics {
          id: number; // id
          componentName: string; // 部件名称
          partNumber: string; // 件号
          componentCategory: string; // 部件类别
          mtbf: number; // MTBUR指标
          mtbur: number; // MTBF指标
          count: number; // 数量
          remark?: string; // 备注
  }

// 可靠性指标 API
export const ReliabilityDataMetricsApi = {
  // 查询可靠性指标分页
  getReliabilityDataMetricsPage: async (params: any) => {
    return await request.get({ url: `/ReliabilityDataMetrics/reliability-data-metrics/page`, params })
  },

  // 查询可靠性指标详情
  getReliabilityDataMetrics: async (id: number) => {
    return await request.get({ url: `/ReliabilityDataMetrics/reliability-data-metrics/get?id=` + id })
  },

  // 新增可靠性指标
  createReliabilityDataMetrics: async (data: ReliabilityDataMetrics) => {
    return await request.post({ url: `/ReliabilityDataMetrics/reliability-data-metrics/create`, data })
  },

  // 修改可靠性指标
  updateReliabilityDataMetrics: async (data: ReliabilityDataMetrics) => {
    return await request.put({ url: `/ReliabilityDataMetrics/reliability-data-metrics/update`, data })
  },

  // 删除可靠性指标
  deleteReliabilityDataMetrics: async (id: number) => {
    return await request.delete({ url: `/ReliabilityDataMetrics/reliability-data-metrics/delete?id=` + id })
  },

  /** 批量删除可靠性指标 */
  deleteReliabilityDataMetricsList: async (ids: number[]) => {
    return await request.delete({ url: `/ReliabilityDataMetrics/reliability-data-metrics/delete-list?ids=${ids.join(',')}` })
  },

  // 导出可靠性指标 Excel
  exportReliabilityDataMetrics: async (params) => {
    return await request.download({ url: `/ReliabilityDataMetrics/reliability-data-metrics/export-excel`, params })
  }
}
import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 飞行性能数据管理信息 */
export interface FlyPerfDataManagement {
          id: number; // ID
          serialNumber?: number; // 编号
          aircraftModel?: string; // 机型
          meanTimeBetweenUnplannedDisassemblies?: number; // 平均非计划拆卸间隔
          meanTimeBetweenFailures?: number; // 平均失效间隔
          timeToFirstFailure?: number; // 均首次故障前故障时间
          faultMode?: string; // 故障模式
          causeOfMalfunction?: string; // 故障原因
          faultImpact?: string; // 故障影响
          airline?: string; // 航司
          remark: string; // 备注
  }

// 飞行性能数据管理 API
export const FlyPerfDataManagementApi = {
  // 查询飞行性能数据管理分页
  getFlyPerfDataManagementPage: async (params: any) => {
    return await request.get({ url: `/data/fly-perf-data-management/page`, params })
  },

  // 查询飞行性能数据管理详情
  getFlyPerfDataManagement: async (id: number) => {
    return await request.get({ url: `/data/fly-perf-data-management/get?id=` + id })
  },

  // 新增飞行性能数据管理
  createFlyPerfDataManagement: async (data: FlyPerfDataManagement) => {
    return await request.post({ url: `/data/fly-perf-data-management/create`, data })
  },

  // 修改飞行性能数据管理
  updateFlyPerfDataManagement: async (data: FlyPerfDataManagement) => {
    return await request.put({ url: `/data/fly-perf-data-management/update`, data })
  },

  // 删除飞行性能数据管理
  deleteFlyPerfDataManagement: async (id: number) => {
    return await request.delete({ url: `/data/fly-perf-data-management/delete?id=` + id })
  },

  /** 批量删除飞行性能数据管理 */
  deleteFlyPerfDataManagementList: async (ids: number[]) => {
    return await request.delete({ url: `/data/fly-perf-data-management/delete-list?ids=${ids.join(',')}` })
  },

  // 导出飞行性能数据管理 Excel
  exportFlyPerfDataManagement: async (params) => {
    return await request.download({ url: `/data/fly-perf-data-management/export-excel`, params })
  }
}
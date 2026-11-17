import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 故障信息信息 */
export interface FaultInformation {
          id: number; // 故障信息
          faultDate?: string | Dayjs; // 故障日期
          faultPhenomenon: string; // 故障现象
          faultStatus?: boolean; // 故障状态0已解决1排查中
          causeOfFailure: string; // 故障原因
          resolver: string; // 解决人
          remark: string; //  备注
  }

// 故障信息 API
export const FaultInformationApi = {
  // 查询故障信息分页
  getFaultInformationPage: async (params: any) => {
    return await request.get({ url: `/data/fault-information/page`, params })
  },

  // 查询故障信息详情
  getFaultInformation: async (id: number) => {
    return await request.get({ url: `/data/fault-information/get?id=` + id })
  },

  // 新增故障信息
  createFaultInformation: async (data: FaultInformation) => {
    return await request.post({ url: `/data/fault-information/create`, data })
  },

  // 修改故障信息
  updateFaultInformation: async (data: FaultInformation) => {
    return await request.put({ url: `/data/fault-information/update`, data })
  },

  // 删除故障信息
  deleteFaultInformation: async (id: number) => {
    return await request.delete({ url: `/data/fault-information/delete?id=` + id })
  },

  /** 批量删除故障信息 */
  deleteFaultInformationList: async (ids: number[]) => {
    return await request.delete({ url: `/data/fault-information/delete-list?ids=${ids.join(',')}` })
  },

  // 导出故障信息 Excel
  exportFaultInformation: async (params) => {
    return await request.download({ url: `/data/fault-information/export-excel`, params })
  }
}

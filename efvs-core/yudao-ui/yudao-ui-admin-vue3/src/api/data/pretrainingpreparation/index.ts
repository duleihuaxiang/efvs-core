import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训前准备信息 */
export interface PreTrainingPreparation {
          id: number; // ID
          serialNumber?: number; // 编号
          preTrainingMatters?: string; // 培训前事项
          isCompleted?: number; // 是否完成 (0 否 1 是)
          remark: string; // 备注
  }

// 培训前准备 API
export const PreTrainingPreparationApi = {
  // 查询培训前准备分页
  getPreTrainingPreparationPage: async (params: any) => {
    return await request.get({ url: `/data/pre-training-preparation/page`, params })
  },

  // 查询培训前准备详情
  getPreTrainingPreparation: async (id: number) => {
    return await request.get({ url: `/data/pre-training-preparation/get?id=` + id })
  },

  // 新增培训前准备
  createPreTrainingPreparation: async (data: PreTrainingPreparation) => {
    return await request.post({ url: `/data/pre-training-preparation/create`, data })
  },

  // 修改培训前准备
  updatePreTrainingPreparation: async (data: PreTrainingPreparation) => {
    return await request.put({ url: `/data/pre-training-preparation/update`, data })
  },

  // 删除培训前准备
  deletePreTrainingPreparation: async (id: number) => {
    return await request.delete({ url: `/data/pre-training-preparation/delete?id=` + id })
  },

  /** 批量删除培训前准备 */
  deletePreTrainingPreparationList: async (ids: number[]) => {
    return await request.delete({ url: `/data/pre-training-preparation/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训前准备 Excel
  exportPreTrainingPreparation: async (params) => {
    return await request.download({ url: `/data/pre-training-preparation/export-excel`, params })
  }
}
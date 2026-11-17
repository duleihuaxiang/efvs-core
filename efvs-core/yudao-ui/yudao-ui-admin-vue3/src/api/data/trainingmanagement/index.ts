import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训管理信息 */
export interface TrainingManagement {
          id: number; // ID
          serialNumber?: number; // 编号ID
          processName?: string; // 流程名称
          initiationTime?: string | Dayjs; // 发起时间
          initiator?: string; // 发起人
          currentStage?: string; // 当前环节
          remark: string; // 备注
  }

// 培训管理 API
export const TrainingManagementApi = {
  // 查询培训管理分页
  getTrainingManagementPage: async (params: any) => {
    return await request.get({ url: `/data/training-management/page`, params })
  },

  // 查询培训管理详情
  getTrainingManagement: async (id: number) => {
    return await request.get({ url: `/data/training-management/get?id=` + id })
  },

  // 新增培训管理
  createTrainingManagement: async (data: TrainingManagement) => {
    return await request.post({ url: `/data/training-management/create`, data })
  },

  // 修改培训管理
  updateTrainingManagement: async (data: TrainingManagement) => {
    return await request.put({ url: `/data/training-management/update`, data })
  },

  // 删除培训管理
  deleteTrainingManagement: async (id: number) => {
    return await request.delete({ url: `/data/training-management/delete?id=` + id })
  },

  /** 批量删除培训管理 */
  deleteTrainingManagementList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-management/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训管理 Excel
  exportTrainingManagement: async (params) => {
    return await request.download({ url: `/data/training-management/export-excel`, params })
  }
}
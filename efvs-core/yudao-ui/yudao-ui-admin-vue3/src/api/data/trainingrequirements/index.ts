import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训需求信息 */
export interface TrainingRequirements {
          id: number; // ID
          trainingTheme: string; // 培训主题
          customerName: string; // 客户名称
          trainingContent: string; // 培训内容
          trainingType: string; // 培训类型
          trainingTarget: string; // 培训对象
          trainingTime: string | Dayjs; // 培训时间
          remark: string; // 备注
  }

// 培训需求 API
export const TrainingRequirementsApi = {
  // 查询培训需求分页
  getTrainingRequirementsPage: async (params: any) => {
    return await request.get({ url: `/data/training-requirements/page`, params })
  },

  // 查询培训需求详情
  getTrainingRequirements: async (id: number) => {
    return await request.get({ url: `/data/training-requirements/get?id=` + id })
  },

  // 新增培训需求
  createTrainingRequirements: async (data: TrainingRequirements) => {
    return await request.post({ url: `/data/training-requirements/create`, data })
  },

  // 修改培训需求
  updateTrainingRequirements: async (data: TrainingRequirements) => {
    return await request.put({ url: `/data/training-requirements/update`, data })
  },

  // 删除培训需求
  deleteTrainingRequirements: async (id: number) => {
    return await request.delete({ url: `/data/training-requirements/delete?id=` + id })
  },

  /** 批量删除培训需求 */
  deleteTrainingRequirementsList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-requirements/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训需求 Excel
  exportTrainingRequirements: async (params) => {
    return await request.download({ url: `/data/training-requirements/export-excel`, params })
  }
}
import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 培训计划信息 */
export interface TrainingPlan {
  id: number // ID
  annualPlanProjectNumberId?: number // 年度计划项目编号
  trainingProgramsCourses?: string // 培训项目/课程
  trainingTargets?: string // 培训对象
  numberOfTrainees?: number // 培训人数
  trainingTime?: string | Dayjs // 培训时间
  trainingLocation?: string // 培训地点
  projectLeader?: string // 项目负责人
  remark: string // 备注
}

// 培训计划 API
export const TrainingPlanApi = {
  // 查询培训计划分页
  getTrainingPlanPage: async (params: any) => {
    return await request.get({ url: `/data/training-plan/page`, params })
  },

  // 查询培训计划详情
  getTrainingPlan: async (id: number) => {
    return await request.get({ url: `/data/training-plan/get?id=` + id })
  },

  // 新增培训计划
  createTrainingPlan: async (data: TrainingPlan) => {
    return await request.post({ url: `/data/training-plan/create`, data })
  },

  // 修改培训计划
  updateTrainingPlan: async (data: TrainingPlan) => {
    return await request.put({ url: `/data/training-plan/update`, data })
  },

  // 删除培训计划
  deleteTrainingPlan: async (id: number) => {
    return await request.delete({ url: `/data/training-plan/delete?id=` + id })
  },

  /** 批量删除培训计划 */
  deleteTrainingPlanList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-plan/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训计划 Excel
  exportTrainingPlan: async (params) => {
    return await request.download({ url: `/data/training-plan/export-excel`, params })
  },

  // 查项目负责人name
  getTrainingById: async (params: any) => {
    return await request.get({ url: `/data/training-plan/getTrainingById`, params })
  }
}

import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训考核信息 */
export interface TrainingAssessment {
          id: number; // ID，主键自增长
          serialNumber: number; // 编号
          name: string; // 姓名
          courseName: string; // 课程名称
          learningTime: number; // 学习时间
          isCompleted: number; // 是否完成，0是1否
          completedForm: string; // 完成形式
          score: string; // 成绩
          instructor: string; // 教员
          completionDate: string | Dayjs; // 完成日期
          remark: string; // 备注
  }

// 培训考核 API
export const TrainingAssessmentApi = {
  // 查询培训考核分页
  getTrainingAssessmentPage: async (params: any) => {
    return await request.get({ url: `/data/training-assessment/page`, params })
  },

  // 查询培训考核详情
  getTrainingAssessment: async (id: number) => {
    return await request.get({ url: `/data/training-assessment/get?id=` + id })
  },

  // 新增培训考核
  createTrainingAssessment: async (data: TrainingAssessment) => {
    return await request.post({ url: `/data/training-assessment/create`, data })
  },

  // 修改培训考核
  updateTrainingAssessment: async (data: TrainingAssessment) => {
    return await request.put({ url: `/data/training-assessment/update`, data })
  },

  // 删除培训考核
  deleteTrainingAssessment: async (id: number) => {
    return await request.delete({ url: `/data/training-assessment/delete?id=` + id })
  },

  /** 批量删除培训考核 */
  deleteTrainingAssessmentList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-assessment/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训考核 Excel
  exportTrainingAssessment: async (params) => {
    return await request.download({ url: `/data/training-assessment/export-excel`, params })
  }
}
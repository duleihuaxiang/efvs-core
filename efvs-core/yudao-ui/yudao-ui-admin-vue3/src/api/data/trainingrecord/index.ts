import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训记录信息 */
export interface TrainingRecord {
          id: number; // ID
          serialNumber?: number; // 编号
          name?: string; // 姓名
          courseName?: string; // 课程名称
          learningTime?: number; // 学习时间
          isCompleted?: number; // 是否完成 (0 否 1 是)
          perfectiveForm?: string; // 完成形式
          grades?: number; // 成绩
          instructor?: string; // 教员
          completionDate?: string | Dayjs; // 完成日期
          remark: string; // 备注
  }

// 培训记录 API
export const TrainingRecordApi = {
  // 查询培训记录分页
  getTrainingRecordPage: async (params: any) => {
    return await request.get({ url: `/data/training-record/page`, params })
  },

  // 查询培训记录详情
  getTrainingRecord: async (id: number) => {
    return await request.get({ url: `/data/training-record/get?id=` + id })
  },

  // 新增培训记录
  createTrainingRecord: async (data: TrainingRecord) => {
    return await request.post({ url: `/data/training-record/create`, data })
  },

  // 修改培训记录
  updateTrainingRecord: async (data: TrainingRecord) => {
    return await request.put({ url: `/data/training-record/update`, data })
  },

  // 删除培训记录
  deleteTrainingRecord: async (id: number) => {
    return await request.delete({ url: `/data/training-record/delete?id=` + id })
  },

  /** 批量删除培训记录 */
  deleteTrainingRecordList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-record/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训记录 Excel
  exportTrainingRecord: async (params) => {
    return await request.download({ url: `/data/training-record/export-excel`, params })
  }
}
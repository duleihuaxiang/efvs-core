import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训签到信息 */
export interface TrainingAttendance {
          id: number; // ID
          serialNumber?: number; // 编号
          name?: string; // 姓名 
          unitName?: string; // 单位名称
          trainingTheme?: string; // 培训主题
          trainingTime?: string | Dayjs; // 培训时间
          trainingInstructor?: number; // 培训讲师 (0 否 1 是)
          trainingCategory?: string; // 培训类别
          trainingLocation?: string; // 培训地点
          trainingContent?: string; // 培训内容
          remark: string; // 备注
  }

// 培训签到 API
export const TrainingAttendanceApi = {
  // 查询培训签到分页
  getTrainingAttendancePage: async (params: any) => {
    return await request.get({ url: `/data/training-attendance/page`, params })
  },

  // 查询培训签到详情
  getTrainingAttendance: async (id: number) => {
    return await request.get({ url: `/data/training-attendance/get?id=` + id })
  },

  // 新增培训签到
  createTrainingAttendance: async (data: TrainingAttendance) => {
    return await request.post({ url: `/data/training-attendance/create`, data })
  },

  // 修改培训签到
  updateTrainingAttendance: async (data: TrainingAttendance) => {
    return await request.put({ url: `/data/training-attendance/update`, data })
  },

  // 删除培训签到
  deleteTrainingAttendance: async (id: number) => {
    return await request.delete({ url: `/data/training-attendance/delete?id=` + id })
  },

  /** 批量删除培训签到 */
  deleteTrainingAttendanceList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-attendance/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训签到 Excel
  exportTrainingAttendance: async (params) => {
    return await request.download({ url: `/data/training-attendance/export-excel`, params })
  }
}
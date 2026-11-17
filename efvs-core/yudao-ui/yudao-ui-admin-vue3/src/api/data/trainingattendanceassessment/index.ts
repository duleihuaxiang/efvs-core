import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训签到考核证书信息 */
export interface TrainingAttendanceAssessment {
          id: number; // ID
          name: string; // 姓名
          courseName: string | Dayjs; // 课程名称
          checkInTime: string | Dayjs; // 签到时间
          isCompleted: number; // 是否完成学习 0是1否
          completedForm: string; // 完成形式
          score: string; // 成绩
          receiveCertificate: string; // 获得证书
          instructor: string; // 教员
          completionDate: string | Dayjs; // 完成日期
          remark: string; // 备注
  }

// 培训签到考核证书 API
export const TrainingAttendanceAssessmentApi = {
  // 查询培训签到考核证书分页
  getTrainingAttendanceAssessmentPage: async (params: any) => {
    return await request.get({ url: `/data/training-attendance-assessment/page`, params })
  },

  // 查询培训签到考核证书详情
  getTrainingAttendanceAssessment: async (id: number) => {
    return await request.get({ url: `/data/training-attendance-assessment/get?id=` + id })
  },

  // 新增培训签到考核证书
  createTrainingAttendanceAssessment: async (data: TrainingAttendanceAssessment) => {
    return await request.post({ url: `/data/training-attendance-assessment/create`, data })
  },

  // 修改培训签到考核证书
  updateTrainingAttendanceAssessment: async (data: TrainingAttendanceAssessment) => {
    return await request.put({ url: `/data/training-attendance-assessment/update`, data })
  },

  // 删除培训签到考核证书
  deleteTrainingAttendanceAssessment: async (id: number) => {
    return await request.delete({ url: `/data/training-attendance-assessment/delete?id=` + id })
  },

  /** 批量删除培训签到考核证书 */
  deleteTrainingAttendanceAssessmentList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-attendance-assessment/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训签到考核证书 Excel
  exportTrainingAttendanceAssessment: async (params) => {
    return await request.download({ url: `/data/training-attendance-assessment/export-excel`, params })
  }
}

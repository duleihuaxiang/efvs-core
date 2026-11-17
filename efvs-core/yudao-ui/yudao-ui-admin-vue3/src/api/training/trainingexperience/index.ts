import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训经历信息 */
export interface Experience {
          id: number; // ID
          userId: number; // 用户ID
          trainingTheme: string; // 培训主题
          trainingTime: string | Dayjs; // 培训时间
          trainingLocation: string; // 培训地点
          trainingDuration: number; // 培训时长
          achieveResults: string; // 培训成绩
          trainingTeachers: string; // 培训教师
          remark: string; // 备注
  }

// 培训经历 API
export const ExperienceApi = {
  // 查询培训经历分页
  getExperiencePage: async (params: any) => {
    return await request.get({ url: `/training/experience/page`, params })
  },

  // 查询培训经历详情
  getExperience: async (id: number) => {
    return await request.get({ url: `/training/experience/get?id=` + id })
  },

  // 新增培训经历
  createExperience: async (data: Experience) => {
    return await request.post({ url: `/training/experience/create`, data })
  },

  // 修改培训经历
  updateExperience: async (data: Experience) => {
    return await request.post({ url: `/training/experience/update`, data })
  },

  // 删除培训经历
  deleteExperience: async (id: number) => {
    return await request.delete({ url: `/training/experience/delete?id=` + id })
  },

  /** 批量删除培训经历 */
  deleteExperienceList: async (ids: number[]) => {
    return await request.delete({ url: `/training/experience/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训经历 Excel
  exportExperience: async (params) => {
    return await request.download({ url: `/training/experience/export-excel`, params })
  }
}

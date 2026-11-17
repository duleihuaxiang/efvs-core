import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训前准备问题信息 */
export interface Ppql {
          id: number; // ID
          problemDescription: string; // 问题描述
          remark: string; // 备注
  }

// 培训前准备问题 API
export const PpqlApi = {
  // 查询培训前准备问题分页
  getPpqlPage: async (params: any) => {
    return await request.get({ url: `/data/ppql/page`, params })
  },

  // 查询培训前准备问题详情
  getPpql: async (id: number) => {
    return await request.get({ url: `/data/ppql/get?id=` + id })
  },

  // 新增培训前准备问题
  createPpql: async (data: Ppql) => {
    return await request.post({ url: `/data/ppql/create`, data })
  },

  // 修改培训前准备问题
  updatePpql: async (data: Ppql) => {
    return await request.put({ url: `/data/ppql/update`, data })
  },

  // 删除培训前准备问题
  deletePpql: async (id: number) => {
    return await request.delete({ url: `/data/ppql/delete?id=` + id })
  },

  /** 批量删除培训前准备问题 */
  deletePpqlList: async (ids: number[]) => {
    return await request.delete({ url: `/data/ppql/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训前准备问题 Excel
  exportPpql: async (params) => {
    return await request.download({ url: `/data/ppql/export-excel`, params })
  }
}
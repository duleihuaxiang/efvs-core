import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 机型属性信息 */
export interface MemoryAttribute {
          id: number; // 机型属性编号
          applicableModelNumber: string; // 适用机型编号
          installationAircraftNumber: string; // 装机飞机编号
          installationLocationName: string; // 装机位置名称
          adaptationVersion: string; // 适配版本要求
          installationTime: string | Dayjs; // 装机时间
          adaptationStatus: number; // 适航适配状态0是1否（是否完成适航验证）
          remark: string; // 备注
  }

// 机型属性 API
export const MemoryAttributeApi = {
  // 查询机型属性分页
  getMemoryAttributePage: async (params: any) => {
    return await request.get({ url: `/data/memory-attribute/page`, params })
  },

  // 查询机型属性详情
  getMemoryAttribute: async (id: number) => {
    return await request.get({ url: `/data/memory-attribute/get?id=` + id })
  },

  // 新增机型属性
  createMemoryAttribute: async (data: MemoryAttribute) => {
    return await request.post({ url: `/data/memory-attribute/create`, data })
  },

  // 修改机型属性
  updateMemoryAttribute: async (data: MemoryAttribute) => {
    return await request.put({ url: `/data/memory-attribute/update`, data })
  },

  // 删除机型属性
  deleteMemoryAttribute: async (id: number) => {
    return await request.delete({ url: `/data/memory-attribute/delete?id=` + id })
  },

  /** 批量删除机型属性 */
  deleteMemoryAttributeList: async (ids: number[]) => {
    return await request.delete({ url: `/data/memory-attribute/delete-list?ids=${ids.join(',')}` })
  },

  // 导出机型属性 Excel
  exportMemoryAttribute: async (params) => {
    return await request.download({ url: `/data/memory-attribute/export-excel`, params })
  }
}
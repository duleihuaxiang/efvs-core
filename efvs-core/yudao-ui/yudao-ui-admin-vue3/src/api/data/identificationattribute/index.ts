import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 标识属性信息 */
export interface IdentificationAttribute {
          id: number; // 标识属性ID
          name: string; // 装机产品名称
          productNumber: string; // 装机产品编号
          sequence: string; // 序列号
          installationLocationNumber: string; // 安装位置编号
          productType: string; // 产品类别
          affiliatedSystem: string; // 所属系统
          remark: string; // 备注
  }

// 标识属性 API
export const IdentificationAttributeApi = {
  // 查询标识属性分页
  getIdentificationAttributePage: async (params: any) => {
    return await request.get({ url: `/data/identification-attribute/page`, params })
  },

  // 查询标识属性详情
  getIdentificationAttribute: async (id: number) => {
    return await request.get({ url: `/data/identification-attribute/get?id=` + id })
  },

  // 新增标识属性
  createIdentificationAttribute: async (data: IdentificationAttribute) => {
    return await request.post({ url: `/data/identification-attribute/create`, data })
  },

  // 修改标识属性
  updateIdentificationAttribute: async (data: IdentificationAttribute) => {
    return await request.put({ url: `/data/identification-attribute/update`, data })
  },

  // 删除标识属性
  deleteIdentificationAttribute: async (id: number) => {
    return await request.delete({ url: `/data/identification-attribute/delete?id=` + id })
  },

  /** 批量删除标识属性 */
  deleteIdentificationAttributeList: async (ids: number[]) => {
    return await request.delete({ url: `/data/identification-attribute/delete-list?ids=${ids.join(',')}` })
  },

  // 导出标识属性 Excel
  exportIdentificationAttribute: async (params) => {
    return await request.download({ url: `/data/identification-attribute/export-excel`, params })
  }
}

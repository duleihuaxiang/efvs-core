import request from '@/config/axios'

/** 保障属性信息 */
export interface GuaranteeAttributes {
          id: number; // 保障编号
          currentLifespan: number; // 当前寿命累计（小时）
          maintenanceInterval: number; // 保养周期（月）
          maintenanceRecordNumber: string; // 检修记录编号
          maintenanceIevel: string; // 维修级别
          remark: string; // 备注
  }

// 保障属性 API
export const GuaranteeAttributesApi = {
  // 查询保障属性分页
  getGuaranteeAttributesPage: async (params: any) => {
    return await request.get({ url: `/data/guarantee-attributes/page`, params })
  },

  // 查询保障属性详情
  getGuaranteeAttributes: async (id: number) => {
    return await request.get({ url: `/data/guarantee-attributes/get?id=` + id })
  },

  // 新增保障属性
  createGuaranteeAttributes: async (data: GuaranteeAttributes) => {
    return await request.post({ url: `/data/guarantee-attributes/create`, data })
  },

  // 修改保障属性
  updateGuaranteeAttributes: async (data: GuaranteeAttributes) => {
    return await request.put({ url: `/data/guarantee-attributes/update`, data })
  },

  // 删除保障属性
  deleteGuaranteeAttributes: async (id: number) => {
    return await request.delete({ url: `/data/guarantee-attributes/delete?id=` + id })
  },

  /** 批量删除保障属性 */
  deleteGuaranteeAttributesList: async (ids: number[]) => {
    return await request.delete({ url: `/data/guarantee-attributes/delete-list?ids=${ids.join(',')}` })
  },

  // 导出保障属性 Excel
  exportGuaranteeAttributes: async (params) => {
    return await request.download({ url: `/data/guarantee-attributes/export-excel`, params })
  }
}

import request from '@/config/axios'

/** 设计属性信息 */
export interface DesignAttribute {
          id: number; // 设计ID
          version?: string; // 设计版本号
          domLevel: string; // DOM结构级别
          description: string; // 功能描述
          approvalUmber?: string; // 批准号
          designLifespan: number; // 设计寿命
          technicalDrawingNumber: string; // 技术图纸编号
          remark: string; // 备注
  }

// 设计属性 API
export const DesignAttributeApi = {
  // 查询设计属性分页
  getDesignAttributePage: async (params: any) => {
    return await request.get({ url: `/data/design-attribute/page`, params })
  },

  // 查询设计属性详情
  getDesignAttribute: async (id: number) => {
    return await request.get({ url: `/data/design-attribute/get?id=` + id })
  },

  // 新增设计属性
  createDesignAttribute: async (data: DesignAttribute) => {
    return await request.post({ url: `/data/design-attribute/create`, data })
  },

  // 修改设计属性
  updateDesignAttribute: async (data: DesignAttribute) => {
    return await request.put({ url: `/data/design-attribute/update`, data })
  },

  // 删除设计属性
  deleteDesignAttribute: async (id: number) => {
    return await request.delete({ url: `/data/design-attribute/delete?id=` + id })
  },

  /** 批量删除设计属性 */
  deleteDesignAttributeList: async (ids: number[]) => {
    return await request.delete({ url: `/data/design-attribute/delete-list?ids=${ids.join(',')}` })
  },

  // 导出设计属性 Excel
  exportDesignAttribute: async (params) => {
    return await request.download({ url: `/data/design-attribute/export-excel`, params })
  }
}

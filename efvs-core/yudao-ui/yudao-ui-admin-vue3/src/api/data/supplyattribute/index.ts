import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 供应属性信息 */
export interface SupplyAttribute {
          id: number; // 供应属性编号
          name: string; // 供应商名称
          supplierNumber: string; // 供应商编号
          productionDate: string | Dayjs; // 生产日期
          batchNumber: string; // 保修状态0是1否
          warrantyStatus: number; // 保修状态
          purchaseOrderNumber: string; // 采购订单号
          remark: string; // 备注
  }

// 供应属性 API
export const SupplyAttributeApi = {
  // 查询供应属性分页
  getSupplyAttributePage: async (params: any) => {
    return await request.get({ url: `/data/supply-attribute/page`, params })
  },

  // 查询供应属性详情
  getSupplyAttribute: async (id: number) => {
    return await request.get({ url: `/data/supply-attribute/get?id=` + id })
  },

  // 新增供应属性
  createSupplyAttribute: async (data: SupplyAttribute) => {
    return await request.post({ url: `/data/supply-attribute/create`, data })
  },

  // 修改供应属性
  updateSupplyAttribute: async (data: SupplyAttribute) => {
    return await request.put({ url: `/data/supply-attribute/update`, data })
  },

  // 删除供应属性
  deleteSupplyAttribute: async (id: number) => {
    return await request.delete({ url: `/data/supply-attribute/delete?id=` + id })
  },

  /** 批量删除供应属性 */
  deleteSupplyAttributeList: async (ids: number[]) => {
    return await request.delete({ url: `/data/supply-attribute/delete-list?ids=${ids.join(',')}` })
  },

  // 导出供应属性 Excel
  exportSupplyAttribute: async (params) => {
    return await request.download({ url: `/data/supply-attribute/export-excel`, params })
  }
}

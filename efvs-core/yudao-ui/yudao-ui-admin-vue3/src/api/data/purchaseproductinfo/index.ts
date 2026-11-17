import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 购买产品信息信息 */
export interface PurchaseProductInfo {
          id: number; // 产品编号
          customerId: string; // 客户id
          productCategory: string; // 产品大类
          productName: string; // 产品名称
          productModel: string; // 产品型号
          productSerialNum: string; // 产品序列号
          productQuantity: number; // 产品数量
          remark: string; // 备注
  }

// 购买产品信息 API
export const PurchaseProductInfoApi = {
  // 查询购买产品信息分页
  getPurchaseProductInfoPage: async (params: any) => {
    return await request.get({ url: `/data/purchase-product-info/page`, params })
  },

  // 查询购买产品信息详情
  getPurchaseProductInfo: async (id: number) => {
    return await request.get({ url: `/data/purchase-product-info/get?id=` + id })
  },

  // 新增购买产品信息
  createPurchaseProductInfo: async (data: PurchaseProductInfo) => {
    return await request.post({ url: `/data/purchase-product-info/create`, data })
  },

  // 修改购买产品信息
  updatePurchaseProductInfo: async (data: PurchaseProductInfo) => {
    return await request.put({ url: `/data/purchase-product-info/update`, data })
  },

  // 删除购买产品信息
  deletePurchaseProductInfo: async (id: number) => {
    return await request.delete({ url: `/data/purchase-product-info/delete?id=` + id })
  },

  /** 批量删除购买产品信息 */
  deletePurchaseProductInfoList: async (ids: number[]) => {
    return await request.delete({ url: `/data/purchase-product-info/delete-list?ids=${ids.join(',')}` })
  },

  // 导出购买产品信息 Excel
  exportPurchaseProductInfo: async (params) => {
    return await request.download({ url: `/data/purchase-product-info/export-excel`, params })
  }
}
import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 产品BOM信息 */
export interface ProductBom {
          id: number; // 产品BOMID
          pmn: string; // PMN
          level: number; // 层级
          productVersion: string; // 版本
          productType: number; // 类型
          partNumber: string; // 产品代号
          name: string; // 名称
          productPartNumber: string; // 所属产品件号
          productNum: number; // 数量
          unit: string; // 单位
          tagNumber: string; // 位号
          subcategory: string; // 细类
          drawingNumber: string; // 规格或图号
          category: number; // 大类
          smallSubcategory: number; // 小类
          remark: string; // 备注
  }

// 产品BOM API
export const ProductBomApi = {
  // 查询产品BOM分页
  getProductBomPage: async (params: any) => {
    return await request.get({ url: `/data/product-bom/page`, params })
  },

  // 查询产品BOM详情
  getProductBom: async (id: number) => {
    return await request.get({ url: `/data/product-bom/get?id=` + id })
  },

  // 新增产品BOM
  createProductBom: async (data: ProductBom) => {
    return await request.post({ url: `/data/product-bom/create`, data })
  },

  // 修改产品BOM
  updateProductBom: async (data: ProductBom) => {
    return await request.put({ url: `/data/product-bom/update`, data })
  },

  // 删除产品BOM
  deleteProductBom: async (id: number) => {
    return await request.delete({ url: `/data/product-bom/delete?id=` + id })
  },

  /** 批量删除产品BOM */
  deleteProductBomList: async (ids: number[]) => {
    return await request.delete({ url: `/data/product-bom/delete-list?ids=${ids.join(',')}` })
  },

  // 导出产品BOM Excel
  exportProductBom: async (params) => {
    return await request.download({ url: `/data/product-bom/export-excel`, params })
  },

  /** 导出BOM详情 Excel */
  exportProductBomDetail: async (partNumber: string) => {
    return await request.download({
      url: '/data/product-bom/export-detail-excel',
      params: { partNumber }
    })
  },
  importProductBom: (data: FormData) => {
    return request.post({
      url: '/data/product-bom/import-excel',
      data,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 下载产品BOM导入模版
  downloadTemplate: async () => {
    return await request.download({ url: '/data/product-bom/download-template' })
  }
}
import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 产品信息 */
export interface Product {
  id: number // 商品编号
  guaranteeId: number // 保障属性ID
  supplyId: number // 供应属性ID
  identificaionId: number // 标识属性ID
  designId: number // 设计属性ID
  memoryId: number // 机型属性ID
  faultInformationId: number // 故障信息ID
  pmn: string // PMN
  level: number // 层级
  name: string // 名称
  codeName: string // 代号
  componentCode: string // 所属部件代号
  productVersion: string // 版本
  productType: number // 类型
  productNum: number // 数量
  partNumber: string // 产品件号
  sequenceNumber: string // 序列号
  manufacturer: string // 厂家
  customerNumber: string // 客户编号
  customerName: string // 客户名称
  unit: string // 单位
  remark: string // 备注
}

// 产品 API
export const ProductApi = {
  // 查询产品分页
  getProductPage: async (params: any) => {
    return await request.get({ url: `/data/product/page`, params })
  },

  // 查询产品详情
  getProduct: async (id: number) => {
    return await request.get({ url: `/data/product/get?id=` + id })
  },

  // 新增产品
  createProduct: async (data: Product) => {
    return await request.post({ url: `/data/product/create`, data })
  },

  // 修改产品
  updateProduct: async (data: any) => {
    return await request.put({ url: `/data/product/update`, data })
  },

  // 删除产品
  deleteProduct: async (id: number) => {
    return await request.delete({ url: `/data/product/delete?id=` + id })
  },

  /** 批量删除产品 */
  deleteProductList: async (ids: number[]) => {
    return await request.delete({ url: `/data/product/delete-list?ids=${ids.join(',')}` })
  },

  // 导出产品 Excel
  exportProduct: async (params) => {
    return await request.download({ url: `/data/product/export-excel`, params })
  },

  //查询全部故障信息
  getAllFaultInformation: async () => {
    return await request.get({ url: `/data/fault-information/getAllFaultInformation` })
  },
  //查询全部保障信息
  getAllGuaranteeAttributes: async () => {
    return await request.get({ url: `/data/guarantee-attributes/getAllGuaranteeAttributes` })
  },

  // 查询全部标识属性
  getAllIdentificationAttribute: async () => {
    return await request.get({
      url: `/data/identification-attribute/getAllIdentificationAttribute`
    })
  },
  //查询全部机型属性
  getAllMemoryAttribute: async () => {
    return await request.get({ url: `/data/product/memory-attribute/getAllMemoryAttribute` })
  },

  //查询全部供应属性
  getAllSupplyAttribute: async () => {
    return await request.get({ url: `/data/supply-attribute/getAllSupplyAttribute` })
  },
  //查询全部设计属性
  getAllDesignAttribute: async () => {
    return await request.get({ url: `/data/design-attribute/getAllDesignAttribute` })
  },
  // 获取供应属性
  getSupplyAttribute: async (ids: Array<number>) => {
    return await request.get({ url: `/data/supply-attribute/get`, params: { ids: ids.join(',') } })
  },
  // 获取机型属性
  getMemoryAttribute: async (ids: Array<number>) => {
    return await request.get({
      url: `/data/product/memory-attribute/get`,
      params: { ids: ids.join(',') }
    })
  },
  // 获得标识属性
  getIdentificationAttribute: async (ids: Array<number>) => {
    return await request.get({
      url: `/data/identification-attribute/get`,
      params: { ids: ids.join(',') }
    })
  },
  // 获得保障属性
  getGuaranteeAttributes: async (ids: Array<number>) => {
    return await request.get({
      url: `/data/guarantee-attributes/get`,
      params: { ids: ids.join(',') }
    })
  },
  // 获得设计属性
  getDesignAttribute: async (ids: Array<number>) => {
    return await request.get({
      url: `/data/design-attribute/get`,
      params: { ids: ids.join(',') }
    })
  },

  // 获取故障信息
  getFaultInformation: async (ids: Array<number>) => {
    return await request.get({ url: `/data/fault-information/get`, params: { ids: ids.join(',') } })
  },

  // 获取产品全部属性备注（富文本）
  getProductAllAttributes: async (id: number) => {
    return await request.get({ url: `/efvs/attribute/get?id=` + id })
  },

  // 修改产品全部属性备注
  updateProductAllAttributes: async (data: {
    id?: number
    productId: number
    designAttribute?: string
    guaranteeAttribute?: string
    supplyAttribute?: string
    modelAttribute?: string
    faultAttribute?: string
  }) => {
    return await request.put({ url: `/efvs/attribute/update`, data })
  },

  // 查询产品序列号
  getProductSerialNumber: async (params: any) => {
    return await request.get({ url: `/data/product/getProductSerialNumber`, params })
  },

  // 获得产品档案
  getProductFile: async (params: any) => {
    return await request.get({ url: `/data/product/getProductFile`, params })
  },

  // 下载产品档案模版
  downloadArchiveTemplate: async () => {
    return await request.download({ url: `/data/product/download-archive-template` })
  },

  // 导入产品档案 Excel（不要手动设置 Content-Type，否则缺少 boundary，后端收不到 partNumber）
  importProductFileExcel: (data: FormData) => {
    return request.post({ url: '/data/product/import-file-excel', data })
  },

  // 获取产品BOM列表
  getBOMPage: async (params: any) => {
    return await request.get({ url: `/data/product-bom/page`, params })
  },

  // 获取产品BOM详情
  getBOMList: async (params: any) => {
    return await request.get({ url: `/data/product-bom/getBOMDetailList`, params })
  },

  // 新增产品BOM
  createBOM: async (data: any) => {
    return await request.post({ url: `/data/product-bom/create`, data })
  },
  //修改产品BOM
  updateBOM: async (data: any) => {
    return await request.post({ url: `/data/product-bom/update`, data })
  },

  // 获取产品BOM详情
  getBOM: async (id: number) => {
    return await request.get({ url: `/data/product-bom/get?id=` + id })
  },

  // 删除产品BOM
  deleteBOM: async (ids: number[]) => {
    return await request.delete({ url: `/data/product-bom/delete-list?ids=${ids.join(',')}` })
  }
}

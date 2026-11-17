import request from '@/config/axios'

/** 客户信息信息 */
export interface CustomerInfo {
  id: number // ID
  customerName: string // 客户名称
  customerId: string // 客户编号
  customerContactPerson: string // 客户联系人
  phone: number // 联系人电话
  dept: number // 所属部门
  email: string // 邮箱地址
  province: string // 客户地址（省）
  city: string // 客户地址（市）
  district: string // 客户地址（区）
  postalCode: number // 邮编
  address: string // 详细地址
  dataSource: string // 数据获取渠道
  remark: string // 备注
}

// 客户信息 API
export const CustomerInfoApi = {
  // 查询客户信息分页
  getCustomerInfoPage: async (params: any) => {
    return await request.get({ url: `/data/customer-info/getCustomerInfoPage`, params })
  },

  // 查询客户信息详情
  getCustomerInfo: async (id: number) => {
    return await request.get({ url: `/data/customer-info/getCustomerInfoById?id=` + id })
  },

  // 新增客户信息
  createCustomerInfo: async (data: CustomerInfo) => {
    return await request.post({ url: `/data/customer-info/createCustomerInfo`, data })
  },

  // 修改客户信息
  updateCustomerInfo: async (data: CustomerInfo) => {
    return await request.post({ url: `/data/customer-info/updateCustomerInfo`, data })
  },

  // 删除客户信息
  deleteCustomerInfo: async (id: number) => {
    return await request.delete({ url: `/data/customer-info/deleteCustomerInfoById?id=` + id })
  },

  /** 批量删除客户信息 */
  deleteCustomerInfoList: async (ids: number[]) => {
    return await request.delete({
      url: `/data/customer-info/deleteCustomerInfoByIds?ids=${ids.join(',')}`
    })
  },

  // 导出客户信息 Excel
  exportCustomerInfo: async (params) => {
    return await request.download({ url: `/data/customer-info/exportCustomerInfoExcel`, params })
  },

  // 客户地址省市区
  customerArea: async (params?: any) => {
    return await request.get({ url: `/geo`, params })
  }
}

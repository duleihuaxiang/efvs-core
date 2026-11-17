import request from '@/config/axios'
// import type { Dayjs } from 'dayjs';

/** QAR映射信息信息 */
export interface Info {
  qarCode: string // QAR编号（唯一标识QAR映射记录）
  mappingName: string // 映射名称（如东航QAR）
  mappingTableFile: any // 映射表格文件名（如QAR映射.xlsx）
  isEnabled: string // 是否启用：1-是 0-否
  remark: string // 备注
}

// QAR映射信息 API
export const InfoApi = {
  // 查询QAR映射信息分页
  getInfoPage: async (params: any) => {
    return await request.get({ url: `/reliabilityData/qarMapping/getQarMappingInfoPage`, params })
  },

  // 查询QAR映射信息详情
  getInfo: async (id: number) => {
    return await request.get({ url: `/reliabilityData/qarMapping/getQarMappingInfoById?id=` + id })
  },

  // 新增QAR映射信息
  createInfo: async (data: Info) => {
    return await request.post({ url: `/reliabilityData/qarMapping/createQarMappingInfo`, data })
  },

  // 修改QAR映射信息
  updateInfo: async (data: Info) => {
    return await request.post({ url: `/reliabilityData/qarMapping/updateQarMappingInfo`, data })
  },

  // 删除QAR映射信息
  deleteInfo: async (id: number) => {
    return await request.delete({
      url: `/reliabilityData/qarMapping/deleteQarMappingInfoById?id=` + id
    })
  },

  /** 批量删除QAR映射信息 */
  deleteInfoList: async (ids: number[]) => {
    return await request.delete({
      url: `/reliabilityData/qarMapping/deleteQarMappingInfoByIds?ids=${ids.join(',')}`
    })
  },

  // 导出QAR映射信息 Excel
  exportInfo: async (params) => {
    return await request.download({
      url: `/reliabilityData/qarMapping/exportQarMappingInfoExcel`,
      params
    })
  },

  // 是否启用
  updateIsEnabled: async (data: any) => {
    return await request.post({
      url: `/reliabilityData/qarMapping/updateIsEnabled`,
      data
    })
  }
}

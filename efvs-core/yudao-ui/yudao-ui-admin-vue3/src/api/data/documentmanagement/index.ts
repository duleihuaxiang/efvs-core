import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 文档管理信息 */
export interface DocumentManagement {
          id: number; // ID
          name?: string; // 节点名称
          parentId?: number; // 父节点ID 0表示无父节点（即一级节点）
          nodeType?: string; // 节点类型 可用于区分是目录还是具体资料类型
          remark: string; // 备注
  }

// 文档管理 API
export const DocumentManagementApi = {
  // 查询文档管理分页
  getDocumentManagementPage: async (params: any) => {
    return await request.get({ url: `/data/document-management/page`, params })
  },

  // 查询文档管理详情
  getDocumentManagement: async (id: number) => {
    return await request.get({ url: `/data/document-management/get?id=` + id })
  },

  // 新增文档管理
  createDocumentManagement: async (data: DocumentManagement) => {
    return await request.post({ url: `/data/document-management/create`, data })
  },

  // 修改文档管理
  updateDocumentManagement: async (data: DocumentManagement) => {
    return await request.put({ url: `/data/document-management/update`, data })
  },

  // 删除文档管理
  deleteDocumentManagement: async (id: number) => {
    return await request.delete({ url: `/data/document-management/delete?id=` + id })
  },

  /** 批量删除文档管理 */
  deleteDocumentManagementList: async (ids: number[]) => {
    return await request.delete({ url: `/data/document-management/delete-list?ids=${ids.join(',')}` })
  },

  // 导出文档管理 Excel
  exportDocumentManagement: async (params) => {
    return await request.download({ url: `/data/document-management/export-excel`, params })
  }
}
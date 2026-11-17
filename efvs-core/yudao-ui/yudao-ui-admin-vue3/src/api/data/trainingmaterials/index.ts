import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 培训资料管理信息 */
export interface TrainingMaterials {
          id: number; // ID
          trainingId?: number; // 培训资料表ID
          documentName?: string; // 资料名称
          containingDirectory?: string; // 所属目录
          category?: string; // 分类
          size?: number; // 大小(M)
          remark: string; // 备注
  }

// 培训资料管理 API
export const TrainingMaterialsApi = {
  // 查询培训资料管理分页
  getTrainingMaterialsPage: async (params: any) => {
    return await request.get({ url: `/data/training-materials/page`, params })
  },

  // 查询培训资料管理详情
  getTrainingMaterials: async (id: number) => {
    return await request.get({ url: `/data/training-materials/get?id=` + id })
  },

  // 新增培训资料管理
  createTrainingMaterials: async (data: TrainingMaterials) => {
    return await request.post({ url: `/data/training-materials/create`, data })
  },

  // 修改培训资料管理
  updateTrainingMaterials: async (data: TrainingMaterials) => {
    return await request.put({ url: `/data/training-materials/update`, data })
  },

  // 删除培训资料管理
  deleteTrainingMaterials: async (id: number) => {
    return await request.delete({ url: `/data/training-materials/delete?id=` + id })
  },

  /** 批量删除培训资料管理 */
  deleteTrainingMaterialsList: async (ids: number[]) => {
    return await request.delete({ url: `/data/training-materials/delete-list?ids=${ids.join(',')}` })
  },

  // 导出培训资料管理 Excel
  exportTrainingMaterials: async (params) => {
    return await request.download({ url: `/data/training-materials/export-excel`, params })
  }
}
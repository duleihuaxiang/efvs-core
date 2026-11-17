import request from '@/config/axios'

export type FormVO = {
  id: number
  name: string
  conf: string
  fields: string[]
  status: number
  remark: string
  createTime: string
}
export type ElFormconfigVO = {
  processId: any
  activityId: string | undefined
  formType: string | undefined
  formUrl: string | undefined
  status: number
}

// 创建工作流的表单定义
export const createForm = async (data: FormVO) => {
  return await request.post({
    url: '/bpm/form/create',
    data: data
  })
}

// 更新工作流的表单定义
export const updateForm = async (data: FormVO) => {
  return await request.put({
    url: '/bpm/form/update',
    data: data
  })
}

// 删除工作流的表单定义
export const deleteForm = async (id: number) => {
  return await request.delete({
    url: '/bpm/form/delete?id=' + id
  })
}

// 获得工作流的表单定义
export const getForm = async (id: number) => {
  return await request.get({
    url: '/bpm/form/get?id=' + id
  })
}

// 获得工作流的表单定义分页
export const getFormPage = async (params) => {
  return await request.get({
    url: '/bpm/form/page',
    params
  })
}

// 获得动态表单的精简列表
export const getFormSimpleList = async () => {
  return await request.get({
    url: '/bpm/form/simple-list'
  })
}

// 创建或修改外置表单配置
export const creatProcessActFormConfig = async (data: ElFormconfigVO) => {
  return await request.post({
    url: '/form/external-form/creatProcessActFormConfig',
    data: data
  })
}

// /admin-api/form/external-form/processActFormConfigByProDefIDAndActID
export const getFormConf = async (params) => {
  return await request.get({
    url: '/form/external-form/processActFormConfigByProDefIDAndActID',
    params
  })
}

// 获取流程审批数据
export const getProcessInstanceId = async (params) => {
  return await request.get({
    url: '/customerResponse/customer-service-issues/getProcessInstanceId',
    params
  })
}

// 获取培训审批-培训计划详情
// /data/training-plan/getTrainingPlan
export const getTrainingPlan = async (params) => {
  return await request.get({
    url: '/data/training-plan/getTrainingPlan',
    params
  })
}

// 获取培训前准备工作详情
//  /data/pre-training-preparation/getPreTrainingPreparation
export const getPreTrainingPreparation = async (params) => {
  return await request.get({
    url: '/data/pre-training-preparation/getPreTrainingPreparation',
    params
  })
}

// 获得培训前准备问题列表
export const getPpqlList = async () => {
  return await request.get({
    url: '/data/ppql/list'
  })
}

// 培训签到考核证书发放表单详情信息
export const getTrainingAttendanceAssessment = async (params) => {
  return await request.get({
    url: '/data/training-attendance-assessment/getTrainingAttendanceAssessmentByIds',
    params
  })
}

// 培训签到考核证书发放下载模板
export const getTrainingAttendanceAssessmentList = async () => {
  return await request.download({
    url: '/data/training-attendance-assessment/get-import-template'
  })
}

//导入培训签到考核证书表格
export const importTrainingSignAttendanceExcel = async (data: FormData) => {
  return await request.post({ url: '/data/training-attendance-assessment/parse-excel ', data })
}

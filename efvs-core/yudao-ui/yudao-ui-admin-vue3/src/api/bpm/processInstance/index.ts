import request from '@/config/axios'
import { ProcessDefinitionVO } from '@/api/bpm/model'
import { NodeType, CandidateStrategy } from '@/components/SimpleProcessDesignerV2/src/consts'
export type Task = {
  id: string
  name: string
}

export type ProcessInstanceVO = {
  id: number
  name: string
  processDefinitionId: string
  category: string
  result: number
  tasks: Task[]
  fields: string[]
  status: number
  remark: string
  businessKey: string
  createTime: string
  endTime: string
  processDefinition?: ProcessDefinitionVO
}

// 用户信息
export type User = {
  id: number
  nickname: string
  avatar: string
}

// 审批任务信息
export type ApprovalTaskInfo = {
  id: number
  ownerUser: User
  assigneeUser: User
  status: number
  reason: string
  signPicUrl: string
}

// 审批节点信息
export type ApprovalNodeInfo = {
  id: number
  name: string
  nodeType: NodeType
  candidateStrategy?: CandidateStrategy
  status: number
  startTime?: Date
  endTime?: Date
  processInstanceId?: string
  candidateUsers?: User[]
  tasks: ApprovalTaskInfo[]
}

export const getProcessInstanceMyPage = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/my-page', params })
}

export const getProcessInstanceManagerPage = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/manager-page', params })
}

export const createProcessInstance = async (data) => {
  return await request.post({ url: '/bpm/process-instance/create', data: data })
}

export const cancelProcessInstanceByStartUser = async (id: number, reason: string) => {
  const data = {
    id: id,
    reason: reason
  }
  return await request.delete({ url: '/bpm/process-instance/cancel-by-start-user', data: data })
}

export const cancelProcessInstanceByAdmin = async (id: number, reason: string) => {
  const data = {
    id: id,
    reason: reason
  }
  return await request.delete({ url: '/bpm/process-instance/cancel-by-admin', data: data })
}

export const getProcessInstance = async (id: string) => {
  return await request.get({ url: '/bpm/process-instance/get?id=' + id })
}

export const getProcessInstanceCopyPage = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/copy/page', params })
}

// 获取审批详情
export const getApprovalDetail = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/get-approval-detail', params })
}

// 获取下一个执行的流程节点
export const getNextApprovalNodes = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/get-next-approval-nodes', params })
}

// 获取表单字段权限
export const getFormFieldsPermission = async (params: any) => {
  return await request.get({ url: '/bpm/process-instance/get-form-fields-permission', params })
}

// 获取流程实例的 BPMN 模型视图
export const getProcessInstanceBpmnModelView = async (id: string) => {
  return await request.get({ url: '/bpm/process-instance/get-bpmn-model-view?id=' + id })
}

// 获取流程实例打印数据
export const getProcessInstancePrintData = async (id: string) => {
  return await request.get({ url: '/bpm/process-instance/get-print-data?processInstanceId=' + id })
}

// 获取客户信息
export const getCustomerInfoAll = async () => {
  return await request.get({ url: '/data/customer-info/getCustomerInfoAll' })
}
// 培训计划编号
export const generateNumber = async (type: string) => {
  return await request.get({ url: '/flowId/generate-number?type=' + type })
}

// 获取所有的可靠性编号
export const getAllReliabilityData = async () => {
  return await request.get({ url: '/reliability/data/getAll' })
}

// 生成可靠性数据收集表单编号：
export const generateReliabilityFormNo = async () => {
  return await request.get({ url: '/reliability/data/form-no' })
}

// 可靠性数据收集表单 获取部件名称/代号/SRU名称/SRU代号/序列号
export const getProductAll = async () => {
  return await request.get({ url: '/data/product/getProductAll' })
}
// 可靠性数据详情
export const getReliabilityDataById = async (processInstanceId: string) => {
  return await request.get({
    url: 'reliability/data/getReliabilityByProcessInstanceId?processInstanceId=' + processInstanceId
  })
}
// 版本
export const getReliabilityDataOptions = async (params: any) => {
  return await request.get({
    url: '/reliability/data/options/series',
    params
  })
}
//序列号 getSerialNumber
export const getSerialNumber = async (params: any) => {
  return await request.get({
    url: '/reliability/data/options/serialNumbers',
    params
  })
}
// 获取部件名称
export const getPartNameAll = async () => {
  return await request.get({
    url: '/reliability/data/options/partNames'
  })
}

// 获取部件代号和交付时间
export const getFillPartCodeAndDeliveryTime = async (params: any) => {
  return await request.get({
    url: '/reliability/data/options/fillPartCodeAndDeliveryTime',
    params
  })
}

// SRU下拉数据
export const findSRU = async (params: any) => {
  return await request.get({
    url: '/reliability/data/findSRU',
    params
  })
}

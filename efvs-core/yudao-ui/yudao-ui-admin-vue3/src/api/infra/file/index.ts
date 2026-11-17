import request from '@/config/axios'

export interface FilePresignedUrlRespVO {
  configId: number
  uploadUrl: string
  url: string
  path: string
}

export interface FileVO {
  id: number
  configId: number
  url: string
  path: string
  name: string
  type: string
  size: number
  createTime: Date
}

// 获取文件预签名地址
export const getFilePresignedUrl = (fileName: string, directory?: string) => {
  return request.get({ url: '/infra/file/presigned-url', params: { fileName, directory } })
}

// 上传文件（后端模式）
export const updateFile = (data: { file: File; directory?: string }) => {
  const formData = new FormData()
  formData.append('file', data.file)
  if (data.directory) {
    formData.append('directory', data.directory)
  }
  return request.post({ url: '/infra/file/upload', data: formData })
}

// 创建文件信息
export const createFile = (data: {
  configId: number
  url: string
  path: string
  name: string
  type: string
  size: number
}) => {
  return request.post({ url: '/infra/file/create', data })
}

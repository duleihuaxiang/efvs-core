import { service } from './service'

import { config } from './config'

const { default_headers } = config

const request = (option: any) => {
  const { headersType, headers, ...otherOption } = option
  const isFormData = otherOption.data instanceof FormData
  return service({
    ...otherOption,
    headers: {
      // FormData 时不要设置 Content-Type，让浏览器自动带 boundary，否则后端解析不到 partNumber 等字段
      ...(isFormData ? {} : { 'Content-Type': headersType || default_headers }),
      ...headers
    }
  })
}
export default {
  get: async <T = any>(option: any) => {
    const res = await request({ method: 'GET', ...option })
    return res.data as unknown as T
  },
  post: async <T = any>(option: any) => {
    const res = await request({ method: 'POST', ...option })
    return res.data as unknown as T
  },
  postOriginal: async (option: any) => {
    const res = await request({ method: 'POST', ...option })
    return res
  },
  delete: async <T = any>(option: any) => {
    const res = await request({ method: 'DELETE', ...option })
    return res.data as unknown as T
  },
  put: async <T = any>(option: any) => {
    const res = await request({ method: 'PUT', ...option })
    return res.data as unknown as T
  },
  download: async <T = any>(option: any) => {
    const res = await request({ method: 'GET', responseType: 'blob', ...option })
    return res as unknown as Promise<T>
  },
  postDownload: async <T = any>(option: any) => {
    const res = await request({
      method: 'POST',
      responseType: 'blob',
      ...option
    })
    return res as unknown as Promise<T>
  },

  upload: async <T = any>(option: any) => {
    option.headersType = 'multipart/form-data'
    const res = await request({ method: 'POST', ...option })
    return res as unknown as Promise<T>
  }
}

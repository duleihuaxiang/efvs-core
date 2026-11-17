<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120"
      v-loading="formLoading"
    >
      <el-button type="primary" @click="exportTemplate" :loading="exportLoading" class="mb-5">
        <Icon icon="ep:download" class="mr-5px" /> 下载模板
      </el-button>
      <el-form-item label="航司" prop="airline">
        <el-input v-model="formData.airline" placeholder="请输入航司" />
      </el-form-item>
      <el-form-item label="efvs构型" prop="efvsConfiguration">
        <el-input v-model="formData.efvsConfiguration" placeholder="请输入efvs构型" />
      </el-form-item>
      <el-form-item label="机型" prop="model">
        <el-input v-model="formData.model" placeholder="请输入机型" />
      </el-form-item>
      <el-form-item label="文件来源" prop="fileSource">
        <el-input v-model="formData.fileSource" placeholder="请输入文件来源" />
      </el-form-item>
      <!-- 
      <el-form-item label="数据模版文件" prop="mappingFile">
        <UploadFile v-model="formData.mappingFile" :limit="1" :file-type="['xls', 'xlsx']" />
      </el-form-item> -->
      <el-form-item label="" prop="files">
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          :auto-upload="false"
          :limit="20"
          :accept="'.csv'"
          :on-change="handleFileChange"
          :on-error="handleUploadError"
          :on-exceed="handleExceed"
          :on-remove="handleFileRemove"
          :action="uploadUrl"
          :multiple="true"
          :headers="{ Authorization: 'Bearer ' + getAccessToken() }"
          :data="{ directory: 'upload' }"
          drag
        >
          <i class="el-icon-upload"><em>点击</em>上传性能数据文件，支持多个</i>
          <div class="el-upload__tip" style="color: #f56c6c"
            >仅支持格式为 csv 文件，文件大小不超过 100M</div
          >
          <!-- <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div> -->
          <template #file="row">
            <div class="flex items-center">
              <el-link
                :href="getFileUrl(row.file.url)"
                :underline="false"
                type="primary"
                @click.prevent="downloadFileByUrl(row.file.url, row.file.name)"
                class="ml-2"
                ><span class="file-name">{{ row.file.name }}</span>
                <Icon icon="ep:download" class="ml-1" />
              </el-link>
              <div class="ml-4">
                <el-button link type="danger" @click="handleFileRemove(row.file)"> 删除</el-button>
              </div>
            </div>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { QarApi, Qar } from '@/api/efvs/qar'
import download from '@/utils/download'
import type { UploadFile } from 'element-plus/es/components/upload/src/upload'
import { getAccessToken } from '@/utils/auth'
import * as UserApi from '@/api/system/user'

/** qar数据管理 表单 */
defineOptions({ name: 'QarForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  airline: undefined,
  model: undefined,
  efvsConfiguration: undefined,
  fileSource: undefined,
  // mappingFile: '',
  files: [] as Array<{ name: string; fileUrl: string; size: number }>,
  flag: 'Performance'
})
const formRules = reactive({
  airline: [{ required: true, message: '航司不能为空', trigger: 'blur' }],
  model: [{ required: true, message: '机型不能为空', trigger: 'blur' }],
  efvsConfiguration: [{ required: true, message: 'efvs构型不能为空', trigger: 'blur' }],
  fileSource: [{ required: true, message: '文件来源不能为空', trigger: 'blur' }]
  // mappingFile: [{ required: true, message: '请选择数据模版文件', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref
const uploadRef = ref() // 上传组件 Ref
const fileList = ref<UploadFile[]>([]) // 文件列表
/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await QarApi.getQar(id)

      // 回显附件
      fileList.value = [
        {
          uid: formData.value.id ?? Date.now(), // 使用唯一标识符
          id: formData.value.id,
          name: formData.value.name,
          url: formData.value.fileUrl ?? '',
          size: formData.value.size ?? 0,
          status: 'success',
          percentage: 100
        }
      ]

      formData.value.files = [
        {
          name: formData.value.name ?? '',
          fileUrl: formData.value.fileUrl ?? '',
          size: formData.value.size ?? 0
        }
      ]
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗
// 重新拼接文件地址：IP 和端口取自地址栏的 window.location.origin，其余地址保持不变
const getFileUrl = (url: string) => {
  try {
    const parsed = new URL(url)
    return `${window.location.origin}${parsed.pathname}${parsed.search}${parsed.hash}`
  } catch {
    return url
  }
}
/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  if (fileList.value.length === 0) {
    message.error('请上传性能数据文件！')
    return
  }
  if (formType.value === 'update' && fileList.value.length > 1) {
    message.error('编辑时,仅支持上传一个文件!')
    return false
  }

  // 提交请求
  formLoading.value = true
  try {
    try {
      const formDataFile = new FormData()
      formDataFile.append('directory', 'upload')
      fileList.value.forEach((file) => {
        if (file.raw) {
          formDataFile.append('file', file.raw as File)
        }
      })

      const hasFiles = fileList.value && fileList.value.length > 0
      const isNewMode = formType.value === 'create'
      const hasNewFiles =
        formType.value !== 'create' &&
        fileList.value.some((file) => file.status !== 'success' || !file.id)

      if (hasFiles && (isNewMode || hasNewFiles)) {
        const uploadFileData = await UserApi.uploadFile(formDataFile)
        if (uploadFileData) {
          formData.value.files = uploadFileData.data || []
        }
      }
    } catch (error: any) {
      console.error('文件上传失败:', error)
      return false
    }

    const { id, airline, model, efvsConfiguration, fileSource, files, flag } = formData.value
    // 构造提交数据对象，仅包含必要字段
    const data: Qar = {
      id,
      airline,
      model,
      efvsConfiguration,
      fileSource,
      files,
      flag
    }

    if (formType.value === 'create') {
      await QarApi.createQar(data)
      message.success(t('common.createSuccess'))
    } else {
      await QarApi.updateQar(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    airline: undefined,
    model: undefined,
    efvsConfiguration: undefined,
    fileSource: undefined,
    files: [],
    flag: 'Performance'
  }
  formRef.value?.resetFields()
  fileList.value = []
}

const deleteFileIds = ref([]) //编辑时删除的附件id

const uploadUrl = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/api/files/upload'

/** 处理文件选择 */
const handleFileChange = (uploadFile: UploadFile, files: UploadFile[]) => {
  // 验证文件类型
  if (uploadFile.raw) {
    let fileExtension = ''
    if (uploadFile.raw.name.lastIndexOf('.') > -1) {
      fileExtension = uploadFile.raw.name.slice(uploadFile.raw.name.lastIndexOf('.') + 1)
    }
    /*  const isImg = fileType.value.some((type: string) => {
      if (uploadFile.raw.type.indexOf(type) > -1) return true
      return !!(fileExtension && fileExtension.indexOf(type) > -1)
    }) */
    // const isZip = uploadFile.raw.name.endsWith('.zip')
    const isLt100M = uploadFile.raw.size / 1024 / 1024 < 100

    /*   if (!isImg) {
      message.error(`文件格式不正确, 请上传${fileType.value.join('/')}格式!`)
      uploadRef.value?.abort(uploadFile)
      uploadRef.value?.clearFiles()
      return false
    } */

    if (!isLt100M) {
      message.error('上传文件大小不能超过 100M!')
      uploadRef.value?.abort(uploadFile)
      uploadRef.value?.clearFiles()
      return false
    }
    fileList.value = files
  }
}

// 移除文件
const handleFileRemove = (params) => {
  if (params?.id) {
    // @ts-ignore
    deleteFileIds.value.push(params.id)
  }
  const index = fileList.value.map((f) => f.name).indexOf(params.name)

  if (index > -1) {
    fileList.value.splice(index, 1)
  }

  // selectedFile.value = null
}

/** 处理上传错误 */
const handleUploadError = () => {
  message.error('文件上传失败，请重新上传!')
}

/** 文件数超出提示 */
const handleExceed = () => {
  message.error('最多只能上传20个文件！')
}

// 文件下载
const downloadFileByUrl = (url?: string, filename?: string) => {
  if (!url || !filename) {
    message.warning('文件链接或名称缺失，无法下载')
    return
  }
  const link = document.createElement('a')
  link.href = getFileUrl(url)
  link.download = filename
  link.target = '_blank'
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
const exportLoading = ref(false)
const exportTemplate = async () => {
  try {
    // 设置导出加载状态
    exportLoading.value = true

    // 调用API导出
    const response = await QarApi.downloadTemplate()

    // 下载文件
    download.csv(response, 'QAR数据_模版文件.csv')

    // 提示成功
    message.success(`导出成功`)
  } catch (error) {
    console.error(`导出失败:`, error)
    message.error(`导出失败，请稍后重试`)
  } finally {
    exportLoading.value = false
  }
}
</script>

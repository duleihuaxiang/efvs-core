<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="700" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="培训主题" prop="trainingTheme">
            <el-input
              v-model="formData.trainingTheme"
              placeholder="请输入培训主题"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="培训时间" prop="trainingTime">
            <el-date-picker
              v-model="formData.trainingTime"
              type="date"
              format="YYYY-MM-DD"
              value-format="x"
              placeholder="选择培训时间"
              class="!w-280"
            /> </el-form-item
        ></el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :span="12"
          ><el-form-item label="培训地点" prop="trainingLocation">
            <el-input
              v-model="formData.trainingLocation"
              placeholder="请输入培训地点"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="培训时长" prop="trainingDuration">
            <el-input
              v-model="formData.trainingDuration"
              placeholder="请输入培训时长"
            /> </el-form-item
        ></el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="获得成绩" prop="achieveResults">
            <el-input
              v-model="formData.achieveResults"
              placeholder="请输入培训成绩"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="培训教师" prop="trainingTeachers">
            <el-input
              v-model="formData.trainingTeachers"
              placeholder="请输入培训教师"
            /> </el-form-item
        ></el-col>
      </el-row>

      <el-row>
        <el-col :span="24">
          <el-form-item label="获得证书" required>
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              v-model:file-list="fileList"
              :limit="5"
              :on-change="handleFileChange"
              :on-error="handleUploadError"
              :on-exceed="handleExceed"
              multiple
              drag
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              <template #tip>
                <div class="el-upload__tip" style="color: red"> 提示：上传文件不能大于1GB </div>
              </template>
              <template #file="row">
                <div class="flex items-center">
                  <span>{{ row.file.name }}</span>

                  <div class="ml-10px">
                    <el-button link type="danger" @click="handleFileRemove(row.file)">
                      删除</el-button
                    >
                  </div>
                </div>
              </template>
            </el-upload>

            <!-- 文件预览和下载 -->
            <div v-if="uploadedFile.url" class="file-preview mt-2">
              <el-card shadow="hover">
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <i class="el-icon-document" style="font-size: 24px; color: #409eff"></i>
                    <div class="ml-2">
                      <div>{{ uploadedFile.name }}</div>
                      <div class="text-sm text-gray-500">{{ uploadedFile.size }} bytes</div>
                    </div>
                  </div>
                  <div>
                    <el-button type="primary" link @click="downloadFile" :loading="downloading">
                      下载
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" :rows="3" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { ExperienceApi, Experience } from '@/api/training/trainingexperience'
import * as UserApi from '@/api/system/user'
import type { UploadFile } from 'element-plus/es/components/upload/src/upload'
import { FormRules } from 'element-plus'
import { ElDatePicker } from 'element-plus'

const userList = ref([]) // 人员列表数据
/** 培训经历 表单 */
defineOptions({ name: 'TrainForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  userId: undefined as number | undefined,
  trainingTheme: undefined,
  trainingTime: undefined,
  trainingLocation: undefined,
  trainingDuration: undefined,
  achieveResults: undefined,
  trainingTeachers: undefined,
  remark: '',
  fileUploadResults: []
})

const formRules = reactive<FormRules>({
  trainingTheme: [{ required: true, message: '培训主题必填！', trigger: 'blur' }],
  trainingTime: [{ required: true, message: '培训时间必填！', trigger: 'blur' }],
  trainingLocation: [{ required: true, message: '培训地点必填！', trigger: 'blur' }],
  trainingDuration: [{ required: true, message: '培训时长必填！', trigger: 'blur' }]
  // file: [{ required: true, message: '获得证书必传！', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const uploadRef = ref() // 上传组件 Ref
const fileList = ref<UploadFile[]>([]) // 文件列表
const selectedFile = ref<File | null>(null) // 选中的文件
const uploadedFile = ref({
  name: '',
  url: '',
  size: 0
}) // 已上传的文件信息
const downloading = ref(false) // 下载状态
/* const fileType = ref(['doc', 'xls', 'ppt', 'txt', 'pdf', 'zip'])
const uploadUrl =
  import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/api/files/uploads' */
const experienceIds = ref([])
// const fileIds = ref([])
const deleteFileIds = ref([]) //编辑时删除的附件id

// 定义禁用分钟的函数：返回需要禁用的分钟数组
const handleDisabledMinutes = () => {
  const disabled = []
  // 禁用所有不能被5整除的分钟
  for (let i = 0; i < 60; i++) {
    if (i % 5 !== 0) {
      disabled.push(i)
    }
  }
  return disabled
}
/** 打开弹窗 */
const open = async (type: string, id?: number, userId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  formData.value.userId = userId || undefined
  // @ts-ignore
  userList.value = await UserApi.getSimpleUserList()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ExperienceApi.getExperience(id)

      const convertToUploadFileArray = (fileList: any[]): UploadFile[] => {
        return (fileList || []).map((attachment) => ({
          uid: attachment.id ?? Date.now(), // 使用唯一标识符
          id: attachment.id,
          name: attachment.fileName ?? attachment.name,
          url: attachment.fileUrl ?? '',
          size: attachment.fileSize ?? 0,
          status: 'success',
          percentage: 100
          // raw: attachment.file || new File([], attachment.fileName || 'unknown')
        }))
      }

      // 回显附件
      fileList.value = convertToUploadFileArray(formData.value.fileList) || []
    } finally {
      formLoading.value = false
    }
  }
}

/** 处理文件选择 */
const handleFileChange = async (uploadFile: UploadFile, files: UploadFile[]) => {
  // 验证文件类型
  if (uploadFile.raw) {
    // const isZip = uploadFile.raw.name.endsWith('.zip')
    let fileExtension = ''
    if (uploadFile.raw.name.lastIndexOf('.') > -1) {
      fileExtension = uploadFile.raw.name.slice(uploadFile.raw.name.lastIndexOf('.') + 1)
    }
    if (uploadFile.raw.isDirectory) {
      message.error('不允许上传文件夹！')
      uploadRef.value?.abort(uploadFile)
      uploadRef.value?.clearFiles()
      return false
    }
    /* const isImg = fileType.value.some((type: string) => {
      if (uploadFile.raw.type.indexOf(type) > -1) return true
      return !!(fileExtension && fileExtension.indexOf(type) > -1)
    }) */
    const isLt100M = uploadFile.raw.size / 1024 / 1024 < 1024

    /*  if (!isImg) {
      message.error(`文件格式不正确, 请上传${fileType.value.join('/')}格式!`)
      uploadRef.value?.abort(uploadFile)
      uploadRef.value?.clearFiles()
      return false
    } */

    if (!isLt100M) {
      message.error('上传文件大小不能超过 1GB!')
      uploadRef.value?.abort(uploadFile)
      uploadRef.value?.clearFiles()
      return false
    }

    selectedFile.value = uploadFile.raw
    fileList.value = files
  }
}

/** 处理文件移除 */
const handleFileRemove = (params) => {
  // @ts-ignore
  deleteFileIds.value.push(params?.id)
  const index = fileList.value.map((f) => f.name).indexOf(params.name)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  selectedFile.value = null
}

/** 处理上传错误 */
const handleUploadError = () => {
  message.error('文件上传失败，请重新上传!')
}

/** 文件数超出提示 */
const handleExceed = () => {
  message.error('最多只能上传一个文件！')
}

/** 下载文件 */
const downloadFile = async () => {
  if (!uploadedFile.value.url) return

  downloading.value = true
  try {
    // 这里应该调用实际的下载API
    // 示例代码：
    // const response = await fetch(uploadedFile.value.url)
    // const blob = await response.blob()
    // const url = window.URL.createObjectURL(blob)
    // const link = document.createElement('a')
    // link.href = url
    // link.download = uploadedFile.value.name
    // link.click()
    // window.URL.revokeObjectURL(url)
    message.info('下载功能待实现')
  } catch (error) {
    message.error('下载失败')
  } finally {
    downloading.value = false
  }
}

defineExpose({ open, experienceIds }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()

  if (fileList.value.length <= 0) {
    message.error('请上传获得证书文件！')
    return
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

      // 判断是否需要上传文件（新增模式或编辑模式下有新文件）
      const hasFiles = fileList.value && fileList.value.length > 0
      const isNewMode = formType.value === 'create'
      const hasNewFiles =
        formType.value !== 'create' &&
        fileList.value.some((file) => file.status !== 'success' || !file.id)

      if (hasFiles && (isNewMode || hasNewFiles)) {
        const uploadFileData = await UserApi.uploadFile(formDataFile)
        if (uploadFileData) {
          formData.value.fileUploadResults = uploadFileData.data || []
        }
      }
    } catch (error: any) {
      message.error(`文件上传失败:${error}`)
      return false
    }
    const data = formData.value as unknown as Experience

    if (formType.value === 'create') {
      const createExperienceResponse = await ExperienceApi.createExperience(data)
      if (createExperienceResponse) {
        // @ts-ignore
        experienceIds.value.push(createExperienceResponse[0])
      }
      message.success(t('common.createSuccess'))
    } else {
      // 删除文件
      await UserApi.deleteFile(deleteFileIds.value)
      await ExperienceApi.updateExperience(data)
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
    userId: undefined,
    trainingTheme: undefined,
    trainingTime: undefined,
    trainingLocation: undefined,
    trainingDuration: undefined,
    achieveResults: undefined,
    trainingTeachers: undefined,
    remark: '',
    fileUploadResults: []
  }
  formRef.value?.resetFields()
  // 重置文件上传相关状态
  selectedFile.value = null
  fileList.value = []
  uploadedFile.value = {
    name: '',
    url: '',
    size: 0
  }
  uploadRef.value?.clearFiles()
}
</script>
<style scoped lang="scss"></style>

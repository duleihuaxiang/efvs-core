<template>
  <el-form
    ref="signAttendanceRef"
    :model="signAttendanceTableData"
    :rules="rules"
    label-width="120px"
    :disabled="signAttendanceDisabled"
  >
    <el-form-item label="签到表附件：" prop="signSheetFile" required>
      <UploadFile
        v-model="signSheetFile"
        :file-size="20"
        :file-type="['png', 'jpg', 'jpeg', 'webp', 'gif']"
      />
    </el-form-item>
    <el-form-item>
      <el-button @click="handleDownloadTemplate" plain type="primary">
        <Icon icon="ep:download" class="mr-5px" /> 下载模版
      </el-button>

      <el-button @click="handleImport" plain type="primary">
        <Icon icon="ep:upload" class="mr-5px" /> 导入
      </el-button>

      <input
        ref="fileInputRef"
        type="file"
        accept=".xlsx"
        style="display: none"
        @change="onFileSelected"
      />
      <span class="import-tip">
        <Icon icon="ep:warning" class="mr-5px" />
        导入文件不可超过200M
      </span>
    </el-form-item>
    <el-form-item>
      <el-table
        border
        ref="tableBox"
        :data="signAttendanceTableData"
        :row-style="{ height: '70px' }"
        :cell-style="{ padding: '0px' }"
        class="mb-4"
      >
        <el-table-column type="index" width="55" align="center" />
        <el-table-column prop="name" label="姓名" align="center" min-width="150">
          <template #default="scope">
            <el-form-item>
              <el-input
                v-model="scope.row.name"
                maxlength="200"
                oninput="if(value.length > 200) value=value.slice(0, 200)"
                placeholder="请输入"
              />
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" align="center" min-width="220">
          <template #default="scope">
            <el-form-item>
              <el-date-picker
                v-model="scope.row.checkInTime"
                type="datetime"
                placeholder="请选择签到时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="!w-240"
              />
            </el-form-item>
          </template> </el-table-column
        ><el-table-column prop="learningTime" label="学习时间(H)" align="center" min-width="120">
          <template #default="scope">
            <el-form-item>
              <el-input-number
                v-model="scope.row.learningTime"
                :min="0"
                :step="1"
                controls-position="right"
                class="w-full"
                placeholder="请输入"
              />
            </el-form-item>
          </template> </el-table-column
        ><el-table-column prop="isCompleted" label="是否完成学习" align="center" min-width="150">
          <template #default="scope">
            <el-form-item>
              <el-select v-model="scope.row.isCompleted" class="w-full" clearable>
                <el-option label="是" :value="0" />
                <el-option label="否" :value="1" />
              </el-select>
            </el-form-item>
          </template> </el-table-column
        ><el-table-column prop="completedForm" label="完成形式" align="center" min-width="150">
          <template #default="scope">
            <el-form-item>
              <el-select v-model="scope.row.completedForm" clearable>
                <el-option
                  v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_TYPE)"
                  :key="dict.value"
                  :label="dict.label"
                  :value="Number(dict.value)"
                />
              </el-select>
            </el-form-item>
          </template> </el-table-column
        ><el-table-column prop="score" label="成绩" align="center" min-width="120">
          <template #default="scope">
            <el-form-item>
              <el-input-number
                v-model="scope.row.score"
                :min="0"
                :step="1"
                :precision="0"
                controls-position="right"
                class="w-full"
                placeholder="请输入"
              />
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column prop="fileUploadResults" label="获得证书" align="left" min-width="280">
          <template #default="scope">
            <el-form-item>
              <el-upload
                ref="uploadRef"
                v-model:file-list="scope.row.fileUploadResults"
                :auto-upload="false"
                :limit="3"
                :on-change="handleFileChange"
                :on-error="handleUploadError"
                :on-exceed="handleExceed"
                :on-remove="handleFileRemove"
                :action="uploadUrl"
                :headers="{ Authorization: 'Bearer ' + getAccessToken() }"
                :data="{ directory: 'upload' }"
                list-type="text"
                class="upload-container"
              >
                <el-button size="small">点击上传</el-button>
                <!--   <template #tip>
                  <div class="el-upload__tip" style="color: red"> 提示：上传文件不得超过1GB </div>
                </template> -->
                <template #file="row">
                  <div>
                    <el-link
                      :href="row.file.url"
                      :underline="false"
                      type="primary"
                      @click.prevent="downloadFileByUrl(row.file.url, row.file.name)"
                      class="ml-2"
                      ><span class="file-name">{{ row.file.name }}</span>
                      <Icon icon="ep:download" class="ml-1" />
                    </el-link>
                    <div>
                      <el-button
                        link
                        type="danger"
                        @click="handleFileRemove(row.file, scope.row.fileUploadResults)"
                        v-if="!signAttendanceDisabled"
                      >
                        删除</el-button
                      >
                    </div>
                  </div>
                </template>
              </el-upload>
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column prop="instructor" label="教员" align="center" min-width="150">
          <template #default="scope">
            <el-form-item>
              <el-input
                v-model="scope.row.instructor"
                maxlength="200"
                oninput="if(value.length > 200) value=value.slice(0, 200)"
                placeholder="请输入"
                disabled
              />
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column prop="completionDate" label="完成日期" align="center" min-width="200">
          <template #default="scope">
            <el-form-item>
              <el-date-picker
                v-model="scope.row.completionDate"
                type="date"
                placeholder="请选择完成日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="170" align="center">
          <template #default="scope">
            <el-button size="small" @click="signAttendanceAddRow(scope.$index)">
              <Icon icon="ep:plus"
            /></el-button>
            <el-button size="small" @click="signAttendanceCopyRow(scope.$index)">
              <Icon icon="ep:copy-document"
            /></el-button>

            <el-button
              type="primary"
              size="small"
              @click="signAttendanceRemoveRow(scope.$index)"
              :disabled="signAttendanceTableData.length <= 1"
            >
              <Icon icon="ep:delete" />
            </el-button>
          </template>
        </el-table-column> </el-table
    ></el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import * as FormApi from '@/api/bpm/form'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import type { UploadFile } from 'element-plus/es/components/upload/src/upload'
import { getAccessToken } from '@/utils/auth'
import download from '@/utils/download'
import { stringifyQuery } from 'vue-router'

// 表单数据模型
interface signAttendanceTableData {
  traningAttachments: string
  name: string // 姓名
  checkInTime: string // 签到时间
  learningTime: string // 学习时间(H)
  isCompleted: string // 是否完成学习
  completedForm: string // 完成形式
  score: string // 成绩
  fileUploadResults: UploadFile[] // 获得证书
  fileUploadId: string[] // 获得证书id
  instructor: string // 教员
  completionDate: string // 完成日期
  processInstanceId: string
}

const props = defineProps({
  signAttendanceDisabled: { type: Boolean, default: false, required: true },
  processInstanceId: { type: String, default: '', required: false },
  projectLeader: {
    type: [String, Number],
    default: null
  },
  projectLeaderNickName: {
    type: String,
    default: ''
  }
})

const message = useMessage()
const fileInputRef = ref<HTMLInputElement | null>(null)

// 初始化表单数据
const signAttendanceTableData = ref<signAttendanceTableData[]>([
  {
    traningAttachments: '',
    name: '',
    checkInTime: '',
    learningTime: '',
    isCompleted: '',
    completedForm: '',
    score: '',
    fileUploadResults: [],
    fileUploadId: [],
    instructor: '',
    completionDate: '',
    processInstanceId: props.processInstanceId
  }
])

// 表单校验规则
const rules = reactive({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
})
const signSheetFile = ref([])
const uploadRef = ref() // 上传组件 Ref

const fileList = ref<UploadFile[]>([]) // 文件列表

const selectedFile = ref<File | null>(null) // 选中的文件
const uploadedFile = ref({
  name: '',
  url: '',
  size: 0
}) // 已上传的文件信息

const deleteFileIds = ref([]) //编辑时删除的附件id

const uploadUrl = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/api/files/upload'

/* 下载模板 */
const handleDownloadTemplate = async () => {
  try {
    const blob = await FormApi.getTrainingAttendanceAssessmentList()
    download.excel(blob as unknown as Blob, '签到模版.xlsx')
    message.success('模板下载成功')
  } catch {
    message.error('模板下载失败')
  }
}

const handleImport = () => {
  fileInputRef.value?.click()
}

const onFileSelected = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  // 检查文件大小（最大 200M）
  const maxSize = 200 * 1024 * 1024 // 200MB
  if (file.size > maxSize) {
    message.error('文件大小超过限制，最大支持 200M')
    input.value = ''
    return
  }
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在导入数据，请稍后...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await FormApi.importTrainingSignAttendanceExcel(formData)
    // 关闭加载遮罩
    loadingInstance.close()
    signAttendanceTableData.value = data
    updateInstructor(props.projectLeaderNickName)
    signAttendanceTableData.value.forEach((row) => {
      row.processInstanceId = props.processInstanceId
      row.traningAttachments = [...signSheetFile.value].join(',')
    })

    message.success('导入成功')
  } catch (error) {
    // 关闭加载遮罩
    loadingInstance.close()
    message.error('导入失败')
    console.error('导入错误:', error)
  } finally {
    // 清空文件输入
    input.value = ''
  }
}

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
    const isLt100M = uploadFile.raw.size / 1024 / 1024 < 1024

    /*   if (!isImg) {
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

// 移除文件
const handleFileRemove = (params, list) => {
  if (params?.id) {
    // @ts-ignore
    deleteFileIds.value.push(params.id)
  }
  const index = list.map((f) => f.name).indexOf(params.name)

  if (index > -1) {
    list.splice(index, 1)
  }

  // selectedFile.value = null
}

/** 处理上传错误 */
const handleUploadError = () => {
  message.error('文件上传失败，请重新上传!')
}

/** 文件数超出提示 */
const handleExceed = () => {
  message.error('最多只能上传三个文件！')
}

// 文件下载
const downloadFileByUrl = async (url?: string, filename?: string) => {
  if (!url || !filename) {
    message.warning('文件链接或名称缺失，无法下载')
    return
  }
  try {
    // console.log('开始下载文件:', { url, filename })

    // 检查文件类型
    const fileExtension = filename.split('.').pop()?.toLowerCase()

    // 对于图片文件，使用fetch方法获取blob并下载
    if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(fileExtension || '')) {
      // console.log('处理图片文件下载')

      // 使用fetch获取文件
      const response = await fetch(url)
      const blob = await response.blob()

      // 创建Blob URL
      const blobUrl = URL.createObjectURL(blob)

      // 创建下载链接
      const link = document.createElement('a')
      link.href = blobUrl
      link.download = filename
      link.style.display = 'none'
      document.body.appendChild(link)

      // 模拟点击
      link.click()

      // 清理
      setTimeout(() => {
        document.body.removeChild(link)
        URL.revokeObjectURL(blobUrl)
        // console.log('图片下载完成')
      }, 100)
    } else {
      // 非图片文件使用常规下载方法
      // console.log('处理非图片文件下载')
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()

      setTimeout(() => {
        document.body.removeChild(link)
        // console.log('文件下载完成')
      }, 100)
    }
  } catch (error) {
    console.error('下载失败:', error)
    message.error('文件下载失败')
  }
}

// 表单实例引用
const signAttendanceRef = ref<FormInstance>()
// 添加新行
const signAttendanceAddRow = (index: number) => {
  const newRow = {
    traningAttachments: [...signSheetFile.value].join(','),
    name: '',
    checkInTime: '',
    learningTime: '',
    isCompleted: '',
    completedForm: '',
    score: '',
    fileUploadResults: [],
    fileUploadId: [],
    instructor: props.projectLeaderNickName,
    completionDate: '',
    processInstanceId: props.processInstanceId
  }
  signAttendanceTableData.value.splice(index + 1, 0, newRow)
}

// 删除指定行
const signAttendanceRemoveRow = (index: number) => {
  if (signAttendanceTableData.value.length > 1) {
    signAttendanceTableData.value.splice(index, 1)
  }
}

// 拷贝指定行
const signAttendanceCopyRow = (index: number) => {
  const currentRow = signAttendanceTableData.value[index]
  // 创建新行，除了姓名其他字段都拷贝
  const newRow = {
    traningAttachments: [...signSheetFile.value].join(','),
    name: '', // 姓名设为空
    checkInTime: currentRow.checkInTime,
    learningTime: currentRow.learningTime,
    isCompleted: currentRow.isCompleted,
    completedForm: currentRow.completedForm,
    score: currentRow.score,
    fileUploadResults: [...currentRow.fileUploadResults],
    fileUploadId: JSON.parse(JSON.stringify(currentRow.fileUploadId)),
    instructor: currentRow.instructor,
    completionDate: currentRow.completionDate,
    processInstanceId: props.processInstanceId
  }

  // 在当前行的下一行插入
  signAttendanceTableData.value.splice(index + 1, 0, newRow)
}

/* const syncSignSheetFileToTable = () => {
  // 将签到表附件的值同步到表格数据的每一行
  signAttendanceTableData.value = signAttendanceTableData.value.map((row) => ({
    ...row,
    signSheetFile: [...signSheetFile.value]
  }))
} */
// 监听签到表附件变化，自动同步到表格数据
watch(
  signSheetFile,
  (newVal) => {
    // 将签到表附件的新值同步到表格数据的每一行
    if (signAttendanceTableData.value && signAttendanceTableData.value.length > 0) {
      signAttendanceTableData.value = signAttendanceTableData.value.map((row) => ({
        ...row,
        traningAttachments: Array.isArray(newVal) ? [...newVal].join(',') : newVal
      }))
    }
  },
  { deep: true }
)
/** 表单校验 */
const validateSignAttendance = async () => {
  // 先同步签到表附件数据
  // syncSignSheetFileToTable()

  /*  const uncompletedItems = signAttendanceTableData.value.filter((item) => !item.name)
  if (uncompletedItems.length > 0) {
    throw new Error('请完成所有姓名')
  } */

  // 检查是否有未填写的  签到表附件
  const emptySignSheetFile = signAttendanceTableData.value.filter(
    (item) => !item.traningAttachments || item.traningAttachments.length === 0
  )
  if (emptySignSheetFile.length > 0) {
    throw new Error('请上传签到表附件')
  }

  // await signAttendanceRef.value?.validate()
}

// 更新教员字段
const updateInstructor = (leaderId) => {
  // 假设您的表格数据在某个响应式变量中，例如 signAttendanceTableData
  // 更新表格中所有行的instructor字段
  signAttendanceTableData.value.forEach((row) => {
    row.instructor = leaderId
  })
}
onMounted(async () => {
  // if (props.signAttendanceDisabled) {
  const getTrainingAttendanceAssessmentData = await FormApi.getTrainingAttendanceAssessment({
    processInstanceId: props.processInstanceId
  })
  if (!getTrainingAttendanceAssessmentData || getTrainingAttendanceAssessmentData.length === 0) {
    // message.error('查询不到培训签到考核证书发放表单详情信息！')
    return
  }
  // 转换文件数据格式以适配 Upload 组件
  const convertedData = getTrainingAttendanceAssessmentData.map((item) => ({
    ...item,
    fileUploadResults:
      item.fileUploadResults?.map((file) => ({
        name: file.name || file.fileName || file.originalFilename || '未知文件',
        url: file.url || file.fileUrl || file.accessUrl,
        uid: file.uid || file.id || Date.now() + Math.random(),
        id: file.id,
        size: file.size,
        status: 'success' // 设置文件上传状态为成功
      })) || []
  }))
  signAttendanceTableData.value = convertedData
  signSheetFile.value = getTrainingAttendanceAssessmentData[0].traningAttachments
  // }
})

defineExpose({
  variablesData: {
    list: signAttendanceTableData,
    formType: 'TrainingAttendanceAssessment'
  },
  validateSignAttendance,
  updateInstructor
})
</script>
<style scoped>
.file-name {
  overflow: hidden;
  font-size: 12px;
  color: #606266;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 调整文件列表的间距 */
.el-upload--text .el-upload-list {
  padding: 0 !important;
  margin: 0 !important;
}

.el-upload-list__item {
  padding: 4px 0;
  border-bottom: 1px solid #ebeef5;
}

.el-upload-list__item-name {
  font-size: 12px;
  color: #606266;
}

.el-upload__button {
  margin: 0 !important;
}

.import-tip {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
  font-size: 12px;
  color: #f56c6c;

  .el-icon {
    margin-right: 5px;
  }
}
</style>

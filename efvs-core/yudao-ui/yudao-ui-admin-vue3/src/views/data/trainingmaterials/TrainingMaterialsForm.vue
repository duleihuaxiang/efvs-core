<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="资料名称" prop="documentName">
        <el-input v-model="formData.documentName" placeholder="请输入资料名称" />
      </el-form-item>
      <el-form-item label="所属目录" prop="containingDirectory">
        <el-tree-select
          :data="treeData"
          :props="{ label: 'name', children: 'children' }"
          node-key="name"
          default-expandedKeys="[1]"
          v-model="formData.containingDirectory"
          :default-expand-all="true"
        />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="formData.category" placeholder="请选择分类">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_MATERIALS_CLASSIFICATION)"
            :key="dict.value"
            :label="dict.label"
            :value="Number(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="大小(M)" prop="size">
        <el-input v-model="formData.size" disabled />
      </el-form-item>
      <!-- 新增文件上传功能 -->
      <el-form-item label="上传附件" prop="file">
        <el-upload
          ref="uploadRef"
          v-model:file-list="fileList"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :on-error="handleUploadError"
          :on-exceed="handleExceed"
          :on-remove="handleFileRemove"
          :action="uploadUrl"
          :headers="{ Authorization: 'Bearer ' + getAccessToken() }"
          drag
          :data="{ directory: 'upload' }"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip" style="color: red"> 提示：上传文件不得超过1GB </div>
          </template>
          <template #file="row">
            <div class="flex items-center">
              <span>{{ row.file.name }}</span>

              <div class="ml-10px">
                <el-button link type="danger" @click="handleFileRemove(row.file)"> 删除</el-button>
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
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { TrainingMaterialsApi, TrainingMaterials } from '@/api/data/trainingmaterials'
import { getSimpleDeptList } from '@/api/data/docDirectory'
import * as UserApi from '@/api/system/user'
import type { UploadFile } from 'element-plus/es/components/upload/src/upload'
// import { useUpload } from '@/components/UploadFile/src/useUpload'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { getAccessToken } from '@/utils/auth'

/** 培训资料管理 表单 */
defineOptions({ name: 'TrainingMaterialsForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 扩展UploadFile类型，支持自定义id属性
interface CustomUploadFile extends UploadFile {
  id?: number | string
}
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  // trainingId: undefined,
  documentName: '',
  containingDirectory: undefined,
  category: undefined as number | undefined, // 明确指定类型
  size: undefined as number | undefined,
  remark: undefined,
  fileUploadResults: []
})
const formRules = reactive({
  // trainingId: [{ required: true, message: '培训资料表ID不能为空', trigger: 'blur' }],
  documentName: [{ required: true, message: '资料名称不能为空', trigger: 'blur' }],
  containingDirectory: [{ required: true, message: '所属目录不能为空', trigger: 'blur' }],
  category: [{ required: true, message: '分类不能为空', trigger: 'blur' }],
  size: [{ required: true, message: '大小(M)不能为空', trigger: 'blur' }]
})
// const fileType = ref(['doc', 'xls', 'ppt', 'txt', 'pdf', 'zip'])
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
const deleteFileIds = ref([]) //编辑时删除的附件id
const uploadUrl = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/api/files/upload'
/** 打开弹窗 */
const open = async (type: string, id?: number, query?: any) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const result = await TrainingMaterialsApi.getTrainingMaterials(id)
      formData.value = {
        ...result,
        category: result.category !== undefined ? Number(result.category) : undefined
      }
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
  if (query) {
    formData.value.containingDirectory = query.name
  }
  // 在表单打开时触发：重新加载树形数据
  await refreshTreeData()
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗
// 所属目录树形数据
const treeData = ref<any[]>([])
onMounted(async () => {
  try {
    const data = await getSimpleDeptList()
    // 过滤掉"导入数据管理"及其子节点
    const filteredData = filterTree(data, '导入数据管理')
    treeData.value = filteredData
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
})
/** 刷新目录树数据 */
const refreshTreeData = async () => {
  try {
    const data = await getSimpleDeptList()
    const filteredData = filterTree(data, '导入数据管理')
    treeData.value = filteredData
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
}

/** 过滤树形数据，移除指定名称的节点及其子节点 */
const filterTree = (data: any[], excludeName: string): any[] => {
  return data.filter((item) => {
    // 如果当前节点不是要排除的节点，则保留
    if (item.name !== excludeName) {
      // 递归处理子节点
      if (item.children && item.children.length > 0) {
        item.children = filterTree(item.children, excludeName)
      }
      return true
    }
    // 如果是需要排除的节点，则返回false，表示不包含该节点
    return false
  })
}

/** 处理文件选择 */
const handleFileChange = (uploadFile: UploadFile, files: UploadFile[]) => {
  // 验证文件类型
  if (uploadFile.raw) {
    console.log('upload', uploadFile)

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

    // 同步文件名到资料名称字段
    if (fileList.value.length) {
      const fileNameWithoutExt = fileList.value[0]?.name.replace(/\.zip$/i, '') || ''
      formData.value.documentName = fileNameWithoutExt
    }

    // 同步文件大小到大小字段
    if (fileList.value.length) {
      let totalSize = 0
      for (let i = 0; i < fileList.value.length; i++) {
        if (fileList.value[i]?.size) {
          totalSize += Number(fileList.value[i]?.size)
        }
      }
      const fileSizeInMB = (totalSize / (1024 * 1024)).toFixed(2)
      formData.value.size = parseFloat(fileSizeInMB)
    }
  }
}

// 移除文件
const handleFileRemove = async (params) => {
  if (params?.id) {
    await message.delConfirm()
    // @ts-ignore
    deleteFileIds.value.push(params.id)
  }
  const index = fileList.value.map((f) => f.name).indexOf(params.name)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
  formData.value.size = 0
  selectedFile.value = null

  /*   if (remainingFiles.length) {
    let totalSize = 0
    for (let i = 0; i < remainingFiles.length; i++) {
      if (remainingFiles[i]?.size) {
        totalSize += Number(remainingFiles[i]?.size)
      }
    }
    const fileSizeInMB = (totalSize / (1024 * 1024)).toFixed(2)
    formData.value.size = parseFloat(fileSizeInMB)
  } */
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
/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  if (fileList.value.length <= 0) {
    message.error('请上传附件！')
    return
  }

  // 输出文件信息到控制台
  if (selectedFile.value) {
    console.log('准备上传的文件:', {
      name: selectedFile.value.name,
      size: selectedFile.value.size,
      type: selectedFile.value.type
    })
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
          formData.value.fileUploadResults = uploadFileData.data || []
        }
      }
    } catch (error: any) {
      console.error('文件上传失败:', error)
      return false
    }
    const data = formData.value as unknown as TrainingMaterials
    if (formType.value === 'create') {
      await TrainingMaterialsApi.createTrainingMaterials(data)

      message.success(t('common.createSuccess'))
    } else {
      // 删除文件
      await UserApi.deleteFile(deleteFileIds.value)
      await TrainingMaterialsApi.updateTrainingMaterials(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } catch (error) {
    console.error(error)
    message.error('操作失败')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    // trainingId: undefined,
    documentName: '',
    containingDirectory: undefined,
    category: undefined,
    size: undefined,
    remark: undefined,
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
<style scoped>
.file-preview {
  width: 100%;
}

.mt-2 {
  margin-top: 0.5rem;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.justify-between {
  justify-content: space-between;
}

.ml-2 {
  margin-left: 0.5rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-gray-500 {
  color: #9ca3af;
}
</style>

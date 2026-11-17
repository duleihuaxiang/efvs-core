<template>
  <el-row class="h-screen">
    <!-- 左侧部门树 -->
    <el-col :span="4" :xs="24" class="h-screen overflow-hidden">
      <ContentWrap class="h-full flex flex-col">
        <DocTree @click="handleDeptNodeClick" class="flex-1 overflow-auto" />
      </ContentWrap>
    </el-col>
    <!-- 右侧内容区（红框区域） -->
    <el-col :span="20" :xs="24" class="h-screen overflow-hidden">
      <ContentWrap class="h-full flex flex-col">
        <!-- 搜索工作栏 -->
        <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="100px"
        >
          <!-- <el-form-item label="培训资料表ID" prop="trainingId">
            <el-input
              v-model="queryParams.trainingId"
              placeholder="请输入培训资料表ID"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item> -->
          <el-form-item label="资料名称" prop="documentName">
            <el-input
              v-model="queryParams.documentName"
              placeholder="请输入资料名称"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="所属目录" prop="containingDirectory">
            <el-input
              v-model="queryParams.containingDirectory"
              placeholder="请输入所属目录"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="queryParams.category" placeholder="请选择分类" class="!w-240px">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_MATERIALS_CLASSIFICATION)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery"
              ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button
            >
            <el-button @click="resetQuery"
              ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button
            >
            <el-button
              type="primary"
              plain
              @click="openForm('create', undefined, queryContainingDirectory)"
              v-hasPermi="['data:training-materials:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
            <el-button
              type="success"
              plain
              @click="handleExport"
              :loading="exportLoading"
              v-hasPermi="['data:training-materials:export']"
            >
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
            <el-button
              type="danger"
              plain
              :disabled="isEmpty(checkedIds)"
              @click="handleDeleteBatch"
              v-hasPermi="['data:training-materials:delete']"
            >
              <Icon icon="ep:delete" class="mr-5px" /> 批量删除
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 列表和分页 -->
        <div class="flex-1 overflow-auto">
          <el-table
            row-key="id"
            v-loading="loading"
            :data="list"
            :stripe="true"
            @selection-change="handleRowCheckboxChange"
            class="h-full"
          >
            <el-table-column type="selection" width="55" />
            <!-- <el-table-column label="ID" align="center" prop="id" /> -->
            <!-- <el-table-column label="培训资料表ID" align="center" prop="trainingId" /> -->
            <el-table-column label="资料名称" align="left" prop="documentName" />
            <el-table-column label="所属目录" align="center" prop="containingDirectory" />
            <el-table-column label="分类" prop="category" width="100">
              <template #default="{ row }">
                <dict-tag
                  :type="DICT_TYPE.TRAINING_MATERIALS_CLASSIFICATION"
                  :value="row.category"
                />
              </template>
            </el-table-column>
            <el-table-column label="大小(M)" align="center" prop="size" />
            <el-table-column label="文件内容" align="center" prop="url" min-width="180">
              <template #default="{ row }">
                <!--  <el-image
                v-if="row.type.includes('image')"
                class="h-80px w-80px"
                lazy
                :src="row.url"
                :preview-src-list="[row.url]"
                preview-teleported
                fit="cover"
              />
              <el-link
                v-else-if="row.type.includes('pdf')"
                type="primary"
                :href="row.url"
                :underline="false"
                target="_blank"
                >预览</el-link
              > -->
                <div class="flex flex-col">
                  <el-link
                    :download="file.fileName"
                    v-for="file in row.fileList"
                    :key="file.id"
                    :href="getFileUrl(file.fileUrl)"
                    :underline="false"
                    class="file-name-link"
                    @click.prevent="handleFileDownload(file)"
                    >{{ file.fileName }}<Icon icon="ep:download" class="ml-5px" />
                  </el-link>
                </div>
              </template>
            </el-table-column>
            <!-- <el-table-column label="备注" align="center" prop="remark" /> -->
            <el-table-column label="操作" align="center" width="130">
              <template #default="scope">
                <!-- <div class="flex items-center space-x-1 h-full"> -->
                <div class="flex items-center justify-center space-x-0 h-full">
                  <!-- 仅压缩包展示预览 zip rar 7z-->
                  <!-- <el-link
                    type="primary"
                    :href="scope.row.linkUrl"
                    v-if="scope.row.showPreview"
                    target="_blank"
                    :underline="false"
                  >
                    预览
                  </el-link> -->
                  <el-button
                    link
                    type="primary"
                    @click="openPreview(scope.row)"
                    v-if="scope.row.showPreview"
                  >
                    预览
                  </el-button>
                  <el-button
                    link
                    type="primary"
                    @click="openForm('update', scope.row.id)"
                    v-hasPermi="['data:training-materials:update']"
                  >
                    编辑
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    @click="handleDelete(scope.row.id)"
                    v-hasPermi="['data:training-materials:delete']"
                  >
                    删除
                  </el-button></div
                >
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页 -->
          <Pagination
            :total="total"
            v-model:page="queryParams.pageNo"
            v-model:limit="queryParams.pageSize"
            @pagination="getList"
            class="p-4"
          />
        </div>
      </ContentWrap>
    </el-col>
  </el-row>

  <!-- 表单弹窗：添加/修改 -->
  <TrainingMaterialsForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
const router = useRouter()
// 原有逻辑代码保持不变
import { isEmpty } from '@/utils/is'
import download from '@/utils/download'
import { TrainingMaterialsApi, TrainingMaterials } from '@/api/data/trainingmaterials'
import TrainingMaterialsForm from './TrainingMaterialsForm.vue'
import DocTree from '@/views/data/trainingmaterials/DocTree.vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

defineOptions({ name: 'TrainingMaterials' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref<TrainingMaterials[]>([])
const total = ref(0)

const fileType = ref(['zip', 'rar', '7z'])

// 新增时的默认所属目录
const queryContainingDirectory = ref({
  id: undefined,
  name: ''
})
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  // trainingId: undefined,
  documentName: undefined,
  containingDirectory: undefined,
  category: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)

const getList = async () => {
  loading.value = true
  try {
    const data = await TrainingMaterialsApi.getTrainingMaterialsPage(queryParams)
    list.value = data.list
    total.value = data.total
    list.value.forEach((item) => {
      // @ts-ignore
      if (item?.fileList.length > 0) {
        // @ts-ignore
        let url = item.fileList[0]?.filePreviewPath
        if (url) {
          // @ts-ignore
          item.showPreview = fileType.value.includes(item.fileList[0]?.fileName.split('.').at(-1))

          // 历史 Windows 数据使用反斜杠，麒麟/Linux 使用正斜杠；预览 URL 统一使用正斜杠。
          const normalizedPath = url.replace(/\\/g, '/')
          const targetSegment = 'filePreviewPath/'
          const startIndex = normalizedPath.indexOf(targetSegment)
          if (startIndex >= 0) {
            url = normalizedPath.slice(startIndex + targetSegment.length)
          }
        }
        // console.log('origin', window.location.origin)

        // @ts-ignore
        item.linkUrl = `${window.location.origin}/preview/${url}/${item.fileList[0]?.typeName || 'player.html'}`
      }
    })
  } finally {
    loading.value = false
  }
}

const openPreview = (row: TrainingMaterials) => {
  window.open(row.linkUrl, '_blank')
}
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const handleDeptNodeClick = async (row: any) => {
  if (row === undefined) {
    // queryParams.trainingId = undefined
    await getList()
  } else if (row.name === '主数据-客户信息') {
    // 跳转到客户信息页面
    router.push('/data/customer-info')
  } else if (row.name === '主数据-培训信息') {
    router.push('/data/training-record')
  } else if (row.name === '主数据-产品信息') {
    router.push('/data/product')
  } else if (
    row.name === '文档管理' ||
    row.name === '培训资料管理' ||
    row.name === '导入数据管理' ||
    row.name === '2025'
  ) {
    await getList()
    queryParams.containingDirectory = undefined
  } else {
    queryParams.containingDirectory = row.name
    queryContainingDirectory.value = {
      id: row.id,
      name: row.name
    }
    await getList()
  }
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number, query?: any) => {
  formRef.value.open(type, id, query)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await TrainingMaterialsApi.deleteTrainingMaterials(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleDeleteBatch = async () => {
  try {
    await message.delConfirm()
    await TrainingMaterialsApi.deleteTrainingMaterialsList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: TrainingMaterials[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

// 重新拼接文件地址：IP 和端口取自地址栏的 window.location.origin，其余地址保持不变
const getFileUrl = (url: string) => {
  try {
    const parsed = new URL(url)
    return `${window.location.origin}${parsed.pathname}${parsed.search}${parsed.hash}`
  } catch {
    return url
  }
}
// 处理文件下载
const handleFileDownload = async (file: any) => {
  try {
    console.log('开始下载文件:', file)

    // 检查文件类型
    const fileName = file.fileName || 'download'
    const fileExtension = fileName.split('.').pop()?.toLowerCase()

    // 对于图片文件，使用fetch方法获取blob并下载
    if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(fileExtension || '')) {
      console.log('处理图片文件下载')

      // 使用fetch获取文件
      const response = await fetch(getFileUrl(file.fileUrl))
      const blob = await response.blob()

      // 创建Blob URL
      const url = URL.createObjectURL(blob)

      // 创建下载链接
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      link.style.display = 'none'
      document.body.appendChild(link)

      // 模拟点击
      link.click()

      // 清理
      setTimeout(() => {
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        console.log('图片下载完成')
      }, 100)
    } else {
      // 非图片文件使用常规下载方法
      console.log('处理非图片文件下载')
      const link = document.createElement('a')
      link.href = getFileUrl(file.fileUrl)
      link.download = fileName
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()

      setTimeout(() => {
        document.body.removeChild(link)
        console.log('文件下载完成')
      }, 100)
    }
  } catch (error) {
    console.error('下载失败:', error)
    message.error('文件下载失败')
  }
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await TrainingMaterialsApi.exportTrainingMaterials(queryParams)
    download.excel(data, '培训资料管理.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 全局高度控制，确保缩放时布局不塌陷 */
.h-screen {
  height: 100vh;
}

.overflow-hidden {
  overflow: hidden;
}

.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.flex-1 {
  flex: 1;
}

.overflow-auto {
  overflow: auto;
}

/* 表格高度适配，避免缩放时变形 */
.el-table {
  height: 100% !important;
}
</style>
<style lang="scss" scoped>
.el-table {
  .cell {
    text-align: left !important;
    word-break: break-all !important;
    white-space: normal !important;
  }
}

.file-name-link {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

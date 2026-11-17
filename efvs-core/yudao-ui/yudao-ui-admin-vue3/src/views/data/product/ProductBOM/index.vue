<template>
  <ContentWrap>
    <!-- 搜索区域 -->

    <el-form :model="productBOMFormData" class="demo-form-inline" label-width="80" ref="BOMFormRef">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="产品名称" prop="name">
            <el-input v-model="productBOMFormData.name" placeholder="请输入" class="!w-300px" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="代号" prop="partNumber">
            <el-input
              v-model="productBOMFormData.partNumber"
              placeholder="请输入"
              class="!w-300px"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="类型" prop="productType">
            <el-select
              v-model="productBOMFormData.productType"
              placeholder="请选择类型"
              clearable
              class="!w-300px"
            >
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <!--   <el-col :span="12">
          <el-form-item label="生产厂家">
            <el-input
              v-model="productBOMFormData.manufacturer"
              placeholder="请输入"
              class="!w-300px"
            />
          </el-form-item>
        </el-col> -->
      </el-row>

      <el-form-item>
        <el-button @click="handleSearch"><Icon icon="ep:search" class="mr-5px" />查询</el-button>
        <el-button @click="resetForm"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <!-- <el-button type="primary" plain @click="handleAdd('create')">
          <Icon icon="ep:plus" class="mr-5px" />新增
        </el-button> -->
        <el-button type="danger" plain @click="handleBatchDelete" :disabled="isEmpty(checkedIds)">
          <Icon icon="ep:delete" class="mr-5px" />
          批量删除
        </el-button>
        <!--   <el-button @click="handleEditInfo">
          <Icon icon="ep:edit" class="mr-5px" />
          编辑信息
        </el-button> -->
        <!--   <el-button @click="handleDownloadTemplate" plain type="primary">
          <Icon icon="ep:download" class="mr-5px" /> 下载模版
        </el-button> -->
        <el-button @click="handleExport" plain type="success">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button @click="handleImport" plain type="primary">
          <Icon icon="ep:upload" class="mr-5px" /> 导入
        </el-button>

        <!--  <input
          ref="fileInputRef"
          type="file"
          accept=".xlsx"
          style="display: none"
          @change="onFileSelected"
        />
        <span class="import-tip">
          <Icon icon="ep:warning" class="mr-5px" />
          导入文件不可超过200M
        </span> -->
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      :data="ProductBOMTable"
      style="width: 100%"
      @selection-change="handleSelectionChange"
      v-loading="loading"
      row-key="id"
      :tree-props="treeProps"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="PMN" align="center" prop="pmn" min-width="180" />
      <el-table-column label="层级" align="center" prop="level" />
      <el-table-column label="版本" align="center" prop="productVersion" />
      <el-table-column label="类型" align="center" prop="productType">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_TYPE, scope.row.productType) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="代号" align="center" prop="partNumber" min-width="160" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column
        label="所属部件代号"
        align="center"
        prop="productPartNumber"
        min-width="150"
      />
      <el-table-column label="单位" align="center" prop="unit" />
      <el-table-column label="位号" align="center" prop="tagNumber" />
      <el-table-column label="细类" align="center" prop="subcategory">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_SUBCATEGORY, scope.row.subcategory) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="规格或图号" align="center" prop="drawingNumber" min-width="160" />
      <el-table-column label="大类" align="center" prop="category">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_CATEGORY, scope.row.category) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="小类" align="center" prop="smallSubcategory">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_SMALL_SUBCATEGORY, scope.row.smallSubcategory) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="remark" label="备注" />

      <el-table-column label="操作" min-width="200" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="handleViewBOM(row.partNumber)"
            v-show="row.level === 0"
            >列表展示BOM</el-button
          >
          <!-- <el-button link type="primary" @click="handleAdd('update', row.id)">编辑</el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="productBOMFormData.pageNo"
      v-model:limit="productBOMFormData.pageSize"
      @pagination="getProductBOMList"
    />
  </ContentWrap>

  <!-- 新增/编辑dialog -->
  <addAndUpdate ref="addAndUpdateRef" @success="getProductBOMList" />
  <!-- 列表展示BOM -->
  <ProductBOMDetail ref="ProductBOMDetailRef" />
  <ImportExclBOM ref="ImportExclBOMRef" @success="getProductBOMList" />
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { isEmpty } from '@/utils/is'
import { ElLoading } from 'element-plus'
import { onMounted } from 'vue'
import { ProductApi } from '@/api/data/product'
import { ProductBomApi } from '@/api/data/productbom'
import * as UserApi from '@/api/system/user'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import ProductBOMDetail from '@/views/data/product/ProductBOM/detail.vue'
import addAndUpdate from '@/views/data/product/ProductBOM/addAndUpdate.vue'
import download from '@/utils/download'
import ImportExclBOM from './ImportExclBOM.vue'

defineOptions({ name: 'ProductBOMList' })

const router = useRouter()
const message = useMessage()
const { t } = useI18n()

const BOMFormRef = ref()
const fileInputRef = ref<HTMLInputElement | null>(null)

const escapeHtml = (value: any) => {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

const loading = ref(false) //table loading
// 表单数据
const productBOMFormData = reactive({
  name: '',
  partNumber: '',
  productType: '',
  // manufacturer: '',
  pageNo: 1, //页码
  pageSize: 10 //每页条数
})
const total = ref(0)
const treeProps = reactive({
  checkStrictly: true
})

// 表格数据
const ProductBOMTable = ref([])

// 查询方法
const handleSearch = () => {
  productBOMFormData.pageNo = 1
  getProductBOMList()
}

// 查询表格数据
/** 查询列表 */
const getProductBOMList = async () => {
  loading.value = true
  try {
    const data = await ProductApi.getBOMPage(productBOMFormData)
    ProductBOMTable.value = data.list
    total.value = data.total
    // 预加载所有创建人的姓名
    ProductBOMTable.value.forEach(async (item) => {
      // @ts-ignore
      if (item?.creator) {
        // @ts-ignore
        const user = await UserApi.getUser(item.creator)
        // @ts-ignore
        item.creatorName = user.username
      } else {
        // @ts-ignore
        item.creatorName = ''
      }
    })
  } finally {
    loading.value = false
  }
}
const resetForm = () => {
  BOMFormRef.value.resetFields()
  handleSearch()
}

// 下载 BOM 导入模版
const handleDownloadTemplate = async () => {
  const data = await ProductBomApi.downloadTemplate()
  download.excel(data, 'BOM导入模版.xlsx')
}

const addAndUpdateRef = ref()
const ProductBOMDetailRef = ref()
const handleAdd = (type: string, id?: number) => {
  addAndUpdateRef.value.openDialog(type, id)
}

const handleBatchDelete = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await ProductApi.deleteBOM(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getProductBOMList()
  } catch {}
}

const handleExport = async () => {
  try {
    const params = {
      name: productBOMFormData.name,
      partNumber: productBOMFormData.partNumber,
      productType: productBOMFormData.productType
    }
    const blob = await ProductBomApi.exportProductBom(params)
    download.excel(blob as unknown as Blob, '产品BOM.xlsx')
    message.success('导出成功，文件已保存到浏览器默认下载目录')
  } catch (_) {}
}

const ImportExclBOMRef = ref()
const handleImport = () => {
  ImportExclBOMRef.value?.openDialog()
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
    const duplicatedRows = await ProductBomApi.importProductBom(formData)
    // 关闭加载遮罩
    loadingInstance.close()
    if (Array.isArray(duplicatedRows) && duplicatedRows.length > 0) {
      const duplicatedText = duplicatedRows
        .map((item: any, index: number) => {
          const rowNo = item?.excelRowNum ?? '-'
          const reason = item?.skipReason ?? '重复数据'
          const partNumber = item?.partNumber ?? ''
          const productVersion = item?.productVersion ?? ''
          return `<div class="duplicate-msg-item"><span class="duplicate-index">${index + 1}.</span><span class="duplicate-text">第${escapeHtml(rowNo)}行 | ${escapeHtml(reason)} | 代号:${escapeHtml(partNumber)} | 版本号:${escapeHtml(productVersion)}</span></div>`
        })
        .join('')
      const alertHtml = `<div class="duplicate-msg"><div class="duplicate-msg-title">文件：${escapeHtml(file.name)}</div><div class="duplicate-msg-subtitle">导入完成：跳过 ${duplicatedRows.length} 条，其他数据已入库。</div><div class="duplicate-msg-subtitle">跳过明细如下：</div><div class="duplicate-msg-list">${duplicatedText}</div></div>`
      await ElMessageBox.alert(alertHtml, '导入完成', {
        confirmButtonText: '我知道了',
        customClass: 'import-duplicate-dialog',
        width: '90vw',
        dangerouslyUseHTMLString: true
      })
      message.success(`导入完成：跳过 ${duplicatedRows.length} 条，其他数据已入库`)
    } else {
      message.success('导入成功')
    }
    await getProductBOMList()
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

// 查看 BOM
const handleViewBOM = (partNumber: any) => {
  ProductBOMDetailRef.value?.open?.(partNumber)
}

const checkedIds = ref<number[]>([])
const handleSelectionChange = (records: any[]) => {
  checkedIds.value = records.map((item) => item.id!)
}
onMounted(() => {
  getProductBOMList()
})
</script>

<style scoped>
.product-management {
  padding: 20px;
}

.search-box {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  flex-wrap: wrap;
}

.table-container {
  margin-top: 20px;
}

.right-align {
  float: right;
  margin-right: 20px;
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

:deep(.import-duplicate-dialog) {
  width: 90vw;
  max-width: 90vw;
}

:deep(.import-duplicate-dialog .el-message-box__content) {
  max-height: 65vh;
  overflow-y: auto;
}

:deep(.import-duplicate-dialog .el-message-box__message) {
  line-height: 1.6;
  text-align: left;
  word-break: break-word;
  white-space: pre-wrap;
}

:deep(.import-duplicate-dialog .duplicate-msg-title) {
  margin-bottom: 8px;
  font-weight: 500;
}

:deep(.import-duplicate-dialog .duplicate-msg-subtitle) {
  margin-bottom: 6px;
}

:deep(.import-duplicate-dialog .duplicate-msg-list) {
  padding-left: 0;
  margin: 0;
}

:deep(.import-duplicate-dialog .duplicate-msg-item) {
  display: flex;
  align-items: flex-start;
  margin: 4px 0;
}

:deep(.import-duplicate-dialog .duplicate-index) {
  font-weight: 600;
  text-align: left;
  flex: 0 0 32px;
}

:deep(.import-duplicate-dialog .duplicate-text) {
  flex: 1;
  text-align: left;
  word-break: break-word;
}
</style>

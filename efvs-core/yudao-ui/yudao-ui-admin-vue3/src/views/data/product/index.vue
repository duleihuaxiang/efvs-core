<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" label-width="100">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="产品名称" prop="name">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入产品名称"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="产品件号" prop="partNumber">
            <el-input
              v-model="queryParams.partNumber"
              placeholder="请输入产品件号"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="标牌版本" prop="signVersion">
            <el-input
              v-model="queryParams.signVersion"
              placeholder="请输入标牌版本"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="对应基线" prop="baseline" label-width="100">
            <el-input
              v-model="queryParams.baseline"
              placeholder="请输入对应基线"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="交付去向" prop="deliveryDestination" label-width="100">
            <el-input
              v-model="queryParams.deliveryDestination"
              placeholder="请输入交付去向"
              clearable
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="交付时间" prop="deliveryTime">
            <el-date-picker
              v-model="queryParams.deliveryTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:product:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
        <el-button type="primary" plain @click="enterEditMode" v-if="!isEdit">
          <Icon icon="ep:edit" class="mr-5px" /> 编辑
        </el-button>
        <el-button type="info" plain @click="cancelEdit" v-if="isEdit">
          <Icon icon="ep:close" class="mr-5px" /> 取消编辑
        </el-button>

        <el-button
          type="primary"
          plain
          @click="saveChanges"
          :disabled="modifiedRows.size === 0"
          v-if="isEdit"
        >
          <Icon icon="ep:check" class="mr-5px" /> 保存
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:product:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>

        <!--        <el-button type="primary" plain @click="handleDownloadArchiveTemplate">
          <Icon icon="ep:download" class="mr-5px" /> 下载档案模板
        </el-button> -->
        <el-button type="primary" plain @click="handleImportArchive">
          <Icon icon="ep:upload" class="mr-5px" /> 导入档案
        </el-button>
        <!--   <input
          ref="archiveFileInputRef"
          type="file"
          accept=".xlsx"
          style="display: none"
          @change="onArchiveFileSelected"
        />
        <span class="import-tip">
          <Icon icon="ep:warning" class="mr-5px" />
          导入文件不可超过200M
        </span> -->
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <!--       <el-table-column label="PMN" align="center" prop="pmn" min-width="180" />
      <el-table-column label="层级" align="center" prop="level" /> -->

      <el-table-column label="产品名称" align="left" prop="name" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.name" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.name
          }}</span>
        </template>
      </el-table-column>
      <el-table-column label="产品件号" align="center" prop="partNumber" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.partNumber" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.partNumber
          }}</span>
        </template></el-table-column
      >
      <el-table-column label="标牌版本" align="center" prop="signVersion" min-width="130">
        <template #default="{ row }">
          <el-input v-model="row.signVersion" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.signVersion
          }}</span>
        </template></el-table-column
      >
      <el-table-column label="序列号" align="left" prop="sequenceNumber" min-width="130">
        <template #default="{ row }">
          <el-input v-model="row.sequenceNumber" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.sequenceNumber
          }}</span>
        </template>
      </el-table-column>
      <el-table-column label="对应基线" align="left" prop="baseline" min-width="130">
        <template #default="{ row }">
          <el-input v-model="row.baseline" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.baseline
          }}</span>
        </template>
      </el-table-column>
      <!--      <el-table-column label="所属部件件号" align="center" prop="productPartNumber" min-width="150">
        <template #default="{ row }">
          <el-input v-model="row.productPartNumber" :disabled="!isEdit" v-if="isEdit" /><span
            v-else
            >{{ row.productPartNumber }}</span
          >
        </template></el-table-column
      > -->

      <!--  <el-table-column label="版本" align="center" prop="productVersion">
        <template #default="{ row }">
          <el-input v-model="row.productVersion" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.productVersion
          }}</span>
        </template></el-table-column
      > -->
      <!--      <el-table-column label="类型" align="center" prop="productType" min-width="130">
        <template #default="scope">
          <el-select
            v-model="scope.row.productType"
            placeholder="请选择类型"
            clearable
            :disabled="!isEdit"
            v-if="isEdit"
          >
            <el-option
              v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_TYPE)"
              :key="dict.value"
              :label="dict.label"
              :value="Number(dict.value)"
            />
          </el-select>
          <span v-else>
            {{ getDictLabel(DICT_TYPE.PRODUCT_TYPE, scope.row.productType) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="生产厂家" align="center" prop="manufacturer" min-width="130">
        <template #default="{ row }">
          <el-input v-model="row.manufacturer" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.manufacturer
          }}</span>
        </template></el-table-column
      > -->
      <el-table-column label="交付去向" align="center" prop="deliveryDestination" min-width="130">
        <template #default="{ row }">
          <el-input v-model="row.deliveryDestination" :disabled="!isEdit" v-if="isEdit" /><span
            v-else
            >{{ row.deliveryDestination }}</span
          >
        </template></el-table-column
      >
      <el-table-column label="交付时间" align="center" prop="deliveryTime" min-width="210">
        <template #default="{ row }">
          <el-date-picker
            v-model="row.deliveryTime"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled="!isEdit"
            v-if="isEdit"
          /><span v-else>{{ row.deliveryTime }}</span>
        </template></el-table-column
      >
      <!--       <el-table-column label="创建人" align="center" prop="createName" />
      <el-table-column label="备注" align="center" prop="remark">
        <template #default="{ row }">
          <el-input v-model="row.remark" :disabled="!isEdit" v-if="isEdit" /><span v-else>{{
            row.remark
          }}</span>
        </template></el-table-column
      > -->
      <!--    <el-table-column label="数量" align="center" prop="productNum" />
      <el-table-column label="单位" align="center" prop="unit" />
      <el-table-column label="位号" align="center" prop="tagNumber" />
      <el-table-column label="细类" align="center" prop="subcategory">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_SUBCATEGORY, scope.row.subcategory) }}
          </span>
        </template>
      </el-table-column> -->

      <!--     <el-table-column label="规格或图号" align="center" prop="drawingNumber" min-width="160" />
      <el-table-column label="大类" align="center" prop="category">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_CATEGORY, scope.row.category) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="小类" align="center" prop="small_subcategory">
        <template #default="scope">
          <span>
            {{ getDictLabel(DICT_TYPE.PRODUCT_SMALL_SUBCATEGORY, scope.row.small_subcategory) }}
          </span>
        </template>
      </el-table-column>
 -->

      <el-table-column label="操作" align="center" min-width="200">
        <template #default="scope">
          <el-button link type="primary" @click="toDetail(scope.row)"> 详情 </el-button>
          <!--     <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['data:product:update']"
          >
            编辑
          </el-button> -->
          <el-button link type="primary" @click="toArchive(scope.row)"> 档案 </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:product:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="handlePagination"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <ProductForm ref="formRef" @success="getList" />
  <!-- 产品详情 -->
  <ProductDetail ref="ProductDetailRef" />
  <!-- 产品档案列表 -->
  <!-- <ProductArchiveList ref="ProductArchiveListRef" /> -->
  <!-- 档案详情 -->
  <ProductArchiveForm ref="ProductArchiveFormRef" />
  <!-- 导入-->
  <ImportExc ref="ImportExcRef" @success="getList" />
</template>

<script setup lang="ts">
// import { unref } from 'vue'
import { isEmpty } from '@/utils/is'
import download from '@/utils/download'
import { ProductApi, Product } from '@/api/data/product'
// import * as UserApi from '@/api/system/user'
import ProductForm from './ProductForm.vue'
// import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
// import { useTagsViewStore } from '@/store/modules/tagsView'
import ProductDetail from './ProductDetail.vue'
import ProductArchiveForm from './ProductArchiveForm.vue'
import ImportExc from './ImportExc.vue'

/** 产品 列表 */
defineOptions({ name: 'Product' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
// const { delView } = useTagsViewStore() // 视图操作（关闭 tag）

const loading = ref(true) // 列表的加载中
const list = ref<Product[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
// const router = useRouter() // 路由对象
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  signVersion: undefined,
  partNumber: undefined,
  baseline: undefined,
  deliveryDestination: undefined,
  deliveryTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

const isEdit = ref(false) // 是否是编辑状态
const ProductDetailRef = ref()
// const ProductArchiveListRef = ref()
const archiveFileInputRef = ref<HTMLInputElement | null>(null)

const escapeHtml = (value: any) => {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ProductApi.getProductPage(queryParams)
    list.value = data.list
    total.value = data.total
    // 如果当前处于编辑模式，在获取新数据后也要更新原始列表
    if (isEdit.value) {
      originalList.value = new Map()
      list.value.forEach((item) => {
        if (item.id) {
          originalList.value.set(item.id, { ...item })
        }
      })
      // 清空之前的修改记录，因为页面已切换
      modifiedRows.value.clear()
      modifiedData.value.clear()
    }
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 添加原始数据备份 - 使用 Map 存储，以 ID 为键
const originalList = ref<Map<number, Product>>(new Map())

// 记录哪些行被修改了 - 仍使用 Set 存储 ID
const modifiedRows = ref<Set<number>>(new Set())

// 记录修改的数据 - 使用 Map 存储，以 ID 为键
const modifiedData = ref<Map<number, Partial<Product>>>(new Map())

// 进入编辑模式
const enterEditMode = () => {
  isEdit.value = true
  // 进入编辑模式时备份原始数据 - 使用 Map 存储
  originalList.value = new Map()
  list.value.forEach((item) => {
    if (item.id) {
      originalList.value.set(item.id, { ...item })
    }
  })
}

// 保存按钮的点击事件
const saveChanges = async () => {
  if (modifiedRows.value.size === 0) {
    message.info('没有数据需要保存')
    return
  }

  try {
    // 构建需要更新的数据
    const updateData = Array.from(modifiedData.value.values())

    // 发送到后端更新
    await ProductApi.updateProduct(updateData) // 批量更新API

    message.success('保存成功')

    // 清空修改记录
    modifiedRows.value.clear()
    modifiedData.value.clear()

    // 退出编辑模式
    isEdit.value = false

    // 刷新列表
    await getList()
  } catch (error) {
    message.error('保存失败: ' + (error as Error).message)
  }
}

// 取消编辑，恢复原始数据
const cancelEdit = () => {
  // 恢复原始数据
  list.value = list.value.map((item) => {
    if (item.id && originalList.value.has(item.id)) {
      return { ...originalList.value.get(item.id)! }
    }
    return item
  })

  // 退出编辑模式
  isEdit.value = false

  // 清空修改记录
  modifiedRows.value.clear()
  modifiedData.value.clear()
}

// 监听数据变化，记录修改的行 - 修改比较逻辑
watch(
  list,
  (newList) => {
    if (!isEdit.value) return

    newList.forEach((item) => {
      if (item.id) {
        const originalItem = originalList.value.get(item.id)
        if (originalItem) {
          // 比较各个字段是否有变化
          const hasChanged = Object.keys(item).some((key) => {
            // @ts-ignore
            return item[key] !== originalItem[key]
          })

          if (hasChanged) {
            modifiedRows.value.add(item.id)
            // @ts-ignore - 将整个对象作为修改数据
            modifiedData.value.set(item.id, { ...item })
          } else {
            modifiedRows.value.delete(item.id)
            modifiedData.value.delete(item.id)
          }
        } else {
          // 如果原始列表中没有该项目，说明是新添加的或者是跨页切换导致的
          // 添加到原始列表中
          originalList.value.set(item.id, { ...item })
        }
      }
    })
  },
  { deep: true }
)

// 监听编辑状态变化
watch(isEdit, (newVal) => {
  if (newVal) {
    // 进入编辑模式时备份原始数据
    originalList.value = new Map()
    list.value.forEach((item) => {
      if (item.id) {
        originalList.value.set(item.id, { ...item })
      }
    })
  } else {
    // 退出编辑模式时清空修改记录
    modifiedRows.value.clear()
    modifiedData.value.clear()
  }
})

const handlePagination = async () => {
  if (isEdit.value && modifiedRows.value.size > 0) {
    try {
      const confirmResult = await message.confirm(
        '当前有未保存的更改，切换页面将丢失这些更改，是否继续？'
      )
      if (confirmResult) {
        modifiedRows.value.clear()
        modifiedData.value.clear()
      } else {
        // 恢复分页参数，不执行切换
        return
      }
    } catch {
      // 用户取消，不执行切换
      return
    }
  }
  getList()
}

// 点击详情
const toDetail = (row: any) => {
  ProductDetailRef.value?.open?.(row.id, {
    partNumber: row.partNumber,
    sequenceNumber: row.sequenceNumber,
    signVersion: row.signVersion
  })
}

// 点击档案
const ProductArchiveFormRef = ref()

// 点击档案
const toArchive = (row: any) => {
  ProductArchiveFormRef.value?.open?.(row)
}
// 点击产品BOM
/* const toBOM = () => {
  router.push({ name: 'ProductBOMList' })
} */
/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await ProductApi.deleteProduct(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除产品 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await ProductApi.deleteProductList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Product[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await ProductApi.exportProduct(queryParams)
    download.excel(data, '产品.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

// 下载档案模板
const handleDownloadArchiveTemplate = async () => {
  try {
    const blob = await ProductApi.downloadArchiveTemplate()
    download.excel(blob as unknown as Blob, '档案模版.xlsx')
    message.success('模板下载成功')
  } catch {
    message.error('模板下载失败')
  }
}

const ImportExcRef = ref()
// 导入档案：触发文件选择
const handleImportArchive = () => {
  // archiveFileInputRef.value?.click()
  ImportExcRef.value?.openDialog()
}

// 选择文件后调用导入接口
const onArchiveFileSelected = async (e: Event) => {
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
    const duplicatedRows = await ProductApi.importProductFileExcel(formData)
    loadingInstance.close()
    if (Array.isArray(duplicatedRows) && duplicatedRows.length > 0) {
      const duplicatedText = duplicatedRows
        .map((item: any, index: number) => {
          const rowNo = item?.excelRowNum ?? '-'
          const reason = item?.skipReason ?? '重复数据'
          const partNumber = item?.partNumber ?? ''
          const sequenceNumber = item?.sequenceNumber ?? ''
          const drawingVersion = item?.drawingVersion ?? ''
          return `<div class="duplicate-msg-item"><span class="duplicate-index">${index + 1}.</span><span class="duplicate-text">第${escapeHtml(rowNo)}行 | ${escapeHtml(reason)} | 产品件号:${escapeHtml(partNumber)} | 序列号:${escapeHtml(sequenceNumber)} | 图纸版本:${escapeHtml(drawingVersion)}</span></div>`
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
    await getList()
  } catch (error) {
    loadingInstance.close()
    message.error('导入失败')
    console.error('导入错误:', error)
  } finally {
    input.value = ''
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style scoped>
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

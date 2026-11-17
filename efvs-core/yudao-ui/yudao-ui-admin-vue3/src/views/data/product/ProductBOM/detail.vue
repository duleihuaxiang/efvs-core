<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="900"
    :auto-fullscreen="true"
    @closed="handleClosed"
  >
    <!-- 操作按钮区域 -->
    <!-- 搜索区域 -->

    <el-form
      :model="productBOMFormData"
      class="demo-form-inline"
      label-width="80"
      ref="productBOMFormRef"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="productBOMFormData.name" placeholder="请输入" class="!w-300px" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="件号" prop="partNumber">
            <el-input v-model="productBOMFormData.partNumber" class="!w-300px" disabled />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="版本" prop="productVersion">
            <el-input
              v-model="productBOMFormData.productVersion"
              placeholder="请输入"
              class="!w-300px"
            />
          </el-form-item>
        </el-col>
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
      </el-row>

      <el-form-item>
        <el-button @click="getDetail"><Icon icon="ep:search" class="mr-5px" />查询</el-button>
        <el-button @click="resetForm"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['data:product-bom:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />新增
        </el-button>
        <el-button type="success" plain @click="handleExport" v-hasPermi="['data:product:export']">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </el-form-item>
    </el-form>
    <!--     <div class="action-buttons">
      <el-button
        type="primary"
        @click="openForm('create')"
        v-hasPermi="['data:product-bom:create']"
      >
        <Icon icon="ep:plus" class="mr-5px" /> 新增
      </el-button>
      <el-button @click="handleExport">导出</el-button>
    </div> -->

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="tableData"
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="pmn" label="PMN" />
        <el-table-column prop="level" label="层级" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="partNumber" label="件号" />
        <el-table-column prop="productPartNumber" label="所属部件代号" />
        <el-table-column prop="productVersion" label="版本" />
        <el-table-column prop="productType" label="类型">
          <template #default="scope">
            <span>
              {{ getDictLabel(DICT_TYPE.PRODUCT_TYPE, scope.row.productType) }}
            </span>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="productNum" label="数量" /> -->
        <el-table-column prop="unit" label="单位" />
        <el-table-column label="位号" align="center" prop="tagNumber" />
        <el-table-column label="细类" align="center" prop="subcategory">
          <template #default="scope">
            <span>
              {{ getDictLabel(DICT_TYPE.PRODUCT_SUBCATEGORY, scope.row.subcategory) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="规格或图号" align="center" prop="drawingNumber" />
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
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openForm('update', scope.row.id)"
              v-hasPermi="['data:product-bom:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row)"
              v-hasPermi="['data:product-bom:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 表单弹窗：添加/修改 -->
    <DetailEdit ref="formRef" @success="getDetail()" />
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ProductApi } from '@/api/data/product'
import { ProductBomApi } from '@/api/data/productbom'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import download from '@/utils/download'
import DetailEdit from './DetailEdit.vue'

defineOptions({
  name: 'ProductBOMDetail'
})
const currentPartNumber = ref<string | undefined>(undefined)

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const productBOMFormRef = ref()
const productBOMFormData = reactive({
  name: '',
  partNumber: '',
  productType: '',
  productVersion: ''
})
// 表格数据
const tableData = ref([])

// 选中行数据
const selectedRows = ref<any[]>([])
const loading = ref(false)

const message = useMessage()
const formRef = ref()

// 打开弹窗
const open = async (partNumber: any) => {
  dialogTitle.value = 'BOM 详情'
  dialogVisible.value = true
  productBOMFormData.partNumber = partNumber
  currentPartNumber.value = partNumber

  // 调用方法获取数据
  // if (partNumber) {
  await getDetail()
  // }
}

const resetForm = () => {
  productBOMFormRef.value.resetFields()
  productBOMFormData.partNumber = currentPartNumber.value
  getDetail()
}

// 关闭弹窗
const close = () => {
  dialogVisible.value = false
}

// 弹窗关闭后清空查询条件（保留件号）
const handleClosed = () => {
  productBOMFormData.name = ''
  productBOMFormData.productType = ''
  productBOMFormData.productVersion = ''
  tableData.value = []
  productBOMFormRef.value?.clearValidate()
}

// 暴露方法
defineExpose({
  open
})

// 打开表单弹窗
const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, currentPartNumber.value, id)
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await message.delConfirm()
    await ProductBomApi.deleteProductBom(row.id)
    message.success('删除成功')
    await getDetail()
  } catch {}
}

// 操作方法
const handleExport = async () => {
  if (!currentPartNumber.value) {
    return
  }
  try {
    const blob = await ProductBomApi.exportProductBomDetail(currentPartNumber.value)
    const fileName = `BOM详情-${currentPartNumber.value}.xlsx`
    download.excel(blob as unknown as Blob, fileName)
    message.success('导出成功，文件已保存到浏览器默认下载目录（如：下载/Downloads）')
  } catch (_) {}
}
/* const goBack = () => {
  close()
} */
const handleSelectionChange = (val: any[]) => {
  selectedRows.value = val
}
//获取详情
const getDetail = async () => {
  loading.value = true
  try {
    const data = await ProductApi.getBOMList({
      ...productBOMFormData
    })
    tableData.value = data
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.bom-management {
  padding: 20px;
}

.action-buttons {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  flex-wrap: wrap;
}

.right-buttons {
  margin-left: auto;
}

.table-container {
  margin-top: 20px;
}
</style>

<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <!-- 产品属性 -->
    <div class="mb-4">
      <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">产品属性</h3>
      <el-form label-width="150" class="flex flex-wrap gap-4">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称:">
              <el-input v-model="product.name" disabled /> </el-form-item></el-col
          ><el-col :span="12">
            <el-form-item label="产品件号:">
              <el-input v-model="product.partNumber" disabled /> </el-form-item></el-col
        ></el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属部件件号:">
              <el-input v-model="product.productPartNumber" disabled /> </el-form-item></el-col
          ><el-col :span="12">
            <el-form-item label="版本:">
              <el-input v-model="product.productVersion" disabled /> </el-form-item></el-col
        ></el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备注:">
              <el-input v-model="product.remark" disabled /> </el-form-item></el-col
          ><el-col :span="12">
            <el-form-item label="生产厂家:">
              <el-input v-model="product.manufacturer" disabled /> </el-form-item></el-col
        ></el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="序列号:">
              <el-input v-model="product.sequenceNumber" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 返回按钮 -->
      <div class="mt-6 text-right">
        <el-form :model="queryParams">
          <el-row :gutter="20">
            <el-col :span="4">
              <el-form-item label="序列号:">
                <el-input v-model="queryParams.SerialNumber" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item>
                <el-button @click="handleQuery"
                  ><Icon icon="ep:search" class="mr-5px" /> 查询</el-button
                >
                <!--   <el-button @click="resetQuery"
            ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button
          > --></el-form-item
              >
            </el-col>
          </el-row></el-form
        >
        <!-- <el-button @click="goBack" type="primary">返回</el-button> -->
      </div>
    </div>

    <!-- 产品序列号 -->
    <div class="mb-4">
      <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">产品序列号</h3>
      <el-table :data="serialList" border>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="sequenceNumber" label="序列号" />
        <el-table-column prop="partNumber" label="产品件号" />
        <!-- <el-table-column prop="productPartNumber" label="所属产品件号" /> -->
        <el-table-column prop="productVersion" label="版本" />
        <!--  <el-table-column prop="productType" label="类型">
          <template #default="{ row }">
            {{ getDictLabel(DICT_TYPE.PRODUCT_TYPE, row.productType) }}
          </template>
        </el-table-column> -->
        <!-- <el-table-column prop="productNum" label="数量" /> -->
        <!-- <el-table-column prop="manufacturer" label="生产厂家" /> -->
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createName" label="创建人" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="openForm('update', row.id)"
              v-hasPermi="['data:product:update']"
              >编辑</el-button
            >
            <el-button size="small" type="primary" @click="toArchive(row)">档案</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑弹框-->
    <ProductArchiveEdit ref="formRef" @success="handleQuery" />

    <!-- 档案详情 -->
    <ProductArchiveForm ref="ProductArchiveFormRef" />
  </Dialog>
</template>

<script setup lang="ts">
import { ref, unref } from 'vue'
import { ProductApi } from '@/api/data/product'
// import { useRoute } from 'vue-router'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { DICT_TYPE, getDictLabel } from '@/utils/dict'
import ProductArchiveForm from './ProductArchiveForm.vue'
import ProductArchiveEdit from './ProductArchiveEdit.vue'

defineOptions({ name: 'ProductArchiveList' })

// const route = useRoute()
const router = useRouter() // 路由对象
const message = useMessage() // 消息弹窗
const { delView } = useTagsViewStore() // 视图操作（关闭 tag）
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题

// 打开弹窗
const open = (row: any) => {
  dialogTitle.value = '产品档案'
  dialogVisible.value = true

  // 调用方法获取数据
  if (row) {
    getProductDetail(Number(row.id))
    getSerialList({ partNumber: row.partNumber })
  }
}

// 关闭弹窗
const close = () => {
  dialogVisible.value = false
}

// 暴露方法
defineExpose({
  open
})

const product = ref({
  name: '',
  partNumber: '',
  productPartNumber: '',
  productVersion: '',
  remark: '',
  manufacturer: '',
  sequenceNumber: '', //序列号
  designId: [], //设计属性id
  identificaionId: [], //标识属性id
  memoryId: [], //机型属性id
  guaranteeId: [], //保障属性id
  supplyId: [], // 供应属性id
  faultInformationId: [] // 故障信息id
})

const queryParams = reactive({
  SerialNumber: undefined
})

const serialList = ref<any[]>([])
const ProductArchiveFormRef = ref()

// 点击档案
const toArchive = (row: any) => {
  ProductArchiveFormRef.value?.open?.(row)
}

// 返回按钮
/* const goBack = () => {
  close()
} */

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

// 获取产品详情
const getProductDetail = async (id: number) => {
  try {
    const data = await ProductApi.getProduct(id)
    product.value = data || {}
  } catch (error) {
    message.error(`获取产品详情失败:${error}`)
  }
}
// 获取产品序列号列表
const getSerialList = async (params: any) => {
  try {
    const data = await ProductApi.getProductSerialNumber(params)
    serialList.value = data
  } catch (error) {
    message.error(`获取产品序列号列表失败:${error}`)
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  const params = {
    partNumber: product.value.partNumber
  }
  if (queryParams.SerialNumber) {
    params.SerialNumber = queryParams.SerialNumber
  }
  getSerialList(params)
}
// 不再在挂载时自动获取数据，而是通过 open 方法传入参数
</script>

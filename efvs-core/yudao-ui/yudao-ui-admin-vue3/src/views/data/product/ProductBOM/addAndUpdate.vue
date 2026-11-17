<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="PMN" prop="pmn">
            <el-input v-model="formData.pmn" placeholder="请输入PMN" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="层级" prop="level">
            <el-input v-model="formData.level" placeholder="请输入层级" type="number" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="代号" prop="partNumber">
            <el-input v-model="formData.partNumber" placeholder="请输入代号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="版本" prop="productVersion">
            <el-input v-model="formData.productVersion" placeholder="请输入版本" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型" prop="productType">
            <el-select v-model="formData.productType" placeholder="请选择类型">
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

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="单位" prop="unit">
            <el-input v-model="formData.unit" placeholder="请输入单位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属部件代号" prop="productPartNumber">
            <el-input v-model="formData.productPartNumber" placeholder="请输入所属部件代号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="位号" prop="tagNumber">
            <el-input v-model="formData.tagNumber" placeholder="请输入位号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="细类" prop="subcategory">
            <el-select v-model="formData.subcategory" placeholder="请选择细类">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_SUBCATEGORY)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="小类" prop="small_subcategory">
            <el-select v-model="formData.small_subcategory" placeholder="请选择小类">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_SMALL_SUBCATEGORY)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="大类" prop="category">
            <el-select v-model="formData.category" placeholder="请选择大类">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_CATEGORY)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="规格图号" prop="drawing_number">
            <el-input
              v-model="formData.drawing_number"
              placeholder="请输入规格图号"
            /> </el-form-item
        ></el-col>
        <el-col :span="12">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" :rows="3" /> </el-form-item
        ></el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { ProductApi, Product } from '@/api/data/product'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** 产品 表单 */
defineOptions({ name: 'ProductForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  pmn: undefined,
  level: undefined,
  partNumber: undefined,
  productNum: undefined,
  productPartNumber: undefined,
  tagNumber: undefined,
  subcategory: undefined,
  small_subcategory: undefined,
  category: undefined,
  drawing_number: undefined,
  productType: undefined,
  productVersion: undefined,
  unit: undefined,
  guaranteeId: undefined,
  supplyId: undefined,
  identificaionId: undefined,
  designId: undefined,

  name: undefined,
  // quantity: undefined,
  manufacturer: undefined,
  remark: undefined
})
const formRules = reactive({})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const openDialog = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type

  resetForm()

  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = (await ProductApi.getBOM(id)) || {}
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ openDialog }) // 提供 openDialog 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as Product
    if (formType.value === 'create') {
      await ProductApi.createBOM(data)

      message.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateBOM(data)

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
    pmn: undefined,
    level: undefined,
    partNumber: undefined,
    productNum: undefined,
    productPartNumber: undefined,
    tagNumber: undefined,
    subcategory: undefined,
    small_subcategory: undefined,
    category: undefined,
    drawing_number: undefined,
    productType: undefined,
    productVersion: undefined,
    unit: undefined,
    guaranteeId: undefined,
    supplyId: undefined,
    identificaionId: undefined,
    designId: undefined,

    name: undefined,
    // quantity: undefined,
    manufacturer: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

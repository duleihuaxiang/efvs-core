<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="PMN" prop="pmn">
            <el-input v-model="formData.pmn" placeholder="请输入 PMN" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="层级" prop="level">
            <el-input v-model="formData.level" placeholder="请输入层级" />
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
          <el-form-item label="件号" prop="partNumber">
            <el-input v-model="formData.partNumber" placeholder="请输入件号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属部件代号" prop="productPartNumber">
            <el-input v-model="formData.productPartNumber" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="版本" prop="productVersion">
            <el-input v-model="formData.productVersion" placeholder="请输入版本" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="类型" prop="productType">
            <el-select v-model="formData.productType" placeholder="请选择类型" style="width: 100%">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单位" prop="unit">
            <el-input v-model="formData.unit" placeholder="请输入单位" />
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
            <el-select v-model="formData.subcategory" placeholder="请选择细类" style="width: 100%">
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
          <el-form-item label="规格或图号" prop="drawingNumber">
            <el-input v-model="formData.drawingNumber" placeholder="请输入规格或图号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="大类" prop="category">
            <el-select v-model="formData.category" placeholder="请选择大类" style="width: 100%">
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
          <el-form-item label="小类" prop="smallSubcategory">
            <el-select
              v-model="formData.smallSubcategory"
              placeholder="请选择小类"
              style="width: 100%"
            >
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_SMALL_SUBCATEGORY)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitForm" :loading="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

import { ProductApi, Product } from '@/api/data/product'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'ProductBOMForm' })

const message = useMessage()
const { t } = useI18n() // 国际化

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formRef = ref<FormInstance>()
const currentPartNumber = ref<string | undefined>(undefined)

const formData = ref({
  id: undefined,
  pmn: '',
  level: 1,
  name: '',
  partNumber: '',
  productPartNumber: currentPartNumber.value,
  productVersion: '',
  productType: undefined,
  unit: '',
  tagNumber: '',
  subcategory: undefined,
  drawingNumber: '',
  category: undefined,
  smallSubcategory: undefined,
  remark: '',
  parentPartNumber: ''
})

const formRules = reactive<FormRules>({
  pmn: [{ required: true, message: '请输入 PMN', trigger: 'blur' }],
  level: [{ required: true, message: '请输入层级', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  partNumber: [{ required: true, message: '请输入件号', trigger: 'blur' }]
})

const open = async (type: string, partNumber: any, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增 BOM' : '编辑 BOM'
  formType.value = type
  currentPartNumber.value = partNumber
  resetForm()
  if (type === 'update' && id) {
    formLoading.value = true
    try {
      const data = await ProductApi.getBOM(id)
      if (data) {
        formData.value = {
          ...data,
          // 将字典字段转换为 number 类型，确保回显时能正确匹配选项
          productType: data.productType ? Number(data.productType) : undefined,
          subcategory: data.subcategory ? Number(data.subcategory) : undefined,
          category: data.category ? Number(data.category) : undefined,
          smallSubcategory: data.smallSubcategory ? Number(data.smallSubcategory) : undefined
        }
      }
    } finally {
      formLoading.value = false
    }
  }
}

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value?.validate()
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

const resetForm = () => {
  formData.value = {
    id: undefined,
    pmn: '',
    level: 1,
    name: '',
    partNumber: '',
    productPartNumber: currentPartNumber.value,
    productVersion: '',
    productType: undefined,
    unit: '',
    tagNumber: '',
    subcategory: undefined,
    drawingNumber: '',
    category: undefined,
    smallSubcategory: undefined,
    remark: '',
    parentPartNumber: ''
  }
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>

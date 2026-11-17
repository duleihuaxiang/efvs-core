<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :autoFullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="序列号" prop="sequenceNumber">
            <el-input v-model="formData.sequenceNumber" placeholder="请输入序列号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="版本" prop="signVersion">
            <el-input v-model="formData.signVersion" placeholder="请输入版本" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品件号" prop="partNumber">
            <el-input v-model="formData.partNumber" placeholder="请输入产品件号" />
          </el-form-item>
          <!--     <el-form-item label="类型" prop="productType">
            <el-select v-model="formData.productType" placeholder="请选择类型">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item> -->
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <!--      <el-form-item label="生产厂家" prop="manufacturer">
            <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" />
          </el-form-item> -->
        </el-col>
        <el-col :span="12" />
      </el-row>
      <!--     <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属产品件号" prop="productPartNumber">
            <el-input v-model="formData.productPartNumber" placeholder="请输入所属产品件号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="数量" prop="productNum">
            <el-input v-model="formData.productNum" placeholder="请输入数量" />
          </el-form-item>
        </el-col>
      </el-row> -->
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" />
      </el-form-item>
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
defineOptions({ name: 'ProductArchiveEdit' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  productPartNumber: undefined,
  signVersion: undefined,
  productType: undefined,
  productNum: undefined,
  partNumber: undefined,
  sequenceNumber: undefined,
  manufacturer: undefined,
  remark: undefined
})
const formRules = reactive({})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type

  resetForm()

  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductApi.getProduct(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

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
      await ProductApi.createProduct(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateProduct([data])
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
    name: undefined,
    productPartNumber: undefined,
    signVersion: undefined,
    productType: undefined,
    productNum: undefined,
    partNumber: undefined,
    sequenceNumber: undefined,
    manufacturer: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="客户id" prop="customerId">
        <el-input v-model="formData.customerId" placeholder="请输入客户id" />
      </el-form-item>
      <el-form-item label="产品大类" prop="productCategory">
        <el-input v-model="formData.productCategory" placeholder="请输入产品大类" />
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="formData.productName" placeholder="请输入产品名称" />
      </el-form-item>
      <el-form-item label="产品型号" prop="productModel">
        <el-input v-model="formData.productModel" placeholder="请输入产品型号" />
      </el-form-item>
      <el-form-item label="产品序列号" prop="productSerialNum">
        <el-input v-model="formData.productSerialNum" placeholder="请输入产品序列号" />
      </el-form-item>
      <el-form-item label="产品数量" prop="productQuantity">
        <el-input v-model="formData.productQuantity" placeholder="请输入产品数量" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { PurchaseProductInfoApi, PurchaseProductInfo } from '@/api/data/purchaseproductinfo'

/** 购买产品信息 表单 */
defineOptions({ name: 'PurchaseProductInfoForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  customerId: undefined,
  productCategory: undefined,
  productName: undefined,
  productModel: undefined,
  productSerialNum: undefined,
  productQuantity: undefined,
  remark: undefined
})
const formRules = reactive({
  productCategory: [{ required: true, message: '产品大类不能为空', trigger: 'blur' }],
  productName: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
  productModel: [{ required: true, message: '产品型号不能为空', trigger: 'blur' }],
  productSerialNum: [{ required: true, message: '产品序列号不能为空', trigger: 'blur' }],
  productQuantity: [{ required: true, message: '产品数量不能为空', trigger: 'blur' }]
})
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
      formData.value = await PurchaseProductInfoApi.getPurchaseProductInfo(id)
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
    const data = formData.value as unknown as PurchaseProductInfo
    if (formType.value === 'create') {
      await PurchaseProductInfoApi.createPurchaseProductInfo(data)
      message.success(t('common.createSuccess'))
    } else {
      await PurchaseProductInfoApi.updatePurchaseProductInfo(data)
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
    customerId: undefined,
    productCategory: undefined,
    productName: undefined,
    productModel: undefined,
    productSerialNum: undefined,
    productQuantity: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

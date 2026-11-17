<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="供应商名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入供应商名称" />
      </el-form-item>
      <el-form-item label="供应商编号" prop="supplierNumber">
        <el-input v-model="formData.supplierNumber" placeholder="请输入供应商编号" />
      </el-form-item>
      <el-form-item label="生产日期" prop="productionDate">
        <el-date-picker
          v-model="formData.productionDate"
          type="date"
          value-format="x"
          placeholder="选择生产日期"
        />
      </el-form-item>
      <el-form-item label="保修状态0是1否" prop="batchNumber">
        <el-input v-model="formData.batchNumber" placeholder="请输入保修状态0是1否" />
      </el-form-item>
      <el-form-item label="保修状态" prop="warrantyStatus">
        <el-input v-model="formData.warrantyStatus" placeholder="请输入保修状态" />
      </el-form-item>
      <el-form-item label="采购订单号" prop="purchaseOrderNumber">
        <el-input v-model="formData.purchaseOrderNumber" placeholder="请输入采购订单号" />
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
import { SupplyAttributeApi, SupplyAttribute } from '@/api/data/supplyattribute'

/** 供应属性 表单 */
defineOptions({ name: 'SupplyAttributeForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  supplierNumber: undefined,
  productionDate: undefined,
  batchNumber: undefined,
  warrantyStatus: undefined,
  purchaseOrderNumber: undefined,
  remark: undefined
})
const formRules = reactive({
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
      formData.value = await SupplyAttributeApi.getSupplyAttribute(id)
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
    const data = formData.value as unknown as SupplyAttribute
    if (formType.value === 'create') {
      await SupplyAttributeApi.createSupplyAttribute(data)
      message.success(t('common.createSuccess'))
    } else {
      await SupplyAttributeApi.updateSupplyAttribute(data)
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
    supplierNumber: undefined,
    productionDate: undefined,
    batchNumber: undefined,
    warrantyStatus: undefined,
    purchaseOrderNumber: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
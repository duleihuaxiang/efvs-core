<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="当前寿命累计（小时）" prop="currentLifespan">
        <el-input v-model="formData.currentLifespan" placeholder="请输入当前寿命累计（小时）" />
      </el-form-item>
      <el-form-item label="保养周期（月）" prop="maintenanceInterval">
        <el-input v-model="formData.maintenanceInterval" placeholder="请输入保养周期（月）" />
      </el-form-item>
      <el-form-item label="检修记录编号" prop="maintenanceRecordNumber">
        <el-input v-model="formData.maintenanceRecordNumber" placeholder="请输入检修记录编号" />
      </el-form-item>
      <el-form-item label="维修级别" prop="maintenanceIevel">
        <el-input v-model="formData.maintenanceIevel" placeholder="请输入维修级别" />
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
import { GuaranteeAttributesApi, GuaranteeAttributes } from '@/api/data/guaranteeattributes'

/** 保障属性 表单 */
defineOptions({ name: 'GuaranteeAttributesForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  currentLifespan: undefined,
  maintenanceInterval: undefined,
  maintenanceRecordNumber: undefined,
  maintenanceIevel: undefined,
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
      formData.value = await GuaranteeAttributesApi.getGuaranteeAttributes(id)
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
    const data = formData.value as unknown as GuaranteeAttributes
    if (formType.value === 'create') {
      await GuaranteeAttributesApi.createGuaranteeAttributes(data)
      message.success(t('common.createSuccess'))
    } else {
      await GuaranteeAttributesApi.updateGuaranteeAttributes(data)
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
    currentLifespan: undefined,
    maintenanceInterval: undefined,
    maintenanceRecordNumber: undefined,
    maintenanceIevel: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

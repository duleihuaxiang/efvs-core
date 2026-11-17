<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="设计版本号" prop="version">
        <el-input v-model="formData.version" placeholder="请输入设计版本号" />
      </el-form-item>
      <el-form-item label="DOM结构级别" prop="domLevel">
        <el-input v-model="formData.domLevel" placeholder="请输入DOM结构级别" />
      </el-form-item>
      <el-form-item label="功能描述" prop="description">
        <el-input v-model="formData.description" type="textarea" placeholder="请输入功能描述" />
      </el-form-item>
      <el-form-item label="批准号" prop="approvalUmber">
        <el-input v-model="formData.approvalUmber" placeholder="请输入批准号" />
      </el-form-item>
      <el-form-item label="设计寿命" prop="designLifespan">
        <el-input v-model="formData.designLifespan" placeholder="请输入设计寿命" />
      </el-form-item>
      <el-form-item label="技术图纸编号" prop="technicalDrawingNumber">
        <el-input v-model="formData.technicalDrawingNumber" placeholder="请输入技术图纸编号" />
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
import { DesignAttributeApi, DesignAttribute } from '@/api/data/designattribute'

/** 设计属性 表单 */
defineOptions({ name: 'DesignAttributeForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  version: undefined,
  domLevel: undefined,
  description: undefined,
  approvalUmber: undefined,
  designLifespan: undefined,
  technicalDrawingNumber: undefined,
  remark: undefined
})
const formRules = reactive({
  version: [{ required: true, message: '设计版本号不能为空', trigger: 'blur' }],
  approvalUmber: [{ required: true, message: '批准号不能为空', trigger: 'blur' }]
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
      formData.value = await DesignAttributeApi.getDesignAttribute(id)
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
    const data = formData.value as unknown as DesignAttribute
    if (formType.value === 'create') {
      await DesignAttributeApi.createDesignAttribute(data)
      message.success(t('common.createSuccess'))
    } else {
      await DesignAttributeApi.updateDesignAttribute(data)
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
    version: undefined,
    domLevel: undefined,
    description: undefined,
    approvalUmber: undefined,
    designLifespan: undefined,
    technicalDrawingNumber: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
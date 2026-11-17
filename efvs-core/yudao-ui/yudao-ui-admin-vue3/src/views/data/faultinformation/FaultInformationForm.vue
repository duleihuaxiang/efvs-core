<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="故障日期" prop="faultDate">
        <el-date-picker
          v-model="formData.faultDate"
          type="date"
          value-format="x"
          placeholder="选择故障日期"
        />
      </el-form-item>
      <el-form-item label="故障现象" prop="faultPhenomenon">
        <el-input v-model="formData.faultPhenomenon" placeholder="请输入故障现象" />
      </el-form-item>
      <el-form-item label="故障状态0已解决1排查中" prop="faultStatus">
        <el-input v-model="formData.faultStatus" placeholder="请选择故障状态" />
      </el-form-item>
      <el-form-item label="故障原因" prop="causeOfFailure">
        <el-input v-model="formData.causeOfFailure" placeholder="请输入故障原因" />
      </el-form-item>
      <el-form-item label="解决人" prop="resolver">
        <el-input v-model="formData.resolver" placeholder="请输入解决人" />
      </el-form-item>
      <el-form-item label=" 备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入 备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { FaultInformationApi, FaultInformation } from '@/api/data/faultinformation'

/** 故障信息 表单 */
defineOptions({ name: 'FaultInformationForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  faultDate: undefined,
  faultPhenomenon: undefined,
  faultStatus: undefined,
  causeOfFailure: undefined,
  resolver: undefined,
  remark: undefined
})
const formRules = reactive({
  faultDate: [{ required: true, message: '故障日期不能为空', trigger: 'blur' }],
  faultStatus: [{ required: true, message: '故障状态0已解决1排查中不能为空', trigger: 'blur' }]
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
      formData.value = await FaultInformationApi.getFaultInformation(id)
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
    const data = formData.value as unknown as FaultInformation
    if (formType.value === 'create') {
      await FaultInformationApi.createFaultInformation(data)
      message.success(t('common.createSuccess'))
    } else {
      await FaultInformationApi.updateFaultInformation(data)
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
    faultDate: undefined,
    faultPhenomenon: undefined,
    faultStatus: undefined,
    causeOfFailure: undefined,
    resolver: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

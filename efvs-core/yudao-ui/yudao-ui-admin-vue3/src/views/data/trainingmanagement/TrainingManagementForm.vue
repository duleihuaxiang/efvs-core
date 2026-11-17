<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="编号ID" prop="serialNumber">
        <el-input v-model="formData.serialNumber" placeholder="请输入编号ID" />
      </el-form-item>
      <el-form-item label="流程名称" prop="processName">
        <el-input v-model="formData.processName" placeholder="请输入流程名称" />
      </el-form-item>
      <el-form-item label="发起时间" prop="initiationTime">
        <el-date-picker
          v-model="formData.initiationTime"
          type="date"
          value-format="x"
          placeholder="选择发起时间"
        />
      </el-form-item>
      <el-form-item label="发起人" prop="initiator">
        <el-input v-model="formData.initiator" placeholder="请输入发起人" />
      </el-form-item>
      <el-form-item label="当前环节" prop="currentStage">
        <el-input v-model="formData.currentStage" placeholder="请输入当前环节" />
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
import { TrainingManagementApi, TrainingManagement } from '@/api/data/trainingmanagement'

/** 培训管理 表单 */
defineOptions({ name: 'TrainingManagementForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  serialNumber: undefined,
  processName: undefined,
  initiationTime: undefined,
  initiator: undefined,
  currentStage: undefined,
  remark: undefined
})
const formRules = reactive({
  serialNumber: [{ required: true, message: '编号ID不能为空', trigger: 'blur' }],
  processName: [{ required: true, message: '流程名称不能为空', trigger: 'blur' }],
  initiationTime: [{ required: true, message: '发起时间不能为空', trigger: 'blur' }],
  initiator: [{ required: true, message: '发起人不能为空', trigger: 'blur' }],
  currentStage: [{ required: true, message: '当前环节不能为空', trigger: 'blur' }]
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
      formData.value = await TrainingManagementApi.getTrainingManagement(id)
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
    const data = formData.value as unknown as TrainingManagement
    if (formType.value === 'create') {
      await TrainingManagementApi.createTrainingManagement(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrainingManagementApi.updateTrainingManagement(data)
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
    serialNumber: undefined,
    processName: undefined,
    initiationTime: undefined,
    initiator: undefined,
    currentStage: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12"
          ><el-form-item label="培训主题" prop="trainingTheme">
            <el-input
              v-model="formData.trainingTheme"
              placeholder="请输入培训主题"
            /> </el-form-item
        ></el-col>
        <el-col :span="12">
          <el-form-item label="客户名称" prop="customerName">
            <el-input v-model="formData.customerName" placeholder="请输入客户名称" /> </el-form-item
        ></el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训内容" prop="trainingContent">
            <el-input
              v-model="formData.trainingContent"
              type="textarea"
              placeholder="请输入培训内容"
            /> </el-form-item
        ></el-col>
        <el-col :span="12">
          <el-form-item label="培训类型" prop="trainingType">
            <el-select v-model="formData.trainingType" placeholder="请选择培训类型">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select> </el-form-item
        ></el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训对象" prop="trainingTarget">
            <el-input
              v-model="formData.trainingTarget"
              placeholder="请输入培训对象"
            /> </el-form-item
        ></el-col>
        <el-col :span="12">
          <el-form-item label="培训时间" prop="trainingTime">
            <el-date-picker
              v-model="formData.trainingTime"
              type="date"
              value-format="x"
              placeholder="选择培训时间"
              class="!w-260px"
            /> </el-form-item
        ></el-col>
      </el-row>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { TrainingRequirementsApi, TrainingRequirements } from '@/api/data/trainingrequirements'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** 培训需求 表单 */
defineOptions({ name: 'TrainingRequirementsForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  trainingTheme: undefined,
  customerName: undefined,
  trainingContent: undefined,
  trainingType: undefined,
  trainingTarget: undefined,
  trainingTime: undefined,
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
      formData.value = await TrainingRequirementsApi.getTrainingRequirements(id)
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
    const data = formData.value as unknown as TrainingRequirements
    if (formType.value === 'create') {
      await TrainingRequirementsApi.createTrainingRequirements(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrainingRequirementsApi.updateTrainingRequirements(data)
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
    trainingTheme: undefined,
    customerName: undefined,
    trainingContent: undefined,
    trainingType: undefined,
    trainingTarget: undefined,
    trainingTime: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

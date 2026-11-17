<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="140px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训计划编号" prop="annualPlanProjectNumberId">
            <el-input
              v-model="formData.annualPlanProjectNumberId"
              placeholder="请输入培训计划编号"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="培训课程" prop="trainingProgramsCourses">
            <el-input
              v-model="formData.trainingProgramsCourses"
              placeholder="请输入培训课程"
            /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训对象" prop="trainingTargets">
            <el-input
              v-model="formData.trainingTargets"
              placeholder="请输入培训对象"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="培训人数" prop="numberOfTrainees">
            <el-input
              v-model="formData.numberOfTrainees"
              placeholder="请输入培训人数"
            /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训时间" prop="trainingTime">
            <el-date-picker
              v-model="formData.trainingTime"
              type="date"
              value-format="x"
              placeholder="选择培训时间"
              class="!w-240px"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="培训地点" prop="trainingLocation">
            <el-input
              v-model="formData.trainingLocation"
              placeholder="请输入培训地点"
            /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="项目负责人" prop="projectLeader">
            <el-input
              v-model="formData.projectLeader"
              placeholder="请输入项目负责人"
            /> </el-form-item></el-col
        ><el-col :span="12"
      /></el-row>

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
import { TrainingPlanApi, TrainingPlan } from '@/api/data/trainingplan'

/** 培训计划 表单 */
defineOptions({ name: 'TrainingPlanForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  annualPlanProjectNumberId: undefined,
  trainingProgramsCourses: undefined,
  trainingTargets: undefined,
  numberOfTrainees: undefined,
  trainingTime: undefined,
  trainingLocation: undefined,
  projectLeader: undefined,
  remark: undefined
})
const formRules = reactive({
  annualPlanProjectNumberId: [
    { required: true, message: '年度计划项目编号不能为空', trigger: 'blur' }
  ],
  trainingProgramsCourses: [{ required: true, message: '培训项目/课程不能为空', trigger: 'blur' }],
  trainingTargets: [{ required: true, message: '培训对象不能为空', trigger: 'blur' }],
  numberOfTrainees: [{ required: true, message: '培训人数不能为空', trigger: 'blur' }],
  trainingTime: [{ required: true, message: '培训时间不能为空', trigger: 'blur' }],
  trainingLocation: [{ required: true, message: '培训地点不能为空', trigger: 'blur' }],
  projectLeader: [{ required: true, message: '项目负责人不能为空', trigger: 'blur' }]
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
      formData.value = await TrainingPlanApi.getTrainingPlan(id)
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
    const data = formData.value as unknown as TrainingPlan
    if (formType.value === 'create') {
      await TrainingPlanApi.createTrainingPlan(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrainingPlanApi.updateTrainingPlan(data)
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
    annualPlanProjectNumberId: undefined,
    trainingProgramsCourses: undefined,
    trainingTargets: undefined,
    numberOfTrainees: undefined,
    trainingTime: undefined,
    trainingLocation: undefined,
    projectLeader: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

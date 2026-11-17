<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="编号" prop="serialNumber">
        <el-input v-model="formData.serialNumber" placeholder="请输入编号" />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入姓名" />
      </el-form-item>
      <el-form-item label="课程名称" prop="courseName">
        <el-input v-model="formData.courseName" placeholder="请输入课程名称" />
      </el-form-item>
      <el-form-item label="学习时间" prop="learningTime">
        <el-date-picker
          v-model="formData.learningTime"
          type="date"
          value-format="x"
          placeholder="选择学习时间"
        />
      </el-form-item>
      <el-form-item label="是否完成，0是1否" prop="isCompleted">
        <el-input v-model="formData.isCompleted" placeholder="请输入是否完成，0是1否" />
      </el-form-item>
      <el-form-item label="完成形式" prop="completedForm">
        <el-input v-model="formData.completedForm" placeholder="请输入完成形式" />
      </el-form-item>
      <el-form-item label="成绩" prop="score">
        <el-input v-model="formData.score" placeholder="请输入成绩" />
      </el-form-item>
      <el-form-item label="教员" prop="instructor">
        <el-input v-model="formData.instructor" placeholder="请输入教员" />
      </el-form-item>
      <el-form-item label="完成日期" prop="completionDate">
        <el-date-picker
          v-model="formData.completionDate"
          type="date"
          value-format="x"
          placeholder="选择完成日期"
        />
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
import { TrainingAssessmentApi, TrainingAssessment } from '@/api/data/trainingassessment'

/** 培训考核 表单 */
defineOptions({ name: 'TrainingAssessmentForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  serialNumber: undefined,
  name: undefined,
  courseName: undefined,
  learningTime: undefined,
  isCompleted: undefined,
  completedForm: undefined,
  score: undefined,
  instructor: undefined,
  completionDate: undefined,
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
      formData.value = await TrainingAssessmentApi.getTrainingAssessment(id)
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
    const data = formData.value as unknown as TrainingAssessment
    if (formType.value === 'create') {
      await TrainingAssessmentApi.createTrainingAssessment(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrainingAssessmentApi.updateTrainingAssessment(data)
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
    name: undefined,
    courseName: undefined,
    learningTime: undefined,
    isCompleted: undefined,
    completedForm: undefined,
    score: undefined,
    instructor: undefined,
    completionDate: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
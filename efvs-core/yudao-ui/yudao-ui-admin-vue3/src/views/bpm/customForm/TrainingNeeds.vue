<template>
  <h3>培训需求</h3>
  <el-form
    ref="trainingNeedsRef"
    :model="trainingNeedsData"
    :rules="rules"
    label-width="120px"
    class="training-form"
    :disabled="trainNeedsDisabled"
  >
    <!-- 任务编号 -->
    <el-form-item label="任务编号" prop="task_id">
      <el-input v-model="trainingNeedsData.task_id" placeholder="请输入任务编号" />
    </el-form-item>

    <!-- 培训主题 -->
    <el-form-item label="培训主题" prop="training_theme">
      <el-input v-model="trainingNeedsData.training_theme" placeholder="请输入培训主题" />
    </el-form-item>

    <!-- 培训类型 -->
    <el-form-item label="培训类型" prop="training_type">
      <el-select v-model="trainingNeedsData.training_type" placeholder="请选择培训类型">
        <el-option label="实操培训" value="online" />
        <el-option label="理论培训" value="offline" />
      </el-select>
    </el-form-item>

    <!-- 培训对象 -->
    <el-form-item label="培训对象" prop="training_target">
      <el-select v-model="trainingNeedsData.training_target" placeholder="请选择培训对象">
        <el-option label="飞行员" value="all" />
        <el-option label="机务人员" value="department" />
      </el-select>
    </el-form-item>

    <!-- 培训时间 -->
    <el-form-item label="培训时间" prop="training_time">
      <el-date-picker
        v-model="trainingNeedsData.training_time"
        type="daterange"
        range-separator="-"
        start-placeholder="请选择开始培训时间"
        end-placeholder="请选择结束培训时间"
        value-format="YYYY-MM-DD"
        format="YYYY-MM-DD"
      />
    </el-form-item>

    <!-- 培训人数 -->
    <el-form-item label="培训人数" prop="training_number">
      <el-input-number
        v-model="trainingNeedsData.training_number"
        :min="1"
        placeholder="请输入培训人数"
      />
    </el-form-item>

    <!-- 培训时长 -->
    <el-form-item label="培训时长" prop="training_duration">
      <el-input v-model="trainingNeedsData.training_duration" placeholder="请输入培训时长" />
    </el-form-item>

    <!-- 培训地点 -->
    <el-form-item label="培训地点" prop="training_location">
      <el-input v-model="trainingNeedsData.training_location" placeholder="请输入培训地点" />
    </el-form-item>

    <!-- 培训内容 -->
    <el-form-item label="培训内容" prop="training_content">
      <el-input
        v-model="trainingNeedsData.training_content"
        type="textarea"
        :rows="4"
        placeholder="请输入培训内容"
      />
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

// 表单数据模型
interface FormData {
  task_id: string
  training_theme: string
  training_type: string
  training_target: string
  training_time: [string, string] | null
  training_number: number | null
  training_duration: string
  training_location: string
  training_content: string
}
const props = defineProps({
  trainNeedsDisabled: { type: Boolean, default: false, required: true }
})

// 初始化表单数据
const trainingNeedsData = reactive<FormData>({
  task_id: '',
  training_theme: '',
  training_type: '',
  training_target: '',
  training_time: null,
  training_number: null,
  training_duration: '',
  training_location: '',
  training_content: ''
})

// 表单校验规则
const rules = reactive<FormRules<FormData>>({
  task_id: [{ required: true, message: '请输入任务编号', trigger: 'blur' }],
  training_theme: [{ required: true, message: '请输入培训主题', trigger: 'blur' }],
  training_type: [{ required: true, message: '请选择培训类型', trigger: 'change' }],
  training_target: [{ required: true, message: '请选择培训对象', trigger: 'change' }],
  training_time: [{ required: true, message: '请选择培训时间', trigger: 'change' }],
  training_number: [
    { required: true, message: '请输入培训人数', trigger: 'blur' },
    { type: 'number', message: '请输入有效数字', trigger: 'blur' }
  ],
  training_duration: [{ required: true, message: '请输入培训时长', trigger: 'blur' }],
  training_location: [{ required: true, message: '请输入培训地点', trigger: 'blur' }],
  training_content: [{ required: true, message: '请输入培训内容', trigger: 'blur' }]
})

// 表单实例引用
const trainingNeedsRef = ref<FormInstance>()
const validateTrainNeed = async () => await trainingNeedsRef.value?.validate()

// 提交表单方法
const submitForm = async () => {
  await trainingNeedsRef.value?.validate()
  console.log('表单提交成功:', trainingNeedsData)
}

// 重置表单方法
const resetForm = () => {
  trainingNeedsRef.value?.resetFields()
}
defineExpose({
  validateTrainNeed,
  submitForm,
  resetForm
})
</script>

<style scoped>
.training-form {
  max-width: 800px;
  margin: 0 auto;
}
</style>

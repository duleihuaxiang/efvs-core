<template>
  <h3>培训签到记录</h3>
  <el-form
    ref="trainCheckinLogRef"
    :model="trainCheckinLogData"
    :rules="rules"
    label-width="120px"
    class="training-signin-form"
    :disabled="trainCheckinLogDisable"
  >
    <!-- 编号 -->
    <el-form-item label="编号" prop="id">
      <el-input v-model="trainCheckinLogData.id" placeholder="请输入编号" />
    </el-form-item>

    <!-- 姓名 -->
    <el-form-item label="姓名" prop="name">
      <el-input v-model="trainCheckinLogData.name" placeholder="请输入姓名" />
    </el-form-item>

    <!-- 单位名称 -->
    <el-form-item label="单位名称" prop="unit_name">
      <el-input v-model="trainCheckinLogData.unit_name" placeholder="请输入单位名称" />
    </el-form-item>

    <!-- 培训主题 -->
    <el-form-item label="培训主题" prop="training_theme">
      <el-input v-model="trainCheckinLogData.training_theme" placeholder="请输入培训主题" />
    </el-form-item>

    <!-- 培训时间 -->
    <el-form-item label="培训时间" prop="training_time">
      <el-date-picker
        v-model="trainCheckinLogData.training_time"
        type="datetime"
        placeholder="请选择培训时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        format="YYYY-MM-DD HH:mm:ss"
      />
    </el-form-item>

    <!-- 培训讲师 -->
    <el-form-item label="培训讲师" prop="is_trainer">
      <el-radio-group v-model="trainCheckinLogData.is_trainer">
        <el-radio label="是">是</el-radio>
        <el-radio label="否">否</el-radio>
      </el-radio-group>
    </el-form-item>

    <!-- 培训类别 -->
    <el-form-item label="培训类别" prop="training_category">
      <el-select v-model="trainCheckinLogData.training_category" placeholder="请选择培训类别">
        <el-option label="技术培训" value="technical" />
        <el-option label="管理培训" value="management" />
        <el-option label="综合培训" value="comprehensive" />
      </el-select>
    </el-form-item>

    <!-- 培训地点 -->
    <el-form-item label="培训地点" prop="training_location">
      <el-input v-model="trainCheckinLogData.training_location" placeholder="请输入培训地点" />
    </el-form-item>

    <!-- 培训内容 -->
    <el-form-item label="培训内容" prop="training_content">
      <el-input
        v-model="trainCheckinLogData.training_content"
        type="textarea"
        :rows="4"
        placeholder="请输入培训内容吧"
      />
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

// 表单数据模型
interface FormData {
  id: string
  name: string
  unit_name: string
  training_theme: string
  training_time: string | null
  is_trainer: string
  training_category: string
  training_location: string
  training_content: string
}

const props = defineProps({
  trainCheckinLogDisable: {
    type: Boolean,
    default: false,
    required: true
  }
})
// 初始化表单数据
const trainCheckinLogData = reactive<FormData>({
  id: '',
  name: '',
  unit_name: '',
  training_theme: '',
  training_time: null,
  is_trainer: '否',
  training_category: '',
  training_location: '',
  training_content: ''
})

// 表单校验规则
const rules = reactive<FormRules<FormData>>({
  id: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  unit_name: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
  training_theme: [{ required: true, message: '请输入培训主题', trigger: 'blur' }],
  training_time: [{ required: true, message: '请选择培训时间', trigger: 'change' }],
  is_trainer: [{ required: true, message: '请选择是否为培训讲师', trigger: 'change' }],
  training_category: [{ required: true, message: '请选择培训类别', trigger: 'change' }],
  training_location: [{ required: true, message: '请输入培训地点', trigger: 'blur' }],
  training_content: [{ required: true, message: '请输入培训内容', trigger: 'blur' }]
})

// 表单实例引用
const trainCheckinLogRef = ref<FormInstance>()

// 提交表单方法
const submitForm = async () => {
  await trainCheckinLogRef.value?.validate()
  console.log('表单提交成功:', trainCheckinLogData)
}

// 重置表单方法
const resetForm = () => {
  trainCheckinLogRef.value?.resetFields()
}

/** 表单校验 */
const validateTrainCheckinLog = async () => {
  await trainCheckinLogRef.value?.validate()
}

defineExpose({
  validateTrainCheckinLog
})
</script>

<style scoped>
.training-signin-form {
  max-width: 800px;
  margin: 0 auto;
}
</style>

<template>
  <h3>培训考核记录</h3>
  <el-form
    ref="trainAppraiseLogRef"
    :model="trainAppraiseLogData"
    :rules="rules"
    label-width="120px"
    class="training-evaluation-form"
    :disabled="trainAppraiseLogDisabled"
  >
    <!-- 编号 -->
    <el-form-item label="编号" prop="id">
      <el-input v-model="trainAppraiseLogData.id" placeholder="请输入编号" />
    </el-form-item>

    <!-- 姓名 -->
    <el-form-item label="姓名" prop="name">
      <el-input v-model="trainAppraiseLogData.name" placeholder="请输入姓名" />
    </el-form-item>

    <!-- 课程名称 -->
    <el-form-item label="课程名称" prop="course_name">
      <el-input v-model="trainAppraiseLogData.course_name" placeholder="请输入课程名称" />
    </el-form-item>

    <!-- 学习时间 -->
    <el-form-item label="学习时间" prop="study_time">
      <el-input-number
        v-model="trainAppraiseLogData.study_time"
        :min="0"
        placeholder="请输入学习时间"
      />
    </el-form-item>

    <!-- 是否完成 -->
    <el-form-item label="是否完成" prop="is_completed">
      <el-radio-group v-model="trainAppraiseLogData.is_completed">
        <el-radio label="是">是</el-radio>
        <el-radio label="否">否</el-radio>
      </el-radio-group>
    </el-form-item>

    <!-- 完成形式 -->
    <el-form-item label="完成形式" prop="completion_type">
      <el-select v-model="trainAppraiseLogData.completion_type" placeholder="请选择">
        <el-option label="线上学习" value="online" />
        <el-option label="线下培训" value="offline" />
        <el-option label="混合学习" value="hybrid" />
      </el-select>
    </el-form-item>

    <!-- 成绩 -->
    <el-form-item label="成绩" prop="score">
      <el-input v-model="trainAppraiseLogData.score" placeholder="请输入成绩" />
    </el-form-item>

    <!-- 教员 -->
    <el-form-item label="教员" prop="instructor">
      <el-select v-model="trainAppraiseLogData.instructor" placeholder="请选择教员">
        <el-option label="张三" value="zhangsan" />
        <el-option label="李四" value="lisi" />
        <el-option label="王五" value="wangwu" />
      </el-select>
    </el-form-item>

    <!-- 完成日期 -->
    <el-form-item label="完成日期" prop="completion_date">
      <el-date-picker
        v-model="trainAppraiseLogData.completion_date"
        type="date"
        placeholder="请选择完成日期"
        value-format="YYYY-MM-DD"
        format="YYYY-MM-DD"
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
  course_name: string
  study_time: number | null
  is_completed: string
  completion_type: string
  score: string
  instructor: string
  completion_date: string | null
}

const props = defineProps({
  trainAppraiseLogDisabled: { type: Boolean, default: false, required: true }
})

// 初始化表单数据
const trainAppraiseLogData = reactive<FormData>({
  id: '',
  name: '',
  course_name: '',
  study_time: null,
  is_completed: '否',
  completion_type: '',
  score: '',
  instructor: '',
  completion_date: null
})

// 表单校验规则
const rules = reactive<FormRules<FormData>>({
  id: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  course_name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  study_time: [
    { required: true, message: '请输入学习时间', trigger: 'blur' },
    { type: 'number', message: '请输入有效数字', trigger: 'blur' }
  ],
  is_completed: [{ required: true, message: '请选择是否完成', trigger: 'change' }],
  completion_type: [{ required: true, message: '请选择完成形式', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  instructor: [{ required: true, message: '请选择教员', trigger: 'change' }],
  completion_date: [{ required: true, message: '请选择完成日期', trigger: 'change' }]
})

// 表单实例引用
const trainAppraiseLogRef = ref<FormInstance>()

// 提交表单方法
const submitForm = async () => {
  await trainAppraiseLogRef.value?.validate()
  console.log('表单提交成功:', trainAppraiseLogData)
}

// 重置表单方法
const resetForm = () => {
  trainAppraiseLogRef.value?.resetFields()
}
/** 表单校验 */
const validateTrainAppraise = async () => {
  await trainAppraiseLogRef.value?.validate()
}
defineExpose({
  validateTrainAppraise
})
</script>

<style scoped>
.training-evaluation-form {
  max-width: 800px;
  margin: 0 auto;
}
</style>

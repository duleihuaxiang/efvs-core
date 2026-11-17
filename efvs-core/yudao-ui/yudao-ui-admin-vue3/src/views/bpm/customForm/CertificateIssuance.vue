<template>
  <h3>证书发放</h3>
  <el-form
    ref="certificateRef"
    :model="certificateData"
    :rules="rules"
    label-width="120px"
    class="certificate-issue-form"
    :disabled="certificateDisabled"
  >
    <!-- 编号 -->
    <el-form-item label="编号" prop="id">
      <el-input v-model="certificateData.id" placeholder="请输入编号" />
    </el-form-item>

    <!-- 姓名 -->
    <el-form-item label="姓名" prop="name">
      <el-input v-model="certificateData.name" placeholder="请输入姓名" />
    </el-form-item>

    <!-- 成绩 -->
    <el-form-item label="成绩" prop="score">
      <el-input v-model="certificateData.score" placeholder="请输入成绩" />
    </el-form-item>

    <!-- 证书上传 -->
    <el-form-item label="证书上传" prop="certificate_file">
      <UploadFile v-model="certificateData.certificate_file" class="min-w-80px" />
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = defineProps({
  certificateDisabled: { type: Boolean, default: false, required: true }
})
// 表单数据模型
interface FormData {
  id: string
  name: string
  score: string
  certificate_file: Array<string>
}

// 初始化表单数据
const certificateData = reactive<FormData>({
  id: '',
  name: '',
  score: '',
  certificate_file: []
})

// 表单校验规则
const rules = reactive<FormRules<FormData>>({
  id: [{ required: true, message: '请输入编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  certificate_file: [{ required: true, message: '请上传证书文件', trigger: 'change' }]
})

// 表单实例引用
const certificateRef = ref<FormInstance>()

// 提交表单方法
const submitForm = async () => {
  await certificateRef.value?.validate()
  console.log('表单提交成功:', certificateData)
}

// 重置表单方法
const resetForm = () => {
  certificateRef.value?.resetFields()
}
/** 表单校验 */
const validateCertificate = async () => {
  await certificateRef.value?.validate()
}

defineExpose({
  validateCertificate
})
</script>

<style scoped>
.certificate-issue-form {
  max-width: 800px;
  margin: 0 auto;
}
</style>

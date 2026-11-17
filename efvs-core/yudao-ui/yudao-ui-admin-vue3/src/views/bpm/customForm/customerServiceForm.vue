<template>
  <el-form
    ref="customerServiceRef"
    :model="customerServiceData"
    label-width="120px"
    :disabled="customerDisabled"
    :rules="rules"
  >
    <!-- 问题编号 -->
    <el-form-item label="问题编号" prop="problem_id">
      <el-input v-model="customerServiceData.problem_id" />
    </el-form-item>

    <!-- 期望答复时间 -->
    <el-form-item label="期望答复时间" prop="response_time">
      <el-time-picker v-model="customerServiceData.response_time" />
    </el-form-item>

    <!-- 优先级 -->
    <el-form-item label="优先级" prop="priority">
      <el-input v-model="customerServiceData.priority" />
    </el-form-item>

    <!-- 版本 -->
    <el-form-item label="版本" prop="problem_version">
      <el-input v-model="customerServiceData.problem_version" />
    </el-form-item>

    <!-- 问题输入方式 -->
    <el-form-item label="问题输入方式" prop="problem_input">
      <el-input v-model="customerServiceData.problem_input" />
    </el-form-item>

    <!-- 客户名称 -->
    <el-form-item label="客户名称" prop="customer_name">
      <el-input v-model="customerServiceData.customer_name" />
    </el-form-item>

    <!-- 联系人 -->
    <el-form-item label="联系人" prop="contact">
      <el-input v-model="customerServiceData.contact" />
    </el-form-item>

    <!-- 机型 -->
    <el-form-item label="机型" prop="aircraft_model">
      <el-input v-model="customerServiceData.aircraft_model" />
    </el-form-item>

    <!-- 注册号 -->
    <el-form-item label="注册号" prop="registration_number">
      <el-input v-model="customerServiceData.registration_number" />
    </el-form-item>

    <!-- 设备型号 -->
    <el-form-item label="设备型号" prop="device_model">
      <el-input v-model="customerServiceData.device_model" />
    </el-form-item>

    <!-- 序列号 -->
    <el-form-item label="序列号" prop="serial_number">
      <el-input v-model="customerServiceData.serial_number" />
    </el-form-item>

    <!-- 交付周期 -->
    <el-form-item label="交付周期" prop="delivery_cycle">
      <el-input v-model="customerServiceData.delivery_cycle" />
    </el-form-item>

    <!-- 问题描述 -->
    <el-form-item label="问题描述" prop="problem_description">
      <el-input v-model="customerServiceData.problem_description" />
    </el-form-item>

    <!-- 客户期望 -->
    <el-form-item label="客户期望" prop="customer_expectations">
      <el-input v-model="customerServiceData.customer_expectations" />
    </el-form-item>

    <!-- 客户附件 -->
    <el-form-item label="客户附件" prop="customer_attachment">
      <UploadFile v-model="customerServiceData.customer_attachment" />
    </el-form-item>

    <!-- 问题答复 -->
    <el-form-item label="问题答复" prop="problem_response">
      <el-input v-model="customerServiceData.problem_response" />
    </el-form-item>

    <!-- 答复附件 -->
    <el-form-item label="答复附件" prop="response_attachment">
      <UploadFile v-model="customerServiceData.response_attachment" />
    </el-form-item>

    <!-- 创建人 -->
    <el-form-item label="创建人" prop="creator">
      <el-input v-model="customerServiceData.creator" />
    </el-form-item>

    <!-- 批准人 -->
    <el-form-item label="批准人" prop="updater">
      <el-input v-model="customerServiceData.updater" />
    </el-form-item>
    <el-form-item label="状态" prop="status" class="mb-20px" v-if="false">
      <el-input v-model="customerServiceData.status" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  customerDisabled: { type: Boolean, default: false, required: true }
})
// 表单数据
const customerServiceData = ref({
  problem_id: '',
  response_time: '',
  priority: '',
  problem_version: '',
  problem_input: '',
  customer_name: '',
  contact: '',
  aircraft_model: '',
  registration_number: '',
  device_model: '',
  serial_number: '',
  delivery_cycle: '',
  problem_description: '',
  customer_expectations: '',
  customer_attachment: [],
  problem_response: '',
  response_attachment: [],
  creator: '',
  updater: '',
  formType: 'quickResponse',
  tableName: 'efvs_customer_service_form'
})

// 表单引用
const customerServiceRef = ref()

// 校验规则
const rules = {
  problem_id: [{ required: true, message: '问题编号不能为空', trigger: 'blur' }],
  response_time: [{ required: true, message: '期望答复时间不能为空', trigger: 'blur' }],
  priority: [{ required: true, message: '优先级不能为空', trigger: 'blur' }],
  problem_version: [{ required: true, message: '版本不能为空', trigger: 'blur' }],
  problem_input: [{ required: true, message: '问题输入方式不能为空', trigger: 'blur' }],
  customer_name: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
  contact: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  aircraft_model: [{ required: true, message: '机型不能为空', trigger: 'blur' }],
  registration_number: [{ required: true, message: '注册号不能为空', trigger: 'blur' }],
  device_model: [{ required: true, message: '设备型号不能为空', trigger: 'blur' }],
  serial_number: [{ required: true, message: '序列号不能为空', trigger: 'blur' }],
  delivery_cycle: [{ required: true, message: '交付周期不能为空', trigger: 'blur' }],
  problem_description: [{ required: true, message: '问题描述不能为空', trigger: 'blur' }],
  customer_expectations: [{ required: true, message: '客户期望不能为空', trigger: 'blur' }],
  customer_attachment: [{ required: true, message: '客户附件不能为空', trigger: 'blur' }],
  problem_response: [{ required: true, message: '问题答复不能为空', trigger: 'blur' }],
  response_attachment: [{ required: true, message: '答复附件不能为空', trigger: 'blur' }],
  creator: [{ required: true, message: '创建人不能为空', trigger: 'blur' }],
  updater: [{ required: true, message: '批准人不能为空', trigger: 'blur' }]
}

/** 表单校验 */
const validateCustomer = async () => {
  await customerServiceRef.value.validate()
}
/** 表单验证方法 - 供外部调用（如审批通过时） */
const validate = async (): Promise<boolean> => {
  if (!customerServiceRef.value) {
    return true
  }
  try {
    await customerServiceRef.value.validate()
    return true
  } catch {
    return false
  }
}

const emit = defineEmits(['update:customerServiceData'])
defineExpose({
  formFlag: 'customerService',
  formData: customerServiceData,
  validate
})
</script>

<style scoped>
/* 自定义样式 */
.el-form-item {
  margin-bottom: 20px;
}
</style>

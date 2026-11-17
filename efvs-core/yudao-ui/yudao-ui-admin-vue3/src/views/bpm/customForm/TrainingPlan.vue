<template>
  <el-form
    ref="trainPlanRef"
    :model="trainPlanData"
    :rules="rules"
    label-width="120px"
    :disabled="trainPlanDisabled"
  >
    <el-form-item label="培训主题" prop="trainingTheme">
      <el-input v-model="trainPlanData.trainingTheme" placeholder="请输入培训主题" />
    </el-form-item>
    <el-row :gutter="20"
      ><el-col :span="12"
        ><!-- 培训计划编号 自动生成：年度+3位递增整数-->
        <el-form-item label="培训计划编号" prop="annualPlanProjectNumberId">
          <el-input
            v-model="trainPlanData.annualPlanProjectNumberId"
            disabled
          /> </el-form-item></el-col
      ><el-col :span="12">
        <!-- 培训课程 -->
        <el-form-item label="培训课程" prop="trainingProgramsCourses">
          <el-input
            v-model="trainPlanData.trainingProgramsCourses"
            placeholder="请输入课程"
          /> </el-form-item></el-col
    ></el-row>
    <el-row :gutter="20"
      ><el-col :span="12"
        ><!-- 培训对象 -->
        <el-form-item label="培训对象" prop="trainingTargets">
          <el-input
            v-model="trainPlanData.trainingTargets"
            placeholder="请输入培训对象"
          /> </el-form-item></el-col
      ><el-col :span="12">
        <!-- 培训人数 -->
        <el-form-item label="培训人数" prop="numberOfTrainees">
          <el-input
            v-model="trainPlanData.numberOfTrainees"
            :min="1"
            placeholder="请输入培训人数"
          /> </el-form-item></el-col
    ></el-row>

    <!-- 培训时间 -->
    <el-form-item label="培训时间" prop="trainingTimeArray">
      <el-date-picker
        v-model="trainPlanData.trainingTimeArray"
        type="daterange"
        range-separator="-"
        start-placeholder="请选择开始培训时间"
        end-placeholder="请选择结束培训时间"
        value-format="YYYY-MM-DD"
        format="YYYY-MM-DD"
      />
    </el-form-item>

    <!-- 培训地点 -->
    <el-form-item label="培训地点" prop="trainingLocation">
      <el-input v-model="trainPlanData.trainingLocation" placeholder="请输入培训地点" />
    </el-form-item>

    <!-- 项目负责人 -->
    <el-form-item label="项目负责人" prop="projectLeader">
      <el-select
        v-model="trainPlanData.projectLeader"
        clearable
        style="width: 100%"
        placeholder="请选择项目负责人"
      >
        <el-option
          v-for="item in projectUserList"
          :key="item.id"
          :label="item.nickname"
          :value="item.id"
        />
      </el-select>
    </el-form-item>

    <!-- 备注 -->
    <el-form-item label="备注" prop="remark">
      <el-input v-model="trainPlanData.remark" placeholder="请输入备注" type="textarea" :rows="4" />
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import * as UserApi from '@/api/system/user'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'

// 表单数据模型
interface FormData {
  trainingTheme: string
  annualPlanProjectNumberId: string
  trainingProgramsCourses: string
  trainingTargets: string
  numberOfTrainees: null
  trainingTimeArray: Array<string>
  trainingStartTime: string
  trainingEndTime: string
  trainingLocation: string
  projectLeader: string
  remark: string
  formType: string
}

const props = defineProps({
  trainPlanDisabled: { type: Boolean, default: false, required: true }
})
// 初始化表单数据
const trainPlanData = ref<FormData>({
  trainingTheme: '',
  annualPlanProjectNumberId: '',
  trainingProgramsCourses: '',
  trainingTargets: '',
  numberOfTrainees: null,
  trainingTimeArray: [],
  trainingStartTime: '',
  trainingEndTime: '',
  trainingLocation: '',
  projectLeader: '',
  remark: '',
  formType: 'trainingPlan'
})

// 表单校验规则
const rules = reactive<FormRules<FormData>>({
  annualPlanProjectNumberId: [{ required: true, message: '请输入培训计划编号', trigger: 'blur' }],
  trainingTheme: [{ required: true, message: '请输入培训主题', trigger: 'blur' }],
  trainingProgramsCourses: [{ required: true, message: '请输入培训课程', trigger: 'blur' }],
  trainingTargets: [{ required: true, message: '请输入培训对象', trigger: 'blur' }],
  numberOfTrainees: [{ required: true, message: '请输入培训人数', trigger: 'blur' }],
  trainingTimeArray: [{ required: true, message: '请选择培训时间', trigger: 'blur' }],
  trainingLocation: [{ required: true, message: '请输入培训地点', trigger: 'blur' }],
  projectLeader: [{ required: true, message: '请选择项目负责人', trigger: 'blur' }]
})

// 表单实例引用
const trainPlanRef = ref<FormInstance>()
const projectUserList = ref<any[]>([])
/** 查询项目负责人下拉列表 */
const getProjectUserList = async () => {
  const data = await UserApi.getUserListByDeptId()
  projectUserList.value = data
}
// 提交表单方法
const submitForm = async () => {
  await trainPlanRef.value?.validate()
  console.log('表单提交成功:', trainPlanData)
}

// 重置表单方法
const resetForm = () => {
  trainPlanRef.value?.resetFields()
}
/** 表单校验 */
const validatePlan = async () => {
  await trainPlanRef.value?.validate()
}
const generateNumber = async () => {
  return await ProcessInstanceApi.generateNumber('training')
}
onMounted(async () => {
  if (!props.trainPlanDisabled) {
    trainPlanData.value.annualPlanProjectNumberId = await generateNumber()
  }

  getProjectUserList()
})
defineExpose({
  submitForm,
  resetForm,
  validatePlan,
  variablesData: { trainPlanData: trainPlanData, formType: 'trainingPlan' }
})
</script>

<style scoped>
.annual-plan-form {
  max-width: 800px;
  margin: 0 auto;
}
</style>

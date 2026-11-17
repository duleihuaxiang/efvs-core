<template>
  <el-form
    ref="preparationRef"
    :model="preparationData"
    :rules="rules"
    label-width="120px"
    :disabled="preparationDisabled"
  >
    <el-form-item>
      <el-table
        border
        ref="tableBox"
        :data="preparationData"
        :row-style="{ height: '70px' }"
        :cell-style="{ padding: '0px' }"
        class="mb-4"
      >
        <el-table-column type="index" width="55" align="center" />
        <el-table-column prop="preTrainingMatters" label="培训前事项" align="center">
          <template #header>
            <span><span class="required-mark">*</span>培训前事项</span>
          </template>
          <template #default="scope">
            <!-- :prop="'preparationData.' + scope.$index + '.preTrainingMatters'"
              :rules="{ required: true, message: '培训前事项不能为空', trigger: 'blur' }" -->
            <el-form-item>
              <span style="font-size: 12px; color: red">{{ scope.row.tooltipContent }}</span>
              <el-input
                style="margin-top: 0"
                v-model="scope.row.preTrainingMatters"
                maxlength="200"
                oninput="if(value.length > 200) value=value.slice(0, 200)"
                placeholder="请输入"
              />
            </el-form-item>
          </template>
        </el-table-column>
        <!-- (0 否 1 是) -->
        <el-table-column prop="isCompleted" label="*是否完成" align="center" width="150">
          <template #header>
            <span><span class="required-mark">*</span>是否完成</span>
          </template>
          <template #default="scope">
            <!-- <el-form-item
              :prop="'preparationData.' + scope.$index + '.isCompleted'"
              :rules="{ required: true, message: '完成后才可提交', trigger: 'blur' }"
            > -->
            <el-checkbox
              label="是"
              size="large"
              :model-value="scope.row.isCompleted === 1"
              @update:model-value="handleCheckboxChange(scope.row, $event)"
            />
            <!-- </el-form-item> -->
            <!-- :rules="{ required: true, message: '完成后才可提交', trigger: 'blur' }" -->
          </template>
        </el-table-column>
      </el-table></el-form-item
    >
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import * as FormApi from '@/api/bpm/form'

const message = useMessage()

// 表单数据模型
interface PreparationTableData {
  preTrainingMatters: string
  isCompleted: number
  tooltipContent: string
  processInstanceId: string
  // formType: string
}

const props = defineProps({
  preparationDisabled: { type: Boolean, default: false, required: true },
  processInstanceId: { type: String, default: '', required: false }
})

// 初始化表单数据
const preparationData = ref<PreparationTableData[]>([])

// 表单校验规则
const rules = reactive({
  /*  preTrainingMatters: [{ required: true, message: '请输入培训前事项', trigger: 'blur' }],
  isCompleted: [{ required: true, message: '请选择是否完成', trigger: 'change' }] */
})

// 表单实例引用
const preparationRef = ref<FormInstance>()
const tooltipContentArry = [
  '完成培训需求收集并和客户确认',
  '通知学员培训时间、地点、培训内容',
  '课件准备、考试试题准备、教具准备',
  '准备三餐、休息间隙零食、饮水、住宿、培训和会议场地、交通用车',
  '培训资料、工具和器材准备（电脑、数码投影仪、激光笔、白板、书写设备、扩音设备等）',
  '审查学员资质',
  '其他事项'
]
// 在接收后端数据后，为每个项目添加默认 tooltipContent
const initializeData = (backendData: any[]) => {
  if (!backendData || backendData.length === 0) {
    return (backendData = tooltipContentArry.map((item) => {
      return {
        preTrainingMatters: '',
        isCompleted: 0,
        tooltipContent: item,
        processInstanceId: props.processInstanceId
      }
    }))
  }
  return backendData.map((item, index) => ({
    ...item,
    tooltipContent: tooltipContentArry[index],
    processInstanceId: props.processInstanceId
  }))
}

onMounted(async () => {
  // if (props.preparationDisabled) {
  const getTrainingPreparationData = await FormApi.getPreTrainingPreparation({
    processInstanceId: props.processInstanceId
  })
  if (!getTrainingPreparationData) {
    /*    message.error('查询不到培训前准备工作表单详情信息！')
      return */

    const PpqlListData = await FormApi.getPpqlList()
    preparationData.value = PpqlListData.map((item) => {
      return {
        preTrainingMatters: '',
        isCompleted: 0,
        tooltipContent: item.problemDescription,
        ppqlId: item.id,
        processInstanceId: props.processInstanceId
      }
    })
  }
  preparationData.value = initializeData(getTrainingPreparationData)
  // }
})

// 提交表单方法
/* const submitForm = async () => {
  await preparationRef.value?.validate()
  // console.log('表单提交成功:', preparationData)
} */

// 重置表单方法
/* const resetForm = () => {
  preparationRef.value?.resetFields()
} */
const handleCheckboxChange = (row: PreparationTableData, checked: boolean) => {
  row.isCompleted = checked ? 1 : 0
}
/** 表单校验 */
const validatePreparation = async () => {
  // 检查是否有未完成的项目
  // 排除最后一行，检查其他行是否有未完成的项目
  const dataToValidate = preparationData.value.slice(0, -1) // 除最后一行外的所有数据
  const uncompletedItems = dataToValidate.filter((item) => item.isCompleted !== 1)
  if (uncompletedItems.length > 0) {
    throw new Error('请完成所有培训前准备工作项目')
  }

  // 检查是否有未填写的事项
  const emptyMatters = dataToValidate.filter((item) => !item.preTrainingMatters)
  if (emptyMatters.length > 0) {
    throw new Error('请填写所有培训前事项')
  }
  // await preparationRef.value?.validate()
}

defineExpose({
  variablesData: {
    PreparationList: preparationData,
    formType: 'preTrainingPreparation'
  },
  validatePreparation
})
</script>

<style scoped>
.training-preparation-form {
  max-width: 800px;
  margin: 0 auto;
}

.required-mark {
  margin-right: 2px;
  color: red;
}
</style>

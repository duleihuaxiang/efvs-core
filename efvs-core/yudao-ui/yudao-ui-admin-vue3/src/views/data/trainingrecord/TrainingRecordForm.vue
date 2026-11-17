<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20"
        ><el-col :span="12"
          ><el-form-item label="编号" prop="serialNumber">
            <el-input
              v-model="formData.serialNumber"
              placeholder="请输入编号"
            /> </el-form-item></el-col
        ><el-col :span="12"
          ><el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名" /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20"
        ><el-col :span="12">
          <el-form-item label="课程名称" prop="courseName">
            <el-input
              v-model="formData.courseName"
              placeholder="请输入课程名称"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="学习时间" prop="learningTime">
            <el-date-picker
              v-model="formData.learningTime"
              type="date"
              value-format="x"
              placeholder="选择学习时间"
              class="!w-260px"
            /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20"
        ><el-col :span="12"
          ><el-form-item label="是否完成" prop="isCompleted">
            <el-select v-model="formData.isCompleted" placeholder="请选择是否完成" clearable>
              <el-option
                v-for="item in isCompletedOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="完成形式" prop="perfectiveForm">
            <el-input
              v-model="formData.perfectiveForm"
              placeholder="请输入完成形式"
            /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20"
        ><el-col :span="12">
          <el-form-item label="成绩" prop="grades">
            <el-input v-model="formData.grades" placeholder="请输入成绩" /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="教员" prop="instructor">
            <el-input
              v-model="formData.instructor"
              placeholder="请输入教员"
            /> </el-form-item></el-col
      ></el-row>

      <el-row :gutter="20"
        ><el-col :span="12">
          <el-form-item label="完成日期" prop="completionDate">
            <el-date-picker
              v-model="formData.completionDate"
              type="date"
              value-format="x"
              placeholder="选择完成日期"
              class="!w-260px"
            />
          </el-form-item>
        </el-col>
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
import { TrainingRecordApi, TrainingRecord } from '@/api/data/trainingrecord'

/** 培训记录 表单 */
defineOptions({ name: 'TrainingRecordForm' })

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
  perfectiveForm: undefined,
  grades: undefined,
  instructor: undefined,
  completionDate: undefined,
  remark: undefined
})
const formRules = reactive({
  serialNumber: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  courseName: [{ required: true, message: '课程名称不能为空', trigger: 'blur' }],
  learningTime: [{ required: true, message: '学习时间不能为空', trigger: 'blur' }],
  isCompleted: [{ required: true, message: '是否完成 (0 否 1 是)不能为空', trigger: 'blur' }],
  perfectiveForm: [{ required: true, message: '完成形式不能为空', trigger: 'blur' }],
  grades: [{ required: true, message: '成绩不能为空', trigger: 'blur' }],
  instructor: [{ required: true, message: '教员不能为空', trigger: 'blur' }],
  completionDate: [{ required: true, message: '完成日期不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const isCompletedOptions = [
  {
    value: 0,
    label: '否'
  },
  {
    value: 1,
    label: '是'
  }
]
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
      formData.value = await TrainingRecordApi.getTrainingRecord(id)
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
    const data = formData.value as unknown as TrainingRecord
    if (formType.value === 'create') {
      await TrainingRecordApi.createTrainingRecord(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrainingRecordApi.updateTrainingRecord(data)
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
    perfectiveForm: undefined,
    grades: undefined,
    instructor: undefined,
    completionDate: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

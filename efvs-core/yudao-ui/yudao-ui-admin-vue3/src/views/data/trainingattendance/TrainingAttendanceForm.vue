<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="800">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="编号" prop="serialNumber">
            <el-input v-model="formData.serialNumber" placeholder="请输入编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名 " prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名 " />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="单位名称" prop="unitName">
            <el-input v-model="formData.unitName" placeholder="请输入单位名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="培训主题" prop="trainingTheme">
            <el-input v-model="formData.trainingTheme" placeholder="请输入培训主题" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训时间" prop="trainingTime">
            <el-date-picker
              v-model="formData.trainingTime"
              type="date"
              value-format="x"
              placeholder="选择培训时间"
              class="!w-260px"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <!--  (0 否 1 是) -->
          <el-form-item label="培训讲师" prop="trainingInstructor">
            <el-select
              v-model="formData.trainingInstructor"
              placeholder="请选择是否培训讲师"
              clearable
            >
              <el-option
                v-for="item in istrainingInstructor"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="培训类别" prop="trainingCategory">
            <el-select v-model="formData.trainingCategory" placeholder="请选择培训类别" clearable>
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="培训地点" prop="trainingLocation">
            <el-input v-model="formData.trainingLocation" placeholder="请输入培训地点" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="培训内容" prop="trainingContent">
        <Editor v-model="formData.trainingContent" height="150px" />
      </el-form-item>

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
import { TrainingAttendanceApi, TrainingAttendance } from '@/api/data/trainingattendance'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
/** 培训签到 表单 */
defineOptions({ name: 'TrainingAttendanceForm' })

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
  unitName: undefined,
  trainingTheme: undefined,
  trainingTime: undefined,
  trainingInstructor: undefined,
  trainingCategory: undefined,
  trainingLocation: undefined,
  trainingContent: undefined,
  remark: undefined
})
const formRules = reactive({
  serialNumber: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '姓名 不能为空', trigger: 'blur' }],
  unitName: [{ required: true, message: '单位名称不能为空', trigger: 'blur' }],
  trainingTheme: [{ required: true, message: '培训主题不能为空', trigger: 'blur' }],
  trainingTime: [{ required: true, message: '培训时间不能为空', trigger: 'blur' }],
  trainingInstructor: [
    { required: true, message: '培训讲师 (0 否 1 是)不能为空', trigger: 'blur' }
  ],
  trainingCategory: [{ required: true, message: '培训类别不能为空', trigger: 'blur' }],
  trainingLocation: [{ required: true, message: '培训地点不能为空', trigger: 'blur' }],
  trainingContent: [{ required: true, message: '培训内容不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const istrainingInstructor = [
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
      formData.value = await TrainingAttendanceApi.getTrainingAttendance(id)
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
    const data = formData.value as unknown as TrainingAttendance
    if (formType.value === 'create') {
      await TrainingAttendanceApi.createTrainingAttendance(data)
      message.success(t('common.createSuccess'))
    } else {
      await TrainingAttendanceApi.updateTrainingAttendance(data)
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
    unitName: undefined,
    trainingTheme: undefined,
    trainingTime: undefined,
    trainingInstructor: undefined,
    trainingCategory: undefined,
    trainingLocation: undefined,
    trainingContent: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

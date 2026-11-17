<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="用户ID" prop="userId">
        <el-select v-model="formData.userId" placeholder="请选择用户ID">
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.nickname"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="培训主题" prop="trainingTheme">
        <el-input v-model="formData.trainingTheme" placeholder="请输入培训主题" />
      </el-form-item>
      <el-form-item label="培训时间" prop="trainingTime">
        <el-date-picker
          v-model="formData.trainingTime"
          type="date"
          value-format="x"
          placeholder="选择培训时间"
        />
      </el-form-item>
      <el-form-item label="培训地点" prop="trainingLocation">
        <el-input v-model="formData.trainingLocation" placeholder="请输入培训地点" />
      </el-form-item>
      <el-form-item label="培训时长" prop="trainingDuration">
        <el-input v-model="formData.trainingDuration" placeholder="请输入培训时长" />
      </el-form-item>
      <el-form-item label="培训成绩" prop="achieveResults">
        <el-input v-model="formData.achieveResults" placeholder="请输入培训成绩" />
      </el-form-item>
      <el-form-item label="培训教师" prop="trainingTeachers">
        <el-input v-model="formData.trainingTeachers" placeholder="请输入培训教师" />
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
import { ExperienceApi, Experience } from '@/api/training/trainingexperience'
import * as UserApi from '@/api/system/user'
const userList = ref([]) // 用户列表数据
/** 培训经历 表单 */
defineOptions({ name: 'ExperienceForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  userId: undefined,
  trainingTheme: undefined,
  trainingTime: undefined,
  trainingLocation: undefined,
  trainingDuration: undefined,
  achieveResults: undefined,
  trainingTeachers: undefined,
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

  // 加载用户列表  还有这个代码
  userList.value = await UserApi.getSimpleUserList()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ExperienceApi.getExperience(id)
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
    const data = formData.value as unknown as Experience
    if (formType.value === 'create') {
      await ExperienceApi.createExperience(data)
      message.success(t('common.createSuccess'))
    } else {
      await ExperienceApi.updateExperience(data)
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
    userId: undefined,
    trainingTheme: undefined,
    trainingTime: undefined,
    trainingLocation: undefined,
    trainingDuration: undefined,
    achieveResults: undefined,
    trainingTeachers: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="适用机型编号" prop="applicableModelNumber">
        <el-input v-model="formData.applicableModelNumber" placeholder="请输入适用机型编号" />
      </el-form-item>
      <el-form-item label="装机飞机编号" prop="installationAircraftNumber">
        <el-input v-model="formData.installationAircraftNumber" placeholder="请输入装机飞机编号" />
      </el-form-item>
      <el-form-item label="装机位置名称" prop="installationLocationName">
        <el-input v-model="formData.installationLocationName" placeholder="请输入装机位置名称" />
      </el-form-item>
      <el-form-item label="适配版本要求" prop="adaptationVersion">
        <el-input v-model="formData.adaptationVersion" placeholder="请输入适配版本要求" />
      </el-form-item>
      <el-form-item label="装机时间" prop="installationTime">
        <el-date-picker
          v-model="formData.installationTime"
          type="date"
          value-format="x"
          placeholder="选择装机时间"
        />
      </el-form-item>
      <el-form-item label="适航适配状态0是1否（是否完成适航验证）" prop="adaptationStatus">
        <el-input v-model="formData.adaptationStatus" placeholder="请输入适航状态" />
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
import { MemoryAttributeApi, MemoryAttribute } from '@/api/data/memoryattribute'

/** 机型属性 表单 */
defineOptions({ name: 'MemoryAttributeForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  applicableModelNumber: undefined,
  installationAircraftNumber: undefined,
  installationLocationName: undefined,
  adaptationVersion: undefined,
  installationTime: undefined,
  adaptationStatus: undefined,
  remark: undefined
})
const formRules = reactive({})
const formRef = ref() // 表单 Ref

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
      formData.value = await MemoryAttributeApi.getMemoryAttribute(id)
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
    const data = formData.value as unknown as MemoryAttribute
    if (formType.value === 'create') {
      await MemoryAttributeApi.createMemoryAttribute(data)
      message.success(t('common.createSuccess'))
    } else {
      await MemoryAttributeApi.updateMemoryAttribute(data)
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
    applicableModelNumber: undefined,
    installationAircraftNumber: undefined,
    installationLocationName: undefined,
    adaptationVersion: undefined,
    installationTime: undefined,
    adaptationStatus: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

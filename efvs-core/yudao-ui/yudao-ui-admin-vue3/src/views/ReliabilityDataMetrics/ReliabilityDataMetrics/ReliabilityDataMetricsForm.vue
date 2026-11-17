<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="部件名称" prop="componentName">
        <el-input v-model="formData.componentName" placeholder="请输入部件名称" />
      </el-form-item>
      <el-form-item label="件号" prop="partNumber">
        <el-input v-model="formData.partNumber" placeholder="请输入件号" />
      </el-form-item>
      <el-form-item label="部件类别" prop="componentCategory">
        <el-input v-model="formData.componentCategory" placeholder="请输入部件类别" />
      </el-form-item>
      <el-form-item label="MTBUR指标" prop="mtbur">
        <el-input v-model="formData.mtbur" placeholder="请输入MTBUR指标" />
      </el-form-item>
      <el-form-item label="MTBF指标" prop="mtbf">
        <el-input v-model="formData.mtbf" placeholder="请输入MTBF指标" />
      </el-form-item>
      <el-form-item label="数量" prop="count">
        <el-input
          v-model.number="formData.count"
          maxlength="10"
          placeholder="请输入数量"
          @input="handleCountInput"
        />
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
import {
  ReliabilityDataMetricsApi,
  ReliabilityDataMetrics
} from '@/api/ReliabilityDataMetrics/ReliabilityDataMetrics'

/** 可靠性指标 表单 */
defineOptions({ name: 'ReliabilityDataMetricsForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  componentName: undefined,
  partNumber: undefined,
  componentCategory: undefined,
  mtbf: undefined,
  mtbur: undefined,
  count: 0,
  remark: undefined
})
const formRules = reactive({
  // remark: [{ required: true, message: '备注不能为空', trigger: 'blur' }]
  count: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (value === undefined || value === null || value === '') return callback()
        const num = typeof value === 'number' ? value : Number(value)
        if (!Number.isFinite(num)) return callback(new Error('数量超出可表示范围，请重新输入'))
        if (!Number.isInteger(num)) return callback(new Error('数量必须为整数'))
        if (num > 2147483647) return callback(new Error('数量不能超过 2147483647'))
        callback()
      },
      trigger: 'blur'
    }
  ]
})
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
      formData.value = await ReliabilityDataMetricsApi.getReliabilityDataMetrics(id)
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
    const data = formData.value as unknown as ReliabilityDataMetrics
    if (formType.value === 'create') {
      await ReliabilityDataMetricsApi.createReliabilityDataMetrics(data)
      message.success(t('common.createSuccess'))
    } else {
      await ReliabilityDataMetricsApi.updateReliabilityDataMetrics(data)
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
    componentName: undefined,
    partNumber: undefined,
    componentCategory: undefined,
    mtbf: undefined,
    mtbur: undefined,
    count: 0,
    remark: undefined
  }
  formRef.value?.resetFields()
}

/** 数量输入保护：v-model.number 底层用 parseFloat 转换，数值超过 Number.MAX_VALUE（约 1.8e308）时会变成 Infinity，这里检测到后重置并提示 */
const handleCountInput = () => {
  const val = formData.value.count
  const num = typeof val === 'string' ? parseFloat(val) : val
  if (!Number.isFinite(num)) {
    formData.value.count = 0
    // message.error('数量超出可表示范围，请重新输入')
  }
}
</script>

<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="可靠性数据收集表单编号" prop="reliabilityDataFormNo">
        <el-input
          v-model="formData.reliabilityDataFormNo"
          placeholder="请输入可靠性数据收集表单编号"
        />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
      </el-form-item>
      <el-form-item label="部件名称" prop="partName">
        <el-input v-model="formData.partName" placeholder="请输入部件名称" />
      </el-form-item>
      <el-form-item label="代号（件号）" prop="partNumber">
        <el-input v-model="formData.partNumber" placeholder="请输入代号（件号）" />
      </el-form-item>
      <el-form-item label="SRU名称" prop="sruName">
        <el-input v-model="formData.sruName" placeholder="请输入SRU名称" />
      </el-form-item>
      <el-form-item label="SRU代号" prop="sruNumber">
        <el-input v-model="formData.sruNumber" placeholder="请输入SRU代号" />
      </el-form-item>
      <el-form-item label="序列号" prop="serialNumber">
        <el-input v-model="formData.serialNumber" placeholder="请输入序列号" />
      </el-form-item>
      <el-form-item label="所属部件代号（所属部件件号）" prop="productPartNumber">
        <el-input
          v-model="formData.productPartNumber"
          placeholder="请输入所属部件代号（所属部件件号）"
        />
      </el-form-item>
      <el-form-item label="机型" prop="model">
        <el-input v-model="formData.model" placeholder="请输入机型" />
      </el-form-item>
      <el-form-item label="系列" prop="series">
        <el-input v-model="formData.series" placeholder="请输入系列" />
      </el-form-item>
      <el-form-item label="机号" prop="machineNumber">
        <el-input v-model="formData.machineNumber" placeholder="请输入机号" />
      </el-form-item>
      <el-form-item label="报告开始日期" prop="reportStartDate">
        <el-date-picker
          v-model="formData.reportStartDate"
          type="date"
          value-format="x"
          placeholder="选择报告开始日期"
        />
      </el-form-item>
      <el-form-item label="报告结束日期" prop="reportEndDate">
        <el-date-picker
          v-model="formData.reportEndDate"
          type="date"
          value-format="x"
          placeholder="选择报告结束日期"
        />
      </el-form-item>
      <el-form-item label="拆卸日期" prop="removalDate">
        <el-date-picker
          v-model="formData.removalDate"
          type="date"
          value-format="x"
          placeholder="选择拆卸日期"
        />
      </el-form-item>
      <el-form-item label="故障报告编码" prop="failureReportNumber">
        <el-input v-model="formData.failureReportNumber" placeholder="请输入故障报告编码" />
      </el-form-item>
      <!--      <el-form-item label="是否是产品问题：0=否，1=是" prop="isProductIssue">-->
      <!--        <el-radio-group v-model="formData.isProductIssue">-->
      <!--          <el-radio value="1">请选择字典生成</el-radio>-->
      <!--        </el-radio-group>-->
      <!--      </el-form-item>-->
      <el-form-item label="是否是产品问题：0=否，1=是" prop="isProductIssue">
        <el-input
          v-model="formData.isProductIssue"
          placeholder="请输入是否是产品问题：0=否，1=是"
          type="text"
        />
      </el-form-item>
      <!--      <el-form-item label="拆卸类型：S-计划拆换；U-非计划拆换" prop="removalType">-->
      <!--        <el-select v-model="formData.removalType" placeholder="请选择拆卸类型：S-计划拆换；U-非计划拆换">-->
      <!--          <el-option label="请选择字典生成" value="" />-->
      <!--        </el-select>-->
      <!--      </el-form-item>-->
      <el-form-item label="拆卸类型：S-计划拆换；U-非计划拆换" prop="removalType">
        <el-select v-model="formData.removalType" placeholder="请选择拆卸类型">
          <el-option label="S-计划拆换" value="S" />
          <el-option label="U-非计划拆换" value="U" />
        </el-select>
      </el-form-item>
      <el-form-item label="拆换原因" prop="removalCause">
        <el-input v-model="formData.removalCause" placeholder="请输入拆换原因" />
      </el-form-item>
      <el-form-item label="故障描述" prop="failureDescription">
        <Editor v-model="formData.failureDescription" height="150px" />
      </el-form-item>
      <el-form-item label="拆卸部件件号" prop="removedPartNumber">
        <el-input v-model="formData.removedPartNumber" placeholder="请输入拆卸部件件号" />
      </el-form-item>
      <el-form-item label="拆卸部件序列号" prop="removedPartSerialNumber">
        <el-input v-model="formData.removedPartSerialNumber" placeholder="请输入拆卸部件序列号" />
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
import { DataApi, Data } from '@/api/reliability/reliabilitydata'

/** 可靠性数据收集 表单 */
defineOptions({ name: 'DataForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  reliabilityDataFormNo: undefined,
  customerName: undefined,
  partName: undefined,
  partNumber: undefined,
  sruName: undefined,
  sruNumber: undefined,
  serialNumber: undefined,
  productPartNumber: undefined,
  model: undefined,
  series: undefined,
  machineNumber: undefined,
  reportStartDate: undefined,
  reportEndDate: undefined,
  removalDate: undefined,
  failureReportNumber: undefined,
  isProductIssue: undefined,
  removalType: undefined,
  removalCause: undefined,
  failureDescription: undefined,
  removedPartNumber: undefined,
  removedPartSerialNumber: undefined,
  remark: undefined
})
// const formRules = reactive({
//   isProductIssue: [{ required: true, message: '是否是产品问题：0=否，1=是不能为空', trigger: 'blur' }]
// })
const formRules = reactive({
  isProductIssue: [
    { required: true, message: '是否是产品问题：0=否，1=是不能为空', trigger: 'blur' },
    { pattern: /^[01]$/, message: '请输入有效值：0(否)或1(是)', trigger: 'blur' }
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
      formData.value = await DataApi.getData(id)
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
    const data = formData.value as unknown as Data
    if (formType.value === 'create') {
      await DataApi.createData(data)
      message.success(t('common.createSuccess'))
    } else {
      await DataApi.updateData(data)
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
    reliabilityDataFormNo: undefined,
    customerName: undefined,
    partName: undefined,
    partNumber: undefined,
    sruName: undefined,
    sruNumber: undefined,
    serialNumber: undefined,
    productPartNumber: undefined,
    model: undefined,
    series: undefined,
    machineNumber: undefined,
    reportStartDate: undefined,
    reportEndDate: undefined,
    removalDate: undefined,
    failureReportNumber: undefined,
    isProductIssue: undefined,
    removalType: undefined,
    removalCause: undefined,
    failureDescription: undefined,
    removedPartNumber: undefined,
    removedPartSerialNumber: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

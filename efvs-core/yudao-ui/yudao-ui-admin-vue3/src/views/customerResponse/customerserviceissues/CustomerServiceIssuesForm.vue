<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="流程实例id" prop="processInstanceId">
        <el-input v-model="formData.processInstanceId" placeholder="请输入流程实例id" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
      </el-form-item>
      <el-form-item label="所属部门" prop="department">
        <el-input v-model="formData.department" placeholder="请输入所属部门" />
      </el-form-item>
      <el-form-item label="联系人" prop="contact">
        <el-input v-model="formData.contact" placeholder="请输入联系人" />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactNumber">
        <el-input v-model="formData.contactNumber" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="联系人邮箱" prop="contactEmail">
        <el-input v-model="formData.contactEmail" placeholder="请输入联系人邮箱" />
      </el-form-item>
      <el-form-item label="记录日期" prop="recordDate">
        <el-date-picker
          v-model="formData.recordDate"
          type="date"
          value-format="x"
          placeholder="选择记录日期"
        />
      </el-form-item>
      <el-form-item label="问题编号" prop="questionNumber">
        <el-input v-model="formData.questionNumber" placeholder="请输入问题编号" />
      </el-form-item>
      <el-form-item label="客户要求时间" prop="customerRequestsTime">
        <el-date-picker
          v-model="formData.customerRequestsTime"
          type="date"
          value-format="x"
          placeholder="选择客户要求时间"
        />
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-input v-model="formData.priority" placeholder="请输入优先级" />
      </el-form-item>
      <el-form-item label="客户问题描述" prop="customerProblemDescription">
        <Editor v-model="formData.customerProblemDescription" height="150px" />
      </el-form-item>
      <el-form-item label="客户期望" prop="customerExpectations">
        <el-input v-model="formData.customerExpectations" placeholder="请输入客户期望" />
      </el-form-item>
      <el-form-item label="客户附件" prop="customerAttachments">
        <el-input v-model="formData.customerAttachments" placeholder="请输入客户附件" />
      </el-form-item>
      <el-form-item label="客户问题适用性" prop="applicableToCustomerIssues">
        <el-input v-model="formData.applicableToCustomerIssues" placeholder="请输入客户问题适用性" />
      </el-form-item>
      <el-form-item label="客户问题行业标准" prop="customerIssuesIndustryStandards">
        <el-input v-model="formData.customerIssuesIndustryStandards" placeholder="请输入客户问题行业标准" />
      </el-form-item>
      <el-form-item label="客户问题影响" prop="customerProblemImpact">
        <el-input v-model="formData.customerProblemImpact" placeholder="请输入客户问题影响" />
      </el-form-item>
      <el-form-item label="客户问题类型" prop="customerProblemTypes">
        <el-input v-model="formData.customerProblemTypes" placeholder="请输入客户问题类型" />
      </el-form-item>
      <el-form-item label="是否可以直接答复(0是1否)" prop="isReply">
        <el-input v-model="formData.isReply" placeholder="请输入是否可以直接答复(0是1否)" />
      </el-form-item>
      <el-form-item label="解决方案负责人" prop="solutionManager">
        <el-input v-model="formData.solutionManager" placeholder="请输入解决方案负责人" />
      </el-form-item>
      <el-form-item label="编制时间" prop="preparationTime">
        <el-date-picker
          v-model="formData.preparationTime"
          type="date"
          value-format="x"
          placeholder="选择编制时间"
        />
      </el-form-item>
      <el-form-item label="编制人" prop="preparedBy">
        <el-input v-model="formData.preparedBy" placeholder="请输入编制人" />
      </el-form-item>
      <el-form-item label="解决方案内容" prop="solutionContent">
        <Editor v-model="formData.solutionContent" height="150px" />
      </el-form-item>
      <el-form-item label="解决方案的附件" prop="solutionAttachment">
        <el-input v-model="formData.solutionAttachment" placeholder="请输入解决方案的附件" />
      </el-form-item>
      <el-form-item label="回复时间" prop="responseTime">
        <el-date-picker
          v-model="formData.responseTime"
          type="date"
          value-format="x"
          placeholder="选择回复时间"
        />
      </el-form-item>
      <el-form-item label="回复人" prop="respondent">
        <el-input v-model="formData.respondent" placeholder="请输入回复人" />
      </el-form-item>
      <el-form-item label="回复内容" prop="replyContent">
        <Editor v-model="formData.replyContent" height="150px" />
      </el-form-item>
      <el-form-item label="回复附件" prop="replyAttachment">
        <el-input v-model="formData.replyAttachment" placeholder="请输入回复附件" />
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
import { CustomerServiceIssuesApi, CustomerServiceIssues } from '@/api/customerResponse/customerserviceissues'

/** 客户服务问题 表单 */
defineOptions({ name: 'CustomerServiceIssuesForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  processInstanceId: undefined,
  customerName: undefined,
  department: undefined,
  contact: undefined,
  contactNumber: undefined,
  contactEmail: undefined,
  recordDate: undefined,
  questionNumber: undefined,
  customerRequestsTime: undefined,
  priority: undefined,
  customerProblemDescription: undefined,
  customerExpectations: undefined,
  customerAttachments: undefined,
  applicableToCustomerIssues: undefined,
  customerIssuesIndustryStandards: undefined,
  customerProblemImpact: undefined,
  customerProblemTypes: undefined,
  isReply: undefined,
  solutionManager: undefined,
  preparationTime: undefined,
  preparedBy: undefined,
  solutionContent: undefined,
  solutionAttachment: undefined,
  responseTime: undefined,
  respondent: undefined,
  replyContent: undefined,
  replyAttachment: undefined,
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
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CustomerServiceIssuesApi.getCustomerServiceIssues(id)
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
    const data = formData.value as unknown as CustomerServiceIssues
    if (formType.value === 'create') {
      await CustomerServiceIssuesApi.createCustomerServiceIssues(data)
      message.success(t('common.createSuccess'))
    } else {
      await CustomerServiceIssuesApi.updateCustomerServiceIssues(data)
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
    processInstanceId: undefined,
    customerName: undefined,
    department: undefined,
    contact: undefined,
    contactNumber: undefined,
    contactEmail: undefined,
    recordDate: undefined,
    questionNumber: undefined,
    customerRequestsTime: undefined,
    priority: undefined,
    customerProblemDescription: undefined,
    customerExpectations: undefined,
    customerAttachments: undefined,
    applicableToCustomerIssues: undefined,
    customerIssuesIndustryStandards: undefined,
    customerProblemImpact: undefined,
    customerProblemTypes: undefined,
    isReply: undefined,
    solutionManager: undefined,
    preparationTime: undefined,
    preparedBy: undefined,
    solutionContent: undefined,
    solutionAttachment: undefined,
    responseTime: undefined,
    respondent: undefined,
    replyContent: undefined,
    replyAttachment: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
<template>
  <ContentWrap title="客户服务问题表">
    <el-form
      :model="customerIssueData"
      :disabled="customerDisabled"
      v-loading="formLoading"
      :rules="rules"
      ref="customerIssueRef"
      label-width="120px"
    >
      <!-- 客户信息 -->
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="customerIssueData.customerName" placeholder="请输入客户名称" />
      </el-form-item>

      <el-form-item label="所属部门" prop="department">
        <el-input v-model="customerIssueData.department" placeholder="请输入所属部门" />
      </el-form-item>

      <el-row>
        <el-col :span="12">
          <el-form-item label="联系人" prop="contact">
            <el-input v-model="customerIssueData.contact" placeholder="请输入联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="contactNumber">
            <el-input v-model="customerIssueData.contactNumber" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-form-item label="联系人邮箱" prop="contactEmail">
            <el-input v-model="customerIssueData.contactEmail" placeholder="请输入联系人邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="记录日期" prop="recordDate">
            <el-date-picker
              v-model="customerIssueData.recordDate"
              type="date"
              placeholder="请选择记录日期"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="问题编号" prop="questionNumber">
        <el-input v-model="customerIssueData.questionNumber" placeholder="请输入问题编号" />
      </el-form-item>

      <el-row
        ><el-col :span="12"
          ><el-form-item label="客户要求时间" prop="customerRequestsTime">
            <el-date-picker
              v-model="customerIssueData.customerRequestsTime"
              type="datetime"
              placeholder="请选择客户要求时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="customerIssueData.priority" placeholder="请选择优先级">
              <el-option label="AOG/紧急" :value="1" />
              <el-option label="高" :value="2" />
              <el-option label="中" :value="3" />
              <el-option label="低" :value="4" />
            </el-select> </el-form-item></el-col
      ></el-row>

      <el-form-item label="客户问题描述" prop="customerProblemDescription">
        <el-input
          v-model="customerIssueData.customerProblemDescription"
          type="textarea"
          :rows="4"
          placeholder="请输入客户问题描述"
        />
      </el-form-item>

      <el-form-item label="客户期望" prop="customerExpectations">
        <el-input
          v-model="customerIssueData.customerExpectations"
          type="textarea"
          :rows="3"
          placeholder="请输入客户期望"
        />
      </el-form-item>

      <el-form-item label="客户附件" prop="customerAttachments">
        <!-- <UploadFile v-model="customerIssueData.customerAttachments" class="min-w-80px" /> -->
      </el-form-item>

      <el-form-item label="客户问题适用性" prop="applicableToCustomerIssues">
        <el-input
          v-model="customerIssueData.applicableToCustomerIssues"
          placeholder="请输入适用性"
        />
      </el-form-item>

      <el-form-item label="客户问题行业标准" prop="customerIssuesIndustryStandards">
        <el-input
          v-model="customerIssueData.customerIssuesIndustryStandards"
          placeholder="请输入行业标准"
        />
      </el-form-item>

      <el-form-item label="客户问题影响" prop="customerProblemImpact">
        <el-input v-model="customerIssueData.customerProblemImpact" placeholder="请输入影响" />
      </el-form-item>

      <el-form-item label="客户问题类型" prop="customerProblemTypes">
        <el-input v-model="customerIssueData.customerProblemTypes" placeholder="请输入问题类型" />
      </el-form-item>

      <el-form-item label="是否可直接答复" prop="isReply">
        <el-radio-group v-model="customerIssueData.isReply">
          <el-radio :value="1">是</el-radio>
          <el-radio :value="0">否</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 任务分配 -->
      <el-form-item label="解决方案负责人" prop="solutionManager">
        <el-input v-model="customerIssueData.solutionManager" placeholder="请输入负责人" />
      </el-form-item>

      <el-row
        ><el-col :span="12"
          ><el-form-item label="编制时间" prop="preparationTime">
            <el-date-picker
              v-model="customerIssueData.preparationTime"
              type="datetime"
              placeholder="请选择编制时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="编制人" prop="preparedBy">
            <el-input v-model="customerIssueData.preparedBy" placeholder="请输入编制人" />
          </el-form-item> </el-col
      ></el-row>

      <el-form-item label="解决方案内容" prop="solutionContent">
        <el-input
          v-model="customerIssueData.solutionContent"
          type="textarea"
          :rows="4"
          placeholder="请输入解决方案内容"
        />
      </el-form-item>

      <el-form-item label="解决方案附件" prop="solutionAttachment">
        <!-- <UploadFile v-model="customerIssueData.solutionAttachment" class="min-w-80px" /> -->
      </el-form-item>

      <!-- 客户回复 -->
      <el-row
        ><el-col :span="12">
          <el-form-item label="回复时间" prop="responseTime">
            <el-date-picker
              v-model="customerIssueData.responseTime"
              type="datetime"
              placeholder="请选择回复时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            /> </el-form-item></el-col
        ><el-col :span="12">
          <el-form-item label="回复人" prop="respondent">
            <el-input
              v-model="customerIssueData.respondent"
              placeholder="请输入回复人"
            /> </el-form-item></el-col
      ></el-row>

      <el-form-item label="回复内容" prop="replyContent">
        <el-input
          v-model="customerIssueData.replyContent"
          type="textarea"
          :rows="4"
          placeholder="请输入回复内容"
        />
      </el-form-item>

      <el-form-item label="答复附件" prop="replyAttachment">
        <!-- <UploadFile v-model="customerIssueData.replyAttachment" class="min-w-80px" /> -->
      </el-form-item>
    </el-form>
  </ContentWrap>
</template>
<script setup lang="ts">
import { ref } from 'vue'

import { FormItemRule } from 'element-plus'

const props = defineProps({
  customerDisabled: { type: Boolean, default: false, required: true }
})
const isShowBtn = ref(true)
// 表单数据
const customerIssueData = ref({
  customerName: '',
  department: '',
  contact: '',
  contactNumber: '',
  contactEmail: '',
  recordDate: '',
  questionNumber: '',
  customerRequestsTime: '',
  priority: '',
  customerProblemDescription: '',
  customerExpectations: '',
  customerAttachments: '',
  applicableToCustomerIssues: '',
  customerIssuesIndustryStandards: '',
  customerProblemImpact: '',
  customerProblemTypes: '',
  isReply: 1,
  solutionManager: '',
  preparationTime: '',
  preparedBy: '',
  solutionContent: '',
  solutionAttachment: '',
  responseTime: '',
  respondent: '',
  replyContent: '',
  replyAttachment: '',
  formType: 'quickResponse'
})

// 校验规则
const rules: { [key: string]: FormItemRule | FormItemRule[] } = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属部门', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  /* contactNumber: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: '请输入联系人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ], */
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
  questionNumber: [{ required: true, message: '请输入问题编号', trigger: 'blur' }],
  customerRequestsTime: [{ required: true, message: '请选择客户要求时间', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  customerProblemDescription: [{ required: true, message: '请输入客户问题描述', trigger: 'blur' }],
  customerExpectations: [{ required: true, message: '请输入客户期望', trigger: 'blur' }],
  solutionManager: [{ required: true, message: '请输入解决方案负责人', trigger: 'blur' }]
  /*   preparationTime: [{ required: true, message: '请选择编制时间', trigger: 'change' }],
  preparedBy: [{ required: true, message: '请输入编制人', trigger: 'blur' }],
  solutionContent: [{ required: true, message: '请输入解决方案内容', trigger: 'blur' }],
  responseTime: [{ required: true, message: '请选择回复时间', trigger: 'change' }],
  respondent: [{ required: true, message: '请输入回复人', trigger: 'blur' }],
  replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }] */
}
// 表单引用
const customerIssueRef = ref()

// 审批相关：变量

const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用

/** 表单验证方法 - 供外部调用（如审批通过时） */
const validate = async (): Promise<boolean> => {
  if (!customerIssueRef.value) {
    return true
  }
  try {
    await customerIssueRef.value.validate()
    return true
  } catch {
    return false
  }
}
defineExpose({
  validate,
  formData: customerIssueData,
  isShowBtn: isShowBtn
})
</script>

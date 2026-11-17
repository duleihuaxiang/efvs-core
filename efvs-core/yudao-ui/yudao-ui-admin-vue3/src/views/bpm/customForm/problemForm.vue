<template>
  <el-row :gutter="20">
    <el-col :span="16">
      <ContentWrap title="问题表单信息">
        <el-form
          ref="problemRef"
          :model="problemData"
          :rules="rules"
          label-width="120px"
          class="mt-20px"
          :disabled="problemDisabled"
          v-loading="formLoading"
        >
          <el-form-item label="客户名称" prop="customerName" class="mb-20px">
            <el-input v-model="problemData.customerName" clearable />
          </el-form-item>
          <el-form-item label="客户简称" prop="customerAbbreviation" class="mb-20px">
            <el-input v-model="problemData.customerAbbreviation" clearable />
          </el-form-item>
          <el-form-item label="联系人" prop="contact" class="mb-20px">
            <el-input v-model="problemData.contact" clearable />
          </el-form-item>

          <el-form-item label="联系方式" prop="contactNumber" class="mb-20px">
            <el-input v-model="problemData.contactNumber" clearable />
          </el-form-item>
          <el-form-item label="详细地址" prop="address" class="mb-20px">
            <el-input v-model="problemData.address" clearable type="textarea" />
          </el-form-item>
          <el-form-item label="客户类型" prop="customerType" class="mb-20px">
            <el-input v-model="problemData.customerType" clearable />
          </el-form-item>
          <el-form-item label="时间" prop="createTime" class="mb-20px">
            <el-date-picker
              v-model="problemData.createTime"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              class="!w-240px"
              end-placeholder="结束时间"
              start-placeholder="开始时间"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>

          <el-form-item label="问题分类" prop="problemType" class="mb-20px">
            <el-input v-model="problemData.problemType" clearable />
          </el-form-item>
          <el-form-item label="紧急程度" prop="urgencyLevel" class="mb-20px">
            <el-input v-model="problemData.urgencyLevel" clearable />
          </el-form-item>

          <el-form-item label="客户问题描述" prop="customerProblemDescription" class="mb-20px">
            <el-input v-model="problemData.customerProblemDescription" clearable type="textarea" />
          </el-form-item>
          <el-form-item label="附件" prop="attachment" class="mb-20px">
            <UploadFile v-model="problemData.attachment" class="min-w-80px" />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="mb-20px">
            <el-input v-model="problemData.remark" clearable type="textarea" />
          </el-form-item>
          <el-form-item label="状态" prop="status" class="mb-20px" v-if="false">
            <el-input v-model="problemData.status" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitForm" :disabled="formLoading">提交</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>
    </el-col>

    <!-- 审批相关：流程信息 -->
    <el-col :span="8">
      <ContentWrap title="审批流程" :bodyStyle="{ padding: '0 20px 0' }">
        <ProcessInstanceTimeline
          ref="timelineRef"
          :activity-nodes="activityNodes"
          :show-status-icon="false"
          @select-user-confirm="selectUserConfirm"
        />
      </ContentWrap>
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
// 审批相关：import
import * as DefinitionApi from '@/api/bpm/definition'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import { ApprovalNodeInfo } from '@/api/bpm/processInstance'

import { useTagsViewStore } from '@/store/modules/tagsView'

const props = defineProps({
  /*   problemData: {
    type: Object,
    required: false,
    default: () => ({ status: 'quickResponse' })
  }, */
  problemDisabled: { type: Boolean, default: false, required: true }
})

const message = useMessage() // 消息弹窗
const { delView } = useTagsViewStore() // 视图操作
const { push, currentRoute } = useRouter() // 路由
// 使用 computed 实现双向绑定
/* const localProblemData = computed({
  get: () => props.problemData,
  set: (value) => emit('update:problemData', value)
}) */
// const problemData = defineModel<any>()
const problemData = ref({
  customerName: '',
  customerAbbreviation: '',
  contact: '',
  contactNumber: '',
  address: '',
  customerType: '',
  createTime: [],
  startTime: '',
  endTime: '',
  problemType: '',
  urgencyLevel: '',
  customerProblemDescription: '',
  attachment: [],
  remark: '',
  formType: 'quickResponse'
})
const problemRef = ref()

const rules = {
  customerName: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
  customerAbbreviation: [{ required: true, message: '客户简称', trigger: 'blur' }],
  contact: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactNumber: [{ required: true, message: '联系方式不能为空', trigger: 'blur' }],
  address: [{ required: true, message: '详细地址不能为空', trigger: 'blur' }],
  customerType: [{ required: true, message: '客户类型不能为空', trigger: 'blur' }],
  createTime: [{ required: true, message: '时间不能为空', trigger: 'blur' }],
  problemType: [{ required: true, message: '问题分类简称', trigger: 'blur' }],
  urgencyLevel: [{ required: true, message: '紧急程度不能为空', trigger: 'blur' }],
  customerProblemDescription: [
    { required: true, message: '客户问题描述不能为空', trigger: 'blur' }
  ],
  attachment: [{ required: true, message: '附件不能为空', trigger: 'blur' }],
  remark: [{ required: true, message: '备注不能为空', trigger: 'blur' }]
}

// 审批相关：变量
const processDefineKey = 'process_flag' // 流程定义 Key
const startUserSelectTasks = ref([]) // 发起人需要选择审批人的用户任务列表
const startUserSelectAssignees = ref({}) // 发起人选择审批人的数据
const tempStartUserSelectAssignees = ref({}) // 历史发起人选择审批人的数据，用于每次表单变更时，临时保存
const activityNodes = ref<ProcessInstanceApi.ApprovalNodeInfo[]>([]) // 审批节点信息
const processDefinitionId = ref('')
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用

/** 表单校验 */
const validateProblem = async () => {
  await problemRef.value.validate()
}
/** 表单验证方法 - 供外部调用（如审批通过时） */
const validate = async (): Promise<boolean> => {
  if (!problemRef.value) {
    return true
  }
  try {
    await problemRef.value.validate()
    return true
  } catch {
    return false
  }
}
// 提交表单方法
const submitForm = async () => {
  // 1.1 校验表单
  if (!problemRef) return
  const valid = await problemRef.value?.validate()
  if (!valid) return
  // 1.2 审批相关：校验指定审批人
  if (startUserSelectTasks.value?.length > 0) {
    for (const userTask of startUserSelectTasks.value) {
      if (
        Array.isArray(startUserSelectAssignees.value[userTask.id]) &&
        startUserSelectAssignees.value[userTask.id].length === 0
      ) {
        return message.warning(`请选择${userTask.name}的审批人`)
      }
    }
  }

  // 2. 提交请求
  formLoading.value = true
  try {
    await ProcessInstanceApi.createProcessInstance({
      processDefinitionId: processDefinitionId.value,
      variables: problemData.value,
      startUserSelectAssignees: startUserSelectAssignees.value
    })
    // 提示
    message.success('发起流程成功')
    // 跳转回去
    delView(unref(currentRoute))
    await push({
      name: 'BpmProcessInstanceMy'
    })
  } finally {
    formLoading.value = false
  }
}

// 重置表单方法
const resetForm = (ref) => {
  problemRef.value?.resetFields()
}

/** 审批相关：获取审批详情 */
const getApprovalDetail = async () => {
  try {
    const data = await ProcessInstanceApi.getApprovalDetail({
      processDefinitionId: processDefinitionId.value,
      // TODO 小北：可以支持 processDefinitionKey 查询
      activityId: NodeId.START_USER_NODE_ID,
      processVariablesStr: JSON.stringify({ day: daysDifference() }) // 解决 GET 无法传递对象的问题，后端 String 再转 JSON
    })

    if (!data) {
      message.error('查询不到审批详情信息！')
      return
    }
    // 获取审批节点，显示 Timeline 的数据
    activityNodes.value = data.activityNodes

    // 获取发起人自选的任务
    startUserSelectTasks.value = data.activityNodes?.filter(
      (node: ApprovalNodeInfo) => CandidateStrategy.START_USER_SELECT === node.candidateStrategy
    )
    // 恢复之前的选择审批人
    if (startUserSelectTasks.value?.length > 0) {
      for (const node of startUserSelectTasks.value) {
        if (
          tempStartUserSelectAssignees.value[node.id] &&
          tempStartUserSelectAssignees.value[node.id].length > 0
        ) {
          startUserSelectAssignees.value[node.id] = tempStartUserSelectAssignees.value[node.id]
        } else {
          startUserSelectAssignees.value[node.id] = []
        }
      }
    }
  } finally {
  }
}

/** 审批相关：选择发起人 */
const selectUserConfirm = (id: string, userList: any[]) => {
  startUserSelectAssignees.value[id] = userList?.map((item: any) => item.id)
}
// 计算天数差
// TODO @小北：可以搞到 formatTime 里面去，然后看看 dayjs 里面有没有现成的方法，或者辅助计算的方法。
const daysDifference = () => {
  const oneDay = 24 * 60 * 60 * 1000 // 一天的毫秒数
  const diffTime = Math.abs(Number(problemData.value.endTime) - Number(problemData.value.startTime))
  return Math.floor(diffTime / oneDay)
}

/** 初始化 */
onMounted(async () => {
  // TODO @小北：这里可以简化，统一通过 getApprovalDetail 处理么？
  const processDefinitionDetail = await DefinitionApi.getProcessDefinition(
    undefined,
    processDefineKey
  )

  if (!processDefinitionDetail) {
    message.error('OA 请假的流程模型未配置，请检查！')
    return
  }
  processDefinitionId.value = processDefinitionDetail.id
  startUserSelectTasks.value = processDefinitionDetail.startUserSelectTasks

  // 审批相关：加载最新的审批详情，主要用于节点预测
  await getApprovalDetail()
})

/** 审批相关：预测流程节点会因为输入的参数值而产生新的预测结果值，所以需重新预测一次, formData.value可改成实际业务中的特定字段 */
watch(
  problemData.value,
  (newValue, oldValue) => {
    if (!oldValue) {
      return
    }
    if (newValue && Object.keys(newValue).length > 0) {
      // 记录之前的节点审批人
      tempStartUserSelectAssignees.value = startUserSelectAssignees.value
      startUserSelectAssignees.value = {}
      // 加载最新的审批详情,主要用于节点预测
      getApprovalDetail()
    }
  },
  {
    immediate: true
  }
)

defineExpose({
  validateProblem,
  validate
})
</script>

<style lang="scss" scoped>
.bg-gray-100 {
  background-color: #f5f7fa;
  transition: all 0.3s;

  &:hover {
    background-color: #e6e8eb;
  }

  .ep-close {
    font-size: 14px;
    color: #909399;
    transition: color 0.3s;

    &:hover {
      color: #f56c6c;
    }
  }
}
</style>

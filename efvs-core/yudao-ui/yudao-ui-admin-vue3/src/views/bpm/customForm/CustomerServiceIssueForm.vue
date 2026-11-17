<template>
  <el-row :gutter="20">
    <el-col :span="props.isShowBtn ? 16 : 24">
      <ContentWrap title="客户服务问题表">
        <el-form
          :model="customerIssueData"
          :disabled="customerDisabled"
          v-loading="formLoading"
          :rules="rules"
          ref="customerIssueRef"
          label-width="180"
        >
          <!-- 客户信息 -->
          <div id="part1" v-if="shouldShowPart1">
            <el-form-item label="客户名称" prop="customerId">
              <el-select
                v-model="customerIssueData.customerId"
                @change="customerChange"
                :disabled="disabledPart1"
              >
                <el-option
                  v-for="item in customerList"
                  :key="item.customerId"
                  :label="item.customerName"
                  :value="item.customerId"
                  placeholder="请选择客户名称"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="所属部门" prop="department">
              <el-input
                v-model="customerIssueData.department"
                placeholder="请输入所属部门"
                :disabled="disabledPart1"
              />
            </el-form-item>

            <el-row>
              <el-col :span="12">
                <el-form-item label="联系人" prop="contact">
                  <el-input
                    v-model="customerIssueData.contact"
                    placeholder="请输入联系人"
                    :disabled="disabledPart1"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactNumber">
                  <el-input
                    v-model="customerIssueData.contactNumber"
                    placeholder="请输入联系电话"
                    :disabled="disabledPart1"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row>
              <el-col :span="12">
                <el-form-item label="联系人邮箱" prop="contactEmail">
                  <el-input
                    v-model="customerIssueData.contactEmail"
                    placeholder="请输入联系人邮箱"
                    :disabled="disabledPart1"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记录日期" prop="recordDate">
                  <el-date-picker
                    v-model="customerIssueData.recordDate"
                    type="date"
                    placeholder="请选择记录日期"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    class="!w-240"
                    :disabled="disabledPart1"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <!-- 自动生成问题编号 -->
            <el-form-item label="问题编号" prop="questionNumber">
              <el-input v-model="customerIssueData.questionNumber" disabled />
            </el-form-item>

            <el-row
              ><el-col :span="12"
                ><el-form-item label="客户要求时间" prop="customerRequestsTime">
                  <el-date-picker
                    v-model="customerIssueData.customerRequestsTime"
                    type="datetime"
                    placeholder="请选择客户要求时间"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disabled="disabledPart1"
                    class="!w-240"
                  /> </el-form-item></el-col
              ><el-col :span="12">
                <el-form-item label="优先级" prop="priority">
                  <el-select
                    v-model="customerIssueData.priority"
                    placeholder="请选择优先级"
                    :disabled="disabledPart1"
                  >
                    <el-option
                      v-for="dict in getIntDictOptions(DICT_TYPE.CUSTOMER_RESPONSE_LEVEL)"
                      :key="dict.value"
                      :label="dict.label"
                      :value="Number(dict.value)"
                    />
                  </el-select> </el-form-item></el-col
            ></el-row>

            <el-form-item label="客户问题描述" prop="customerProblemDescription">
              <!--  <el-input
              v-model="customerIssueData.customerProblemDescription"
              type="textarea"
              :rows="4"
              placeholder="请输入客户问题描述"
            /> -->
              <Editor
                v-model:modelValue="customerIssueData.customerProblemDescription"
                :height="200"
                :readonly="disabledPart1"
              />
            </el-form-item>

            <el-form-item label="客户期望" prop="customerExpectations">
              <el-input
                v-model="customerIssueData.customerExpectations"
                type="textarea"
                :rows="3"
                placeholder="请输入客户期望"
                :disabled="disabledPart1"
              />
            </el-form-item>

            <el-form-item label="客户附件" prop="customerAttachments">
              <UploadFile
                v-model="customerIssueData.customerAttachments"
                :disabled="disabledPart1"
                :file-size="fileSize"
                :file-type="fileType"
              />
            </el-form-item>

            <!--  <el-form-item label="客户问题适用性" prop="applicableToCustomerIssues">
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
          </el-form-item> -->

            <el-form-item label="客户问题类型" prop="customerProblemTypes">
              <el-select
                v-model="customerIssueData.customerProblemTypes"
                placeholder="请选择客户问题类型"
                :disabled="disabledPart1"
              >
                <el-option
                  v-for="dict in getIntDictOptions(DICT_TYPE.CUSTOMER_PROBLEM_TYPES)"
                  :key="dict.value"
                  :label="dict.label"
                  :value="Number(dict.value)"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="是否可直接答复" prop="isReply">
              <el-radio-group v-model="customerIssueData.isReply" :disabled="disabledPart1">
                <el-radio :value="0">是</el-radio>
                <el-radio :value="1">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <!-- 任务分配 -->
          <div id="part2" v-if="shouldShowPart2 && customerIssueData.isReply">
            <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">任务分配</h3>
            <el-form-item label="解决方案负责人" prop="solutionManager">
              <el-select
                v-model="customerIssueData.solutionManager"
                clearable
                style="width: 100%"
                placeholder="请选择解决方案负责人"
                :disabled="disabledPart2"
              >
                <el-option
                  v-for="item in userList"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </div>
          <div id="part3" v-if="shouldShowPart3 && customerIssueData.isReply">
            <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">解决方案</h3>
            <el-form-item label="问题类型" prop="problemTypes">
              <el-select
                v-model="customerIssueData.problemTypes"
                placeholder="请选择问题类型"
                :disabled="disabledPart3"
                clearable
              >
                <el-option
                  v-for="dict in getIntDictOptions(DICT_TYPE.PROBLEM_TYPES)"
                  :key="dict.value"
                  :label="dict.label"
                  :value="Number(dict.value)"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="可靠性数据收集表单编号" prop="reliabilityDataFormNo">
              <el-select
                v-model="customerIssueData.reliabilityDataFormNo"
                clearable
                filterable
                style="width: 100%"
                placeholder="请选择可靠性数据收集表单编号"
                :disabled="disabledPart3"
              >
                <el-option
                  v-for="item in reliabilityData"
                  :key="item?.reliabilityDataFormNo"
                  :label="item?.reliabilityDataFormNo"
                  :value="item?.reliabilityDataFormNo"
                />
              </el-select>
            </el-form-item>
            <el-row
              ><el-col :span="12"
                ><el-form-item label="编制时间" prop="preparationTime">
                  <el-date-picker
                    v-model="customerIssueData.preparationTime"
                    type="datetime"
                    placeholder="请选择编制时间"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    class="!w-240"
                    :disabled="disabledPart3"
                  /> </el-form-item></el-col
              ><el-col :span="12">
                <el-form-item label="编制人" prop="preparedBy">
                  <el-select
                    v-model="customerIssueData.preparedBy"
                    clearable
                    style="width: 100%"
                    placeholder="请选择编制人"
                    :disabled="disabledPart3"
                  >
                    <el-option
                      v-for="item in userList"
                      :key="item.id"
                      :label="item.nickname"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item> </el-col
            ></el-row>

            <el-form-item label="解决方案内容" prop="solutionContent">
              <!--  <el-input
              v-model="customerIssueData.solutionContent"
              type="textarea"
              :rows="4"
              placeholder="请输入解决方案内容"
            /> -->
              <Editor
                v-model:modelValue="customerIssueData.solutionContent"
                :height="200"
                :readonly="disabledPart3"
              />
            </el-form-item>

            <el-form-item label="解决方案附件" prop="solutionAttachment">
              <UploadFile
                v-model="customerIssueData.solutionAttachment"
                :disabled="disabledPart3"
                :file-size="fileSize"
                :file-type="fileType"
              />
            </el-form-item>
          </div>
          <!-- 客户回复 -->
          <div id="part4" v-if="shouldShowPart4">
            <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">客户回复</h3>
            <el-row
              ><el-col :span="12">
                <el-form-item label="回复时间" prop="responseTime">
                  <el-date-picker
                    v-model="customerIssueData.responseTime"
                    type="datetime"
                    disabled
                    value-format="YYYY-MM-DD HH:mm:ss"
                    class="!w-240"
                  /> </el-form-item></el-col
              ><el-col :span="12">
                <el-form-item label="回复人" prop="respondent">
                  <el-select
                    v-model="customerIssueData.respondent"
                    clearable
                    style="width: 100%"
                    placeholder="请选择回复人"
                    :disabled="disabledPart4"
                  >
                    <el-option
                      v-for="item in userList"
                      :key="item.id"
                      :label="item.nickname"
                      :value="item.id"
                    />
                  </el-select> </el-form-item></el-col
            ></el-row>

            <el-form-item label="回复内容" prop="replyContent">
              <el-input
                v-model="customerIssueData.replyContent"
                type="textarea"
                :rows="4"
                placeholder="请输入回复内容"
                :disabled="disabledPart4"
              />
            </el-form-item>

            <el-form-item label="答复附件" prop="replyAttachment">
              <UploadFile
                v-model="customerIssueData.replyAttachment"
                :disabled="disabledPart4"
                :file-size="fileSize"
                :file-type="fileType"
              />
            </el-form-item>
          </div>
          <el-form-item v-if="isShowBtn">
            <el-button type="primary" @click="submitForm">提交</el-button>
            <el-button @click="resetForm">取消</el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>
    </el-col>
    <!-- 审批相关：流程信息 -->
    <el-col :span="8" v-if="isShowBtn">
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
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { FormItemRule } from 'element-plus'
import { Editor } from '@/components/Editor'
import { formatDate } from '@/utils/formatTime'
// 审批相关：import
import * as DefinitionApi from '@/api/bpm/definition'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import { ApprovalNodeInfo } from '@/api/bpm/processInstance'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as FormApi from '@/api/bpm/form'
import * as UserApi from '@/api/system/user'
import { useUserStore } from '@/store/modules/user'

const message = useMessage() // 消息弹窗
const route = useRoute()
const { delView } = useTagsViewStore() // 视图操作
const { push, currentRoute } = useRouter() // 路由
const userStore = useUserStore()

const props = defineProps({
  customerDisabled: { type: Boolean, default: false, required: false },
  isShowBtn: { type: Boolean, default: true, required: false },
  processInstanceId: { type: String, default: '', required: false },
  currentName: { type: String, default: '', required: false }
})

const fileType = [
  'doc',
  'docx',
  'xlsx',
  'ppt',
  'txt',
  'pdf',
  'zip',
  'rar',
  '7z',
  'png',
  'jpg',
  'jpeg',
  'webp',
  'gif'
]
const fileSize = 100
// 核心：格式化当前日期为 YYYY-MM-DD  格式
const formatCurrentDate = () => {
  const now = new Date()
  // 获取年、月、日（补前导0，保证两位数）
  const year = now.getFullYear()
  const month = (now.getMonth() + 1).toString().padStart(2, '0') // 月份从0开始，需+1
  const day = now.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day} 00:00:00`
}

// const isShowBtn = ref(true)
// 表单数据
const customerIssueData = ref({
  customerName: '',
  customerId: '',
  department: '',
  contact: '',
  contactNumber: '',
  contactEmail: '',
  recordDate: formatCurrentDate(),
  questionNumber: '',
  customerRequestsTime: '',
  priority: '',
  customerProblemDescription: '',
  customerExpectations: '',
  customerAttachments: [],
  applicableToCustomerIssues: '',
  customerIssuesIndustryStandards: '',
  customerProblemImpact: '',
  customerProblemTypes: '',
  isReply: 0,
  solutionManager: '',
  problemTypes: undefined,
  reliabilityDataFormNo: '',
  preparationTime: '',
  preparedBy: '',
  solutionContent: '',
  solutionAttachment: [],
  responseTime: formatDate(new Date()),
  respondent: null,
  replyContent: '',
  replyAttachment: [],
  formType: 'quickResponse'
})

// 校验规则
const rules = computed(() => {
  const baseRules: { [key: string]: FormItemRule | FormItemRule[] } = {
    customerId: [{ required: true, message: '请选择客户名称', trigger: 'blur' }],
    department: [{ required: true, message: '请输入所属部门', trigger: 'blur' }],
    contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
    recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
    questionNumber: [{ required: true, message: '问题编号不能为空', trigger: 'blur' }],
    customerRequestsTime: [{ required: true, message: '请选择客户要求时间', trigger: 'change' }],
    priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
    customerProblemDescription: [
      { required: true, message: '请输入客户问题描述', trigger: 'blur' }
    ],
    customerExpectations: [{ required: true, message: '请输入客户期望', trigger: 'blur' }],
    customerProblemTypes: [{ required: true, message: '请选择客户问题类型', trigger: 'change' }],
    solutionManager: [{ required: true, message: '请输入解决方案负责人', trigger: 'blur' }],
    problemTypes: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
    preparationTime: [{ required: true, message: '请输入编制时间', trigger: 'blur' }],
    preparedBy: [{ required: true, message: '请输入编制人', trigger: 'blur' }],
    solutionContent: [{ required: true, message: '请输入解决方案内容', trigger: 'blur' }],
    responseTime: [{ required: true, message: '请输入回复时间', trigger: 'blur' }],
    respondent: [{ required: true, message: '请选择回复人', trigger: 'blur' }],
    replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
  }

  // 动态添加 reliabilityDataFormNo 校验规则
  if (
    customerIssueData.value.problemTypes !== undefined &&
    [1, 2].includes(customerIssueData.value.problemTypes)
  ) {
    baseRules.reliabilityDataFormNo = [
      { required: true, message: '请选择可靠性数据收集表单编号', trigger: 'blur' }
    ]
  }

  return baseRules
})
// 表单引用
const customerIssueRef = ref()
// 客户信息列表
const customerList = ref([])

// 审批相关：变量
const processDefineKey = route.query.key as string // 流程定义 Key
const startUserSelectTasks = ref([]) // 发起人需要选择审批人的用户任务列表
const startUserSelectAssignees = ref({}) // 发起人选择审批人的数据
const tempStartUserSelectAssignees = ref({}) // 历史发起人选择审批人的数据，用于每次表单变更时，临时保存
const activityNodes = ref<ProcessInstanceApi.ApprovalNodeInfo[]>([]) // 审批节点信息
const processDefinitionId = ref('')
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用

const userList = ref<any[]>([])
const reliabilityData = ref([])
/** 查询项目负责人下拉列表 */
const getUserList = async () => {
  const data = await UserApi.getUserListByDeptId()
  userList.value = data
}

/** 查询所有的可靠性编号下拉列表 */
const getAllReliabilityData = async () => {
  const data = await ProcessInstanceApi.getAllReliabilityData()
  reliabilityData.value = data
}
// 提交表单
const submitForm = () => {
  customerIssueRef.value?.validate(async (valid) => {
    if (valid) {
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
        // @ts-ignore
        customerIssueData.value.customerAttachments =
          customerIssueData.value.customerAttachments.join(',')
        // @ts-ignore
        customerIssueData.value.solutionAttachment =
          customerIssueData.value.solutionAttachment.join(',')
        // @ts-ignore
        customerIssueData.value.replyAttachment = customerIssueData.value.replyAttachment.join(',')

        await ProcessInstanceApi.createProcessInstance({
          processDefinitionId: processDefinitionId.value,
          variables: customerIssueData.value,
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
    } else {
      ElMessage.error('请检查表单填写是否正确')
    }
  })
}

// 重置表单
const resetForm = () => {
  customerIssueRef.value?.resetFields()
  customerIssueData.value.recordDate = formatCurrentDate()
}

const getVisibleParts = computed(() => {
  const { currentName, isShowBtn } = props

  if (isShowBtn || currentName === '客服主管收集提报客户问题') {
    return { part1: true, part2: false, part3: false, part4: false }
  } else if (currentName === '项目主管创建问题清单分派任务') {
    return { part1: true, part2: true, part3: false, part4: false }
  } else if (
    currentName === '制定回复方案' ||
    currentName === '方案审核' ||
    currentName === '方案批准'
  ) {
    return { part1: true, part2: true, part3: true, part4: false }
  } else {
    return { part1: true, part2: true, part3: true, part4: true }
  }
})
const shouldShowPart1 = computed(() => getVisibleParts.value.part1)
const shouldShowPart2 = computed(() => getVisibleParts.value.part2)
const shouldShowPart3 = computed(() => getVisibleParts.value.part3)
const shouldShowPart4 = computed(() => getVisibleParts.value.part4)

const disabledPart1 = computed(() => {
  return props.currentName !== '客服主管收集提报客户问题' && !props.isShowBtn
})
const disabledPart2 = computed(() => {
  return props.currentName !== '项目主管创建问题清单分派任务'
})
const disabledPart3 = computed(() => {
  return props.currentName !== '制定回复方案'
})
const disabledPart4 = computed(() => {
  return props.currentName !== '客服主管答复客户'
})
/** 审批相关：获取审批详情 */
const getApprovalDetail = async () => {
  if (!props.isShowBtn) {
    const getProcessInstanceData = await FormApi.getProcessInstanceId({
      processInstanceId: props.processInstanceId
    })
    if (!getProcessInstanceData) {
      message.error('查询不到表单详情信息！')
      return
    }
    customerIssueData.value = getProcessInstanceData
    customerIssueData.value.formType = 'quickResponse'
    customerIssueData.value.responseTime = formatDate(new Date())
    if (disabledPart2.value) {
      customerIssueData.value.preparedBy = customerIssueData.value.solutionManager
    }
    if (!disabledPart4.value) {
      customerIssueData.value.respondent = userStore.user.id
    }
  } else {
    try {
      const data = await ProcessInstanceApi.getApprovalDetail({
        processDefinitionId: processDefinitionId.value,
        activityId: NodeId.START_USER_NODE_ID,
        processVariablesStr: JSON.stringify({ isReply: customerIssueData.value.isReply }) // 解决 GET 无法传递对象的问题，后端 String 再转 JSON
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
}

const generateNumber = async () => {
  return await ProcessInstanceApi.generateNumber('customer')
}
/** 审批相关：选择发起人 */
const selectUserConfirm = (id: string, userList: any[]) => {
  startUserSelectAssignees.value[id] = userList?.map((item: any) => item.id)
}

// 获取客户信息列表
const getCustomerList = async () => {
  customerList.value = await ProcessInstanceApi.getCustomerInfoAll()
}

const customerChange = (value: any) => {
  const selectedCustomer = customerList.value.find((customer) => customer?.customerId === value)
  if (selectedCustomer) {
    customerIssueData.value.customerName = selectedCustomer?.customerName
  }
}

/** 初始化 */
onMounted(async () => {
  const processDefinitionDetail = await DefinitionApi.getProcessDefinition(
    undefined,
    processDefineKey
  )

  if (!processDefinitionDetail) {
    message.error('流程模型未配置，请检查！')
    return
  }
  processDefinitionId.value = processDefinitionDetail.id
  startUserSelectTasks.value = processDefinitionDetail.startUserSelectTasks

  await getCustomerList()
  if (props.isShowBtn) {
    customerIssueData.value.questionNumber = await generateNumber()
  }
  // 审批相关：加载最新的审批详情，主要用于节点预测
  await getApprovalDetail()
  getUserList()
  getAllReliabilityData()
})

/** 审批相关：预测流程节点会因为输入的参数值而产生新的预测结果值，所以需重新预测一次, customerIssueData.value可改成实际业务中的特定字段 */
watch(
  customerIssueData.value,
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
  formFlag: 'customerIssue'
})
</script>
<style lang="scss" scoped></style>

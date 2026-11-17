<template>
  <el-row :gutter="20">
    <el-col :span="props.isShowBtn ? 16 : 24">
      <ContentWrap title="可靠性数据收集">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="180"
          class="reliability-form"
          :disabled="disabledPart1"
        >
          <!-- 可靠性数据收集表单编号 -->
          <el-form-item label="可靠性数据收集表单编号" prop="reliabilityDataFormNo">
            <el-input v-model="formData.reliabilityDataFormNo" disabled />
          </el-form-item>

          <!-- 客户名称 -->
          <el-form-item label="客户名称" prop="customerId">
            <el-select v-model="formData.customerId" @change="customerChange">
              <el-option
                v-for="item in customerList"
                :key="item.customerId"
                :label="item.customerName"
                :value="item.customerId"
                placeholder="请选择客户名称"
              />
            </el-select>
          </el-form-item>

          <!-- 部件名称和代号 -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="部件名称" prop="partName">
                <el-select
                  v-model="formData.partName"
                  clearable
                  style="width: 100%"
                  @change="partNameChange"
                  placeholder="请选择项部件名称"
                >
                  <el-option
                    v-for="item in getPartNameList"
                    :key="item.partNameAndPartNumber"
                    :label="item.partNameAndPartNumber"
                    :value="item.partNameAndPartNumber"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部件件号" prop="partNumber">
                <el-input v-model="formData.partNumber" disabled
              /></el-form-item>
            </el-col>
          </el-row>
          <!-- 序列号和所属部件代号 -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="版本" prop="version">
                <el-select
                  v-model="formData.version"
                  clearable
                  @change="versionChange"
                  style="width: 100%"
                  placeholder="请选择版本"
                >
                  <el-option
                    v-for="item in getReliabilityDataOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="序列号" prop="serialNumber">
                <el-select
                  v-model="formData.serialNumber"
                  clearable
                  @change="serialNumberChange"
                  style="width: 100%"
                  placeholder="请选择项序列号"
                >
                  <el-option
                    v-for="item in getSerialNumberList"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="交付时间" prop="installationDate">
                <el-input v-model="formData.installationDate" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12" />
          </el-row>

          <!-- SRU名称和SRU代号 -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="SRU名称" prop="sruName">
                <el-select
                  v-model="formData.sruName"
                  clearable
                  style="width: 100%"
                  @change="sruNameChange"
                  placeholder="请选择项SRU名称"
                >
                  <el-option
                    v-for="item in getSRUList"
                    :key="item.name"
                    :label="item.name"
                    :value="item.name"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="SRU代号" prop="sruNumber">
                <el-input v-model="formData.sruNumber" disabled /> </el-form-item
            ></el-col>
          </el-row>

          <!-- 机型、系列、机号 -->
          <el-form-item label="机型" prop="model">
            <el-input v-model="formData.model" placeholder="请输入机型" />
          </el-form-item>

          <el-form-item label="系列" prop="series">
            <el-input v-model="formData.series" placeholder="请输入系列" />
          </el-form-item>

          <el-form-item label="机号" prop="machineNumber">
            <el-input v-model="formData.machineNumber" placeholder="请输入机号" />
          </el-form-item>

          <!-- 报告开始日期和结束日期 -->

          <el-form-item label="报告开始日期" prop="reportStartDate">
            <el-date-picker
              v-model="formData.reportStartDate"
              type="datetime"
              placeholder="请选择报告开始日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-250"
            />
          </el-form-item>
          <el-form-item label="报告结束日期" prop="reportEndDate">
            <el-date-picker
              v-model="formData.reportEndDate"
              type="datetime"
              placeholder="请选择报告结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-250"
            />
          </el-form-item>

          <!-- 拆卸日期 -->
          <el-form-item label="拆卸日期" prop="removalDate">
            <el-date-picker
              v-model="formData.removalDate"
              type="datetime"
              placeholder="请选择拆卸日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-250"
            />
          </el-form-item>

          <!-- 故障报告编码 -->
          <el-form-item label="故障报告编码" prop="failureReportNumber">
            <el-input v-model="formData.failureReportNumber" placeholder="请输入故障报告编码" />
          </el-form-item>

          <!-- 是否是产品问题  0=否，1=是-->
          <el-form-item label="是否是产品问题" prop="isProductIssue">
            <el-select v-model="formData.isProductIssue" placeholder="请选择">
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </el-form-item>

          <!-- 拆卸类型 -->
          <el-form-item label="拆卸类型*" prop="removalType">
            <el-select v-model="formData.removalType" clearable>
              <el-option label="U:非计划拆换" value="U" />
              <el-option label="S:计划拆换" value="S" />
            </el-select>
          </el-form-item>

          <!-- 拆换原因 -->
          <el-form-item label="拆换原因" prop="removalCause">
            <el-input v-model="formData.removalCause" placeholder="请输入拆换原因" />
          </el-form-item>

          <!-- 故障描述 -->
          <el-form-item label="故障描述" prop="failureDescription">
            <el-input
              v-model="formData.failureDescription"
              type="textarea"
              :rows="4"
              placeholder="请输入故障描述"
            />
          </el-form-item>

          <!-- 拆卸部件件号 -->
          <el-form-item label="拆卸部件件号" prop="removedPartNumber">
            <el-input v-model="formData.removedPartNumber" placeholder="请输入拆卸部件件号" />
          </el-form-item>

          <!-- 拆卸部件序列号 -->
          <el-form-item label="拆卸部件序列号" prop="removedPartSerialNumber">
            <el-input
              v-model="formData.removedPartSerialNumber"
              placeholder="请输入拆卸部件序列号"
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item v-if="isShowBtn">
            <el-button type="primary" @click="submitForm">提交</el-button>
            <el-button @click="resetForm">取消</el-button>
          </el-form-item>
        </el-form></ContentWrap
      >
    </el-col>
    <!-- 审批相关：流程信息 -->
    <el-col :span="8" v-if="isShowBtn">
      <ContentWrap title="审批流程" :bodyStyle="{ padding: '0 20px 0' }">
        <ProcessInstanceTimeline
          ref="timelineRef"
          :activity-nodes="activityNodes"
          :show-status-icon="false"
          @select-user-confirm="selectUserConfirm"
        /> </ContentWrap
    ></el-col>
  </el-row>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import type { FormInstance } from 'element-plus'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import { ApprovalNodeInfo } from '@/api/bpm/processInstance'
import * as DefinitionApi from '@/api/bpm/definition'
import { formatDate } from '@/utils/formatTime'
import { useTagsViewStore } from '@/store/modules/tagsView'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'

const props = defineProps({
  customerDisabled: { type: Boolean, default: false, required: false },
  isShowBtn: { type: Boolean, default: true, required: false },
  processInstanceId: { type: String, default: '', required: false },
  currentName: { type: String, default: '', required: false }
})
const route = useRoute()
const message = useMessage()
const { delView } = useTagsViewStore() // 视图操作
const { push, currentRoute } = useRouter() // 路由s
// 表单数据模型
interface FormData {
  reliabilityDataFormNo: string
  customerName: string
  customerId: string
  partName: string
  partNumber: string
  sruName: string
  sruNumber: string
  serialNumber: string
  installationDate: number | null
  version: string
  // productPartNumber: string
  model: string
  series: string
  machineNumber: string
  reportStartDate: string | null
  reportEndDate: string | null
  removalDate: string | null
  failureReportNumber: string
  isProductIssue: number
  removalType: string
  removalCause: string
  level: number | undefined
  failureDescription: string
  removedPartNumber: string
  removedPartSerialNumber: string
  formType: string
}

// 表单校验规则
const rules = reactive({
  // reliabilityDataFormNo: [{ required: true, message: '请输入表单编号', trigger: 'blur' }],
  customerName: [{ required: true, message: '请选择客户名称', trigger: 'change' }],
  partName: [{ required: true, message: '请选择部件名称', trigger: 'change' }],
  partNumber: [{ required: true, message: '请选择部件件号', trigger: 'change' }],
  serialNumber: [{ required: true, message: '请选择序列号', trigger: 'change' }],
  // productPartNumber: [{ required: true, message: '请选择所属部件代号', trigger: 'change' }],
  reportStartDate: [{ required: true, message: '请选择报告开始日期', trigger: 'change' }],
  reportEndDate: [{ required: true, message: '请选择报告结束日期', trigger: 'change' }],
  failureReportNumber: [{ required: true, message: '请输入故障报告编码', trigger: 'blur' }],
  isProductIssue: [{ required: true, message: '请选择是否是产品问题', trigger: 'change' }],
  removalType: [{ required: true, message: '请选择拆卸类型', trigger: 'change' }],
  failureDescription: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
  version: [{ required: true, message: '请选择版本', trigger: 'blur' }]
})

// 表单实例引用
const formRef = ref<FormInstance>()

// 初始化表单数据
const formData = ref<FormData>({
  reliabilityDataFormNo: '',
  customerName: '',
  customerId: '',
  partName: '',
  partNumber: '',
  sruName: '',
  sruNumber: '',
  serialNumber: '',
  installationDate: null,
  version: '',
  // productPartNumber: '',
  model: '',
  series: '',
  machineNumber: '',
  reportStartDate: formatDate(new Date()), // 默认当前日期时间,
  reportEndDate: formatDate(new Date()), // 默认当前日期时间,,
  removalDate: null,
  failureReportNumber: '',
  isProductIssue: 1,
  removalType: 'U',
  removalCause: '',
  failureDescription: '',
  level: undefined,
  removedPartNumber: '',
  removedPartSerialNumber: '',
  formType: 'reliabilityData'
})
// 客户信息列表
const customerList = ref([])
// 审批相关：变量
const processDefineKey = route.query.key as string // 流程定义 Key
const startUserSelectTasks = ref([]) // 发起人需要选择审批人的用户任务列表
const startUserSelectAssignees = ref({}) // 发起人选择审批人的数据
const tempStartUserSelectAssignees = ref({}) // 历史发起人选择审批人的数据，用于每次表单变更时，临时保存
const activityNodes = ref<ProcessInstanceApi.ApprovalNodeInfo[]>([]) // 审批节点信息
const processDefinitionId = ref('')
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用s

const disabledPart1 = computed(() => {
  return props.currentName !== '技术支持主管收集数据' && !props.isShowBtn
})
/** 审批相关：获取审批详情 */
const getApprovalDetail = async () => {
  if (!props.isShowBtn) {
    const getProcessInstanceData = await ProcessInstanceApi.getReliabilityDataById(
      props.processInstanceId
    )
    if (!getProcessInstanceData) {
      message.error('查询不到表单详情信息！')
      return
    }
    formData.value = getProcessInstanceData
    formData.value.formType = 'reliabilityData'
  } else {
    try {
      const data = await ProcessInstanceApi.getApprovalDetail({
        processDefinitionId: processDefinitionId.value,
        activityId: NodeId.START_USER_NODE_ID,
        processVariablesStr: null
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
/** 审批相关：选择发起人 */
const selectUserConfirm = (id: string, userList: any[]) => {
  startUserSelectAssignees.value[id] = userList?.map((item: any) => item.id)
}

const generateReliabilityFormNo = async () => {
  return await ProcessInstanceApi.generateReliabilityFormNo()
}

const partNameId = ref('')
const sruNameId = ref('')
// 部件名称改变时，
const partNameChange = (value: any) => {
  // partNameId.value = value

  formData.value.serialNumber = ''
  formData.value.version = ''
  formData.value.installationDate = null
  getReliabilityDataOptions.value = []
  getSerialNumberList.value = []
  const selectedPart = getPartNameList.value.find((item) => item.partNameAndPartNumber === value)
  if (selectedPart) {
    formData.value.partNumber = selectedPart.partNumber
    formData.value.partName = selectedPart.partName
    getReliabilityData(formData.value.partName)
    // getSerialNumber(formData.value.partName)
  }
}

// SRU名称改变时，自动填充对应的SRU代号
const sruNameChange = (value: any) => {
  formData.value.sruNumber = ''
  const selectedSru = getSRUList.value.find((item) => item.name === value)
  if (selectedSru) {
    formData.value.sruNumber = selectedSru.partNumber
    formData.value.sruName = selectedSru.name
  }
}

const getFillPartCodeAndDeliveryTime = async (
  partName: string,
  serialNumber: string,
  series: string
) => {
  const params = {
    partName,
    serialNumber,
    series
  }
  const getFillPartCodeAndDeliveryTime =
    await ProcessInstanceApi.getFillPartCodeAndDeliveryTime(params)
  if (!getFillPartCodeAndDeliveryTime) {
    message.error('查询不到数据！')
    return
  }

  formData.value.installationDate = getFillPartCodeAndDeliveryTime.installationDate
}

// 版本号变更
const versionChange = (value: any) => {
  formData.value.serialNumber = ''
  getSerialNumberList.value = []
  if (value) {
    getSerialNumber(formData.value.partName, value)
  }

  formData.value.installationDate = null

  if (value && formData.value.partName && formData.value.serialNumber) {
    getFillPartCodeAndDeliveryTime(formData.value.partName, formData.value.serialNumber, value)
    getSRU(formData.value.partNumber, value, formData.value.serialNumber)
  }
}

// 序列编号变更
const serialNumberChange = (value: any) => {
  /*   const selectedSerialNumber = getProductAllList.value.find((item) => item.sequenceNumber === value)
  if (selectedSerialNumber) {
    formData.value.installationDate = selectedSerialNumber.installationDate
  } */
  /*   if (!formData.value.version) {
    getReliabilityData(formData.value.partName, value)
  } */

  formData.value.installationDate = null

  if (value && formData.value.partName && formData.value.version) {
    getFillPartCodeAndDeliveryTime(formData.value.partName, value, formData.value.version)
    getSRU(formData.value.partNumber, formData.value.version, value)
  }
}

// 部件名称/代号/SRU名称/SRU代号/序列号数据
const getProductAllList = ref<any[]>([])
const getProductAll = async () => {
  const getProductAll = await ProcessInstanceApi.getProductAll()
  if (!getProductAll) {
    message.error('查询不到产品信息下拉数据！')
    return
  }

  getProductAllList.value = getProductAll
}

const getPartNameList = ref<any[]>([])
const getPartNameAll = async () => {
  const getPartNameAll = await ProcessInstanceApi.getPartNameAll()
  if (!getPartNameAll) {
    message.error('查询不到部件名称下拉数据！')
    return
  }

  getPartNameList.value = getPartNameAll
}

// 版本下拉数据
const getReliabilityDataOptions = ref<any[]>([])
const getReliabilityData = async (partName: any, serialNumber?: any) => {
  const getReliabilityData = await ProcessInstanceApi.getReliabilityDataOptions({
    partName,
    serialNumber
  })
  if (!getReliabilityData) {
    message.error('查询不到版本信息下拉数据！')
    return
  }

  getReliabilityDataOptions.value = getReliabilityData
}

// 序列号下拉数据
const getSerialNumberList = ref<any[]>([])
const getSerialNumber = async (partName: any, series?: any) => {
  const getSerialNumber = await ProcessInstanceApi.getSerialNumber({ partName, series })
  if (!getSerialNumber) {
    message.error('查询不到序列号信息下拉数据！')
    return
  }

  getSerialNumberList.value = getSerialNumber
}

// SRU下拉数据
const getSRUList = ref<any[]>([])
const getSRU = async (partNumber: any, version: any, serialNumber: any) => {
  const getSRU = await ProcessInstanceApi.findSRU({ partNumber, version, serialNumber })
  if (!getSRU) {
    message.error('查询不到SRU信息下拉数据！')
    return
  }

  getSRUList.value = getSRU
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
  if (props.isShowBtn) {
    formData.value.reliabilityDataFormNo = await generateReliabilityFormNo()
  }
  await getCustomerList()
  await getProductAll()
  await getPartNameAll()
  // 审批相关：加载最新的审批详情，主要用于节点预测
  await getApprovalDetail()
})

/** 审批相关：预测流程节点会因为输入的参数值而产生新的预测结果值，所以需重新预测一次, formData.value可改成实际业务中的特定字段 */
watch(
  formData.value,
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
// 获取客户信息列表
const getCustomerList = async () => {
  customerList.value = await ProcessInstanceApi.getCustomerInfoAll()
}
const customerChange = (value: any) => {
  const selectedCustomer = customerList.value.find((customer) => customer?.customerId === value)
  if (selectedCustomer) {
    formData.value.customerName = selectedCustomer?.customerName
  }
}
/**
 * 获取产品级别 - 根据选择的代号和SRU代号字段决定返回哪个级别的值
 * @param formData - 表单数据
 * @param productDataList - 产品数据列表
 * @returns 返回对应的产品级别
 */
const getProductLevel = (formData: FormData, productDataList: any[]) => {
  // 如果同时选择了代号(partNumber)和SRU代号(sruNumber)
  if (formData.partNumber && formData.sruNumber) {
    // 返回SRU代号对应的level
    const selectedSru = productDataList.find((item) => item.id === sruNameId.value)
    return selectedSru?.level ?? null
  }

  // 如果只选择了代号(partNumber)
  if (formData.partNumber && !formData.sruNumber) {
    // 返回代号对应的level
    const selectedPart = productDataList.find((item) => item.id === partNameId.value)

    return selectedPart?.level ?? null
  }

  // 如果都没有选择或有其他情况，返回null
  return null
}
// 提交表单方法
const submitForm = () => {
  formRef.value?.validate(async (valid) => {
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
      // 处理产品级别逻辑
      // formData.value.level = getProductLevel(formData.value, getProductAllList.value)
      // 级别逻辑优化：只返回部件代号对应的level
      /*    const selectedPart = getProductAllList.value.find((item) => item.id === partNameId.value)
      formData.value.level = selectedPart?.level ?? null */
      /*       formData.value.level =
        getProductAllList.value?.length && partNameId.value
          ? (getProductAllList.value.find((item) => item.id === partNameId.value)?.level ?? null)
          : null */

      // 2. 提交请求
      formLoading.value = true
      try {
        await ProcessInstanceApi.createProcessInstance({
          processDefinitionId: processDefinitionId.value,
          variables: formData.value,
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

// 重置表单方法
const resetForm = () => {
  formRef.value?.resetFields()
}
/** 表单验证方法 - 供外部调用（如审批通过时） */
const validate = async (): Promise<boolean> => {
  if (!formRef.value) {
    return true
  }
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

defineExpose({
  validate,
  formData: formData,
  formFlag: 'ReliabilityDataCollection'
})
</script>

<style scoped>
/* 响应式设计 */
@media (width <= 768px) {
  .flex-row {
    flex-direction: column;
  }

  .form-footer {
    flex-direction: column;
    gap: 10px;
  }

  .form-footer .el-button {
    width: 100%;
    margin-left: 0;
  }
}

.reliability-form {
  max-width: 1200px;
  padding: 20px;
  margin: 0 auto;
}

.flex-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.flex-row .el-form-item {
  flex: 1;
}

.form-footer {
  margin-top: 30px;
  text-align: right;
}

.form-footer .el-button {
  margin-left: 10px;
}

.el-form-item__label {
  font-weight: bold;
}

/* 自定义样式 */
.el-form-item__content {
  flex: 1;
}

.el-input,
.el-select,
.el-date-editor {
  width: 100%;
}

.el-textarea {
  width: 100%;
}
</style>

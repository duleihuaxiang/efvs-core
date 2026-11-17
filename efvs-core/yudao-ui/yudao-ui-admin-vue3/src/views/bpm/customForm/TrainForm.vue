<template>
  <ContentWrap title="培训管理">
    <el-row :gutter="20">
      <el-col :span="props.isShowBtn ? 16 : 21">
        <div
          ref="containerRef"
          style="height: 100vh; overflow: hidden auto"
          class="container-hide-scrollbar"
        >
          <el-form
            ref="trainFormRef"
            :model="trainingFormData"
            :rules="rules"
            label-width="120px"
            v-loading="formLoading"
          >
            <!--  <div id="part1">
              <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">培训需求</h3>

              <el-form-item label="培训主题" prop="training_theme">
                <el-input v-model="trainingFormData.training_theme" placeholder="请输入培训主题" />
              </el-form-item>

              <el-form-item label="客户名称" prop="cuastomer_name">
                <el-input v-model="trainingFormData.cuastomer_name" placeholder="请输入客户名称" />
              </el-form-item>

              <el-form-item label="培训时间" prop="training_time">
                <el-date-picker
                  v-model="trainingFormData.training_time"
                  type="daterange"
                  range-separator="-"
                  start-placeholder="请选择开始培训时间"
                  end-placeholder="请选择结束培训时间"
                  value-format="YYYY-MM-DD"
                  format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-row :gutter="20"
                ><el-col :span="12">
                  <el-form-item label="培训类型" prop="training_type">
                    <el-select
                      v-model="trainingFormData.training_type"
                      placeholder="请选择培训类型"
                      clearable
                    >
                      <el-option
                        v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_TYPE)"
                        :key="dict.value"
                        :label="dict.label"
                        :value="Number(dict.value)"
                      />
                    </el-select> </el-form-item></el-col
                ><el-col :span="12">
                  <el-form-item label="培训对象" prop="training_target">
                    <el-select
                      v-model="trainingFormData.training_target"
                      placeholder="请选择培训对象"
                    >
                      <el-option
                        v-for="dict in getIntDictOptions(
                          DICT_TYPE.TRAINING_MATERIALS_CLASSIFICATION
                        )"
                        :key="dict.value"
                        :label="dict.label"
                        :value="Number(dict.value)"
                      />
                    </el-select> </el-form-item></el-col
              ></el-row>

              <el-form-item label="培训内容" prop="training_content">
                <el-input
                  v-model="trainingFormData.training_content"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入培训内容"
                />
              </el-form-item>

              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="trainingFormData.remark"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入备注"
                />
              </el-form-item>
            </div> -->
            <div id="part2" v-if="shouldShowPart2">
              <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">培训计划</h3>
              <TrainingPlan :trainPlanDisabled="disabledPlan" ref="trainingPlanRef" />
              <!-- <el-row :gutter="20"
                ><el-col :span="12"
                  >
                  <el-form-item label="培训计划编号" prop="annualPlanProjectNumberId">
                    <el-input
                      v-model="trainingFormData.trainingPlan.annualPlanProjectNumberId"
                      disabled
                    /> </el-form-item></el-col
                ><el-col :span="12">
                 
                  <el-form-item label="培训课程" prop="trainingProgramsCourses">
                    <el-input
                      v-model="trainingFormData.trainingPlan.trainingProgramsCourses"
                      placeholder="请输入课程"
                      :disabled="!isShowBtn"
                    /> </el-form-item></el-col
              ></el-row>
              <el-row :gutter="20"
                ><el-col :span="12"
                  >
                  <el-form-item label="培训对象" prop="trainingTargets">
                    <el-input
                      v-model="trainingFormData.trainingPlan.trainingTargets"
                      placeholder="请输入培训对象"
                      :disabled="!isShowBtn"
                    /> </el-form-item></el-col
                ><el-col :span="12">
            
                  <el-form-item label="培训人数" prop="numberOfTrainees">
                    <el-input
                      v-model="trainingFormData.trainingPlan.numberOfTrainees"
                      :min="1"
                      placeholder="请输入培训人数"
                      :disabled="!isShowBtn"
                    /> </el-form-item></el-col
              ></el-row>

              <el-form-item label="培训时间" prop="trainingTimeArray">
                <el-date-picker
                  v-model="trainingFormData.trainingPlan.trainingTimeArray"
                  type="daterange"
                  range-separator="-"
                  start-placeholder="请选择开始培训时间"
                  end-placeholder="请选择结束培训时间"
                  value-format="YYYY-MM-DD"
                  format="YYYY-MM-DD"
                  :disabled="!isShowBtn"
                />
              </el-form-item>

              <el-form-item label="培训地点" prop="trainingLocation">
                <el-input
                  v-model="trainingFormData.trainingPlan.trainingLocation"
                  placeholder="请输入培训地点"
                  :disabled="!isShowBtn"
                />
              </el-form-item>

              <el-form-item label="项目负责人" prop="projectLeader">
                <el-select
                  v-model="trainingFormData.trainingPlan.projectLeader"
                  clearable
                  style="width: 100%"
                  placeholder="请选择项目负责人"
                  :disabled="!isShowBtn"
                >
                  <el-option
                    v-for="item in projectUserList"
                    :key="item.id"
                    :label="item.nickname"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="trainingFormData.trainingPlan.remark"
                  placeholder="请输入备注"
                  type="textarea"
                  :rows="4"
                  :disabled="!isShowBtn"
                /> </el-form-item
            > --></div
            >

            <div id="part3" v-if="shouldShowPart3">
              <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">培训前准备工作</h3>
              <TrainingPreparation
                :preparationDisabled="disabledPreparation"
                ref="trainingPreparationRef"
                :processInstanceId="processInstanceId"
              />
              <!-- 
              <el-form-item>
                <el-table
                  border
                  ref="tableBox"
                  :data="trainingFormData.preparationTableData"
                  :row-style="{ height: '70px' }"
                  :cell-style="{ padding: '0px' }"
                  class="mb-4"
                >
                  <el-table-column type="index" width="55" align="center" />
                  <el-table-column prop="preparation_items" label="培训前事项" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <span style="font-size: 12px; color: red">{{
                          scope.row.tooltipContent
                        }}</span>
                        <el-input
                          style="margin-top: 0"
                          v-model="scope.row.preparation_items"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart3"
                        />
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column prop="is_completed" label="是否完成" align="center" width="150">
                    <template #default="scope">
                      <el-checkbox
                        v-model="scope.row.is_completed"
                        label="是"
                        size="large"
                        :disabled="disabledPart3"
                      />
                    </template>
                  </el-table-column>

              
                </el-table> 
              </el-form-item>-->
            </div>

            <div id="part4" v-if="shouldShowPart4">
              <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5">培训签到考核证书发放 </h3>
              <TrainingsignAttendance
                ref="trainingSignAttendanceRef"
                :signAttendanceDisabled="disabledSignAttendance"
                :processInstanceId="processInstanceId"
                :projectLeaderNickName="projectLeaderNickName"
              />
              <!--  <el-form-item>
                <el-table
                  border
                  ref="tableBox"
                  :data="trainingFormData.signAttendanceTableData"
                  :row-style="{ height: '70px' }"
                  :cell-style="{ padding: '0px' }"
                  class="mb-4"
                >
                  <el-table-column type="index" width="55" align="center" />
                  <el-table-column prop="name" label="姓名" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.name"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column prop="signInTime" label="签到时间" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.signInTime"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template> </el-table-column
                  ><el-table-column prop="studyHours" label="学习时间(H)" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.studyHours"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template> </el-table-column
                  ><el-table-column prop="isStudyCompleted" label="是否完成学习" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.isStudyCompleted"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template> </el-table-column
                  ><el-table-column prop="completionMethod" label="完成形式" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.completionMethod"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template> </el-table-column
                  ><el-table-column prop="score" label="成绩" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.score"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template> </el-table-column
                  ><el-table-column prop="certificateObtained" label="获得证书" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.certificateObtained"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template> </el-table-column
                  ><el-table-column prop="instructor" label="教员" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.instructor"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column prop="completionDate" label="完成日期" align="center">
                    <template #default="scope">
                      <el-form-item>
                        <el-input
                          v-model="scope.row.completionDate"
                          maxlength="200"
                          oninput="if(value.length > 200) value=value.slice(0, 200)"
                          placeholder="请输入"
                          :disabled="disabledPart4"
                        />
                      </el-form-item>
                    </template>
                  </el-table-column>

                  <el-table-column label="操作" width="150" align="center">
                    <template #default="scope">
                      <el-button
                        size="small"
                        @click="signAttendanceAddRow(scope.$index)"
                        :disabled="disabledPart4"
                      >
                        <Icon icon="ep:plus"
                      /></el-button>
                      <el-button
                        type="primary"
                        size="small"
                        @click="signAttendanceRemoveRow(scope.$index)"
                        :disabled="
                          trainingFormData.preparationTableData.length <= 1 || disabledPart4
                        "
                      >
                        <Icon icon="ep:delete" />
                      </el-button>
                    </template>
                  </el-table-column> </el-table></el-form-item
            > --></div
            >
            <el-form-item v-if="isShowBtn">
              <el-button type="primary" @click="submitForm">提交</el-button>
              <el-button @click="resetForm">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <!-- <el-col :span="2" > -->
      <div :style="{ position: 'fixed', right: props.isShowBtn ? '23vw' : '28vw' }">
        <el-anchor
          :affix="true"
          :container="containerRef"
          direction="vertical"
          type="underline"
          :offset="50"
          :select-scroll-top="true"
          @click="handleClick"
        >
          <!-- <el-anchor-link href="#part1" title="培训需求" /> -->
          <el-anchor-link href="#part2" title="培训计划" />
          <el-anchor-link href="#part3" title="培训前准备工作" v-if="shouldShowPart3" />
          <el-anchor-link href="#part4" title="培训签到考核证书发放" v-if="shouldShowPart4" />
        </el-anchor>
      </div>
      <!-- </el-col> -->
      <el-col :span="2" />

      <!-- 审批相关：流程信息 -->
      <el-col :span="6" v-if="isShowBtn">
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
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import ProcessInstanceTimeline from '@/views/bpm/processInstance/detail/ProcessInstanceTimeline.vue'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import * as DefinitionApi from '@/api/bpm/definition'
import * as FormApi from '@/api/bpm/form'
import { ApprovalNodeInfo } from '@/api/bpm/processInstance'
import type { FormInstance, FormRules } from 'element-plus'
import * as UserApi from '@/api/system/user'
// import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CandidateStrategy, NodeId } from '@/components/SimpleProcessDesignerV2/src/consts'
import { useTagsViewStore } from '@/store/modules/tagsView'
import TrainingPlan from './TrainingPlan.vue'
import TrainingPreparation from './TrainingPreparation.vue'
import TrainingsignAttendance from './TrainingSignAttendance.vue'

// 表单数据模型
interface FormData {
  trainingPlan: {
    annualPlanProjectNumberId: string
    trainingProgramsCourses: string
    trainingTargets: string
    numberOfTrainees: null
    trainingTimeArray: Array<string>
    trainingStartTime: string
    trainingEndTime: string
    trainingLocation: string
    projectLeader: string
    remark: string
    formType: string
  }
  preparationTableData: {
    preparation_items: string
    is_completed: string
    tooltipContent: string
  }[]
  signAttendanceTableData: {
    name: string // 姓名
    signInTime: string // 签到时间
    studyHours: string // 学习时间(H)
    isStudyCompleted: string // 是否完成学习
    completionMethod: string // 完成形式
    score: string // 成绩
    certificateObtained: string // 获得证书
    instructor: string // 教员
    completionDate: string // 完成日期
  }[]
  formType: string
}

const props = defineProps({
  isShowBtn: { type: Boolean, default: true, required: false },
  processInstanceId: { type: String, default: '', required: false },
  currentName: { type: String, default: '', required: false }
})
const message = useMessage()
const route = useRoute()
const { delView } = useTagsViewStore() // 视图操作
const { push, currentRoute } = useRouter() // 路由
const containerRef = ref<HTMLElement | null>(null)
const trainingPlanRef = ref<InstanceType<typeof TrainingPlan> | null>(null)
const trainingPreparationRef = ref<InstanceType<typeof TrainingPreparation> | null>(null)
const trainingSignAttendanceRef = ref<InstanceType<typeof TrainingsignAttendance> | null>(null)
// 审批相关：变量
const processDefineKey = route.query.key as string // 流程定义 Key
const startUserSelectTasks = ref([]) // 发起人需要选择审批人的用户任务列表
const startUserSelectAssignees = ref({}) // 发起人选择审批人的数据
const tempStartUserSelectAssignees = ref({}) // 历史发起人选择审批人的数据，用于每次表单变更时，临时保存
const activityNodes = ref<ProcessInstanceApi.ApprovalNodeInfo[]>([]) // 审批节点信息
const processDefinitionId = ref('')
const trainFormRef = ref()
const formLoading = ref(false)
const handleClick = (e: MouseEvent) => {
  e.preventDefault()
  const targetId = e.currentTarget.getAttribute('href')
  if (!targetId) return

  const targetElement = document.querySelector(targetId)
  if (targetElement) {
    // 手动滚动到目标元素
    targetElement.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    })
  }
}

// 初始化表单数据
const trainingFormData = ref<FormData>({
  //培训计划
  trainingPlan: {
    annualPlanProjectNumberId: '',
    trainingProgramsCourses: '',
    trainingTargets: '',
    numberOfTrainees: null,
    trainingTimeArray: [],
    trainingStartTime: '',
    trainingEndTime: '',
    trainingLocation: '',
    projectLeader: '',
    remark: '',
    formType: 'trainingPlan'
  },

  // 培训前准备工作

  preparationTableData: [
    {
      preparation_items: '',
      is_completed: '',
      tooltipContent: '完成培训需求收集并和客户确认'
    },

    {
      preparation_items: '',
      is_completed: '',
      tooltipContent: '通知学员培训时间、地点、培训内容'
    },

    {
      preparation_items: '',
      is_completed: '',
      tooltipContent: '课件准备、考试试题准备、教具准备、'
    },
    {
      preparation_items: '',
      is_completed: '',
      tooltipContent:
        '培训资料、工具和器材准备（电脑、数码投影仪、激光笔、白板、书写设备、扩音设备等）'
    },
    {
      preparation_items: '',
      is_completed: '',
      tooltipContent: '准备三餐、休息间隙零食、饮水、住宿、培训和会议场地、交通用车'
    },
    {
      preparation_items: '',
      is_completed: '',
      tooltipContent: '审查学员资质'
    },
    {
      preparation_items: '',
      is_completed: '',
      tooltipContent: '其他事项'
    }
  ],
  // 培训签到考核证书发放

  signAttendanceTableData: [
    {
      name: '王新元',
      signInTime: '2025-10-22 10:22',
      studyHours: '5',
      isStudyCompleted: '是',
      completionMethod: '理论考试',
      score: '80',
      certificateObtained: '培训证书.png',
      instructor: '张大拿',
      completionDate: '2025-10-22'
    }
  ],
  formType: 'trainingPlan'
})

// 表单校验规则
const rules = reactive<FormRules<FormData>>({
  signAttendanceTableData: [
    { required: true, message: '培训签到考核证书发放表格不能为空', trigger: 'blur' }
  ]
})

const getVisibleParts = computed(() => {
  const { currentName, isShowBtn } = props

  if (isShowBtn || currentName === '培训主管确认培训计划') {
    // 当 currentName === '培训主管确认培训计划' 以及 props.isShowBtn 时，只展示 part2
    return { part2: true, part3: false, part4: false }
  } else if (currentName === '培训教员培训前准备') {
    // 当 currentName === '培训教员培训前准备' 时，展示 part2 和 part3
    return { part2: true, part3: true, part4: false }
  } else {
    // 其他情况 part2, part3, part4 都展示
    return { part2: true, part3: true, part4: true }
  }
})
const shouldShowPart2 = computed(() => getVisibleParts.value.part2)
const shouldShowPart3 = computed(() => getVisibleParts.value.part3)
const shouldShowPart4 = computed(() => getVisibleParts.value.part4)
const disabledPlan = computed(() => {
  return props.currentName !== '培训主管确认培训计划' && !props.isShowBtn
})
const disabledPreparation = computed(() => {
  return props.currentName !== '培训教员培训前准备'
})
const disabledSignAttendance = computed(() => {
  return props.currentName !== '培训教员培训签到考核发证书'
})
/** 审批相关：选择发起人 */
const selectUserConfirm = (id: string, userList: any[]) => {
  startUserSelectAssignees.value[id] = userList?.map((item: any) => item.id)
}
// 提交表单
const submitForm = async () => {
  if (!trainingPlanRef.value) return
  try {
    await trainingPlanRef.value.validatePlan()
    // 如果验证通过，继续执行后续逻辑
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
    const subData = ref({})

    subData.value = trainingPlanRef.value.variablesData.trainPlanData.value
    if (subData.value.trainingTimeArray?.length) {
      subData.value.trainingStartTime = subData.value.trainingTimeArray[0]
      subData.value.trainingEndTime = subData.value.trainingTimeArray[1]
    }

    try {
      await ProcessInstanceApi.createProcessInstance({
        processDefinitionId: processDefinitionId.value,
        variables: subData.value,
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
  } catch (error) {
    message.error('请检查表单填写是否正确')
    return
  }
}

const projectLeaderNickName = ref('')

/** 审批相关：获取审批详情 */
const getApprovalDetail = async () => {
  if (!props.isShowBtn) {
    const getTrainingPlanData = await FormApi.getTrainingPlan({
      processInstanceId: props.processInstanceId
    })
    if (!getTrainingPlanData) {
      message.error('查询不到培训计划表单详情信息！')
      return
    }
    if (trainingPlanRef.value) {
      trainingPlanRef.value.variablesData.trainPlanData.value = getTrainingPlanData
      trainingPlanRef.value.variablesData.trainPlanData.value.trainingTimeArray = [
        getTrainingPlanData.trainingStartTime,
        getTrainingPlanData.trainingEndTime
      ]
      if (getTrainingPlanData.projectLeaderNickName && trainingSignAttendanceRef.value) {
        trainingSignAttendanceRef.value.updateInstructor(getTrainingPlanData.projectLeaderNickName)
        projectLeaderNickName.value = getTrainingPlanData.projectLeaderNickName
      }
    }
  } else {
    try {
      const data = await ProcessInstanceApi.getApprovalDetail({
        processDefinitionId: processDefinitionId.value,
        activityId: NodeId.START_USER_NODE_ID,
        processVariablesStr: JSON.stringify({ isReply: trainingFormData.value.isReply }) // 解决 GET 无法传递对象的问题，后端 String 再转 JSON
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

  // 审批相关：加载最新的审批详情，主要用于节点预测
  await getApprovalDetail()
})

// 重置表单
const resetForm = () => {
  trainFormRef.value?.resetFields()
}
/** 表单验证方法 - 供外部调用（如审批通过时） */
const validate = async (): Promise<boolean> => {
  console.log(trainingPreparationRef.value, trainingSignAttendanceRef.value, props.currentName)

  if (props.currentName === '培训教员培训前准备' && trainingPreparationRef.value) {
    try {
      await trainingPreparationRef.value.validatePreparation()
      return true
    } catch {
      return false
    }
  } else if (
    props.currentName === '培训教员培训签到考核发证书' &&
    trainingSignAttendanceRef.value
  ) {
    try {
      await trainingSignAttendanceRef.value.validateSignAttendance()
      return true
    } catch {
      return false
    }
  } else {
    return true
  }
}

defineExpose({
  validate,
  get formData() {
    // 根据当前状态动态获取数据
    if (props.currentName === '培训主管确认培训计划') {
      return trainingPlanRef.value?.variablesData || {}
    } else if (props.currentName === '培训教员培训前准备') {
      return trainingPreparationRef.value?.variablesData || {}
    } else if (props.currentName === '培训教员培训签到考核发证书') {
      return trainingSignAttendanceRef.value?.variablesData || {}
    } else {
      return {}
    }
  },
  formFlag: 'trainingForm'
})
</script>

<style scoped lang="scss">
.training-form {
  max-width: 800px;
  margin: 0 auto;
}

/* 隐藏滚动条 */
.container-hide-scrollbar {
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.container-hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.anchor-fixed {
  position: fixed;
  top: 200px;
  right: 270px;
  z-index: 999;
}

.table-disabled {
  pointer-events: none;

  // 禁用所有输入元素
  :deep(input),
  :deep(textarea),
  :deep(select),
  :deep(.el-input__inner),
  :deep(.el-textarea__inner),
  :deep(.el-select),
  :deep(.el-checkbox),
  :deep(.el-button) {
    pointer-events: none;
    opacity: 0.6;
    filter: grayscale(100%);
  }
}
</style>

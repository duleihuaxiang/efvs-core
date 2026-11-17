<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="150px"
    >
      <el-form-item label="培训计划编号" prop="annualPlanProjectNumberId">
        <el-input
          v-model="queryParams.annualPlanProjectNumberId"
          placeholder="请输入培训计划编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训课程" prop="trainingProgramsCourses">
        <el-input
          v-model="queryParams.trainingProgramsCourses"
          placeholder="请输入培训课程"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训地点" prop="trainingLocation">
        <el-input
          v-model="queryParams.trainingLocation"
          placeholder="请输入培训地点"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>

      <el-form-item label="培训时间" prop="trainingTime">
        <el-date-picker
          v-model="trainingTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          @change="handleTrainingTimeChange"
        />
      </el-form-item>
      <el-form-item label="项目负责人" prop="projectLeader">
        <!--     <el-input
          v-model="queryParams.projectLeader"
          placeholder="请输入项目负责人"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        /> -->
        <el-select
          v-model="queryParams.projectLeader"
          clearable
          placeholder="请选择项目负责人"
          class="!w-240px"
        >
          <el-option
            v-for="item in projectUserList"
            :key="item.id"
            :label="item.nickname"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" @click="launchProcess"> 发起流程 </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:training-plan:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <!-- <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:training-plan:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button> -->
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <!-- <el-table-column label="ID" align="center" prop="id" /> -->
      <el-table-column label="培训主题" align="center" prop="trainingTheme" />
      <el-table-column
        label="培训计划编号"
        align="center"
        prop="annualPlanProjectNumberId"
        width="180px"
      />
      <el-table-column label="培训课程" align="center" prop="trainingProgramsCourses" />
      <el-table-column label="培训对象" align="center" prop="trainingTargets" />
      <el-table-column label="培训人数" align="center" prop="numberOfTrainees" />
      <el-table-column
        label="培训时间"
        align="center"
        prop="trainingTime"
        :formatter="dateFormatter"
        width="180px"
        ><template #default="{ row }">
          <span v-if="row.trainingStartTime || row.trainingEndTime">
            {{ row.trainingStartTime }}~{{ row.trainingEndTime }}
          </span>
        </template></el-table-column
      >
      <el-table-column label="培训地点" align="center" prop="trainingLocation" />
      <el-table-column label="项目负责人" align="center" prop="projectLeader">
        <template #default="{ row }">
          {{ getProjectLeaderName(row.projectLeader) }}
        </template></el-table-column
      >
      <!--       <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="更新时间"
        align="center"
        prop="updateTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="创建人" align="center" prop="createName" /> -->
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="{ row }">
          <el-button link type="primary" @click="toDetail(row)"> 详情 </el-button>
          <el-button link type="primary" @click="openPreparationList(row.processInstanceId)">
            培训前准备
          </el-button>

          <!-- <el-button
            link
            type="danger"  
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:training-plan:delete']"
          >
            删除
          </el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <TrainingPlanForm ref="formRef" @success="getList" />
  <PreparationList ref="PreparationListRef" />
</template>

<script setup lang="ts">
// import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { TrainingPlanApi, TrainingPlan } from '@/api/data/trainingplan'
import PreparationList from './PreparationList.vue'
import * as UserApi from '@/api/system/user'
import TrainingPlanForm from './TrainingPlanForm.vue'
import * as DefinitionApi from '@/api/bpm/definition'

/** 培训计划 列表 */
defineOptions({ name: 'TrainingPlan' })

const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化
const router = useRouter()

const loading = ref(true) // 列表的加载中
const list = ref<TrainingPlan[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const trainingTime = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  annualPlanProjectNumberId: undefined,
  trainingProgramsCourses: undefined,
  projectLeader: undefined,
  trainingLocation: undefined,
  trainingStartTime: undefined,
  trainingEndTime: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
// 响应式变量存储获取到的数据
const idDataMap = ref<any>({})
const PreparationListRef = ref()
const projectUserList = ref<any[]>([])
/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TrainingPlanApi.getTrainingPlanPage(queryParams)
    list.value = data.list
    total.value = data.total
    const idArray = data.list.map((item) => item.projectLeader).filter((leader) => Boolean(leader)) // Boolean(leader) 会过滤掉 null, undefined, '', 0, false 等假值
    if (idArray.length > 0) {
      const idData = await TrainingPlanApi.getTrainingById({ ids: idArray.toString() })
      idDataMap.value = idData
    }
  } finally {
    loading.value = false
  }
}

/** 培训时间选择变化处理 */
const handleTrainingTimeChange = (value: string[] | null) => {
  if (value && value.length === 2) {
    queryParams.trainingStartTime = value[0]
    queryParams.trainingEndTime = value[1]
  } else {
    queryParams.trainingStartTime = undefined
    queryParams.trainingEndTime = undefined
  }
}

// 根据传入的 ID 获取对应的值
const getProjectLeaderName = (id: string | number): string => {
  if (!id) return ''
  if (!id || !idDataMap.value) return String(id)

  // 如果 idData 是对象格式：{ id1: name1, id2: name2, ... }
  if (typeof idDataMap.value === 'object' && !Array.isArray(idDataMap.value)) {
    return idDataMap.value[String(id)] || String(id)
  }

  // 如果 idData 是数组格式：[{id: id1, name: name1}, ...]
  if (Array.isArray(idDataMap.value)) {
    const foundItem = idDataMap.value.find((item: any) => item.id == id)
    return foundItem
      ? foundItem.name || foundItem.nickname || foundItem.username || String(id)
      : String(id)
  }

  return String(id)
}

/** 查询项目负责人下拉列表 */
const getProjectUserList = async () => {
  const data = await UserApi.getUserListByDeptId()
  projectUserList.value = data
}
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  queryParams.trainingStartTime = undefined
  queryParams.trainingEndTime = undefined
  trainingTime.value = []
  handleQuery()
}

// 查看详情
const toDetail = (row: any) => {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId,
      taskId: row.taskId,
      key: row.processKey
    }
  })
}
const openPreparationList = async (processInstanceId: number) => {
  PreparationListRef.value.openList(processInstanceId)
}

//发起流程
const launchProcess = async () => {
  try {
    // 获取所有激活状态的流程定义
    const processList = await DefinitionApi.getProcessDefinitionList({
      suspensionState: 1
    })

    // 查找相关的流程定义
    const customerResponseProcess = processList.find((process: any) => {
      return (
        process.formCustomCreatePath === '/bpm/customForm/TrainForm' ||
        process.name === '培训服务管理'
      )
    })

    if (!customerResponseProcess) {
      message.error('未找到培训服务管理对应的流程定义')
      return
    }

    // 获取流程定义的 key
    const processKey = customerResponseProcess.key

    // 跳转到流程发起页面
    router.push({
      name: 'TrainForm',
      query: {
        key: processKey
      }
    })
  } catch (error) {
    console.error('发起流程失败:', error)
    message.error('发起流程失败，请稍后重试')
  }
}
/** 添加/修改操作 */
// const formRef = ref()
/* const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
} */

/** 删除按钮操作 */
/* const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await TrainingPlanApi.deleteTrainingPlan(id)
    message.success(t('common.delSuccess'))

    // 刷新列表
    await getList()
  } catch {}
} */

/** 批量删除培训计划 */
/* const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await TrainingPlanApi.deleteTrainingPlanList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
} */

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: TrainingPlan[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await TrainingPlanApi.exportTrainingPlan(queryParams)
    download.excel(data, '培训计划.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
  getProjectUserList()
})
</script>

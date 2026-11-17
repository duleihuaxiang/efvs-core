<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="培训主题" prop="trainingTheme">
        <el-input
          v-model="queryParams.trainingTheme"
          placeholder="请输入培训主题"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训时间" prop="trainingTime">
        <el-date-picker
          v-model="queryParams.trainingTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
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
      <el-form-item label="培训成绩" prop="achieveResults">
        <el-input
          v-model="queryParams.achieveResults"
          placeholder="请输入培训成绩"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训教师" prop="trainingTeachers">
        <el-input
          v-model="queryParams.trainingTeachers"
          placeholder="请输入培训教师"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['training:experience:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['training:experience:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <!--     <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['training:experience:delete']"
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
      <el-table-column label="用户名" align="center" prop="userId">
        <template #default="{ row }">
          {{ getProjectLeaderName(row.userId) }}
        </template>
      </el-table-column>
      <el-table-column label="培训主题" align="center" prop="trainingTheme" />
      <el-table-column
        label="培训时间"
        align="center"
        prop="trainingTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="培训地点" align="center" prop="trainingLocation" />
      <el-table-column label="培训时长" align="center" prop="trainingDuration" />
      <el-table-column label="培训成绩" align="center" prop="achieveResults" />
      <el-table-column label="培训教师" align="center" prop="trainingTeachers" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="备注" align="center" prop="remark" />
      <!--     <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['training:experience:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['training:experience:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column> -->
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
  <ExperienceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import { isEmpty } from '@/utils/is'
import { TrainingPlanApi } from '@/api/data/trainingplan'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as UserApi from '@/api/system/user'
import { ExperienceApi, Experience } from '@/api/training/trainingexperience'
import ExperienceForm from './ExperienceForm.vue'

/** 培训经历 列表 */
defineOptions({ name: 'TrainingExperience' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<Experience[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  trainingTheme: undefined,
  trainingTime: [],
  trainingLocation: undefined,
  achieveResults: undefined,
  trainingTeachers: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
// 响应式变量存储获取到的数据
const idDataMap = ref<any>({})
/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ExperienceApi.getExperiencePage(queryParams)
    list.value = data.list
    total.value = data.total
    const idArray = data.list.map((item) => item.userId).filter((leader) => Boolean(leader)) // Boolean(leader) 会过滤掉 null, undefined, '', 0, false 等假值
    if (idArray.length > 0) {
      const idData = await TrainingPlanApi.getTrainingById({ ids: idArray.toString() })
      idDataMap.value = idData
    }
  } finally {
    loading.value = false
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

const getUserInfo = async (userId: number) => {
  try {
    const data = await UserApi.getSimpleUserList(userId)
    return data.nickname
  } catch {
    return '未知用户'
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await ExperienceApi.deleteExperience(id)
    message.success(t('common.delSuccess'))

    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除培训经历 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await ExperienceApi.deleteExperienceList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Experience[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await ExperienceApi.exportExperience(queryParams)
    download.excel(data, '培训经历.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

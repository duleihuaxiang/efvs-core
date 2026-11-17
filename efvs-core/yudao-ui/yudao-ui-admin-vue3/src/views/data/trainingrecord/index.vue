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
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="课程名称" prop="courseName">
        <el-input
          v-model="queryParams.courseName"
          placeholder="请输入课程名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="是否完成" prop="isCompleted">
        <el-select
          v-model="queryParams.isCompleted"
          placeholder="请选择是否完成"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="item in isCompletedOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="完成日期" prop="completedTime">
        <el-date-picker
          v-model="queryParams.completedTime"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-value="[new Date(2010, 9, 1), new Date(2010, 10, 1)]"
        />
      </el-form-item>
      <el-form-item label="教员" prop="instructor">
        <el-input
          v-model="queryParams.instructor"
          placeholder="请输入教员"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <!--  <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['data:training-record:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button> -->
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:training-record:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <!--  <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:training-record:delete']"
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
      :show-overflow-tooltip="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <!-- <el-table-column label="ID" align="center" prop="id" /> -->
      <el-table-column label="编号" align="center" prop="serialNumber" />
      <el-table-column label="姓名" align="center" prop="name" />
      <el-table-column label="课程名称" align="center" prop="courseName" />
      <el-table-column label="学习时间" align="center" prop="learningTime" />
      <!-- (0 否 1 是) -->
      <el-table-column label="是否完成" align="center" prop="isCompleted">
        <template #default="scope">
          <el-tag v-if="scope.row.isCompleted === 0">否</el-tag>
          <el-tag v-else>是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="完成形式" align="center" prop="perfectiveForm" />
      <el-table-column label="成绩" align="center" prop="grades" />
      <el-table-column label="教员" align="center" prop="instructor" />
      <el-table-column
        label="完成日期"
        align="center"
        prop="completionDate"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
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
      <!--  <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['data:training-record:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:training-record:delete']"
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
  <TrainingRecordForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { TrainingRecordApi, TrainingRecord } from '@/api/data/trainingrecord'
import TrainingRecordForm from './TrainingRecordForm.vue'

/** 培训记录 列表 */
defineOptions({ name: 'TrainingRecord' })

const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<TrainingRecord[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  serialNumber: undefined,
  name: undefined,
  courseName: undefined,
  isCompleted: undefined,
  completedTime: '',
  instructor: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const isCompletedOptions = [
  {
    value: 0,
    label: '否'
  },
  {
    value: 1,
    label: '是'
  }
]
/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TrainingRecordApi.getTrainingRecordPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
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
/* const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
} */

/** 删除按钮操作 */
/* const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await TrainingRecordApi.deleteTrainingRecord(id)
    message.success(t('common.delSuccess'))
    // currentRow.value = {}
    // 刷新列表
    await getList()
  } catch {}
} */

/** 批量删除培训记录 */
/* const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await TrainingRecordApi.deleteTrainingRecordList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
} */

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: TrainingRecord[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await TrainingRecordApi.exportTrainingRecord(queryParams)
    download.excel(data, '培训记录.xls')
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

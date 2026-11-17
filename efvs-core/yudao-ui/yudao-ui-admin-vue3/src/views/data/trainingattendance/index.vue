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
      <!--   <el-form-item label="编号" prop="serialNumber">
        <el-input
          v-model="queryParams.serialNumber"
          placeholder="请输入编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item> -->
      <el-form-item label="姓名 " prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名 "
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="单位名称" prop="unitName">
        <el-input
          v-model="queryParams.unitName"
          placeholder="请输入单位名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训主题" prop="trainingTheme">
        <el-input
          v-model="queryParams.trainingTheme"
          placeholder="请输入培训主题"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训日期" prop="trainingTime">
        <el-date-picker
          v-model="queryParams.trainingTime"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-value="[new Date(2010, 9, 1), new Date(2010, 10, 1)]"
        />
      </el-form-item>
      <el-form-item label="培训讲师" prop="trainingInstructor">
        <el-input
          v-model="queryParams.trainingInstructor"
          placeholder="请输入培训讲师"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训类别" prop="trainingType">
        <el-select
          v-model="queryParams.trainingType"
          placeholder="请选择培训类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="Number(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <!--  <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['data:training-attendance:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button> -->
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:training-attendance:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <!--   <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:training-attendance:delete']"
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
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="编号" align="center" prop="serialNumber" />
      <el-table-column label="姓名 " align="center" prop="name" />
      <el-table-column label="单位名称" align="center" prop="unitName" />
      <el-table-column label="培训主题" align="center" prop="trainingTheme" />
      <el-table-column
        label="培训时间"
        align="center"
        prop="trainingTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <!--  (0 否 1 是) -->
      <el-table-column label="培训讲师" align="center" prop="trainingInstructor">
        <template #default="scope">
          <el-tag v-if="scope.row.trainingInstructor === 0">否</el-tag>
          <el-tag v-else>是</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="培训类别" align="center" prop="trainingCategory" />
      <el-table-column label="培训地点" align="center" prop="trainingLocation" />
      <el-table-column label="培训内容" align="center" prop="trainingContent" />
      <!--   <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      /> -->
      <el-table-column label="备注" align="center" prop="remark" />
      <!--     <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['data:training-attendance:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:training-attendance:delete']"
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
  <TrainingAttendanceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { TrainingAttendanceApi, TrainingAttendance } from '@/api/data/trainingattendance'
import TrainingAttendanceForm from './TrainingAttendanceForm.vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** 培训签到 列表 */
defineOptions({ name: 'TrainingAttendance' })

const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<TrainingAttendance[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  serialNumber: undefined,
  name: undefined,
  unitName: undefined,
  trainingTheme: undefined,
  trainingTime: '', //培训日期
  trainingInstructor: '', //培训讲师
  trainingType: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TrainingAttendanceApi.getTrainingAttendancePage(queryParams)
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
    await TrainingAttendanceApi.deleteTrainingAttendance(id)
    message.success(t('common.delSuccess'))
    // currentRow.value = {}
    // 刷新列表
    await getList()
  } catch {}
} */

/** 批量删除培训签到 */
/* const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await TrainingAttendanceApi.deleteTrainingAttendanceList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
} */

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: TrainingAttendance[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await TrainingAttendanceApi.exportTrainingAttendance(queryParams)
    download.excel(data, '培训签到.xls')
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

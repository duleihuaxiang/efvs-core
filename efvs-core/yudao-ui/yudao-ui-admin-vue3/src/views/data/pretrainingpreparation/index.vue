<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item label="编号" prop="serialNumber">
        <el-input
          v-model="queryParams.serialNumber"
          placeholder="请输入编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="培训前事项" prop="preTrainingMatters">
        <el-input
          v-model="queryParams.preTrainingMatters"
          placeholder="请输入培训前事项"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <!--  (0 否 1 是) -->
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
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['data:pre-training-preparation:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:pre-training-preparation:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:pre-training-preparation:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
        <!--   <el-button type="danger" plain @click="openPreparationList">
          <Icon icon="ep:delete" class="mr-5px" /> 培训前准备
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
      <el-table-column label="培训主题" align="center" prop="" />
      <el-table-column label="培训对象" align="center" prop="trainingTargets" />
      <el-table-column label="培训人数" align="center" prop="numberOfTrainees" />
      <el-table-column
        label="培训时间"
        align="center"
        prop="trainingTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="培训地点" align="center" prop="trainingLocation" />
      <el-table-column label="项目负责人" align="center" prop="projectLeader" />
      <el-table-column label="创建人" align="center" prop="" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['data:pre-training-preparation:update']"
          >
            编辑
          </el-button>
          <el-button link type="success" @click="openPreparationList"> 培训前准备 </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:pre-training-preparation:delete']"
          >
            删除
          </el-button>
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
  <PreTrainingPreparationForm ref="formRef" @success="getList" />
  <PreparationList ref="PreparationListRef" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import {
  PreTrainingPreparationApi,
  PreTrainingPreparation
} from '@/api/data/pretrainingpreparation'
import PreTrainingPreparationForm from './PreTrainingPreparationForm.vue'
import PreparationList from './PreparationList.vue'

/** 培训前准备 列表 */
defineOptions({ name: 'PreTrainingPreparation' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<PreTrainingPreparation[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  serialNumber: undefined,
  preTrainingMatters: undefined,
  isCompleted: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const PreparationListRef = ref()

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
    const data = await PreTrainingPreparationApi.getPreTrainingPreparationPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openPreparationList = async () => {
  PreparationListRef.value.openList()
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
    await PreTrainingPreparationApi.deletePreTrainingPreparation(id)
    message.success(t('common.delSuccess'))
    // currentRow.value = {}
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除培训前准备 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await PreTrainingPreparationApi.deletePreTrainingPreparationList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: PreTrainingPreparation[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await PreTrainingPreparationApi.exportPreTrainingPreparation(queryParams)
    download.excel(data, '培训前准备.xls')
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

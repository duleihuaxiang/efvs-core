<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <!-- 标题 -->
    <!-- <ContentWrap> -->
    <el-form
      ><el-formItem>
        <el-button type="primary" @click="openForm('create')">
          <Icon icon="ep:plus" class="mr-2" /> 新增
        </el-button>
        <el-button type="success" plain @click="openForm('update')">
          <Icon icon="ep:edit" class="mr-2" /> 编辑信息
        </el-button>
        <el-button type="danger" plain :disabled="isEmpty(checkedIds)" @click="handleDeleteBatch">
          <Icon icon="ep:delete" class="mr-2" /> 批量删除
        </el-button></el-formItem
      ></el-form
    >
    <!-- </ContentWrap>/ -->

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
        <el-table-column label="编号" align="center" prop="serialNumber" />
        <el-table-column label="培训前事项" align="left" prop="preTrainingMatters" />
        <el-table-column label="是否完成" align="center" width="120px">
          <template #default="scope">
            <el-checkbox v-model="scope.row.isCompleted" />
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
  </Dialog>
  <!-- 表单弹窗 -->
  <PreTrainingPreparationForm ref="PreparationFormRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import {
  PreTrainingPreparationApi,
  PreTrainingPreparation
} from '@/api/data/pretrainingpreparation'
import PreTrainingPreparationForm from './PreTrainingPreparationForm.vue'

const message = useMessage()
const { t } = useI18n()

const dialogVisible = ref(false)
const dialogTitle = ref('本次培训前准备工作')
const loading = ref(true)
const list = ref<PreTrainingPreparation[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})
/* const queryFormRef = ref()
const exportLoading = ref(false) */

// const emit= defineEmits(['openList'])

const openList = () => {
  dialogVisible.value = true
}
// 查询列表
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

// 添加/修改操作
const PreparationFormRef = ref()
const openForm = (type: string, id?: number) => {
  PreparationFormRef.value.open(type, id)
}

// 批量删除
const handleDeleteBatch = async () => {
  try {
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

// 初始化
onMounted(() => {
  getList()
})

defineExpose({
  openList
})
</script>

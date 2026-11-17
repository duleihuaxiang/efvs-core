<!-- 工作流 - 抄送我的流程 -->
<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      ref="queryFormRef"
      :inline="true"
      class="-mb-15px"
      label-width="68px"
      :model="queryParams"
    >
      <el-form-item label="流程名称" prop="processInstanceName">
        <el-input
          v-model="queryParams.processInstanceName"
          @keyup.enter="handleQuery"
          class="!w-240px"
          clearable
          placeholder="请输入流程名称"
        />
      </el-form-item>
      <el-form-item label="抄送时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          end-placeholder="结束日期"
          start-placeholder="开始日期"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" class="custom-table">
      <el-table-column align="left" label="流程名称" prop="processInstanceName" min-width="190">
        <template #default="scope">
          <el-text
            >【{{ scope.row.startUser.nickname }}】在 【{{
              formatDate(scope.row.processInstanceStartTime)
            }}】 发起 【{{ scope.row.processInstanceName }}】</el-text
          >
        </template>
      </el-table-column>

      <el-table-column align="center" label="抄送节点" prop="activityName" min-width="180" />
      <el-table-column align="center" label="抄送人">
        <template #default="scope"> {{ scope.row.createUser?.nickname || '系统' }} </template>
      </el-table-column>
      <el-table-column align="center" label="抄送意见" prop="reason" />
      <el-table-column
        align="center"
        label="抄送时间"
        prop="createTime"
        :formatter="dateFormatter"
      />
      <el-table-column align="center" label="操作" fixed="right" width="80">
        <template #default="scope">
          <el-button link type="primary" @click="handleAudit(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>
<script lang="ts" setup>
import { dateFormatter, formatDate } from '@/utils/formatTime'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'

defineOptions({ name: 'BpmProcessInstanceCopy' })

const { push } = useRouter() // 路由

const loading = ref(false) // 列表的加载中
const total = ref(0) // 列表的总页数
const list = ref([]) // 列表的数据
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  processInstanceId: '',
  processInstanceName: '',
  createTime: []
})
const queryFormRef = ref() // 搜索的表单

/** 查询任务列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ProcessInstanceApi.getProcessInstanceCopyPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 处理审批按钮 */
const handleAudit = (row: any) => {
  console.log('row', row)
  const query = {
    id: row.processInstanceId,
    activityId: undefined,
    key: row.processInstanceKey
  }
  if (row.activityId) {
    query.activityId = row.activityId
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: query
  })
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

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
<style lang="scss" scoped>
.custom-table :deep(.el-table__header th) {
  text-align: center !important; /* 表头强制居中 */
}
</style>

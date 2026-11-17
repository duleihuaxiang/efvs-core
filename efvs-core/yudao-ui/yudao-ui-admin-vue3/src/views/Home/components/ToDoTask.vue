<!-- src/components/ToDoChart.vue -->
<template>
  <div class="todo-chart">
    <div class="chart-header">
      <h3>待办事项</h3>
    </div>
    <div class="chart-content">
      <el-table v-loading="loading" style="width: 100%" max-height="200px" :data="list">
        <el-table-column align="left" label="流程名称" prop="processInstance.name">
          <template #default="scope">
            <el-text>
              <!-- <div class="icon-tag-container"> -->
              <el-icon
                v-if="scope.row.timeOut"
                :class="['bellFilled', scope.row.timeOut ? 'bellFilled_red' : '']"
              >
                <BellFilled />
              </el-icon>
              <span v-if="scope.row.timeOut" style="color: rgb(238 68 68)">{{
                isTaskTimeout(scope.row.timeOut) ? '【已超时】' : '【即将超时】'
              }}</span>
              <!-- </div> -->
              【{{ scope.row.processInstance.startUser.nickname }}】在【{{
                formatDate(scope.row.processInstance.createTime)
              }}】发起【{{ scope.row.processInstance.name }}】
            </el-text>
          </template></el-table-column
        >
        <el-table-column
          :formatter="dateFormatter"
          align="center"
          label="预计超时时间"
          prop="timeOut"
          width="130"
        />
        <el-table-column
          align="left"
          label="当前任务"
          prop="name"
          width="130"
          :show-overflow-tooltip="true"
        />
        <el-table-column
          :formatter="dateFormatter"
          align="center"
          label="任务时间"
          prop="createTime"
          :show-overflow-tooltip="true"
          width="130"
        />

        <el-table-column align="center" label="操作" fixed="right" width="80">
          <template #default="scope">
            <el-button link type="primary" @click="handleAudit(scope.row)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->

      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
        style="width: 100%"
      />
      <!-- <div class="no-data" v-if="todoList.length === 0">暂无待办事项</div> -->
    </div>
  </div>
</template>

<script lang="ts" setup>
import { dateFormatter, formatDate } from '@/utils/formatTime'
import * as TaskApi from '@/api/bpm/task'
import { BellFilled } from '@element-plus/icons-vue'

defineOptions({ name: 'TodoTask' })
const loading = ref(true) // 列表的加载中
const list = ref([]) // 列表的数据
const total = ref(0) // 列表的总页数
const { push } = useRouter() // 路由

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10
})

/** 查询任务列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await TaskApi.getTaskTodoPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 处理审批按钮 */
const handleAudit = (row: any) => {
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstance.id,
      taskId: row.id,
      key: row.processInstance.processDefinitionKey
    }
  })
}
// 判断任务是否已超时：timeOut 是预计超时时间（毫秒时间戳）。当前时间已到达 timeOut 则视为「已超时」；否则视为「即将超时」
const isTaskTimeout = (timeOut: number): boolean => {
  if (!timeOut) return false
  return Date.now() >= timeOut
}

/** 初始化 **/
onMounted(async () => {
  await getList()
})

// 暴露刷新方法供父组件调用
defineExpose({
  refresh: getList
})
</script>

<style scoped lang="scss">
.todo-chart {
  display: flex;
  width: 100%;
  height: 100%;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
  margin-bottom: 10px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
}

.chart-content {
  width: 100%;
  overflow: hidden;
  flex: 1;
}

.chart-content:deep(.el-table) {
  width: 100%;
  height: 100%;
}

.chart-content:deep(.el-table__body-wrapper) {
  height: calc(100% - 48px);
}

.icon-tag-container {
  display: inline-flex;
  align-items: flex-end;
  gap: 4px;
  // margin-right: 8px;
}

.bellFilled {
  /* 确保图标大小不会影响对齐 */
  height: auto;
  font-size: 15px;
}

.bellFilled_red {
  color: rgb(238 68 68);
  text-shadow: 0 0 10px rgb(245 108 108 / 50%);
}

.bellFilled_yellow {
  color: rgb(255 203 0);
  text-shadow: 0 0 10px rgb(255 203 0 / 50%);
}
</style>

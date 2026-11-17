<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100"
    >
      <el-form-item label="记录日期" prop="recordDate">
        <el-date-picker
          v-model="queryParams.recordDate"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item label="问题编号" prop="questionNumber">
        <el-input
          v-model="queryParams.questionNumber"
          placeholder="请输入问题编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" placeholder="请选择优先级" class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CUSTOMER_RESPONSE_LEVEL)"
            :key="dict.value"
            :label="dict.label"
            :value="Number(dict.value)"
          />
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="客户问题影响" prop="customerProblemImpact">
        <el-input
          v-model="queryParams.customerProblemImpact"
          placeholder="请输入客户问题影响"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item> -->
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" @click="launchProcess"> 发起流程 </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['customerResponse:customer-service-issues:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <!--   <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['customerResponse:customer-service-issues:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button> -->
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="processInstanceId"
      v-loading="loading"
      :data="list"
      :stripe="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="200" />
      <el-table-column label="所属部门" align="center" prop="department" />
      <el-table-column label="联系人" align="center" prop="contact" />
      <el-table-column label="联系电话" align="center" prop="contactNumber" />
      <el-table-column label="联系人邮箱" align="center" prop="contactEmail" min-width="100" />
      <el-table-column
        label="记录日期"
        align="center"
        prop="recordDate"
        :formatter="dateFormatter2"
        min-width="120"
      />
      <el-table-column label="问题编号" align="center" prop="questionNumber" />
      <el-table-column
        label="客户要求时间"
        align="center"
        prop="customerRequestsTime"
        :formatter="dateFormatter"
        min-width="150"
      />
      <el-table-column label="优先级" align="center" prop="priority">
        <template #default="scope">
          {{ getDictLabel(DICT_TYPE.CUSTOMER_RESPONSE_LEVEL, scope.row.priority) }}
        </template>
      </el-table-column>
      <!--   <el-table-column
        label="客户问题描述"
        align="center"
        prop="customerProblemDescription"
        min-width="150"
      /> -->
      <el-table-column label="客户期望" align="center" prop="customerExpectations" />
      <!--  <el-table-column
        label="客户问题适用性"
        align="center"
        prop="applicableToCustomerIssues"
        min-width="150"
      />
      <el-table-column
        label="客户问题行业标准"
        align="center"
        prop="customerIssuesIndustryStandards"
        min-width="150"
      />
      <el-table-column
        label="客户问题影响"
        align="center"
        prop="customerProblemImpact"
        min-width="150"
      /> -->
      <el-table-column
        label="客户问题类型"
        align="center"
        prop="customerProblemTypes"
        min-width="150"
      >
        <template #default="scope">
          {{ getDictLabel(DICT_TYPE.CUSTOMER_PROBLEM_TYPES, scope.row.customerProblemTypes) }}
        </template></el-table-column
      >
      <el-table-column
        label="回复时间"
        align="center"
        prop="responseTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="回复人" align="center" prop="nikeName" />
      <el-table-column label="回复内容" align="center" prop="replyContent" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        min-width="100"
      />
      <!--    <el-table-column
        label="修改时间"
        align="center"
        prop="updateTime"
        :formatter="dateFormatter"
        width="180px"
      /> -->
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button link type="primary" @click="toDetail(scope.row)"> 详情 </el-button>
          <!--  <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['customerResponse:customer-service-issues:delete']"
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
  <CustomerServiceIssuesForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import { isEmpty } from '@/utils/is'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'
import download from '@/utils/download'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import {
  CustomerServiceIssuesApi,
  CustomerServiceIssues
} from '@/api/customerResponse/customerserviceissues'
import CustomerServiceIssuesForm from './CustomerServiceIssuesForm.vue'
import * as DefinitionApi from '@/api/bpm/definition'

/** 客户服务问题 列表 */
defineOptions({ name: 'CustomerServiceIssues' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const router = useRouter()

const loading = ref(true) // 列表的加载中
const list = ref<CustomerServiceIssues[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  recordDate: [],
  questionNumber: undefined,
  priority: undefined,
  customerProblemDescription: undefined,
  customerProblemImpact: undefined,
  replyContent: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CustomerServiceIssuesApi.getCustomerServiceIssuesPage(queryParams)
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
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

//发起流程
const launchProcess = async () => {
  try {
    // 获取所有激活状态的流程定义
    const processList = await DefinitionApi.getProcessDefinitionList({
      suspensionState: 1
    })

    // 查找与客户服务问题相关的流程定义
    const customerResponseProcess = processList.find((process: any) => {
      return (
        process.formCustomCreatePath === '/bpm/customForm/CustomerServiceIssueForm' ||
        process.name === '客户响应流程'
      )
    })

    if (!customerResponseProcess) {
      message.error('未找到客户服务问题对应的流程定义')
      return
    }

    // 获取流程定义的 key
    const processKey = customerResponseProcess.key

    // 跳转到流程发起页面
    router.push({
      name: 'CustomerServiceIssueForm',
      query: {
        key: processKey
      }
    })
  } catch (error) {
    console.error('发起流程失败:', error)
    message.error('发起流程失败，请稍后重试')
  }
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: CustomerServiceIssues[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await CustomerServiceIssuesApi.exportCustomerServiceIssues(queryParams)
    download.excel(data, '客户服务问题.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
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

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

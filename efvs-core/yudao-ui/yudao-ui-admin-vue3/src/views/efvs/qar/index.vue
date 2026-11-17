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
      <el-form-item label="航司" prop="airline">
        <el-input
          v-model="queryParams.airline"
          placeholder="请输入航司"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="机型" prop="model">
        <el-input
          v-model="queryParams.model"
          placeholder="请输入机型"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="efvs构型" prop="efvsConfiguration">
        <el-input
          v-model="queryParams.efvsConfiguration"
          placeholder="请输入efvs构型"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="文件名称" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="timeList">
        <el-date-picker
          v-model="queryParams.timeList"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始"
          end-placeholder="结束"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['efvs:qar:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['efvs:qar:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['efvs:qar:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
        <el-button
          type="primary"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDataAnalysis(checkedIds)"
        >
          性能分析
        </el-button>
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

      <el-table-column label="航司" align="center" prop="airline" />
      <el-table-column label="机型" align="center" prop="model" />
      <el-table-column label="efvs构型" align="center" prop="efvsConfiguration" />
      <el-table-column label="文件名称" align="center" prop="name" />
      <el-table-column label="文件大小(kb)" align="center" prop="size" />
      <el-table-column label="文件来源" align="center" prop="fileSource" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="创建人名称" align="center" prop="createName" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" min-width="160px">
        <template #default="scope">
          <div class="flex items-center justify-center space-x-0 h-full">
            <el-button
              link
              type="primary"
              @click="openForm('update', scope.row.id)"
              v-hasPermi="['efvs:qar:update']"
            >
              编辑
            </el-button>
            <!--   <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['efvs:qar:delete']"
          >
            删除
          </el-button> -->
            <!-- @click="openForm('update', scope.row.id)"   -->
            <el-link
              type="primary"
              download
              :href="getFileUrl(scope.row.fileUrl)"
              :underline="false"
              target="_blank"
              class="file-name-link"
              >下载
            </el-link>
            <!-- @click="handleDelete(scope.row.id)" -->
            <el-button
              link
              type="primary"
              @click="handleDataAnalysis([scope.row.id])"
              v-hasPermi="['efvs:qar:delete']"
            >
              性能分析
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-hasPermi="['efvs:qar:delete']"
            >
              删除
            </el-button></div
          >
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
  <QarForm ref="formRef" @success="getList" />
  <DataAnalysis ref="dataAnalysisRef" />
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { QarApi, Qar } from '@/api/efvs/qar'
import QarForm from './QarForm.vue'
import DataAnalysis from './DataAnalysis.vue'

/** qar数据管理 列表 */
defineOptions({ name: 'Qar' })

// 重新拼接文件地址：IP 和端口取自地址栏的 window.location.origin，其余地址保持不变
const getFileUrl = (url: string) => {
  try {
    const parsed = new URL(url)
    return `${window.location.origin}${parsed.pathname}${parsed.search}${parsed.hash}`
  } catch {
    return url
  }
}

const router = useRouter()
const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<Qar[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  airline: undefined,
  model: undefined,
  efvsConfiguration: undefined,
  fileName: undefined,
  flag: 'Performance',
  timeList: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const dataAnalysisRef = ref()
/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await QarApi.getQarPage(queryParams)
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

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await QarApi.deleteQar(id)
    message.success(t('common.delSuccess'))

    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除qar数据管理 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await QarApi.deleteQarList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Qar[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 数据分析：携带 id 跳转数据分析页面 */
const handleDataAnalysis = (ids: number[]) => {
  dataAnalysisRef.value?.open(ids)
}

/** 导出按钮操作：勾选了就只导出勾中的，没勾选导出当前条件下的全部 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const params: any = { ...queryParams }
    if (!isEmpty(checkedIds.value)) {
      params.ids = checkedIds.value.join(',')
    }
    const data = await QarApi.exportQar(params)
    download.excel(data, 'qar数据管理.xlsx')
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

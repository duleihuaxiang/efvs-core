<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="180"
    >
      <el-form-item label="部件名称" prop="partName">
        <el-input
          v-model="queryParams.partName"
          placeholder="请输入部件名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="件号" prop="partNumber">
        <el-input
          v-model="queryParams.partNumber"
          placeholder="请输入件号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="拆卸日期" prop="removalDate">
        <el-date-picker
          v-model="queryParams.removalDate"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" @click="launchProcess"> 发起流程 </el-button>
        <!--  <el-button
          type="success"
          plain
          @click="handleExport('MTBF')"
          :loading="exportLoading"
          v-hasPermi="['reliability:data:export']"
          :disabled="checkedRows.length !== 1"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出MTBF
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport('MTBUR')"
          :loading="exportLoading"
          v-hasPermi="['reliability:data:export']"
          :disabled="checkedRows.length !== 1"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出MTBUR
        </el-button> -->
        <el-button
          type="primary"
          plain
          @click="openMTBFDialog()"
          :loading="exportLoading"
          v-hasPermi="['reliability:data:export']"
          :disabled="checkedRows.length !== 1"
        >
          <Icon icon="ep:view" class="mr-5px" /> 查看MTBF
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openMTBURDialog()"
          :loading="exportLoading"
          v-hasPermi="['reliability:data:export']"
          :disabled="checkedRows.length !== 1"
        >
          <Icon icon="ep:view" class="mr-5px" /> 查看MTBUR
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="partNumber"
      v-loading="loading"
      :data="list"
      :stripe="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />

      <el-table-column label="部件名称" align="center" prop="partName" min-width="150" />
      <el-table-column label="件号" align="center" prop="partNumber" min-width="150" />

      <el-table-column label="故障次数" align="center" prop="issueCount" />

      <el-table-column
        label="最新报告日期"
        align="center"
        prop="reportStartDate"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="最新故障描述"
        align="center"
        prop="failureDescription"
        min-width="200"
      />
      <el-table-column
        label="最新创建日期"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="拆卸日期" align="center" prop="removalDate" min-width="200" />
      <el-table-column label="操作" align="center" min-width="200">
        <template #default="scope">
          <el-button link type="primary" @click="toDetail(scope.row)"> 查看明细 </el-button>

          <el-button
            link
            type="primary"
            @click="handleExport('MTBF', scope.row)"
            v-hasPermi="['reliability:data:export']"
          >
            导出MTBF和MTBUR
          </el-button>
          <!--    <el-button
            link
            type="primary"
            @click="handleExport('MTBUR', scope.row)"
            v-hasPermi="['reliability:data:export']"
          >
            导出MTBUR
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

  <!-- MTBF Dialog -->
  <Dialog
    v-model="mtbfDialogVisible"
    title="平均故障间隔时间（自动计算）"
    :before-close="handleMTBFDialogClose"
    :auto-fullscreen="true"
    header-class="dialog-title-blue"
  >
    <div
      class="text-center mt-6"
      style="padding: 10px 0; border: 1px solid #0e0e0f; border-bottom: none"
    >
      <h2 style="font-weight: normal; color: #0e0e0f">平均故障间隔时间（MTBF）</h2>
    </div>

    <el-table
      :data="mtbfData"
      border
      style="width: 100%"
      :show-header="true"
      class="custom-table-border"
    >
      <el-table-column prop="partNo" label="件号" />
      <el-table-column prop="equipmentName" label="设备名称" />
      <el-table-column prop="totalFlightTime" label="总飞行时间" />
      <el-table-column prop="totalInstallCount" label="总装机数" />
      <el-table-column prop="failureCount" label="故障次数（次）" />
      <el-table-column prop="mtbfActual" label="MTBF（实际值）" />
      <el-table-column prop="mtbfTarget" label="MTBF（指标）" />
    </el-table>

    <template #footer>
      <el-button type="primary" @click="exportMTBF" :loading="exportLoading">
        <Icon icon="ep:download" class="mr-5px" /> 导出MTBF
      </el-button>
      <el-button @click="mtbfDialogVisible = false">关闭</el-button>
    </template>
  </Dialog>

  <!-- MTBUR Dialog -->
  <Dialog
    v-model="mtburDialogVisible"
    title="平均非计划拆换时间（自动计算）"
    :before-close="handleMTBURDialogClose"
    :auto-fullscreen="true"
  >
    <div
      class="text-center mt-6"
      style="padding: 10px 0; border: 1px solid #0e0e0f; border-bottom: none"
    >
      <h2 style="font-weight: normal; color: #0e0e0f">平均非计划拆换时间（MTBUR）</h2>
    </div>

    <el-table
      :data="mtburData"
      border
      style="width: 100%"
      :show-header="true"
      class="custom-table-border"
    >
      <el-table-column prop="partNo" label="件号" />
      <el-table-column prop="equipmentName" label="设备名称" />
      <el-table-column prop="totalFlightTime" label="总飞行时间" />
      <el-table-column prop="totalInstallCount" label="总装机数" />
      <el-table-column prop="unplannedReplaceCount" label="非计划拆换次数（次）" />
      <el-table-column prop="mtburActual" label="MTBUR（实际值）" />
      <el-table-column prop="mtburTarget" label="MTBUR（指标）" />
    </el-table>

    <template #footer>
      <el-button type="primary" @click="exportMTBUR" :loading="exportLoading">
        <Icon icon="ep:download" class="mr-5px" /> 导出MTBUR
      </el-button>
      <el-button @click="mtburDialogVisible = false">关闭</el-button>
    </template>
  </Dialog>

  <!-- 明细 -->
  <ReliabilityDataDetail ref="ReliabilityDataDetailRef" />
</template>

<script setup lang="ts">
import * as DefinitionApi from '@/api/bpm/definition'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { DataApi, Data } from '@/api/reliability/reliabilitydata'
import ReliabilityDataDetail from './detail.vue'

/** 可靠性数据收集 列表 */
defineOptions({ name: 'ReliabilityData' })

const message = useMessage() // 消息弹窗
const router = useRouter()

const loading = ref(true) // 列表的加载中
const list = ref<Data[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  reliabilityDataFormNo: undefined,
  customerName: undefined,
  partName: undefined,
  partNumber: undefined,
  serialNumber: undefined,
  model: undefined,
  series: undefined,
  machineNumber: undefined,
  reportStartDate: [],
  reportEndDate: [],
  removalDate: [],
  isProductIssue: undefined,
  removalType: undefined,
  removedPartNumber: undefined,
  removedPartSerialNumber: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
// MTBF/MTBUR Dialog 相关状态
const mtbfDialogVisible = ref(false)
const mtburDialogVisible = ref(false)
const ReliabilityDataDetailRef = ref()

// MTBF/MTBUR 数据
const mtbfData = ref<any[]>([])
const mtburData = ref<any[]>([])

// 当前选中的行数据
const selectedRow = ref<Data | null>(null)
/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DataApi.getDataPage(queryParams)
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
        process.formCustomCreatePath === '/bpm/customForm/ReliabilityDataCollection' ||
        process.name === '可靠性数据简化流程'
      )
    })

    if (!customerResponseProcess) {
      message.error('未找到可靠性数据简化流程对应的流程定义')
      return
    }

    // 获取流程定义的 key
    const processKey = customerResponseProcess.key

    // 跳转到流程发起页面
    router.push({
      name: 'ReliabilityDataCollection',
      query: {
        key: processKey
      }
    })
  } catch (error) {
    console.error('发起流程失败:', error)
    message.error('发起流程失败，请稍后重试')
  }
}

const checkedRows = ref<Data[]>([])
const handleRowCheckboxChange = (records: Data[]) => {
  checkedRows.value = records
}

/** 导出按钮操作 */
const handleExport = async (_type: 'MTBF' | 'MTBUR', row?: Data) => {
  try {
    let selectedRow: Data | undefined

    if (row) {
      selectedRow = row
    } else {
      if (checkedRows.value.length === 0) {
        message.warning('请先选择一条数据进行导出')
        return
      }

      if (checkedRows.value.length !== 1) {
        message.warning('仅支持单条数据导出，请选择一条数据')
        return
      }

      selectedRow = checkedRows.value[0]
    }
    if (!selectedRow?.partNumber) {
      message.error('未找到件号')
      return
    }

    // 导出的二次确认
    await message.exportConfirm()

    // 构建导出参数
    const exportParams = {
      // type: type.toLowerCase(), // 'mtbf' or 'mtbur'
      partName: selectedRow.partName,
      partNumber: selectedRow.partNumber,
      serialNumber: selectedRow.serialNumber || null
    }

    // 设置导出加载状态
    exportLoading.value = true

    // 调用API导出Word格式文件
    const data = await DataApi.exportDataMtbfAndMtburCombined(exportParams)

    // 生成文件名
    const fileName = `${selectedRow.partName}.docx`

    // 下载文件
    download.word(data, fileName)

    // 提示成功
    message.success(`导出成功`)
  } catch (error: any) {
    console.error(error)
  } finally {
    exportLoading.value = false
  }
}

const exportData = async (type: 'MTBF' | 'MTBUR') => {
  try {
    // 确保有选中的数据
    if (!selectedRow.value) {
      message.warning('没有可导出的数据')
      return
    }

    // 构建导出参数
    const exportParams = {
      type: type.toLowerCase(),
      partName: selectedRow.value.partName,
      partNumber: selectedRow.value.partNumber,
      serialNumber: selectedRow.value.serialNumber || null
    }

    // 设置导出加载状态
    exportLoading.value = true

    // 调用API导出
    const response = await DataApi.exportDataMtbfAndMtbur(exportParams)

    // 生成文件名
    const fileName = `${selectedRow.value.partName}_${type}.docx`

    // 下载文件
    download.word(response, fileName)

    // 提示成功
    message.success(`导出${type}成功`)
  } catch (error) {
    console.error(`导出${type}失败:`, error)
    message.error(`导出${type}失败，请稍后重试`)
  } finally {
    exportLoading.value = false
  }
}

/** 导出MTBF */
const exportMTBF = () => {
  exportData('MTBF')
}

/** 导出MTBUR */
const exportMTBUR = () => {
  exportData('MTBUR')
}
/** 关闭MTBF Dialog */
const handleMTBFDialogClose = () => {
  mtbfDialogVisible.value = false
}

/** 关闭MTBUR Dialog */
const handleMTBURDialogClose = () => {
  mtburDialogVisible.value = false
}

/** 打开MTBF Dialog */
const openMTBFDialog = async () => {
  try {
    if (checkedRows.value.length === 0) {
      message.warning('请先选择一条数据进行查看')
      return
    }

    if (checkedRows.value.length !== 1) {
      message.warning('仅支持单条数据查看，请选择一条数据')
      return
    }

    selectedRow.value = checkedRows.value[0]
    if (!selectedRow.value?.partNumber) {
      message.error('未找到件号')
      return
    }

    // 获取MTBF数据
    await fetchMTBFData(selectedRow.value)

    // 显示Dialog
    mtbfDialogVisible.value = true
  } catch (error) {
    console.error('打开MTBF Dialog失败:', error)
    message.error('打开MTBF信息失败，请稍后重试')
  }
}

/** 打开MTBUR Dialog */
const openMTBURDialog = async (row?: Data) => {
  try {
    if (row) {
      selectedRow.value = row
    } else {
      if (checkedRows.value.length === 0) {
        message.warning('请先选择一条数据进行查看')
        return
      }

      if (checkedRows.value.length !== 1) {
        message.warning('仅支持单条数据查看，请选择一条数据')
        return
      }

      selectedRow.value = checkedRows.value[0]
    }

    if (!selectedRow.value?.partNumber) {
      message.error('未找到件号')
      return
    }

    // 获取MTBUR数据
    await fetchMTBURData(selectedRow.value)

    // 显示Dialog
    mtburDialogVisible.value = true
  } catch (error) {
    console.error('打开MTBUR Dialog失败:', error)
    message.error('打开MTBUR信息失败，请稍后重试')
  }
}
/** 获取MTBF数据 */
const fetchMTBFData = async (row: Data) => {
  try {
    // 构建请求参数
    const params = {
      type: 'mtbf',
      partName: row.partName,
      partNumber: row.partNumber,
      serialNumber: row.serialNumber || null
    }

    // 调用API获取MTBF数据
    const response = await DataApi.queryMtbfAndMtbur(params)
    mtbfData.value = response || []
  } catch (error) {
    console.error('获取MTBF数据失败:', error)
    message.error('获取MTBF数据失败，请稍后重试')
  }
}

/** 获取MTBUR数据 */
const fetchMTBURData = async (row: Data) => {
  try {
    // 构建请求参数
    const params = {
      type: 'mtbur',
      partName: row.partName,
      partNumber: row.partNumber,
      serialNumber: row.serialNumber || null
    }

    // 调用API获取MTBUR数据
    const response = await DataApi.queryMtbfAndMtbur(params)

    mtburData.value = response || []
  } catch (error) {
    console.error('获取MTBUR数据失败:', error)
    message.error('获取MTBUR数据失败，请稍后重试')
  }
}
// 点击详情
const toDetail = (query: any) => {
  // router.push({ path: '/reliabilityData/data/detail', query })
  ReliabilityDataDetailRef.value?.open?.(query)
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
<style scoped>
.custom-table-border {
  --el-table-border-color: #0e0e0f;

  .el-table__header th > div {
    color: #0e0e0f;
  }
}
</style>

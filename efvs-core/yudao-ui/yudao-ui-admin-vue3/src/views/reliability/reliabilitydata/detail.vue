<template>
  <Dialog
    :title="dialogTitle"
    v-model="dialogVisible"
    width="900"
    :auto-fullscreen="true"
    @closed="handleClosed"
  >
    <!-- 返回按钮 -->
    <div class="back-button">
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>

    <!-- 搜索工作栏 -->
    <div class="search-form">
      <el-form :model="queryParams" ref="queryFormRef" label-width="120">
        <el-row :gutter="20">
          <!-- 第一行 -->
          <el-col :span="8">
            <el-form-item label="部件名称:" prop="partName">
              <el-input v-model="queryParams.partName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="件号:">
              <el-input v-model="queryParams.partNumber" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="序列号:" prop="serialNumber">
              <el-input v-model="queryParams.serialNumber" placeholder="请输入序列号" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="版本:" prop="version">
              <el-input v-model="queryParams.version" placeholder="请输入版本" />
            </el-form-item>
          </el-col>
          <!-- 第二行 -->
          <el-col :span="8">
            <el-form-item label="系列:" prop="series">
              <el-input v-model="queryParams.series" placeholder="请输入系列" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="机型:" prop="model">
              <el-input v-model="queryParams.model" placeholder="请输入机型" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="机号:" prop="machineNumber">
              <el-input v-model="queryParams.machineNumber" placeholder="请输入机号" clearable />
            </el-form-item>
          </el-col>

          <!-- 第三行 -->
          <el-col :span="8">
            <el-form-item label="客户名称:" prop="customerName">
              <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否产品问题:" prop="isProductIssue">
              <el-select v-model="queryParams.isProductIssue" placeholder="请选择">
                <el-option label="是" :value="1" />
                <el-option label="否" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="拆卸日期:" prop="removalDate">
              <el-date-picker
                v-model="queryParams.removalDate"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="daterange"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
                class="!w-250"
              />
            </el-form-item>
          </el-col>

          <!-- 第四行 -->
          <el-col :span="8">
            <el-form-item label="拆卸部件序列号:" prop="removedPartSerialNumber">
              <el-input
                v-model="queryParams.removedPartSerialNumber"
                placeholder="请输入拆卸部件序列号"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="拆卸类型:" prop="removalType">
              <el-select v-model="queryParams.removalType" clearable>
                <el-option label="U:非计划拆换" value="U" />
                <el-option label="S:计划拆换" value="S" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="拆卸部件号:" prop="removedPartNumber">
              <el-input
                v-model="queryParams.removedPartNumber"
                placeholder="请输入拆卸部件号"
                clearable
              />
            </el-form-item>
          </el-col>

          <!-- 查询按钮 -->
          <el-col :span="8">
            <el-form-item style="text-align: right">
              <el-button type="primary" @click="handleQuery">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="success" plain @click="exportData"
        ><Icon icon="ep:download" class="mr-5px" /> 导出</el-button
      >
    </div>

    <!-- 数据表格 -->
    <div class="data-table">
      <el-table :data="list" border stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" width="55" />
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="" @click="toHistory(row)">查看审批历史</el-button></template
          >
        </el-table-column>
        <el-table-column prop="partName" label="部件名称" width="150" />
        <el-table-column prop="partNumber" label="件号" width="150" />
        <el-table-column prop="serialNumber" label="序列号" width="180" />
        <el-table-column prop="model" label="机型" width="150" />
        <el-table-column prop="series" label="系列" width="150" />
        <el-table-column prop="machineNumber" label="机号" width="150" />
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="reportStartDate" label="报告开始日期" width="180" />
        <el-table-column prop="reportEndDate" label="报告结束日期" width="180" />
        <el-table-column prop="removalDate" label="拆卸日期" width="150" />
        <el-table-column prop="failureReportNumber" label="故障报告编码" width="180" />
        <el-table-column prop="removalType" label="拆卸类型" width="180">
          <template #default="{ row }"> {{ formatRemovalType(row.removalType) }}</template>
        </el-table-column>
        <el-table-column prop="removalCause" label="拆换原因" width="200" />
        <el-table-column prop="failureDescription" label="故障描述" width="150" />
        <el-table-column prop="removedPartNumber" label="拆卸部件号" width="180" />
        <el-table-column prop="removedPartSerialNumber" label="拆卸部件序列号" width="180" />
      </el-table>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { DataApi } from '@/api/reliability/reliabilitydata'
import download from '@/utils/download'

// 定义数据类型
interface DataItem {
  partName: string
  partNumber: string
  serialNumber: string
  model: string
  series: string
  machineNumber: string
  customerName: string
  reportStartDate: string
  reportEndDate: string
  removalDate: string
  failureReportCode: string
  removalType: string
  removalReason: string
  flightNumber: string
}
// 路由器
const router = useRouter()
const route = useRoute()
// 定义查询参数
const queryParams = reactive({
  partName: '',
  partNumber: '',
  version: '',
  serialNumber: '',
  series: '',
  model: '',
  machineNumber: '',
  customerName: '',
  removalDate: [],
  removedPartSerialNumber: '',
  removalType: '',
  removedPartNumber: '',
  isProductIssue: null
})
const queryFormRef = ref() // 搜索的表单
// 分页信息
const total = ref(0)
// 表格数据
const list = ref<DataItem[]>([])
// 加载状态
const loading = ref(true)
const dialogTitle = ref('') // 弹窗的标题
const dialogVisible = ref(false) // 弹窗的是否展示
// const originalPartName = ref('')
const originalPartNumber = ref('')
/* const originalVersion = ref('')
const originnalSerialNumber = ref('') */
// 添加格式化函数
const formatRemovalType = (type: string) => {
  if (!type) return ''

  const typeMap: Record<string, string> = {
    U: 'U:非计划拆换',
    u: 'U:非计划拆换',
    S: 'S:计划拆换',
    s: 'S:计划拆换'
  }

  return typeMap[type] || type
}

// 打开弹窗
const open = (row: any) => {
  dialogTitle.value = '明细'
  dialogVisible.value = true

  if (row) {
    // originalPartName.value = String(row.partName)
    originalPartNumber.value = String(row.partNumber)
    /*  originalVersion.value = String(row.version)
    originnalSerialNumber.value = String(row.serialNumber) */
    // if (row.partName) queryParams.partName = String(row.partName)
    if (row.partNumber) queryParams.partNumber = String(row.partNumber)

    /*     if (row.version) queryParams.version = String(row.version)
    if (row.serialNumber) queryParams.serialNumber = String(row.serialNumber) */
    getList()
  }
}
// 返回按钮
const goBack = () => {
  // router.push('/reliabilityData/data')
  dialogVisible.value = false
}

// 弹窗关闭后清空搜索条件（件号由 open 重新赋值，保留）
const handleClosed = () => {
  queryParams.partName = ''
  queryParams.version = ''
  queryParams.serialNumber = ''
  queryParams.series = ''
  queryParams.model = ''
  queryParams.machineNumber = ''
  queryParams.customerName = ''
  queryParams.removalDate = []
  queryParams.removedPartSerialNumber = ''
  queryParams.removalType = ''
  queryParams.removedPartNumber = ''
  queryParams.isProductIssue = null
}

// 查询按钮
const handleQuery = async () => {
  await getList()
}

// 重置按钮
const resetQuery = () => {
  queryFormRef.value.resetFields()
  // queryParams.partName = originalPartName.value
  queryParams.partNumber = originalPartNumber.value
  // queryParams.isProductIssue = 1
  /*   queryParams.version = originalVersion.value
  queryParams.serialNumber = originnalSerialNumber.value */
  handleQuery()
}

// 获取数据列表
const getList = async () => {
  loading.value = true
  try {
    const res = await DataApi.getReliabilityDataDetailPage(queryParams)
    // 后端已改为返回全部数据的数组（不再分页）
    list.value = Array.isArray(res) ? res : res?.list || []
    total.value = list.value.length
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化
onMounted(async () => {
  // 从路由参数获取partName和partNumber
  /*   const { partName, partNumber } = route.query
  if (partName) queryParams.partName = String(partName)
  if (partNumber) queryParams.partNumber = String(partNumber) */
  // 自动执行查询
  // await getList()
})

// 查看审批历史
const toHistory = (row: any) => {
  router.push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: row.processInstanceId,
      taskId: row.taskId,
      key: row.processKey
    }
  })
}
// 导出按钮
const exportData = async () => {
  try {
    // 调用导出接口，参数与查询参数相同
    const data = await DataApi.exportReliabilityDataDetail(queryParams)
    // 生成文件名
    // const fileName = `${queryParams.partName}.xlsx`

    // 下载文件
    download.excel(data, '明细.xlsx') // 注意：虽然方法名叫excel，但实际可以下载各种格式的文件

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 暴露方法
defineExpose({
  open
})
</script>

<style scoped>
.reliability-detail-page {
  padding: 20px;
  background-color: #f5f7fa;
}

.back-button {
  margin-bottom: 20px;
}

.search-form {
  padding: 20px;
  margin-bottom: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
}

.action-buttons {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
  justify-content: flex-start;
}

.data-table {
  padding: 20px;
  margin-bottom: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
}

.tips-box {
  padding: 15px;
  background-color: #fff8b8;
  border: 1px solid #ffe56d;
  border-radius: 8px;
}

.tip-item {
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}
</style>

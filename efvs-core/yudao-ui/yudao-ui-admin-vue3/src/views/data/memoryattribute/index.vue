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
      <el-form-item label="适用机型编号" prop="applicableModelNumber">
        <el-input
          v-model="queryParams.applicableModelNumber"
          placeholder="请输入适用机型编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="装机飞机编号" prop="installationAircraftNumber">
        <el-input
          v-model="queryParams.installationAircraftNumber"
          placeholder="请输入装机飞机编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="装机位置名称" prop="installationLocationName">
        <el-input
          v-model="queryParams.installationLocationName"
          placeholder="请输入装机位置名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="适配版本要求" prop="adaptationVersion">
        <el-input
          v-model="queryParams.adaptationVersion"
          placeholder="请输入适配版本要求"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="装机时间" prop="installationTime">
        <el-date-picker
          v-model="queryParams.installationTime"
          value-format="YYYY-MM-DD"
          type="date"
          placeholder="选择装机时间"
          clearable
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="适航适配状态0是1否（是否完成适航验证）" prop="adaptationStatus">
        <el-select
          v-model="queryParams.adaptationStatus"
          placeholder="请选择适航适配状态0是1否（是否完成适航验证）"
          clearable
          class="!w-240px"
        >
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['data:memory-attribute:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:memory-attribute:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:memory-attribute:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
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
      :show-overflow-tooltip="true"
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="机型属性编号" align="center" prop="id" />
      <el-table-column label="适用机型编号" align="center" prop="applicableModelNumber" />
      <el-table-column label="装机飞机编号" align="center" prop="installationAircraftNumber" />
      <el-table-column label="装机位置名称" align="center" prop="installationLocationName" />
      <el-table-column label="适配版本要求" align="center" prop="adaptationVersion" />
      <el-table-column
        label="装机时间"
        align="center"
        prop="installationTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="适航适配状态0是1否（是否完成适航验证）"
        align="center"
        prop="adaptationStatus"
      />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['data:memory-attribute:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:memory-attribute:delete']"
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
  <MemoryAttributeForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { MemoryAttributeApi, MemoryAttribute } from '@/api/data/memoryattribute'
import MemoryAttributeForm from './MemoryAttributeForm.vue'

/** 机型属性 列表 */
defineOptions({ name: 'MemoryAttribute' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<MemoryAttribute[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  applicableModelNumber: undefined,
  installationAircraftNumber: undefined,
  installationLocationName: undefined,
  adaptationVersion: undefined,
  installationTime: undefined,
  installationTime: [],
  adaptationStatus: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await MemoryAttributeApi.getMemoryAttributePage(queryParams)
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
    await MemoryAttributeApi.deleteMemoryAttribute(id)
    message.success(t('common.delSuccess'))

    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除机型属性 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await MemoryAttributeApi.deleteMemoryAttributeList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: MemoryAttribute[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await MemoryAttributeApi.exportMemoryAttribute(queryParams)
    download.excel(data, '机型属性.xls')
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

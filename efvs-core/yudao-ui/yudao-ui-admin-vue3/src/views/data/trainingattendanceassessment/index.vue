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
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名"
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
      <el-form-item label="培训地点" prop="trainingLocation">
        <el-input
          v-model="queryParams.trainingLocation"
          placeholder="请输入培训地点"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="签到时间" prop="checkInTime">
        <el-date-picker
          v-model="queryParams.checkInTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item label="完成形式" prop="completedForm">
        <el-select v-model="queryParams.completedForm" clearable class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.TRAINING_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="Number(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="成绩" prop="score">
        <el-input
          v-model="queryParams.score"
          placeholder="请输入成绩"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="教员" prop="instructor">
        <el-input
          v-model="queryParams.instructor"
          placeholder="请输入教员"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
        <!--        <el-select
          v-model="queryParams.instructor"
          clearable
          style="width: 100%"
          placeholder="请选择教员"
          class="!w-240px"
        >
          <el-option
            v-for="item in projectUserList"
            :key="item.id"
            :label="item.nickname"
            :value="item.id"
          />
        </el-select> -->
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <!-- <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['data:training-attendance-assessment:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button> -->
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['data:training-attendance-assessment:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <!--   <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['data:training-attendance-assessment:delete']"
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
      <!-- <el-table-column label="ID" align="center" prop="id" /> -->
      <el-table-column label="姓名" align="center" prop="name" />
      <el-table-column label="培训主题" align="center" prop="trainingTheme" />
      <el-table-column label="培训地点" align="center" prop="trainingLocation" />
      <el-table-column label="培训时间" align="center" prop="" width="180px"
        ><template #default="{ row }"
          >{{ row.trainingStartTime }}~{{ row.trainingEndTime }}</template
        ></el-table-column
      >
      <el-table-column
        label="签到时间"
        align="center"
        prop="checkInTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="是否完成学习" align="center" prop="isCompleted" min-width="100">
        <template #default="{ row }">
          <span v-if="row.isCompleted !== null && row.isCompleted !== undefined">{{
            row.isCompleted === 0 ? '是' : '否'
          }}</span>
        </template></el-table-column
      >
      <el-table-column label="完成形式" align="center" prop="completedForm">
        <template #default="{ row }">
          <span>
            {{ getDictLabel(DICT_TYPE.TRAINING_TYPE, row.completedForm) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="成绩" align="center" prop="score" />
      <el-table-column label="获得证书" align="center" prop="receiveCertificate" min-width="200">
        <template #default="{ row }">
          <div class="flex flex-col">
            <el-link
              :download="file.fileName"
              v-for="file in row.fileUploadResults"
              :key="file.id"
              :href="file.fileUrl"
              :underline="false"
              class="file-name-link"
              @click.prevent="handleFileDownload(file)"
              >{{ file.fileName }}<Icon icon="ep:download" class="ml-5px" />
            </el-link>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="教员" align="center" prop="instructor" />
      <el-table-column
        label="完成日期"
        align="center"
        prop="completionDate"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="{ row }">
          <el-button link type="primary" @click="toDetail(row)"> 详情 </el-button>
          <!--   <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['data:training-attendance-assessment:delete']"
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
  <TrainingAttendanceAssessmentForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import { isEmpty } from '@/utils/is'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import {
  TrainingAttendanceAssessmentApi,
  TrainingAttendanceAssessment
} from '@/api/data/trainingattendanceassessment'
import * as UserApi from '@/api/system/user'
import { DICT_TYPE, getIntDictOptions, getDictLabel } from '@/utils/dict'
import TrainingAttendanceAssessmentForm from './TrainingAttendanceAssessmentForm.vue'

/** 培训签到考核证书 列表 */
defineOptions({ name: 'TrainingAttendanceAssessment' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const router = useRouter()

const loading = ref(true) // 列表的加载中
const list = ref<TrainingAttendanceAssessment[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  courseName: undefined,
  trainingTheme: undefined,
  trainingLocation: undefined,
  checkInTime: [],
  completedForm: undefined,
  score: undefined,
  receiveCertificate: undefined,
  instructor: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const projectUserList = ref<any[]>([])
/** 查询教员下拉列表 */
const getProjectUserList = async () => {
  const data = await UserApi.getUserListByDeptId()
  projectUserList.value = data
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data =
      await TrainingAttendanceAssessmentApi.getTrainingAttendanceAssessmentPage(queryParams)
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

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: TrainingAttendanceAssessment[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

// 处理文件下载
const handleFileDownload = async (file: any) => {
  try {
    console.log('开始下载文件:', file)

    // 检查文件类型
    const fileName = file.fileName || 'download'
    const fileExtension = fileName.split('.').pop()?.toLowerCase()

    // 对于图片文件，使用fetch方法获取blob并下载
    if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(fileExtension || '')) {
      console.log('处理图片文件下载')

      // 使用fetch获取文件
      const response = await fetch(file.fileUrl)
      const blob = await response.blob()

      // 创建Blob URL
      const url = URL.createObjectURL(blob)

      // 创建下载链接
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      link.style.display = 'none'
      document.body.appendChild(link)

      // 模拟点击
      link.click()

      // 清理
      setTimeout(() => {
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        console.log('图片下载完成')
      }, 100)
    } else {
      // 非图片文件使用常规下载方法
      console.log('处理非图片文件下载')
      const link = document.createElement('a')
      link.href = file.fileUrl
      link.download = fileName
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()

      setTimeout(() => {
        document.body.removeChild(link)
        console.log('文件下载完成')
      }, 100)
    }
  } catch (error) {
    console.error('下载失败:', error)
    message.error('文件下载失败')
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data =
      await TrainingAttendanceAssessmentApi.exportTrainingAttendanceAssessment(queryParams)
    download.excel(data, '培训签到考核证书.xlsx')
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
  getProjectUserList()
})
</script>
<style scoped>
.file-name-link {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

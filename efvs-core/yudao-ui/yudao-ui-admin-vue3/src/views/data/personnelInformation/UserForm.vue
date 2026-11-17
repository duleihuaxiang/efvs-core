<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="1000" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item label="人员姓名" prop="nickname">
            <el-input v-model="formData.nickname" placeholder="请输入人员姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="人员性别" prop="sex">
            <el-select v-model="formData.sex" placeholder="请选择">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.SYSTEM_USER_SEX)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="手机号码" prop="mobile">
            <el-input v-model="formData.mobile" maxlength="11" placeholder="请输入手机号码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" maxlength="50" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="人员登录名" prop="username">
            <el-input v-model="formData.username" placeholder="请输入人员登录名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item v-if="formData.id === undefined" label="人员密码" prop="password">
            <el-input
              v-model="formData.password"
              placeholder="请输入人员密码"
              show-password
              type="password"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row />
      <el-row>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input
              v-model="formData.remark"
              placeholder="请输入内容"
              type="textarea"
              :rows="3"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item class="-text-right">
        <el-button type="primary" @click="savePersonInformation">保存人员信息</el-button>
        <el-button @click="resetForm">清 空</el-button>
      </el-form-item>
      <h3 class="mb-3"
        >培训经历
        <span style="font-size: 12px; font-weight: normal; color: #f56c6c"
          >注：保存人员信息后,才可添加培训经历</span
        ></h3
      >
      <el-form
        class="-mb-15px"
        :model="trainData"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item>
          <el-button
            type="primary"
            plain
            @click="openForm('create', undefined, formData.userId)"
            v-hasPermi="['training:experience:create']"
            :disabled="formData.userId === undefined"
          >
            <Icon icon="ep:plus" class="mr-5px" /> 增加培训经历
          </el-button>
          <el-button
            type="danger"
            plain
            :disabled="isEmpty(checkedIds)"
            @click="handleDeleteBatch"
            v-hasPermi="['training:experience:delete']"
            v-show="formData.userId !== undefined"
          >
            <Icon icon="ep:delete" class="mr-5px" /> 批量删除培训经历
          </el-button>
        </el-form-item>
      </el-form>
      <!-- 列表 -->
      <ContentWrap v-show="formData.userId !== undefined">
        <el-table
          row-key="id"
          v-loading="loading"
          :data="list"
          :stripe="true"
          :show-overflow-tooltip="true"
          @selection-change="handleRowCheckboxChange"
        >
          <el-table-column type="selection" width="55" />

          <el-table-column label="培训主题" align="center" prop="trainingTheme" />
          <el-table-column
            label="培训时间"
            align="center"
            prop="trainingTime"
            :formatter="dateFormatter3"
            width="180px"
          />
          <el-table-column label="培训地点" align="center" prop="trainingLocation" />
          <el-table-column label="培训时长" align="center" prop="trainingDuration" />
          <el-table-column label="文件内容" align="center" prop="url" width="200px">
            <template #default="{ row }">
              <div class="flex flex-col">
                <el-link
                  :download="file.fileName"
                  v-for="file in row.fileList"
                  :key="file.id"
                  :href="file.fileUrl"
                  :underline="false"
                  class="file-name-link"
                  @click.prevent="handleFileDownload(file)"
                  >{{ file.fileName }}</el-link
                >
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" min-width="120px">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="openForm('update', scope.row.id)"
                v-hasPermi="['training:experience:update']"
              >
                编辑
              </el-button>
              <el-button
                link
                type="danger"
                @click="handleDelete(scope.row.id)"
                v-hasPermi="['training:experience:delete']"
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
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="dialogVisible = false"
        >确 定</el-button
      >
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
  <!-- 表单弹窗：添加/修改 -->
  <TrainForm ref="TrainFormRef" @success="getList(formData.userId)" />
</template>
<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import { dateFormatter3 } from '@/utils/formatTime'
import * as UserApi from '@/api/system/user'
import { isEmpty } from '@/utils/is'
import { FormRules } from 'element-plus'
import * as PermissionApi from '@/api/system/permission'
import { ExperienceApi, Experience } from '@/api/training/trainingexperience'
import TrainForm from './TrainForm.vue'

defineOptions({ name: 'SystemUserForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
// const userList = ref([]) // 人员列表数据

const formData = ref({
  nickname: '',
  deptId: 116,
  mobile: '',
  email: '',
  id: undefined,
  username: '',
  password: '',
  sex: undefined,
  remark: '',
  status: CommonStatusEnum.ENABLE,
  userId: undefined, //人员id
  experienceIds: []
})
const formRules = reactive<FormRules>({
  username: [{ required: true, message: '人员登录名不能为空', trigger: 'blur' }],
  sex: [{ required: true, message: '人员性别不能为空', trigger: 'blur' }],
  nickname: [{ required: true, message: '人员姓名不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '人员密码不能为空', trigger: 'blur' }],
  email: [
    { required: true, type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  mobile: [
    {
      required: true,
      pattern: /^(?:(?:\+|00)86)?1(?:3[\d]|4[5-79]|5[0-35-9]|6[5-7]|7[0-8]|8[\d]|9[189])\d{8}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur'
    }
  ]
})
const formRef = ref() // 表单 Ref
const trainData = ref([])
const loading = ref(true) // 列表的加载中
const list = ref<Experience[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined
})
/** 添加/修改操作 */
const TrainFormRef = ref()
const openForm = (type: string, id?: number, userId?: number) => {
  TrainFormRef.value.open(type, id, userId)
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: Experience[]) => {
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

/** 保存人员信息 */
const savePersonInformation = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 提交请求
  formLoading.value = true
  try {
    formData.value.experienceIds = TrainFormRef.value.experienceIds
    const data = formData.value as unknown as UserApi.UserVO
    if (formType.value === 'create' && !formData.value.userId) {
      formData.value.userId = await UserApi.createUser(data)
      try {
        await PermissionApi.assignUserRole({
          userId: formData.value.userId,
          roleIds: [169]
        })
      } catch (e) {
        throw e
      }
      getList(formData.value.userId)
      message.success('保存成功')
    } else {
      await UserApi.updateUser(data)
      message.success(t('common.updateSuccess'))
    }
    // dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}
/** 批量删除培训经历 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await ExperienceApi.deleteExperienceList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList(formData.value.userId)
  } catch {}
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await ExperienceApi.deleteExperience(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList(formData.value.userId)
  } catch {}
}

/** 查询列表 */
const getList = async (userId?: any) => {
  loading.value = true
  try {
    queryParams.userId = userId
    const data = await ExperienceApi.getExperiencePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  // userList.value = await UserApi.getSimpleUserList()

  dialogVisible.value = true
  dialogTitle.value = t('action.' + type) + '人员信息'
  formType.value = type

  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await UserApi.getUser(id)
      formData.value.userId = id
      getList(id)
    } finally {
      formLoading.value = false
    }
  } else {
    // 新增时预设部门ID
    formData.value.deptId = 116
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return

  // 提交请求
  formLoading.value = true
  try {
    formData.value.experienceIds = TrainFormRef.value.experienceIds
    const data = formData.value as unknown as UserApi.UserVO
    if (formType.value === 'create') {
      await UserApi.createUser(data)
      message.success(t('common.createSuccess'))
    } else {
      await UserApi.updateUser(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    nickname: '',
    deptId: 116,
    mobile: '',
    email: '',
    id: undefined,
    username: '',
    password: '',
    sex: undefined,
    // postIds: [],
    remark: '',
    status: CommonStatusEnum.ENABLE,
    userId: undefined, //人员id
    experienceIds: []
  }
  formRef.value?.resetFields()
}
</script>
<style lang="scss" scoped>
.el-table {
  .cell {
    text-align: left !important;
    word-break: break-all !important;
    white-space: normal !important;
  }
}

.file-name-link {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

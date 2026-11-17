<template>
  <Dialog v-model="dialogVisible" title="用户导入" width="900">
    <div class="import-container">
      <!-- 操作说明 -->
      <p class="instruction">
        1. 请先下载模板，按照格式填写数据。<br />
        2. 上传填写好的 Excel 文件（支持 .xlsx 格式，最大 200MB）。
      </p>

      <!-- 按钮区域 -->
      <div class="button-group">
        <el-button type="primary" plain @click="handleDownloadTemplate">
          <Icon icon="ep:download" class="mr-5px" /> 下载模板
        </el-button>
        <el-button @click="handleUploadTemplate"> 上传模板 </el-button>
        <input
          ref="templateFileInputRef"
          type="file"
          accept=".xlsx"
          style="display: none"
          @change="onFileSelected"
        />
        <!--   <el-upload
          ref="uploadRef"
          :action="importUrl"
          :auto-upload="false"
          :disabled="formLoading"
          :headers="uploadHeaders"
          :limit="1"
          :on-error="submitFormError"
          :on-exceed="handleExceed"
          :on-success="submitFormSuccess"
          :before-upload="beforeUpload"
          accept=".xlsx, .xls"
          :show-file-list="false"
        >
          <el-button type="primary" :loading="formLoading">
            <Icon icon="ep:upload" class="mr-5px" /> 上传模板
          </el-button>
        </el-upload> -->
      </div>

      <!-- 选项区域 -->
      <!--   <div class="option-group">
        <el-checkbox v-model="updateSupport"> 是否更新已经存在的用户数据 </el-checkbox>
      </div> -->

      <!-- 提示信息 -->
      <div class="tip-text">
        <Icon icon="ep:warning" class="mr-5px" />
        注意：导入过程可能需要几秒钟，请耐心等待，勿关闭页面。
      </div>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import * as UserApi from '@/api/system/user'
import { getAccessToken, getTenantId } from '@/utils/auth'
import download from '@/utils/download'
import { ElLoading } from 'element-plus'
import { Icon } from '@/components/Icon' // 假设你有 Icon 组件，如果没有请移除或替换为 ep:xxx

defineOptions({ name: 'SystemUserImportForm' })

const message = useMessage()

// --- 状态定义 ---
const dialogVisible = ref(false)
const formLoading = ref(false)
const uploadRef = ref()
const updateSupport = ref(false) // 默认不更新，根据需求调整
const loadingInstance = ref<any>(null)
const templateFileInputRef = ref<HTMLInputElement | null>(null)
const escapeHtml = (value: any) => {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

// 动态构建上传地址
const importUrl = computed(() => {
  return (
    import.meta.env.VITE_BASE_URL +
    import.meta.env.VITE_API_URL +
    '/system/user/import?updateSupport=' +
    updateSupport.value
  )
})

// 上传头信息
const uploadHeaders = ref<Record<string, any>>({})

// --- 方法定义 ---

/** 打开弹窗 */
const open = () => {
  dialogVisible.value = true
  // updateSupport.value = false
  resetForm()
}
defineExpose({ open })

/** 下载模板 */
const handleDownloadTemplate = async () => {
  try {
    const res = await UserApi.importUserTemplate()
    download.excel(res, '用户导入模版.xlsx')
  } catch (error) {
    message.error('模板下载失败')
  }
}

const handleUploadTemplate = () => {
  templateFileInputRef.value?.click()
}

// 选择文件后调用导入接口
const onFileSelected = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  // 检查文件大小（最大 200M）
  const maxSize = 200 * 1024 * 1024 // 200MB
  if (file.size > maxSize) {
    message.error('文件大小超过限制，最大支持 200M')
    input.value = ''
    return
  }
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在导入数据，请稍后...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await UserApi.importUserFileExcel(formData)
    console.log('data', data)

    // 关闭加载遮罩
    loadingInstance.close()
    let text = '上传成功数量：' + data.createUsernames.length + ';'
    for (let username of data.createUsernames) {
      text += '< ' + username + ' >'
    }
    text += '更新成功数量：' + data.updateUsernames.length + ';'
    for (const username of data.updateUsernames) {
      text += '< ' + username + ' >'
    }
    text += '更新失败数量：' + Object.keys(data.failureUsernames).length + ';'
    for (const username in data.failureUsernames) {
      text += '< ' + username + ': ' + data.failureUsernames[username] + ' >'
    }
    message.alert(text)
    emit('success')
    dialogVisible.value = false
  } catch (error) {
    // 关闭加载遮罩
    loadingInstance.close()
    message.error('导入失败')
    console.error('导入错误:', error)
    dialogVisible.value = false
  } finally {
    // 清空文件输入
    input.value = ''
  }
}

/** 上传前校验 */
const beforeUpload = (file: File) => {
  const isExcel =
    file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
    file.type === 'application/vnd.ms-excel'
  const isLt200M = file.size / 1024 / 1024 < 200

  if (!isExcel) {
    message.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt200M) {
    message.error('上传文件大小不能超过 200MB!')
    return false
  }

  // 设置 Header
  uploadHeaders.value = {
    Authorization: 'Bearer ' + getAccessToken(),
    'tenant-id': getTenantId()
  }

  return true
}

/** 提交表单 (由 el-upload 内部触发，这里主要处理加载状态) */
// 注意：因为设置了 auto-upload=false，我们需要手动调用 submit，
// 但为了简化，我们可以利用 before-upload 返回 true 后，el-upload 会自动尝试上传，
// 或者我们监听 change 事件。
// 这里采用更稳健的方式：在 beforeUpload 通过后，el-upload 会自动发起请求。
// 如果需要点击按钮才上传，可以将 el-button 放在 el-upload 外面并调用 uploadRef.value.submit()
// 但通常 el-upload 包裹按钮时，点击按钮即选择文件并上传。

/** 文件上传成功 */
const submitFormSuccess = (response: any) => {
  loadingInstance.value?.close()
  formLoading.value = false

  if (response.code !== 0) {
    message.error(response.msg || '导入失败')
    return
  }

  const data = response.data
  if (!data) {
    message.success('导入成功')
    dialogVisible.value = false
    emit('success')
    return
  }

  // 拼接详细结果
  let text = `导入完成！<br/>`
  text += `创建成功：${data.createUsernames?.length || 0} 条<br/>`
  text += `更新成功：${data.updateUsernames?.length || 0} 条<br/>`

  if (data.failureUsernames && Object.keys(data.failureUsernames).length > 0) {
    text += `<span style="color:red">失败：${Object.keys(data.failureUsernames).length} 条</span><br/>`
    // 可选：显示失败详情，如果太多可以省略
    // for (const username in data.failureUsernames) {
    //   text += `${username}: ${data.failureUsernames[username]}<br/>`
    // }
  }

  message.alert(text, '导入结果', { dangerouslyUseHTMLString: true })
  dialogVisible.value = false
  emit('success')
}

/** 上传错误 */
const submitFormError = () => {
  loadingInstance.value?.close()
  formLoading.value = false
  message.error('上传失败，请检查网络或文件格式')
}

/** 文件超出限制 */
const handleExceed = () => {
  message.warning('当前限制选择 1 个文件，本次选择了多个文件，共选择了多个文件')
}

/** 重置表单 */
const resetForm = () => {
  formLoading.value = false
  nextTick(() => {
    uploadRef.value?.clearFiles()
  })
}

const emit = defineEmits(['success'])
</script>

<style scoped>
.import-container {
  padding: 10px 20px;
}

.instruction {
  padding: 10px;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  background-color: #f4f4f5;
  border-radius: 4px;
}

.button-group {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.option-group {
  margin-bottom: 15px;
}

.tip-text {
  display: flex;
  font-size: 12px;
  color: #909399;
  align-items: center;
}
</style>

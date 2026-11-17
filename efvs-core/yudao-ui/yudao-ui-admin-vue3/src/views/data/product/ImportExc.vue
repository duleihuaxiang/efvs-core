<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" @close="handleClose">
    <div class="import-dialog-content">
      <!-- 操作说明 -->
      <p class="instruction">
        操作说明： <br />1. 请先下载模板，按照格式填写数据。<br />
        2. 上传填写好的 Excel 文件（支持 .xlsx 格式，最大 200MB）。
      </p>

      <div class="button-group">
        <el-button type="primary" @click="handleDownloadTemplate"> 下载模板 </el-button>
        <el-button @click="handleUploadTemplate"> 上传模板 </el-button>
        <input
          ref="archiveFileInputRef"
          type="file"
          accept=".xlsx"
          style="display: none"
          @change="onArchiveFileSelected"
        />
      </div>

      <!--   <div class="field-list">
        <p><strong>模板字段：</strong></p>
        <p>
          项目编号、项目名称、任务编号、任务标题、任务负责人、任务开始时间、任务完成时间、任务计划工时、备注
        </p>
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
import { ProductApi } from '@/api/data/product'
import download from '@/utils/download'

// 对话框标题
const dialogTitle = ref('导入 Excel')

// 控制对话框显示隐藏
const dialogVisible = ref(false)
const message = useMessage()
const archiveFileInputRef = ref<HTMLInputElement | null>(null)
const emit = defineEmits(['success'])

const escapeHtml = (value: any) => {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}
// 打开弹窗
const openDialog = () => {
  dialogVisible.value = true
}

// 关闭弹窗
const handleClose = () => {
  dialogVisible.value = false
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const blob = await ProductApi.downloadArchiveTemplate()
    download.excel(blob as unknown as Blob, '档案模版.xlsx')
    message.success('模板下载成功')
  } catch {
    message.error('模板下载失败')
  }
}

// 上传模板
const handleUploadTemplate = () => {
  archiveFileInputRef.value?.click()
}
// 选择文件后调用导入接口
const onArchiveFileSelected = async (e: Event) => {
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
    const duplicatedRows = await ProductApi.importProductFileExcel(formData)
    loadingInstance.close()
    if (Array.isArray(duplicatedRows) && duplicatedRows.length > 0) {
      const duplicatedText = duplicatedRows
        .map((item: any, index: number) => {
          const rowNo = item?.excelRowNum ?? '-'
          const reason = item?.skipReason ?? '重复数据'
          const partNumber = item?.partNumber ?? ''
          const sequenceNumber = item?.sequenceNumber ?? ''
          const drawingVersion = item?.drawingVersion ?? ''
          return `<div class="duplicate-msg-item"><span class="duplicate-index">${index + 1}.</span><span class="duplicate-text">第${escapeHtml(rowNo)}行 | ${escapeHtml(reason)} | 产品件号:${escapeHtml(partNumber)} | 序列号:${escapeHtml(sequenceNumber)} | 图纸版本:${escapeHtml(drawingVersion)}</span></div>`
        })
        .join('')
      const alertHtml = `<div class="duplicate-msg"><div class="duplicate-msg-title">文件：${escapeHtml(file.name)}</div><div class="duplicate-msg-subtitle">导入完成：跳过 ${duplicatedRows.length} 条，其他数据已入库。</div><div class="duplicate-msg-subtitle">跳过明细如下：</div><div class="duplicate-msg-list">${duplicatedText}</div></div>`
      await ElMessageBox.alert(alertHtml, '导入完成', {
        confirmButtonText: '我知道了',
        customClass: 'import-duplicate-dialog',
        width: '90vw',
        dangerouslyUseHTMLString: true
      })
      message.success(`导入完成：跳过 ${duplicatedRows.length} 条，其他数据已入库`)
    } else {
      message.success('导入成功')
    }
    emit('success')
    handleClose()
  } catch (error) {
    loadingInstance.close()
    message.error('导入失败')
    console.error('导入错误:', error)
  } finally {
    input.value = ''
  }
}

// 暴露方法供外部调用
defineExpose({
  openDialog
})
</script>

<style scoped>
.import-dialog-content {
  padding: 20px;
  font-size: 14px;
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
  gap: 10px;
  margin-bottom: 20px;
}

.field-list {
  line-height: 1.6;
  color: #999;
}

.field-list p {
  margin: 4px 0;
}

.tip-text {
  display: flex;
  font-size: 12px;
  color: #909399;
  align-items: center;
}
</style>

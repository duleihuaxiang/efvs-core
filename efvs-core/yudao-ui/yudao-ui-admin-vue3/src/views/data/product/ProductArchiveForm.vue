<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <div class="product-record-page">
      <!-- 顶部操作按钮 -->
      <div class="top-buttons">
        <el-button size="small" @click="goBack">返回</el-button>
      </div>

      <!-- 主标题 -->
      <div
        class="text-center mt-6"
        style="padding: 10px 0; border: 1px solid #0e0e0f; border-bottom: none"
      >
        <h2 style="font-weight: normal">{{ archiveData.name }}</h2>
      </div>

      <el-descriptions
        border
        style="text-align: center; border: 1px solid #0e0e0f"
        :column="5"
        class="custom-descriptions"
      >
        <el-descriptions-item label="Document Number:" label-width="260" width="150">{{
          archiveData.documentNumber
        }}</el-descriptions-item>
        <el-descriptions-item label="Revision:" label-width="150" width="150">{{
          archiveData.revision
        }}</el-descriptions-item>
        <el-descriptions-item label="客户名称" label-width="200" width="220">{{
          archiveData.customerName
        }}</el-descriptions-item>
        <el-descriptions-item label="客户编号" label-width="150">
          {{ archiveData.customerNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="交付时间" label-width="150">{{
          archiveData.deliveryTime
        }}</el-descriptions-item>
      </el-descriptions>

      <!-- LRU基本信息表 -->
      <div
        class="main-title text-center"
        style="padding: 10px 0; border: 1px solid #0e0e0f; border-top: none; border-bottom: none"
      >
        <h6 style="font-weight: normal">一、LRU基本信息</h6>
      </div>
      <!-- <div class="lru-basic-info"> -->
      <el-table :data="[{}]" border class="custom-table-border">
        <el-table-column label="序号" width="60">
          <template #default>
            <span>1</span>
          </template>
        </el-table-column>
        <el-table-column label="产品名称" width="200">
          <template #default>
            <span>{{ archiveData.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="产品件号" width="150">
          <template #default>
            <span>{{ archiveData.partNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标牌版本" width="150">
          <template #default>
            <span>{{ archiveData.signVersion }}</span>
          </template>
        </el-table-column>
        <el-table-column label="序列号" width="150">
          <template #default>
            <span>{{ archiveData.sequenceNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="对应基线" width="200">
          <template #default>
            <span>{{ archiveData.baseline }}</span>
          </template>
        </el-table-column>
        <!--    <el-table-column label="交付时间" width="200">
          <template #default>
            <span>{{ archiveData.deliveryTime }}</span>
          </template>
        </el-table-column> -->

        <el-table-column label="备注">
          <template #default>
            <span>{{ archiveData.remark }}</span>
          </template>
        </el-table-column>
      </el-table>
      <!-- </div> -->

      <!-- LRU构建信息表 -->
      <div
        class="main-title text-center"
        style="padding: 10px 0; border: 1px solid #0e0e0f; border-top: none; border-bottom: none"
      >
        <h6 style="font-weight: normal">二、LRU构建信息</h6>
      </div>
      <!-- <div class="lru-construction-info mt-4"> -->
      <el-table :data="constructionList" border class="custom-table-border">
        <el-table-column label="序号" width="60">
          <template #default="{ row }">
            <span>{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="零部件名称" width="200">
          <template #default="{ row }">
            <span>{{ row.partName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="零部件图号" width="150">
          <template #default="{ row }">
            <span>{{ row.drawingNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图纸版本" width="150">
          <template #default="{ row }">
            <span>{{ row.drawingVersion }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生产编号/序列号" width="150">
          <template #default="{ row }">
            <span>{{ row.productionNumber }}</span>
          </template>
        </el-table-column>
        <!--         <el-table-column label="产品件号" width="150">
          <template #default="{ row }">
            <span>{{ row.partNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="产品序列号" width="150">
          <template #default="{ row }">
            <span>{{ row.sequenceNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户名称" width="180">
          <template #default="{ row }">
            <span>{{ row.customerName || archiveData.customerName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户编号" width="150">
          <template #default="{ row }">
            <span>{{ row.customerNumber || archiveData.customerNumber }}</span>
          </template>
        </el-table-column> -->
        <el-table-column label="对应基线" width="200">
          <template #default="{ row }">
            <span>{{ row.fileBaseline }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注">
          <template #default="{ row }">
            <span>{{ row.remark }}</span>
          </template>
        </el-table-column>
      </el-table>
      <!-- </div> -->
    </div></Dialog
  >
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ProductApi } from '@/api/data/product'

defineOptions({ name: 'ProductArchive' })

const message = useMessage()
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题

const constructionList = ref<any[]>([])
const archiveData = ref<any>({
  name: '',
  documentNumber: '',
  revision: '',
  customerName: '',
  customerNumber: '',
  partNumber: '',
  signVersion: '',
  sequenceNumber: '',
  baseline: '',
  deliveryTime: '',
  remark: ''
})

const mapArchiveRow = (row: any) => {
  return {
    name: row?.name ?? '',
    documentNumber: row?.documentNumber ?? '',
    revision: row?.revision ?? '',
    customerName: row?.customerName ?? '',
    customerNumber: row?.customerNumber ?? '',
    partNumber: row?.partNumber ?? '',
    signVersion: row?.signVersion ?? '',
    sequenceNumber: row?.sequenceNumber ?? '',
    baseline: row?.baseline ?? '',
    deliveryTime: row?.deliveryTime ?? '',
    remark: row?.remark ?? ''
  }
}

const mapConstructionRows = (list: any[] = []) => {
  return list
    .filter((r) => {
      const v = r?.partName
      return v !== undefined && v !== null && String(v).trim() !== ''
    })
    .map((r) => ({
      id: r?.id,
      partNumber: r?.partNumber ?? '',
      sequenceNumber: r?.sequenceNumber ?? '',
      partName: r?.partName ?? '',
      drawingNumber: r?.drawingNumber ?? '',
      drawingVersion: r?.drawingVersion ?? '',
      productionNumber: r?.productionNumber ?? '',
      fileBaseline: r?.fileBaseline ?? '',
      customerName: r?.customerName ?? '',
      customerNumber: r?.customerNumber ?? '',
      remark: r?.remark ?? ''
    }))
}

// 打开弹窗
const open = async (row: any) => {
  dialogTitle.value = '产品档案详情'
  dialogVisible.value = true

  // 调用方法获取数据
  if (row) {
    const result = await ProductApi.getProductFile({
      partNumber: row.partNumber,
      sequenceNumber: row.sequenceNumber
    })
    // 兼容新旧结构：
    // - 新结构：[{ ..., productFileList: [...] }]
    // - 旧结构：[{ partName, drawingNumber, ... }]
    const first = Array.isArray(result) ? result[0] : undefined
    if (first && Array.isArray(first.productFileList)) {
      constructionList.value = mapConstructionRows(first.productFileList || [])
      archiveData.value = mapArchiveRow(first)
    } else {
      constructionList.value = mapConstructionRows(result || [])
      archiveData.value = mapArchiveRow(row)
    }
  }
}

// 关闭弹窗
const close = () => {
  dialogVisible.value = false
}

// 暴露方法
defineExpose({
  open
})

const goBack = () => {
  close()
}
</script>

<style scoped lang="scss">
.product-record-page {
  padding: 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.top-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.main-title {
  font-size: 1.5rem;

  /* font-weight: bold; */
  color: #333;
}

.document-info,
.lru-basic-info,
.lru-construction-info {
  margin-top: 20px;
}

.custom-table-border {
  --el-table-border-color: #0e0e0f;

  .el-table__header th > div {
    color: #0e0e0f;
  }
}
// 局部穿透样式
:deep(.custom-descriptions) {
  // 外层容器边框
  // border: 1px solid #e6a23c !important; // 外层边框-橙色

  // 列边框
  .el-descriptions__cell {
    // border-color: #0d0d0d !important; // 列边框-绿色

    // 可选：区分“标签列”和“内容列”
    &.el-descriptions__label {
      border-right-color: #0b0b0b !important; // 标签列右边框-红色
    }

    &.el-descriptions__content {
      border-right-color: #0a0909 !important; // 内容列左边框-红色
      border-left-color: #0a0909 !important; // 内容列左边框-红色
    }
  }

  // 行分隔线
  .el-descriptions__row {
    // border-bottom-color: #909399 !important; // 行底边框-灰色
  }
}

.import-tip {
  margin-bottom: 15px;

  .tip-text {
    display: flex;
    font-size: 12px;
    color: #f56c6c;
    align-items: center;
  }
}
</style>

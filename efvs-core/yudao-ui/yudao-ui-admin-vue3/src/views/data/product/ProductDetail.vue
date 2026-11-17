<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <!-- 标识属性 -->
    <div class="mb-4">
      <div class="flex items-center mb-2 ml-5">
        <h3 class="text-blue-500 text-sm font-bold mr-2">标识属性</h3>
      </div>
      <el-input :model-value="identifierDisplay" disabled />
    </div>

    <!-- 设计属性 -->
    <div class="mb-4">
      <div class="flex items-center mb-2 ml-5">
        <h3 class="text-blue-500 text-sm font-bold mr-2">设计属性</h3>
        <el-button v-if="!designEditing" type="" size="default" @click="startDesignEdit">
          编辑
        </el-button>
        <template v-else>
          <el-button type="success" size="default" @click="saveDesignAttribute">保存</el-button>
          <el-button type="default" size="default" @click="cancelDesignEdit">取消</el-button>
        </template>
      </div>
      <Editor v-model="designAttribute" :readonly="!designEditing" height="200px" />
    </div>

    <!-- 保障属性 -->
    <div class="mb-4">
      <div class="flex items-center mb-2 ml-5">
        <h3 class="text-blue-500 text-sm font-bold mr-2">保障属性</h3>
        <el-button v-if="!guaranteeEditing" type="" size="default" @click="startGuaranteeEdit">
          编辑
        </el-button>
        <template v-else>
          <el-button type="success" size="default" @click="saveGuaranteeAttribute">保存</el-button>
          <el-button type="default" size="default" @click="cancelGuaranteeEdit">取消</el-button>
        </template>
      </div>
      <Editor v-model="guaranteeAttribute" :readonly="!guaranteeEditing" height="200px" />
    </div>

    <!-- 供应属性 -->
    <div class="mb-4">
      <div class="flex items-center mb-2 ml-5">
        <h3 class="text-blue-500 text-sm font-bold mr-2">供应属性</h3>
        <el-button v-if="!supplyEditing" type="" size="default" @click="startSupplyEdit">
          编辑
        </el-button>
        <template v-else>
          <el-button type="success" size="default" @click="saveSupplyAttribute">保存</el-button>
          <el-button type="default" size="default" @click="cancelSupplyEdit">取消</el-button>
        </template>
      </div>
      <Editor v-model="supplyAttribute" :readonly="!supplyEditing" height="200px" />
    </div>

    <!-- 机型属性 -->
    <div class="mb-4">
      <div class="flex items-center mb-2 ml-5">
        <h3 class="text-blue-500 text-sm font-bold mr-2">机型属性</h3>
        <el-button v-if="!modelEditing" type="" size="default" @click="startModelEdit">
          编辑
        </el-button>
        <template v-else>
          <el-button type="success" size="default" @click="saveModelAttribute">保存</el-button>
          <el-button type="default" size="default" @click="cancelModelEdit">取消</el-button>
        </template>
      </div>
      <Editor v-model="modelAttribute" :readonly="!modelEditing" height="200px" />
    </div>

    <!-- 故障信息 -->
    <div class="mb-4">
      <div class="flex items-center mb-2 ml-5">
        <h3 class="text-blue-500 text-sm font-bold mr-2">故障信息</h3>
        <el-button v-if="!faultEditing" type="" size="default" @click="startFaultEdit">
          编辑
        </el-button>
        <template v-else>
          <el-button type="success" size="default" @click="saveFaultAttribute">保存</el-button>
          <el-button type="default" size="default" @click="cancelFaultEdit">取消</el-button>
        </template>
      </div>
      <Editor v-model="faultAttribute" :readonly="!faultEditing" height="200px" />
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { ProductApi } from '@/api/data/product'
import { useRoute, useRouter } from 'vue-router'
import { useTagsView } from '@/hooks/web/useTagsView'

defineOptions({ name: 'ProductDetail' })

const route = useRoute()
const router = useRouter()
const message = useMessage() // 消息弹窗
const { setTitle } = useTagsView() // 标签页操作
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const isStandaloneRoute = computed(() => route.name === 'ProductDetail')

const product = ref({
  id: undefined as number | undefined,
  partName: '',
  documentNumber: '',
  revision: '',
  drawingNumber: '',
  productionNumber: '',
  name: '',
  partNumber: '',
  productPartNumber: '',
  productVersion: '',
  remark: '',
  manufacturer: '',
  designId: [],
  identificaionId: [],
  memoryId: [],
  guaranteeId: [],
  supplyId: [],
  faultInformationId: []
})

const identifierList = ref<any[]>([])

// 当前产品ID（从列表页传入，不依赖 getProduct 接口）
const currentProductId = ref<number | undefined>(undefined)
// 属性记录ID（从 getProductAllAttributes 返回的 id）
const attributeId = ref<number | undefined>(undefined)

// 属性字段（与接口 /efvs/attribute/update 统一）
const designAttribute = ref('')
const designEditing = ref(false)
const designAttributeOriginal = ref('')

const guaranteeAttribute = ref('')
const guaranteeEditing = ref(false)
const guaranteeAttributeOriginal = ref('')

const supplyAttribute = ref('')
const supplyEditing = ref(false)
const supplyAttributeOriginal = ref('')

const modelAttribute = ref('')
const modelEditing = ref(false)
const modelAttributeOriginal = ref('')

const faultAttribute = ref('')
const faultEditing = ref(false)
const faultAttributeOriginal = ref('')

// 标识属性（从列表页传递）
const identifierPartNumber = ref('')
const identifierSequenceNumber = ref('')
const identifierSignVersion = ref('')

const identifierDisplay = computed(() => {
  return [identifierPartNumber.value, identifierSequenceNumber.value, identifierSignVersion.value]
    .filter(Boolean)
    .join('；')
})

const resetData = () => {
  product.value = {
    id: undefined,
    partName: '',
    documentNumber: '',
    revision: '',
    drawingNumber: '',
    productionNumber: '',
    name: '',
    partNumber: '',
    productPartNumber: '',
    productVersion: '',
    remark: '',
    manufacturer: '',
    designId: [],
    identificaionId: [],
    memoryId: [],
    guaranteeId: [],
    supplyId: [],
    faultInformationId: []
  }
  identifierList.value = []
  currentProductId.value = undefined
  identifierPartNumber.value = ''
  identifierSequenceNumber.value = ''
  identifierSignVersion.value = ''
  designAttribute.value = ''
  designEditing.value = false
  designAttributeOriginal.value = ''
  guaranteeAttribute.value = ''
  guaranteeEditing.value = false
  guaranteeAttributeOriginal.value = ''
  supplyAttribute.value = ''
  supplyEditing.value = false
  supplyAttributeOriginal.value = ''
  modelAttribute.value = ''
  modelEditing.value = false
  modelAttributeOriginal.value = ''
  faultAttribute.value = ''
  faultEditing.value = false
  faultAttributeOriginal.value = ''
}

// 获取产品详情
const getProductDetail = async (id: number) => {
  try {
    /*     const data = await ProductApi.getProduct(id)
    product.value = data
    // 仅在作为独立路由页面时，才修改页签标题（避免在列表页弹窗时误改当前页签）
    if (isStandaloneRoute.value) {
      setTitle(data.name, route.fullPath)
    }

    getIdentifierList(product.value?.identificaionId) */
    // 从统一接口获取其余5个属性的备注
    getAllAttributes(id)
  } catch (error) {
    message.error(`获取产品详情失败:${error}`)
  }
}

// 获取统一属性备注（设计/保障/供应/机型/故障）
const getAllAttributes = async (id: number) => {
  try {
    const data = await ProductApi.getProductAllAttributes(id)
    attributeId.value = data.id
    designAttribute.value = data.designAttribute || ''
    designAttributeOriginal.value = data.designAttribute || ''
    guaranteeAttribute.value = data.guaranteeAttribute || ''
    guaranteeAttributeOriginal.value = data.guaranteeAttribute || ''
    supplyAttribute.value = data.supplyAttribute || ''
    supplyAttributeOriginal.value = data.supplyAttribute || ''
    modelAttribute.value = data.modelAttribute || ''
    modelAttributeOriginal.value = data.modelAttribute || ''
    faultAttribute.value = data.faultAttribute || ''
    faultAttributeOriginal.value = data.faultAttribute || ''
  } catch (error) {
    // 接口未实现时静默处理
    console.warn('获取统一属性备注失败:', error)
  }
}

// 获取标识属性
const getIdentifierList = async (identificaionId: number[]) => {
  if (!identificaionId) return
  try {
    const data = await ProductApi.getIdentificationAttribute(identificaionId)
    identifierList.value = data || []
  } catch (error) {
    message.error(`获取标识属性失败: ${error}`)
  }
}

// ---- 保存统一属性接口 ----
const saveAllAttributes = async () => {
  if (!currentProductId.value) return
  try {
    await ProductApi.updateProductAllAttributes({
      id: attributeId.value,
      productId: currentProductId.value,
      designAttribute: designAttribute.value,
      guaranteeAttribute: guaranteeAttribute.value,
      supplyAttribute: supplyAttribute.value,
      modelAttribute: modelAttribute.value,
      faultAttribute: faultAttribute.value
    })
  } catch (error) {
    message.error(`保存失败:${error}`)
    throw error
  }
}

// ---- 编辑/保存/取消 处理函数 ----

// 设计属性
const startDesignEdit = () => {
  designAttributeOriginal.value = designAttribute.value
  designEditing.value = true
}
const saveDesignAttribute = async () => {
  try {
    await saveAllAttributes()
    message.success('设计属性保存成功')
    designEditing.value = false
  } catch {
    // 错误已在 saveAllAttributes 中处理
  }
}
const cancelDesignEdit = () => {
  designAttribute.value = designAttributeOriginal.value
  designEditing.value = false
}

// 保障属性
const startGuaranteeEdit = () => {
  guaranteeAttributeOriginal.value = guaranteeAttribute.value
  guaranteeEditing.value = true
}
const saveGuaranteeAttribute = async () => {
  try {
    await saveAllAttributes()
    message.success('保障属性保存成功')
    guaranteeEditing.value = false
  } catch {
    // 错误已在 saveAllAttributes 中处理
  }
}
const cancelGuaranteeEdit = () => {
  guaranteeAttribute.value = guaranteeAttributeOriginal.value
  guaranteeEditing.value = false
}

// 供应属性
const startSupplyEdit = () => {
  supplyAttributeOriginal.value = supplyAttribute.value
  supplyEditing.value = true
}
const saveSupplyAttribute = async () => {
  try {
    await saveAllAttributes()
    message.success('供应属性保存成功')
    supplyEditing.value = false
  } catch {
    // 错误已在 saveAllAttributes 中处理
  }
}
const cancelSupplyEdit = () => {
  supplyAttribute.value = supplyAttributeOriginal.value
  supplyEditing.value = false
}

// 机型属性
const startModelEdit = () => {
  modelAttributeOriginal.value = modelAttribute.value
  modelEditing.value = true
}
const saveModelAttribute = async () => {
  try {
    await saveAllAttributes()
    message.success('机型属性保存成功')
    modelEditing.value = false
  } catch {
    // 错误已在 saveAllAttributes 中处理
  }
}
const cancelModelEdit = () => {
  modelAttribute.value = modelAttributeOriginal.value
  modelEditing.value = false
}

// 故障信息
const startFaultEdit = () => {
  faultAttributeOriginal.value = faultAttribute.value
  faultEditing.value = true
}
const saveFaultAttribute = async () => {
  try {
    await saveAllAttributes()
    message.success('故障信息保存成功')
    faultEditing.value = false
  } catch {
    // 错误已在 saveAllAttributes 中处理
  }
}
const cancelFaultEdit = () => {
  faultAttribute.value = faultAttributeOriginal.value
  faultEditing.value = false
}

/** 打开弹窗（供父组件调用） */
const open = async (
  id: number,
  identifierData?: { partNumber?: string; sequenceNumber?: string; signVersion?: string }
) => {
  dialogTitle.value = '产品详情'
  dialogVisible.value = true
  resetData()
  // 保存从列表页传递过来的产品ID
  currentProductId.value = id
  // 设置从列表页传递过来的标识属性数据
  if (identifierData) {
    identifierPartNumber.value = identifierData.partNumber || ''
    identifierSequenceNumber.value = identifierData.sequenceNumber || ''
    identifierSignVersion.value = identifierData.signVersion || ''
  }
  if (!id) return
  await getProductDetail(id)
}

defineExpose({ open })

// 如果通过路由进入详情页：自动打开弹窗并在关闭后返回上一页
watch(
  () => dialogVisible.value,
  (visible) => {
    if (!visible && isStandaloneRoute.value) {
      router.back()
    }
  }
)

// 初始化
onMounted(() => {
  const productId = Number(route.query.id)
  if (productId) {
    open(productId)
  }
})
</script>

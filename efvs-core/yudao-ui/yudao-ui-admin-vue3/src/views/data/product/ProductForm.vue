<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :autoFullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <!--  <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="保障属性ID" prop="guaranteeId">
            <el-select multiple v-model="formData.guaranteeId" placeholder="请选择保障属性ID">
              <el-option
                v-for="item in guaranteeIdList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="供应属性ID" prop="supplyId">
            <el-select multiple v-model="formData.supplyId" placeholder="请选择供应属性ID">
              <el-option
                v-for="item in supplyIdList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="标识属性ID" prop="identificaionId">
            <el-select v-model="formData.identificaionId" placeholder="请选择标识属性ID" multiple>
              <el-option
                v-for="item in identificationIdList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设计属性ID" prop="designId">
            <el-select v-model="formData.designId" placeholder="请选择设计属性ID" multiple>
              <el-option
                v-for="item in designIdList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="机型属性ID" prop="memoryId">
            <el-select v-model="formData.memoryId" placeholder="请选择机型属性ID" multiple>
              <el-option
                v-for="item in memoryIdList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="故障信息ID" prop="faultInformationId">
            <el-select
              v-model="formData.faultInformationId"
              placeholder="请选择故障信息ID"
              multiple
            >
              <el-option
                v-for="item in faultInformationIdList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row> -->

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属部件件号" prop="productPartNumber">
            <el-input v-model="formData.productPartNumber" placeholder="请输入所属部件代号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="版本" prop="productVersion">
            <el-input v-model="formData.productVersion" placeholder="请输入版本" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型" prop="productType">
            <el-select v-model="formData.productType" placeholder="请选择类型">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.PRODUCT_TYPE)"
                :key="dict.value"
                :label="dict.label"
                :value="Number(dict.value)"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="厂家" prop="manufacturer">
            <el-input v-model="formData.manufacturer" placeholder="请输入厂家" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品件号" prop="partNumber">
            <el-input v-model="formData.partNumber" placeholder="请输入产品件号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { ProductApi, Product } from '@/api/data/product'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'

/** 产品 表单 */
defineOptions({ name: 'ProductForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  guaranteeId: [],
  supplyId: [],
  identificaionId: [],
  designId: [],
  memoryId: [],
  faultInformationId: [],
  pmn: undefined,
  level: undefined,
  name: undefined,
  codeName: undefined,
  productPartNumber: undefined,
  productVersion: undefined,
  productType: undefined,
  // productNum: undefined,
  partNumber: undefined,
  sequenceNumber: undefined,
  manufacturer: undefined,
  tagNumber: undefined,
  subcategory: undefined,
  small_subcategory: undefined,
  category: undefined,
  drawing_number: undefined,
  customerNumber: undefined,
  customerName: undefined,
  unit: undefined,
  remark: undefined,
  signVersion: undefined, //标牌版本
  baseline: undefined //对应基线
})
const formRules = reactive({})
const formRef = ref() // 表单 Ref
const guaranteeIdList = ref([]) //保障属性optionList
const supplyIdList = ref([]) //供应属性optionList
const identificationIdList = ref([]) //标识属性optionList
const designIdList = ref([]) //设计属性optionList
const memoryIdList = ref([]) //机型属性optionList
const faultInformationIdList = ref([]) //故障信息optionList

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  guaranteeIdList.value = (await ProductApi.getAllGuaranteeAttributes()) || []
  faultInformationIdList.value = (await ProductApi.getAllFaultInformation()) || []
  identificationIdList.value = (await ProductApi.getAllIdentificationAttribute()) || []
  memoryIdList.value = (await ProductApi.getAllMemoryAttribute()) || []
  supplyIdList.value = (await ProductApi.getAllSupplyAttribute()) || []
  designIdList.value = (await ProductApi.getAllDesignAttribute()) || []

  resetForm()

  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await ProductApi.getProduct(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as Product
    if (formType.value === 'create') {
      await ProductApi.createProduct(data)
      message.success(t('common.createSuccess'))
    } else {
      await ProductApi.updateProduct(data)
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
    id: undefined,
    guaranteeId: [],
    supplyId: [],
    identificaionId: [],
    designId: [],
    memoryId: [],
    faultInformationId: [],
    pmn: undefined,
    level: undefined,
    name: undefined,
    codeName: undefined,
    productPartNumber: undefined,
    productVersion: undefined,
    productType: undefined,
    // productNum: undefined,
    partNumber: undefined,
    sequenceNumber: undefined,
    manufacturer: undefined,
    tagNumber: undefined,
    subcategory: undefined,
    small_subcategory: undefined,
    category: undefined,
    drawing_number: undefined,
    customerNumber: undefined,
    customerName: undefined,
    unit: undefined,
    remark: undefined,
    signVersion: undefined, //标牌版本
    baseline: undefined //对应基线
  }
  formRef.value?.resetFields()
}
</script>

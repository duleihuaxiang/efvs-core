<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="900" :auto-fullscreen="true">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110"
      v-loading="formLoading"
    >
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="客户名称" prop="customerName">
            <el-input v-model="formData.customerName" placeholder="请输入客户名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="客户编号" prop="customerId">
            <el-input v-model="formData.customerId" placeholder="请输入客户编号" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="客户联系人" prop="customerContactPerson">
            <el-input v-model="formData.customerContactPerson" placeholder="请输入客户联系人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系人电话" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入联系人电话" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="所属部门" prop="dept">
            <el-input v-model="formData.dept" placeholder="请输入所属部门" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱地址" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱地址" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="客户地址(省)" prop="provinceId">
            <el-select
              v-model="formData.provinceId"
              placeholder="请选择省"
              @change="handleProvinceChange"
            >
              <el-option
                v-for="item in provinceList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="(市)" prop="cityId">
            <el-select v-model="formData.cityId" placeholder="请选择市" @change="handleCityChange">
              <el-option
                v-for="item in cityList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="(区)" prop="districtId">
            <el-select
              v-model="formData.districtId"
              placeholder="请选择区"
              @change="handleDistrictChange"
            >
              <el-option
                v-for="item in districtList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮编" prop="postalCode">
            <el-input v-model="formData.postalCode" placeholder="请输入邮编" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="formData.address" placeholder="请输入详细地址" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="数据获取渠道" prop="dataSource">
            <el-input v-model="formData.dataSource" placeholder="请输入数据获取渠道" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>
      <h3 class="text-blue-500 text-sm font-bold mb-2 ml-5" v-if="formType === 'update'"
        >购买产品信息</h3
      >
      <!-- 搜索工作栏 -->
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        label-width="100"
        v-if="formType === 'update'"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input
                v-model="queryParams.name"
                placeholder="请输入名称"
                clearable
                @keyup.enter="handleQuery"
                class="!w-300px"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版本" prop="productVersion" label-width="100">
              <el-input
                v-model="queryParams.productVersion"
                placeholder="请输入版本"
                clearable
                @keyup.enter="handleQuery"
                class="!w-300px"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 列表 -->
      <ContentWrap v-if="formType === 'update'">
        <el-table row-key="id" v-loading="loading" :data="list">
          <el-table-column label="产品名称" align="left" prop="name" />
          <!--    <el-table-column
            label="所属部件件号"
            align="center"
            prop="productPartNumber"
            min-width="100"
          /> -->
          <el-table-column label="产品件号" align="center" prop="partNumber" />
          <el-table-column label="标牌版本" align="center" prop="productVersion" />
          <!--         <el-table-column label="类型" align="center" prop="productType">
            <template #default="scope">
              <span>
                {{ getDictLabel(DICT_TYPE.PRODUCT_TYPE, scope.row.productType) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="生产厂家" align="center" prop="manufacturer" />
 -->
          <el-table-column label="序列号" align="left" prop="sequenceNumber" />
          <el-table-column label="对应基线" align="left" prop="baseline" min-width="130" />
          <el-table-column
            label="交付去向"
            align="center"
            prop="deliveryDestination"
            min-width="130"
          />
          <el-table-column label="交付时间" align="center" prop="deliveryTime" min-width="210" />
          <el-table-column label="备注" align="center" prop="remark" />

          <el-table-column label="操作" align="center" min-width="160">
            <template #default="scope">
              <el-button link type="primary" @click="toDetail(scope.row)"> 详情 </el-button>
              <!--   <el-button
                link
                type="primary"
                @click="openForm('update', scope.row.id)"
                v-hasPermi="['data:product:update']"
              >
                编辑
              </el-button> -->
              <el-button link type="primary" @click="toArchive(scope.row)"> 档案 </el-button>
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
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
    <!-- 表单弹窗：添加/修改 -->
    <ProductForm ref="listRef" @success="getList" />
    <!-- 产品档案列表 -->
    <ProductArchiveList ref="ProductArchiveListRef" />
    <!-- 产品详情 -->
    <ProductDetail ref="ProductDetailRef" />
  </Dialog>
</template>
<script setup lang="ts">
import { CustomerInfoApi, CustomerInfo } from '@/api/data/customerinfo'
import { ProductApi, Product } from '@/api/data/product'
import { DICT_TYPE, getDictLabel } from '@/utils/dict'
import ProductArchiveList from '@/views/data/product/ProductArchiveList.vue'
import ProductDetail from '@/views/data/product/ProductDetail.vue'
import ProductForm from '@/views/data/product/ProductForm.vue'

/** 客户信息 表单 */
defineOptions({ name: 'CustomerInfoForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const router = useRouter()

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  customerName: undefined,
  customerId: undefined,
  customerContactPerson: undefined,
  phone: undefined,
  dept: undefined,
  email: undefined,
  provinceId: undefined,
  cityId: undefined,
  districtId: undefined,
  province: undefined,
  city: undefined,
  district: undefined,
  postalCode: undefined,
  address: undefined,
  dataSource: undefined,
  remark: undefined
})
const formRules = reactive({
  customerName: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
  customerId: [{ required: true, message: '客户编号不能为空', trigger: 'blur' }],
  provinceId: [{ required: true, message: '客户地址（省）不能为空', trigger: 'blur' }],
  cityId: [{ required: true, message: '客户地址（市）不能为空', trigger: 'blur' }],
  districtId: [{ required: true, message: '客户地址（区）不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref
const ProductArchiveListRef = ref()
const ProductDetailRef = ref()
const provinceList = ref([])
const cityList = ref([])
const districtList = ref([])
const loading = ref(false) // 列表的加载中
const list = ref<Product[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  customerNumber: '',
  customerName: '',
  name: undefined,
  productVersion: undefined
})
const queryFormRef = ref() // 搜索的表单
/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}
/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await ProductApi.getProductPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const listRef = ref()
const openForm = (type: string, id?: number) => {
  listRef.value.open(type, id)
}

// 点击详情
const toDetail = (row: any) => {
  ProductDetailRef.value?.open?.(row.id, {
    partNumber: row.partNumber,
    sequenceNumber: row.sequenceNumber,
    signVersion: row.signVersion
  })
}

// 点击档案
const toArchive = (row: any) => {
  ProductArchiveListRef.value?.open?.(row)
}
// 获取省份
const getProvinceList = async () => {
  const data = await CustomerInfoApi.customerArea()
  provinceList.value = data.areas
}

// 省选择变化
const handleProvinceChange = async (value: number) => {
  if (value) {
    // 设置省份ID和名称
    const selectedProvince = provinceList.value.find((item) => item.id === value)
    if (selectedProvince) {
      formData.value.province = selectedProvince.name
    }
    const data = await CustomerInfoApi.customerArea({ parentId: value })
    cityList.value = data.areas || []
    districtList.value = []
    formData.value.cityId = undefined
    formData.value.districtId = undefined
    formData.value.city = undefined // 清空市名称
    formData.value.district = undefined // 清空区名称
  }
}

// 市选择变化
const handleCityChange = async (value: number) => {
  if (value) {
    // 设置城市ID和名称
    const selectedCity = cityList.value.find((item) => item.id === value)
    if (selectedCity) {
      formData.value.city = selectedCity.name
    }
    const data = await CustomerInfoApi.customerArea({ parentId: value })
    districtList.value = data.areas || []
    formData.value.districtId = undefined
    formData.value.district = undefined // 清空区名称
  }
}
// 区变化
const handleDistrictChange = (value: number) => {
  if (value) {
    // 设置区域ID和名称
    const selectedDistrict = districtList.value.find((item) => item.id === value)
    if (selectedDistrict) {
      formData.value.district = selectedDistrict.name
    }
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number, customerName?: string, customerId?: string) => {
  dialogVisible.value = true
  getProvinceList()
  dialogTitle.value = t('action.' + type)

  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    queryParams.customerNumber = customerId || ''
    queryParams.customerName = customerName || ''
    getList()
    formLoading.value = true
    try {
      formData.value = await CustomerInfoApi.getCustomerInfo(id)
      if (formData.value.provinceId) {
        const data = await CustomerInfoApi.customerArea({ parentId: formData.value.provinceId })
        cityList.value = data.areas || []

        if (formData.value.cityId) {
          const data = await CustomerInfoApi.customerArea({ parentId: formData.value.cityId })
          districtList.value = data.areas || []
        }
      }
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
    const data = formData.value as unknown as CustomerInfo
    if (formType.value === 'create') {
      await CustomerInfoApi.createCustomerInfo(data)
      message.success(t('common.createSuccess'))
    } else {
      await CustomerInfoApi.updateCustomerInfo(data)
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
    customerName: undefined,
    customerId: undefined,
    customerContactPerson: undefined,
    phone: undefined,
    dept: undefined,
    email: undefined,
    provinceId: undefined,
    cityId: undefined,
    districtId: undefined,
    province: undefined,
    city: undefined,
    district: undefined,
    postalCode: undefined,
    address: undefined,
    dataSource: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>

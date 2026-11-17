<template>
  <div class="panel-tab__content">
    <el-form label-width="80px">
      <el-form-item label="表单类型" prop="elementFormType" class="mb-20px">
        <el-radio-group v-model="elementFormType" @change="updateElementFormType">
          <el-radio
            v-for="dict in getIntDictOptions(DICT_TYPE.BPM_MODEL_FORM_TYPE)"
            :key="dict.value"
            :value="dict.value"
          >
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <!-- 流程表单 -->
      <el-form-item label="流程表单" v-if="elementFormType === BpmModelFormType.NORMAL">
        <!--        <el-input v-model="formKey" clearable @change="updateElementFormKey" />-->
        <el-select v-model="formKey" clearable @change="updateElementFormKey">
          <el-option v-for="form in formList" :key="form.id" :label="form.name" :value="form.id" />
        </el-select>
      </el-form-item>
      <!-- 业务表单 -->
      <el-form-item
        label="表单地址"
        prop="elementCreatePath"
        v-if="elementFormType === BpmModelFormType.CUSTOM"
      >
        <el-input
          v-model="elementCreatePath"
          placeholder="请输入表单地址"
          style="width: 330px"
          @change="updateElementCreatePath"
        />
      </el-form-item>
      <!--  <el-form-item label="业务标识">
        <el-select v-model="businessKey" @change="updateElementBusinessKey">
          <el-option v-for="i in formList" :key="i.id" :value="i.id" :label="i.name" />
          <el-option label="无" value="" />
        </el-select>
      </el-form-item> -->
    </el-form>

    <!--字段列表-->
    <!--    <div class="element-property list-property">-->
    <!--      <el-divider><Icon icon="ep:coin" /> 表单字段</el-divider>-->
    <!--      <el-table :data="fieldList" max-height="240" fit border>-->
    <!--        <el-table-column label="序号" type="index" width="50px" />-->
    <!--        <el-table-column label="字段名称" prop="label" min-width="80px" show-overflow-tooltip />-->
    <!--        <el-table-column-->
    <!--          label="字段类型"-->
    <!--          prop="type"-->
    <!--          min-width="80px"-->
    <!--          :formatter="(row) => fieldType[row.type] || row.type"-->
    <!--          show-overflow-tooltip-->
    <!--        />-->
    <!--        <el-table-column-->
    <!--          label="默认值"-->
    <!--          prop="defaultValue"-->
    <!--          min-width="80px"-->
    <!--          show-overflow-tooltip-->
    <!--        />-->
    <!--        <el-table-column label="操作" width="90px">-->
    <!--          <template #default="scope">-->
    <!--            <el-button type="primary" link @click="openFieldForm(scope, scope.$index)"-->
    <!--              >编辑</el-button-->
    <!--            >-->
    <!--            <el-divider direction="vertical" />-->
    <!--            <el-button-->
    <!--              type="primary"-->
    <!--              link-->
    <!--              style="color: #ff4d4f"-->
    <!--              @click="removeField(scope, scope.$index)"-->
    <!--              >移除</el-button-->
    <!--            >-->
    <!--          </template>-->
    <!--        </el-table-column>-->
    <!--      </el-table>-->
    <!--    </div>-->
    <!--    <div class="element-drawer__button">-->
    <!--      <XButton type="primary" proIcon="ep:plus" title="添加字段" @click="openFieldForm(null, -1)" />-->
    <!--    </div>-->

    <!--字段配置侧边栏-->
    <!--    <el-drawer-->
    <!--      v-model="fieldModelVisible"-->
    <!--      title="字段配置"-->
    <!--      :size="`${width}px`"-->
    <!--      append-to-body-->
    <!--      destroy-on-close-->
    <!--    >-->
    <!--      <el-form :model="formFieldForm" label-width="90px">-->
    <!--        <el-form-item label="字段ID">-->
    <!--          <el-input v-model="formFieldForm.id" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="类型">-->
    <!--          <el-select-->
    <!--            v-model="formFieldForm.typeType"-->
    <!--            placeholder="请选择字段类型"-->
    <!--            clearable-->
    <!--            @change="changeFieldTypeType"-->
    <!--          >-->
    <!--            <el-option v-for="(value, key) of fieldType" :label="value" :value="key" :key="key" />-->
    <!--          </el-select>-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="类型名称" v-if="formFieldForm.typeType === 'custom'">-->
    <!--          <el-input v-model="formFieldForm.type" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="名称">-->
    <!--          <el-input v-model="formFieldForm.label" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="时间格式" v-if="formFieldForm.typeType === 'date'">-->
    <!--          <el-input v-model="formFieldForm.datePattern" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="默认值">-->
    <!--          <el-input v-model="formFieldForm.defaultValue" clearable />-->
    <!--        </el-form-item>-->
    <!--      </el-form>-->

    <!--      &lt;!&ndash; 枚举值设置 &ndash;&gt;-->
    <!--      <template v-if="formFieldForm.type === 'enum'">-->
    <!--        <el-divider key="enum-divider" />-->
    <!--        <p class="listener-filed__title" key="enum-title">-->
    <!--          <span><Icon icon="ep:menu" />枚举值列表：</span>-->
    <!--          <el-button type="primary" @click="openFieldOptionForm(null, -1, 'enum')"-->
    <!--            >添加枚举值</el-button-->
    <!--          >-->
    <!--        </p>-->
    <!--        <el-table :data="fieldEnumList" key="enum-table" max-height="240" fit border>-->
    <!--          <el-table-column label="序号" width="50px" type="index" />-->
    <!--          <el-table-column label="枚举值编号" prop="id" min-width="100px" show-overflow-tooltip />-->
    <!--          <el-table-column label="枚举值名称" prop="name" min-width="100px" show-overflow-tooltip />-->
    <!--          <el-table-column label="操作" width="90px">-->
    <!--            <template #default="scope">-->
    <!--              <el-button-->
    <!--                type="primary"-->
    <!--                link-->
    <!--                @click="openFieldOptionForm(scope, scope.$index, 'enum')"-->
    <!--                >编辑</el-button-->
    <!--              >-->
    <!--              <el-divider direction="vertical" />-->
    <!--              <el-button-->
    <!--                type="primary"-->
    <!--                link-->
    <!--                style="color: #ff4d4f"-->
    <!--                @click="removeFieldOptionItem(scope, scope.$index, 'enum')"-->
    <!--                >移除</el-button-->
    <!--              >-->
    <!--            </template>-->
    <!--          </el-table-column>-->
    <!--        </el-table>-->
    <!--      </template>-->

    <!--      &lt;!&ndash; 校验规则 &ndash;&gt;-->
    <!--      <el-divider key="validation-divider" />-->
    <!--      <p class="listener-filed__title" key="validation-title">-->
    <!--        <span><Icon icon="ep:menu" />约束条件列表：</span>-->
    <!--        <el-button type="primary" @click="openFieldOptionForm(null, -1, 'constraint')"-->
    <!--          >添加约束</el-button-->
    <!--        >-->
    <!--      </p>-->
    <!--      <el-table :data="fieldConstraintsList" key="validation-table" max-height="240" fit border>-->
    <!--        <el-table-column label="序号" width="50px" type="index" />-->
    <!--        <el-table-column label="约束名称" prop="name" min-width="100px" show-overflow-tooltip />-->
    <!--        <el-table-column label="约束配置" prop="config" min-width="100px" show-overflow-tooltip />-->
    <!--        <el-table-column label="操作" width="90px">-->
    <!--          <template #default="scope">-->
    <!--            <el-button-->
    <!--              type="primary"-->
    <!--              link-->
    <!--              @click="openFieldOptionForm(scope, scope.$index, 'constraint')"-->
    <!--              >编辑</el-button-->
    <!--            >-->
    <!--            <el-divider direction="vertical" />-->
    <!--            <el-button-->
    <!--              type="primary"-->
    <!--              link-->
    <!--              style="color: #ff4d4f"-->
    <!--              @click="removeFieldOptionItem(scope, scope.$index, 'constraint')"-->
    <!--              >移除</el-button-->
    <!--            >-->
    <!--          </template>-->
    <!--        </el-table-column>-->
    <!--      </el-table>-->

    <!--      &lt;!&ndash; 表单属性 &ndash;&gt;-->
    <!--      <el-divider key="property-divider" />-->
    <!--      <p class="listener-filed__title" key="property-title">-->
    <!--        <span><Icon icon="ep:menu" />字段属性列表：</span>-->
    <!--        <el-button type="primary" @click="openFieldOptionForm(null, -1, 'property')"-->
    <!--          >添加属性</el-button-->
    <!--        >-->
    <!--      </p>-->
    <!--      <el-table :data="fieldPropertiesList" key="property-table" max-height="240" fit border>-->
    <!--        <el-table-column label="序号" width="50px" type="index" />-->
    <!--        <el-table-column label="属性编号" prop="id" min-width="100px" show-overflow-tooltip />-->
    <!--        <el-table-column label="属性值" prop="value" min-width="100px" show-overflow-tooltip />-->
    <!--        <el-table-column label="操作" width="90px">-->
    <!--          <template #default="scope">-->
    <!--            <el-button-->
    <!--              type="primary"-->
    <!--              link-->
    <!--              @click="openFieldOptionForm(scope, scope.$index, 'property')"-->
    <!--              >编辑</el-button-->
    <!--            >-->
    <!--            <el-divider direction="vertical" />-->
    <!--            <el-button-->
    <!--              type="primary"-->
    <!--              link-->
    <!--              style="color: #ff4d4f"-->
    <!--              @click="removeFieldOptionItem(scope, scope.$index, 'property')"-->
    <!--              >移除</el-button-->
    <!--            >-->
    <!--          </template>-->
    <!--        </el-table-column>-->
    <!--      </el-table>-->

    <!--      &lt;!&ndash; 底部按钮 &ndash;&gt;-->
    <!--      <div class="element-drawer__button">-->
    <!--        <el-button>取 消</el-button>-->
    <!--        <el-button type="primary" @click="saveField">保 存</el-button>-->
    <!--      </div>-->
    <!--    </el-drawer>-->

    <!--    <el-dialog-->
    <!--      v-model="fieldOptionModelVisible"-->
    <!--      :title="optionModelTitle"-->
    <!--      width="600px"-->
    <!--      append-to-body-->
    <!--      destroy-on-close-->
    <!--    >-->
    <!--      <el-form :model="fieldOptionForm" label-width="96px">-->
    <!--        <el-form-item label="编号/ID" v-if="fieldOptionType !== 'constraint'" key="option-id">-->
    <!--          <el-input v-model="fieldOptionForm.id" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="名称" v-if="fieldOptionType !== 'property'" key="option-name">-->
    <!--          <el-input v-model="fieldOptionForm.name" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="配置" v-if="fieldOptionType === 'constraint'" key="option-config">-->
    <!--          <el-input v-model="fieldOptionForm.config" clearable />-->
    <!--        </el-form-item>-->
    <!--        <el-form-item label="值" v-if="fieldOptionType === 'property'" key="option-value">-->
    <!--          <el-input v-model="fieldOptionForm.value" clearable />-->
    <!--        </el-form-item>-->
    <!--      </el-form>-->
    <!--      <template #footer>-->
    <!--        <el-button @click="fieldOptionModelVisible = false">取 消</el-button>-->
    <!--        <el-button type="primary" @click="saveFieldOption">确 定</el-button>-->
    <!--      </template>-->
    <!--    </el-dialog>-->
  </div>
</template>

<script lang="ts" setup>
import * as FormApi from '@/api/bpm/form'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { BpmModelFormType } from '@/utils/constants'

defineOptions({ name: 'ElementForm' })

const props = defineProps({
  id: String,
  type: String
})
const prefix = inject('prefix')
const width = inject('width')
const modelData = inject('modelData') as Ref

const formKey = ref(undefined)
const businessKey = ref('')
const test = ref('')
const elementFormType = ref(undefined) //表单类型  流程表单/业务表单
const elementCreatePath = ref('') //表单地址
const optionModelTitle = ref('')
const fieldList = ref<any[]>([])
const formFieldForm = ref<any>({})

const fieldType = ref({
  long: '长整型',
  string: '字符串',
  boolean: '布尔类',
  date: '日期类',
  enum: '枚举类',
  custom: '自定义类型'
})
const formFieldIndex = ref(-1) // 编辑中的字段， -1 为新增
const formFieldOptionIndex = ref(-1) // 编辑中的字段配置项， -1 为新增
const fieldModelVisible = ref(false)
const fieldOptionModelVisible = ref(false)
const fieldOptionForm = ref<any>({}) // 当前激活的字段配置项数据
const fieldOptionType = ref('') // 当前激活的字段配置项弹窗 类型
const fieldEnumList = ref<any[]>([]) // 枚举值列表
const fieldConstraintsList = ref<any[]>([]) // 约束条件列表
const fieldPropertiesList = ref<any[]>([]) // 绑定属性列表
const bpmnELement = ref()
const elExtensionElements = ref()
const formData = ref()
const otherExtensions = ref()

const bpmnInstances = () => (window as any)?.bpmnInstances
const resetFormList = () => {
  bpmnELement.value = bpmnInstances().bpmnElement
  formKey.value = bpmnELement.value.businessObject.formKey
  elementFormType.value = bpmnELement.value.businessObject.$attrs.elementFormType
  elementCreatePath.value = bpmnELement.value.businessObject.$attrs.elementCreatePath

  // 获取元素扩展属性 或者 创建扩展属性
  elExtensionElements.value =
    bpmnELement.value.businessObject.get('extensionElements') ||
    bpmnInstances().moddle.create('bpmn:ExtensionElements', { values: [] })
  // 获取元素表单配置 或者 创建新的表单配置
  formData.value =
    elExtensionElements.value.values.filter((ex) => ex.$type === `${prefix}:FormData`)?.[0] ||
    bpmnInstances().moddle.create(`${prefix}:FormData`, {
      fields: []
    })

  // 业务标识 businessKey， 绑定在 formData 中
  businessKey.value = formData.value.businessKey
  console.log('=======', bpmnELement.value.businessObject)

  // 保留剩余扩展元素，便于后面更新该元素对应属性
  otherExtensions.value = elExtensionElements.value.values.filter(
    (ex) => ex.$type !== `${prefix}:FormData`
  )

  // 复制原始值，填充表格
  fieldList.value = JSON.parse(JSON.stringify(formData.value.fields || []))

  // 更新元素扩展属性，避免后续报错
  updateElementExtensions()
}
const updateElementFormKey = async () => {
  bpmnInstances().modeling.updateProperties(toRaw(bpmnELement.value), {
    formKey: formKey.value,
    elementCreatePath: ''
  })

  const params = {
    processId: modelData.value.key,
    activityId: props.id,
    formType: elementFormType.value,
    formKey: elementFormType.value === BpmModelFormType.NORMAL ? formKey.value : undefined,
    formUrl: elementFormType.value === BpmModelFormType.CUSTOM ? elementCreatePath.value : '',
    status: 1
  }
  const data = await FormApi.creatProcessActFormConfig(params)
}

const updateElementFormType = () => {
  bpmnInstances().modeling.updateProperties(toRaw(bpmnELement.value), {
    elementFormType: elementFormType.value
  })
}

const updateElementCreatePath = async () => {
  bpmnInstances().modeling.updateProperties(toRaw(bpmnELement.value), {
    elementCreatePath: elementCreatePath.value,
    formKey: undefined
  })

  const params = {
    processId: modelData.value.key,
    activityId: props.id,
    formType: elementFormType.value,
    formKey: elementFormType.value === BpmModelFormType.NORMAL ? formKey.value : undefined,
    formUrl: elementFormType.value === BpmModelFormType.CUSTOM ? elementCreatePath.value : '',
    status: 1
  }

  const data = await FormApi.creatProcessActFormConfig(params)
}
const updateElementBusinessKey = () => {
  bpmnInstances().modeling.updateModdleProperties(toRaw(bpmnELement.value), formData.value, {
    businessKey: businessKey.value
  })
}

// 根据类型调整字段type
const changeFieldTypeType = (type) => {
  // this.$set(this.formFieldForm, "type", type === "custom" ? "" : type);
  formFieldForm.value['type'] = type === 'custom' ? '' : type
}

// 打开字段详情侧边栏
const openFieldForm = (field, index) => {
  formFieldIndex.value = index
  if (index !== -1) {
    const FieldObject = formData.value.fields[index]
    formFieldForm.value = JSON.parse(JSON.stringify(field))
    // 设置自定义类型
    // this.$set(this.formFieldForm, "typeType", !this.fieldType[field.type] ? "custom" : field.type);
    formFieldForm.value['typeType'] = !fieldType.value[field.type] ? 'custom' : field.type
    // 初始化枚举值列表
    field.type === 'enum' &&
      (fieldEnumList.value = JSON.parse(JSON.stringify(FieldObject?.values || [])))
    // 初始化约束条件列表
    fieldConstraintsList.value = JSON.parse(
      JSON.stringify(FieldObject?.validation?.constraints || [])
    )
    // 初始化自定义属性列表
    fieldPropertiesList.value = JSON.parse(JSON.stringify(FieldObject?.properties?.values || []))
  } else {
    formFieldForm.value = {}
    // 初始化枚举值列表
    fieldEnumList.value = []
    // 初始化约束条件列表
    fieldConstraintsList.value = []
    // 初始化自定义属性列表
    fieldPropertiesList.value = []
  }
  fieldModelVisible.value = true
}
// 打开字段 某个 配置项 弹窗
const openFieldOptionForm = (option, index, type) => {
  fieldOptionModelVisible.value = true
  fieldOptionType.value = type
  formFieldOptionIndex.value = index
  if (type === 'property') {
    fieldOptionForm.value = option ? JSON.parse(JSON.stringify(option)) : {}
    return (optionModelTitle.value = '属性配置')
  }
  if (type === 'enum') {
    fieldOptionForm.value = option ? JSON.parse(JSON.stringify(option)) : {}
    return (optionModelTitle.value = '枚举值配置')
  }
  fieldOptionForm.value = option ? JSON.parse(JSON.stringify(option)) : {}
  return (optionModelTitle.value = '约束条件配置')
}

// 保存字段 某个 配置项
const saveFieldOption = () => {
  if (formFieldOptionIndex.value === -1) {
    if (fieldOptionType.value === 'property') {
      fieldPropertiesList.value.push(fieldOptionForm.value)
    }
    if (fieldOptionType.value === 'constraint') {
      fieldConstraintsList.value.push(fieldOptionForm.value)
    }
    if (fieldOptionType.value === 'enum') {
      fieldEnumList.value.push(fieldOptionForm.value)
    }
  } else {
    fieldOptionType.value === 'property' &&
      fieldPropertiesList.value.splice(formFieldOptionIndex.value, 1, fieldOptionForm.value)
    fieldOptionType.value === 'constraint' &&
      fieldConstraintsList.value.splice(formFieldOptionIndex.value, 1, fieldOptionForm.value)
    fieldOptionType.value === 'enum' &&
      fieldEnumList.value.splice(formFieldOptionIndex.value, 1, fieldOptionForm.value)
  }
  fieldOptionModelVisible.value = false
  fieldOptionForm.value = {}
}
// 保存字段配置
const saveField = () => {
  const { id, type, label, defaultValue, datePattern } = formFieldForm.value
  const Field = bpmnInstances().moddle.create(`${prefix}:FormField`, { id, type, label })
  defaultValue && (Field.defaultValue = defaultValue)
  datePattern && (Field.datePattern = datePattern)
  // 构建属性
  if (fieldPropertiesList.value && fieldPropertiesList.value.length) {
    const fieldPropertyList = fieldPropertiesList.value.map((fp) => {
      return bpmnInstances().moddle.create(`${prefix}:Property`, {
        id: fp.id,
        value: fp.value
      })
    })
    Field.properties = bpmnInstances().moddle.create(`${prefix}:Properties`, {
      values: fieldPropertyList
    })
  }
  // 构建校验规则
  if (fieldConstraintsList.value && fieldConstraintsList.value.length) {
    const fieldConstraintList = fieldConstraintsList.value.map((fc) => {
      return bpmnInstances().moddle.create(`${prefix}:Constraint`, {
        name: fc.name,
        config: fc.config
      })
    })
    Field.validation = bpmnInstances().moddle.create(`${prefix}:Validation`, {
      constraints: fieldConstraintList
    })
  }
  // 构建枚举值
  if (fieldEnumList.value && fieldEnumList.value.length) {
    Field.values = fieldEnumList.value.map((fe) => {
      return bpmnInstances().moddle.create(`${prefix}:Value`, { name: fe.name, id: fe.id })
    })
  }
  // 更新数组 与 表单配置实例
  if (formFieldIndex.value === -1) {
    fieldList.value.push(formFieldForm.value)
    formData.value.fields.push(Field)
  } else {
    fieldList.value.splice(formFieldIndex.value, 1, formFieldForm.value)
    formData.value.fields.splice(formFieldIndex.value, 1, Field)
  }
  updateElementExtensions()
  fieldModelVisible.value = false
}

// 移除某个 字段的 配置项
const removeFieldOptionItem = (option, index, type) => {
  // console.log(option, 'option')
  if (type === 'property') {
    fieldPropertiesList.value.splice(index, 1)
    return
  }
  if (type === 'enum') {
    fieldEnumList.value.splice(index, 1)
    return
  }
  fieldConstraintsList.value.splice(index, 1)
}
// 移除 字段
const removeField = (field, index) => {
  console.log(field, 'field')
  fieldList.value.splice(index, 1)
  formData.value.fields.splice(index, 1)
  updateElementExtensions()
}

const updateElementExtensions = () => {
  // 更新回扩展元素
  const newElExtensionElements = bpmnInstances().moddle.create(`bpmn:ExtensionElements`, {
    values: otherExtensions.value.concat(formData.value)
  })
  // 更新到元素上
  bpmnInstances().modeling.updateProperties(toRaw(bpmnELement.value), {
    extensionElements: newElExtensionElements
  })
}

const formList = ref([]) // 流程表单的下拉框的数据

// 根据当前节点加载表单配置（每次切换节点都会重新获取，解决组件复用导致 onMounted 不重复执行、表单数据不刷新的问题）
const loadFormConfig = async () => {
  const currentId = props.id
  if (!currentId) return
  const params = {
    processId: modelData.value.key,
    activityId: currentId
  }
  const formConf = await FormApi.getFormConf(params)
  if (currentId !== props.id) return // 快速切换节点时丢弃过期响应，避免旧节点数据覆盖新节点
  if (formConf?.formType) {
    elementFormType.value = formConf.formType
    elementCreatePath.value =
      elementFormType.value === BpmModelFormType.CUSTOM ? formConf?.formUrl : ''
    formKey.value =
      elementFormType.value === BpmModelFormType.NORMAL ? formConf?.formKey : undefined
  }
}

onMounted(async () => {
  formList.value = await FormApi.getFormSimpleList()
  // @ts-ignore
  formKey.value = parseInt(formKey.value)
})

watch(
  () => props.id,
  (val) => {
    val &&
      val.length &&
      nextTick(() => {
        resetFormList()
        loadFormConfig()
      })
  },
  { immediate: true }
)
</script>

<template>
  <div class="tree-container">
    <!-- 树形组件 -->
    <el-tree
      :data="treeData"
      node-key="id"
      default-expandedKeys="[1]"
      @node-click="handleNodeClick"
      :props="{ label: 'name', children: 'children' }"
      @node-contextmenu="handleRightClick"
      :highlight-current="true"
      :default-expand-all="true"
    />
    <!-- 右键菜单 -->
    <el-popover
      ref="popover"
      v-model:visible="contextMenuVisible"
      placement="bottom-start"
      width="100"
      :popper-style="{
        left: `${menuPosition.x}px`,
        top: `${menuPosition.y}px`,
        position: 'absolute'
      }"
    >
      <div>
        <div @click="addNode" class="popover-item">新增节点</div>
        <div @click="editNode" class="popover-item">修改节点</div>
        <div @click="deleteNode" class="popover-item">删除节点</div>
      </div>
    </el-popover>
    <el-dialog :title="title" v-model="dialogVisible">
      <el-form ref="formRef" :model="form" label-width="80px" :rules="rules">
        <el-form-item label="节点" prop="name">
          <el-input v-model="form.name" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="Cancel">取消</el-button>
          <el-button type="primary" @click="Confirm"> 确定 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getSimpleDeptList,
  addDirectory,
  deleteDirectory,
  updateDirectory
} from '@/api/data/docDirectory'

const message = useMessage()
// 树形数据
const treeData = ref<any[]>([])
const formRef = ref<any>(null)
// 新增节点dialog
const dialogVisible = ref(false)
const form = ref<any>({
  name: '',
  remark: ''
})
const rules = ref({
  name: [{ required: true, message: '请输入节点名称', trigger: 'blur' }]
})
const title = ref('新增节点')
const formType = ref('add')

// 节点点击事件
const handleNodeClick = (node: any) => {
  emit('click', node)
}
// 菜单相关
const contextMenuVisible = ref(false)
const menuPosition = ref({ x: 0, y: 0 })
const selectedNode = ref<any>(null)
// 处理右键点击事件
const handleRightClick = (event: MouseEvent, node: any, treeNode: any) => {
  event.preventDefault()
  console.log('右键点击节点:', node, treeNode)

  selectedNode.value = node
  menuPosition.value = { x: event.clientX, y: event.clientY } // 获取鼠标点击的位置
  contextMenuVisible.value = true

  emit('rightClick', node)
}
// 新增节点
const addNode = () => {
  formType.value = 'add'
  title.value = '新增节点'
  contextMenuVisible.value = false
  form.value = {
    name: '',
    remark: ''
  }
  dialogVisible.value = true
}
// 修改节点
const editNode = () => {
  contextMenuVisible.value = false
  title.value = '修改节点'
  formType.value = 'edit'
  if (selectedNode.value) {
    form.value.name = selectedNode.value.name
    // 在这里处理修改数据然后请求方法
    dialogVisible.value = true
  }
}

// 删除节点
const deleteNode = () => {
  contextMenuVisible.value = false
  if (selectedNode.value) {
    ElMessageBox.confirm('确定删除该节点吗?', '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        // 请求删除节点的方法  删除节点
        await deleteDirectory({ id: selectedNode.value.id })
        message.success('删除成功')
        const data = await getSimpleDeptList()
        treeData.value = data
      })
      .catch((error: any) => {
        message.error(error.message)
      })
  }
}

// 取消按钮
const Cancel = () => {
  dialogVisible.value = false
  form.value = {
    name: '',
    remark: ''
  }
}
// 确定按钮
const Confirm = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      dialogVisible.value = false

      const params = {
        name: form.value.name,
        remark: form.value.remark,
        parentId: formType.value === 'add' ? selectedNode.value.id : selectedNode.value.parentId,
        level: formType.value === 'add' ? selectedNode.value.level + 1 : selectedNode.value.level,
        id: formType.value === 'add' ? undefined : selectedNode.value.id
      }
      // 调接口
      if (formType.value === 'add') {
        await addDirectory(params)
      } else {
        await updateDirectory(params)
      }

      message.success(`${title.value}成功！`)
      const data = await getSimpleDeptList()
      treeData.value = data
    } else {
      message.error('请填写必填项！')
    }
  })
}

// 复选框变化事件
// const handleCheckChange = (checked: boolean, node: any) => {
//   console.log('复选框变化:', checked, node)
// }

// 获取树形数据
onMounted(async () => {
  try {
    const data = await getSimpleDeptList()
    treeData.value = data
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
})

const emit = defineEmits<{
  click
  rightClick
}>()
</script>

<style scoped>
/* <!--.tree-container {-->
<!--  padding: 20px;-->
<!--  background-color: #f5f7fa;-->
<!--  border-radius: 8px;-->
<!--}--> */
.popover-item {
  padding: 8px;
  text-align: center;
  cursor: pointer;
  border-bottom: 1px solid #ebeef5;
}
</style>

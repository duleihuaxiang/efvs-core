<template>
  <div class="tree-container">
    <!-- 树形组件 -->
    <el-tree
      :data="treeData"
      show-checkbox
      node-key="id"
      default-expandedKeys="[1]"
    @node-click="handleNodeClick"
    @check-change="handleCheckChange"
    :props="{ label: 'name', children: 'children' }"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSimpleDeptList } from '@/api/data/docDirectory'

// 树形数据
const treeData = ref<any[]>([])

// 节点点击事件
const handleNodeClick = (node: any) => {
  console.log('点击节点:', node)
}

// 复选框变化事件
const handleCheckChange = (checked: boolean, node: any) => {
  console.log('复选框变化:', checked, node)
}

// 获取树形数据
onMounted(async () => {
  try {
    const data = await getSimpleDeptList()
    // 假设返回的数据格式是：
    // [
    //   { id: 1, name: '研发部', children: [...] },
    //   { id: 2, name: '市场部', children: [...] }
    // ]
    treeData.value = data
  } catch (error) {
    console.error('获取部门树失败:', error)
  }
})
</script>

<style scoped>
.tree-container {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
</style>

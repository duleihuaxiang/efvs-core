<template>
  <el-form
    ref="meetingRef"
    :model="meetingData"
    :rules="rules"
    label-width="120px"
    class="mt-20px meetingRef"
    :disabled="meetingDisabled"
  >
    <el-form-item label="会议编号" prop="meeting_number" class="mb-20px">
      <el-input v-model="meetingData.meeting_number" clearable />
    </el-form-item>
    <el-form-item label="会议时间" prop="meeting_time" class="mb-20px">
      <el-time-picker v-model="meetingData.meeting_time" />
    </el-form-item>
    <el-form-item label="会议地点" prop="metting_adress" class="mb-20px">
      <el-input v-model="meetingData.metting_adress" clearable />
    </el-form-item>

    <el-form-item label="会议人员" prop="meeting_participants" class="mb-20px">
      <el-input v-model="meetingData.meeting_participants" clearable />
    </el-form-item>
    <el-form-item label="客户" prop="customer" class="mb-20px">
      <el-input v-model="meetingData.customer" clearable />
    </el-form-item>

    <el-form-item label="我方" prop="our_side" class="mb-20px">
      <el-input v-model="meetingData.our_side" clearable />
    </el-form-item>
    <el-form-item label="会议概述" prop="content_overview" class="mb-20px">
      <el-input v-model="meetingData.content_overview" clearable />
    </el-form-item>
    <el-form-item label="议题" prop="topic" class="mb-20px">
      <div v-for="(item, index) in meetingData.topic" :key="index" class="form-item-group">
        <span>议题{{ index + 1 }}</span>
        <el-form-item
          label="客户意见"
          class="mb-20px"
          :prop="'topic.' + index + '.customerOpinion'"
          :rules="{ required: true, message: '客户意见不能为空', trigger: 'blur' }"
          style="margin-bottom: 20px"
        >
          <el-input v-model="item.customerOpinion" />
        </el-form-item>
        <el-form-item
          label="我方意见"
          :prop="'topic.' + index + '.ourOpinion'"
          :rules="{ required: true, message: '我方意见不能为空', trigger: 'blur' }"
          class="mb-20px"
        >
          <el-input v-model="item.ourOpinion" />
        </el-form-item>
        <div class="button-group-bottom-right">
          <el-button type="primary" round size="small" @click="addItem(index)">+</el-button>
          <el-button
            type="danger"
            round
            size="small"
            @click="removeItem(index)"
            :disabled="meetingData.topic.length <= 1"
            >-</el-button
          >
        </div>
      </div>
      <!-- <el-button :icon="Plus" circle color="#626aef" v-else @click="clickTopic1" /> -->
    </el-form-item>
    <!-- <el-form-item label="议题2" prop="topic2" class="mb-20px">
      <div class="form-item-group">
        <el-form-item
          label="客户意见"
          :prop="'topic2.' + 'customerOpinion'"
          :rules="{ required: true, message: '客户意见不能为空', trigger: 'blur' }"
          class="mb-20px"
          style="margin-bottom: 20px"
        >
          <el-input v-model="meetingData.topic2.customerOpinion" />
        </el-form-item>
        <el-form-item
          label="我方意见"
          :prop="'topic2.' + 'ourOpinion'"
          :rules="{ required: true, message: '我方意见不能为空', trigger: 'blur' }"
          class="mb-20px"
        >
          <el-input v-model="meetingData.topic2.ourOpinion" />
        </el-form-item>
      </div>
    </el-form-item> -->
    <el-form-item label="会议成果" prop="meeting_outcomes" class="mb-20px">
      <el-input v-model="meetingData.meeting_outcomes" clearable />
    </el-form-item>

    <el-form-item label="表格表单" prop="" class="mb-20px">
      <el-table
        border
        ref="tableBox"
        :data="meetingData.tableData"
        :row-style="{ height: '70px' }"
        :cell-style="{ padding: '0px' }"
      >
        <el-table-column type="index" min-width="25" align="center" />
        <el-table-column prop="topic" label="议题" min-width="70" align="center">
          <template #default="scope">
            <el-form-item
              :prop="'tableData.' + scope.$index + '.topic'"
              :rules="{ required: true, message: '议题不能为空', trigger: 'blur' }"
            >
              <el-input
                v-model="scope.row.topic"
                maxlength="200"
                oninput="if(value.length > 200) value=value.slice(0, 200)"
                placeholder="请输入"
              />
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column prop="action_project" label="行动项目" min-width="70" align="center">
          <template #default="scope">
            <el-form-item
              :prop="'tableData.' + scope.$index + '.action_project'"
              :rules="{ required: true, message: '行动项目不能为空', trigger: 'blur' }"
            >
              <el-input
                v-model="scope.row.action_project"
                maxlength="200"
                oninput="if(value.length > 200) value=value.slice(0, 200)"
                placeholder="请输入"
              />
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column prop="scenicArea" label="负责人" min-width="70" align="center">
          <template #default="scope">
            <el-form-item
              :prop="'tableData.' + scope.$index + '.person_charge'"
              :rules="{ required: true, message: '负责人不能为空', trigger: 'blur' }"
            >
              <el-input
                v-model="scope.row.person_charge"
                maxlength="200"
                oninput="if(value.length > 200) value=value.slice(0, 200)"
                placeholder="请输入"
              />
            </el-form-item>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="50" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="addRow(scope.$index)">+</el-button>
            <el-button
              type="danger"
              size="small"
              @click="removeRow(scope.$index)"
              :disabled="meetingData.tableData.length <= 1"
            >
              -
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-form-item>

    <el-form-item label="状态" prop="status" class="mb-20px" v-if="false">
      <el-input v-model="meetingData.status" />
    </el-form-item>
    <el-form-item>
      <!-- <el-button :disabled="formLoading" type="primary" @click="submitForm"> 确 定 </el-button> -->
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import emitter from '@/hooks/web/useEmitt'

const props = defineProps({
  /* formData: {
    type: Object,
    required: false,
    default: () => ({ status: 'quickResponse' })
  }, */
  meetingDisabled: { type: Boolean, default: false, required: true }
})

// 添加新项topic
const addItem = (index: number) => {
  meetingData.value.topic.splice(index + 1, 0, {
    customerOpinion: '',
    ourOpinion: ''
  })
  // meetingData.value.topic = formData.value
}
// 删除项topic1
const removeItem = (index: number) => {
  if (meetingData.value.topic.length > 1) {
    meetingData.value.topic.splice(index, 1)
  }
  // meetingData.value.topic = formData.value
}

// 添加新行
const addRow = (index: number) => {
  const newRow = {
    topic: '', //议题
    person_charge: '', //负责人
    action_project: '' //行动项目
  }
  meetingData.value.tableData.splice(index + 1, 0, newRow)
}

// 删除指定行
const removeRow = (index: number) => {
  if (meetingData.value.tableData.length > 1) {
    meetingData.value.tableData.splice(index, 1)
  }
}

const meetingData = ref({
  meeting_number: '',
  meeting_time: '',
  metting_adress: '',
  meeting_participants: '',
  customer: '',
  our_side: '',
  content_overview: '',
  topic: [{ customerOpinion: '', ourOpinion: '' }],
  meeting_outcomes: '',
  tableData: [
    {
      topic: '', //议题
      person_charge: '', //负责人
      action_project: '' //行动项目
    }
  ],
  formType: 'quickResponse'
})
const meetingRef = ref()

const rules = {
  meeting_number: [{ required: true, message: '会议编号不能为空', trigger: 'blur' }],
  meeting_time: [{ required: true, message: '会议时间不能为空', trigger: 'blur' }],
  metting_adress: [{ required: true, message: '会议地点不能为空', trigger: 'blur' }],
  meeting_participants: [{ required: true, message: '会议人员不能为空', trigger: 'blur' }],
  customer: [{ required: true, message: '客户不能为空', trigger: 'blur' }],
  our_side: [{ required: true, message: '我方不能为空', trigger: 'blur' }],
  content_overview: [{ required: true, message: '会议概述不能为空', trigger: 'blur' }],
  topic: [{ required: true, message: '议题不能为空', trigger: 'blur' }],
  // topic2: [{ required: true, message: '议题2不能为空', trigger: 'blur' }],
  meeting_outcomes: [{ required: true, message: '会议成果不能为空', trigger: 'blur' }]
  // customerProblemDescription: [{ required: true, message: '表格表单不能为空', trigger: 'blur' }]
}

/** 表单校验 */
const validateMeeting = async () => {
  await meetingRef.value.validate() // 校验基础字段
}
/** 表单验证方法 - 供外部调用（如审批通过时） */
const validate = async (): Promise<boolean> => {
  if (!meetingRef.value) {
    return true
  }
  try {
    await meetingRef.value.validate()
    return true
  } catch {
    return false
  }
}

emitter.emit('goStudy', meetingData.value)
defineExpose({
  formFlag: 'meeting',
  formData: meetingData,
  validate
})
</script>

<style lang="scss" scoped>
.bg-gray-100 {
  background-color: #f5f7fa;
  transition: all 0.3s;

  &:hover {
    background-color: #e6e8eb;
  }

  .ep-close {
    font-size: 14px;
    color: #909399;
    transition: color 0.3s;

    &:hover {
      color: #f56c6c;
    }
  }
}

.form-item-group {
  position: relative; /* 添加相对定位 */
  width: 100%;
  padding: 20px;
  margin-bottom: 10px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
}

.button-group-bottom-right {
  position: absolute; /* 绝对定位 */
  right: 8px; /* 距离右侧的距离 */
  bottom: -10px; /* 距离顶部的距离 */
  display: flex;
  gap: 8px;
}

.button-group-bottom-right .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

.dynamic-item {
  position: relative;
  padding: 20px;
  margin-bottom: 10px;
  border: 1px dashed #dcdfe6;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.button-group .el-button {
  padding: 4px 8px;
  font-size: 12px;
}
</style>

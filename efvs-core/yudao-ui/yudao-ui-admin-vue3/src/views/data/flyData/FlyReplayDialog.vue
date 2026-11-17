<template>
  <!-- 不用 ElDialog：避免 Teleport + FocusTrap + 过渡与高频图表更新并发 patch 导致 __vnode -->
  <Teleport to="body">
    <Transition name="fly-replay-fade" @after-leave="onShellAfterLeave">
      <div
        v-if="shellVisible"
        ref="rootEl"
        class="fly-replay-root"
        role="dialog"
        aria-modal="true"
        tabindex="-1"
        @keydown.esc.prevent="handleClose"
      >
        <div class="fly-replay-mask" @click.self="handleClose"></div>
        <div class="fly-replay-shell" @click.stop>
          <div class="fly-replay-header">
            <span class="fly-replay-title">{{ dialogTitle }}</span>
            <Icon
              class="is-hover cursor-pointer fly-replay-close"
              icon="ep:close"
              color="var(--el-color-info)"
              hover-color="var(--el-color-primary)"
              @click="handleClose"
            />
          </div>
          <div class="fly-replay-body">
            <FlyDataIndex
              v-if="contentActive && replayId != null"
              :key="replayId"
              :qar-id="replayId"
              embedded
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { watch, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import FlyDataIndex from './flyDataIndex.vue'

defineOptions({ name: 'FlyReplayDialog' })

const dialogTitle = ref('飞行复现')
const replayId = ref<number | null>(null)
/** 为 true 时挂载飞行复现页（先 true，关窗动画结束后再 false） */
const contentActive = ref(false)
/** 控制 Teleport 外壳显隐（带动画） */
const shellVisible = ref(false)
const rootEl = ref<HTMLElement | null>(null)

let bodyPrevOverflow = ''

watch(shellVisible, (visible) => {
  if (visible) {
    bodyPrevOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = bodyPrevOverflow
  }
})

const onShellAfterLeave = () => {
  contentActive.value = false
}

const handleClose = () => {
  shellVisible.value = false
}

const open = (id: number, title?: string) => {
  const n = Number(id)
  if (!Number.isFinite(n)) {
    ElMessage.warning('QAR 记录 ID 无效')
    return
  }
  replayId.value = n
  dialogTitle.value = title ?? `飞行复现 · 架次 ${n}`
  contentActive.value = true
  shellVisible.value = true
  nextTick(() => {
    rootEl.value?.focus()
  })
}

defineExpose({ open })

onBeforeUnmount(() => {
  document.body.style.overflow = bodyPrevOverflow
})
</script>

<style scoped>
.fly-replay-root {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: stretch;
  justify-content: center;
  outline: none;
}

.fly-replay-mask {
  position: absolute;
  inset: 0;
  background: rgb(0 0 0 / 50%);
}

.fly-replay-shell {
  position: relative;
  z-index: 1;
  display: flex;
  max-width: 100%;
  min-height: 0;
  margin: 8px;
  overflow: hidden;
  background: #0b0f1a;
  border: 1px solid #23263a;
  border-radius: 8px;
  box-shadow: 0 16px 64px rgb(0 0 0 / 45%);
  flex: 1;
  flex-direction: column;
}

.fly-replay-header {
  display: flex;
  height: 48px;
  padding: 0 14px;
  background: #14172a;
  border-bottom: 1px solid #23263a;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
}

.fly-replay-title {
  font-size: 15px;
  font-weight: 600;
  color: #e2e8f0;
}

.fly-replay-close {
  font-size: 20px;
}

.fly-replay-body {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
}
</style>

<style>
.fly-replay-fade-enter-active,
.fly-replay-fade-leave-active {
  transition: opacity 0.18s ease;
}

.fly-replay-fade-enter-from,
.fly-replay-fade-leave-to {
  opacity: 0;
}
</style>

<template>
  <!-- 原生 select：避免 el-select 在父组件高频更新（WebSocket + updateChart）时 patch 竞态报 __vnode -->
  <select
    class="speed-select-native"
    :class="{ 'is-disabled': disabled }"
    :value="String(localSpeed)"
    :disabled="disabled"
    :aria-label="'传输倍速'"
    @change="onNativeChange"
  >
    <option v-for="opt in options" :key="opt.value" :value="String(opt.value)">
      {{ opt.label }}
    </option>
  </select>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const options = [
  { label: '0.25倍速', value: 0.25 },
  { label: '0.5倍速', value: 0.5 },
  { label: '1倍速', value: 1 },
  { label: '2倍速', value: 2 },
  { label: '5倍速', value: 5 },
  { label: '10倍速', value: 10 }
] as const

const props = withDefaults(
  defineProps<{
    modelValue?: number
    disabled?: boolean
  }>(),
  { modelValue: 1, disabled: false }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
  (e: 'change', value: number): void
}>()

const localSpeed = ref(props.modelValue)

watch(
  () => props.modelValue,
  (v) => {
    localSpeed.value = v
  }
)

const onNativeChange = (e: Event) => {
  const el = e.target as HTMLSelectElement
  const v = Number(el.value)
  if (!Number.isFinite(v)) return
  localSpeed.value = v
  emit('update:modelValue', v)
  emit('change', v)
}
</script>

<style scoped>
.speed-select-native {
  width: 90px;
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
  color: #e2e8f0;
  cursor: pointer;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 4px;
  outline: none;
}

.speed-select-native:hover:not(.is-disabled) {
  border-color: #64748b;
}

.speed-select-native:focus {
  border-color: var(--el-color-primary, #409eff);
}

.speed-select-native.is-disabled {
  cursor: not-allowed;
  opacity: 0.55;
}
</style>

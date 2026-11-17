<script lang="ts" setup>
import { ElMessageBox } from 'element-plus'

import avatarImg from '@/assets/imgs/avatar.gif'
import { useDesign } from '@/hooks/web/useDesign'
import { useTagsViewStore } from '@/store/modules/tagsView'
import { useUserStore } from '@/store/modules/user'
import LockDialog from './components/LockDialog.vue'
import LockPage from './components/LockPage.vue'
import { useLockStore } from '@/store/modules/lock'

defineOptions({ name: 'UserInfo' })

const { t } = useI18n()

const { push, replace } = useRouter()

const userStore = useUserStore()

const tagsViewStore = useTagsViewStore()

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('user-info')

const avatar = computed(() => userStore.user.avatar || avatarImg)
const userName = computed(() => userStore.user.nickname ?? 'Admin')

// 锁定屏幕
const lockStore = useLockStore()
const getIsLock = computed(() => lockStore.getLockInfo?.isLock ?? false)
const dialogVisible = ref<boolean>(false)
const lockScreen = () => {
  dialogVisible.value = true
}

// ========== 自动锁屏功能 ==========
const AUTO_LOCK_TIME = 5 * 60 * 1000 // （5分钟）
let lastActivityTime = ref(Date.now())
let autoLockTimer: NodeJS.Timeout | null = null

// 更新最后活动时间
const updateLastActivity = () => {
  lastActivityTime.value = Date.now()
}

// 检查是否需要自动锁屏
const checkAutoLock = () => {
  // 如果已经锁定，不需要再检查
  if (getIsLock.value) return

  const idleTime = Date.now() - lastActivityTime.value
  if (idleTime >= AUTO_LOCK_TIME) {
    // 自动锁屏
    lockStore.setLockInfo({
      isLock: true
    })
  }
}

// 启动自动锁屏定时器
const startAutoLockTimer = () => {
  // 每10秒检查一次
  autoLockTimer = setInterval(checkAutoLock, 10000)
}

// 停止自动锁屏定时器
const stopAutoLockTimer = () => {
  if (autoLockTimer) {
    clearInterval(autoLockTimer)
    autoLockTimer = null
  }
}

// 监听用户活动事件
const activityEvents = ['mousedown', 'mousemove', 'keydown', 'click', 'scroll', 'touchstart']

onMounted(() => {
  // 添加活动事件监听
  activityEvents.forEach((event) => {
    document.addEventListener(event, updateLastActivity, true)
  })

  // 启动自动锁屏定时器
  startAutoLockTimer()
})

onUnmounted(() => {
  // 移除事件监听
  activityEvents.forEach((event) => {
    document.removeEventListener(event, updateLastActivity, true)
  })

  // 停止定时器
  stopAutoLockTimer()
})

const loginOut = async () => {
  try {
    await ElMessageBox.confirm(t('common.loginOutMessage'), t('common.reminder'), {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await userStore.loginOut()
    tagsViewStore.delAllViews()
    replace('/login?redirect=/index')
  } catch {}
}
const toProfile = async () => {
  push('/user/profile')
}
/* const toDocument = () => {
  window.open('https://doc.iocoder.cn/')
} */
</script>

<template>
  <ElDropdown class="custom-hover" :class="prefixCls" trigger="click">
    <div class="flex items-center">
      <ElAvatar :src="avatar" alt="" class="w-[calc(var(--logo-height)-25px)] rounded-[50%]" />
      <span class="pl-[5px] text-14px text-[var(--top-header-text-color)] <lg:hidden">
        {{ userName }}
      </span>
    </div>
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem>
          <Icon icon="ep:tools" />
          <div @click="toProfile">{{ t('common.profile') }}</div>
        </ElDropdownItem>
        <!--   <ElDropdownItem>
          <Icon icon="ep:menu" />
          <div @click="toDocument">{{ t('common.document') }}</div>
        </ElDropdownItem> -->
        <ElDropdownItem divided>
          <Icon icon="ep:lock" />
          <div @click="lockScreen">{{ t('lock.lockScreen') }}</div>
        </ElDropdownItem>
        <ElDropdownItem divided @click="loginOut">
          <Icon icon="ep:switch-button" />
          <div>{{ t('common.loginOut') }}</div>
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>

  <LockDialog v-if="dialogVisible" v-model="dialogVisible" />

  <teleport to="body">
    <transition name="fade-bottom" mode="out-in">
      <LockPage v-if="getIsLock" />
    </transition>
  </teleport>
</template>

<style scoped lang="scss">
.fade-bottom-enter-active,
.fade-bottom-leave-active {
  transition:
    opacity 0.25s,
    transform 0.3s;
}

.fade-bottom-enter-from {
  opacity: 0;
  transform: translateY(-10%);
}

.fade-bottom-leave-to {
  opacity: 0;
  transform: translateY(10%);
}
</style>

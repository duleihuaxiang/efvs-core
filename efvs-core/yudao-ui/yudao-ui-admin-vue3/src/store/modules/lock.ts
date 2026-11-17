import { defineStore } from 'pinia'
import { store } from '@/store'
import { login } from '@/api/login'
import type { UserLoginVO } from '@/api/login/types'
import { useUserStore } from './user'

interface lockInfo {
  isLock?: boolean
  password?: string
}

interface LockState {
  lockInfo: lockInfo
}

export const useLockStore = defineStore('lock', {
  state: (): LockState => {
    return {
      lockInfo: {
        // isLock: false, // 是否锁定屏幕
        // password: '' // 锁屏密码
      }
    }
  },
  getters: {
    getLockInfo(): lockInfo {
      return this.lockInfo
    }
  },
  actions: {
    setLockInfo(lockInfo: lockInfo) {
      this.lockInfo = lockInfo
    },
    resetLockInfo() {
      this.lockInfo = {}
    },
    async unLock(password: string) {
      // 如果设置了锁屏密码，使用锁屏密码验证
      if (this.lockInfo?.password) {
        if (this.lockInfo.password === password) {
          this.resetLockInfo()
          return true
        } else {
          return false
        }
      } else {
        // 自动锁屏，使用登录密码验证
        try {
          const userStore = useUserStore()
          const userInfo = userStore.getUser

          // 构建登录参数
          const loginParams: UserLoginVO = {
            username: userInfo.username || 'admin', // 使用正确的 username 字段
            password: password,
            captchaVerification: ''
          }

          // 调用登录接口验证密码
          const res = await login(loginParams)
          if (res) {
            this.resetLockInfo()
            return true
          } else {
            return false
          }
        } catch (error) {
          return false
        }
      }
    }
  },
  persist: true
})

export const useLockStoreWithOut = () => {
  return useLockStore(store)
}

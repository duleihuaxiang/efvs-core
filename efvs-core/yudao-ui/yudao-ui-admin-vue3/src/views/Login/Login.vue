<template>
  <div class="login">
    <!-- 右上角的主题、语言选择 -->
    <!--  <div
            class="flex items-center justify-between at-2xl:justify-end at-xl:justify-end"
            style="color: var(--el-text-color-primary)"
          >
            <div class="flex items-center justify-end space-x-10px h-48px">
              <ThemeSwitch />
              <LocaleDropdown />
            </div>
          </div> -->
    <!-- 右边的登录界面 -->

    <!-- 账号登录 -->
    <LoginForm class="m-auto h-auto p-20px lt-xl:(rounded-3xl light:bg-white)" />
    <!-- 手机登录 -->
    <MobileForm class="m-auto h-auto p-20px lt-xl:(rounded-3xl light:bg-white)" />
    <!-- 二维码登录 -->
    <QrCodeForm class="m-auto h-auto p-20px lt-xl:(rounded-3xl light:bg-white)" />
    <!-- 注册 -->
    <RegisterForm class="m-auto h-auto p-20px lt-xl:(rounded-3xl light:bg-white)" />
    <!-- 三方登录 -->
    <!-- <SSOLoginVue class="m-auto h-auto p-20px lt-xl:(rounded-3xl light:bg-white)" /> -->
    <!-- 忘记密码 -->
    <ForgetPasswordForm class="m-auto h-auto p-20px lt-xl:(rounded-3xl light:bg-white)" />
  </div>
</template>
<script lang="ts" setup>
import { underlineToHump } from '@/utils'

import { useDesign } from '@/hooks/web/useDesign'
import { useAppStore } from '@/store/modules/app'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import { LocaleDropdown } from '@/layout/components/LocaleDropdown'

import {
  LoginForm,
  MobileForm,
  QrCodeForm,
  RegisterForm,
  ForgetPasswordForm
} from './components'

defineOptions({ name: 'Login' })

const { t } = useI18n()
const appStore = useAppStore()
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  overflow: auto;

  &__left {
    &::before {
      position: absolute;
      top: 0;
      left: 0;
      z-index: -1;
      width: 100%;
      height: 100%;
      background-image: url('@/assets/svgs/login-bg.svg');
      background-position: center;
      background-repeat: no-repeat;
      content: '';
    }
  }
}
</style>

<style lang="scss">
.dark .login-form {
  .el-divider__text {
    background-color: var(--login-bg-color);
  }

  .el-card {
    background-color: var(--login-bg-color);
  }
}

.login {
  position: relative;
  display: flex;
  height: 100%;
  background-image: url('@/assets/imgs/bg.png');
  background-size: cover;
  justify-content: center;
  align-items: center;
}
</style>

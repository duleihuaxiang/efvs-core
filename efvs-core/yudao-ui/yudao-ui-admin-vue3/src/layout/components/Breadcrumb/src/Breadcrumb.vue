<script lang="tsx">
import { ElBreadcrumb, ElBreadcrumbItem } from 'element-plus'
import { ref, watch, computed, unref, defineComponent, TransitionGroup } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissionStore } from '@/store/modules/permission'
import { filterBreadcrumb } from './helper'
import type { RouteLocationNormalizedLoaded, RouteMeta } from 'vue-router'

import { Icon } from '@/components/Icon'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { useTagsViewStore } from '@/store/modules/tagsView'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('breadcrumb')

const appStore = useAppStore()

// 面包屑图标
const breadcrumbIcon = computed(() => appStore.getBreadcrumbIcon)

export default defineComponent({
  name: 'Breadcrumb',
  setup() {
    const { currentRoute } = useRouter()

    const levelList = ref<AppRouteRecordRaw[]>([])

    const permissionStore = usePermissionStore()
    const tagsViewStore = useTagsViewStore()

    const menuRouters = computed(() => {
      const routers = permissionStore.getRouters
      return filterBreadcrumb(routers)
    })

    // 查找路由在菜单树中的完整路径（包含所有父级）
    const findRouteChain = (
      routers: AppRouteRecordRaw[],
      targetPath: string
    ): AppRouteRecordRaw[] => {
      for (let i = 0; i < routers.length; i++) {
        const router = routers[i]
        // 复制路由对象，避免修改原数组
        const copyRouter = JSON.parse(JSON.stringify(router))
        // 清除子级，避免显示多余的路径
        delete copyRouter.children

        if (copyRouter.path === targetPath) {
          return [copyRouter]
        }
        if (router.children && router.children.length > 0) {
          const chain = findRouteChain(router.children, targetPath)
          if (chain.length > 0) {
            return [copyRouter, ...chain]
          }
        }
      }
      return []
    }

    const getBreadcrumb = () => {
      const currentRouteMeta = currentRoute.value.meta

      // 如果当前路由设置了 activeMenu 属性，使用该属性作为父级路由
      if (currentRouteMeta.activeMenu) {
        // 查找 activeMenu 对应的完整路由链
        const parentRouteChain = findRouteChain(unref(menuRouters), currentRouteMeta.activeMenu)

        // 构建面包屑结构
        if (parentRouteChain.length > 0) {
          // 直接从当前路由信息创建子路由
          const currentItem: any = {
            path: currentRoute.value.path,
            name: currentRoute.value.name || 'unknown',
            meta: {
              ...currentRoute.value.meta,
              title: currentRoute.value.meta.title // 先使用默认标题
            }
          }

          // 检查 tagsView 中是否有动态设置的标题
          const tagsView = tagsViewStore.getVisitedViews.find(
            (v) => v.path === currentRoute.value.path
          )
          if (tagsView && tagsView.meta.title && tagsView.meta.title !== currentItem.meta.title) {
            currentItem.meta.title = tagsView.meta.title
          }

          // 将当前路由添加到面包屑链的末尾
          levelList.value = [...parentRouteChain, currentItem]
        } else {
          // 如果找不到 activeMenu 对应的路由，只显示当前路由
          const currentItem: any = {
            path: currentRoute.value.path,
            name: currentRoute.value.name || 'unknown',
            meta: {
              ...currentRoute.value.meta,
              title: currentRoute.value.meta.title
            }
          }

          // 检查 tagsView 中是否有动态设置的标题
          const tagsView = tagsViewStore.getVisitedViews.find(
            (v) => v.path === currentRoute.value.path
          )
          if (tagsView && tagsView.meta.title && tagsView.meta.title !== currentItem.meta.title) {
            currentItem.meta.title = tagsView.meta.title
          }

          levelList.value = [currentItem]
        }
      } else {
        // 普通情况，查找当前路径对应的完整路由链
        const currentPath = currentRoute.value.matched.slice(-1)[0].path
        const currentRouteChain = findRouteChain(unref(menuRouters), currentPath)

        if (currentRouteChain.length > 0) {
          const breadcrumbChain = JSON.parse(JSON.stringify(currentRouteChain))

          // 检查并更新链中最后一个路由的标题（动态标题）
          const lastRoute = breadcrumbChain[breadcrumbChain.length - 1]
          const tagsView = tagsViewStore.getVisitedViews.find((v) => v.path === currentPath)
          if (tagsView && tagsView.meta.title && tagsView.meta.title !== lastRoute.meta.title) {
            lastRoute.meta.title = tagsView.meta.title
          }

          levelList.value = breadcrumbChain
        }
      }
    }

    const renderBreadcrumb = () => {
      // 直接使用 levelList 数组构建面包屑导航，只显示当前路径的层级
      return unref(levelList).map((v, index) => {
        const isLastItem = index === unref(levelList).length - 1
        const disabled = isLastItem || !v.redirect || v.redirect === 'noredirect'
        const meta = v.meta as RouteMeta

        // 构建跳转路由信息
        let to: any = ''
        if (!disabled) {
          // 对于非当前页面，需要正确构建跳转路由
          // 检查当前路由是否与面包屑项路径匹配
          const isCurrentPathMatch = currentRoute.value.path.startsWith(v.path)

          // 如果是当前路由的祖先路径，需要保留参数信息
          if (isCurrentPathMatch && index < unref(levelList).length - 1) {
            // 对于祖先路径，直接使用路径跳转
            to = { path: v.path }
          } else {
            // 对于其他路径，使用完整的路由信息
            to = {
              path: v.path,
              query: v.path === currentRoute.value.path ? currentRoute.value.query : {},
              params: v.path === currentRoute.value.path ? currentRoute.value.params : {}
            }
          }
        }

        return (
          <ElBreadcrumbItem to={to} key={v.name}>
            {meta?.icon && breadcrumbIcon.value ? (
              <div class="flex items-center">
                <Icon icon={meta.icon} class="mr-[2px]" svgClass="inline-block"></Icon>
                {meta.title}
              </div>
            ) : (
              meta.title
            )}
          </ElBreadcrumbItem>
        )
      })
    }

    watch(
      () => currentRoute.value,
      (route: RouteLocationNormalizedLoaded) => {
        if (route.path.startsWith('/redirect/')) {
          return
        }
        getBreadcrumb()
      },
      {
        immediate: true
      }
    )

    return () => (
      <ElBreadcrumb separator="/" class={`${prefixCls} flex items-center h-full ml-[10px]`}>
        <TransitionGroup appear enter-active-class="animate__animated animate__fadeInRight">
          {renderBreadcrumb()}
        </TransitionGroup>
      </ElBreadcrumb>
    )
  }
})
</script>

<style lang="scss" scoped>
$prefix-cls: #{$elNamespace}-breadcrumb;

.#{$prefix-cls} {
  :deep(.#{$prefix-cls}__item) {
    display: flex;
    .#{$prefix-cls}__inner {
      display: flex;
      align-items: center;
      color: var(--top-header-text-color);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  :deep(.#{$prefix-cls}__item):not(:last-child) {
    .#{$prefix-cls}__inner {
      color: var(--top-header-text-color);

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  :deep(.#{$prefix-cls}__item):last-child {
    .#{$prefix-cls}__inner {
      display: flex;
      align-items: center;
      color: var(--el-text-color-placeholder);

      &:hover {
        color: var(--el-text-color-placeholder);
      }
    }
  }
}
</style>

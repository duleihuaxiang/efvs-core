import type { RouteMeta, RouteLocationNormalizedLoaded } from 'vue-router'
import { pathResolve } from '@/utils/routerHelper'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

export const filterAffixTags = (routes: AppRouteRecordRaw[], parentPath = '') => {
  let tags: RouteLocationNormalizedLoaded[] = []
  routes.forEach((route) => {
    const meta = route.meta as RouteMeta
    const tagPath = pathResolve(parentPath, route.path)
    if (meta?.affix) {
      // 创建一个新的路由对象
      const tagRoute = { ...route, path: tagPath, fullPath: tagPath } as RouteLocationNormalizedLoaded
      // 如果 meta.title 已经是翻译后的文本（不包含 t() 语法），则直接使用
      // 如果是需要翻译的 key（如 'router.home'），则进行翻译
      if (typeof meta.title === 'string' && (meta.title.includes('router.') || meta.title.includes('common.'))) {
        tagRoute.meta.title = t(meta.title as string)
      }
      tags.push(tagRoute)
    }
    if (route.children) {
      const tempTags: RouteLocationNormalizedLoaded[] = filterAffixTags(route.children, tagPath)
      if (tempTags.length >= 1) {
        tags = [...tags, ...tempTags]
      }
    }
  })

  return tags
}

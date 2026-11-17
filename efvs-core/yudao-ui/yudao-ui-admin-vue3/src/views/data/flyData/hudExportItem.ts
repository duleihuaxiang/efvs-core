export interface HudExportDataItem {
  airspeed?: unknown
  selectedSpeed?: unknown
  pitchAngle?: unknown
  magneticHeading?: unknown
  radioAltitude?: unknown
  locDeviation?: unknown
  glideslopeDeviationRes?: unknown
  ground_speed_km?: unknown
}

const HUD_VALUE_KEYS: Array<keyof HudExportDataItem> = [
  'airspeed',
  'selectedSpeed',
  'pitchAngle',
  'magneticHeading',
  'radioAltitude',
  'locDeviation',
  'glideslopeDeviationRes',
  'ground_speed_km'
]

const isFiniteHudValue = (value: unknown): boolean => {
  if (value === null || value === undefined || value === '') return false
  const parsed = typeof value === 'number' ? value : Number.parseFloat(String(value))
  return Number.isFinite(parsed)
}

export const scoreHudDataItem = (item?: HudExportDataItem | null): number => {
  if (!item) return 0
  return HUD_VALUE_KEYS.reduce((score, key) => score + (isFiniteHudValue(item[key]) ? 1 : 0), 0)
}

/**
 * HUD 导出优先保持当前播放帧；当前帧核心参数过少时，选择距离当前帧最近且字段最完整的帧。
 * 仅返回原数组中的数据，不修改播放索引。
 */
export const selectHudExportItem = <T extends HudExportDataItem>(
  items: T[],
  currentIndex: number,
  minimumCurrentScore = 4
): T | null => {
  if (!items.length) return null
  const safeIndex = Math.max(0, Math.min(Math.round(currentIndex), items.length - 1))
  const current = items[safeIndex]
  if (scoreHudDataItem(current) >= minimumCurrentScore) return current

  let bestItem: T | null = null
  let bestScore = 0
  let bestDistance = Number.POSITIVE_INFINITY
  items.forEach((item, index) => {
    const score = scoreHudDataItem(item)
    const distance = Math.abs(index - safeIndex)
    if (score > bestScore || (score === bestScore && score > 0 && distance < bestDistance)) {
      bestItem = item
      bestScore = score
      bestDistance = distance
    }
  })
  return bestScore > 0 ? bestItem : null
}

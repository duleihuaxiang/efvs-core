import { AlignmentType, Document, HeadingLevel, ImageRun, Packer, Paragraph, TextRun } from 'docx'

export type FlightReportPhase = 'TAKEOFF_ROLL' | 'CLIMB' | 'APPROACH' | 'TAXI'
export type FlightReportImageKey =
  | 'loc'
  | 'gs'
  | 'airspeed'
  | 'magneticHeading'
  | 'pitchAngle'
  | 'slope'

export interface FlightReportPhaseData {
  capturedAt: string
  images: Partial<Record<FlightReportImageKey, string>>
}

export interface FlightReportCycleData {
  cycle: number
  phases: Partial<Record<FlightReportPhase, FlightReportPhaseData>>
}

export interface CreateFlightReportDocxOptions {
  title: string
  exportedAt: string
  hudImage: string
  cycles: FlightReportCycleData[]
  fileDate?: Date
}

export interface FlightReportDocxResult {
  blob: Blob
  fileName: string
}

const DOCX_MIME = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
const PNG_PREFIX = 'data:image/png;base64,'
const PNG_SIGNATURE = [137, 80, 78, 71, 13, 10, 26, 10]

const PHASE_ORDER: FlightReportPhase[] = ['TAKEOFF_ROLL', 'CLIMB', 'APPROACH', 'TAXI']
const PHASE_LABELS: Record<FlightReportPhase, string> = {
  TAKEOFF_ROLL: '滑跑',
  CLIMB: '爬升',
  APPROACH: '进近',
  TAXI: '滑行'
}
const IMAGE_ORDER: Array<{ key: FlightReportImageKey; label: string }> = [
  { key: 'loc', label: 'LOC 航向道偏差' },
  { key: 'gs', label: 'GS 下滑道偏差' },
  { key: 'airspeed', label: '空速 / 选择速度' },
  { key: 'magneticHeading', label: '磁航向' },
  { key: 'pitchAngle', label: '仰角' },
  { key: 'slope', label: '坡度' }
]

export function pngDataUrlToUint8Array(dataUrl: string): Uint8Array {
  if (!dataUrl.startsWith(PNG_PREFIX)) {
    throw new Error('图片必须是 PNG Data URL')
  }
  const base64 = dataUrl.slice(PNG_PREFIX.length)
  if (!base64 || base64.length % 4 !== 0 || !/^[A-Za-z0-9+/]+={0,2}$/.test(base64)) {
    throw new Error('PNG Data URL 的 Base64 内容无效')
  }

  let binary: string
  try {
    binary = atob(base64)
  } catch (error) {
    throw new Error('PNG Data URL 解码失败', { cause: error })
  }
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0))
  const isPng = PNG_SIGNATURE.every((value, index) => bytes[index] === value)
  if (!isPng) {
    throw new Error('图片内容不是有效的 PNG')
  }
  return bytes
}

export function createFlightReportFileName(date = new Date()): string {
  return `飞行复现_按飞行状态导出_${date.toISOString().replace(/[:.]/g, '-')}.docx`
}

const createImageParagraph = (dataUrl: string, width: number, height: number): Paragraph =>
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 240 },
    children: [
      new ImageRun({
        type: 'png',
        data: pngDataUrlToUint8Array(dataUrl),
        transformation: { width, height }
      })
    ]
  })

const createHeading = (
  text: string,
  level:
    | typeof HeadingLevel.HEADING_1
    | typeof HeadingLevel.HEADING_2
    | typeof HeadingLevel.HEADING_3
) =>
  new Paragraph({
    text,
    heading: level,
    spacing: { before: 180, after: 120 }
  })

export async function createFlightReportDocx(
  options: CreateFlightReportDocxOptions
): Promise<FlightReportDocxResult> {
  const children: Paragraph[] = [
    new Paragraph({
      alignment: AlignmentType.CENTER,
      heading: HeadingLevel.TITLE,
      spacing: { after: 240 },
      children: [new TextRun({ text: options.title, bold: true, size: 36 })]
    }),
    new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { after: 280 },
      children: [new TextRun({ text: `导出时间：${options.exportedAt}`, color: '666666' })]
    }),
    createHeading('HUD 飞行数据显示', HeadingLevel.HEADING_1),
    createImageParagraph(options.hudImage, 620, 522)
  ]

  for (const cycle of options.cycles) {
    children.push(createHeading(`第${cycle.cycle}次滑跑`, HeadingLevel.HEADING_1))
    for (const phase of PHASE_ORDER) {
      const phaseData = cycle.phases[phase]
      if (!phaseData) continue
      children.push(
        createHeading(`${PHASE_LABELS[phase]}（${phase}）`, HeadingLevel.HEADING_2),
        new Paragraph({
          spacing: { after: 120 },
          children: [
            new TextRun({ text: `图表快照时间：${phaseData.capturedAt}`, color: '666666' })
          ]
        })
      )
      for (const image of IMAGE_ORDER) {
        if (image.key === 'gs' && phase !== 'APPROACH') continue
        const dataUrl = phaseData.images[image.key]
        if (!dataUrl) continue
        children.push(
          createHeading(image.label, HeadingLevel.HEADING_3),
          createImageParagraph(dataUrl, 620, 329)
        )
      }
    }
  }

  const document = new Document({
    creator: 'EFVS 飞行复现',
    title: options.title,
    description: '飞行复现 HUD 与分阶段飞参曲线报告',
    sections: [{ children }]
  })
  const packed = await Packer.toBlob(document)
  const blob =
    packed.type === DOCX_MIME ? packed : new Blob([await packed.arrayBuffer()], { type: DOCX_MIME })

  return {
    blob,
    fileName: createFlightReportFileName(options.fileDate)
  }
}

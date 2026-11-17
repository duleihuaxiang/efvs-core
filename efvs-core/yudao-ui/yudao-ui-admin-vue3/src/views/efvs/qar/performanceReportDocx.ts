import { AlignmentType, Document, HeadingLevel, ImageRun, Packer, Paragraph, TextRun } from 'docx'

export interface PerformanceReportImage {
  title: string
  dataUrl: string
}

export interface CreatePerformanceReportOptions {
  qarIds: number[]
  flightPhaseLabel: string
  totalCount: number
  exportedAt: string
  images: PerformanceReportImage[]
  fileDate?: Date
}

export interface PerformanceReportDocxResult {
  blob: Blob
  fileName: string
}

const DOCX_MIME = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
const PNG_PREFIX = 'data:image/png;base64,'
const PNG_SIGNATURE = [137, 80, 78, 71, 13, 10, 26, 10]

export function pngDataUrlToUint8Array(dataUrl: string): Uint8Array {
  if (!dataUrl.startsWith(PNG_PREFIX)) {
    throw new Error('图表图片必须是 PNG Data URL')
  }
  const base64 = dataUrl.slice(PNG_PREFIX.length)
  if (!base64 || base64.length % 4 !== 0 || !/^[A-Za-z0-9+/]+={0,2}$/.test(base64)) {
    throw new Error('图表 PNG 的 Base64 内容无效')
  }

  let binary: string
  try {
    binary = atob(base64)
  } catch (error) {
    throw new Error('图表 PNG 解码失败', { cause: error })
  }
  const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0))
  if (!PNG_SIGNATURE.every((value, index) => bytes[index] === value)) {
    throw new Error('图表图片内容不是有效的 PNG')
  }
  return bytes
}

const pad = (value: number): string => String(value).padStart(2, '0')

export function createPerformanceReportFileName(qarIds: number[], date = new Date()): string {
  const timestamp = `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}-${pad(
    date.getHours()
  )}${pad(date.getMinutes())}${pad(date.getSeconds())}`
  const sortieIds = qarIds.length > 0 ? qarIds.join('-') : '未知'
  return `飞行性能分析_架次${sortieIds}_${timestamp}.docx`
}

const createHeading = (text: string, level: typeof HeadingLevel.HEADING_2): Paragraph =>
  new Paragraph({
    text,
    heading: level,
    spacing: { before: 240, after: 120 }
  })

const createChartImage = (dataUrl: string): Paragraph =>
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { after: 240 },
    children: [
      new ImageRun({
        type: 'png',
        data: pngDataUrlToUint8Array(dataUrl),
        transformation: { width: 620, height: 329 }
      })
    ]
  })

export async function createPerformanceReportDocx(
  options: CreatePerformanceReportOptions
): Promise<PerformanceReportDocxResult> {
  if (!options.qarIds.length) throw new Error('缺少要导出的架次 ID')
  if (!options.images.length) throw new Error('没有可导出的图表图片')

  const children: Paragraph[] = [
    new Paragraph({
      alignment: AlignmentType.CENTER,
      heading: HeadingLevel.TITLE,
      spacing: { after: 240 },
      children: [new TextRun({ text: '飞行性能分析报告', bold: true, size: 36 })]
    }),
    new Paragraph({
      spacing: { after: 100 },
      children: [new TextRun({ text: `架次：${options.qarIds.join('、')}`, bold: true })]
    }),
    new Paragraph({
      spacing: { after: 100 },
      children: [new TextRun(`当前阶段：${options.flightPhaseLabel || '-'}`)]
    }),
    new Paragraph({
      spacing: { after: 100 },
      children: [new TextRun(`已接收：${options.totalCount} 条`)]
    }),
    new Paragraph({
      spacing: { after: 240 },
      children: [new TextRun({ text: `导出时间：${options.exportedAt}`, color: '666666' })]
    })
  ]

  options.images.forEach((image) => {
    children.push(
      createHeading(image.title, HeadingLevel.HEADING_2),
      createChartImage(image.dataUrl)
    )
  })

  const document = new Document({
    creator: 'EFVS 飞行性能分析',
    title: '飞行性能分析报告',
    description: '飞行性能分析 LOC 与 GS 图表报告',
    sections: [{ children }]
  })
  const packed = await Packer.toBlob(document)
  const blob =
    packed.type === DOCX_MIME ? packed : new Blob([await packed.arrayBuffer()], { type: DOCX_MIME })

  return {
    blob,
    fileName: createPerformanceReportFileName(options.qarIds, options.fileDate)
  }
}

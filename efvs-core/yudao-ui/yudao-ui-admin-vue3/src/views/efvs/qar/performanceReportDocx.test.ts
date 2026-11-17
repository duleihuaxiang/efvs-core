import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import {
  createPerformanceReportDocx,
  createPerformanceReportFileName,
  pngDataUrlToUint8Array
} from './performanceReportDocx'

const ONE_PIXEL_PNG =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='

describe('flight performance report DOCX', () => {
  it('decodes PNG data URLs and rejects invalid image content', () => {
    assert.deepEqual(
      Array.from(pngDataUrlToUint8Array(ONE_PIXEL_PNG).slice(0, 8)),
      [137, 80, 78, 71, 13, 10, 26, 10]
    )
    assert.throws(() => pngDataUrlToUint8Array('data:image/jpeg;base64,AA=='))
    assert.throws(() => pngDataUrlToUint8Array('data:image/png;base64,'))
    assert.throws(() => pngDataUrlToUint8Array('data:image/png;base64,%%%'))
  })

  it('creates a real OOXML report with a .docx filename', async () => {
    const { blob, fileName } = await createPerformanceReportDocx({
      qarIds: [21, 20],
      flightPhaseLabel: '滑跑、爬升、进近、滑行',
      totalCount: 500,
      exportedAt: '2026-10-12 14:30:00',
      fileDate: new Date('2026-10-12T06:30:00.123Z'),
      images: [
        { title: 'LOC 航向道偏差（滑跑）', dataUrl: ONE_PIXEL_PNG },
        { title: 'GS 下滑道偏差', dataUrl: ONE_PIXEL_PNG }
      ]
    })
    const signature = new Uint8Array(await blob.slice(0, 2).arrayBuffer())

    assert.deepEqual(Array.from(signature), [0x50, 0x4b])
    assert.equal(
      blob.type,
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    )
    assert.equal(fileName, '飞行性能分析_架次21-20_20261012-143000.docx')
    assert.equal(fileName.endsWith('.doc'), false)
  })

  it('rejects a report without chart images', async () => {
    await assert.rejects(() =>
      createPerformanceReportDocx({
        qarIds: [21],
        flightPhaseLabel: '-',
        totalCount: 0,
        exportedAt: '2026-10-12 14:30:00',
        images: []
      })
    )
  })

  it('creates a filesystem-safe filename', () => {
    assert.equal(
      createPerformanceReportFileName([21, 20], new Date('2026-10-12T06:30:00.123Z')),
      '飞行性能分析_架次21-20_20261012-143000.docx'
    )
  })
})

import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import {
  createFlightReportDocx,
  createFlightReportFileName,
  pngDataUrlToUint8Array
} from './flightReportDocx'

const ONE_PIXEL_PNG =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='

describe('pngDataUrlToUint8Array', () => {
  it('decodes a PNG data URL', () => {
    const bytes = pngDataUrlToUint8Array(ONE_PIXEL_PNG)

    assert.deepEqual(Array.from(bytes.slice(0, 8)), [137, 80, 78, 71, 13, 10, 26, 10])
  })

  for (const [label, value] of [
    ['non-PNG media type', 'data:image/jpeg;base64,AA=='],
    ['empty payload', 'data:image/png;base64,'],
    ['invalid base64', 'data:image/png;base64,%%%']
  ]) {
    it(`rejects ${label}`, () => {
      assert.throws(() => pngDataUrlToUint8Array(value))
    })
  }
})

describe('flight report DOCX', () => {
  it('creates a real OOXML blob and a .docx filename', async () => {
    const { blob, fileName } = await createFlightReportDocx({
      title: '飞行复现测试报告',
      exportedAt: '2026-10-10 12:30:45',
      hudImage: ONE_PIXEL_PNG,
      cycles: [
        {
          cycle: 1,
          phases: {
            TAKEOFF_ROLL: {
              capturedAt: '2026-10-10 12:29:00',
              images: { loc: ONE_PIXEL_PNG }
            }
          }
        }
      ]
    })
    const signature = Array.from(new Uint8Array(await blob.slice(0, 4).arrayBuffer()))

    assert.equal(
      blob.type,
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    )
    assert.deepEqual(signature.slice(0, 2), [0x50, 0x4b])
    assert.match(fileName, /^飞行复现_按飞行状态导出_.*\.docx$/)
    assert.equal(fileName.endsWith('.doc'), false)
  })

  it('creates a filesystem-safe DOCX filename', () => {
    assert.equal(
      createFlightReportFileName(new Date('2026-10-10T12:30:45.123Z')),
      '飞行复现_按飞行状态导出_2026-10-10T12-30-45-123Z.docx'
    )
  })
})

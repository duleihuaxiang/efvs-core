import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import { scoreHudDataItem, selectHudExportItem } from './hudExportItem'

describe('selectHudExportItem', () => {
  it('keeps the current frame when it contains enough HUD values', () => {
    const current = {
      airspeed: '120',
      pitchAngle: '2.5',
      magneticHeading: '338',
      radioAltitude: '800'
    }
    assert.equal(selectHudExportItem([{ airspeed: '' }, current], 1), current)
  })

  it('falls back to the closest most complete frame when the current frame is empty', () => {
    const complete = {
      airspeed: '140',
      selectedSpeed: '145',
      pitchAngle: '3',
      magneticHeading: '338',
      radioAltitude: '900',
      locDeviation: '1.5'
    }
    assert.equal(
      selectHudExportItem([{ airspeed: '' }, complete, { airspeed: '100' }], 0),
      complete
    )
  })

  it('returns null when no frame contains a finite HUD value', () => {
    assert.equal(selectHudExportItem([{ airspeed: '' }, { radioAltitude: 'NULL' }], 0), null)
    assert.equal(scoreHudDataItem({ magneticHeading: 'not-a-number' }), 0)
  })
})

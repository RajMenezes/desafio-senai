import { describe, expect, it } from 'vitest'
import { buySchema } from './buy-schema'

const NOW = new Date('2026-11-02T10:15:00-03:00')
const schema = buySchema(() => NOW)

function messagesOf(input: { clubId: string; date: string; time: string }) {
  const result = schema.safeParse(input)
  return result.success ? [] : result.error.issues.map((issue) => issue.message)
}

describe('buySchema', () => {
  it('monta o corpo da compra com a data no formato da RN01', () => {
    expect(schema.parse({ clubId: '1', date: '2027-03-02', time: '10:15' })).toEqual({
      club_id: 1,
      loyalty_until: '2027-03-02T10:15-03:00',
    })
  })

  it('exige o clube, a data e a hora', () => {
    expect(messagesOf({ clubId: '', date: '', time: '' })).toEqual([
      'Escolha o clube comprador.',
      'Informe a data.',
      'Informe a hora.',
    ])
  })

  it('exige um momento depois de agora (horário de Brasília)', () => {
    expect(messagesOf({ clubId: '1', date: '2026-11-02', time: '10:15' })).toEqual([
      'Escolha um momento depois de agora.',
    ])
    expect(messagesOf({ clubId: '1', date: '2026-11-02', time: '10:16' })).toEqual([])
  })

  it('aceita até 1.095 dias depois de agora', () => {
    expect(messagesOf({ clubId: '1', date: '2029-11-01', time: '10:15' })).toEqual([])
    expect(messagesOf({ clubId: '1', date: '2029-11-01', time: '10:16' })).toEqual([
      'A fidelidade vai até 1.095 dias a partir de agora.',
    ])
  })
})

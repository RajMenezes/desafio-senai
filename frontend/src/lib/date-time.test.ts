import { describe, expect, it } from 'vitest'
import { formatDateTime, toApiDateTime } from './date-time'

describe('formatDateTime', () => {
  it('mostra a data e a hora da API no horário de Brasília', () => {
    expect(formatDateTime('2026-11-07T10:30:00-03:00')).toBe('07/11/2026 10:30')
  })

  it('converte instantes em outro fuso para o horário de Brasília', () => {
    expect(formatDateTime('2026-11-08T01:15:00Z')).toBe('07/11/2026 22:15')
  })

  it('usa 00 para a meia-noite', () => {
    expect(formatDateTime('2027-03-02T00:05:00-03:00')).toBe('02/03/2027 00:05')
  })
})

describe('toApiDateTime', () => {
  it('monta a data e a hora no formato da RN01', () => {
    expect(toApiDateTime('2027-03-02', '10:15')).toBe('2027-03-02T10:15-03:00')
  })
})

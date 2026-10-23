import { describe, expect, it } from 'vitest'
import { formatInteger, formatMoney, formatSignedInteger, formatSignedMoney } from './format'

describe('formatMoney', () => {
  it.each([
    [1744.14, '1.744,14'],
    [37, '37,00'],
    [0, '0,00'],
    [48.1, '48,10'],
    [-45.55, '-45,55'],
  ])('formata %d como %s', (value, expected) => {
    expect(formatMoney(value)).toBe(expected)
  })
})

describe('formatSignedMoney', () => {
  it.each([
    [35.7, '+35,70'],
    [-45.55, '-45,55'],
    [0, '0,00'],
  ])('formata %d como %s', (value, expected) => {
    expect(formatSignedMoney(value)).toBe(expected)
  })
})

describe('formatInteger', () => {
  it('usa ponto como separador de milhar', () => {
    expect(formatInteger(1089)).toBe('1.089')
  })
})

describe('formatSignedInteger', () => {
  it.each([
    [2, '+2'],
    [-2, '-2'],
    [0, '0'],
  ])('formata %d como %s', (value, expected) => {
    expect(formatSignedInteger(value)).toBe(expected)
  })
})

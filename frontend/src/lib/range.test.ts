import { describe, expect, it } from 'vitest'
import { formatMoney } from './format'
import { describeRange, validateRange, type RangeRules } from './range'

const ageRules: RangeRules = { limits: { min: 16, max: 45 }, allowDecimals: false, format: String }
const valueRules: RangeRules = {
  limits: { min: 0, max: 550 },
  allowDecimals: true,
  format: formatMoney,
}

describe('validateRange', () => {
  it('aceita os dois limites, só um ou nenhum', () => {
    expect(validateRange('18', '30', ageRules)).toEqual({ range: { min: 18, max: 30 } })
    expect(validateRange('', '30', ageRules)).toEqual({ range: { min: undefined, max: 30 } })
    expect(validateRange(' ', '', ageRules)).toEqual({ range: { min: undefined, max: undefined } })
  })

  it('aceita vírgula ou ponto com até 2 casas no valor', () => {
    expect(validateRange('10,5', '60.25', valueRules)).toEqual({ range: { min: 10.5, max: 60.25 } })
  })

  it('recusa formatos inválidos', () => {
    expect(validateRange('18.5', '', ageRules)).toEqual({
      errors: { min: 'Use um número inteiro.', max: undefined },
    })
    expect(validateRange('', '10,555', valueRules)).toEqual({
      errors: { min: undefined, max: 'Use um número com até 2 casas decimais.' },
    })
  })

  it('recusa valores fora da faixa aceita', () => {
    expect(validateRange('15', '', ageRules)).toEqual({
      errors: { min: 'Use um valor de 16 a 45.', max: undefined },
    })
    expect(validateRange('', '551', valueRules)).toEqual({
      errors: { min: undefined, max: 'Use um valor de 0,00 a 550,00.' },
    })
  })

  it('recusa mínimo maior que o máximo', () => {
    expect(validateRange('30', '18', ageRules)).toEqual({
      errors: { max: 'O máximo não pode ser menor que o mínimo.' },
    })
  })
})

describe('describeRange', () => {
  it.each([
    [{ min: 18, max: 30 }, '18 a 30'],
    [{ min: 18 }, 'a partir de 18'],
    [{ max: 30 }, 'até 30'],
    [{}, undefined],
  ])('descreve %o como %s', (range, expected) => {
    expect(describeRange(range, String)).toBe(expected)
  })

  it('formata os limites com a função informada', () => {
    expect(describeRange({ min: 10, max: 60 }, formatMoney)).toBe('10,00 a 60,00')
  })
})

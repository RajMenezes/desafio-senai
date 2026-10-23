import { describe, expect, it } from 'vitest'
import { createPlayerSchema, updatePlayerSchema, type CreatePlayerInput } from './player-schemas'

const validPlayer: CreatePlayerInput = {
  name: 'Meia Lua',
  position: 'MEI',
  age: '25',
  agility: '85',
  pace: '70',
  shooting: '68',
  passing: '82',
  dribbling: '79',
  defending: '55',
  physical: '64',
  potential: '86',
}

function errorsOf(input: CreatePlayerInput) {
  const result = createPlayerSchema.safeParse(input)
  if (result.success) return {}
  return Object.fromEntries(
    result.error.issues.map((issue) => [issue.path.join('.'), issue.message]),
  )
}

describe('createPlayerSchema', () => {
  it('converte o formulário no corpo do POST /players', () => {
    expect(createPlayerSchema.parse({ ...validPlayer, name: '  Meia   Lua ' })).toEqual({
      name: 'Meia Lua',
      position: 'MEI',
      age: 25,
      agility: 85,
      pace: 70,
      shooting: 68,
      passing: 82,
      dribbling: 79,
      defending: 55,
      physical: 64,
      potential: 86,
    })
  })

  it.each([
    ['Da', 'Use de 3 a 100 caracteres.'],
    ['Davi', 'Informe pelo menos duas palavras.'],
    ['Davi Melo 2', 'O nome não pode ter números.'],
    ['   ', 'Informe o nome.'],
  ])('recusa o nome %j', (name, message) => {
    expect(errorsOf({ ...validPlayer, name }).name).toBe(message)
  })

  it('valida as faixas de cada campo', () => {
    expect(errorsOf({ ...validPlayer, age: '15', passing: '120', potential: '' })).toEqual({
      age: 'Use uma idade de 16 a 45 anos.',
      passing: 'Use um valor de 1 a 99.',
      potential: 'Informe o potencial.',
    })
  })

  it('recusa números com casas decimais', () => {
    expect(errorsOf({ ...validPlayer, agility: '50.5' }).agility).toBe('Use um número inteiro.')
  })

  it('limita a agilidade a 85 com 35 anos ou mais', () => {
    expect(errorsOf({ ...validPlayer, age: '35', agility: '86', potential: '82' }).agility).toBe(
      'Com 35 anos ou mais, a agilidade vai até 85.',
    )
  })

  it('limita a soma dos seis atributos a 480', () => {
    const strong = { pace: '90', shooting: '90', passing: '90', dribbling: '90', defending: '90' }
    expect(errorsOf({ ...validPlayer, ...strong, physical: '31', potential: '90' }).root).toBe(
      'A soma dos seis atributos é 481 e não pode passar de 480.',
    )
  })

  it('exige potencial maior ou igual ao maior atributo', () => {
    expect(errorsOf({ ...validPlayer, potential: '81' }).potential).toBe(
      'Use pelo menos o maior atributo (82).',
    )
  })

  it('exige potencial igual ao maior atributo com 33 anos ou mais', () => {
    expect(errorsOf({ ...validPlayer, age: '33' }).potential).toBe(
      'Com 33 anos ou mais, use o maior atributo (82).',
    )
    expect(errorsOf({ ...validPlayer, age: '33', potential: '82' })).toEqual({})
  })
})

describe('updatePlayerSchema', () => {
  const schema = updatePlayerSchema(30)

  it('aceita a mesma idade ou maior', () => {
    expect(
      schema.parse({ name: 'Danilo Freitas', position: 'ZAG', age: '31', agility: '25' }),
    ).toEqual({ name: 'Danilo Freitas', position: 'ZAG', age: 31, agility: 25 })
  })

  it('não deixa a idade diminuir', () => {
    const result = schema.safeParse({
      name: 'Danilo Freitas',
      position: 'ZAG',
      age: '29',
      agility: '25',
    })

    expect(result.success).toBe(false)
    expect(result.error?.issues[0].message).toBe('A idade não pode diminuir (atual: 30).')
  })
})

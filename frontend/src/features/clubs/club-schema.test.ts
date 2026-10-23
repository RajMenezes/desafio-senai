import { describe, expect, it } from 'vitest'
import { acronymMatchesName, createClubSchema } from './club-schema'

function errorsOf(name: string, acronym: string) {
  const result = createClubSchema.safeParse({ name, acronym })
  if (result.success) return {}
  return Object.fromEntries(
    result.error.issues.map((issue) => [issue.path.join('.'), issue.message]),
  )
}

describe('acronymMatchesName', () => {
  it.each([
    ['AGU', 'Águias FC'],
    ['LDS', 'Leões da Serra'],
    ['TUB', 'Tubarões EC'],
    ['EPS', 'Épsilon EC'],
  ])('aceita %s para %s', (acronym, name) => {
    expect(acronymMatchesName(acronym, name)).toBe(true)
  })

  it.each([
    ['GAU', 'Águias FC', 'não começa pela primeira letra'],
    ['AUG', 'Águias FC', 'letras fora de ordem'],
    ['AGX', 'Águias FC', 'letra que não está no nome'],
  ])('recusa %s para %s (%s)', (acronym, name) => {
    expect(acronymMatchesName(acronym, name)).toBe(false)
  })
})

describe('createClubSchema', () => {
  it('normaliza os espaços do nome', () => {
    expect(createClubSchema.parse({ name: '  Leões   da Serra ', acronym: 'LDS' })).toEqual({
      name: 'Leões da Serra',
      acronym: 'LDS',
    })
  })

  it.each([
    ['', 'Informe o nome.'],
    ['FC', 'Use de 3 a 60 caracteres.'],
    ['Clube 10', 'Use só letras, espaços, hífen e ponto.'],
    ['Flamengo', 'Use de 2 a 5 palavras.'],
    ['Um Dois Três Quatro Cinco Seis', 'Use de 2 a 5 palavras.'],
  ])('recusa o nome %j', (name, message) => {
    expect(errorsOf(name, 'AAB').name).toBe(message)
  })

  it.each([
    ['', 'Informe a sigla.'],
    ['ag', 'Use 3 letras de A a Z, maiúsculas.'],
    ['AÁG', 'Use 3 letras de A a Z, maiúsculas.'],
    ['AAA', 'As três letras não podem ser iguais.'],
    ['AUG', 'Use letras do nome, nessa ordem, começando pela primeira letra.'],
  ])('recusa a sigla %j', (acronym, message) => {
    expect(errorsOf('Águias FC', acronym).acronym).toBe(message)
  })

  it('aceita nome com hífen e ponto', () => {
    expect(errorsOf('Sport Club Bom-Fim Jr.', 'SCB')).toEqual({})
  })
})

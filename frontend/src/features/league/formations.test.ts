import { describe, expect, it } from 'vitest'
import { player } from '@/features/players/test-fixtures'
import { assignToSlots, candidatesFor, describeFormation, slotsFor } from './formations'

const goalkeeper = player({ id: 1, position: 'GOL', condition: 'APTO' })
const centreBack = player({ id: 2, position: 'ZAG', condition: 'APTO' })
const fullBack = player({ id: 3, position: 'LAT', condition: 'MAL_CONDICIONADO' })
const midfielder = player({ id: 4, position: 'MEI', condition: 'APTO' })
const striker = player({ id: 5, position: 'ATA', condition: 'APTO' })
const sickStriker = player({ id: 6, position: 'ATA', condition: 'DOENTE' })

describe('slotsFor', () => {
  it.each([
    ['3-1', ['goal', 'defense', 'defense', 'defense', 'attack']],
    ['2-2', ['goal', 'defense', 'defense', 'attack', 'attack']],
    ['2-1-1', ['goal', 'defense', 'defense', 'midfield', 'attack']],
    ['1-2-1', ['goal', 'defense', 'midfield', 'midfield', 'attack']],
  ] as const)('monta as 5 vagas da formação %s, do goleiro ao ataque', (formation, sectors) => {
    expect(slotsFor(formation).map((slot) => slot.sector)).toEqual(sectors)
  })
})

describe('describeFormation', () => {
  it('descreve os setores com jogadores', () => {
    expect(describeFormation('2-1-1')).toBe('Defesa 2, meio 1, ataque 1')
    expect(describeFormation('3-1')).toBe('Defesa 3, ataque 1')
  })
})

describe('assignToSlots', () => {
  it('coloca cada titular salvo na vaga da posição dele', () => {
    const starters = [goalkeeper, centreBack, fullBack, midfielder, striker]

    expect(assignToSlots(slotsFor('2-1-1'), starters)).toEqual([1, 2, 3, 4, 5])
  })

  it('deixa vazias as vagas que a troca de formação não consegue preencher', () => {
    const starters = [goalkeeper, centreBack, fullBack, midfielder, striker]

    expect(assignToSlots(slotsFor('2-2'), starters)).toEqual([1, 2, 3, 5, undefined])
  })
})

describe('candidatesFor', () => {
  const squad = [goalkeeper, centreBack, fullBack, midfielder, striker, sickStriker]
  const slots = slotsFor('2-1-1')

  it('oferece só a posição da vaga, sem DOENTE', () => {
    const attackIndex = 4
    expect(candidatesFor(slots[attackIndex], attackIndex, squad, [])).toEqual([striker])
  })

  it('não repete quem já está em outra vaga', () => {
    const chosen = [1, 2, undefined, undefined, undefined]

    expect(candidatesFor(slots[2], 2, squad, chosen)).toEqual([fullBack])
    expect(candidatesFor(slots[1], 1, squad, chosen)).toEqual([centreBack, fullBack])
  })
})

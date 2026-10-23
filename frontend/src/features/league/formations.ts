import type { Player, Position } from '@/features/players/types'
import type { Formation } from './types'

/** Jogadores de linha por setor, de trás para frente, sem o goleiro (RN19). */
const SECTORS: Record<Formation, { defense: number; midfield: number; attack: number }> = {
  '3-1': { defense: 3, midfield: 0, attack: 1 },
  '2-2': { defense: 2, midfield: 0, attack: 2 },
  '2-1-1': { defense: 2, midfield: 1, attack: 1 },
  '1-2-1': { defense: 1, midfield: 2, attack: 1 },
}

export const DEFAULT_FORMATION: Formation = '2-1-1'

export const STARTERS_COUNT = 5

export type SlotSector = 'goal' | 'defense' | 'midfield' | 'attack'

export interface LineupSlot {
  sector: SlotSector
  label: string
  positionsLabel: string
  positions: Position[]
}

const SLOT_TEMPLATES: Record<SlotSector, Omit<LineupSlot, 'sector'>> = {
  goal: { label: 'Goleiro', positionsLabel: 'GOL', positions: ['GOL'] },
  defense: { label: 'Defesa', positionsLabel: 'ZAG ou LAT', positions: ['ZAG', 'LAT'] },
  midfield: { label: 'Meio', positionsLabel: 'MEI', positions: ['MEI'] },
  attack: { label: 'Ataque', positionsLabel: 'ATA', positions: ['ATA'] },
}

function repeatSlot(sector: SlotSector, count: number): LineupSlot[] {
  return Array.from({ length: count }, () => ({ sector, ...SLOT_TEMPLATES[sector] }))
}

/** Vagas da formação, do goleiro ao ataque. */
export function slotsFor(formation: Formation): LineupSlot[] {
  const { defense, midfield, attack } = SECTORS[formation]
  return [
    ...repeatSlot('goal', 1),
    ...repeatSlot('defense', defense),
    ...repeatSlot('midfield', midfield),
    ...repeatSlot('attack', attack),
  ]
}

/** Descrição curta da formação: "Defesa 2, meio 1, ataque 1". */
export function describeFormation(formation: Formation): string {
  const { defense, midfield, attack } = SECTORS[formation]
  return [`Defesa ${defense}`, midfield > 0 ? `meio ${midfield}` : undefined, `ataque ${attack}`]
    .filter(Boolean)
    .join(', ')
}

/**
 * Distribui os jogadores pelas vagas, na ordem recebida: cada vaga fica com o primeiro jogador
 * ainda livre que joga numa posição dela. Vagas sem jogador ficam `undefined`.
 */
export function assignToSlots(slots: LineupSlot[], players: Player[]): (number | undefined)[] {
  const used = new Set<number>()
  return slots.map((slot) => {
    const player = players.find((candidate) => {
      return !used.has(candidate.id) && slot.positions.includes(candidate.position)
    })
    if (!player) return undefined
    used.add(player.id)
    return player.id
  })
}

/** Jogadores do elenco que podem ocupar a vaga: posição certa, sem DOENTE e fora das outras vagas. */
export function candidatesFor(
  slot: LineupSlot,
  slotIndex: number,
  squad: Player[],
  chosen: (number | undefined)[],
): Player[] {
  return squad.filter(
    (player) =>
      slot.positions.includes(player.position) &&
      player.condition !== 'DOENTE' &&
      !chosen.some((id, index) => index !== slotIndex && id === player.id),
  )
}

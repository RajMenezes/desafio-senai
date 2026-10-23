import type { Club } from '@/features/clubs/types'
import type { Page } from '@/lib/api/types'
import type { Player } from './types'

export const clubs: Club[] = [
  { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
]

export function player(changes: Partial<Player> = {}): Player {
  return {
    id: 1,
    name: 'Meia Lua',
    position: 'MEI',
    agility: 85,
    age: 25,
    condition: 'MAL_CONDICIONADO',
    base_value: 40,
    market_value: 37,
    pace: 70,
    shooting: 68,
    passing: 82,
    dribbling: 79,
    defending: 55,
    physical: 64,
    potential: 86,
    overall: 75,
    club_id: null,
    ...changes,
  }
}

export function page(data: Player[], totalItems = data.length): Page<Player> {
  return {
    data,
    meta: { page: 1, limit: 10, total_items: totalItems, total_pages: Math.ceil(totalItems / 10) },
  }
}

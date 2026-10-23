import type { Player } from '@/features/players/types'

export const FORMATIONS = ['3-1', '2-2', '2-1-1', '1-2-1'] as const
export type Formation = (typeof FORMATIONS)[number]

/** Escalação do clube (RN19). Sem escalação: `formation` nula, `players` vazio e incompleta. */
export interface Lineup {
  club_id: number
  formation: Formation | null
  complete: boolean
  players: Player[]
}

export interface SaveLineupRequest {
  formation: Formation
  player_ids: number[]
}

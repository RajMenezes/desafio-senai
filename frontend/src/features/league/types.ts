import type { Condition, Player, Position } from '@/features/players/types'

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

export type MatchResult = 'MANDANTE' | 'VISITANTE' | 'EMPATE'

export const MATCH_RESULT_LABELS: Record<MatchResult, string> = {
  MANDANTE: 'Vitória do mandante',
  VISITANTE: 'Vitória do visitante',
  EMPATE: 'Empate',
}

export interface MatchTeam {
  club_id: number
  club_name: string
  formation: Formation
  goals: number
}

export type DuelAttribute = 'shooting' | 'passing' | 'dribbling' | 'defending'

export const DUEL_ATTRIBUTE_LABELS: Record<DuelAttribute, string> = {
  shooting: 'Finalização',
  passing: 'Passe',
  dribbling: 'Drible',
  defending: 'Defesa',
}

/** Um lado do duelo, com os dados gravados no momento da partida (RN20). */
export interface DuelSide {
  player_id: number
  player_name: string
  position: Position
  condition: Condition
  attribute: DuelAttribute
  value: number
}

export interface Duel {
  order: number
  attacking_club_id: number
  attacker: DuelSide
  defender: DuelSide
  goal: boolean
}

/** Partida (2.4.2). `GET /matches` traz as partidas sem `duels`. */
export interface Match {
  id: number
  played_at: string
  home: MatchTeam
  away: MatchTeam
  result: MatchResult
  duels?: Duel[]
}

export interface PlayMatchRequest {
  home_club_id: number
  away_club_id: number
}

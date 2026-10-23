import type { Player } from '@/features/players/types'

/** Limite de jogadores no elenco de um clube. */
export const SQUAD_LIMIT = 12

/** Limite de clubes na liga (RN10). */
export const CLUB_LIMIT = 20

/** Caixa com que todo clube nasce (RN10). */
export const CLUB_INITIAL_CASH = 300

export interface Club {
  id: number
  name: string
  acronym: string
  cash: number
  squad_value: number
  net_worth: number
}

/** Contrato ativo do jogador, calculado para uma venda agora (RN11, RN15). */
export interface Contract {
  purchase_value: number
  loyalty_until: string
  remaining_days: number
  penalty_rate: number
}

export interface SquadPlayer extends Player {
  contract: Contract
}

export interface ClubDetail extends Club {
  players: SquadPlayer[]
}

export const CLUB_SORTS = [
  { value: 'name', label: 'Nome' },
  { value: 'net_worth_desc', label: 'Patrimônio (maior para o menor)' },
  { value: 'net_worth_asc', label: 'Patrimônio (menor para o maior)' },
] as const
export type ClubSort = (typeof CLUB_SORTS)[number]['value']

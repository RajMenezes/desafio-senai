import { usePlayers } from './api'
import { toTabCountQuery, type PlayerFilters, type PlayerTab } from './player-filters'

/** Total de jogadores de cada aba com os filtros atuais, lido do `meta.total_items` da API. */
export function usePlayerTabCounts(filters: PlayerFilters): Partial<Record<PlayerTab, number>> {
  const canCountFree = filters.clubId === undefined
  const all = usePlayers(toTabCountQuery(filters, 'all'))
  const free = usePlayers(toTabCountQuery(filters, 'free'), canCountFree)
  const inClub = usePlayers(toTabCountQuery(filters, 'in_club'))

  return {
    all: all.data?.meta.total_items,
    free: canCountFree ? free.data?.meta.total_items : undefined,
    in_club: inClub.data?.meta.total_items,
  }
}

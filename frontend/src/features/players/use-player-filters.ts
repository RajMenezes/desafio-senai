import { useCallback, useMemo } from 'react'
import { useSearchParams } from 'react-router'
import {
  EMPTY_PLAYER_FILTERS,
  parsePlayerFilters,
  toSearchParams,
  type PlayerFilters,
} from './player-filters'

/** Filtros da lista de jogadores guardados na URL. Mudar um filtro volta para a página 1. */
export function usePlayerFilters() {
  const [searchParams, setSearchParams] = useSearchParams()
  const filters = useMemo(() => parsePlayerFilters(searchParams), [searchParams])

  const updateFilters = useCallback(
    (changes: Partial<PlayerFilters>) => {
      setSearchParams(toSearchParams({ ...filters, page: 1, ...changes }), { replace: true })
    },
    [filters, setSearchParams],
  )

  const clearFilters = useCallback(() => {
    setSearchParams(
      toSearchParams({ ...EMPTY_PLAYER_FILTERS, tab: filters.tab, sort: filters.sort }),
      { replace: true },
    )
  }, [filters.sort, filters.tab, setSearchParams])

  return { filters, updateFilters, clearFilters }
}

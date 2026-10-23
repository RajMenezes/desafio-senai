import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { request, type QueryParams } from '@/lib/api/client'
import type { Page } from '@/lib/api/types'
import type { Player } from './types'

export const playerKeys = {
  all: ['players'] as const,
  list: (params: QueryParams) => [...playerKeys.all, 'list', params] as const,
}

function fetchPlayers(params: QueryParams) {
  return request<Page<Player>>('/players', { params })
}

export function usePlayers(params: QueryParams, enabled = true) {
  return useQuery({
    queryKey: playerKeys.list(params),
    queryFn: () => fetchPlayers(params),
    placeholderData: keepPreviousData,
    enabled,
  })
}

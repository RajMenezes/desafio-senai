import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clubKeys } from '@/features/clubs/api'
import { request, type QueryParams } from '@/lib/api/client'
import type { Page } from '@/lib/api/types'
import type { CreatePlayerRequest, UpdatePlayerRequest } from './player-schemas'
import type { Condition, Player } from './types'

export const playerKeys = {
  all: ['players'] as const,
  list: (params: QueryParams) => [...playerKeys.all, 'list', params] as const,
  detail: (id: number) => [...playerKeys.all, 'detail', id] as const,
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

export function usePlayer(id: number) {
  return useQuery({
    queryKey: playerKeys.detail(id),
    queryFn: () => request<Player>(`/players/${id}`),
  })
}

/** Depois de mudar um jogador, recarrega as listas e os clubes (valor do elenco e patrimônio). */
export function useInvalidatePlayersAndClubs() {
  const queryClient = useQueryClient()
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: playerKeys.all }),
      queryClient.invalidateQueries({ queryKey: clubKeys.all }),
    ])
}

export function useCreatePlayer() {
  const invalidatePlayers = useInvalidatePlayersAndClubs()
  return useMutation({
    mutationFn: (player: CreatePlayerRequest) =>
      request<Player>('/players', { method: 'POST', body: player }),
    onSuccess: invalidatePlayers,
  })
}

export function useUpdatePlayer(id: number) {
  const invalidatePlayers = useInvalidatePlayersAndClubs()
  return useMutation({
    mutationFn: (player: UpdatePlayerRequest) =>
      request<Player>(`/players/${id}`, { method: 'PUT', body: player }),
    onSuccess: invalidatePlayers,
  })
}

export function useChangeCondition(id: number) {
  const invalidatePlayers = useInvalidatePlayersAndClubs()
  return useMutation({
    mutationFn: (condition: Condition) =>
      request<Player>(`/players/${id}/condition`, { method: 'PATCH', body: { condition } }),
    onSuccess: invalidatePlayers,
  })
}

export function useDeletePlayer(id: number) {
  const invalidatePlayers = useInvalidatePlayersAndClubs()
  return useMutation({
    mutationFn: () => request<void>(`/players/${id}`, { method: 'DELETE' }),
    onSuccess: invalidatePlayers,
  })
}

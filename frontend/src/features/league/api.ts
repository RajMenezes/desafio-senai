import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clubKeys } from '@/features/clubs/api'
import { request } from '@/lib/api/client'
import type { Lineup, Match, PlayMatchRequest, SaveLineupRequest, Standing } from './types'

/** A escalação fica sob a chave dos clubes: venda e condição DOENTE a mudam (RN19). */
const lineupKeys = {
  detail: (clubId: number) => [...clubKeys.all, 'lineup', clubId] as const,
}

export function useLineup(clubId: number, enabled = true) {
  return useQuery({
    queryKey: lineupKeys.detail(clubId),
    queryFn: () => request<Lineup>(`/clubs/${clubId}/lineup`),
    enabled,
  })
}

/** Salva a escalação; em qualquer resultado, recarrega o clube e a escalação (os dados podem ter mudado). */
export function useSaveLineup(clubId: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (lineup: SaveLineupRequest) =>
      request<Lineup>(`/clubs/${clubId}/lineup`, { method: 'PUT', body: lineup }),
    onSettled: () => queryClient.invalidateQueries({ queryKey: clubKeys.all }),
  })
}

const matchKeys = {
  all: ['matches'] as const,
  list: (clubId?: number) => [...matchKeys.all, 'list', clubId ?? 'all'] as const,
  detail: (id: number) => [...matchKeys.all, 'detail', id] as const,
}

/**
 * Classificação (RN21): muda a cada partida e a cada clube criado (que entra zerado).
 * Fica sob a chave dos clubes para que a criação de clube também a recarregue.
 */
const standingsKeys = {
  all: [...clubKeys.all, 'standings'] as const,
}

export function useStandings() {
  return useQuery({
    queryKey: standingsKeys.all,
    queryFn: () => request<Standing[]>('/standings'),
  })
}

/** Partidas da mais recente para a mais antiga; com `clubId`, só as do clube. */
export function useMatches(clubId?: number) {
  return useQuery({
    queryKey: matchKeys.list(clubId),
    queryFn: () => request<Match[]>('/matches', { params: { club_id: clubId } }),
    placeholderData: keepPreviousData,
  })
}

export function useMatch(id: number, enabled = true) {
  return useQuery({
    queryKey: matchKeys.detail(id),
    queryFn: () => request<Match>(`/matches/${id}`),
    enabled,
  })
}

export function usePlayMatch() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (match: PlayMatchRequest) =>
      request<Match>('/matches', { method: 'POST', body: match }),
    onSuccess: (match) => {
      queryClient.setQueryData(matchKeys.detail(match.id), match)
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: matchKeys.all }),
        queryClient.invalidateQueries({ queryKey: standingsKeys.all }),
      ])
    },
  })
}

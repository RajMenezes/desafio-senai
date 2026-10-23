import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clubKeys } from '@/features/clubs/api'
import { request } from '@/lib/api/client'
import type { Lineup, SaveLineupRequest } from './types'

/** A escalação fica sob a chave dos clubes: venda e condição DOENTE a mudam (RN19). */
export const lineupKeys = {
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

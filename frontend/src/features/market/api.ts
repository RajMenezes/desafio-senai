import { useMutation } from '@tanstack/react-query'
import { useInvalidatePlayersAndClubs } from '@/features/players/api'
import { request } from '@/lib/api/client'
import type { BuyPlayerRequest, PurchaseResult, SaleResult } from './types'

export function useBuyPlayer(playerId: number) {
  const invalidatePlayersAndClubs = useInvalidatePlayersAndClubs()
  return useMutation({
    mutationFn: (purchase: BuyPlayerRequest) =>
      request<PurchaseResult>(`/players/${playerId}/buy`, { method: 'POST', body: purchase }),
    onSuccess: invalidatePlayersAndClubs,
  })
}

export function useSellPlayer(playerId: number) {
  const invalidatePlayersAndClubs = useInvalidatePlayersAndClubs()
  return useMutation({
    mutationFn: () => request<SaleResult>(`/players/${playerId}/sell`, { method: 'POST' }),
    onSuccess: invalidatePlayersAndClubs,
  })
}

import { keepPreviousData, useMutation, useQuery } from '@tanstack/react-query'
import { clubKeys } from '@/features/clubs/api'
import { useInvalidatePlayersAndClubs } from '@/features/players/api'
import { request, requestFile } from '@/lib/api/client'
import { toTransactionParams } from './transaction-filters'
import type {
  BuyPlayerRequest,
  PurchaseResult,
  SaleResult,
  TransactionFilters,
  TransactionHistory,
} from './types'

/** O histórico fica sob a chave dos clubes: compras e vendas o recarregam junto com o caixa. */
export const transactionKeys = {
  history: (clubId: number, filters: TransactionFilters) =>
    [...clubKeys.all, 'transactions', clubId, toTransactionParams(filters)] as const,
}

export function useTransactions(clubId: number, filters: TransactionFilters, enabled = true) {
  return useQuery({
    queryKey: transactionKeys.history(clubId, filters),
    queryFn: () =>
      request<TransactionHistory>(`/clubs/${clubId}/transactions`, {
        params: toTransactionParams(filters),
      }),
    placeholderData: keepPreviousData,
    enabled,
  })
}

export function useExportTransactions(clubId: number) {
  return useMutation({
    mutationFn: (filters: TransactionFilters) =>
      requestFile(`/clubs/${clubId}/transactions/export`, {
        params: toTransactionParams(filters),
      }),
  })
}

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

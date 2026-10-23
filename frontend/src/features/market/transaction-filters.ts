import type { QueryParams } from '@/lib/api/client'
import { TRANSACTION_TYPES, type TransactionFilters } from './types'

export const ALL_TRANSACTION_FILTERS: TransactionFilters = {
  types: TRANSACTION_TYPES.map((type) => type.value),
}

/** Query params do histórico e da exportação: com todos os tipos marcados, não filtra por tipo. */
export function toTransactionParams(filters: TransactionFilters): QueryParams {
  const allTypes = filters.types.length === TRANSACTION_TYPES.length
  return {
    type: allTypes ? undefined : filters.types.join(','),
    player_id: filters.playerId,
  }
}

export function hasTransactionFilters(filters: TransactionFilters): boolean {
  return Object.values(toTransactionParams(filters)).some((value) => value !== undefined)
}

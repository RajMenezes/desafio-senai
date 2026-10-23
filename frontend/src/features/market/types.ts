export type TransactionType = 'COMPRA' | 'VENDA'

/** Resposta do `POST /players/{id}/buy` (2.3.1): a transação, o contrato criado e o novo caixa. */
export interface PurchaseResult {
  id: number
  club_id: number
  player_id: number
  player_name: string
  type: TransactionType
  value: number
  fee: number
  penalty: number
  profit: number | null
  cash_change: number
  created_at: string
  contract: {
    purchase_value: number
    loyalty_until: string
    status: string
  }
  club_cash: number
}

export interface BuyPlayerRequest {
  club_id: number
  loyalty_until: string
}

/** Situação do contrato depois da venda (RN15): com multa, rescindido; sem multa, encerrado. */
export type ClosedContractStatus = 'RESCINDIDO' | 'ENCERRADO'

/** Resposta do `POST /players/{id}/sell` (2.3.2). `fee` é a comissão sobre o lucro. */
export interface SaleResult {
  id: number
  club_id: number
  player_id: number
  player_name: string
  type: TransactionType
  value: number
  fee: number
  penalty: number
  profit: number
  cash_change: number
  created_at: string
  purchase_value: number
  remaining_days: number
  penalty_rate: number
  contract_status: ClosedContractStatus
  club_cash: number
}

/** Transação do histórico (RN17): valores e nome gravados no momento da operação. */
export interface Transaction {
  id: number
  club_id: number
  player_id: number
  player_name: string
  type: TransactionType
  value: number
  fee: number
  penalty: number
  profit: number | null
  cash_change: number
  created_at: string
}

export interface TransactionSummary {
  total_spent: number
  total_received: number
  total_fees: number
  total_penalties: number
  total_profit: number
}

export interface TransactionHistory {
  data: Transaction[]
  summary: TransactionSummary
}

export const TRANSACTION_TYPES: { value: TransactionType; label: string }[] = [
  { value: 'COMPRA', label: 'Compra' },
  { value: 'VENDA', label: 'Venda' },
]

/** Filtros do histórico: tipos marcados (todos = sem filtro) e jogador opcional. */
export interface TransactionFilters {
  types: TransactionType[]
  playerId?: number
}

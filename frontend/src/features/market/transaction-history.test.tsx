import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ClubPage } from '@/features/clubs/club-page'
import type { ClubDetail } from '@/features/clubs/types'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import { toTransactionParams } from './transaction-filters'
import type { Transaction, TransactionHistory } from './types'

const club: ClubDetail = {
  id: 1,
  name: 'Águias FC',
  acronym: 'AGU',
  cash: 296.85,
  squad_value: 0,
  net_worth: 296.85,
  players: [],
}

const sale: Transaction = {
  id: 11,
  club_id: 1,
  player_id: 2,
  player_name: 'Meia Lua',
  type: 'VENDA',
  value: 40,
  fee: 0.3,
  penalty: 4,
  profit: 3,
  cash_change: 35.7,
  created_at: '2026-11-05T16:40:00-03:00',
}

const purchase: Transaction = {
  id: 10,
  club_id: 1,
  player_id: 2,
  player_name: 'Meia Lua',
  type: 'COMPRA',
  value: 37,
  fee: 1.85,
  penalty: 0,
  profit: null,
  cash_change: -38.85,
  created_at: '2026-11-02T10:15:00-03:00',
}

const fullHistory: TransactionHistory = {
  data: [sale, purchase],
  summary: {
    total_spent: 38.85,
    total_received: 35.7,
    total_fees: 2.15,
    total_penalties: 4,
    total_profit: 3,
  },
}

const salesOnly: TransactionHistory = {
  data: [sale],
  summary: {
    total_spent: 0,
    total_received: 35.7,
    total_fees: 0.3,
    total_penalties: 4,
    total_profit: 3,
  },
}

type HistoryHandler = (url: URL) => unknown

function stubHistoryApi(
  history: HistoryHandler = (url) => (url.searchParams.has('type') ? salesOnly : fullHistory),
) {
  return stubApi((url) => {
    if (url.pathname.endsWith('/export')) {
      return new Response('data_hora;tipo\n', {
        status: 200,
        headers: {
          'Content-Type': 'text/csv',
          'Content-Disposition': 'attachment; filename="transacoes-clube-1.csv"',
        },
      })
    }
    if (url.pathname.endsWith('/transactions')) return history(url)
    return club
  })
}

function historyRequests(fetchMock: ReturnType<typeof stubApi>, suffix = '/transactions') {
  return fetchMock.mock.calls
    .map(([input]) => new URL(input))
    .filter((url) => url.pathname.endsWith(suffix))
    .map((url) => url.search)
}

async function openHistory() {
  renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/1?aba=historico' })
  return screen.findByRole('region', { name: 'Histórico de transações' })
}

beforeEach(() => {
  URL.createObjectURL = vi.fn(() => 'blob:csv')
  URL.revokeObjectURL = vi.fn()
  vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
})

afterEach(() => {
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})

describe('toTransactionParams', () => {
  it('não filtra por tipo com os dois tipos marcados', () => {
    expect(toTransactionParams({ types: ['COMPRA', 'VENDA'] })).toEqual({
      type: undefined,
      player_id: undefined,
    })
  })

  it('envia o tipo marcado e o jogador', () => {
    expect(toTransactionParams({ types: ['VENDA'], playerId: 2 })).toEqual({
      type: 'VENDA',
      player_id: 2,
    })
  })
})

describe('histórico de transações', () => {
  it('lista as transações e os totais do summary, com sinal na variação do caixa', async () => {
    stubHistoryApi()
    const section = await openHistory()

    const saleRow = (await within(section).findByText('05/11/2026')).closest('tr')!
    expect(within(saleRow).getByText('Venda')).toBeInTheDocument()
    expect(within(saleRow).getByText('+3,00')).toBeInTheDocument()
    expect(within(saleRow).getByText('+35,70')).toBeInTheDocument()

    const purchaseRow = within(section).getByText('02/11/2026').closest('tr')!
    expect(within(purchaseRow).getByLabelText('Sem lucro (compra)')).toBeInTheDocument()
    expect(within(purchaseRow).getByText('-38,85')).toBeInTheDocument()

    const totals = within(section).getByLabelText('Totais do histórico')
    expect(within(totals).getByText('38,85')).toBeInTheDocument()
    expect(within(totals).getByText('2,15')).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /Histórico 2/ })).toHaveAttribute(
      'aria-selected',
      'true',
    )
  })

  it('filtra por tipo e mostra os totais devolvidos para o filtro', async () => {
    const fetchMock = stubHistoryApi()
    const section = await openHistory()

    await within(section).findByText('02/11/2026')
    await userEvent.click(within(section).getByRole('checkbox', { name: 'Compra' }))

    await vi.waitFor(() =>
      expect(within(section).queryByText('02/11/2026')).not.toBeInTheDocument(),
    )
    expect(historyRequests(fetchMock)).toContain('?type=VENDA')
    const totals = within(section).getByLabelText('Totais do histórico')
    expect(within(totals).getByText('0,00')).toBeInTheDocument()
    expect(within(section).getByRole('checkbox', { name: 'Venda' })).toBeDisabled()
  })

  it('filtra por um jogador que aparece no histórico', async () => {
    const fetchMock = stubHistoryApi()
    const section = await openHistory()

    await within(section).findByText('02/11/2026')
    await userEvent.click(within(section).getByRole('button', { name: 'Jogador' }))
    await userEvent.click(await screen.findByRole('menuitemradio', { name: 'Meia Lua' }))

    await vi.waitFor(() => expect(historyRequests(fetchMock)).toContain('?player_id=2'))
  })

  it('mostra o message da API quando o filtro é recusado, sem tabela nem totais', async () => {
    stubHistoryApi((url) =>
      url.searchParams.has('player_id') ? jsonError(404, 'Jogador não encontrado.') : fullHistory,
    )
    const section = await openHistory()

    await within(section).findByText('02/11/2026')
    await userEvent.click(within(section).getByRole('button', { name: 'Jogador' }))
    await userEvent.click(await screen.findByRole('menuitemradio', { name: 'Meia Lua' }))

    expect(await within(section).findByText('O filtro foi recusado')).toBeInTheDocument()
    expect(within(section).getByText('Jogador não encontrado.')).toBeInTheDocument()
    expect(within(section).queryByLabelText('Totais do histórico')).not.toBeInTheDocument()
  })

  it('mostra a lista vazia com os totais zerados', async () => {
    stubHistoryApi(() => ({
      data: [],
      summary: {
        total_spent: 0,
        total_received: 0,
        total_fees: 0,
        total_penalties: 0,
        total_profit: 0,
      },
    }))
    const section = await openHistory()

    expect(await within(section).findByText('Nenhuma transação ainda')).toBeInTheDocument()
    expect(within(section).getAllByText('0,00')).toHaveLength(5)
  })

  it('exporta o CSV com os filtros aplicados', async () => {
    const fetchMock = stubHistoryApi()
    const section = await openHistory()

    await within(section).findByText('02/11/2026')
    await userEvent.click(within(section).getByRole('checkbox', { name: 'Compra' }))
    await userEvent.click(within(section).getByRole('button', { name: 'Exportar CSV' }))

    await vi.waitFor(() => expect(HTMLAnchorElement.prototype.click).toHaveBeenCalled())
    expect(historyRequests(fetchMock, '/export')).toEqual(['?type=VENDA'])
  })
})

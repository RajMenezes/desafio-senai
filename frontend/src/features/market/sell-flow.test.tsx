import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ClubPage } from '@/features/clubs/club-page'
import type { ClubDetail } from '@/features/clubs/types'
import { player } from '@/features/players/test-fixtures'
import { EMPTY_HISTORY, emptyLineup, jsonError, renderPage, stubApi } from '@/test/render-page'
import type { SaleResult } from './types'

const club: ClubDetail = {
  id: 1,
  name: 'Águias FC',
  acronym: 'AGU',
  cash: 51.58,
  squad_value: 52,
  net_worth: 103.58,
  players: [
    {
      ...player({ id: 5, name: 'Felipe Moura', position: 'ZAG', market_value: 52, club_id: 1 }),
      contract: {
        purchase_value: 44.2,
        loyalty_until: '2027-04-04T15:45:00-03:00',
        remaining_days: 144,
        penalty_rate: 10,
      },
    },
  ],
}

const sale: SaleResult = {
  id: 11,
  club_id: 1,
  player_id: 5,
  player_name: 'Felipe Moura',
  type: 'VENDA',
  value: 52,
  fee: 0.78,
  penalty: 5.2,
  profit: 7.8,
  cash_change: 46.02,
  created_at: '2026-11-12T15:00:00-03:00',
  purchase_value: 44.2,
  remaining_days: 144,
  penalty_rate: 10,
  contract_status: 'RESCINDIDO',
  club_cash: 97.6,
}

function stubClubApi(onSell: () => unknown, lineup: unknown = emptyLineup(1)) {
  return stubApi((url, init) => {
    if (init?.method === 'POST') return onSell()
    if (url.pathname.endsWith('/transactions')) return EMPTY_HISTORY
    return url.pathname.endsWith('/lineup') ? lineup : club
  })
}

async function openSale() {
  renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/1' })
  await userEvent.click(await screen.findByRole('button', { name: 'Vender Felipe Moura' }))
  return screen.getByRole('alertdialog')
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('venda de jogador', () => {
  it('confirma com o contrato atual e mostra o resultado devolvido pela API', async () => {
    const fetchMock = stubClubApi(() => sale)

    const confirm = await openSale()
    expect(within(confirm).getByText('Vender Felipe Moura?')).toBeInTheDocument()
    expect(within(confirm).getByText('44,20')).toBeInTheDocument()
    expect(within(confirm).getByText('04/04/2027 15:45')).toBeInTheDocument()
    expect(within(confirm).getByText('144')).toBeInTheDocument()
    expect(within(confirm).getByText('10%')).toBeInTheDocument()

    await userEvent.click(within(confirm).getByRole('button', { name: 'Confirmar venda' }))

    const result = await screen.findByRole('dialog', { name: 'Venda concluída' })
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')
    expect(new URL(post![0]).pathname).toMatch(/\/players\/5\/sell$/)
    expect(post![1]!.body).toBeUndefined()
    expect(within(result).getByText('5,20')).toBeInTheDocument()
    expect(within(result).getByText('+7,80')).toBeInTheDocument()
    expect(within(result).getByText('0,78')).toBeInTheDocument()
    expect(within(result).getByText('+46,02')).toBeInTheDocument()
    expect(within(result).getByText('RESCINDIDO')).toBeInTheDocument()
    expect(within(result).getByText('Novo caixa do Águias FC')).toBeInTheDocument()
    expect(within(result).getByText('97,60')).toBeInTheDocument()

    await userEvent.click(within(result).getByRole('button', { name: 'Ver histórico' }))

    expect(screen.queryByRole('dialog', { name: 'Venda concluída' })).not.toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /Histórico/ })).toHaveAttribute('aria-selected', 'true')
  })

  it('explica a venda sem multa quando a fidelidade já terminou', async () => {
    stubClubApi(() => ({
      ...sale,
      penalty: 0,
      penalty_rate: 0,
      remaining_days: 0,
      profit: -4.8,
      fee: 0,
      contract_status: 'ENCERRADO',
    }))

    const confirm = await openSale()
    await userEvent.click(within(confirm).getByRole('button', { name: 'Confirmar venda' }))

    const result = await screen.findByRole('dialog', { name: 'Venda concluída' })
    expect(within(result).getByText(/não houve multa/)).toBeInTheDocument()
    expect(within(result).getByText('Fidelidade cumprida: sem multa')).toBeInTheDocument()
    expect(within(result).getByText('-4,80')).toBeInTheDocument()
    expect(within(result).getByText('Sem lucro: sem comissão')).toBeInTheDocument()
    expect(within(result).getByText('ENCERRADO')).toBeInTheDocument()
  })

  it('mostra o message da API quando a venda é recusada', async () => {
    stubClubApi(() => jsonError(409, 'Felipe Moura já está livre e não pode ser vendido.'))

    const confirm = await openSale()
    await userEvent.click(within(confirm).getByRole('button', { name: 'Confirmar venda' }))

    expect(await within(confirm).findByText('A venda foi recusada')).toBeInTheDocument()
    expect(
      within(confirm).getByText('Felipe Moura já está livre e não pode ser vendido.'),
    ).toBeInTheDocument()
  })

  it('avisa que o titular sai da escalação', async () => {
    stubClubApi(() => sale, {
      club_id: 1,
      formation: '2-1-1',
      complete: true,
      players: [club.players[0]],
    })

    const confirm = await openSale()
    expect(within(confirm).getByText('Felipe Moura é titular')).toBeInTheDocument()

    await userEvent.click(within(confirm).getByRole('button', { name: 'Confirmar venda' }))

    const result = await screen.findByRole('dialog', { name: 'Venda concluída' })
    expect(within(result).getByText(/voltou a ficar livre e saiu da escalação/)).toBeInTheDocument()
  })
})

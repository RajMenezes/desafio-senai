import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { PlayersPage } from '@/features/players/players-page'
import { clubs, page, player } from '@/features/players/test-fixtures'
import { NetworkError } from '@/lib/api/errors'
import { toDateTimeInputs } from '@/lib/date-time'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import type { PurchaseResult } from './types'

const freePlayer = player({ id: 4, name: 'Eduardo Paiva', condition: 'APTO', market_value: 46 })

const purchase: PurchaseResult = {
  id: 10,
  club_id: 1,
  player_id: 4,
  player_name: 'Eduardo Paiva',
  type: 'COMPRA',
  value: 46,
  fee: 2.3,
  penalty: 0,
  profit: null,
  cash_change: -48.3,
  created_at: '2026-11-12T15:00:00-03:00',
  contract: { purchase_value: 46, loyalty_until: '2027-11-12T15:00:00-03:00', status: 'ATIVO' },
  club_cash: 251.7,
}

function stubMarketApi(onBuy: () => unknown) {
  return stubApi((url, init) => {
    if (init?.method === 'POST') return onBuy()
    if (url.pathname.endsWith('/clubs')) return clubs
    return page([freePlayer])
  })
}

/** Abre a compra do jogador livre, escolhe o Águias FC e confirma com a data informada. */
async function buyWith(date: string, time: string) {
  await userEvent.click(await screen.findByRole('button', { name: 'Comprar Eduardo Paiva' }))
  await userEvent.click(screen.getByRole('combobox', { name: 'Clube comprador' }))
  await userEvent.click(await screen.findByRole('option', { name: /Águias FC \(AGU\)/ }))
  const dateInput = screen.getByLabelText('Data do fim da fidelidade')
  const timeInput = screen.getByLabelText('Hora do fim da fidelidade')
  await userEvent.clear(dateInput)
  await userEvent.type(dateInput, date)
  await userEvent.clear(timeInput)
  await userEvent.type(timeInput, time)
  await userEvent.click(screen.getByRole('button', { name: /Confirmar compra/ }))
}

const inThirtyDays = toDateTimeInputs(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000))

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('compra de jogador', () => {
  it('envia o clube e a fidelidade e mostra o resultado devolvido pela API', async () => {
    const fetchMock = stubMarketApi(() => purchase)
    renderPage(<PlayersPage />)

    await buyWith(inThirtyDays.date, '15:00')

    const result = await screen.findByRole('dialog', { name: 'Compra concluída' })
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')
    expect(new URL(post![0]).pathname).toMatch(/\/players\/4\/buy$/)
    expect(JSON.parse(String(post![1]!.body))).toEqual({
      club_id: 1,
      loyalty_until: `${inThirtyDays.date}T15:00-03:00`,
    })
    expect(within(result).getByText('2,30')).toBeInTheDocument()
    expect(within(result).getByText('48,30')).toBeInTheDocument()
    expect(within(result).getByText('12/11/2027 15:00')).toBeInTheDocument()
    expect(within(result).getByText('ATIVO')).toBeInTheDocument()
    expect(within(result).getByText('Novo caixa do Águias FC')).toBeInTheDocument()
    expect(within(result).getByText('251,70')).toBeInTheDocument()
    expect(within(result).getByRole('link', { name: /Ver clube/ })).toHaveAttribute(
      'href',
      '/clubes/1',
    )
  })

  it('não envia fidelidade no passado', async () => {
    const fetchMock = stubMarketApi(() => purchase)
    renderPage(<PlayersPage />)

    await buyWith('2020-01-01', '10:00')

    expect(await screen.findByText('Escolha um momento depois de agora.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false)
  })

  it('envia a compra acima do limite e mostra a recusa da API', async () => {
    stubMarketApi(() =>
      jsonError(409, 'O custo da compra (105,00) passa do limite de 50% do caixa (90,92).'),
    )
    renderPage(<PlayersPage />)

    await buyWith(inThirtyDays.date, '15:00')

    expect(await screen.findByText('Compra recusada')).toBeInTheDocument()
    expect(
      screen.getByText('O custo da compra (105,00) passa do limite de 50% do caixa (90,92).'),
    ).toBeInTheDocument()
  })

  it('orienta a tentar de novo quando a API não responde', async () => {
    stubMarketApi(() => {
      throw new NetworkError()
    })
    renderPage(<PlayersPage />)

    await buyWith(inThirtyDays.date, '15:00')

    expect(await screen.findByText('Sem conexão')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Tentar de novo/ })).toBeInTheDocument()
  })
})

import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ClubPage } from '@/features/clubs/club-page'
import type { ClubDetail, SquadPlayer } from '@/features/clubs/types'
import { player } from '@/features/players/test-fixtures'
import type { Player } from '@/features/players/types'
import { EMPTY_HISTORY, emptyLineup, jsonError, renderPage, stubApi } from '@/test/render-page'
import type { Lineup } from './types'

const contract = {
  purchase_value: 30,
  loyalty_until: '2027-06-01T10:00:00-03:00',
  remaining_days: 200,
  penalty_rate: 20,
}

function squadPlayer(changes: Partial<Player>): SquadPlayer {
  return { ...player({ club_id: 1, condition: 'APTO', ...changes }), contract }
}

const squad = [
  squadPlayer({ id: 1, name: 'Bruno Tavares', position: 'GOL' }),
  squadPlayer({ id: 2, name: 'Felipe Moura', position: 'ZAG' }),
  squadPlayer({ id: 3, name: 'Diego Ramos', position: 'LAT', condition: 'MAL_CONDICIONADO' }),
  squadPlayer({ id: 4, name: 'Sávio Lima', position: 'MEI' }),
  squadPlayer({ id: 5, name: 'João Silva', position: 'ATA' }),
  squadPlayer({ id: 6, name: 'Lucas Ferraz', position: 'MEI', condition: 'DOENTE' }),
]

const club: ClubDetail = {
  id: 1,
  name: 'Águias FC',
  acronym: 'AGU',
  cash: 51.58,
  squad_value: 222.68,
  net_worth: 274.26,
  players: squad,
}

const completeLineup: Lineup = {
  club_id: 1,
  formation: '2-1-1',
  complete: true,
  players: squad.slice(0, 5),
}

function stubLineupApi(lineup: Lineup, onSave: () => unknown = () => lineup) {
  return stubApi((url, init) => {
    if (init?.method === 'PUT') return onSave()
    if (url.pathname.endsWith('/transactions')) return EMPTY_HISTORY
    return url.pathname.endsWith('/lineup') ? lineup : club
  })
}

async function openLineup() {
  renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/1?aba=escalacao' })
  return screen.findByRole('region', { name: 'Titulares' })
}

function slotTriggers(section: HTMLElement) {
  return within(section).getAllByRole('combobox')
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('escalação do clube', () => {
  it('mostra a escalação salva, as condições e o aviso de mal condicionado', async () => {
    stubLineupApi(completeLineup)
    const section = await openLineup()

    expect(within(section).getByText('Completa')).toBeInTheDocument()
    expect(within(section).getByRole('radio', { name: /2-1-1/ })).toBeChecked()
    expect(slotTriggers(section).map((trigger) => trigger.textContent)).toEqual([
      'Bruno Tavares (GOL)',
      'Felipe Moura (ZAG)',
      'Diego Ramos (LAT)',
      'Sávio Lima (MEI)',
      'João Silva (ATA)',
    ])
    expect(within(section).getByText('Diego Ramos está mal condicionado')).toBeInTheDocument()

    const reserves = screen.getByRole('region', { name: 'Reservas' })
    expect(within(reserves).getByText('Lucas Ferraz')).toBeInTheDocument()
    expect(within(reserves).getByText('Doente não pode ser escalado.')).toBeInTheDocument()
  })

  it('sem escalação, avisa que o clube não joga e só libera o salvar com as 5 vagas', async () => {
    stubLineupApi(emptyLineup(1) as Lineup)
    const section = await openLineup()

    expect(
      screen.getByText('Escalação incompleta: o Águias FC não pode jogar partidas'),
    ).toBeInTheDocument()
    expect(within(section).getByText('Incompleta')).toBeInTheDocument()
    expect(within(section).getByRole('button', { name: /Salvar escalação/ })).toBeDisabled()
  })

  it('envia a formação e os 5 titulares escolhidos', async () => {
    const fetchMock = stubLineupApi(emptyLineup(1) as Lineup, () => completeLineup)
    const section = await openLineup()

    const names = ['Bruno Tavares', 'Felipe Moura', 'Diego Ramos', 'Sávio Lima', 'João Silva']
    for (const [index, name] of names.entries()) {
      await userEvent.click(slotTriggers(section)[index])
      await userEvent.click(await screen.findByRole('option', { name: new RegExp(name) }))
    }
    await userEvent.click(within(section).getByRole('button', { name: /Salvar escalação/ }))

    await vi.waitFor(() =>
      expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'PUT')).toBe(true),
    )
    const put = fetchMock.mock.calls.find(([, init]) => init?.method === 'PUT')!
    expect(new URL(put[0]).pathname).toMatch(/\/clubs\/1\/lineup$/)
    expect(JSON.parse(String(put[1]!.body))).toEqual({
      formation: '2-1-1',
      player_ids: [1, 2, 3, 4, 5],
    })
  })

  it('não oferece jogador doente nas vagas', async () => {
    stubLineupApi(emptyLineup(1) as Lineup)
    const section = await openLineup()

    await userEvent.click(slotTriggers(section)[3])

    expect(await screen.findByRole('option', { name: /Sávio Lima/ })).toBeInTheDocument()
    expect(screen.queryByRole('option', { name: /Lucas Ferraz/ })).not.toBeInTheDocument()
  })

  it('mostra o message da API quando a escalação é recusada', async () => {
    stubLineupApi(completeLineup, () =>
      jsonError(409, 'Lucas Ferraz está DOENTE e não pode ser escalado.'),
    )
    const section = await openLineup()

    await userEvent.click(within(section).getByRole('button', { name: /Salvar escalação/ }))

    expect(await within(section).findByText('A escalação foi recusada')).toBeInTheDocument()
    expect(
      within(section).getByText('Lucas Ferraz está DOENTE e não pode ser escalado.'),
    ).toBeInTheDocument()
  })
})

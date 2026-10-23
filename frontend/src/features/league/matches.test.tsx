import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Club } from '@/features/clubs/types'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import { MatchPage } from './match-page'
import { MatchesPage } from './matches-page'
import type { Duel, DuelSide, Match } from './types'

const clubs: Club[] = [
  { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
  { id: 2, name: 'Delta FC', acronym: 'DEL', cash: 300, squad_value: 0, net_worth: 300 },
  { id: 3, name: 'Gama SC', acronym: 'GAM', cash: 300, squad_value: 0, net_worth: 300 },
]

function side(changes: Partial<DuelSide>): DuelSide {
  return {
    player_id: 1,
    player_name: 'Jogador',
    position: 'ATA',
    condition: 'APTO',
    attribute: 'shooting',
    value: 70,
    ...changes,
  }
}

const duels: Duel[] = [
  {
    order: 1,
    attacking_club_id: 1,
    attacker: side({ player_id: 34, player_name: 'Otto Rangel', value: 75 }),
    defender: side({
      player_id: 40,
      player_name: 'Ivo Teles',
      position: 'GOL',
      attribute: 'defending',
      value: 72,
    }),
    goal: true,
  },
  {
    order: 2,
    attacking_club_id: 1,
    attacker: side({
      player_id: 33,
      player_name: 'Lauro Neves',
      position: 'MEI',
      attribute: 'passing',
      value: 82,
    }),
    defender: side({
      player_id: 41,
      player_name: 'Hugo Vaz',
      position: 'ZAG',
      attribute: 'defending',
      value: 84,
    }),
    goal: false,
  },
  {
    order: 3,
    attacking_club_id: 1,
    attacker: side({
      player_id: 31,
      player_name: 'Davi Lins',
      position: 'LAT',
      condition: 'MAL_CONDICIONADO',
      attribute: 'dribbling',
      value: 63,
    }),
    defender: side({
      player_id: 42,
      player_name: 'Lino Matos',
      position: 'LAT',
      attribute: 'defending',
      value: 63,
    }),
    goal: false,
  },
  {
    order: 4,
    attacking_club_id: 2,
    attacker: side({ player_id: 44, player_name: 'Ugo Falcão', value: 81 }),
    defender: side({
      player_id: 30,
      player_name: 'Rui Prado',
      position: 'GOL',
      attribute: 'defending',
      value: 77,
    }),
    goal: true,
  },
  {
    order: 5,
    attacking_club_id: 2,
    attacker: side({
      player_id: 43,
      player_name: 'Nei Soares',
      position: 'MEI',
      attribute: 'passing',
      value: 70,
    }),
    defender: side({
      player_id: 32,
      player_name: 'Téo Brandão',
      position: 'ZAG',
      attribute: 'defending',
      value: 84,
    }),
    goal: false,
  },
  {
    order: 6,
    attacking_club_id: 2,
    attacker: side({
      player_id: 42,
      player_name: 'Lino Matos',
      position: 'LAT',
      attribute: 'dribbling',
      value: 79,
    }),
    defender: side({
      player_id: 31,
      player_name: 'Davi Lins',
      position: 'LAT',
      condition: 'MAL_CONDICIONADO',
      attribute: 'defending',
      value: 56,
    }),
    goal: true,
  },
]

const match: Match = {
  id: 7,
  played_at: '2026-11-12T15:00:00-03:00',
  home: { club_id: 1, club_name: 'Águias FC', formation: '2-1-1', goals: 1 },
  away: { club_id: 2, club_name: 'Delta FC', formation: '2-1-1', goals: 2 },
  result: 'VISITANTE',
}

const olderMatch: Match = {
  id: 6,
  played_at: '2026-11-10T20:00:00-03:00',
  home: { club_id: 2, club_name: 'Delta FC', formation: '2-1-1', goals: 2 },
  away: { club_id: 3, club_name: 'Gama SC', formation: '3-1', goals: 2 },
  result: 'EMPATE',
}

type Handler = (url: URL, init?: RequestInit) => unknown

function stubLeagueApi(handler: Handler = () => undefined) {
  return stubApi((url, init) => {
    const custom = handler(url, init)
    if (custom !== undefined) return custom
    if (url.pathname.endsWith('/clubs')) return clubs
    if (url.pathname.endsWith('/matches')) return [match, olderMatch]
    if (url.pathname.endsWith('/matches/7')) return { ...match, duels }
    return jsonError(404, 'Partida não encontrada.')
  })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('MatchesPage', () => {
  it('mostra a última partida e o histórico na ordem da API', async () => {
    stubLeagueApi()
    renderPage(<MatchesPage />)

    const last = await screen.findByRole('region', { name: 'Última partida' })
    expect(within(last).getByLabelText('Placar: 1 a 2')).toBeInTheDocument()
    expect(within(last).getByText('Vitória do visitante')).toBeInTheDocument()

    const history = screen.getByRole('region', { name: 'Histórico de partidas' })
    const rows = within(history).getAllByRole('row').slice(1)
    expect(rows).toHaveLength(2)
    expect(within(rows[0]).getByText('12/11/2026')).toBeInTheDocument()
    expect(within(rows[1]).getByText('Empate')).toBeInTheDocument()
    expect(within(rows[0]).getByRole('link', { name: /Ver duelos/ })).toHaveAttribute(
      'href',
      '/partidas/7',
    )
    expect(within(history).getByText('2 partidas')).toBeInTheDocument()
  })

  it('filtra o histórico pelo clube', async () => {
    const fetchMock = stubLeagueApi()
    renderPage(<MatchesPage />)

    const history = await screen.findByRole('region', { name: 'Histórico de partidas' })
    await userEvent.click(within(history).getByRole('button', { name: 'Clube' }))
    await userEvent.click(await screen.findByRole('menuitemradio', { name: 'Gama SC' }))

    await vi.waitFor(() =>
      expect(fetchMock.mock.calls.some(([input]) => new URL(input).search === '?club_id=3')).toBe(
        true,
      ),
    )
  })

  it('mostra a lista vazia com o atalho para jogar', async () => {
    stubLeagueApi((url) => (url.pathname.endsWith('/matches') ? [] : undefined))
    renderPage(<MatchesPage />)

    expect(await screen.findByText('Nenhuma partida jogada')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Última partida' })).not.toBeInTheDocument()
  })
})

describe('nova partida', () => {
  async function chooseClub(label: string, name: string) {
    await userEvent.click(screen.getByRole('combobox', { name: label }))
    await userEvent.click(await screen.findByRole('option', { name: new RegExp(name) }))
  }

  it('joga a partida e abre os duelos', async () => {
    const fetchMock = stubLeagueApi((_url, init) =>
      init?.method === 'POST' ? { ...match, duels } : undefined,
    )
    const router = renderPage(<MatchesPage />, { url: '/?nova' })

    await chooseClub('Mandante', 'Águias FC')
    await chooseClub('Visitante', 'Delta FC')
    await userEvent.click(screen.getByRole('button', { name: /Jogar partida/ }))

    await vi.waitFor(() => expect(router.state.location.pathname).toBe('/partidas/7'))
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')!
    expect(JSON.parse(String(post[1]!.body))).toEqual({ home_club_id: 1, away_club_id: 2 })
  })

  it('não oferece o mandante como visitante e exige os dois clubes', async () => {
    const fetchMock = stubLeagueApi()
    renderPage(<MatchesPage />, { url: '/?nova' })

    await userEvent.click(await screen.findByRole('button', { name: /Jogar partida/ }))
    expect(screen.getByText('Escolha o mandante.')).toBeInTheDocument()
    expect(screen.getByText('Escolha o visitante.')).toBeInTheDocument()

    await chooseClub('Mandante', 'Águias FC')
    await userEvent.click(screen.getByRole('combobox', { name: 'Visitante' }))
    expect(screen.queryByRole('option', { name: /Águias FC/ })).not.toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false)
  })

  it('mostra o message da API quando um clube não tem escalação completa', async () => {
    stubLeagueApi((_url, init) =>
      init?.method === 'POST'
        ? jsonError(409, 'Escalação incompleta: o Gama SC não tem os 5 titulares.')
        : undefined,
    )
    renderPage(<MatchesPage />, { url: '/?nova' })

    await chooseClub('Mandante', 'Gama SC')
    await chooseClub('Visitante', 'Águias FC')
    await userEvent.click(screen.getByRole('button', { name: /Jogar partida/ }))

    expect(await screen.findByText('Não foi possível jogar a partida')).toBeInTheDocument()
    expect(
      screen.getByText('Escalação incompleta: o Gama SC não tem os 5 titulares.'),
    ).toBeInTheDocument()
  })
})

describe('MatchPage', () => {
  it('mostra o placar e os 6 duelos com os valores da API', async () => {
    stubLeagueApi()
    renderPage(<MatchPage />, { path: '/partidas/:matchId', url: '/partidas/7' })

    expect(await screen.findByLabelText('Placar: 1 a 2')).toBeInTheDocument()
    const duelsSection = screen.getByRole('region', { name: 'Duelos' })
    expect(within(duelsSection).getByText('Ataques do Águias FC (mandante)')).toBeInTheDocument()
    expect(within(duelsSection).getByText('Ataques do Delta FC (visitante)')).toBeInTheDocument()

    const first = within(duelsSection).getByText('Otto Rangel').closest('tr')!
    expect(within(first).getByText('ATA, 4º de linha · Águias FC')).toBeInTheDocument()
    expect(within(first).getByText('GOL, Goleiro · Delta FC')).toBeInTheDocument()
    expect(within(first).getByText('75')).toBeInTheDocument()
    expect(within(first).getByText('Gol')).toBeInTheDocument()

    const tied = within(duelsSection).getAllByText('Davi Lins')[0].closest('tr')!
    expect(within(tied).getByText(/Mal condicionado/)).toBeInTheDocument()
    expect(within(tied).getByText('Sem gol')).toBeInTheDocument()
    expect(within(duelsSection).getAllByText('Gol')).toHaveLength(3)
  })

  it('mostra "Partida não encontrada" no 404', async () => {
    stubLeagueApi()
    renderPage(<MatchPage />, { path: '/partidas/:matchId', url: '/partidas/99' })

    expect(await screen.findByText('Partida não encontrada')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Voltar para partidas' })).toHaveAttribute(
      'href',
      '/partidas',
    )
  })
})

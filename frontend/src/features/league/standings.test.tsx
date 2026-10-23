import { screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Club } from '@/features/clubs/types'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import { StandingsPage } from './standings-page'
import type { Match, Standing } from './types'

const clubs: Club[] = [
  { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
  { id: 2, name: 'Delta FC', acronym: 'DEL', cash: 300, squad_value: 0, net_worth: 300 },
  { id: 3, name: 'Mangue FC', acronym: 'MNG', cash: 300, squad_value: 0, net_worth: 300 },
]

function standing(changes: Partial<Standing>): Standing {
  return {
    position: 1,
    club_id: 1,
    club_name: 'Águias FC',
    played: 0,
    wins: 0,
    draws: 0,
    losses: 0,
    goals_for: 0,
    goals_against: 0,
    goal_difference: 0,
    points: 0,
    ...changes,
  }
}

const standings: Standing[] = [
  standing({
    position: 1,
    club_id: 2,
    club_name: 'Delta FC',
    played: 2,
    wins: 2,
    goals_for: 4,
    goals_against: 2,
    goal_difference: 2,
    points: 6,
  }),
  standing({ position: 2, club_id: 3, club_name: 'Mangue FC' }),
  standing({
    position: 3,
    club_id: 1,
    club_name: 'Águias FC',
    played: 2,
    losses: 2,
    goals_for: 2,
    goals_against: 4,
    goal_difference: -2,
  }),
]

const matches: Match[] = [
  {
    id: 2,
    played_at: '2026-11-11T20:00:00-03:00',
    home: { club_id: 2, club_name: 'Delta FC', formation: '2-1-1', goals: 2 },
    away: { club_id: 1, club_name: 'Águias FC', formation: '2-1-1', goals: 1 },
    result: 'MANDANTE',
  },
]

function stubStandingsApi(standingsResponse: () => unknown = () => standings) {
  return stubApi((url) => {
    if (url.pathname.endsWith('/standings')) return standingsResponse()
    if (url.pathname.endsWith('/matches')) return matches
    return clubs
  })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('StandingsPage', () => {
  it('mostra a classificação na ordem e com os totais da API', async () => {
    stubStandingsApi()
    renderPage(<StandingsPage />)

    const table = await screen.findByRole('region', { name: 'Tabela de classificação' })
    await within(table).findByRole('link', { name: 'Delta FC' })
    const rows = within(table).getAllByRole('row').slice(1)
    expect(rows.map((row) => within(row).getByRole('link').textContent)).toEqual([
      'Delta FC',
      'Mangue FC',
      'Águias FC',
    ])
    expect(within(rows[0]).getByText('+2')).toBeInTheDocument()
    expect(within(rows[0]).getByText('6')).toBeInTheDocument()
    expect(within(rows[0]).getByText('DEL')).toBeInTheDocument()
    expect(within(rows[2]).getByText('-2')).toBeInTheDocument()
    expect(within(rows[2]).getByRole('link', { name: 'Águias FC' })).toHaveAttribute(
      'href',
      '/clubes/1',
    )
  })

  it('mostra as últimas partidas e os critérios de desempate', async () => {
    stubStandingsApi()
    renderPage(<StandingsPage />)

    const recent = await screen.findByRole('region', { name: 'Últimas partidas' })
    expect(await within(recent).findByText('2 × 1')).toBeInTheDocument()
    expect(within(recent).getByRole('link', { name: /Ver todas as partidas/ })).toHaveAttribute(
      'href',
      '/partidas',
    )
    const order = screen.getByRole('region', { name: 'Ordem da tabela' })
    expect(
      within(order)
        .getAllByRole('listitem')
        .map((item) => item.textContent),
    ).toEqual([
      '1Mais pontos',
      '2Maior saldo de gols',
      '3Mais gols marcados',
      '4Nome do clube, de A a Z',
    ])
  })

  it('mostra o estado vazio sem clubes', async () => {
    stubStandingsApi(() => [])
    renderPage(<StandingsPage />)

    expect(await screen.findByText('Nenhum clube cadastrado')).toBeInTheDocument()
  })

  it('mostra o erro com o message da API', async () => {
    stubStandingsApi(() => jsonError(500, 'Falha ao calcular a classificação.'))
    renderPage(<StandingsPage />)

    expect(await screen.findByText('Falha ao calcular a classificação.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Tentar de novo/ })).toBeInTheDocument()
  })
})

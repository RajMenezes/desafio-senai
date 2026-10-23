import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Club } from '@/features/clubs/types'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import { PlayersPage } from './players-page'
import type { Player } from './types'

const clubs: Club[] = [
  { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 300, squad_value: 0, net_worth: 300 },
]

function player(changes: Partial<Player>): Player {
  return {
    id: 1,
    name: 'Meia Lua',
    position: 'MEI',
    agility: 85,
    age: 25,
    condition: 'MAL_CONDICIONADO',
    base_value: 40,
    market_value: 37,
    pace: 70,
    shooting: 68,
    passing: 82,
    dribbling: 79,
    defending: 55,
    physical: 64,
    potential: 86,
    overall: 75,
    club_id: null,
    ...changes,
  }
}

const players = [
  player({ id: 1 }),
  player({
    id: 2,
    name: 'João Silva',
    position: 'ATA',
    condition: 'APTO',
    club_id: 1,
    market_value: 1052.5,
  }),
]

function page(data: Player[], totalItems = data.length) {
  return {
    data,
    meta: { page: 1, limit: 10, total_items: totalItems, total_pages: Math.ceil(totalItems / 10) },
  }
}

/** API padrão: devolve os dois jogadores na lista e totais fixos nas contagens das abas. */
function stubPlayersApi(listResponse: (url: URL) => unknown = () => page(players)) {
  return stubApi((url) => {
    if (url.pathname.endsWith('/clubs')) return clubs
    if (url.searchParams.get('limit') === '1') {
      const free = url.searchParams.get('free')
      return page([], free === 'true' ? 7 : free === 'false' ? 16 : 23)
    }
    return listResponse(url)
  })
}

function playerRequests(fetchMock: ReturnType<typeof stubApi>) {
  return fetchMock.mock.calls
    .map(([input]) => new URL(input))
    .filter((url) => url.pathname.endsWith('/players') && url.searchParams.get('limit') === '10')
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('PlayersPage', () => {
  it('lista os jogadores com os valores da API, o clube e a paginação', async () => {
    stubPlayersApi()
    renderPage(<PlayersPage />)

    const row = (await screen.findByText('João Silva')).closest('tr')!
    expect(within(row).getByText('ATA')).toBeInTheDocument()
    expect(within(row).getByText('Apto')).toBeInTheDocument()
    expect(within(row).getByText('1.052,50')).toBeInTheDocument()
    expect(within(row).getByRole('link', { name: 'Águias FC' })).toHaveAttribute(
      'href',
      '/clubes/1',
    )

    const freeRow = screen.getByText('Meia Lua').closest('tr')!
    expect(within(freeRow).getByText('Livre')).toBeInTheDocument()
    expect(within(freeRow).getByText('Mal condicionado')).toBeInTheDocument()
    expect(within(freeRow).getByText('37,00')).toBeInTheDocument()

    expect(screen.getByText('Mostrando 1–2 de 2 jogadores')).toBeInTheDocument()
    expect(await screen.findByRole('tab', { name: 'Livres 7' })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Em clubes 16' })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Todos 23' })).toHaveAttribute('aria-selected', 'true')
  })

  it('envia os filtros da URL para a API', async () => {
    const fetchMock = stubPlayersApi()
    renderPage(<PlayersPage />, { url: '/?condition=DOENTE&min_age=18&max_age=30&sort=age_desc' })

    await screen.findByText('Meia Lua')

    const [request] = playerRequests(fetchMock)
    expect(Object.fromEntries(request.searchParams)).toEqual({
      condition: 'DOENTE',
      min_age: '18',
      max_age: '30',
      sort: 'age_desc',
      page: '1',
      limit: '10',
    })
    expect(
      screen.getByRole('button', { name: /Remover filtro Condição: Doente/ }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('button', { name: /Remover filtro Idade: 18 a 30/ }),
    ).toBeInTheDocument()
  })

  it('remove um filtro pelo chip e volta para a primeira página', async () => {
    const fetchMock = stubPlayersApi()
    const router = renderPage(<PlayersPage />, { url: '/?position=GOL&page=2' })

    await userEvent.click(await screen.findByRole('button', { name: /Remover filtro Posição/ }))

    expect(router.state.location.search).toBe('')
    expect(playerRequests(fetchMock).at(-1)?.searchParams.get('position')).toBeNull()
  })

  it('bloqueia o filtro por clube na aba de livres', async () => {
    stubPlayersApi()
    renderPage(<PlayersPage />, { url: '/?free=true' })

    await screen.findByText('Meia Lua')

    expect(screen.getByRole('button', { name: 'Clube' })).toBeDisabled()
  })

  it('bloqueia a aba de livres quando há filtro por clube', async () => {
    stubPlayersApi()
    renderPage(<PlayersPage />, { url: '/?club_id=1' })

    await screen.findByText('Meia Lua')

    expect(screen.getByRole('tab', { name: /Livres/ })).toBeDisabled()
  })

  it('mostra a lista vazia da busca com a opção de limpar os filtros', async () => {
    stubPlayersApi(() => page([]))
    const router = renderPage(<PlayersPage />, { url: '/?search=zzz' })

    expect(await screen.findByText('Nenhum jogador encontrado para “zzz”')).toBeInTheDocument()

    await userEvent.click(screen.getAllByRole('button', { name: /Limpar filtros/ })[0])

    expect(router.state.location.search).toBe('')
  })

  it('mostra o message da API quando a lista é recusada', async () => {
    stubPlayersApi(() => jsonError(400, 'min_age não pode ser maior que max_age.'))
    renderPage(<PlayersPage />, { url: '/?min_age=40&max_age=20' })

    expect(await screen.findByText('min_age não pode ser maior que max_age.')).toBeInTheDocument()
    expect(screen.getByText('Não foi possível carregar os jogadores')).toBeInTheDocument()
  })
})

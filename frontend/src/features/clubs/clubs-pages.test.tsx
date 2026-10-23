import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { player } from '@/features/players/test-fixtures'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import { ClubPage } from './club-page'
import { ClubsPage } from './clubs-page'
import type { Club, ClubDetail } from './types'

const clubs: Club[] = [
  { id: 1, name: 'Águias FC', acronym: 'AGU', cash: 51.58, squad_value: 222.68, net_worth: 274.26 },
  { id: 2, name: 'Tubarões EC', acronym: 'TUB', cash: 300, squad_value: 0, net_worth: 300 },
]

const aguias: ClubDetail = {
  ...clubs[0],
  players: [
    {
      ...player({ id: 7, name: 'Bruno Tavares', position: 'GOL', condition: 'APTO', club_id: 1 }),
      contract: {
        purchase_value: 32,
        loyalty_until: '2028-11-03T11:20:00-03:00',
        remaining_days: 1089,
        penalty_rate: 20,
      },
    },
    {
      ...player({ id: 8, name: 'Lucas Ferraz', condition: 'DOENTE', club_id: 1 }),
      contract: {
        purchase_value: 28.8,
        loyalty_until: '2026-11-07T09:00:00-03:00',
        remaining_days: 0,
        penalty_rate: 0,
      },
    },
  ],
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('ClubsPage', () => {
  it('lista os clubes com caixa, valor do elenco e patrimônio', async () => {
    stubApi(() => clubs)
    renderPage(<ClubsPage />)

    const row = (await screen.findByText('Águias FC')).closest('tr')!
    expect(within(row).getByText('51,58')).toBeInTheDocument()
    expect(within(row).getByText('222,68')).toBeInTheDocument()
    expect(within(row).getByText('274,26')).toBeInTheDocument()
    expect(within(row).getByRole('link', { name: 'Ver clube Águias FC' })).toHaveAttribute(
      'href',
      '/clubes/1',
    )
    expect(screen.getByText('2 de 20')).toBeInTheDocument()
  })

  it('pede à API a ordenação escolhida, na ordem que ela devolve', async () => {
    const fetchMock = stubApi(() => clubs)
    renderPage(<ClubsPage />)

    await screen.findByText('Águias FC')
    await userEvent.click(screen.getByRole('button', { name: /Ordenar: nome/ }))
    await userEvent.click(
      await screen.findByRole('menuitemradio', { name: 'Patrimônio (maior para o menor)' }),
    )

    await screen.findByRole('button', { name: /Ordenar: patrimônio \(maior para o menor\)/ })
    const sorts = fetchMock.mock.calls.map(([input]) => new URL(input).searchParams.get('sort'))
    expect(sorts).toEqual(['name', 'net_worth_desc'])
  })

  it('mostra a lista vazia com o atalho para criar', async () => {
    stubApi(() => [])
    renderPage(<ClubsPage />)

    expect(await screen.findByText('Nenhum clube cadastrado')).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: 'Novo clube' })).toHaveLength(2)
  })

  it('cria o clube e abre a página dele', async () => {
    const created: Club = {
      id: 3,
      name: 'Leões da Serra',
      acronym: 'LDS',
      cash: 300,
      squad_value: 0,
      net_worth: 300,
    }
    const fetchMock = stubApi((_url, init) => (init?.method === 'POST' ? created : clubs))
    const router = renderPage(<ClubsPage />)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo clube' }))
    await userEvent.type(screen.getByLabelText('Nome'), 'Leões da Serra')
    await userEvent.type(screen.getByLabelText('Sigla'), 'lds')
    await userEvent.click(screen.getByRole('button', { name: 'Criar clube' }))

    await vi.waitFor(() => expect(router.state.location.pathname).toBe('/clubes/3'))
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')
    expect(JSON.parse(String(post?.[1]?.body))).toEqual({ name: 'Leões da Serra', acronym: 'LDS' })
  })

  it('mostra o message da API quando a criação é recusada', async () => {
    stubApi((_url, init) =>
      init?.method === 'POST'
        ? jsonError(409, 'Já existe um clube com o nome “Águias FC”.')
        : clubs,
    )
    renderPage(<ClubsPage />, { url: '/?novo' })

    await userEvent.type(await screen.findByLabelText('Nome'), 'águias fc')
    await userEvent.type(screen.getByLabelText('Sigla'), 'AGF')
    await userEvent.click(screen.getByRole('button', { name: 'Criar clube' }))

    expect(
      await screen.findByText('Já existe um clube com o nome “Águias FC”.'),
    ).toBeInTheDocument()
  })
})

describe('ClubPage', () => {
  it('mostra os valores do clube e o elenco com o contrato', async () => {
    stubApi(() => aguias)
    renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/1' })

    expect(await screen.findByRole('heading', { name: 'Águias FC' })).toBeInTheDocument()
    expect(screen.getByText('274,26')).toBeInTheDocument()
    expect(screen.getAllByText('2 de 12').length).toBeGreaterThan(0)

    const goalkeeper = screen.getByText('Bruno Tavares').closest('tr')!
    expect(within(goalkeeper).getByText('03/11/2028')).toBeInTheDocument()
    expect(within(goalkeeper).getByText('11:20')).toBeInTheDocument()
    expect(within(goalkeeper).getByText('1.089')).toBeInTheDocument()
    expect(within(goalkeeper).getByText('20%')).toBeInTheDocument()

    const sick = screen.getByText('Lucas Ferraz').closest('tr')!
    expect(within(sick).getByText('0%')).toBeInTheDocument()
    expect(within(sick).getByText('28,80')).toBeInTheDocument()
  })

  it('mostra o elenco vazio com o caminho para os jogadores livres', async () => {
    stubApi(() => ({ ...clubs[1], players: [] }))
    renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/2' })

    expect(await screen.findByText('Nenhum jogador no elenco')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver jogadores livres' })).toHaveAttribute(
      'href',
      '/jogadores?free=true',
    )
  })

  it('mostra "Clube não encontrado" com o message da API no 404', async () => {
    stubApi(() => jsonError(404, 'Clube 99 não encontrado.'))
    renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/99' })

    expect(await screen.findByText('Clube não encontrado')).toBeInTheDocument()
    expect(screen.getByText('Clube 99 não encontrado.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Voltar para clubes' })).toHaveAttribute(
      'href',
      '/clubes',
    )
  })

  it('não chama a API com um id inválido', async () => {
    const fetchMock = stubApi(() => aguias)
    renderPage(<ClubPage />, { path: '/clubes/:clubId', url: '/clubes/01' })

    expect(await screen.findByText('Clube não encontrado')).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })
})

import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonError, renderPage, stubApi } from '@/test/render-page'
import { PlayersPage } from './players-page'
import { clubs, page, player } from './test-fixtures'
import type { Player } from './types'

const freePlayer = player({ id: 1, name: 'Ênio Carvalho', condition: 'DOENTE', position: 'LAT' })
const clubPlayer = player({
  id: 2,
  name: 'Danilo Freitas',
  position: 'ZAG',
  age: 30,
  agility: 25,
  condition: 'APTO',
  club_id: 1,
})

type WriteHandler = (url: URL, init: RequestInit) => unknown

/** API com a lista fixa; pedidos de escrita vão para `onWrite`. */
function stubPlayersApi(onWrite: WriteHandler = () => ({})) {
  return stubApi((url, init) => {
    if (init?.method && init.method !== 'GET') return onWrite(url, init)
    if (url.pathname.endsWith('/clubs')) return clubs
    if (url.pathname.endsWith('/lineup')) {
      return { club_id: 1, formation: '2-1-1', complete: false, players: [clubPlayer] }
    }
    const detail = url.pathname.match(/\/players\/(\d+)$/)
    if (detail) return [freePlayer, clubPlayer].find((p) => p.id === Number(detail[1]))
    return page([freePlayer, clubPlayer])
  })
}

function writeRequests(fetchMock: ReturnType<typeof stubApi>) {
  return fetchMock.mock.calls
    .filter(([, init]) => init?.method && init.method !== 'GET')
    .map(([input, init]) => ({
      method: init?.method,
      path: new URL(input).pathname.replace(/^.*\/api\/v1/, ''),
      body: init?.body ? JSON.parse(String(init.body)) : undefined,
    }))
}

async function openRowAction(playerName: string, action: string) {
  await userEvent.click(await screen.findByRole('button', { name: `Ações de ${playerName}` }))
  await userEvent.click(await screen.findByRole('menuitem', { name: new RegExp(action) }))
}

async function fillCreateForm(values: Record<string, string>) {
  for (const [label, value] of Object.entries(values)) {
    await userEvent.type(screen.getByLabelText(label), value)
  }
}

const VALID_FORM = {
  Nome: 'Davi Melo',
  Idade: '23',
  Agilidade: '70',
  Velocidade: '66',
  Finalização: '62',
  Passe: '74',
  Drible: '70',
  Defesa: '48',
  Físico: '58',
  Potencial: '82',
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('cadastro de jogador', () => {
  it('envia o formulário e mostra os valores calculados pela API', async () => {
    const created: Player = player({
      id: 9,
      name: 'Davi Melo',
      condition: 'APTO',
      base_value: 26,
      market_value: 26,
      overall: 68,
    })
    const fetchMock = stubPlayersApi(() => created)
    renderPage(<PlayersPage />)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo jogador' }))
    await userEvent.click(screen.getByRole('radio', { name: /ATA/ }))
    await fillCreateForm(VALID_FORM)
    await userEvent.click(screen.getByRole('button', { name: 'Salvar jogador' }))

    expect(await screen.findByRole('heading', { name: 'Jogador cadastrado' })).toBeInTheDocument()
    expect(writeRequests(fetchMock)).toEqual([
      {
        method: 'POST',
        path: '/players',
        body: {
          name: 'Davi Melo',
          position: 'ATA',
          age: 23,
          agility: 70,
          pace: 66,
          shooting: 62,
          passing: 74,
          dribbling: 70,
          defending: 48,
          physical: 58,
          potential: 82,
        },
      },
    ])
    const result = screen.getByRole('dialog')
    expect(within(result).getAllByText('26,00')).toHaveLength(2)
    expect(within(result).getByText('68')).toBeInTheDocument()
  })

  it('mostra os erros junto aos campos sem chamar a API', async () => {
    const fetchMock = stubPlayersApi()
    renderPage(<PlayersPage />)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo jogador' }))
    await userEvent.type(screen.getByLabelText('Nome'), 'Da')
    await userEvent.click(screen.getByRole('button', { name: 'Salvar jogador' }))

    expect(await screen.findByText('Use de 3 a 100 caracteres.')).toBeInTheDocument()
    expect(screen.getByText('Informe o potencial.')).toBeInTheDocument()
    expect(screen.getByLabelText('Nome')).toHaveAttribute('aria-invalid', 'true')
    expect(writeRequests(fetchMock)).toEqual([])
  })

  it('mostra o message da API quando o cadastro é recusado', async () => {
    stubPlayersApi(() => jsonError(400, 'A soma dos atributos passa de 480.'))
    renderPage(<PlayersPage />)

    await userEvent.click(await screen.findByRole('button', { name: 'Novo jogador' }))
    await fillCreateForm(VALID_FORM)
    await userEvent.click(screen.getByRole('button', { name: 'Salvar jogador' }))

    expect(await screen.findByText('A soma dos atributos passa de 480.')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Novo jogador' })).toBeInTheDocument()
  })
})

describe('edição de jogador', () => {
  it('trava a posição de jogador com clube e não deixa a idade diminuir', async () => {
    const fetchMock = stubPlayersApi()
    renderPage(<PlayersPage />)

    await openRowAction('Danilo Freitas', 'Editar')

    expect(screen.getByRole('radio', { name: /ZAG/ })).toBeChecked()
    expect(screen.getByRole('radio', { name: /ATA/ })).toBeDisabled()

    const age = screen.getByLabelText('Idade')
    await userEvent.clear(age)
    await userEvent.type(age, '29')
    await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }))

    expect(await screen.findByText('A idade não pode diminuir (atual: 30).')).toBeInTheDocument()
    expect(writeRequests(fetchMock)).toEqual([])
  })

  it('envia o PUT com os quatro campos', async () => {
    const fetchMock = stubPlayersApi(() => ({ ...clubPlayer, age: 31 }))
    renderPage(<PlayersPage />)

    await openRowAction('Danilo Freitas', 'Editar')
    const age = screen.getByLabelText('Idade')
    await userEvent.clear(age)
    await userEvent.type(age, '31')
    await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }))

    await screen.findByRole('button', { name: 'Ações de Danilo Freitas' })
    expect(writeRequests(fetchMock)).toEqual([
      {
        method: 'PUT',
        path: '/players/2',
        body: { name: 'Danilo Freitas', position: 'ZAG', age: 31, agility: 25 },
      },
    ])
  })
})

describe('alteração de condição', () => {
  it('bloqueia Doente direto para Apto e envia a nova condição', async () => {
    const fetchMock = stubPlayersApi(() => ({ ...freePlayer, condition: 'MAL_CONDICIONADO' }))
    renderPage(<PlayersPage />)

    await openRowAction('Ênio Carvalho', 'Alterar condição')

    expect(screen.getByRole('radio', { name: /Apto/ })).toBeDisabled()
    expect(screen.getByText('De Doente não passa direto para Apto.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Salvar condição' })).toBeDisabled()

    await userEvent.click(screen.getByRole('radio', { name: /Mal condicionado/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Salvar condição' }))

    await screen.findByRole('button', { name: 'Ações de Ênio Carvalho' })
    expect(writeRequests(fetchMock)).toEqual([
      { method: 'PATCH', path: '/players/1/condition', body: { condition: 'MAL_CONDICIONADO' } },
    ])
  })
})

describe('condição de titular', () => {
  it('avisa que o titular sai da escalação ao passar a Doente', async () => {
    stubPlayersApi()
    renderPage(<PlayersPage />)

    await openRowAction('Danilo Freitas', 'Alterar condição')
    await userEvent.click(screen.getByRole('radio', { name: /Doente/ }))

    expect(await screen.findByText('Danilo Freitas é titular')).toBeInTheDocument()
    expect(screen.getByText(/o Águias FC só volta a jogar partidas/)).toBeInTheDocument()
  })
})

describe('exclusão de jogador', () => {
  it('só permite excluir jogador livre', async () => {
    stubPlayersApi()
    renderPage(<PlayersPage />)

    await userEvent.click(await screen.findByRole('button', { name: 'Ações de Danilo Freitas' }))

    expect(await screen.findByRole('menuitem', { name: /Excluir/ })).toHaveAttribute(
      'aria-disabled',
      'true',
    )
  })

  it('confirma e envia o DELETE', async () => {
    const fetchMock = stubPlayersApi(() => new Response(null, { status: 204 }))
    renderPage(<PlayersPage />)

    await openRowAction('Ênio Carvalho', 'Excluir')
    await userEvent.click(screen.getByRole('button', { name: 'Excluir jogador' }))

    await screen.findByRole('button', { name: 'Ações de Ênio Carvalho' })
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    expect(writeRequests(fetchMock)).toEqual([
      { method: 'DELETE', path: '/players/1', body: undefined },
    ])
  })

  it('mostra o message da API quando a exclusão é recusada', async () => {
    stubPlayersApi(() => jsonError(409, 'O jogador está no elenco de um clube.'))
    renderPage(<PlayersPage />)

    await openRowAction('Ênio Carvalho', 'Excluir')
    await userEvent.click(screen.getByRole('button', { name: 'Excluir jogador' }))

    expect(await screen.findByText('O jogador está no elenco de um clube.')).toBeInTheDocument()
    expect(screen.getByRole('alertdialog')).toBeInTheDocument()
  })
})

describe('detalhes do jogador', () => {
  it('mostra os valores do GET /players/{id}', async () => {
    stubPlayersApi()
    renderPage(<PlayersPage />)

    await openRowAction('Danilo Freitas', 'Ver detalhes')

    const sheet = await screen.findByRole('dialog')
    expect(await within(sheet).findByText('Valor de mercado')).toBeInTheDocument()
    expect(within(sheet).getByText('Águias FC')).toBeInTheDocument()
    expect(within(sheet).getByText('30 anos')).toBeInTheDocument()
  })
})

import { afterEach, describe, expect, it, vi } from 'vitest'
import { buildUrl, request } from './client'
import { ApiError, NetworkError, getErrorMessage } from './errors'

const API_URL = import.meta.env.VITE_API_URL

function mockFetch(response: Response | Error) {
  const fetchMock =
    response instanceof Error
      ? vi.fn().mockRejectedValue(response)
      : vi.fn().mockResolvedValue(response)
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

function jsonResponse(body: unknown, status: number) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('buildUrl', () => {
  it('junta a origem da API com o caminho e ignora parâmetros vazios', () => {
    const url = buildUrl('/players', {
      search: 'joão',
      club_id: 3,
      free: false,
      position: undefined,
      sort: '',
      min_age: null,
    })

    expect(url).toBe(`${API_URL}/players?search=jo%C3%A3o&club_id=3&free=false`)
  })
})

describe('request', () => {
  it('envia o corpo como JSON e devolve a resposta', async () => {
    const fetchMock = mockFetch(jsonResponse({ id: 7 }, 201))

    const result = await request<{ id: number }>('/clubs', {
      method: 'POST',
      body: { name: 'Delta FC' },
    })

    expect(result).toEqual({ id: 7 })
    expect(fetchMock).toHaveBeenCalledWith(`${API_URL}/clubs`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'Delta FC' }),
    })
  })

  it('devolve undefined quando a API responde 204', async () => {
    mockFetch(new Response(null, { status: 204 }))

    await expect(request('/players/1', { method: 'DELETE' })).resolves.toBeUndefined()
  })

  it('lança ApiError com o status e o message da API', async () => {
    mockFetch(jsonResponse({ message: 'Clube não encontrado.' }, 404))

    const error = await request('/clubs/99').catch((caught: unknown) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status: 404, message: 'Clube não encontrado.' })
  })

  it('usa uma mensagem padrão quando a recusa não traz message', async () => {
    mockFetch(new Response('erro', { status: 500 }))

    await expect(request('/players')).rejects.toThrow('A API respondeu com o status 500.')
  })

  it('lança NetworkError quando a API não responde', async () => {
    mockFetch(new TypeError('Failed to fetch'))

    await expect(request('/players')).rejects.toBeInstanceOf(NetworkError)
  })
})

describe('getErrorMessage', () => {
  it('devolve o message da API', () => {
    expect(getErrorMessage(new ApiError(409, 'Elenco cheio.'))).toBe('Elenco cheio.')
  })

  it('usa uma mensagem genérica para erros desconhecidos', () => {
    expect(getErrorMessage(new Error('falha interna'))).toBe(
      'Ocorreu um erro inesperado. Tente de novo.',
    )
  })
})

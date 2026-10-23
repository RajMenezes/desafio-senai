import { QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import type { ReactElement } from 'react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { vi } from 'vitest'
import { TooltipProvider } from '@/components/ui/tooltip'
import { createQueryClient } from '@/lib/api/query-client'

interface RenderPageOptions {
  /** Padrão da rota, para páginas com parâmetros (ex.: "/clubes/:clubId"). */
  path?: string
  url?: string
}

/** Renderiza uma página numa rota de memória, com os providers do app. */
export function renderPage(
  element: ReactElement,
  { path = '/', url = '/' }: RenderPageOptions = {},
) {
  const router = createMemoryRouter(
    [
      { path, element },
      { path: '*', element: null },
    ],
    {
      initialEntries: [url],
    },
  )
  render(
    <QueryClientProvider client={createQueryClient()}>
      <TooltipProvider>
        <RouterProvider router={router} />
      </TooltipProvider>
    </QueryClientProvider>,
  )
  return router
}

type ApiHandler = (url: URL, init?: RequestInit) => unknown

/** Simula a API: o handler recebe a URL chamada e devolve o corpo JSON (ou uma Response pronta). */
export function stubApi(handler: ApiHandler) {
  const fetchMock = vi.fn(async (input: string, init?: RequestInit) => {
    const result = handler(new URL(input), init)
    if (result instanceof Response) return result
    return new Response(JSON.stringify(result), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    })
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

export function jsonError(status: number, message: string) {
  return new Response(JSON.stringify({ message }), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

/** Histórico de transações vazio, para páginas de clube que não testam o histórico. */
export const EMPTY_HISTORY = {
  data: [],
  summary: {
    total_spent: 0,
    total_received: 0,
    total_fees: 0,
    total_penalties: 0,
    total_profit: 0,
  },
}

/** Clube sem escalação, como a API devolve em `GET /clubs/{id}/lineup` (RN19). */
export function emptyLineup(clubId: number) {
  return { club_id: clubId, formation: null, complete: false, players: [] }
}

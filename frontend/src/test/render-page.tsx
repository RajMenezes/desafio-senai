import { QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import type { ReactElement } from 'react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { vi } from 'vitest'
import { TooltipProvider } from '@/components/ui/tooltip'
import { createQueryClient } from '@/lib/api/query-client'

/** Renderiza uma página numa rota de memória, com os providers do app. */
export function renderPage(element: ReactElement, { url = '/' }: { url?: string } = {}) {
  const router = createMemoryRouter([{ path: '/', element }], { initialEntries: [url] })
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

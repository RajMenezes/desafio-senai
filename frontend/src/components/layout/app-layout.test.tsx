import { QueryClientProvider } from '@tanstack/react-query'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { TooltipProvider } from '@/components/ui/tooltip'
import { clubs, page, player } from '@/features/players/test-fixtures'
import { createQueryClient } from '@/lib/api/query-client'
import { stubApi } from '@/test/render-page'
import { AppLayout } from './app-layout'

function sidebarMenu() {
  return within(screen.getByRole('navigation', { name: 'Menu principal' }))
}

function renderLayout(initialPath: string) {
  const router = createMemoryRouter(
    [
      {
        element: <AppLayout />,
        children: [
          {
            path: 'jogadores',
            element: <h1>Página de jogadores</h1>,
            handle: { section: 'Mercado', title: 'Jogadores' },
          },
          {
            path: 'classificacao',
            element: <h1>Página de classificação</h1>,
            handle: { section: 'Liga', title: 'Classificação' },
          },
          {
            path: 'clubes/:clubId',
            element: <h1>Página do clube</h1>,
            handle: {
              section: 'Mercado',
              title: 'Clube',
              parent: { title: 'Clubes', to: '/clubes' },
            },
          },
        ],
      },
    ],
    { initialEntries: [initialPath] },
  )

  render(
    <QueryClientProvider client={createQueryClient()}>
      <TooltipProvider>
        <RouterProvider router={router} />
      </TooltipProvider>
    </QueryClientProvider>,
  )
}

beforeEach(() => {
  stubApi((url) => {
    if (url.pathname.endsWith('/clubs')) return clubs
    if (url.pathname.endsWith('/matches')) return []
    return page([player()], 23)
  })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('AppLayout', () => {
  it('mostra o menu, a página atual e o breadcrumb', async () => {
    renderLayout('/jogadores')

    expect(screen.getByRole('heading', { name: 'Página de jogadores' })).toBeInTheDocument()
    expect(sidebarMenu().getByRole('link', { name: 'Jogadores' })).toHaveAttribute(
      'data-active',
      'true',
    )
    expect(sidebarMenu().getByRole('link', { name: 'Clubes' })).toHaveAttribute(
      'data-active',
      'false',
    )
    expect(screen.getByRole('navigation', { name: 'breadcrumb' })).toHaveTextContent(
      'MercadoJogadores',
    )
    expect(await sidebarMenu().findByText('23')).toBeInTheDocument()
  })

  it('navega pelo menu', async () => {
    renderLayout('/jogadores')

    await userEvent.click(sidebarMenu().getByRole('link', { name: 'Classificação' }))

    expect(screen.getByRole('heading', { name: 'Página de classificação' })).toBeInTheDocument()
    expect(screen.getByRole('navigation', { name: 'breadcrumb' })).toHaveTextContent(
      'LigaClassificação',
    )
  })

  it('lista os clubes no menu e leva para a página do clube', async () => {
    renderLayout('/jogadores')

    await userEvent.click(await sidebarMenu().findByRole('link', { name: /Águias FC/ }))

    expect(screen.getByRole('heading', { name: 'Página do clube' })).toBeInTheDocument()
    expect(sidebarMenu().getByRole('link', { name: /Águias FC/ })).toHaveAttribute(
      'data-active',
      'true',
    )
    const breadcrumb = screen.getByRole('navigation', { name: 'breadcrumb' })
    expect(within(breadcrumb).getByRole('link', { name: 'Clubes' })).toHaveAttribute(
      'href',
      '/clubes',
    )
  })
})

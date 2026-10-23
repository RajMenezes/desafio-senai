import { QueryClientProvider } from '@tanstack/react-query'
import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { describe, expect, it } from 'vitest'
import { TooltipProvider } from '@/components/ui/tooltip'
import { createQueryClient } from '@/lib/api/query-client'
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

describe('AppLayout', () => {
  it('mostra o menu, a página atual e o breadcrumb', () => {
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
  })

  it('navega pelo menu', async () => {
    renderLayout('/jogadores')

    await userEvent.click(sidebarMenu().getByRole('link', { name: 'Classificação' }))

    expect(screen.getByRole('heading', { name: 'Página de classificação' })).toBeInTheDocument()
    expect(screen.getByRole('navigation', { name: 'breadcrumb' })).toHaveTextContent(
      'LigaClassificação',
    )
  })
})

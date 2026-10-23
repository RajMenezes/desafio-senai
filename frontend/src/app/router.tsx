import { createBrowserRouter, Navigate } from 'react-router'
import { NotFoundPage } from '@/app/not-found-page'
import { paths } from '@/app/paths'
import type { RouteHandle } from '@/app/route-handle'
import { AppLayout } from '@/components/layout/app-layout'

const handle = (section: string, title: string): RouteHandle => ({ section, title })

export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/jogadores" replace /> },
      {
        path: 'jogadores',
        handle: handle('Mercado', 'Jogadores'),
        lazy: async () => ({
          Component: (await import('@/features/players/players-page')).PlayersPage,
        }),
      },
      {
        path: 'clubes',
        handle: handle('Mercado', 'Clubes'),
        lazy: async () => ({
          Component: (await import('@/features/clubs/clubs-page')).ClubsPage,
        }),
      },
      {
        path: 'clubes/:clubId',
        handle: { ...handle('Mercado', 'Clube'), parent: { title: 'Clubes', to: paths.clubs } },
        lazy: async () => ({
          Component: (await import('@/features/clubs/club-page')).ClubPage,
        }),
      },
      {
        path: 'partidas',
        handle: handle('Liga', 'Partidas'),
        lazy: async () => ({
          Component: (await import('@/features/league/matches-page')).MatchesPage,
        }),
      },
      {
        path: 'classificacao',
        handle: handle('Liga', 'Classificação'),
        lazy: async () => ({
          Component: (await import('@/features/league/standings-page')).StandingsPage,
        }),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])

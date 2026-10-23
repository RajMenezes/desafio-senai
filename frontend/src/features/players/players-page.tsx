import { Plus, Users } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { PageHeader } from '@/components/common/page-header'
import { SortMenu } from '@/components/common/sort-menu'
import { Button } from '@/components/ui/button'
import { useClubs } from '@/features/clubs/api'
import { usePlayers } from './api'
import { ActiveFilters } from './components/active-filters'
import { PlayerDialogs, type PlayerDialogState } from './components/player-dialogs'
import { PlayerTabs } from './components/player-tabs'
import { PlayersList } from './components/players-list'
import { PlayersToolbar } from './components/players-toolbar'
import { PLAYER_SORTS, toPlayersQuery } from './player-filters'
import { usePlayerFilters } from './use-player-filters'
import { usePlayerTabCounts } from './use-player-tab-counts'

export function PlayersPage() {
  const { filters, updateFilters, clearFilters } = usePlayerFilters()
  const clubsQuery = useClubs()
  const playersQuery = usePlayers(toPlayersQuery(filters))
  const counts = usePlayerTabCounts(filters)
  const [dialog, setDialog] = useState<PlayerDialogState | null>(null)
  const openCreateDialog = () => setDialog({ kind: 'create' })

  const clubs = useMemo(() => clubsQuery.data ?? [], [clubsQuery.data])
  const clubsById = useMemo(() => new Map(clubs.map((club) => [club.id, club])), [clubs])

  // Página além da última (ex.: depois de uma exclusão): volta para a última página existente.
  const totalPages = playersQuery.data?.meta.total_pages ?? 0
  useEffect(() => {
    if (totalPages > 0 && filters.page > totalPages) {
      updateFilters({ page: totalPages })
    }
  }, [filters.page, totalPages, updateFilters])

  return (
    <>
      <PageHeader
        icon={Users}
        title="Jogadores"
        description="Cadastre jogadores, acompanhe os indicadores e compre os que estão livres."
        actions={
          <Button onClick={openCreateDialog}>
            <Plus data-icon="inline-start" aria-hidden />
            Novo jogador
          </Button>
        }
      />
      <PlayersToolbar filters={filters} clubs={clubs} onChange={updateFilters} />
      <ActiveFilters
        filters={filters}
        clubs={clubs}
        onChange={updateFilters}
        onClear={clearFilters}
      />
      <section aria-label="Lista de jogadores" className="rounded-xl border bg-card">
        <div className="flex flex-wrap items-center justify-between gap-3 p-4">
          <PlayerTabs
            tab={filters.tab}
            counts={counts}
            freeDisabled={filters.clubId !== undefined}
            onChange={(tab) => updateFilters({ tab })}
          />
          <SortMenu
            options={PLAYER_SORTS}
            value={filters.sort}
            onChange={(sort) => updateFilters({ sort })}
          />
        </div>
        <PlayersList
          query={playersQuery}
          filters={filters}
          clubsById={clubsById}
          onPageChange={(page) => updateFilters({ page })}
          onClearFilters={clearFilters}
          onCreatePlayer={openCreateDialog}
          onAction={(kind, player) => setDialog({ kind, player })}
        />
      </section>
      <PlayerDialogs state={dialog} clubsById={clubsById} onChange={setDialog} />
    </>
  )
}

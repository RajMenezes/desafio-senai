import { Users } from 'lucide-react'
import { useEffect, useMemo } from 'react'
import { PageHeader } from '@/components/common/page-header'
import { useClubs } from '@/features/clubs/api'
import { usePlayers } from './api'
import { ActiveFilters } from './components/active-filters'
import { PlayerSortMenu } from './components/player-sort-menu'
import { PlayerTabs } from './components/player-tabs'
import { PlayersList } from './components/players-list'
import { PlayersToolbar } from './components/players-toolbar'
import { toPlayersQuery } from './player-filters'
import { usePlayerFilters } from './use-player-filters'
import { usePlayerTabCounts } from './use-player-tab-counts'

export function PlayersPage() {
  const { filters, updateFilters, clearFilters } = usePlayerFilters()
  const clubsQuery = useClubs()
  const playersQuery = usePlayers(toPlayersQuery(filters))
  const counts = usePlayerTabCounts(filters)

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
          <PlayerSortMenu sort={filters.sort} onChange={(sort) => updateFilters({ sort })} />
        </div>
        <PlayersList
          query={playersQuery}
          filters={filters}
          clubsById={clubsById}
          onPageChange={(page) => updateFilters({ page })}
          onClearFilters={clearFilters}
        />
      </section>
    </>
  )
}

import { Plus, SearchX, Users, X } from 'lucide-react'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorState } from '@/components/common/error-state'
import { ListPagination } from '@/components/common/list-pagination'
import { Button } from '@/components/ui/button'
import type { Club } from '@/features/clubs/types'
import type { Player } from '../types'
import { usePlayers } from '../api'
import { effectiveSearch, hasActiveFilters, type PlayerFilters } from '../player-filters'
import type { PlayerAction } from './player-actions-menu'
import { PlayersTable } from './players-table'

const TAB_EMPTY_STATES = {
  free: {
    title: 'Nenhum jogador livre',
    description: 'Todos os jogadores cadastrados estão em clubes.',
  },
  in_club: {
    title: 'Nenhum jogador em clubes',
    description: 'Compre jogadores livres para formar os elencos.',
  },
  all: {
    title: 'Nenhum jogador cadastrado',
    description: 'Com o banco recém-criado, a lista começa vazia.',
  },
}

function PlayersEmptyState({
  filters,
  onClearFilters,
  onCreatePlayer,
}: {
  filters: PlayerFilters
  onClearFilters: () => void
  onCreatePlayer: () => void
}) {
  if (hasActiveFilters(filters)) {
    const search = effectiveSearch(filters.search)
    return (
      <EmptyState
        icon={SearchX}
        title={search ? `Nenhum jogador encontrado para “${search}”` : 'Nenhum jogador encontrado'}
        description="Confira a grafia ou tire alguns filtros. A busca ignora maiúsculas, minúsculas e acentos."
        action={
          <Button variant="outline" onClick={onClearFilters}>
            <X data-icon="inline-start" aria-hidden />
            Limpar filtros
          </Button>
        }
      />
    )
  }
  if (filters.tab === 'all') {
    return (
      <EmptyState
        icon={Users}
        {...TAB_EMPTY_STATES.all}
        action={
          <Button variant="outline" onClick={onCreatePlayer}>
            <Plus data-icon="inline-start" aria-hidden />
            Novo jogador
          </Button>
        }
      />
    )
  }
  return <EmptyState icon={Users} {...TAB_EMPTY_STATES[filters.tab]} />
}

interface PlayersListProps {
  query: ReturnType<typeof usePlayers>
  filters: PlayerFilters
  clubsById: Map<number, Club>
  onPageChange: (page: number) => void
  onClearFilters: () => void
  onCreatePlayer: () => void
  onAction: (action: PlayerAction, player: Player) => void
}

export function PlayersList({
  query,
  filters,
  clubsById,
  onPageChange,
  onClearFilters,
  onCreatePlayer,
  onAction,
}: PlayersListProps) {
  if (query.isError) {
    return (
      <ErrorState
        title="Não foi possível carregar os jogadores"
        error={query.error}
        onRetry={() => query.refetch()}
        retrying={query.isFetching}
      />
    )
  }

  if (query.isPending) {
    return <PlayersTable players={[]} clubsById={clubsById} onAction={onAction} loading />
  }

  const { data: players, meta } = query.data
  if (meta.total_items === 0) {
    return (
      <PlayersEmptyState
        filters={filters}
        onClearFilters={onClearFilters}
        onCreatePlayer={onCreatePlayer}
      />
    )
  }

  return (
    <>
      <PlayersTable
        players={players}
        clubsById={clubsById}
        onAction={onAction}
        loading={players.length === 0}
        stale={query.isPlaceholderData}
      />
      <ListPagination meta={meta} itemLabel="jogadores" onPageChange={onPageChange} />
    </>
  )
}

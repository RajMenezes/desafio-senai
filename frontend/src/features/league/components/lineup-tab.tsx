import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import type { ClubDetail } from '@/features/clubs/types'
import { useLineup } from '../api'
import { LineupEditor } from './lineup-editor'

/** Aba Escalação da página do clube. O editor recomeça sempre que a escalação salva muda. */
export function LineupTab({ club }: { club: ClubDetail }) {
  const query = useLineup(club.id)

  if (query.isError) {
    return (
      <ErrorState
        title="Não foi possível carregar a escalação"
        error={query.error}
        onRetry={() => query.refetch()}
        retrying={query.isFetching}
      />
    )
  }
  if (query.isPending) return <LoadingState label="Carregando escalação…" />

  const lineup = query.data
  const savedKey = `${lineup.formation}:${lineup.players.map((player) => player.id).join(',')}`
  return <LineupEditor key={savedKey} club={club} lineup={lineup} />
}

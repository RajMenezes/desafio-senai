import { ArrowLeft, SearchX } from 'lucide-react'
import { Link, useParams } from 'react-router'
import { usePageTitle } from '@/app/page-title'
import { paths } from '@/app/paths'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import { Button } from '@/components/ui/button'
import { ApiError, getErrorMessage } from '@/lib/api/errors'
import { useMatch } from './api'
import { DuelsTable } from './components/duels-table'
import { MatchScoreboard } from './components/match-scoreboard'
import { useClubAcronyms } from './use-club-acronyms'

function parseMatchId(value: string | undefined): number | undefined {
  return value !== undefined && /^[1-9]\d*$/.test(value) ? Number(value) : undefined
}

function BackToMatches() {
  return (
    <Button variant="ghost" className="self-start" asChild>
      <Link to={paths.matches}>
        <ArrowLeft data-icon="inline-start" aria-hidden />
        Voltar para partidas
      </Link>
    </Button>
  )
}

export function MatchPage() {
  const { matchId } = useParams()
  const id = parseMatchId(matchId)
  const query = useMatch(id ?? 0, id !== undefined)
  const acronymOf = useClubAcronyms()
  const match = query.data
  const notFound =
    id === undefined || (query.error instanceof ApiError && query.error.status === 404)

  usePageTitle(
    match
      ? `${match.home.club_name} ${match.home.goals} × ${match.away.goals} ${match.away.club_name}`
      : `Partida ${matchId ?? ''}`,
  )

  if (notFound) {
    return (
      <>
        <BackToMatches />
        <section className="rounded-xl border bg-card">
          <EmptyState
            icon={SearchX}
            title="Partida não encontrada"
            description={
              query.error ? getErrorMessage(query.error) : 'O endereço pode estar errado.'
            }
          />
        </section>
      </>
    )
  }
  if (query.isError) {
    return (
      <ErrorState
        title="Não foi possível carregar a partida"
        error={query.error}
        onRetry={() => query.refetch()}
        retrying={query.isFetching}
      />
    )
  }
  if (!match) return <LoadingState label="Carregando partida…" />

  return (
    <>
      <BackToMatches />
      <section aria-label="Placar" className="rounded-xl border bg-card p-6">
        <MatchScoreboard match={match} acronymOf={acronymOf} />
      </section>
      <section aria-labelledby="duels-title" className="overflow-hidden rounded-xl border bg-card">
        <div className="p-4">
          <h2 id="duels-title" className="font-semibold">
            Duelos
          </h2>
          <p className="text-sm text-muted-foreground">
            Os 3 ataques do mandante e depois os 3 do visitante. Os jogadores de linha vão do 1º (o
            mais recuado) ao 4º (o mais avançado), pela ordem do elenco: o 4º ataca o goleiro, o 3º
            ataca o 1º e o 2º ataca o 2º. Os valores já trazem o efeito da condição.
          </p>
        </div>
        <DuelsTable match={match} duels={match.duels ?? []} />
      </section>
    </>
  )
}

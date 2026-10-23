import { ArrowRight, ListOrdered, Plus, Shield } from 'lucide-react'
import { Link } from 'react-router'
import { paths } from '@/app/paths'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import { PageHeader } from '@/components/common/page-header'
import { Button } from '@/components/ui/button'
import { formatDateTime } from '@/lib/date-time'
import { cn } from '@/lib/utils'
import { useMatches, useStandings } from './api'
import { StandingsTable } from './components/standings-table'
import type { Match } from './types'
import { useClubAcronyms } from './use-club-acronyms'

const RECENT_MATCHES = 3

const TIEBREAKERS = [
  'Mais pontos',
  'Maior saldo de gols',
  'Mais gols marcados',
  'Nome do clube, de A a Z',
]

function RecentMatches({ matches }: { matches: Match[] }) {
  return (
    <section aria-labelledby="recent-matches-title" className="rounded-xl border bg-card">
      <div className="border-b p-4">
        <h2 id="recent-matches-title" className="font-semibold">
          Últimas partidas
        </h2>
        <p className="text-xs text-muted-foreground">Da mais recente para a mais antiga.</p>
      </div>
      <div className="flex flex-col gap-3 p-4">
        {matches.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nenhuma partida jogada ainda.</p>
        ) : (
          <ul className="flex flex-col gap-3">
            {matches.map((match) => (
              <li key={match.id}>
                <Link to={paths.match(match.id)} className="group flex flex-col gap-0.5">
                  <span className="text-xs text-muted-foreground">
                    {formatDateTime(match.played_at)}
                  </span>
                  <span className="flex flex-wrap items-center gap-2 text-sm group-hover:text-primary">
                    <span className={cn(match.result === 'MANDANTE' && 'font-semibold')}>
                      {match.home.club_name}
                    </span>
                    <span className="rounded bg-muted px-1.5 font-semibold tabular-nums">
                      {match.home.goals} × {match.away.goals}
                    </span>
                    <span className={cn(match.result === 'VISITANTE' && 'font-semibold')}>
                      {match.away.club_name}
                    </span>
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        )}
        <Link
          to={paths.matches}
          className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
        >
          Ver todas as partidas
          <ArrowRight className="size-4" aria-hidden />
        </Link>
      </div>
    </section>
  )
}

function Tiebreakers() {
  return (
    <section aria-labelledby="tiebreakers-title" className="rounded-xl border bg-card">
      <div className="border-b p-4">
        <h2 id="tiebreakers-title" className="font-semibold">
          Ordem da tabela
        </h2>
        <p className="text-xs text-muted-foreground">Critérios de classificação, nesta ordem.</p>
      </div>
      <ol className="flex flex-col gap-2 p-4 text-sm">
        {TIEBREAKERS.map((criterion, index) => (
          <li key={criterion} className="flex items-center gap-3">
            <span className="flex size-5 items-center justify-center rounded border text-xs tabular-nums">
              {index + 1}
            </span>
            {criterion}
          </li>
        ))}
      </ol>
    </section>
  )
}

export function StandingsPage() {
  const standingsQuery = useStandings()
  const matchesQuery = useMatches()
  const acronymOf = useClubAcronyms()

  const content = () => {
    if (standingsQuery.isError) {
      return (
        <ErrorState
          title="Não foi possível carregar a classificação"
          error={standingsQuery.error}
          onRetry={() => standingsQuery.refetch()}
          retrying={standingsQuery.isFetching}
        />
      )
    }
    if (standingsQuery.isPending) return <LoadingState label="Carregando classificação…" />
    if (standingsQuery.data.length === 0) {
      return (
        <EmptyState
          icon={Shield}
          title="Nenhum clube cadastrado"
          description="A classificação mostra todos os clubes, mesmo os que ainda não jogaram."
        />
      )
    }
    return (
      <>
        <StandingsTable standings={standingsQuery.data} acronymOf={acronymOf} />
        <p className="border-t px-4 py-3 text-xs text-muted-foreground">
          J: jogos · V: vitórias · E: empates · D: derrotas · GP: gols marcados · GC: gols sofridos
          · SG: saldo de gols · Pts: pontos
        </p>
      </>
    )
  }

  return (
    <>
      <PageHeader
        icon={ListOrdered}
        title="Classificação"
        description="Todos os clubes, com 3 pontos por vitória e 1 por empate."
        actions={
          <Button variant="outline" asChild>
            <Link to={paths.newMatch}>
              <Plus data-icon="inline-start" aria-hidden />
              Nova partida
            </Link>
          </Button>
        }
      />
      <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
        <section
          aria-label="Tabela de classificação"
          className="overflow-hidden rounded-xl border bg-card"
        >
          {content()}
        </section>
        <div className="flex flex-col gap-4">
          <RecentMatches matches={(matchesQuery.data ?? []).slice(0, RECENT_MATCHES)} />
          <Tiebreakers />
        </div>
      </div>
    </>
  )
}

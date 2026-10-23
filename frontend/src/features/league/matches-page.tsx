import { ChevronRight, Plus, Shield, Swords } from 'lucide-react'
import { Link, useSearchParams } from 'react-router'
import { NEW_MATCH_PARAM, paths } from '@/app/paths'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import { PageHeader } from '@/components/common/page-header'
import { SelectFilter } from '@/components/common/select-filter'
import { Button } from '@/components/ui/button'
import { useClubs } from '@/features/clubs/api'
import { formatInteger } from '@/lib/format'
import { cn } from '@/lib/utils'
import { useMatches } from './api'
import { MatchScoreboard } from './components/match-scoreboard'
import { MatchesTable } from './components/matches-table'
import { NewMatchDialog } from './components/new-match-dialog'
import type { Match } from './types'
import { useClubAcronyms } from './use-club-acronyms'

const CLUB_PARAM = 'club_id'

function parseClubId(value: string | null): number | undefined {
  return value !== null && /^[1-9]\d*$/.test(value) ? Number(value) : undefined
}

function LastMatch({ match, acronymOf }: { match: Match; acronymOf: (id: number) => string }) {
  return (
    <section aria-labelledby="last-match-title" className="flex flex-col gap-2">
      <h2 id="last-match-title" className="text-sm text-muted-foreground">
        Última partida
      </h2>
      <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl border bg-card p-6">
        <div className="min-w-0 flex-1">
          <MatchScoreboard match={match} acronymOf={acronymOf} />
        </div>
        <Button variant="outline" asChild>
          <Link to={paths.match(match.id)}>
            Ver duelos
            <ChevronRight data-icon="inline-end" aria-hidden />
          </Link>
        </Button>
      </div>
    </section>
  )
}

export function MatchesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const clubId = parseClubId(searchParams.get(CLUB_PARAM))
  const newMatchOpen = searchParams.has(NEW_MATCH_PARAM)
  const clubsQuery = useClubs()
  const clubs = clubsQuery.data ?? []
  const allMatchesQuery = useMatches()
  const matchesQuery = useMatches(clubId)
  const acronymOf = useClubAcronyms()

  const updateParams = (change: (params: URLSearchParams) => void) => {
    const next = new URLSearchParams(searchParams)
    change(next)
    setSearchParams(next, { replace: true })
  }
  const setNewMatchOpen = (open: boolean) =>
    updateParams((params) =>
      open ? params.set(NEW_MATCH_PARAM, '') : params.delete(NEW_MATCH_PARAM),
    )
  const setClubId = (value: number | undefined) =>
    updateParams((params) =>
      value === undefined ? params.delete(CLUB_PARAM) : params.set(CLUB_PARAM, String(value)),
    )

  const lastMatch = allMatchesQuery.data?.[0]

  const content = () => {
    if (matchesQuery.isError) {
      return (
        <ErrorState
          title="Não foi possível carregar as partidas"
          error={matchesQuery.error}
          onRetry={() => matchesQuery.refetch()}
          retrying={matchesQuery.isFetching}
        />
      )
    }
    if (matchesQuery.isPending) return <LoadingState label="Carregando partidas…" />
    const matches = matchesQuery.data
    if (matches.length === 0) {
      return clubId === undefined ? (
        <EmptyState
          icon={Swords}
          title="Nenhuma partida jogada"
          description="Escale os titulares de dois clubes e jogue a primeira partida."
          action={
            <Button variant="outline" onClick={() => setNewMatchOpen(true)}>
              <Plus data-icon="inline-start" aria-hidden />
              Nova partida
            </Button>
          }
        />
      ) : (
        <EmptyState
          icon={Swords}
          title="Nenhuma partida deste clube"
          description="O clube ainda não jogou como mandante nem como visitante."
        />
      )
    }
    return (
      <div className={cn('transition-opacity', matchesQuery.isPlaceholderData && 'opacity-60')}>
        <MatchesTable matches={matches} acronymOf={acronymOf} />
        <p className="border-t px-4 py-3 text-sm text-muted-foreground">
          {formatInteger(matches.length)} {matches.length === 1 ? 'partida' : 'partidas'}
        </p>
      </div>
    )
  }

  return (
    <>
      <PageHeader
        icon={Swords}
        title="Partidas"
        description="Jogue partidas entre clubes com os 5 titulares escalados e acompanhe o histórico."
        actions={
          <Button onClick={() => setNewMatchOpen(true)}>
            <Plus data-icon="inline-start" aria-hidden />
            Nova partida
          </Button>
        }
      />
      {lastMatch && <LastMatch match={lastMatch} acronymOf={acronymOf} />}
      <section
        aria-labelledby="matches-title"
        className="overflow-hidden rounded-xl border bg-card"
      >
        <div className="flex flex-wrap items-start justify-between gap-3 p-4">
          <div>
            <h2 id="matches-title" className="font-semibold">
              Histórico de partidas
            </h2>
            <p className="text-sm text-muted-foreground">Da mais recente para a mais antiga.</p>
          </div>
          <SelectFilter
            icon={Shield}
            label="Clube"
            allLabel="Todos os clubes"
            value={clubId}
            options={clubs.map((club) => ({ value: club.id, label: club.name }))}
            onChange={setClubId}
          />
        </div>
        {content()}
      </section>
      <NewMatchDialog open={newMatchOpen} clubs={clubs} onOpenChange={setNewMatchOpen} />
    </>
  )
}

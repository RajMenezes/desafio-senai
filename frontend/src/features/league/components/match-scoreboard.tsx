import { Badge } from '@/components/ui/badge'
import { ClubTile } from '@/features/clubs/components/club-tile'
import { formatDateTime } from '@/lib/date-time'
import { cn } from '@/lib/utils'
import { MATCH_RESULT_LABELS, type Match, type MatchTeam } from '../types'

interface TeamSideProps {
  team: MatchTeam
  role: 'Mandante' | 'Visitante'
  acronym: string
  align: 'start' | 'end'
}

function TeamSide({ team, role, acronym, align }: TeamSideProps) {
  const text = (
    <div className={cn('leading-tight', align === 'start' ? 'text-right' : 'text-left')}>
      <p className="font-semibold">{team.club_name}</p>
      <p className="text-xs text-muted-foreground">
        {role} · {team.formation}
      </p>
    </div>
  )
  const tile = <ClubTile acronym={acronym} size="lg" />
  return (
    <div className="flex items-center gap-3">
      {align === 'start' ? (
        <>
          {text}
          {tile}
        </>
      ) : (
        <>
          {tile}
          {text}
        </>
      )}
    </div>
  )
}

interface MatchScoreboardProps {
  match: Match
  acronymOf: (clubId: number) => string
}

/** Placar da partida: mandante à esquerda, visitante à direita, como a API devolve. */
export function MatchScoreboard({ match, acronymOf }: MatchScoreboardProps) {
  return (
    <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-4">
      <div className="flex justify-end">
        <TeamSide
          team={match.home}
          role="Mandante"
          acronym={acronymOf(match.home.club_id)}
          align="start"
        />
      </div>
      <div className="flex flex-col items-center gap-1">
        <p
          className="rounded-lg bg-muted px-4 py-1 text-2xl font-semibold tabular-nums"
          aria-label={`Placar: ${match.home.goals} a ${match.away.goals}`}
        >
          {match.home.goals} × {match.away.goals}
        </p>
        <p className="text-xs text-muted-foreground">{formatDateTime(match.played_at)}</p>
        <Badge variant="outline">{MATCH_RESULT_LABELS[match.result]}</Badge>
      </div>
      <TeamSide
        team={match.away}
        role="Visitante"
        acronym={acronymOf(match.away.club_id)}
        align="end"
      />
    </div>
  )
}

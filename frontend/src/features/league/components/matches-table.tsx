import { ChevronRight } from 'lucide-react'
import { Link } from 'react-router'
import { paths } from '@/app/paths'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { formatDateTimeParts } from '@/lib/date-time'
import { cn } from '@/lib/utils'
import { MATCH_RESULT_LABELS, type Match, type MatchTeam } from '../types'

function TeamCell({
  team,
  acronym,
  winner,
  align,
}: {
  team: MatchTeam
  acronym: string
  winner: boolean
  align: 'start' | 'end'
}) {
  const name = <span className={cn(winner && 'font-semibold')}>{team.club_name}</span>
  const badge = (
    <Badge variant="outline" className="rounded-md">
      {acronym}
    </Badge>
  )
  return (
    <span className="inline-flex items-center gap-2">
      {align === 'start' ? (
        <>
          {name}
          {badge}
        </>
      ) : (
        <>
          {badge}
          {name}
        </>
      )}
    </span>
  )
}

interface MatchesTableProps {
  matches: Match[]
  acronymOf: (clubId: number) => string
}

/** Histórico na ordem da API (da mais recente para a mais antiga); o vencedor fica em negrito. */
export function MatchesTable({ matches, acronymOf }: MatchesTableProps) {
  return (
    <Table>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          <TableHead className="px-4">Data e hora</TableHead>
          <TableHead className="px-2 text-right">Mandante</TableHead>
          <TableHead className="px-2 text-center">Placar</TableHead>
          <TableHead className="px-2">Visitante</TableHead>
          <TableHead className="px-2">Resultado</TableHead>
          <TableHead className="px-4">
            <span className="sr-only">Ações</span>
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {matches.map((match) => {
          const { date, time } = formatDateTimeParts(match.played_at)
          return (
            <TableRow key={match.id}>
              <TableCell className="px-4 tabular-nums">
                {date} <span className="text-muted-foreground">{time}</span>
              </TableCell>
              <TableCell className="px-2">
                <span className="flex justify-end">
                  <TeamCell
                    team={match.home}
                    acronym={acronymOf(match.home.club_id)}
                    winner={match.result === 'MANDANTE'}
                    align="start"
                  />
                </span>
              </TableCell>
              <TableCell className="px-2 text-center">
                <Badge variant="secondary" className="font-semibold tabular-nums">
                  {match.home.goals} × {match.away.goals}
                </Badge>
              </TableCell>
              <TableCell className="px-2">
                <TeamCell
                  team={match.away}
                  acronym={acronymOf(match.away.club_id)}
                  winner={match.result === 'VISITANTE'}
                  align="end"
                />
              </TableCell>
              <TableCell className="px-2">
                <Badge variant="outline">{MATCH_RESULT_LABELS[match.result]}</Badge>
              </TableCell>
              <TableCell className="px-4 text-right">
                <Link
                  to={paths.match(match.id)}
                  className="inline-flex items-center gap-1 font-medium hover:text-primary"
                  aria-label={`Ver duelos de ${match.home.club_name} ${match.home.goals} × ${match.away.goals} ${match.away.club_name}`}
                >
                  Ver duelos
                  <ChevronRight className="size-4" aria-hidden />
                </Link>
              </TableCell>
            </TableRow>
          )
        })}
      </TableBody>
    </Table>
  )
}

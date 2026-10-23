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
import { formatSignedInteger } from '@/lib/format'
import { cn } from '@/lib/utils'
import type { Standing } from '../types'

const STAT_COLUMNS = [
  { key: 'played', abbr: 'J', title: 'Jogos' },
  { key: 'wins', abbr: 'V', title: 'Vitórias' },
  { key: 'draws', abbr: 'E', title: 'Empates' },
  { key: 'losses', abbr: 'D', title: 'Derrotas' },
  { key: 'goals_for', abbr: 'GP', title: 'Gols marcados' },
  { key: 'goals_against', abbr: 'GC', title: 'Gols sofridos' },
] as const

interface StandingsTableProps {
  standings: Standing[]
  acronymOf: (clubId: number) => string
}

/** Classificação na ordem devolvida pela API, sem recalcular pontos, saldo ou posição. */
export function StandingsTable({ standings, acronymOf }: StandingsTableProps) {
  return (
    <Table>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          <TableHead className="w-12 px-4 text-center">
            <abbr title="Posição" className="no-underline">
              #
            </abbr>
          </TableHead>
          <TableHead className="px-2">Clube</TableHead>
          {STAT_COLUMNS.map((column) => (
            <TableHead key={column.key} className="px-2 text-center">
              <abbr title={column.title} className="no-underline">
                {column.abbr}
              </abbr>
            </TableHead>
          ))}
          <TableHead className="px-2 text-center">
            <abbr title="Saldo de gols" className="no-underline">
              SG
            </abbr>
          </TableHead>
          <TableHead className="px-4 text-center">
            <abbr title="Pontos" className="no-underline">
              Pts
            </abbr>
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {standings.map((standing) => (
          <TableRow key={standing.club_id}>
            <TableCell className="px-4 text-center">
              <span
                className={cn(
                  'inline-flex size-6 items-center justify-center rounded-md border text-xs font-semibold tabular-nums',
                  standing.position === 1 && 'border-primary bg-primary text-primary-foreground',
                )}
              >
                {standing.position}
              </span>
            </TableCell>
            <TableCell className="px-2">
              <span className="flex items-center gap-2">
                <Badge variant="outline" className="rounded-md">
                  {acronymOf(standing.club_id)}
                </Badge>
                <Link
                  to={paths.club(standing.club_id)}
                  className="font-medium underline underline-offset-4 hover:text-primary"
                >
                  {standing.club_name}
                </Link>
              </span>
            </TableCell>
            {STAT_COLUMNS.map((column) => (
              <TableCell key={column.key} className="px-2 text-center tabular-nums">
                {standing[column.key]}
              </TableCell>
            ))}
            <TableCell className="px-2 text-center tabular-nums">
              {formatSignedInteger(standing.goal_difference)}
            </TableCell>
            <TableCell className="px-4 text-center font-semibold tabular-nums">
              {standing.points}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

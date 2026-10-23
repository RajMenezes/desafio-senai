import { Link } from 'react-router'
import { TableSkeleton } from '@/components/common/table-skeleton'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import type { Club } from '@/features/clubs/types'
import { formatMoney } from '@/lib/format'
import { cn } from '@/lib/utils'
import { POSITION_LABELS, type Player } from '../types'
import { AgilityBar, ConditionBadge, PlayerAvatar, PositionBadge } from './player-cells'

const COLUMNS = [
  { label: 'Jogador' },
  { label: 'Posição' },
  { label: 'Overall', numeric: true },
  { label: 'Agilidade' },
  { label: 'Idade', numeric: true },
  { label: 'Condição' },
  { label: 'Valor base', numeric: true },
  { label: 'Valor de mercado', numeric: true },
  { label: 'Clube' },
]

function ClubCell({ clubId, clubsById }: { clubId: number | null; clubsById: Map<number, Club> }) {
  if (clubId === null) {
    return <Badge variant="secondary">Livre</Badge>
  }
  const club = clubsById.get(clubId)
  return (
    <div className="flex items-center gap-2">
      {club && <span className="text-xs font-semibold">{club.acronym}</span>}
      <Link to={`/clubes/${clubId}`} className="underline underline-offset-4 hover:text-primary">
        {club?.name ?? `Clube ${clubId}`}
      </Link>
    </div>
  )
}

interface PlayersTableProps {
  players: Player[]
  clubsById: Map<number, Club>
  loading?: boolean
  stale?: boolean
}

export function PlayersTable({
  players,
  clubsById,
  loading = false,
  stale = false,
}: PlayersTableProps) {
  return (
    <Table aria-busy={loading || stale}>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHead key={column.label} className={cn('px-4', column.numeric && 'text-right')}>
              {column.label}
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      {loading ? (
        <TableSkeleton columns={COLUMNS.length} />
      ) : (
        <TableBody className={cn('transition-opacity', stale && 'opacity-60')}>
          {players.map((player) => (
            <TableRow key={player.id}>
              <TableCell className="px-4">
                <div className="flex items-center gap-3">
                  <PlayerAvatar id={player.id} name={player.name} />
                  <div className="leading-tight">
                    <p className="font-medium">{player.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {POSITION_LABELS[player.position]}
                    </p>
                  </div>
                </div>
              </TableCell>
              <TableCell className="px-4">
                <PositionBadge position={player.position} />
              </TableCell>
              <TableCell className="px-4 text-right font-semibold tabular-nums">
                {player.overall}
              </TableCell>
              <TableCell className="px-4">
                <AgilityBar agility={player.agility} />
              </TableCell>
              <TableCell className="px-4 text-right tabular-nums">{player.age}</TableCell>
              <TableCell className="px-4">
                <ConditionBadge condition={player.condition} />
              </TableCell>
              <TableCell className="px-4 text-right text-muted-foreground tabular-nums">
                {formatMoney(player.base_value)}
              </TableCell>
              <TableCell className="px-4 text-right font-semibold tabular-nums">
                {formatMoney(player.market_value)}
              </TableCell>
              <TableCell className="px-4">
                <ClubCell clubId={player.club_id} clubsById={clubsById} />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      )}
    </Table>
  )
}

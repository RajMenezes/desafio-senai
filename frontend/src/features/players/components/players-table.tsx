import { ShoppingCart } from 'lucide-react'
import { Link } from 'react-router'
import { paths } from '@/app/paths'
import { TableSkeleton } from '@/components/common/table-skeleton'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
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
import { PlayerActionsMenu, type PlayerAction } from './player-actions-menu'
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
  { label: 'Ações', hidden: true },
]

function ClubCell({ clubId, clubsById }: { clubId: number | null; clubsById: Map<number, Club> }) {
  if (clubId === null) {
    return <Badge variant="secondary">Livre</Badge>
  }
  const club = clubsById.get(clubId)
  return (
    <div className="flex items-center gap-2">
      {club && <span className="text-xs font-semibold">{club.acronym}</span>}
      <Link to={paths.club(clubId)} className="underline underline-offset-4 hover:text-primary">
        {club?.name ?? `Clube ${clubId}`}
      </Link>
    </div>
  )
}

interface PlayersTableProps {
  players: Player[]
  clubsById: Map<number, Club>
  onAction: (action: PlayerAction, player: Player) => void
  loading?: boolean
  stale?: boolean
}

export function PlayersTable({
  players,
  clubsById,
  onAction,
  loading = false,
  stale = false,
}: PlayersTableProps) {
  return (
    <Table aria-busy={loading || stale}>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHead key={column.label} className={cn('px-3', column.numeric && 'text-right')}>
              {column.hidden ? <span className="sr-only">{column.label}</span> : column.label}
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
              <TableCell className="px-3">
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
              <TableCell className="px-3">
                <PositionBadge position={player.position} />
              </TableCell>
              <TableCell className="px-3 text-right font-semibold tabular-nums">
                {player.overall}
              </TableCell>
              <TableCell className="px-3">
                <AgilityBar agility={player.agility} />
              </TableCell>
              <TableCell className="px-3 text-right tabular-nums">{player.age}</TableCell>
              <TableCell className="px-3">
                <ConditionBadge condition={player.condition} />
              </TableCell>
              <TableCell className="px-3 text-right text-muted-foreground tabular-nums">
                {formatMoney(player.base_value)}
              </TableCell>
              <TableCell className="px-3 text-right font-semibold tabular-nums">
                {formatMoney(player.market_value)}
              </TableCell>
              <TableCell className="px-3">
                <ClubCell clubId={player.club_id} clubsById={clubsById} />
              </TableCell>
              <TableCell className="px-3 text-right">
                <div className="flex items-center justify-end gap-1">
                  {player.club_id === null && (
                    <Button
                      variant="outline"
                      size="sm"
                      aria-label={`Comprar ${player.name}`}
                      onClick={() => onAction('buy', player)}
                    >
                      <ShoppingCart data-icon="inline-start" aria-hidden />
                      Comprar
                    </Button>
                  )}
                  <PlayerActionsMenu player={player} onAction={onAction} />
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      )}
    </Table>
  )
}

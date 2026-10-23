import { HandCoins } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  ConditionBadge,
  PlayerAvatar,
  PositionBadge,
} from '@/features/players/components/player-cells'
import { formatDateTimeParts } from '@/lib/date-time'
import { formatInteger, formatMoney } from '@/lib/format'
import { cn } from '@/lib/utils'
import type { SquadPlayer } from '../types'
import { PenaltyBadge } from './penalty-badge'

const COLUMNS = [
  { label: 'Jogador' },
  { label: 'Posição' },
  { label: 'Overall', numeric: true },
  { label: 'Condição' },
  { label: 'Valor de mercado', numeric: true },
  { label: 'Valor de compra', numeric: true },
  { label: 'Fim da fidelidade' },
  { label: 'Dias restantes', numeric: true },
  { label: '% de multa' },
  { label: 'Ações', hidden: true },
]

function LoyaltyUntil({ value }: { value: string }) {
  const { date, time } = formatDateTimeParts(value)
  return (
    <span className="flex flex-col leading-tight">
      <span>{date}</span>
      <span className="text-xs text-muted-foreground">{time}</span>
    </span>
  )
}

interface SquadTableProps {
  players: SquadPlayer[]
  onSell: (player: SquadPlayer) => void
}

export function SquadTable({ players, onSell }: SquadTableProps) {
  return (
    <Table>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHead
              key={column.label}
              className={cn('px-2 whitespace-normal', column.numeric && 'text-right')}
            >
              {column.hidden ? <span className="sr-only">{column.label}</span> : column.label}
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody>
        {players.map((player) => (
          <TableRow key={player.id}>
            <TableCell className="px-2">
              <div className="flex items-center gap-3">
                <PlayerAvatar id={player.id} name={player.name} />
                <span className="font-medium">{player.name}</span>
              </div>
            </TableCell>
            <TableCell className="px-2">
              <PositionBadge position={player.position} />
            </TableCell>
            <TableCell className="px-2 text-right font-semibold tabular-nums">
              {player.overall}
            </TableCell>
            <TableCell className="px-2">
              <ConditionBadge condition={player.condition} />
            </TableCell>
            <TableCell className="px-2 text-right font-semibold tabular-nums">
              {formatMoney(player.market_value)}
            </TableCell>
            <TableCell className="px-2 text-right text-muted-foreground tabular-nums">
              {formatMoney(player.contract.purchase_value)}
            </TableCell>
            <TableCell className="px-2 tabular-nums">
              <LoyaltyUntil value={player.contract.loyalty_until} />
            </TableCell>
            <TableCell className="px-2 text-right tabular-nums">
              {formatInteger(player.contract.remaining_days)}
            </TableCell>
            <TableCell className="px-2">
              <PenaltyBadge rate={player.contract.penalty_rate} />
            </TableCell>
            <TableCell className="px-2 text-right">
              <Button
                variant="outline"
                size="sm"
                aria-label={`Vender ${player.name}`}
                onClick={() => onSell(player)}
              >
                <HandCoins data-icon="inline-start" aria-hidden />
                Vender
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

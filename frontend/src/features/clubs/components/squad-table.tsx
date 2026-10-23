import { Check } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
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
]

/** Percentual de multa da RN15 vindo da API: sem multa em verde, a maior em vermelho. */
function PenaltyBadge({ rate }: { rate: number }) {
  if (rate === 0) {
    return (
      <Badge variant="success">
        <Check aria-hidden />
        0%
      </Badge>
    )
  }
  return <Badge variant={rate >= 20 ? 'danger' : 'warning'}>{rate}%</Badge>
}

function LoyaltyUntil({ value }: { value: string }) {
  const { date, time } = formatDateTimeParts(value)
  return (
    <span className="flex flex-col leading-tight">
      <span>{date}</span>
      <span className="text-xs text-muted-foreground">{time}</span>
    </span>
  )
}

export function SquadTable({ players }: { players: SquadPlayer[] }) {
  return (
    <Table>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHead
              key={column.label}
              className={cn('px-2 whitespace-normal', column.numeric && 'text-right')}
            >
              {column.label}
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
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

import { HandCoins, ShoppingCart } from 'lucide-react'
import { SignedMoney } from '@/components/common/signed-money'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { PlayerAvatar } from '@/features/players/components/player-cells'
import { formatDateTimeParts } from '@/lib/date-time'
import { formatMoney } from '@/lib/format'
import { cn } from '@/lib/utils'
import type { Transaction, TransactionType } from '../types'

const COLUMNS = [
  { label: 'Data e hora' },
  { label: 'Tipo' },
  { label: 'Jogador' },
  { label: 'Valor', numeric: true },
  { label: 'Taxa ou comissão', numeric: true },
  { label: 'Multa', numeric: true },
  { label: 'Lucro', numeric: true },
  { label: 'Variação do caixa', numeric: true },
]

function TypeBadge({ type }: { type: TransactionType }) {
  return type === 'COMPRA' ? (
    <Badge variant="outline" className="rounded-md">
      <ShoppingCart aria-hidden />
      Compra
    </Badge>
  ) : (
    <Badge variant="secondary" className="rounded-md">
      <HandCoins aria-hidden />
      Venda
    </Badge>
  )
}

function CreatedAt({ value }: { value: string }) {
  const { date, time } = formatDateTimeParts(value)
  return (
    <span className="tabular-nums">
      {date} <span className="text-muted-foreground">{time}</span>
    </span>
  )
}

/** Transações na ordem da API (da mais recente para a mais antiga), sem recalcular valores. */
export function TransactionsTable({ transactions }: { transactions: Transaction[] }) {
  return (
    <Table>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHead key={column.label} className={cn('px-3', column.numeric && 'text-right')}>
              {column.label}
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody>
        {transactions.map((transaction) => (
          <TableRow key={transaction.id}>
            <TableCell className="px-3">
              <CreatedAt value={transaction.created_at} />
            </TableCell>
            <TableCell className="px-3">
              <TypeBadge type={transaction.type} />
            </TableCell>
            <TableCell className="px-3">
              <div className="flex items-center gap-3">
                <PlayerAvatar id={transaction.player_id} name={transaction.player_name} />
                <span className="font-medium">{transaction.player_name}</span>
              </div>
            </TableCell>
            <TableCell className="px-3 text-right tabular-nums">
              {formatMoney(transaction.value)}
            </TableCell>
            <TableCell className="px-3 text-right tabular-nums">
              {formatMoney(transaction.fee)}
            </TableCell>
            <TableCell className="px-3 text-right tabular-nums">
              {formatMoney(transaction.penalty)}
            </TableCell>
            <TableCell className="px-3 text-right tabular-nums">
              {transaction.profit === null ? (
                <span className="text-muted-foreground" aria-label="Sem lucro (compra)">
                  –
                </span>
              ) : (
                <SignedMoney value={transaction.profit} />
              )}
            </TableCell>
            <TableCell className="px-3 text-right font-semibold tabular-nums">
              <SignedMoney value={transaction.cash_change} />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

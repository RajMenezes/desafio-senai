import {
  BadgePercent,
  HandCoins,
  ShoppingCart,
  TrendingUp,
  TriangleAlert,
  type LucideIcon,
} from 'lucide-react'
import type { ReactNode } from 'react'
import { SignedMoney } from '@/components/common/signed-money'
import { formatMoney } from '@/lib/format'
import type { TransactionSummary } from '../types'

interface SummaryItem {
  label: string
  hint: string
  icon: LucideIcon
  value: ReactNode
}

/** Totais do `summary` devolvido pela API, já respeitando os filtros (RN17). */
export function TransactionsSummary({ summary }: { summary: TransactionSummary }) {
  const items: SummaryItem[] = [
    {
      label: 'Total pago em compras',
      hint: 'Valor + taxa',
      icon: ShoppingCart,
      value: formatMoney(summary.total_spent),
    },
    {
      label: 'Total recebido em vendas',
      hint: 'Crédito líquido',
      icon: HandCoins,
      value: formatMoney(summary.total_received),
    },
    {
      label: 'Taxas e comissões',
      hint: 'Somadas',
      icon: BadgePercent,
      value: formatMoney(summary.total_fees),
    },
    {
      label: 'Multas',
      hint: 'Por fidelidade',
      icon: TriangleAlert,
      value: formatMoney(summary.total_penalties),
    },
    {
      label: 'Lucro total',
      hint: 'Soma dos lucros, com sinal',
      icon: TrendingUp,
      value: <SignedMoney value={summary.total_profit} />,
    },
  ]

  return (
    <dl
      aria-label="Totais do histórico"
      className="grid divide-y border-t bg-muted-subtle sm:grid-cols-5 sm:divide-x sm:divide-y-0"
    >
      {items.map(({ label, hint, icon: Icon, value }) => (
        <div key={label} className="flex flex-col gap-0.5 px-4 py-3">
          <dt className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <Icon className="size-3.5" aria-hidden />
            {label}
          </dt>
          <dd className="text-lg font-semibold tabular-nums">{value}</dd>
          <dd className="text-xs text-muted-foreground">{hint}</dd>
        </div>
      ))}
    </dl>
  )
}

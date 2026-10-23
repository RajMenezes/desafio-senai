import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

export interface ResultRow {
  label: string
  hint?: string
  value: ReactNode
  highlight?: boolean
}

/** Lista de valores de um resultado (compra, venda, cadastro), com linhas em destaque. */
export function ResultRows({ rows }: { rows: ResultRow[] }) {
  return (
    <dl className="divide-y rounded-lg border">
      {rows.map((row) => (
        <div
          key={row.label}
          className={cn(
            'flex items-center justify-between gap-4 px-4 py-2.5',
            row.highlight && 'bg-muted-subtle',
          )}
        >
          <div>
            <dt className={row.highlight ? 'font-semibold' : 'text-muted-foreground'}>
              {row.label}
            </dt>
            {row.hint && <p className="text-xs text-muted-foreground">{row.hint}</p>}
          </div>
          <dd
            className={cn(
              'text-right tabular-nums',
              row.highlight ? 'text-lg font-semibold' : 'font-medium',
            )}
          >
            {row.value}
          </dd>
        </div>
      ))}
    </dl>
  )
}

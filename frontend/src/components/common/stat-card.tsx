import type { LucideIcon } from 'lucide-react'

interface StatCardProps {
  label: string
  value: string
  unit?: string
  hint?: string
  icon?: LucideIcon
}

/** Indicador em destaque: rótulo, valor grande (com unidade opcional) e uma linha de explicação. */
export function StatCard({ label, value, unit, hint, icon: Icon }: StatCardProps) {
  return (
    <div className="flex flex-col gap-1 rounded-xl border bg-card p-4">
      <div className="flex items-center justify-between gap-2 text-sm text-muted-foreground">
        <span>{label}</span>
        {Icon && <Icon className="size-4" aria-hidden />}
      </div>
      <p className="text-2xl font-semibold tabular-nums">
        {value}
        {unit && <span className="ml-1.5 text-sm font-normal text-muted-foreground">{unit}</span>}
      </p>
      {hint && <p className="text-xs text-muted-foreground">{hint}</p>}
    </div>
  )
}

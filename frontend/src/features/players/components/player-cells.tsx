import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'
import { initialsOf } from '../player-names'
import { CONDITION_LABELS, type Condition, type Position } from '../types'

const AVATAR_COLORS = [
  'bg-avatar-zinc text-avatar-zinc-foreground',
  'bg-avatar-pink text-avatar-pink-foreground',
  'bg-avatar-cyan text-avatar-cyan-foreground',
  'bg-avatar-teal text-avatar-teal-foreground',
  'bg-avatar-violet text-avatar-violet-foreground',
]

export function PlayerAvatar({ id, name }: { id: number; name: string }) {
  return (
    <span
      aria-hidden
      className={cn(
        'flex size-8 shrink-0 items-center justify-center rounded-full text-xs font-medium',
        AVATAR_COLORS[id % AVATAR_COLORS.length],
      )}
    >
      {initialsOf(name)}
    </span>
  )
}

export function PositionBadge({ position }: { position: Position }) {
  return (
    <Badge variant="outline" className="rounded-md font-semibold">
      {position}
    </Badge>
  )
}

const CONDITION_VARIANTS = {
  APTO: 'success',
  MAL_CONDICIONADO: 'warning',
  DOENTE: 'danger',
} as const

export function ConditionBadge({ condition }: { condition: Condition }) {
  return (
    <Badge variant={CONDITION_VARIANTS[condition]}>
      <span className="size-1.5 rounded-full bg-current" aria-hidden />
      {CONDITION_LABELS[condition]}
    </Badge>
  )
}

/** Faixas da RN03: de 80 a 100 reduz o desconto por condição; de 1 a 30 aumenta. */
function agilityColor(agility: number): string {
  if (agility >= 80) return 'bg-success'
  if (agility <= 30) return 'bg-destructive'
  return 'bg-muted-foreground'
}

export function AgilityBar({ agility }: { agility: number }) {
  return (
    <div className="flex items-center gap-2">
      <span className="w-6 text-right tabular-nums">{agility}</span>
      <span className="h-1.5 w-10 overflow-hidden rounded-full bg-muted" aria-hidden>
        <span
          className={cn('block h-full rounded-full', agilityColor(agility))}
          style={{ width: `${agility}%` }}
        />
      </span>
    </div>
  )
}

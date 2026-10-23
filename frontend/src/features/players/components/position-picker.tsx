import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'
import { POSITION_LABELS, POSITIONS, type Position } from '../types'

interface PositionPickerProps {
  name: string
  value: Position
  onChange: (position: Position) => void
  lockedTo?: Position
  hint?: ReactNode
}

/** Escolha da posição em cartões; com `lockedTo`, as outras posições ficam desabilitadas. */
export function PositionPicker({ name, value, onChange, lockedTo, hint }: PositionPickerProps) {
  return (
    <fieldset className="flex flex-col gap-2">
      <legend className="mb-2 text-sm font-medium">Posição</legend>
      <div className="grid grid-cols-3 gap-2 sm:grid-cols-5">
        {POSITIONS.map((position) => (
          <label
            key={position}
            className={cn(
              'flex cursor-pointer flex-col items-center rounded-lg border px-2 py-2 text-center transition-colors',
              'hover:bg-muted has-checked:border-primary has-checked:bg-primary-soft has-checked:text-primary',
              'has-focus-visible:ring-3 has-focus-visible:ring-ring/50',
              'has-disabled:cursor-not-allowed has-disabled:opacity-50 has-disabled:hover:bg-transparent',
            )}
          >
            <input
              type="radio"
              name={name}
              value={position}
              checked={value === position}
              disabled={lockedTo !== undefined && position !== lockedTo}
              onChange={() => onChange(position)}
              className="sr-only"
            />
            <span className="text-sm font-semibold">{position}</span>
            <span className="text-xs text-muted-foreground">{POSITION_LABELS[position]}</span>
          </label>
        ))}
      </div>
      {hint && <p className="text-xs text-muted-foreground">{hint}</p>}
    </fieldset>
  )
}

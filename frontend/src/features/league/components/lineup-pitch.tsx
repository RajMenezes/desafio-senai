import { Plus } from 'lucide-react'
import { initialsOf } from '@/features/players/player-names'
import type { Player } from '@/features/players/types'
import type { LineupSlot, SlotSector } from '../formations'

/** Linhas do campo, do ataque (em cima) ao goleiro (embaixo). */
const ROWS: SlotSector[] = ['attack', 'midfield', 'defense', 'goal']

function lastName(name: string): string {
  return name.trim().split(/\s+/).at(-1) ?? name
}

function Marker({ slot, player }: { slot: LineupSlot; player?: Player }) {
  if (!player) {
    return (
      <div className="flex flex-col items-center gap-1">
        <span className="flex size-9 items-center justify-center rounded-full border-2 border-dashed border-white/60 text-white/80">
          <Plus className="size-4" aria-hidden />
        </span>
        <span className="rounded bg-black/40 px-1.5 text-xs text-white">{slot.label}</span>
      </div>
    )
  }
  return (
    <div className="relative flex flex-col items-center gap-1">
      <span className="flex size-9 items-center justify-center rounded-full border-2 border-white bg-primary text-xs font-semibold text-primary-foreground">
        {initialsOf(player.name)}
      </span>
      {player.condition === 'MAL_CONDICIONADO' && (
        <span
          className="absolute -top-1 -right-1 size-3 rounded-full border-2 border-white bg-warning"
          title="Mal condicionado"
        />
      )}
      <span className="rounded bg-black/40 px-1.5 text-xs text-white">{lastName(player.name)}</span>
    </div>
  )
}

interface LineupPitchProps {
  slots: LineupSlot[]
  chosen: (number | undefined)[]
  playersById: Map<number, Player>
}

/** Prévia visual da formação; vagas vazias ficam tracejadas. */
export function LineupPitch({ slots, chosen, playersById }: LineupPitchProps) {
  return (
    <div
      aria-hidden
      className="relative flex aspect-[4/5] flex-col justify-around overflow-hidden rounded-lg bg-[repeating-linear-gradient(to_bottom,#14532d_0,#14532d_12.5%,#166534_12.5%,#166534_25%)] p-4"
    >
      <span className="pointer-events-none absolute inset-3 rounded border border-white/30" />
      <span className="pointer-events-none absolute inset-x-3 top-1/2 border-t border-white/30" />
      <span className="pointer-events-none absolute top-1/2 left-1/2 size-20 -translate-x-1/2 -translate-y-1/2 rounded-full border border-white/30" />
      <span className="pointer-events-none absolute bottom-3 left-1/2 h-12 w-32 -translate-x-1/2 border border-b-0 border-white/30" />
      {ROWS.map((sector) => {
        const indexes = slots.flatMap((slot, index) => (slot.sector === sector ? [index] : []))
        if (indexes.length === 0) return null
        return (
          <div key={sector} className="relative flex justify-around">
            {indexes.map((index) => {
              const id = chosen[index]
              return (
                <Marker
                  key={index}
                  slot={slots[index]}
                  player={id === undefined ? undefined : playersById.get(id)}
                />
              )
            })}
          </div>
        )
      })}
    </div>
  )
}

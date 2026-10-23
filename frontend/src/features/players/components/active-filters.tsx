import { X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { Club } from '@/features/clubs/types'
import { formatMoney } from '@/lib/format'
import { describeRange } from '@/lib/range'
import { effectiveSearch, type PlayerFilters } from '../player-filters'
import { CONDITION_LABELS, POSITION_LABELS } from '../types'

interface FilterChip {
  label: string
  removal: Partial<PlayerFilters>
}

function activeFilterChips(filters: PlayerFilters, clubs: Club[]): FilterChip[] {
  const chips: FilterChip[] = []
  const search = effectiveSearch(filters.search)
  if (search) {
    chips.push({ label: `Busca: ${search}`, removal: { search: '' } })
  }
  if (filters.position) {
    chips.push({
      label: `Posição: ${POSITION_LABELS[filters.position]}`,
      removal: { position: undefined },
    })
  }
  if (filters.condition) {
    chips.push({
      label: `Condição: ${CONDITION_LABELS[filters.condition]}`,
      removal: { condition: undefined },
    })
  }
  if (filters.clubId !== undefined) {
    const club = clubs.find((candidate) => candidate.id === filters.clubId)
    chips.push({
      label: `Clube: ${club?.name ?? filters.clubId}`,
      removal: { clubId: undefined },
    })
  }
  const age = describeRange(filters.age, String)
  if (age) {
    chips.push({ label: `Idade: ${age}`, removal: { age: {} } })
  }
  const marketValue = describeRange(filters.marketValue, formatMoney)
  if (marketValue) {
    chips.push({ label: `Valor de mercado: ${marketValue}`, removal: { marketValue: {} } })
  }
  return chips
}

interface ActiveFiltersProps {
  filters: PlayerFilters
  clubs: Club[]
  onChange: (changes: Partial<PlayerFilters>) => void
  onClear: () => void
}

export function ActiveFilters({ filters, clubs, onChange, onClear }: ActiveFiltersProps) {
  const chips = activeFilterChips(filters, clubs)
  if (chips.length === 0) return null

  return (
    <div className="flex flex-wrap items-center gap-2 text-sm">
      <span className="text-muted-foreground">Filtros ativos:</span>
      {chips.map((chip) => (
        <Badge key={chip.label} variant="secondary" className="h-7 gap-1 rounded-md pr-1">
          {chip.label}
          <Button
            variant="ghost"
            size="icon-xs"
            aria-label={`Remover filtro ${chip.label}`}
            onClick={() => onChange(chip.removal)}
          >
            <X aria-hidden />
          </Button>
        </Badge>
      ))}
      <Button variant="link" className="px-1" onClick={onClear}>
        Limpar filtros
      </Button>
    </div>
  )
}

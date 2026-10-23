import { Coins, HeartPulse, Shield, Shirt, User } from 'lucide-react'
import { useCallback } from 'react'
import { RangeFilter } from '@/components/common/range-filter'
import { SearchInput } from '@/components/common/search-input'
import { SelectFilter } from '@/components/common/select-filter'
import type { Club } from '@/features/clubs/types'
import { formatMoney } from '@/lib/format'
import {
  AGE_LIMITS,
  FREE_WITH_CLUB_REASON,
  MARKET_VALUE_LIMITS,
  SEARCH_MIN_LENGTH,
  type PlayerFilters,
} from '../player-filters'
import { CONDITION_LABELS, CONDITIONS, POSITION_LABELS, POSITIONS } from '../types'

const POSITION_OPTIONS = POSITIONS.map((position) => ({
  value: position,
  label: `${position} · ${POSITION_LABELS[position]}`,
}))

const CONDITION_OPTIONS = CONDITIONS.map((condition) => ({
  value: condition,
  label: CONDITION_LABELS[condition],
}))

interface PlayersToolbarProps {
  filters: PlayerFilters
  clubs: Club[]
  onChange: (changes: Partial<PlayerFilters>) => void
}

export function PlayersToolbar({ filters, clubs, onChange }: PlayersToolbarProps) {
  const handleSearchChange = useCallback((search: string) => onChange({ search }), [onChange])
  const searchTooShort =
    filters.search.trim().length > 0 && filters.search.trim().length < SEARCH_MIN_LENGTH

  return (
    <div className="flex flex-col gap-1">
      <div className="flex flex-wrap gap-2 rounded-xl border bg-card p-2">
        <SearchInput
          value={filters.search}
          onChange={handleSearchChange}
          placeholder="Buscar por nome"
        />
        <SelectFilter
          icon={Shirt}
          label="Posição"
          allLabel="Todas as posições"
          value={filters.position}
          options={POSITION_OPTIONS}
          onChange={(position) => onChange({ position })}
        />
        <SelectFilter
          icon={HeartPulse}
          label="Condição"
          allLabel="Todas as condições"
          value={filters.condition}
          options={CONDITION_OPTIONS}
          onChange={(condition) => onChange({ condition })}
        />
        <SelectFilter
          icon={Shield}
          label="Clube"
          allLabel="Todos os clubes"
          value={filters.clubId}
          options={clubs.map((club) => ({ value: club.id, label: club.name }))}
          onChange={(clubId) => onChange({ clubId })}
          disabledReason={filters.tab === 'free' ? FREE_WITH_CLUB_REASON : undefined}
        />
        <RangeFilter
          icon={User}
          label="Idade"
          value={filters.age}
          limits={AGE_LIMITS}
          allowDecimals={false}
          format={String}
          onChange={(age) => onChange({ age })}
        />
        <RangeFilter
          icon={Coins}
          label="Valor de mercado"
          shortLabel="Valor"
          value={filters.marketValue}
          limits={MARKET_VALUE_LIMITS}
          allowDecimals
          format={formatMoney}
          onChange={(marketValue) => onChange({ marketValue })}
        />
      </div>
      {searchTooShort && (
        <p className="px-1 text-xs text-muted-foreground">
          Digite pelo menos {SEARCH_MIN_LENGTH} letras para buscar pelo nome.
        </p>
      )}
    </div>
  )
}

import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { formatInteger } from '@/lib/format'
import { FREE_WITH_CLUB_REASON, type PlayerTab } from '../player-filters'

const TABS: { value: PlayerTab; label: string }[] = [
  { value: 'all', label: 'Todos' },
  { value: 'free', label: 'Livres' },
  { value: 'in_club', label: 'Em clubes' },
]

interface PlayerTabsProps {
  tab: PlayerTab
  counts: Partial<Record<PlayerTab, number>>
  freeDisabled: boolean
  onChange: (tab: PlayerTab) => void
}

export function PlayerTabs({ tab, counts, freeDisabled, onChange }: PlayerTabsProps) {
  const handleValueChange = (value: string) => {
    const selected = TABS.find((candidate) => candidate.value === value)
    if (selected) onChange(selected.value)
  }

  return (
    <Tabs value={tab} onValueChange={handleValueChange}>
      <TabsList aria-label="Vínculo com clube">
        {TABS.map(({ value, label }) => {
          const disabled = value === 'free' && freeDisabled
          const count = counts[value]
          return (
            <TabsTrigger
              key={value}
              value={value}
              disabled={disabled}
              title={disabled ? FREE_WITH_CLUB_REASON : undefined}
              className="gap-1.5"
            >
              {label}
              {count !== undefined && (
                <>
                  {' '}
                  <span className="text-muted-foreground tabular-nums">{formatInteger(count)}</span>
                </>
              )}
            </TabsTrigger>
          )
        })}
      </TabsList>
    </Tabs>
  )
}

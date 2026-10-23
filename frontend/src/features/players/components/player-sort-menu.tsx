import { ArrowUpDown, ChevronDown } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { PLAYER_SORTS, type PlayerSort } from '../player-filters'

interface PlayerSortMenuProps {
  sort: PlayerSort
  onChange: (sort: PlayerSort) => void
}

function lowerFirst(text: string): string {
  return text.charAt(0).toLowerCase() + text.slice(1)
}

export function PlayerSortMenu({ sort, onChange }: PlayerSortMenuProps) {
  const current = PLAYER_SORTS.find((option) => option.value === sort) ?? PLAYER_SORTS[0]

  const handleValueChange = (value: string) => {
    const selected = PLAYER_SORTS.find((option) => option.value === value)
    if (selected) onChange(selected.value)
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" className="h-9">
          <ArrowUpDown data-icon="inline-start" aria-hidden />
          Ordenar: {lowerFirst(current.label)}
          <ChevronDown data-icon="inline-end" className="text-muted-foreground" aria-hidden />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="min-w-56">
        <DropdownMenuLabel>Ordenar por</DropdownMenuLabel>
        <DropdownMenuRadioGroup value={sort} onValueChange={handleValueChange}>
          {PLAYER_SORTS.map((option) => (
            <DropdownMenuRadioItem key={option.value} value={option.value}>
              {option.label}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

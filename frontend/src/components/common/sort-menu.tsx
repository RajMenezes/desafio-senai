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

interface SortOption<T extends string> {
  value: T
  label: string
}

interface SortMenuProps<T extends string> {
  options: readonly SortOption<T>[]
  value: T
  onChange: (value: T) => void
}

function lowerFirst(text: string): string {
  return text.charAt(0).toLowerCase() + text.slice(1)
}

export function SortMenu<T extends string>({ options, value, onChange }: SortMenuProps<T>) {
  const current = options.find((option) => option.value === value) ?? options[0]

  const handleValueChange = (newValue: string) => {
    const selected = options.find((option) => option.value === newValue)
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
        <DropdownMenuRadioGroup value={value} onValueChange={handleValueChange}>
          {options.map((option) => (
            <DropdownMenuRadioItem key={option.value} value={option.value}>
              {option.label}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

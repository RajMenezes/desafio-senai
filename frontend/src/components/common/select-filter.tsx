import type { LucideIcon } from 'lucide-react'
import { DisabledFilter, FilterTrigger } from '@/components/common/filter-trigger'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

export interface FilterOption<T extends string | number> {
  value: T
  label: string
}

interface SelectFilterProps<T extends string | number> {
  icon: LucideIcon
  label: string
  allLabel: string
  value?: T
  options: FilterOption<T>[]
  onChange: (value: T | undefined) => void
  disabledReason?: string
}

const ALL_VALUE = ''

/** Filtro de escolha única; a primeira opção ("Todas", "Todos"…) remove o filtro. */
export function SelectFilter<T extends string | number>({
  icon,
  label,
  allLabel,
  value,
  options,
  onChange,
  disabledReason,
}: SelectFilterProps<T>) {
  const selected = options.find((option) => option.value === value)
  const text = selected ? `${label}: ${selected.label}` : label

  if (disabledReason) {
    return <DisabledFilter icon={icon} text={text} reason={disabledReason} />
  }

  const handleValueChange = (newValue: string) => {
    onChange(options.find((option) => String(option.value) === newValue)?.value)
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <FilterTrigger icon={icon} text={text} />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="max-h-80 min-w-48">
        <DropdownMenuLabel>{label}</DropdownMenuLabel>
        <DropdownMenuRadioGroup
          value={selected ? String(selected.value) : ALL_VALUE}
          onValueChange={handleValueChange}
        >
          <DropdownMenuRadioItem value={ALL_VALUE}>{allLabel}</DropdownMenuRadioItem>
          {options.map((option) => (
            <DropdownMenuRadioItem key={option.value} value={String(option.value)}>
              {option.label}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

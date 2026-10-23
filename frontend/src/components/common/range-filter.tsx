import type { LucideIcon } from 'lucide-react'
import { useId, useState, type FormEvent } from 'react'
import { FilterTrigger } from '@/components/common/filter-trigger'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { describeRange, validateRange, type Range, type RangeRules } from '@/lib/range'

interface RangeFilterProps extends RangeRules {
  icon: LucideIcon
  label: string
  shortLabel?: string
  value: Range
  onChange: (range: Range) => void
}

function toText(value: number | undefined): string {
  return value === undefined ? '' : String(value).replace('.', ',')
}

/** Filtro de faixa (mínimo e máximo, inclusivos) aberto num popover. */
export function RangeFilter({
  icon,
  label,
  shortLabel = label,
  value,
  onChange,
  ...rules
}: RangeFilterProps) {
  const id = useId()
  const [open, setOpen] = useState(false)
  const [minText, setMinText] = useState('')
  const [maxText, setMaxText] = useState('')
  const [errors, setErrors] = useState<{ min?: string; max?: string }>({})

  const description = describeRange(value, rules.format)

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      setMinText(toText(value.min))
      setMaxText(toText(value.max))
      setErrors({})
    }
    setOpen(nextOpen)
  }

  const handleApply = (event: FormEvent) => {
    event.preventDefault()
    const result = validateRange(minText, maxText, rules)
    if ('errors' in result) {
      setErrors(result.errors)
      return
    }
    onChange(result.range)
    setOpen(false)
  }

  const handleClear = () => {
    onChange({})
    setOpen(false)
  }

  const inputMode = rules.allowDecimals ? 'decimal' : 'numeric'

  return (
    <Popover open={open} onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <FilterTrigger icon={icon} text={description ? `${shortLabel}: ${description}` : label} />
      </PopoverTrigger>
      <PopoverContent align="start" className="w-72">
        <form onSubmit={handleApply} noValidate className="flex flex-col gap-4">
          <div>
            <p className="text-sm font-medium">{label}</p>
            <p className="text-xs text-muted-foreground">
              De {rules.format(rules.limits.min)} a {rules.format(rules.limits.max)}, inclusive.
            </p>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Field data-invalid={errors.min !== undefined}>
              <FieldLabel htmlFor={`${id}-min`}>Mínimo</FieldLabel>
              <Input
                id={`${id}-min`}
                inputMode={inputMode}
                value={minText}
                onChange={(event) => setMinText(event.target.value)}
                aria-invalid={errors.min !== undefined}
              />
              <FieldError>{errors.min}</FieldError>
            </Field>
            <Field data-invalid={errors.max !== undefined}>
              <FieldLabel htmlFor={`${id}-max`}>Máximo</FieldLabel>
              <Input
                id={`${id}-max`}
                inputMode={inputMode}
                value={maxText}
                onChange={(event) => setMaxText(event.target.value)}
                aria-invalid={errors.max !== undefined}
              />
              <FieldError>{errors.max}</FieldError>
            </Field>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={handleClear}>
              Limpar
            </Button>
            <Button type="submit">Aplicar</Button>
          </div>
        </form>
      </PopoverContent>
    </Popover>
  )
}

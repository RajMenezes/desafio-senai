import { ChevronDown, type LucideIcon } from 'lucide-react'
import type { ComponentProps } from 'react'
import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'

type FilterTriggerProps = ComponentProps<typeof Button> & {
  icon: LucideIcon
  text: string
}

/** Botão dos filtros da barra de busca: ícone, texto (com o valor escolhido) e seta. */
export function FilterTrigger({ icon: Icon, text, ...props }: FilterTriggerProps) {
  return (
    <Button variant="outline" className="h-9 font-normal" {...props}>
      <Icon data-icon="inline-start" aria-hidden />
      {text}
      <ChevronDown data-icon="inline-end" className="text-muted-foreground" aria-hidden />
    </Button>
  )
}

interface DisabledFilterProps {
  icon: LucideIcon
  text: string
  reason: string
}

/** Filtro bloqueado, com o motivo numa dica ao passar o mouse ou focar. */
export function DisabledFilter({ icon, text, reason }: DisabledFilterProps) {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span tabIndex={0} aria-label={`${text}: ${reason}`}>
          <FilterTrigger icon={icon} text={text} disabled />
        </span>
      </TooltipTrigger>
      <TooltipContent>{reason}</TooltipContent>
    </Tooltip>
  )
}

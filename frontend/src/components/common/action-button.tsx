import type { ComponentProps } from 'react'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'

type ActionButtonProps = ComponentProps<typeof Button> & {
  pending?: boolean
}

/** Botão que fica desabilitado e mostra um indicador enquanto a ação está em andamento. */
export function ActionButton({ pending = false, disabled, children, ...props }: ActionButtonProps) {
  return (
    <Button disabled={disabled || pending} aria-busy={pending} {...props}>
      {pending && <Spinner data-icon="inline-start" />}
      {children}
    </Button>
  )
}

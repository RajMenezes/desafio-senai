import { formatSignedMoney } from '@/lib/format'
import { cn } from '@/lib/utils'

/** Valor com sinal sempre visível: entrada e lucro em verde, saída e prejuízo em vermelho. */
export function SignedMoney({ value, className }: { value: number; className?: string }) {
  return (
    <span className={cn(value > 0 && 'text-success', value < 0 && 'text-destructive', className)}>
      {formatSignedMoney(value)}
    </span>
  )
}

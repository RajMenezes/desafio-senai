import { Check } from 'lucide-react'
import { Badge } from '@/components/ui/badge'

/** Percentual de multa da RN15 vindo da API: sem multa em verde, a maior em vermelho. */
export function PenaltyBadge({ rate }: { rate: number }) {
  if (rate === 0) {
    return (
      <Badge variant="success">
        <Check aria-hidden />
        0%
      </Badge>
    )
  }
  return <Badge variant={rate >= 20 ? 'danger' : 'warning'}>{rate}%</Badge>
}

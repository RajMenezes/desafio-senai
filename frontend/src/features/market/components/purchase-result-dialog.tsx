import { ArrowRight, CircleCheck } from 'lucide-react'
import { Link } from 'react-router'
import { paths } from '@/app/paths'
import { ResultRows } from '@/components/common/result-rows'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import type { Club } from '@/features/clubs/types'
import { formatDateTime } from '@/lib/date-time'
import { formatMoney } from '@/lib/format'
import type { PurchaseResult } from '../types'

interface PurchaseResultDialogProps {
  result: PurchaseResult
  club: Club | undefined
  onClose: () => void
}

/** Resultado da compra com os valores devolvidos pela API (sem recalcular). */
export function PurchaseResultDialog({ result, club, onClose }: PurchaseResultDialogProps) {
  const clubName = club?.name ?? 'clube'
  const rows = [
    {
      label: 'Valor',
      hint: 'Valor de mercado no momento da compra',
      value: formatMoney(result.value),
    },
    {
      label: 'Taxa',
      hint:
        result.fee > 0
          ? '5% do valor, porque passa de 30,00'
          : 'Sem taxa: o valor não passa de 30,00',
      value: formatMoney(result.fee),
    },
    {
      label: 'Custo total',
      hint: 'Valor + taxa',
      value: formatMoney(-result.cash_change),
      highlight: true,
    },
    { label: 'Fim da fidelidade', value: formatDateTime(result.contract.loyalty_until) },
    {
      label: 'Contrato',
      value: (
        <Badge variant="success">
          <span className="size-1.5 rounded-full bg-current" aria-hidden />
          {result.contract.status}
        </Badge>
      ),
    },
    { label: `Novo caixa do ${clubName}`, value: formatMoney(result.club_cash), highlight: true },
  ]

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="mb-2 flex size-9 items-center justify-center rounded-full bg-success-soft text-success">
            <CircleCheck className="size-5" aria-hidden />
          </div>
          <DialogTitle>Compra concluída</DialogTitle>
          <DialogDescription>
            {result.player_name} agora é do {clubName}.
          </DialogDescription>
        </DialogHeader>
        <ResultRows rows={rows} />
        <DialogFooter>
          <Button variant="outline" asChild>
            <Link to={paths.club(result.club_id)}>
              Ver clube
              <ArrowRight data-icon="inline-end" aria-hidden />
            </Link>
          </Button>
          <Button onClick={onClose}>Fechar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

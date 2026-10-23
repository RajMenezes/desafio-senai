import { CircleCheck, History } from 'lucide-react'
import { ResultRows } from '@/components/common/result-rows'
import { SignedMoney } from '@/components/common/signed-money'
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
import { formatInteger, formatMoney, formatSignedMoney } from '@/lib/format'
import type { SaleResult } from '../types'

interface SaleResultDialogProps {
  result: SaleResult
  clubName: string
  leftLineup: boolean
  onClose: () => void
  onShowHistory: () => void
}

/** Resultado da venda com os valores devolvidos pela API (sem recalcular). */
export function SaleResultDialog({
  result,
  clubName,
  leftLineup,
  onClose,
  onShowHistory,
}: SaleResultDialogProps) {
  const loyaltyFulfilled = result.penalty_rate === 0
  const rows = [
    { label: 'Valor de compra', value: formatMoney(result.purchase_value) },
    {
      label: 'Valor de venda',
      hint: 'Valor de mercado no momento da venda',
      value: formatMoney(result.value),
    },
    { label: 'Dias restantes', value: formatInteger(result.remaining_days) },
    {
      label: 'Multa',
      hint: loyaltyFulfilled
        ? 'Fidelidade cumprida: sem multa'
        : `${result.penalty_rate}% do valor de venda, com teto de 10,00`,
      value: formatMoney(result.penalty),
    },
    { label: 'Lucro', value: <SignedMoney value={result.profit} /> },
    {
      label: 'Comissão',
      hint:
        result.fee > 0 ? '10% do lucro até 10,00 + 20% do que passar' : 'Sem lucro: sem comissão',
      value: formatMoney(result.fee),
    },
    {
      label: 'Crédito no caixa',
      hint: 'Valor de venda − multa − comissão',
      value: formatSignedMoney(result.cash_change),
      highlight: true,
    },
    {
      label: 'Contrato',
      value: (
        <Badge variant={result.contract_status === 'RESCINDIDO' ? 'warning' : 'secondary'}>
          <span className="size-1.5 rounded-full bg-current" aria-hidden />
          {result.contract_status}
        </Badge>
      ),
    },
    { label: `Novo caixa do ${clubName}`, value: formatMoney(result.club_cash), highlight: true },
  ]

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-h-[calc(100dvh-2rem)] overflow-y-auto sm:max-w-md">
        <DialogHeader>
          <div className="mb-2 flex size-9 items-center justify-center rounded-full bg-success-soft text-success">
            <CircleCheck className="size-5" aria-hidden />
          </div>
          <DialogTitle>Venda concluída</DialogTitle>
          <DialogDescription>
            {result.player_name} voltou a ficar livre{leftLineup && ' e saiu da escalação'}.
            {loyaltyFulfilled && ' A fidelidade já tinha terminado, então não houve multa.'}
          </DialogDescription>
        </DialogHeader>
        <ResultRows rows={rows} />
        <DialogFooter>
          <Button variant="outline" onClick={onShowHistory}>
            <History data-icon="inline-start" aria-hidden />
            Ver histórico
          </Button>
          <Button onClick={onClose}>Fechar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

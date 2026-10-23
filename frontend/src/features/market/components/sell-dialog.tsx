import { Info } from 'lucide-react'
import { ConfirmDialog } from '@/components/common/confirm-dialog'
import { ResultRows } from '@/components/common/result-rows'
import { PenaltyBadge } from '@/features/clubs/components/penalty-badge'
import type { SquadPlayer } from '@/features/clubs/types'
import { StarterWarning } from '@/features/league/components/starter-warning'
import { PlayerSummary } from '@/features/players/components/player-summary'
import { formatDateTime } from '@/lib/date-time'
import { formatInteger, formatMoney } from '@/lib/format'
import { useSellPlayer } from '../api'
import type { SaleResult } from '../types'

interface SellDialogProps {
  player: SquadPlayer
  clubName: string
  isStarter: boolean
  onClose: () => void
  onSold: (result: SaleResult) => void
}

/** Confirmação da venda com o contrato atual; multa, lucro, comissão e crédito vêm da API. */
export function SellDialog({ player, clubName, isStarter, onClose, onSold }: SellDialogProps) {
  const sellPlayer = useSellPlayer(player.id)
  const { contract } = player

  return (
    <ConfirmDialog
      open
      onOpenChange={(open) => !open && onClose()}
      title={`Vender ${player.name}?`}
      description={`O jogador sai do elenco do ${clubName} e volta a ficar livre.`}
      confirmLabel="Confirmar venda"
      onConfirm={() => sellPlayer.mutate(undefined, { onSuccess: onSold })}
      pending={sellPlayer.isPending}
      error={sellPlayer.error}
      errorTitle="A venda foi recusada"
    >
      {isStarter && <StarterWarning playerName={player.name} clubName={clubName} reason="sale" />}
      <PlayerSummary player={player} detail="market_value" />
      <ResultRows
        rows={[
          { label: 'Valor de compra', value: formatMoney(contract.purchase_value) },
          { label: 'Fim da fidelidade', value: formatDateTime(contract.loyalty_until) },
          { label: 'Dias restantes', value: formatInteger(contract.remaining_days) },
          {
            label: '% de multa se vender agora',
            value: <PenaltyBadge rate={contract.penalty_rate} />,
          },
        ]}
      />
      <p className="flex items-start gap-2 rounded-lg border bg-muted-subtle px-3 py-2 text-xs text-muted-foreground">
        <Info className="mt-px size-3.5 shrink-0" aria-hidden />A venda é pelo valor de mercado do
        momento. Multa (teto de 10,00), lucro, comissão e crédito são calculados ao confirmar.
      </p>
    </ConfirmDialog>
  )
}

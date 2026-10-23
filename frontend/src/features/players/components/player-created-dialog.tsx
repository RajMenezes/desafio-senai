import { CircleCheck, Eye } from 'lucide-react'
import { ResultRows } from '@/components/common/result-rows'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { formatMoney } from '@/lib/format'
import { POSITION_LABELS, type Player } from '../types'

interface PlayerCreatedDialogProps {
  player: Player
  onClose: () => void
  onShowDetails: () => void
}

/** Resultado do cadastro, com os valores que a API calculou ao salvar. */
export function PlayerCreatedDialog({ player, onClose, onShowDetails }: PlayerCreatedDialogProps) {
  const rows = [
    {
      label: 'Valor base',
      hint: 'Calculado no cadastro; não muda depois',
      value: formatMoney(player.base_value),
      highlight: true,
    },
    {
      label: 'Valor de mercado',
      hint: 'Com condição, agilidade e idade',
      value: formatMoney(player.market_value),
    },
    {
      label: 'Overall',
      hint: `Pelos pesos da posição ${POSITION_LABELS[player.position]}`,
      value: String(player.overall),
    },
  ]

  return (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="mb-2 flex size-9 items-center justify-center rounded-full bg-success-soft text-success">
            <CircleCheck className="size-5" aria-hidden />
          </div>
          <DialogTitle>Jogador cadastrado</DialogTitle>
          <DialogDescription>
            {player.name} entrou livre e Apto. Valores em moedas, calculados pela API ao salvar.
          </DialogDescription>
        </DialogHeader>
        <ResultRows rows={rows} />
        <DialogFooter>
          <Button variant="outline" onClick={onShowDetails}>
            <Eye data-icon="inline-start" aria-hidden />
            Ver detalhes
          </Button>
          <Button onClick={onClose}>Fechar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

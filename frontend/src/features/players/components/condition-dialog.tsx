import { Lock } from 'lucide-react'
import { useState } from 'react'
import { ActionButton } from '@/components/common/action-button'
import { ErrorAlert } from '@/components/common/error-alert'
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
import { useClubs } from '@/features/clubs/api'
import { useLineup } from '@/features/league/api'
import { StarterWarning } from '@/features/league/components/starter-warning'
import { formatMoney } from '@/lib/format'
import { notifySuccess } from '@/lib/notify'
import { cn } from '@/lib/utils'
import { useChangeCondition } from '../api'
import { CONDITION_LABELS, CONDITIONS, type Condition, type Player } from '../types'
import { PlayerSummary } from './player-summary'

const CONDITION_DESCRIPTIONS: Record<Condition, string> = {
  APTO: 'Sem desconto por condição.',
  MAL_CONDICIONADO:
    'Desconto de 15%. Agilidade de 80 a 100 corta pela metade; de 1 a 30 soma 10 pontos.',
  DOENTE: 'Desconto de 30%, com o mesmo ajuste de agilidade.',
}

/** A API recusa a mudança de DOENTE direto para APTO (409); a tela já bloqueia essa opção. */
function blockedReason(current: Condition, target: Condition): string | undefined {
  return current === 'DOENTE' && target === 'APTO'
    ? 'De Doente não passa direto para Apto.'
    : undefined
}

interface ConditionDialogProps {
  player: Player
  onClose: () => void
}

export function ConditionDialog({ player, onClose }: ConditionDialogProps) {
  const changeCondition = useChangeCondition(player.id)
  const [selected, setSelected] = useState<Condition>(player.condition)
  const hasClub = player.club_id !== null
  const lineupQuery = useLineup(player.club_id ?? 0, hasClub)
  const clubsQuery = useClubs()
  const isStarter = lineupQuery.data?.players.some((starter) => starter.id === player.id) ?? false
  const clubName = clubsQuery.data?.find((club) => club.id === player.club_id)?.name ?? 'clube'

  const handleOpenChange = (open: boolean) => {
    if (!open && !changeCondition.isPending) onClose()
  }

  const handleSave = () => {
    changeCondition.mutate(selected, {
      onSuccess: (updated) => {
        notifySuccess(
          'Condição alterada',
          `${updated.name} agora está ${CONDITION_LABELS[updated.condition]}. Valor de mercado: ${formatMoney(updated.market_value)}.`,
        )
        onClose()
      },
    })
  }

  return (
    <Dialog open onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Alterar condição física</DialogTitle>
          <DialogDescription>
            A condição muda o desconto do valor de mercado, recalculado ao salvar.
          </DialogDescription>
        </DialogHeader>

        <PlayerSummary player={player} />

        {changeCondition.error && (
          <ErrorAlert title="Não foi possível alterar a condição" error={changeCondition.error} />
        )}

        <fieldset className="flex flex-col gap-2">
          <legend className="sr-only">Nova condição</legend>
          {CONDITIONS.map((condition) => {
            const reason = blockedReason(player.condition, condition)
            return (
              <label
                key={condition}
                className={cn(
                  'flex cursor-pointer gap-3 rounded-lg border p-3 transition-colors hover:bg-muted',
                  'has-checked:border-primary has-checked:bg-primary-soft',
                  'has-focus-visible:ring-3 has-focus-visible:ring-ring/50',
                  'has-disabled:cursor-not-allowed has-disabled:opacity-60 has-disabled:hover:bg-transparent',
                )}
              >
                <input
                  type="radio"
                  name="condition"
                  value={condition}
                  checked={selected === condition}
                  disabled={reason !== undefined}
                  onChange={() => setSelected(condition)}
                  className="mt-1 accent-primary"
                />
                <span className="flex flex-col gap-0.5">
                  <span className="flex items-center gap-2 font-medium">
                    {CONDITION_LABELS[condition]}
                    {condition === player.condition && <Badge variant="secondary">Atual</Badge>}
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {CONDITION_DESCRIPTIONS[condition]}
                  </span>
                  {reason && (
                    <span className="flex items-center gap-1 text-xs text-muted-foreground">
                      <Lock className="size-3" aria-hidden />
                      {reason}
                    </span>
                  )}
                </span>
              </label>
            )
          })}
        </fieldset>

        {isStarter && selected === 'DOENTE' && player.condition !== 'DOENTE' && (
          <StarterWarning playerName={player.name} clubName={clubName} reason="sick" />
        )}

        <DialogFooter>
          <Button variant="outline" disabled={changeCondition.isPending} onClick={onClose}>
            Cancelar
          </Button>
          <ActionButton
            pending={changeCondition.isPending}
            disabled={selected === player.condition}
            onClick={handleSave}
          >
            Salvar condição
          </ActionButton>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

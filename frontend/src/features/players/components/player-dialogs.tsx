import type { Club } from '@/features/clubs/types'
import { BuyDialog } from '@/features/market/components/buy-dialog'
import { PurchaseResultDialog } from '@/features/market/components/purchase-result-dialog'
import type { PurchaseResult } from '@/features/market/types'
import type { Player } from '../types'
import { ConditionDialog } from './condition-dialog'
import { DeletePlayerDialog } from './delete-player-dialog'
import type { PlayerAction } from './player-actions-menu'
import { PlayerCreateDialog } from './player-create-dialog'
import { PlayerCreatedDialog } from './player-created-dialog'
import { PlayerDetailsSheet } from './player-details-sheet'
import { PlayerEditDialog } from './player-edit-dialog'

/** Diálogo aberto na tela de jogadores (no máximo um por vez). */
export type PlayerDialogState =
  | { kind: 'create' }
  | { kind: 'created'; player: Player }
  | { kind: 'purchased'; result: PurchaseResult; club: Club | undefined }
  | { kind: PlayerAction; player: Player }

interface PlayerDialogsProps {
  state: PlayerDialogState | null
  clubsById: Map<number, Club>
  onChange: (state: PlayerDialogState | null) => void
}

export function PlayerDialogs({ state, clubsById, onChange }: PlayerDialogsProps) {
  const close = () => onChange(null)
  const openAction = (kind: PlayerAction, player: Player) => onChange({ kind, player })

  return (
    <>
      <PlayerCreateDialog
        open={state?.kind === 'create'}
        onOpenChange={(open) => onChange(open ? { kind: 'create' } : null)}
        onCreated={(player) => onChange({ kind: 'created', player })}
      />
      {state?.kind === 'created' && (
        <PlayerCreatedDialog
          player={state.player}
          onClose={close}
          onShowDetails={() => openAction('details', state.player)}
        />
      )}
      {state?.kind === 'details' && (
        <PlayerDetailsSheet
          playerId={state.player.id}
          clubsById={clubsById}
          onClose={close}
          onAction={openAction}
        />
      )}
      {state?.kind === 'edit' && <PlayerEditDialog player={state.player} onClose={close} />}
      {state?.kind === 'condition' && <ConditionDialog player={state.player} onClose={close} />}
      {state?.kind === 'delete' && <DeletePlayerDialog player={state.player} onClose={close} />}
      {state?.kind === 'buy' && (
        <BuyDialog
          player={state.player}
          onClose={close}
          onPurchased={(result, club) => onChange({ kind: 'purchased', result, club })}
        />
      )}
      {state?.kind === 'purchased' && (
        <PurchaseResultDialog result={state.result} club={state.club} onClose={close} />
      )}
    </>
  )
}

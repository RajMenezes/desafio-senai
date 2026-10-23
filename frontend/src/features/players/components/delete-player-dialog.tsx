import { ConfirmDialog } from '@/components/common/confirm-dialog'
import { notifySuccess } from '@/lib/notify'
import { useDeletePlayer } from '../api'
import type { Player } from '../types'
import { PlayerSummary } from './player-summary'

interface DeletePlayerDialogProps {
  player: Player
  onClose: () => void
}

export function DeletePlayerDialog({ player, onClose }: DeletePlayerDialogProps) {
  const deletePlayer = useDeletePlayer(player.id)

  const handleConfirm = () => {
    deletePlayer.mutate(undefined, {
      onSuccess: () => {
        notifySuccess(
          'Jogador excluído',
          `${player.name} saiu da lista. Transações e partidas antigas dele continuam gravadas.`,
        )
        onClose()
      },
    })
  }

  return (
    <ConfirmDialog
      open
      onOpenChange={(open) => !open && onClose()}
      title={`Excluir ${player.name}?`}
      description="O jogador sai da lista e da busca, mas as transações e as partidas antigas dele continuam como foram gravadas."
      confirmLabel="Excluir jogador"
      onConfirm={handleConfirm}
      pending={deletePlayer.isPending}
      error={deletePlayer.error}
      errorTitle="Não foi possível excluir o jogador"
      destructive
    >
      <PlayerSummary player={player} detail="club" />
    </ConfirmDialog>
  )
}

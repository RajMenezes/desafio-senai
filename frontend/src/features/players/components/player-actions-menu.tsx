import { Eye, HeartPulse, MoreHorizontal, Pencil, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { Player } from '../types'

export type PlayerAction = 'details' | 'edit' | 'condition' | 'delete'

interface PlayerActionsMenuProps {
  player: Player
  onAction: (action: PlayerAction, player: Player) => void
}

export function PlayerActionsMenu({ player, onAction }: PlayerActionsMenuProps) {
  const isFree = player.club_id === null

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon-sm" aria-label={`Ações de ${player.name}`}>
          <MoreHorizontal aria-hidden />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        <DropdownMenuLabel className="truncate">{player.name}</DropdownMenuLabel>
        <DropdownMenuItem onSelect={() => onAction('details', player)}>
          <Eye aria-hidden />
          Ver detalhes
        </DropdownMenuItem>
        <DropdownMenuItem onSelect={() => onAction('edit', player)}>
          <Pencil aria-hidden />
          Editar
        </DropdownMenuItem>
        <DropdownMenuItem onSelect={() => onAction('condition', player)}>
          <HeartPulse aria-hidden />
          Alterar condição
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          variant="destructive"
          disabled={!isFree}
          onSelect={() => onAction('delete', player)}
          className="flex-col items-start gap-0.5"
        >
          <span className="flex items-center gap-2">
            <Trash2 aria-hidden />
            Excluir
          </span>
          <span className="text-xs text-muted-foreground">
            {isFree
              ? 'Disponível porque o jogador está livre.'
              : 'Só jogadores livres podem ser excluídos.'}
          </span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

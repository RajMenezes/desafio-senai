import { Badge } from '@/components/ui/badge'
import { POSITION_LABELS, type Player } from '../types'
import { ConditionBadge, PlayerAvatar } from './player-cells'

/** Cartão com o jogador da ação: avatar, nome, posição, idade e situação. */
export function PlayerSummary({
  player,
  status = 'condition',
}: {
  player: Player
  status?: 'condition' | 'club'
}) {
  return (
    <div className="flex items-center gap-3 rounded-lg border bg-muted-subtle p-3">
      <PlayerAvatar id={player.id} name={player.name} />
      <div className="min-w-0 flex-1 leading-tight">
        <p className="truncate font-medium">{player.name}</p>
        <p className="text-xs text-muted-foreground">
          {player.position} · {POSITION_LABELS[player.position]} · {player.age} anos
        </p>
      </div>
      {status === 'condition' ? (
        <ConditionBadge condition={player.condition} />
      ) : (
        player.club_id === null && <Badge variant="secondary">Livre</Badge>
      )}
    </div>
  )
}

import { Badge } from '@/components/ui/badge'
import { formatMoney } from '@/lib/format'
import { POSITION_LABELS, type Player } from '../types'
import { ConditionBadge, PlayerAvatar } from './player-cells'

type SummaryDetail = 'condition' | 'club' | 'market_value'

function SummaryDetailContent({ player, detail }: { player: Player; detail: SummaryDetail }) {
  if (detail === 'condition') return <ConditionBadge condition={player.condition} />
  if (detail === 'market_value') {
    return (
      <div className="text-right leading-tight">
        <p className="text-xs text-muted-foreground">Valor de mercado</p>
        <p className="text-lg font-semibold tabular-nums">{formatMoney(player.market_value)}</p>
      </div>
    )
  }
  return player.club_id === null ? <Badge variant="secondary">Livre</Badge> : null
}

/** Cartão com o jogador da ação: avatar, nome, posição, idade e um detalhe à direita. */
export function PlayerSummary({
  player,
  detail = 'condition',
}: {
  player: Player
  detail?: SummaryDetail
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
      <SummaryDetailContent player={player} detail={detail} />
    </div>
  )
}

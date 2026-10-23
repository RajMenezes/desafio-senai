import { BarChart3, Coins, HeartPulse, Pencil, Tag, TrendingUp } from 'lucide-react'
import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import { StatCard } from '@/components/common/stat-card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import type { Club } from '@/features/clubs/types'
import { formatMoney } from '@/lib/format'
import { usePlayer } from '../api'
import { ATTRIBUTES, INDICATOR_LIMITS } from '../player-schemas'
import { POSITION_LABELS, type Player } from '../types'
import type { PlayerAction } from './player-actions-menu'
import { ConditionBadge, PlayerAvatar, PositionBadge } from './player-cells'

function AttributeBars({ player }: { player: Player }) {
  return (
    <section aria-labelledby="player-attributes-title" className="flex flex-col gap-3">
      <div className="flex items-baseline justify-between">
        <h3 id="player-attributes-title" className="font-semibold">
          Indicadores técnicos
        </h3>
        <span className="text-xs text-muted-foreground">
          Escala de {INDICATOR_LIMITS.min} a {INDICATOR_LIMITS.max}
        </span>
      </div>
      <dl className="flex flex-col gap-2">
        {ATTRIBUTES.map(({ key, label }) => (
          <div key={key} className="grid grid-cols-[6rem_1fr_2rem] items-center gap-3 text-sm">
            <dt className="text-muted-foreground">{label}</dt>
            <span className="relative h-2 rounded-full bg-muted" aria-hidden>
              <span
                className="absolute inset-y-0 left-0 rounded-full bg-primary/25"
                style={{ width: `${player.potential}%` }}
              />
              <span
                className="absolute inset-y-0 left-0 rounded-full bg-primary"
                style={{ width: `${player[key]}%` }}
              />
            </span>
            <dd className="text-right font-medium tabular-nums">{player[key]}</dd>
          </div>
        ))}
      </dl>
      <p className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
        <span className="flex items-center gap-1.5">
          <span className="size-2 rounded-sm bg-primary" aria-hidden />
          Valor do atributo
        </span>
        <span className="flex items-center gap-1.5">
          <span className="size-2 rounded-sm bg-primary/25" aria-hidden />
          Até o potencial ({player.potential}), que nenhum atributo passa
        </span>
      </p>
    </section>
  )
}

function PlayerDetails({ player, club }: { player: Player; club?: Club }) {
  const details = [
    { label: 'Idade', value: `${player.age} anos` },
    { label: 'Agilidade', value: String(player.agility) },
    { label: 'Clube', value: player.club_id === null ? 'Livre' : (club?.name ?? '—') },
  ]

  return (
    <div className="flex flex-col gap-6 px-4">
      <div className="grid grid-cols-2 gap-3">
        <StatCard
          label="Valor de mercado"
          value={formatMoney(player.market_value)}
          hint="Calculado automaticamente"
          icon={Coins}
        />
        <StatCard
          label="Valor base"
          value={formatMoney(player.base_value)}
          hint="Calculado no cadastro"
          icon={Tag}
        />
        <StatCard
          label="Overall"
          value={String(player.overall)}
          hint="Calculado pela posição"
          icon={BarChart3}
        />
        <StatCard
          label="Potencial"
          value={String(player.potential)}
          hint="Informado no cadastro"
          icon={TrendingUp}
        />
      </div>
      <AttributeBars player={player} />
      <dl className="divide-y rounded-lg border text-sm">
        {details.map((detail) => (
          <div key={detail.label} className="flex justify-between px-4 py-2.5">
            <dt className="text-muted-foreground">{detail.label}</dt>
            <dd className="font-medium">{detail.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  )
}

interface PlayerDetailsSheetProps {
  playerId: number
  clubsById: Map<number, Club>
  onClose: () => void
  onAction: (action: PlayerAction, player: Player) => void
}

export function PlayerDetailsSheet({
  playerId,
  clubsById,
  onClose,
  onAction,
}: PlayerDetailsSheetProps) {
  const query = usePlayer(playerId)
  const player = query.data

  return (
    <Sheet open onOpenChange={(open) => !open && onClose()}>
      <SheetContent className="w-full gap-0 overflow-y-auto sm:max-w-md">
        <SheetHeader className="flex-row items-center gap-3">
          {player && <PlayerAvatar id={player.id} name={player.name} />}
          <div className="flex flex-col gap-1">
            <SheetTitle>{player?.name ?? 'Detalhes do jogador'}</SheetTitle>
            <SheetDescription asChild>
              <div className="flex flex-wrap items-center gap-1.5">
                {player ? (
                  <>
                    <PositionBadge position={player.position} />
                    <span className="sr-only">{POSITION_LABELS[player.position]}</span>
                    <ConditionBadge condition={player.condition} />
                    {player.club_id === null && <Badge variant="secondary">Livre</Badge>}
                  </>
                ) : (
                  'Indicadores, valores e situação do jogador.'
                )}
              </div>
            </SheetDescription>
          </div>
        </SheetHeader>

        {query.isError ? (
          <ErrorState
            title="Não foi possível carregar o jogador"
            error={query.error}
            onRetry={() => query.refetch()}
            retrying={query.isFetching}
          />
        ) : player ? (
          <PlayerDetails
            player={player}
            club={player.club_id === null ? undefined : clubsById.get(player.club_id)}
          />
        ) : (
          <LoadingState label="Carregando jogador…" />
        )}

        {player && (
          <SheetFooter className="mt-6 flex-row border-t">
            <Button variant="outline" onClick={() => onAction('edit', player)}>
              <Pencil data-icon="inline-start" aria-hidden />
              Editar
            </Button>
            <Button variant="outline" onClick={() => onAction('condition', player)}>
              <HeartPulse data-icon="inline-start" aria-hidden />
              Alterar condição
            </Button>
          </SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}

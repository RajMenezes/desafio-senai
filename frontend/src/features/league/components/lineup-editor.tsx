import { Check, CircleCheck, Lock, Swords, TriangleAlert } from 'lucide-react'
import { useMemo, useState } from 'react'
import { ActionButton } from '@/components/common/action-button'
import { ErrorAlert } from '@/components/common/error-alert'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import type { ClubDetail } from '@/features/clubs/types'
import { ConditionBadge, PlayerAvatar } from '@/features/players/components/player-cells'
import { POSITION_LABELS, type Player } from '@/features/players/types'
import { notifySuccess } from '@/lib/notify'
import { cn } from '@/lib/utils'
import { useSaveLineup } from '../api'
import {
  assignToSlots,
  candidatesFor,
  DEFAULT_FORMATION,
  describeFormation,
  slotsFor,
  STARTERS_COUNT,
} from '../formations'
import { FORMATIONS, type Formation, type Lineup } from '../types'
import { LineupPitch } from './lineup-pitch'

/** Atributos que o titular usa na partida (RN20): ataca com o da posição e defende com a defesa. */
function usedAttributes(player: Player): { label: string; value: number }[] {
  const defending = { label: 'Defesa', value: player.defending }
  if (player.position === 'GOL') return [defending]
  if (player.position === 'ATA')
    return [{ label: 'Finalização', value: player.shooting }, defending]
  if (player.position === 'MEI') return [{ label: 'Passe', value: player.passing }, defending]
  return [{ label: 'Drible', value: player.dribbling }, defending]
}

function joinNames(names: string[]): string {
  return names.length <= 1
    ? names.join('')
    : `${names.slice(0, -1).join(', ')} e ${names[names.length - 1]}`
}

function IncompleteWarning({ club, lineup }: { club: ClubDetail; lineup: Lineup }) {
  return (
    <Alert variant="warning">
      <TriangleAlert aria-hidden />
      <AlertTitle>Escalação incompleta: o {club.name} não pode jogar partidas</AlertTitle>
      <AlertDescription>
        {lineup.formation === null
          ? `O clube ainda não tem escalação. Escolha a formação e um titular para cada uma das ${STARTERS_COUNT} vagas: 1 goleiro e 4 jogadores de linha.`
          : `A escalação salva (${lineup.formation}) tem ${lineup.players.length} de ${STARTERS_COUNT} titulares, porque um titular foi vendido ou ficou doente. Escale de novo para voltar a jogar.`}
      </AlertDescription>
    </Alert>
  )
}

function FormationPicker({
  value,
  onChange,
}: {
  value: Formation
  onChange: (formation: Formation) => void
}) {
  return (
    <fieldset className="flex flex-col gap-2 px-4">
      <legend className="text-sm font-medium">Formação</legend>
      <p className="text-xs text-muted-foreground">
        Os números contam os jogadores de linha de trás para frente. O goleiro fica fora da conta.
      </p>
      <div className="mt-1 grid grid-cols-2 gap-2 md:grid-cols-4">
        {FORMATIONS.map((formation) => (
          <label
            key={formation}
            className={cn(
              'flex cursor-pointer items-start gap-2 rounded-lg border p-3 transition-colors hover:bg-muted',
              'has-checked:border-primary has-checked:bg-primary-soft has-focus-visible:ring-3 has-focus-visible:ring-ring/50',
            )}
          >
            <input
              type="radio"
              name="formation"
              value={formation}
              checked={value === formation}
              onChange={() => onChange(formation)}
              className="mt-1 accent-primary"
            />
            <span className="flex flex-col">
              <span className="font-semibold">{formation}</span>
              <span className="text-xs text-muted-foreground">{describeFormation(formation)}</span>
            </span>
          </label>
        ))}
      </div>
    </fieldset>
  )
}

function Reserves({ reserves }: { reserves: Player[] }) {
  return (
    <section aria-labelledby="reserves-title" className="rounded-xl border bg-card">
      <div className="border-b p-4">
        <h3 id="reserves-title" className="font-semibold">
          Reservas
        </h3>
        <p className="text-xs text-muted-foreground">Jogadores do elenco fora da escalação.</p>
      </div>
      {reserves.length === 0 ? (
        <p className="p-4 text-sm text-muted-foreground">
          Nenhum reserva: todos os jogadores do elenco estão escolhidos.
        </p>
      ) : (
        <ul className="divide-y">
          {reserves.map((player) => (
            <li key={player.id} className="flex flex-col gap-2 p-4">
              <div className="flex items-center gap-3">
                <PlayerAvatar id={player.id} name={player.name} />
                <div className="min-w-0 flex-1 leading-tight">
                  <p className="truncate font-medium">{player.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {POSITION_LABELS[player.position]}
                  </p>
                </div>
                <ConditionBadge condition={player.condition} />
              </div>
              {player.condition === 'DOENTE' && (
                <p className="flex items-center gap-1.5 rounded-md bg-muted-subtle px-2 py-1 text-xs text-muted-foreground">
                  <Lock className="size-3" aria-hidden />
                  Doente não pode ser escalado.
                </p>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

interface LineupEditorProps {
  club: ClubDetail
  lineup: Lineup
}

/** Escolha da formação e dos 5 titulares. Começa com a escalação salva e só grava ao salvar. */
export function LineupEditor({ club, lineup }: LineupEditorProps) {
  const savedFormation = lineup.formation ?? DEFAULT_FORMATION
  const [formation, setFormation] = useState<Formation>(savedFormation)
  const [chosen, setChosen] = useState(() =>
    assignToSlots(slotsFor(savedFormation), lineup.players),
  )
  const saveLineup = useSaveLineup(club.id)

  const slots = useMemo(() => slotsFor(formation), [formation])
  const playersById = useMemo(
    () => new Map(club.players.map((player) => [player.id, player])),
    [club.players],
  )
  const chosenPlayers = chosen.flatMap((id) => {
    const player = id === undefined ? undefined : playersById.get(id)
    return player ? [player] : []
  })
  const reserves = club.players.filter((player) => !chosen.includes(player.id))
  const unfit = chosenPlayers.filter((player) => player.condition === 'MAL_CONDICIONADO')
  const allSlotsFilled = chosen.every((id) => id !== undefined)

  const changeFormation = (next: Formation) => {
    setFormation(next)
    setChosen(assignToSlots(slotsFor(next), chosenPlayers))
  }

  const changeSlot = (index: number, playerId: number) =>
    setChosen((current) => current.map((id, slotIndex) => (slotIndex === index ? playerId : id)))

  const reset = () => {
    setFormation(savedFormation)
    setChosen(assignToSlots(slotsFor(savedFormation), lineup.players))
    saveLineup.reset()
  }

  const save = () => {
    const playerIds = chosen.filter((id) => id !== undefined)
    saveLineup.mutate(
      { formation, player_ids: playerIds },
      {
        onSuccess: (saved) =>
          notifySuccess(
            'Escalação salva',
            `${club.name} está pronto para jogar no ${saved.formation}.`,
          ),
      },
    )
  }

  return (
    <div className="flex flex-col gap-4">
      {!lineup.complete && <IncompleteWarning club={club} lineup={lineup} />}
      <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
        <section aria-labelledby="starters-title" className="rounded-xl border bg-card">
          <div className="flex flex-wrap items-start justify-between gap-2 border-b p-4">
            <div>
              <h2 id="starters-title" className="font-semibold">
                Titulares
              </h2>
              <p className="text-sm text-muted-foreground">
                1 goleiro e 4 jogadores de linha, na formação escolhida. Salvar troca os 5 de uma
                vez.
              </p>
            </div>
            {lineup.complete ? (
              <Badge variant="success">
                <CircleCheck aria-hidden />
                Completa
              </Badge>
            ) : (
              <Badge variant="warning">
                <TriangleAlert aria-hidden />
                Incompleta
              </Badge>
            )}
          </div>

          <div className="flex flex-col gap-4 py-4">
            {saveLineup.error && (
              <div className="px-4">
                <ErrorAlert title="A escalação foi recusada" error={saveLineup.error} />
              </div>
            )}
            <FormationPicker value={formation} onChange={changeFormation} />
            <Table>
              <TableHeader className="bg-muted-subtle">
                <TableRow>
                  <TableHead className="px-4">Vaga</TableHead>
                  <TableHead className="px-2">Titular</TableHead>
                  <TableHead className="px-2">Condição</TableHead>
                  <TableHead className="px-2 text-right">Overall</TableHead>
                  <TableHead className="px-2">Atributos usados</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {slots.map((slot, index) => {
                  const candidates = candidatesFor(slot, index, club.players, chosen)
                  const id = chosen[index]
                  const player = id === undefined ? undefined : playersById.get(id)
                  const slotId = `lineup-slot-${index}`
                  return (
                    <TableRow key={`${formation}-${index}`}>
                      <TableCell className="px-4">
                        <label htmlFor={slotId} className="flex flex-col leading-tight">
                          <span className="font-medium">{slot.label}</span>
                          <span className="text-xs text-muted-foreground">
                            {slot.positionsLabel}
                          </span>
                        </label>
                      </TableCell>
                      <TableCell className="px-2">
                        <Select
                          value={id === undefined ? '' : String(id)}
                          onValueChange={(value) => changeSlot(index, Number(value))}
                          disabled={candidates.length === 0}
                        >
                          <SelectTrigger id={slotId} className="w-52">
                            <SelectValue
                              placeholder={
                                candidates.length === 0
                                  ? `Sem ${slot.positionsLabel} disponível`
                                  : 'Escolha o titular'
                              }
                            />
                          </SelectTrigger>
                          <SelectContent>
                            {candidates.map((candidate) => (
                              <SelectItem key={candidate.id} value={String(candidate.id)}>
                                {candidate.name} ({candidate.position})
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </TableCell>
                      <TableCell className="px-2">
                        {player && <ConditionBadge condition={player.condition} />}
                      </TableCell>
                      <TableCell className="px-2 text-right font-semibold tabular-nums">
                        {player?.overall}
                      </TableCell>
                      <TableCell className="px-2">
                        {player && (
                          <span className="flex gap-3 text-muted-foreground">
                            {usedAttributes(player).map((attribute) => (
                              <span key={attribute.label}>
                                {attribute.label}{' '}
                                <span className="font-medium text-foreground tabular-nums">
                                  {attribute.value}
                                </span>
                              </span>
                            ))}
                          </span>
                        )}
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
            {unfit.length > 0 && (
              <div className="px-4">
                <Alert variant="warning">
                  <TriangleAlert aria-hidden />
                  <AlertTitle>
                    {joinNames(unfit.map((player) => player.name))}{' '}
                    {unfit.length === 1 ? 'está mal condicionado' : 'estão mal condicionados'}
                  </AlertTitle>
                  <AlertDescription>
                    Podem jogar, mas cada atributo vale 15% a menos durante a partida.
                  </AlertDescription>
                </Alert>
              </div>
            )}
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 border-t p-4">
            <p className="max-w-md text-xs text-muted-foreground">
              {allSlotsFilled
                ? 'A escalação vale até ser trocada. Titular vendido ou que fica doente sai dela, e o clube só volta a jogar depois de escalar de novo.'
                : 'Escolha a formação e um titular para cada vaga para salvar.'}
            </p>
            <div className="flex gap-2">
              <Button variant="outline" disabled={saveLineup.isPending} onClick={reset}>
                Cancelar
              </Button>
              <ActionButton
                pending={saveLineup.isPending}
                disabled={!allSlotsFilled}
                onClick={save}
              >
                {!saveLineup.isPending && <Check data-icon="inline-start" aria-hidden />}
                Salvar escalação
              </ActionButton>
            </div>
          </div>
        </section>

        <div className="flex flex-col gap-4">
          <section aria-labelledby="pitch-title" className="rounded-xl border bg-card">
            <div className="border-b p-4">
              <h3 id="pitch-title" className="font-semibold">
                Em campo
              </h3>
              <p className="text-xs text-muted-foreground">
                Prévia da formação {formation}; as vagas vazias ficam tracejadas.
              </p>
            </div>
            <div className="p-4">
              <LineupPitch slots={slots} chosen={chosen} playersById={playersById} />
            </div>
            <p className="flex gap-2 border-t p-4 text-xs text-muted-foreground">
              <Swords className="size-3.5 shrink-0" aria-hidden />
              Na partida, atacam os 3 jogadores de linha mais avançados e defendem o goleiro e os 2
              mais recuados. É gol quando o ataque é maior que a defesa.
            </p>
          </section>
          <Reserves reserves={reserves} />
        </div>
      </div>
    </div>
  )
}

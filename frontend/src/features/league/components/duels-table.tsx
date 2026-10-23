import { CircleCheck } from 'lucide-react'
import { Fragment } from 'react'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { PlayerAvatar } from '@/features/players/components/player-cells'
import { CONDITION_LABELS } from '@/features/players/types'
import {
  DUEL_ATTRIBUTE_LABELS,
  type Duel,
  type DuelSide,
  type Match,
  type MatchTeam,
} from '../types'

/**
 * Papel de cada lado pelo número do duelo (RN20): nos duelos 1 e 4 o 4º jogador de linha ataca
 * o goleiro; nos 2 e 5, o 3º ataca o 1º; nos 3 e 6, o 2º ataca o 2º.
 */
const ROLES_BY_SLOT = [
  { attacker: '4º de linha', defender: 'Goleiro' },
  { attacker: '3º de linha', defender: '1º de linha' },
  { attacker: '2º de linha', defender: '2º de linha' },
]

function rolesOf(order: number) {
  return ROLES_BY_SLOT[(order - 1) % ROLES_BY_SLOT.length]
}

function DuelPlayer({ side, role, clubName }: { side: DuelSide; role: string; clubName: string }) {
  return (
    <div className="flex items-center gap-3">
      <PlayerAvatar id={side.player_id} name={side.player_name} />
      <div className="leading-tight">
        <p className="font-medium">{side.player_name}</p>
        <p className="text-xs text-muted-foreground">
          {side.position}, {role} · {clubName}
          {side.condition === 'MAL_CONDICIONADO' && ` · ${CONDITION_LABELS[side.condition]}`}
        </p>
      </div>
    </div>
  )
}

function DuelValue({ side }: { side: DuelSide }) {
  return (
    <span className="text-muted-foreground">
      {DUEL_ATTRIBUTE_LABELS[side.attribute]}{' '}
      <span className="font-semibold text-foreground tabular-nums">{side.value}</span>
    </span>
  )
}

interface AttackGroup {
  team: MatchTeam
  opponent: MatchTeam
  role: 'mandante' | 'visitante'
  duels: Duel[]
}

/** Os 6 duelos devolvidos pela API, agrupados pelos ataques de cada clube, sem recalcular nada. */
export function DuelsTable({ match, duels }: { match: Match; duels: Duel[] }) {
  const groups: AttackGroup[] = [
    { team: match.home, opponent: match.away, role: 'mandante', duels: [] },
    { team: match.away, opponent: match.home, role: 'visitante', duels: [] },
  ]
  for (const duel of duels) {
    groups.find((group) => group.team.club_id === duel.attacking_club_id)?.duels.push(duel)
  }

  return (
    <Table>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          <TableHead className="px-4">Duelo</TableHead>
          <TableHead className="px-2">Atacante</TableHead>
          <TableHead className="px-2">Ataque</TableHead>
          <TableHead className="px-2">Defensor</TableHead>
          <TableHead className="px-2">Defesa</TableHead>
          <TableHead className="px-4">Resultado</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {groups.map((group) => (
          <Fragment key={group.role}>
            <TableRow className="bg-muted-subtle hover:bg-muted-subtle">
              <TableCell colSpan={5} className="px-4 text-sm font-medium">
                Ataques do {group.team.club_name} ({group.role})
              </TableCell>
              <TableCell className="px-4 text-right text-sm text-muted-foreground">
                {group.team.goals} {group.team.goals === 1 ? 'gol' : 'gols'}
              </TableCell>
            </TableRow>
            {group.duels.map((duel) => {
              const roles = rolesOf(duel.order)
              return (
                <TableRow key={duel.order}>
                  <TableCell className="px-4">
                    <Badge variant="outline" className="rounded-md tabular-nums">
                      {duel.order}
                    </Badge>
                  </TableCell>
                  <TableCell className="px-2">
                    <DuelPlayer
                      side={duel.attacker}
                      role={roles.attacker}
                      clubName={group.team.club_name}
                    />
                  </TableCell>
                  <TableCell className="px-2">
                    <DuelValue side={duel.attacker} />
                  </TableCell>
                  <TableCell className="px-2">
                    <DuelPlayer
                      side={duel.defender}
                      role={roles.defender}
                      clubName={group.opponent.club_name}
                    />
                  </TableCell>
                  <TableCell className="px-2">
                    <DuelValue side={duel.defender} />
                  </TableCell>
                  <TableCell className="px-4">
                    {duel.goal ? (
                      <Badge variant="success">
                        <CircleCheck aria-hidden />
                        Gol
                      </Badge>
                    ) : (
                      <Badge variant="outline">Sem gol</Badge>
                    )}
                  </TableCell>
                </TableRow>
              )
            })}
          </Fragment>
        ))}
      </TableBody>
    </Table>
  )
}

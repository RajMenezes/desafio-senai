import { Info, Swords } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import { paths } from '@/app/paths'
import { ActionButton } from '@/components/common/action-button'
import { ErrorAlert } from '@/components/common/error-alert'
import { FormField } from '@/components/common/form-field'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { Club } from '@/features/clubs/types'
import { notifySuccess } from '@/lib/notify'
import { usePlayMatch } from '../api'

interface ClubSelectProps {
  value: string
  clubs: Club[]
  onChange: (value: string) => void
  controlProps: { id: string; 'aria-invalid': boolean; 'aria-describedby'?: string }
}

function ClubSelect({ value, clubs, onChange, controlProps }: ClubSelectProps) {
  return (
    <Select value={value} onValueChange={onChange}>
      <SelectTrigger {...controlProps} className="w-full">
        <SelectValue placeholder="Selecione o clube" />
      </SelectTrigger>
      <SelectContent>
        {clubs.map((club) => (
          <SelectItem key={club.id} value={String(club.id)}>
            {club.name} ({club.acronym})
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}

interface NewMatchDialogProps {
  open: boolean
  clubs: Club[]
  onOpenChange: (open: boolean) => void
}

/**
 * Escolha do mandante e do visitante. A tela só exige os dois clubes, diferentes entre si;
 * a escalação incompleta é recusada pela API (409) e o `message` aparece no diálogo.
 */
export function NewMatchDialog({ open, clubs, onOpenChange }: NewMatchDialogProps) {
  const navigate = useNavigate()
  const playMatch = usePlayMatch()
  const [homeId, setHomeId] = useState('')
  const [awayId, setAwayId] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const homeError = submitted && homeId === '' ? 'Escolha o mandante.' : undefined
  const awayError = submitted && awayId === '' ? 'Escolha o visitante.' : undefined

  const handleOpenChange = (nextOpen: boolean) => {
    if (playMatch.isPending) return
    if (!nextOpen) {
      setHomeId('')
      setAwayId('')
      setSubmitted(false)
      playMatch.reset()
    }
    onOpenChange(nextOpen)
  }

  const changeHome = (value: string) => {
    setHomeId(value)
    if (value === awayId) setAwayId('')
  }

  const play = () => {
    setSubmitted(true)
    if (homeId === '' || awayId === '') return
    playMatch.mutate(
      { home_club_id: Number(homeId), away_club_id: Number(awayId) },
      {
        onSuccess: (match) => {
          notifySuccess(
            'Partida jogada',
            `${match.home.club_name} ${match.home.goals} × ${match.away.goals} ${match.away.club_name}. Placar e duelos devolvidos pela API.`,
          )
          handleOpenChange(false)
          navigate(paths.match(match.id))
        },
      },
    )
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Nova partida</DialogTitle>
          <DialogDescription>
            Escolha o mandante e o visitante. O placar sai dos atributos dos titulares, sem sorteio.
          </DialogDescription>
        </DialogHeader>
        <div className="flex flex-col gap-5">
          {playMatch.error && (
            <ErrorAlert title="Não foi possível jogar a partida" error={playMatch.error} />
          )}
          <FormField
            id="match-home"
            label="Mandante"
            hint="Faz os 3 primeiros ataques."
            error={homeError}
          >
            {(controlProps) => (
              <ClubSelect
                value={homeId}
                clubs={clubs}
                onChange={changeHome}
                controlProps={controlProps}
              />
            )}
          </FormField>
          <FormField
            id="match-away"
            label="Visitante"
            hint="Precisa ser um clube diferente do mandante."
            error={awayError}
          >
            {(controlProps) => (
              <ClubSelect
                value={awayId}
                clubs={clubs.filter((club) => String(club.id) !== homeId)}
                onChange={setAwayId}
                controlProps={controlProps}
              />
            )}
          </FormField>
          <div className="flex flex-col gap-2 rounded-lg border bg-muted-subtle p-3 text-xs text-muted-foreground">
            <p className="flex items-center gap-2 text-sm font-medium text-foreground">
              <Info className="size-4" aria-hidden />
              Como sai o placar
            </p>
            <p>
              Cada time faz 3 ataques. Pela ordem do elenco, os jogadores de linha vão do 1º (o mais
              recuado) ao 4º (o mais avançado): o 4º enfrenta o goleiro, o 3º enfrenta o 1º e o 2º
              enfrenta o 2º do adversário. O atacante usa finalização (ATA), passe (MEI) ou drible
              (ZAG e LAT), e quem defende usa defesa. É gol só quando o ataque é maior; mal
              condicionado joga com 15% a menos.
            </p>
            <p>
              Os dois clubes precisam ter os 5 titulares escalados. A partida não mexe no caixa.
            </p>
          </div>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            disabled={playMatch.isPending}
            onClick={() => handleOpenChange(false)}
          >
            Cancelar
          </Button>
          <ActionButton pending={playMatch.isPending} onClick={play}>
            {!playMatch.isPending && <Swords data-icon="inline-start" aria-hidden />}
            Jogar partida
          </ActionButton>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

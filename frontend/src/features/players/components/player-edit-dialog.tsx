import { zodResolver } from '@hookform/resolvers/zod'
import { Lock } from 'lucide-react'
import { useMemo } from 'react'
import { Controller, useForm } from 'react-hook-form'
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
import { Input } from '@/components/ui/input'
import { formatMoney } from '@/lib/format'
import { notifySuccess } from '@/lib/notify'
import { useUpdatePlayer } from '../api'
import {
  AGE_LIMITS,
  AGILITY_LIMITS,
  NAME_LIMITS,
  updatePlayerSchema,
  VETERAN_AGE,
  VETERAN_MAX_AGILITY,
  type UpdatePlayerInput,
  type UpdatePlayerRequest,
} from '../player-schemas'
import type { Player } from '../types'
import { PositionPicker } from './position-picker'

interface PlayerEditDialogProps {
  player: Player
  onClose: () => void
}

export function PlayerEditDialog({ player, onClose }: PlayerEditDialogProps) {
  const updatePlayer = useUpdatePlayer(player.id)
  const schema = useMemo(() => updatePlayerSchema(player.age), [player.age])
  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdatePlayerInput, unknown, UpdatePlayerRequest>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: player.name,
      position: player.position,
      age: String(player.age),
      agility: String(player.agility),
    },
  })

  const hasClub = player.club_id !== null

  const handleOpenChange = (open: boolean) => {
    if (!open && !updatePlayer.isPending) onClose()
  }

  const onSubmit = handleSubmit((values) => {
    updatePlayer.mutate(values, {
      onSuccess: (updated) => {
        notifySuccess('Jogador atualizado', `As alterações de ${updated.name} foram salvas.`)
        onClose()
      },
    })
  })

  return (
    <Dialog open onOpenChange={handleOpenChange}>
      <DialogContent className="max-h-[calc(100dvh-2rem)] overflow-y-auto sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>Editar jogador</DialogTitle>
          <DialogDescription>
            Edite nome, posição, idade e agilidade. Os demais dados não mudam.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
          {updatePlayer.error && (
            <ErrorAlert title="Não foi possível salvar as alterações" error={updatePlayer.error} />
          )}

          <FormField
            id="edit-player-name"
            label="Nome"
            hint={`De ${NAME_LIMITS.min} a ${NAME_LIMITS.max} caracteres, com pelo menos duas palavras e sem números.`}
            error={errors.name?.message}
          >
            {(controlProps) => (
              <Input {...controlProps} autoComplete="off" maxLength={150} {...register('name')} />
            )}
          </FormField>

          <Controller
            control={control}
            name="position"
            render={({ field }) => (
              <PositionPicker
                name={field.name}
                value={field.value}
                onChange={field.onChange}
                lockedTo={hasClub ? player.position : undefined}
                hint={
                  hasClub
                    ? 'Jogador com clube não muda de posição: as outras posições ficam desabilitadas.'
                    : undefined
                }
              />
            )}
          />

          <div className="grid gap-4 sm:grid-cols-2">
            <FormField
              id="edit-player-age"
              label="Idade"
              hint={`De ${AGE_LIMITS.min} a ${AGE_LIMITS.max}, sem diminuir (atual: ${player.age}).`}
              error={errors.age?.message}
            >
              {(controlProps) => (
                <Input {...controlProps} inputMode="numeric" {...register('age')} />
              )}
            </FormField>
            <FormField
              id="edit-player-agility"
              label="Agilidade"
              hint={`De ${AGILITY_LIMITS.min} a ${AGILITY_LIMITS.max} (até ${VETERAN_MAX_AGILITY} com ${VETERAN_AGE} anos ou mais).`}
              error={errors.agility?.message}
            >
              {(controlProps) => (
                <Input {...controlProps} inputMode="numeric" {...register('agility')} />
              )}
            </FormField>
          </div>

          <p className="flex items-center gap-2 rounded-lg border border-dashed px-3 py-2 text-xs text-muted-foreground">
            <Lock className="size-3.5 shrink-0" aria-hidden />
            Não mudam depois do cadastro: valor base ({formatMoney(player.base_value)}), indicadores
            e potencial.
          </p>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              disabled={updatePlayer.isPending}
              onClick={onClose}
            >
              Cancelar
            </Button>
            <ActionButton type="submit" pending={updatePlayer.isPending}>
              Salvar alterações
            </ActionButton>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

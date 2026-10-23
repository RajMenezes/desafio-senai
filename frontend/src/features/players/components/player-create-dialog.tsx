import { zodResolver } from '@hookform/resolvers/zod'
import { Info } from 'lucide-react'
import { Controller, useForm } from 'react-hook-form'
import { ActionButton } from '@/components/common/action-button'
import { ErrorAlert } from '@/components/common/error-alert'
import { FormField } from '@/components/common/form-field'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { useCreatePlayer } from '../api'
import {
  AGE_LIMITS,
  AGILITY_LIMITS,
  ATTRIBUTES,
  createPlayerSchema,
  EMPTY_CREATE_PLAYER_FORM,
  INDICATOR_LIMITS,
  MAX_ATTRIBUTES_TOTAL,
  NAME_LIMITS,
  POTENTIAL_LOCK_AGE,
  VETERAN_AGE,
  VETERAN_MAX_AGILITY,
  type CreatePlayerInput,
  type CreatePlayerRequest,
} from '../player-schemas'
import type { Player } from '../types'
import { LockedValue } from './locked-value'
import { PositionPicker } from './position-picker'

interface PlayerCreateDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onCreated: (player: Player) => void
}

export function PlayerCreateDialog({ open, onOpenChange, onCreated }: PlayerCreateDialogProps) {
  const createPlayer = useCreatePlayer()
  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreatePlayerInput, unknown, CreatePlayerRequest>({
    resolver: zodResolver(createPlayerSchema),
    defaultValues: EMPTY_CREATE_PLAYER_FORM,
  })

  const handleOpenChange = (nextOpen: boolean) => {
    if (createPlayer.isPending) return
    if (!nextOpen) {
      reset()
      createPlayer.reset()
    }
    onOpenChange(nextOpen)
  }

  const onSubmit = handleSubmit((values) => {
    createPlayer.mutate(values, {
      onSuccess: (player) => {
        reset()
        createPlayer.reset()
        onCreated(player)
      },
    })
  })

  const hasErrors = Object.keys(errors).length > 0

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-h-[calc(100dvh-2rem)] overflow-y-auto sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle>Novo jogador</DialogTitle>
          <DialogDescription>
            {hasErrors
              ? 'Corrija os campos destacados para salvar.'
              : 'Preencha os dados e os indicadores técnicos. O valor base, o overall e o valor de mercado são calculados ao salvar.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
          {createPlayer.error && (
            <ErrorAlert title="Não foi possível cadastrar o jogador" error={createPlayer.error} />
          )}

          <FormField
            id="player-name"
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
              <PositionPicker name={field.name} value={field.value} onChange={field.onChange} />
            )}
          />

          <div className="grid gap-4 sm:grid-cols-3">
            <FormField
              id="player-age"
              label="Idade"
              hint={`De ${AGE_LIMITS.min} a ${AGE_LIMITS.max} anos.`}
              error={errors.age?.message}
            >
              {(controlProps) => (
                <Input {...controlProps} inputMode="numeric" {...register('age')} />
              )}
            </FormField>
            <FormField
              id="player-agility"
              label="Agilidade"
              hint={`De ${AGILITY_LIMITS.min} a ${AGILITY_LIMITS.max} (até ${VETERAN_MAX_AGILITY} com ${VETERAN_AGE} anos ou mais).`}
              error={errors.agility?.message}
            >
              {(controlProps) => (
                <Input {...controlProps} inputMode="numeric" {...register('agility')} />
              )}
            </FormField>
            <LockedValue label="Valor base (moedas)" hint="Calculado no cadastro." />
          </div>

          <Separator />

          <section aria-labelledby="player-indicators-title" className="flex flex-col gap-4">
            <div>
              <h3 id="player-indicators-title" className="font-semibold">
                Indicadores técnicos
              </h3>
              <p className="text-sm text-muted-foreground">
                Seis atributos e o potencial, de {INDICATOR_LIMITS.min} a {INDICATOR_LIMITS.max}.
                Não mudam depois do cadastro; a soma dos seis atributos não pode passar de{' '}
                {MAX_ATTRIBUTES_TOTAL}.
              </p>
              {errors.root?.message && (
                <p role="alert" className="mt-1 text-sm text-destructive">
                  {errors.root.message}
                </p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              {ATTRIBUTES.map(({ key, label }) => (
                <FormField
                  key={key}
                  id={`player-${key}`}
                  label={label}
                  error={errors[key]?.message}
                >
                  {(controlProps) => (
                    <Input {...controlProps} inputMode="numeric" {...register(key)} />
                  )}
                </FormField>
              ))}
              <FormField
                id="player-potential"
                label="Potencial"
                hint={`≥ maior atributo; igual a ele com ${POTENTIAL_LOCK_AGE}+ anos.`}
                error={errors.potential?.message}
              >
                {(controlProps) => (
                  <Input {...controlProps} inputMode="numeric" {...register('potential')} />
                )}
              </FormField>
              <LockedValue label="Overall" hint="Calculado pela posição." />
            </div>
          </section>

          <Separator />

          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <Info className="size-3.5" aria-hidden />O jogador começa livre e Apto.
            </p>
            <div className="flex gap-2">
              <Button
                type="button"
                variant="outline"
                disabled={createPlayer.isPending}
                onClick={() => handleOpenChange(false)}
              >
                Cancelar
              </Button>
              <ActionButton type="submit" pending={createPlayer.isPending}>
                Salvar jogador
              </ActionButton>
            </div>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}

import { zodResolver } from '@hookform/resolvers/zod'
import { Wallet } from 'lucide-react'
import { useForm } from 'react-hook-form'
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
import { Input } from '@/components/ui/input'
import { formatMoney } from '@/lib/format'
import { notifySuccess } from '@/lib/notify'
import { useCreateClub } from '../api'
import {
  CLUB_NAME_LIMITS,
  CLUB_NAME_WORDS,
  createClubSchema,
  type CreateClubInput,
  type CreateClubRequest,
} from '../club-schema'
import { CLUB_INITIAL_CASH } from '../types'

interface CreateClubDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function CreateClubDialog({ open, onOpenChange }: CreateClubDialogProps) {
  const navigate = useNavigate()
  const createClub = useCreateClub()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateClubInput, unknown, CreateClubRequest>({
    resolver: zodResolver(createClubSchema),
    defaultValues: { name: '', acronym: '' },
  })

  const handleOpenChange = (nextOpen: boolean) => {
    if (createClub.isPending) return
    if (!nextOpen) {
      reset()
      createClub.reset()
    }
    onOpenChange(nextOpen)
  }

  const onSubmit = handleSubmit((values) => {
    createClub.mutate(values, {
      onSuccess: (club) => {
        notifySuccess(
          'Clube criado',
          `${club.name} (${club.acronym}) começa com ${formatMoney(club.cash)} em caixa.`,
        )
        reset()
        createClub.reset()
        navigate(paths.club(club.id))
      },
    })
  })

  const acronymField = register('acronym', {
    setValueAs: (value: string) => value.toUpperCase(),
  })

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Novo clube</DialogTitle>
          <DialogDescription>Informe o nome e a sigla do clube.</DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
          {createClub.error && (
            <ErrorAlert title="Não foi possível criar o clube" error={createClub.error} />
          )}
          <FormField
            id="club-name"
            label="Nome"
            hint={`De ${CLUB_NAME_LIMITS.min} a ${CLUB_NAME_LIMITS.max} caracteres e de ${CLUB_NAME_WORDS.min} a ${CLUB_NAME_WORDS.max} palavras; só letras, espaços, hífen e ponto. Não pode repetir outro clube.`}
            error={errors.name?.message}
          >
            {(controlProps) => (
              <Input {...controlProps} autoComplete="off" maxLength={80} {...register('name')} />
            )}
          </FormField>
          <FormField
            id="club-acronym"
            label="Sigla"
            hint="3 letras de A a Z, maiúsculas, que aparecem no nome nessa ordem; a primeira é a primeira letra do nome."
            error={errors.acronym?.message}
          >
            {(controlProps) => (
              <Input
                {...controlProps}
                autoComplete="off"
                maxLength={3}
                className="uppercase"
                {...acronymField}
              />
            )}
          </FormField>
          <p className="flex items-center gap-2 rounded-lg border bg-muted-subtle px-3 py-2 text-sm text-muted-foreground">
            <Wallet className="size-4 shrink-0" aria-hidden />O clube começa com{' '}
            {formatMoney(CLUB_INITIAL_CASH)} em caixa e sem jogadores.
          </p>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              disabled={createClub.isPending}
              onClick={() => handleOpenChange(false)}
            >
              Cancelar
            </Button>
            <ActionButton type="submit" pending={createClub.isPending}>
              Criar clube
            </ActionButton>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

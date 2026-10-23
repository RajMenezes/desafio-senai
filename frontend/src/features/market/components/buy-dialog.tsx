import { zodResolver } from '@hookform/resolvers/zod'
import { Info, ShoppingCart } from 'lucide-react'
import { useMemo } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Link } from 'react-router'
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useClubs } from '@/features/clubs/api'
import type { Club } from '@/features/clubs/types'
import { PlayerSummary } from '@/features/players/components/player-summary'
import type { Player } from '@/features/players/types'
import { NetworkError } from '@/lib/api/errors'
import { toDateTimeInputs } from '@/lib/date-time'
import { formatInteger, formatMoney } from '@/lib/format'
import { cn } from '@/lib/utils'
import { useBuyPlayer } from '../api'
import { buySchema, MAX_LOYALTY_DAYS, type BuyFormInput } from '../buy-schema'
import type { BuyPlayerRequest, PurchaseResult } from '../types'

const ONE_YEAR_MS = 365 * 24 * 60 * 60 * 1000

/** Sugestão inicial da fidelidade: daqui a um ano, no horário de Brasília. */
function defaultFormValues(): BuyFormInput {
  return { clubId: '', ...toDateTimeInputs(new Date(Date.now() + ONE_YEAR_MS)) }
}

function BuyError({ error }: { error: unknown }) {
  if (error instanceof NetworkError) {
    return (
      <ErrorAlert
        title="Sem conexão"
        error={error}
        message="A compra não foi confirmada. Verifique a conexão e tente de novo."
      />
    )
  }
  return <ErrorAlert title="Compra recusada" error={error} />
}

interface BuyDialogProps {
  player: Player
  onClose: () => void
  onPurchased: (result: PurchaseResult, club: Club | undefined) => void
}

export function BuyDialog({ player, onClose, onPurchased }: BuyDialogProps) {
  const buyPlayer = useBuyPlayer(player.id)
  const clubsQuery = useClubs()
  const clubs = useMemo(() => clubsQuery.data ?? [], [clubsQuery.data])
  const schema = useMemo(() => buySchema(), [])
  const {
    control,
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<BuyFormInput, unknown, BuyPlayerRequest>({
    resolver: zodResolver(schema),
    defaultValues: defaultFormValues(),
  })

  const selectedClubId = useWatch({ control, name: 'clubId' })
  const selectedClub = clubs.find((club) => String(club.id) === selectedClubId)
  const loyaltyError = errors.date?.message ?? errors.time?.message

  const handleOpenChange = (open: boolean) => {
    if (!open && !buyPlayer.isPending) onClose()
  }

  const onSubmit = handleSubmit((purchase) => {
    buyPlayer.mutate(purchase, {
      onSuccess: (result) => onPurchased(result, selectedClub),
    })
  })

  return (
    <Dialog open onOpenChange={handleOpenChange}>
      <DialogContent className="max-h-[calc(100dvh-2rem)] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Comprar jogador</DialogTitle>
          <DialogDescription>
            Escolha o clube comprador e até quando vai a fidelidade do contrato.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} noValidate className="flex flex-col gap-5">
          {buyPlayer.error && <BuyError error={buyPlayer.error} />}

          <PlayerSummary player={player} detail="market_value" />

          <FormField
            id="buy-club"
            label="Clube comprador"
            hint={
              selectedClub
                ? `Caixa atual: ${formatMoney(selectedClub.cash)}. O custo total (valor + taxa) pode ir até 50% do caixa.`
                : 'O custo total (valor + taxa) pode ir até 50% do caixa do clube.'
            }
            error={errors.clubId?.message}
          >
            {(controlProps) =>
              clubsQuery.data && clubs.length === 0 ? (
                <p className="rounded-lg border border-dashed px-3 py-2 text-sm text-muted-foreground">
                  Nenhum clube cadastrado.{' '}
                  <Link to={paths.newClub} className="font-medium text-primary underline">
                    Crie um clube
                  </Link>{' '}
                  para comprar jogadores.
                </p>
              ) : (
                <Controller
                  control={control}
                  name="clubId"
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger {...controlProps} className="w-full">
                        <SelectValue placeholder="Selecione o clube" />
                      </SelectTrigger>
                      <SelectContent>
                        {clubs.map((club) => (
                          <SelectItem key={club.id} value={String(club.id)}>
                            {club.name} ({club.acronym}) · caixa {formatMoney(club.cash)}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
              )
            }
          </FormField>

          <fieldset className="flex flex-col gap-2" aria-describedby="buy-loyalty-message">
            <legend className="mb-2 text-sm font-medium">Fim da fidelidade</legend>
            <div className="grid grid-cols-[1fr_8rem] gap-2">
              <Input
                type="date"
                aria-label="Data do fim da fidelidade"
                aria-invalid={errors.date !== undefined}
                {...register('date')}
              />
              <Input
                type="time"
                aria-label="Hora do fim da fidelidade"
                aria-invalid={errors.time !== undefined}
                {...register('time')}
              />
            </div>
            <p
              id="buy-loyalty-message"
              className={cn('text-xs', loyaltyError ? 'text-destructive' : 'text-muted-foreground')}
            >
              {loyaltyError ??
                `Depois do momento da compra e até ${formatInteger(MAX_LOYALTY_DAYS)} dias. Horário de Brasília; enviado com o fuso (-03:00).`}
            </p>
          </fieldset>

          <p className="flex items-start gap-2 rounded-lg border bg-muted-subtle px-3 py-2 text-xs text-muted-foreground">
            <Info className="mt-px size-3.5 shrink-0" aria-hidden />A taxa é de 5% do valor de
            mercado, só quando ele passa de 30,00. Taxa, custo e novo caixa são calculados ao
            confirmar.
          </p>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              disabled={buyPlayer.isPending}
              onClick={onClose}
            >
              Cancelar
            </Button>
            <ActionButton type="submit" pending={buyPlayer.isPending} disabled={clubs.length === 0}>
              {!buyPlayer.isPending && <ShoppingCart data-icon="inline-start" aria-hidden />}
              {buyPlayer.error instanceof NetworkError ? 'Tentar de novo' : 'Confirmar compra'}
            </ActionButton>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

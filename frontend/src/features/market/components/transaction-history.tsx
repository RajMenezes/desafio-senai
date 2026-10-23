import { Download, History, User } from 'lucide-react'
import { useMemo, useState } from 'react'
import { ActionButton } from '@/components/common/action-button'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorAlert } from '@/components/common/error-alert'
import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import { SelectFilter } from '@/components/common/select-filter'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { ApiError } from '@/lib/api/errors'
import { notifyError, notifySuccess } from '@/lib/notify'
import { saveFile } from '@/lib/save-file'
import { cn } from '@/lib/utils'
import { useExportTransactions, useTransactions } from '../api'
import { ALL_TRANSACTION_FILTERS, hasTransactionFilters } from '../transaction-filters'
import { TRANSACTION_TYPES, type TransactionFilters, type TransactionType } from '../types'
import { TransactionsSummary } from './transactions-summary'
import { TransactionsTable } from './transactions-table'

interface TypeToggleProps {
  types: TransactionType[]
  onChange: (types: TransactionType[]) => void
}

/** Tipos marcados; o último marcado não pode ser desmarcado (a API exige ao menos um tipo). */
function TypeToggle({ types, onChange }: TypeToggleProps) {
  const toggle = (type: TransactionType, checked: boolean) =>
    onChange(
      TRANSACTION_TYPES.map((option) => option.value).filter((value) =>
        value === type ? checked : types.includes(value),
      ),
    )

  return (
    <fieldset className="flex items-center gap-2">
      <legend className="sr-only">Tipo</legend>
      <span aria-hidden className="text-sm text-muted-foreground">
        Tipo
      </span>
      {TRANSACTION_TYPES.map(({ value, label }) => {
        const checked = types.includes(value)
        return (
          <label
            key={value}
            className={cn(
              'flex h-9 cursor-pointer items-center gap-2 rounded-lg border px-3 text-sm font-medium transition-colors',
              checked && 'border-primary bg-primary-soft text-primary',
            )}
          >
            <Checkbox
              checked={checked}
              disabled={checked && types.length === 1}
              onCheckedChange={(state) => toggle(value, state === true)}
            />
            {label}
          </label>
        )
      })}
    </fieldset>
  )
}

/** Filtro recusado pela API (ex.: jogador excluído depois de a tela abrir): 400 ou 404. */
function isFilterRefusal(error: unknown): boolean {
  return error instanceof ApiError && (error.status === 400 || error.status === 404)
}

export function TransactionHistory({ clubId }: { clubId: number }) {
  const [filters, setFilters] = useState<TransactionFilters>(ALL_TRANSACTION_FILTERS)
  const allQuery = useTransactions(clubId, ALL_TRANSACTION_FILTERS)
  const query = useTransactions(clubId, filters)
  const exportTransactions = useExportTransactions(clubId)
  const filtered = hasTransactionFilters(filters)

  /** Só os jogadores que aparecem no histórico do clube, com o nome da transação mais recente. */
  const playerOptions = useMemo(() => {
    const names = new Map<number, string>()
    for (const transaction of allQuery.data?.data ?? []) {
      if (!names.has(transaction.player_id)) {
        names.set(transaction.player_id, transaction.player_name)
      }
    }
    return [...names].map(([value, label]) => ({ value, label }))
  }, [allQuery.data])

  const handleExport = () => {
    exportTransactions.mutate(filters, {
      onSuccess: ({ blob, filename = `transacoes-clube-${clubId}.csv` }) => {
        saveFile(blob, filename)
        notifySuccess('CSV baixado', filtered ? `${filename}, com os filtros aplicados.` : filename)
      },
      onError: (error) => notifyError('Não foi possível exportar o CSV', error),
    })
  }

  const content = () => {
    if (query.isError) {
      return isFilterRefusal(query.error) ? (
        <div className="p-4">
          <ErrorAlert title="O filtro foi recusado" error={query.error} />
        </div>
      ) : (
        <ErrorState
          title="Não foi possível carregar o histórico"
          error={query.error}
          onRetry={() => query.refetch()}
          retrying={query.isFetching}
        />
      )
    }
    if (query.isPending) return <LoadingState label="Carregando histórico…" />

    const { data: transactions, summary } = query.data
    return (
      <>
        {transactions.length === 0 ? (
          <EmptyState
            icon={History}
            title={filtered ? 'Nenhuma transação com esses filtros' : 'Nenhuma transação ainda'}
            description={
              filtered
                ? 'Mude os filtros para ver outras compras e vendas do clube.'
                : 'As compras e vendas do clube aparecem aqui, com os totais logo abaixo.'
            }
          />
        ) : (
          <div className={cn('transition-opacity', query.isPlaceholderData && 'opacity-60')}>
            <TransactionsTable transactions={transactions} />
          </div>
        )}
        <TransactionsSummary summary={summary} />
      </>
    )
  }

  return (
    <section aria-labelledby="history-title" className="overflow-hidden rounded-xl border bg-card">
      <div className="flex flex-col gap-4 border-b p-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 id="history-title" className="font-semibold">
              Histórico de transações
            </h2>
            <p className="text-sm text-muted-foreground">
              Da mais recente para a mais antiga, com o nome e os valores do momento de cada
              operação.
            </p>
          </div>
          <ActionButton
            variant="outline"
            pending={exportTransactions.isPending}
            onClick={handleExport}
          >
            {!exportTransactions.isPending && <Download data-icon="inline-start" aria-hidden />}
            Exportar CSV
          </ActionButton>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <TypeToggle
            types={filters.types}
            onChange={(types) => setFilters((current) => ({ ...current, types }))}
          />
          <SelectFilter
            icon={User}
            label="Jogador"
            allLabel="Todos os jogadores"
            value={filters.playerId}
            options={playerOptions}
            onChange={(playerId) => setFilters((current) => ({ ...current, playerId }))}
          />
          {filtered && (
            <Button
              variant="link"
              className="px-1"
              onClick={() => setFilters(ALL_TRANSACTION_FILTERS)}
            >
              Limpar filtros
            </Button>
          )}
        </div>
      </div>
      {content()}
    </section>
  )
}

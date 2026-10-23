import { Plus, Shield } from 'lucide-react'
import { useSearchParams } from 'react-router'
import { CREATE_CLUB_PARAM } from '@/app/paths'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorState } from '@/components/common/error-state'
import { PageHeader } from '@/components/common/page-header'
import { SortMenu } from '@/components/common/sort-menu'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatMoney } from '@/lib/format'
import { useClubs } from './api'
import { ClubsTable } from './components/clubs-table'
import { CreateClubDialog } from './components/create-club-dialog'
import { CLUB_INITIAL_CASH, CLUB_LIMIT, CLUB_SORTS, type ClubSort } from './types'

function parseSort(value: string | null): ClubSort {
  return CLUB_SORTS.find((sort) => sort.value === value)?.value ?? 'name'
}

export function ClubsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const sort = parseSort(searchParams.get('sort'))
  const createOpen = searchParams.has(CREATE_CLUB_PARAM)
  const clubsQuery = useClubs(sort)

  const updateParams = (change: (params: URLSearchParams) => void) => {
    const next = new URLSearchParams(searchParams)
    change(next)
    setSearchParams(next, { replace: true })
  }
  const setSort = (value: ClubSort) =>
    updateParams((params) => (value === 'name' ? params.delete('sort') : params.set('sort', value)))
  const setCreateOpen = (open: boolean) =>
    updateParams((params) =>
      open ? params.set(CREATE_CLUB_PARAM, '') : params.delete(CREATE_CLUB_PARAM),
    )

  const clubs = clubsQuery.data ?? []

  const content = () => {
    if (clubsQuery.isError) {
      return (
        <ErrorState
          title="Não foi possível carregar os clubes"
          error={clubsQuery.error}
          onRetry={() => clubsQuery.refetch()}
          retrying={clubsQuery.isFetching}
        />
      )
    }
    if (clubsQuery.isPending) return <ClubsTable clubs={[]} loading />
    if (clubs.length === 0) {
      return (
        <EmptyState
          icon={Shield}
          title="Nenhum clube cadastrado"
          description={`Crie o primeiro clube com nome e sigla. Ele começa com ${formatMoney(CLUB_INITIAL_CASH)} em caixa e sem jogadores.`}
          action={
            <Button variant="outline" onClick={() => setCreateOpen(true)}>
              <Plus data-icon="inline-start" aria-hidden />
              Novo clube
            </Button>
          }
        />
      )
    }
    return <ClubsTable clubs={clubs} />
  }

  return (
    <>
      <PageHeader
        icon={Shield}
        title="Clubes"
        description={`Cada clube começa com ${formatMoney(CLUB_INITIAL_CASH)} em caixa e sem jogadores. A liga aceita no máximo ${CLUB_LIMIT} clubes.`}
        actions={
          <Button onClick={() => setCreateOpen(true)}>
            <Plus data-icon="inline-start" aria-hidden />
            Novo clube
          </Button>
        }
      />
      <section aria-label="Lista de clubes" className="rounded-xl border bg-card">
        <div className="flex flex-wrap items-center justify-between gap-3 p-4">
          <div className="flex items-center gap-2">
            <span className="font-semibold">
              {clubsQuery.data
                ? `${clubs.length} ${clubs.length === 1 ? 'clube' : 'clubes'}`
                : 'Clubes'}
            </span>
            {clubsQuery.data && (
              <Badge variant="secondary">
                {clubs.length} de {CLUB_LIMIT}
              </Badge>
            )}
          </div>
          <SortMenu options={CLUB_SORTS} value={sort} onChange={setSort} />
        </div>
        {content()}
      </section>
      <CreateClubDialog open={createOpen} onOpenChange={setCreateOpen} />
    </>
  )
}

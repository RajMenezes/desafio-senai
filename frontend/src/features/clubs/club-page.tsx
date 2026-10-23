import {
  ArrowLeft,
  Coins,
  Landmark,
  SearchX,
  Shirt,
  ShoppingCart,
  Users,
  Wallet,
} from 'lucide-react'
import { Link, useParams } from 'react-router'
import { usePageTitle } from '@/app/page-title'
import { paths } from '@/app/paths'
import { EmptyState } from '@/components/common/empty-state'
import { ErrorState } from '@/components/common/error-state'
import { LoadingState } from '@/components/common/loading-state'
import { StatCard } from '@/components/common/stat-card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ApiError, getErrorMessage } from '@/lib/api/errors'
import { formatMoney } from '@/lib/format'
import { useClub } from './api'
import { ClubTile } from './components/club-tile'
import { SquadTable } from './components/squad-table'
import { SQUAD_LIMIT, type ClubDetail } from './types'

/** Id da URL aceito pela API: inteiro positivo, só dígitos, sem zeros à esquerda (V8). */
function parseClubId(value: string | undefined): number | undefined {
  return value !== undefined && /^[1-9]\d*$/.test(value) ? Number(value) : undefined
}

function ClubNotFound({ error }: { error?: unknown }) {
  return (
    <section className="rounded-xl border bg-card">
      <EmptyState
        icon={SearchX}
        title="Clube não encontrado"
        description={
          error ? getErrorMessage(error) : 'O endereço pode estar errado ou o clube não existe.'
        }
        action={
          <Button variant="outline" asChild>
            <Link to={paths.clubs}>
              <ArrowLeft data-icon="inline-start" aria-hidden />
              Voltar para clubes
            </Link>
          </Button>
        }
      />
    </section>
  )
}

function ClubHeader({ club }: { club: ClubDetail }) {
  return (
    <header className="flex flex-wrap items-start justify-between gap-4">
      <div className="flex items-center gap-4">
        <ClubTile acronym={club.acronym} size="lg" />
        <div className="flex flex-col gap-1.5">
          <h1 className="text-2xl font-semibold tracking-tight">{club.name}</h1>
          <div className="flex flex-wrap gap-1.5">
            <Badge variant="outline" className="rounded-md">
              Sigla {club.acronym}
            </Badge>
            <Badge variant="outline" className="rounded-md">
              <Shirt aria-hidden />
              {club.players.length} de {SQUAD_LIMIT} jogadores
            </Badge>
          </div>
        </div>
      </div>
      <Button asChild>
        <Link to={paths.freePlayers}>
          <ShoppingCart data-icon="inline-start" aria-hidden />
          Comprar jogadores
        </Link>
      </Button>
    </header>
  )
}

function ClubStats({ club }: { club: ClubDetail }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard
        label="Caixa"
        value={formatMoney(club.cash)}
        unit="moedas"
        hint="Só muda por compras e vendas"
        icon={Wallet}
      />
      <StatCard
        label="Valor do elenco"
        value={formatMoney(club.squad_value)}
        unit="moedas"
        hint="Soma dos valores de mercado"
        icon={Coins}
      />
      <StatCard
        label="Patrimônio"
        value={formatMoney(club.net_worth)}
        unit="moedas"
        hint="Caixa + valor do elenco"
        icon={Landmark}
      />
      <StatCard
        label="Elenco"
        value={`${club.players.length} de ${SQUAD_LIMIT}`}
        hint={`Limite de ${SQUAD_LIMIT} jogadores`}
        icon={Shirt}
      />
    </div>
  )
}

function SquadSection({ club }: { club: ClubDetail }) {
  if (club.players.length === 0) {
    return (
      <section className="rounded-xl border bg-card">
        <EmptyState
          icon={Shirt}
          title="Nenhum jogador no elenco"
          description={`Compre jogadores livres para montar o elenco. O limite é de ${SQUAD_LIMIT} jogadores, e o custo de cada compra não pode passar de 50% do caixa.`}
          action={
            <Button variant="outline" asChild>
              <Link to={paths.freePlayers}>
                <Users data-icon="inline-start" aria-hidden />
                Ver jogadores livres
              </Link>
            </Button>
          }
        />
      </section>
    )
  }
  return (
    <section aria-labelledby="squad-title" className="rounded-xl border bg-card">
      <div className="p-4">
        <h2 id="squad-title" className="font-semibold">
          Elenco
        </h2>
        <p className="text-sm text-muted-foreground">
          Ordem por posição (GOL, ZAG, LAT, MEI, ATA), depois pelo nome. Contrato calculado para uma
          venda agora.
        </p>
      </div>
      <SquadTable players={club.players} />
    </section>
  )
}

export function ClubPage() {
  const { clubId } = useParams()
  const id = parseClubId(clubId)
  const query = useClub(id ?? 0, id !== undefined)
  const notFound =
    id === undefined || (query.error instanceof ApiError && query.error.status === 404)

  usePageTitle(query.data?.name ?? (clubId ? `Clube ${clubId}` : undefined))

  if (notFound) return <ClubNotFound error={query.error ?? undefined} />
  if (query.isError) {
    return (
      <ErrorState
        title="Não foi possível carregar o clube"
        error={query.error}
        onRetry={() => query.refetch()}
        retrying={query.isFetching}
      />
    )
  }
  if (query.isPending) return <LoadingState label="Carregando clube…" />

  const club = query.data
  return (
    <>
      <ClubHeader club={club} />
      <ClubStats club={club} />
      <Tabs defaultValue="squad" className="gap-4">
        <TabsList variant="line" aria-label="Seções do clube">
          <TabsTrigger value="squad" className="gap-1.5">
            <Shirt aria-hidden />
            Elenco
            <Badge variant="secondary">{club.players.length}</Badge>
          </TabsTrigger>
        </TabsList>
        <TabsContent value="squad">
          <SquadSection club={club} />
        </TabsContent>
      </Tabs>
    </>
  )
}

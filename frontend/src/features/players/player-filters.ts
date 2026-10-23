import type { QueryParams } from '@/lib/api/client'
import type { Range } from '@/lib/range'
import { CONDITIONS, POSITIONS, type Condition, type Position } from './types'

const PLAYERS_PAGE_SIZE = 10

export const MARKET_VALUE_LIMITS = { min: 0, max: 550 } as const
export const SEARCH_MIN_LENGTH = 2

/** A API recusa `free=true` junto com `club_id` (400); a tela impede essa combinação. */
export const FREE_WITH_CLUB_REASON = '“Livres” não combina com o filtro por clube.'

export const PLAYER_SORTS = [
  { value: 'name', label: 'Nome' },
  { value: 'value_desc', label: 'Maior valor de mercado' },
  { value: 'value_asc', label: 'Menor valor de mercado' },
  { value: 'age_desc', label: 'Maior idade' },
  { value: 'age_asc', label: 'Menor idade' },
  { value: 'position', label: 'Posição (GOL a ATA)' },
] as const
type PlayerSort = (typeof PLAYER_SORTS)[number]['value']

const DEFAULT_SORT: PlayerSort = 'name'

/** Abas da lista: todos, só livres (`free=true`) ou só com clube (`free=false`). */
export type PlayerTab = 'all' | 'free' | 'in_club'

export interface PlayerFilters {
  search: string
  position?: Position
  condition?: Condition
  clubId?: number
  age: Range
  marketValue: Range
  tab: PlayerTab
  sort: PlayerSort
  page: number
}

export const EMPTY_PLAYER_FILTERS: PlayerFilters = {
  search: '',
  age: {},
  marketValue: {},
  tab: 'all',
  sort: DEFAULT_SORT,
  page: 1,
}

function parseOption<T extends string>(value: string | null, options: readonly T[]): T | undefined {
  return options.find((option) => option === value)
}

function parsePositiveInteger(value: string | null): number | undefined {
  return value !== null && /^[1-9]\d*$/.test(value) ? Number(value) : undefined
}

function parseNumber(value: string | null): number | undefined {
  return value !== null && /^\d+(\.\d{1,2})?$/.test(value) ? Number(value) : undefined
}

function parseTab(free: string | null): PlayerTab {
  if (free === 'true') return 'free'
  if (free === 'false') return 'in_club'
  return 'all'
}

/** Lê os filtros da URL; valores inválidos são ignorados. */
export function parsePlayerFilters(params: URLSearchParams): PlayerFilters {
  return {
    search: params.get('search') ?? '',
    position: parseOption(params.get('position'), POSITIONS),
    condition: parseOption(params.get('condition'), CONDITIONS),
    clubId: parsePositiveInteger(params.get('club_id')),
    age: {
      min: parsePositiveInteger(params.get('min_age')),
      max: parsePositiveInteger(params.get('max_age')),
    },
    marketValue: {
      min: parseNumber(params.get('min_value')),
      max: parseNumber(params.get('max_value')),
    },
    tab: parseTab(params.get('free')),
    sort:
      parseOption(
        params.get('sort'),
        PLAYER_SORTS.map((sort) => sort.value),
      ) ?? DEFAULT_SORT,
    page: parsePositiveInteger(params.get('page')) ?? 1,
  }
}

function freeParam(tab: PlayerTab): boolean | undefined {
  if (tab === 'free') return true
  if (tab === 'in_club') return false
  return undefined
}

function filterParams(filters: PlayerFilters): QueryParams {
  return {
    position: filters.position,
    condition: filters.condition,
    club_id: filters.clubId,
    min_age: filters.age.min,
    max_age: filters.age.max,
    min_value: filters.marketValue.min,
    max_value: filters.marketValue.max,
  }
}

/** Parâmetros da URL da tela, sem os valores padrão. */
export function toSearchParams(filters: PlayerFilters): URLSearchParams {
  const params: QueryParams = {
    search: filters.search,
    ...filterParams(filters),
    free: freeParam(filters.tab),
    sort: filters.sort === DEFAULT_SORT ? undefined : filters.sort,
    page: filters.page === 1 ? undefined : filters.page,
  }
  const searchParams = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, String(value))
    }
  }
  return searchParams
}

/** Busca efetivamente enviada à API: a API recusa textos com menos de 2 caracteres. */
export function effectiveSearch(search: string): string | undefined {
  const trimmed = search.trim()
  return trimmed.length >= SEARCH_MIN_LENGTH ? trimmed : undefined
}

/** Query params do `GET /players` para a página atual, na aba informada. */
export function toPlayersQuery(filters: PlayerFilters, tab: PlayerTab = filters.tab): QueryParams {
  return {
    search: effectiveSearch(filters.search),
    ...filterParams(filters),
    free: freeParam(tab),
    sort: filters.sort,
    page: filters.page,
    limit: PLAYERS_PAGE_SIZE,
  }
}

/** Query params que só contam os jogadores de uma aba (`meta.total_items`). */
export function toTabCountQuery(filters: PlayerFilters, tab: PlayerTab): QueryParams {
  return { ...toPlayersQuery(filters, tab), sort: undefined, page: 1, limit: 1 }
}

export function hasActiveFilters(filters: PlayerFilters): boolean {
  return (
    effectiveSearch(filters.search) !== undefined ||
    Object.values(filterParams(filters)).some((value) => value !== undefined)
  )
}

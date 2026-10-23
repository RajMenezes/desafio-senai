import { describe, expect, it } from 'vitest'
import {
  EMPTY_PLAYER_FILTERS,
  hasActiveFilters,
  parsePlayerFilters,
  toPlayersQuery,
  toSearchParams,
  toTabCountQuery,
  type PlayerFilters,
} from './player-filters'

const filters = (changes: Partial<PlayerFilters>): PlayerFilters => ({
  ...EMPTY_PLAYER_FILTERS,
  ...changes,
})

describe('parsePlayerFilters', () => {
  it('lê os filtros da URL com os nomes da API', () => {
    const params = new URLSearchParams(
      'search=lua&position=MEI&condition=APTO&club_id=3&min_age=18&max_age=30&min_value=10.5&max_value=60&free=false&sort=value_desc&page=2',
    )

    expect(parsePlayerFilters(params)).toEqual({
      search: 'lua',
      position: 'MEI',
      condition: 'APTO',
      clubId: 3,
      age: { min: 18, max: 30 },
      marketValue: { min: 10.5, max: 60 },
      tab: 'in_club',
      sort: 'value_desc',
      page: 2,
    })
  })

  it('usa os padrões quando a URL não tem filtros', () => {
    expect(parsePlayerFilters(new URLSearchParams())).toEqual(EMPTY_PLAYER_FILTERS)
  })

  it('ignora valores que a API recusaria', () => {
    const params = new URLSearchParams(
      'position=mei&condition=X&club_id=0&min_age=-1&min_value=1e3&sort=POSITION&page=abc',
    )

    expect(parsePlayerFilters(params)).toEqual(EMPTY_PLAYER_FILTERS)
  })
})

describe('toSearchParams', () => {
  it('omite os valores padrão', () => {
    expect(toSearchParams(EMPTY_PLAYER_FILTERS).toString()).toBe('')
  })

  it('faz o caminho de volta de parsePlayerFilters', () => {
    const query = 'search=lua&position=MEI&club_id=3&min_age=18&free=false&sort=age_asc&page=2'

    expect(toSearchParams(parsePlayerFilters(new URLSearchParams(query))).toString()).toBe(query)
  })
})

describe('toPlayersQuery', () => {
  it('monta a página atual com 10 jogadores', () => {
    expect(toPlayersQuery(filters({ tab: 'free', page: 3, position: 'ATA' }))).toEqual({
      search: undefined,
      position: 'ATA',
      condition: undefined,
      club_id: undefined,
      min_age: undefined,
      max_age: undefined,
      min_value: undefined,
      max_value: undefined,
      free: true,
      sort: 'name',
      page: 3,
      limit: 10,
    })
  })

  it('só envia a busca com 2 caracteres ou mais, sem espaços nas pontas', () => {
    expect(toPlayersQuery(filters({ search: ' j ' })).search).toBeUndefined()
    expect(toPlayersQuery(filters({ search: '  jo ' })).search).toBe('jo')
  })
})

describe('toTabCountQuery', () => {
  it('conta a aba pedida na primeira página, com 1 item', () => {
    const query = toTabCountQuery(filters({ tab: 'all', page: 4, sort: 'age_desc' }), 'in_club')

    expect(query).toMatchObject({ free: false, page: 1, limit: 1, sort: undefined })
  })
})

describe('hasActiveFilters', () => {
  it('não considera a aba, a ordenação, a página nem a busca curta', () => {
    expect(hasActiveFilters(filters({ tab: 'free', sort: 'position', page: 2, search: 'a' }))).toBe(
      false,
    )
  })

  it('considera qualquer filtro preenchido', () => {
    expect(hasActiveFilters(filters({ age: { max: 30 } }))).toBe(true)
    expect(hasActiveFilters(filters({ search: 'lua' }))).toBe(true)
  })
})

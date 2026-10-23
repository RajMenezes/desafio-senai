import { useCallback, useMemo } from 'react'
import { useClubs } from '@/features/clubs/api'

/** Sigla de cada clube pelo id; enquanto os clubes não chegam, mostra um traço. */
export function useClubAcronyms(): (clubId: number) => string {
  const clubsQuery = useClubs()
  const acronyms = useMemo(
    () => new Map((clubsQuery.data ?? []).map((club) => [club.id, club.acronym])),
    [clubsQuery.data],
  )
  return useCallback((clubId: number) => acronyms.get(clubId) ?? '—', [acronyms])
}

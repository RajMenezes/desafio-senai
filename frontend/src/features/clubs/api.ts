import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { request } from '@/lib/api/client'
import type { CreateClubRequest } from './club-schema'
import type { Club, ClubDetail, ClubSort } from './types'

export const clubKeys = {
  all: ['clubs'] as const,
  list: (sort: ClubSort) => [...clubKeys.all, 'list', sort] as const,
  detail: (id: number) => [...clubKeys.all, 'detail', id] as const,
}

/** Clubes na ordem pedida; o padrão da API é por nome. */
export function useClubs(sort: ClubSort = 'name') {
  return useQuery({
    queryKey: clubKeys.list(sort),
    queryFn: () => request<Club[]>('/clubs', { params: { sort } }),
  })
}

export function useClub(id: number, enabled = true) {
  return useQuery({
    queryKey: clubKeys.detail(id),
    queryFn: () => request<ClubDetail>(`/clubs/${id}`),
    enabled,
  })
}

export function useCreateClub() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (club: CreateClubRequest) =>
      request<Club>('/clubs', { method: 'POST', body: club }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: clubKeys.all }),
  })
}

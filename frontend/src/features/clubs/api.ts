import { useQuery } from '@tanstack/react-query'
import { request } from '@/lib/api/client'
import type { Club } from './types'

export const clubKeys = {
  all: ['clubs'] as const,
  list: () => [...clubKeys.all, 'list'] as const,
}

function fetchClubs() {
  return request<Club[]>('/clubs')
}

/** Clubes na ordem padrão da API (por nome). */
export function useClubs() {
  return useQuery({ queryKey: clubKeys.list(), queryFn: fetchClubs })
}

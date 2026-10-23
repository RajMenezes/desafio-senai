import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './errors'

const MAX_QUERY_RETRIES = 1

function shouldRetry(failureCount: number, error: Error): boolean {
  return !(error instanceof ApiError) && failureCount < MAX_QUERY_RETRIES
}

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: shouldRetry,
        refetchOnWindowFocus: false,
      },
      mutations: {
        retry: false,
      },
    },
  })
}

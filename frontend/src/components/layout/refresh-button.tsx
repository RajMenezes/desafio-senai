import { useIsFetching, useQueryClient } from '@tanstack/react-query'
import { RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

export function RefreshButton() {
  const queryClient = useQueryClient()
  const isFetching = useIsFetching() > 0

  return (
    <Button
      variant="ghost"
      size="icon"
      aria-label="Atualizar dados"
      title="Atualizar dados"
      disabled={isFetching}
      onClick={() => queryClient.invalidateQueries()}
    >
      <RefreshCw className={cn(isFetching && 'animate-spin')} aria-hidden />
    </Button>
  )
}

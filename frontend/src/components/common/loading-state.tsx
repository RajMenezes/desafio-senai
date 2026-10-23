import { Skeleton } from '@/components/ui/skeleton'

interface LoadingStateProps {
  label?: string
}

export function LoadingState({ label = 'Carregando…' }: LoadingStateProps) {
  return (
    <div role="status" aria-live="polite" className="flex flex-col gap-3 p-4">
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-3/4" />
      <Skeleton className="h-4 w-1/2" />
      <span className="pt-2 text-sm text-muted-foreground">{label}</span>
    </div>
  )
}

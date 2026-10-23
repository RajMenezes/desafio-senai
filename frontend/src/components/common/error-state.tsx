import { RefreshCw, ServerOff } from 'lucide-react'
import { ActionButton } from '@/components/common/action-button'
import { getErrorMessage } from '@/lib/api/errors'

interface ErrorStateProps {
  title: string
  error: unknown
  onRetry: () => void
  retrying?: boolean
}

export function ErrorState({ title, error, onRetry, retrying = false }: ErrorStateProps) {
  return (
    <div role="alert" className="flex flex-col items-center gap-2 px-4 py-12 text-center">
      <div className="mb-2 flex size-12 items-center justify-center rounded-xl bg-muted text-muted-foreground">
        <ServerOff className="size-5" aria-hidden />
      </div>
      <p className="font-semibold">{title}</p>
      <p className="max-w-md text-sm text-muted-foreground">{getErrorMessage(error)}</p>
      <ActionButton variant="outline" className="mt-2" pending={retrying} onClick={onRetry}>
        {!retrying && <RefreshCw data-icon="inline-start" aria-hidden />}
        Tentar de novo
      </ActionButton>
    </div>
  )
}

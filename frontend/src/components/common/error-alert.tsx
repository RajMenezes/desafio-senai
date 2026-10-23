import { CircleAlert } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { getErrorMessage } from '@/lib/api/errors'

interface ErrorAlertProps {
  title: string
  error: unknown
  /** Texto no lugar da mensagem do erro (ex.: orientação específica para falha de rede). */
  message?: string
}

export function ErrorAlert({ title, error, message }: ErrorAlertProps) {
  return (
    <Alert variant="destructive">
      <CircleAlert aria-hidden />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>{message ?? getErrorMessage(error)}</AlertDescription>
    </Alert>
  )
}

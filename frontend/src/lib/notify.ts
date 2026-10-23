import { toast } from 'sonner'
import { getErrorMessage } from './api/errors'

export function notifySuccess(title: string, description?: string) {
  toast.success(title, { description })
}

export function notifyError(title: string, error: unknown) {
  toast.error(title, { description: getErrorMessage(error) })
}

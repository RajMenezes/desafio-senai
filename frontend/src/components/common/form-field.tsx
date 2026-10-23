import { CircleAlert } from 'lucide-react'
import type { ReactNode } from 'react'
import { Field, FieldDescription, FieldError, FieldLabel } from '@/components/ui/field'

interface FieldControlProps {
  id: string
  'aria-invalid': boolean
  'aria-describedby'?: string
}

interface FormFieldProps {
  id: string
  label: string
  hint?: ReactNode
  error?: string
  className?: string
  children: (controlProps: FieldControlProps) => ReactNode
}

/** Campo de formulário com rótulo, dica e erro; o erro aparece no lugar da dica. */
export function FormField({ id, label, hint, error, className, children }: FormFieldProps) {
  const messageId = `${id}-message`
  const hasMessage = error !== undefined || hint !== undefined

  return (
    <Field data-invalid={error !== undefined} className={className}>
      <FieldLabel htmlFor={id}>{label}</FieldLabel>
      {children({
        id,
        'aria-invalid': error !== undefined,
        'aria-describedby': hasMessage ? messageId : undefined,
      })}
      {error !== undefined ? (
        <FieldError id={messageId} className="flex items-start gap-1.5 text-xs">
          <CircleAlert className="mt-px size-3.5 shrink-0" aria-hidden />
          {error}
        </FieldError>
      ) : (
        hint !== undefined && (
          <FieldDescription id={messageId} className="text-xs">
            {hint}
          </FieldDescription>
        )
      )}
    </Field>
  )
}

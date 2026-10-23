import { cn } from '@/lib/utils'

/** Quadro com a sigla do clube, usado como "escudo" nas listas e no cabeçalho. */
export function ClubTile({ acronym, size = 'md' }: { acronym: string; size?: 'md' | 'lg' }) {
  return (
    <span
      aria-hidden
      className={cn(
        'flex shrink-0 items-center justify-center rounded-lg border bg-muted-subtle font-semibold',
        size === 'md' ? 'size-9 text-xs' : 'size-12 text-sm',
      )}
    >
      {acronym}
    </span>
  )
}

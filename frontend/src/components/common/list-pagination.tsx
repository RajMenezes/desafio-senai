import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import type { PageMeta } from '@/lib/api/types'
import { formatInteger } from '@/lib/format'

type PageItem = number | 'ellipsis-start' | 'ellipsis-end'

const MAX_PAGES_WITHOUT_ELLIPSIS = 7

/** Páginas exibidas: todas quando são poucas; senão a primeira, a última e as vizinhas da atual. */
function pageItems(page: number, totalPages: number): PageItem[] {
  if (totalPages <= MAX_PAGES_WITHOUT_ELLIPSIS) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }
  const start = Math.max(2, Math.min(page - 1, totalPages - 4))
  const end = Math.min(totalPages - 1, Math.max(page + 1, 5))
  const middle = Array.from({ length: end - start + 1 }, (_, index) => start + index)
  return [
    1,
    ...(start > 2 ? (['ellipsis-start'] as const) : []),
    ...middle,
    ...(end < totalPages - 1 ? (['ellipsis-end'] as const) : []),
    totalPages,
  ]
}

interface ListPaginationProps {
  meta: PageMeta
  itemLabel: string
  onPageChange: (page: number) => void
}

export function ListPagination({ meta, itemLabel, onPageChange }: ListPaginationProps) {
  const { page, limit, total_items: totalItems, total_pages: totalPages } = meta
  const from = (page - 1) * limit + 1
  const to = Math.min(page * limit, totalItems)

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
      <p className="text-sm text-muted-foreground">
        Mostrando {formatInteger(from)}–{formatInteger(to)} de {formatInteger(totalItems)}{' '}
        {itemLabel}
      </p>
      <nav aria-label="Paginação" className="flex items-center gap-1">
        <Button
          variant="ghost"
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
          aria-label="Página anterior"
        >
          <ChevronLeft data-icon="inline-start" aria-hidden />
          Anterior
        </Button>
        {pageItems(page, totalPages).map((item) =>
          typeof item === 'number' ? (
            <Button
              key={item}
              variant={item === page ? 'outline' : 'ghost'}
              size="icon"
              aria-label={`Página ${item}`}
              aria-current={item === page ? 'page' : undefined}
              onClick={() => onPageChange(item)}
            >
              {item}
            </Button>
          ) : (
            <span key={item} className="px-1 text-muted-foreground" aria-hidden>
              …
            </span>
          ),
        )}
        <Button
          variant="ghost"
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
          aria-label="Próxima página"
        >
          Próxima
          <ChevronRight data-icon="inline-end" aria-hidden />
        </Button>
      </nav>
    </div>
  )
}

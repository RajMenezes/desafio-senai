import { Skeleton } from '@/components/ui/skeleton'
import { TableBody, TableCell, TableRow } from '@/components/ui/table'

interface TableSkeletonProps {
  columns: number
  rows?: number
}

/** Corpo de tabela com linhas cinza enquanto os dados carregam. */
export function TableSkeleton({ columns, rows = 10 }: TableSkeletonProps) {
  return (
    <TableBody>
      {Array.from({ length: rows }, (_, row) => (
        <TableRow key={row}>
          {Array.from({ length: columns }, (_, column) => (
            <TableCell key={column} className="py-4">
              <Skeleton className="h-4 w-full max-w-28" />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </TableBody>
  )
}

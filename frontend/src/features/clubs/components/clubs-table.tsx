import { ChevronRight } from 'lucide-react'
import { Link } from 'react-router'
import { paths } from '@/app/paths'
import { TableSkeleton } from '@/components/common/table-skeleton'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { formatMoney } from '@/lib/format'
import { cn } from '@/lib/utils'
import type { Club } from '../types'
import { ClubTile } from './club-tile'

const COLUMNS = [
  { label: 'Clube' },
  { label: 'Sigla' },
  { label: 'Caixa', numeric: true },
  { label: 'Valor do elenco', numeric: true },
  { label: 'Patrimônio', numeric: true },
  { label: 'Ações', hidden: true },
]

export function ClubsTable({ clubs, loading = false }: { clubs: Club[]; loading?: boolean }) {
  return (
    <Table aria-busy={loading}>
      <TableHeader className="bg-muted-subtle">
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHead key={column.label} className={cn('px-4', column.numeric && 'text-right')}>
              {column.hidden ? <span className="sr-only">{column.label}</span> : column.label}
            </TableHead>
          ))}
        </TableRow>
      </TableHeader>
      {loading ? (
        <TableSkeleton columns={COLUMNS.length} rows={5} />
      ) : (
        <TableBody>
          {clubs.map((club) => (
            <TableRow key={club.id}>
              <TableCell className="px-4">
                <div className="flex items-center gap-3">
                  <ClubTile acronym={club.acronym} />
                  <span className="font-medium">{club.name}</span>
                </div>
              </TableCell>
              <TableCell className="px-4">
                <Badge variant="outline" className="rounded-md">
                  {club.acronym}
                </Badge>
              </TableCell>
              <TableCell className="px-4 text-right tabular-nums">
                {formatMoney(club.cash)}
              </TableCell>
              <TableCell className="px-4 text-right tabular-nums">
                {formatMoney(club.squad_value)}
              </TableCell>
              <TableCell className="px-4 text-right font-semibold tabular-nums">
                {formatMoney(club.net_worth)}
              </TableCell>
              <TableCell className="px-4 text-right">
                <Link
                  to={paths.club(club.id)}
                  className="inline-flex items-center gap-1 font-medium hover:text-primary"
                  aria-label={`Ver clube ${club.name}`}
                >
                  Ver clube
                  <ChevronRight className="size-4" aria-hidden />
                </Link>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      )}
    </Table>
  )
}

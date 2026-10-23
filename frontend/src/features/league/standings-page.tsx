import { ListOrdered } from 'lucide-react'
import { PageHeader } from '@/components/common/page-header'

export function StandingsPage() {
  return (
    <PageHeader
      icon={ListOrdered}
      title="Classificação"
      description="Pontos, saldo e gols de cada clube na liga."
    />
  )
}

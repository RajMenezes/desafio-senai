import { Users } from 'lucide-react'
import { PageHeader } from '@/components/common/page-header'

export function PlayersPage() {
  return (
    <PageHeader
      icon={Users}
      title="Jogadores"
      description="Cadastre jogadores, acompanhe os indicadores e compre os que estão livres."
    />
  )
}

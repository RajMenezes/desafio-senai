import { Shield } from 'lucide-react'
import { PageHeader } from '@/components/common/page-header'

export function ClubsPage() {
  return (
    <PageHeader
      icon={Shield}
      title="Clubes"
      description="Crie clubes e acompanhe o caixa, o valor do elenco e o patrimônio."
    />
  )
}

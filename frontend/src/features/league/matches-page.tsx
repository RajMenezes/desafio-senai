import { Swords } from 'lucide-react'
import { PageHeader } from '@/components/common/page-header'

export function MatchesPage() {
  return (
    <PageHeader
      icon={Swords}
      title="Partidas"
      description="Escolha o mandante e o visitante, jogue e confira os duelos."
    />
  )
}

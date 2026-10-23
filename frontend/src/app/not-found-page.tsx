import { Link } from 'react-router'
import { EmptyState } from '@/components/common/empty-state'
import { Button } from '@/components/ui/button'

export function NotFoundPage() {
  return (
    <EmptyState
      title="Página não encontrada"
      description="O endereço acessado não existe. Volte para a lista de jogadores."
      action={
        <Button variant="outline" asChild>
          <Link to="/jogadores">Ir para jogadores</Link>
        </Button>
      }
    />
  )
}

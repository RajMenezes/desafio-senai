import { TriangleAlert } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'

const CONSEQUENCES = {
  sale: 'Ao vender',
  sick: 'Ao passar a Doente',
}

interface StarterWarningProps {
  playerName: string
  clubName: string
  reason: keyof typeof CONSEQUENCES
}

/** Aviso de que o titular sai da escalação na venda ou ao ficar doente (RN19). */
export function StarterWarning({ playerName, clubName, reason }: StarterWarningProps) {
  return (
    <Alert variant="warning">
      <TriangleAlert aria-hidden />
      <AlertTitle>{playerName} é titular</AlertTitle>
      <AlertDescription>
        {CONSEQUENCES[reason]}, o jogador sai da escalação, e o {clubName} só volta a jogar partidas
        depois de escalar de novo os 5 titulares.
      </AlertDescription>
    </Alert>
  )
}

import { z } from 'zod'
import { toApiDateTime } from '@/lib/date-time'
import type { BuyPlayerRequest } from './types'

/** Prazo máximo da fidelidade, contado do momento da compra (2.3.1). */
export const MAX_LOYALTY_DAYS = 1095

const DAY_MS = 24 * 60 * 60 * 1000

/**
 * Formulário da compra. A fidelidade precisa ser futura e ter até 1.095 dias; a data e a hora
 * digitadas valem no horário de Brasília. `now` é lido a cada validação.
 */
export function buySchema(now: () => Date = () => new Date()) {
  return z
    .object({
      clubId: z.string().min(1, 'Escolha o clube comprador.'),
      date: z.string().min(1, 'Informe a data.'),
      time: z.string().min(1, 'Informe a hora.'),
    })
    .superRefine(({ date, time }, context) => {
      if (date === '' || time === '') return
      const loyaltyUntil = Date.parse(toApiDateTime(date, time))
      const current = now().getTime()
      if (Number.isNaN(loyaltyUntil)) {
        context.addIssue({ code: 'custom', path: ['date'], message: 'Data ou hora inválida.' })
      } else if (loyaltyUntil <= current) {
        context.addIssue({
          code: 'custom',
          path: ['date'],
          message: 'Escolha um momento depois de agora.',
        })
      } else if (loyaltyUntil > current + MAX_LOYALTY_DAYS * DAY_MS) {
        context.addIssue({
          code: 'custom',
          path: ['date'],
          message: 'A fidelidade vai até 1.095 dias a partir de agora.',
        })
      }
    })
    .transform(({ clubId, date, time }): BuyPlayerRequest => ({
      club_id: Number(clubId),
      loyalty_until: toApiDateTime(date, time),
    }))
}

export type BuyFormInput = z.input<ReturnType<typeof buySchema>>

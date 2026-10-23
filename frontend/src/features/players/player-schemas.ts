import { z } from 'zod'
import { characterCount, normalizeSpaces, wordCount } from '@/lib/text'
import { POSITIONS } from './types'

export const NAME_LIMITS = { min: 3, max: 100 } as const
export const AGE_LIMITS = { min: 16, max: 45 } as const
export const AGILITY_LIMITS = { min: 1, max: 100 } as const
export const VETERAN_AGE = 35
export const VETERAN_MAX_AGILITY = 85
export const INDICATOR_LIMITS = { min: 1, max: 99 } as const
export const MAX_ATTRIBUTES_TOTAL = 480
export const POTENTIAL_LOCK_AGE = 33

export const ATTRIBUTES = [
  { key: 'pace', label: 'Velocidade' },
  { key: 'shooting', label: 'Finalização' },
  { key: 'passing', label: 'Passe' },
  { key: 'dribbling', label: 'Drible' },
  { key: 'defending', label: 'Defesa' },
  { key: 'physical', label: 'Físico' },
] as const
export type AttributeKey = (typeof ATTRIBUTES)[number]['key']

const nameField = z
  .string()
  .transform(normalizeSpaces)
  .superRefine((name, context) => {
    if (name === '') {
      context.addIssue({ code: 'custom', message: 'Informe o nome.' })
    } else if (characterCount(name) < NAME_LIMITS.min || characterCount(name) > NAME_LIMITS.max) {
      context.addIssue({
        code: 'custom',
        message: `Use de ${NAME_LIMITS.min} a ${NAME_LIMITS.max} caracteres.`,
      })
    } else if (/\d/.test(name)) {
      context.addIssue({ code: 'custom', message: 'O nome não pode ter números.' })
    } else if (wordCount(name) < 2) {
      context.addIssue({ code: 'custom', message: 'Informe pelo menos duas palavras.' })
    }
  })

/** Inteiro digitado num campo de texto, com mensagens para vazio, formato e faixa. */
function integerField(missingMessage: string, rangeMessage: string, min: number, max: number) {
  return z
    .string()
    .trim()
    .min(1, { message: missingMessage, abort: true })
    .regex(/^\d+$/, { message: 'Use um número inteiro.', abort: true })
    .transform(Number)
    .pipe(z.number().min(min, rangeMessage).max(max, rangeMessage))
}

const ageField = integerField(
  'Informe a idade.',
  `Use uma idade de ${AGE_LIMITS.min} a ${AGE_LIMITS.max} anos.`,
  AGE_LIMITS.min,
  AGE_LIMITS.max,
)

const agilityField = integerField(
  'Informe a agilidade.',
  `Use um valor de ${AGILITY_LIMITS.min} a ${AGILITY_LIMITS.max}.`,
  AGILITY_LIMITS.min,
  AGILITY_LIMITS.max,
)

function indicatorField(missingMessage: string) {
  return integerField(
    missingMessage,
    `Use um valor de ${INDICATOR_LIMITS.min} a ${INDICATOR_LIMITS.max}.`,
    INDICATOR_LIMITS.min,
    INDICATOR_LIMITS.max,
  )
}

const positionField = z.enum(POSITIONS, { error: 'Escolha a posição.' })

function isWithin(value: number, limits: { min: number; max: number }): boolean {
  return Number.isInteger(value) && value >= limits.min && value <= limits.max
}

/** As regras entre campos só valem quando cada campo envolvido já é válido sozinho. */
function checkVeteranAgility(values: { age: number; agility: number }, context: z.RefinementCtx) {
  if (!isWithin(values.age, AGE_LIMITS) || !isWithin(values.agility, AGILITY_LIMITS)) return
  if (values.age >= VETERAN_AGE && values.agility > VETERAN_MAX_AGILITY) {
    context.addIssue({
      code: 'custom',
      path: ['agility'],
      message: `Com ${VETERAN_AGE} anos ou mais, a agilidade vai até ${VETERAN_MAX_AGILITY}.`,
    })
  }
}

export const createPlayerSchema = z
  .object({
    name: nameField,
    position: positionField,
    age: ageField,
    agility: agilityField,
    pace: indicatorField('Informe a velocidade.'),
    shooting: indicatorField('Informe a finalização.'),
    passing: indicatorField('Informe o passe.'),
    dribbling: indicatorField('Informe o drible.'),
    defending: indicatorField('Informe a defesa.'),
    physical: indicatorField('Informe o físico.'),
    potential: indicatorField('Informe o potencial.'),
  })
  .superRefine((values, context) => {
    checkVeteranAgility(values, context)

    const attributes = ATTRIBUTES.map(({ key }) => values[key])
    if (!attributes.every((value) => isWithin(value, INDICATOR_LIMITS))) return

    const total = attributes.reduce((sum, value) => sum + value, 0)
    if (total > MAX_ATTRIBUTES_TOTAL) {
      context.addIssue({
        code: 'custom',
        path: ['root'],
        message: `A soma dos seis atributos é ${total} e não pode passar de ${MAX_ATTRIBUTES_TOTAL}.`,
      })
    }

    const highest = Math.max(...attributes)
    if (!isWithin(values.potential, INDICATOR_LIMITS) || !isWithin(values.age, AGE_LIMITS)) return
    if (values.age >= POTENTIAL_LOCK_AGE && values.potential !== highest) {
      context.addIssue({
        code: 'custom',
        path: ['potential'],
        message: `Com ${POTENTIAL_LOCK_AGE} anos ou mais, use o maior atributo (${highest}).`,
      })
    } else if (values.potential < highest) {
      context.addIssue({
        code: 'custom',
        path: ['potential'],
        message: `Use pelo menos o maior atributo (${highest}).`,
      })
    }
  })

export type CreatePlayerInput = z.input<typeof createPlayerSchema>
export type CreatePlayerRequest = z.output<typeof createPlayerSchema>

export const EMPTY_CREATE_PLAYER_FORM: CreatePlayerInput = {
  name: '',
  position: 'MEI',
  age: '',
  agility: '',
  pace: '',
  shooting: '',
  passing: '',
  dribbling: '',
  defending: '',
  physical: '',
  potential: '',
}

/** A edição só muda nome, posição, idade e agilidade; a idade não pode diminuir (RN08). */
export function updatePlayerSchema(currentAge: number) {
  return z
    .object({
      name: nameField,
      position: positionField,
      age: ageField.refine((age) => age >= currentAge, {
        message: `A idade não pode diminuir (atual: ${currentAge}).`,
      }),
      agility: agilityField,
    })
    .superRefine(checkVeteranAgility)
}

export type UpdatePlayerInput = z.input<ReturnType<typeof updatePlayerSchema>>
export type UpdatePlayerRequest = z.output<ReturnType<typeof updatePlayerSchema>>

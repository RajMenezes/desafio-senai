import { z } from 'zod'
import { characterCount, normalizeSpaces, removeAccents, wordCount } from '@/lib/text'

export const CLUB_NAME_LIMITS = { min: 3, max: 60 } as const
export const CLUB_NAME_WORDS = { min: 2, max: 5 } as const

const ACRONYM_PATTERN = /^[A-Z]{3}$/

/** Erro do nome já normalizado (V4), ou undefined quando ele é válido. */
function clubNameError(name: string): string | undefined {
  const length = characterCount(name)
  const words = wordCount(name)
  if (name === '') return 'Informe o nome.'
  if (length < CLUB_NAME_LIMITS.min || length > CLUB_NAME_LIMITS.max) {
    return `Use de ${CLUB_NAME_LIMITS.min} a ${CLUB_NAME_LIMITS.max} caracteres.`
  }
  if (!/^[\p{L} .-]+$/u.test(name)) return 'Use só letras, espaços, hífen e ponto.'
  if (words < CLUB_NAME_WORDS.min || words > CLUB_NAME_WORDS.max) {
    return `Use de ${CLUB_NAME_WORDS.min} a ${CLUB_NAME_WORDS.max} palavras.`
  }
  return undefined
}

function acronymError(acronym: string): string | undefined {
  if (acronym === '') return 'Informe a sigla.'
  if (!ACRONYM_PATTERN.test(acronym)) return 'Use 3 letras de A a Z, maiúsculas.'
  if (new Set(acronym).size === 1) return 'As três letras não podem ser iguais.'
  return undefined
}

/**
 * A sigla tem de aparecer no nome, nessa ordem, começando pela primeira letra do nome,
 * sem diferenciar maiúsculas, minúsculas e acentos.
 */
export function acronymMatchesName(acronym: string, name: string): boolean {
  const letters = removeAccents(name).toUpperCase()
  if (letters[0] !== acronym[0]) return false
  let position = 1
  for (const letter of acronym.slice(1)) {
    position = letters.indexOf(letter, position) + 1
    if (position === 0) return false
  }
  return true
}

export const createClubSchema = z
  .object({
    name: z.string().transform(normalizeSpaces),
    acronym: z.string(),
  })
  .superRefine(({ name, acronym }, context) => {
    const nameMessage = clubNameError(name)
    const acronymMessage = acronymError(acronym)
    if (nameMessage) context.addIssue({ code: 'custom', path: ['name'], message: nameMessage })
    if (acronymMessage) {
      context.addIssue({ code: 'custom', path: ['acronym'], message: acronymMessage })
    }
    if (!nameMessage && !acronymMessage && !acronymMatchesName(acronym, name)) {
      context.addIssue({
        code: 'custom',
        path: ['acronym'],
        message: 'Use letras do nome, nessa ordem, começando pela primeira letra.',
      })
    }
  })

export type CreateClubInput = z.input<typeof createClubSchema>
export type CreateClubRequest = z.output<typeof createClubSchema>

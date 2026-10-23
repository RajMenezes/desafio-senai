/** Faixa inclusiva de um filtro; um limite ausente assume o extremo aceito pela API. */
export interface Range {
  min?: number
  max?: number
}

export interface RangeRules {
  limits: { min: number; max: number }
  allowDecimals: boolean
  format: (value: number) => string
}

interface RangeErrors {
  min?: string
  max?: string
}

export type RangeValidation = { range: Range } | { errors: RangeErrors }

const INTEGER_PATTERN = /^\d+$/
const DECIMAL_PATTERN = /^\d+([.,]\d{1,2})?$/

type ParsedBound = { value?: number } | { error: string }

function parseBound(text: string, rules: RangeRules): ParsedBound {
  const trimmed = text.trim()
  if (trimmed === '') return {}

  const pattern = rules.allowDecimals ? DECIMAL_PATTERN : INTEGER_PATTERN
  if (!pattern.test(trimmed)) {
    return {
      error: rules.allowDecimals
        ? 'Use um número com até 2 casas decimais.'
        : 'Use um número inteiro.',
    }
  }

  const value = Number(trimmed.replace(',', '.'))
  if (value < rules.limits.min || value > rules.limits.max) {
    return {
      error: `Use um valor de ${rules.format(rules.limits.min)} a ${rules.format(rules.limits.max)}.`,
    }
  }
  return { value }
}

/** Valida os limites digitados: formato, faixa aceita e mínimo menor ou igual ao máximo. */
export function validateRange(
  minText: string,
  maxText: string,
  rules: RangeRules,
): RangeValidation {
  const min = parseBound(minText, rules)
  const max = parseBound(maxText, rules)

  if ('error' in min || 'error' in max) {
    return {
      errors: {
        min: 'error' in min ? min.error : undefined,
        max: 'error' in max ? max.error : undefined,
      },
    }
  }
  if (min.value !== undefined && max.value !== undefined && min.value > max.value) {
    return { errors: { max: 'O máximo não pode ser menor que o mínimo.' } }
  }
  return { range: { min: min.value, max: max.value } }
}

/** Texto da faixa: "18 a 30", "a partir de 18", "até 30" ou undefined sem limites. */
export function describeRange(range: Range, format: (value: number) => string): string | undefined {
  if (range.min !== undefined && range.max !== undefined) {
    return `${format(range.min)} a ${format(range.max)}`
  }
  if (range.min !== undefined) return `a partir de ${format(range.min)}`
  if (range.max !== undefined) return `até ${format(range.max)}`
  return undefined
}

const moneyFormat = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const signedMoneyFormat = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
  signDisplay: 'exceptZero',
})

const integerFormat = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 0 })

/** Moedas com 2 casas e vírgula decimal: 1744.14 → "1.744,14". */
export function formatMoney(value: number): string {
  return moneyFormat.format(value)
}

/** Variação com sinal sempre visível: 35.7 → "+35,70", -45.55 → "-45,55". */
export function formatSignedMoney(value: number): string {
  return signedMoneyFormat.format(value)
}

/** Inteiro com separador de milhar: 1089 → "1.089". */
export function formatInteger(value: number): string {
  return integerFormat.format(value)
}

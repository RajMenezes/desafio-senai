/** Remove os espaços das pontas e junta espaços repetidos, como a API faz nos nomes (V4). */
export function normalizeSpaces(text: string): string {
  return text.trim().replace(/ {2,}/g, ' ')
}

/** Tamanho em caracteres Unicode (pontos de código), como a API conta (V4). */
export function characterCount(text: string): number {
  return [...text].length
}

/** Remove os acentos como a V4 define: forma NFD sem as marcas da categoria Mn (inclui a cedilha). */
export function removeAccents(text: string): string {
  return text.normalize('NFD').replace(/\p{Mn}/gu, '')
}

/** Palavras no sentido da V4: sequências sem espaços com pelo menos uma letra. */
export function wordCount(text: string): number {
  return text.split(' ').filter((word) => /\p{L}/u.test(word)).length
}

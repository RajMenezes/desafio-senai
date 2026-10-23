const BRASILIA_TIME_ZONE = 'America/Sao_Paulo'
const BRASILIA_OFFSET = '-03:00'

const dateTimeFormat = new Intl.DateTimeFormat('pt-BR', {
  timeZone: BRASILIA_TIME_ZONE,
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hourCycle: 'h23',
})

/** Data e hora da API no horário de Brasília, separadas: { date: "07/11/2026", time: "10:30" }. */
export function formatDateTimeParts(isoDateTime: string): { date: string; time: string } {
  const parts = Object.fromEntries(
    dateTimeFormat.formatToParts(new Date(isoDateTime)).map((part) => [part.type, part.value]),
  )
  return {
    date: `${parts.day}/${parts.month}/${parts.year}`,
    time: `${parts.hour}:${parts.minute}`,
  }
}

/** Data e hora da API no horário de Brasília: "2026-11-07T10:30:00-03:00" → "07/11/2026 10:30". */
export function formatDateTime(isoDateTime: string): string {
  const { date, time } = formatDateTimeParts(isoDateTime)
  return `${date} ${time}`
}

/** Data ("AAAA-MM-DD") e hora ("HH:MM") digitadas na tela, no formato da RN01. */
export function toApiDateTime(date: string, time: string): string {
  return `${date}T${time}${BRASILIA_OFFSET}`
}

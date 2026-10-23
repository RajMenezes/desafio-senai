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

/** Dia, mês, ano, hora e minuto de um instante no horário de Brasília, com zeros à esquerda. */
function brasiliaParts(instant: Date) {
  const parts = Object.fromEntries(
    dateTimeFormat.formatToParts(instant).map((part) => [part.type, part.value]),
  )
  return {
    day: parts.day,
    month: parts.month,
    year: parts.year,
    hour: parts.hour,
    minute: parts.minute,
  }
}

/** Data e hora da API no horário de Brasília, separadas: { date: "07/11/2026", time: "10:30" }. */
export function formatDateTimeParts(isoDateTime: string): { date: string; time: string } {
  const { day, month, year, hour, minute } = brasiliaParts(new Date(isoDateTime))
  return { date: `${day}/${month}/${year}`, time: `${hour}:${minute}` }
}

/** Data e hora da API no horário de Brasília: "2026-11-07T10:30:00-03:00" → "07/11/2026 10:30". */
export function formatDateTime(isoDateTime: string): string {
  const { date, time } = formatDateTimeParts(isoDateTime)
  return `${date} ${time}`
}

/** Valores para os campos `date` ("AAAA-MM-DD") e `time` ("HH:MM") no horário de Brasília. */
export function toDateTimeInputs(instant: Date): { date: string; time: string } {
  const { day, month, year, hour, minute } = brasiliaParts(instant)
  return { date: `${year}-${month}-${day}`, time: `${hour}:${minute}` }
}

/** Data ("AAAA-MM-DD") e hora ("HH:MM") digitadas na tela, no formato da RN01. */
export function toApiDateTime(date: string, time: string): string {
  return `${date}T${time}${BRASILIA_OFFSET}`
}

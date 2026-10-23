export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export class NetworkError extends Error {
  constructor() {
    super('Não foi possível conectar à API. Verifique a conexão e tente de novo.')
    this.name = 'NetworkError'
  }
}

const UNEXPECTED_ERROR_MESSAGE = 'Ocorreu um erro inesperado. Tente de novo.'

export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError || error instanceof NetworkError) {
    return error.message
  }
  return UNEXPECTED_ERROR_MESSAGE
}

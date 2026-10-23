import { ApiError, NetworkError } from './errors'

type QueryValue = string | number | boolean | null | undefined

export type QueryParams = Record<string, QueryValue>

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

interface RequestOptions {
  method?: HttpMethod
  params?: QueryParams
  body?: unknown
}

const API_URL = import.meta.env.VITE_API_URL

export function buildUrl(path: string, params: QueryParams = {}): string {
  const url = new URL(`${API_URL}${path}`)
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  }
  return url.toString()
}

async function send(path: string, { method = 'GET', params, body }: RequestOptions) {
  let response: Response
  try {
    response = await fetch(buildUrl(path, params), {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new NetworkError()
  }

  if (!response.ok) {
    throw new ApiError(response.status, await readErrorMessage(response))
  }
  return response
}

async function readErrorMessage(response: Response): Promise<string> {
  const fallback = `A API respondeu com o status ${response.status}.`
  try {
    const body: unknown = await response.json()
    if (isErrorBody(body) && body.message.trim() !== '') {
      return body.message
    }
    return fallback
  } catch {
    return fallback
  }
}

function isErrorBody(body: unknown): body is { message: string } {
  return (
    typeof body === 'object' &&
    body !== null &&
    'message' in body &&
    typeof body.message === 'string'
  )
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await send(path, options)
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

interface DownloadedFile {
  blob: Blob
  filename?: string
}

/** Nome do arquivo no cabeçalho `Content-Disposition` (ex.: `attachment; filename="a.csv"`). */
function filenameFrom(contentDisposition: string | null): string | undefined {
  return contentDisposition?.match(/filename="?([^";]+)"?/)?.[1]
}

/** Baixa um arquivo da API; as recusas continuam vindo em JSON, com `message`. */
export async function requestFile(
  path: string,
  options: RequestOptions = {},
): Promise<DownloadedFile> {
  const response = await send(path, options)
  return {
    blob: await response.blob(),
    filename: filenameFrom(response.headers.get('Content-Disposition')),
  }
}

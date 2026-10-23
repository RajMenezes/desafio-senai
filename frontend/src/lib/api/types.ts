/** Metadados das listas paginadas (RN07). */
export interface PageMeta {
  page: number
  limit: number
  total_items: number
  total_pages: number
}

export interface Page<T> {
  data: T[]
  meta: PageMeta
}

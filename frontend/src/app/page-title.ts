import { createContext, useContext, useEffect } from 'react'

interface PageTitleState {
  title?: string
  setTitle: (title: string | undefined) => void
}

/** Título dinâmico do breadcrumb, definido pela página (ex.: o nome do clube aberto). */
export const PageTitleContext = createContext<PageTitleState>({ setTitle: () => {} })

export function useCurrentPageTitle(): string | undefined {
  return useContext(PageTitleContext).title
}

/** Troca o último item do breadcrumb enquanto a página estiver aberta. */
export function usePageTitle(title: string | undefined) {
  const { setTitle } = useContext(PageTitleContext)
  useEffect(() => {
    setTitle(title)
    return () => setTitle(undefined)
  }, [title, setTitle])
}

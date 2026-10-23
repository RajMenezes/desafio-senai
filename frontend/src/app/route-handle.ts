import { useMatches } from 'react-router'

/** Dados de cada rota usados no breadcrumb do cabeçalho. */
export interface RouteHandle {
  section: string
  title: string
}

export function useRouteHandle(): RouteHandle | undefined {
  const handles = useMatches()
    .map((match) => match.handle as RouteHandle | undefined)
    .filter((handle) => handle !== undefined)
  return handles.at(-1)
}

/** Parâmetro da URL de clubes que abre o diálogo de criação. */
export const CREATE_CLUB_PARAM = 'novo'

/** Caminhos das telas, para links e navegação. */
export const paths = {
  players: '/jogadores',
  freePlayers: '/jogadores?free=true',
  clubs: '/clubes',
  newClub: `/clubes?${CREATE_CLUB_PARAM}`,
  club: (id: number) => `/clubes/${id}`,
  matches: '/partidas',
  standings: '/classificacao',
}

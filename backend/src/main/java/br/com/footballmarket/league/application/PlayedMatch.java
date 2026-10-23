package br.com.footballmarket.league.application;

import br.com.footballmarket.league.domain.Match;

/**
 * Partida com os nomes dos clubes, que a resposta mostra. Clubes não são editados, então o nome atual
 * é o do momento da partida.
 */
public record PlayedMatch(Match match, String homeClubName, String awayClubName) {
}

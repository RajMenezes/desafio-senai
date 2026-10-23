package br.com.footballmarket.league.domain;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Position;

/**
 * Um lado do duelo com os dados do momento da partida e o valor usado, já com o efeito da condição.
 */
public record DuelSide(Integer playerId, String playerName, Position position, Condition condition,
                       DuelAttribute attribute, int value) {
}

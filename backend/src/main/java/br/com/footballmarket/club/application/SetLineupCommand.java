package br.com.footballmarket.club.application;

import br.com.footballmarket.club.domain.Formation;

import java.util.Set;

/**
 * @param playerIds exatamente 5 ids distintos (conferido na entrada)
 */
public record SetLineupCommand(Formation formation, Set<Integer> playerIds) {

    public SetLineupCommand {
        playerIds = Set.copyOf(playerIds);
    }
}

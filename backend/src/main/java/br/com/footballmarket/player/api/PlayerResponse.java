package br.com.footballmarket.player.api;

import br.com.footballmarket.player.domain.Attributes;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.money.Money;

/**
 * Jogador como aparece nas respostas (2.1.2). {@code market_value} e {@code overall} são calculados na hora.
 */
public record PlayerResponse(
        Integer id,
        String name,
        Position position,
        int agility,
        int age,
        Condition condition,
        Money baseValue,
        Money marketValue,
        int pace,
        int shooting,
        int passing,
        int dribbling,
        int defending,
        int physical,
        int potential,
        int overall,
        Integer clubId) {

    public static PlayerResponse from(Player player) {
        Attributes attributes = player.attributes();
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getPosition(),
                player.getAgility(),
                player.getAge(),
                player.getCondition(),
                player.getBaseValue(),
                player.marketValue(),
                attributes.pace(),
                attributes.shooting(),
                attributes.passing(),
                attributes.dribbling(),
                attributes.defending(),
                attributes.physical(),
                player.getPotential(),
                player.overall(),
                player.getClubId());
    }
}

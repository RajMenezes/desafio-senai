package br.com.footballmarket.club.domain;

import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.shared.money.Money;

import java.util.List;

/**
 * Clube com o elenco e os valores calculados na hora da consulta (RN11): nada disso é gravado.
 *
 * @param squadValue soma dos valores de mercado do elenco, cada um já arredondado (RN05)
 */
public record ValuedClub(Club club, List<Player> squad, Money squadValue) {

    public ValuedClub {
        squad = List.copyOf(squad);
    }

    public static ValuedClub of(Club club, List<Player> squad) {
        Money squadValue = squad.stream().map(Player::marketValue).reduce(Money.ZERO, Money::plus);
        return new ValuedClub(club, squad, squadValue);
    }

    /**
     * Patrimônio = caixa + valor do elenco.
     */
    public Money netWorth() {
        return club.getCash().plus(squadValue);
    }
}

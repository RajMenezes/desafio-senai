package br.com.footballmarket.club.api;

import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.ValuedClub;
import br.com.footballmarket.shared.money.Money;

/**
 * Campos mínimos de cada clube (2.2), sem o elenco. {@code squad_value} e {@code net_worth} são calculados na hora.
 */
public record ClubResponse(
        Integer id,
        String name,
        String acronym,
        Money cash,
        Money squadValue,
        Money netWorth) {

    public static ClubResponse from(ValuedClub valued) {
        Club club = valued.club();
        return new ClubResponse(club.getId(), club.getName(), club.getAcronym(), club.getCash(),
                valued.squadValue(), valued.netWorth());
    }
}

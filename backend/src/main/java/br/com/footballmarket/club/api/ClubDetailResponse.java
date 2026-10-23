package br.com.footballmarket.club.api;

import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.ValuedClub;
import br.com.footballmarket.player.api.PlayerResponse;
import br.com.footballmarket.shared.money.Money;

import java.util.List;

/**
 * {@code GET /clubs/{id}} (2.2.2): os campos de cada clube mais o elenco na ordem da RN11.
 * Etapa 5: cada jogador passa a trazer também o {@code contract}.
 */
public record ClubDetailResponse(
        Integer id,
        String name,
        String acronym,
        Money cash,
        Money squadValue,
        Money netWorth,
        List<PlayerResponse> players) {

    public static ClubDetailResponse from(ValuedClub valued) {
        Club club = valued.club();
        return new ClubDetailResponse(club.getId(), club.getName(), club.getAcronym(), club.getCash(),
                valued.squadValue(), valued.netWorth(),
                valued.squad().stream().map(PlayerResponse::from).toList());
    }
}

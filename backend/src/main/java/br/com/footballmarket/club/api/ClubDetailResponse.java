package br.com.footballmarket.club.api;

import br.com.footballmarket.club.application.ClubDetail;
import br.com.footballmarket.club.application.SquadContracts.SquadContract;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.ValuedClub;
import br.com.footballmarket.player.api.PlayerResponse;
import br.com.footballmarket.shared.money.Money;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * {@code GET /clubs/{id}} (2.2.2): os campos de cada clube mais o elenco na ordem da RN11,
 * cada jogador com os campos de {@code GET /players/{id}} e o {@code contract}.
 */
public record ClubDetailResponse(
        Integer id,
        String name,
        String acronym,
        Money cash,
        Money squadValue,
        Money netWorth,
        List<SquadPlayerResponse> players) {

    public record SquadPlayerResponse(@JsonUnwrapped PlayerResponse player, ContractResponse contract) {
    }

    public record ContractResponse(Money purchaseValue, OffsetDateTime loyaltyUntil,
                                   long remainingDays, int penaltyRate) {

        static ContractResponse from(SquadContract contract) {
            return contract == null ? null : new ContractResponse(contract.purchaseValue(), contract.loyaltyUntil(),
                    contract.remainingDays(), contract.penaltyRate());
        }
    }

    public static ClubDetailResponse from(ClubDetail detail) {
        ValuedClub valued = detail.club();
        Club club = valued.club();
        List<SquadPlayerResponse> players = valued.squad().stream()
                .map(player -> new SquadPlayerResponse(PlayerResponse.from(player),
                        ContractResponse.from(detail.contractsByPlayer().get(player.getId()))))
                .toList();
        return new ClubDetailResponse(club.getId(), club.getName(), club.getAcronym(), club.getCash(),
                valued.squadValue(), valued.netWorth(), players);
    }
}

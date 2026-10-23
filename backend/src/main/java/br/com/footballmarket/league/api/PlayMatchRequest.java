package br.com.footballmarket.league.api;

import br.com.footballmarket.shared.error.BadRequestException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo do POST /matches.
 */
public record PlayMatchRequest(
        @NotNull @Min(1) Integer homeClubId,
        @NotNull @Min(1) Integer awayClubId) {

    /**
     * Mandante igual ao visitante é formato inválido (400), conferido antes de buscar os clubes.
     */
    void requireDifferentClubs() {
        if (homeClubId.equals(awayClubId)) {
            throw new BadRequestException("O mandante e o visitante devem ser clubes diferentes.");
        }
    }
}

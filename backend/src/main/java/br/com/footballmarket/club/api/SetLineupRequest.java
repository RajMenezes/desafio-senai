package br.com.footballmarket.club.api;

import br.com.footballmarket.club.application.SetLineupCommand;
import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.shared.error.BadRequestException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Corpo do PUT /clubs/{id}/lineup. Tudo o que é formato vira 400 aqui, antes de buscar clube e jogadores.
 */
public record SetLineupRequest(
        @NotNull String formation,
        @NotNull @Size(min = Formation.STARTERS, max = Formation.STARTERS, message = "deve ter exatamente 5 ids")
        List<@NotNull @Min(1) Integer> playerIds) {

    SetLineupCommand toCommand() {
        Formation chosen = Formation.fromLabel(formation).orElseThrow(() -> new BadRequestException(
                "O campo 'formation' deve ser 3-1, 2-2, 2-1-1 ou 1-2-1."));
        Set<Integer> distinct = new HashSet<>(playerIds);
        if (distinct.size() != playerIds.size()) {
            throw new BadRequestException("O campo 'player_ids' não pode ter ids repetidos.");
        }
        return new SetLineupCommand(chosen, distinct);
    }
}

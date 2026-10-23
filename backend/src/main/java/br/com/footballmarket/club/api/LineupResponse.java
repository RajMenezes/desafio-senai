package br.com.footballmarket.club.api;

import br.com.footballmarket.club.application.Lineup;
import br.com.footballmarket.player.api.PlayerResponse;

import java.util.List;

/**
 * Escalação (2.4.1): sem escalação, {@code formation} nula, {@code players} vazio e {@code complete} falso.
 */
public record LineupResponse(Integer clubId, String formation, boolean complete, List<PlayerResponse> players) {

    public static LineupResponse from(Lineup lineup) {
        return new LineupResponse(
                lineup.clubId(),
                lineup.formation() == null ? null : lineup.formation().label(),
                lineup.complete(),
                lineup.starters().stream().map(PlayerResponse::from).toList());
    }
}

package br.com.footballmarket.club.application;

import br.com.footballmarket.club.infra.LineupRepository;
import br.com.footballmarket.player.application.StarterRemoval;
import org.springframework.stereotype.Component;

/**
 * Atende o módulo de jogadores: titular que passa a DOENTE sai da escalação (RN19).
 * Usa o repositório direto (e não o {@link LineupService}), porque o serviço depende do de jogadores.
 */
@Component
class StarterRemovalAdapter implements StarterRemoval {

    private final LineupRepository lineups;

    StarterRemovalAdapter(LineupRepository lineups) {
        this.lineups = lineups;
    }

    @Override
    public void removeStarter(Integer playerId) {
        lineups.deleteByPlayer(playerId);
    }
}

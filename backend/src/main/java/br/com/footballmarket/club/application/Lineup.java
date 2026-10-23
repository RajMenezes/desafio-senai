package br.com.footballmarket.club.application;

import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.player.domain.Player;

import java.util.List;

/**
 * Escalação atual do clube (RN19): a formação salva e os titulares que estão nela, na ordem do elenco (RN11).
 *
 * @param formation {@code null} enquanto o clube não escalou
 */
public record Lineup(Integer clubId, Formation formation, List<Player> starters) {

    public Lineup {
        starters = List.copyOf(starters);
    }

    /**
     * Completa com os 5 titulares; um titular vendido ou DOENTE deixa a escalação incompleta.
     */
    public boolean complete() {
        return formation != null && starters.size() == Formation.STARTERS;
    }
}

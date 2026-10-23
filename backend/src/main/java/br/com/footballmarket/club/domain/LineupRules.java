package br.com.footballmarket.club.domain;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.shared.error.BusinessRuleException;

import java.util.List;
import java.util.Objects;

/**
 * Regras de negócio da escalação (RN19, item 4), todas 409. O formato (5 ids distintos, formação da lista)
 * é conferido antes, na entrada.
 */
public final class LineupRules {

    private LineupRules() {
    }

    public static void requireValid(Club club, Formation formation, List<Player> starters) {
        for (Player starter : starters) {
            if (!Objects.equals(starter.getClubId(), club.getId())) {
                throw new BusinessRuleException("O jogador " + starter.getId() + " não é do elenco do clube.");
            }
        }
        for (Player starter : starters) {
            if (starter.getCondition() == Condition.DOENTE) {
                throw new BusinessRuleException("O jogador " + starter.getId() + " está DOENTE e não pode ser escalado.");
            }
        }
        if (!formation.fits(starters.stream().map(Player::getPosition).toList())) {
            throw new BusinessRuleException("As posições não fecham a formação " + formation.label()
                    + ": são necessários 1 GOL e os jogadores de linha de cada setor.");
        }
    }
}

package br.com.footballmarket.club.domain;

import br.com.footballmarket.player.domain.Position;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Formações aceitas (RN19): os 4 jogadores de linha por setor, de trás para frente, sem o goleiro.
 * Defesa é ZAG ou LAT, meio é MEI e ataque é ATA.
 */
public enum Formation {

    THREE_ONE("3-1", 3, 0, 1),
    TWO_TWO("2-2", 2, 0, 2),
    TWO_ONE_ONE("2-1-1", 2, 1, 1),
    ONE_TWO_ONE("1-2-1", 1, 2, 1);

    public static final int STARTERS = 5;

    private final String label;
    private final int defenders;
    private final int midfielders;
    private final int attackers;

    Formation(String label, int defenders, int midfielders, int attackers) {
        this.label = label;
        this.defenders = defenders;
        this.midfielders = midfielders;
        this.attackers = attackers;
    }

    /**
     * Valor exato da API e do banco ({@code "2-1-1"}).
     */
    public static Optional<Formation> fromLabel(String label) {
        return Arrays.stream(values()).filter(formation -> formation.label.equals(label)).findFirst();
    }

    public String label() {
        return label;
    }

    /**
     * As posições fecham a formação: exatamente 1 GOL e, em cada setor, a quantidade da tabela.
     */
    public boolean fits(Collection<Position> positions) {
        return positions.size() == STARTERS
                && count(positions, Position.GOL) == 1
                && count(positions, Position.ZAG) + count(positions, Position.LAT) == defenders
                && count(positions, Position.MEI) == midfielders
                && count(positions, Position.ATA) == attackers;
    }

    private static long count(Collection<Position> positions, Position position) {
        return positions.stream().filter(candidate -> candidate == position).count();
    }
}

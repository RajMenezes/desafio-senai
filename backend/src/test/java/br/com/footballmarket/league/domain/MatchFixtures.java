package br.com.footballmarket.league.domain;

import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.league.domain.MatchEngine.TeamSheet;
import br.com.footballmarket.player.domain.Attributes;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerSort;
import br.com.footballmarket.player.domain.Position;

import java.util.List;

import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static br.com.footballmarket.player.domain.PlayerFixtures.withId;

/**
 * Os titulares do exemplo da RN20: Águias FC (id 1) × Delta FC (id 2), os dois no 2-1-1.
 * Só os atributos que entram nos duelos importam; os outros são neutros.
 */
public final class MatchFixtures {

    private MatchFixtures() {
    }

    /**
     * Jogador com os atributos do duelo; os demais valem 50.
     */
    public static Player starter(int id, int clubId, String name, Position position,
                                 int shooting, int passing, int dribbling, int defending) {
        Attributes attributes = new Attributes(50, shooting, passing, dribbling, defending, 50);
        Player player = Player.register(name, position, 70, 25, attributes, 99, NOW);
        return withId(inClub(player, clubId), id);
    }

    public static List<Player> aguiasStarters() {
        return List.of(
                starter(30, 1, "Rui Prado", Position.GOL, 50, 50, 50, 77),
                withCondition(starter(31, 1, "Davi Lins", Position.LAT, 50, 50, 74, 66), Condition.MAL_CONDICIONADO),
                starter(32, 1, "Téo Brandão", Position.ZAG, 50, 50, 50, 84),
                starter(33, 1, "Lauro Neves", Position.MEI, 50, 82, 50, 50),
                starter(34, 1, "Otto Rangel", Position.ATA, 75, 50, 50, 50));
    }

    public static List<Player> deltaStarters() {
        return List.of(
                starter(40, 2, "Ivo Teles", Position.GOL, 50, 50, 50, 72),
                starter(41, 2, "Hugo Vaz", Position.ZAG, 50, 50, 50, 84),
                starter(42, 2, "Lino Matos", Position.LAT, 50, 50, 79, 63),
                starter(43, 2, "Nei Soares", Position.MEI, 50, 70, 50, 50),
                starter(44, 2, "Ugo Falcão", Position.ATA, 81, 50, 50, 50));
    }

    /**
     * Titulares na ordem do elenco (RN11), como o {@code LineupService} os entrega.
     */
    public static TeamSheet sheet(int clubId, Formation formation, List<Player> starters) {
        return new TeamSheet(clubId, formation,
                starters.stream().sorted(PlayerSort.squadOrdering()).toList());
    }

    public static TeamSheet aguias() {
        return sheet(1, Formation.TWO_ONE_ONE, aguiasStarters());
    }

    public static TeamSheet delta() {
        return sheet(2, Formation.TWO_ONE_ONE, deltaStarters());
    }
}

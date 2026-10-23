package br.com.footballmarket.league.domain;

import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Joga a partida (RN20). Sem sorteio: os mesmos titulares nas mesmas condições dão sempre o mesmo resultado.
 */
public final class MatchEngine {

    static final BigDecimal UNFIT_FACTOR = new BigDecimal("0.85");

    private MatchEngine() {
    }

    /**
     * Os 5 titulares de um clube na ordem do elenco (RN11): o goleiro e os jogadores de linha do 1º
     * (o mais recuado) ao 4º (o mais avançado).
     */
    public record TeamSheet(Integer clubId, Formation formation, List<Player> starters) {

        public TeamSheet {
            starters = List.copyOf(starters);
            if (starters.size() != Formation.STARTERS || starters.getFirst().getPosition() != Position.GOL) {
                throw new IllegalArgumentException("Escalação incompleta ou fora da ordem do elenco: " + starters);
            }
        }

        Player goalkeeper() {
            return starters.getFirst();
        }

        /**
         * @param number de 1 (o mais recuado) a 4 (o mais avançado)
         */
        Player outfield(int number) {
            return starters.get(number);
        }
    }

    public record MatchOutcome(int homeGoals, int awayGoals, List<DuelOutcome> duels) {

        public MatchOutcome {
            duels = List.copyOf(duels);
        }
    }

    /**
     * Duelos 1 a 3: ataques do mandante; 4 a 6: do visitante. Em cada lado, o 4º enfrenta o goleiro,
     * o 3º enfrenta o 1º e o 2º enfrenta o 2º do adversário.
     */
    public static MatchOutcome play(TeamSheet home, TeamSheet away) {
        List<DuelOutcome> duels = new ArrayList<>();
        attack(home, away, 1, duels);
        attack(away, home, 4, duels);
        int homeGoals = (int) duels.subList(0, 3).stream().filter(DuelOutcome::goal).count();
        int awayGoals = (int) duels.subList(3, 6).stream().filter(DuelOutcome::goal).count();
        return new MatchOutcome(homeGoals, awayGoals, duels);
    }

    private static void attack(TeamSheet attacking, TeamSheet defending, int firstOrder, List<DuelOutcome> duels) {
        duels.add(duel(firstOrder, attacking, attacking.outfield(4), defending.goalkeeper()));
        duels.add(duel(firstOrder + 1, attacking, attacking.outfield(3), defending.outfield(1)));
        duels.add(duel(firstOrder + 2, attacking, attacking.outfield(2), defending.outfield(2)));
    }

    private static DuelOutcome duel(int order, TeamSheet attacking, Player attacker, Player defender) {
        return new DuelOutcome(order, attacking.clubId(),
                side(attacker, DuelAttribute.attackOf(attacker.getPosition())),
                side(defender, DuelAttribute.DEFENDING));
    }

    private static DuelSide side(Player player, DuelAttribute attribute) {
        int value = effectiveValue(attribute.read(player.attributes()), player.getCondition());
        return new DuelSide(player.getId(), player.getName(), player.getPosition(), player.getCondition(),
                attribute, value);
    }

    /**
     * MAL_CONDICIONADO joga com 15% a menos, arredondado para o inteiro mais próximo, com o meio para cima
     * (74 → 62,9 → 63; 50 → 42,5 → 43). APTO joga com o atributo do cadastro.
     */
    static int effectiveValue(int attribute, Condition condition) {
        return switch (condition) {
            case APTO -> attribute;
            case MAL_CONDICIONADO -> BigDecimal.valueOf(attribute).multiply(UNFIT_FACTOR)
                    .setScale(0, RoundingMode.HALF_UP).intValueExact();
            case DOENTE -> throw new IllegalArgumentException("Jogador DOENTE não joga");
        };
    }
}

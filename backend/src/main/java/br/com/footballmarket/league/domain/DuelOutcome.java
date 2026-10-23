package br.com.footballmarket.league.domain;

/**
 * @param order de 1 a 6: os 3 ataques do mandante e depois os 3 do visitante
 */
public record DuelOutcome(int order, Integer attackingClubId, DuelSide attacker, DuelSide defender) {

    /**
     * Gol quando o ataque é maior que a defesa; duelo empatado não é gol.
     */
    public boolean goal() {
        return attacker.value() > defender.value();
    }
}

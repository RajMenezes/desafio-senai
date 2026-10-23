package br.com.footballmarket.league.domain;

/**
 * Quem marcou mais gols; gols iguais dão empate (RN20, passo 6).
 */
public enum MatchResult {
    MANDANTE,
    VISITANTE,
    EMPATE;

    public static MatchResult of(int homeGoals, int awayGoals) {
        if (homeGoals > awayGoals) {
            return MANDANTE;
        }
        return homeGoals < awayGoals ? VISITANTE : EMPATE;
    }
}

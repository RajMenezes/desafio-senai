package br.com.footballmarket.league.domain;

import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.club.domain.FormationConverter;
import br.com.footballmarket.league.domain.MatchEngine.MatchOutcome;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Partida jogada (RN20): placar, formações e os 6 duelos do momento. Nada é recalculado depois.
 * Os clubes são referenciados só pelo id.
 */
@Entity
@Table(name = "partidas")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mandante_id", nullable = false, updatable = false)
    private Integer homeClubId;

    @Column(name = "visitante_id", nullable = false, updatable = false)
    private Integer awayClubId;

    @Column(name = "gols_mandante", nullable = false, updatable = false)
    private int homeGoals;

    @Column(name = "gols_visitante", nullable = false, updatable = false)
    private int awayGoals;

    @Convert(converter = FormationConverter.class)
    @Column(name = "formacao_mandante", nullable = false, updatable = false, length = 5)
    private Formation homeFormation;

    @Convert(converter = FormationConverter.class)
    @Column(name = "formacao_visitante", nullable = false, updatable = false, length = 5)
    private Formation awayFormation;

    @Column(name = "jogada_em", nullable = false, updatable = false)
    private OffsetDateTime playedAt;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "partida_id", nullable = false, updatable = false)
    @OrderBy("sequence")
    private List<MatchDuel> duels = new ArrayList<>();

    protected Match() {
        // JPA
    }

    public static Match record(MatchEngine.TeamSheet home, MatchEngine.TeamSheet away, MatchOutcome outcome,
                               OffsetDateTime now) {
        Match match = new Match();
        match.homeClubId = home.clubId();
        match.awayClubId = away.clubId();
        match.homeFormation = home.formation();
        match.awayFormation = away.formation();
        match.homeGoals = outcome.homeGoals();
        match.awayGoals = outcome.awayGoals();
        match.playedAt = now;
        outcome.duels().stream().map(MatchDuel::of).forEach(match.duels::add);
        return match;
    }

    public MatchResult result() {
        return MatchResult.of(homeGoals, awayGoals);
    }

    public Integer getId() {
        return id;
    }

    public Integer getHomeClubId() {
        return homeClubId;
    }

    public Integer getAwayClubId() {
        return awayClubId;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public Formation getHomeFormation() {
        return homeFormation;
    }

    public Formation getAwayFormation() {
        return awayFormation;
    }

    public OffsetDateTime getPlayedAt() {
        return playedAt;
    }

    public List<MatchDuel> getDuels() {
        return List.copyOf(duels);
    }
}

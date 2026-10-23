package br.com.footballmarket.league.infra;

import br.com.footballmarket.league.domain.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Integer> {

    /**
     * Com os duelos já carregados, porque a resposta é montada fora da transação.
     */
    @EntityGraph(attributePaths = "duels")
    Optional<Match> findWithDuelsById(Integer id);

    /**
     * Histórico (RN21): mais recente primeiro; no mesmo instante, maior id primeiro.
     */
    @Query("select m from Match m order by m.playedAt desc, m.id desc")
    List<Match> findHistory();

    @Query("""
            select m from Match m
            where m.homeClubId = :clubId or m.awayClubId = :clubId
            order by m.playedAt desc, m.id desc""")
    List<Match> findHistoryOfClub(@Param("clubId") Integer clubId);
}

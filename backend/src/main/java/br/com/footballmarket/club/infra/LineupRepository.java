package br.com.footballmarket.club.infra;

import br.com.footballmarket.club.domain.LineupEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LineupRepository extends JpaRepository<LineupEntry, LineupEntry.Key> {

    List<LineupEntry> findByClubId(Integer clubId);

    /**
     * Exclusão em lote, executada na hora: os titulares antigos saem do banco antes de os novos
     * serem inseridos (o Hibernate, sozinho, faria os INSERTs antes dos DELETEs e esbarraria na
     * unicidade do jogador).
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from LineupEntry e where e.clubId = :clubId")
    void deleteAllOfClub(@Param("clubId") Integer clubId);

    @Modifying(flushAutomatically = true)
    @Query("delete from LineupEntry e where e.playerId = :playerId")
    void deleteByPlayer(@Param("playerId") Integer playerId);
}

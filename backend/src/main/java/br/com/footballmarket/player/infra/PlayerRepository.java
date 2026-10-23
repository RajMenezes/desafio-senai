package br.com.footballmarket.player.infra;

import br.com.footballmarket.player.domain.Player;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer>, JpaSpecificationExecutor<Player> {

    /**
     * Jogador excluído logicamente conta como inexistente em todos os endpoints (RN09).
     */
    Optional<Player> findByIdAndDeletedAtIsNull(Integer id);

    /**
     * Mesmo filtro, com {@code SELECT ... FOR UPDATE}: operações que mudam o jogador esperam umas pelas outras
     * (ex.: duas compras simultâneas do mesmo jogador, ou compra e exclusão).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Player p where p.id = :id and p.deletedAt is null")
    Optional<Player> findActiveForUpdate(@Param("id") Integer id);

    long countByClubId(Integer clubId);

    /**
     * Jogador com clube nunca está excluído (RN09), então basta o clube.
     */
    List<Player> findByClubId(Integer clubId);

    List<Player> findByClubIdIsNotNull();
}

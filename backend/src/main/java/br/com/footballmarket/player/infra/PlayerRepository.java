package br.com.footballmarket.player.infra;

import br.com.footballmarket.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer>, JpaSpecificationExecutor<Player> {

    /**
     * Jogador excluído logicamente conta como inexistente em todos os endpoints (RN09).
     */
    Optional<Player> findByIdAndDeletedAtIsNull(Integer id);
}

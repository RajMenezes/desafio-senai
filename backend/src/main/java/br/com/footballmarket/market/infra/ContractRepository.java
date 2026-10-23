package br.com.footballmarket.market.infra;

import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    List<Contract> findByClubIdAndStatus(Integer clubId, ContractStatus status);

    /**
     * No máximo um contrato ATIVO por jogador (índice único no banco).
     */
    Optional<Contract> findByPlayerIdAndStatus(Integer playerId, ContractStatus status);
}

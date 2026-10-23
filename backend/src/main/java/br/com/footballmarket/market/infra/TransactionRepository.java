package br.com.footballmarket.market.infra;

import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<MarketTransaction, Integer> {

    boolean existsByClubIdAndPlayerIdAndType(Integer clubId, Integer playerId, TransactionType type);
}

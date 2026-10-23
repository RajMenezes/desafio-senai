package br.com.footballmarket.market.infra;

import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    List<Contract> findByClubIdAndStatus(Integer clubId, ContractStatus status);
}

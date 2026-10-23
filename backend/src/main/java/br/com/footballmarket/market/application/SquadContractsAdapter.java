package br.com.footballmarket.market.application;

import br.com.footballmarket.club.application.SquadContracts;
import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.ContractStatus;
import br.com.footballmarket.market.infra.ContractRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entrega ao módulo de clubes os contratos ativos do elenco, com dias restantes e multa do momento.
 */
@Component
class SquadContractsAdapter implements SquadContracts {

    private final ContractRepository contracts;
    private final Clock clock;

    SquadContractsAdapter(ContractRepository contracts, Clock clock) {
        this.contracts = contracts;
        this.clock = clock;
    }

    @Override
    public Map<Integer, SquadContract> activeByPlayer(Integer clubId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        return contracts.findByClubIdAndStatus(clubId, ContractStatus.ATIVO).stream()
                .collect(Collectors.toMap(Contract::getPlayerId, contract -> new SquadContract(
                        contract.getPurchaseValue(), contract.getLoyaltyUntil(),
                        contract.remainingDays(now), contract.penaltyRate(now))));
    }
}

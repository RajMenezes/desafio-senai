package br.com.footballmarket.market.application;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.ContractStatus;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.SalePricing;
import br.com.footballmarket.market.infra.ContractRepository;
import br.com.footballmarket.market.infra.TransactionRepository;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.shared.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Venda de jogador pelo clube atual (RN15 e RN16), numa única transação de banco:
 * caixa, elenco, contrato e transação são gravados juntos ou nada é gravado.
 */
@Service
@Transactional
public class SaleService {

    private final PlayerService players;
    private final ClubService clubs;
    private final ContractRepository contracts;
    private final TransactionRepository transactions;
    private final Clock clock;

    public SaleService(PlayerService players, ClubService clubs, ContractRepository contracts,
                       TransactionRepository transactions, Clock clock) {
        this.players = players;
        this.clubs = clubs;
        this.contracts = contracts;
        this.transactions = transactions;
        this.clock = clock;
    }

    /**
     * Trava o jogador e depois o clube dele, a mesma ordem da compra.
     */
    public SaleResult sell(Integer playerId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Player player = players.findForUpdate(playerId);
        player.requireInClub();
        Club club = clubs.findForUpdate(player.getClubId());
        Contract contract = contracts.findByPlayerIdAndStatus(player.getId(), ContractStatus.ATIVO)
                .orElseThrow(() -> new IllegalStateException("Jogador " + playerId + " está no clube sem contrato ativo."));

        Money value = player.marketValue();
        long remainingDays = contract.remainingDays(now);
        int penaltyRate = contract.penaltyRate(now);
        Money penalty = SalePricing.penalty(value, penaltyRate);
        Money profit = value.minus(contract.getPurchaseValue());
        Money commission = SalePricing.commission(profit);
        MarketTransaction transaction = MarketTransaction.sale(club.getId(), player.getId(), player.getName(),
                value, commission, penalty, profit, now);

        club.credit(transaction.getCashChange());
        player.leaveClub(now);
        contract.close(penalty, now);
        // Etapa 8: titular vendido sai da escalação (RN19)
        return new SaleResult(transactions.save(transaction), contract, remainingDays, penaltyRate, club.getCash());
    }
}

package br.com.footballmarket.market.application;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.Loyalty;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.PurchasePricing;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.market.infra.ContractRepository;
import br.com.footballmarket.market.infra.TransactionRepository;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Compra de jogador (RN12 a RN14), numa única transação de banco: caixa, elenco, contrato e transação
 * são gravados juntos ou nada é gravado.
 */
@Service
@Transactional
public class PurchaseService {

    static final int MAX_SQUAD_SIZE = 12;

    private final PlayerService players;
    private final ClubService clubs;
    private final ContractRepository contracts;
    private final TransactionRepository transactions;
    private final Clock clock;

    public PurchaseService(PlayerService players, ClubService clubs, ContractRepository contracts,
                           TransactionRepository transactions, Clock clock) {
        this.players = players;
        this.clubs = clubs;
        this.contracts = contracts;
        this.transactions = transactions;
        this.clock = clock;
    }

    /**
     * Ordem das conferências: fim da fidelidade (400), jogador e clube (404) e as regras de negócio (409).
     * O jogador é travado antes do clube, a mesma ordem da venda, para que as operações não se bloqueiem
     * mutuamente.
     */
    public PurchaseResult buy(BuyPlayerCommand command) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Loyalty.requireValidEnd(command.loyaltyUntil(), now);
        Player player = players.findForUpdate(command.playerId());
        Club club = clubs.findForUpdate(command.clubId());

        player.requireFree();
        if (transactions.existsByClubIdAndPlayerIdAndType(club.getId(), player.getId(), TransactionType.VENDA)) {
            throw new BusinessRuleException("O clube não pode comprar um jogador que ele mesmo já vendeu.");
        }
        if (players.squadSize(club.getId()) >= MAX_SQUAD_SIZE) {
            throw new BusinessRuleException("O elenco do clube já tem o máximo de " + MAX_SQUAD_SIZE + " jogadores.");
        }
        Money value = player.marketValue();
        Money fee = PurchasePricing.fee(value);
        Money totalCost = value.plus(fee);
        PurchasePricing.requireAffordable(totalCost, club.getCash());

        club.debit(totalCost);
        player.joinClub(club.getId(), now);
        Contract contract = contracts.save(
                Contract.start(player.getId(), club.getId(), value, command.loyaltyUntil(), now));
        MarketTransaction transaction = transactions.save(
                MarketTransaction.purchase(club.getId(), player.getId(), player.getName(), value, fee, now));
        return new PurchaseResult(transaction, contract, club.getCash());
    }
}

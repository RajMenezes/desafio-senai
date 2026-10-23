package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Transação de compra ou venda (RN17). Guarda os valores e o nome do momento e nunca muda depois:
 * todas as colunas são {@code updatable = false}.
 * <p>
 * Chamada de {@code MarketTransaction} para não confundir com as transações de banco ({@code @Transactional}).
 */
@Entity
@Table(name = "transacoes")
public class MarketTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clube_id", nullable = false, updatable = false)
    private Integer clubId;

    @Column(name = "jogador_id", nullable = false, updatable = false)
    private Integer playerId;

    @Column(name = "nome_jogador", nullable = false, updatable = false, length = 100)
    private String playerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, updatable = false, length = 6)
    private TransactionType type;

    @Column(name = "valor", nullable = false, updatable = false, precision = 6, scale = 2)
    private Money value;

    @Column(name = "taxa", nullable = false, updatable = false, precision = 6, scale = 2)
    private Money fee;

    @Column(name = "multa", nullable = false, updatable = false, precision = 6, scale = 2)
    private Money penalty;

    @Column(name = "lucro", updatable = false, precision = 7, scale = 2)
    private Money profit;

    @Column(name = "variacao_caixa", nullable = false, updatable = false, precision = 8, scale = 2)
    private Money cashChange;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected MarketTransaction() {
        // JPA
    }

    /**
     * Compra: sem multa e sem lucro; o caixa varia em −(valor + taxa).
     */
    public static MarketTransaction purchase(Integer clubId, Integer playerId, String playerName,
                                             Money value, Money fee, OffsetDateTime now) {
        MarketTransaction transaction = new MarketTransaction();
        transaction.clubId = clubId;
        transaction.playerId = playerId;
        transaction.playerName = playerName;
        transaction.type = TransactionType.COMPRA;
        transaction.value = value;
        transaction.fee = fee;
        transaction.penalty = Money.ZERO;
        transaction.profit = null;
        transaction.cashChange = value.plus(fee).negate();
        transaction.createdAt = now;
        return transaction;
    }

    /**
     * Venda: {@code fee} é a comissão; o caixa varia em valor − multa − comissão.
     */
    public static MarketTransaction sale(Integer clubId, Integer playerId, String playerName, Money value,
                                         Money commission, Money penalty, Money profit, OffsetDateTime now) {
        MarketTransaction transaction = new MarketTransaction();
        transaction.clubId = clubId;
        transaction.playerId = playerId;
        transaction.playerName = playerName;
        transaction.type = TransactionType.VENDA;
        transaction.value = value;
        transaction.fee = commission;
        transaction.penalty = penalty;
        transaction.profit = profit;
        transaction.cashChange = value.minus(penalty).minus(commission);
        transaction.createdAt = now;
        return transaction;
    }

    public Integer getId() {
        return id;
    }

    public Integer getClubId() {
        return clubId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public TransactionType getType() {
        return type;
    }

    public Money getValue() {
        return value;
    }

    public Money getFee() {
        return fee;
    }

    public Money getPenalty() {
        return penalty;
    }

    public Money getProfit() {
        return profit;
    }

    public Money getCashChange() {
        return cashChange;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

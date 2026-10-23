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
 * Contrato entre clube e jogador (RN12). Jogador e clube são referenciados só pelo id.
 */
@Entity
@Table(name = "contratos")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "jogador_id", nullable = false, updatable = false)
    private Integer playerId;

    @Column(name = "clube_id", nullable = false, updatable = false)
    private Integer clubId;

    @Column(name = "valor_compra", nullable = false, updatable = false, precision = 6, scale = 2)
    private Money purchaseValue;

    @Column(name = "fidelidade_ate", nullable = false, updatable = false)
    private OffsetDateTime loyaltyUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 12)
    private ContractStatus status;

    @Column(name = "iniciado_em", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "encerrado_em")
    private OffsetDateTime endedAt;

    protected Contract() {
        // JPA
    }

    /**
     * @param purchaseValue valor de mercado pago, sem a taxa (RN16)
     */
    public static Contract start(Integer playerId, Integer clubId, Money purchaseValue,
                                 OffsetDateTime loyaltyUntil, OffsetDateTime now) {
        Contract contract = new Contract();
        contract.playerId = playerId;
        contract.clubId = clubId;
        contract.purchaseValue = purchaseValue;
        contract.loyaltyUntil = loyaltyUntil;
        contract.status = ContractStatus.ATIVO;
        contract.startedAt = now;
        return contract;
    }

    public long remainingDays(OffsetDateTime now) {
        return Loyalty.remainingDays(loyaltyUntil, now);
    }

    public int penaltyRate(OffsetDateTime now) {
        return Loyalty.penaltyRate(remainingDays(now));
    }

    public Integer getId() {
        return id;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public Integer getClubId() {
        return clubId;
    }

    public Money getPurchaseValue() {
        return purchaseValue;
    }

    public OffsetDateTime getLoyaltyUntil() {
        return loyaltyUntil;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }
}

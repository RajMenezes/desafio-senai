package br.com.footballmarket.league.domain;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Um dos 6 duelos gravados da partida, com os dados do momento (RN20, passo 7). Nunca muda depois.
 */
@Entity
@Table(name = "duelos_partida")
public class MatchDuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ordem", nullable = false, updatable = false)
    private int sequence;

    @Column(name = "clube_atacante_id", nullable = false, updatable = false)
    private Integer attackingClubId;

    @Column(name = "atacante_id", nullable = false, updatable = false)
    private Integer attackerId;

    @Column(name = "defensor_id", nullable = false, updatable = false)
    private Integer defenderId;

    @Column(name = "nome_atacante", nullable = false, updatable = false, length = 100)
    private String attackerName;

    @Column(name = "nome_defensor", nullable = false, updatable = false, length = 100)
    private String defenderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "posicao_atacante", nullable = false, updatable = false, length = 3)
    private Position attackerPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "posicao_defensor", nullable = false, updatable = false, length = 3)
    private Position defenderPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao_atacante", nullable = false, updatable = false, length = 20)
    private Condition attackerCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao_defensor", nullable = false, updatable = false, length = 20)
    private Condition defenderCondition;

    @Convert(converter = DuelAttributeConverter.class)
    @Column(name = "atributo_ataque", nullable = false, updatable = false, length = 10)
    private DuelAttribute attackAttribute;

    @Convert(converter = DuelAttributeConverter.class)
    @Column(name = "atributo_defesa", nullable = false, updatable = false, length = 10)
    private DuelAttribute defenseAttribute;

    @Column(name = "valor_ataque", nullable = false, updatable = false)
    private int attackValue;

    @Column(name = "valor_defesa", nullable = false, updatable = false)
    private int defenseValue;

    @Column(name = "gol", nullable = false, updatable = false)
    private boolean goal;

    protected MatchDuel() {
        // JPA
    }

    static MatchDuel of(DuelOutcome outcome) {
        MatchDuel duel = new MatchDuel();
        duel.sequence = outcome.order();
        duel.attackingClubId = outcome.attackingClubId();
        duel.attackerId = outcome.attacker().playerId();
        duel.attackerName = outcome.attacker().playerName();
        duel.attackerPosition = outcome.attacker().position();
        duel.attackerCondition = outcome.attacker().condition();
        duel.attackAttribute = outcome.attacker().attribute();
        duel.attackValue = outcome.attacker().value();
        duel.defenderId = outcome.defender().playerId();
        duel.defenderName = outcome.defender().playerName();
        duel.defenderPosition = outcome.defender().position();
        duel.defenderCondition = outcome.defender().condition();
        duel.defenseAttribute = outcome.defender().attribute();
        duel.defenseValue = outcome.defender().value();
        duel.goal = outcome.goal();
        return duel;
    }

    public int getOrder() {
        return sequence;
    }

    public Integer getAttackingClubId() {
        return attackingClubId;
    }

    public DuelSide attacker() {
        return new DuelSide(attackerId, attackerName, attackerPosition, attackerCondition, attackAttribute, attackValue);
    }

    public DuelSide defender() {
        return new DuelSide(defenderId, defenderName, defenderPosition, defenderCondition, defenseAttribute, defenseValue);
    }

    public boolean isGoal() {
        return goal;
    }
}

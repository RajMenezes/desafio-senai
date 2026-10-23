package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.error.BusinessRuleException;
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
 * Jogador. As regras de mudança de estado ficam aqui, e não no serviço, para que nenhum caminho
 * consiga deixar o jogador num estado inválido.
 * <p>
 * O clube é referenciado só pelo id: os módulos se comunicam por identificadores, não por associações JPA.
 */
@Entity
@Table(name = "jogadores")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "posicao", nullable = false, length = 3)
    private Position position;

    @Column(name = "agilidade", nullable = false)
    private int agility;

    @Column(name = "idade", nullable = false)
    private int age;

    @Column(name = "valor_base", nullable = false, precision = 6, scale = 2)
    private Money baseValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao", nullable = false, length = 20)
    private Condition condition;

    @Column(name = "clube_id")
    private Integer clubId;

    @Column(name = "velocidade", nullable = false)
    private int pace;

    @Column(name = "finalizacao", nullable = false)
    private int shooting;

    @Column(name = "passe", nullable = false)
    private int passing;

    @Column(name = "drible", nullable = false)
    private int dribbling;

    @Column(name = "defesa", nullable = false)
    private int defending;

    @Column(name = "fisico", nullable = false)
    private int physical;

    @Column(name = "potencial", nullable = false)
    private int potential;

    @Column(name = "excluido_em")
    private OffsetDateTime deletedAt;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected Player() {
        // JPA
    }

    /**
     * Cadastro: o jogador nasce livre e APTO, e o valor base é calculado uma única vez (RN06).
     */
    public static Player register(String name, Position position, int agility, int age,
                                  Attributes attributes, int potential, OffsetDateTime now) {
        PlayerRules.requireValidName(name);
        PlayerRules.requireAgilityAllowedForAge(agility, age);
        PlayerRules.requireValidAttributes(attributes, potential, age);

        Player player = new Player();
        player.name = name;
        player.position = position;
        player.agility = agility;
        player.age = age;
        player.pace = attributes.pace();
        player.shooting = attributes.shooting();
        player.passing = attributes.passing();
        player.dribbling = attributes.dribbling();
        player.defending = attributes.defending();
        player.physical = attributes.physical();
        player.potential = potential;
        player.condition = Condition.APTO;
        player.baseValue = BaseValueCalculator.calculate(OverallCalculator.calculate(position, attributes), potential, age);
        player.createdAt = now;
        player.updatedAt = now;
        return player;
    }

    /**
     * RN08: o PUT muda só nome, posição, agilidade e idade. Jogador com clube não muda de posição,
     * e a idade não pode diminuir (409). Indicadores, potencial e valor base não mudam.
     */
    public void updateProfile(String name, Position position, int agility, int age, OffsetDateTime now) {
        PlayerRules.requireValidName(name);
        PlayerRules.requireAgilityAllowedForAge(agility, age);
        if (hasClub() && position != this.position) {
            throw new BusinessRuleException("Jogador com clube não pode mudar de posição.");
        }
        if (age < this.age) {
            throw new BusinessRuleException(
                    "A idade não pode diminuir: a idade atual é " + this.age + " e foi enviado " + age + ".");
        }
        this.name = name;
        this.position = position;
        this.agility = agility;
        this.age = age;
        this.updatedAt = now;
    }

    /**
     * Enviar a condição atual é aceito, sem mudança. De DOENTE não se vai direto para APTO (409).
     */
    public void changeCondition(Condition target, OffsetDateTime now) {
        if (target == condition) {
            return;
        }
        if (!condition.canChangeTo(target)) {
            throw new BusinessRuleException("Jogador DOENTE não pode passar direto para APTO.");
        }
        this.condition = target;
        this.updatedAt = now;
    }

    /**
     * RN09: exclusão lógica, só de jogador livre.
     */
    public void delete(OffsetDateTime now) {
        if (hasClub()) {
            throw new BusinessRuleException("Só jogador livre pode ser excluído; este jogador está num clube.");
        }
        this.deletedAt = now;
        this.updatedAt = now;
    }

    /**
     * Compra (RN12): só jogador livre entra num elenco.
     */
    public void joinClub(Integer clubId, OffsetDateTime now) {
        requireFree();
        this.clubId = clubId;
        this.updatedAt = now;
    }

    /**
     * Venda (RN16): o jogador volta a ficar livre. Jogador livre não pode ser vendido (409).
     */
    public void leaveClub(OffsetDateTime now) {
        requireInClub();
        this.clubId = null;
        this.updatedAt = now;
    }

    public void requireInClub() {
        if (!hasClub()) {
            throw new BusinessRuleException("Jogador livre não pode ser vendido: ele não pertence a nenhum clube.");
        }
    }

    public void requireFree() {
        if (hasClub()) {
            throw new BusinessRuleException("O jogador não está livre: já pertence a um clube.");
        }
    }

    public boolean hasClub() {
        return clubId != null;
    }

    /**
     * Calculado a cada consulta, porque a posição pode mudar no PUT (RN06).
     */
    public int overall() {
        return OverallCalculator.calculate(position, attributes());
    }

    /**
     * Calculado a cada consulta pela condição, agilidade e idade atuais (RN05).
     */
    public Money marketValue() {
        return MarketValueCalculator.calculate(baseValue, condition, agility, age);
    }

    public Attributes attributes() {
        return new Attributes(pace, shooting, passing, dribbling, defending, physical);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public int getAgility() {
        return agility;
    }

    public int getAge() {
        return age;
    }

    public Money getBaseValue() {
        return baseValue;
    }

    public Condition getCondition() {
        return condition;
    }

    public Integer getClubId() {
        return clubId;
    }

    public int getPotential() {
        return potential;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

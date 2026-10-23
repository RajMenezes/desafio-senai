package br.com.footballmarket.club.domain;

import br.com.footballmarket.shared.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * Clube. O caixa só muda por compras e vendas (RN10); o elenco é consultado no módulo de jogadores pelo id.
 */
@Entity
@Table(name = "clubes")
public class Club {

    public static final Money INITIAL_CASH = Money.of("300.00");
    public static final int MAX_CLUBS = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false, length = 60)
    private String name;

    @Column(name = "nome_chave", nullable = false, length = 120)
    private String nameKey;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "sigla", nullable = false, length = 3)
    private String acronym;

    @Column(name = "caixa", nullable = false, precision = 10, scale = 2)
    private Money cash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Club() {
        // JPA
    }

    /**
     * RN10: o clube nasce com 300,00 em caixa e sem jogadores.
     *
     * @param name nome já normalizado pela V4
     */
    public static Club create(String name, String acronym, OffsetDateTime now) {
        ClubRules.requireValidName(name);
        ClubRules.requireValidAcronym(acronym, name);

        Club club = new Club();
        club.name = name;
        club.nameKey = nameKey(name);
        club.acronym = acronym;
        club.cash = INITIAL_CASH;
        club.createdAt = now;
        return club;
    }

    /**
     * Chave de unicidade do nome (RN10): ignora maiúsculas e minúsculas, mas não acentos.
     * "Águias FC" e "águias fc" têm a mesma chave; "Aguias FC" tem outra.
     */
    public static String nameKey(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Desconto do custo de uma compra. O limite de 50% (RN13) é conferido antes; o caixa nunca fica negativo.
     */
    public void debit(Money amount) {
        Money newCash = cash.minus(amount);
        if (newCash.isLessThan(Money.ZERO)) {
            throw new IllegalStateException("Caixa negativo: " + newCash);
        }
        this.cash = newCash;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAcronym() {
        return acronym;
    }

    public Money getCash() {
        return cash;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

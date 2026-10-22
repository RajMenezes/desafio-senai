package br.com.footballmarket.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Valor monetário com 2 casas decimais.
 * <p>
 * O arredondamento acontece uma única vez, ao criar o {@code Money} a partir do resultado final de um cálculo.
 * {@link RoundingMode#HALF_UP} arredonda o "5" para longe do zero, como pede o enunciado.
 * Cálculos intermediários devem ser feitos em {@link BigDecimal}, sem arredondar.
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final Money ZERO = Money.of(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money negate() {
        return new Money(amount.negate());
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public Money max(Money other) {
        return isLessThan(other) ? other : this;
    }

    public Money min(Money other) {
        return isGreaterThan(other) ? other : this;
    }

    /**
     * Formato brasileiro com vírgula decimal e sem separador de milhar (ex.: {@code 50,40}),
     * usado em mensagens e no CSV.
     */
    public String toBrazilianFormat() {
        return amount.toPlainString().replace('.', ',');
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}

package br.com.footballmarket.shared.money;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @ParameterizedTest
    @CsvSource({
            "10.005, 10.01",
            "10.004, 10.00",
            "-10.005, -10.01", // meio para longe do zero também nos negativos
            "-10.004, -10.00",
            "40, 40.00",
            "40.0, 40.00"
    })
    void roundsHalfAwayFromZeroToTwoDecimals(String raw, String expected) {
        assertThat(Money.of(raw).amount()).isEqualByComparingTo(expected);
        assertThat(Money.of(raw).amount().scale()).isEqualTo(2);
    }

    @Test
    void equalAmountsWithDifferentScalesAreEqual() {
        assertThat(Money.of("40")).isEqualTo(Money.of("40.00"));
        assertThat(Money.of(new BigDecimal("40.0"))).isEqualTo(Money.of("40"));
    }

    @Test
    void arithmeticAndComparisons() {
        Money cash = Money.of("300.00");
        Money cost = Money.of("50.40");

        assertThat(cash.minus(cost)).isEqualTo(Money.of("249.60"));
        assertThat(cash.plus(cost)).isEqualTo(Money.of("350.40"));
        assertThat(cost.negate()).isEqualTo(Money.of("-50.40"));
        assertThat(cost.isGreaterThan(Money.of("50.00"))).isTrue();
        assertThat(cost.isLessThan(Money.of("50.40"))).isFalse();
        assertThat(Money.of("0.50").max(Money.of("1.00"))).isEqualTo(Money.of("1.00"));
        assertThat(Money.of("12.00").min(Money.of("10.00"))).isEqualTo(Money.of("10.00"));
        assertThat(Money.ZERO.isPositive()).isFalse();
    }

    @Test
    void brazilianFormatUsesDecimalComma() {
        assertThat(Money.of("50.4").toBrazilianFormat()).isEqualTo("50,40");
        assertThat(Money.of("1234.5").toBrazilianFormat()).isEqualTo("1234,50");
        assertThat(Money.of("-3").toBrazilianFormat()).isEqualTo("-3,00");
    }
}

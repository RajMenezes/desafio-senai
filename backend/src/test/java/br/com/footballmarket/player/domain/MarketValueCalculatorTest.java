package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exemplos da tabela da RN05, mais os casos de fronteira das faixas de agilidade e idade.
 */
class MarketValueCalculatorTest {

    @ParameterizedTest(name = "{0} {1} agilidade {2} idade {3} → {4}")
    @CsvSource({
            "82.00, APTO, 20, 25, 82.00",              // agilidade não age em APTO
            "82.00, APTO, 50, 19, 90.20",
            "82.00, MAL_CONDICIONADO, 50, 25, 69.70",
            "82.00, MAL_CONDICIONADO, 80, 21, 84.05",  // 7,5% e +10%
            "82.00, MAL_CONDICIONADO, 30, 33, 53.30",  // 25% e −10%
            "82.00, DOENTE, 90, 25, 69.70",
            "82.00, DOENTE, 30, 25, 49.20",            // exatamente no teto de −40%
            "82.00, DOENTE, 10, 40, 49.20",            // −50% limitado a −40%
            "9.50, MAL_CONDICIONADO, 85, 25, 8.79",    // 8,7875 arredondado
            "1.00, MAL_CONDICIONADO, 50, 35, 1.00",    // 0,75 sobe para o piso
            "40.00, MAL_CONDICIONADO, 85, 25, 37.00"   // Meia Lua (2.1.4)
    })
    void calculatesReadmeExamples(String baseValue, Condition condition, int agility, int age, String expected) {
        assertThat(MarketValueCalculator.calculate(Money.of(baseValue), condition, agility, age))
                .isEqualTo(Money.of(expected));
    }

    @ParameterizedTest(name = "{0} agilidade {1} → desconto {2}%")
    @CsvSource({
            "MAL_CONDICIONADO, 100, 7.5",
            "MAL_CONDICIONADO, 80, 7.5",
            "MAL_CONDICIONADO, 79, 15",
            "MAL_CONDICIONADO, 31, 15",
            "MAL_CONDICIONADO, 30, 25",
            "MAL_CONDICIONADO, 1, 25",
            "DOENTE, 80, 15",
            "DOENTE, 30, 40",
            "APTO, 1, 0",
            "APTO, 100, 0"
    })
    void agilityChangesOnlyConditionDiscount(Condition condition, int agility, String expectedPercent) {
        assertThat(MarketValueCalculator.conditionDiscountPercent(condition, agility))
                .isEqualByComparingTo(expectedPercent);
    }

    @ParameterizedTest(name = "idade {0} → {1}%")
    @CsvSource({"16, 10", "21, 10", "22, 0", "31, 0", "32, -10", "45, -10"})
    void ageAdjustment(int age, String expectedPercent) {
        assertThat(MarketValueCalculator.ageAdjustmentPercent(age)).isEqualByComparingTo(expectedPercent);
    }
}

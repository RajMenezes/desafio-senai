package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exemplos da tabela "Exemplos de cálculo do valor base" (RN06).
 */
class BaseValueCalculatorTest {

    @ParameterizedTest(name = "overall {0}, potencial {1}, idade {2} → {3}")
    @CsvSource({
            "75, 86, 25, 40.00",
            "60, 70, 30, 10.00",  // fim da primeira faixa
            "61, 70, 30, 12.00",  // início da segunda faixa
            "76, 80, 22, 46.00",
            "85, 92, 27, 100.00",
            "70, 84, 21, 34.20",  // jovem promessa: 30,00 × 114 ÷ 100
            "70, 84, 22, 30.00",  // 22 anos já não é jovem promessa
            "45, 90, 18, 3.63",   // 3,625 → meio para longe do zero
            "40, 66, 36, 1.00",   // parte do overall 0 → piso
            "99, 99, 25, 184.00", // todas as faixas cheias: 10 + 30 + 144
            "41, 41, 16, 1.00"    // 0,50 × 1,00 = 0,50 → piso
    })
    void calculatesBaseValue(int overall, int potential, int age, String expected) {
        assertThat(BaseValueCalculator.calculate(overall, potential, age)).isEqualTo(Money.of(expected));
    }
}

package br.com.footballmarket.player.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exemplos da tabela "Exemplos de cálculo do overall" (RN06).
 */
class OverallCalculatorTest {

    @ParameterizedTest(name = "{0} {1}/{2}/{3}/{4}/{5}/{6} → {7}")
    @CsvSource({
            "MEI, 68, 65, 80, 78, 52, 60, 73", // 72,65
            "ATA, 78, 75, 73, 82, 48, 68, 76", // 76,10
            "LAT, 72, 70, 78, 76, 70, 66, 72", // 72,40
            "ZAG, 50, 90, 60, 40, 80, 75, 73", // 72,50 → meio para cima
            "GOL, 55, 30, 65, 40, 72, 68, 68", // 67,70
            "MEI, 70, 68, 82, 79, 55, 64, 75"  // Meia Lua (2.1.3)
    })
    void calculatesOverallFromPositionWeights(Position position, int pace, int shooting, int passing,
                                              int dribbling, int defending, int physical, int expected) {
        Attributes attributes = new Attributes(pace, shooting, passing, dribbling, defending, physical);

        assertThat(OverallCalculator.calculate(position, attributes)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"GOL", "ZAG", "LAT", "MEI", "ATA"})
    void weightsOfEachPositionSumToHundred(Position position) {
        Attributes allOnes = new Attributes(1, 1, 1, 1, 1, 1);

        assertThat(position.weightedSum(allOnes)).isEqualTo(100);
    }
}

package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SalePricingTest {

    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 11, 5, 16, 40, 0, 0, ZoneOffset.ofHours(-3));

    /**
     * Primeira tabela da RN16.
     */
    @ParameterizedTest(name = "lucro {0} → comissão {1}")
    @CsvSource({
            "3.00, 0.30",
            "3.85, 0.39",
            "10.00, 1.00",
            "10.01, 1.00",
            "15.00, 2.00",
            "-7.50, 0.00",
            "0.00, 0.00",
            "0.01, 0.00",   // 0,001 arredondado
            "0.05, 0.01"    // 0,005 arredondado para longe do zero
    })
    void progressiveCommissionRoundedOnce(String profit, String commission) {
        assertThat(SalePricing.commission(Money.of(profit))).isEqualTo(Money.of(commission));
    }

    /**
     * Segunda tabela da RN16: venda completa, de ponta a ponta.
     */
    @ParameterizedTest(name = "compra {0}, venda {1}")
    @CsvSource({
            // compra, venda, % multa, multa, lucro, comissão, crédito
            "37.00, 40.00, 10,  4.00,  3.00, 0.30, 35.70",
            "50.00, 42.50, 20,  8.50, -7.50, 0.00, 34.00",
            "26.15, 30.00,  5,  1.50,  3.85, 0.39, 28.11",
            "25.00, 25.00,  0,  0.00,  0.00, 0.00, 25.00",
            "60.00, 75.00, 20, 10.00, 15.00, 2.00, 63.00"
    })
    void saleTable(String purchase, String sale, int rate, String penalty, String profit,
                   String commission, String credit) {
        Money value = Money.of(sale);
        Money expectedPenalty = SalePricing.penalty(value, rate);
        Money expectedProfit = value.minus(Money.of(purchase));
        Money expectedCommission = SalePricing.commission(expectedProfit);

        MarketTransaction transaction = MarketTransaction.sale(1, 2, "Meia Lua", value, expectedCommission,
                expectedPenalty, expectedProfit, NOW);

        assertThat(expectedPenalty).isEqualTo(Money.of(penalty));
        assertThat(expectedProfit).isEqualTo(Money.of(profit));
        assertThat(expectedCommission).isEqualTo(Money.of(commission));
        assertThat(transaction.getCashChange()).isEqualTo(Money.of(credit));
        assertThat(transaction.getType()).isEqualTo(TransactionType.VENDA);
        assertThat(transaction.getFee()).isEqualTo(Money.of(commission));
    }

    @Test
    void penaltyIsRoundedAndCappedAt10() {
        assertThat(SalePricing.penalty(Money.of("8.79"), 5)).isEqualTo(Money.of("0.44"));  // 0,4395
        assertThat(SalePricing.penalty(Money.of("50.00"), 20)).isEqualTo(Money.of("10.00"));
        assertThat(SalePricing.penalty(Money.of("50.05"), 20)).isEqualTo(Money.of("10.00"));
        assertThat(SalePricing.penalty(Money.of("1.00"), 5)).isEqualTo(Money.of("0.05"));
    }

    @Test
    void contractIsRescindedWithPenaltyAndEndedWithout() {
        Contract withPenalty = Contract.start(2, 1, Money.of("37.00"), NOW.plusDays(117), NOW.minusDays(3));
        withPenalty.close(Money.of("4.00"), NOW);
        assertThat(withPenalty.getStatus()).isEqualTo(ContractStatus.RESCINDIDO);
        assertThat(withPenalty.getEndedAt()).isEqualTo(NOW);

        Contract fulfilled = Contract.start(2, 1, Money.of("25.00"), NOW.minusDays(1), NOW.minusDays(30));
        fulfilled.close(Money.ZERO, NOW);
        assertThat(fulfilled.getStatus()).isEqualTo(ContractStatus.ENCERRADO);
    }
}

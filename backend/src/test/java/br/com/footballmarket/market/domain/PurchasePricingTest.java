package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchasePricingTest {

    /**
     * Tabela da RN13.
     */
    @ParameterizedTest(name = "caixa {0}, valor {1}")
    @CsvSource({
            // caixa, valor de mercado, taxa, custo total, aceita
            "300.00, 28.00, 0.00, 28.00, true",
            "300.00, 30.00, 0.00, 30.00, true",
            "300.00, 30.01, 1.50, 31.51, true",
            "100.00, 47.62, 2.38, 50.00, true",
            "100.00, 48.00, 2.40, 50.40, false"
    })
    void feeAndHalfOfTheCashLimit(String cash, String marketValue, String fee, String totalCost, boolean accepted) {
        Money value = Money.of(marketValue);

        assertThat(PurchasePricing.fee(value)).isEqualTo(Money.of(fee));
        Money cost = value.plus(PurchasePricing.fee(value));
        assertThat(cost).isEqualTo(Money.of(totalCost));
        if (accepted) {
            assertThatCode(() -> PurchasePricing.requireAffordable(cost, Money.of(cash))).doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> PurchasePricing.requireAffordable(cost, Money.of(cash)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("O custo da compra (50,40) passa do limite de 50% do caixa (50,00).");
        }
    }

    @Test
    void halfOfTheCashIsNotRounded() {
        Money cash = Money.of("261.15"); // limite 130,575

        assertThatCode(() -> PurchasePricing.requireAffordable(Money.of("130.57"), cash)).doesNotThrowAnyException();
        assertThatThrownBy(() -> PurchasePricing.requireAffordable(Money.of("130.58"), cash))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("130,575");
    }

    @Test
    void feeExampleOfTheBuyEndpoint() {
        assertThat(PurchasePricing.fee(Money.of("37.00"))).isEqualTo(Money.of("1.85"));
    }
}

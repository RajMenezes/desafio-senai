package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoyaltyTest {

    private static final OffsetDateTime SALE = OffsetDateTime.of(2026, 11, 5, 16, 40, 0, 0, ZoneOffset.ofHours(-3));

    /**
     * Tabela da RN15: fim da fidelidade a partir do momento da venda → dias restantes e percentual.
     */
    @ParameterizedTest(name = "{0} → {1} dias, {2}%")
    @CsvSource({
            "-PT1M,         0,  0",
            "PT0S,          0,  0",
            "PT1S,          1,  5",
            "P10DT2H,      11,  5",
            "P30D,         30,  5",
            "P30DT1M,      31, 10",
            "P180D,       180, 10",
            "P180DT1M,    181, 20",
            "P200D,       200, 20",
            "P116DT17H35M, 117, 10"
    })
    void remainingDaysRoundUpAndSetThePenaltyRate(String untilSale, long days, int rate) {
        OffsetDateTime loyaltyUntil = SALE.plus(Duration.parse(untilSale));

        assertThat(Loyalty.remainingDays(loyaltyUntil, SALE)).isEqualTo(days);
        assertThat(Loyalty.penaltyRate(days)).isEqualTo(rate);
    }

    @Test
    void exampleOfTheSaleEndpoint() {
        OffsetDateTime loyaltyUntil = OffsetDateTime.of(2027, 3, 2, 10, 15, 0, 0, ZoneOffset.ofHours(-3));

        assertThat(Loyalty.remainingDays(loyaltyUntil, SALE)).isEqualTo(117);
    }

    @Test
    void endMustBeAfterThePurchaseAndAtMost1095DaysLater() {
        OffsetDateTime purchase = SALE.plusSeconds(30);

        assertThatCode(() -> Loyalty.requireValidEnd(purchase.plusMinutes(1), purchase)).doesNotThrowAnyException();
        assertThatCode(() -> Loyalty.requireValidEnd(purchase.plusDays(1095), purchase)).doesNotThrowAnyException();
        assertThatThrownBy(() -> Loyalty.requireValidEnd(purchase, purchase)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> Loyalty.requireValidEnd(purchase.minusMinutes(1), purchase))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> Loyalty.requireValidEnd(purchase.plusDays(1095).plusSeconds(1), purchase))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void comparesInstantsWhateverTheOffset() {
        OffsetDateTime sameInstantInUtc = SALE.withOffsetSameInstant(ZoneOffset.UTC);

        assertThat(Loyalty.remainingDays(sameInstantInUtc.plusDays(3), SALE)).isEqualTo(3);
    }
}
